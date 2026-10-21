package api.publica;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;

class GestionSolicitudesR34B3A4ContratoTest {
    @Test void conservaGestionAtomicaYEsquemaDeCuatroParticipantes()
            throws Exception {
        String servicio=Files.readString(Path.of(
                "src/main/java/servicio/GestionSolicitudesComunidadService.java"));
        String sql=Files.readString(Path.of(
                "database/34_comunidad_gestion_solicitudes_r34b3a4.sql"));
        String clean=Files.readString(Path.of(
                "database/docker-init/035_34_comunidad_gestion_solicitudes_r34b3a4.sql"));
        assertEquals(sql,clean);
        assertTrue(sql.contains("BETWEEN 1 AND 4"));
        assertTrue(servicio.contains("setSolicitudOrigenId"));
        assertTrue(servicio.contains("SOLICITUD_ACEPTADA"));
        assertTrue(servicio.contains("PARTICIPANTE_CONFIRMADO"));
        assertTrue(servicio.contains("CERRADA_POR_CUPO"));
        assertTrue(servicio.contains("CONVOCATORIA_COMPLETA"));
        assertTrue(servicio.contains("c.commit()"));
    }
}
