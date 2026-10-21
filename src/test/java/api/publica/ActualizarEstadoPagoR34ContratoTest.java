package api.publica;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ActualizarEstadoPagoR34ContratoTest {
 @Test void comunicaCargaResultadoYRefrescaReserva() throws Exception {
  String cuenta=Files.readString(Path.of("web-publica-v2/src/w2/scripts/cuenta.ts"));
  assertTrue(cuenta.contains("button.textContent='Actualizando…'"));
  assertTrue(cuenta.contains("await api<Historial>('/cliente/reservas')"));
  assertTrue(cuenta.contains("delete host.dataset.w2pFingerprint"));
  assertTrue(cuenta.contains("La acreditación sigue pendiente"));
  assertTrue(cuenta.contains("w2rCommunityRender(communityHost,actualizada)"));
 }
}
