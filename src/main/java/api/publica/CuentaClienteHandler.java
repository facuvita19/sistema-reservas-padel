package api.publica;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import api.publica.CuentaClienteDTO.ActualizarPerfil;
import api.publica.CuentaClienteDTO.CambiarPassword;
import api.publica.CuentaClienteDTO.ActualizarPreferenciasDeportivas;
import api.publica.PreferenciasComunidadDTO.Actualizar;
import servicio.PreferenciasComunidadService;
import servicio.PreferenciasComunidadService.ConflictoPreferenciasComunidadException;
import servicio.IdentidadDeportivaService;
import servicio.AutenticacionClienteService;
import servicio.AutenticacionClienteService.SesionAutenticada;
import servicio.CuentaClienteService;
import api.publica.PagoClienteDTO.OrdenPagoPublica;
import servicio.CuentaClienteService.ConflictoPerfilException;
import servicio.CuentaClienteService.RecursoClienteNoEncontradoException;

public final class CuentaClienteHandler {

    private static final String PREFIJO = "/api/publica/cliente";

    private final AutenticacionClienteService autenticacionService;
    private final CuentaClienteService cuentaService;
    private final ObjectMapper mapper;
    private final PagoClienteCuentaService pagoCuentaService;
    private final JugadorPublicoService jugadorPublicoService;
    private final IdentidadDeportivaService identidadDeportivaService;
    private final PreferenciasComunidadService preferenciasComunidadService;

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
        this.pagoCuentaService = new PagoClienteCuentaService();
        this.jugadorPublicoService = new JugadorPublicoService();
        this.identidadDeportivaService = new IdentidadDeportivaService();
        this.preferenciasComunidadService = new PreferenciasComunidadService();
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

            if (ruta.equals(PREFIJO + "/perfil-deportivo")
                    && "GET".equals(metodo)) {
                responder(intercambio, 200,
                        jugadorPublicoService.buscarPorCliente(
                                autenticada.cliente().getId()));
                return;
            }

            if (ruta.equals(PREFIJO + "/perfil-deportivo/preferencias")
                    && "PUT".equals(metodo)) {
                actualizarPreferenciasDeportivas(intercambio, autenticada);
                return;
            }

            if (ruta.equals(PREFIJO + "/comunidad/preferencias")
                    && "GET".equals(metodo)) {
                responder(intercambio, 200, preferenciasComunidadService.obtener(
                        autenticada.cliente().getId()));
                return;
            }

            if (ruta.equals(PREFIJO + "/comunidad/preferencias")
                    && "PUT".equals(metodo)) {
                Actualizar entrada = mapper.readValue(
                        intercambio.getRequestBody(), Actualizar.class);
                responder(intercambio, 200, preferenciasComunidadService.actualizar(
                        autenticada.cliente().getId(), entrada));
                return;
            }

            if (ruta.equals(PREFIJO + "/perfil")
                    && "PUT".equals(metodo)) {
                actualizarPerfil(intercambio, autenticada);
                return;
            }

            // cambiarPasswordClienteEndpointV1
            if (ruta.equals(PREFIJO + "/password")
                    && "PUT".equals(metodo)) {
                cambiarPassword(intercambio, autenticada);
                return;
            }

            if (ruta.equals(PREFIJO + "/pagos/capacidades") && "GET".equals(metodo)) {
                responder(intercambio, 200, pagoCuentaService.capacidades());
                return;
            }

            String pagoBase = PREFIJO + "/reservas/";
            if (ruta.startsWith(pagoBase) && ruta.endsWith("/pago")) {
                String valor = ruta.substring(pagoBase.length(), ruta.length() - "/pago".length());
                long reservaId = parsearId(valor);
                if ("GET".equals(metodo)) {
                    OrdenPagoPublica actual = pagoCuentaService.actual(autenticada.cliente().getId(), reservaId);
                    responder(intercambio, 200, actual == null ? new SinOrdenPago("La reserva todavía no tiene una orden de pago.") : actual);
                    return;
                }
                if ("POST".equals(metodo)) {
                    byte[] cuerpo = intercambio.getRequestBody().readAllBytes();
                    PagoClienteDTO.CrearOrdenPago entrada = cuerpo.length == 0 ? new PagoClienteDTO.CrearOrdenPago("TRANSFERENCIA_MANUAL") : mapper.readValue(cuerpo, PagoClienteDTO.CrearOrdenPago.class);
                    responder(intercambio, 201, pagoCuentaService.crear(autenticada.cliente().getId(), reservaId, entrada.metodo()));
                    return;
                }
            }

            if (ruta.startsWith(pagoBase) && ruta.endsWith("/pagos") && "GET".equals(metodo)) {
                String valor = ruta.substring(pagoBase.length(), ruta.length() - "/pagos".length());
                responder(intercambio, 200, pagoCuentaService.historial(autenticada.cliente().getId(), parsearId(valor)));
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
        } catch (JugadorPublicoService.PerfilPublicoNoEncontradoException exception) {
            error(intercambio, 404, "PERFIL_DEPORTIVO_NO_VINCULADO",
                    "Tu cuenta todavía no tiene un perfil deportivo vinculado.",
                    operacionId);
        } catch (RecursoClienteNoEncontradoException exception) {
            error(intercambio, 404, "RECURSO_NO_ENCONTRADO",
                    exception.getMessage(), operacionId);
        } catch (ConflictoPerfilException exception) {
            error(intercambio, 409, "CONFLICTO_PERFIL",
                    exception.getMessage(), operacionId);
        } catch (ConflictoPreferenciasComunidadException exception) {
            error(intercambio, 409, "CONFLICTO_PREFERENCIAS_COMUNIDAD",
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

    private void cambiarPassword(
            HttpExchange intercambio,
            SesionAutenticada autenticada) throws IOException {
        CambiarPassword entrada = mapper.readValue(
                intercambio.getRequestBody(),
                CambiarPassword.class);
        cuentaService.cambiarPassword(
                autenticada.usuario().getId(),
                entrada);
        responder(intercambio, 200,
                new MensajeCuenta(
                        "La contrasena fue actualizada correctamente."));
    }

    private void actualizarPreferenciasDeportivas(
            HttpExchange intercambio,
            SesionAutenticada autenticada) throws IOException {
        ActualizarPreferenciasDeportivas entrada = mapper.readValue(
                intercambio.getRequestBody(),
                ActualizarPreferenciasDeportivas.class);
        if (entrada.disponibleParaJugar() == null
                || entrada.aceptaInvitaciones() == null) {
            throw new IllegalArgumentException(
                    "Las dos preferencias deportivas son obligatorias.");
        }
        identidadDeportivaService.actualizarPreferenciasPorCliente(
                autenticada.cliente().getId(),
                entrada.disponibleParaJugar(),
                entrada.aceptaInvitaciones());
        responder(intercambio, 200,
                jugadorPublicoService.buscarPorCliente(
                        autenticada.cliente().getId()));
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

    private record MensajeCuenta(String mensaje) {
    }

    private record SinOrdenPago(String mensaje) {
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
