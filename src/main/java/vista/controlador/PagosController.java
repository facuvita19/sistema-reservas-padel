package vista.controlador;

import java.math.BigDecimal;
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
    @FXML private Button botonPendientes, botonAcreditados, botonReembolsados, botonAnulados, botonHoy, botonTodos;
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, Long> columnaReserva;
    @FXML private TableColumn<Pago, String> columnaTurno;
    @FXML private TableColumn<Pago, BigDecimal> columnaImporte;
    @FXML private TableColumn<Pago, MetodoPago> columnaMetodo;
    @FXML private TableColumn<Pago, EstadoPago> columnaEstado;
    @FXML private TableColumn<Pago, LocalDateTime> columnaFecha;
    @FXML private TableColumn<Pago, LocalDateTime> columnaAcreditado;
    @FXML private TableColumn<Pago, String> columnaReferencia;

    @FXML private VBox panelFormulario, panelDetalle;
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
    @FXML private Label etiquetaAccionesPago;
    @FXML private Button botonAcreditar, botonAnular, botonGuardarMovimiento, botonNuevoMovimiento;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarCombos();
        configurarFiltros();
        cargarReservas();
        mostrarPanelDetalleVacio();
        cargarPagos();
        Platform.runLater(() -> {
            aplicarSolicitudDeReserva();
            aplicarSolicitudFiltroDashboard();
            if (Navegacion.consumirSolicitudFiltroPagos() == null && pagosFiltrados.isEmpty()) verHoy();
        });
    }

    private void configurarTabla() {
        columnaReserva.setCellValueFactory(new PropertyValueFactory<>("reservaId"));
        columnaReserva.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Long id, boolean vacia) {
                super.updateItem(id, vacia);
                Reserva r = id == null ? null : reservasPorId.get(id);
                setText(vacia || id == null ? null : r == null ? "Reserva #" + id : reservaBreve(r));
            }
        });
        columnaTurno.setCellValueFactory(d -> {
            Reserva r = reservasPorId.get(d.getValue().getReservaId());
            return new javafx.beans.property.SimpleStringProperty(r == null ? "-" : r.getFecha().format(FECHA) + " · " + r.getHoraInicio().format(HORA));
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
        columnaEstado.setCellFactory(celda -> new TableCell<>() {
            private final Label insignia = new Label();
            {
                insignia.getStyleClass().add("payment-status-badge");
                setText(null);
            }
            @Override protected void updateItem(EstadoPago estado, boolean vacia) {
                super.updateItem(estado, vacia);
                if (vacia || estado == null) {
                    setGraphic(null);
                    return;
                }
                insignia.setText(estado.toString().toUpperCase(Locale.ROOT));
                insignia.getStyleClass().removeAll(
                        "payment-status-pending",
                        "payment-status-accredited",
                        "payment-status-refunded",
                        "payment-status-cancelled");
                insignia.getStyleClass().add(switch (estado) {
                    case PENDIENTE -> "payment-status-pending";
                    case ACREDITADO -> "payment-status-accredited";
                    case REEMBOLSADO -> "payment-status-refunded";
                    case ANULADO -> "payment-status-cancelled";
                });
                setGraphic(insignia);
            }
        });
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        columnaAcreditado.setCellValueFactory(new PropertyValueFactory<>("fechaPago"));
        columnaFecha.setCellFactory(c -> celdaFecha());
        columnaAcreditado.setCellFactory(c -> celdaFecha());
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

    @FXML
    private void limpiarSeleccionPago() {
        tablaPagos.getSelectionModel().clearSelection();
        mostrarPanelDetalleVacio();
        tablaPagos.requestFocus();
    }

    private void configurarAnchosProporcionales() {
        vincularAncho(columnaReserva, 0.24);
        vincularAncho(columnaTurno, 0.17);
        vincularAncho(columnaImporte, 0.12);
        vincularAncho(columnaMetodo, 0.13);
        vincularAncho(columnaEstado, 0.14);
        vincularAncho(columnaFecha, 0.20);
    }

    private void vincularAncho(
            TableColumn<Pago, ?> columna,
            double proporcion) {
        columna.prefWidthProperty().bind(
                tablaPagos.widthProperty().subtract(14).multiply(proporcion));
        columna.setResizable(false);
        columna.setReorderable(false);
    }

    private TableCell<Pago, LocalDateTime> celdaFecha() {
        return new TableCell<>() { @Override protected void updateItem(LocalDateTime v, boolean vacia) { super.updateItem(v,vacia); setText(vacia || v == null ? "-" : v.format(FECHA_HORA)); } };
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
                ? visibles + (visibles == 1 ? " movimiento" : " movimientos")
                : visibles + " de " + total + " movimientos");
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
    @FXML private void verHoy(){seleccionarVista(null,true,botonHoy);}
    @FXML private void verTodos(){seleccionarVista(null,false,botonTodos);}
    private void seleccionarVista(EstadoPago e,boolean hoy,Button b){vistaRapida=e;soloHoy=hoy;actualizarSeleccionRapida(b);aplicarFiltros();}
    private void actualizarSeleccionRapida(Button activo){for(Button b:List.of(botonPendientes,botonAcreditados,botonReembolsados,botonAnulados,botonHoy,botonTodos))b.getStyleClass().remove("payment-view-active");if(activo!=null)activo.getStyleClass().add("payment-view-active");}
    private void actualizarContadores(){botonPendientes.setText("PENDIENTES  "+contar(EstadoPago.PENDIENTE));botonAcreditados.setText("ACREDITADOS  "+contar(EstadoPago.ACREDITADO));botonReembolsados.setText("REEMBOLSADOS  "+contar(EstadoPago.REEMBOLSADO));botonAnulados.setText("ANULADOS  "+contar(EstadoPago.ANULADO));}
    private long contar(EstadoPago e){return pagos.stream().filter(p->p.getEstado()==e).count();}

    @FXML private void limpiarFiltros(){campoBuscar.clear();filtroMetodo.getSelectionModel().clearSelection();filtroDesde.setValue(null);filtroHasta.setValue(null);checkConSaldo.setSelected(false);verTodos();}

    @FXML private void nuevoPago(){pagoSeleccionado=null;tablaPagos.getSelectionModel().clearSelection();panelDetalle.setVisible(false);panelDetalle.setManaged(false);panelFormulario.setVisible(true);panelFormulario.setManaged(true);activarModoCreacion(true);tituloFormulario.setText("Nuevo movimiento");comboReserva.getSelectionModel().clearSelection();campoImporte.clear();comboMetodo.setValue(MetodoPago.TRANSFERENCIA);comboEstadoInicial.setValue(EstadoPago.PENDIENTE);campoReferencia.clear();limpiarResumen();actualizarBotonGuardar();Platform.runLater(comboReserva::requestFocus);}
    @FXML private void cancelarFormulario(){panelFormulario.setVisible(false);panelFormulario.setManaged(false);activarModoCreacion(false);mantenerSeleccionVisible();}

    private void mostrarDetallePago(Pago p){pagoSeleccionado=p;activarModoCreacion(false);panelFormulario.setVisible(false);panelFormulario.setManaged(false);panelDetalle.setVisible(true);panelDetalle.setManaged(true);Reserva r=reservasPorId.get(p.getReservaId());detalleTitulo.setText("Movimiento #" + p.getId());
        detalleEstadoCabecera.setText(p.getEstado().toString().toUpperCase(Locale.ROOT));
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
        detalleReserva.setText(r==null?"Reserva #"+p.getReservaId():"#"+r.getId()+" · "+r.getNombreCancha());detalleCliente.setText(r==null?"Sin datos":r.getNombreCliente());detalleTurno.setText(r==null?"-":r.getFecha().format(FECHA)+" · "+r.getHoraInicio().format(HORA));detalleImporte.setText(FormateadorMoneda.pesos(p.getImporte()));detalleMetodo.setText(p.getMetodoPago().toString());detalleRegistrado.setText(formatearFechaDetalle(p.getFechaCreacion()));detalleAcreditado.setText(formatearFechaDetalle(p.getFechaPago()));detalleReferencia.setText(p.getReferencia()==null||p.getReferencia().isBlank()?"Sin referencia":p.getReferencia());boolean pendiente=p.getEstado()==EstadoPago.PENDIENTE;botonAcreditar.setDisable(!pendiente);botonAnular.setDisable(!pendiente);etiquetaAccionesPago.setText(pendiente?"":"Este movimiento ya no admite acreditación ni anulación.");etiquetaAccionesPago.setVisible(!pendiente);etiquetaAccionesPago.setManaged(!pendiente);}
    private void mostrarPanelDetalleVacio(){pagoSeleccionado=null;panelFormulario.setVisible(false);panelFormulario.setManaged(false);panelDetalle.setVisible(true);panelDetalle.setManaged(true);detalleTitulo.setText("Seleccioná un movimiento");
        detalleEstadoCabecera.setVisible(false);
        detalleEstadoCabecera.setManaged(false);
        detalleReserva.setText("-");detalleCliente.setText("-");detalleTurno.setText("-");detalleImporte.setText("-");detalleMetodo.setText("-");detalleRegistrado.setText("-");detalleAcreditado.setText("-");detalleReferencia.setText("-");etiquetaAccionesPago.setVisible(false);etiquetaAccionesPago.setManaged(false);botonAcreditar.setDisable(true);botonAnular.setDisable(true);}

    private void aplicarSolicitudFiltroDashboard(){SolicitudFiltroPagos s=Navegacion.consumirSolicitudFiltroPagos();if(s!=null&&s.filtro()==FiltroPagos.PENDIENTES_ACREDITACION){verPendientes();mostrarInfo(pagosFiltrados.size()+" pago(s) pendientes de acreditar.");}}
    private void aplicarSolicitudDeReserva(){SolicitudPagoReserva s=Navegacion.consumirSolicitudPagoReserva();if(s==null)return;Reserva r=reservasPorId.get(s.reservaId());if(r==null){mostrarError("La reserva seleccionada no está disponible para pagos.");return;}nuevoPago();comboReserva.setValue(r);actualizarResumen(r);campoImporte.requestFocus();}

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
    private void mostrarError(String m){etiquetaMensaje.setText(m==null?"Ocurrió un error.":m);etiquetaMensaje.getStyleClass().remove("mensaje-exito");if(!etiquetaMensaje.getStyleClass().contains("mensaje-error"))etiquetaMensaje.getStyleClass().add("mensaje-error");}
    private void mostrarInfo(String m){etiquetaMensaje.setText(m);etiquetaMensaje.getStyleClass().remove("mensaje-error");if(!etiquetaMensaje.getStyleClass().contains("mensaje-exito"))etiquetaMensaje.getStyleClass().add("mensaje-exito");}
    // gestion-pagos-integral-v1
    // gestion-pagos-carga-fix-v1
    // gestion-pagos-modo-creacion-v1
}
