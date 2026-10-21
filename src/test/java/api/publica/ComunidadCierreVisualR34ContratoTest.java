package api.publica;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ComunidadCierreVisualR34ContratoTest {
 @Test void conservaCierreVisualYAccionesDelTitular() throws Exception {
  String comunidad=Files.readString(Path.of("web-publica-v2/src/w2/scripts/comunidad.ts"));
  String cuenta=Files.readString(Path.of("web-publica-v2/src/w2/scripts/cuenta.ts"));
  String detalle=Files.readString(Path.of("web-publica-v2/src/pages/w2/comunidad/convocatoria.astro"));
  assertTrue(comunidad.contains("w2cm-turn-tags"));
  assertTrue(cuenta.contains("Ver mi convocatoria"));
  assertTrue(cuenta.contains("w2rc-close-confirm"));
  assertTrue(cuenta.contains("Categoría buscada"));
  assertTrue(detalle.contains("w2cvCopyLink"));
 }
}
