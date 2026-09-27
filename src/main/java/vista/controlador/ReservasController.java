package vista.controlador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import javafx.scene.control.Button;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import negocio.Cancha;
import negocio.Cliente;
import negocio.EstadoReserva;
import negocio.Reserva;
import servicio.CanchaService;
import servicio.ClienteService;
import servicio.PagoService;
import servicio.PoliticaReservaService;
import servicio.ReprogramacionReservaService;
import servicio.ReservaService;
import vista.FiltroReservas;
import vista.Navegacion;
import vista.SolicitudFiltroReservas;
import vista.SolicitudReservaAgenda;

public class ReservasController {

	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final ReservaService reservaService = new ReservaService();
	private final ClienteService clienteService = new ClienteService();
	private final CanchaService canchaService = new CanchaService();

	private final ObservableList<Reserva> reservas = FXCollections.observableArrayList();
	private FilteredList<Reserva> reservasFiltradas;
	private FiltroReservas filtroDashboard;
	private Reserva reservaSeleccionada;
	private final PagoService pagoService = new PagoService();
	private final PoliticaReservaService politicaReservaService = new PoliticaReservaService();
	private final ReprogramacionReservaService reprogramacionService = new ReprogramacionReservaService();

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
	private Label etiquetaSaldoPendiente;
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
	private Button botonAccionPrincipal;
	@FXML
	private Button botonPagos;
	@FXML
	private Button botonCancelar;
	@FXML
	private VBox contenedorCancelacion;
	@FXML
	private Label etiquetaCancelacion;

	private boolean modoReprogramacion;

	private boolean reprogramacionAdministrativa;

	@FXML
	private Button botonGuardar;
	@FXML
	private Button botonReprogramar;

	@FXML
	private void initialize() {
		configurarTabla();
		configurarFiltros();
		configurarFormulario();
		cargarDatosBase();
		cargarReservas();
		nuevaReserva();

		Platform.runLater(() -> {
			aplicarSolicitudDesdeAgenda();
			aplicarSolicitudFiltroDashboard();
		});
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

	private void aplicarSolicitudFiltroDashboard() {
		SolicitudFiltroReservas solicitud = Navegacion.consumirSolicitudFiltroReservas();

		if (solicitud == null) {
			return;
		}

		filtroDashboard = solicitud.filtro();

		campoBuscar.clear();
		filtroEstado.getSelectionModel().clearSelection();

		/*
		 * Los filtros del Dashboard pueden incluir reservas de diferentes fechas, por
		 * lo que se elimina temporalmente el filtro diario.
		 */
		filtroFecha.setValue(null);

		switch (filtroDashboard) {
		case PENDIENTES_SENIA -> {
			filtroEstado.setValue(EstadoReserva.PENDIENTE);

			mostrarInfo("Mostrando reservas vigentes esperando seña.");
		}

		case PROXIMAS_A_VENCER -> {
			filtroEstado.setValue(EstadoReserva.PENDIENTE);

			mostrarInfo("Mostrando solicitudes web que vencen " + "dentro de los próximos 5 minutos.");
		}

		case PENDIENTES_CIERRE -> {
			filtroEstado.setValue(EstadoReserva.CONFIRMADA);

			mostrarInfo("Mostrando turnos finalizados pendientes de cierre.");
		}
		}

		aplicarFiltros();
	}

	private void mostrarDialogoCierreTurno() {
		ButtonType botonCompletada = new ButtonType("Se jugó normalmente");

		ButtonType botonAusente = new ButtonType("No se presentó");

		ButtonType botonVolver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION, "", botonCompletada, botonAusente, botonVolver);

		dialogo.setTitle("Cerrar turno");
		dialogo.setHeaderText("¿Cómo finalizó la reserva?");

		dialogo.setContentText(reservaSeleccionada.getNombreCliente() + "\n" + reservaSeleccionada.getNombreCancha()
				+ "\n" + reservaSeleccionada.getHoraInicio().format(FORMATO_HORA) + " - "
				+ reservaSeleccionada.getHoraFin().format(FORMATO_HORA));

		dialogo.showAndWait().ifPresent(respuesta -> {
			if (respuesta == botonCompletada) {
				cambiarEstadoSeleccionado(EstadoReserva.COMPLETADA);

			} else if (respuesta == botonAusente) {
				cambiarEstadoSeleccionado(EstadoReserva.AUSENTE);
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

	private boolean tienePagosAcreditados() {
		return reservaSeleccionada != null && pagoService.tienePagosAcreditados(reservaSeleccionada.getId());
	}

	private String totalAcreditadoSeleccionado() {
		if (reservaSeleccionada == null) {
			return "ARS 0.00";
		}

		BigDecimal total = pagoService.totalAcreditado(reservaSeleccionada.getId());

		return "ARS " + total.toPlainString();
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

	@FXML
	private void iniciarReprogramacion() {
		if (reservaSeleccionada == null) {
			mostrarError("Seleccioná una reserva para reprogramarla.");
			return;
		}

		EstadoReserva estado = reservaSeleccionada.getEstado();

		if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {

			mostrarError("Solo se pueden reprogramar reservas " + "pendientes o confirmadas.");
			return;
		}

		try {
			politicaReservaService.validarPlazoCancelacion(reservaSeleccionada);

			reprogramacionAdministrativa = false;
			modoReprogramacion = true;

			tituloFormulario.setText("Reprogramar reserva");

			botonGuardar.setText("GUARDAR REPROGRAMACIÓN");

			comboCliente.setDisable(true);
			spinnerJugadores.setDisable(true);

			comboCancha.setDisable(false);
			selectorFecha.setDisable(false);
			comboHorario.setDisable(false);

			botonReprogramar.setVisible(false);
			botonReprogramar.setManaged(false);

			mostrarInfo("Seleccioná una nueva cancha, fecha y horario.");

		} catch (IllegalArgumentException exception) {
			ofrecerReprogramacionAdministrativa(exception.getMessage());
		}
	}

	private void ofrecerReprogramacionAdministrativa(String restriccion) {

		Alert aviso = new Alert(Alert.AlertType.WARNING);

		aviso.setTitle("Reprogramación fuera de plazo");

		aviso.setHeaderText("La reserva no puede reprogramarse normalmente.");

		configurarTextoLargo(aviso, restriccion + "\n\n" + "Si el cambio corresponde a una situación "
				+ "atribuible al complejo, podés realizar una " + "reprogramación administrativa.");

		ButtonType botonContinuar = new ButtonType("Reprogramación administrativa");

		ButtonType botonVolver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

		aviso.getButtonTypes().setAll(botonContinuar, botonVolver);

		aviso.showAndWait().ifPresent(respuesta -> {
			if (respuesta == botonContinuar) {
				reprogramacionAdministrativa = true;
				modoReprogramacion = true;

				tituloFormulario.setText("Reprogramación administrativa");

				botonGuardar.setText("GUARDAR REPROGRAMACIÓN");

				comboCliente.setDisable(true);
				spinnerJugadores.setDisable(true);

				botonReprogramar.setVisible(false);
				botonReprogramar.setManaged(false);

				mostrarInfo("Seleccioná el nuevo horario. " + "El motivo será obligatorio.");
			}
		});
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
		LocalDateTime ahora = LocalDateTime.now();
		LocalDateTime limiteVencimiento = ahora.plusMinutes(5);

		reservasFiltradas.setPredicate(reserva -> {
			boolean coincideFecha = fecha == null || fecha.equals(reserva.getFecha());

			boolean coincideTexto = texto.isBlank() || contiene(reserva.getNombreCliente(), texto)
					|| contiene(reserva.getNombreCancha(), texto) || contiene(reserva.getEstado().toString(), texto);

			boolean coincideEstado = estado == null || reserva.getEstado() == estado;

			boolean coincideDashboard = coincideFiltroDashboard(reserva, ahora, limiteVencimiento);

			return coincideFecha && coincideTexto && coincideEstado && coincideDashboard;
		});
	}

	private boolean coincideFiltroDashboard(Reserva reserva, LocalDateTime ahora, LocalDateTime limiteVencimiento) {

		if (filtroDashboard == null) {
			return true;
		}

		return switch (filtroDashboard) {
		case PENDIENTES_SENIA -> reserva.getEstado() == EstadoReserva.PENDIENTE
				&& (reserva.getFechaVencimiento() == null || reserva.getFechaVencimiento().isAfter(ahora));

		case PROXIMAS_A_VENCER -> reserva.getEstado() == EstadoReserva.PENDIENTE
				&& reserva.getFechaVencimiento() != null && reserva.getFechaVencimiento().isAfter(ahora)
				&& !reserva.getFechaVencimiento().isAfter(limiteVencimiento);

		case PENDIENTES_CIERRE -> reserva.getEstado() == EstadoReserva.CONFIRMADA && reserva.getFecha() != null
				&& reserva.getHoraFin() != null
				&& !LocalDateTime.of(reserva.getFecha(), reserva.getHoraFin()).isAfter(ahora);
		};
	}

	private boolean contiene(String valor, String filtro) {
		return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
	}

	@FXML
	private void limpiarFiltros() {
		filtroDashboard = null;

		campoBuscar.clear();
		filtroEstado.getSelectionModel().clearSelection();
		filtroFecha.setValue(LocalDate.now());

		actualizarEtiquetaFecha();
		aplicarFiltros();

		mostrarInfo("Se eliminaron los filtros del Dashboard.");
	}

	@FXML
	private void nuevaReserva() {
		modoReprogramacion = false;
		reprogramacionAdministrativa = false;
		reservaSeleccionada = null;
		tablaReservas.getSelectionModel().clearSelection();
		tituloFormulario.setText("Nueva reserva");

		comboCliente.getSelectionModel().clearSelection();
		comboCancha.getSelectionModel().clearSelection();
		selectorFecha.setValue(LocalDate.now());
		comboHorario.getItems().clear();
		spinnerJugadores.getValueFactory().setValue(4);
		campoComentarios.clear();
		campoObservaciones.clear();
		etiquetaPrecio.setText("ARS 0");
		etiquetaMensaje.setText("");
		etiquetaSaldoPendiente.setText("ARS 0");
		contenedorCancelacion.setVisible(false);
		contenedorCancelacion.setManaged(false);
		etiquetaCancelacion.setText("");
		botonGuardar.setText("GUARDAR RESERVA");
		comboCliente.setDisable(false);
		spinnerJugadores.setDisable(false);

		actualizarAccionesReserva();
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

		try {
			BigDecimal saldo = pagoService.calcularSaldo(reserva.getId());

			etiquetaSaldoPendiente.setText("ARS " + saldo.toPlainString());

		} catch (RuntimeException exception) {
			etiquetaSaldoPendiente.setText("No disponible");
		}
		actualizarAccionesReserva();
		mostrarDetalleCancelacion(reserva);
	}

	private void mostrarDetalleCancelacion(Reserva reserva) {

		boolean cancelada = reserva.getEstado() == EstadoReserva.CANCELADA && reserva.getTipoCancelacion() != null;

		contenedorCancelacion.setVisible(cancelada);
		contenedorCancelacion.setManaged(cancelada);

		if (!cancelada) {
			etiquetaCancelacion.setText("");
			return;
		}

		String tipo = switch (reserva.getTipoCancelacion()) {

		case CLIENTE -> "Cancelación normal";

		case ADMINISTRATIVA -> "Cancelación administrativa";
		};

		String motivo = reserva.getMotivoCancelacion() == null ? "Sin motivo registrado"
				: reserva.getMotivoCancelacion();

		String fecha = reserva.getFechaCancelacion() == null ? "Sin fecha registrada"
				: reserva.getFechaCancelacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

		etiquetaCancelacion.setText(tipo + "\nMotivo: " + motivo + "\nFecha: " + fecha);
	}

	@FXML
	private void ejecutarAccionPrincipal() {
		if (reservaSeleccionada == null) {
			mostrarError("Seleccioná una reserva de la tabla.");
			return;
		}

		if (reservaSeleccionada.getEstado() == EstadoReserva.PENDIENTE) {

			mostrarError("La reserva se confirmará automáticamente " + "cuando los pagos acreditados alcancen "
					+ "el importe mínimo de la seña.");
			return;
		}

		if (reservaSeleccionada.getEstado() == EstadoReserva.CONFIRMADA && turnoFinalizado(reservaSeleccionada)) {

			mostrarDialogoCierreTurno();
		}
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
		limpiarMensaje();

		if (cancha == null || fecha == null) {
			actualizarResumenPrecio();
			return;
		}

		try {
			long reservaExcluidaId = reservaSeleccionada == null ? 0L : reservaSeleccionada.getId();

			List<LocalTime> horarios = reservaService.listarHorariosDisponibles(cancha.getId(), fecha,
					reservaExcluidaId);

			comboHorario.setItems(FXCollections.observableArrayList(horarios));

			if (horarios.isEmpty()) {
				mostrarError("No hay horarios disponibles para esa fecha.");
			} else {
				comboHorario.getSelectionModel().selectFirst();
				limpiarMensaje();
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

	private void actualizarAccionesReserva() {
		if (reservaSeleccionada == null) {
			configurarBoton(botonAccionPrincipal, false, false, "CERRAR TURNO");

			configurarBoton(botonPagos, false, false, "ABRIR PAGOS");

			configurarBoton(botonCancelar, false, false, "CANCELAR RESERVA");

			configurarBoton(botonReprogramar, false, false, "REPROGRAMAR");

			return;
		}

		EstadoReserva estado = reservaSeleccionada.getEstado();

		boolean pendiente = estado == EstadoReserva.PENDIENTE;

		boolean confirmada = estado == EstadoReserva.CONFIRMADA;

		boolean finalizada = estado == EstadoReserva.COMPLETADA || estado == EstadoReserva.AUSENTE
				|| estado == EstadoReserva.CANCELADA || estado == EstadoReserva.EXPIRADA;

		if (confirmada && turnoFinalizado(reservaSeleccionada)) {

			configurarBoton(botonAccionPrincipal, true, true, "CERRAR TURNO");
		} else {
			configurarBoton(botonAccionPrincipal, false, false, "CERRAR TURNO");
		}

		boolean mostrarPagos = pendiente || confirmada || estado == EstadoReserva.COMPLETADA;

		boolean mostrarCancelar = pendiente || confirmada;

		boolean mostrarReprogramar = pendiente || confirmada;

		configurarBoton(botonPagos, mostrarPagos, mostrarPagos, pendiente ? "REGISTRAR SEÑA" : "ABRIR PAGOS");

		configurarBoton(botonCancelar, mostrarCancelar, mostrarCancelar, "CANCELAR RESERVA");

		configurarBoton(botonReprogramar, mostrarReprogramar, mostrarReprogramar, "REPROGRAMAR");
	}

	private void configurarBoton(Button boton, boolean visible, boolean administrado, String texto) {

		boton.setVisible(visible);
		boton.setManaged(administrado);
		boton.setText(texto);
	}

	private boolean turnoFinalizado(Reserva reserva) {
		LocalDateTime finalizacion = LocalDateTime.of(reserva.getFecha(), reserva.getHoraFin());

		return !finalizacion.isAfter(LocalDateTime.now());
	}

	@FXML
	private void guardar() {
		if (modoReprogramacion) {
			guardarReprogramacion();
			return;
		}
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

			if (reservaSeleccionada == null) {
				reserva.setEstado(EstadoReserva.PENDIENTE);
				reserva.setFechaVencimiento(null);
				reserva.setFechaExpiracion(null);
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

	private void guardarReprogramacion() {
		if (reservaSeleccionada == null) {
			mostrarError("La reserva seleccionada ya no está disponible.");
			cancelarModoReprogramacion();
			return;
		}

		if (comboCancha.getValue() == null || selectorFecha.getValue() == null || comboHorario.getValue() == null) {

			mostrarError("Seleccioná la nueva cancha, fecha y horario.");
			return;
		}

		TextInputDialog dialogo = new TextInputDialog();

		dialogo.setTitle("Motivo de reprogramación");

		dialogo.setHeaderText("Ingresá el motivo del cambio");

		dialogo.setContentText("Motivo:");

		dialogo.getEditor().setPromptText("Ejemplo: solicitud del cliente o problema operativo");

		dialogo.showAndWait().ifPresent(this::ejecutarReprogramacion);
	}

	private void ejecutarReprogramacion(String motivo) {
		if (motivo == null || motivo.isBlank()) {
			mostrarError("El motivo de la reprogramación es obligatorio.");
			return;
		}

		if (Navegacion.getUsuarioActual() == null) {
			mostrarError("La sesión administrativa finalizó.");
			return;
		}

		try {
			long reservaId = reservaSeleccionada.getId();
			LocalDate nuevaFecha = selectorFecha.getValue();

			if (reprogramacionAdministrativa) {
				reprogramacionService.reprogramarAdministrativamente(reservaId, comboCancha.getValue().getId(),
						nuevaFecha, comboHorario.getValue(), motivo, Navegacion.getUsuarioActual().getId());
			} else {
				reprogramacionService.reprogramar(reservaId, comboCancha.getValue().getId(), nuevaFecha,
						comboHorario.getValue(), motivo, Navegacion.getUsuarioActual().getId());
			}

			cancelarModoReprogramacion();
			cargarReservas();

			filtroFecha.setValue(nuevaFecha);
			aplicarFiltros();

			Reserva actualizada = reservas.stream().filter(reserva -> reserva.getId() == reservaId).findFirst()
					.orElse(null);

			if (actualizada != null) {
				tablaReservas.getSelectionModel().select(actualizada);
				tablaReservas.scrollTo(actualizada);
				mostrarDetalle(actualizada);
			}

			mostrarInfo("La reserva se reprogramó correctamente. " + "Los pagos se conservaron.");

		} catch (RuntimeException exception) {
			mostrarError(exception.getMessage());
		}
	}

	private void configurarTextoLargo(Alert alerta, String contenido) {

		Label texto = new Label(contenido);
		texto.setWrapText(true);
		texto.setMaxWidth(Double.MAX_VALUE);
		texto.setMinHeight(Region.USE_PREF_SIZE);
		texto.setPrefWidth(500);

		alerta.getDialogPane().setContent(texto);
		alerta.getDialogPane().setPrefWidth(580);
		alerta.setResizable(true);
	}

	private void cancelarModoReprogramacion() {
		modoReprogramacion = false;
		reprogramacionAdministrativa = false;

		botonGuardar.setText("GUARDAR RESERVA");

		comboCliente.setDisable(false);
		spinnerJugadores.setDisable(false);

		comboCancha.setDisable(false);
		selectorFecha.setDisable(false);
		comboHorario.setDisable(false);

		actualizarAccionesReserva();
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
		if (reservaSeleccionada == null) {
			politicaReservaService.validarAnticipacionMinima(selectorFecha.getValue(), comboHorario.getValue());
		}
	}

	@FXML
	private void cancelarReserva() {
		if (reservaSeleccionada == null) {
			mostrarError("Seleccioná una reserva de la tabla.");
			return;
		}

		try {
			politicaReservaService.validarPlazoCancelacion(reservaSeleccionada);

			mostrarOpcionesCancelacionDentroDePlazo();

		} catch (IllegalArgumentException exception) {
			mostrarOpcionesCancelacionFueraDePlazo(exception.getMessage());
		}
	}

	private void mostrarOpcionesCancelacionDentroDePlazo() {
		boolean tienePagos = tienePagosAcreditados();

		ButtonType botonReprogramar = new ButtonType("Reprogramar");

		ButtonType botonCancelar = new ButtonType(tienePagos ? "Cancelar y reembolsar" : "Cancelar reserva");

		ButtonType botonVolver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION);

		dialogo.setTitle("Cancelar reserva");
		dialogo.setHeaderText("La reserva está dentro del plazo permitido.");

		String pagos = tienePagos
				? "\nPagos acreditados: " + totalAcreditadoSeleccionado() + "\n\nLa seña puede trasladarse "
						+ "a otro horario o reembolsarse."
				: "\nLa reserva no tiene pagos acreditados.";

		dialogo.setContentText(reservaSeleccionada.getNombreCliente() + "\n" + reservaSeleccionada.getNombreCancha()
				+ "\n" + reservaSeleccionada.getFecha().format(FORMATO_FECHA) + " "
				+ reservaSeleccionada.getHoraInicio().format(FORMATO_HORA) + pagos);

		dialogo.getButtonTypes().setAll(botonReprogramar, botonCancelar, botonVolver);

		dialogo.showAndWait().ifPresent(respuesta -> {
			if (respuesta == botonReprogramar) {
				iniciarReprogramacion();

			} else if (respuesta == botonCancelar) {
				if (tienePagos) {
					confirmarCancelacionConReembolso(false);
				} else {
					confirmarCancelacionSinPagos();
				}
			}
		});
	}

	private void confirmarCancelacionSinPagos() {
		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

		confirmacion.setTitle("Cancelar reserva");
		confirmacion.setHeaderText("¿Confirmar la cancelación?");

		confirmacion
				.setContentText("La reserva no tiene pagos acreditados. " + "El horario volverá a quedar disponible.");

		confirmacion.showAndWait().ifPresent(respuesta -> {
			if (respuesta == ButtonType.OK) {
				ejecutarCancelacionNormal();
			}
		});
	}

	private void mostrarOpcionesCancelacionFueraDePlazo(String restriccion) {

		ButtonType botonRetener = new ButtonType(
				tienePagosAcreditados() ? "Cancelar y retener seña" : "Cancelar reserva");

		ButtonType botonAdministrativa = new ButtonType("Excepción administrativa");

		ButtonType botonVolver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert dialogo = new Alert(Alert.AlertType.WARNING);

		dialogo.setTitle("Cancelación fuera de plazo");
		dialogo.setHeaderText("La reserva está fuera del plazo permitido.");

		String pago = tienePagosAcreditados()
				? "\n\nLa seña acreditada de " + totalAcreditadoSeleccionado() + " quedará retenida."
				: "";

		configurarTextoLargo(dialogo, restriccion + pago + "\n\n" + "Una excepción administrativa solo debe utilizarse "
				+ "cuando la cancelación sea responsabilidad del complejo.");

		dialogo.getButtonTypes().setAll(botonRetener, botonAdministrativa, botonVolver);

		dialogo.showAndWait().ifPresent(respuesta -> {
			if (respuesta == botonRetener) {
				confirmarCancelacionConSeniaRetenida(true);

			} else if (respuesta == botonAdministrativa) {
				mostrarOpcionesAdministrativas();
			}
		});
	}

	private void recargarDespuesDeCancelar(LocalDate fecha, String mensaje) {

		cargarReservas();
		nuevaReserva();

		filtroFecha.setValue(fecha);
		aplicarFiltros();

		mostrarInfo(mensaje);
	}

	private void mostrarOpcionesAdministrativas() {
		boolean tienePagos = tienePagosAcreditados();

		ButtonType botonReprogramar = new ButtonType("Reprogramar");

		ButtonType botonCancelar = new ButtonType(tienePagos ? "Cancelar y reembolsar" : "Cancelar reserva");

		ButtonType botonVolver = new ButtonType("Volver", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

		Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION);

		dialogo.setTitle("Excepción administrativa");
		dialogo.setHeaderText("Seleccioná la solución para el cliente.");

		dialogo.setContentText("Esta operación debe utilizarse únicamente "
				+ "por lluvia, mantenimiento, corte de energía " + "u otra situación atribuible al complejo.");

		dialogo.getButtonTypes().setAll(botonReprogramar, botonCancelar, botonVolver);

		dialogo.showAndWait().ifPresent(respuesta -> {
			if (respuesta == botonReprogramar) {
				iniciarReprogramacionAdministrativa();

			} else if (respuesta == botonCancelar) {
				solicitarMotivoAdministrativo(tienePagos);
			}
		});
	}

	private void iniciarReprogramacionAdministrativa() {
		if (reservaSeleccionada == null) {
			mostrarError("Seleccioná una reserva para reprogramarla.");
			return;
		}

		reprogramacionAdministrativa = true;
		modoReprogramacion = true;

		tituloFormulario.setText("Reprogramación administrativa");

		botonGuardar.setText("GUARDAR REPROGRAMACIÓN");

		comboCliente.setDisable(true);
		spinnerJugadores.setDisable(true);

		comboCancha.setDisable(false);
		selectorFecha.setDisable(false);
		comboHorario.setDisable(false);

		botonReprogramar.setVisible(false);
		botonReprogramar.setManaged(false);

		mostrarInfo("Seleccioná una nueva cancha, fecha y horario. " + "El motivo será obligatorio.");
	}

	private void confirmarCancelacionConSeniaRetenida(boolean fueraDePlazo) {

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

		confirmacion.setTitle("Confirmar cancelación");

		confirmacion.setHeaderText(tienePagosAcreditados() ? "¿Cancelar y retener la seña?" : "¿Cancelar la reserva?");

		String detalle = tienePagosAcreditados()
				? "El importe acreditado de " + totalAcreditadoSeleccionado() + " no será reembolsado."
				: "La reserva quedará cancelada.";

		if (fueraDePlazo) {
			detalle += "\n\nLa cancelación se registrará " + "fuera del plazo permitido.";
		}

		confirmacion.setContentText(detalle);

		confirmacion.showAndWait().ifPresent(respuesta -> {
			if (respuesta == ButtonType.OK) {
				ejecutarCancelacionNormal();
			}
		});
	}

	private void ejecutarCancelacionNormal() {
		try {
			LocalDate fechaReserva = reservaSeleccionada.getFecha();

			boolean teniaSenia = tienePagosAcreditados();

			reservaService.cancelarNormal(reservaSeleccionada.getId());

			recargarDespuesDeCancelar(fechaReserva, teniaSenia ? "La reserva fue cancelada y la seña quedó retenida."
					: "La reserva fue cancelada correctamente.");

		} catch (RuntimeException exception) {
			mostrarError(exception.getMessage());
		}
	}

	private void ejecutarCancelacionAdministrativa(String motivo, boolean reembolsar) {

		try {
			long reservaId = reservaSeleccionada.getId();

			LocalDate fechaReserva = reservaSeleccionada.getFecha();

			reservaService.cancelarAdministrativamente(reservaId, motivo, Navegacion.getUsuarioActual().getId());

			if (reembolsar) {
				pagoService.reembolsarPagosDeReservaAutorizado(reservaId);
			}

			recargarDespuesDeCancelar(fechaReserva,
					reembolsar ? "La reserva fue cancelada administrativamente " + "y los pagos fueron reembolsados."
							: "La reserva fue cancelada administrativamente.");

		} catch (RuntimeException exception) {
			mostrarError(exception.getMessage());
		}
	}

	private void confirmarCancelacionConReembolso(boolean administrativa) {

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

		confirmacion.setTitle("Cancelar y reembolsar");
		confirmacion.setHeaderText("¿Cancelar la reserva y reembolsar " + totalAcreditadoSeleccionado() + "?");

		confirmacion.setContentText("Todos los pagos acreditados de esta reserva " + "pasarán a estado Reembolsado.");

		confirmacion.showAndWait().ifPresent(respuesta -> {
			if (respuesta == ButtonType.OK) {
				if (administrativa) {
					solicitarMotivoAdministrativo(true);
				} else {
					ejecutarCancelacionYReembolsoNormal();
				}
			}
		});
	}

	private void ejecutarCancelacionYReembolsoNormal() {
		try {
			long reservaId = reservaSeleccionada.getId();
			LocalDate fechaReserva = reservaSeleccionada.getFecha();

			reservaService.cancelarNormal(reservaId);

			pagoService.reembolsarPagosDeReservaAutorizado(reservaId);

			recargarDespuesDeCancelar(fechaReserva,
					"La reserva fue cancelada y los pagos " + "acreditados fueron reembolsados.");

		} catch (RuntimeException exception) {
			mostrarError("La operación no pudo completarse: " + exception.getMessage());
		}
	}

	private void solicitarMotivoAdministrativo(boolean reembolsar) {

		TextInputDialog dialogo = new TextInputDialog();

		dialogo.setTitle("Cancelación administrativa");

		dialogo.setHeaderText("Ingresá el motivo de la cancelación");

		dialogo.setContentText("Motivo:");

		dialogo.getEditor().setPromptText("Ejemplo: lluvia, corte de energía o mantenimiento");

		dialogo.showAndWait().ifPresent(motivo -> {
			if (motivo == null || motivo.isBlank()) {
				mostrarError("El motivo administrativo es obligatorio.");
				return;
			}

			ejecutarCancelacionAdministrativa(motivo, reembolsar);
		});
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
					LocalDate fechaReserva = reservaSeleccionada.getFecha();

					reservaService.cambiarEstado(reservaSeleccionada.getId(), estado);

					cargarReservas();
					nuevaReserva();

					filtroFecha.setValue(fechaReserva);
					aplicarFiltros();

					mostrarInfo("El estado se actualizó correctamente.");
				} catch (RuntimeException exception) {
					mostrarError(exception.getMessage());
				}
			}
		});
	}

	@FXML
	private void abrirPagos() {
		if (reservaSeleccionada == null) {
			mostrarError("Seleccioná una reserva antes de abrir sus pagos.");
			return;
		}

		Navegacion.mostrarPagosDeReserva(reservaSeleccionada.getId());
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

	private void limpiarMensaje() {
		etiquetaMensaje.setText("");

		etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
	}
}
