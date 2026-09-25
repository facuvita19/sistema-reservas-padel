package vista.controlador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import negocio.AgendaDiaria;
import negocio.Cancha;
import negocio.CeldaAgenda;
import negocio.EstadoCeldaAgenda;
import negocio.FilaAgenda;
import servicio.AgendaService;
import vista.Navegacion;

public class AgendaController {

	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter FORMATO_FECHA_LARGA = DateTimeFormatter
			.ofPattern("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "AR"));

	private final AgendaService agendaService = new AgendaService();

	@FXML
	private DatePicker selectorFecha;
	@FXML
	private Label etiquetaFecha;
	@FXML
	private Label etiquetaEstado;
	@FXML
	private VBox contenedorSinCanchas;
	@FXML
	private ProgressIndicator indicadorCarga;
	@FXML
	private Button botonActualizar;
	@FXML
	private ScrollPane scrollAgenda;
	@FXML
	private GridPane grillaAgenda;

	@FXML
	private void initialize() {
		selectorFecha.setValue(LocalDate.now());
		selectorFecha.valueProperty().addListener((obs, anterior, actual) -> {
			if (actual != null && !actual.equals(anterior)) {
				cargarAgenda();
			}
		});
		Platform.runLater(this::cargarAgenda);
	}

	@FXML
	private void diaAnterior() {
		selectorFecha.setValue(selectorFecha.getValue().minusDays(1));
	}

	@FXML
	private void diaSiguiente() {
		selectorFecha.setValue(selectorFecha.getValue().plusDays(1));
	}

	@FXML
	private void irHoy() {
		LocalDate hoy = LocalDate.now();
		if (hoy.equals(selectorFecha.getValue())) {
			cargarAgenda();
		} else {
			selectorFecha.setValue(hoy);
		}
	}

	@FXML
	private void cargarAgenda() {
		LocalDate fecha = selectorFecha.getValue();
		if (fecha == null) {
			mostrarError("Seleccioná una fecha.");
			return;
		}

		cambiarCarga(true, "Cargando agenda...");

		Task<AgendaDiaria> tarea = new Task<>() {
			@Override
			protected AgendaDiaria call() {
				return agendaService.obtener(fecha);
			}
		};

		tarea.setOnSucceeded(evento -> {
			AgendaDiaria agenda = tarea.getValue();
			construirGrilla(agenda);
			etiquetaFecha.setText(capitalizar(agenda.getFecha().format(FORMATO_FECHA_LARGA)));
			cambiarCarga(false, "Agenda actualizada.");
		});

		tarea.setOnFailed(evento -> {
			Throwable error = tarea.getException();
			if (error != null) {
				error.printStackTrace();
			}
			mostrarError(
					error == null || error.getMessage() == null ? "No se pudo cargar la agenda." : error.getMessage());
			cambiarCarga(false, etiquetaEstado.getText());
		});

		Thread hilo = new Thread(tarea, "agenda-diaria-padel");
		hilo.setDaemon(true);
		hilo.start();
	}

	private void construirGrilla(AgendaDiaria agenda) {
		grillaAgenda.getChildren().clear();
		grillaAgenda.getColumnConstraints().clear();
		grillaAgenda.getRowConstraints().clear();

		boolean vacia = agenda.getCanchas().isEmpty();

		contenedorSinCanchas.setVisible(vacia);
		contenedorSinCanchas.setManaged(vacia);

		scrollAgenda.setVisible(!vacia);
		scrollAgenda.setManaged(!vacia);

		if (vacia) {
			return;
		}

		agregarRestriccionesColumnas(agenda);
		agregarEncabezadoHora();

		for (int indice = 0; indice < agenda.getCanchas().size(); indice++) {
			agregarEncabezadoCancha(agenda.getCanchas().get(indice), indice + 1);
		}

		agregarFilasAgenda(agenda);
	}

	private void agregarFilasAgenda(AgendaDiaria agenda) {
		Set<String> posicionesOcupadas = new HashSet<>();

		for (int filaIndice = 0; filaIndice < agenda.getFilas().size(); filaIndice++) {

			FilaAgenda fila = agenda.getFilas().get(filaIndice);
			int filaVisual = filaIndice + 1;

			agregarHora(fila.getHoraInicio(), filaVisual);

			for (int columna = 0; columna < fila.getCeldas().size(); columna++) {

				String posicion = clavePosicion(filaIndice, columna);

				if (posicionesOcupadas.contains(posicion)) {
					continue;
				}

				CeldaAgenda celda = fila.getCeldas().get(columna);

				int cantidadFilas = calcularCantidadFilas(agenda, filaIndice, columna, celda);

				Node nodo = crearCelda(celda, cantidadFilas);

				grillaAgenda.add(nodo, columna + 1, filaVisual);

				if (cantidadFilas > 1) {
					GridPane.setRowSpan(nodo, cantidadFilas);

					marcarPosicionesOcupadas(posicionesOcupadas, filaIndice, columna, cantidadFilas);
				}
			}
		}
	}

	private int calcularCantidadFilas(AgendaDiaria agenda, int filaInicial, int columna, CeldaAgenda celdaInicial) {

		if (celdaInicial.getEstado() == EstadoCeldaAgenda.DISPONIBLE) {

			int minutos = (int) java.time.Duration.between(celdaInicial.getHoraInicio(), celdaInicial.getHoraFin())
					.toMinutes();

			return limitarCantidadFilas(agenda, filaInicial, minutos / 30);
		}

		if (celdaInicial.getReservaId() != null) {
			return contarReservaContinua(agenda, filaInicial, columna, celdaInicial.getReservaId());
		}

		if (celdaInicial.getBloqueoId() != null) {
			return contarBloqueoContinuo(agenda, filaInicial, columna, celdaInicial.getBloqueoId());
		}

		return 1;
	}

	private int contarReservaContinua(AgendaDiaria agenda, int filaInicial, int columna, Long reservaId) {

		int cantidad = 0;

		for (int fila = filaInicial; fila < agenda.getFilas().size(); fila++) {

			CeldaAgenda siguiente = obtenerCelda(agenda, fila, columna);

			if (!reservaId.equals(siguiente.getReservaId())) {
				break;
			}

			cantidad++;
		}

		return Math.max(1, cantidad);
	}

	private int contarBloqueoContinuo(AgendaDiaria agenda, int filaInicial, int columna, Long bloqueoId) {

		int cantidad = 0;

		for (int fila = filaInicial; fila < agenda.getFilas().size(); fila++) {

			CeldaAgenda siguiente = obtenerCelda(agenda, fila, columna);

			if (!bloqueoId.equals(siguiente.getBloqueoId())) {
				break;
			}

			cantidad++;
		}

		return Math.max(1, cantidad);
	}

	private CeldaAgenda obtenerCelda(AgendaDiaria agenda, int fila, int columna) {

		return agenda.getFilas().get(fila).getCeldas().get(columna);
	}

	private int limitarCantidadFilas(AgendaDiaria agenda, int filaInicial, int cantidadSolicitada) {

		int filasRestantes = agenda.getFilas().size() - filaInicial;

		return Math.max(1, Math.min(cantidadSolicitada, filasRestantes));
	}

	private void marcarPosicionesOcupadas(Set<String> posicionesOcupadas, int filaInicial, int columna,
			int cantidadFilas) {

		for (int desplazamiento = 1; desplazamiento < cantidadFilas; desplazamiento++) {

			posicionesOcupadas.add(clavePosicion(filaInicial + desplazamiento, columna));
		}
	}

	private String clavePosicion(int fila, int columna) {

		return fila + ":" + columna;
	}

	private void agregarRestriccionesColumnas(AgendaDiaria agenda) {
		ColumnConstraints hora = new ColumnConstraints();
		hora.setMinWidth(78);
		hora.setPrefWidth(86);
		hora.setMaxWidth(96);
		grillaAgenda.getColumnConstraints().add(hora);

		for (int i = 0; i < agenda.getCanchas().size(); i++) {
			ColumnConstraints cancha = new ColumnConstraints();
			cancha.setMinWidth(150);
			cancha.setPrefWidth(190);
			cancha.setHgrow(Priority.ALWAYS);
			grillaAgenda.getColumnConstraints().add(cancha);
		}
	}

	private void agregarEncabezadoHora() {
		Label etiqueta = new Label("HORA");
		etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		etiqueta.setAlignment(Pos.CENTER);
		etiqueta.getStyleClass().addAll("agenda-header", "agenda-time-header");
		grillaAgenda.add(etiqueta, 0, 0);
	}

	private void agregarEncabezadoCancha(Cancha cancha, int columna) {
		Label etiqueta = new Label(cancha.getNombre());
		etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		etiqueta.setAlignment(Pos.CENTER);
		etiqueta.getStyleClass().add("agenda-header");
		Tooltip.install(etiqueta,
				new Tooltip(cancha.getNombre() + "\n" + cancha.getHoraApertura().format(FORMATO_HORA) + " - "
						+ cancha.getHoraCierre().format(FORMATO_HORA) + "\nReservas de " + cancha.getDuracionReserva()
						+ " minutos"));
		grillaAgenda.add(etiqueta, columna, 0);
	}

	private void agregarHora(LocalTime hora, int fila) {
		Label etiqueta = new Label(hora.format(FORMATO_HORA));
		etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		etiqueta.setAlignment(Pos.TOP_CENTER);
		etiqueta.getStyleClass().add("agenda-time-cell");
		grillaAgenda.add(etiqueta, 0, fila);
	}

	private Node crearCelda(CeldaAgenda celda, int cantidadFilas) {

		LocalTime horaFinalVisual = celda.getHoraInicio().plusMinutes(cantidadFilas * 30L);

		Label etiqueta = new Label(textoCelda(celda, horaFinalVisual));

		etiqueta.setWrapText(true);
		etiqueta.setAlignment(Pos.CENTER_LEFT);
		etiqueta.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		etiqueta.setMinHeight(cantidadFilas * 42.0);

		etiqueta.getStyleClass().addAll("agenda-cell", claseEstado(celda.getEstado()));

		String tooltip = celda.getNombreCancha() + "\n" + celda.getHoraInicio().format(FORMATO_HORA) + " - "
				+ horaFinalVisual.format(FORMATO_HORA) + "\n" + formatoEstado(celda.getEstado())
				+ (celda.getDetalle() == null ? "" : "\n" + celda.getDetalle());

		Tooltip.install(etiqueta, new Tooltip(tooltip));

		etiqueta.setOnMouseClicked(evento -> manejarDobleClic(evento, celda));

		return etiqueta;
	}

	private String textoCelda(CeldaAgenda celda, LocalTime horaFinalVisual) {

		String horario = celda.getHoraInicio().format(FORMATO_HORA) + " - " + horaFinalVisual.format(FORMATO_HORA);

		return switch (celda.getEstado()) {
		case DISPONIBLE -> horario + "\nDisponible";

		case BLOQUEADA -> horario + "\n" + (celda.getDetalle() == null ? "Bloqueada" : celda.getDetalle());

		case NO_DISPONIBLE, PASADA -> "";

		default ->
			horario + "\n" + (celda.getDetalle() == null ? formatoEstado(celda.getEstado()) : celda.getDetalle());
		};
	}

	private void manejarDobleClic(MouseEvent evento, CeldaAgenda celda) {

		if (evento.getClickCount() != 2) {
			return;
		}

		if (celda.getEstado() == EstadoCeldaAgenda.DISPONIBLE) {
			Navegacion.mostrarNuevaReservaDesdeAgenda(selectorFecha.getValue(), celda.getCanchaId(),
					celda.getHoraInicio());
			return;
		}

		if (celda.getReservaId() != null) {
			Navegacion.mostrarReservaDesdeAgenda(celda.getReservaId());
			return;
		}

		if (celda.getEstado() == EstadoCeldaAgenda.BLOQUEADA) {
			Navegacion.mostrarBloqueos();
		}
	}

	private String claseEstado(EstadoCeldaAgenda estado) {
		return "agenda-" + estado.name().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	private String formatoEstado(EstadoCeldaAgenda estado) {
		String texto = estado.name().replace('_', ' ').toLowerCase(Locale.ROOT);
		return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
	}

	private void cambiarCarga(boolean cargando, String mensaje) {
		indicadorCarga.setVisible(cargando);
		botonActualizar.setDisable(cargando);
		etiquetaEstado.setText(mensaje);
		etiquetaEstado.getStyleClass().removeAll("agenda-status-ok", "agenda-status-error");
		if (!cargando) {
			etiquetaEstado.getStyleClass().add("agenda-status-ok");
		}
	}

	private void mostrarError(String mensaje) {
		etiquetaEstado.setText(mensaje);
		etiquetaEstado.getStyleClass().remove("agenda-status-ok");
		if (!etiquetaEstado.getStyleClass().contains("agenda-status-error")) {
			etiquetaEstado.getStyleClass().add("agenda-status-error");
		}
	}

	private String capitalizar(String texto) {
		if (texto == null || texto.isBlank()) {
			return "";
		}
		return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
	}

	@FXML
	private void volver() {
		Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
	}
}
