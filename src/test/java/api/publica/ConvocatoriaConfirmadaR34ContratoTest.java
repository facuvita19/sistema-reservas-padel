package api.publica;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ConvocatoriaConfirmadaR34ContratoTest {
 @Test void exigeReservaConfirmadaYPreservaPayloadSeleccionado() throws Exception {
  String service=Files.readString(Path.of("src/main/java/servicio/ConvocatoriaComunidadService.java"));
  String cuenta=Files.readString(Path.of("web-publica-v2/src/w2/scripts/cuenta.ts"));
  String vista=Files.readString(Path.of("web-publica-v2/src/pages/w2/comunidad/convocatoria.astro"));
  assertTrue(service.contains("r.estado()!=EstadoReserva.CONFIRMADA"));
  assertTrue(service.contains("Confirmá la seña para publicar este turno en Comunidad."));
  assertTrue(cuenta.contains("if(!r.confirmada)"));
  assertTrue(cuenta.contains("categoria.value?Number(categoria.value):null"));
  assertTrue(cuenta.contains("posicionBuscada:posicion.value||null"));
  assertTrue(cuenta.contains("comentario:comentario.value.trim()||null"));
  assertTrue(vista.contains("Turno publicado por el titular"));
 }
}
