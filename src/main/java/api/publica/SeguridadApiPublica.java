package api.publica;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SeguridadApiPublica {

    public static final int MAXIMO_JSON_BYTES = 16 * 1024;

    private static final Duration VENTANA_GENERAL = Duration.ofMinutes(1);
    private static final Duration VENTANA_CREACION = Duration.ofMinutes(10);
    private static final Duration VENTANA_REGISTRO = Duration.ofMinutes(30);
    private static final Duration VENTANA_LOGIN_IP = Duration.ofMinutes(15);
    private static final Duration VENTANA_LOGIN_CUENTA =
            Duration.ofMinutes(15);

    private static final int LIMITE_GENERAL = 120;
    private static final int LIMITE_CREACION = 8;
    private static final int LIMITE_REGISTRO = 5;
    private static final int LIMITE_LOGIN_IP = 20;
    private static final int LIMITE_LOGIN_CUENTA = 8;
    private static final int MAXIMO_ENTRADAS = 10_000;

    private final Map<String, Ventana> general =
            new ConcurrentHashMap<>();
    private final Map<String, Ventana> creacion =
            new ConcurrentHashMap<>();
    private final Map<String, Ventana> registro =
            new ConcurrentHashMap<>();
    private final Map<String, Ventana> loginPorIp =
            new ConcurrentHashMap<>();
    private final Map<String, Ventana> loginPorCuenta =
            new ConcurrentHashMap<>();

    public void validarGeneral(String ip) {
        validar(general, claveIp(ip), LIMITE_GENERAL, VENTANA_GENERAL);
    }

    public void validarCreacion(String ip) {
        validar(creacion, claveIp(ip), LIMITE_CREACION, VENTANA_CREACION);
    }

    public void validarRegistro(String ip) {
        validar(registro, claveIp(ip), LIMITE_REGISTRO, VENTANA_REGISTRO);
    }

    public void validarLogin(String ip, String email) {
        String ipNormalizada = claveIp(ip);
        validar(loginPorIp,
                ipNormalizada,
                LIMITE_LOGIN_IP,
                VENTANA_LOGIN_IP);
        validar(loginPorCuenta,
                ipNormalizada + "|" + normalizarCuenta(email),
                LIMITE_LOGIN_CUENTA,
                VENTANA_LOGIN_CUENTA);
    }

    public byte[] leerCuerpoLimitado(InputStream entrada)
            throws IOException {
        try (entrada;
             ByteArrayOutputStream salida =
                     new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int leidos;
            while ((leidos = entrada.read(buffer)) != -1) {
                total += leidos;
                if (total > MAXIMO_JSON_BYTES) {
                    throw new CuerpoDemasiadoGrandeException();
                }
                salida.write(buffer, 0, leidos);
            }
            return salida.toByteArray();
        }
    }

    public String normalizarIp(InetAddress direccion) {
        return direccion == null
                ? "desconocida"
                : direccion.getHostAddress();
    }

    private String claveIp(String ip) {
        return ip == null || ip.isBlank()
                ? "desconocida"
                : ip.trim();
    }

    private String normalizarCuenta(String email) {
        return email == null || email.isBlank()
                ? "desconocida"
                : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validar(
            Map<String, Ventana> mapa,
            String clave,
            int limite,
            Duration duracion) {
        long ahora = System.currentTimeMillis();
        long ventanaMs = duracion.toMillis();

        Ventana resultado = mapa.compute(clave, (k, actual) -> {
            if (actual == null
                    || ahora - actual.inicio() >= ventanaMs) {
                return new Ventana(ahora, 1);
            }
            return new Ventana(
                    actual.inicio(),
                    actual.cantidad() + 1);
        });

        if (resultado.cantidad() > limite) {
            long espera = Math.max(
                    1,
                    (ventanaMs
                            - (ahora - resultado.inicio())
                            + 999) / 1000);
            throw new DemasiadasSolicitudesException(espera);
        }

        if (mapa.size() > MAXIMO_ENTRADAS) {
            mapa.entrySet().removeIf(entrada ->
                    ahora - entrada.getValue().inicio() >= ventanaMs);
        }
    }

    private record Ventana(long inicio, int cantidad) {
    }

    public static final class CuerpoDemasiadoGrandeException
            extends RuntimeException {

        public CuerpoDemasiadoGrandeException() {
            super("El cuerpo de la solicitud supera el maximo permitido.");
        }
    }

    public static final class DemasiadasSolicitudesException
            extends RuntimeException {

        private final long segundosEspera;

        public DemasiadasSolicitudesException(long segundosEspera) {
            super("Se realizaron demasiados intentos. "
                    + "Espera unos minutos.");
            this.segundosEspera = segundosEspera;
        }

        public long getSegundosEspera() {
            return segundosEspera;
        }
    }
}
