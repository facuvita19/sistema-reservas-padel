package api.publica;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CookieSesionClientePersistenciaContratoTest {
 @Test void distingueCookieTemporalYPersistente() throws Exception {
  String fuente=Files.readString(Path.of("src/main/java/api/publica/CookieSesionCliente.java"));
  assertTrue(fuente.contains("escribirTemporal"));
  String temporal=fuente.substring(fuente.indexOf("escribirTemporal"),fuente.indexOf("private static void validar"));
  assertFalse(temporal.contains("Max-Age"));
  assertTrue(fuente.contains("; Max-Age="));
  assertTrue(fuente.contains("HttpOnly; SameSite=Lax"));
 }
}
