package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.CierreCaja;
import negocio.EstadisticasPadel;
import negocio.EstadisticasPadel.DatoGrafico;
import servicio.EstadisticasService;
import vista.Navegacion;

public class EstadisticasController {
    private static final DateTimeFormatter FECHA=DateTimeFormatter.ofPattern("dd/MM/yyyy"), FECHA_HORA=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final EstadisticasService service=new EstadisticasService();
    @FXML private DatePicker fechaDesde,fechaHasta; @FXML private Button botonActualizar; @FXML private ProgressIndicator indicadorCarga; @FXML private Label etiquetaEstado;
    @FXML private Label etiquetaTotalReservas,etiquetaCompletadas,etiquetaCanceladas,etiquetaAusencias,etiquetaIngresos,etiquetaReembolsado,etiquetaTicketPromedio,etiquetaPagosAcreditados,etiquetaOcupacion,etiquetaVariacionReservas,etiquetaVariacionIngresos;
    @FXML private PieChart graficoEstados,graficoMetodos; @FXML private BarChart<String,Number> graficoCanchas,graficoHorarios,graficoDias,graficoClientes; @FXML private LineChart<String,Number> graficoIngresos; @FXML private ScrollPane scrollEstadisticas;
    @FXML private TableView<CierreCaja> tablaCierres; @FXML private TableColumn<CierreCaja,LocalDate> columnaFechaCierre; @FXML private TableColumn<CierreCaja,BigDecimal> columnaTotalCierre,columnaEfectivoCalculado,columnaEfectivoDeclarado,columnaDiferencia,columnaReembolsado; @FXML private TableColumn<CierreCaja,Long> columnaUsuario; @FXML private TableColumn<CierreCaja,LocalDateTime> columnaFechaHoraCierre;
    @FXML private void initialize(){LocalDate h=LocalDate.now();fechaHasta.setValue(h);fechaDesde.setValue(h.minusMonths(11).withDayOfMonth(1));fechaDesde.setDayCellFactory(x->celda());fechaHasta.setDayCellFactory(x->celda());configurarGraficos();configurarTabla();Platform.runLater(this::actualizar);}
    private DateCell celda(){return new DateCell(){@Override public void updateItem(LocalDate f,boolean v){super.updateItem(f,v);setDisable(v||f==null||f.isAfter(LocalDate.now()));}};}
    private void configurarGraficos(){for(PieChart p:List.of(graficoEstados,graficoMetodos)){p.setLegendVisible(true);p.setLegendSide(Side.BOTTOM);p.setLabelsVisible(false);p.setAnimated(false);} for(XYChart<String,Number> g:List.of(graficoCanchas,graficoHorarios,graficoDias,graficoClientes,graficoIngresos)){g.setAnimated(false);g.setLegendVisible(false);if(g.getYAxis() instanceof NumberAxis n){n.setForceZeroInRange(true);n.setMinorTickVisible(false);}}}
    private void configurarTabla(){columnaFechaCierre.setCellValueFactory(new PropertyValueFactory<>("fecha"));columnaTotalCierre.setCellValueFactory(new PropertyValueFactory<>("totalAcreditado"));columnaEfectivoCalculado.setCellValueFactory(new PropertyValueFactory<>("totalEfectivoCalculado"));columnaEfectivoDeclarado.setCellValueFactory(new PropertyValueFactory<>("efectivoDeclarado"));columnaDiferencia.setCellValueFactory(new PropertyValueFactory<>("diferenciaEfectivo"));columnaReembolsado.setCellValueFactory(new PropertyValueFactory<>("totalReembolsado"));columnaUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioCierreId"));columnaFechaHoraCierre.setCellValueFactory(new PropertyValueFactory<>("fechaCierre"));columnaFechaCierre.setCellFactory(x->new TableCell<>(){@Override protected void updateItem(LocalDate v,boolean e){super.updateItem(v,e);setText(e||v==null?null:v.format(FECHA));}});columnaFechaHoraCierre.setCellFactory(x->new TableCell<>(){@Override protected void updateItem(LocalDateTime v,boolean e){super.updateItem(v,e);setText(e||v==null?null:v.format(FECHA_HORA));}});for(TableColumn<CierreCaja,BigDecimal> c:List.of(columnaTotalCierre,columnaEfectivoCalculado,columnaEfectivoDeclarado,columnaDiferencia,columnaReembolsado))c.setCellFactory(x->new TableCell<>(){@Override protected void updateItem(BigDecimal v,boolean e){super.updateItem(v,e);setText(e||v==null?null:moneda(v));}});}
    @FXML private void actualizar(){LocalDate d=fechaDesde.getValue(),h=fechaHasta.getValue();if(d==null||h==null){error("Seleccioná las dos fechas del período.");return;}if(h.isBefore(d)){error("La fecha final no puede ser anterior a la inicial.");return;}carga(true,"Consultando estadísticas...");Task<EstadisticasPadel> t=new Task<>(){@Override protected EstadisticasPadel call(){return service.obtener(d,h);}};t.setOnSucceeded(e->{mostrar(t.getValue());carga(false,"Estadísticas actualizadas. Comparación realizada contra el período anterior equivalente.");Platform.runLater(()->scrollEstadisticas.setVvalue(0));});t.setOnFailed(e->{Throwable x=t.getException();if(x!=null)x.printStackTrace();error(x==null?"No se pudieron cargar las estadísticas.":x.getMessage());});Thread th=new Thread(t,"estadisticas-padel");th.setDaemon(true);th.start();}
    private void mostrar(EstadisticasPadel d){etiquetaTotalReservas.setText(String.valueOf(d.getTotalReservas()));etiquetaCompletadas.setText(String.valueOf(d.getReservasCompletadas()));etiquetaCanceladas.setText(d.getReservasCanceladas()+" · "+d.getTasaCancelacion()+" %");etiquetaAusencias.setText(d.getReservasAusentes()+" · "+d.getTasaAusencia()+" %");etiquetaIngresos.setText(moneda(d.getIngresosAcreditados()));etiquetaReembolsado.setText(moneda(d.getTotalReembolsado()));etiquetaTicketPromedio.setText(moneda(d.getTicketPromedio()));etiquetaPagosAcreditados.setText(String.valueOf(d.getCantidadPagosAcreditados()));etiquetaOcupacion.setText(d.getPorcentajeOcupacion()+" %");variacion(etiquetaVariacionReservas,d.getVariacionReservas());variacion(etiquetaVariacionIngresos,d.getVariacionIngresos());pie(graficoEstados,d.getReservasPorEstado());pie(graficoMetodos,d.getIngresosPorMetodo());barras(graficoCanchas,d.getReservasPorCancha());barras(graficoHorarios,d.getHorariosMasSolicitados());barras(graficoDias,d.getDiasMasSolicitados());barras(graficoClientes,d.getClientesFrecuentes());linea(d.getIngresosPorMes());tablaCierres.getItems().setAll(d.getCierresCaja());}
    private void variacion(Label l,BigDecimal v){l.setText((v.signum()>0?"+":"")+v+" % vs. período anterior");l.getStyleClass().removeAll("stats-trend-up","stats-trend-down","stats-trend-flat");l.getStyleClass().add(v.signum()>0?"stats-trend-up":v.signum()<0?"stats-trend-down":"stats-trend-flat");}
    private void pie(PieChart g,List<DatoGrafico> ds){g.getData().clear();for(DatoGrafico d:ds)g.getData().add(new PieChart.Data(etiqueta(d.etiqueta()),d.valor().doubleValue()));}
    private void barras(BarChart<String,Number> g,List<DatoGrafico> ds){g.getData().clear();XYChart.Series<String,Number>s=new XYChart.Series<>();for(DatoGrafico d:ds)s.getData().add(new XYChart.Data<>(etiqueta(d.etiqueta()),d.valor()));g.getData().add(s);}
    private void linea(List<DatoGrafico> ds){graficoIngresos.getData().clear();XYChart.Series<String,Number>s=new XYChart.Series<>();for(DatoGrafico d:ds)s.getData().add(new XYChart.Data<>(d.etiqueta(),d.valor()));graficoIngresos.getData().add(s);}
    private String etiqueta(String v){if(v==null||v.isBlank())return "Sin datos";String t=v.replace('_',' ').toLowerCase(Locale.ROOT);return Character.toUpperCase(t.charAt(0))+t.substring(1);}
    private String moneda(BigDecimal v){return NumberFormat.getCurrencyInstance(new Locale("es","AR")).format(v==null?BigDecimal.ZERO:v.setScale(2,RoundingMode.HALF_UP));}
    private void carga(boolean c,String m){indicadorCarga.setVisible(c);botonActualizar.setDisable(c);etiquetaEstado.setText(m);etiquetaEstado.getStyleClass().removeAll("stats-status-ok","stats-status-error");if(!c)etiquetaEstado.getStyleClass().add("stats-status-ok");}
    private void error(String m){indicadorCarga.setVisible(false);botonActualizar.setDisable(false);etiquetaEstado.setText(m==null?"No se pudieron cargar las estadísticas.":m);etiquetaEstado.getStyleClass().removeAll("stats-status-ok","stats-status-error");etiquetaEstado.getStyleClass().add("stats-status-error");}
    @FXML private void volver(){Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());}
}
