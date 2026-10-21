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
import dao.ParticipanteComunidadDAO;
import dao.ParticipanteComunidadDAOMySQL;
import dao.PerfilJugadorDAO;
import dao.PerfilJugadorDAOMySQL;
import dao.PreferenciasComunidadDAO;
import dao.PreferenciasComunidadDAOMySQL;
import dao.SolicitudComunidadDAO;
import dao.SolicitudComunidadDAOMySQL;
import negocio.ConvocatoriaComunidad;
import negocio.EstadoConvocatoriaComunidad;
import negocio.EstadoPerfilJugador;
import negocio.EstadoReserva;
import negocio.EstadoSolicitudComunidad;
import negocio.EventoComunidad;
import negocio.ParticipanteComunidad;
import negocio.PerfilJugador;
import negocio.PreferenciasComunidad;
import negocio.SolicitudComunidad;
import negocio.TipoEventoComunidad;

public class SolicitudComunidadService {
    private final ConvocatoriaComunidadDAO convocatorias;
    private final SolicitudComunidadDAO solicitudes;
    private final ParticipanteComunidadDAO participantes;
    private final PerfilJugadorDAO perfiles;
    private final PreferenciasComunidadDAO preferencias;
    private final EventoComunidadDAO eventos;
    private final ProveedorConexion conexiones;
    private final ProveedorFecha reloj;
    private final LectorReservaBloqueada reservas;

    public SolicitudComunidadService() {
        this(new ConvocatoriaComunidadDAOMySQL(),
                new SolicitudComunidadDAOMySQL(),
                new ParticipanteComunidadDAOMySQL(),
                new PerfilJugadorDAOMySQL(),
                new PreferenciasComunidadDAOMySQL(),
                new EventoComunidadDAOMySQL(),
                ConexionBD::obtenerConexion, LocalDateTime::now,
                SolicitudComunidadService::bloquearReserva);
    }

    public SolicitudComunidadService(ConvocatoriaComunidadDAO convocatorias,
            SolicitudComunidadDAO solicitudes,
            ParticipanteComunidadDAO participantes,
            PerfilJugadorDAO perfiles,
            PreferenciasComunidadDAO preferencias,
            EventoComunidadDAO eventos, ProveedorConexion conexiones,
            ProveedorFecha reloj, LectorReservaBloqueada reservas) {
        this.convocatorias = Objects.requireNonNull(convocatorias);
        this.solicitudes = Objects.requireNonNull(solicitudes);
        this.participantes = Objects.requireNonNull(participantes);
        this.perfiles = Objects.requireNonNull(perfiles);
        this.preferencias = Objects.requireNonNull(preferencias);
        this.eventos = Objects.requireNonNull(eventos);
        this.conexiones = Objects.requireNonNull(conexiones);
        this.reloj = Objects.requireNonNull(reloj);
        this.reservas = Objects.requireNonNull(reservas);
    }

    public SolicitudComunidad solicitar(long clienteId, long convocatoriaId,
            String mensaje) {
        validarIds(clienteId, convocatoriaId);
        String limpio = limitar(mensaje, 300);
        try (Connection c = conexiones.obtener()) {
            c.setAutoCommit(false);
            try {
                Contexto contexto = requerirContexto(c, clienteId,
                        convocatoriaId, true);
                SolicitudComunidad existente =
                        solicitudes.buscarPorConvocatoriaYPerfil(c,
                                convocatoriaId, contexto.perfil().getId(), true);
                if (existente != null) {
                    if (existente.getEstado().esPendiente()
                            || existente.getEstado()
                                    == EstadoSolicitudComunidad.ACEPTADA) {
                        c.commit();
                        return existente;
                    }
                    if (existente.getEstado()
                            == EstadoSolicitudComunidad.CANCELADA) {
                        LocalDateTime ahora = reloj.ahora();
                        solicitudes.reabrirCancelada(c, existente, limpio, ahora);
                        registrar(c, TipoEventoComunidad.SOLICITUD_RECIBIDA,
                                contexto, existente, clienteId, ahora,
                                "{\"reabierta\":true}");
                        c.commit();
                        return existente;
                    }
                    throw new SolicitudNoReabribleException(
                            existente.getEstado());
                }

                LocalDateTime ahora = reloj.ahora();
                SolicitudComunidad nueva = new SolicitudComunidad();
                nueva.setConvocatoriaId(convocatoriaId);
                nueva.setPerfilSolicitanteId(contexto.perfil().getId());
                nueva.setEstado(EstadoSolicitudComunidad.PENDIENTE);
                nueva.setMensaje(limpio);
                nueva.setFechaSolicitud(ahora);
                nueva.validar(contexto.convocatoria().getPerfilCreadorId());
                solicitudes.guardar(c, nueva);
                registrar(c, TipoEventoComunidad.SOLICITUD_RECIBIDA,
                        contexto, nueva, clienteId, ahora, null);
                c.commit();
                return nueva;
            } catch (RuntimeException | SQLException e) {
                rollback(c, e);
                throw e;
            } finally {
                auto(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo crear la solicitud.", e);
        }
    }

    public SolicitudComunidad obtenerPropia(long clienteId,
            long convocatoriaId) {
        validarIds(clienteId, convocatoriaId);
        try (Connection c = conexiones.obtener()) {
            PerfilJugador perfil = requerirPerfilActivo(c, clienteId);
            return solicitudes.buscarPorConvocatoriaYPerfil(c,
                    convocatoriaId, perfil.getId(), false);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudo consultar tu solicitud.", e);
        }
    }

    public SolicitudComunidad retirar(long clienteId, long solicitudId) {
        validarIds(clienteId, solicitudId);
        try (Connection c = conexiones.obtener()) {
            c.setAutoCommit(false);
            try {
                PerfilJugador perfil = requerirPerfilActivo(c, clienteId);
                SolicitudComunidad solicitud =
                        solicitudes.buscar(c, solicitudId, true);
                if (solicitud == null
                        || solicitud.getPerfilSolicitanteId()
                                != perfil.getId()) {
                    throw new SolicitudPropiaNoEncontradaException();
                }
                if (solicitud.getEstado()
                        == EstadoSolicitudComunidad.CANCELADA) {
                    c.commit();
                    return solicitud;
                }
                if (!solicitud.getEstado().esPendiente()) {
                    throw new SolicitudNoRetirableException();
                }
                ConvocatoriaComunidad convocatoria = convocatorias.buscar(c,
                        solicitud.getConvocatoriaId(), true);
                if (convocatoria == null) {
                    throw new ConvocatoriaNoEncontradaException();
                }
                LocalDateTime ahora = reloj.ahora();
                solicitud.setEstado(EstadoSolicitudComunidad.CANCELADA);
                solicitud.setFechaResolucion(ahora);
                solicitudes.guardar(c, solicitud);
                Contexto contexto = new Contexto(convocatoria, perfil, null);
                registrar(c, TipoEventoComunidad.SOLICITUD_CANCELADA,
                        contexto, solicitud, clienteId, ahora, null);
                c.commit();
                return solicitud;
            } catch (RuntimeException | SQLException e) {
                rollback(c, e);
                throw e;
            } finally {
                auto(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo retirar la solicitud.", e);
        }
    }

    private Contexto requerirContexto(Connection c, long clienteId,
            long convocatoriaId, boolean bloquear) throws SQLException {
        ConvocatoriaComunidad convocatoria = convocatorias.buscar(c,
                convocatoriaId, bloquear);
        if (convocatoria == null) throw new ConvocatoriaNoEncontradaException();
        PerfilJugador perfil = requerirPerfilHabilitado(c, clienteId);
        if (convocatoria.getPerfilCreadorId() == perfil.getId()
                || convocatoria.getClienteCreadorId() == clienteId) {
            throw new AutoSolicitudException();
        }
        if (!convocatoria.getEstado().esActiva()) {
            throw new ConvocatoriaNoDisponibleException();
        }
        LocalDateTime ahora = reloj.ahora();
        if (convocatoria.getFechaExpiracion() == null
                || !ahora.isBefore(convocatoria.getFechaExpiracion())) {
            throw new ConvocatoriaVencidaException();
        }
        ReservaBloqueada reserva = reservas.buscar(c,
                convocatoria.getReservaId());
        if (reserva == null
                || (reserva.estado() != EstadoReserva.PENDIENTE
                        && reserva.estado() != EstadoReserva.CONFIRMADA)
                || !reserva.inicio().isAfter(ahora)) {
            throw new ReservaNoDisponibleException();
        }
        if (participantes.buscarConfirmadoPorPerfil(c, convocatoriaId,
                perfil.getId(), bloquear) != null) {
            throw new PerfilYaParticipaException();
        }
        int confirmados = participantes.contarConfirmados(c, convocatoriaId);
        if (confirmados >= 4 || convocatoria.lugaresDisponibles() <= 0) {
            throw new ConvocatoriaSinCupoException();
        }
        return new Contexto(convocatoria, perfil, reserva);
    }

    private PerfilJugador requerirPerfilActivo(Connection c, long clienteId) {
        PerfilJugador perfil = perfiles.buscarPorCliente(c, clienteId);
        if (perfil == null || !perfil.isActivo()
                || perfil.getEstado() != EstadoPerfilJugador.DEFINITIVO
                || perfil.getClienteId() == null
                || perfil.getClienteId() != clienteId) {
            throw new PerfilNoHabilitadoException();
        }
        return perfil;
    }

    private PerfilJugador requerirPerfilHabilitado(Connection c,
            long clienteId) {
        PerfilJugador perfil = requerirPerfilActivo(c, clienteId);
        PreferenciasComunidad p = preferencias.buscar(c, clienteId);
        if (!perfil.isDisponibleParaJugar() || !perfil.isAceptaInvitaciones()
                || p == null || !p.isBuscarTurnos()) {
            throw new PerfilNoDisponibleParaTurnosException();
        }
        return perfil;
    }

    private void registrar(Connection c, TipoEventoComunidad tipo,
            Contexto contexto, SolicitudComunidad solicitud, long clienteId,
            LocalDateTime fecha, String detalle) {
        EventoComunidad evento = new EventoComunidad();
        evento.setTipo(tipo);
        evento.setConvocatoriaId(contexto.convocatoria().getId());
        evento.setSolicitudId(solicitud.getId());
        evento.setActorClienteId(clienteId);
        evento.setActorPerfilId(contexto.perfil().getId());
        evento.setDetalleJson(detalle);
        evento.setFechaEvento(fecha);
        eventos.guardar(c, evento);
    }

    private static ReservaBloqueada bloquearReserva(Connection c, long id)
            throws SQLException {
        String q = "SELECT id,estado,fecha,hora_inicio FROM reservas WHERE id=? FOR UPDATE";
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

    private String limitar(String valor, int maximo) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(
                    "El mensaje no puede superar 300 caracteres.");
        }
        return limpio;
    }

    private void validarIds(long primero, long segundo) {
        if (primero <= 0 || segundo <= 0) {
            throw new IllegalArgumentException(
                    "Los identificadores deben ser positivos.");
        }
    }

    private void rollback(Connection c, Exception e) {
        try { c.rollback(); } catch (SQLException r) { e.addSuppressed(r); }
    }

    private void auto(Connection c) {
        try { c.setAutoCommit(true); } catch (SQLException ignored) { }
    }

    private record Contexto(ConvocatoriaComunidad convocatoria,
            PerfilJugador perfil, ReservaBloqueada reserva) { }
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
    public static final class AutoSolicitudException
            extends IllegalArgumentException {
        public AutoSolicitudException() {
            super("No podés solicitar unirte a tu propia convocatoria.");
        }
    }
    public static final class PerfilNoHabilitadoException
            extends IllegalArgumentException {
        public PerfilNoHabilitadoException() {
            super("Tu cuenta necesita un perfil deportivo definitivo y activo.");
        }
    }
    public static final class PerfilNoDisponibleParaTurnosException
            extends IllegalStateException {
        public PerfilNoDisponibleParaTurnosException() {
            super("Activá tu disponibilidad para turnos e invitaciones antes de solicitar un lugar.");
        }
    }
    public static final class PerfilYaParticipaException
            extends IllegalStateException {
        public PerfilYaParticipaException() {
            super("Tu perfil ya participa en este turno.");
        }
    }
    public static final class ConvocatoriaNoDisponibleException
            extends IllegalStateException {
        public ConvocatoriaNoDisponibleException() {
            super("La convocatoria ya no acepta solicitudes.");
        }
    }
    public static final class ConvocatoriaVencidaException
            extends IllegalStateException {
        public ConvocatoriaVencidaException() {
            super("La convocatoria ya venció.");
        }
    }
    public static final class ReservaNoDisponibleException
            extends IllegalStateException {
        public ReservaNoDisponibleException() {
            super("La reserva ya no está disponible para Comunidad.");
        }
    }
    public static final class ConvocatoriaSinCupoException
            extends IllegalStateException {
        public ConvocatoriaSinCupoException() {
            super("El lugar acaba de cubrirse y la búsqueda ya está cerrada.");
        }
    }
    public static final class SolicitudPropiaNoEncontradaException
            extends IllegalArgumentException {
        public SolicitudPropiaNoEncontradaException() {
            super("La solicitud no existe o no pertenece a tu perfil.");
        }
    }
    public static final class SolicitudNoRetirableException
            extends IllegalStateException {
        public SolicitudNoRetirableException() {
            super("La solicitud ya no puede retirarse.");
        }
    }
    public static final class SolicitudNoReabribleException
            extends IllegalStateException {
        public SolicitudNoReabribleException(EstadoSolicitudComunidad estado) {
            super("La solicitud anterior no puede reabrirse desde el estado "
                    + estado.name() + ".");
        }
    }
}
