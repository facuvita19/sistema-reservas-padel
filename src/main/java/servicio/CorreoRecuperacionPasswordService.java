package servicio;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CorreoRecuperacionPasswordService {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CorreoService correoService;
    private final String urlPublica;

    public CorreoRecuperacionPasswordService(
            CorreoService correoService,
            String urlPublica) {
        if (correoService == null) {
            throw new IllegalArgumentException(
                    "El servicio de correo no puede ser nulo.");
        }
        if (urlPublica == null || urlPublica.isBlank()) {
            throw new IllegalArgumentException(
                    "La URL publica no puede estar vacia.");
        }
        this.correoService = correoService;
        this.urlPublica = normalizarUrl(urlPublica);
    }

    public void enviar(
            String destinatario,
            String token,
            LocalDateTime vencimiento) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "El destinatario es obligatorio.");
        }
        if (token == null || token.isBlank() || vencimiento == null) {
            throw new IllegalArgumentException(
                    "El token y su vencimiento son obligatorios.");
        }

        String enlace = urlPublica + "restablecer?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
        String vence = FECHA.format(vencimiento);

        String texto = "Recibimos una solicitud para restablecer "
                + "la contrasena de tu cuenta.\n\n"
                + "Abri este enlace antes del " + vence + ":\n"
                + enlace + "\n\n"
                + "Si no solicitaste el cambio, ignora este mensaje.";

        String html = "<!doctype html><html><body style=\"margin:0;"
                + "padding:24px;background:#f4f7f9;font-family:Arial,"
                + "sans-serif;color:#17212b\"><div style=\"max-width:"
                + "560px;margin:auto;background:#ffffff;border-radius:16px;"
                + "padding:28px;border:1px solid #dfe7ec\">"
                + "<p style=\"font-size:12px;font-weight:700;letter-spacing:"
                + "1px;color:#147d64\">PADEL RESERVAS</p>"
                + "<h1 style=\"font-size:24px;margin:8px 0 16px\">"
                + "Restablecer contrasena</h1>"
                + "<p>Recibimos una solicitud para restablecer la "
                + "contrasena de tu cuenta.</p>"
                + "<p style=\"margin:24px 0\"><a href=\""
                + escaparAtributo(enlace)
                + "\" style=\"display:inline-block;padding:13px 20px;"
                + "border-radius:10px;background:#147d64;color:#ffffff;"
                + "text-decoration:none;font-weight:700\">"
                + "Crear nueva contrasena</a></p>"
                + "<p style=\"font-size:14px;color:#5d6b75\">El enlace "
                + "vence el " + vence + " y puede utilizarse una sola vez."
                + "</p><p style=\"font-size:14px;color:#5d6b75\">Si no "
                + "solicitaste el cambio, ignora este mensaje.</p>"
                + "</div></body></html>";

        correoService.enviar(
                destinatario,
                "Restablecer contrasena - Padel Reservas",
                texto,
                html);
    }

    private String normalizarUrl(String valor) {
        String limpia = valor.trim();
        while (limpia.endsWith("?") || limpia.endsWith("/")) {
            limpia = limpia.substring(0, limpia.length() - 1);
        }
        return limpia + "/";
    }

    private String escaparAtributo(String valor) {
        return valor.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
