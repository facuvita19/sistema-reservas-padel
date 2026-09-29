package servicio;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

import config.ConfiguracionCorreo;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class CorreoService {

    private final ConfiguracionCorreo configuracion;
    private final Session sesion;

    public CorreoService() {
        this(ConfiguracionCorreo.cargar());
    }

    public CorreoService(ConfiguracionCorreo configuracion) {
        if (configuracion == null) {
            throw new IllegalArgumentException(
                    "La configuracion de correo no puede ser nula.");
        }
        this.configuracion = configuracion;
        this.sesion = crearSesion(configuracion);
    }

    public void enviar(
            String destinatario,
            String asunto,
            String textoPlano,
            String html) {
        validarTexto(destinatario, "El destinatario es obligatorio.");
        validarTexto(asunto, "El asunto es obligatorio.");
        validarTexto(textoPlano, "El contenido de texto es obligatorio.");
        validarTexto(html, "El contenido HTML es obligatorio.");

        try {
            MimeMessage mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(
                    configuracion.emailRemitente(),
                    configuracion.nombreRemitente(),
                    StandardCharsets.UTF_8.name()));
            mensaje.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(destinatario, true));
            mensaje.setSubject(asunto, StandardCharsets.UTF_8.name());
            mensaje.setSentDate(new Date());

            MimeBodyPart texto = new MimeBodyPart();
            texto.setText(
                    textoPlano,
                    StandardCharsets.UTF_8.name());

            MimeBodyPart contenidoHtml = new MimeBodyPart();
            contenidoHtml.setContent(
                    html,
                    "text/html; charset=UTF-8");

            MimeMultipart alternativo = new MimeMultipart("alternative");
            alternativo.addBodyPart(texto);
            alternativo.addBodyPart(contenidoHtml);
            mensaje.setContent(alternativo);

            Transport.send(mensaje);
        } catch (Exception exception) {
            throw new RuntimeException(
                    "No se pudo enviar el correo electronico.",
                    exception);
        }
    }

    public void probarConexion() {
        try (Transport transporte = sesion.getTransport(
                configuracion.ssl() ? "smtps" : "smtp")) {
            if (configuracion.requiereAutenticacion()) {
                transporte.connect(
                        configuracion.host(),
                        configuracion.puerto(),
                        configuracion.usuario(),
                        configuracion.password());
            } else {
                transporte.connect(
                        configuracion.host(),
                        configuracion.puerto(),
                        null,
                        null);
            }
        } catch (MessagingException exception) {
            throw new RuntimeException(
                    "No se pudo conectar con el servidor SMTP.",
                    exception);
        }
    }

    private Session crearSesion(ConfiguracionCorreo config) {
        Properties propiedades = new Properties();
        propiedades.setProperty("mail.smtp.host", config.host());
        propiedades.setProperty(
                "mail.smtp.port", String.valueOf(config.puerto()));
        propiedades.setProperty(
                "mail.smtp.auth",
                String.valueOf(config.requiereAutenticacion()));
        propiedades.setProperty(
                "mail.smtp.starttls.enable",
                String.valueOf(config.startTls()));
        propiedades.setProperty(
                "mail.smtp.starttls.required",
                String.valueOf(config.startTls()));
        propiedades.setProperty(
                "mail.smtp.ssl.enable",
                String.valueOf(config.ssl()));
        propiedades.setProperty(
                "mail.smtp.connectiontimeout",
                String.valueOf(config.timeoutMilisegundos()));
        propiedades.setProperty(
                "mail.smtp.timeout",
                String.valueOf(config.timeoutMilisegundos()));
        propiedades.setProperty(
                "mail.smtp.writetimeout",
                String.valueOf(config.timeoutMilisegundos()));

        Authenticator autenticador = config.requiereAutenticacion()
                ? new Authenticator() {
                    @Override
                    protected PasswordAuthentication
                            getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                config.usuario(), config.password());
                    }
                }
                : null;
        return Session.getInstance(propiedades, autenticador);
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
