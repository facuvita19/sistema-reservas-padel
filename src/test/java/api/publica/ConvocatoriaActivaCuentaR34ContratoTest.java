package api.publica;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ConvocatoriaActivaCuentaR34ContratoTest {
 @Test void priorizaLaConvocatoriaActivaPorReserva() throws Exception {
  String cuenta=Files.readString(Path.of("web-publica-v2/src/w2/scripts/cuenta.ts"));
  assertTrue(cuenta.contains("w2rConvocatorias=new Map();"));
  assertTrue(cuenta.contains("!actual.activa&&convocatoria.activa"));
  assertTrue(cuenta.contains("w2rConvocatorias.set(convocatoria.reservaId,convocatoria)"));
 }
}
