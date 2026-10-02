package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import config.ConexionBD;
import dao.ClienteDAO;
import dao.ClienteDAOMySQL;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoInscripcionJugadorDAO;
import dao.TorneoInscripcionJugadorDAOMySQL;
import negocio.Cliente;
import negocio.EstadoInscripcionTorneo;
import negocio.EstadoTorneo;
import negocio.OrigenInscripcionTorneo;
import negocio.TipoVinculacionTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import servicio.VinculacionClienteTelefonoService.ResultadoVinculacion;
import util.NormalizadorTelefono;

public class AltaAdministrativaInscripcionTorneoService {
    private final TorneoDAO torneoDAO;
    private final TorneoCategoriaDAO categoriaDAO;
    private final TorneoInscripcionDAO inscripcionDAO;
    private final TorneoInscripcionJugadorDAO jugadorDAO;
    private final ClienteDAO clienteDAO;
    private final VinculacionClienteTelefonoService vinculacion;
    private final ProveedorConexion proveedorConexion;

    public AltaAdministrativaInscripcionTorneoService() {
        this(new TorneoDAOMySQL(), new TorneoCategoriaDAOMySQL(),
                new TorneoInscripcionDAOMySQL(),
                new TorneoInscripcionJugadorDAOMySQL(),
                new ClienteDAOMySQL(),
                new VinculacionClienteTelefonoService(),
                ConexionBD::obtenerConexion);
    }

    public AltaAdministrativaInscripcionTorneoService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoInscripcionDAO inscripcionDAO,
            TorneoInscripcionJugadorDAO jugadorDAO,
            ClienteDAO clienteDAO,
            VinculacionClienteTelefonoService vinculacion,
            ProveedorConexion proveedorConexion) {
        if (torneoDAO == null || categoriaDAO == null
                || inscripcionDAO == null || jugadorDAO == null
                || clienteDAO == null || vinculacion == null
                || proveedorConexion == null) {
            throw new IllegalArgumentException(
                    "Las dependencias del alta administrativa son obligatorias.");
        }
        this.torneoDAO = torneoDAO;
        this.categoriaDAO = categoriaDAO;
        this.inscripcionDAO = inscripcionDAO;
        this.jugadorDAO = jugadorDAO;
        this.clienteDAO = clienteDAO;
        this.vinculacion = vinculacion;
        this.proveedorConexion = proveedorConexion;
    }

    public TorneoInscripcion crear(Solicitud solicitud) {
        validar(solicitud);
        try (Connection conexion = proveedorConexion.obtener()) {
            conexion.setAutoCommit(false);
            try {
                TorneoCategoria categoria = categoriaDAO.buscar(
                        conexion, solicitud.categoriaId());
                if (categoria == null || !categoria.isActivo()) {
                    throw new IllegalArgumentException(
                            "La categoria seleccionada no esta disponible.");
                }
                Torneo torneo = torneoDAO.buscar(
                        conexion, categoria.getTorneoId());
                validarTorneo(torneo);

                JugadorResuelto responsable = resolver(
                        conexion, solicitud.responsable());
                JugadorResuelto pareja = resolver(
                        conexion, solicitud.pareja());
                validarIntegrantes(conexion, categoria.getId(),
                        responsable, pareja);

                if (solicitud.estado() == EstadoInscripcionTorneo.CONFIRMADA) {
                    int confirmadas = inscripcionDAO.contarPorCategoriaYEstados(
                            conexion, categoria.getId(),
                            List.of(EstadoInscripcionTorneo.CONFIRMADA));
                    if (confirmadas >= categoria.getCupoParejas()) {
                        throw new IllegalArgumentException(
                                "La categoria no tiene cupos disponibles.");
                    }
                }

                TorneoInscripcion inscripcion = new TorneoInscripcion();
                inscripcion.setTorneoCategoriaId(categoria.getId());
                inscripcion.setEstado(solicitud.estado());
                inscripcion.setOrigen(OrigenInscripcionTorneo.ADMINISTRACION);
                inscripcion.setResponsableClienteId(
                        responsable.vinculacion().clienteId());
                inscripcion.setPrecioInscripcion(
                        categoria.getPrecioInscripcion());
                inscripcion.setComentarios(
                        limpiar(solicitud.comentarios(), 500));
                inscripcion.setObservacionesAdministrativas(
                        limpiar(solicitud.observaciones(), 1000));
                inscripcion.setUsuarioGestionId(solicitud.usuarioId());
                if (solicitud.estado() == EstadoInscripcionTorneo.CONFIRMADA) {
                    inscripcion.setFechaConfirmacion(LocalDateTime.now());
                }
                inscripcionDAO.guardar(conexion, inscripcion);

                TorneoInscripcionJugador j1 = jugador(
                        inscripcion.getId(), 1, true, responsable);
                TorneoInscripcionJugador j2 = jugador(
                        inscripcion.getId(), 2, false, pareja);
                jugadorDAO.guardar(conexion, j1);
                jugadorDAO.guardar(conexion, j2);
                inscripcion.setJugadores(List.of(j1, j2));
                conexion.commit();
                return inscripcion;
            } catch (SQLException | RuntimeException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo crear la inscripcion administrativa.",
                    exception);
        }
    }

    private JugadorResuelto resolver(
            Connection conexion, DatosJugador datos) {
        if (datos.clienteId() == null) {
            ResultadoVinculacion resultado = vinculacion.buscar(
                    conexion, datos.telefono());
            return new JugadorResuelto(
                    datos.nombre().trim(), datos.apellido().trim(),
                    datos.telefono().trim(), resultado);
        }
        if (datos.clienteId() <= 0) {
            throw new IllegalArgumentException(
                    "El cliente seleccionado no es valido.");
        }
        Cliente cliente = clienteDAO.buscar(conexion, datos.clienteId());
        if (cliente == null || !cliente.isActivo()) {
            throw new IllegalArgumentException(
                    "El cliente seleccionado no existe o esta inactivo.");
        }
        String telefono = cliente.getTelefono();
        String normalizado = NormalizadorTelefono.normalizar(telefono);
        ResultadoVinculacion manual = new ResultadoVinculacion(
                VinculacionClienteTelefonoService.EstadoVinculacion.ENCONTRADO,
                normalizado, cliente, 1);
        return new JugadorResuelto(
                cliente.getNombre().trim(), cliente.getApellido().trim(),
                telefono.trim(), manual, TipoVinculacionTorneo.MANUAL);
    }

    private void validarTorneo(Torneo torneo) {
        if (torneo == null || !torneo.isActivo()) {
            throw new IllegalArgumentException("El torneo no esta disponible.");
        }
        EstadoTorneo estado = torneo.getEstado();
        if (estado != EstadoTorneo.PUBLICADO
                && estado != EstadoTorneo.INSCRIPCION_ABIERTA
                && estado != EstadoTorneo.INSCRIPCION_CERRADA) {
            throw new IllegalArgumentException(
                    "Solo pueden agregarse parejas a un torneo publicado o con inscripciones abiertas o cerradas.");
        }
    }

    private void validar(Solicitud solicitud) {
        if (solicitud == null || solicitud.categoriaId() <= 0
                || solicitud.usuarioId() <= 0) {
            throw new IllegalArgumentException(
                    "La categoria y el usuario son obligatorios.");
        }
        if (solicitud.estado() != EstadoInscripcionTorneo.CONFIRMADA
                && solicitud.estado() != EstadoInscripcionTorneo.PENDIENTE
                && solicitud.estado() != EstadoInscripcionTorneo.LISTA_ESPERA) {
            throw new IllegalArgumentException(
                    "El estado inicial no es valido.");
        }
        validarJugador(solicitud.responsable(), "responsable");
        validarJugador(solicitud.pareja(), "segundo integrante");
        if (solicitud.responsable().clienteId() != null
                && solicitud.responsable().clienteId().equals(
                        solicitud.pareja().clienteId())) {
            throw new IllegalArgumentException(
                    "No se puede seleccionar al mismo cliente para ambos integrantes.");
        }
        String telefono1 = NormalizadorTelefono.normalizar(
                solicitud.responsable().telefono());
        String telefono2 = NormalizadorTelefono.normalizar(
                solicitud.pareja().telefono());
        if (telefono1.equals(telefono2)) {
            throw new IllegalArgumentException(
                    "Los integrantes deben tener telefonos diferentes.");
        }
    }

    private void validarJugador(DatosJugador jugador, String descripcion) {
        if (jugador == null || jugador.nombre() == null
                || jugador.nombre().isBlank() || jugador.apellido() == null
                || jugador.apellido().isBlank()) {
            throw new IllegalArgumentException(
                    "Los datos del " + descripcion + " son obligatorios.");
        }
        if (jugador.nombre().trim().length() > 100
                || jugador.apellido().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre y apellido no pueden superar 100 caracteres.");
        }
        NormalizadorTelefono.normalizar(jugador.telefono());
    }

    private void validarIntegrantes(
            Connection conexion,
            long categoriaId,
            JugadorResuelto responsable,
            JugadorResuelto pareja) {
        Long id1 = responsable.vinculacion().clienteId();
        Long id2 = pareja.vinculacion().clienteId();
        if (id1 != null && id1.equals(id2)) {
            throw new IllegalArgumentException(
                    "Una persona no puede inscribirse como su propia pareja.");
        }
        Set<Long> ids = new HashSet<>();
        if (id1 != null) ids.add(id1);
        if (id2 != null) ids.add(id2);
        for (Long id : ids) {
            if (inscripcionDAO.clienteParticipaEnCategoria(
                    conexion, categoriaId, id, null)) {
                throw new IllegalArgumentException(
                        "Uno de los integrantes ya participa en esta categoria.");
            }
        }
    }

    private TorneoInscripcionJugador jugador(
            long inscripcionId,
            int orden,
            boolean responsable,
            JugadorResuelto datos) {
        TorneoInscripcionJugador jugador = new TorneoInscripcionJugador();
        jugador.setInscripcionId(inscripcionId);
        jugador.setOrdenIntegrante(orden);
        jugador.setClienteId(datos.vinculacion().clienteId());
        jugador.setNombre(datos.nombre());
        jugador.setApellido(datos.apellido());
        jugador.setTelefono(datos.telefono());
        jugador.setTelefonoNormalizado(
                datos.vinculacion().telefonoNormalizado());
        jugador.setResponsable(responsable);
        jugador.setTipoVinculacion(datos.tipoVinculacion());
        jugador.setRequiereRevision(
                datos.vinculacion().requiereRevision());
        return jugador;
    }

    private String limpiar(String valor, int maximo) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(
                    "El texto no puede superar " + maximo + " caracteres.");
        }
        return limpio;
    }

    private void rollbackSeguro(Connection conexion, Exception original) {
        try {
            conexion.rollback();
        } catch (SQLException rollback) {
            original.addSuppressed(rollback);
        }
    }

    private void restaurarAutoCommit(Connection conexion) {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    public record DatosJugador(
            Long clienteId,
            String nombre,
            String apellido,
            String telefono) {
        public DatosJugador(String nombre, String apellido, String telefono) {
            this(null, nombre, apellido, telefono);
        }
    }

    public record Solicitud(
            long categoriaId,
            EstadoInscripcionTorneo estado,
            DatosJugador responsable,
            DatosJugador pareja,
            String comentarios,
            String observaciones,
            long usuarioId) {
    }

    private record JugadorResuelto(
            String nombre,
            String apellido,
            String telefono,
            ResultadoVinculacion vinculacion,
            TipoVinculacionTorneo tipoVinculacion) {
        JugadorResuelto(String nombre, String apellido, String telefono,
                ResultadoVinculacion vinculacion) {
            this(nombre, apellido, telefono, vinculacion,
                    vinculacion.tipoVinculacion());
        }
    }

    @FunctionalInterface
    public interface ProveedorConexion {
        Connection obtener() throws SQLException;
    }
}
