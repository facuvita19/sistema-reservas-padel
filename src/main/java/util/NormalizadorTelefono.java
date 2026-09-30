package util;

public final class NormalizadorTelefono {

    private static final int MINIMO_DIGITOS = 8;
    private static final int MAXIMO_DIGITOS = 15;

    private NormalizadorTelefono() {
    }

    public static String normalizar(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException(
                    "El telefono es obligatorio.");
        }
        String numero = telefono.replaceAll("\\D", "");
        if (numero.length() < MINIMO_DIGITOS
                || numero.length() > MAXIMO_DIGITOS) {
            throw new IllegalArgumentException(
                    "El telefono debe contener entre 8 y 15 digitos.");
        }
        return numero;
    }

    public static String normalizarOpcional(String telefono) {
        return telefono == null || telefono.isBlank()
                ? null
                : normalizar(telefono);
    }
}
