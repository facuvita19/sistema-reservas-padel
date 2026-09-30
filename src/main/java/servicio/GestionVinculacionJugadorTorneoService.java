package servicio;

import java.sql.Connection;
import java.sql.SQLException;

import config.ConexionBD;
import dao.ClienteDAO;
import dao.ClienteDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoInscripcionJugadorDAO;
import dao.TorneoInscripcionJugadorDAOMySQL;
import negocio.Cliente;
import negocio.TipoVinculacionTorneo;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;

public class GestionVinculacionJugadorTorneoService {

    private final TorneoInscripcionJugadorDAO jugadorDAO;
    private final TorneoInscripcionDAO inscripcionDAO;
    private final ClienteDAO clienteDAO;
    private final ProveedorConexion proveedorConexion;

    public GestionVinculacionJugadorTorneoService() {
        this(
                new TorneoInscripcionJugadorDAOMySQL(),
                new TorneoInscripcionDAOMySQL(),
                new ClienteDAOMySQL(),
                ConexionBD::obtenerConexion);
    }

    public GestionVinculacionJugadorTorneoService(
            TorneoInscripcionJugadorDAO jugadorDAO,
            TorneoInscripcionDAO inscripcionDAO,
            ClienteDAO clienteDAO,
            ProveedorConexion proveedorConexion) {
        if (jugadorDAO == null || inscripcionDAO == null
                || clienteDAO == null || proveedorConexion == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de vinculacion no pueden ser nulas.");
        }
        this.jugadorDAO = jugadorDAO;
        this.inscripcionDAO = inscripcionDAO;
        this.clienteDAO = clienteDAO;
        this.proveedorConexion = proveedorConexion;
    }

    public TorneoInscripcionJugador vincularManualmente(
            long jugadorId,
            long clienteId) {
        validarId(jugadorId, "El ID del integrante debe ser positivo.");
        validarId(clienteId, "El ID del cliente debe ser positivo.");

        try (Connection conexion = proveedorConexion.obtener()) {
            conexion.setAutoCommit(false);
            try {
                TorneoInscripcionJugador jugador = buscarJugador(
                        conexion, jugadorId);
                Cliente cliente = clienteDAO.buscar(conexion, clienteId);
                if (cliente == null) {
                    throw new IllegalArgumentException(
                            "El cliente seleccionado no existe.");
                }
                if (!cliente.isActivo()) {
                    throw new IllegalArgumentException(
                            "El cliente seleccionado no esta activo.");
                }

                TorneoInscripcion inscripcion = buscarInscripcion(
                        conexion, jugador.getInscripcionId());
                validarIntegrante(inscripcion, jugadorId);
                validarClienteEnPareja(inscripcion, jugadorId, clienteId);

                if (inscripcionDAO.clienteParticipaEnCategoria(
                        conexion,
                        inscripcion.getTorneoCategoriaId(),
                        clienteId,
                        inscripcion.getId())) {
                    throw new IllegalArgumentException(
                            "El cliente ya participa en otra pareja de esta categoria.");
                }

                jugador.setClienteId(clienteId);
                jugador.setTipoVinculacion(
                        TipoVinculacionTorneo.MANUAL);
                jugador.setRequiereRevision(false);
                jugadorDAO.guardar(conexion, jugador);

                if (jugador.isResponsable()) {
                    inscripcion.setResponsableClienteId(clienteId);
                    inscripcionDAO.guardar(conexion, inscripcion);
                }

                conexion.commit();
                return jugador;
            } catch (RuntimeException | SQLException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo vincular el cliente con el integrante.",
                    exception);
        }
    }

    public TorneoInscripcionJugador desvincular(long jugadorId) {
        validarId(jugadorId, "El ID del integrante debe ser positivo.");

        try (Connection conexion = proveedorConexion.obtener()) {
            conexion.setAutoCommit(false);
            try {
                TorneoInscripcionJugador jugador = buscarJugador(
                        conexion, jugadorId);
                TorneoInscripcion inscripcion = buscarInscripcion(
                        conexion, jugador.getInscripcionId());
                validarIntegrante(inscripcion, jugadorId);

                jugador.setClienteId(null);
                jugador.setTipoVinculacion(
                        TipoVinculacionTorneo.SIN_VINCULAR);
                jugador.setRequiereRevision(false);
                jugadorDAO.guardar(conexion, jugador);

                if (jugador.isResponsable()) {
                    inscripcion.setResponsableClienteId(null);
                    inscripcionDAO.guardar(conexion, inscripcion);
                }

                conexion.commit();
                return jugador;
            } catch (RuntimeException | SQLException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo desvincular el cliente del integrante.",
                    exception);
        }
    }

    private TorneoInscripcionJugador buscarJugador(
            Connection conexion,
            long jugadorId) {
        TorneoInscripcionJugador jugador = jugadorDAO.buscar(
                conexion, jugadorId);
        if (jugador == null) {
            throw new IllegalArgumentException(
                    "El integrante del torneo no existe.");
        }
        return jugador;
    }

    private TorneoInscripcion buscarInscripcion(
            Connection conexion,
            long inscripcionId) {
        TorneoInscripcion inscripcion = inscripcionDAO.buscar(
                conexion, inscripcionId);
        if (inscripcion == null) {
            throw new IllegalArgumentException(
                    "La inscripcion al torneo no existe.");
        }
        return inscripcion;
    }

    private void validarIntegrante(
            TorneoInscripcion inscripcion,
            long jugadorId) {
        boolean pertenece = inscripcion.getJugadores().stream()
                .anyMatch(jugador -> jugador.getId() == jugadorId);
        if (!pertenece) {
            throw new IllegalArgumentException(
                    "El integrante no pertenece a la inscripcion indicada.");
        }
    }

    private void validarClienteEnPareja(
            TorneoInscripcion inscripcion,
            long jugadorId,
            long clienteId) {
        boolean repetido = inscripcion.getJugadores().stream()
                .filter(jugador -> jugador.getId() != jugadorId)
                .anyMatch(jugador -> jugador.getClienteId() != null
                        && jugador.getClienteId() == clienteId);
        if (repetido) {
            throw new IllegalArgumentException(
                    "Los dos integrantes no pueden vincularse al mismo cliente.");
        }
    }

    private void validarId(long id, String mensaje) {
        if (id <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
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
}