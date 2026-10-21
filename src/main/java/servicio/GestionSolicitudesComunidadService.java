package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import api.publica.GestionSolicitudesComunidadDTO;
import api.publica.GestionSolicitudesComunidadDTO.Listado;
import api.publica.GestionSolicitudesComunidadDTO.Perfil;
import api.publica.GestionSolicitudesComunidadDTO.Resultado;
import config.ConexionBD;
import dao.ConvocatoriaComunidadDAO;
import dao.ConvocatoriaComunidadDAOMySQL;
import dao.EventoComunidadDAO;
import dao.EventoComunidadDAOMySQL;
import dao.ParticipanteComunidadDAO;
import dao.ParticipanteComunidadDAOMySQL;
import dao.SolicitudComunidadDAO;
import dao.SolicitudComunidadDAOMySQL;
import negocio.ConvocatoriaComunidad;
import negocio.EstadoConvocatoriaComunidad;
import negocio.EstadoParticipanteComunidad;
import negocio.EstadoSolicitudComunidad;
import negocio.EventoComunidad;
import negocio.MotivoCierreConvocatoria;
import negocio.OrigenParticipanteComunidad;
import negocio.ParticipanteComunidad;
import negocio.SolicitudComunidad;
import negocio.TipoEventoComunidad;
import negocio.TipoParticipanteComunidad;

public class GestionSolicitudesComunidadService {
    private final ConvocatoriaComunidadDAO convocatorias;
    private final SolicitudComunidadDAO solicitudes;
    private final ParticipanteComunidadDAO participantes;
    private final EventoComunidadDAO eventos;
    private final ProveedorConexion conexiones;
    private final ProveedorFecha reloj;
    private final LectorPerfilPublico perfiles;

    public GestionSolicitudesComunidadService() {
        this(new ConvocatoriaComunidadDAOMySQL(),
                new SolicitudComunidadDAOMySQL(),
                new ParticipanteComunidadDAOMySQL(),
                new EventoComunidadDAOMySQL(),
                ConexionBD::obtenerConexion, LocalDateTime::now,
                GestionSolicitudesComunidadService::leerPerfil);
    }

    public GestionSolicitudesComunidadService(
            ConvocatoriaComunidadDAO convocatorias,
            SolicitudComunidadDAO solicitudes,
            ParticipanteComunidadDAO participantes,
            EventoComunidadDAO eventos,
            ProveedorConexion conexiones,
            ProveedorFecha reloj,
            LectorPerfilPublico perfiles) {
        this.convocatorias = Objects.requireNonNull(convocatorias);
        this.solicitudes = Objects.requireNonNull(solicitudes);
        this.participantes = Objects.requireNonNull(participantes);
        this.eventos = Objects.requireNonNull(eventos);
        this.conexiones = Objects.requireNonNull(conexiones);
        this.reloj = Objects.requireNonNull(reloj);
        this.perfiles = Objects.requireNonNull(perfiles);
    }

    public Listado listar(long clienteId, long convocatoriaId) {
        validarIds(clienteId, convocatoriaId);
        try (Connection c = conexiones.obtener()) {
            ConvocatoriaComunidad convocatoria = requerirConvocatoria(
                    c, convocatoriaId, false);
            requerirPropietario(convocatoria, clienteId);
            List<GestionSolicitudesComunidadDTO.Solicitud> items =
                    new ArrayList<>();
            for (SolicitudComunidad solicitud
                    : solicitudes.listarPorConvocatoria(c, convocatoriaId)) {
                items.add(dto(c, solicitud));
            }
            return listado(convocatoria, items);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudieron consultar las solicitudes recibidas.", e);
        }
    }

    public Resultado aceptar(long clienteId, long solicitudId) {
        validarIds(clienteId, solicitudId);
        try (Connection c = conexiones.obtener()) {
            c.setAutoCommit(false);
            try {
                SolicitudComunidad solicitud = requerirSolicitud(
                        c, solicitudId, true);
                ConvocatoriaComunidad convocatoria = requerirConvocatoria(
                        c, solicitud.getConvocatoriaId(), true);
                requerirPropietario(convocatoria, clienteId);
                if (solicitud.getEstado()
                        == EstadoSolicitudComunidad.ACEPTADA) {
                    c.commit();
                    return resultado(c, convocatoria, solicitud);
                }
                requerirPendiente(solicitud);
                requerirActiva(convocatoria);
                LocalDateTime ahora = reloj.ahora();
                int confirmados = participantes.contarConfirmados(
                        c, convocatoria.getId());
                if (confirmados >= 4
                        || convocatoria.lugaresDisponibles() <= 0) {
                    throw new SinCupoParaAceptarException();
                }
                if (participantes.buscarConfirmadoPorPerfil(c,
                        convocatoria.getId(),
                        solicitud.getPerfilSolicitanteId(), true) != null) {
                    throw new PerfilYaParticipaException();
                }

                ParticipanteComunidad participante = new ParticipanteComunidad();
                participante.setConvocatoriaId(convocatoria.getId());
                participante.setPerfilJugadorId(
                        solicitud.getPerfilSolicitanteId());
                participante.setSolicitudOrigenId(solicitud.getId());
                participante.setTipo(TipoParticipanteComunidad.PERFIL);
                participante.setEstado(EstadoParticipanteComunidad.CONFIRMADO);
                participante.setOrigen(OrigenParticipanteComunidad.COMUNIDAD);
                participante.setDescripcionPublica(
                        "Jugador incorporado desde Comunidad");
                participante.setFechaConfirmacion(ahora);
                participantes.guardar(c, participante);

                solicitud.setEstado(EstadoSolicitudComunidad.ACEPTADA);
                solicitud.setFechaResolucion(ahora);
                solicitudes.guardar(c, solicitud);
                registrar(c, TipoEventoComunidad.SOLICITUD_ACEPTADA,
                        convocatoria, solicitud, clienteId,
                        convocatoria.getPerfilCreadorId(), ahora, null);
                registrar(c, TipoEventoComunidad.PARTICIPANTE_CONFIRMADO,
                        convocatoria, solicitud, clienteId,
                        solicitud.getPerfilSolicitanteId(), ahora,
                        "{\"origen\":\"COMUNIDAD\"}");

                confirmados = participantes.contarConfirmados(
                        c, convocatoria.getId());
                convocatoria.setParticipantesConfirmados(confirmados);
                boolean completa = confirmados >= 4;
                if (completa) {
                    convocatoria.setEstado(
                            EstadoConvocatoriaComunidad.COMPLETA);
                    convocatoria.setMotivoCierre(
                            MotivoCierreConvocatoria.SOLICITUD_ACEPTADA);
                    convocatoria.setFechaCierre(ahora);
                    convocatorias.guardar(c, convocatoria);
                    solicitudes.resolverPendientes(c, convocatoria.getId(),
                            EstadoSolicitudComunidad.CERRADA_POR_CUPO);
                    registrar(c, TipoEventoComunidad.CONVOCATORIA_COMPLETA,
                            convocatoria, solicitud, clienteId,
                            convocatoria.getPerfilCreadorId(), ahora,
                            "{\"participantesConfirmados\":"
                                    + confirmados + "}");
                } else {
                    convocatorias.guardar(c, convocatoria);
                }
                c.commit();
                return resultado(c, convocatoria, solicitud);
            } catch (RuntimeException | SQLException e) {
                rollback(c, e);
                throw e;
            } finally {
                auto(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudo aceptar la solicitud.", e);
        }
    }

    public Resultado rechazar(long clienteId, long solicitudId) {
        validarIds(clienteId, solicitudId);
        try (Connection c = conexiones.obtener()) {
            c.setAutoCommit(false);
            try {
                SolicitudComunidad solicitud = requerirSolicitud(
                        c, solicitudId, true);
                ConvocatoriaComunidad convocatoria = requerirConvocatoria(
                        c, solicitud.getConvocatoriaId(), true);
                requerirPropietario(convocatoria, clienteId);
                if (solicitud.getEstado()
                        == EstadoSolicitudComunidad.RECHAZADA) {
                    c.commit();
                    return resultado(c, convocatoria, solicitud);
                }
                requerirPendiente(solicitud);
                requerirActiva(convocatoria);
                LocalDateTime ahora = reloj.ahora();
                solicitud.setEstado(EstadoSolicitudComunidad.RECHAZADA);
                solicitud.setFechaResolucion(ahora);
                solicitudes.guardar(c, solicitud);
                registrar(c, TipoEventoComunidad.SOLICITUD_RECHAZADA,
                        convocatoria, solicitud, clienteId,
                        convocatoria.getPerfilCreadorId(), ahora, null);
                c.commit();
                return resultado(c, convocatoria, solicitud);
            } catch (RuntimeException | SQLException e) {
                rollback(c, e);
                throw e;
            } finally {
                auto(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudo rechazar la solicitud.", e);
        }
    }

    private Resultado resultado(Connection c,
            ConvocatoriaComunidad convocatoria,
            SolicitudComunidad solicitud) throws SQLException {
        int confirmados = participantes.contarConfirmados(
                c, convocatoria.getId());
        return new Resultado(dto(c, solicitud),
                convocatoria.getEstado().name(), confirmados,
                convocatoria.lugaresDisponibles(),
                convocatoria.getEstado()
                        == EstadoConvocatoriaComunidad.COMPLETA);
    }

    private Listado listado(ConvocatoriaComunidad convocatoria,
            List<GestionSolicitudesComunidadDTO.Solicitud> items) {
        return new Listado(convocatoria.getId(),
                convocatoria.getEstado().name(),
                convocatoria.getParticipantesConfirmados(),
                convocatoria.lugaresDisponibles(), items);
    }

    private GestionSolicitudesComunidadDTO.Solicitud dto(
            Connection c, SolicitudComunidad solicitud) throws SQLException {
        Perfil perfil = perfiles.buscar(c,
                solicitud.getPerfilSolicitanteId());
        boolean pendiente = solicitud.getEstado().esPendiente();
        return new GestionSolicitudesComunidadDTO.Solicitud(
                solicitud.getId(), solicitud.getConvocatoriaId(),
                solicitud.getEstado().name(), solicitud.getMensaje(),
                solicitud.getFechaSolicitud(), solicitud.getFechaResolucion(),
                solicitud.getVersion(), pendiente, pendiente, perfil);
    }

    private ConvocatoriaComunidad requerirConvocatoria(Connection c,
            long id, boolean bloquear) {
        ConvocatoriaComunidad convocatoria = convocatorias.buscar(
                c, id, bloquear);
        if (convocatoria == null) {
            throw new ConvocatoriaNoEncontradaException();
        }
        return convocatoria;
    }

    private SolicitudComunidad requerirSolicitud(Connection c,
            long id, boolean bloquear) {
        SolicitudComunidad solicitud = solicitudes.buscar(c, id, bloquear);
        if (solicitud == null) throw new SolicitudNoEncontradaException();
        return solicitud;
    }

    private void requerirPropietario(ConvocatoriaComunidad convocatoria,
            long clienteId) {
        if (convocatoria.getClienteCreadorId() != clienteId) {
            throw new ConvocatoriaAjenaException();
        }
    }

    private void requerirActiva(ConvocatoriaComunidad convocatoria) {
        if (!convocatoria.getEstado().esActiva()) {
            throw new ConvocatoriaNoAdmiteGestionException();
        }
    }

    private void requerirPendiente(SolicitudComunidad solicitud) {
        if (!solicitud.getEstado().esPendiente()) {
            throw new SolicitudYaResueltaException();
        }
    }

    private void registrar(Connection c, TipoEventoComunidad tipo,
            ConvocatoriaComunidad convocatoria,
            SolicitudComunidad solicitud, long clienteId, long perfilId,
            LocalDateTime fecha, String detalle) {
        EventoComunidad evento = new EventoComunidad();
        evento.setTipo(tipo);
        evento.setConvocatoriaId(convocatoria.getId());
        evento.setSolicitudId(solicitud.getId());
        evento.setActorClienteId(clienteId);
        evento.setActorPerfilId(perfilId);
        evento.setDetalleJson(detalle);
        evento.setFechaEvento(fecha);
        eventos.guardar(c, evento);
    }

    private static Perfil leerPerfil(Connection c, long perfilId)
            throws SQLException {
        String q = "SELECT p.id,p.nombre_publico,p.posicion_preferida,"
                + "cd.nombre categoria,cd.color_hex "
                + "FROM perfiles_jugador p "
                + "LEFT JOIN jugador_categorias jc ON "
                + "jc.perfil_jugador_id=p.id AND jc.estado='VIGENTE' "
                + "LEFT JOIN categorias_deportivas cd ON "
                + "cd.id=jc.categoria_deportiva_id "
                + "WHERE p.id=? AND p.activo=TRUE";
        try (PreparedStatement s = c.prepareStatement(q)) {
            s.setLong(1, perfilId);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) {
                    throw new SolicitudNoEncontradaException();
                }
                String nombre = r.getString("nombre_publico");
                return new Perfil(perfilId, nombre, iniciales(nombre),
                        r.getString("posicion_preferida"),
                        r.getString("categoria"),
                        r.getString("color_hex"));
            }
        }
    }

    private static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "PJ";
        String[] partes = nombre.trim().split("\\s+");
        return (partes[0].substring(0, 1)
                + (partes.length > 1
                        ? partes[partes.length - 1].substring(0, 1) : ""))
                .toUpperCase();
    }

    private void validarIds(long clienteId, long recursoId) {
        if (clienteId <= 0 || recursoId <= 0) {
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

    @FunctionalInterface public interface ProveedorConexion {
        Connection obtener() throws SQLException;
    }
    @FunctionalInterface public interface ProveedorFecha {
        LocalDateTime ahora();
    }
    @FunctionalInterface public interface LectorPerfilPublico {
        Perfil buscar(Connection c, long perfilId) throws SQLException;
    }

    public static final class ConvocatoriaNoEncontradaException
            extends IllegalArgumentException {
        public ConvocatoriaNoEncontradaException() {
            super("La convocatoria no existe.");
        }
    }
    public static final class SolicitudNoEncontradaException
            extends IllegalArgumentException {
        public SolicitudNoEncontradaException() {
            super("La solicitud no existe.");
        }
    }
    public static final class ConvocatoriaAjenaException
            extends SecurityException {
        public ConvocatoriaAjenaException() {
            super("No podés gestionar solicitudes de otra convocatoria.");
        }
    }
    public static final class ConvocatoriaNoAdmiteGestionException
            extends IllegalStateException {
        public ConvocatoriaNoAdmiteGestionException() {
            super("La convocatoria ya no admite gestión de solicitudes.");
        }
    }
    public static final class SolicitudYaResueltaException
            extends IllegalStateException {
        public SolicitudYaResueltaException() {
            super("La solicitud ya fue resuelta.");
        }
    }
    public static final class SinCupoParaAceptarException
            extends IllegalStateException {
        public SinCupoParaAceptarException() {
            super("El turno ya no tiene cupos disponibles.");
        }
    }
    public static final class PerfilYaParticipaException
            extends IllegalStateException {
        public PerfilYaParticipaException() {
            super("El perfil ya participa en este turno.");
        }
    }
}
