package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import negocio.CierreCaja;
import negocio.DetalleMedioPago;
import negocio.ResumenCajaDiaria;
import negocio.Usuario;
import servicio.CierreCajaService;
import vista.Navegacion;

public class CierreCajaController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CierreCajaService cierreService =
            new CierreCajaService();

    private ResumenCajaDiaria resumenActual;
    private CierreCaja cierreExistente;

    @FXML private DatePicker selectorFecha;
    @FXML private Label etiquetaEstadoCaja;
    @FXML private Label etiquetaTotalAcreditado;
    @FXML private Label etiquetaEfectivoCalculado;
    @FXML private Label etiquetaTotalReembolsado;
    @FXML private Label etiquetaPagosAcreditados;
    @FXML private Label etiquetaPagosPendientes;
    @FXML private Label etiquetaReservasCompletadas;
    @FXML private Label etiquetaReservasAusentes;
    @FXML private Label etiquetaReservasCanceladas;
    @FXML private Label etiquetaDiferencia;
    @FXML private Label etiquetaMensaje;
    @FXML private Label etiquetaDatosCierre;

    @FXML private TableView<DetalleMedioPago> tablaMetodos;
    @FXML private TableColumn<DetalleMedioPago, Object> columnaMetodo;
    @FXML private TableColumn<DetalleMedioPago, Integer> columnaMovimientos;
    @FXML private TableColumn<DetalleMedioPago, BigDecimal> columnaTotal;

    @FXML private TextField campoEfectivoDeclarado;
    @FXML private TextArea campoObservaciones;
    @FXML private Button botonCerrarCaja;
    @FXML private VBox contenedorCierreExistente;
    @FXML private VBox contenedorFormularioCierre;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFecha();
        configurarCalculoDiferencia();
        cargarCaja();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void configurarTabla() {
        columnaMetodo.setCellValueFactory(
                new PropertyValueFactory("metodoPago"));
        columnaMovimientos.setCellValueFactory(
                new PropertyValueFactory<>("cantidadMovimientos"));
        columnaTotal.setCellValueFactory(
                new PropertyValueFactory<>("total"));
        columnaTotal.setCellFactory(columna -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(BigDecimal total, boolean vacia) {
                super.updateItem(total, vacia);
                setText(vacia || total == null ? null : formatearMoneda(total));
            }
        });
    }

    private void configurarFecha() {
        Usuario usuario = Navegacion.getUsuarioActual();
        boolean administrador = usuario != null && usuario.esAdministrador();

        selectorFecha.setValue(LocalDate.now());
        selectorFecha.setDisable(!administrador);
        selectorFecha.setDayCellFactory(control -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isAfter(LocalDate.now()));
            }
        });
        selectorFecha.valueProperty().addListener(
                (obs, anterior, actual) -> {
                    if (actual != null) {
                        cargarCaja();
                    }
                });
    }

    private void configurarCalculoDiferencia() {
        campoEfectivoDeclarado.textProperty().addListener(
                (obs, anterior, actual) -> actualizarDiferencia());
    }

    @FXML
    private void cargarCaja() {
        LocalDate fecha = selectorFecha.getValue();
        if (fecha == null) {
            mostrarError("Seleccioná una fecha.");
            return;
        }

        try {
            cierreExistente = cierreService.buscarPorFecha(fecha);
            resumenActual = cierreService.obtenerResumen(fecha);
            mostrarResumen(resumenActual);

            if (cierreExistente == null) {
                mostrarCajaAbierta();
            } else {
                mostrarCajaCerrada(cierreExistente);
            }
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void mostrarResumen(ResumenCajaDiaria resumen) {
        etiquetaTotalAcreditado.setText(
                formatearMoneda(resumen.getTotalAcreditado()));
        etiquetaEfectivoCalculado.setText(
                formatearMoneda(resumen.getTotalEfectivo()));
        etiquetaTotalReembolsado.setText(
                formatearMoneda(resumen.getTotalReembolsado()));
        etiquetaPagosAcreditados.setText(
                String.valueOf(resumen.getCantidadPagosAcreditados()));
        etiquetaPagosPendientes.setText(
                String.valueOf(resumen.getPagosPendientes()));
        etiquetaReservasCompletadas.setText(
                String.valueOf(resumen.getReservasCompletadas()));
        etiquetaReservasAusentes.setText(
                String.valueOf(resumen.getReservasAusentes()));
        etiquetaReservasCanceladas.setText(
                String.valueOf(resumen.getReservasCanceladas()));
        tablaMetodos.setItems(FXCollections.observableArrayList(
                resumen.getDetalles()));
    }

    private void mostrarCajaAbierta() {
        etiquetaEstadoCaja.setText("CAJA ABIERTA");
        etiquetaEstadoCaja.getStyleClass().removeAll(
                "cash-status-open", "cash-status-closed");
        etiquetaEstadoCaja.getStyleClass().add("cash-status-open");

        contenedorFormularioCierre.setVisible(true);
        contenedorFormularioCierre.setManaged(true);
        contenedorCierreExistente.setVisible(false);
        contenedorCierreExistente.setManaged(false);
        campoEfectivoDeclarado.setDisable(false);
        campoObservaciones.setDisable(false);
        botonCerrarCaja.setDisable(false);

        campoEfectivoDeclarado.setText(
                resumenActual.getTotalEfectivo()
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString());
        campoObservaciones.clear();
        etiquetaDatosCierre.setText("");
        actualizarDiferencia();
        limpiarMensaje();
    }

    private void mostrarCajaCerrada(CierreCaja cierre) {
        etiquetaEstadoCaja.setText("CAJA CERRADA");
        etiquetaEstadoCaja.getStyleClass().removeAll(
                "cash-status-open", "cash-status-closed");
        etiquetaEstadoCaja.getStyleClass().add("cash-status-closed");

        contenedorFormularioCierre.setVisible(false);
        contenedorFormularioCierre.setManaged(false);
        contenedorCierreExistente.setVisible(true);
        contenedorCierreExistente.setManaged(true);

        String fechaCierre = cierre.getFechaCierre() == null
                ? "Sin fecha registrada"
                : cierre.getFechaCierre().format(FORMATO_FECHA_HORA);

        etiquetaDatosCierre.setText(
                "Fecha: " + cierre.getFecha().format(FORMATO_FECHA)
                        + "\nCerrada: " + fechaCierre
                        + "\nUsuario ID: " + cierre.getUsuarioCierreId()
                        + "\nEfectivo calculado: "
                        + formatearMoneda(cierre.getTotalEfectivoCalculado())
                        + "\nEfectivo declarado: "
                        + formatearMoneda(cierre.getEfectivoDeclarado())
                        + "\nDiferencia: "
                        + formatearMonedaConSigno(cierre.getDiferenciaEfectivo())
                        + (cierre.getObservaciones() == null
                                ? ""
                                : "\nObservaciones: " + cierre.getObservaciones()));

        etiquetaDiferencia.setText(
                formatearMonedaConSigno(cierre.getDiferenciaEfectivo()));
        actualizarEstiloDiferencia(cierre.getDiferenciaEfectivo());
        mostrarInfo("La caja de esta fecha ya fue cerrada.");
    }

    private void actualizarDiferencia() {
        if (resumenActual == null) {
            etiquetaDiferencia.setText(formatearMoneda(BigDecimal.ZERO));
            return;
        }

        try {
            BigDecimal declarado = leerImporte(campoEfectivoDeclarado.getText());
            BigDecimal diferencia = declarado.subtract(
                    resumenActual.getTotalEfectivo());
            etiquetaDiferencia.setText(formatearMonedaConSigno(diferencia));
            actualizarEstiloDiferencia(diferencia);
        } catch (IllegalArgumentException exception) {
            etiquetaDiferencia.setText("Importe inválido");
            actualizarEstiloDiferencia(BigDecimal.ONE.negate());
        }
    }

    private void actualizarEstiloDiferencia(BigDecimal diferencia) {
        etiquetaDiferencia.getStyleClass().removeAll(
                "cash-difference-ok", "cash-difference-warning");
        etiquetaDiferencia.getStyleClass().add(
                diferencia != null && diferencia.compareTo(BigDecimal.ZERO) == 0
                        ? "cash-difference-ok"
                        : "cash-difference-warning");
    }

    @FXML
    private void cerrarCaja() {
        if (cierreExistente != null) {
            mostrarError("La caja de esta fecha ya fue cerrada.");
            return;
        }

        Usuario usuario = Navegacion.getUsuarioActual();
        if (usuario == null) {
            mostrarError("La sesión administrativa finalizó.");
            return;
        }

        try {
            BigDecimal declarado = leerImporte(
                    campoEfectivoDeclarado.getText());
            BigDecimal diferencia = declarado.subtract(
                    resumenActual.getTotalEfectivo());

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar cierre de caja");
            confirmacion.setHeaderText(
                    "¿Cerrar definitivamente la caja del "
                            + selectorFecha.getValue().format(FORMATO_FECHA)
                            + "?");
            confirmacion.setContentText(
                    "Efectivo calculado: "
                            + formatearMoneda(resumenActual.getTotalEfectivo())
                            + "\nEfectivo declarado: "
                            + formatearMoneda(declarado)
                            + "\nDiferencia: "
                            + formatearMonedaConSigno(diferencia)
                            + "\n\nEl cierre no podrá modificarse desde esta pantalla.");

            confirmacion.showAndWait().ifPresent(respuesta -> {
                if (respuesta == ButtonType.OK) {
                    ejecutarCierre(declarado);
                }
            });
        } catch (IllegalArgumentException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void ejecutarCierre(BigDecimal declarado) {
        try {
            Usuario usuario = Navegacion.getUsuarioActual();
            cierreService.cerrar(
                    selectorFecha.getValue(),
                    declarado,
                    campoObservaciones.getText(),
                    usuario.getId());
            cargarCaja();
            mostrarInfo("La caja se cerró correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private BigDecimal leerImporte(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "Ingresá el efectivo contado.");
        }

        String normalizado = texto.trim()
                .replace("$", "")
                .replace("ARS", "")
                .replace(" ", "");

        if (normalizado.contains(",") && normalizado.contains(".")) {
            normalizado = normalizado.replace(".", "").replace(",", ".");
        } else if (normalizado.contains(",")) {
            normalizado = normalizado.replace(",", ".");
        }

        try {
            BigDecimal valor = new BigDecimal(normalizado)
                    .setScale(2, RoundingMode.HALF_UP);
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "El efectivo contado no puede ser negativo.");
            }
            return valor;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "El efectivo contado no tiene un formato válido.");
        }
    }

    private String formatearMoneda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(
                new Locale("es", "AR"));
        return formato.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private String formatearMonedaConSigno(BigDecimal valor) {
        BigDecimal seguro = valor == null ? BigDecimal.ZERO : valor;
        return (seguro.compareTo(BigDecimal.ZERO) > 0 ? "+" : "")
                + formatearMoneda(seguro);
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

    private void limpiarMensaje() {
        etiquetaMensaje.setText("");
        etiquetaMensaje.getStyleClass().removeAll(
                "mensaje-error", "mensaje-exito");
    }
}
