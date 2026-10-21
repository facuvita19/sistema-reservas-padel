package migracion;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
class ComunidadPersistenciaR34B3A1ContratoTest {
 @Test void actualizacionEInstalacionLimpiaConservanElMismoContrato() throws Exception {
  String update=Files.readString(Path.of("database/33_comunidad_r34b3a1.sql"));
  String clean=Files.readString(Path.of("database/docker-init/034_33_comunidad_r34b3a1.sql"));
  assertEquals(update,clean);
  for(String tabla:new String[]{"comunidad_preferencias","comunidad_convocatorias","comunidad_solicitudes","comunidad_participantes","comunidad_eventos"})assertTrue(update.contains("CREATE TABLE IF NOT EXISTS "+tabla));
  for(String contrato:new String[]{"solicitud_origen_id","uk_comunidad_participante_solicitud","fk_comunidad_participante_solicitud"})assertTrue(update.contains(contrato));
  assertTrue(update.contains("information_schema.COLUMNS"));
  assertTrue(update.contains("information_schema.STATISTICS"));
  assertTrue(update.contains("information_schema.TABLE_CONSTRAINTS"));
 }
}
