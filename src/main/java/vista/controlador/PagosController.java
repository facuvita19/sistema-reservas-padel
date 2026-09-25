package vista.controlador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.EstadoPago;
import negocio.EstadoReserva;
import negocio.MetodoPago;
import negocio.Pago;
import negocio.Reserva;
import servicio.PagoService;
import servicio.ReservaService;
import vista.Navegacion;

public class PagosController {

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private final PagoService pagoService = new PagoService();
    private final ReservaService reservaService = new ReservaService();

    private final ObservableList<Pago> pagos =
            FXCollections.observableArrayList();
    private final ObservableList<Reserva> reservas =
            FXCollections.observableArrayList();
    private final Map<Long, Reserva> reservasPorId = new HashMap<>();

    private FilteredList<Pago> pagosFiltrados;
    private Pago pagoSeleccionado;

    @FXML private TextField campoBuscar;
    @FXML private ComboBox<EstadoPago> filtroEstado;
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, Long> columnaReserva;
    @FXML private TableColumn<Pago, BigDecimal> columnaImporte;
    @FXML private TableColumn<Pago, MetodoPago> columnaMetodo;
    @FXML private TableColumn<Pago, EstadoPago> columnaEstado;
    @FXML private TableColumn<Pago, LocalDateTime> columnaFecha;
    @FXML private TableColumn<Pago, String> columnaReferencia;

    @FXML private ComboBox<Reserva> comboReserva;
    @FXML private TextField campoImporte;
    @FXML private ComboBox<MetodoPago> comboMetodo;
    @FXML private ComboBox<EstadoPago> comboEstadoInicial;
    @FXML private TextField campoReferencia;
    @FXML private Label etiquetaPrecioTotal;
    @FXML private Label etiquetaTotalAcreditado;
    @FXML private Label etiquetaSaldo;
    @FXML private Label etiquetaReservaDetalle;
    @FXML private Label etiquetaMensaje;
    @FXML private Label tituloFormulario;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarCombos();
        configurarFiltros();
        cargarReservas();
        cargarPagos();
        nuevoPago();
    }

    private void configurarTabla() {
        columnaReserva.setCellValueFactory(
                new PropertyValueFactory<>("reservaId")
        );
        columnaReserva.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(Long reservaId, boolean vacia) {
                super.updateItem(reservaId, vacia);
                if (vacia || reservaId == null) {
                    setText(null);
                    return;
                }
                Reserva reserva = reservasPorId.get(reservaId);
                setText(reserva == null
                        ? "Reserva #" + reservaId
                        : reservaBreve(reserva));
            }
        });

        columnaImporte.setCellValueFactory(
                new PropertyValueFactory<>("importe")
        );
        columnaImporte.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal importe, boolean vacia) {
                super.updateItem(importe, vacia);
                setText(vacia || importe == null
                        ? null
                        : "ARS " + importe.toPlainString());
            }
        });

        columnaMetodo.setCellValueFactory(
                new PropertyValueFactory<>("metodoPago")
        );
        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );
        columnaFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaCreacion")
        );
        columnaFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null
                        ? null
                        : fecha.format(FORMATO_FECHA_HORA));
            }
        });
        columnaReferencia.setCellValueFactory(
                new PropertyValueFactory<>("referencia")
        );

        tablaPagos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) {
                        mostrarDetallePago(actual);
                    }
                });
    }

    private void configurarCombos() {
        comboMetodo.setItems(FXCollections.observableArrayList(
                MetodoPago.values()
        ));
        comboEstadoInicial.setItems(FXCollections.observableArrayList(
                EstadoPago.PENDIENTE,
                EstadoPago.ACREDITADO
        ));

        comboReserva.setCellFactory(lista -> crearCeldaReserva());
        comboReserva.setButtonCell(crearCeldaReserva());
        comboReserva.valueProperty().addListener(
                (obs, anterior, actual) -> actualizarResumen(actual)
        );
    }

    private ListCell<Reserva> crearCeldaReserva() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Reserva reserva, boolean vacia) {
                super.updateItem(reserva, vacia);
                setText(vacia || reserva == null
                        ? null
                        : reservaExtendida(reserva));
            }
        };
    }

    private void configurarFiltros() {
        filtroEstado.setItems(FXCollections.observableArrayList(
                EstadoPago.values()
        ));
        pagosFiltrados = new FilteredList<>(pagos, pago -> true);
        tablaPagos.setItems(pagosFiltrados);

        campoBuscar.textProperty().addListener(
                (obs, anterior, actual) -> aplicarFiltros()
        );
        filtroEstado.valueProperty().addListener(
                (obs, anterior, actual) -> aplicarFiltros()
        );
    }

    @FXML
    private void cargarReservas() {
        try {
            List<Reserva> resultado = reservaService.listar().stream()
                    .filter(reserva ->
                            reserva.getEstado() != EstadoReserva.CANCELADA)
                    .toList();

            reservas.setAll(resultado);
            reservasPorId.clear();
            resultado.forEach(reserva ->
                    reservasPorId.put(reserva.getId(), reserva));
            comboReserva.setItems(reservas);
            tablaPagos.refresh();
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    @FXML
    private void cargarPagos() {
        try {
            pagos.setAll(pagoService.listar());
            aplicarFiltros();
            mostrarInfo(pagos.size() + " movimiento(s) cargado(s).");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (pagosFiltrados == null) {
            return;
        }

        String texto = campoBuscar.getText() == null
                ? ""
                : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        EstadoPago estado = filtroEstado.getValue();

        pagosFiltrados.setPredicate(pago -> {
            Reserva reserva = reservasPorId.get(pago.getReservaId());
            boolean coincideTexto = texto.isBlank()
                    || contiene(pago.getReferencia(), texto)
                    || contiene(pago.getMetodoPago().toString(), texto)
                    || contiene(pago.getEstado().toString(), texto)
                    || contiene(reserva == null
                            ? "Reserva " + pago.getReservaId()
                            : reservaExtendida(reserva), texto);
            boolean coincideEstado = estado == null
                    || pago.getEstado() == estado;
            return coincideTexto && coincideEstado;
        });
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null
                && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    @FXML
    private void limpiarFiltros() {
        campoBuscar.clear();
        filtroEstado.getSelectionModel().clearSelection();
    }

    @FXML
    private void nuevoPago() {
        pagoSeleccionado = null;
        tablaPagos.getSelectionModel().clearSelection();
        tituloFormulario.setText("Nuevo movimiento");
        comboReserva.getSelectionModel().clearSelection();
        campoImporte.clear();
        comboMetodo.getSelectionModel().select(MetodoPago.TRANSFERENCIA);
        comboEstadoInicial.getSelectionModel().select(EstadoPago.PENDIENTE);
        campoReferencia.clear();
        etiquetaMensaje.setText("");
        limpiarResumen();
    }

    private void mostrarDetallePago(Pago pago) {
        pagoSeleccionado = pago;
        tituloFormulario.setText("Detalle del movimiento");
        comboReserva.setValue(reservasPorId.get(pago.getReservaId()));
        campoImporte.setText(pago.getImporte().toPlainString());
        comboMetodo.setValue(pago.getMetodoPago());
        comboEstadoInicial.setValue(pago.getEstado());
        campoReferencia.setText(pago.getReferencia());
        actualizarResumen(comboReserva.getValue());
    }

    private void actualizarResumen(Reserva reserva) {
        if (reserva == null) {
            limpiarResumen();
            return;
        }

        try {
            BigDecimal saldo = pagoService.calcularSaldo(reserva.getId());
            BigDecimal acreditado = reserva.getPrecioTotal().subtract(saldo);

            etiquetaPrecioTotal.setText(
                    "ARS " + reserva.getPrecioTotal().toPlainString()
            );
            etiquetaTotalAcreditado.setText(
                    "ARS " + acreditado.toPlainString()
            );
            etiquetaSaldo.setText("ARS " + saldo.toPlainString());
            etiquetaReservaDetalle.setText(reservaExtendida(reserva));

            if (pagoSeleccionado == null) {
                campoImporte.setText(saldo.toPlainString());
            }
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void limpiarResumen() {
        etiquetaPrecioTotal.setText("ARS 0");
        etiquetaTotalAcreditado.setText("ARS 0");
        etiquetaSaldo.setText("ARS 0");
        etiquetaReservaDetalle.setText("Seleccioná una reserva para ver el saldo.");
    }

    @FXML
    private void guardar() {
        try {
            validarFormulario();

            Pago pago = pagoSeleccionado == null
                    ? new Pago()
                    : pagoSeleccionado;

            pago.setReservaId(comboReserva.getValue().getId());
            pago.setImporte(parsearImporte(campoImporte.getText()));
            pago.setMetodoPago(comboMetodo.getValue());
            pago.setEstado(comboEstadoInicial.getValue());
            pago.setReferencia(campoReferencia.getText());

            pagoService.guardar(pago);
            cargarPagos();
            actualizarResumen(comboReserva.getValue());
            nuevoPago();
            mostrarInfo("El movimiento se guardó correctamente.");

        } catch (IllegalArgumentException exception) {
            mostrarError(exception.getMessage());
        } catch (RuntimeException exception) {
            mostrarError("No se pudo guardar el movimiento: "
                    + exception.getMessage());
        }
    }

    private void validarFormulario() {
        if (comboReserva.getValue() == null) {
            throw new IllegalArgumentException("Seleccioná una reserva.");
        }
        if (comboMetodo.getValue() == null) {
            throw new IllegalArgumentException("Seleccioná el método de pago.");
        }
        if (comboEstadoInicial.getValue() == null) {
            throw new IllegalArgumentException("Seleccioná el estado inicial.");
        }
    }

    private BigDecimal parsearImporte(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Ingresá el importe.");
        }

        try {
            return new BigDecimal(
                    valor.trim()
                            .replace("ARS", "")
                            .replace("$", "")
                            .replace(" ", "")
                            .replace(",", ".")
            );
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "El importe debe ser un número válido."
            );
        }
    }

    @FXML
    private void acreditar() {
        ejecutarCambioEstado("Acreditar pago", () ->
                pagoService.acreditar(pagoSeleccionado.getId()));
    }

    @FXML
    private void anular() {
        ejecutarCambioEstado("Anular pago", () ->
                pagoService.anular(pagoSeleccionado.getId()));
    }

    @FXML
    private void reembolsar() {
        ejecutarCambioEstado("Reembolsar pago", () ->
                pagoService.reembolsar(pagoSeleccionado.getId()));
    }

    private void ejecutarCambioEstado(String titulo, Runnable accion) {
        if (pagoSeleccionado == null) {
            mostrarError("Seleccioná un movimiento de la tabla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(titulo);
        confirmacion.setHeaderText(titulo + " seleccionado");
        confirmacion.setContentText(
                "Importe: ARS " + pagoSeleccionado.getImporte().toPlainString()
        );

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.OK) {
                try {
                    long reservaId = pagoSeleccionado.getReservaId();
                    accion.run();
                    cargarPagos();
                    Reserva reserva = reservasPorId.get(reservaId);
                    nuevoPago();
                    if (reserva != null) {
                        comboReserva.setValue(reserva);
                        actualizarResumen(reserva);
                    }
                    mostrarInfo("El estado se actualizó correctamente.");
                } catch (RuntimeException exception) {
                    mostrarError(exception.getMessage());
                }
            }
        });
    }

    private String reservaBreve(Reserva reserva) {
        return "#" + reserva.getId()
                + " · " + reserva.getNombreCliente();
    }

    private String reservaExtendida(Reserva reserva) {
        return "#" + reserva.getId()
                + " · " + reserva.getNombreCliente()
                + " · " + reserva.getNombreCancha()
                + " · " + reserva.getFecha().format(FORMATO_FECHA)
                + " " + reserva.getHoraInicio().format(FORMATO_HORA);
    }

    @FXML
    private void volver() {
        Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
    }

    private void mostrarError(String mensaje) {
        etiquetaMensaje.setText(mensaje == null ? "Ocurrió un error." : mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-exito");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) {
            etiquetaMensaje.getStyleClass().add("mensaje-error");
        }
    }

    private void mostrarInfo(String mensaje) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-error");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-exito")) {
            etiquetaMensaje.getStyleClass().add("mensaje-exito");
        }
    }
}
