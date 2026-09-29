package api.publica;

import java.time.Duration;
import java.util.List;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public final class CookieSesionCliente {

    public static final String NOMBRE = "padel_sesion";
    private static final String PATH = "/";

    private CookieSesionCliente() {
    }

    public static String leer(HttpExchange intercambio) {
        if (intercambio == null) return null;
        List<String> cabeceras = intercambio.getRequestHeaders()
                .get("Cookie");
        if (cabeceras == null) return null;

        for (String cabecera : cabeceras) {
            if (cabecera == null || cabecera.isBlank()) continue;
            for (String parte : cabecera.split(";")) {
                String[] par = parte.trim().split("=", 2);
                if (par.length == 2 && NOMBRE.equals(par[0].trim())) {
                    String valor = par[1].trim();
                    return valor.isBlank() ? null : valor;
                }
            }
        }
        return null;
    }

    public static void escribir(
            HttpExchange intercambio,
            String token,
            Duration duracion,
            boolean segura) {
        if (intercambio == null) {
            throw new IllegalArgumentException(
                    "El intercambio HTTP no puede ser nulo.");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "El token de sesion es obligatorio.");
        }
        if (duracion == null
                || duracion.isNegative()
                || duracion.isZero()) {
            throw new IllegalArgumentException(
                    "La duracion de la cookie debe ser positiva.");
        }

        long segundos = Math.max(1L, duracion.toSeconds());
        String cookie = NOMBRE + "=" + token
                + "; Path=" + PATH
                + "; Max-Age=" + segundos
                + "; HttpOnly; SameSite=Lax"
                + (segura ? "; Secure" : "");
        agregar(intercambio.getResponseHeaders(), cookie);
    }

    public static void eliminar(
            HttpExchange intercambio,
            boolean segura) {
        if (intercambio == null) return;
        String cookie = NOMBRE + "="
                + "; Path=" + PATH
                + "; Max-Age=0"
                + "; HttpOnly; SameSite=Lax"
                + (segura ? "; Secure" : "");
        agregar(intercambio.getResponseHeaders(), cookie);
    }

    public static boolean debeSerSegura() {
        String propiedad = System.getProperty("api.cookies.secure");
        if (propiedad == null || propiedad.isBlank()) {
            propiedad = System.getenv("API_COOKIES_SECURE");
        }
        return propiedad != null
                && Boolean.parseBoolean(propiedad.trim());
    }

    private static void agregar(Headers cabeceras, String cookie) {
        cabeceras.add("Set-Cookie", cookie);
    }
}
