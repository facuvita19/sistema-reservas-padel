package api.publica;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import api.publica.TorneoInscripcionPublicaDTO.CrearSolicitud;
import api.publica.TorneoInscripcionPublicaDTO.Jugador;
import api.publica.TorneoInscripcionPublicaDTO.SolicitudCreada;
import api.publica.TorneoPublicoService.TorneoPublicoNoEncontradoException;
import negocio.TorneoInscripcion;
import servicio.InscripcionTorneoWebService;
import servicio.InscripcionTorneoWebService.DatosJugador;
import servicio.InscripcionTorneoWebService.SolicitudInscripcion;

public final class TorneoPublicoHandler {

    private static final String PREFIJO = "/api/publica/torneos";
    private static final String RUTA_INSCRIPCIONES =
            PREFIJO + "/inscripciones";

    private final TorneoPublicoService torneoService;
    private final CuadroTorneoPublicoService cuadroService;
    private final CompetenciaTorneoPublicaService competenciaService;
    private final InscripcionTorneoWebService inscripcionService;
    private final SeguridadApiPublica seguridad;
    private final ObjectMapper mapper;

    public TorneoPublicoHandler(
            TorneoPublicoService torneoService,
            InscripcionTorneoWebService inscripcionService,
            SeguridadApiPublica seguridad,
            ObjectMapper mapper) {
        if (torneoService == null || inscripcionService == null
                || seguridad == null || mapper == null) {
            throw new IllegalArgumentException(
                    "Las dependencias del handler no pueden ser nulas.");
        }
        this.torneoService = torneoService;
        this.cuadroService = new CuadroTorneoPublicoService();
        this.competenciaService = new CompetenciaTorneoPublicaService();
        this.inscripcionService = inscripcionService;
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
            if (RUTA_INSCRIPCIONES.equals(ruta)
                    && "POST".equals(metodo)) {
                crearInscripcion(intercambio, ip);
                return;
            }
            if (!"GET".equals(metodo)) {
                error(intercambio, 405, "METODO_NO_PERMITIDO",
                        "El metodo solicitado no esta permitido.",
                        operacionId);
                return;
            }
            if (ruta.equals(PREFIJO)) {
                responder(intercambio, 200, torneoService.listar());
                return;
            }
            if (RUTA_INSCRIPCIONES.equals(ruta)) {
                error(intercambio, 405, "METODO_NO_PERMITIDO",
                        "El metodo solicitado no esta permitido.",
                        operacionId);
                return;
            }
            String prefijoCategoria = PREFIJO + "/categorias/";
            if (ruta.startsWith(prefijoCategoria)
                    && ruta.endsWith("/competencia")) {
                String valor = ruta.substring(prefijoCategoria.length(),
                        ruta.length() - "/competencia".length());
                responder(intercambio, 200,
                        competenciaService.buscar(parsearId(valor)));
                return;
            }
            if (ruta.startsWith(prefijoCategoria)
                    && ruta.endsWith("/cuadro")) {
                String valor = ruta.substring(prefijoCategoria.length(),
                        ruta.length() - "/cuadro".length());
                responder(intercambio, 200,
                        cuadroService.buscarPorCategoria(parsearId(valor)));
                return;
            }
            String relativo = ruta.substring((PREFIJO + "/").length());
            if (relativo.endsWith("/cuadro")) {
                String valor = relativo.substring(0,
                        relativo.length() - "/cuadro".length());
                responder(intercambio, 200,
                        cuadroService.buscarPorTorneo(parsearId(valor)));
                return;
            }
            long id = parsearId(relativo);
            responder(intercambio, 200, torneoService.buscar(id));
        } catch (CompetenciaTorneoPublicaService.CompetenciaNoEncontradaException exception) {
            error(intercambio, 404, "COMPETENCIA_NO_ENCONTRADA",
                    exception.getMessage(), operacionId);
        } catch (CuadroTorneoPublicoService.CuadroNoEncontradoException exception) {
            error(intercambio, 404, "CUADRO_NO_ENCONTRADO",
                    exception.getMessage(), operacionId);
        } catch (TorneoPublicoNoEncontradoException exception) {
            error(intercambio, 404, "TORNEO_NO_ENCONTRADO",
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

    private void crearInscripcion(
            HttpExchange intercambio,
            String ip) throws IOException {
        seguridad.validarCreacion(ip);
        byte[] cuerpo = seguridad.leerCuerpoLimitado(
                intercambio.getRequestBody());
        CrearSolicitud entrada = mapper.readValue(
                cuerpo, CrearSolicitud.class);
        if (entrada.responsable() == null || entrada.pareja() == null) {
            throw new IllegalArgumentException(
                    "Los dos integrantes son obligatorios.");
        }
        TorneoInscripcion creada = inscripcionService.solicitar(
                new SolicitudInscripcion(
                        entrada.torneoCategoriaId(),
                        convertir(entrada.responsable()),
                        convertir(entrada.pareja()),
                        entrada.comentarios()));
        responder(intercambio, 201, new SolicitudCreada(
                creada.getId(),
                creada.getEstado().name(),
                "La solicitud fue enviada y queda pendiente de revision."));
    }

    private DatosJugador convertir(Jugador jugador) {
        return new DatosJugador(
                jugador.nombre(),
                jugador.apellido(),
                jugador.telefono());
    }

    private long parsearId(String valor) {
        try {
            long id = Long.parseLong(valor);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "El ID del torneo debe ser positivo.");
        }
    }

    private void error(
            HttpExchange intercambio,
            int estado,
            String codigo,
            String mensaje,
            String operacionId) throws IOException {
        responder(intercambio, estado, new ErrorTorneo(
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

    private record ErrorTorneo(
            int estado,
            String codigo,
            String mensaje,
            String ruta,
            String operacionId) {
    }
}
