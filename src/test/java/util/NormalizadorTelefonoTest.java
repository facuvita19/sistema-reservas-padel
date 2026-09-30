package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NormalizadorTelefonoTest {

    @Test
    void eliminaCaracteresNoNumericos() {
        assertEquals(
                "5491155551234",
                NormalizadorTelefono.normalizar(
                        "+54 9 (11) 5555-1234"));
    }

    @Test
    void aceptaOchoYQuinceDigitos() {
        assertEquals(
                "11555512",
                NormalizadorTelefono.normalizar("11 5555-12"));
        assertEquals(
                "123456789012345",
                NormalizadorTelefono.normalizar(
                        "123 456 789 012 345"));
    }

    @Test
    void rechazaTelefonoVacio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> NormalizadorTelefono.normalizar("  "));
    }

    @Test
    void rechazaLongitudesFueraDeRango() {
        assertThrows(
                IllegalArgumentException.class,
                () -> NormalizadorTelefono.normalizar("1234567"));
        assertThrows(
                IllegalArgumentException.class,
                () -> NormalizadorTelefono.normalizar(
                        "1234567890123456"));
    }

    @Test
    void normalizarOpcionalDevuelveNuloParaVacio() {
        assertNull(NormalizadorTelefono.normalizarOpcional(null));
        assertNull(NormalizadorTelefono.normalizarOpcional(" "));
    }
}
