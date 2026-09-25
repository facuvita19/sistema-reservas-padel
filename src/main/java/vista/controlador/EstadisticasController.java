package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import negocio.EstadisticasPadel;
import negocio.EstadisticasPadel.DatoGrafico;
import servicio.EstadisticasService;
import vista.Navegacion;

public class EstadisticasController {

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
	private Label etiquetaCancelacion;
	@FXML
	private Label etiquetaIngresos;
	@FXML
	private Label etiquetaTicketPromedio;

	@FXML
	private PieChart graficoEstados;
	@FXML
	private BarChart<String, Number> graficoCanchas;
	@FXML
	private LineChart<String, Number> graficoIngresos;
	@FXML
	private BarChart<String, Number> graficoHorarios;

	@FXML
	private void initialize() {
		configurarFechas();
		configurarGraficos();
		Platform.runLater(this::actualizar);
	}

	private void configurarFechas() {
		LocalDate hasta = LocalDate.now();
		fechaHasta.setValue(hasta);
		fechaDesde.setValue(hasta.minusMonths(11).withDayOfMonth(1));

		fechaHasta.setDayCellFactory(control -> new javafx.scene.control.DateCell() {
			@Override
			public void updateItem(LocalDate fecha, boolean vacia) {
				super.updateItem(fecha, vacia);
				setDisable(vacia || fecha.isAfter(LocalDate.now()));
			}
		});
	}

	private void configurarGraficos() {
		graficoEstados.setLegendVisible(true);
		graficoEstados.setLegendSide(Side.RIGHT);
		graficoEstados.setLabelsVisible(false);
		graficoEstados.setAnimated(false);
		graficoEstados.setClockwise(true);
		graficoEstados.setStartAngle(90);

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

	private void configurarEjeNumerico(XYChart<String, Number> grafico) {
		if (grafico.getYAxis() instanceof NumberAxis eje) {
			eje.setForceZeroInRange(true);
			eje.setMinorTickVisible(false);
		}
		if (grafico.getXAxis() instanceof CategoryAxis eje) {
			eje.setTickLabelRotation(0);
		}
	}

	private void configurarEjeCantidad(XYChart<String, Number> grafico) {

		if (grafico.getYAxis() instanceof NumberAxis eje) {
			eje.setForceZeroInRange(true);
			eje.setMinorTickVisible(false);
			eje.setTickUnit(1);
		}
	}

	@FXML
	private void actualizar() {
		LocalDate desde = fechaDesde.getValue();
		LocalDate hasta = fechaHasta.getValue();

		if (desde == null || hasta == null) {
			mostrarError("Seleccioná las dos fechas del período.");
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
			cambiarCarga(false, "Estadísticas actualizadas.");
		});

		tarea.setOnFailed(evento -> {
			Throwable error = tarea.getException();
			if (error != null) {
				error.printStackTrace();
			}
			cambiarCarga(false, error == null || error.getMessage() == null ? "No se pudieron cargar las estadísticas."
					: error.getMessage());
			etiquetaEstado.getStyleClass().add("stats-status-error");
		});

		Thread hilo = new Thread(tarea, "estadisticas-padel-reservas");
		hilo.setDaemon(true);
		hilo.start();
	}

	private void mostrarEstadisticas(EstadisticasPadel datos) {
		etiquetaTotalReservas.setText(String.valueOf(datos.getTotalReservas()));
		etiquetaCompletadas.setText(String.valueOf(datos.getReservasCompletadas()));
		etiquetaCancelacion.setText(datos.getTasaCancelacion().toPlainString() + " %");
		etiquetaIngresos.setText(formatearMoneda(datos.getIngresosAcreditados()));
		etiquetaTicketPromedio.setText(formatearMoneda(datos.getTicketPromedio()));

		cargarCircular(datos.getReservasPorEstado());
		cargarBarras(graficoCanchas, "Reservas", datos.getReservasPorCancha());
		cargarLinea(datos.getIngresosPorMes());
		cargarBarras(graficoHorarios, "Reservas", datos.getHorariosMasSolicitados());
	}

	private void cargarCircular(List<DatoGrafico> datos) {
		graficoEstados.getData().clear();
		for (DatoGrafico dato : datos) {
			graficoEstados.getData()
					.add(new PieChart.Data(formatearEtiqueta(dato.etiqueta()), dato.valor().doubleValue()));
		}
	}

	private void cargarBarras(BarChart<String, Number> grafico, String nombreSerie, List<DatoGrafico> datos) {

		grafico.getData().clear();
		XYChart.Series<String, Number> serie = new XYChart.Series<>();
		serie.setName(nombreSerie);

		for (DatoGrafico dato : datos) {
			serie.getData().add(new XYChart.Data<>(formatearEtiqueta(dato.etiqueta()), dato.valor()));
		}
		grafico.getData().add(serie);
	}

	private void cargarLinea(List<DatoGrafico> datos) {
		graficoIngresos.getData().clear();
		XYChart.Series<String, Number> serie = new XYChart.Series<>();
		serie.setName("Ingresos acreditados");

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
		cambiarCarga(false, mensaje);
		etiquetaEstado.getStyleClass().remove("stats-status-ok");
		etiquetaEstado.getStyleClass().add("stats-status-error");
	}

	@FXML
	private void volver() {
		Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
	}
}
