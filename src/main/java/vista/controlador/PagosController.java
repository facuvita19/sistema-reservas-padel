package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import negocio.EstadoPago;
import negocio.EstadoReserva;
import negocio.MetodoPago;
import negocio.Pago;
import negocio.Reserva;
import servicio.ConfiguracionComplejoService;
import servicio.PagoService;
import servicio.ReservaService;
import util.FormateadorMoneda;
import vista.FiltroPagos;
import vista.Navegacion;
import vista.Dialogos;
import vista.SolicitudFiltroPagos;
import vista.SolicitudPagoReserva;

public class PagosController {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final PagoService pagoService = new PagoService();
    private final ReservaService reservaService = new ReservaService();
    private final ConfiguracionComplejoService configuracionService = new ConfiguracionComplejoService();
    private final ObservableList<Pago> pagos = FXCollections.observableArrayList();
    private final ObservableList<Reserva> reservas = FXCollections.observableArrayList();
    private final Map<Long, Reserva> reservasPorId = new HashMap<>();
    private final Map<Long, BigDecimal> saldoPorReserva = new HashMap<>();
    private FilteredList<Pago> pagosFiltrados;
    private Pago pagoSeleccionado;
    private EstadoPago vistaRapida;
    private boolean soloHoy;

    @FXML private TextField campoBuscar;
    @FXML private ComboBox<MetodoPago> filtroMetodo;
    @FXML private DatePicker filtroDesde;
    @FXML private DatePicker filtroHasta;
    @FXML private CheckBox checkConSaldo;
    @FXML private Label etiquetaCantidad;
    @FXML private Label etiquetaPendientes, etiquetaAcreditados;
    @FXML private Label etiquetaReembolsados, etiquetaAnulados;
    @FXML private Label etiquetaAyudaResultados;
    @FXML private Label etiquetaActualizacion;
    @FXML private Button botonPendientes, botonAcreditados;
    @FXML private Button botonReembolsados, botonAnulados;
    @FXML private Button botonHoy, botonTodos;
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, Long> columnaReserva;
    @FXML private TableColumn<Pago, String> columnaTurno;
    @FXML private TableColumn<Pago, BigDecimal> columnaImporte;
    @FXML private TableColumn<Pago, MetodoPago> columnaMetodo;
    @FXML private TableColumn<Pago, EstadoPago> columnaEstado;
    @FXML private TableColumn<Pago, LocalDateTime> columnaFecha;
    @FXML private TableColumn<Pago, LocalDateTime> columnaAcreditado;
    @FXML private TableColumn<Pago, String> columnaReferencia;

    @FXML private VBox panelFormulario, panelDetalle, panelDetalleVacio;
    @FXML private ScrollPane scrollPanelDerecho, scrollPanelFormulario;
    @FXML private ComboBox<Reserva> comboReserva;
    @FXML private TextField campoImporte;
    @FXML private ComboBox<MetodoPago> comboMetodo;
    @FXML private ComboBox<EstadoPago> comboEstadoInicial;
    @FXML private TextField campoReferencia;
    @FXML private Label etiquetaPrecioTotal, etiquetaTotalAcreditado, etiquetaSaldo;
    @FXML private Label etiquetaReservaDetalle, etiquetaMensaje, tituloFormulario;
    @FXML private Label detalleTitulo, detalleReserva, detalleCliente, detalleTurno;
    @FXML private Label detalleEstadoCabecera;
    @FXML private Label detalleImporte, detalleMetodo;
    @FXML private Label detalleRegistrado, detalleAcreditado, detalleReferencia;
    @FXML private Label detallePrecioReserva, detalleTotalAcreditado;
    @FXML private Label detalleSaldoReserva, detallePorcentajePago;
    @FXML private javafx.scene.control.ProgressBar detalleProgresoPago;
    @FXML private Label detalleSituacionTitulo, detalleSituacionAyuda;
    @FXML private Label etiquetaAccionesPago;
    @FXML private Button botonAcreditar, botonAnular;
    @FXML private Button botonGuardarMovimiento, botonNuevoMovimiento;
    @FXML private Button botonAbrirReserva;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarCombos();
        configurarFiltros();
        configurarInteracciones();
        cargarReservas();
        mostrarPanelDetalleVacio();
        cargarPagos();
        Platform.runLater(() -> {
            aplicarSolicitudDeReserva();
            aplicarSolicitudFiltroDashboard();
            if (Navegacion.consumirSolicitudFiltroPagos() == null && pagosFiltrados.isEmpty()) verHoy();
        });
    }

    private void configurarInteracciones() {
        tablaPagos.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 1
                    && clicEnFondoTabla(evento.getTarget())) {
                limpiarSeleccionPago();
            }
        });

        campoReferencia.textProperty().addListener(
                (obs, anterior, actual) -> limpiarMensajeContextual());
    }

    private void configurarTabla() {
        columnaReserva.setCellValueFactory(new PropertyValueFactory<>("reservaId"));
        columnaReserva.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Long id, boolean vacia) {
                super.updateItem(id, vacia);
                Reserva r = id == null ? null : reservasPorId.get(id);
                if (vacia || id == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                VBox contenido = new VBox(2);
                Label principal = new Label(
                        r == null ? "Reserva #" + id : r.getNombreCliente());
                principal.getStyleClass().add("payment-table-primary");

                Label secundario = new Label(
                        r == null ? "Reserva no disponible"
                                : "Reserva #" + id + " · " + r.getNombreCancha());
                secundario.getStyleClass().add("payment-table-secondary");
                instalarTooltipSiTruncado(principal);
                instalarTooltipSiTruncado(secundario);

                contenido.getChildren().setAll(principal, secundario);
                setText(null);
                setGraphic(contenido);
            }
        });
        columnaTurno.setCellValueFactory(d -> {
            Reserva r = reservasPorId.get(d.getValue().getReservaId());
            return new javafx.beans.property.SimpleStringProperty(
                    r == null ? "-"
                            : r.getFecha().format(FECHA)
                            + " · " + r.getHoraInicio().format(HORA));
        });
        columnaImporte.setCellValueFactory(new PropertyValueFactory<>("importe"));
        columnaImporte.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal v, boolean vacia) {
                super.updateItem(v, vacia);
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                getStyleClass().add("payment-amount-cell");
                setText(vacia || v == null ? null : FormateadorMoneda.pesos(v));
            }
        });
        columnaMetodo.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columnaEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(EstadoPago estado, boolean vacia) {
                super.updateItem(estado, vacia);
                setGraphic(null);
                setText(vacia || estado == null ? null : estado.toString());
                getStyleClass().removeAll(
                        "payment-state-pending", "payment-state-accredited",
                        "payment-state-refunded", "payment-state-cancelled");
                if (!vacia && estado != null) {
                    getStyleClass().add(switch (estado) {
                        case PENDIENTE -> "payment-state-pending";
                        case ACREDITADO -> "payment-state-accredited";
                        case REEMBOLSADO -> "payment-state-refunded";
                        case ANULADO -> "payment-state-cancelled";
                    });
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        columnaAcreditado.setCellValueFactory(new PropertyValueFactory<>("fechaPago"));
        columnaFecha.setCellFactory(c -> celdaFecha());
        columnaAcreditado.setCellFactory(c -> celdaFecha());
        columnaMetodo.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(MetodoPago metodo, boolean vacia) {
                super.updateItem(metodo, vacia);
                setText(vacia || metodo == null ? null : metodo.toString());
                setAlignment(javafx.geometry.Pos.CENTER);
                getStyleClass().add("payment-method-cell");
            }
        });
        columnaReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
        configurarAnchosProporcionales();
        tablaPagos.getSelectionModel().selectedItemProperty().addListener((o, a, pago) -> {
            if (pago != null) mostrarDetallePago(pago);
        });
        tablaPagos.setRowFactory(tabla -> {
            TableRow<Pago> fila = new TableRow<>();
            fila.selectedProperty().addListener((obs, anterior, seleccionada) -> {
                fila.getStyleClass().remove("payment-row-selected");
                if (seleccionada) fila.getStyleClass().add("payment-row-selected");
            });
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    Navegacion.mostrarReservaDesdeAgenda(
                            fila.getItem().getReservaId());
                }
            });
            return fila;
        });
    }

    private boolean clicEnFondoTabla(Object objetivo) {
        if (!(objetivo instanceof Node nodo)) return false;
        Node actual = nodo;
        while (actual != null && actual != tablaPagos) {
            if (actual instanceof TableRow<?> fila) return fila.isEmpty();
            actual = actual.getParent();
        }
        return actual == tablaPagos;
    }

    private void mostrarFormularioPago(boolean visible) {
        panelFormulario.setVisible(visible);
        panelFormulario.setManaged(visible);
        scrollPanelFormulario.setVisible(visible);
        scrollPanelFormulario.setManaged(visible);
        if (visible) {
            Platform.runLater(() -> scrollPanelFormulario.setVvalue(0));
        }
    }

    private void ocultarDetallePago() {
        panelDetalle.setVisible(false);
        panelDetalle.setManaged(false);
        scrollPanelDerecho.setVisible(false);
        scrollPanelDerecho.setManaged(false);
    }

    @FXML
    private void limpiarSeleccionPago() {
        tablaPagos.getSelectionModel().clearSelection();
        mostrarPanelDetalleVacio();
        tablaPagos.requestFocus();
    }

    private void configurarAnchosProporcionales() {
        vincularAncho(columnaReserva, 0.255);
        vincularAncho(columnaTurno, 0.145);
        vincularAncho(columnaImporte, 0.115);
        vincularAncho(columnaMetodo, 0.125);
        vincularAncho(columnaEstado, 0.135);
        vincularAncho(columnaFecha, 0.190);
    }

    private void vincularAncho(
            TableColumn<Pago, ?> columna,
            double proporcion) {
        columna.prefWidthProperty().bind(
                tablaPagos.widthProperty().subtract(14).multiply(proporcion));
        columna.setResizable(false);
        columna.setReorderable(false);
    }

    private void instalarTooltipSiTruncado(Label etiqueta) {
        Tooltip tooltip = new Tooltip();
        etiqueta.widthProperty().addListener((obs, anterior, actual) -> {
            boolean truncado = etiqueta.getText() != null
                    && etiqueta.getLayoutBounds().getWidth() > actual.doubleValue();
            if (truncado) {
                tooltip.setText(etiqueta.getText());
                etiqueta.setTooltip(tooltip);
            } else {
                etiqueta.setTooltip(null);
            }
        });
    }

    private TableCell<Pago, LocalDateTime> celdaFecha() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setGraphic(null);
                setText(vacia || valor == null ? null
                        : valor.format(FECHA) + " · " + valor.format(HORA));
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        };
    }

    private void configurarCombos() {
        comboMetodo.setItems(FXCollections.observableArrayList(MetodoPago.values()));
        comboEstadoInicial.setItems(FXCollections.observableArrayList(EstadoPago.PENDIENTE, EstadoPago.ACREDITADO));
        comboReserva.setCellFactory(l -> crearCeldaReserva()); comboReserva.setButtonCell(crearCeldaReserva());
        comboReserva.valueProperty().addListener((o,a,r) -> actualizarResumen(r));
        filtroMetodo.setItems(FXCollections.observableArrayList(MetodoPago.values()));
        comboReserva.setVisibleRowCount(7);
        campoImporte.textProperty().addListener((o, a, v) -> actualizarBotonGuardar());
        comboReserva.valueProperty().addListener((o, a, v) -> actualizarBotonGuardar());
        comboMetodo.valueProperty().addListener((o, a, v) -> actualizarBotonGuardar());
        comboEstadoInicial.valueProperty().addListener((o, a, v) -> actualizarBotonGuardar());
    }

    private ListCell<Reserva> crearCeldaReserva() {
        return new ListCell<>() { @Override protected void updateItem(Reserva r, boolean vacia) { super.updateItem(r,vacia); setText(vacia || r == null ? null : reservaExtendida(r)); } };
    }

    private void configurarFiltros() {
        pagosFiltrados = new FilteredList<>(pagos, p -> true); tablaPagos.setItems(pagosFiltrados);
        campoBuscar.textProperty().addListener((o,a,v) -> aplicarFiltros());
        filtroMetodo.valueProperty().addListener((o,a,v) -> aplicarFiltros());
        filtroDesde.valueProperty().addListener((o,a,v) -> aplicarFiltros());
        filtroHasta.valueProperty().addListener((o,a,v) -> aplicarFiltros());
        checkConSaldo.selectedProperty().addListener((o,a,v) -> aplicarFiltros());
    }

    @FXML private void cargarReservas() {
        try {
            List<Reserva> lista = reservaService.listar();
            reservas.setAll(lista.stream().filter(r -> r.getEstado()!=EstadoReserva.CANCELADA && r.getEstado()!=EstadoReserva.EXPIRADA).toList());
            reservasPorId.clear(); lista.forEach(r -> reservasPorId.put(r.getId(), r));
            comboReserva.setItems(reservas);
            recalcularSaldosLocales();
            tablaPagos.refresh();
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    @FXML private void cargarPagos() {
        try {
            pagos.setAll(pagoService.listar());
            recalcularSaldosLocales();
            actualizarContadores();
            aplicarFiltros();
            if (etiquetaActualizacion != null) {
                etiquetaActualizacion.setText(
                        "Última actualización: " + java.time.LocalTime.now().format(HORA));
            }
        }
        catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    private void aplicarFiltros() {
        if (pagosFiltrados == null) return;
        String q = campoBuscar.getText()==null ? "" : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        EstadoPago estado = vistaRapida;
        MetodoPago metodo = filtroMetodo.getValue(); LocalDate desde=filtroDesde.getValue(), hasta=filtroHasta.getValue();
        pagosFiltrados.setPredicate(p -> {
            Reserva r = reservasPorId.get(p.getReservaId());
            LocalDate fecha = fechaFiltro(p);
            boolean texto = q.isBlank() || contiene(p.getReferencia(),q) || contiene(p.getMetodoPago().toString(),q)
                    || contiene(p.getEstado().toString(),q) || contiene(r==null?"Reserva "+p.getReservaId():reservaExtendida(r),q);
            boolean estadoOk = estado==null || p.getEstado()==estado;
            boolean metodoOk = metodo==null || p.getMetodoPago()==metodo;
            boolean fechaOk = (desde==null || !fecha.isBefore(desde)) && (hasta==null || !fecha.isAfter(hasta));
            boolean hoyOk = !soloHoy || fecha.equals(LocalDate.now());
            boolean saldoOk = !checkConSaldo.isSelected()
                    || (r != null && saldoPorReserva
                            .getOrDefault(r.getId(), BigDecimal.ZERO)
                            .signum() > 0);
            return texto && estadoOk && metodoOk && fechaOk && hoyOk && saldoOk;
        });
        actualizarCantidadVisible();
        mantenerSeleccionVisible();
    }

    private void actualizarCantidadVisible() {
        int visibles = pagosFiltrados == null ? 0 : pagosFiltrados.size();
        int total = pagos.size();

        etiquetaCantidad.setText(visibles == total
                ? textoCantidad(visibles, "movimiento visible", "movimientos visibles")
                : visibles + " movimientos visibles de " + total);

        etiquetaAyudaResultados.setText(visibles == 0
                ? "Sin resultados para los filtros actuales"
                : "Doble clic para abrir la reserva");
    }

    private String textoCantidad(
            int cantidad, String singular, String plural) {
        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }

    private void mantenerSeleccionVisible() {
        if (panelFormulario.isVisible() || pagosFiltrados == null) return;
        Pago actual = tablaPagos.getSelectionModel().getSelectedItem();
        if (actual != null && pagosFiltrados.contains(actual)) {
            mostrarDetallePago(actual);
            return;
        }
        if (pagosFiltrados.isEmpty()) {
            mostrarPanelDetalleVacio();
            return;
        }
        tablaPagos.getSelectionModel().selectFirst();
        tablaPagos.scrollTo(0);
    }

    private void recalcularSaldosLocales() {
        Map<Long, BigDecimal> acreditadoPorReserva = new HashMap<>();
        for (Pago pago : pagos) {
            if (pago.getEstado() == EstadoPago.ACREDITADO) {
                acreditadoPorReserva.merge(
                        pago.getReservaId(),
                        pago.getImporte() == null
                                ? BigDecimal.ZERO : pago.getImporte(),
                        BigDecimal::add);
            }
        }
        saldoPorReserva.clear();
        for (Reserva reserva : reservasPorId.values()) {
            BigDecimal precio = reserva.getPrecioTotal() == null
                    ? BigDecimal.ZERO : reserva.getPrecioTotal();
            BigDecimal acreditado = acreditadoPorReserva.getOrDefault(
                    reserva.getId(), BigDecimal.ZERO);
            saldoPorReserva.put(
                    reserva.getId(),
                    precio.subtract(acreditado).max(BigDecimal.ZERO));
        }
    }

    private LocalDate fechaFiltro(Pago p) { LocalDateTime f=p.getFechaPago()!=null?p.getFechaPago():p.getFechaCreacion(); return f==null?LocalDate.MIN:f.toLocalDate(); }
    private boolean contiene(String v,String q){return v!=null&&v.toLowerCase(Locale.ROOT).contains(q);}

    @FXML private void verPendientes(){seleccionarVista(EstadoPago.PENDIENTE,false,botonPendientes);}
    @FXML private void verAcreditados(){seleccionarVista(EstadoPago.ACREDITADO,false,botonAcreditados);}
    @FXML private void verReembolsados(){seleccionarVista(EstadoPago.REEMBOLSADO,false,botonReembolsados);}
    @FXML private void verAnulados(){seleccionarVista(EstadoPago.ANULADO,false,botonAnulados);}
    @FXML
    private void verHoy() {
        soloHoy = true;
        actualizarPeriodo();
        aplicarFiltros();
    }

    @FXML
    private void verTodos() {
        soloHoy = false;
        actualizarPeriodo();
        aplicarFiltros();
    }

    private void seleccionarVista(
            EstadoPago estado, boolean hoy, Button boton) {
        vistaRapida = estado;
        soloHoy = hoy;
        actualizarSeleccionRapida(boton);
        actualizarPeriodo();
        aplicarFiltros();
    }

    private void actualizarSeleccionRapida(Button activo) {
        for (Button boton : List.of(
                botonPendientes, botonAcreditados,
                botonReembolsados, botonAnulados)) {
            boton.getStyleClass().remove("payment-view-active");
        }
        if (activo != null) {
            activo.getStyleClass().add("payment-view-active");
        }
    }

    private void actualizarPeriodo() {
        botonHoy.getStyleClass().remove("payment-period-active");
        botonTodos.getStyleClass().remove("payment-period-active");
        (soloHoy ? botonHoy : botonTodos)
                .getStyleClass().add("payment-period-active");
    }
    private void actualizarContadores() {
        etiquetaPendientes.setText(
                String.valueOf(contar(EstadoPago.PENDIENTE)));
        etiquetaAcreditados.setText(
                String.valueOf(contar(EstadoPago.ACREDITADO)));
        etiquetaReembolsados.setText(
                String.valueOf(contar(EstadoPago.REEMBOLSADO)));
        etiquetaAnulados.setText(
                String.valueOf(contar(EstadoPago.ANULADO)));
    }
    private long contar(EstadoPago e){return pagos.stream().filter(p->p.getEstado()==e).count();}

    @FXML
    private void limpiarFiltros() {
        campoBuscar.clear();
        filtroMetodo.getSelectionModel().clearSelection();
        filtroDesde.setValue(null);
        filtroHasta.setValue(null);
        checkConSaldo.setSelected(false);
        vistaRapida = null;
        soloHoy = false;
        actualizarSeleccionRapida(null);
        actualizarPeriodo();
        aplicarFiltros();
        campoBuscar.requestFocus();
    }

    @FXML
    private void nuevoPago() {
        pagoSeleccionado = null;
        tablaPagos.getSelectionModel().clearSelection();
        panelDetalleVacio.setVisible(false);
        panelDetalleVacio.setManaged(false);
        ocultarDetallePago();
        mostrarFormularioPago(true);
        activarModoCreacion(true);
        tituloFormulario.setText("Nuevo movimiento");
        comboReserva.getSelectionModel().clearSelection();
        campoImporte.clear();
        comboMetodo.setValue(MetodoPago.TRANSFERENCIA);
        comboEstadoInicial.setValue(EstadoPago.PENDIENTE);
        campoReferencia.clear();
        limpiarResumen();
        limpiarMensajeContextual();
        actualizarBotonGuardar();
        volverArribaDetalle();
        Platform.runLater(comboReserva::requestFocus);
    }
    @FXML
    private void cancelarFormulario() {
        mostrarFormularioPago(false);
        activarModoCreacion(false);
        limpiarMensajeContextual();
        mantenerSeleccionVisible();
    }

    private void mostrarDetallePago(Pago p) {
        pagoSeleccionado = p;
        activarModoCreacion(false);
        mostrarFormularioPago(false);
        panelDetalleVacio.setVisible(false);
        panelDetalleVacio.setManaged(false);
        panelDetalle.setVisible(true);
        scrollPanelDerecho.setVisible(true);
        panelDetalle.setManaged(true);
        scrollPanelDerecho.setManaged(true);

        Reserva r = reservasPorId.get(p.getReservaId());
        detalleTitulo.setText("Movimiento #" + p.getId());
        volverArribaDetalle();
        limpiarMensajeContextual();

        detalleEstadoCabecera.setText(
                p.getEstado().toString().toUpperCase(Locale.ROOT));
        detalleEstadoCabecera.getStyleClass().removeAll(
                "payment-detail-status-pending", "payment-detail-status-accredited",
                "payment-detail-status-refunded", "payment-detail-status-cancelled");
        detalleEstadoCabecera.getStyleClass().add(switch (p.getEstado()) {
            case PENDIENTE -> "payment-detail-status-pending";
            case ACREDITADO -> "payment-detail-status-accredited";
            case REEMBOLSADO -> "payment-detail-status-refunded";
            case ANULADO -> "payment-detail-status-cancelled";
        });
        detalleEstadoCabecera.setVisible(true);
        detalleEstadoCabecera.setManaged(true);
        detalleReserva.setText(r == null
                ? "Reserva #" + p.getReservaId()
                : "Reserva #" + r.getId() + " · " + r.getNombreCancha());
        detalleCliente.setText(
                r == null ? "Datos no disponibles" : r.getNombreCliente());
        detalleTurno.setText(r == null ? "-"
                : r.getFecha().format(FECHA)
                + " · " + r.getHoraInicio().format(HORA));

        detalleImporte.setText(FormateadorMoneda.pesos(p.getImporte()));
        detalleMetodo.setText(p.getMetodoPago().toString());
        detalleRegistrado.setText(
                formatearFechaDetalle(p.getFechaCreacion()));
        detalleAcreditado.setText(
                formatearFechaDetalle(p.getFechaPago()));
        detalleReferencia.setText(
                p.getReferencia() == null || p.getReferencia().isBlank()
                        ? "Sin referencia" : p.getReferencia());

        actualizarResumenDetalle(r);

        boolean pendiente = p.getEstado() == EstadoPago.PENDIENTE;
        botonAcreditar.setVisible(pendiente);
        botonAcreditar.setManaged(pendiente);
        botonAnular.setVisible(pendiente);
        botonAnular.setManaged(pendiente);
        botonAcreditar.setDisable(!pendiente);
        botonAnular.setDisable(!pendiente);

        botonAbrirReserva.setDisable(r == null);

        detalleSituacionTitulo.setText(pendiente ? "Pendiente" : "Finalizado");
        detalleSituacionAyuda.setText(pendiente
                ? "El movimiento está listo para acreditarse o anularse."
                : "Este movimiento ya no admite modificaciones.");
        etiquetaAccionesPago.setText("");
        etiquetaAccionesPago.setVisible(false);
        etiquetaAccionesPago.setManaged(false);
        etiquetaAccionesPago.getStyleClass().removeAll(
                "payment-action-pending", "payment-action-final");
        etiquetaAccionesPago.getStyleClass().add(pendiente
                ? "payment-action-pending" : "payment-action-final");
    }
    private void mostrarPanelDetalleVacio() {
        pagoSeleccionado = null;
        mostrarFormularioPago(false);
        panelDetalle.setVisible(false);
        scrollPanelDerecho.setVisible(false);
        panelDetalle.setManaged(false);
        scrollPanelDerecho.setManaged(false);
        panelDetalleVacio.setVisible(true);
        panelDetalleVacio.setManaged(true);
        botonAcreditar.setDisable(true);
        botonAnular.setDisable(true);
        volverArribaDetalle();
        limpiarMensajeContextual();
    }

    private void actualizarResumenDetalle(Reserva reserva) {
        if (reserva == null) {
            detallePrecioReserva.setText("-");
            detalleTotalAcreditado.setText("-");
            detalleSaldoReserva.setText("-");
            detalleProgresoPago.setProgress(0);
            detallePorcentajePago.setText("0 % acreditado");
            return;
        }

        BigDecimal precio = reserva.getPrecioTotal() == null
                ? BigDecimal.ZERO : reserva.getPrecioTotal();
        BigDecimal saldo = saldoPorReserva.getOrDefault(
                reserva.getId(), BigDecimal.ZERO);
        BigDecimal acreditado = precio.subtract(saldo).max(BigDecimal.ZERO);
        double progreso = precio.signum() <= 0 ? 0
                : acreditado.divide(precio, 4, RoundingMode.HALF_UP)
                        .max(BigDecimal.ZERO).min(BigDecimal.ONE).doubleValue();

        detallePrecioReserva.setText(FormateadorMoneda.pesos(precio));
        detalleTotalAcreditado.setText(FormateadorMoneda.pesos(acreditado));
        detalleSaldoReserva.setText(FormateadorMoneda.pesos(saldo));
        detalleProgresoPago.setProgress(progreso);
        detallePorcentajePago.setText(
                Math.round(progreso * 100) + " % acreditado");
    }

    @FXML
    private void abrirReservaSeleccionada() {
        if (pagoSeleccionado == null) {
            mostrarError("Seleccioná un movimiento.");
            return;
        }
        Navegacion.mostrarReservaDesdeAgenda(
                pagoSeleccionado.getReservaId());
    }

    private void volverArribaDetalle() {
        if (scrollPanelDerecho != null) {
            Platform.runLater(() -> scrollPanelDerecho.setVvalue(0));
        }
    }

    private void aplicarSolicitudFiltroDashboard(){SolicitudFiltroPagos s=Navegacion.consumirSolicitudFiltroPagos();if(s!=null&&s.filtro()==FiltroPagos.PENDIENTES_ACREDITACION){verPendientes();mostrarInfo(pagosFiltrados.size()+" pago(s) pendientes de acreditar.");}}
    private void aplicarSolicitudDeReserva() {
        SolicitudPagoReserva solicitud = Navegacion.consumirSolicitudPagoReserva();
        if (solicitud == null) return;

        Reserva reserva = reservasPorId.get(solicitud.reservaId());
        if (reserva == null) {
            mostrarError("La reserva seleccionada no está disponible para pagos.");
            return;
        }

        nuevoPago();
        comboReserva.setValue(reserva);
        actualizarResumen(reserva);
        Platform.runLater(() -> {
            scrollPanelFormulario.setVvalue(0);
            campoImporte.requestFocus();
            campoImporte.selectAll();
        });
    }

    private void actualizarResumen(Reserva r){if(r==null){limpiarResumen();return;}BigDecimal saldo=pagoService.calcularSaldo(r.getId());BigDecimal acreditado=r.getPrecioTotal().subtract(saldo);etiquetaPrecioTotal.setText(FormateadorMoneda.pesos(r.getPrecioTotal()));etiquetaTotalAcreditado.setText(FormateadorMoneda.pesos(acreditado));etiquetaSaldo.setText(FormateadorMoneda.pesos(saldo));etiquetaReservaDetalle.setText(reservaExtendida(r));if(pagoSeleccionado==null){BigDecimal s=configuracionService.calcularSenia(r.getPrecioTotal());campoImporte.setText(saldo.min(s).toPlainString());}}
    private void limpiarResumen(){etiquetaPrecioTotal.setText("ARS 0");etiquetaTotalAcreditado.setText("ARS 0");etiquetaSaldo.setText("ARS 0");etiquetaReservaDetalle.setText("Seleccioná una reserva para ver el saldo.");}

    private void activarModoCreacion(boolean activo) {
        if (botonNuevoMovimiento == null) return;
        botonNuevoMovimiento.setDisable(activo);
        botonNuevoMovimiento.setText(
                activo ? "MOVIMIENTO EN EDICIÓN" : "NUEVO MOVIMIENTO");
        botonNuevoMovimiento.getStyleClass().remove(
                "payment-new-button-active");
        if (activo) {
            botonNuevoMovimiento.getStyleClass().add(
                    "payment-new-button-active");
        }
    }

    private String formatearFechaDetalle(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FECHA) + " · " + fecha.format(HORA);
    }

    private void actualizarBotonGuardar() {
        if (botonGuardarMovimiento == null) return;
        boolean importeValido = false;
        try {
            importeValido = parsearImporte(campoImporte.getText()).signum() > 0;
        } catch (RuntimeException ignored) {
            importeValido = false;
        }
        botonGuardarMovimiento.setDisable(
                comboReserva.getValue() == null
                || comboMetodo.getValue() == null
                || comboEstadoInicial.getValue() == null
                || !importeValido);
    }

    @FXML private void guardar(){try{if(comboReserva.getValue()==null)throw new IllegalArgumentException("Seleccioná una reserva.");Pago p=new Pago();p.setReservaId(comboReserva.getValue().getId());p.setImporte(parsearImporte(campoImporte.getText()));p.setMetodoPago(comboMetodo.getValue());p.setEstado(comboEstadoInicial.getValue());p.setReferencia(campoReferencia.getText());pagoService.guardar(p);cargarPagos();mostrarDetallePago(p);mostrarInfo("El movimiento se guardó correctamente.");}catch(RuntimeException e){mostrarError(e.getMessage());}}
    private BigDecimal parsearImporte(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("Ingresá el importe.");try{return new BigDecimal(v.trim().replace("ARS","").replace("$","").replace(" ","").replace(",","."));}catch(NumberFormatException e){throw new IllegalArgumentException("El importe debe ser un número válido.");}}
    @FXML private void acreditar(){cambiarEstado("Acreditar pago",()->pagoService.acreditar(pagoSeleccionado.getId()));}
    @FXML private void anular(){cambiarEstado("Anular pago",()->pagoService.anular(pagoSeleccionado.getId()));}
    private void cambiarEstado(String titulo, Runnable accion) {
        if (pagoSeleccionado == null) {
            mostrarError("Seleccioná un movimiento.");
            return;
        }
        boolean peligrosa = titulo.toLowerCase().contains("anular")
                || titulo.toLowerCase().contains("reembols");
        String mensaje = "Movimiento #" + pagoSeleccionado.getId()
                + "\nImporte: ARS " + pagoSeleccionado.getImporte();
        boolean confirmado = peligrosa
                ? Dialogos.confirmarPeligro(titulo, mensaje)
                : Dialogos.confirmar(titulo, mensaje);
        if (!confirmado) return;
        try {
            long id = pagoSeleccionado.getId();
            accion.run();
            cargarPagos();
            pagos.stream().filter(p -> p.getId() == id).findFirst()
                    .ifPresent(this::mostrarDetallePago);
            mostrarInfo("El estado se actualizó correctamente.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    private String reservaBreve(Reserva r){return "#"+r.getId()+" · "+r.getNombreCliente()+" · "+r.getNombreCancha();}
    private String reservaExtendida(Reserva r){return "#"+r.getId()+" · "+r.getNombreCliente()+" · "+r.getNombreCancha()+" · "+r.getFecha().format(FECHA)+" "+r.getHoraInicio().format(HORA);}
    @FXML private void volver(){Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());}
    private void limpiarMensajeContextual() {
        etiquetaMensaje.setText("");
        etiquetaMensaje.getStyleClass().removeAll(
                "mensaje-error", "mensaje-exito");
    }

    private void mostrarError(String mensaje) {
        etiquetaMensaje.setText(
                mensaje == null ? "Ocurrió un error." : mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-exito");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) {
            etiquetaMensaje.getStyleClass().add("mensaje-error");
        }
    }

    private void mostrarInfo(String mensaje) {
        etiquetaMensaje.setText(mensaje == null ? "" : mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-error");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-exito")) {
            etiquetaMensaje.getStyleClass().add("mensaje-exito");
        }
    }
    // pagos-unificacion-estructural-v3
    // corregir-detalle-y-tabla-pagos-v7
    // cerrar-detalles-visuales-pagos-v8
    // corregir-registro-senia-pagos-v9
}
