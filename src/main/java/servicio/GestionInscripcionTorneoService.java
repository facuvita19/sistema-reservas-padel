package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import config.ConexionBD;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import negocio.EstadoInscripcionTorneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;

public class GestionInscripcionTorneoService {

    private final TorneoInscripcionDAO inscripcionDAO;
    private final TorneoCategoriaDAO categoriaDAO;
    private final ProveedorConexion proveedorConexion;

    public GestionInscripcionTorneoService() {
        this(
                new TorneoInscripcionDAOMySQL(),
                new TorneoCategoriaDAOMySQL(),
                ConexionBD::obtenerConexion);
    }

    public GestionInscripcionTorneoService(
            TorneoInscripcionDAO inscripcionDAO,
            TorneoCategoriaDAO categoriaDAO,
            ProveedorConexion proveedorConexion) {
        if (inscripcionDAO == null || categoriaDAO == null
                || proveedorConexion == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de gestion no pueden ser nulas.");
        }
        this.inscripcionDAO = inscripcionDAO;
        this.categoriaDAO = categoriaDAO;
        this.proveedorConexion = proveedorConexion;
    }

    public TorneoInscripcion confirmar(
            long inscripcionId,
            long usuarioGestionId,
            String observaciones) {
        return cambiarEstado(
                inscripcionId,
                EstadoInscripcionTorneo.CONFIRMADA,
                usuarioGestionId,
                observaciones);
    }

    public TorneoInscripcion enviarAListaEspera(
            long inscripcionId,
            long usuarioGestionId,
            String observaciones) {
        return cambiarEstado(
                inscripcionId,
                EstadoInscripcionTorneo.LISTA_ESPERA,
                usuarioGestionId,
                observaciones);
    }

    public TorneoInscripcion rechazar(
            long inscripcionId,
            long usuarioGestionId,
            String observaciones) {
        return cambiarEstado(
                inscripcionId,
                EstadoInscripcionTorneo.RECHAZADA,
                usuarioGestionId,
                observaciones);
    }

    public TorneoInscripcion cancelar(
            long inscripcionId,
            long usuarioGestionId,
            String observaciones) {
        return cambiarEstado(
                inscripcionId,
                EstadoInscripcionTorneo.CANCELADA,
                usuarioGestionId,
                observaciones);
    }

    public TorneoInscripcion cambiarEstado(
            long inscripcionId,
            EstadoInscripcionTorneo nuevoEstado,
            long usuarioGestionId,
            String observaciones) {
        validarEntrada(inscripcionId, nuevoEstado, usuarioGestionId);

        try (Connection conexion = proveedorConexion.obtener()) {
            conexion.setAutoCommit(false);
            try {
                TorneoInscripcion inscripcion = inscripcionDAO.buscar(
                        conexion, inscripcionId);
                if (inscripcion == null) {
                    throw new IllegalArgumentException(
                            "La inscripcion al torneo no existe.");
                }

                validarTransicion(inscripcion.getEstado(), nuevoEstado);
                validarParejaCompleta(inscripcion);

                if (nuevoEstado == EstadoInscripcionTorneo.CONFIRMADA) {
                    validarConfirmacion(conexion, inscripcion);
                }

                LocalDateTime ahora = LocalDateTime.now();
                inscripcion.setEstado(nuevoEstado);
                inscripcion.setUsuarioGestionId(usuarioGestionId);
                inscripcion.setObservacionesAdministrativas(
                        limpiarObservaciones(observaciones));
                inscripcion.setFechaConfirmacion(
                        nuevoEstado == EstadoInscripcionTorneo.CONFIRMADA
                                ? ahora
                                : null);
                inscripcion.setFechaCancelacion(
                        nuevoEstado == EstadoInscripcionTorneo.CANCELADA
                                ? ahora
                                : null);

                inscripcionDAO.guardar(conexion, inscripcion);
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
                    "No se pudo gestionar la inscripcion al torneo.",
                    exception);
        }
    }

    public TorneoInscripcion guardarObservaciones(
            long inscripcionId, long usuarioGestionId, String observaciones) {
        if (inscripcionId <= 0 || usuarioGestionId <= 0) {
            throw new IllegalArgumentException("La inscripción y el usuario son obligatorios.");
        }
        try (Connection conexion = proveedorConexion.obtener()) {
            conexion.setAutoCommit(false);
            try {
                TorneoInscripcion inscripcion = inscripcionDAO.buscar(conexion, inscripcionId);
                if (inscripcion == null) throw new IllegalArgumentException("La inscripción al torneo no existe.");
                inscripcion.setObservacionesAdministrativas(limpiarObservaciones(observaciones));
                inscripcion.setUsuarioGestionId(usuarioGestionId);
                inscripcionDAO.guardar(conexion, inscripcion);
                conexion.commit();
                return inscripcion;
            } catch (SQLException | RuntimeException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo guardar la observación interna.", exception);
        }
    }

    private void validarConfirmacion(
            Connection conexion,
            TorneoInscripcion inscripcion) {
        TorneoCategoria categoria = categoriaDAO.buscar(
                conexion, inscripcion.getTorneoCategoriaId());
        if (categoria == null || !categoria.isActivo()) {
            throw new IllegalArgumentException(
                    "La categoria del torneo no esta disponible.");
        }

        int confirmadas = inscripcionDAO.contarPorCategoriaYEstados(
                conexion,
                categoria.getId(),
                List.of(EstadoInscripcionTorneo.CONFIRMADA));
        if (confirmadas >= categoria.getCupoParejas()) {
            throw new CupoTorneoCompletoException();
        }

        for (TorneoInscripcionJugador jugador
                : inscripcion.getJugadores()) {
            if (jugador.getClienteId() != null
                    && inscripcionDAO.clienteParticipaEnCategoria(
                            conexion,
                            categoria.getId(),
                            jugador.getClienteId(),
                            inscripcion.getId())) {
                throw new IllegalArgumentException(
                        "Uno de los integrantes ya participa en otra pareja de esta categoria.");
            }
        }
    }

    private void validarParejaCompleta(TorneoInscripcion inscripcion) {
        if (!inscripcion.tieneParejaCompleta()) {
            throw new IllegalArgumentException(
                    "La inscripcion debe tener exactamente dos integrantes.");
        }
    }

    private void validarTransicion(
            EstadoInscripcionTorneo actual,
            EstadoInscripcionTorneo nuevo) {
        if (actual == null) {
            throw new IllegalArgumentException(
                    "La inscripcion no tiene un estado valido.");
        }
        if (actual == nuevo) {
            throw new IllegalArgumentException(
                    "La inscripcion ya tiene el estado seleccionado.");
        }
        boolean permitida = switch (actual) {
            case PENDIENTE -> nuevo == EstadoInscripcionTorneo.CONFIRMADA
                    || nuevo == EstadoInscripcionTorneo.LISTA_ESPERA
                    || nuevo == EstadoInscripcionTorneo.RECHAZADA
                    || nuevo == EstadoInscripcionTorneo.CANCELADA;
            case LISTA_ESPERA -> nuevo == EstadoInscripcionTorneo.CONFIRMADA
                    || nuevo == EstadoInscripcionTorneo.RECHAZADA
                    || nuevo == EstadoInscripcionTorneo.CANCELADA;
            case CONFIRMADA -> nuevo == EstadoInscripcionTorneo.CANCELADA;
            case RECHAZADA, CANCELADA -> false;
        };
        if (!permitida) {
            throw new IllegalArgumentException(
                    "El cambio de estado de la inscripcion no esta permitido.");
        }
    }

    private void validarEntrada(
            long inscripcionId,
            EstadoInscripcionTorneo nuevoEstado,
            long usuarioGestionId) {
        if (inscripcionId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la inscripcion debe ser positivo.");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                    "El nuevo estado es obligatorio.");
        }
        if (usuarioGestionId <= 0) {
            throw new IllegalArgumentException(
                    "El usuario de gestion es obligatorio.");
        }
    }

    private String limpiarObservaciones(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim();
        if (limpio.length() > 1000) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar 1000 caracteres.");
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

    @FunctionalInterface
    public interface ProveedorConexion {
        Connection obtener();
    }

    public static class CupoTorneoCompletoException
            extends IllegalArgumentException {

        public CupoTorneoCompletoException() {
            super("La categoria no tiene cupos disponibles.");
        }
    }
}
