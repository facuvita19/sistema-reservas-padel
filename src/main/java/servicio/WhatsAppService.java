package servicio;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import util.NormalizadorTelefono;

public class WhatsAppService {

    private static final String URL_WHATSAPP =
            "https://wa.me/%s?text=%s";

    public void abrirConversacion(
            String telefono,
            String mensaje) {

        URI enlace = crearEnlace(telefono, mensaje);

        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException(
                    "El sistema no permite abrir enlaces externos."
            );
        }

        Desktop escritorio = Desktop.getDesktop();

        if (!escritorio.isSupported(Desktop.Action.BROWSE)) {
            throw new IllegalStateException(
                    "El sistema no permite abrir el navegador."
            );
        }

        try {
            escritorio.browse(enlace);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "No se pudo abrir WhatsApp.",
                    exception
            );
        }
    }

    public URI crearEnlace(
            String telefono,
            String mensaje) {

        String numero = normalizarTelefono(telefono);

        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje de WhatsApp no puede estar vacío."
            );
        }

        String textoCodificado = URLEncoder.encode(
                mensaje.trim(),
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        try {
            return new URI(
                    URL_WHATSAPP.formatted(
                            numero,
                            textoCodificado
                    )
            );
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(
                    "No se pudo construir el enlace de WhatsApp.",
                    exception
            );
        }
    }

    public String normalizarTelefono(String telefono) {
        try {
            return NormalizadorTelefono.normalizar(telefono);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El telefono debe incluir el codigo de pais "
                            + "y contener entre 8 y 15 digitos.",
                    exception);
        }
    }}
