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
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
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
    // caja-pestanas-logica-v3
    private enum PestanaCaja { RESUMEN, MOVIMIENTOS, CIERRE }
    private PestanaCaja pestanaActiva = PestanaCaja.RESUMEN;
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
    @FXML private Label etiquetaPagosAcreditadosDetalle;
    @FXML private Label etiquetaPagosAcreditadosVisible;
    @FXML private Label etiquetaCierreEsperado;
    @FXML private Label etiquetaCierreJornada;
    @FXML private Label etiquetaCierreFecha;
    @FXML private Label etiquetaCierreResponsable;
    @FXML private Label etiquetaCierreObservaciones;
    @FXML private Label etiquetaCierreEsperadoConfirmado;
    @FXML private Label etiquetaCierreDeclarado;
    @FXML private Label etiquetaCierreDiferencia;

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
    @FXML private VBox panelResumen;
    @FXML private VBox panelMovimientos;
    @FXML private ScrollPane scrollMovimientos;
    @FXML private VBox panelCierre;
    @FXML private Button botonPestanaResumen;
    @FXML private Button botonPestanaMovimientos;
    @FXML private Button botonPestanaCierre;

    @FXML
    private void initialize() {
        configurarTablaMetodos();
        configurarTablaPagos();
        configurarTablaMovimientos();
        configurarFormularioMovimiento();
        configurarNavegacion();
        configurarValidaciones();
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
    }    private void configurarTablaMovimientos() {
        columnaMovimientoFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaCreacion"));
        columnaMovimientoTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipo"));
        columnaMovimientoTipo.setCellFactory(columna -> new TableCell<>() {
            private final Label insignia = new Label();
            {
                insignia.getStyleClass().add("cash-movement-badge");
                setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override
            protected void updateItem(
                    TipoMovimientoCaja tipo, boolean vacia) {
                super.updateItem(tipo, vacia);
                if (vacia || tipo == null) {
                    setGraphic(null);
                    return;
                }
                insignia.setText(tipo.toString().toUpperCase());
                insignia.getStyleClass().removeAll(
                        "cash-movement-income", "cash-movement-expense");
                insignia.getStyleClass().add(
                        tipo == TipoMovimientoCaja.INGRESO
                                ? "cash-movement-income"
                                : "cash-movement-expense");
                setGraphic(insignia);
            }
        });
        columnaMovimientoConcepto.setCellValueFactory(
                new PropertyValueFactory<>("concepto"));
        columnaMovimientoConcepto.setCellFactory(
                columna -> celdaTexto(javafx.geometry.Pos.CENTER_LEFT));
        columnaMovimientoMedio.setCellValueFactory(
                new PropertyValueFactory<>("medioPago"));
        columnaMovimientoMedio.setCellFactory(
                columna -> celdaTexto(javafx.geometry.Pos.CENTER));
        columnaMovimientoImporte.setCellValueFactory(
                new PropertyValueFactory<>("importe"));
        columnaMovimientoUsuario.setCellValueFactory(
                new PropertyValueFactory<>("nombreUsuario"));
        columnaMovimientoUsuario.setCellFactory(
                columna -> celdaTexto(javafx.geometry.Pos.CENTER));
        columnaMovimientoFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setAlignment(javafx.geometry.Pos.CENTER);
                setText(vacia || fecha == null
                        ? null : fecha.format(FORMATO_FECHA_HORA));
            }
        });
        columnaMovimientoImporte.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal importe, boolean vacia) {
                super.updateItem(importe, vacia);
                MovimientoCaja movimiento = vacia || getTableRow() == null
                        ? null : getTableRow().getItem();
                if (vacia || importe == null || movimiento == null) {
                    setText(null);
                    getStyleClass().removeAll(
                            "cash-income", "cash-expense");
                    return;
                }
                setAlignment(javafx.geometry.Pos.CENTER);
                if (!getStyleClass().contains("cash-money-cell")) {
                    getStyleClass().add("cash-money-cell");
                }
                setText((movimiento.getTipo()
                        == TipoMovimientoCaja.INGRESO ? "+" : "-")
                        + formatearMoneda(importe));
                getStyleClass().removeAll(
                        "cash-income", "cash-expense");
                getStyleClass().add(movimiento.getTipo()
                        == TipoMovimientoCaja.INGRESO
                                ? "cash-income" : "cash-expense");
            }
        });
        configurarAnchosTablaMovimientos();


        // caja-movimientos-centrados-v1
        for (TableColumn<MovimientoCaja, ?> columna : java.util.List.of(
                columnaMovimientoFecha, columnaMovimientoTipo,
                columnaMovimientoMedio,
                columnaMovimientoImporte, columnaMovimientoUsuario)) {
            columna.setStyle("-fx-alignment: CENTER;");
            columna.setReorderable(false);
        }
        columnaMovimientoConcepto.setStyle("-fx-alignment: CENTER-LEFT;");
}

    private <T> TableCell<MovimientoCaja, T> celdaTexto(
            javafx.geometry.Pos alineacion) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(alineacion);
                setText(vacia || valor == null ? null : valor.toString());
            }
        };
    }

    private void configurarAnchosTablaMovimientos() {
        vincularAnchoMovimiento(columnaMovimientoFecha, 0.14);
        vincularAnchoMovimiento(columnaMovimientoTipo, 0.11);
        vincularAnchoMovimiento(columnaMovimientoConcepto, 0.31);
        vincularAnchoMovimiento(columnaMovimientoMedio, 0.17);
        vincularAnchoMovimiento(columnaMovimientoImporte, 0.14);
        vincularAnchoMovimiento(columnaMovimientoUsuario, 0.13);
        columnaMovimientoConcepto.getStyleClass().add(
                "cash-concept-column-v5");
    }

    private void vincularAnchoMovimiento(
            TableColumn<MovimientoCaja, ?> columna, double proporcion) {
        columna.prefWidthProperty().bind(
                tablaMovimientos.widthProperty()
                        .subtract(14).multiply(proporcion));
        columna.setResizable(false);
        columna.setReorderable(false);
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
        comboTipoMovimiento.getSelectionModel().clearSelection();
        comboMedioMovimiento.getSelectionModel().clearSelection();
    }

    private void configurarNavegacion() {
        mostrarPestana(PestanaCaja.RESUMEN);
    }

    @FXML private void mostrarResumenTab() { mostrarPestana(PestanaCaja.RESUMEN); }
    @FXML private void mostrarMovimientosTab() { mostrarPestana(PestanaCaja.MOVIMIENTOS); }
    @FXML private void mostrarCierreTab() { mostrarPestana(PestanaCaja.CIERRE); }    private void mostrarPestana(PestanaCaja pestana) {
        pestanaActiva = pestana;
        limpiarMensaje();
        mostrar(panelResumen, pestana == PestanaCaja.RESUMEN);
        boolean movimientos = pestana == PestanaCaja.MOVIMIENTOS;
        scrollMovimientos.setVisible(movimientos);
        scrollMovimientos.setManaged(movimientos);
        mostrar(panelCierre, pestana == PestanaCaja.CIERRE);
        for (Button boton : java.util.List.of(
                botonPestanaResumen, botonPestanaMovimientos,
                botonPestanaCierre)) {
            boton.getStyleClass().remove("cash-tab-active-v3");
        }
        Button activo = switch (pestana) {
            case RESUMEN -> botonPestanaResumen;
            case MOVIMIENTOS -> botonPestanaMovimientos;
            case CIERRE -> botonPestanaCierre;
        };
        activo.getStyleClass().add("cash-tab-active-v3");
        if (movimientos) {
            scrollMovimientos.setVvalue(0);
            javafx.application.Platform.runLater(() -> {
                scrollMovimientos.applyCss();
                scrollMovimientos.layout();
                scrollMovimientos.setVvalue(0);
                javafx.application.Platform.runLater(() -> {
                    scrollMovimientos.setVvalue(0);
                    if (contenedorNuevoMovimiento.isVisible()
                            && !comboTipoMovimiento.isDisabled()) {
                        comboTipoMovimiento.requestFocus();
                    }
                });
            });
        }
    }    private void configurarValidaciones() {
        comboTipoMovimiento.valueProperty().addListener(
                (o, a, n) -> actualizarBotonMovimiento());
        comboMedioMovimiento.valueProperty().addListener(
                (o, a, n) -> actualizarBotonMovimiento());
        campoConceptoMovimiento.textProperty().addListener(
                (o, a, n) -> actualizarBotonMovimiento());
        campoImporteMovimiento.textProperty().addListener(
                (o, a, n) -> actualizarBotonMovimiento());
        campoEfectivoDeclarado.focusedProperty().addListener(
                (o, anterior, enfocado) -> {
                    if (enfocado) {
                        javafx.application.Platform.runLater(
                                campoEfectivoDeclarado::selectAll);
                    }
                });
        actualizarBotonMovimiento();
        actualizarBotonCierre();
    }

    private void actualizarBotonMovimiento() {
        boolean valido = comboTipoMovimiento.getValue() != null
                && comboMedioMovimiento.getValue() != null
                && campoConceptoMovimiento.getText() != null
                && !campoConceptoMovimiento.getText().trim().isBlank()
                && importePositivo(campoImporteMovimiento.getText());
        botonRegistrarMovimiento.setDisable(cierreExistente != null || !valido);
    }

    private boolean importePositivo(String texto) {
        try { return leerImporte(texto).signum() > 0; }
        catch (IllegalArgumentException exception) { return false; }
    }

    private void actualizarBotonCierre() {
        if (botonCerrarCaja == null) return;
        botonCerrarCaja.setDisable(cierreExistente != null
                || !esImporteValidoNoNegativo(campoEfectivoDeclarado.getText()));
    }

    private boolean esImporteValidoNoNegativo(String texto) {
        try { return leerImporte(texto).signum() >= 0; }
        catch (IllegalArgumentException exception) { return false; }
    }

    private void configurarFecha() {
        selectorFecha.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha == null ? "" : fecha.format(FORMATO_FECHA);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) return null;
                try {
                    return LocalDate.parse(texto.trim(), FORMATO_FECHA);
                } catch (java.time.format.DateTimeParseException exception) {
                    return selectorFecha.getValue();
                }
            }
        });
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
            int pagosVisibles = tablaPagosAcreditados.getItems().size();
            etiquetaPagosAcreditadosVisible.setText(
                    pagosVisibles + (pagosVisibles == 1
                            ? " pago visible" : " pagos visibles"));
            tablaMovimientos.setItems(FXCollections.observableArrayList(
                    movimientos));
            mostrarResumen(resumenActual);
            if (cierreExistente == null) mostrarCajaAbierta();
            else mostrarCajaCerrada(cierreExistente);
            mostrarPestana(pestanaActiva);
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
        etiquetaPagosAcreditadosDetalle.setText(
                resumen.getCantidadPagosAcreditados() == 1
                        ? "movimiento" : "movimientos");
        etiquetaCierreEsperado.setText(
                formatearMoneda(resumen.getEfectivoEsperado()));
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
        actualizarBotonMovimiento();
        actualizarBotonCierre();
        limpiarMensaje();
    }

    private void mostrarCajaCerrada(CierreCaja cierre) {
        cambiarEstadoCaja("CAJA CERRADA", "cash-status-closed");
        mostrar(contenedorFormularioCierre, false);
        mostrar(contenedorCierreExistente, true);
        mostrar(contenedorNuevoMovimiento, false);
        etiquetaCierreJornada.setText(cierre.getFecha().format(FORMATO_FECHA));
        etiquetaCierreFecha.setText(cierre.getFechaCierre() == null
                ? "Sin fecha registrada"
                : cierre.getFechaCierre().format(FORMATO_FECHA_HORA).replace(" ", " · "));
        String responsable = cierre.getNombreUsuarioCierre();
        if (responsable == null || responsable.isBlank()) {
            responsable = "Usuario administrativo #" + cierre.getUsuarioCierreId();
        }
        etiquetaCierreResponsable.setText(responsable);
        etiquetaCierreObservaciones.setText(cierre.getObservaciones() == null
                || cierre.getObservaciones().isBlank()
                        ? "Sin observaciones" : cierre.getObservaciones());
        etiquetaCierreEsperadoConfirmado.setText(
                formatearMoneda(cierre.getTotalEfectivoCalculado()));
        etiquetaCierreDeclarado.setText(
                formatearMoneda(cierre.getEfectivoDeclarado()));
        etiquetaCierreDiferencia.setText(
                formatearMonedaConSigno(cierre.getDiferenciaEfectivo()));
        actualizarEstiloDiferencia(etiquetaCierreDiferencia, cierre.getDiferenciaEfectivo());
        etiquetaDatosCierre.setText("");
        actualizarBotonMovimiento();
        actualizarBotonCierre();
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
    }    @FXML
    private void registrarMovimiento() {
        actualizarBotonMovimiento();
        if (botonRegistrarMovimiento.isDisabled()) {
            mostrarError("Completá tipo, concepto, importe mayor que cero y medio de pago.");
            return;
        }
        Usuario usuario = Navegacion.getUsuarioActual();
        if (usuario == null) {
            mostrarError("La sesión administrativa finalizó.");
            return;
        }
        try {
            movimientoService.registrar(
                    selectorFecha.getValue(), comboTipoMovimiento.getValue(),
                    campoConceptoMovimiento.getText().trim(),
                    leerImporte(campoImporteMovimiento.getText()),
                    comboMedioMovimiento.getValue(),
                    campoObservacionesMovimiento.getText(), usuario.getId());
            campoConceptoMovimiento.clear();
            campoImporteMovimiento.clear();
            campoObservacionesMovimiento.clear();
            comboTipoMovimiento.getSelectionModel().clearSelection();
            comboMedioMovimiento.getSelectionModel().clearSelection();
            cargarCaja();
            mostrarPestana(PestanaCaja.MOVIMIENTOS);
            actualizarBotonMovimiento();
            mostrarInfo("El movimiento se registró correctamente.");
            javafx.application.Platform.runLater(() -> {
                scrollMovimientos.setVvalue(0);
                tablaMovimientos.scrollTo(
                        Math.max(0, tablaMovimientos.getItems().size() - 1));
            });
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void actualizarDiferencia() {
        if (resumenActual == null) return;
        if (campoEfectivoDeclarado.getText() == null
                || campoEfectivoDeclarado.getText().isBlank()) {
            etiquetaDiferencia.setText("Ingresá el efectivo contado");
            actualizarEstiloDiferencia(etiquetaDiferencia, null);
            actualizarBotonCierre();
            return;
        }
        try {
            BigDecimal diferencia = leerImporte(campoEfectivoDeclarado.getText())
                    .subtract(resumenActual.getEfectivoEsperado());
            etiquetaDiferencia.setText(formatearMonedaConSigno(diferencia));
            actualizarEstiloDiferencia(etiquetaDiferencia, diferencia);
        } catch (IllegalArgumentException exception) {
            etiquetaDiferencia.setText("Importe inválido");
            actualizarEstiloDiferencia(etiquetaDiferencia, null);
        }
        actualizarBotonCierre();
    }

    private void actualizarEstiloDiferencia(BigDecimal diferencia) {
        actualizarEstiloDiferencia(etiquetaDiferencia, diferencia);
    }

    private void actualizarEstiloDiferencia(Label etiqueta, BigDecimal diferencia) {
        etiqueta.getStyleClass().removeAll(
                "cash-difference-ok", "cash-difference-warning",
                "cash-difference-neutral", "cash-difference-surplus",
                "cash-difference-shortage");
        String clase = diferencia == null ? "cash-difference-neutral"
                : diferencia.signum() == 0 ? "cash-difference-ok"
                : diferencia.signum() > 0 ? "cash-difference-surplus"
                : "cash-difference-shortage";
        etiqueta.getStyleClass().add(clase);
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
            if (confirmarCierreEspecifico(titulo, detalle)) {
                ejecutarCierre(declarado);
            }
        } catch (IllegalArgumentException exception) {
            mostrarError(exception.getMessage());
        }
    }    private boolean confirmarCierreEspecifico(
            String titulo, String detalle) {
        javafx.scene.control.Dialog<Boolean> dialogo =
                new javafx.scene.control.Dialog<>();
        dialogo.setTitle(titulo);
        javafx.scene.control.ButtonType confirmar =
                new javafx.scene.control.ButtonType(
                        "CONFIRMAR CIERRE",
                        javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType volver =
                new javafx.scene.control.ButtonType(
                        "VOLVER",
                        javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(confirmar, volver);

        String[] lineas = detalle.split("\n");
        javafx.scene.control.Label tituloContenido =
                new javafx.scene.control.Label("Confirmar cierre definitivo");
        tituloContenido.getStyleClass().add("cash-confirm-title-v4");
        javafx.scene.control.Label esperado = new javafx.scene.control.Label(
                valorDetalle(lineas, "Efectivo esperado:"));
        javafx.scene.control.Label declarado = new javafx.scene.control.Label(
                valorDetalle(lineas, "Efectivo declarado:"));
        javafx.scene.control.Label diferencia = new javafx.scene.control.Label(
                valorDetalle(lineas, "Diferencia:"));
        esperado.getStyleClass().add("cash-confirm-value-v4");
        declarado.getStyleClass().add("cash-confirm-value-v4");
        diferencia.getStyleClass().add("cash-confirm-value-v4");

        javafx.scene.layout.VBox tarjetaEsperado = tarjetaConfirmacion(
                "EFECTIVO ESPERADO", esperado);
        javafx.scene.layout.VBox tarjetaDeclarado = tarjetaConfirmacion(
                "EFECTIVO DECLARADO", declarado);
        javafx.scene.layout.VBox tarjetaDiferencia = tarjetaConfirmacion(
                "DIFERENCIA", diferencia);
        javafx.scene.layout.HBox cifras = new javafx.scene.layout.HBox(
                10, tarjetaEsperado, tarjetaDeclarado, tarjetaDiferencia);
        for (javafx.scene.layout.VBox tarjeta : java.util.List.of(
                tarjetaEsperado, tarjetaDeclarado, tarjetaDiferencia)) {
            javafx.scene.layout.HBox.setHgrow(
                    tarjeta, javafx.scene.layout.Priority.ALWAYS);
            tarjeta.setMaxWidth(Double.MAX_VALUE);
        }

        javafx.scene.control.Label advertencia = new javafx.scene.control.Label(
                "El cierre consolida definitivamente la jornada seleccionada. "
                + "Revisá los pagos, los movimientos manuales y el efectivo "
                + "declarado antes de continuar.");
        advertencia.setWrapText(true);
        advertencia.setMaxWidth(Double.MAX_VALUE);
        advertencia.getStyleClass().add("cash-confirm-warning-v4");
        javafx.scene.control.Label pendientes = new javafx.scene.control.Label();
        boolean hayPendientes = resumenActual != null
                && resumenActual.getPagosPendientes() > 0;
        pendientes.setText(hayPendientes
                ? "Hay pagos pendientes de acreditar."
                : "No hay pagos pendientes de acreditar.");
        pendientes.getStyleClass().add(hayPendientes
                ? "cash-confirm-pending-v4" : "cash-confirm-ok-v4");

        javafx.scene.layout.VBox contenido = new javafx.scene.layout.VBox(
                14, tituloContenido, cifras, advertencia, pendientes);
        contenido.setPadding(new javafx.geometry.Insets(18));
        contenido.setPrefWidth(680);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefWidth(720);
        dialogo.getDialogPane().getStyleClass().add("cash-confirm-dialog-v4");
        var css = CierreCajaController.class.getResource(
                "/css/cierre-caja.css");
        if (css != null) {
            dialogo.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        javafx.scene.Node confirmarNodo =
                dialogo.getDialogPane().lookupButton(confirmar);
        javafx.scene.Node volverNodo =
                dialogo.getDialogPane().lookupButton(volver);
        confirmarNodo.getStyleClass().add("cash-confirm-button-v4");
        volverNodo.getStyleClass().add("cash-back-button-v4");
        dialogo.setResultConverter(tipo -> tipo == confirmar);
        return dialogo.showAndWait().orElse(false);
    }

    private javafx.scene.layout.VBox tarjetaConfirmacion(
            String titulo, javafx.scene.control.Label valor) {
        javafx.scene.control.Label etiqueta =
                new javafx.scene.control.Label(titulo);
        etiqueta.getStyleClass().add("cash-confirm-label-v4");
        javafx.scene.layout.VBox tarjeta =
                new javafx.scene.layout.VBox(5, etiqueta, valor);
        tarjeta.getStyleClass().add("cash-confirm-card-v4");
        return tarjeta;
    }

    private String valorDetalle(String[] lineas, String prefijo) {
        for (String linea : lineas) {
            if (linea.startsWith(prefijo)) {
                return linea.substring(prefijo.length()).trim();
            }
        }
        return "-";
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
    // modernizar-caja-operativa-unificada-v1
    // cerrar-caja-operativa-v3
}
