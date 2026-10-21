package api.publica;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;import java.nio.file.Path;import org.junit.jupiter.api.Test;
class ContactoProtegidoInvitacionesR34B4DContratoTest {
 @Test void contratoEsDiferidoAutenticadoYRevocable() throws Exception {
  String dto=Files.readString(Path.of("src/main/java/api/publica/InvitacionComunidadDTO.java"));
  String service=Files.readString(Path.of("src/main/java/servicio/InvitacionComunidadService.java"));
  String handler=Files.readString(Path.of("src/main/java/api/publica/CuentaClienteHandler.java"));
  assertTrue(dto.contains("record Contacto"));assertTrue(handler.contains("/contacto"));assertTrue(handler.contains("invitacionComunidadService.contacto"));
  assertTrue(service.contains("EstadoInvitacionComunidad.ACEPTADA"));assertTrue(service.contains("fecha_consentimiento_whatsapp"));assertTrue(service.contains("contacto_whatsapp"));assertTrue(service.contains("InvitacionAjenaException"));assertTrue(service.contains("https://wa.me/"));
 }
}
