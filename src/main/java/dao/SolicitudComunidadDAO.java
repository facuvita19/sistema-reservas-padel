package dao;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import negocio.EstadoSolicitudComunidad;
import negocio.SolicitudComunidad;

public interface SolicitudComunidadDAO {
    void guardar(Connection c, SolicitudComunidad v);
    SolicitudComunidad buscar(Connection c, long id, boolean bloquear);
    List<SolicitudComunidad> listarPorConvocatoria(
            Connection c, long convocatoriaId);

    default SolicitudComunidad buscarPorConvocatoriaYPerfil(
            Connection c, long convocatoriaId, long perfilId,
            boolean bloquear) {
        return listarPorConvocatoria(c, convocatoriaId).stream()
                .filter(v -> v.getPerfilSolicitanteId() == perfilId)
                .findFirst().orElse(null);
    }

    default void reabrirCancelada(Connection c,
            SolicitudComunidad solicitud, String mensaje,
            LocalDateTime fechaSolicitud) {
        throw new UnsupportedOperationException(
                "El adaptador debe implementar la reapertura controlada.");
    }

    void cerrarPendientes(Connection c, long convocatoriaId);

    default int resolverPendientes(Connection c, long convocatoriaId,
            EstadoSolicitudComunidad estado) {
        if (estado == null || estado.esPendiente()
                || estado == EstadoSolicitudComunidad.ACEPTADA
                || estado == EstadoSolicitudComunidad.RECHAZADA
                || estado == EstadoSolicitudComunidad.CANCELADA) {
            throw new IllegalArgumentException(
                    "El estado no puede resolver solicitudes pendientes en bloque.");
        }
        cerrarPendientes(c, convocatoriaId);
        return 0;
    }
}
