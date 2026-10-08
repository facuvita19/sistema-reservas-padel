package vista.controlador;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.SVGPath;
import negocio.Cancha;
import negocio.TipoCancha;
import servicio.CanchaService;
import util.FormateadorMoneda;
import vista.Navegacion;
import vista.Dialogos;

public class CanchasController {

	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

	private final CanchaService canchaService = new CanchaService();
	private final ObservableList<Cancha> canchas = FXCollections.observableArrayList();

	private FilteredList<Cancha> canchasFiltradas;
	private Cancha canchaSeleccionada;
	private String estadoFormularioInicial = "";
	private boolean actualizandoFormulario;

	@FXML
	private TextField campoBuscar;
	@FXML private ScrollPane scrollDetalleCancha;
	@FXML private Label etiquetaResultados;
	@FXML private Label etiquetaAyudaListado;
	@FXML private Label etiquetaModoCancha;
	@FXML
	private TableView<Cancha> tablaCanchas;
	@FXML
	private TableColumn<Cancha, Integer> columnaOrden;
	@FXML
	private TableColumn<Cancha, String> columnaNombre;
	@FXML
	private TableColumn<Cancha, TipoCancha> columnaTipo;
	@FXML
	private TableColumn<Cancha, String> columnaHorario;
	@FXML
	private TableColumn<Cancha, BigDecimal> columnaPrecio;
	@FXML
	private TableColumn<Cancha, String> columnaEstado;

	@FXML
	private Label tituloFormulario;
	@FXML
	private Label subtituloFormulario;
	@FXML
	private Label insigniaEstadoCancha;
	@FXML
	private Label etiquetaMensaje;
	@FXML
	private TextField campoNombre;
	@FXML
	private ComboBox<TipoCancha> comboTipo;
	@FXML
	private TextField campoSuperficie;
	@FXML
	private CheckBox checkIluminacion;
	@FXML
	private ComboBox<String> comboApertura;
	@FXML
	private ComboBox<String> comboCierre;
	@FXML
	private ComboBox<Integer> comboDuracion;
	@FXML
	private TextField campoPrecio;
	@FXML
	private TextArea campoDescripcion;

	@FXML
	private CheckBox checkLunes;
	@FXML
	private CheckBox checkMartes;
	@FXML
	private CheckBox checkMiercoles;
	@FXML
	private CheckBox checkJueves;
	@FXML
	private CheckBox checkViernes;
	@FXML
	private CheckBox checkSabado;
	@FXML
	private CheckBox checkDomingo;

	@FXML
	private Button botonGuardar;
	@FXML
	private Button botonDesactivar;
	@FXML private SVGPath iconoAccionEstado;
	@FXML
	private Button botonEliminarDefinitivamente;
	@FXML
	private Button botonSubir;
	@FXML
	private Button botonBajar;

	@FXML
	private void initialize() {
		configurarCombos();
		configurarTabla();
		configurarBusqueda();
		configurarCambiosFormulario();
		cargarCanchas();
		nuevo();
	}

	private void configurarCombos() {
		comboTipo.setItems(FXCollections.observableArrayList(TipoCancha.values()));
		comboDuracion.setItems(FXCollections.observableArrayList(30, 60, 90, 120, 150, 180));

		ObservableList<String> horarios = FXCollections.observableArrayList();

		int minutosIniciales = 6 * 60;
		int minutosFinales = 23 * 60 + 30;

		for (int minutos = minutosIniciales; minutos <= minutosFinales; minutos += 30) {

			LocalTime hora = LocalTime.of(minutos / 60, minutos % 60);

			horarios.add(hora.format(FORMATO_HORA));
		}

		comboApertura.setItems(horarios);
		comboCierre.setItems(FXCollections.observableArrayList(horarios));
	}

	private void configurarTabla() {
		tablaCanchas.setFixedCellSize(61);
		columnaOrden.setCellValueFactory(
		                new PropertyValueFactory<>("ordenVisual"));
		columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
		columnaTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
		columnaHorario.setCellValueFactory(datos -> new javafx.beans.property.SimpleStringProperty(
				datos.getValue().getHoraApertura().format(FORMATO_HORA) + " - "
						+ datos.getValue().getHoraCierre().format(FORMATO_HORA)));
		columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
		columnaPrecio.setCellFactory(columna -> new javafx.scene.control.TableCell<>() {
                        {
                                setAlignment(javafx.geometry.Pos.CENTER);
                                getStyleClass().add("court-price-cell");
                        }
                        @Override
                        protected void updateItem(BigDecimal importe, boolean vacia) {
                                super.updateItem(importe, vacia);
                                setText(vacia || importe == null ? null : FormateadorMoneda.pesos(importe));
                        }
                });
                columnaEstado.setCellValueFactory(datos -> new javafx.beans.property.SimpleStringProperty(
                                datos.getValue().isActivo() ? "Activa" : "Inactiva"));
                columnaEstado.setCellFactory(columna -> new javafx.scene.control.TableCell<>() {
                        private final Label insignia = new Label();
                        { insignia.getStyleClass().add("court-status-badge"); setText(null); }
                        @Override
                        protected void updateItem(String estado, boolean vacia) {
                                super.updateItem(estado, vacia);
                                if (vacia || estado == null) { setGraphic(null); return; }
                                boolean activa = "Activa".equals(estado);
                                insignia.setText(estado.toUpperCase(Locale.ROOT));
                                insignia.getStyleClass().removeAll("court-status-active", "court-status-inactive");
                                insignia.getStyleClass().add(activa ? "court-status-active" : "court-status-inactive");
                                setGraphic(insignia);
                        }
                });

		tablaCanchas.setRowFactory(tabla -> {
                        TableRow<Cancha> fila = new TableRow<>();
                        fila.itemProperty().addListener((obs, anterior, actual) -> actualizarClaseFila(fila, actual));
                        fila.selectedProperty().addListener((obs, anterior, actual) -> actualizarClaseFila(fila, fila.getItem()));
                        fila.emptyProperty().addListener((obs, anterior, actual) -> actualizarClaseFila(fila, fila.getItem()));
                        fila.setOnMousePressed(evento -> seleccionarFilaCancha(fila));
                        return fila;
                });
                tablaCanchas.setOnMouseClicked(evento -> { if (clicEnFondoTabla(evento.getTarget())) nuevo(); });
                tablaCanchas.getSelectionModel().selectedItemProperty().addListener((observable, anterior, actual) -> {
                        if (actual != null) editar(actual);
                });
	}

	private void seleccionarFilaCancha(TableRow<Cancha> fila) {
	        if (fila == null || fila.isEmpty() || fila.getItem() == null) return;
	        tablaCanchas.getSelectionModel().select(fila.getItem());
	        tablaCanchas.requestFocus();
	}

	private void actualizarClaseFila(TableRow<Cancha> fila, Cancha cancha) {
	        fila.getStyleClass().removeAll("court-row-inactive", "court-row-selected");
	        if (cancha == null || fila.isEmpty()) return;
	        if (!cancha.isActivo()) fila.getStyleClass().add("court-row-inactive");
	        if (fila.isSelected()) fila.getStyleClass().add("court-row-selected");
	}

	private boolean clicEnFondoTabla(Object objetivo) {
	        if (!(objetivo instanceof Node nodo)) return false;
	        Node actual = nodo;
	        while (actual != null && actual != tablaCanchas) {
	                if (actual instanceof TableRow<?> fila) return fila.isEmpty();
	                actual = actual.getParent();
	        }
	        return actual == tablaCanchas;
	}

	private void configurarBusqueda() {
		canchasFiltradas = new FilteredList<>(canchas, cancha -> true);
		tablaCanchas.setItems(canchasFiltradas);

		campoBuscar.textProperty().addListener((obs, anterior, texto) -> {
			String filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);

			canchasFiltradas.setPredicate(cancha -> filtro.isBlank() || contiene(cancha.getNombre(), filtro)
					|| contiene(cancha.getSuperficie(), filtro) || contiene(cancha.getTipo().toString(), filtro));
			actualizarResultadosVisibles();
		});
	}

	@FXML
	private void limpiarBusqueda() {
		campoBuscar.clear();
		limpiarMensajeContextual();
		nuevo();
		actualizarResultadosVisibles();
		campoBuscar.requestFocus();
	}

	private void actualizarResultadosVisibles() {
		if (etiquetaResultados == null || canchasFiltradas == null) return;
		int visibles = canchasFiltradas.size();
		etiquetaResultados.setText(visibles + (visibles == 1 ? " cancha visible" : " canchas visibles"));
	}

	private void actualizarAyudaListado(Cancha cancha) {
		if (etiquetaAyudaListado == null) return;
		etiquetaAyudaListado.setText(cancha == null ? "Seleccioná una cancha para editarla" : "Seleccionada: " + cancha.getNombre());
	}

	private boolean contiene(String valor, String filtro) {
		return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
	}

	@FXML
	private void cargarCanchas() {
		try {
			List<Cancha> resultado = canchaService.listarTodas();
			canchas.setAll(resultado);
			if (tablaCanchas.getSelectionModel().getSelectedItem() == null && !resultado.isEmpty()) tablaCanchas.scrollTo(0);
			actualizarResultadosVisibles();
			mostrarInfo(resultado.size() + (resultado.size() == 1 ? " cancha cargada." : " canchas cargadas."));
		} catch (RuntimeException exception) {
			mostrarError(exception.getMessage());
		}
	}

	@FXML
	private void nuevo() {
		limpiarMensajeContextual();
		actualizandoFormulario = true;
		canchaSeleccionada = null;
		tablaCanchas.getSelectionModel().clearSelection();
		etiquetaModoCancha.setText("NUEVA CANCHA");
		tituloFormulario.setText("Nueva cancha");
                subtituloFormulario.setText("Configurá disponibilidad, horarios y tarifa del nuevo espacio.");
                insigniaEstadoCancha.setVisible(false);
                insigniaEstadoCancha.setManaged(false);
		botonGuardar.setText("GUARDAR CANCHA");
		botonDesactivar.setVisible(false);
		botonDesactivar.setManaged(false);
                botonEliminarDefinitivamente.setVisible(false);
                botonEliminarDefinitivamente.setManaged(false);
                botonSubir.setVisible(false);
                botonSubir.setManaged(false);
                botonBajar.setVisible(false);
                botonBajar.setManaged(false);
                configurarBotonEstado(true);


		campoNombre.clear();
		comboTipo.getSelectionModel().select(TipoCancha.CUBIERTA);
		campoSuperficie.setText("Césped sintético");
		checkIluminacion.setSelected(true);
		comboApertura.getSelectionModel().select("08:00");
		comboCierre.getSelectionModel().select("23:00");
		comboDuracion.getSelectionModel().select(Integer.valueOf(90));
		campoPrecio.clear();
		campoDescripcion.clear();
		seleccionarDiasLaborales();
		actualizarMensaje("", false);
		estadoFormularioInicial = estadoFormularioActual();
		actualizandoFormulario = false;
		actualizarEstadoGuardar();
		actualizarAyudaListado(null);
		volverArribaDetalle();
		campoNombre.requestFocus();
	}

	private void editar(Cancha cancha) {
		limpiarMensajeContextual();
		actualizandoFormulario = true;
		canchaSeleccionada = cancha;
		etiquetaModoCancha.setText("CANCHA SELECCIONADA");
		tituloFormulario.setText("Editar cancha");
                subtituloFormulario.setText(cancha.getNombre() + " · " + cancha.getTipo() + "\n"
                                + (cancha.getSuperficie() == null || cancha.getSuperficie().isBlank()
                                                ? "Sin superficie informada" : cancha.getSuperficie())
                                + (cancha.isTieneIluminacion() ? " · Con iluminación" : " · Sin iluminación"));
                insigniaEstadoCancha.setText(cancha.isActivo() ? "ACTIVA" : "INACTIVA");
                insigniaEstadoCancha.getStyleClass().removeAll("court-detail-active", "court-detail-inactive");
                insigniaEstadoCancha.getStyleClass().add(cancha.isActivo() ? "court-detail-active" : "court-detail-inactive");
                insigniaEstadoCancha.setVisible(true);
                insigniaEstadoCancha.setManaged(true);
		botonGuardar.setText("GUARDAR CAMBIOS");
		botonDesactivar.setVisible(true);
		botonDesactivar.setManaged(true);
                configurarBotonEstado(cancha.isActivo());

                botonEliminarDefinitivamente.setVisible(!cancha.isActivo());
                botonEliminarDefinitivamente.setManaged(!cancha.isActivo());
                botonSubir.setVisible(true);
                botonSubir.setManaged(true);
                botonBajar.setVisible(true);
                botonBajar.setManaged(true);
                actualizarBotonesOrden(cancha);

		campoNombre.setText(cancha.getNombre());
		comboTipo.setValue(cancha.getTipo());
		campoSuperficie.setText(cancha.getSuperficie());
		checkIluminacion.setSelected(cancha.isTieneIluminacion());
		comboApertura.setValue(cancha.getHoraApertura().format(FORMATO_HORA));
		comboCierre.setValue(cancha.getHoraCierre().format(FORMATO_HORA));
		comboDuracion.setValue(cancha.getDuracionReserva());
		campoPrecio.setText(cancha.getPrecio().toPlainString());
		campoDescripcion.setText(cancha.getDescripcion());
		cargarDias(cancha.getDiasDisponibles());
		actualizarMensaje("", false);
		estadoFormularioInicial = estadoFormularioActual();
		actualizandoFormulario = false;
		actualizarEstadoGuardar();
		actualizarAyudaListado(cancha);
		volverArribaDetalle();
	}

	private void volverArribaDetalle() {
		if (scrollDetalleCancha == null) return;
		javafx.application.Platform.runLater(() -> { scrollDetalleCancha.setVvalue(0); scrollDetalleCancha.setHvalue(0); });
	}

	private void configurarCambiosFormulario() {
		javafx.beans.InvalidationListener listener = obs -> { actualizarEstadoGuardar(); actualizarMensaje("", false); };
		campoNombre.textProperty().addListener(listener); campoSuperficie.textProperty().addListener(listener); campoPrecio.textProperty().addListener(listener); campoDescripcion.textProperty().addListener(listener);
		comboTipo.valueProperty().addListener(listener); comboApertura.valueProperty().addListener(listener); comboCierre.valueProperty().addListener(listener); comboDuracion.valueProperty().addListener(listener); checkIluminacion.selectedProperty().addListener(listener);
		for (CheckBox check : List.of(checkLunes, checkMartes, checkMiercoles, checkJueves, checkViernes, checkSabado, checkDomingo)) check.selectedProperty().addListener(listener);
	}

	private void actualizarEstadoGuardar() {
		if (actualizandoFormulario || botonGuardar == null) return;
		boolean valido = !texto(campoNombre).isBlank() && comboTipo.getValue() != null && comboApertura.getValue() != null && comboCierre.getValue() != null && comboDuracion.getValue() != null && !texto(campoPrecio).isBlank() && !obtenerDiasSeleccionados().isEmpty();
		boolean habilitado = canchaSeleccionada == null ? valido : canchaSeleccionada.isActivo() && valido && !estadoFormularioActual().equals(estadoFormularioInicial);
		botonGuardar.setDisable(!habilitado);
	}

	private String estadoFormularioActual() {
		return String.join("\u001F", texto(campoNombre), String.valueOf(comboTipo.getValue()), texto(campoSuperficie), String.valueOf(checkIluminacion.isSelected()), String.valueOf(comboApertura.getValue()), String.valueOf(comboCierre.getValue()), String.valueOf(comboDuracion.getValue()), texto(campoPrecio), texto(campoDescripcion), obtenerDiasSeleccionados().toString());
	}

	private String texto(TextField campo) { return campo == null || campo.getText() == null ? "" : campo.getText().trim(); }
	private String texto(TextArea campo) { return campo == null || campo.getText() == null ? "" : campo.getText().trim(); }

	@FXML
	private void guardar() {
		try {
			Cancha cancha = canchaSeleccionada == null ? new Cancha() : canchaSeleccionada;

			cancha.setNombre(campoNombre.getText());
			cancha.setTipo(comboTipo.getValue());
			cancha.setSuperficie(campoSuperficie.getText());
			cancha.setTieneIluminacion(checkIluminacion.isSelected());
			cancha.setHoraApertura(parsearHora(comboApertura.getValue()));
			cancha.setHoraCierre(parsearHora(comboCierre.getValue()));
			cancha.setDuracionReserva(comboDuracion.getValue() == null ? 0 : comboDuracion.getValue());
			cancha.setPrecio(parsearPrecio(campoPrecio.getText()));
			cancha.setDescripcion(campoDescripcion.getText());
			cancha.setDiasDisponibles(obtenerDiasSeleccionados());

			canchaService.guardar(cancha);
			cargarCanchas();
			nuevo();
			mostrarInfo("La cancha se guardó correctamente.");

		} catch (IllegalArgumentException exception) {
			mostrarError(exception.getMessage());
		} catch (RuntimeException exception) {
			mostrarError("No se pudo guardar la cancha: " + exception.getMessage());
		}
	}

	private void configurarBotonEstado(boolean activa) {
		botonDesactivar.setText(activa ? "DESACTIVAR" : "REACTIVAR");
		botonDesactivar.getStyleClass().removeAll(
				"danger-button", "reactivate-button",
				"court-deactivate-button-v1",
				"court-reactivate-button-v3");
		botonDesactivar.getStyleClass().add(activa
				? "court-deactivate-button-v1"
				: "court-reactivate-button-v3");
		if (iconoAccionEstado != null) {
			iconoAccionEstado.setContent(activa
					? "M12 3 C7 3 3 7 3 12 C3 17 7 21 12 21 C17 21 21 17 21 12 C21 7 17 3 12 3 M7 12 L17 12"
					: "M20 7 L20 2 L15 2 M20 2 C15 -1 7 1 4 7 M4 17 L4 22 L9 22 M4 22 C9 25 17 23 20 17");
		}
	}

	@FXML
	private void desactivar() {
                if (canchaSeleccionada == null) return;
                if (!canchaSeleccionada.isActivo()) {
                        reactivarSeleccionada();
                        return;
                }
          if (!Dialogos.confirmarPeligro("Desactivar cancha",
                  "¿Desactivar " + canchaSeleccionada.getNombre() + "?\n\n"
                          + "La cancha dejará de aparecer como disponible "
                          + "para nuevas reservas.")) return;
          try {
                  canchaService.eliminar(canchaSeleccionada.getId());
                  cargarCanchas();
                  nuevo();
                  mostrarInfo("La cancha fue desactivada.");
          } catch (RuntimeException exception) {
                  mostrarError(exception.getMessage());
          }
	}

	@FXML
	private void eliminarDefinitivamente() {
	        if (canchaSeleccionada == null) return;
	        if (canchaSeleccionada.isActivo()) {
	                mostrarError("Primero desactivá la cancha.");
	                return;
	        }
	        if (!Dialogos.confirmarPeligro(
	                "Eliminar cancha definitivamente",
	                "¿Eliminar definitivamente " + canchaSeleccionada.getNombre() + "?\n\n"
	                        + "Esta acción solo continuará si la cancha no tiene datos relacionados. "
	                        + "No se puede deshacer.")) return;
	        try {
	                canchaService.eliminarDefinitivamente(canchaSeleccionada.getId());
	                cargarCanchas();
	                nuevo();
	                mostrarInfo("La cancha fue eliminada definitivamente.");
	        } catch (RuntimeException exception) {
	                mostrarError(exception.getMessage());
	        }
	}

	private void reactivarSeleccionada() {
                if (!Dialogos.confirmarAccion(
                        "Reactivar cancha",
                        "¿Reactivar " + canchaSeleccionada.getNombre() + "?",
                        "La cancha volverá a estar disponible para nuevas reservas.",
                        "REACTIVAR")) return;
                try {
                        canchaService.reactivar(canchaSeleccionada.getId());
                        cargarCanchas();
                        nuevo();
                        mostrarInfo("La cancha fue reactivada correctamente.");
                } catch (RuntimeException exception) {
                        mostrarError(exception.getMessage());
                }
        }

	@FXML
	private void subirOrden() {
	        moverOrden(true);
	}

	@FXML
	private void bajarOrden() {
	        moverOrden(false);
	}

	private void moverOrden(boolean subir) {
	        if (canchaSeleccionada == null) {
	                mostrarError("Seleccioná una cancha.");
	                return;
	        }

	        long id = canchaSeleccionada.getId();
	        try {
	                if (subir) {
	                        canchaService.subirOrden(id);
	                } else {
	                        canchaService.bajarOrden(id);
	                }

	                cargarCanchas();
	                canchas.stream()
	                        .filter(cancha -> cancha.getId() == id)
	                        .findFirst()
	                        .ifPresent(cancha -> {
	                                tablaCanchas.getSelectionModel().select(cancha);
	                                javafx.application.Platform.runLater(() ->
	                                        tablaCanchas.scrollTo(cancha));
	                        });
	                mostrarInfo("El orden de las canchas fue actualizado.");
	        } catch (RuntimeException exception) {
	                mostrarError(exception.getMessage());
	        }
	}

	private void actualizarBotonesOrden(Cancha cancha) {
	        int indice = canchas.indexOf(cancha);
	        botonSubir.setDisable(indice <= 0);
	        botonBajar.setDisable(
	                indice < 0 || indice >= canchas.size() - 1);
	}

	@FXML
	private void volver() {
		Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
	}

	private LocalTime parsearHora(String valor) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("Seleccioná la hora de apertura y cierre.");
		}
		return LocalTime.parse(valor, FORMATO_HORA);
	}

	private BigDecimal parsearPrecio(String valor) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("Ingresá el precio de la reserva.");
		}
		try {
			return new BigDecimal(valor.trim().replace("$", "").replace(",", "."));
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("El precio debe ser un número válido.");
		}
	}

	private Set<DayOfWeek> obtenerDiasSeleccionados() {
		EnumSet<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
		agregarDia(dias, checkLunes, DayOfWeek.MONDAY);
		agregarDia(dias, checkMartes, DayOfWeek.TUESDAY);
		agregarDia(dias, checkMiercoles, DayOfWeek.WEDNESDAY);
		agregarDia(dias, checkJueves, DayOfWeek.THURSDAY);
		agregarDia(dias, checkViernes, DayOfWeek.FRIDAY);
		agregarDia(dias, checkSabado, DayOfWeek.SATURDAY);
		agregarDia(dias, checkDomingo, DayOfWeek.SUNDAY);
		return dias;
	}

	private void agregarDia(Set<DayOfWeek> dias, CheckBox check, DayOfWeek dia) {
		if (check.isSelected()) {
			dias.add(dia);
		}
	}

	private void cargarDias(Set<DayOfWeek> dias) {
		checkLunes.setSelected(dias.contains(DayOfWeek.MONDAY));
		checkMartes.setSelected(dias.contains(DayOfWeek.TUESDAY));
		checkMiercoles.setSelected(dias.contains(DayOfWeek.WEDNESDAY));
		checkJueves.setSelected(dias.contains(DayOfWeek.THURSDAY));
		checkViernes.setSelected(dias.contains(DayOfWeek.FRIDAY));
		checkSabado.setSelected(dias.contains(DayOfWeek.SATURDAY));
		checkDomingo.setSelected(dias.contains(DayOfWeek.SUNDAY));
	}

	private void seleccionarDiasLaborales() {
		cargarDias(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
				DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));
	}

	private void limpiarMensajeContextual() {
		if (etiquetaMensaje == null) return;
		etiquetaMensaje.setText("");
		etiquetaMensaje.setVisible(false);
		etiquetaMensaje.setManaged(false);
		etiquetaMensaje.getStyleClass().removeAll("mensaje-exito", "mensaje-error");
	}

	private void mostrarError(String mensaje) { actualizarMensaje(mensaje == null ? "Ocurrió un error." : mensaje, true); }
	private void mostrarInfo(String mensaje) { actualizarMensaje(mensaje, false); }
	private void actualizarMensaje(String mensaje, boolean error) {
		String texto = mensaje == null ? "" : mensaje.trim(); etiquetaMensaje.setText(texto); etiquetaMensaje.setVisible(!texto.isBlank()); etiquetaMensaje.setManaged(!texto.isBlank()); etiquetaMensaje.getStyleClass().removeAll("mensaje-exito", "mensaje-error"); if (!texto.isBlank()) etiquetaMensaje.getStyleClass().add(error ? "mensaje-error" : "mensaje-exito");
	}
}
