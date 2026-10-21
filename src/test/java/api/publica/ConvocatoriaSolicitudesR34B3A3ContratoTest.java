package api.publica;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ConvocatoriaSolicitudesR34B3A3ContratoTest {
    @Test
    void publicaDetalleYProtegeOperacionesAutenticadas() throws Exception {
        String server = Files.readString(Path.of(
                "src/main/java/api/publica/ApiPublicaServer.java"));
        String cuenta = Files.readString(Path.of(
                "src/main/java/api/publica/CuentaClienteHandler.java"));
        String detalle = Files.readString(Path.of(
                "src/main/java/api/publica/DetalleConvocatoriaComunidadService.java"));

        assertTrue(server.contains(
                "PREFIJO + \"/comunidad/convocatorias/\""));
        assertTrue(server.contains(
                "detalleConvocatoriaComunidadService.buscar"));
        assertTrue(cuenta.contains("/mi-estado"));
        assertTrue(cuenta.contains("/solicitudes"));
        assertTrue(cuenta.contains("/retirar"));
        assertTrue(cuenta.contains(
                "detalleConvocatoriaComunidadService.solicitar"));
        assertTrue(cuenta.contains(
                "detalleConvocatoriaComunidadService.retirar"));
        assertTrue(detalle.contains("solicitudes.solicitar"));
        assertTrue(detalle.contains("solicitudes.retirar"));
        assertTrue(cuenta.contains(
                "SOLICITUD_COMUNIDAD_NO_RETIRABLE"));
    }
}
