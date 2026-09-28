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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import api.publica.DisponibilidadPublicaService.RecursoNoEncontradoException;
import api.publica.SolicitudWebPublicaService.ConflictoDisponibilidadException;

public final class ApiPublicaServer {
    private static final int PUERTO_PREDETERMINADO = 8080;
    private static final String PREFIJO = "/api/publica";

    private final DisponibilidadPublicaService disponibilidadService;
    private final SolicitudWebPublicaService solicitudWebService;
    private final ObjectMapper objectMapper;
    private HttpServer servidor;
    private ExecutorService ejecutor;

    public ApiPublicaServer() {
        this(new DisponibilidadPublicaService(),
                new SolicitudWebPublicaService());
    }

    public ApiPublicaServer(
            DisponibilidadPublicaService disponibilidadService,
            SolicitudWebPublicaService solicitudWebService) {
        if (disponibilidadService == null || solicitudWebService == null) {
            throw new IllegalArgumentException(
                    "Los servicios de la API no pueden ser nulos.");
        }
        this.disponibilidadService = disponibilidadService;
        this.solicitudWebService = solicitudWebService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public synchronized void iniciar() {
        if (servidor != null) return;
        int puerto = obtenerPuerto();
        try {
            servidor = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", puerto), 0);
            ejecutor = Executors.newVirtualThreadPerTaskExecutor();
            servidor.setExecutor(ejecutor);
            servidor.createContext(PREFIJO, this::procesar);
            servidor.start();
            System.out.println("API pública iniciada en http://localhost:"
                    + puerto + PREFIJO);
        } catch (IOException exception) {
            servidor = null;
            if (ejecutor != null) ejecutor.close();
            ejecutor = null;
            throw new IllegalStateException(
                    "No se pudo iniciar la API pública en el puerto "
                            + puerto + ".", exception);
        }
    }

    public synchronized void detener() {
        if (servidor != null) {
            servidor.stop(1);
            servidor = null;
        }
        if (ejecutor != null) {
            ejecutor.close();
            ejecutor = null;
        }
    }

    public synchronized boolean estaIniciada() { return servidor != null; }

    private void procesar(HttpExchange intercambio) throws IOException {
        agregarCabeceras(intercambio);
        if ("OPTIONS".equalsIgnoreCase(intercambio.getRequestMethod())) {
            intercambio.sendResponseHeaders(204, -1);
            intercambio.close();
            return;
        }
        String metodo = intercambio.getRequestMethod().toUpperCase();
        String ruta = normalizarRuta(intercambio.getRequestURI().getPath());
        try {
            if (ruta.equals(PREFIJO + "/solicitudes")
                    && "POST".equals(metodo)) {
                SolicitudWebDTO.CrearSolicitud entrada = objectMapper.readValue(
                        intercambio.getRequestBody(),
                        SolicitudWebDTO.CrearSolicitud.class);
                responderJson(intercambio, 201,
                        solicitudWebService.crear(entrada));
                return;
            }
            if (!"GET".equals(metodo)) {
                responderError(intercambio, 405, "METODO_NO_PERMITIDO",
                        "El método solicitado no está permitido.");
                return;
            }
            if (ruta.equals(PREFIJO) || ruta.equals(PREFIJO + "/estado")) {
                responderJson(intercambio, 200, new ApiPublicaDTO.EstadoApi(
                        "api-publica-padel", "disponible",
                        LocalDateTime.now().toString()));
                return;
            }
            if (ruta.equals(PREFIJO + "/complejo")) {
                responderJson(intercambio, 200,
                        disponibilidadService.obtenerComplejo());
                return;
            }
            if (ruta.equals(PREFIJO + "/canchas")) {
                responderJson(intercambio, 200,
                        disponibilidadService.listarCanchas());
                return;
            }
            if (ruta.startsWith(PREFIJO + "/canchas/")) {
                long id = parsearId(ruta.substring(
                        (PREFIJO + "/canchas/").length()));
                responderJson(intercambio, 200,
                        disponibilidadService.buscarCancha(id));
                return;
            }
            if (ruta.equals(PREFIJO + "/disponibilidad")) {
                Map<String, String> parametros = parametros(
                        intercambio.getRequestURI());
                long canchaId = parsearId(parametros.get("canchaId"));
                LocalDate fecha = parsearFecha(parametros.get("fecha"));
                responderJson(intercambio, 200,
                        disponibilidadService.obtenerDisponibilidad(
                                canchaId, fecha));
                return;
            }
            responderError(intercambio, 404, "RECURSO_NO_ENCONTRADO",
                    "El recurso solicitado no existe.");
        } catch (RecursoNoEncontradoException exception) {
            responderError(intercambio, 404, "RECURSO_NO_ENCONTRADO",
                    exception.getMessage());
        } catch (ConflictoDisponibilidadException exception) {
            responderError(intercambio, 409, "TURNO_NO_DISPONIBLE",
                    exception.getMessage());
        } catch (JacksonException exception) {
            responderError(intercambio, 400, "JSON_INVALIDO",
                    "El cuerpo JSON no tiene el formato esperado.");
        } catch (IllegalArgumentException exception) {
            responderError(intercambio, 400, "SOLICITUD_INVALIDA",
                    exception.getMessage());
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            responderError(intercambio, 500, "ERROR_INTERNO",
                    "No se pudo procesar la solicitud.");
        }
    }

    private long parsearId(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El parámetro canchaId es obligatorio.");
        }
        try {
            long id = Long.parseLong(valor);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "El ID de la cancha debe ser un número positivo.");
        }
    }

    private LocalDate parsearFecha(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El parámetro fecha es obligatorio.");
        }
        try {
            return LocalDate.parse(valor);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "La fecha debe tener formato AAAA-MM-DD.");
        }
    }

    private Map<String, String> parametros(URI uri) {
        Map<String, String> resultado = new HashMap<>();
        String consulta = uri.getRawQuery();
        if (consulta == null || consulta.isBlank()) return resultado;
        for (String parte : consulta.split("&")) {
            String[] componentes = parte.split("=", 2);
            resultado.put(decodificar(componentes[0]),
                    componentes.length > 1
                            ? decodificar(componentes[1]) : "");
        }
        return resultado;
    }

    private String decodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    private String normalizarRuta(String ruta) {
        if (ruta == null || ruta.isBlank()) return "/";
        return ruta.length() > 1 && ruta.endsWith("/")
                ? ruta.substring(0, ruta.length() - 1) : ruta;
    }

    private void responderError(HttpExchange intercambio, int estado,
            String codigo, String mensaje) throws IOException {
        responderJson(intercambio, estado, new ApiPublicaDTO.ErrorApi(
                estado, codigo, mensaje,
                intercambio.getRequestURI().getPath(),
                LocalDateTime.now().toString()));
    }

    private void responderJson(HttpExchange intercambio, int estado,
            Object cuerpo) throws IOException {
        byte[] contenido = objectMapper.writeValueAsBytes(cuerpo);
        intercambio.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");
        intercambio.sendResponseHeaders(estado, contenido.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(contenido);
        }
    }

    private void agregarCabeceras(HttpExchange intercambio) {
        String origenPermitido = System.getProperty(
                "api.cors.origen", "http://localhost:5173");
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Origin", origenPermitido);
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        intercambio.getResponseHeaders().set(
                "Access-Control-Allow-Headers", "Content-Type");
        intercambio.getResponseHeaders().set("Cache-Control", "no-store");
        intercambio.getResponseHeaders().set(
                "X-Content-Type-Options", "nosniff");
    }

    private int obtenerPuerto() {
        String valor = System.getProperty("api.port");
        if (valor == null || valor.isBlank()) return PUERTO_PREDETERMINADO;
        try {
            int puerto = Integer.parseInt(valor);
            if (puerto < 1 || puerto > 65535) throw new NumberFormatException();
            return puerto;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "La propiedad api.port debe contener un puerto válido.");
        }
    }
}
