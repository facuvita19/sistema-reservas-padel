package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.ResumenDashboard;
import negocio.Usuario;
import servicio.DashboardService;
import vista.Navegacion;

public class DashboardAdminController {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

	private final DashboardService dashboardService = new DashboardService();

	@FXML
	private Label etiquetaUsuario;
	@FXML
	private Label etiquetaFecha;
	@FXML
	private Label etiquetaReservasHoy;
	@FXML
	private Label etiquetaCanchasActivas;
	@FXML
	private Label etiquetaIngresosMes;
	@FXML
	private Label etiquetaPagosPendientes;
	@FXML
	private Label etiquetaEstadoCarga;
	@FXML
	private ProgressIndicator indicadorCarga;
	@FXML
	private Button botonActualizar;

	@FXML
	private TableView<Reserva> tablaProximasReservas;
	@FXML
	private TableColumn<Reserva, LocalDate> columnaFecha;
	@FXML
	private TableColumn<Reserva, LocalTime> columnaHora;
	@FXML
	private TableColumn<Reserva, String> columnaCancha;
	@FXML
	private TableColumn<Reserva, String> columnaCliente;
	@FXML
	private TableColumn<Reserva, EstadoReserva> columnaEstado;

	@FXML
	private void initialize() {
		mostrarDatosSesion();
		configurarTabla();
		Platform.runLater(this::actualizarDashboard);
	}

	private void mostrarDatosSesion() {
		Usuario usuario = Navegacion.getUsuarioActual();
		etiquetaUsuario.setText(usuario == null ? "Administrador" : usuario.getNombreUsuario());

		DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "AR"));
		etiquetaFecha.setText(capitalizar(LocalDate.now().format(formato)));
	}

	private void configurarTabla() {
		columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
		columnaFecha.setCellFactory(columna -> new TableCell<>() {
			@Override
			protected void updateItem(LocalDate fecha, boolean vacia) {
				super.updateItem(fecha, vacia);
				setText(vacia || fecha == null ? null : fecha.format(FORMATO_FECHA));
			}
		});

		columnaHora.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
		columnaHora.setCellFactory(columna -> new TableCell<>() {
			@Override
			protected void updateItem(LocalTime hora, boolean vacia) {
				super.updateItem(hora, vacia);
				setText(vacia || hora == null ? null : hora.format(FORMATO_HORA));
			}
		});

		columnaCancha.setCellValueFactory(new PropertyValueFactory<>("nombreCancha"));
		columnaCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
		columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

		tablaProximasReservas.setRowFactory(tabla -> {
			javafx.scene.control.TableRow<Reserva> fila = new javafx.scene.control.TableRow<>();
			fila.setOnMouseClicked(evento -> {
				if (evento.getClickCount() == 2 && !fila.isEmpty()) {
					Navegacion.mostrarReservas();
				}
			});
			return fila;
		});
	}

	@FXML
	private void actualizarDashboard() {
		cambiarEstadoCarga(true, "Actualizando información...");

		Task<DatosDashboard> tarea = new Task<>() {
			@Override
			protected DatosDashboard call() {
				ResumenDashboard resumen = dashboardService.obtenerResumen();
				List<Reserva> proximas = dashboardService.listarProximasReservas();
				return new DatosDashboard(resumen, proximas);
			}
		};

		tarea.setOnSucceeded(evento -> {
			DatosDashboard datos = tarea.getValue();
			mostrarResumen(datos.resumen());
			tablaProximasReservas.getItems().setAll(datos.proximas());
			cambiarEstadoCarga(false, "Datos actualizados correctamente.");
		});

		tarea.setOnFailed(evento -> {
			Throwable error = tarea.getException();
			if (error != null) {
				error.printStackTrace();
			}
			cambiarEstadoCarga(false,
					error == null || error.getMessage() == null ? "No se pudo actualizar el dashboard."
							: error.getMessage());
		});

		Thread hilo = new Thread(tarea, "dashboard-padel-reservas");
		hilo.setDaemon(true);
		hilo.start();
	}

	private void mostrarResumen(ResumenDashboard resumen) {
		etiquetaReservasHoy.setText(String.valueOf(resumen.getReservasHoy()));
		etiquetaCanchasActivas.setText(String.valueOf(resumen.getCanchasActivas()));
		etiquetaIngresosMes.setText(formatearMoneda(resumen.getIngresosMes()));
		etiquetaPagosPendientes.setText(String.valueOf(resumen.getPagosPendientes()));
	}

	private String formatearMoneda(BigDecimal importe) {
		BigDecimal valor = importe == null ? BigDecimal.ZERO : importe.setScale(2, RoundingMode.HALF_UP);
		NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
		return formato.format(valor);
	}

	private void cambiarEstadoCarga(boolean cargando, String mensaje) {
		indicadorCarga.setVisible(cargando);
		botonActualizar.setDisable(cargando);
		etiquetaEstadoCarga.setText(mensaje);
		etiquetaEstadoCarga.getStyleClass().removeAll("dashboard-status-ok", "dashboard-status-error");

		if (!cargando) {
			String texto = mensaje == null ? "" : mensaje.toLowerCase();
			etiquetaEstadoCarga.getStyleClass()
					.add(texto.contains("no se pudo") || texto.contains("error") ? "dashboard-status-error"
							: "dashboard-status-ok");
		}
	}

	@FXML
	private void abrirCanchas() {
		Navegacion.mostrarCanchas();
	}

	@FXML
	private void abrirClientes() {
		Navegacion.mostrarClientes();
	}

	@FXML
	private void abrirReservas() {
		Navegacion.mostrarReservas();
	}

	@FXML
	private void abrirPagos() {
		Navegacion.mostrarPagos();
	}

	@FXML
	private void abrirBloqueos() {
		Navegacion.mostrarBloqueos();
	}

	@FXML
	private void abrirEstadisticas() {
		Navegacion.mostrarEstadisticas();
	}
	
	@FXML
	private void abrirConfiguracion() {
	    Navegacion.mostrarConfiguracion();
	}

	@FXML
	private void cerrarSesion() {
		Navegacion.cerrarSesion();
	}

	private String capitalizar(String texto) {
		if (texto == null || texto.isBlank()) {
			return "";
		}
		return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
	}

	private record DatosDashboard(ResumenDashboard resumen, List<Reserva> proximas) {
	}
}
