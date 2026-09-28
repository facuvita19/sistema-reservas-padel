package api.publica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

import api.publica.DisponibilidadPublicaService.RecursoNoEncontradoException;
import api.publica.SolicitudWebPublicaService.ConflictoDisponibilidadException;

@TestMethodOrder(OrderAnnotation.class)
class ApiPublicaServerTest {

    private static ApiPublicaServer server;
    private static DisponibilidadPublicaService disponibilidad;
    private static SolicitudWebPublicaService solicitudes;
    private static EstadoSolicitudWebPublicaService estados;
    private static HttpClient client;
    private static String baseUrl;

    @BeforeAll
    static void iniciarServidor() throws Exception {
        int puerto = puertoLibre();
        System.setProperty("api.port", String.valueOf(puerto));
        System.setProperty("api.cors.origen", "local");

        disponibilidad = Mockito.mock(
                DisponibilidadPublicaService.class);
        solicitudes = Mockito.mock(
                SolicitudWebPublicaService.class);
        estados = Mockito.mock(
                EstadoSolicitudWebPublicaService.class);

        server = new ApiPublicaServer(
                disponibilidad, solicitudes, estados);
        server.iniciar();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        baseUrl = "http://127.0.0.1:" + puerto + "/api/publica";
    }

    @AfterAll
    static void detenerServidor() {
        if (server != null) {
            server.detener();
        }
        System.clearProperty("api.port");
        System.clearProperty("api.cors.origen");
    }

    @BeforeEach
    void limpiarMocks() {
        reset(disponibilidad, solicitudes, estados);
    }

    @Test
    @Order(1)
    void estadoDevuelve200YCabecerasSeguras() throws Exception {
        HttpResponse<String> respuesta = enviar(
                HttpRequest.newBuilder(URI.create(baseUrl + "/estado"))
                        .GET().build());

        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("api-publica-padel"));
        assertEquals("nosniff", encabezado(
                respuesta, "X-Content-Type-Options"));
        assertEquals("DENY", encabezado(respuesta, "X-Frame-Options"));
        assertFalse(encabezado(respuesta, "X-Operacion-Id").isBlank());
    }

    @Test
    @Order(2)
    void corsAceptaLocalhost() throws Exception {
        HttpResponse<String> respuesta = enviar(
                HttpRequest.newBuilder(URI.create(baseUrl + "/estado"))
                        .header("Origin", "http://localhost:5173")
                        .GET().build());

        assertEquals(200, respuesta.statusCode());
        assertEquals("http://localhost:5173",
                encabezado(respuesta, "Access-Control-Allow-Origin"));
    }

    @Test
    @Order(3)
    void recursoInexistenteDevuelve404() throws Exception {
        HttpResponse<String> respuesta = enviar(
                HttpRequest.newBuilder(URI.create(baseUrl + "/inexistente"))
                        .GET().build());

        assertEquals(404, respuesta.statusCode());
        assertTrue(respuesta.body().contains("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    @Order(4)
    void metodoNoPermitidoDevuelve405() throws Exception {
        HttpResponse<String> respuesta = enviar(
                HttpRequest.newBuilder(URI.create(baseUrl + "/canchas"))
                        .POST(HttpRequest.BodyPublishers.noBody()).build());

        assertEquals(405, respuesta.statusCode());
        assertTrue(respuesta.body().contains("METODO_NO_PERMITIDO"));
    }

    @Test
    @Order(5)
    void jsonInvalidoDevuelve400() throws Exception {
        HttpResponse<String> respuesta = post("{json-invalido");

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("JSON_INVALIDO"));
    }

    @Test
    @Order(6)
    void codigoConFormatoInvalidoDevuelve400() throws Exception {
        when(estados.consultar("codigo-invalido"))
                .thenThrow(new IllegalArgumentException(
                        "El código de seguimiento no tiene un formato válido."));

        HttpResponse<String> respuesta = enviar(
                HttpRequest.newBuilder(URI.create(
                        baseUrl + "/solicitudes/codigo-invalido"))
                        .GET().build());

        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("SOLICITUD_INVALIDA"));
    }

    @Test
    @Order(7)
    void codigoInexistenteDevuelve404() throws Exception {
        String codigo = UUID.randomUUID().toString();
        when(estados.consultar(codigo))
                .thenThrow(new RecursoNoEncontradoException(
                        "La solicitud no existe."));

        HttpResponse<String> respuesta = enviar(
                HttpRequest.newBuilder(URI.create(
                        baseUrl + "/solicitudes/" + codigo))
                        .GET().build());

        assertEquals(404, respuesta.statusCode());
        assertTrue(respuesta.body().contains("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    @Order(8)
    void conflictoDeDisponibilidadDevuelve409() throws Exception {
        when(solicitudes.crear(any()))
                .thenThrow(new ConflictoDisponibilidadException(
                        "El turno acaba de ser ocupado."));

        HttpResponse<String> respuesta = post(solicitudValida());

        assertEquals(409, respuesta.statusCode());
        assertTrue(respuesta.body().contains("TURNO_NO_DISPONIBLE"));
    }

    @Test
    @Order(9)
    void cuerpoDemasiadoGrandeDevuelve413() throws Exception {
        String cuerpo = "x".repeat(
                SeguridadApiPublica.MAXIMO_JSON_BYTES + 1);

        HttpResponse<String> respuesta = post(cuerpo);

        assertEquals(413, respuesta.statusCode());
        assertTrue(respuesta.body().contains("CUERPO_DEMASIADO_GRANDE"));
    }

    @Test
    @Order(10)
    void excesoDeCreacionesDevuelve429() throws Exception {
        HttpResponse<String> ultima = null;
        for (int i = 0; i < 9; i++) {
            ultima = post("{json-invalido");
        }

        assertNotNull(ultima);
        assertEquals(429, ultima.statusCode());
        assertTrue(ultima.body().contains("DEMASIADOS_INTENTOS"));
        assertFalse(encabezado(ultima, "Retry-After").isBlank());
    }

    private static HttpResponse<String> post(String body)
            throws IOException, InterruptedException {
        return enviar(HttpRequest.newBuilder(
                URI.create(baseUrl + "/solicitudes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        body, StandardCharsets.UTF_8))
                .build());
    }

    private static HttpResponse<String> enviar(HttpRequest request)
            throws IOException, InterruptedException {
        return client.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String encabezado(
            HttpResponse<?> respuesta, String nombre) {
        return respuesta.headers().firstValue(nombre).orElse("");
    }

    private static int puertoLibre() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static String solicitudValida() {
        return """
                {
                  "canchaId": 1,
                  "fecha": "2030-01-10",
                  "horaInicio": "10:00:00",
                  "cantidadJugadores": 4,
                  "cliente": {
                    "nombre": "Test",
                    "apellido": "API",
                    "documento": "30123456",
                    "telefono": "1123456789",
                    "email": "test@example.invalid"
                  },
                  "comentarios": "Test HTTP"
                }
                """;
    }
}
