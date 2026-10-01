package vista.controlador;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.Cliente;
import negocio.EstadoReserva;
import negocio.OrigenReserva;
import negocio.Reserva;
import servicio.ClienteService;
import servicio.MensajeReservaService;
import servicio.PagoService;
import servicio.ReservaService;
import servicio.WhatsAppService;
import util.FormateadorMoneda;
import vista.Navegacion;

public class SolicitudesWebController {
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReservaService reservaService = new ReservaService();
    private final ClienteService clienteService = new ClienteService();
    private final PagoService pagoService = new PagoService();
    private final MensajeReservaService mensajeService =
            new MensajeReservaService();
    private final WhatsAppService whatsAppService = new WhatsAppService();

    private final ObservableList<Reserva> solicitudes =
            FXCollections.observableArrayList();
    private FilteredList<Reserva> solicitudesFiltradas;
    private Reserva seleccionada;
    private Timeline reloj;

    @FXML private TextField campoBuscar;
    @FXML private ComboBox<EstadoReserva> filtroEstado;
    @FXML private TableView<Reserva> tablaSolicitudes;
    @FXML private TableColumn<Reserva, Long> columnaId;
    @FXML private TableColumn<Reserva, String> columnaCliente;
    @FXML private TableColumn<Reserva, String> columnaTurno;
    @FXML private TableColumn<Reserva, String> columnaCancha;
    @FXML private TableColumn<Reserva, EstadoReserva> columnaEstado;
    @FXML private TableColumn<Reserva, LocalDateTime> columnaVencimiento;
    @FXML private TableColumn<Reserva, Reserva> columnaTiempo;

    @FXML private Label etiquetaTotal;
    @FXML private Label etiquetaVigentes;
    @FXML private Label etiquetaProximas;
    @FXML private Label etiquetaExpiradas;
    @FXML private Label etiquetaMensaje;
    @FXML private Label detalleTitulo;
    @FXML private Label detalleCliente;
    @FXML private Label detalleTelefono;
    @FXML private Label detalleTurno;
    @FXML private Label detalleCancha;
    @FXML private Label detalleEstado;
    @FXML private Label detalleOrigen;
    @FXML private Label detalleVencimiento;
    @FXML private Label detallePrecio;
    @FXML private Label detalleAcreditado;
    @FXML private Label detalleSaldo;
    @FXML private Button botonReserva;
    @FXML private Button botonPagos;
    @FXML private Button botonWhatsApp;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFiltros();
        cargarSolicitudes();
        iniciarReloj();
    }

    private void configurarTabla() {
        columnaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnaCliente.setCellValueFactory(
                new PropertyValueFactory<>("nombreCliente"));
        columnaCancha.setCellValueFactory(
                new PropertyValueFactory<>("nombreCancha"));
        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado"));
        columnaTurno.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleStringProperty(
                        datos.getValue().getFecha().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " " + datos.getValue().getHoraInicio().format(
                                DateTimeFormatter.ofPattern("HH:mm"))));
        columnaVencimiento.setCellValueFactory(
                new PropertyValueFactory<>("fechaVencimiento"));
        columnaVencimiento.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? "-" : fecha.format(FECHA_HORA));
            }
        });
        columnaTiempo.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleObjectProperty<>(datos.getValue()));
        columnaTiempo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Reserva reserva, boolean vacia) {
                super.updateItem(reserva, vacia);
                setText(vacia || reserva == null ? null : textoTiempo(reserva));
                getStyleClass().removeAll(
                        "web-time-ok", "web-time-warning", "web-time-expired");
                if (!vacia && reserva != null) {
                    getStyleClass().add(claseTiempo(reserva));
                }
            }
        });
        tablaSolicitudes.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) mostrarDetalle(actual);
                });
        tablaSolicitudes.setRowFactory(tabla -> {
            javafx.scene.control.TableRow<Reserva> fila =
                    new javafx.scene.control.TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    Navegacion.mostrarReservaDesdeAgenda(fila.getItem().getId());
                }
            });
            return fila;
        });
    }

    private void configurarFiltros() {
        filtroEstado.setItems(FXCollections.observableArrayList(
                EstadoReserva.PENDIENTE,
                EstadoReserva.CONFIRMADA,
                EstadoReserva.EXPIRADA,
                EstadoReserva.CANCELADA,
                EstadoReserva.COMPLETADA,
                EstadoReserva.AUSENTE));
        solicitudesFiltradas = new FilteredList<>(solicitudes, valor -> true);
        tablaSolicitudes.setItems(solicitudesFiltradas);
        campoBuscar.textProperty().addListener((obs, ant, act) -> aplicarFiltros());
        filtroEstado.valueProperty().addListener((obs, ant, act) -> aplicarFiltros());
    }

    @FXML
    private void cargarSolicitudes() {
        try {
            solicitudes.setAll(reservaService.listar().stream()
                    .filter(reserva -> reserva.getOrigen() == OrigenReserva.WEB)
                    .toList());
            aplicarFiltros();
            actualizarResumen();
            etiquetaMensaje.setText(
                    solicitudes.size() + " solicitud(es) web cargada(s).");
        } catch (RuntimeException exception) {
            etiquetaMensaje.setText(exception.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (solicitudesFiltradas == null) return;
        String texto = campoBuscar.getText() == null ? ""
                : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        EstadoReserva estado = filtroEstado.getValue();
        solicitudesFiltradas.setPredicate(reserva ->
                (estado == null || reserva.getEstado() == estado)
                && (texto.isBlank()
                || contiene(reserva.getNombreCliente(), texto)
                || contiene(reserva.getNombreCancha(), texto)
                || contiene(reserva.getEstado().toString(), texto)
                || String.valueOf(reserva.getId()).contains(texto)));
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    @FXML
    private void limpiarFiltros() {
        campoBuscar.clear();
        filtroEstado.getSelectionModel().clearSelection();
    }

    private void actualizarResumen() {
        LocalDateTime ahora = LocalDateTime.now();
        long vigentes = solicitudes.stream()
                .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE)
                .filter(r -> r.getFechaVencimiento() != null
                        && r.getFechaVencimiento().isAfter(ahora))
                .count();
        long proximas = solicitudes.stream()
                .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE)
                .filter(r -> r.getFechaVencimiento() != null)
                .filter(r -> r.getFechaVencimiento().isAfter(ahora))
                .filter(r -> !r.getFechaVencimiento()
                        .isAfter(ahora.plusMinutes(5)))
                .count();
        long expiradas = solicitudes.stream()
                .filter(r -> r.getEstado() == EstadoReserva.EXPIRADA)
                .count();
        etiquetaTotal.setText(String.valueOf(solicitudes.size()));
        etiquetaVigentes.setText(String.valueOf(vigentes));
        etiquetaProximas.setText(String.valueOf(proximas));
        etiquetaExpiradas.setText(String.valueOf(expiradas));
    }

    private void iniciarReloj() {
        reloj = new Timeline(new KeyFrame(
                javafx.util.Duration.seconds(1),
                evento -> {
                    tablaSolicitudes.refresh();
                    actualizarResumen();
                    if (seleccionada != null) actualizarVencimientoDetalle();
                }));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    private String textoTiempo(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.EXPIRADA) return "Expirada";
        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                || reserva.getFechaVencimiento() == null) return "Finalizada";
        Duration restante = Duration.between(
                LocalDateTime.now(), reserva.getFechaVencimiento());
        if (restante.isNegative() || restante.isZero()) return "Venciendo...";
        long minutos = restante.toMinutes();
        long segundos = restante.minusMinutes(minutos).getSeconds();
        return String.format("%02d:%02d", minutos, segundos);
    }

    private String claseTiempo(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.EXPIRADA) {
            return "web-time-expired";
        }
        if (reserva.getEstado() == EstadoReserva.PENDIENTE
                && reserva.getFechaVencimiento() != null
                && !reserva.getFechaVencimiento()
                        .isAfter(LocalDateTime.now().plusMinutes(5))) {
            return "web-time-warning";
        }
        return "web-time-ok";
    }

    private void mostrarDetalle(Reserva reserva) {
        seleccionada = reserva;
        Cliente cliente = clienteService.buscar(reserva.getClienteId());
        detalleTitulo.setText("Solicitud web #" + reserva.getId());
        detalleCliente.setText(reserva.getNombreCliente());
        detalleTelefono.setText(cliente == null
                || cliente.getTelefono() == null
                || cliente.getTelefono().isBlank()
                        ? "Sin teléfono registrado" : cliente.getTelefono());
        detalleTurno.setText(reserva.getFecha().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " · " + reserva.getHoraInicio().format(
                        DateTimeFormatter.ofPattern("HH:mm"))
                + " - " + reserva.getHoraFin().format(
                        DateTimeFormatter.ofPattern("HH:mm")));
        detalleCancha.setText(reserva.getNombreCancha());
        detalleEstado.setText(nombreEstado(reserva.getEstado()));
        detalleOrigen.setText(reserva.getOrigen().toString());
        BigDecimal acreditado = pagoService.totalAcreditado(reserva.getId());
        BigDecimal saldo = pagoService.calcularSaldo(reserva.getId());
        detallePrecio.setText(moneda(reserva.getPrecioTotal()));
        detalleAcreditado.setText(moneda(acreditado));
        detalleSaldo.setText(moneda(saldo));
        actualizarVencimientoDetalle();
        configurarBoton(botonReserva, true);
        configurarBoton(botonPagos,
                reserva.getEstado() == EstadoReserva.PENDIENTE
                || reserva.getEstado() == EstadoReserva.CONFIRMADA
                || reserva.getEstado() == EstadoReserva.COMPLETADA);
        configurarBoton(botonWhatsApp, cliente != null
                && cliente.getTelefono() != null
                && !cliente.getTelefono().isBlank());
    }

    private void actualizarVencimientoDetalle() {
        detalleVencimiento.setText(seleccionada == null
                ? "-" : textoTiempo(seleccionada));
    }

    private String nombreEstado(EstadoReserva estado) {
        return switch (estado) {
            case PENDIENTE -> "Esperando seña";
            case CONFIRMADA -> "Confirmada";
            case EXPIRADA -> "Expirada";
            case CANCELADA -> "Cancelada";
            case COMPLETADA -> "Completada";
            case AUSENTE -> "Ausente";
        };
    }

    private String moneda(BigDecimal valor) {
        return FormateadorMoneda.pesos(valor);
    }

    private void configurarBoton(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }

    @FXML private void abrirReserva() {
        if (seleccionada != null) {
            Navegacion.mostrarReservaDesdeAgenda(seleccionada.getId());
        }
    }

    @FXML private void abrirPagos() {
        if (seleccionada != null) {
            Navegacion.mostrarPagosDeReserva(seleccionada.getId());
        }
    }

    @FXML private void abrirWhatsApp() {
        if (seleccionada == null) return;
        try {
            Cliente cliente = clienteService.buscar(seleccionada.getClienteId());
            if (cliente == null || cliente.getTelefono() == null
                    || cliente.getTelefono().isBlank()) {
                throw new IllegalArgumentException(
                        "El cliente no tiene un teléfono registrado.");
            }
            BigDecimal saldo = pagoService.calcularSaldo(seleccionada.getId());
            String mensaje = switch (seleccionada.getEstado()) {
                case PENDIENTE -> mensajeService.crearSolicitudSenia(
                        cliente, seleccionada, saldo.max(BigDecimal.ZERO));
                case CONFIRMADA -> saldo.signum() > 0
                        ? mensajeService.crearAvisoSaldo(cliente, seleccionada, saldo)
                        : mensajeService.crearRecordatorio(cliente, seleccionada);
                case CANCELADA, EXPIRADA ->
                        mensajeService.crearAvisoCancelacion(cliente, seleccionada);
                case COMPLETADA, AUSENTE ->
                        mensajeService.crearRecordatorio(cliente, seleccionada);
            };
            whatsAppService.abrirConversacion(cliente.getTelefono(), mensaje);
        } catch (RuntimeException exception) {
            etiquetaMensaje.setText(exception.getMessage());
        }
    }

    @FXML private void volver() {
        if (reloj != null) reloj.stop();
        Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
    }
}
