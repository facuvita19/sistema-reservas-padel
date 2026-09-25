package util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class ProtectorPassword {

    private static final String ALGORITMO =
            "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 210_000;
    private static final int LONGITUD_SALT_BYTES = 16;
    private static final int LONGITUD_HASH_BITS = 256;
    private static final String SEPARADOR = ":";

    private static final SecureRandom GENERADOR_SEGURO =
            new SecureRandom();

    private ProtectorPassword() {
    }

    public static String generarHash(String password) {
        validarPassword(password);

        byte[] salt = new byte[LONGITUD_SALT_BYTES];
        GENERADOR_SEGURO.nextBytes(salt);

        byte[] hash = derivarClave(
                password.toCharArray(),
                salt,
                ITERACIONES,
                LONGITUD_HASH_BITS
        );

        Base64.Encoder codificador = Base64.getEncoder();

        return ALGORITMO
                + SEPARADOR
                + ITERACIONES
                + SEPARADOR
                + codificador.encodeToString(salt)
                + SEPARADOR
                + codificador.encodeToString(hash);
    }

    public static boolean verificar(
            String password,
            String hashGuardado) {

        if (password == null || hashGuardado == null
                || hashGuardado.isBlank()) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split(SEPARADOR);

            if (partes.length != 4
                    || !ALGORITMO.equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado =
                    Base64.getDecoder().decode(partes[3]);

            byte[] hashCalculado = derivarClave(
                    password.toCharArray(),
                    salt,
                    iteraciones,
                    hashEsperado.length * 8
            );

            return MessageDigest.isEqual(
                    hashEsperado,
                    hashCalculado
            );

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] derivarClave(
            char[] password,
            byte[] salt,
            int iteraciones,
            int longitudBits) {

        PBEKeySpec especificacion = new PBEKeySpec(
                password,
                salt,
                iteraciones,
                longitudBits
        );

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance(ALGORITMO);
            return fabrica.generateSecret(especificacion).getEncoded();

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "No se pudo proteger la contrasena.",
                    exception
            );
        } finally {
            especificacion.clearPassword();
        }
    }

    private static void validarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contrasena es obligatoria."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener al menos 8 caracteres."
            );
        }
    }
}
