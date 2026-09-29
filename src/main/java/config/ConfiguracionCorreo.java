package config;

public record ConfiguracionCorreo(
        String host,
        int puerto,
        String usuario,
        String password,
        String emailRemitente,
        String nombreRemitente,
        boolean startTls,
        boolean ssl,
        int timeoutMilisegundos) {

    private static final int PUERTO_PREDETERMINADO = 587;
    private static final int TIMEOUT_PREDETERMINADO = 10_000;

    public ConfiguracionCorreo {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(
                    "SMTP_HOST es obligatorio.");
        }
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException(
                    "SMTP_PORT no es valido.");
        }
        if ((usuario == null) != (password == null)) {
            throw new IllegalArgumentException(
                    "SMTP_USER y SMTP_PASSWORD deben configurarse juntos.");
        }
        if (emailRemitente == null || emailRemitente.isBlank()) {
            throw new IllegalArgumentException(
                    "SMTP_FROM_EMAIL es obligatorio.");
        }
        if (nombreRemitente == null || nombreRemitente.isBlank()) {
            nombreRemitente = "Padel Reservas";
        }
        if (startTls && ssl) {
            throw new IllegalArgumentException(
                    "SMTP_STARTTLS y SMTP_SSL no pueden estar activos al mismo tiempo.");
        }
        if (timeoutMilisegundos < 1) {
            throw new IllegalArgumentException(
                    "SMTP_TIMEOUT_MS debe ser positivo.");
        }
    }

    public static ConfiguracionCorreo cargar() {
        String host = valor("SMTP_HOST", null);
        int puerto = entero("SMTP_PORT", PUERTO_PREDETERMINADO);
        String usuario = opcional("SMTP_USER");
        String password = opcional("SMTP_PASSWORD");
        String emailRemitente = valor("SMTP_FROM_EMAIL", usuario);
        String nombreRemitente = valor(
                "SMTP_FROM_NAME", "Padel Reservas");
        boolean startTls = booleano("SMTP_STARTTLS", true);
        boolean ssl = booleano("SMTP_SSL", false);
        int timeout = entero(
                "SMTP_TIMEOUT_MS", TIMEOUT_PREDETERMINADO);
        return new ConfiguracionCorreo(
                host, puerto, usuario, password,
                emailRemitente, nombreRemitente,
                startTls, ssl, timeout);
    }

    public static boolean estaConfigurado() {
        String host = opcional("SMTP_HOST");
        String remitente = opcional("SMTP_FROM_EMAIL");
        String usuario = opcional("SMTP_USER");
        return host != null && (remitente != null || usuario != null);
    }

    public boolean requiereAutenticacion() {
        return usuario != null && password != null;
    }

    private static String valor(String clave, String predeterminado) {
        String resultado = opcional(clave);
        return resultado == null ? predeterminado : resultado;
    }

    private static String opcional(String clave) {
        String resultado = System.getenv(clave);
        if (resultado == null || resultado.isBlank()) {
            resultado = System.getProperty(
                    clave.toLowerCase().replace('_', '.'));
        }
        return resultado == null || resultado.isBlank()
                ? null
                : resultado.trim();
    }

    private static int entero(String clave, int predeterminado) {
        String valor = opcional(clave);
        if (valor == null) return predeterminado;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    clave + " debe ser un numero entero.", exception);
        }
    }

    private static boolean booleano(
            String clave,
            boolean predeterminado) {
        String valor = opcional(clave);
        return valor == null
                ? predeterminado
                : Boolean.parseBoolean(valor);
    }
}
