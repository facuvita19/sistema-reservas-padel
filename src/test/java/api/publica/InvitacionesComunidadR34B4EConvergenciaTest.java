package api.publica;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;import java.nio.file.Path;import org.junit.jupiter.api.Test;
class InvitacionesComunidadR34B4EConvergenciaTest {
 @Test void convergeVencimientoIdempotenciaYFoco() throws Exception {
  String service=Files.readString(Path.of("src/main/java/servicio/InvitacionComunidadService.java"));
  String player=Files.readString(Path.of("web-publica-v2/src/w2/scripts/jugador.ts"));
  assertTrue(service.contains("fecha_expiracion<=NOW()"));
  assertTrue(service.contains("Invitacion concurrente=pendiente"));
  assertTrue(player.contains("e.key!=='Tab'"));
  assertTrue(player.contains("Invitación pendiente"));
 }
}
