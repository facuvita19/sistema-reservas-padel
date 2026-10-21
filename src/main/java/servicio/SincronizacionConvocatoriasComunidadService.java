package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

import config.ConexionBD;
import dao.ConvocatoriaComunidadDAO;
import dao.ConvocatoriaComunidadDAOMySQL;
import dao.EventoComunidadDAO;
import dao.EventoComunidadDAOMySQL;
import dao.SolicitudComunidadDAO;
import dao.SolicitudComunidadDAOMySQL;
import negocio.ConvocatoriaComunidad;
import negocio.EstadoConvocatoriaComunidad;
import negocio.EstadoReserva;
import negocio.EstadoSolicitudComunidad;
import negocio.EventoComunidad;
import negocio.TipoEventoComunidad;

public class SincronizacionConvocatoriasComunidadService {
    private final ConvocatoriaComunidadDAO convocatorias;
    private final SolicitudComunidadDAO solicitudes;
    private final EventoComunidadDAO eventos;
    private final ProveedorConexion conexiones;
    private final ProveedorFecha reloj;
    private final LectorReservaBloqueada reservas;

    public SincronizacionConvocatoriasComunidadService() {
        this(new ConvocatoriaComunidadDAOMySQL(),
                new SolicitudComunidadDAOMySQL(),
                new EventoComunidadDAOMySQL(),
                ConexionBD::obtenerConexion, LocalDateTime::now,
                SincronizacionConvocatoriasComunidadService::bloquearReserva);
    }

    public SincronizacionConvocatoriasComunidadService(
            ConvocatoriaComunidadDAO convocatorias,
            SolicitudComunidadDAO solicitudes,
            EventoComunidadDAO eventos,
            ProveedorConexion conexiones,
            ProveedorFecha reloj,
            LectorReservaBloqueada reservas) {
        this.convocatorias = Objects.requireNonNull(convocatorias);
        this.solicitudes = Objects.requireNonNull(solicitudes);
        this.eventos = Objects.requireNonNull(eventos);
        this.conexiones = Objects.requireNonNull(conexiones);
        this.reloj = Objects.requireNonNull(reloj);
        this.reservas = Objects.requireNonNull(reservas);
    }

    public ConvocatoriaComunidad reconciliar(long convocatoriaId) {
        if (convocatoriaId <= 0) {
            throw new IllegalArgumentException(
                    "La convocatoria debe ser positiva.");
        }
        try (Connection c = conexiones.obtener()) {
            c.setAutoCommit(false);
            try {
                ConvocatoriaComunidad convocatoria = convocatorias.buscar(
                        c, convocatoriaId, true);
                if (convocatoria == null) {
                    throw new ConvocatoriaNoEncontradaException();
                }
                if (!convocatoria.getEstado().esActiva()) {
                    c.commit();
                    return convocatoria;
                }
                ReservaBloqueada reserva = reservas.buscar(
                        c, convocatoria.getReservaId());
                LocalDateTime ahora = reloj.ahora();
                if (reserva == null
                        || reserva.estado() == EstadoReserva.CANCELADA
                        || reserva.estado() == EstadoReserva.AUSENTE) {
                    finalizar(c, convocatoria,
                            EstadoConvocatoriaComunidad.CANCELADA,
                            EstadoSolicitudComunidad.CERRADA_POR_CONVOCANTE,
                            TipoEventoComunidad.CONVOCATORIA_CANCELADA,
                            ahora, "RESERVA_CANCELADA");
                } else if (!reserva.inicio().isAfter(ahora)
                        || convocatoria.getFechaExpiracion() == null
                        || !convocatoria.getFechaExpiracion().isAfter(ahora)) {
                    finalizar(c, convocatoria,
                            EstadoConvocatoriaComunidad.VENCIDA,
                            EstadoSolicitudComunidad.VENCIDA,
                            TipoEventoComunidad.CONVOCATORIA_VENCIDA,
                            ahora, "TURNO_INICIADO_O_VENCIDO");
                }
                c.commit();
                return convocatoria;
            } catch (RuntimeException | SQLException e) {
                rollback(c, e);
                throw e;
            } finally {
                auto(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudo sincronizar la convocatoria.", e);
        }
    }

    private void finalizar(Connection c,
            ConvocatoriaComunidad convocatoria,
            EstadoConvocatoriaComunidad estado,
            EstadoSolicitudComunidad estadoSolicitudes,
            TipoEventoComunidad tipoEvento,
            LocalDateTime ahora,
            String causa) {
        convocatoria.setEstado(estado);
        convocatoria.setFechaCierre(ahora);
        convocatorias.guardar(c, convocatoria);
        int resueltas = solicitudes.resolverPendientes(c,
                convocatoria.getId(), estadoSolicitudes);
        EventoComunidad evento = new EventoComunidad();
        evento.setTipo(tipoEvento);
        evento.setConvocatoriaId(convocatoria.getId());
        evento.setActorClienteId(convocatoria.getClienteCreadorId());
        evento.setActorPerfilId(convocatoria.getPerfilCreadorId());
        evento.setDetalleJson("{\"causa\":\"" + causa
                + "\",\"solicitudesResueltas\":" + resueltas + "}");
        evento.setFechaEvento(ahora);
        eventos.guardar(c, evento);
    }

    private static ReservaBloqueada bloquearReserva(Connection c, long id)
            throws SQLException {
        String q = "SELECT id,estado,fecha,hora_inicio FROM reservas "
                + "WHERE id=? FOR UPDATE";
        try (PreparedStatement s = c.prepareStatement(q)) {
            s.setLong(1, id);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) return null;
                return new ReservaBloqueada(r.getLong("id"),
                        EstadoReserva.valueOf(r.getString("estado")),
                        LocalDateTime.of(r.getDate("fecha").toLocalDate(),
                                r.getTime("hora_inicio").toLocalTime()));
            }
        }
    }

    private void rollback(Connection c, Exception e) {
        try { c.rollback(); } catch (SQLException r) { e.addSuppressed(r); }
    }

    private void auto(Connection c) {
        try { c.setAutoCommit(true); } catch (SQLException ignored) { }
    }

    public record ReservaBloqueada(long id, EstadoReserva estado,
            LocalDateTime inicio) { }
    @FunctionalInterface public interface ProveedorConexion {
        Connection obtener() throws SQLException;
    }
    @FunctionalInterface public interface ProveedorFecha {
        LocalDateTime ahora();
    }
    @FunctionalInterface public interface LectorReservaBloqueada {
        ReservaBloqueada buscar(Connection c, long id) throws SQLException;
    }
    public static final class ConvocatoriaNoEncontradaException
            extends IllegalArgumentException {
        public ConvocatoriaNoEncontradaException() {
            super("La convocatoria no existe.");
        }
    }
}
