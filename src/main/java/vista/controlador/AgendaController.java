package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import negocio.AgendaDiaria;
import negocio.Cancha;
import negocio.CeldaAgenda;
import negocio.Cliente;
import negocio.EstadoCeldaAgenda;
import negocio.EstadoReserva;
import negocio.FilaAgenda;
import negocio.Reserva;
import servicio.AgendaService;
import servicio.ClienteService;
import servicio.MensajeReservaService;
import servicio.PagoService;
import servicio.ReservaService;
import servicio.WhatsAppService;
import util.FormateadorMoneda;
import vista.Navegacion;

public class AgendaController {

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter FORMATO_FECHA_LARGA =
            DateTimeFormatter.ofPattern(
                    "EEEE d 'de' MMMM 'de' yyyy",
                    new Locale("es", "AR")
            );

    private static final double ALTO_FILA = 38.0;

    private final AgendaService agendaService = new AgendaService();
    private final ReservaService reservaService = new ReservaService();
    private final ClienteService clienteService = new ClienteService();
    private final PagoService pagoService = new PagoService();
    private final MensajeReservaService mensajeReservaService =
            new MensajeReservaService();
    private final WhatsAppService whatsAppService = new WhatsAppService();

    private Reserva reservaSeleccionada;
    private Cliente clienteSeleccionado;
    private Node tarjetaSeleccionada;
    private AgendaDiaria ultimaAgenda;
    private boolean conservarScrollEnProximaCarga;
    private double scrollVerticalGuardado;
    private double scrollHorizontalGuardado;
    private PauseTransition pausaEstado;
    private FadeTransition desvanecerEstado;

    @FXML private DatePicker selectorFecha;
    @FXML private Label etiquetaFecha;
    @FXML private Label etiquetaEstado;
    @FXML private VBox contenedorSinCanchas;
    @FXML private ProgressIndicator indicadorCarga;
    @FXML private Button botonActualizar;
    @FXML private ScrollPane scrollAgenda;
    @FXML private ScrollPane scrollEncabezado;
    @FXML private GridPane grillaEncabezado;
    @FXML private GridPane grillaAgenda;

    @FXML private VBox panelDetalle;
    @FXML private Label detalleTitulo;
    @FXML private Label detalleHorario;
    @FXML private Label detalleCancha;
    @FXML private Label detalleEstado;
    @FXML private Label detalleCliente;
    @FXML private Label detalleTelefono;
    @FXML private Label detallePrecio;
    @FXML private Label detalleAcreditado;
    @FXML private Label detalleSaldo;
    @FXML private Label detalleAyuda;
    @FXML private Button botonAbrirReserva;
    @FXML private Button botonAbrirPagos;
    @FXML private Button botonWhatsApp;
    @FXML private Button botonAccionDetalle;
    @FXML private Label indicadorDisponible;
    @FXML private Label indicadorPendiente;
    @FXML private Label indicadorConfirmada;
    @FXML private Label indicadorCompletada;
    @FXML private Label indicadorAusente;
    @FXML private Label indicadorBloqueada;
    @FXML private Label indicadorPartido;

    @FXML
    private void initialize() {
        selectorFecha.setValue(LocalDate.now());
        selectorFecha.valueProperty().addListener(
                (observador, anterior, actual) -> {
                    if (actual != null && !actual.equals(anterior)) {
                        conservarScrollEnProximaCarga = false;
                        limpiarDetalle();
                        cargarAgenda();
                    }
                });
        configurarScrollSincronizado();
        limpiarDetalle();
        Platform.runLater(this::cargarAgenda);
    }

    private void actualizarContadoresIndicadores(AgendaDiaria agenda) {
        java.util.Map<EstadoCeldaAgenda, Integer> cantidades =
                new java.util.EnumMap<>(EstadoCeldaAgenda.class);
        Set<String> eventosContados = new LinkedHashSet<>();
        for (FilaAgenda fila : agenda.getFilas()) {
            for (CeldaAgenda celda : fila.getCeldas()) {
                if (!esEstadoContable(celda.getEstado())) continue;
                String clave = claveEvento(celda);
                if (eventosContados.add(clave)) {
                    cantidades.merge(celda.getEstado(), 1, Integer::sum);
                }
            }
        }
        textoIndicador(indicadorDisponible, "Disponible",
                cantidades.getOrDefault(EstadoCeldaAgenda.DISPONIBLE, 0));
        textoIndicador(indicadorPendiente, "Pendiente",
                cantidades.getOrDefault(EstadoCeldaAgenda.PENDIENTE, 0));
        textoIndicador(indicadorConfirmada, "Confirmada",
                cantidades.getOrDefault(EstadoCeldaAgenda.CONFIRMADA, 0));
        textoIndicador(indicadorCompletada, "Completada",
                cantidades.getOrDefault(EstadoCeldaAgenda.COMPLETADA, 0));
        textoIndicador(indicadorAusente, "Ausente",
                cantidades.getOrDefault(EstadoCeldaAgenda.AUSENTE, 0));
        textoIndicador(indicadorBloqueada, "Bloqueada",
                cantidades.getOrDefault(EstadoCeldaAgenda.BLOQUEADA, 0));
        textoIndicador(indicadorPartido, "Partido",
                cantidades.getOrDefault(EstadoCeldaAgenda.PARTIDO_TORNEO, 0));
    }

    private boolean esEstadoContable(EstadoCeldaAgenda estado) {
        return switch (estado) {
            case DISPONIBLE, PENDIENTE, CONFIRMADA, COMPLETADA,
                    AUSENTE, BLOQUEADA, PARTIDO_TORNEO -> true;
            default -> false;
        };
    }

    private String claveEvento(CeldaAgenda celda) {
        if (celda.getReservaId() != null) return "R:" + celda.getReservaId();
        if (celda.getBloqueoId() != null) return "B:" + celda.getBloqueoId();
        if (celda.getPartidoId() != null) return "P:" + celda.getPartidoId();
        return celda.getEstado() + ":" + celda.getCanchaId() + ":"
                + celda.getHoraInicio() + ":" + celda.getHoraFin();
    }

    private void textoIndicador(Label indicador, String nombre,
            int cantidad) {
        indicador.setText(nombre + "  " + cantidad);
        indicador.setOpacity(cantidad == 0 ? 0.32 : 1.0);
    }

    private void configurarScrollSincronizado() {
        ChangeListener<Number> sincronizador = (obs, anterior, actual) ->
                scrollEncabezado.setHvalue(actual.doubleValue());
        scrollAgenda.hvalueProperty().addListener(sincronizador);
    }

    @FXML
    private void diaAnterior() {
        LocalDate fecha = selectorFecha.getValue();
        selectorFecha.setValue(
                (fecha == null ? LocalDate.now() : fecha).minusDays(1));
    }

    @FXML
    private void diaSiguiente() {
        LocalDate fecha = selectorFecha.getValue();
        selectorFecha.setValue(
                (fecha == null ? LocalDate.now() : fecha).plusDays(1));
    }

    @FXML
    private void irHoy() {
        LocalDate hoy = LocalDate.now();
        if (hoy.equals(selectorFecha.getValue())) {
            limpiarDetalle();
            cargarAgenda();
        } else {
            selectorFecha.setValue(hoy);
        }
    }

    @FXML
    private void actualizarAgenda() {
        conservarScrollEnProximaCarga = true;
        scrollVerticalGuardado = scrollAgenda.getVvalue();
        scrollHorizontalGuardado = scrollAgenda.getHvalue();
        cargarAgenda();
    }

    private void cargarAgenda() {
        LocalDate fecha = selectorFecha.getValue();
        if (fecha == null) {
            mostrarError("Seleccioná una fecha.");
            return;
        }

        cambiarCarga(true, "Cargando agenda...");
        Task<AgendaDiaria> tarea = new Task<>() {
            @Override
            protected AgendaDiaria call() {
                return agendaService.obtener(fecha);
            }
        };

        tarea.setOnSucceeded(evento -> {
            AgendaDiaria agenda = tarea.getValue();
            ultimaAgenda = agenda;
            actualizarContadoresIndicadores(agenda);
            construirGrilla(agenda);
            etiquetaFecha.setText(capitalizar(
                    agenda.getFecha().format(FORMATO_FECHA_LARGA)));
            cambiarCarga(false, "Agenda actualizada.");
            posicionarAgendaDespuesDeCarga();
        });

        tarea.setOnFailed(evento -> {
            Throwable error = tarea.getException();
            if (error != null) error.printStackTrace();
            indicadorCarga.setVisible(false);
            botonActualizar.setDisable(false);
            mostrarError(error == null || error.getMessage() == null
                    ? "No se pudo cargar la agenda."
                    : error.getMessage());
        });

        Thread hilo = new Thread(tarea, "agenda-diaria-padel");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void posicionarAgendaDespuesDeCarga() {
        Platform.runLater(() -> {
            if (conservarScrollEnProximaCarga) {
                scrollAgenda.setVvalue(scrollVerticalGuardado);
                scrollAgenda.setHvalue(scrollHorizontalGuardado);
            } else {
                scrollAgenda.setVvalue(0.0);
                scrollAgenda.setHvalue(0.0);
            }
            conservarScrollEnProximaCarga = false;
        });
    }

    private void construirGrilla(AgendaDiaria agenda) {
        limpiarSeleccionVisual();
        grillaAgenda.getChildren().clear();
        grillaAgenda.getColumnConstraints().clear();
        grillaAgenda.getRowConstraints().clear();
        grillaEncabezado.getChildren().clear();
        grillaEncabezado.getColumnConstraints().clear();

        boolean vacia = agenda.getCanchas().isEmpty();
        contenedorSinCanchas.setVisible(vacia);
        contenedorSinCanchas.setManaged(vacia);
        scrollAgenda.setVisible(!vacia);
        scrollAgenda.setManaged(!vacia);
        if (vacia) return;

        agregarRestriccionesColumnas(agenda);
        agregarEncabezadoHora();
        for (int indice = 0; indice < agenda.getCanchas().size(); indice++) {
            agregarEncabezadoCancha(agenda.getCanchas().get(indice), indice + 1);
        }
        agregarFilasAgenda(agenda);
        agregarIndicadorHoraActual(agenda);
    }

    private void agregarIndicadorHoraActual(AgendaDiaria agenda) {
        if (!LocalDate.now().equals(agenda.getFecha())
                || agenda.getFilas().isEmpty()) return;
        LocalTime ahora = LocalTime.now();
        for (int indice = 0; indice < agenda.getFilas().size(); indice++) {
            LocalTime inicio = agenda.getFilas().get(indice).getHoraInicio();
            LocalTime fin = inicio.plusMinutes(30);
            if (ahora.isBefore(inicio) || !ahora.isBefore(fin)) continue;

            Label texto = new Label(ahora.format(FORMATO_HORA));
            texto.setMouseTransparent(true);
            texto.getStyleClass().add("agenda-now-label");

            Region punto = new Region();
            punto.setMouseTransparent(true);
            punto.getStyleClass().add("agenda-now-dot");
            punto.setMinSize(6, 6);
            punto.setPrefSize(6, 6);
            punto.setMaxSize(6, 6);

            Region linea = new Region();
            linea.setMouseTransparent(true);
            linea.getStyleClass().add("agenda-now-line");
            linea.setMinHeight(1);
            linea.setPrefHeight(1);
            linea.setMaxHeight(1);
            HBox.setHgrow(linea, Priority.ALWAYS);

            javafx.scene.layout.HBox indicador =
                    new javafx.scene.layout.HBox(4, texto, punto, linea);
            indicador.setAlignment(Pos.CENTER_LEFT);
            indicador.setMouseTransparent(true);
            indicador.getStyleClass().add("agenda-now-indicator");
            indicador.setMaxWidth(Double.MAX_VALUE);

            double proporcion = java.time.Duration.between(inicio, ahora)
                    .toMinutes() / 30.0;
            indicador.setTranslateY(Math.max(1,
                    Math.min(ALTO_FILA - 7,
                            proporcion * ALTO_FILA)));
            GridPane.setHalignment(indicador,
                    javafx.geometry.HPos.LEFT);
            GridPane.setValignment(indicador,
                    javafx.geometry.VPos.TOP);
            grillaAgenda.add(indicador, 0, indice);
            indicador.toFront();
            break;
        }
    }

    private void agregarFilasAgenda(AgendaDiaria agenda) {
        Set<String> posicionesOcupadas = new HashSet<>();
        for (int filaIndice = 0; filaIndice < agenda.getFilas().size(); filaIndice++) {
            FilaAgenda fila = agenda.getFilas().get(filaIndice);
            int filaVisual = filaIndice;
            agregarHora(fila.getHoraInicio(), filaVisual,
                    esInicioDeTurno(agenda, fila.getHoraInicio()));

            for (int columna = 0; columna < fila.getCeldas().size(); columna++) {
                String posicion = clavePosicion(filaIndice, columna);
                if (posicionesOcupadas.contains(posicion)) continue;

                CeldaAgenda celda = fila.getCeldas().get(columna);
                int cantidadFilas = calcularCantidadFilas(
                        agenda, filaIndice, columna, celda);
                boolean mostrarSeparadorDerecho =
                        columna < agenda.getCanchas().size() - 1;
                Node contenido = crearCelda(celda, cantidadFilas);
                Node nodo = envolverCeldaConSeparador(
                        contenido, mostrarSeparadorDerecho);
                grillaAgenda.add(nodo, columna + 1, filaVisual);

                if (cantidadFilas > 1) {
                    GridPane.setRowSpan(nodo, cantidadFilas);
                    marcarPosicionesOcupadas(
                            posicionesOcupadas, filaIndice, columna,
                            cantidadFilas);
                }
            }
        }
    }

    private int calcularCantidadFilas(
            AgendaDiaria agenda, int filaInicial, int columna,
            CeldaAgenda celdaInicial) {
        if (celdaInicial.getEstado() == EstadoCeldaAgenda.DISPONIBLE) {
            int minutos = (int) Duration.between(
                    celdaInicial.getHoraInicio(), celdaInicial.getHoraFin())
                    .toMinutes();
            return limitarCantidadFilas(agenda, filaInicial, minutos / 30);
        }
        if (celdaInicial.getReservaId() != null) {
            return contarReservaContinua(
                    agenda, filaInicial, columna, celdaInicial.getReservaId());
        }
        if (celdaInicial.getBloqueoId() != null) {
            return contarBloqueoContinuo(
                    agenda, filaInicial, columna, celdaInicial.getBloqueoId());
        }
        if (celdaInicial.getPartidoId() != null) {
            return contarPartidoContinuo(
                    agenda, filaInicial, columna, celdaInicial.getPartidoId());
        }
        return 1;
    }

    private int contarReservaContinua(
            AgendaDiaria agenda, int filaInicial, int columna, Long reservaId) {
        int cantidad = 0;
        for (int fila = filaInicial; fila < agenda.getFilas().size(); fila++) {
            CeldaAgenda siguiente = obtenerCelda(agenda, fila, columna);
            if (!reservaId.equals(siguiente.getReservaId())) break;
            cantidad++;
        }
        return Math.max(1, cantidad);
    }

    private int contarBloqueoContinuo(
            AgendaDiaria agenda, int filaInicial, int columna, Long bloqueoId) {
        int cantidad = 0;
        for (int fila = filaInicial; fila < agenda.getFilas().size(); fila++) {
            CeldaAgenda siguiente = obtenerCelda(agenda, fila, columna);
            if (!bloqueoId.equals(siguiente.getBloqueoId())) break;
            cantidad++;
        }
        return Math.max(1, cantidad);
    }

    private int contarPartidoContinuo(
            AgendaDiaria agenda, int filaInicial, int columna, Long partidoId) {
        int cantidad = 0;
        for (int fila = filaInicial; fila < agenda.getFilas().size(); fila++) {
            CeldaAgenda siguiente = obtenerCelda(agenda, fila, columna);
            if (!partidoId.equals(siguiente.getPartidoId())) break;
            cantidad++;
        }
        return Math.max(1, cantidad);
    }

    private CeldaAgenda obtenerCelda(AgendaDiaria agenda, int fila, int columna) {
        return agenda.getFilas().get(fila).getCeldas().get(columna);
    }

    private int limitarCantidadFilas(
            AgendaDiaria agenda, int filaInicial, int cantidadSolicitada) {
        int filasRestantes = agenda.getFilas().size() - filaInicial;
        return Math.max(1, Math.min(cantidadSolicitada, filasRestantes));
    }

    private void marcarPosicionesOcupadas(
            Set<String> posicionesOcupadas, int filaInicial,
            int columna, int cantidadFilas) {
        for (int desplazamiento = 1;
                desplazamiento < cantidadFilas; desplazamiento++) {
            posicionesOcupadas.add(
                    clavePosicion(filaInicial + desplazamiento, columna));
        }
    }

    private String clavePosicion(int fila, int columna) {
        return fila + ":" + columna;
    }

    private void agregarRestriccionesColumnas(AgendaDiaria agenda) {
        ColumnConstraints hora = new ColumnConstraints();
        hora.setMinWidth(68);
        hora.setPrefWidth(74);
        hora.setMaxWidth(82);
        hora.setHgrow(Priority.NEVER);
        hora.setFillWidth(true);
        grillaAgenda.getColumnConstraints().add(hora);
        grillaEncabezado.getColumnConstraints().add(copiarRestriccion(hora));

        for (int indice = 0; indice < agenda.getCanchas().size(); indice++) {
            ColumnConstraints cancha = new ColumnConstraints();
            cancha.setMinWidth(120);
            cancha.setPrefWidth(170);
            cancha.setMaxWidth(Double.MAX_VALUE);
            cancha.setHgrow(Priority.ALWAYS);
            cancha.setFillWidth(true);
            grillaAgenda.getColumnConstraints().add(cancha);
            grillaEncabezado.getColumnConstraints().add(
                    copiarRestriccion(cancha));
        }
    }

    private ColumnConstraints copiarRestriccion(ColumnConstraints original) {
        ColumnConstraints copia = new ColumnConstraints();
        copia.setMinWidth(original.getMinWidth());
        copia.setPrefWidth(original.getPrefWidth());
        copia.setMaxWidth(original.getMaxWidth());
        copia.setHgrow(original.getHgrow());
        copia.setFillWidth(original.isFillWidth());
        return copia;
    }

    private void agregarEncabezadoHora() {
        Label etiqueta = new Label("HORA");
        etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.getStyleClass().addAll("agenda-header", "agenda-time-header");
        grillaEncabezado.add(etiqueta, 0, 0);
    }

    private void agregarEncabezadoCancha(Cancha cancha, int columna) {
        Label nombre = new Label(cancha.getNombre());
        nombre.getStyleClass().add("agenda-court-name");
        Label informacion = new Label(
                cancha.getHoraApertura().format(FORMATO_HORA)
                + " a " + cancha.getHoraCierre().format(FORMATO_HORA)
                + "  ·  " + cancha.getDuracionReserva() + " min");
        informacion.getStyleClass().add("agenda-court-meta");
        VBox encabezado = new VBox(2, nombre, informacion);
        encabezado.setAlignment(Pos.CENTER);
        encabezado.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        encabezado.getStyleClass().addAll("agenda-header",
                "agenda-court-header");
        Tooltip.install(encabezado, new Tooltip(
                cancha.getNombre() + "\n"
                        + cancha.getHoraApertura().format(FORMATO_HORA)
                        + " - " + cancha.getHoraCierre().format(FORMATO_HORA)
                        + "\nReservas de " + cancha.getDuracionReserva()
                        + " minutos"));
        grillaEncabezado.add(encabezado, columna, 0);
    }

    private void agregarHora(LocalTime hora, int fila,
            boolean inicioDeTurno) {
        Label etiqueta = new Label(hora.format(FORMATO_HORA));
        etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        etiqueta.setAlignment(Pos.TOP_CENTER);
        etiqueta.getStyleClass().add("agenda-time-cell");
        etiqueta.getStyleClass().add(inicioDeTurno
                ? "agenda-time-hour" : "agenda-time-half");
        grillaAgenda.add(etiqueta, 0, fila);
    }

    private boolean esInicioDeTurno(AgendaDiaria agenda, LocalTime hora) {
        if (agenda.getCanchas().isEmpty() || agenda.getFilas().isEmpty()) {
            return false;
        }
        LocalTime aperturaAgenda = agenda.getFilas().get(0).getHoraInicio();
        int duracionReferencia = agenda.getCanchas().stream()
                .mapToInt(Cancha::getDuracionReserva)
                .filter(duracion -> duracion > 0)
                .min().orElse(90);
        long minutosDesdeApertura = Duration.between(
                aperturaAgenda, hora).toMinutes();
        return minutosDesdeApertura >= 0
                && minutosDesdeApertura % duracionReferencia == 0;
    }

    private Node envolverCeldaConSeparador(
            Node contenido,
            boolean mostrarSeparadorDerecho) {
        StackPane contenedor = new StackPane(contenido);
        contenedor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(contenido, Pos.CENTER);

        if (contenido instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }

        if (mostrarSeparadorDerecho) {
            Region separador = new Region();
            separador.setMouseTransparent(true);
            separador.setMinWidth(1);
            separador.setPrefWidth(1);
            separador.setMaxWidth(1);
            separador.setMaxHeight(Double.MAX_VALUE);
            separador.getStyleClass().add("agenda-column-divider");
            StackPane.setAlignment(separador, Pos.CENTER_RIGHT);
            contenedor.getChildren().add(separador);
            separador.toFront();
        }
        return contenedor;
    }

    private Node crearCelda(
            CeldaAgenda celda,
            int cantidadFilas) {
        LocalTime horaFinalVisual = celda.getHoraInicio()
                .plusMinutes(cantidadFilas * 30L);
        if (esSuperficieHoraria(celda.getEstado())) {
            Region superficie = new Region();
            superficie.setMinHeight(cantidadFilas * ALTO_FILA);
            superficie.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            superficie.setMouseTransparent(true);
            superficie.getStyleClass().addAll("agenda-timeline-slot",
                    claseEstado(celda.getEstado()));
            return superficie;
        }
        String horario = celda.getHoraInicio().format(FORMATO_HORA)
                + " a " + horaFinalVisual.format(FORMATO_HORA);

        Label etiquetaHorario = new Label(horario);
        etiquetaHorario.getStyleClass().add("agenda-event-time");

        Label etiquetaTitulo = new Label(tituloCelda(celda));
        etiquetaTitulo.setWrapText(true);
        etiquetaTitulo.setMaxWidth(Double.MAX_VALUE);
        etiquetaTitulo.setMaxHeight(38);
        etiquetaTitulo.getStyleClass().add("agenda-event-title");
        VBox.setVgrow(etiquetaTitulo, Priority.ALWAYS);

        Label etiquetaSecundaria = new Label(subtituloCelda(celda));
        etiquetaSecundaria.setWrapText(true);
        etiquetaSecundaria.setMaxWidth(Double.MAX_VALUE);
        etiquetaSecundaria.setMaxHeight(34);
        etiquetaSecundaria.getStyleClass().add("agenda-event-subtitle");
        boolean mostrarSecundaria = etiquetaSecundaria.getText() != null
                && !etiquetaSecundaria.getText().isBlank()
                && cantidadFilas > 1;
        etiquetaSecundaria.setVisible(mostrarSecundaria);
        etiquetaSecundaria.setManaged(mostrarSecundaria);

        String textoEstado = textoInsignia(celda.getEstado());
        Label insignia = new Label(textoEstado);
        insignia.getStyleClass().addAll("agenda-event-badge",
                "agenda-event-badge-" + celda.getEstado().name()
                        .toLowerCase(Locale.ROOT).replace('_', '-'));
        boolean mostrarInsignia = textoEstado != null
                && !textoEstado.isBlank();
        insignia.setVisible(mostrarInsignia);
        insignia.setManaged(mostrarInsignia);
        HBox cabecera = new HBox(8, etiquetaHorario,
                new javafx.scene.layout.Region(), insignia);
        HBox.setHgrow(cabecera.getChildren().get(1), Priority.ALWAYS);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(5, cabecera, etiquetaTitulo,
                etiquetaSecundaria);
        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tarjeta.setMinHeight(cantidadFilas * ALTO_FILA);
        tarjeta.getStyleClass().addAll(
                "agenda-cell", claseEstado(celda.getEstado()));

        if (celda.getEstado() == EstadoCeldaAgenda.DISPONIBLE) {
            insignia.setVisible(false);
            insignia.setManaged(false);
            etiquetaTitulo.setText("Disponible");
            etiquetaSecundaria.setText("");
            etiquetaSecundaria.setVisible(false);
            etiquetaSecundaria.setManaged(false);
            tarjeta.setOnMouseEntered(evento -> {
                etiquetaTitulo.setText("+  Nueva reserva");
                tarjeta.getStyleClass().add("agenda-cell-active");
            });
            tarjeta.setOnMouseExited(evento -> {
                etiquetaTitulo.setText("Disponible");
                tarjeta.getStyleClass().remove("agenda-cell-active");
            });
        }

        String estadoVisible = formatoEstado(celda.getEstado());
        String detalleTooltip = detalleAdicionalTooltip(
                celda.getDetalle(), estadoVisible);
        String tooltip = celda.getNombreCancha() + "\n"
                + celda.getHoraInicio().format(FORMATO_HORA) + " - "
                + horaFinalVisual.format(FORMATO_HORA) + "\n"
                + estadoVisible
                + (detalleTooltip.isBlank()
                        ? "" : "\n" + detalleTooltip)
                + "\n\nUn clic: ver detalle"
                + "\nDoble clic: abrir accion";
        Tooltip.install(tarjeta, new Tooltip(tooltip));

        boolean interactiva = celda.getEstado()
                != EstadoCeldaAgenda.NO_DISPONIBLE
                && celda.getEstado() != EstadoCeldaAgenda.PASADA
                && celda.getEstado() != EstadoCeldaAgenda.CANCELADA;
        tarjeta.setMouseTransparent(!interactiva);
        if (interactiva) {
            tarjeta.setOnMouseClicked(
                    evento -> manejarClic(evento, celda, tarjeta));
        }
        return tarjeta;
    }

    private boolean esSuperficieHoraria(EstadoCeldaAgenda estado) {
        return estado == EstadoCeldaAgenda.NO_DISPONIBLE
                || estado == EstadoCeldaAgenda.PASADA
                || estado == EstadoCeldaAgenda.CANCELADA;
    }

    private String detalleAdicionalTooltip(String detalle,
            String estadoVisible) {
        if (detalle == null || detalle.isBlank()) return "";
        String limpio = detalle.trim();
        String estado = estadoVisible == null ? "" : estadoVisible.trim();
        if (limpio.equalsIgnoreCase(estado)) return "";
        return limpio;
    }

    private String tituloCelda(CeldaAgenda celda) {
        return switch (celda.getEstado()) {
            case DISPONIBLE -> "Disponible";
            case PARTIDO_TORNEO -> lineaDetalle(celda.getDetalle(), 1,
                    "Partido de torneo");
            case BLOQUEADA -> celda.getDetalle() == null
                    || celda.getDetalle().isBlank()
                            ? "Bloqueo operativo" : celda.getDetalle();
            case NO_DISPONIBLE, PASADA, CANCELADA -> "";
            default -> celda.getDetalle() == null
                    || celda.getDetalle().isBlank()
                            ? formatoEstado(celda.getEstado())
                            : celda.getDetalle();
        };
    }

    private String subtituloCelda(CeldaAgenda celda) {
        if (celda.getEstado() == EstadoCeldaAgenda.PARTIDO_TORNEO) {
            return lineaDetalle(celda.getDetalle(), 2, "Encuentro por definir");
        }
        if (celda.getEstado() == EstadoCeldaAgenda.BLOQUEADA) {
            return "Cancha no disponible";
        }
        return "";
    }

    private String lineaDetalle(String detalle, int indice,
            String alternativa) {
        if (detalle == null || detalle.isBlank()) return alternativa;
        String[] lineas = detalle.split("\\n");
        return indice < lineas.length && !lineas[indice].isBlank()
                ? lineas[indice] : alternativa;
    }

    private String textoInsignia(EstadoCeldaAgenda estado) {
        return switch (estado) {
            case PENDIENTE -> "PENDIENTE";
            case CONFIRMADA -> "CONFIRMADA";
            case COMPLETADA -> "COMPLETADA";
            case AUSENTE -> "AUSENTE";
            case BLOQUEADA -> "BLOQUEO";
            case PARTIDO_TORNEO -> "PARTIDO";
            default -> "";
        };
    }

    private String horarioDisponible(CeldaAgenda celda,
            LocalTime horaFinalVisual) {
        return "Disponible\n" + celda.getHoraInicio().format(FORMATO_HORA)
                + " a " + horaFinalVisual.format(FORMATO_HORA);
    }

    private String textoCelda(CeldaAgenda celda, LocalTime horaFinalVisual) {
        String horario = celda.getHoraInicio().format(FORMATO_HORA)
                + " - " + horaFinalVisual.format(FORMATO_HORA);
        return switch (celda.getEstado()) {
            case DISPONIBLE -> horarioDisponible(celda, horaFinalVisual);
            case PARTIDO_TORNEO -> horario + "\nPARTIDO DE TORNEO\n"
                    + resumenPartido(celda.getDetalle());
            case BLOQUEADA -> horario + "\n"
                    + (celda.getDetalle() == null
                            ? "Bloqueada" : celda.getDetalle());
            case NO_DISPONIBLE, PASADA -> "";
            default -> horario + "\n"
                    + (celda.getDetalle() == null
                            ? formatoEstado(celda.getEstado())
                            : celda.getDetalle());
        };
    }

    private String resumenPartido(String detalle) {
        if (detalle == null || detalle.isBlank()) return "Encuentro programado";
        String[] lineas = detalle.split("\\n");
        return lineas.length > 1 ? lineas[1] : detalle;
    }

    private void manejarClic(MouseEvent evento, CeldaAgenda celda,
            Node tarjeta) {
        seleccionarTarjeta(tarjeta);
        mostrarDetalleCelda(celda);
        if (evento.getClickCount() == 2) {
            ejecutarAccionCelda(celda);
        }
    }

    private void seleccionarTarjeta(Node tarjeta) {
        limpiarSeleccionVisual();
        tarjetaSeleccionada = tarjeta;
        if (tarjetaSeleccionada != null) {
            tarjetaSeleccionada.getStyleClass().add(
                    "agenda-cell-selected");
        }
    }

    private void limpiarSeleccionVisual() {
        if (tarjetaSeleccionada != null) {
            tarjetaSeleccionada.getStyleClass().remove(
                    "agenda-cell-selected");
            tarjetaSeleccionada = null;
        }
    }

    private void ejecutarAccionCelda(CeldaAgenda celda) {
        if (celda.getEstado() == EstadoCeldaAgenda.DISPONIBLE) {
            Navegacion.mostrarNuevaReservaDesdeAgenda(
                    selectorFecha.getValue(),
                    celda.getCanchaId(),
                    celda.getHoraInicio());
            return;
        }
        if (celda.getReservaId() != null) {
            Navegacion.mostrarReservaDesdeAgenda(celda.getReservaId());
            return;
        }
        if (celda.getEstado() == EstadoCeldaAgenda.BLOQUEADA) {
            Navegacion.mostrarBloqueos();
            return;
        }
        if (celda.getEstado() == EstadoCeldaAgenda.PARTIDO_TORNEO
                && celda.getTorneoCategoriaId() != null) {
            Navegacion.mostrarCuadroTorneo(celda.getTorneoCategoriaId());
        }
    }

    private void mostrarDetalleCelda(CeldaAgenda celda) {
        panelDetalle.setVisible(true);
        panelDetalle.setManaged(true);
        detalleCancha.setText(celda.getNombreCancha());
        detalleHorario.setText(
                celda.getHoraInicio().format(FORMATO_HORA) + " - "
                        + celda.getHoraFin().format(FORMATO_HORA));
        detalleEstado.setText(formatoEstado(celda.getEstado()));
        detalleEstado.getStyleClass().removeIf(
                clase -> clase.startsWith("detail-status-"));
        detalleEstado.getStyleClass().add(
                "detail-status-" + celda.getEstado().name()
                        .toLowerCase(Locale.ROOT).replace('_', '-'));

        reservaSeleccionada = null;
        clienteSeleccionado = null;
        ocultarDatosFinancieros();

        if (celda.getReservaId() != null) {
            mostrarDetalleReserva(celda.getReservaId());
            return;
        }

        if (celda.getEstado() == EstadoCeldaAgenda.DISPONIBLE) {
            detalleTitulo.setText("Horario disponible");
            detalleCliente.setText("Listo para reservar");
            detalleTelefono.setText("Doble clic para crear una reserva");
            detalleAyuda.setText(
                    "Podés crear una reserva con la cancha, fecha y hora ya seleccionadas.");
            configurarBoton(botonAccionDetalle, true, "CREAR RESERVA");
            ocultarBoton(botonAbrirReserva);
            ocultarBoton(botonAbrirPagos);
            ocultarBoton(botonWhatsApp);
            botonAccionDetalle.setOnAction(evento ->
                    Navegacion.mostrarNuevaReservaDesdeAgenda(
                            selectorFecha.getValue(),
                            celda.getCanchaId(),
                            celda.getHoraInicio()));
            return;
        }

        if (celda.getEstado() == EstadoCeldaAgenda.PARTIDO_TORNEO) {
            detalleTitulo.setText("Partido de torneo");
            detalleCliente.setText(celda.getDetalle() == null
                    ? "Encuentro programado" : celda.getDetalle());
            detalleTelefono.setText("Sin cliente asociado");
            detalleAyuda.setText(
                    "Doble clic o usa el boton para abrir el cuadro de la categoria.");
            configurarBoton(botonAccionDetalle,
                    celda.getTorneoCategoriaId() != null,
                    "ABRIR CUADRO");
            ocultarBoton(botonAbrirReserva);
            ocultarBoton(botonAbrirPagos);
            ocultarBoton(botonWhatsApp);
            botonAccionDetalle.setOnAction(evento ->
                    Navegacion.mostrarCuadroTorneo(
                            celda.getTorneoCategoriaId()));
            return;
        }

        if (celda.getEstado() == EstadoCeldaAgenda.BLOQUEADA) {
            detalleTitulo.setText("Cancha bloqueada");
            detalleCliente.setText(celda.getDetalle() == null
                    ? "Bloqueo operativo" : celda.getDetalle());
            detalleTelefono.setText("Sin cliente asociado");
            detalleAyuda.setText("Abrí Bloqueos para consultar o modificar el motivo.");
            configurarBoton(botonAccionDetalle, true, "ABRIR BLOQUEOS");
            ocultarBoton(botonAbrirReserva);
            ocultarBoton(botonAbrirPagos);
            ocultarBoton(botonWhatsApp);
            botonAccionDetalle.setOnAction(evento -> Navegacion.mostrarBloqueos());
            return;
        }

        detalleTitulo.setText("Horario no disponible");
        detalleCliente.setText(celda.getDetalle() == null
                ? "Sin información adicional" : celda.getDetalle());
        detalleTelefono.setText("");
        detalleAyuda.setText("Este horario no admite nuevas reservas.");
        ocultarBoton(botonAccionDetalle);
        ocultarBoton(botonAbrirReserva);
        ocultarBoton(botonAbrirPagos);
        ocultarBoton(botonWhatsApp);
    }

    private void mostrarDetalleReserva(long reservaId) {
        try {
            Reserva reserva = reservaService.buscar(reservaId);
            if (reserva == null) {
                mostrarError("La reserva seleccionada ya no existe.");
                return;
            }

            Cliente cliente = reserva.esClienteOcasional() ? null : clienteService.buscar(reserva.getClienteId());
            reservaSeleccionada = reserva;
            clienteSeleccionado = cliente;

            detalleTitulo.setText("Reserva #" + reserva.getId());
            detalleHorario.setText(
                    reserva.getHoraInicio().format(FORMATO_HORA) + " - "
                            + reserva.getHoraFin().format(FORMATO_HORA));
            detalleCancha.setText(reserva.getNombreCancha());
            detalleEstado.setText(nombreEstado(reserva.getEstado()));
            detalleCliente.setText(reserva.getNombreCliente());
            detalleTelefono.setText(reserva.esClienteOcasional()
                    ? (reserva.getClienteOcasionalTelefono() == null || reserva.getClienteOcasionalTelefono().isBlank()
                            ? "Sin teléfono registrado" : reserva.getClienteOcasionalTelefono())
                    : (cliente == null || cliente.getTelefono() == null || cliente.getTelefono().isBlank()
                            ? "Sin teléfono registrado" : cliente.getTelefono()));

            BigDecimal precio = reserva.getPrecioTotal() == null
                    ? BigDecimal.ZERO : reserva.getPrecioTotal();
            BigDecimal acreditado = pagoService.totalAcreditado(reserva.getId());
            BigDecimal saldo = pagoService.calcularSaldo(reserva.getId());
            detallePrecio.setText(formatearMoneda(precio));
            detalleAcreditado.setText(formatearMoneda(acreditado));
            detalleSaldo.setText(formatearMoneda(saldo));
            mostrarDatosFinancieros();

            detalleAyuda.setText(
                    "Doble clic sobre el turno o usá los botones para gestionarlo.");
            configurarBoton(botonAbrirReserva, true, "ABRIR RESERVA");
            botonAbrirReserva.setOnAction(evento ->
                    Navegacion.mostrarReservaDesdeAgenda(reserva.getId()));

            boolean permitePagos = reserva.getEstado() == EstadoReserva.PENDIENTE
                    || reserva.getEstado() == EstadoReserva.CONFIRMADA
                    || reserva.getEstado() == EstadoReserva.COMPLETADA;
            configurarBoton(botonAbrirPagos, permitePagos, "ABRIR PAGOS");
            botonAbrirPagos.setOnAction(evento ->
                    Navegacion.mostrarPagosDeReserva(reserva.getId()));

            boolean telefonoDisponible = cliente != null
                    && cliente.getTelefono() != null
                    && !cliente.getTelefono().isBlank();
            configurarBoton(botonWhatsApp, telefonoDisponible, "WHATSAPP");
            botonWhatsApp.setOnAction(evento -> abrirWhatsAppSeleccionado());
            ocultarBoton(botonAccionDetalle);
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void abrirWhatsAppSeleccionado() {
        if (reservaSeleccionada == null || clienteSeleccionado == null) {
            mostrarError("No se pudo recuperar la reserva o el cliente.");
            return;
        }
        try {
            String mensaje = switch (reservaSeleccionada.getEstado()) {
                case PENDIENTE -> mensajeReservaService.crearSolicitudSenia(
                        clienteSeleccionado,
                        reservaSeleccionada,
                        calcularSeniaPendiente(reservaSeleccionada));
                case CONFIRMADA -> {
                    BigDecimal saldo = pagoService.calcularSaldo(
                            reservaSeleccionada.getId());
                    yield saldo.signum() > 0
                            ? mensajeReservaService.crearAvisoSaldo(
                                    clienteSeleccionado,
                                    reservaSeleccionada,
                                    saldo)
                            : mensajeReservaService.crearRecordatorio(
                                    clienteSeleccionado,
                                    reservaSeleccionada);
                }
                case CANCELADA, EXPIRADA ->
                        mensajeReservaService.crearAvisoCancelacion(
                                clienteSeleccionado, reservaSeleccionada);
                case COMPLETADA, AUSENTE ->
                        mensajeReservaService.crearRecordatorio(
                                clienteSeleccionado, reservaSeleccionada);
            };
            whatsAppService.abrirConversacion(
                    clienteSeleccionado.getTelefono(), mensaje);
        } catch (RuntimeException exception) {
            mostrarError("No se pudo abrir WhatsApp: " + exception.getMessage());
        }
    }

    private BigDecimal calcularSeniaPendiente(Reserva reserva) {
        BigDecimal porcentaje = Navegacion.getConfiguracionActual()
                .getPorcentajeSenia();
        if (porcentaje == null) porcentaje = BigDecimal.ZERO;
        BigDecimal precio = reserva.getPrecioTotal() == null
                ? BigDecimal.ZERO : reserva.getPrecioTotal();
        BigDecimal requerida = precio.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal acreditado = pagoService.totalAcreditado(reserva.getId());
        return requerida.subtract(acreditado)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String nombreEstado(EstadoReserva estado) {
        if (estado == null) return "Sin estado";
        return switch (estado) {
            case PENDIENTE -> "Esperando seña";
            case CONFIRMADA -> "Confirmada";
            case COMPLETADA -> "Completada";
            case CANCELADA -> "Cancelada";
            case AUSENTE -> "Ausente";
            case EXPIRADA -> "Expirada";
        };
    }

    private String formatearMoneda(BigDecimal importe) {
        return FormateadorMoneda.pesos(importe);
    }

    private void limpiarDetalle() {
        limpiarSeleccionVisual();
        reservaSeleccionada = null;
        clienteSeleccionado = null;
        panelDetalle.setVisible(false);
        panelDetalle.setManaged(false);
        detalleTitulo.setText("Sin seleccion");
        detalleEstado.setText("Selecciona una celda");
        detalleCancha.setText("-");
        detalleHorario.setText("-");
        detalleCliente.setText("-");
        detalleTelefono.setText("-");
        detalleAyuda.setText(
                "Selecciona un horario, una reserva, un bloqueo o un partido para ver sus datos.");
        ocultarDatosFinancieros();
        ocultarBoton(botonAccionDetalle);
        ocultarBoton(botonAbrirReserva);
        ocultarBoton(botonAbrirPagos);
        ocultarBoton(botonWhatsApp);
    }

    @FXML
    private void cerrarDetalle() {
        limpiarDetalle();
    }

    private void mostrarDatosFinancieros() {
        detallePrecio.getParent().setVisible(true);
        detallePrecio.getParent().setManaged(true);
    }

    private void ocultarDatosFinancieros() {
        detallePrecio.setText("ARS 0.00");
        detalleAcreditado.setText("ARS 0.00");
        detalleSaldo.setText("ARS 0.00");
        detallePrecio.getParent().setVisible(false);
        detallePrecio.getParent().setManaged(false);
    }

    private void configurarBoton(Button boton, boolean visible, String texto) {
        boton.setVisible(visible);
        boton.setManaged(visible);
        boton.setText(texto);
    }

    private void ocultarBoton(Button boton) {
        boton.setVisible(false);
        boton.setManaged(false);
    }

    private String claseEstado(EstadoCeldaAgenda estado) {
        return "agenda-" + estado.name().toLowerCase(Locale.ROOT)
                .replace('_', '-');
    }

    private String formatoEstado(EstadoCeldaAgenda estado) {
        String texto = estado.name().replace('_', ' ')
                .toLowerCase(Locale.ROOT);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private void cambiarCarga(boolean cargando, String mensaje) {
        indicadorCarga.setVisible(cargando);
        botonActualizar.setDisable(cargando);
        etiquetaEstado.setText(mensaje);
        etiquetaEstado.getStyleClass().removeAll(
                "agenda-status-ok", "agenda-status-error");
        if (!cargando) {
            etiquetaEstado.getStyleClass().add("agenda-status-ok");
            mostrarEstadoTemporal();
        } else {
            detenerAnimacionEstado();
            etiquetaEstado.setOpacity(1.0);
        }
    }

    private void mostrarEstadoTemporal() {
        detenerAnimacionEstado();
        etiquetaEstado.setOpacity(1.0);
        pausaEstado = new PauseTransition(javafx.util.Duration.seconds(2.5));
        pausaEstado.setOnFinished(evento -> {
            desvanecerEstado = new FadeTransition(
                    javafx.util.Duration.millis(500), etiquetaEstado);
            desvanecerEstado.setFromValue(1.0);
            desvanecerEstado.setToValue(0.0);
            desvanecerEstado.play();
        });
        pausaEstado.play();
    }

    private void detenerAnimacionEstado() {
        if (pausaEstado != null) pausaEstado.stop();
        if (desvanecerEstado != null) desvanecerEstado.stop();
    }

    private void mostrarError(String mensaje) {
        detenerAnimacionEstado();
        etiquetaEstado.setOpacity(1.0);
        etiquetaEstado.setText(mensaje == null
                ? "No se pudo cargar la agenda." : mensaje);
        etiquetaEstado.getStyleClass().removeAll(
                "agenda-status-ok", "agenda-status-error");
        etiquetaEstado.getStyleClass().add("agenda-status-error");
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return "";
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    @FXML
    private void volver() {
        Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
    }
}
