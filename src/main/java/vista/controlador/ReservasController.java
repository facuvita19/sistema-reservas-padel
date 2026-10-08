package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import negocio.AuditoriaReserva;
import negocio.Cancha;
import negocio.Cliente;
import negocio.ConfiguracionComplejo;
import negocio.EstadoReserva;
import negocio.Reserva;
import servicio.AuditoriaReservaService;
import servicio.CanchaService;
import servicio.ClienteService;
import servicio.ConfiguracionComplejoService;
import servicio.MensajeReservaService;
import servicio.PagoService;
import servicio.PoliticaReservaService;
import servicio.ReprogramacionReservaService;
import servicio.ReservaService;
import servicio.WhatsAppService;
import util.FormateadorMoneda;
import vista.FiltroReservas;
import vista.Dialogos;
import vista.Navegacion;
import vista.SolicitudFiltroReservas;
import vista.SolicitudReservaAgenda;

public class ReservasController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReservaService reservaService = new ReservaService();
    private final AuditoriaReservaService auditoriaReservaService = new AuditoriaReservaService();
    private final ClienteService clienteService = new ClienteService();
    private final CanchaService canchaService = new CanchaService();
    private final PagoService pagoService = new PagoService();
    private final PoliticaReservaService politicaReservaService = new PoliticaReservaService();
    private final ReprogramacionReservaService reprogramacionService = new ReprogramacionReservaService();
    private final ConfiguracionComplejoService configuracionComplejoService = new ConfiguracionComplejoService();
    private final MensajeReservaService mensajeReservaService = new MensajeReservaService();
    private final WhatsAppService whatsAppService = new WhatsAppService();

    private final ObservableList<Reserva> reservas = FXCollections.observableArrayList();
    private final ObservableList<AuditoriaReserva> auditorias = FXCollections.observableArrayList();
    private FilteredList<Reserva> reservasFiltradas;
    private FiltroReservas filtroDashboard;
    private Reserva reservaSeleccionada;
    private boolean modoReprogramacion;
    private boolean reprogramacionAdministrativa;
    private boolean actualizandoFormulario;

    @FXML private TextField campoBuscar;
    @FXML private ComboBox<EstadoReserva> filtroEstado;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, LocalDate> columnaFecha;
    @FXML private TableColumn<Reserva, String> columnaHorario;
    @FXML private TableColumn<Reserva, String> columnaCancha;
    @FXML private TableColumn<Reserva, String> columnaCliente;
    @FXML private TableColumn<Reserva, EstadoReserva> columnaEstado;
    @FXML private TableColumn<Reserva, BigDecimal> columnaPrecio;
    @FXML private Label tituloFormulario;
    @FXML private Label subtituloFormulario;
    @FXML private Label insigniaEstadoDetalle;
    @FXML private Label insigniaOrigenDetalle;
    @FXML private Label etiquetaVencimientoDetalle;
    @FXML private Label etiquetaMensaje;
    @FXML private Label etiquetaPrecio;
    @FXML private Label etiquetaSaldoPendiente;
    @FXML private Button botonNuevaReservaSuperior;
    @FXML private VBox panelAltaResponsable;
    @FXML private VBox panelResponsableInformativo;
    @FXML private Label etiquetaResponsableNombre;
    @FXML private Label etiquetaResponsableTipo;
    @FXML private Label etiquetaResponsableContacto;
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ToggleButton botonClienteRegistrado;
    @FXML private ToggleButton botonClienteOcasional;
    @FXML private VBox contenedorClienteOcasional;
    @FXML private TextField campoClienteOcasionalNombre;
    @FXML private TextField campoClienteOcasionalTelefono;
    @FXML private TextField campoClienteOcasionalEmail;
    @FXML private TextField campoClienteOcasionalDocumento;
    @FXML private Label insigniaNuevaReserva;
    @FXML private ComboBox<Cancha> comboCancha;
    @FXML private DatePicker selectorFecha;
    @FXML private ComboBox<LocalTime> comboHorario;
    @FXML private Spinner<Integer> spinnerJugadores;
    @FXML private TextArea campoComentarios;
    @FXML private TextArea campoObservaciones;
    @FXML private DatePicker filtroFecha;
    @FXML private Label etiquetaFechaSeleccionada;
    @FXML private Button botonAccionPrincipal;
    @FXML private Button botonPagos;
    @FXML private Button botonCancelar;
    @FXML private Button botonGuardar;
    @FXML private Button botonReprogramar;
    @FXML private Button botonWhatsApp;
    @FXML private javafx.scene.control.ScrollPane scrollFormulario;
    @FXML private VBox avisoModoReprogramacion;
    @FXML private VBox contenedorAccionesReserva;
    @FXML private VBox contenedorCancelacion;
    @FXML private Label etiquetaCancelacion;
    @FXML private VBox contenedorAuditoria;
    @FXML private Label etiquetaCantidadAuditoria;
    @FXML private javafx.scene.control.ListView<AuditoriaReserva> listaAuditoria;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarTablaAuditoria();
        configurarFiltros();
        configurarFormulario();
        cargarDatosBase();
        cargarReservas();
        nuevaReserva();
        Platform.runLater(() -> {
            aplicarSolicitudDesdeAgenda();
            aplicarSolicitudFiltroDashboard();
        });
    }

    private void configurarTabla() {
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columnaFecha.setCellFactory(columna -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : fecha.format(FORMATO_FECHA));
            }
        });
        columnaHorario.setCellValueFactory(datos -> new javafx.beans.property.SimpleStringProperty(
                datos.getValue().getHoraInicio().format(FORMATO_HORA) + " - "
                        + datos.getValue().getHoraFin().format(FORMATO_HORA)));
        columnaCancha.setCellValueFactory(new PropertyValueFactory<>("nombreCancha"));
        columnaCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columnaEstado.setCellFactory(columna ->
                new javafx.scene.control.TableCell<>() {
                    private final Label insignia = new Label();
                    {
                        insignia.getStyleClass().add("reservation-status-badge");
                        setGraphic(insignia);
                        setText(null);
                    }
                    @Override
                    protected void updateItem(EstadoReserva estado,
                            boolean vacia) {
                        super.updateItem(estado, vacia);
                        if (vacia || estado == null) {
                            setGraphic(null);
                            return;
                        }
                        insignia.setText(nombreEstado(estado));
                        insignia.getStyleClass().removeIf(
                                clase -> clase.startsWith(
                                        "reservation-status-")
                                        && !clase.equals(
                                                "reservation-status-badge"));
                        insignia.getStyleClass().add(
                                claseEstadoReserva(estado));
                        setGraphic(insignia);
                    }
                });
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        columnaPrecio.setCellFactory(columna -> new javafx.scene.control.TableCell<>() {
            {
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                getStyleClass().add("reservation-price-cell");
            }
            @Override
            protected void updateItem(BigDecimal importe, boolean vacia) {
                super.updateItem(importe, vacia);
                setText(vacia || importe == null
                        ? null
                        : FormateadorMoneda.pesos(importe));
            }
        });
        tablaReservas.setRowFactory(tabla -> {
            TableRow<Reserva> fila = new TableRow<>();
            fila.itemProperty().addListener((obs, anterior, actual) ->
                    actualizarClaseFila(fila, actual));
            fila.selectedProperty().addListener((obs, anterior, actual) ->
                    actualizarClaseFila(fila, fila.getItem()));
            return fila;
        });
        tablaReservas.setOnMouseClicked(evento -> {
            if (clicEnFondoTabla(evento.getTarget())) {
                tablaReservas.getSelectionModel().clearSelection();
                nuevaReserva();
            }
        });
        tablaReservas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) mostrarDetalle(actual);
        });
    }

    private void actualizarClaseFila(TableRow<Reserva> fila,
            Reserva reserva) {
        fila.getStyleClass().removeIf(
                clase -> clase.startsWith("reservation-row-")
                        || clase.equals("reservation-row-selected"));
        if (reserva == null || fila.isEmpty()) return;
        fila.getStyleClass().add(
                "reservation-row-" + reserva.getEstado().name()
                        .toLowerCase(Locale.ROOT));
        if (fila.isSelected()) {
            fila.getStyleClass().add("reservation-row-selected");
        }
    }

    private String claseEstadoReserva(EstadoReserva estado) {
        return "reservation-status-" + estado.name()
                .toLowerCase(Locale.ROOT);
    }

    private boolean clicEnFondoTabla(Object objetivo) {
        if (!(objetivo instanceof Node nodo)) return false;
        Node actual = nodo;
        while (actual != null && actual != tablaReservas) {
            if (actual instanceof TableRow<?> fila) {
                return fila.isEmpty();
            }
            actual = actual.getParent();
        }
        return actual == tablaReservas;
    }

    private void configurarTablaAuditoria() {
        listaAuditoria.setItems(auditorias);
        listaAuditoria.setCellFactory(lista ->
                new javafx.scene.control.ListCell<>() {
                    @Override
                    protected void updateItem(
                            AuditoriaReserva auditoria,
                            boolean vacia) {
                        super.updateItem(auditoria, vacia);

                        if (vacia || auditoria == null) {
                            setText(null);
                            setGraphic(null);
                            return;
                        }

                        Label accion = new Label(
                                auditoria.getAccion() == null
                                        ? "Movimiento de reserva"
                                        : auditoria.getAccion().toString());
                        accion.getStyleClass().add("audit-card-action");

                        String fecha = auditoria.getFechaEvento() == null
                                ? "Sin fecha"
                                : auditoria.getFechaEvento().format(
                                        DateTimeFormatter.ofPattern(
                                                "dd/MM/yyyy HH:mm"));

                        Label metadatos = new Label(
                                fecha + "  ·  " + auditoria.getResponsable());
                        metadatos.getStyleClass().add("audit-card-meta");

                        Label estados = new Label(
                                describirCambioEstado(auditoria));
                        estados.setWrapText(true);
                        estados.setMaxWidth(Double.MAX_VALUE);
                        estados.getStyleClass().add("audit-card-states");

                        Label detalle = new Label(humanizarDetalle(auditoria));
                        detalle.setWrapText(true);
                        detalle.setMaxWidth(Double.MAX_VALUE);
                        detalle.getStyleClass().add("audit-card-detail");

                        VBox tarjeta = new VBox(
                                5, accion, metadatos, estados, detalle);
                        tarjeta.getStyleClass().add("audit-card");
                        tarjeta.setFillWidth(true);
                        tarjeta.setMaxWidth(Double.MAX_VALUE);
                        tarjeta.prefWidthProperty().bind(
                                listaAuditoria.widthProperty().subtract(24));
                        estados.prefWidthProperty().bind(
                                tarjeta.widthProperty().subtract(22));
                        detalle.prefWidthProperty().bind(
                                tarjeta.widthProperty().subtract(22));

                        setText(null);
                        setGraphic(tarjeta);
                        setMaxWidth(Double.MAX_VALUE); // estabilizar-historial-scroll-reservas-v1
                    }
                });
    }

    private String describirCambioEstado(AuditoriaReserva auditoria) {
        EstadoReserva anterior = auditoria.getEstadoAnterior();
        EstadoReserva nuevo = auditoria.getEstadoNuevo();

        if (anterior == null && nuevo == null) return "";
        if (anterior == null) return "Estado inicial: " + nombreEstado(nuevo);
        if (nuevo == null) return "Estado anterior: " + nombreEstado(anterior);
        if (anterior == nuevo) return "Estado: " + nombreEstado(nuevo);

        return "La reserva pasó de " + nombreEstado(anterior)
                + " a " + nombreEstado(nuevo) + ".";
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

    private String humanizarDetalle(AuditoriaReserva auditoria) {
        if (auditoria.getAccion() == null) {
            return limpiarDetalleTecnico(auditoria.getDetalle());
        }

        return switch (auditoria.getAccion()) {
            case CREACION -> "La reserva fue creada y quedó registrada en el sistema.";
            case CONFIRMACION -> "La reserva fue confirmada al acreditarse la seña requerida.";
            case EXPIRACION_AUTOMATICA -> "La solicitud venció porque no recibió una seña dentro del plazo establecido.";
            case CIERRE_COMPLETADA -> "El turno fue cerrado como jugado normalmente.";
            case CIERRE_AUSENTE -> "El turno fue cerrado porque el cliente no se presentó.";
            case CANCELACION -> "La reserva fue cancelada por solicitud del cliente.";
            case CANCELACION_ADMINISTRATIVA, REPROGRAMACION,
                    REPROGRAMACION_ADMINISTRATIVA, MODIFICACION,
                    CAMBIO_ESTADO -> limpiarDetalleTecnico(
                            auditoria.getDetalle());
        };
    }

    private String limpiarDetalleTecnico(String detalle) {
        if (detalle == null || detalle.isBlank()) {
            return "Movimiento registrado correctamente.";
        }

        String limpio = detalle.trim()
                .replace("PENDIENTE", "Esperando seña")
                .replace("CONFIRMADA", "Confirmada")
                .replace("COMPLETADA", "Completada")
                .replace("CANCELADA", "Cancelada")
                .replace("AUSENTE", "Ausente")
                .replace("EXPIRADA", "Expirada")
                .replace("SIN ESTADO", "Sin estado");

        if (limpio.startsWith("Estado modificado de ")) {
            return limpio.replaceFirst(
                    "Estado modificado de ",
                    "La reserva pasó de ");
        }
        return limpio;
    }
    private void cargarAuditoria(long reservaId) {
        try {
            auditorias.setAll(
                    auditoriaReservaService.listarPorReserva(reservaId));
            actualizarPresentacionAuditoria();
        } catch (RuntimeException exception) {
            auditorias.clear();
            actualizarPresentacionAuditoria();
            mostrarError("No se pudo cargar la auditoria: "
                    + exception.getMessage());
        }
    }

    private void limpiarAuditoria() {
        auditorias.clear();
        actualizarPresentacionAuditoria();
    }

    private void actualizarPresentacionAuditoria() {
        int cantidad = auditorias.size();
        boolean visible = cantidad > 0;
        contenedorAuditoria.setVisible(visible);
        contenedorAuditoria.setManaged(visible);
        if (!visible) {
            etiquetaCantidadAuditoria.setText("");
            listaAuditoria.setPrefHeight(0);
            listaAuditoria.setMinHeight(0);
            return;
        }

        etiquetaCantidadAuditoria.setText(cantidad == 1
                ? "1 movimiento"
                : cantidad + " movimientos");

        double altura = Math.min(300, Math.max(112, cantidad * 112));
        listaAuditoria.setMinHeight(Math.min(altura, 112));
        listaAuditoria.setPrefHeight(altura);
    }
    private void configurarFiltros() {
        filtroFecha.setValue(LocalDate.now());
        filtroFecha.valueProperty().addListener((observable, anterior, nueva) -> {
            actualizarEtiquetaFecha();
            aplicarFiltros();
        });
        filtroEstado.setItems(FXCollections.observableArrayList(EstadoReserva.values()));
        reservasFiltradas = new FilteredList<>(reservas, reserva -> true);
        tablaReservas.setItems(reservasFiltradas);
        campoBuscar.textProperty().addListener((obs, anterior, actual) -> aplicarFiltros());
        filtroEstado.valueProperty().addListener((obs, anterior, actual) -> aplicarFiltros());
        actualizarEtiquetaFecha();
    }

    private void configurarFormulario() {
        spinnerJugadores.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 4));
        selectorFecha.setDayCellFactory(control -> new javafx.scene.control.DateCell() {
            @Override public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isBefore(LocalDate.now()));
            }
        });
        comboCancha.valueProperty().addListener((obs, anterior, actual) -> {
            if (!actualizandoFormulario) actualizarDisponibilidad();
        });
        selectorFecha.valueProperty().addListener((obs, anterior, actual) -> {
            if (!actualizandoFormulario) actualizarDisponibilidad();
        });
        comboHorario.valueProperty().addListener((obs, anterior, actual) -> actualizarResumenPrecio());
    }

    @FXML private void usarClienteRegistrado() {
        if (reservaSeleccionada == null) configurarModoCliente(false);
    }

    @FXML private void usarClienteOcasional() {
        if (reservaSeleccionada == null) configurarModoCliente(true);
    }

    private void configurarModoCliente(boolean ocasional) {
        botonClienteRegistrado.setSelected(!ocasional);
        botonClienteOcasional.setSelected(ocasional);
        comboCliente.setVisible(!ocasional); comboCliente.setManaged(!ocasional);
        contenedorClienteOcasional.setVisible(ocasional); contenedorClienteOcasional.setManaged(ocasional);
        if (ocasional) { comboCliente.getSelectionModel().clearSelection(); campoClienteOcasionalNombre.requestFocus(); }
        else comboCliente.requestFocus();
    }

    @FXML private void cargarDatosBase() {
        try {
            comboCliente.setItems(FXCollections.observableArrayList(clienteService.listar()));
            comboCancha.setItems(FXCollections.observableArrayList(canchaService.listar()));
        } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
    }

    @FXML private void cargarReservas() {
        try {
            reservas.setAll(reservaService.listar());
            aplicarFiltros();
            mostrarInfo(reservas.size() + " reserva(s) cargada(s).");
        } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
    }

    private void aplicarSolicitudDesdeAgenda() {
        SolicitudReservaAgenda solicitud = Navegacion.consumirSolicitudReservaAgenda();
        if (solicitud == null) return;
        if (solicitud.tipo() == SolicitudReservaAgenda.Tipo.NUEVA) {
            prepararNuevaReservaDesdeAgenda(solicitud);
            return;
        }
        abrirReservaDesdeAgenda(solicitud.reservaId());
    }

    private void aplicarSolicitudFiltroDashboard() {
        SolicitudFiltroReservas solicitud = Navegacion.consumirSolicitudFiltroReservas();
        if (solicitud == null) return;
        filtroDashboard = solicitud.filtro();
        campoBuscar.clear();
        filtroEstado.getSelectionModel().clearSelection();
        filtroFecha.setValue(null);
        switch (filtroDashboard) {
            case PENDIENTES_SENIA -> {
                filtroEstado.setValue(EstadoReserva.PENDIENTE);
                mostrarInfo("Mostrando reservas vigentes esperando seña.");
            }
            case PROXIMAS_A_VENCER -> {
                filtroEstado.setValue(EstadoReserva.PENDIENTE);
                mostrarInfo("Mostrando solicitudes web que vencen dentro de los próximos 5 minutos.");
            }
            case PENDIENTES_CIERRE -> {
                filtroEstado.setValue(EstadoReserva.CONFIRMADA);
                mostrarInfo("Mostrando turnos finalizados pendientes de cierre.");
            }
        }
        aplicarFiltros();
    }

    private void prepararNuevaReservaDesdeAgenda(SolicitudReservaAgenda solicitud) {
        nuevaReserva();
        filtroFecha.setValue(solicitud.fecha());
        Cancha cancha = buscarCancha(solicitud.canchaId());
        if (cancha == null) { mostrarError("La cancha seleccionada ya no está disponible."); return; }
        comboCancha.setValue(cancha);
        selectorFecha.setValue(solicitud.fecha());
        actualizarDisponibilidad();
        LocalTime horario = solicitud.horaInicio();
        if (comboHorario.getItems().contains(horario)) {
            comboHorario.setValue(horario);
            mostrarInfo("Completá el cliente y guardá la reserva.");
            comboCliente.requestFocus();
        } else mostrarError("El horario seleccionado ya no está disponible.");
    }

    private void abrirReservaDesdeAgenda(Long reservaId) {
        if (reservaId == null || reservaId <= 0) { mostrarError("La reserva seleccionada no es válida."); return; }
        Reserva reserva = reservas.stream().filter(valor -> valor.getId() == reservaId).findFirst()
                .orElseGet(() -> reservaService.buscar(reservaId));
        if (reserva == null) { mostrarError("La reserva seleccionada ya no existe."); return; }
        if (!reservas.contains(reserva)) reservas.add(reserva);
        filtroFecha.setValue(reserva.getFecha());
        aplicarFiltros();
        tablaReservas.getSelectionModel().select(reserva);
        tablaReservas.scrollTo(reserva);
        mostrarDetalle(reserva);
        mostrarInfo("Reserva seleccionada desde la agenda.");
    }

    private void aplicarFiltros() {
        if (reservasFiltradas == null) return;
        String texto = campoBuscar.getText() == null ? "" : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        EstadoReserva estado = filtroEstado.getValue();
        LocalDate fecha = filtroFecha.getValue();
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusMinutes(5);
        reservasFiltradas.setPredicate(reserva -> {
            boolean coincideFecha = fecha == null || fecha.equals(reserva.getFecha());
            boolean coincideTexto = texto.isBlank() || contiene(reserva.getNombreCliente(), texto)
                    || contiene(reserva.getNombreCancha(), texto) || contiene(reserva.getEstado().toString(), texto);
            boolean coincideEstado = estado == null || reserva.getEstado() == estado;
            return coincideFecha && coincideTexto && coincideEstado && coincideFiltroDashboard(reserva, ahora, limite);
        });
    }

    private boolean coincideFiltroDashboard(Reserva reserva, LocalDateTime ahora, LocalDateTime limite) {
        if (filtroDashboard == null) return true;
        return switch (filtroDashboard) {
            case PENDIENTES_SENIA -> reserva.getEstado() == EstadoReserva.PENDIENTE
                    && (reserva.getFechaVencimiento() == null || reserva.getFechaVencimiento().isAfter(ahora));
            case PROXIMAS_A_VENCER -> reserva.getEstado() == EstadoReserva.PENDIENTE
                    && reserva.getFechaVencimiento() != null && reserva.getFechaVencimiento().isAfter(ahora)
                    && !reserva.getFechaVencimiento().isAfter(limite);
            case PENDIENTES_CIERRE -> reserva.getEstado() == EstadoReserva.CONFIRMADA
                    && reserva.getFecha() != null && reserva.getHoraFin() != null
                    && !LocalDateTime.of(reserva.getFecha(), reserva.getHoraFin()).isAfter(ahora);
        };
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    @FXML private void limpiarFiltros() {
        filtroDashboard = null;
        campoBuscar.clear();
        filtroEstado.getSelectionModel().clearSelection();
        filtroFecha.setValue(LocalDate.now());
        actualizarEtiquetaFecha();
        aplicarFiltros();
        mostrarInfo("Se eliminaron los filtros del Dashboard.");
    }

    @FXML private void nuevaReserva() {
        modoReprogramacion = false;
        reprogramacionAdministrativa = false;
        reservaSeleccionada = null;
        tablaReservas.getSelectionModel().clearSelection();
        tituloFormulario.setText("Nueva reserva");
        insigniaNuevaReserva.setVisible(true); insigniaNuevaReserva.setManaged(true);
        configurarModoCliente(false);
        configurarPanelAlta();
        campoClienteOcasionalNombre.clear(); campoClienteOcasionalTelefono.clear();
        campoClienteOcasionalEmail.clear(); campoClienteOcasionalDocumento.clear();
        scrollFormulario.setVvalue(0);
        subtituloFormulario.setText(
                "Completá los datos para registrar un nuevo turno.");
        insigniaEstadoDetalle.setVisible(false);
        insigniaEstadoDetalle.setManaged(false);
        insigniaOrigenDetalle.setVisible(false);
        insigniaOrigenDetalle.setManaged(false);
        etiquetaVencimientoDetalle.setVisible(false);
        etiquetaVencimientoDetalle.setManaged(false);
        etiquetaVencimientoDetalle.setText("");
        comboCliente.getSelectionModel().clearSelection();
        comboCancha.getSelectionModel().clearSelection();
        selectorFecha.setValue(LocalDate.now());
        comboHorario.getItems().clear();
        spinnerJugadores.getValueFactory().setValue(4);
        campoComentarios.clear();
        campoObservaciones.clear();
        etiquetaPrecio.setText("ARS 0");
        etiquetaSaldoPendiente.setText("ARS 0");
        avisoModoReprogramacion.setVisible(false);
        avisoModoReprogramacion.setManaged(false);
        contenedorAccionesReserva.setVisible(true);
        contenedorAccionesReserva.setManaged(true);
        contenedorCancelacion.setVisible(false);
        contenedorCancelacion.setManaged(false);
        etiquetaCancelacion.setText("");
        botonGuardar.setText("GUARDAR RESERVA");
        comboCliente.setDisable(false);
        spinnerJugadores.setDisable(false);
        limpiarMensaje();
        limpiarAuditoria();
        actualizarAccionesReserva();
    }

    private void mostrarDetalle(Reserva reserva) {
        reservaSeleccionada = reserva;
        tituloFormulario.setText("Detalle de reserva");
        insigniaNuevaReserva.setVisible(false); insigniaNuevaReserva.setManaged(false);
        configurarPanelDetalle(reserva);
        botonGuardar.setText("GUARDAR CAMBIOS");
        actualizarEncabezadoDetalle(reserva);
        actualizandoFormulario = true;
        try {
            comboCliente.setValue(buscarCliente(reserva.getClienteId()));
            comboCancha.setValue(buscarCancha(reserva.getCanchaId()));
            selectorFecha.setValue(reserva.getFecha());
        } finally {
            actualizandoFormulario = false;
        }
        if (esEstadoFinal(reserva)) {
            comboHorario.setItems(FXCollections.observableArrayList(reserva.getHoraInicio()));
            comboHorario.setValue(reserva.getHoraInicio());
            comboCancha.setValue(buscarCancha(reserva.getCanchaId()));
        } else {
            actualizarDisponibilidad();
            if (!comboHorario.getItems().contains(reserva.getHoraInicio())) {
                comboHorario.getItems().add(reserva.getHoraInicio());
            }
            comboHorario.setValue(reserva.getHoraInicio());
        }
        spinnerJugadores.getValueFactory().setValue(reserva.getCantidadJugadores());
        campoComentarios.setText(reserva.getComentarios());
        campoObservaciones.setText(reserva.getObservacionesAdministrativas());
        etiquetaPrecio.setText(FormateadorMoneda.pesos(reserva.getPrecioTotal()));
        try {
            etiquetaSaldoPendiente.setText(FormateadorMoneda.pesos(pagoService.calcularSaldo(reserva.getId())));
        } catch (RuntimeException exception) { etiquetaSaldoPendiente.setText("No disponible"); }
        actualizarAccionesReserva();
        mostrarDetalleCancelacion(reserva);
        cargarAuditoria(reserva.getId());
        aplicarEstadoEdicion(reserva);
    }

    // reservas-panel-boton-final-v14
    private void configurarPanelAlta() {
        panelAltaResponsable.setVisible(true);
        panelAltaResponsable.setManaged(true);
        panelResponsableInformativo.setVisible(false);
        panelResponsableInformativo.setManaged(false);
        botonNuevaReservaSuperior.getStyleClass().add(
                "reservation-new-button-active-v13");
        comboCancha.setDisable(false);
        selectorFecha.setDisable(false);
        comboHorario.setDisable(false);
        spinnerJugadores.setDisable(false);
        campoComentarios.setEditable(true);
        campoObservaciones.setEditable(true);
        botonGuardar.setVisible(true);
        botonGuardar.setManaged(true);
    }

    private void configurarPanelDetalle(Reserva reserva) {
        panelAltaResponsable.setVisible(false);
        panelAltaResponsable.setManaged(false);
        panelResponsableInformativo.setVisible(true);
        panelResponsableInformativo.setManaged(true);
        botonNuevaReservaSuperior.getStyleClass().remove(
                "reservation-new-button-active-v13");

        String nombre = reserva.getNombreCliente();
        etiquetaResponsableNombre.setText(
                nombre == null || nombre.isBlank()
                        ? "Responsable sin nombre" : nombre);
        etiquetaResponsableTipo.setText(
                reserva.esClienteOcasional()
                        ? "CLIENTE OCASIONAL" : "CLIENTE REGISTRADO");

        String contacto;
        if (reserva.esClienteOcasional()) {
            contacto = reserva.getClienteOcasionalTelefono();
        } else {
            Cliente cliente = buscarCliente(reserva.getClienteId());
            contacto = cliente == null ? null : cliente.getTelefono();
        }
        boolean tieneContacto = contacto != null && !contacto.isBlank();
        etiquetaResponsableContacto.setText(
                tieneContacto ? contacto : "Sin teléfono registrado");
        etiquetaResponsableContacto.setVisible(true);
        etiquetaResponsableContacto.setManaged(true);
    }

    private void aplicarEstadoEdicion(Reserva reserva) {
        boolean finalizada = esEstadoFinal(reserva);
        comboCancha.setDisable(finalizada);
        selectorFecha.setDisable(finalizada);
        comboHorario.setDisable(finalizada);
        spinnerJugadores.setDisable(finalizada);
        campoComentarios.setEditable(!finalizada);
        campoObservaciones.setEditable(!finalizada);
        botonGuardar.setVisible(!finalizada);
        botonGuardar.setManaged(!finalizada);

        if (finalizada && !subtituloFormulario.getText().contains(
                "información de solo lectura")) {
            subtituloFormulario.setText(subtituloFormulario.getText()
                    + "\nReserva finalizada · información de solo lectura");
        }
    }

    // corregir-compilacion-reservas-v13
    private boolean esEstadoFinal(Reserva reserva) {
        return reserva != null && reserva.getEstado() != null
                && reserva.getEstado().esFinal();
    }

    private void actualizarEncabezadoDetalle(Reserva reserva) {
        String cliente = reserva.getNombreCliente() == null
                || reserva.getNombreCliente().isBlank()
                        ? "Cliente sin nombre" : reserva.getNombreCliente();
        String cancha = reserva.getNombreCancha() == null
                || reserva.getNombreCancha().isBlank()
                        ? "Cancha sin nombre" : reserva.getNombreCancha();
        String fecha = reserva.getFecha() == null
                ? "Sin fecha" : reserva.getFecha().format(FORMATO_FECHA);
        String horario = reserva.getHoraInicio() == null
                || reserva.getHoraFin() == null
                        ? "Sin horario"
                        : reserva.getHoraInicio().format(FORMATO_HORA)
                                + " a "
                                + reserva.getHoraFin().format(FORMATO_HORA);
        subtituloFormulario.setText(cliente + " · " + cancha
                + "\n" + fecha + " · " + horario);

        insigniaEstadoDetalle.setText(nombreEstado(reserva.getEstado()));
        insigniaEstadoDetalle.getStyleClass().removeIf(
                clase -> clase.startsWith("reservation-detail-status-")
                        && !clase.equals(
                                "reservation-detail-status-badge"));
        insigniaEstadoDetalle.getStyleClass().add(
                "reservation-detail-status-"
                        + reserva.getEstado().name()
                                .toLowerCase(Locale.ROOT));
        insigniaEstadoDetalle.setVisible(true);
        insigniaEstadoDetalle.setManaged(true);

        insigniaOrigenDetalle.setText(
                reserva.esSolicitudWeb() ? "WEB" : "PERSONAL");
        insigniaOrigenDetalle.getStyleClass().removeAll(
                "reservation-origin-web", "reservation-origin-personal");
        insigniaOrigenDetalle.getStyleClass().add(
                reserva.esSolicitudWeb()
                        ? "reservation-origin-web"
                        : "reservation-origin-personal");
        insigniaOrigenDetalle.setVisible(true);
        insigniaOrigenDetalle.setManaged(true);

        boolean mostrarVencimiento = reserva.esSolicitudWeb()
                && reserva.getEstado() == EstadoReserva.PENDIENTE
                && reserva.getFechaVencimiento() != null;
        etiquetaVencimientoDetalle.setVisible(mostrarVencimiento);
        etiquetaVencimientoDetalle.setManaged(mostrarVencimiento);
        if (mostrarVencimiento) {
            etiquetaVencimientoDetalle.setText(
                    "La solicitud web vence el "
                            + reserva.getFechaVencimiento().format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy 'a las' HH:mm")));
        } else {
            etiquetaVencimientoDetalle.setText("");
        }
    }

    private void mostrarDetalleCancelacion(Reserva reserva) {
        boolean cancelada = reserva.getEstado() == EstadoReserva.CANCELADA && reserva.getTipoCancelacion() != null;
        contenedorCancelacion.setVisible(cancelada);
        contenedorCancelacion.setManaged(cancelada);
        if (!cancelada) { etiquetaCancelacion.setText(""); return; }
        String tipo = switch (reserva.getTipoCancelacion()) {
            case CLIENTE -> "Cancelación normal";
            case ADMINISTRATIVA -> "Cancelación administrativa";
        };
        String motivo = reserva.getMotivoCancelacion() == null ? "Sin motivo registrado" : reserva.getMotivoCancelacion();
        String fecha = reserva.getFechaCancelacion() == null ? "Sin fecha registrada"
                : reserva.getFechaCancelacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        etiquetaCancelacion.setText(tipo + "\nMotivo: " + motivo + "\nFecha: " + fecha);
    }

    @FXML private void abrirWhatsApp() {
        if (reservaSeleccionada == null) { mostrarError("Seleccioná una reserva antes de abrir WhatsApp."); return; }
        Cliente cliente = buscarCliente(reservaSeleccionada.getClienteId());
        if (reservaSeleccionada.esClienteOcasional()) {
            String telefono = reservaSeleccionada.getClienteOcasionalTelefono();
            if (telefono == null || telefono.isBlank()) { mostrarError("El cliente ocasional no tiene un teléfono registrado."); return; }
            try { whatsAppService.abrirConversacion(telefono, "Hola " + reservaSeleccionada.getNombreCliente() + ", te contactamos por tu reserva."); }
            catch (RuntimeException exception) { mostrarError("No se pudo abrir WhatsApp: " + exception.getMessage()); }
            return;
        }
        if (cliente == null) { mostrarError("No se pudo encontrar el cliente de la reserva."); return; }
        if (cliente.getTelefono() == null || cliente.getTelefono().isBlank()) {
            mostrarError("El cliente no tiene un teléfono registrado.");
            return;
        }
        try {
            String mensaje = elegirMensajeWhatsApp(cliente, reservaSeleccionada);
            whatsAppService.abrirConversacion(cliente.getTelefono(), mensaje);
            mostrarInfo("Se abrió WhatsApp para " + cliente.getNombreCompleto() + ".");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            mostrarError(exception.getMessage());
        } catch (RuntimeException exception) {
            mostrarError("No se pudo abrir WhatsApp: " + exception.getMessage());
        }
    }

    private String elegirMensajeWhatsApp(Cliente cliente, Reserva reserva) {
        return switch (reserva.getEstado()) {
            case PENDIENTE -> mensajeReservaService.crearSolicitudSenia(cliente, reserva, calcularSeniaPendiente(reserva));
            case CONFIRMADA -> {
                BigDecimal saldo = calcularSaldoSeguro(reserva);
                yield saldo.signum() > 0
                        ? mensajeReservaService.crearAvisoSaldo(cliente, reserva, saldo)
                        : mensajeReservaService.crearRecordatorio(cliente, reserva);
            }
            case CANCELADA, EXPIRADA -> mensajeReservaService.crearAvisoCancelacion(cliente, reserva);
            case COMPLETADA, AUSENTE -> mensajeReservaService.crearRecordatorio(cliente, reserva);
        };
    }

    private BigDecimal calcularSeniaPendiente(Reserva reserva) {
        ConfiguracionComplejo configuracion = configuracionComplejoService.obtener();
        BigDecimal porcentaje = configuracion.getPorcentajeSenia() == null
                ? BigDecimal.ZERO : configuracion.getPorcentajeSenia();
        BigDecimal precio = reserva.getPrecioTotal() == null ? BigDecimal.ZERO : reserva.getPrecioTotal();
        BigDecimal requerida = precio.multiply(porcentaje)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal acreditado = pagoService.totalAcreditado(reserva.getId());
        BigDecimal pendiente = requerida.subtract(acreditado == null ? BigDecimal.ZERO : acreditado);
        return pendiente.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularSaldoSeguro(Reserva reserva) {
        BigDecimal saldo = pagoService.calcularSaldo(reserva.getId());
        return saldo == null ? BigDecimal.ZERO : saldo.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    @FXML private void actualizarDisponibilidad() {
        Cancha cancha = comboCancha.getValue();
        LocalDate fecha = selectorFecha.getValue();
        comboHorario.getItems().clear();
        limpiarMensaje();
        if (cancha == null || fecha == null) { actualizarResumenPrecio(); return; }
        try {
            long excluida = reservaSeleccionada == null ? 0L : reservaSeleccionada.getId();
            List<LocalTime> horarios = reservaService.listarHorariosDisponibles(cancha.getId(), fecha, excluida);
            comboHorario.setItems(FXCollections.observableArrayList(horarios));
            if (horarios.isEmpty()) mostrarError("No hay horarios disponibles para esa fecha.");
            else { comboHorario.getSelectionModel().selectFirst(); limpiarMensaje(); }
            actualizarResumenPrecio();
        } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
    }

    private void actualizarResumenPrecio() {
        Cancha cancha = comboCancha.getValue();
        etiquetaPrecio.setText(FormateadorMoneda.pesos(cancha == null ? null : cancha.getPrecio()));
    }

    private void actualizarAccionesReserva() {
        boolean seleccionada = reservaSeleccionada != null;
        configurarBoton(botonWhatsApp, seleccionada, seleccionada, "ABRIR WHATSAPP");
        if (!seleccionada) {
            configurarBoton(botonAccionPrincipal, false, false, "CERRAR TURNO");
            configurarBoton(botonPagos, false, false, "ABRIR PAGOS");
            configurarBoton(botonCancelar, false, false, "CANCELAR RESERVA");
            configurarBoton(botonReprogramar, false, false, "REPROGRAMAR");
            return;
        }
        EstadoReserva estado = reservaSeleccionada.getEstado();
        boolean pendiente = estado == EstadoReserva.PENDIENTE;
        boolean confirmada = estado == EstadoReserva.CONFIRMADA;
        configurarBoton(botonAccionPrincipal, confirmada && turnoFinalizado(reservaSeleccionada),
                confirmada && turnoFinalizado(reservaSeleccionada), "CERRAR TURNO");
        boolean mostrarPagos = pendiente || confirmada || estado == EstadoReserva.COMPLETADA;
        configurarBoton(botonPagos, mostrarPagos, mostrarPagos, pendiente ? "REGISTRAR SEÑA" : "ABRIR PAGOS");
        configurarBoton(botonCancelar, pendiente || confirmada, pendiente || confirmada, "CANCELAR RESERVA");
        configurarBoton(botonReprogramar, pendiente || confirmada, pendiente || confirmada, "REPROGRAMAR");
    }

    private void configurarBoton(Button boton, boolean visible, boolean administrado, String texto) {
        boton.setVisible(visible); boton.setManaged(administrado); boton.setText(texto);
    }

    @FXML private void ejecutarAccionPrincipal() {
        if (reservaSeleccionada == null) { mostrarError("Seleccioná una reserva de la tabla."); return; }
        if (reservaSeleccionada.getEstado() == EstadoReserva.PENDIENTE) {
            mostrarError("La reserva se confirmará automáticamente cuando los pagos acreditados alcancen el importe mínimo de la seña.");
            return;
        }
        if (reservaSeleccionada.getEstado() == EstadoReserva.CONFIRMADA && turnoFinalizado(reservaSeleccionada))
            mostrarDialogoCierreTurno();
    }

    private void mostrarDialogoCierreTurno() {
        ButtonType completada = new ButtonType("Se jugó normalmente");
        ButtonType ausente = new ButtonType("No se presentó");
        ButtonType volver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION, "", completada, ausente, volver);
        dialogo.setTitle("Cerrar turno");
        dialogo.setHeaderText("¿Cómo finalizó la reserva?");
        dialogo.setContentText(reservaSeleccionada.getNombreCliente() + "\n" + reservaSeleccionada.getNombreCancha()
                + "\n" + reservaSeleccionada.getHoraInicio().format(FORMATO_HORA) + " - "
                + reservaSeleccionada.getHoraFin().format(FORMATO_HORA));
        Dialogos.preparar(dialogo, "dialog-choice");
        // dialogos-reservas-prioridad-final-v16
        dialogo.getDialogPane().setPrefWidth(940);
        dialogo.getDialogPane().setMinWidth(880);
        javafx.scene.Node botonCompletada = dialogo.getDialogPane()
                .lookupButton(completada);
        javafx.scene.Node botonAusente = dialogo.getDialogPane()
                .lookupButton(ausente);
        javafx.scene.Node botonVolver = dialogo.getDialogPane()
                .lookupButton(volver);
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        botonCompletada.getStyleClass().add(
                "reservation-close-complete-v15");
        botonAusente.getStyleClass().add(
                "reservation-close-absent-v15");
        botonVolver.getStyleClass().add(
                "reservation-close-back-v15");
        dialogo.getDialogPane().getStyleClass().add(
                "reservation-close-dialog-v15");
        dialogo.showAndWait().ifPresent(respuesta -> {
            if (respuesta == completada) cambiarEstadoSeleccionado(EstadoReserva.COMPLETADA);
            else if (respuesta == ausente) cambiarEstadoSeleccionado(EstadoReserva.AUSENTE);
        });
    }

    @FXML private void guardar() {
        if (modoReprogramacion) { guardarReprogramacion(); return; }
        try {
            validarSeleccionFormulario();
            Reserva reserva = reservaSeleccionada == null ? new Reserva() : reservaSeleccionada;
            if (reservaSeleccionada == null) {
                if (botonClienteOcasional.isSelected()) {
                    reserva.setClienteId(0L);
                    reserva.setClienteOcasionalNombre(campoClienteOcasionalNombre.getText());
                    reserva.setClienteOcasionalTelefono(campoClienteOcasionalTelefono.getText());
                    reserva.setClienteOcasionalEmail(campoClienteOcasionalEmail.getText());
                    reserva.setClienteOcasionalDocumento(campoClienteOcasionalDocumento.getText());
                } else {
                    reserva.setClienteId(comboCliente.getValue().getId());
                    reserva.setClienteOcasionalNombre(null);
                    reserva.setClienteOcasionalTelefono(null);
                    reserva.setClienteOcasionalEmail(null);
                    reserva.setClienteOcasionalDocumento(null);
                }
            }
            reserva.setCanchaId(comboCancha.getValue().getId());
            reserva.setUsuarioId(Navegacion.getUsuarioActual().getId());
            reserva.setFecha(selectorFecha.getValue());
            reserva.setHoraInicio(comboHorario.getValue());
            reserva.setCantidadJugadores(spinnerJugadores.getValue());
            reserva.setComentarios(campoComentarios.getText());
            reserva.setObservacionesAdministrativas(campoObservaciones.getText());
            if (reservaSeleccionada == null) {
                reserva.setEstado(EstadoReserva.PENDIENTE);
                reserva.setFechaVencimiento(null);
                reserva.setFechaExpiracion(null);
            }
            reservaService.guardar(reserva);
            cargarReservas(); nuevaReserva(); mostrarInfo("La reserva se guardó correctamente.");
        } catch (IllegalArgumentException exception) { mostrarError(exception.getMessage()); }
        catch (RuntimeException exception) { mostrarError("No se pudo guardar la reserva: " + exception.getMessage()); }
    }

    @FXML private void iniciarReprogramacion() {
        if (reservaSeleccionada == null) { mostrarError("Seleccioná una reserva para reprogramarla."); return; }
        EstadoReserva estado = reservaSeleccionada.getEstado();
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            mostrarError("Solo se pueden reprogramar reservas pendientes o confirmadas."); return;
        }
        try {
            politicaReservaService.validarPlazoCancelacion(reservaSeleccionada);
            prepararModoReprogramacion(false);
        } catch (IllegalArgumentException exception) { ofrecerReprogramacionAdministrativa(exception.getMessage()); }
    }

    private void prepararModoReprogramacion(boolean administrativa) {
        reprogramacionAdministrativa = administrativa;
        modoReprogramacion = true;
        tituloFormulario.setText(administrativa
                ? "Reprogramación administrativa"
                : "Reprogramar reserva");
        subtituloFormulario.setText(administrativa
                ? "Seleccioná el nuevo turno y registrá el motivo administrativo."
                : "Seleccioná una nueva cancha, fecha y horario.");
        botonGuardar.setText("GUARDAR REPROGRAMACIÓN");
        comboCliente.setDisable(true); spinnerJugadores.setDisable(true);
        comboCancha.setDisable(false); selectorFecha.setDisable(false); comboHorario.setDisable(false);
        botonReprogramar.setVisible(false);
        botonReprogramar.setManaged(false);
        avisoModoReprogramacion.setVisible(true);
        avisoModoReprogramacion.setManaged(true);
        contenedorAccionesReserva.setVisible(false);
        contenedorAccionesReserva.setManaged(false);
        scrollFormulario.setVvalue(0);
        Platform.runLater(() -> {
            scrollFormulario.setVvalue(0);
            comboCancha.requestFocus();
        });
        mostrarInfo(administrativa
                ? "Modo reprogramación administrativa activo. Completá el nuevo turno y guardá los cambios."
                : "Modo reprogramación activo. Completá el nuevo turno y guardá los cambios.");
    }

    private void ofrecerReprogramacionAdministrativa(String restriccion) {
        String detalle = restriccion
                + "\n\nSi el cambio corresponde a una situacion atribuible "
                + "al complejo, podes realizar una reprogramacion administrativa.";
        if (Dialogos.confirmarAccion(
                "Reprogramacion fuera de plazo",
                "La reserva no puede reprogramarse normalmente",
                detalle,
                "REPROGRAMACION ADMINISTRATIVA")) {
            prepararModoReprogramacion(true);
        }
    }

    private void guardarReprogramacion() {
        if (reservaSeleccionada == null) { mostrarError("La reserva seleccionada ya no está disponible."); cancelarModoReprogramacion(); return; }
        if (comboCancha.getValue() == null || selectorFecha.getValue() == null || comboHorario.getValue() == null) {
            mostrarError("Seleccioná la nueva cancha, fecha y horario."); return;
        }
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Motivo de reprogramación"); dialogo.setHeaderText("Ingresá el motivo del cambio");
        dialogo.setContentText("Motivo:"); dialogo.getEditor().setPromptText("Ejemplo: solicitud del cliente o problema operativo");
        dialogo.showAndWait().ifPresent(this::ejecutarReprogramacion);
    }

    private void ejecutarReprogramacion(String motivo) {
        if (motivo == null || motivo.isBlank()) { mostrarError("El motivo de la reprogramación es obligatorio."); return; }
        if (Navegacion.getUsuarioActual() == null) { mostrarError("La sesión administrativa finalizó."); return; }
        try {
            long id = reservaSeleccionada.getId(); LocalDate nuevaFecha = selectorFecha.getValue();
            if (reprogramacionAdministrativa)
                reprogramacionService.reprogramarAdministrativamente(id, comboCancha.getValue().getId(), nuevaFecha,
                        comboHorario.getValue(), motivo, Navegacion.getUsuarioActual().getId());
            else reprogramacionService.reprogramar(id, comboCancha.getValue().getId(), nuevaFecha,
                    comboHorario.getValue(), motivo, Navegacion.getUsuarioActual().getId());
            cancelarModoReprogramacion(); cargarReservas(); filtroFecha.setValue(nuevaFecha); aplicarFiltros();
            Reserva actualizada = reservas.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
            if (actualizada != null) { tablaReservas.getSelectionModel().select(actualizada); tablaReservas.scrollTo(actualizada); mostrarDetalle(actualizada); }
            mostrarInfo("La reserva se reprogramó correctamente. Los pagos se conservaron.");
        } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
    }

    private void cancelarModoReprogramacion() {
        modoReprogramacion = false; reprogramacionAdministrativa = false;
        botonGuardar.setText("GUARDAR RESERVA"); comboCliente.setDisable(false); spinnerJugadores.setDisable(false);
        comboCancha.setDisable(false);
        selectorFecha.setDisable(false);
        comboHorario.setDisable(false);
        avisoModoReprogramacion.setVisible(false);
        avisoModoReprogramacion.setManaged(false);
        contenedorAccionesReserva.setVisible(true);
        contenedorAccionesReserva.setManaged(true);
        actualizarAccionesReserva();
    }

    private void validarSeleccionFormulario() {
        if (reservaSeleccionada == null) {
            if (botonClienteOcasional.isSelected()) {
                if (campoClienteOcasionalNombre.getText() == null
                        || campoClienteOcasionalNombre.getText().isBlank()) {
                    throw new IllegalArgumentException(
                            "Ingresá el nombre del cliente ocasional.");
                }
            } else if (comboCliente.getValue() == null) {
                throw new IllegalArgumentException("Seleccioná un cliente.");
            }
        }
        if (comboCancha.getValue() == null) throw new IllegalArgumentException("Seleccioná una cancha.");
        if (selectorFecha.getValue() == null) throw new IllegalArgumentException("Seleccioná una fecha.");
        if (comboHorario.getValue() == null) throw new IllegalArgumentException("Seleccioná un horario disponible.");
        if (Navegacion.getUsuarioActual() == null) throw new IllegalArgumentException("La sesión administrativa finalizó.");
        if (reservaSeleccionada == null) politicaReservaService.validarAnticipacionMinima(selectorFecha.getValue(), comboHorario.getValue());
    }

    @FXML private void cancelarReserva() {
        if (reservaSeleccionada == null) { mostrarError("Seleccioná una reserva de la tabla."); return; }
        try { politicaReservaService.validarPlazoCancelacion(reservaSeleccionada); mostrarOpcionesCancelacionDentroDePlazo(); }
        catch (IllegalArgumentException exception) { mostrarOpcionesCancelacionFueraDePlazo(exception.getMessage()); }
    }

    private boolean tienePagosAcreditados() {
        return reservaSeleccionada != null && pagoService.tienePagosAcreditados(reservaSeleccionada.getId());
    }

    private String totalAcreditadoSeleccionado() {
        if (reservaSeleccionada == null) return FormateadorMoneda.pesos(null);
        return FormateadorMoneda.pesos(pagoService.totalAcreditado(reservaSeleccionada.getId()));
    }

    private void mostrarOpcionesCancelacionDentroDePlazo() {
        boolean tienePagos = tienePagosAcreditados();
        String pagos = tienePagos
                ? "\nPagos acreditados: " + totalAcreditadoSeleccionado()
                        + "\n\nLa seña puede trasladarse a otro horario o reembolsarse."
                : "\nLa reserva no tiene pagos acreditados.";
        String detalle = reservaSeleccionada.getNombreCliente() + "\n"
                + reservaSeleccionada.getNombreCancha() + "\n"
                + reservaSeleccionada.getFecha().format(FORMATO_FECHA) + " "
                + reservaSeleccionada.getHoraInicio().format(FORMATO_HORA)
                + pagos;
        Dialogos.Opcion opcion = Dialogos.elegir(
                "Cancelar reserva",
                "La reserva esta dentro del plazo permitido",
                detalle,
                "REPROGRAMAR",
                tienePagos ? "CANCELAR Y REEMBOLSAR" : "CANCELAR RESERVA",
                false,
                true);
        if (opcion == Dialogos.Opcion.PRINCIPAL) {
            iniciarReprogramacion();
        } else if (opcion == Dialogos.Opcion.ALTERNATIVA) {
            if (tienePagos) confirmarCancelacionConReembolso(false);
            else confirmarCancelacionSinPagos();
        }
    }

    private void confirmarCancelacionSinPagos() {
        if (Dialogos.confirmarPeligro(
                "Cancelar reserva",
                "La reserva no tiene pagos acreditados.\n\n"
                        + "El horario volvera a quedar disponible.")) {
            ejecutarCancelacionNormal();
        }
    }

    private void mostrarOpcionesCancelacionFueraDePlazo(String restriccion) {
        boolean tienePagos = tienePagosAcreditados();
        String pago = tienePagos
                ? "\n\nLa seña acreditada de " + totalAcreditadoSeleccionado()
                        + " quedara retenida."
                : "";
        Dialogos.Opcion opcion = Dialogos.elegir(
                "Cancelacion fuera de plazo",
                "La reserva esta fuera del plazo permitido",
                restriccion + pago
                        + "\n\nUna excepcion administrativa solo debe utilizarse "
                        + "cuando la cancelacion sea responsabilidad del complejo.",
                tienePagos ? "CANCELAR Y RETENER SEÑA" : "CANCELAR RESERVA",
                "EXCEPCION ADMINISTRATIVA",
                true,
                false);
        if (opcion == Dialogos.Opcion.PRINCIPAL) {
            confirmarCancelacionConSeniaRetenida(true);
        } else if (opcion == Dialogos.Opcion.ALTERNATIVA) {
            mostrarOpcionesAdministrativas();
        }
    }

    private void mostrarOpcionesAdministrativas() {
        boolean tienePagos = tienePagosAcreditados();
        Dialogos.Opcion opcion = Dialogos.elegir(
                "Excepcion administrativa",
                "Selecciona la solucion para el cliente",
                "Esta operacion debe utilizarse unicamente por lluvia, "
                        + "mantenimiento, corte de energia u otra situacion "
                        + "atribuible al complejo.",
                "REPROGRAMAR",
                tienePagos ? "CANCELAR Y REEMBOLSAR" : "CANCELAR RESERVA",
                false,
                true);
        if (opcion == Dialogos.Opcion.PRINCIPAL) {
            prepararModoReprogramacion(true);
        } else if (opcion == Dialogos.Opcion.ALTERNATIVA) {
            solicitarMotivoAdministrativo(tienePagos);
        }
    }

    private void confirmarCancelacionConSeniaRetenida(boolean fueraDePlazo) {
        boolean tienePagos = tienePagosAcreditados();
        String detalle = tienePagos
                ? "El importe acreditado de " + totalAcreditadoSeleccionado()
                        + " no sera reembolsado."
                : "La reserva quedara cancelada.";
        if (fueraDePlazo) {
            detalle += "\n\nLa cancelacion se registrara fuera del plazo permitido.";
        }
        String titulo = tienePagos
                ? "Cancelar y retener la seña"
                : "Cancelar reserva";
        if (Dialogos.confirmarPeligro(titulo, detalle)) {
            ejecutarCancelacionNormal();
        }
    }    private void ejecutarCancelacionNormal() {
        try {
            LocalDate fecha = reservaSeleccionada.getFecha(); boolean teniaSenia = tienePagosAcreditados();
            reservaService.cancelarNormal(reservaSeleccionada.getId());
            recargarDespuesDeCancelar(fecha, teniaSenia ? "La reserva fue cancelada y la seña quedó retenida." : "La reserva fue cancelada correctamente.");
        } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
    }

    private void confirmarCancelacionConReembolso(boolean administrativa) {
        if (Dialogos.confirmarPeligro(
                "Cancelar y reembolsar",
                "Se cancelara la reserva y se reembolsara "
                        + totalAcreditadoSeleccionado() + ".\n\n"
                        + "Todos los pagos acreditados pasaran a estado Reembolsado.")) {
            if (administrativa) solicitarMotivoAdministrativo(true);
            else ejecutarCancelacionYReembolsoNormal();
        }
    }

    private void ejecutarCancelacionYReembolsoNormal() {
        try {
            long id = reservaSeleccionada.getId(); LocalDate fecha = reservaSeleccionada.getFecha();
            reservaService.cancelarNormal(id); pagoService.reembolsarPagosDeReservaAutorizado(id);
            recargarDespuesDeCancelar(fecha, "La reserva fue cancelada y los pagos acreditados fueron reembolsados.");
        } catch (RuntimeException exception) { mostrarError("La operación no pudo completarse: " + exception.getMessage()); }
    }

    private void solicitarMotivoAdministrativo(boolean reembolsar) {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Cancelación administrativa"); dialogo.setHeaderText("Ingresá el motivo de la cancelación");
        dialogo.setContentText("Motivo:"); dialogo.getEditor().setPromptText("Ejemplo: lluvia, corte de energía o mantenimiento");
        dialogo.showAndWait().ifPresent(motivo -> {
            if (motivo == null || motivo.isBlank()) { mostrarError("El motivo administrativo es obligatorio."); return; }
            ejecutarCancelacionAdministrativa(motivo, reembolsar);
        });
    }

    private void ejecutarCancelacionAdministrativa(String motivo, boolean reembolsar) {
        try {
            long id = reservaSeleccionada.getId(); LocalDate fecha = reservaSeleccionada.getFecha();
            reservaService.cancelarAdministrativamente(id, motivo, Navegacion.getUsuarioActual().getId());
            if (reembolsar) pagoService.reembolsarPagosDeReservaAutorizado(id);
            recargarDespuesDeCancelar(fecha, reembolsar ? "La reserva fue cancelada administrativamente y los pagos fueron reembolsados." : "La reserva fue cancelada administrativamente.");
        } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
    }

    private void recargarDespuesDeCancelar(LocalDate fecha, String mensaje) {
        cargarReservas(); nuevaReserva(); filtroFecha.setValue(fecha); aplicarFiltros(); mostrarInfo(mensaje);
    }

    private void configurarTextoLargo(Alert alerta, String contenido) {
        Label texto = new Label(contenido); texto.setWrapText(true); texto.setMaxWidth(Double.MAX_VALUE);
        texto.setMinHeight(Region.USE_PREF_SIZE); texto.setPrefWidth(500);
        alerta.getDialogPane().setContent(texto); alerta.getDialogPane().setPrefWidth(580); alerta.setResizable(true);
    }

    private boolean turnoFinalizado(Reserva reserva) {
        return !LocalDateTime.of(reserva.getFecha(), reserva.getHoraFin()).isAfter(LocalDateTime.now());
    }

    private Cliente buscarCliente(long id) {
        return comboCliente.getItems().stream().filter(cliente -> cliente.getId() == id).findFirst().orElse(null);
    }

    private Cancha buscarCancha(long id) {
        return comboCancha.getItems().stream().filter(cancha -> cancha.getId() == id).findFirst().orElse(null);
    }

    @FXML private void diaAnterior() {
        LocalDate fecha = filtroFecha.getValue() == null ? LocalDate.now() : filtroFecha.getValue();
        filtroFecha.setValue(fecha.minusDays(1));
    }

    @FXML private void diaSiguiente() {
        LocalDate fecha = filtroFecha.getValue() == null ? LocalDate.now() : filtroFecha.getValue();
        filtroFecha.setValue(fecha.plusDays(1));
    }

    @FXML private void irHoy() {
        if (LocalDate.now().equals(filtroFecha.getValue())) { aplicarFiltros(); actualizarEtiquetaFecha(); }
        else filtroFecha.setValue(LocalDate.now());
    }

    private void actualizarEtiquetaFecha() {
        LocalDate fecha = filtroFecha.getValue();
        if (fecha == null) { etiquetaFechaSeleccionada.setText("Todas las fechas"); return; }
        if (fecha.equals(LocalDate.now())) { etiquetaFechaSeleccionada.setText("Reservas de hoy"); return; }
        if (fecha.equals(LocalDate.now().plusDays(1))) { etiquetaFechaSeleccionada.setText("Reservas de mañana"); return; }
        if (fecha.equals(LocalDate.now().minusDays(1))) { etiquetaFechaSeleccionada.setText("Reservas de ayer"); return; }
        String texto = fecha.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", new Locale("es", "AR")));
        etiquetaFechaSeleccionada.setText(Character.toUpperCase(texto.charAt(0)) + texto.substring(1));
    }

    private void cambiarEstadoSeleccionado(EstadoReserva estado) {
        if (reservaSeleccionada == null) { mostrarError("Seleccioná una reserva de la tabla."); return; }
        String detalle = reservaSeleccionada.getNombreCliente() + " · "
                + reservaSeleccionada.getNombreCancha();
        boolean peligrosa = estado == EstadoReserva.CANCELADA
                || estado == EstadoReserva.AUSENTE;
        boolean confirmado = peligrosa
                ? Dialogos.confirmarPeligro("Cambiar estado",
                        "¿Cambiar la reserva a " + estado + "?\n\n" + detalle)
                : Dialogos.confirmar("Cambiar estado",
                        "¿Cambiar la reserva a " + estado + "?\n\n" + detalle);
        if (!confirmado) return;
        try {
            LocalDate fecha = reservaSeleccionada.getFecha();
            reservaService.cambiarEstado(reservaSeleccionada.getId(), estado);
            cargarReservas();
            nuevaReserva();
            filtroFecha.setValue(fecha);
            aplicarFiltros();
            mostrarInfo("El estado se actualizó correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    @FXML private void abrirPagos() {
        if (reservaSeleccionada == null) { mostrarError("Seleccioná una reserva antes de abrir sus pagos."); return; }
        Navegacion.mostrarPagosDeReserva(reservaSeleccionada.getId());
    }

    @FXML private void volver() { Navegacion.mostrarDashboard(Navegacion.getUsuarioActual()); }

    private void mostrarError(String mensaje) {
        etiquetaMensaje.setText(mensaje == null ? "Ocurrió un error." : mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-exito");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) etiquetaMensaje.getStyleClass().add("mensaje-error");
    }

    private void mostrarInfo(String mensaje) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-error");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-exito")) etiquetaMensaje.getStyleClass().add("mensaje-exito");
    }

    private void limpiarMensaje() {
        etiquetaMensaje.setText("");
        etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
    }
}
