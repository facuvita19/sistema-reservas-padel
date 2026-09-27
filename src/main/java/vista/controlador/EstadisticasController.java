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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.CierreCaja;
import negocio.EstadisticasPadel;
import negocio.EstadisticasPadel.DatoGrafico;
import servicio.EstadisticasService;
import vista.Navegacion;

public class EstadisticasController {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final EstadisticasService estadisticasService = new EstadisticasService();

	@FXML
	private DatePicker fechaDesde;

	@FXML
	private DatePicker fechaHasta;

	@FXML
	private Button botonActualizar;

	@FXML
	private ProgressIndicator indicadorCarga;

	@FXML
	private Label etiquetaEstado;

	@FXML
	private Label etiquetaTotalReservas;

	@FXML
	private Label etiquetaCompletadas;

	@FXML
	private Label etiquetaCanceladas;

	@FXML
	private Label etiquetaAusencias;

	@FXML
	private Label etiquetaIngresos;

	@FXML
	private Label etiquetaReembolsado;

	@FXML
	private Label etiquetaTicketPromedio;

	@FXML
	private Label etiquetaPagosAcreditados;

	@FXML
	private PieChart graficoEstados;
	
	@FXML
	private ScrollPane scrollEstadisticas;

	@FXML
	private BarChart<String, Number> graficoCanchas;

	@FXML
	private LineChart<String, Number> graficoIngresos;

	@FXML
	private BarChart<String, Number> graficoHorarios;

	@FXML
	private PieChart graficoMetodos;

	@FXML
	private TableView<CierreCaja> tablaCierres;

	@FXML
	private TableColumn<CierreCaja, LocalDate> columnaFechaCierre;

	@FXML
	private TableColumn<CierreCaja, BigDecimal> columnaTotalCierre;

	@FXML
	private TableColumn<CierreCaja, BigDecimal> columnaEfectivoCalculado;

	@FXML
	private TableColumn<CierreCaja, BigDecimal> columnaEfectivoDeclarado;

	@FXML
	private TableColumn<CierreCaja, BigDecimal> columnaDiferencia;

	@FXML
	private TableColumn<CierreCaja, BigDecimal> columnaReembolsado;

	@FXML
	private TableColumn<CierreCaja, Long> columnaUsuario;

	@FXML
	private TableColumn<CierreCaja, LocalDateTime> columnaFechaHoraCierre;

	@FXML
	private void initialize() {
		configurarFechas();
		configurarGraficos();
		configurarTablaCierres();
		Platform.runLater(this::actualizar);
	}

	private void configurarFechas() {
		LocalDate hasta = LocalDate.now();

		fechaHasta.setValue(hasta);
		fechaDesde.setValue(hasta.minusMonths(11).withDayOfMonth(1));

		fechaDesde.setDayCellFactory(control -> crearCeldaCalendario());

		fechaHasta.setDayCellFactory(control -> crearCeldaCalendario());
	}

	private DateCell crearCeldaCalendario() {
		return new DateCell() {
			@Override
			public void updateItem(LocalDate fecha, boolean vacia) {

				super.updateItem(fecha, vacia);

				setDisable(vacia || fecha == null || fecha.isAfter(LocalDate.now()));
			}
		};
	}

	private void configurarGraficos() {
		configurarGraficoCircular(graficoEstados);
		configurarGraficoCircular(graficoMetodos);

		graficoCanchas.setAnimated(false);
		graficoCanchas.setLegendVisible(false);

		graficoIngresos.setAnimated(false);
		graficoIngresos.setLegendVisible(false);

		graficoHorarios.setAnimated(false);
		graficoHorarios.setLegendVisible(false);

		configurarEjeCantidad(graficoCanchas);
		configurarEjeCantidad(graficoHorarios);
		configurarEjeNumerico(graficoIngresos);
	}

	private void configurarGraficoCircular(PieChart grafico) {
		grafico.setLegendVisible(true);
		grafico.setLegendSide(Side.BOTTOM);
		grafico.setLabelsVisible(false);
		grafico.setAnimated(false);
		grafico.setClockwise(true);
		grafico.setStartAngle(90);
	}
	private void volverAlInicio() {
	    Platform.runLater(() -> {
	        if (scrollEstadisticas != null) {
	            scrollEstadisticas.setVvalue(0.0);
	            scrollEstadisticas.setHvalue(0.0);
	        }
	    });
	}

	private void configurarEjeNumerico(XYChart<String, Number> grafico) {

		if (grafico.getYAxis() instanceof NumberAxis eje) {
			eje.setForceZeroInRange(true);
			eje.setMinorTickVisible(false);
		}

		if (grafico.getXAxis() instanceof CategoryAxis eje) {
			eje.setTickLabelRotation(-25);
		}
	}

	private void configurarEjeCantidad(XYChart<String, Number> grafico) {

		if (grafico.getYAxis() instanceof NumberAxis eje) {
			eje.setForceZeroInRange(true);
			eje.setMinorTickVisible(false);
			eje.setTickUnit(1);
		}
	}

	private void configurarTablaCierres() {
		columnaFechaCierre.setCellValueFactory(new PropertyValueFactory<>("fecha"));

		columnaTotalCierre.setCellValueFactory(new PropertyValueFactory<>("totalAcreditado"));

		columnaEfectivoCalculado.setCellValueFactory(new PropertyValueFactory<>("totalEfectivoCalculado"));

		columnaEfectivoDeclarado.setCellValueFactory(new PropertyValueFactory<>("efectivoDeclarado"));

		columnaDiferencia.setCellValueFactory(new PropertyValueFactory<>("diferenciaEfectivo"));

		columnaReembolsado.setCellValueFactory(new PropertyValueFactory<>("totalReembolsado"));

		columnaUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioCierreId"));

		columnaFechaHoraCierre.setCellValueFactory(new PropertyValueFactory<>("fechaCierre"));

		columnaFechaCierre.setCellFactory(columna -> crearCeldaFechaTabla());

		columnaFechaHoraCierre.setCellFactory(columna -> crearCeldaFechaHoraTabla());

		configurarCeldaMoneda(columnaTotalCierre);
		configurarCeldaMoneda(columnaEfectivoCalculado);
		configurarCeldaMoneda(columnaEfectivoDeclarado);
		configurarCeldaMoneda(columnaDiferencia);
		configurarCeldaMoneda(columnaReembolsado);
	}

	private TableCell<CierreCaja, LocalDate> crearCeldaFechaTabla() {

		return new TableCell<>() {
			@Override
			protected void updateItem(LocalDate valor, boolean vacia) {

				super.updateItem(valor, vacia);

				setText(vacia || valor == null ? null : valor.format(FORMATO_FECHA));
			}
		};
	}

	private TableCell<CierreCaja, LocalDateTime> crearCeldaFechaHoraTabla() {

		return new TableCell<>() {
			@Override
			protected void updateItem(LocalDateTime valor, boolean vacia) {

				super.updateItem(valor, vacia);

				setText(vacia || valor == null ? null : valor.format(FORMATO_FECHA_HORA));
			}
		};
	}

	private void configurarCeldaMoneda(TableColumn<CierreCaja, BigDecimal> columna) {

		columna.setCellFactory(control -> new TableCell<>() {
			@Override
			protected void updateItem(BigDecimal valor, boolean vacia) {

				super.updateItem(valor, vacia);

				setText(vacia || valor == null ? null : formatearMoneda(valor));
			}
		});
	}

	@FXML
	private void actualizar() {
		LocalDate desde = fechaDesde.getValue();
		LocalDate hasta = fechaHasta.getValue();

		if (desde == null || hasta == null) {
			mostrarError("Seleccioná las dos fechas del período.");
			return;
		}

		if (desde.isAfter(LocalDate.now()) || hasta.isAfter(LocalDate.now())) {

			mostrarError("El período no puede incluir fechas futuras.");
			return;
		}

		if (hasta.isBefore(desde)) {
			mostrarError("La fecha final no puede ser anterior a la inicial.");
			return;
		}

		cambiarCarga(true, "Consultando estadísticas...");

		Task<EstadisticasPadel> tarea = new Task<>() {
			@Override
			protected EstadisticasPadel call() {
				return estadisticasService.obtener(desde, hasta);
			}
		};

		tarea.setOnSucceeded(evento -> {
		    mostrarEstadisticas(tarea.getValue());
		    cambiarCarga(
		            false,
		            "Estadísticas actualizadas."
		    );
		    volverAlInicio();
		});

		tarea.setOnFailed(evento -> {
			Throwable error = tarea.getException();

			if (error != null) {
				error.printStackTrace();
			}

			mostrarError(error == null || error.getMessage() == null ? "No se pudieron cargar las estadísticas."
					: error.getMessage());
		});

		Thread hilo = new Thread(tarea, "estadisticas-padel-reservas");
		hilo.setDaemon(true);
		hilo.start();
	}

	private void mostrarEstadisticas(EstadisticasPadel datos) {
		etiquetaTotalReservas.setText(String.valueOf(datos.getTotalReservas()));

		etiquetaCompletadas.setText(String.valueOf(datos.getReservasCompletadas()));

		etiquetaCanceladas
				.setText(datos.getReservasCanceladas() + " · " + datos.getTasaCancelacion().toPlainString() + " %");

		etiquetaAusencias.setText(datos.getReservasAusentes() + " · " + datos.getTasaAusencia().toPlainString() + " %");

		etiquetaIngresos.setText(formatearMoneda(datos.getIngresosAcreditados()));

		etiquetaReembolsado.setText(formatearMoneda(datos.getTotalReembolsado()));

		etiquetaTicketPromedio.setText(formatearMoneda(datos.getTicketPromedio()));

		etiquetaPagosAcreditados.setText(String.valueOf(datos.getCantidadPagosAcreditados()));

		cargarGraficoCircular(graficoEstados, datos.getReservasPorEstado());

		cargarGraficoCircular(graficoMetodos, datos.getIngresosPorMetodo());

		cargarGraficoBarras(graficoCanchas, datos.getReservasPorCancha());

		cargarGraficoBarras(graficoHorarios, datos.getHorariosMasSolicitados());

		cargarGraficoLinea(datos.getIngresosPorMes());

		tablaCierres.getItems().setAll(datos.getCierresCaja());
	}

	private void cargarGraficoCircular(PieChart grafico, List<DatoGrafico> datos) {

		grafico.getData().clear();

		for (DatoGrafico dato : datos) {
			grafico.getData().add(new PieChart.Data(formatearEtiqueta(dato.etiqueta()), dato.valor().doubleValue()));
		}
	}

	private void cargarGraficoBarras(BarChart<String, Number> grafico, List<DatoGrafico> datos) {

		grafico.getData().clear();

		XYChart.Series<String, Number> serie = new XYChart.Series<>();

		for (DatoGrafico dato : datos) {
			serie.getData().add(new XYChart.Data<>(formatearEtiqueta(dato.etiqueta()), dato.valor()));
		}

		grafico.getData().add(serie);
	}

	private void cargarGraficoLinea(List<DatoGrafico> datos) {
		graficoIngresos.getData().clear();

		XYChart.Series<String, Number> serie = new XYChart.Series<>();

		for (DatoGrafico dato : datos) {
			serie.getData().add(new XYChart.Data<>(dato.etiqueta(), dato.valor()));
		}

		graficoIngresos.getData().add(serie);
	}

	private String formatearEtiqueta(String valor) {
		if (valor == null || valor.isBlank()) {
			return "Sin datos";
		}

		String texto = valor.replace('_', ' ').toLowerCase(Locale.ROOT);

		return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
	}

	private String formatearMoneda(BigDecimal valor) {
		BigDecimal importe = valor == null ? BigDecimal.ZERO : valor.setScale(2, RoundingMode.HALF_UP);

		return NumberFormat.getCurrencyInstance(new Locale("es", "AR")).format(importe);
	}

	private void cambiarCarga(boolean cargando, String mensaje) {

		indicadorCarga.setVisible(cargando);
		botonActualizar.setDisable(cargando);
		etiquetaEstado.setText(mensaje);

		etiquetaEstado.getStyleClass().removeAll("stats-status-ok", "stats-status-error");

		if (!cargando) {
			etiquetaEstado.getStyleClass().add("stats-status-ok");
		}
	}

	private void mostrarError(String mensaje) {
		indicadorCarga.setVisible(false);
		botonActualizar.setDisable(false);
		etiquetaEstado.setText(mensaje == null ? "No se pudieron cargar las estadísticas." : mensaje);

		etiquetaEstado.getStyleClass().removeAll("stats-status-ok", "stats-status-error");

		etiquetaEstado.getStyleClass().add("stats-status-error");
	}

	@FXML
	private void volver() {
		Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
	}
}
