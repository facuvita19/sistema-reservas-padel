package api.publica;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import api.publica.CuentaClienteDTO.ActualizarPerfil;
import servicio.AutenticacionClienteService;
import servicio.AutenticacionClienteService.SesionAutenticada;
import servicio.CuentaClienteService;
import servicio.CuentaClienteService.ConflictoPerfilException;
import servicio.CuentaClienteService.RecursoClienteNoEncontradoException;

public final class CuentaClienteHandler {

    private static final String PREFIJO = "/api/publica/cliente";

    private final AutenticacionClienteService autenticacionService;
    private final CuentaClienteService cuentaService;
    private final ObjectMapper mapper;

    public CuentaClienteHandler(
            AutenticacionClienteService autenticacionService,
            CuentaClienteService cuentaService,
            ObjectMapper mapper) {
        if (autenticacionService == null
                || cuentaService == null
                || mapper == null) {
            throw new IllegalArgumentException(
                    "Las dependencias del handler no pueden ser nulas.");
        }
        this.autenticacionService = autenticacionService;
        this.cuentaService = cuentaService;
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
            String operacionId) throws IOException {
        try {
            SesionAutenticada autenticada = autenticar(intercambio);

            if (ruta.equals(PREFIJO + "/perfil")
                    && "GET".equals(metodo)) {
                responder(intercambio, 200,
                        cuentaService.obtenerPerfil(
                                autenticada.cliente()));
                return;
            }

            if (ruta.equals(PREFIJO + "/perfil")
                    && "PUT".equals(metodo)) {
                actualizarPerfil(intercambio, autenticada);
                return;
            }

            if (!"GET".equals(metodo)) {
                error(intercambio, 405, "METODO_NO_PERMITIDO",
                        "El metodo solicitado no esta permitido.",
                        operacionId);
                return;
            }

            if (ruta.equals(PREFIJO + "/reservas")) {
                responder(intercambio, 200,
                        cuentaService.listarReservas(
                                autenticada.cliente().getId()));
                return;
            }

            String prefijoDetalle = PREFIJO + "/reservas/";
            if (ruta.startsWith(prefijoDetalle)) {
                long reservaId = parsearId(
                        ruta.substring(prefijoDetalle.length()));
                responder(intercambio, 200,
                        cuentaService.buscarReserva(
                                autenticada.cliente().getId(),
                                reservaId));
                return;
            }

            error(intercambio, 404, "RECURSO_NO_ENCONTRADO",
                    "El recurso de la cuenta no existe.",
                    operacionId);
        } catch (NoAutenticadoException exception) {
            CookieSesionCliente.eliminar(
                    intercambio,
                    CookieSesionCliente.debeSerSegura());
            error(intercambio, 401, "NO_AUTENTICADO",
                    exception.getMessage(), operacionId);
        } catch (RecursoClienteNoEncontradoException exception) {
            error(intercambio, 404, "RECURSO_NO_ENCONTRADO",
                    exception.getMessage(), operacionId);
        } catch (ConflictoPerfilException exception) {
            error(intercambio, 409, "CONFLICTO_PERFIL",
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

    private void actualizarPerfil(
            HttpExchange intercambio,
            SesionAutenticada autenticada) throws IOException {
        ActualizarPerfil entrada = mapper.readValue(
                intercambio.getRequestBody(),
                ActualizarPerfil.class);
        responder(intercambio, 200,
                cuentaService.actualizarPerfil(
                        autenticada.cliente().getId(),
                        entrada));
    }

    private SesionAutenticada autenticar(
            HttpExchange intercambio) {
        String token = CookieSesionCliente.leer(intercambio);
        SesionAutenticada autenticada =
                autenticacionService.obtenerSesion(token);
        if (autenticada == null) {
            throw new NoAutenticadoException();
        }
        return autenticada;
    }

    private long parsearId(String valor) {
        try {
            long id = Long.parseLong(valor);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "El ID de la reserva debe ser positivo.");
        }
    }

    private void error(
            HttpExchange intercambio,
            int estado,
            String codigo,
            String mensaje,
            String operacionId) throws IOException {
        responder(intercambio, estado, new ErrorCuenta(
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

    private record ErrorCuenta(
            int estado,
            String codigo,
            String mensaje,
            String ruta,
            String operacionId) {
    }

    public static final class NoAutenticadoException
            extends RuntimeException {

        public NoAutenticadoException() {
            super("Debes iniciar sesion para acceder a este recurso.");
        }
    }
}
