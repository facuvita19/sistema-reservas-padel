package vista.controlador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.Cancha;
import negocio.Cliente;
import negocio.EstadoReserva;
import negocio.Reserva;
import servicio.CanchaService;
import servicio.ClienteService;
import servicio.ReservaService;
import vista.Navegacion;
import vista.SolicitudReservaAgenda;

public class ReservasController {

	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final ReservaService reservaService = new ReservaService();
	private final ClienteService clienteService = new ClienteService();
	private final CanchaService canchaService = new CanchaService();

	private final ObservableList<Reserva> reservas = FXCollections.observableArrayList();
	private FilteredList<Reserva> reservasFiltradas;
	private Reserva reservaSeleccionada;

	@FXML
	private TextField campoBuscar;
	@FXML
	private ComboBox<EstadoReserva> filtroEstado;
	@FXML
	private TableView<Reserva> tablaReservas;
	@FXML
	private TableColumn<Reserva, LocalDate> columnaFecha;
	@FXML
	private TableColumn<Reserva, String> columnaHorario;
	@FXML
	private TableColumn<Reserva, String> columnaCancha;
	@FXML
	private TableColumn<Reserva, String> columnaCliente;
	@FXML
	private TableColumn<Reserva, EstadoReserva> columnaEstado;
	@FXML
	private TableColumn<Reserva, BigDecimal> columnaPrecio;

	@FXML
	private Label tituloFormulario;
	@FXML
	private Label etiquetaMensaje;
	@FXML
	private Label etiquetaPrecio;
	@FXML
	private ComboBox<Cliente> comboCliente;
	@FXML
	private ComboBox<Cancha> comboCancha;
	@FXML
	private DatePicker selectorFecha;
	@FXML
	private ComboBox<LocalTime> comboHorario;
	@FXML
	private Spinner<Integer> spinnerJugadores;
	@FXML
	private TextArea campoComentarios;
	@FXML
	private TextArea campoObservaciones;
	@FXML
	private DatePicker filtroFecha;
	@FXML
	private Label etiquetaFechaSeleccionada;

	@FXML
	private void initialize() {
		configurarTabla();
		configurarFiltros();
		configurarFormulario();
		cargarDatosBase();
		cargarReservas();
		nuevaReserva();

		Platform.runLater(this::aplicarSolicitudDesdeAgenda);
	}

	private void configurarTabla() {
		columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
		columnaFecha.setCellFactory(columna -> new javafx.scene.control.TableCell<>() {
			@Override
			protected void updateItem(LocalDate fecha, boolean vacia) {
				super.updateItem(fecha, vacia);
				setText(vacia || fecha == null ? null : fecha.format(FORMATO_FECHA));
			}
		});

		columnaHorario.setCellValueFactory(datos -> new javafx.beans.property.SimpleStringProperty(
				datos.getValue().getHoraInicio().format(FORMATO_HORA) + " - "
						+ datos.getValue().getHoraFin().format(FORMATO_HORA)));
		columnaCancha.setCellValueFactory(new PropertyValueFactory<>("nombreCancha"));
		columnaCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
		columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
		columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));

		tablaReservas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
			if (actual != null) {
				mostrarDetalle(actual);
			}
		});
	}

	private void configurarFiltros() {
		filtroFecha.setValue(LocalDate.now());

		filtroFecha.valueProperty().addListener((observable, fechaAnterior, fechaNueva) -> {
			if (fechaNueva != null) {
				actualizarEtiquetaFecha();
				aplicarFiltros();
			}
		});
		ObservableList<EstadoReserva> estados = FXCollections.observableArrayList(EstadoReserva.values());
		filtroEstado.setItems(estados);

		reservasFiltradas = new FilteredList<>(reservas, reserva -> true);
		tablaReservas.setItems(reservasFiltradas);

		campoBuscar.textProperty().addListener((obs, anterior, actual) -> aplicarFiltros());
		filtroEstado.valueProperty().addListener((obs, anterior, actual) -> aplicarFiltros());

		actualizarEtiquetaFecha();
	}

	private void configurarFormulario() {
		spinnerJugadores.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 4));
		selectorFecha.setDayCellFactory(control -> new javafx.scene.control.DateCell() {
			@Override
			public void updateItem(LocalDate fecha, boolean vacia) {
				super.updateItem(fecha, vacia);
				setDisable(vacia || fecha.isBefore(LocalDate.now()));
			}
		});

		comboCancha.valueProperty().addListener((obs, anterior, actual) -> actualizarDisponibilidad());
		selectorFecha.valueProperty().addListener((obs, anterior, actual) -> actualizarDisponibilidad());
		comboHorario.valueProperty().addListener((obs, anterior, actual) -> actualizarResumenPrecio());
	}

	@FXML
	private void cargarDatosBase() {
		try {
			comboCliente.setItems(FXCollections.observableArrayList(clienteService.listar()));
			comboCancha.setItems(FXCollections.observableArrayList(canchaService.listar()));
		} catch (RuntimeException exception) {
			mostrarError(exception.getMessage());
		}
	}

	@FXML
	private void cargarReservas() {
		try {
			reservas.setAll(reservaService.listar());
			aplicarFiltros();
			mostrarInfo(reservas.size() + " reserva(s) cargada(s).");
		} catch (RuntimeException exception) {
			mostrarError(exception.getMessage());
		}
	}

	private void aplicarSolicitudDesdeAgenda() {
		SolicitudReservaAgenda solicitud = Navegacion.consumirSolicitudReservaAgenda();

		if (solicitud == null) {
			return;
		}

		if (solicitud.tipo() == SolicitudReservaAgenda.Tipo.NUEVA) {

			prepararNuevaReservaDesdeAgenda(solicitud);
			return;
		}

		abrirReservaDesdeAgenda(solicitud.reservaId());
	}

	private void prepararNuevaReservaDesdeAgenda(SolicitudReservaAgenda solicitud) {

		nuevaReserva();

		filtroFecha.setValue(solicitud.fecha());

		Cancha cancha = buscarCancha(solicitud.canchaId());

		if (cancha == null) {
			mostrarError("La cancha seleccionada ya no está disponible.");
			return;
		}

		comboCancha.setValue(cancha);
		selectorFecha.setValue(solicitud.fecha());

		actualizarDisponibilidad();

		LocalTime horario = solicitud.horaInicio();

		if (comboHorario.getItems().contains(horario)) {
			comboHorario.setValue(horario);

			mostrarInfo("Completá el cliente y guardá la reserva.");

			comboCliente.requestFocus();

		} else {
			mostrarError("El horario seleccionado ya no está disponible.");
		}
	}

	private void abrirReservaDesdeAgenda(Long reservaId) {
		if (reservaId == null || reservaId <= 0) {
			mostrarError("La reserva seleccionada no es válida.");
			return;
		}

		Reserva reserva = reservas.stream().filter(valor -> valor.getId() == reservaId).findFirst()
				.orElseGet(() -> reservaService.buscar(reservaId));

		if (reserva == null) {
			mostrarError("La reserva seleccionada ya no existe.");
			return;
		}

		if (!reservas.contains(reserva)) {
			reservas.add(reserva);
		}

		filtroFecha.setValue(reserva.getFecha());

		aplicarFiltros();

		tablaReservas.getSelectionModel().select(reserva);
		tablaReservas.scrollTo(reserva);
		mostrarDetalle(reserva);

		mostrarInfo("Reserva seleccionada desde la agenda.");
	}

	private void aplicarFiltros() {
		if (reservasFiltradas == null) {
			return;
		}

		String texto = campoBuscar.getText() == null ? "" : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);

		EstadoReserva estado = filtroEstado.getValue();
		LocalDate fecha = filtroFecha.getValue();

		reservasFiltradas.setPredicate(reserva -> {
			boolean coincideFecha = fecha == null || fecha.equals(reserva.getFecha());

			boolean coincideTexto = texto.isBlank() || contiene(reserva.getNombreCliente(), texto)
					|| contiene(reserva.getNombreCancha(), texto) || contiene(reserva.getEstado().toString(), texto);

			boolean coincideEstado = estado == null || reserva.getEstado() == estado;

			return coincideFecha && coincideTexto && coincideEstado;
		});
	}

	private boolean contiene(String valor, String filtro) {
		return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
	}

	@FXML
	private void limpiarFiltros() {
		campoBuscar.clear();
		filtroEstado.getSelectionModel().clearSelection();
		filtroFecha.setValue(LocalDate.now());
		aplicarFiltros();
	}

	@FXML
	private void nuevaReserva() {
		reservaSeleccionada = null;
		tablaReservas.getSelectionModel().clearSelection();
		tituloFormulario.setText("Nueva reserva");

		comboCliente.getSelectionModel().clearSelection();
		comboCancha.getSelectionModel().clearSelection();
		selectorFecha.setValue(LocalDate.now().plusDays(1));
		comboHorario.getItems().clear();
		spinnerJugadores.getValueFactory().setValue(4);
		campoComentarios.clear();
		campoObservaciones.clear();
		etiquetaPrecio.setText("ARS 0");
		etiquetaMensaje.setText("");
	}

	private void mostrarDetalle(Reserva reserva) {
		reservaSeleccionada = reserva;
		tituloFormulario.setText("Detalle de reserva");

		comboCliente.setValue(buscarCliente(reserva.getClienteId()));
		comboCancha.setValue(buscarCancha(reserva.getCanchaId()));
		selectorFecha.setValue(reserva.getFecha());
		actualizarDisponibilidad();
		if (!comboHorario.getItems().contains(reserva.getHoraInicio())) {
			comboHorario.getItems().add(reserva.getHoraInicio());
		}
		comboHorario.setValue(reserva.getHoraInicio());
		spinnerJugadores.getValueFactory().setValue(reserva.getCantidadJugadores());
		campoComentarios.setText(reserva.getComentarios());
		campoObservaciones.setText(reserva.getObservacionesAdministrativas());
		etiquetaPrecio.setText("ARS " + reserva.getPrecioTotal().toPlainString());
	}

	private Cliente buscarCliente(long id) {
		return comboCliente.getItems().stream().filter(cliente -> cliente.getId() == id).findFirst().orElse(null);
	}

	private Cancha buscarCancha(long id) {
		return comboCancha.getItems().stream().filter(cancha -> cancha.getId() == id).findFirst().orElse(null);
	}

	@FXML
	private void actualizarDisponibilidad() {
		Cancha cancha = comboCancha.getValue();
		LocalDate fecha = selectorFecha.getValue();
		comboHorario.getItems().clear();

		if (cancha == null || fecha == null) {
			actualizarResumenPrecio();
			return;
		}

		try {
			long reservaExcluidaId = reservaSeleccionada == null ? 0L : reservaSeleccionada.getId();
			List<LocalTime> horarios = reservaService.listarHorariosDisponibles(cancha.getId(), fecha,
					reservaExcluidaId);
			comboHorario.setItems(FXCollections.observableArrayList(horarios));
			if (!horarios.isEmpty()) {
				comboHorario.getSelectionModel().selectFirst();
			} else {
				mostrarError("No hay horarios disponibles para esa fecha.");
			}
			actualizarResumenPrecio();
		} catch (RuntimeException exception) {
			mostrarError(exception.getMessage());
		}
	}

	private void actualizarResumenPrecio() {
		Cancha cancha = comboCancha.getValue();
		etiquetaPrecio.setText(cancha == null ? "ARS 0" : "ARS " + cancha.getPrecio().toPlainString());
	}

	@FXML
	private void guardar() {
		try {
			validarSeleccionFormulario();

			Reserva reserva = reservaSeleccionada == null ? new Reserva() : reservaSeleccionada;

			reserva.setClienteId(comboCliente.getValue().getId());
			reserva.setCanchaId(comboCancha.getValue().getId());
			reserva.setUsuarioId(Navegacion.getUsuarioActual().getId());
			reserva.setFecha(selectorFecha.getValue());
			reserva.setHoraInicio(comboHorario.getValue());
			reserva.setCantidadJugadores(spinnerJugadores.getValue());
			reserva.setComentarios(campoComentarios.getText());
			reserva.setObservacionesAdministrativas(campoObservaciones.getText());

			if (reserva.getEstado() == null) {
				reserva.setEstado(EstadoReserva.PENDIENTE);
			}

			reservaService.guardar(reserva);
			cargarReservas();
			nuevaReserva();
			mostrarInfo("La reserva se guardó correctamente.");

		} catch (IllegalArgumentException exception) {
			mostrarError(exception.getMessage());
		} catch (RuntimeException exception) {
			mostrarError("No se pudo guardar la reserva: " + exception.getMessage());
		}
	}

	private void validarSeleccionFormulario() {
		if (comboCliente.getValue() == null) {
			throw new IllegalArgumentException("Seleccioná un cliente.");
		}
		if (comboCancha.getValue() == null) {
			throw new IllegalArgumentException("Seleccioná una cancha.");
		}
		if (selectorFecha.getValue() == null) {
			throw new IllegalArgumentException("Seleccioná una fecha.");
		}
		if (comboHorario.getValue() == null) {
			throw new IllegalArgumentException("Seleccioná un horario disponible.");
		}
		if (Navegacion.getUsuarioActual() == null) {
			throw new IllegalArgumentException("La sesión administrativa finalizó.");
		}
	}

	@FXML
	private void confirmarReserva() {
		cambiarEstadoSeleccionado(EstadoReserva.CONFIRMADA);
	}

	@FXML
	private void cancelarReserva() {
		cambiarEstadoSeleccionado(EstadoReserva.CANCELADA);
	}

	@FXML
	private void diaAnterior() {
		LocalDate fechaActual = filtroFecha.getValue();

		if (fechaActual == null) {
			fechaActual = LocalDate.now();
		}

		filtroFecha.setValue(fechaActual.minusDays(1));
	}

	@FXML
	private void diaSiguiente() {
		LocalDate fechaActual = filtroFecha.getValue();

		if (fechaActual == null) {
			fechaActual = LocalDate.now();
		}

		filtroFecha.setValue(fechaActual.plusDays(1));
	}

	@FXML
	private void irHoy() {
		LocalDate hoy = LocalDate.now();

		if (hoy.equals(filtroFecha.getValue())) {
			aplicarFiltros();
			actualizarEtiquetaFecha();
		} else {
			filtroFecha.setValue(hoy);
		}
	}

	private void actualizarEtiquetaFecha() {
		LocalDate fecha = filtroFecha.getValue();

		if (fecha == null) {
			etiquetaFechaSeleccionada.setText("Todas las fechas");
			return;
		}

		if (fecha.equals(LocalDate.now())) {
			etiquetaFechaSeleccionada.setText("Reservas de hoy");
			return;
		}

		if (fecha.equals(LocalDate.now().plusDays(1))) {
			etiquetaFechaSeleccionada.setText("Reservas de mañana");
			return;
		}

		if (fecha.equals(LocalDate.now().minusDays(1))) {
			etiquetaFechaSeleccionada.setText("Reservas de ayer");
			return;
		}

		DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", new Locale("es", "AR"));

		String texto = fecha.format(formato);

		etiquetaFechaSeleccionada.setText(Character.toUpperCase(texto.charAt(0)) + texto.substring(1));
	}

	private void cambiarEstadoSeleccionado(EstadoReserva estado) {
		if (reservaSeleccionada == null) {
			mostrarError("Seleccioná una reserva de la tabla.");
			return;
		}

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Cambiar estado");
		confirmacion.setHeaderText("¿Cambiar la reserva a " + estado + "?");
		confirmacion
				.setContentText(reservaSeleccionada.getNombreCliente() + " - " + reservaSeleccionada.getNombreCancha());

		confirmacion.showAndWait().ifPresent(respuesta -> {
			if (respuesta == ButtonType.OK) {
				try {
					reservaService.cambiarEstado(reservaSeleccionada.getId(), estado);
					cargarReservas();
					nuevaReserva();
					mostrarInfo("El estado se actualizó correctamente.");
				} catch (RuntimeException exception) {
					mostrarError(exception.getMessage());
				}
			}
		});
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
}
