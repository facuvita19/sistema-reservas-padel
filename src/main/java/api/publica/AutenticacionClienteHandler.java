package api.publica;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import api.publica.AutenticacionClienteDTO.Login;
import api.publica.AutenticacionClienteDTO.Mensaje;
import api.publica.AutenticacionClienteDTO.Registro;
import api.publica.AutenticacionClienteDTO.Sesion;
import servicio.AutenticacionClienteService;
import servicio.AutenticacionClienteService.ClienteInactivoException;
import servicio.AutenticacionClienteService.ConflictoIdentidadException;
import servicio.AutenticacionClienteService.CuentaExistenteException;
import servicio.AutenticacionClienteService.RegistroCliente;
import servicio.AutenticacionClienteService.ResultadoAutenticacion;
import servicio.AutenticacionClienteService.SesionAutenticada;
import servicio.SesionClienteService;
import servicio.UsuarioService.CredencialesInvalidasException;

public final class AutenticacionClienteHandler {

    private static final String PREFIJO = "/api/publica/auth";

    private final AutenticacionClienteService autenticacionService;
    private final SesionClienteService sesionService;
    private final SeguridadApiPublica seguridad;
    private final ObjectMapper mapper;

    public AutenticacionClienteHandler(
            AutenticacionClienteService autenticacionService,
            SesionClienteService sesionService,
            SeguridadApiPublica seguridad,
            ObjectMapper mapper) {
        if (autenticacionService == null
                || sesionService == null
                || seguridad == null
                || mapper == null) {
            throw new IllegalArgumentException(
                    "Las dependencias del handler no pueden ser nulas.");
        }
        this.autenticacionService = autenticacionService;
        this.sesionService = sesionService;
        this.seguridad = seguridad;
        this.mapper = mapper;
    }

    public boolean puedeProcesar(String ruta) {
        return ruta != null
                && (ruta.equals(PREFIJO)
                        || ruta.startsWith(PREFIJO + "/"));
    }

    public void procesar(
            HttpExchange intercambio,
            String ruta,
            String metodo,
            String ip,
            String operacionId) throws IOException {
        try {
            if (ruta.equals(PREFIJO + "/registro")
                    && "POST".equals(metodo)) {
                registrar(intercambio, ip);
                return;
            }

            if (ruta.equals(PREFIJO + "/login")
                    && "POST".equals(metodo)) {
                iniciarSesion(intercambio, ip);
                return;
            }

            if (ruta.equals(PREFIJO + "/sesion")
                    && "GET".equals(metodo)) {
                consultarSesion(intercambio);
                return;
            }

            if (ruta.equals(PREFIJO + "/logout")
                    && "POST".equals(metodo)) {
                cerrarSesion(intercambio);
                return;
            }

            if (!"GET".equals(metodo) && !"POST".equals(metodo)) {
                error(intercambio, 405, "METODO_NO_PERMITIDO",
                        "El metodo solicitado no esta permitido.",
                        operacionId);
                return;
            }

            error(intercambio, 404, "RECURSO_NO_ENCONTRADO",
                    "El recurso de autenticacion no existe.",
                    operacionId);
        } catch (CredencialesInvalidasException exception) {
            error(intercambio, 401, "CREDENCIALES_INVALIDAS",
                    exception.getMessage(), operacionId);
        } catch (CuentaExistenteException exception) {
            error(intercambio, 409, "CUENTA_EXISTENTE",
                    exception.getMessage(), operacionId);
        } catch (ConflictoIdentidadException exception) {
            error(intercambio, 409, "CONFLICTO_IDENTIDAD",
                    exception.getMessage(), operacionId);
        } catch (ClienteInactivoException exception) {
            error(intercambio, 403, "CLIENTE_INACTIVO",
                    exception.getMessage(), operacionId);
        } catch (JacksonException exception) {
            error(intercambio, 400, "JSON_INVALIDO",
                    "El cuerpo JSON no tiene el formato esperado.",
                    operacionId);
        } catch (IllegalArgumentException exception) {
            error(intercambio, 400, "SOLICITUD_INVALIDA",
                    exception.getMessage(), operacionId);
        }
    }

    private void registrar(
            HttpExchange intercambio,
            String ip) throws IOException {
        seguridad.validarRegistro(ip);
        Registro entrada = leer(intercambio, Registro.class);

        ResultadoAutenticacion resultado =
                autenticacionService.registrar(
                        new RegistroCliente(
                                entrada.nombre(),
                                entrada.apellido(),
                                entrada.documento(),
                                entrada.telefono(),
                                entrada.email(),
                                entrada.password()));

        escribirCookie(intercambio, resultado.token());
        responder(intercambio, 201, Sesion.desde(resultado));
    }

    private void iniciarSesion(
            HttpExchange intercambio,
            String ip) throws IOException {
        Login entrada = leer(intercambio, Login.class);
        seguridad.validarLogin(ip, entrada.email());

        ResultadoAutenticacion resultado =
                autenticacionService.iniciarSesion(
                        entrada.email(), entrada.password());

        escribirCookie(intercambio, resultado.token());
        responder(intercambio, 200, Sesion.desde(resultado));
    }

    private void consultarSesion(HttpExchange intercambio)
            throws IOException {
        String token = CookieSesionCliente.leer(intercambio);
        SesionAutenticada autenticada =
                autenticacionService.obtenerSesion(token);

        if (autenticada == null) {
            CookieSesionCliente.eliminar(
                    intercambio,
                    CookieSesionCliente.debeSerSegura());
            responder(intercambio, 200, Sesion.noAutenticada());
            return;
        }

        responder(intercambio, 200, Sesion.desde(autenticada));
    }

    private void cerrarSesion(HttpExchange intercambio)
            throws IOException {
        String token = CookieSesionCliente.leer(intercambio);
        autenticacionService.cerrarSesion(token);
        CookieSesionCliente.eliminar(
                intercambio,
                CookieSesionCliente.debeSerSegura());
        responder(intercambio, 200,
                new Mensaje("La sesion fue cerrada correctamente."));
    }

    private void escribirCookie(
            HttpExchange intercambio,
            String token) {
        Duration duracion = sesionService.getDuracionSesion();
        CookieSesionCliente.escribir(
                intercambio,
                token,
                duracion,
                CookieSesionCliente.debeSerSegura());
    }

    private <T> T leer(
            HttpExchange intercambio,
            Class<T> tipo) throws IOException {
        byte[] cuerpo = seguridad.leerCuerpoLimitado(
                intercambio.getRequestBody());
        return mapper.readValue(cuerpo, tipo);
    }

    private void error(
            HttpExchange intercambio,
            int estado,
            String codigo,
            String mensaje,
            String operacionId) throws IOException {
        responder(intercambio, estado, new ErrorAutenticacion(
                estado,
                codigo,
                mensaje,
                intercambio.getRequestURI().getPath(),
                operacionId));
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

    private record ErrorAutenticacion(
            int estado,
            String codigo,
            String mensaje,
            String ruta,
            String operacionId) {
    }
}
