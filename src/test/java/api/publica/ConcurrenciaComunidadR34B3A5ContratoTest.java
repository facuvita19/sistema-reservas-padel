package api.publica;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ConcurrenciaComunidadR34B3A5ContratoTest {
    @Test
    void conservaBloqueosYResolucionSemantica() throws Exception {
        String gestion = Files.readString(Path.of(
                "src/main/java/servicio/GestionSolicitudesComunidadService.java"));
        String solicitud = Files.readString(Path.of(
                "src/main/java/servicio/SolicitudComunidadService.java"));
        String sync = Files.readString(Path.of(
                "src/main/java/servicio/SincronizacionConvocatoriasComunidadService.java"));
        String dao = Files.readString(Path.of(
                "src/main/java/dao/SolicitudComunidadDAOMySQL.java"));

        assertTrue(gestion.contains("solicitudes.buscar(")
                && gestion.contains("solicitudId, true"));
        assertTrue(gestion.contains("buscarConfirmadoPorPerfil"));
        assertTrue(gestion.contains("CERRADA_POR_CUPO"));
        assertTrue(solicitud.contains("FOR UPDATE"));
        assertTrue(sync.contains("WHERE id=? FOR UPDATE"));
        assertTrue(sync.contains("EstadoSolicitudComunidad.VENCIDA"));
        assertTrue(sync.contains("EstadoConvocatoriaComunidad.CANCELADA"));
        assertTrue(dao.contains("resolverPendientes"));
        assertTrue(dao.contains("estado IN ('PENDIENTE','CONTACTADA')"));
        assertTrue(sync.contains("c.commit()"));
        assertTrue(sync.contains("c.rollback()"));
        String detalle = Files.readString(Path.of(
                "src/main/java/api/publica/DetalleConvocatoriaComunidadService.java"));
        assertTrue(detalle.contains("sincronizacion.reconciliar(id)"));
        assertTrue(gestion.contains("sincronizacion.reconciliar(convocatoriaId)"));
    }
}
