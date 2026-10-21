package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import negocio.*;

public class SolicitudComunidadDAOMySQL implements SolicitudComunidadDAO {
    public void guardar(Connection c, SolicitudComunidad v) {
        String q;
        if (v.getId() <= 0) {
            q = "INSERT INTO comunidad_solicitudes(convocatoria_id,perfil_solicitante_id,estado,mensaje,fecha_solicitud,version) VALUES(?,?,?,?,?,0)";
        } else {
            q = "UPDATE comunidad_solicitudes SET estado=?,mensaje=?,fecha_contacto=?,fecha_resolucion=?,version=version+1 WHERE id=? AND version=?";
        }
        try (PreparedStatement s = c.prepareStatement(q,
                Statement.RETURN_GENERATED_KEYS)) {
            if (v.getId() <= 0) {
                s.setLong(1, v.getConvocatoriaId());
                s.setLong(2, v.getPerfilSolicitanteId());
                s.setString(3, v.getEstado().name());
                s.setString(4, v.getMensaje());
                s.setTimestamp(5, Timestamp.valueOf(v.getFechaSolicitud()));
                if (s.executeUpdate() == 0) {
                    throw new RuntimeException("No se creó la solicitud.");
                }
                try (ResultSet r = s.getGeneratedKeys()) {
                    if (r.next()) v.setId(r.getLong(1));
                }
            } else {
                s.setString(1, v.getEstado().name());
                s.setString(2, v.getMensaje());
                time(s, 3, v.getFechaContacto());
                time(s, 4, v.getFechaResolucion());
                s.setLong(5, v.getId());
                s.setLong(6, v.getVersion());
                if (s.executeUpdate() == 0) {
                    throw new IllegalStateException(
                            "La solicitud fue modificada por otra operación.");
                }
                v.setVersion(v.getVersion() + 1);
            }
        } catch (SQLException e) {
            throw e.getErrorCode() == 1062
                    ? new IllegalArgumentException(
                            "Ya existe una solicitud para esta convocatoria.", e)
                    : new RuntimeException("No se pudo guardar la solicitud.", e);
        }
    }

    public SolicitudComunidad buscar(Connection c, long id, boolean lock) {
        List<SolicitudComunidad> x = list(c,
                " WHERE id=?" + (lock ? " FOR UPDATE" : ""), id);
        return x.isEmpty() ? null : x.getFirst();
    }

    public SolicitudComunidad buscarPorConvocatoriaYPerfil(Connection c,
            long convocatoriaId, long perfilId, boolean lock) {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT * FROM comunidad_solicitudes WHERE convocatoria_id=? AND perfil_solicitante_id=?"
                        + (lock ? " FOR UPDATE" : ""))) {
            s.setLong(1, convocatoriaId);
            s.setLong(2, perfilId);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudo consultar la solicitud del perfil.", e);
        }
    }

    public List<SolicitudComunidad> listarPorConvocatoria(Connection c,
            long id) {
        return list(c,
                " WHERE convocatoria_id=? ORDER BY fecha_solicitud DESC", id);
    }

    public void reabrirCancelada(Connection c, SolicitudComunidad v,
            String mensaje, LocalDateTime fechaSolicitud) {
        if (v == null || v.getId() <= 0
                || v.getEstado() != EstadoSolicitudComunidad.CANCELADA
                || fechaSolicitud == null) {
            throw new IllegalArgumentException(
                    "Solo se puede reabrir una solicitud cancelada válida.");
        }
        String q = "UPDATE comunidad_solicitudes SET estado='PENDIENTE',mensaje=?,fecha_solicitud=?,fecha_contacto=NULL,fecha_resolucion=NULL,version=version+1 WHERE id=? AND version=? AND estado='CANCELADA'";
        try (PreparedStatement s = c.prepareStatement(q)) {
            s.setString(1, mensaje);
            s.setTimestamp(2, Timestamp.valueOf(fechaSolicitud));
            s.setLong(3, v.getId());
            s.setLong(4, v.getVersion());
            if (s.executeUpdate() == 0) {
                throw new IllegalStateException(
                        "La solicitud fue modificada por otra operación.");
            }
            v.setEstado(EstadoSolicitudComunidad.PENDIENTE);
            v.setMensaje(mensaje);
            v.setFechaSolicitud(fechaSolicitud);
            v.setFechaContacto(null);
            v.setFechaResolucion(null);
            v.setVersion(v.getVersion() + 1);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudo reabrir la solicitud cancelada.", e);
        }
    }

    private List<SolicitudComunidad> list(Connection c, String where,
            long id) {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT * FROM comunidad_solicitudes" + where)) {
            s.setLong(1, id);
            try (ResultSet r = s.executeQuery()) {
                List<SolicitudComunidad> out = new ArrayList<>();
                while (r.next()) out.add(map(r));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudieron consultar las solicitudes.", e);
        }
    }

    private SolicitudComunidad map(ResultSet r) throws SQLException {
        SolicitudComunidad v = new SolicitudComunidad();
        v.setId(r.getLong("id"));
        v.setConvocatoriaId(r.getLong("convocatoria_id"));
        v.setPerfilSolicitanteId(r.getLong("perfil_solicitante_id"));
        v.setEstado(EstadoSolicitudComunidad.valueOf(r.getString("estado")));
        v.setMensaje(r.getString("mensaje"));
        v.setFechaSolicitud(r.getTimestamp("fecha_solicitud").toLocalDateTime());
        Timestamp t = r.getTimestamp("fecha_contacto");
        if (t != null) v.setFechaContacto(t.toLocalDateTime());
        t = r.getTimestamp("fecha_resolucion");
        if (t != null) v.setFechaResolucion(t.toLocalDateTime());
        t = r.getTimestamp("fecha_actualizacion");
        if (t != null) v.setFechaActualizacion(t.toLocalDateTime());
        v.setVersion(r.getLong("version"));
        return v;
    }

    public void cerrarPendientes(Connection c, long id) {
        resolverPendientes(c, id,
                EstadoSolicitudComunidad.CERRADA_POR_CONVOCANTE);
    }

    public int resolverPendientes(Connection c, long id,
            EstadoSolicitudComunidad estado) {
        if (estado == null || estado.esPendiente()
                || estado == EstadoSolicitudComunidad.ACEPTADA
                || estado == EstadoSolicitudComunidad.RECHAZADA
                || estado == EstadoSolicitudComunidad.CANCELADA) {
            throw new IllegalArgumentException(
                    "El estado no puede resolver solicitudes pendientes en bloque.");
        }
        try (PreparedStatement s = c.prepareStatement(
                "UPDATE comunidad_solicitudes SET estado=?,fecha_resolucion=CURRENT_TIMESTAMP,version=version+1 WHERE convocatoria_id=? AND estado IN ('PENDIENTE','CONTACTADA')")) {
            s.setString(1, estado.name());
            s.setLong(2, id);
            return s.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudieron resolver las solicitudes pendientes.", e);
        }
    }

    private void time(PreparedStatement s, int i, LocalDateTime v)
            throws SQLException {
        if (v == null) s.setNull(i, Types.TIMESTAMP);
        else s.setTimestamp(i, Timestamp.valueOf(v));
    }
}
