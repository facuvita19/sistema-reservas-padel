package vista.controlador;

import java.io.File;
import java.math.BigDecimal;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import negocio.ConfiguracionComplejo;
import servicio.ConfiguracionComplejoService;
import vista.Navegacion;

public class ConfiguracionController {

	private final ConfiguracionComplejoService configuracionService = new ConfiguracionComplejoService();

	private ConfiguracionComplejo configuracionActual;

	@FXML
	private TextField campoNombreComercial;
	@FXML
	private TextField campoRazonSocial;
	@FXML
	private TextField campoDireccion;
	@FXML
	private TextField campoTelefono;
	@FXML
	private TextField campoWhatsapp;
	@FXML
	private TextField campoEmail;
	@FXML
	private TextField campoInstagram;
	@FXML
	private ComboBox<String> comboMoneda;
	@FXML
	private TextField campoPorcentajeSenia;
	@FXML
	private Spinner<Integer> spinnerAnticipacion;
	@FXML
	private Spinner<Integer> spinnerCancelacion;
	@FXML
	private ColorPicker selectorColor;
	@FXML
	private TextField campoRutaLogo;
	@FXML
	private Label etiquetaEjemploSenia;
	@FXML
	private Label etiquetaMensaje;
	@FXML
	private ProgressIndicator indicadorCarga;
	@FXML
	private Button botonGuardar;

	@FXML
	private void initialize() {
		configurarControles();
		Platform.runLater(this::cargarConfiguracion);
	}

	private void configurarControles() {
		comboMoneda.getItems().setAll("ARS", "USD", "EUR", "BRL", "UYU");
		comboMoneda.getSelectionModel().select("ARS");

		spinnerAnticipacion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 720, 2));
		spinnerCancelacion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 720, 12));

		campoPorcentajeSenia.textProperty().addListener((obs, anterior, actual) -> actualizarEjemploSenia());
		comboMoneda.valueProperty().addListener((obs, anterior, actual) -> actualizarEjemploSenia());
	}

	@FXML
	private void cargarConfiguracion() {
		cambiarCarga(true, "Cargando configuración...");

		Task<ConfiguracionComplejo> tarea = new Task<>() {
			@Override
			protected ConfiguracionComplejo call() {
				return configuracionService.obtener();
			}
		};

		tarea.setOnSucceeded(evento -> {
			configuracionActual = tarea.getValue();
			mostrarConfiguracion(configuracionActual);

			Navegacion.recargarConfiguracion();

			cambiarCarga(false, "Configuración cargada.");
		});

		tarea.setOnFailed(evento -> {
			Throwable error = tarea.getException();
			if (error != null) {
				error.printStackTrace();
			}
			cambiarCarga(false, error == null || error.getMessage() == null ? "No se pudo cargar la configuración."
					: error.getMessage());
			mostrarError(etiquetaMensaje.getText());
		});

		Thread hilo = new Thread(tarea, "configuracion-complejo-carga");
		hilo.setDaemon(true);
		hilo.start();
	}

	private void mostrarConfiguracion(ConfiguracionComplejo configuracion) {
		campoNombreComercial.setText(configuracion.getNombreComercial());
		campoRazonSocial.setText(configuracion.getRazonSocial());
		campoDireccion.setText(configuracion.getDireccion());
		campoTelefono.setText(configuracion.getTelefono());
		campoWhatsapp.setText(configuracion.getWhatsapp());
		campoEmail.setText(configuracion.getEmail());
		campoInstagram.setText(configuracion.getInstagram());
		comboMoneda.setValue(configuracion.getMoneda());
		campoPorcentajeSenia.setText(configuracion.getPorcentajeSenia().toPlainString());
		spinnerAnticipacion.getValueFactory().setValue(configuracion.getAnticipacionMinimaHoras());
		spinnerCancelacion.getValueFactory().setValue(configuracion.getCancelacionMinimaHoras());
		campoRutaLogo.setText(configuracion.getRutaLogo());

		try {
			selectorColor.setValue(Color.web(configuracion.getColorPrincipal()));
		} catch (IllegalArgumentException exception) {
			selectorColor.setValue(Color.web("#486B86"));
		}

		actualizarEjemploSenia();
	}

	@FXML
	private void guardar() {
		try {
			ConfiguracionComplejo configuracion = configuracionActual == null ? new ConfiguracionComplejo()
					: configuracionActual;

			configuracion.setNombreComercial(campoNombreComercial.getText());
			configuracion.setRazonSocial(campoRazonSocial.getText());
			configuracion.setDireccion(campoDireccion.getText());
			configuracion.setTelefono(campoTelefono.getText());
			configuracion.setWhatsapp(campoWhatsapp.getText());
			configuracion.setEmail(campoEmail.getText());
			configuracion.setInstagram(campoInstagram.getText());
			configuracion.setMoneda(comboMoneda.getValue());
			configuracion.setPorcentajeSenia(parsearPorcentaje(campoPorcentajeSenia.getText()));
			configuracion.setAnticipacionMinimaHoras(spinnerAnticipacion.getValue());
			configuracion.setCancelacionMinimaHoras(spinnerCancelacion.getValue());
			configuracion.setColorPrincipal(colorHexadecimal(selectorColor.getValue()));
			configuracion.setRutaLogo(campoRutaLogo.getText());

			cambiarCarga(true, "Guardando configuración...");
			guardarEnSegundoPlano(configuracion);

		} catch (IllegalArgumentException exception) {
			mostrarError(exception.getMessage());
		}
	}

	private void guardarEnSegundoPlano(ConfiguracionComplejo configuracion) {
		Task<Void> tarea = new Task<>() {
			@Override
			protected Void call() {
				configuracionService.guardar(configuracion);
				return null;
			}
		};

		tarea.setOnSucceeded(evento -> {
			configuracionActual = configuracion;
			mostrarConfiguracion(configuracion);
			cambiarCarga(false, "La configuración se guardó correctamente.");
			mostrarExito("La configuración se guardó correctamente.");
		});

		tarea.setOnFailed(evento -> {
			Throwable error = tarea.getException();
			if (error != null) {
				error.printStackTrace();
			}
			cambiarCarga(false, "No se pudo guardar la configuración.");
			mostrarError(error == null || error.getMessage() == null ? "No se pudo guardar la configuración."
					: error.getMessage());
		});

		Thread hilo = new Thread(tarea, "configuracion-complejo-guardado");
		hilo.setDaemon(true);
		hilo.start();
	}

	@FXML
	private void seleccionarLogo() {
		FileChooser selector = new FileChooser();
		selector.setTitle("Seleccionar logo del complejo");
		selector.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.webp"));

		File archivo = selector.showOpenDialog(campoRutaLogo.getScene().getWindow());
		if (archivo != null) {
			campoRutaLogo.setText(archivo.getAbsolutePath());
		}
	}

	@FXML
	private void restaurarColor() {
		selectorColor.setValue(Color.web("#486B86"));
	}

	private void actualizarEjemploSenia() {
		String moneda = comboMoneda.getValue() == null ? "ARS" : comboMoneda.getValue();
		try {
			BigDecimal porcentaje = parsearPorcentaje(campoPorcentajeSenia.getText());
			BigDecimal ejemplo = new BigDecimal("40000").multiply(porcentaje).divide(BigDecimal.valueOf(100), 2,
					java.math.RoundingMode.HALF_UP);
			etiquetaEjemploSenia.setText("Ejemplo: para una reserva de " + moneda + " 40.000, la seña será " + moneda
					+ " " + ejemplo.toPlainString() + ".");
		} catch (IllegalArgumentException exception) {
			etiquetaEjemploSenia.setText("Ingresá un porcentaje válido entre 0 y 100.");
		}
	}

	private BigDecimal parsearPorcentaje(String valor) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("Ingresá el porcentaje de seña.");
		}
		try {
			return new BigDecimal(valor.trim().replace(",", "."));
		} catch (NumberFormatException exception) {
			throw new IllegalArgumentException("El porcentaje de seña debe ser numérico.");
		}
	}

	private String colorHexadecimal(Color color) {
		return String.format("#%02X%02X%02X", Math.round(color.getRed() * 255), Math.round(color.getGreen() * 255),
				Math.round(color.getBlue() * 255));
	}

	private void cambiarCarga(boolean cargando, String mensaje) {
		indicadorCarga.setVisible(cargando);
		botonGuardar.setDisable(cargando);
		etiquetaMensaje.setText(mensaje);
	}

	private void mostrarError(String mensaje) {
		etiquetaMensaje.setText(mensaje == null ? "Ocurrió un error." : mensaje);
		etiquetaMensaje.getStyleClass().remove("mensaje-exito");
		if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) {
			etiquetaMensaje.getStyleClass().add("mensaje-error");
		}
	}

	private void mostrarExito(String mensaje) {
		etiquetaMensaje.setText(mensaje);
		etiquetaMensaje.getStyleClass().remove("mensaje-error");
		if (!etiquetaMensaje.getStyleClass().contains("mensaje-exito")) {
			etiquetaMensaje.getStyleClass().add("mensaje-exito");
		}
	}

	@FXML
	private void volver() {
		Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
	}
}
