package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import negocio.CierreCaja;
import negocio.DetalleMedioPago;
import negocio.DetallePagoCaja;
import negocio.MetodoPago;
import negocio.MovimientoCaja;
import negocio.ResumenCajaDiaria;
import negocio.TipoMovimientoCaja;
import negocio.Usuario;
import servicio.CierreCajaService;
import servicio.DetallePagoCajaService;
import servicio.MovimientoCajaService;
import util.FormateadorMoneda;
import vista.Dialogos;
import vista.Navegacion;

public class CierreCajaController {
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CierreCajaService cierreService = new CierreCajaService();
    private final MovimientoCajaService movimientoService =
            new MovimientoCajaService();
    private final DetallePagoCajaService detallePagoService =
            new DetallePagoCajaService();

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
    @FXML private Label etiquetaIngresosManuales;
    @FXML private Label etiquetaEgresosManuales;
    @FXML private Label etiquetaDiferencia;
    @FXML private Label etiquetaMensaje;
    @FXML private Label etiquetaDatosCierre;
    @FXML private Label etiquetaAdvertenciaPendientes;

    @FXML private TableView<DetalleMedioPago> tablaMetodos;
    @FXML private TableColumn<DetalleMedioPago, Object> columnaMetodo;
    @FXML private TableColumn<DetalleMedioPago, Integer> columnaMovimientos;
    @FXML private TableColumn<DetalleMedioPago, BigDecimal> columnaTotal;

    @FXML private TableView<DetallePagoCaja> tablaPagosAcreditados;
    @FXML private TableColumn<DetallePagoCaja, LocalDateTime> columnaPagoAcreditado;
    @FXML private TableColumn<DetallePagoCaja, String> columnaPagoCliente;
    @FXML private TableColumn<DetallePagoCaja, Long> columnaPagoReserva;
    @FXML private TableColumn<DetallePagoCaja, String> columnaPagoTurno;
    @FXML private TableColumn<DetallePagoCaja, String> columnaPagoCancha;
    @FXML private TableColumn<DetallePagoCaja, MetodoPago> columnaPagoMetodo;
    @FXML private TableColumn<DetallePagoCaja, BigDecimal> columnaPagoImporte;
    @FXML private TableColumn<DetallePagoCaja, String> columnaPagoUsuario;
    @FXML private TableView<MovimientoCaja> tablaMovimientos;
    @FXML private TableColumn<MovimientoCaja, LocalDateTime> columnaMovimientoFecha;
    @FXML private TableColumn<MovimientoCaja, TipoMovimientoCaja> columnaMovimientoTipo;
    @FXML private TableColumn<MovimientoCaja, String> columnaMovimientoConcepto;
    @FXML private TableColumn<MovimientoCaja, MetodoPago> columnaMovimientoMedio;
    @FXML private TableColumn<MovimientoCaja, BigDecimal> columnaMovimientoImporte;
    @FXML private TableColumn<MovimientoCaja, String> columnaMovimientoUsuario;

    @FXML private ComboBox<TipoMovimientoCaja> comboTipoMovimiento;
    @FXML private ComboBox<MetodoPago> comboMedioMovimiento;
    @FXML private TextField campoConceptoMovimiento;
    @FXML private TextField campoImporteMovimiento;
    @FXML private TextArea campoObservacionesMovimiento;
    @FXML private Button botonRegistrarMovimiento;

    @FXML private TextField campoEfectivoDeclarado;
    @FXML private TextArea campoObservaciones;
    @FXML private Button botonCerrarCaja;
    @FXML private VBox contenedorCierreExistente;
    @FXML private VBox contenedorFormularioCierre;
    @FXML private VBox contenedorNuevoMovimiento;

    @FXML
    private void initialize() {
        configurarTablaMetodos();
        configurarTablaPagos();
        configurarTablaMovimientos();
        configurarFormularioMovimiento();
        configurarFecha();
        campoEfectivoDeclarado.textProperty().addListener(
                (obs, anterior, actual) -> actualizarDiferencia());
        cargarCaja();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void configurarTablaMetodos() {
        columnaMetodo.setCellValueFactory(new PropertyValueFactory("metodoPago"));
        columnaMovimientos.setCellValueFactory(
                new PropertyValueFactory<>("cantidadMovimientos"));
        columnaTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        columnaTotal.setCellFactory(columna -> celdaMoneda());
    }

    private void configurarTablaPagos() {
        columnaPagoAcreditado.setCellValueFactory(
                new PropertyValueFactory<>("fechaAcreditacion"));
        columnaPagoCliente.setCellValueFactory(
                new PropertyValueFactory<>("nombreCliente"));
        columnaPagoReserva.setCellValueFactory(
                new PropertyValueFactory<>("reservaId"));
        columnaPagoCancha.setCellValueFactory(
                new PropertyValueFactory<>("nombreCancha"));
        columnaPagoMetodo.setCellValueFactory(
                new PropertyValueFactory<>("metodoPago"));
        columnaPagoImporte.setCellValueFactory(
                new PropertyValueFactory<>("importe"));
        columnaPagoUsuario.setCellValueFactory(
                new PropertyValueFactory<>("nombreUsuario"));
        columnaPagoTurno.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleStringProperty(
                        datos.getValue().getFechaTurno().format(FORMATO_FECHA)
                        + " " + datos.getValue().getHoraTurno().format(
                                DateTimeFormatter.ofPattern("HH:mm"))));

        columnaPagoAcreditado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null
                        ? null : fecha.format(FORMATO_FECHA_HORA));
            }
        });
        columnaPagoImporte.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal importe, boolean vacia) {
                super.updateItem(importe, vacia);
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                getStyleClass().add("cash-money-cell");
                setStyle("-fx-alignment: CENTER-RIGHT;"
                        + "-fx-padding: 0 14 0 4;");
                setText(vacia || importe == null
                        ? null : formatearMoneda(importe));
            }
        });
        tablaPagosAcreditados.setRowFactory(tabla -> {
            javafx.scene.control.TableRow<DetallePagoCaja> fila =
                    new javafx.scene.control.TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    Navegacion.mostrarPagosDeReserva(
                            fila.getItem().getReservaId());
                }
            });
            return fila;
        });
    }
    private void configurarTablaMovimientos() {
        columnaMovimientoFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaCreacion"));
        columnaMovimientoTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipo"));
        columnaMovimientoTipo.setCellFactory(columna -> new TableCell<>() {
            private final Label insignia = new Label();
            { insignia.getStyleClass().add("cash-movement-badge"); }
            @Override protected void updateItem(
                    TipoMovimientoCaja tipo, boolean vacia) {
                super.updateItem(tipo, vacia);
                if (vacia || tipo == null) { setGraphic(null); return; }
                insignia.setText(tipo.toString().toUpperCase());
                insignia.getStyleClass().removeAll(
                        "cash-movement-income", "cash-movement-expense");
                insignia.getStyleClass().add(tipo == TipoMovimientoCaja.INGRESO
                        ? "cash-movement-income" : "cash-movement-expense");
                setGraphic(insignia);
            }
        });
        columnaMovimientoConcepto.setCellValueFactory(
                new PropertyValueFactory<>("concepto"));
        columnaMovimientoMedio.setCellValueFactory(
                new PropertyValueFactory<>("medioPago"));
        columnaMovimientoImporte.setCellValueFactory(
                new PropertyValueFactory<>("importe"));
        columnaMovimientoUsuario.setCellValueFactory(
                new PropertyValueFactory<>("nombreUsuario"));
        columnaMovimientoFecha.setCellFactory(columna -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : fecha.format(FORMATO_FECHA_HORA));
            }
        });
        columnaMovimientoImporte.setCellFactory(columna -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal importe, boolean vacia) {
                super.updateItem(importe, vacia);
                MovimientoCaja movimiento = vacia || getTableRow() == null
                        ? null : getTableRow().getItem();
                if (vacia || importe == null || movimiento == null) {
                    setText(null);
                    getStyleClass().removeAll("cash-income", "cash-expense");
                    return;
                }
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                if (!getStyleClass().contains("cash-money-cell")) {
                    getStyleClass().add("cash-money-cell");
                }
                setStyle("-fx-alignment: CENTER-RIGHT;"
                        + "-fx-padding: 0 14 0 4;");
                setText((movimiento.getTipo() == TipoMovimientoCaja.INGRESO
                        ? "+" : "-") + formatearMoneda(importe));
                getStyleClass().removeAll("cash-income", "cash-expense");
                getStyleClass().add(movimiento.getTipo() == TipoMovimientoCaja.INGRESO
                        ? "cash-income" : "cash-expense");
            }
        });
    }

    private TableCell<DetalleMedioPago, BigDecimal> celdaMoneda() {
        return new TableCell<>() {
            @Override protected void updateItem(BigDecimal total, boolean vacia) {
                super.updateItem(total, vacia);
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                getStyleClass().add("cash-money-cell");
                setStyle("-fx-alignment: CENTER-RIGHT;"
                        + "-fx-padding: 0 14 0 4;");
                setText(vacia || total == null ? null : formatearMoneda(total));
            }
        };
    }

    private void configurarFormularioMovimiento() {
        comboTipoMovimiento.setItems(FXCollections.observableArrayList(
                TipoMovimientoCaja.values()));
        comboMedioMovimiento.setItems(FXCollections.observableArrayList(
                MetodoPago.values()));
        comboTipoMovimiento.setValue(TipoMovimientoCaja.EGRESO);
        comboMedioMovimiento.setValue(MetodoPago.EFECTIVO);
    }

    private void configurarFecha() {
        Usuario usuario = Navegacion.getUsuarioActual();
        selectorFecha.setValue(LocalDate.now());
        selectorFecha.setDisable(usuario == null || !usuario.esAdministrador());
        selectorFecha.setDayCellFactory(control -> new javafx.scene.control.DateCell() {
            @Override public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha == null || fecha.isAfter(LocalDate.now()));
            }
        });
        selectorFecha.valueProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) cargarCaja();
        });
    }

    @FXML
    private void cargarCaja() {
        LocalDate fecha = selectorFecha.getValue();
        if (fecha == null) { mostrarError("Seleccioná una fecha."); return; }
        try {
            cierreExistente = cierreService.buscarPorFecha(fecha);
            var movimientos = movimientoService.listarPorFecha(fecha);
            resumenActual = cierreService.obtenerResumen(fecha, movimientos);
            tablaPagosAcreditados.setItems(FXCollections.observableArrayList(
                    detallePagoService.listarAcreditadosPorFecha(fecha)));
            tablaMovimientos.setItems(FXCollections.observableArrayList(
                    movimientos));
            mostrarResumen(resumenActual);
            if (cierreExistente == null) mostrarCajaAbierta();
            else mostrarCajaCerrada(cierreExistente);
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void mostrarResumen(ResumenCajaDiaria resumen) {
        etiquetaTotalAcreditado.setText(formatearMoneda(
                resumen.getTotalAcreditado()
                        .add(resumen.getIngresosManuales())
                        .subtract(resumen.getEgresosManuales())));
        etiquetaEfectivoCalculado.setText(
                formatearMoneda(resumen.getEfectivoEsperado()));
        etiquetaTotalReembolsado.setText(
                formatearMoneda(resumen.getTotalReembolsado()));
        etiquetaIngresosManuales.setText(
                formatearMoneda(resumen.getIngresosManuales()));
        etiquetaEgresosManuales.setText(
                formatearMoneda(resumen.getEgresosManuales()));
        etiquetaPagosAcreditados.setText(
                String.valueOf(resumen.getCantidadPagosAcreditados()));
        etiquetaPagosPendientes.setText(String.valueOf(resumen.getPagosPendientes()));
        etiquetaReservasCompletadas.setText(String.valueOf(resumen.getReservasCompletadas()));
        etiquetaReservasAusentes.setText(String.valueOf(resumen.getReservasAusentes()));
        etiquetaReservasCanceladas.setText(String.valueOf(resumen.getReservasCanceladas()));
        tablaMetodos.setItems(FXCollections.observableArrayList(resumen.getDetalles()));
        boolean pendientes = resumen.getPagosPendientes() > 0;
        etiquetaAdvertenciaPendientes.setVisible(pendientes);
        etiquetaAdvertenciaPendientes.setManaged(pendientes);
        etiquetaAdvertenciaPendientes.setText(pendientes
                ? "Atención: hay " + resumen.getPagosPendientes()
                        + " pago(s) pendientes de acreditar. Revisalos antes del cierre."
                : "");
    }

    private void mostrarCajaAbierta() {
        cambiarEstadoCaja("CAJA ABIERTA", "cash-status-open");
        mostrar(contenedorFormularioCierre, true);
        mostrar(contenedorCierreExistente, false);
        mostrar(contenedorNuevoMovimiento, true);
        campoEfectivoDeclarado.setText(resumenActual.getEfectivoEsperado()
                .setScale(2, RoundingMode.HALF_UP).toPlainString());
        campoObservaciones.clear();
        etiquetaDatosCierre.setText("");
        actualizarDiferencia();
        limpiarMensaje();
    }

    private void mostrarCajaCerrada(CierreCaja cierre) {
        cambiarEstadoCaja("CAJA CERRADA", "cash-status-closed");
        mostrar(contenedorFormularioCierre, false);
        mostrar(contenedorCierreExistente, true);
        mostrar(contenedorNuevoMovimiento, false);
        String cerrada = cierre.getFechaCierre() == null ? "Sin fecha registrada"
                : cierre.getFechaCierre().format(FORMATO_FECHA_HORA);
        etiquetaDatosCierre.setText("Fecha: " + cierre.getFecha().format(FORMATO_FECHA)
                + "\nCerrada: " + cerrada
                + "\nUsuario ID: " + cierre.getUsuarioCierreId()
                + "\nEfectivo calculado: " + formatearMoneda(cierre.getTotalEfectivoCalculado())
                + "\nEfectivo declarado: " + formatearMoneda(cierre.getEfectivoDeclarado())
                + "\nDiferencia: " + formatearMonedaConSigno(cierre.getDiferenciaEfectivo())
                + (cierre.getObservaciones() == null ? ""
                        : "\nObservaciones: " + cierre.getObservaciones()));
        etiquetaDiferencia.setText(formatearMonedaConSigno(cierre.getDiferenciaEfectivo()));
        actualizarEstiloDiferencia(cierre.getDiferenciaEfectivo());
        mostrarInfo("La caja de esta fecha ya fue cerrada.");
    }

    private void cambiarEstadoCaja(String texto, String clase) {
        etiquetaEstadoCaja.setText(texto);
        etiquetaEstadoCaja.getStyleClass().removeAll(
                "cash-status-open", "cash-status-closed");
        etiquetaEstadoCaja.getStyleClass().add(clase);
    }

    private void mostrar(VBox nodo, boolean visible) {
        nodo.setVisible(visible); nodo.setManaged(visible);
    }

    @FXML
    private void registrarMovimiento() {
        Usuario usuario = Navegacion.getUsuarioActual();
        if (usuario == null) { mostrarError("La sesión administrativa finalizó."); return; }
        try {
            movimientoService.registrar(
                    selectorFecha.getValue(), comboTipoMovimiento.getValue(),
                    campoConceptoMovimiento.getText(),
                    leerImporte(campoImporteMovimiento.getText()),
                    comboMedioMovimiento.getValue(),
                    campoObservacionesMovimiento.getText(), usuario.getId());
            campoConceptoMovimiento.clear();
            campoImporteMovimiento.clear();
            campoObservacionesMovimiento.clear();
            cargarCaja();
            mostrarInfo("El movimiento se registró correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void actualizarDiferencia() {
        if (resumenActual == null) return;
        if (campoEfectivoDeclarado.getText() == null
                || campoEfectivoDeclarado.getText().isBlank()) {
            etiquetaDiferencia.setText("Ingresá el efectivo contado");
            etiquetaDiferencia.getStyleClass().removeAll(
                    "cash-difference-ok", "cash-difference-warning");
            etiquetaDiferencia.getStyleClass().add("cash-difference-neutral");
            return;
        }
        try {
            BigDecimal diferencia = leerImporte(campoEfectivoDeclarado.getText())
                    .subtract(resumenActual.getEfectivoEsperado());
            etiquetaDiferencia.setText(formatearMonedaConSigno(diferencia));
            actualizarEstiloDiferencia(diferencia);
        } catch (IllegalArgumentException exception) {
            etiquetaDiferencia.setText("Importe inválido");
            actualizarEstiloDiferencia(BigDecimal.ONE.negate());
        }
    }

    private void actualizarEstiloDiferencia(BigDecimal diferencia) {
        etiquetaDiferencia.getStyleClass().removeAll(
                "cash-difference-ok", "cash-difference-warning",
                "cash-difference-neutral");
        etiquetaDiferencia.getStyleClass().add(
                diferencia != null && diferencia.signum() == 0
                        ? "cash-difference-ok" : "cash-difference-warning");
    }

    @FXML
    private void cerrarCaja() {
        if (cierreExistente != null) {
            mostrarError("La caja de esta fecha ya fue cerrada.");
            return;
        }
        Usuario usuario = Navegacion.getUsuarioActual();
        if (usuario == null) {
            mostrarError("La sesion administrativa finalizo.");
            return;
        }
        try {
            BigDecimal declarado = leerImporte(
                    campoEfectivoDeclarado.getText());
            BigDecimal diferencia = declarado.subtract(
                    resumenActual.getEfectivoEsperado());
            String detalle = "Efectivo esperado: "
                    + formatearMoneda(resumenActual.getEfectivoEsperado())
                    + "\nEfectivo declarado: " + formatearMoneda(declarado)
                    + "\nDiferencia: " + formatearMonedaConSigno(diferencia)
                    + (resumenActual.getPagosPendientes() > 0
                            ? "\n\nAdvertencia: hay pagos pendientes de acreditar."
                            : "");
            String titulo = "Cerrar caja del "
                    + selectorFecha.getValue().format(FORMATO_FECHA);
            if (Dialogos.confirmarPeligro(titulo, detalle)) {
                ejecutarCierre(declarado);
            }
        } catch (IllegalArgumentException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void ejecutarCierre(BigDecimal declarado) {
        try {
            cierreService.cerrar(selectorFecha.getValue(), declarado,
                    campoObservaciones.getText(), Navegacion.getUsuarioActual().getId());
            cargarCaja();
            mostrarInfo("La caja se cerró correctamente.");
        } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
    }

    private BigDecimal leerImporte(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Ingresá un importe.");
        }
        String valor = texto.trim().replace("$", "").replace("ARS", "").replace(" ", "");
        if (valor.contains(",") && valor.contains(".")) valor = valor.replace(".", "").replace(",", ".");
        else if (valor.contains(",")) valor = valor.replace(",", ".");
        try {
            BigDecimal importe = new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP);
            if (importe.signum() < 0) throw new IllegalArgumentException("El importe no puede ser negativo.");
            return importe;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("El importe no tiene un formato válido.");
        }
    }

    private String formatearMoneda(BigDecimal valor) {

            return FormateadorMoneda.pesos(valor);

    }
    private String formatearMonedaConSigno(BigDecimal valor) {
        BigDecimal seguro = valor == null ? BigDecimal.ZERO : valor;
        return (seguro.signum() > 0 ? "+" : "") + formatearMoneda(seguro);
    }
    @FXML private void volver() { Navegacion.mostrarDashboard(Navegacion.getUsuarioActual()); }
    private void mostrarError(String mensaje) { etiquetaMensaje.setText(mensaje == null ? "Ocurrió un error." : mensaje); etiquetaMensaje.getStyleClass().remove("mensaje-exito"); if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) etiquetaMensaje.getStyleClass().add("mensaje-error"); }
    private void mostrarInfo(String mensaje) { etiquetaMensaje.setText(mensaje); etiquetaMensaje.getStyleClass().remove("mensaje-error"); if (!etiquetaMensaje.getStyleClass().contains("mensaje-exito")) etiquetaMensaje.getStyleClass().add("mensaje-exito"); }
    private void limpiarMensaje() { etiquetaMensaje.setText(""); etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito"); }
}
