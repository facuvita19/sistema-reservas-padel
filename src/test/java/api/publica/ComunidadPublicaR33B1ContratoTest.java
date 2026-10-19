package api.publica;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ComunidadPublicaR33B1ContratoTest {
    @Test
    void servidorPublicaCatalogoPaginadoSinReutilizarPerfilCompleto() throws Exception {
        String codigo = Files.readString(Path.of(
                "src/main/java/api/publica/ApiPublicaServer.java"));
        assertTrue(codigo.contains("/comunidad/jugadores"));
        assertTrue(codigo.contains("consultaJugadoresComunidadService.listar"));
        assertTrue(codigo.contains("enteroPositivo"));
        assertFalse(codigo.contains("jugadorPublicoService.buscar(id(valor)).listar"));
    }
}
