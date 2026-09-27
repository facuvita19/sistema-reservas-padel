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
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
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
import vista.FiltroReservas;
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
    @FXML private Label etiquetaMensaje;
    @FXML private Label etiquetaPrecio;
    @FXML private Label etiquetaSaldoPendiente;
    @FXML private ComboBox<Cliente> comboCliente;
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
    @FXML private VBox contenedorCancelacion;
    @FXML private Label etiquetaCancelacion;
    @FXML private VBox contenedorAuditoria;
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
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        tablaReservas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) mostrarDetalle(actual);
        });
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
                        estados.getStyleClass().add("audit-card-states");

                        Label detalle = new Label(humanizarDetalle(auditoria));
                        detalle.setWrapText(true);
                        detalle.setMaxWidth(Double.MAX_VALUE);
                        detalle.getStyleClass().add("audit-card-detail");

                        VBox tarjeta = new VBox(
                                5, accion, metadatos, estados, detalle);
                        tarjeta.getStyleClass().add("audit-card");
                        tarjeta.setFillWidth(true);

                        setText(null);
                        setGraphic(tarjeta);
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
            contenedorAuditoria.setVisible(true);
            contenedorAuditoria.setManaged(true);
        } catch (RuntimeException exception) {
            auditorias.clear();
            contenedorAuditoria.setVisible(true);
            contenedorAuditoria.setManaged(true);
            mostrarError("No se pudo cargar la auditoria: "
                    + exception.getMessage());
        }
    }

    private void limpiarAuditoria() {
        auditorias.clear();
        contenedorAuditoria.setVisible(false);
        contenedorAuditoria.setManaged(false);
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
        comboCancha.valueProperty().addListener((obs, anterior, actual) -> actualizarDisponibilidad());
        selectorFecha.valueProperty().addListener((obs, anterior, actual) -> actualizarDisponibilidad());
        comboHorario.valueProperty().addListener((obs, anterior, actual) -> actualizarResumenPrecio());
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
        comboCliente.getSelectionModel().clearSelection();
        comboCancha.getSelectionModel().clearSelection();
        selectorFecha.setValue(LocalDate.now());
        comboHorario.getItems().clear();
        spinnerJugadores.getValueFactory().setValue(4);
        campoComentarios.clear();
        campoObservaciones.clear();
        etiquetaPrecio.setText("ARS 0");
        etiquetaSaldoPendiente.setText("ARS 0");
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
        comboCliente.setValue(buscarCliente(reserva.getClienteId()));
        comboCancha.setValue(buscarCancha(reserva.getCanchaId()));
        selectorFecha.setValue(reserva.getFecha());
        actualizarDisponibilidad();
        if (!comboHorario.getItems().contains(reserva.getHoraInicio())) comboHorario.getItems().add(reserva.getHoraInicio());
        comboHorario.setValue(reserva.getHoraInicio());
        spinnerJugadores.getValueFactory().setValue(reserva.getCantidadJugadores());
        campoComentarios.setText(reserva.getComentarios());
        campoObservaciones.setText(reserva.getObservacionesAdministrativas());
        etiquetaPrecio.setText("ARS " + reserva.getPrecioTotal().toPlainString());
        try {
            etiquetaSaldoPendiente.setText("ARS " + pagoService.calcularSaldo(reserva.getId()).toPlainString());
        } catch (RuntimeException exception) { etiquetaSaldoPendiente.setText("No disponible"); }
        actualizarAccionesReserva();
        mostrarDetalleCancelacion(reserva);
        cargarAuditoria(reserva.getId());
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
        etiquetaPrecio.setText(cancha == null ? "ARS 0" : "ARS " + cancha.getPrecio().toPlainString());
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
            reserva.setClienteId(comboCliente.getValue().getId());
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
        tituloFormulario.setText(administrativa ? "Reprogramación administrativa" : "Reprogramar reserva");
        botonGuardar.setText("GUARDAR REPROGRAMACIÓN");
        comboCliente.setDisable(true); spinnerJugadores.setDisable(true);
        comboCancha.setDisable(false); selectorFecha.setDisable(false); comboHorario.setDisable(false);
        botonReprogramar.setVisible(false); botonReprogramar.setManaged(false);
        mostrarInfo(administrativa ? "Seleccioná el nuevo horario. El motivo será obligatorio."
                : "Seleccioná una nueva cancha, fecha y horario.");
    }

    private void ofrecerReprogramacionAdministrativa(String restriccion) {
        Alert aviso = new Alert(Alert.AlertType.WARNING);
        aviso.setTitle("Reprogramación fuera de plazo");
        aviso.setHeaderText("La reserva no puede reprogramarse normalmente.");
        configurarTextoLargo(aviso, restriccion + "\n\nSi el cambio corresponde a una situación atribuible al complejo, podés realizar una reprogramación administrativa.");
        ButtonType continuar = new ButtonType("Reprogramación administrativa");
        ButtonType volver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        aviso.getButtonTypes().setAll(continuar, volver);
        aviso.showAndWait().ifPresent(r -> { if (r == continuar) prepararModoReprogramacion(true); });
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
        comboCancha.setDisable(false); selectorFecha.setDisable(false); comboHorario.setDisable(false); actualizarAccionesReserva();
    }

    private void validarSeleccionFormulario() {
        if (comboCliente.getValue() == null) throw new IllegalArgumentException("Seleccioná un cliente.");
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
        if (reservaSeleccionada == null) return "ARS 0.00";
        return "ARS " + pagoService.totalAcreditado(reservaSeleccionada.getId()).toPlainString();
    }

    private void mostrarOpcionesCancelacionDentroDePlazo() {
        boolean tienePagos = tienePagosAcreditados();
        ButtonType reprogramar = new ButtonType("Reprogramar");
        ButtonType cancelar = new ButtonType(tienePagos ? "Cancelar y reembolsar" : "Cancelar reserva");
        ButtonType volver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION);
        dialogo.setTitle("Cancelar reserva"); dialogo.setHeaderText("La reserva está dentro del plazo permitido.");
        String pagos = tienePagos ? "\nPagos acreditados: " + totalAcreditadoSeleccionado()
                + "\n\nLa seña puede trasladarse a otro horario o reembolsarse." : "\nLa reserva no tiene pagos acreditados.";
        dialogo.setContentText(reservaSeleccionada.getNombreCliente() + "\n" + reservaSeleccionada.getNombreCancha()
                + "\n" + reservaSeleccionada.getFecha().format(FORMATO_FECHA) + " "
                + reservaSeleccionada.getHoraInicio().format(FORMATO_HORA) + pagos);
        dialogo.getButtonTypes().setAll(reprogramar, cancelar, volver);
        dialogo.showAndWait().ifPresent(r -> {
            if (r == reprogramar) iniciarReprogramacion();
            else if (r == cancelar) { if (tienePagos) confirmarCancelacionConReembolso(false); else confirmarCancelacionSinPagos(); }
        });
    }

    private void confirmarCancelacionSinPagos() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cancelar reserva"); alerta.setHeaderText("¿Confirmar la cancelación?");
        alerta.setContentText("La reserva no tiene pagos acreditados. El horario volverá a quedar disponible.");
        alerta.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) ejecutarCancelacionNormal(); });
    }

    private void mostrarOpcionesCancelacionFueraDePlazo(String restriccion) {
        ButtonType retener = new ButtonType(tienePagosAcreditados() ? "Cancelar y retener seña" : "Cancelar reserva");
        ButtonType administrativa = new ButtonType("Excepción administrativa");
        ButtonType volver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert dialogo = new Alert(Alert.AlertType.WARNING);
        dialogo.setTitle("Cancelación fuera de plazo"); dialogo.setHeaderText("La reserva está fuera del plazo permitido.");
        String pago = tienePagosAcreditados() ? "\n\nLa seña acreditada de " + totalAcreditadoSeleccionado() + " quedará retenida." : "";
        configurarTextoLargo(dialogo, restriccion + pago + "\n\nUna excepción administrativa solo debe utilizarse cuando la cancelación sea responsabilidad del complejo.");
        dialogo.getButtonTypes().setAll(retener, administrativa, volver);
        dialogo.showAndWait().ifPresent(r -> { if (r == retener) confirmarCancelacionConSeniaRetenida(true); else if (r == administrativa) mostrarOpcionesAdministrativas(); });
    }

    private void mostrarOpcionesAdministrativas() {
        boolean tienePagos = tienePagosAcreditados();
        ButtonType reprogramar = new ButtonType("Reprogramar");
        ButtonType cancelar = new ButtonType(tienePagos ? "Cancelar y reembolsar" : "Cancelar reserva");
        ButtonType volver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION);
        dialogo.setTitle("Excepción administrativa"); dialogo.setHeaderText("Seleccioná la solución para el cliente.");
        dialogo.setContentText("Esta operación debe utilizarse únicamente por lluvia, mantenimiento, corte de energía u otra situación atribuible al complejo.");
        dialogo.getButtonTypes().setAll(reprogramar, cancelar, volver);
        dialogo.showAndWait().ifPresent(r -> { if (r == reprogramar) prepararModoReprogramacion(true); else if (r == cancelar) solicitarMotivoAdministrativo(tienePagos); });
    }

    private void confirmarCancelacionConSeniaRetenida(boolean fueraDePlazo) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar cancelación");
        alerta.setHeaderText(tienePagosAcreditados() ? "¿Cancelar y retener la seña?" : "¿Cancelar la reserva?");
        String detalle = tienePagosAcreditados() ? "El importe acreditado de " + totalAcreditadoSeleccionado() + " no será reembolsado." : "La reserva quedará cancelada.";
        if (fueraDePlazo) detalle += "\n\nLa cancelación se registrará fuera del plazo permitido.";
        alerta.setContentText(detalle);
        alerta.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) ejecutarCancelacionNormal(); });
    }

    private void ejecutarCancelacionNormal() {
        try {
            LocalDate fecha = reservaSeleccionada.getFecha(); boolean teniaSenia = tienePagosAcreditados();
            reservaService.cancelarNormal(reservaSeleccionada.getId());
            recargarDespuesDeCancelar(fecha, teniaSenia ? "La reserva fue cancelada y la seña quedó retenida." : "La reserva fue cancelada correctamente.");
        } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
    }

    private void confirmarCancelacionConReembolso(boolean administrativa) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cancelar y reembolsar");
        alerta.setHeaderText("¿Cancelar la reserva y reembolsar " + totalAcreditadoSeleccionado() + "?");
        alerta.setContentText("Todos los pagos acreditados de esta reserva pasarán a estado Reembolsado.");
        alerta.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { if (administrativa) solicitarMotivoAdministrativo(true); else ejecutarCancelacionYReembolsoNormal(); }
        });
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
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cambiar estado"); alerta.setHeaderText("¿Cambiar la reserva a " + estado + "?");
        alerta.setContentText(reservaSeleccionada.getNombreCliente() + " - " + reservaSeleccionada.getNombreCancha());
        alerta.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) try {
                LocalDate fecha = reservaSeleccionada.getFecha(); reservaService.cambiarEstado(reservaSeleccionada.getId(), estado);
                cargarReservas(); nuevaReserva(); filtroFecha.setValue(fecha); aplicarFiltros(); mostrarInfo("El estado se actualizó correctamente.");
            } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
        });
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

