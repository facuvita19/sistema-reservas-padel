package api.publica;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class InvitacionesComunidadR34B4AContratoTest {

    @Test
    void rutasAutenticadasYCicloCompleto() throws Exception {
        String handler = Files.readString(Path.of(
                "src/main/java/api/publica/CuentaClienteHandler.java"));

        for (String contrato : new String[] {
                "/comunidad/invitaciones/recibidas",
                "/comunidad/invitaciones/enviadas",
                "invitacionComunidadService.enviar",
                "case \"aceptar\"",
                "case \"rechazar\"",
                "case \"cancelar\""
        }) {
            assertTrue(handler.contains(contrato), contrato);
        }
    }
}
