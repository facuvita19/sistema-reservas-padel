package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import config.ConexionBD;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoInscripcionJugadorDAO;
import dao.TorneoInscripcionJugadorDAOMySQL;
import negocio.EstadoInscripcionTorneo;
import negocio.OrigenInscripcionTorneo;
import negocio.TipoVinculacionTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import servicio.VinculacionClienteTelefonoService.ResultadoVinculacion;
import util.NormalizadorTelefono;

public class InscripcionTorneoWebService {

    private final TorneoDAO torneoDAO;
    private final TorneoCategoriaDAO categoriaDAO;
    private final TorneoInscripcionDAO inscripcionDAO;
    private final TorneoInscripcionJugadorDAO jugadorDAO;
    private final VinculacionClienteTelefonoService vinculacionService;
    private final ProveedorConexion proveedorConexion;

    public InscripcionTorneoWebService() {
        this(
                new TorneoDAOMySQL(),
                new TorneoCategoriaDAOMySQL(),
                new TorneoInscripcionDAOMySQL(),
                new TorneoInscripcionJugadorDAOMySQL(),
                new VinculacionClienteTelefonoService(),
                ConexionBD::obtenerConexion);
    }

    public InscripcionTorneoWebService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoInscripcionDAO inscripcionDAO,
            TorneoInscripcionJugadorDAO jugadorDAO,
            VinculacionClienteTelefonoService vinculacionService) {
        this(torneoDAO, categoriaDAO, inscripcionDAO, jugadorDAO,
                vinculacionService, ConexionBD::obtenerConexion);
    }

    public InscripcionTorneoWebService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoInscripcionDAO inscripcionDAO,
            TorneoInscripcionJugadorDAO jugadorDAO,
            VinculacionClienteTelefonoService vinculacionService,
            ProveedorConexion proveedorConexion) {
        if (torneoDAO == null || categoriaDAO == null
                || inscripcionDAO == null || jugadorDAO == null
                || vinculacionService == null
                || proveedorConexion == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de inscripcion no pueden ser nulas.");
        }
        this.torneoDAO = torneoDAO;
        this.categoriaDAO = categoriaDAO;
        this.inscripcionDAO = inscripcionDAO;
        this.jugadorDAO = jugadorDAO;
        this.vinculacionService = vinculacionService;
        this.proveedorConexion = proveedorConexion;
    }

    public TorneoInscripcion solicitar(SolicitudInscripcion solicitud) {
        validarSolicitud(solicitud);

        try (Connection conexion = proveedorConexion.obtener()) {
            conexion.setAutoCommit(false);
            try {
                TorneoCategoria categoria = categoriaDAO.buscar(
                        conexion, solicitud.torneoCategoriaId());
                if (categoria == null || !categoria.isActivo()) {
                    throw new IllegalArgumentException(
                            "La categoria seleccionada no esta disponible.");
                }

                Torneo torneo = torneoDAO.buscar(
                        conexion, categoria.getTorneoId());
                validarTorneoDisponible(torneo);

                ResultadoVinculacion vinculacionResponsable =
                        vinculacionService.buscar(
                                conexion, solicitud.responsable().telefono());
                ResultadoVinculacion vinculacionPareja =
                        vinculacionService.buscar(
                                conexion, solicitud.pareja().telefono());

                validarIntegrantesDistintos(
                        vinculacionResponsable,
                        vinculacionPareja);
                validarParticipacionesExistentes(
                        conexion,
                        categoria.getId(),
                        vinculacionResponsable,
                        vinculacionPareja);

                TorneoInscripcion inscripcion = new TorneoInscripcion();
                inscripcion.setTorneoCategoriaId(categoria.getId());
                inscripcion.setEstado(EstadoInscripcionTorneo.PENDIENTE);
                inscripcion.setOrigen(OrigenInscripcionTorneo.WEB);
                inscripcion.setResponsableClienteId(
                        vinculacionResponsable.clienteId());
                inscripcion.setPrecioInscripcion(
                        categoria.getPrecioInscripcion());
                inscripcion.setComentarios(
                        limpiarOpcional(solicitud.comentarios(), 500));
                inscripcionDAO.guardar(conexion, inscripcion);

                TorneoInscripcionJugador responsable = crearJugador(
                        inscripcion.getId(),
                        1,
                        true,
                        solicitud.responsable(),
                        vinculacionResponsable);
                TorneoInscripcionJugador pareja = crearJugador(
                        inscripcion.getId(),
                        2,
                        false,
                        solicitud.pareja(),
                        vinculacionPareja);

                jugadorDAO.guardar(conexion, responsable);
                jugadorDAO.guardar(conexion, pareja);
                inscripcion.setJugadores(List.of(responsable, pareja));

                if (!inscripcion.tieneParejaCompleta()) {
                    throw new IllegalStateException(
                            "La inscripcion debe contener dos integrantes.");
                }

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
                    "No se pudo registrar la solicitud de inscripcion.",
                    exception);
        }
    }

    private TorneoInscripcionJugador crearJugador(
            long inscripcionId,
            int orden,
            boolean responsable,
            DatosJugador datos,
            ResultadoVinculacion vinculacion) {
        TorneoInscripcionJugador jugador =
                new TorneoInscripcionJugador();
        jugador.setInscripcionId(inscripcionId);
        jugador.setOrdenIntegrante(orden);
        jugador.setClienteId(vinculacion.clienteId());
        jugador.setNombre(datos.nombre().trim());
        jugador.setApellido(datos.apellido().trim());
        jugador.setTelefono(datos.telefono().trim());
        jugador.setTelefonoNormalizado(
                vinculacion.telefonoNormalizado());
        jugador.setResponsable(responsable);
        jugador.setTipoVinculacion(
                vinculacion.tipoVinculacion());
        jugador.setRequiereRevision(
                vinculacion.requiereRevision());
        return jugador;
    }

    private void validarSolicitud(SolicitudInscripcion solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La solicitud de inscripcion es obligatoria.");
        }
        if (solicitud.torneoCategoriaId() <= 0) {
            throw new IllegalArgumentException(
                    "La categoria seleccionada no es valida.");
        }
        validarDatosJugador(solicitud.responsable(), "responsable");
        validarDatosJugador(solicitud.pareja(), "pareja");
        if (solicitud.comentarios() != null
                && solicitud.comentarios().trim().length() > 500) {
            throw new IllegalArgumentException(
                    "Los comentarios no pueden superar 500 caracteres.");
        }

        String telefonoResponsable = NormalizadorTelefono.normalizar(
                solicitud.responsable().telefono());
        String telefonoPareja = NormalizadorTelefono.normalizar(
                solicitud.pareja().telefono());
        if (telefonoResponsable.equals(telefonoPareja)) {
            throw new IllegalArgumentException(
                    "Los integrantes deben tener telefonos diferentes.");
        }
    }

    private void validarDatosJugador(
            DatosJugador datos,
            String descripcion) {
        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del " + descripcion + " son obligatorios.");
        }
        validarTexto(datos.nombre(), 100,
                "El nombre del " + descripcion + " es obligatorio.");
        validarTexto(datos.apellido(), 100,
                "El apellido del " + descripcion + " es obligatorio.");
        NormalizadorTelefono.normalizar(datos.telefono());
    }

    private void validarTexto(
            String valor,
            int maximo,
            String mensajeObligatorio) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeObligatorio);
        }
        if (valor.trim().length() > maximo) {
            throw new IllegalArgumentException(
                    "El dato no puede superar " + maximo + " caracteres.");
        }
    }

    private void validarTorneoDisponible(Torneo torneo) {
        if (torneo == null || !torneo.isActivo()) {
            throw new IllegalArgumentException(
                    "El torneo seleccionado no esta disponible.");
        }
        if (!torneo.inscripcionDisponible(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "La inscripcion al torneo no esta abierta.");
        }
    }

    private void validarIntegrantesDistintos(
            ResultadoVinculacion responsable,
            ResultadoVinculacion pareja) {
        if (responsable.estaVinculado()
                && pareja.estaVinculado()
                && responsable.clienteId().equals(pareja.clienteId())) {
            throw new IllegalArgumentException(
                    "Una persona no puede inscribirse como su propia pareja.");
        }
    }

    private void validarParticipacionesExistentes(
            Connection conexion,
            long categoriaId,
            ResultadoVinculacion responsable,
            ResultadoVinculacion pareja) {
        Set<Long> clientes = new HashSet<>();
        if (responsable.clienteId() != null) {
            clientes.add(responsable.clienteId());
        }
        if (pareja.clienteId() != null) {
            clientes.add(pareja.clienteId());
        }
        for (Long clienteId : clientes) {
            if (inscripcionDAO.clienteParticipaEnCategoria(
                    conexion, categoriaId, clienteId, null)) {
                throw new IllegalArgumentException(
                        "Uno de los integrantes ya participa en esta categoria.");
            }
        }
    }

    private String limpiarOpcional(String valor, int maximo) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(
                    "El texto supera el maximo permitido.");
        }
        return limpio;
    }

    private void rollbackSeguro(
            Connection conexion,
            Exception original) {
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

    public record SolicitudInscripcion(
            long torneoCategoriaId,
            DatosJugador responsable,
            DatosJugador pareja,
            String comentarios) {
    }

    public record DatosJugador(
            String nombre,
            String apellido,
            String telefono) {
    }

    @FunctionalInterface
    public interface ProveedorConexion {
        Connection obtener();
    }
}
