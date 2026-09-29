package api.publica;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import api.publica.DisponibilidadPublicaService.RecursoNoEncontradoException;
import api.publica.SeguridadApiPublica.CuerpoDemasiadoGrandeException;
import api.publica.SeguridadApiPublica.DemasiadasSolicitudesException;
import api.publica.SolicitudWebPublicaService.ConflictoDisponibilidadException;
import servicio.AutenticacionClienteService;
import servicio.CuentaClienteService;
import servicio.SesionClienteService;

public final class ApiPublicaServer {
    private static final int PUERTO = 8080;
    private static final String PREFIJO = "/api/publica";

    private final DisponibilidadPublicaService disponibilidadService;
    private final SolicitudWebPublicaService solicitudWebService;
    private final EstadoSolicitudWebPublicaService estadoService;
    private final SeguridadApiPublica seguridad = new SeguridadApiPublica();
    private final ObjectMapper mapper;
    private final AutenticacionClienteService autenticacionClienteService;
    private final AutenticacionClienteHandler autenticacionHandler;
    private final CuentaClienteHandler cuentaClienteHandler;
    private HttpServer servidor;
    private ExecutorService ejecutor;

    public ApiPublicaServer() {
        this(new DisponibilidadPublicaService(),
                new SolicitudWebPublicaService(),
                new EstadoSolicitudWebPublicaService());
    }

    public ApiPublicaServer(
            DisponibilidadPublicaService disponibilidadService,
            SolicitudWebPublicaService solicitudWebService,
            EstadoSolicitudWebPublicaService estadoService) {
        if (disponibilidadService == null || solicitudWebService == null
                || estadoService == null) {
            throw new IllegalArgumentException(
                    "Los servicios de la API no pueden ser nulos.");
        }
        this.disponibilidadService = disponibilidadService;
        this.solicitudWebService = solicitudWebService;
        this.estadoService = estadoService;
        mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SesionClienteService sesionClienteService =
                new SesionClienteService();
        autenticacionClienteService =
                new AutenticacionClienteService();
        autenticacionHandler = new AutenticacionClienteHandler(
                autenticacionClienteService,
                sesionClienteService,
                seguridad,
                mapper);
        cuentaClienteHandler = new CuentaClienteHandler(
                autenticacionClienteService,
                new CuentaClienteService(),
                mapper);
    }

    public synchronized void iniciar() {
        if (servidor != null) return;
        int puerto = obtenerPuerto();
        try {
            servidor = HttpServer.create(
                    new InetSocketAddress("0.0.0.0", puerto), 0);
            ejecutor = Executors.newVirtualThreadPerTaskExecutor();
            servidor.setExecutor(ejecutor);
            servidor.createContext(PREFIJO, this::procesar);
            servidor.start();
            System.out.println("API pública iniciada en http://localhost:"
                    + puerto + PREFIJO);
        } catch (IOException exception) {
            detener();
            throw new IllegalStateException(
                    "No se pudo iniciar la API pública.", exception);
        }
    }

    public synchronized void detener() {
        if (servidor != null) servidor.stop(1);
        servidor = null;
        if (ejecutor != null) ejecutor.close();
        ejecutor = null;
    }

    public synchronized boolean estaIniciada() { return servidor != null; }

    private void procesar(HttpExchange x) throws IOException {
        String operacionId = UUID.randomUUID().toString();
        cabeceras(x, operacionId);
        if ("OPTIONS".equalsIgnoreCase(x.getRequestMethod())) {
            x.sendResponseHeaders(204, -1);
            x.close();
            return;
        }

        String ip = seguridad.normalizarIp(x.getRemoteAddress().getAddress());
        String metodo = x.getRequestMethod().toUpperCase();
        String ruta = normalizar(x.getRequestURI().getPath());
        try {
            seguridad.validarGeneral(ip);

            if (autenticacionHandler.puedeProcesar(ruta)) {
                autenticacionHandler.procesar(
                        x, ruta, metodo, ip, operacionId);
                return;
            }

            if (cuentaClienteHandler.puedeProcesar(ruta)) {
                cuentaClienteHandler.procesar(
                        x, ruta, metodo, operacionId);
                return;
            }

            if (ruta.equals(PREFIJO + "/solicitudes")
                    && "POST".equals(metodo)) {
                seguridad.validarCreacion(ip);
                byte[] cuerpo = seguridad.leerCuerpoLimitado(
                        x.getRequestBody());
                SolicitudWebDTO.CrearSolicitud entrada = mapper.readValue(
                        cuerpo, SolicitudWebDTO.CrearSolicitud.class);
                Long clienteIdAutenticado =
                        obtenerClienteAutenticadoParaSolicitud(x);
                SolicitudWebDTO.SolicitudCreada creada =
                        clienteIdAutenticado == null
                                ? solicitudWebService.crear(entrada)
                                : solicitudWebService.crear(
                                        entrada,
                                        clienteIdAutenticado);
                String codigo = estadoService.obtenerCodigo(
                        creada.solicitudId());
                responder(x, 201,
                        new EstadoSolicitudWebDTO.SolicitudCreadaPublica(
                                creada.solicitudId(), codigo, creada.estado(),
                                creada.clienteId(), creada.canchaId(),
                                creada.cancha(), creada.fecha(),
                                creada.horaInicio(), creada.horaFin(),
                                creada.precioTotal(), creada.importeSenia(),
                                creada.vencimiento(), creada.minutosParaPagar(),
                                creada.moneda(), creada.mensaje()));
                return;
            }

            if (ruta.startsWith(PREFIJO + "/solicitudes/")
                    && "GET".equals(metodo)) {
                String codigo = ruta.substring(
                        (PREFIJO + "/solicitudes/").length());
                responder(x, 200, estadoService.consultar(codigo));
                return;
            }

            if (!"GET".equals(metodo)) {
                error(x, 405, "METODO_NO_PERMITIDO",
                        "El método solicitado no está permitido.", operacionId);
                return;
            }

            if (ruta.equals(PREFIJO)
                    || ruta.equals(PREFIJO + "/estado")) {
                responder(x, 200, new ApiPublicaDTO.EstadoApi(
                        "api-publica-padel", "disponible",
                        LocalDateTime.now().toString()));
            } else if (ruta.equals(PREFIJO + "/complejo")) {
                responder(x, 200, disponibilidadService.obtenerComplejo());
            } else if (ruta.equals(PREFIJO + "/canchas")) {
                responder(x, 200, disponibilidadService.listarCanchas());
            } else if (ruta.startsWith(PREFIJO + "/canchas/")) {
                responder(x, 200, disponibilidadService.buscarCancha(
                        id(ruta.substring(
                                (PREFIJO + "/canchas/").length()))));
            } else if (ruta.equals(PREFIJO + "/disponibilidad")) {
                Map<String, String> p = parametros(x.getRequestURI());
                responder(x, 200,
                        disponibilidadService.obtenerDisponibilidad(
                                id(p.get("canchaId")),
                                fecha(p.get("fecha"))));
            } else {
                error(x, 404, "RECURSO_NO_ENCONTRADO",
                        "El recurso solicitado no existe.", operacionId);
            }
        } catch (CuerpoDemasiadoGrandeException e) {
            error(x, 413, "CUERPO_DEMASIADO_GRANDE",
                    e.getMessage(), operacionId);
        } catch (DemasiadasSolicitudesException e) {
            x.getResponseHeaders().set(
                    "Retry-After", String.valueOf(e.getSegundosEspera()));
            error(x, 429, "DEMASIADOS_INTENTOS",
                    e.getMessage(), operacionId);
        } catch (RecursoNoEncontradoException e) {
            error(x, 404, "RECURSO_NO_ENCONTRADO",
                    e.getMessage(), operacionId);
        } catch (ConflictoDisponibilidadException e) {
            error(x, 409, "TURNO_NO_DISPONIBLE",
                    e.getMessage(), operacionId);
        } catch (JacksonException e) {
            error(x, 400, "JSON_INVALIDO",
                    "El cuerpo JSON no tiene el formato esperado.",
                    operacionId);
        } catch (IllegalArgumentException e) {
            error(x, 400, "SOLICITUD_INVALIDA",
                    e.getMessage(), operacionId);
        } catch (RuntimeException e) {
            System.err.println("[API " + operacionId + "] "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            error(x, 500, "ERROR_INTERNO",
                    "No se pudo procesar la solicitud. Referencia: "
                            + operacionId,
                    operacionId);
        }
    }

    private Long obtenerClienteAutenticadoParaSolicitud(
            HttpExchange intercambio) {
        String token = CookieSesionCliente.leer(intercambio);
        if (token == null || token.isBlank()) return null;

        AutenticacionClienteService.SesionAutenticada sesion =
                autenticacionClienteService.obtenerSesion(token);
        if (sesion == null) {
            CookieSesionCliente.eliminar(
                    intercambio,
                    CookieSesionCliente.debeSerSegura());
            return null;
        }
        return sesion.cliente().getId();
    }

    private long id(String valor) {
        try {
            long id = Long.parseLong(valor);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "El ID de la cancha debe ser un número positivo.");
        }
    }

    private LocalDate fecha(String valor) {
        try { return LocalDate.parse(valor); }
        catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "La fecha debe tener formato AAAA-MM-DD.");
        }
    }

    private Map<String, String> parametros(URI uri) {
        Map<String, String> resultado = new HashMap<>();
        if (uri.getRawQuery() == null) return resultado;
        for (String parte : uri.getRawQuery().split("&")) {
            String[] par = parte.split("=", 2);
            resultado.put(decodificar(par[0]),
                    par.length > 1 ? decodificar(par[1]) : "");
        }
        return resultado;
    }

    private String decodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    private String normalizar(String ruta) {
        return ruta != null && ruta.length() > 1 && ruta.endsWith("/")
                ? ruta.substring(0, ruta.length() - 1) : ruta;
    }

    private void error(HttpExchange x, int estado, String codigo,
            String mensaje, String operacionId) throws IOException {
        responder(x, estado, new ErrorSeguro(
                estado, codigo, mensaje, x.getRequestURI().getPath(),
                LocalDateTime.now().toString(), operacionId));
    }

    private void responder(HttpExchange x, int estado, Object cuerpo)
            throws IOException {
        byte[] datos = mapper.writeValueAsBytes(cuerpo);
        x.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        x.sendResponseHeaders(estado, datos.length);
        try (OutputStream salida = x.getResponseBody()) {
            salida.write(datos);
        }
    }

    private void cabeceras(HttpExchange x, String operacionId) {
        String origen = x.getRequestHeaders().getFirst("Origin");
        String permitido = System.getProperty("api.cors.origen", "local");
        if ("local".equalsIgnoreCase(permitido)) {
            if (origen != null && origen.matches(
                    "^http://(localhost|127\\.0\\.0\\.1|10\\.\\d+\\.\\d+\\.\\d+|192\\.168\\.\\d+\\.\\d+|172\\.(1[6-9]|2\\d|3[01])\\.\\d+\\.\\d+):5173$")) {
                x.getResponseHeaders().set(
                        "Access-Control-Allow-Origin", origen);
                x.getResponseHeaders().set("Vary", "Origin");
                x.getResponseHeaders().set(
                        "Access-Control-Allow-Credentials", "true");
            }
        } else {
            x.getResponseHeaders().set(
                    "Access-Control-Allow-Origin", permitido);
            x.getResponseHeaders().set(
                    "Access-Control-Allow-Credentials", "true");
        }
        x.getResponseHeaders().set("Access-Control-Allow-Methods",
                "GET, POST, PUT, OPTIONS");
        x.getResponseHeaders().set("Access-Control-Allow-Headers",
                "Content-Type");
        x.getResponseHeaders().set("Cache-Control", "no-store");
        x.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        x.getResponseHeaders().set("X-Frame-Options", "DENY");
        x.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        x.getResponseHeaders().set("Permissions-Policy",
                "camera=(), microphone=(), geolocation=()");
        x.getResponseHeaders().set("X-Operacion-Id", operacionId);
    }

    private int obtenerPuerto() {
        String valor = System.getProperty("api.port");
        if (valor == null || valor.isBlank()) return PUERTO;
        try {
            int puerto = Integer.parseInt(valor);
            if (puerto < 1 || puerto > 65535) {
                throw new NumberFormatException();
            }
            return puerto;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("api.port no es válido.");
        }
    }

    private record ErrorSeguro(
            int estado,
            String codigo,
            String mensaje,
            String ruta,
            String fechaHora,
            String operacionId) { }
}
