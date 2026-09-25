package util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProtectorPasswordTest {

    @Test
    void generaHashYVerificaPasswordCorrecta() {
        String hash = ProtectorPassword.generarHash("ClaveSegura123");

        assertTrue(hash.startsWith("PBKDF2WithHmacSHA256:"));
        assertTrue(ProtectorPassword.verificar("ClaveSegura123", hash));
        assertFalse(ProtectorPassword.verificar("Incorrecta123", hash));
    }

    @Test
    void generaSaltDiferenteParaLaMismaPassword() {
        String primero = ProtectorPassword.generarHash("ClaveSegura123");
        String segundo = ProtectorPassword.generarHash("ClaveSegura123");

        assertNotEquals(primero, segundo);
        assertTrue(ProtectorPassword.verificar("ClaveSegura123", primero));
        assertTrue(ProtectorPassword.verificar("ClaveSegura123", segundo));
    }

    @Test
    void rechazaPasswordCortaOVacia() {
        assertThrows(IllegalArgumentException.class,
                () -> ProtectorPassword.generarHash("123"));
        assertThrows(IllegalArgumentException.class,
                () -> ProtectorPassword.generarHash("   "));
    }

    @Test
    void rechazaHashNuloOMalformado() {
        assertFalse(ProtectorPassword.verificar("ClaveSegura123", null));
        assertFalse(ProtectorPassword.verificar("ClaveSegura123", "hash-malformado"));
        assertFalse(ProtectorPassword.verificar(null, "hash-malformado"));
    }
}
