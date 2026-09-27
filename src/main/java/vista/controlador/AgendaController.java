package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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

    @FXML private DatePicker selectorFecha;
    @FXML private Label etiquetaFecha;
    @FXML private Label etiquetaEstado;
    @FXML private VBox contenedorSinCanchas;
    @FXML private ProgressIndicator indicadorCarga;
    @FXML private Button botonActualizar;
    @FXML private ScrollPane scrollAgenda;
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

    @FXML
    private void initialize() {
        selectorFecha.setValue(LocalDate.now());
        selectorFecha.valueProperty().addListener(
                (observador, anterior, actual) -> {
                    if (actual != null && !actual.equals(anterior)) {
                        limpiarDetalle();
                        cargarAgenda();
                    }
                });
        limpiarDetalle();
        Platform.runLater(this::cargarAgenda);
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
            construirGrilla(agenda);
            etiquetaFecha.setText(capitalizar(
                    agenda.getFecha().format(FORMATO_FECHA_LARGA)));
            cambiarCarga(false, "Agenda actualizada.");
            volverAlInicioDeLaAgenda();
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

    private void volverAlInicioDeLaAgenda() {
        Platform.runLater(() -> {
            scrollAgenda.setVvalue(0.0);
            scrollAgenda.setHvalue(0.0);
        });
    }

    private void construirGrilla(AgendaDiaria agenda) {
        grillaAgenda.getChildren().clear();
        grillaAgenda.getColumnConstraints().clear();
        grillaAgenda.getRowConstraints().clear();

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
    }

    private void agregarFilasAgenda(AgendaDiaria agenda) {
        Set<String> posicionesOcupadas = new HashSet<>();
        for (int filaIndice = 0; filaIndice < agenda.getFilas().size(); filaIndice++) {
            FilaAgenda fila = agenda.getFilas().get(filaIndice);
            int filaVisual = filaIndice + 1;
            agregarHora(fila.getHoraInicio(), filaVisual);

            for (int columna = 0; columna < fila.getCeldas().size(); columna++) {
                String posicion = clavePosicion(filaIndice, columna);
                if (posicionesOcupadas.contains(posicion)) continue;

                CeldaAgenda celda = fila.getCeldas().get(columna);
                int cantidadFilas = calcularCantidadFilas(
                        agenda, filaIndice, columna, celda);
                Node nodo = crearCelda(celda, cantidadFilas);
                grillaAgenda.add(nodo, columna + 1, filaVisual);

                if (cantidadFilas > 1) {
                    GridPane.setRowSpan(nodo, cantidadFilas);
                    marcarPosicionesOcupadas(
                            posicionesOcupadas, filaIndice, columna, cantidadFilas);
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

        for (int indice = 0; indice < agenda.getCanchas().size(); indice++) {
            ColumnConstraints cancha = new ColumnConstraints();
            cancha.setMinWidth(120);
            cancha.setPrefWidth(170);
            cancha.setMaxWidth(Double.MAX_VALUE);
            cancha.setHgrow(Priority.ALWAYS);
            cancha.setFillWidth(true);
            grillaAgenda.getColumnConstraints().add(cancha);
        }
    }

    private void agregarEncabezadoHora() {
        Label etiqueta = new Label("HORA");
        etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.getStyleClass().addAll("agenda-header", "agenda-time-header");
        grillaAgenda.add(etiqueta, 0, 0);
    }

    private void agregarEncabezadoCancha(Cancha cancha, int columna) {
        Label etiqueta = new Label(cancha.getNombre());
        etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.getStyleClass().add("agenda-header");
        Tooltip.install(etiqueta, new Tooltip(
                cancha.getNombre() + "\n"
                        + cancha.getHoraApertura().format(FORMATO_HORA)
                        + " - " + cancha.getHoraCierre().format(FORMATO_HORA)
                        + "\nReservas de " + cancha.getDuracionReserva()
                        + " minutos"));
        grillaAgenda.add(etiqueta, columna, 0);
    }

    private void agregarHora(LocalTime hora, int fila) {
        Label etiqueta = new Label(hora.format(FORMATO_HORA));
        etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        etiqueta.setAlignment(Pos.TOP_CENTER);
        etiqueta.getStyleClass().add("agenda-time-cell");
        grillaAgenda.add(etiqueta, 0, fila);
    }

    private Node crearCelda(CeldaAgenda celda, int cantidadFilas) {
        LocalTime horaFinalVisual = celda.getHoraInicio()
                .plusMinutes(cantidadFilas * 30L);
        Label etiqueta = new Label(textoCelda(celda, horaFinalVisual));
        etiqueta.setWrapText(true);
        etiqueta.setAlignment(Pos.TOP_LEFT);
        etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        etiqueta.setMinHeight(cantidadFilas * ALTO_FILA);
        etiqueta.getStyleClass().addAll(
                "agenda-cell", claseEstado(celda.getEstado()));

        String tooltip = celda.getNombreCancha() + "\n"
                + celda.getHoraInicio().format(FORMATO_HORA) + " - "
                + horaFinalVisual.format(FORMATO_HORA) + "\n"
                + formatoEstado(celda.getEstado())
                + (celda.getDetalle() == null ? "" : "\n" + celda.getDetalle())
                + "\n\nUn clic: ver detalle"
                + "\nDoble clic: abrir acción";
        Tooltip.install(etiqueta, new Tooltip(tooltip));

        etiqueta.setOnMouseClicked(evento -> manejarClic(evento, celda));
        return etiqueta;
    }

    private String textoCelda(CeldaAgenda celda, LocalTime horaFinalVisual) {
        String horario = celda.getHoraInicio().format(FORMATO_HORA)
                + " - " + horaFinalVisual.format(FORMATO_HORA);
        return switch (celda.getEstado()) {
            case DISPONIBLE -> horario + "\nDisponible";
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

    private void manejarClic(MouseEvent evento, CeldaAgenda celda) {
        mostrarDetalleCelda(celda);
        if (evento.getClickCount() == 2) {
            ejecutarAccionCelda(celda);
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

            Cliente cliente = clienteService.buscar(reserva.getClienteId());
            reservaSeleccionada = reserva;
            clienteSeleccionado = cliente;

            detalleTitulo.setText("Reserva #" + reserva.getId());
            detalleHorario.setText(
                    reserva.getHoraInicio().format(FORMATO_HORA) + " - "
                            + reserva.getHoraFin().format(FORMATO_HORA));
            detalleCancha.setText(reserva.getNombreCancha());
            detalleEstado.setText(nombreEstado(reserva.getEstado()));
            detalleCliente.setText(reserva.getNombreCliente());
            detalleTelefono.setText(cliente == null
                    || cliente.getTelefono() == null
                    || cliente.getTelefono().isBlank()
                            ? "Sin teléfono registrado"
                            : cliente.getTelefono());

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
        BigDecimal valor = importe == null ? BigDecimal.ZERO : importe;
        return "ARS " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void limpiarDetalle() {
        reservaSeleccionada = null;
        clienteSeleccionado = null;
        panelDetalle.setVisible(false);
        panelDetalle.setManaged(false);
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
        if (!cargando) etiquetaEstado.getStyleClass().add("agenda-status-ok");
    }

    private void mostrarError(String mensaje) {
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
