package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;

class WhatsAppServiceTest {

    private final WhatsAppService service =
            new WhatsAppService();

    @Test
    void normalizaTelefonoConSignosYEspacios() {
        assertEquals(
                "5491123456789",
                service.normalizarTelefono(
                        "+54 9 11 2345-6789"
                )
        );
    }

    @Test
    void creaEnlaceConMensajeCodificado() {
        URI enlace = service.crearEnlace(
                "+54 9 11 2345-6789",
                "Hola, tu reserva está confirmada."
        );

        assertTrue(
                enlace.toString().startsWith(
                        "https://wa.me/5491123456789?text="
                )
        );
        assertTrue(enlace.toString().contains("%20"));
    }

    @Test
    void rechazaTelefonoInvalidoYMensajeVacio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.normalizarTelefono("123")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearEnlace(
                        "+54 9 11 2345-6789",
                        " "
                )
        );
    }
}
