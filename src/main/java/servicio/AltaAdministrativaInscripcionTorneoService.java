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
import negocio.EstadoTorneo;
import negocio.OrigenInscripcionTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import servicio.VinculacionClienteTelefonoService.ResultadoVinculacion;
import util.NormalizadorTelefono;

public class AltaAdministrativaInscripcionTorneoService {
    private final TorneoDAO torneoDAO = new TorneoDAOMySQL();
    private final TorneoCategoriaDAO categoriaDAO = new TorneoCategoriaDAOMySQL();
    private final TorneoInscripcionDAO inscripcionDAO = new TorneoInscripcionDAOMySQL();
    private final TorneoInscripcionJugadorDAO jugadorDAO = new TorneoInscripcionJugadorDAOMySQL();
    private final VinculacionClienteTelefonoService vinculacion = new VinculacionClienteTelefonoService();

    public TorneoInscripcion crear(Solicitud solicitud) {
        validar(solicitud);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                TorneoCategoria categoria = categoriaDAO.buscar(
                        conexion, solicitud.categoriaId());
                if (categoria == null || !categoria.isActivo()) {
                    throw new IllegalArgumentException(
                            "La categoria seleccionada no esta disponible.");
                }
                Torneo torneo = torneoDAO.buscar(conexion, categoria.getTorneoId());
                validarTorneo(torneo);
                ResultadoVinculacion v1 = vinculacion.buscar(
                        conexion, solicitud.responsable().telefono());
                ResultadoVinculacion v2 = vinculacion.buscar(
                        conexion, solicitud.pareja().telefono());
                validarIntegrantes(conexion, categoria.getId(), v1, v2);
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
                inscripcion.setResponsableClienteId(v1.clienteId());
                inscripcion.setPrecioInscripcion(categoria.getPrecioInscripcion());
                inscripcion.setComentarios(limpiar(solicitud.comentarios(), 500));
                inscripcion.setObservacionesAdministrativas(
                        limpiar(solicitud.observaciones(), 1000));
                inscripcion.setUsuarioGestionId(solicitud.usuarioId());
                if (solicitud.estado() == EstadoInscripcionTorneo.CONFIRMADA) {
                    inscripcion.setFechaConfirmacion(LocalDateTime.now());
                }
                inscripcionDAO.guardar(conexion, inscripcion);
                TorneoInscripcionJugador j1 = jugador(inscripcion.getId(), 1,
                        true, solicitud.responsable(), v1);
                TorneoInscripcionJugador j2 = jugador(inscripcion.getId(), 2,
                        false, solicitud.pareja(), v2);
                jugadorDAO.guardar(conexion, j1);
                jugadorDAO.guardar(conexion, j2);
                inscripcion.setJugadores(List.of(j1, j2));
                conexion.commit();
                return inscripcion;
            } catch (SQLException | RuntimeException exception) {
                try { conexion.rollback(); }
                catch (SQLException rollback) { exception.addSuppressed(rollback); }
                throw exception;
            } finally {
                try { conexion.setAutoCommit(true); } catch (SQLException ignored) { }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo crear la inscripcion administrativa.", exception);
        }
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

    private void validar(Solicitud s) {
        if (s == null || s.categoriaId() <= 0 || s.usuarioId() <= 0) {
            throw new IllegalArgumentException(
                    "La categoria y el usuario son obligatorios.");
        }
        if (s.estado() != EstadoInscripcionTorneo.CONFIRMADA
                && s.estado() != EstadoInscripcionTorneo.PENDIENTE
                && s.estado() != EstadoInscripcionTorneo.LISTA_ESPERA) {
            throw new IllegalArgumentException("El estado inicial no es valido.");
        }
        validarJugador(s.responsable(), "responsable");
        validarJugador(s.pareja(), "segundo integrante");
        String t1 = NormalizadorTelefono.normalizar(s.responsable().telefono());
        String t2 = NormalizadorTelefono.normalizar(s.pareja().telefono());
        if (t1.equals(t2)) {
            throw new IllegalArgumentException(
                    "Los integrantes deben tener telefonos diferentes.");
        }
    }

    private void validarJugador(DatosJugador j, String nombre) {
        if (j == null || j.nombre() == null || j.nombre().isBlank()
                || j.apellido() == null || j.apellido().isBlank()) {
            throw new IllegalArgumentException(
                    "Los datos del " + nombre + " son obligatorios.");
        }
        if (j.nombre().trim().length() > 100
                || j.apellido().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre y apellido no pueden superar 100 caracteres.");
        }
        NormalizadorTelefono.normalizar(j.telefono());
    }

    private void validarIntegrantes(Connection c, long categoriaId,
            ResultadoVinculacion v1, ResultadoVinculacion v2) {
        if (v1.estaVinculado() && v2.estaVinculado()
                && v1.clienteId().equals(v2.clienteId())) {
            throw new IllegalArgumentException(
                    "Una persona no puede inscribirse como su propia pareja.");
        }
        Set<Long> ids = new HashSet<>();
        if (v1.clienteId() != null) ids.add(v1.clienteId());
        if (v2.clienteId() != null) ids.add(v2.clienteId());
        for (Long id : ids) {
            if (inscripcionDAO.clienteParticipaEnCategoria(
                    c, categoriaId, id, null)) {
                throw new IllegalArgumentException(
                        "Uno de los integrantes ya participa en esta categoria.");
            }
        }
    }

    private TorneoInscripcionJugador jugador(long inscripcionId, int orden,
            boolean responsable, DatosJugador datos, ResultadoVinculacion v) {
        TorneoInscripcionJugador j = new TorneoInscripcionJugador();
        j.setInscripcionId(inscripcionId);
        j.setOrdenIntegrante(orden);
        j.setClienteId(v.clienteId());
        j.setNombre(datos.nombre().trim());
        j.setApellido(datos.apellido().trim());
        j.setTelefono(datos.telefono().trim());
        j.setTelefonoNormalizado(v.telefonoNormalizado());
        j.setResponsable(responsable);
        j.setTipoVinculacion(v.tipoVinculacion());
        j.setRequiereRevision(v.requiereRevision());
        return j;
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

    public record DatosJugador(String nombre, String apellido, String telefono) { }
    public record Solicitud(long categoriaId, EstadoInscripcionTorneo estado,
            DatosJugador responsable, DatosJugador pareja, String comentarios,
            String observaciones, long usuarioId) { }
}
