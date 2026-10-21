package api.publica;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ContadorPagoDestacadoR34ContratoTest {
 @Test void destacaElTiempoNormalUrgenteYVencido() throws Exception {
  String css=Files.readString(Path.of("web-publica-v2/src/w2/styles/w2-account.css"));
  assertTrue(css.contains(".w2p-payment-grid>.w2p-timer"));
  assertTrue(css.contains(".w2p-payment.urgent [data-w2p-time]"));
  assertTrue(css.contains(".w2p-payment.expired [data-w2p-time]"));
  assertTrue(css.contains("font-variant-numeric:tabular-nums"));
  assertTrue(css.contains("prefers-reduced-motion:reduce"));
 }
}
