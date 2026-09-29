package api.publica;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import api.publica.RecuperacionPasswordClienteDTO.EstadoToken;
import api.publica.RecuperacionPasswordClienteDTO.Mensaje;
import api.publica.RecuperacionPasswordClienteDTO.RespuestaSolicitud;
import api.publica.RecuperacionPasswordClienteDTO.RestablecerPassword;
import api.publica.RecuperacionPasswordClienteDTO.SolicitarRecuperacion;
import servicio.RecuperacionPasswordClienteService;
import servicio.RecuperacionPasswordClienteService.SolicitudRecuperacion;
import servicio.RecuperacionPasswordClienteService.TokenRecuperacionInvalidoException;

public final class RecuperacionPasswordClienteHandler {

    private static final String PREFIJO = "/api/publica/auth";
    private static final String MENSAJE_NEUTRAL =
            "Si existe una cuenta asociada, recibiras instrucciones para continuar.";

    private final RecuperacionPasswordClienteService recuperacionService;
    private final SeguridadApiPublica seguridad;
    private final ObjectMapper mapper;

    public RecuperacionPasswordClienteHandler(
            RecuperacionPasswordClienteService recuperacionService,
            SeguridadApiPublica seguridad,
            ObjectMapper mapper) {
        if (recuperacionService == null || seguridad == null || mapper == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de recuperacion no pueden ser nulas.");
        }
        this.recuperacionService = recuperacionService;
        this.seguridad = seguridad;
        this.mapper = mapper;
    }

    public boolean puedeProcesar(String ruta) {
        return (PREFIJO + "/recuperar").equals(ruta)
                || (PREFIJO + "/restablecer").equals(ruta)
                || (PREFIJO + "/recuperacion").equals(ruta);
    }

    public void procesar(
            HttpExchange intercambio,
            String ruta,
            String metodo,
            String ip,
            String operacionId) throws IOException {
        try {
            if ((PREFIJO + "/recuperar").equals(ruta)
                    && "POST".equals(metodo)) {
                solicitar(intercambio, ip);
                return;
            }
            if ((PREFIJO + "/restablecer").equals(ruta)
                    && "POST".equals(metodo)) {
                restablecer(intercambio, ip);
                return;
            }
            if ((PREFIJO + "/recuperacion").equals(ruta)
                    && "GET".equals(metodo)) {
                consultarToken(intercambio);
                return;
            }
            error(intercambio, 405, "METODO_NO_PERMITIDO",
                    "El metodo solicitado no esta permitido.", operacionId);
        } catch (TokenRecuperacionInvalidoException exception) {
            error(intercambio, 400, "TOKEN_RECUPERACION_INVALIDO",
                    exception.getMessage(), operacionId);
        } catch (JacksonException exception) {
            error(intercambio, 400, "JSON_INVALIDO",
                    "El cuerpo JSON no tiene el formato esperado.", operacionId);
        } catch (IllegalArgumentException exception) {
            error(intercambio, 400, "SOLICITUD_INVALIDA",
                    exception.getMessage(), operacionId);
        }
    }

    private void solicitar(HttpExchange intercambio, String ip)
            throws IOException {
        seguridad.validarCreacion(ip);
        SolicitarRecuperacion entrada = leer(
                intercambio, SolicitarRecuperacion.class);
        SolicitudRecuperacion resultado =
                recuperacionService.solicitar(entrada.email());

        String enlace = null;
        java.time.LocalDateTime vencimiento = null;
        if (mostrarTokenPrueba() && resultado.generada()) {
            enlace = urlPublica() + "?recuperar=" + resultado.token();
            vencimiento = resultado.vencimiento();
        }
        responder(intercambio, 200,
                new RespuestaSolicitud(MENSAJE_NEUTRAL, enlace, vencimiento));
    }

    private void restablecer(HttpExchange intercambio, String ip)
            throws IOException {
        seguridad.validarCreacion(ip);
        RestablecerPassword entrada = leer(
                intercambio, RestablecerPassword.class);
        recuperacionService.restablecer(
                entrada.token(),
                entrada.passwordNuevo(),
                entrada.passwordRepetido());
        responder(intercambio, 200,
                new Mensaje("La contrasena fue restablecida correctamente."));
    }

    private void consultarToken(HttpExchange intercambio)
            throws IOException {
        String token = parametros(intercambio).get("token");
        responder(intercambio, 200,
                new EstadoToken(recuperacionService.tokenVigente(token)));
    }

    private <T> T leer(HttpExchange intercambio, Class<T> tipo)
            throws IOException {
        byte[] cuerpo = seguridad.leerCuerpoLimitado(
                intercambio.getRequestBody());
        return mapper.readValue(cuerpo, tipo);
    }

    private Map<String, String> parametros(HttpExchange intercambio) {
        Map<String, String> resultado = new HashMap<>();
        String consulta = intercambio.getRequestURI().getRawQuery();
        if (consulta == null || consulta.isBlank()) return resultado;
        for (String parte : consulta.split("&")) {
            String[] par = parte.split("=", 2);
            String clave = URLDecoder.decode(
                    par[0], StandardCharsets.UTF_8);
            String valor = par.length > 1
                    ? URLDecoder.decode(par[1], StandardCharsets.UTF_8)
                    : "";
            resultado.put(clave, valor);
        }
        return resultado;
    }

    private boolean mostrarTokenPrueba() {
        String valor = System.getProperty("api.recuperacion.mostrarToken");
        if (valor == null || valor.isBlank()) {
            valor = System.getenv("API_RECUPERACION_MOSTRAR_TOKEN");
        }
        return valor != null && Boolean.parseBoolean(valor.trim());
    }

    private String urlPublica() {
        String valor = System.getProperty("api.web.publica.url");
        if (valor == null || valor.isBlank()) {
            valor = System.getenv("API_WEB_PUBLICA_URL");
        }
        return valor == null || valor.isBlank()
                ? "http://localhost:5173/"
                : valor.trim();
    }

    private void error(
            HttpExchange intercambio,
            int estado,
            String codigo,
            String mensaje,
            String operacionId) throws IOException {
        responder(intercambio, estado, new ErrorRecuperacion(
                estado, codigo, mensaje,
                intercambio.getRequestURI().getPath(), operacionId));
    }

    private void responder(
            HttpExchange intercambio,
            int estado,
            Object cuerpo) throws IOException {
        byte[] datos = mapper.writeValueAsBytes(cuerpo);
        intercambio.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        intercambio.sendResponseHeaders(estado, datos.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(datos);
        }
    }

    private record ErrorRecuperacion(
            int estado,
            String codigo,
            String mensaje,
            String ruta,
            String operacionId) {
    }
}
