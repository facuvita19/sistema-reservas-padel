package migracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ReparacionParticipanteSolicitudOrigenContratoTest {
    @Test
    void reparaColumnaIndiceYClaveForaneaDeFormaIdempotente()
            throws Exception {
        String normal = Files.readString(Path.of(
                "database/35_reparar_participante_solicitud_origen.sql"));
        String docker = Files.readString(Path.of(
                "database/docker-init/036_35_reparar_participante_solicitud_origen.sql"));

        assertEquals(normal, docker);
        assertTrue(normal.contains("COLUMN_NAME='solicitud_origen_id'"));
        assertTrue(normal.contains("ADD COLUMN solicitud_origen_id BIGINT NULL"));
        assertTrue(normal.contains("uk_comunidad_participante_solicitud"));
        assertTrue(normal.contains("fk_comunidad_participante_solicitud"));
        assertTrue(normal.contains("REFERENCES comunidad_solicitudes(id)"));
    }
}
