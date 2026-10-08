package vista.controlador;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import negocio.ConfiguracionComplejo;
import servicio.ConfiguracionComplejoService;
import vista.Navegacion;
import vista.TemaDinamico;

public class ConfiguracionController {

	private final ConfiguracionComplejoService configuracionService = new ConfiguracionComplejoService();

	private static final String ACENTO_PREDETERMINADO = "#2F8F83";

	private static final List<Acento> ACENTOS = List.of(
	                new Acento("Turquesa", "#2F8F83"),
	                new Acento("Esmeralda", "#3D8B68"),
	                new Acento("Azul acero", "#527C9B"),
	                new Acento("Violeta", "#7563A8"),
	                new Acento("Ambar", "#B78334"),
	                new Acento("Coral", "#B85F62"));

	private ConfiguracionComplejo configuracionActual;
	private boolean actualizandoApariencia;

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
	private Spinner<Integer> spinnerMinutosReservaPendiente;
	@FXML
	private ColorPicker selectorColor;
	@FXML
	private FlowPane contenedorPaleta;
	@FXML
	private VBox vistaPreviaAcento;
	@FXML
	private Label etiquetaColorActual;
	@FXML
	private Label etiquetaAparienciaPendiente;
	@FXML
	private TextField campoRutaLogo;
	@FXML
	private TextField campoPagoAlias;
	@FXML
	private TextField campoPagoTitular;
	@FXML
	private TextField campoPagoEntidad;
	@FXML
	private javafx.scene.control.TextArea campoPagoInstrucciones;
	@FXML
	private Label etiquetaEjemploSenia;
	@FXML
	private Label etiquetaVencimiento;
	@FXML
	private Label etiquetaMensaje;
	@FXML
	private ProgressIndicator indicadorCarga;
	@FXML
	private Button botonGuardar;

	@FXML
	private void initialize() {
		configurarControles();
		configurarApariencia();
		Platform.runLater(this::cargarConfiguracion);
	}

	private void configurarControles() {
		comboMoneda.getItems().setAll("ARS", "USD", "EUR", "BRL", "UYU");
		comboMoneda.getSelectionModel().select("ARS");

		spinnerAnticipacion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 720, 2));

		spinnerCancelacion.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 720, 12));

		spinnerMinutosReservaPendiente
				.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1440, 15, 1));

		campoPorcentajeSenia.textProperty().addListener((observador, anterior, actual) -> actualizarEjemploSenia());

		comboMoneda.valueProperty().addListener((observador, anterior, actual) -> actualizarEjemploSenia());

		spinnerMinutosReservaPendiente.valueProperty()
				.addListener((observador, anterior, actual) -> actualizarTextoVencimiento());
	}

        private void configurarApariencia() {
                contenedorPaleta.getChildren().clear();
                for (Acento acento : ACENTOS) {
                        javafx.scene.layout.Region muestra =
                                        new javafx.scene.layout.Region();
                        muestra.setMinSize(14, 14);
                        muestra.setPrefSize(14, 14);
                        muestra.setMaxSize(14, 14);
                        muestra.getStyleClass().add("config-accent-swatch");
                        muestra.setStyle("-fx-background-color: "
                                        + acento.color() + ";");

                        Label nombre = new Label(acento.nombre());
                        nombre.getStyleClass().add("config-accent-name");

                        Label marca = new Label("✓");
                        marca.setVisible(false);
                        marca.setManaged(false);
                        marca.getStyleClass().add("config-accent-check");

                        javafx.scene.layout.Region separador =
                                        new javafx.scene.layout.Region();
                        javafx.scene.layout.HBox.setHgrow(separador,
                                        javafx.scene.layout.Priority.ALWAYS);

                        javafx.scene.layout.HBox contenido =
                                        new javafx.scene.layout.HBox(
                                                        7, muestra, nombre,
                                                        separador, marca);
                        contenido.setAlignment(
                                        javafx.geometry.Pos.CENTER_LEFT);
                        contenido.setMouseTransparent(true);

                        Button boton = new Button();
                        boton.setGraphic(contenido);
                        boton.setUserData(acento.color());
                        boton.setPrefWidth(210);
                        boton.setMaxWidth(210);
                        boton.getProperties().put(
                                        "marcaAcento", marca);
                        boton.getStyleClass().add("config-accent-option");
                        boton.setStyle("-config-swatch: "
                                        + acento.color() + ";");
                        boton.setOnAction(evento ->
                                        seleccionarAcento(acento.color()));
                        contenedorPaleta.getChildren().add(boton);
                }
                selectorColor.valueProperty().addListener((obs, anterior, actual) -> {
                        if (!actualizandoApariencia && actual != null) {
                                actualizarVistaPrevia(colorHexadecimal(actual));
                                marcarAparienciaPendiente();
                        }
                });
        }

        private void seleccionarAcento(String color) {
                actualizandoApariencia = true;
                selectorColor.setValue(Color.web(color));
                actualizandoApariencia = false;
                actualizarVistaPrevia(color);
                marcarAparienciaPendiente();
        }

        private void marcarAparienciaPendiente() {
                if (etiquetaAparienciaPendiente == null) return;
                etiquetaAparienciaPendiente.setText(
                                "Vista previa actualizada. Pulsá GUARDAR CONFIGURACIÓN para aplicar el acento en todo el sistema.");
                etiquetaAparienciaPendiente.setVisible(true);
                etiquetaAparienciaPendiente.setManaged(true);
                if (!botonGuardar.getStyleClass().contains(
                                "config-save-button-pending")) {
                        botonGuardar.getStyleClass().add(
                                        "config-save-button-pending");
                }
        }

        private void limpiarAparienciaPendiente() {
                if (etiquetaAparienciaPendiente != null) {
                        etiquetaAparienciaPendiente.setVisible(false);
                        etiquetaAparienciaPendiente.setManaged(false);
                }
                botonGuardar.getStyleClass().remove(
                                "config-save-button-pending");
        }

        private void actualizarVistaPrevia(String color) {
                String normalizado = color == null
                                ? ACENTO_PREDETERMINADO : color.toUpperCase();
                TemaDinamico.aplicarAcento(vistaPreviaAcento, normalizado);
                etiquetaColorActual.setText(normalizado);
                for (javafx.scene.Node nodo : contenedorPaleta.getChildren()) {
                        boolean seleccionado = normalizado.equalsIgnoreCase(
                                        String.valueOf(nodo.getUserData()));
                        nodo.getStyleClass().remove(
                                        "config-accent-option-selected");
                        if (seleccionado) {
                                nodo.getStyleClass().add(
                                                "config-accent-option-selected");
                        }
                        Object marca = nodo.getProperties().get("marcaAcento");
                        if (marca instanceof Label etiquetaMarca) {
                                etiquetaMarca.setVisible(seleccionado);
                                etiquetaMarca.setManaged(seleccionado);
                        }
                }
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
			mostrarExito("Configuración cargada.");
		});

		tarea.setOnFailed(evento -> {
			Throwable error = tarea.getException();
			if (error != null) {
				error.printStackTrace();
			}

			cambiarCarga(false, "No se pudo cargar la configuración.");
			mostrarError(error == null || error.getMessage() == null ? "No se pudo cargar la configuración."
					: error.getMessage());
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
		spinnerMinutosReservaPendiente.getValueFactory().setValue(configuracion.getMinutosReservaPendiente());
		campoRutaLogo.setText(configuracion.getRutaLogo());
		campoPagoAlias.setText(configuracion.getPagoAlias());
		campoPagoTitular.setText(configuracion.getPagoTitular());
		campoPagoEntidad.setText(configuracion.getPagoEntidad());
		campoPagoInstrucciones.setText(configuracion.getPagoInstrucciones());
		try {
			selectorColor.setValue(Color.web(configuracion.getColorPrincipal()));
                        actualizarVistaPrevia(configuracion.getColorPrincipal());
		} catch (IllegalArgumentException exception) {
                        selectorColor.setValue(Color.web(ACENTO_PREDETERMINADO));
                        actualizarVistaPrevia(ACENTO_PREDETERMINADO);
                }

		actualizarEjemploSenia();
		actualizarTextoVencimiento();
                limpiarAparienciaPendiente();
	}

	@FXML
	private void guardar() {
		try {
			ConfiguracionComplejo configuracion =
			        new ConfiguracionComplejo();
			if (configuracionActual != null) {
			        configuracion.setId(
			                configuracionActual.getId());
			}

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
			configuracion.setMinutosReservaPendiente(spinnerMinutosReservaPendiente.getValue());
			configuracion.setColorPrincipal(colorHexadecimal(selectorColor.getValue()));
			configuracion.setRutaLogo(campoRutaLogo.getText());
			configuracion.setPagoAlias(campoPagoAlias.getText());
			configuracion.setPagoTitular(campoPagoTitular.getText());
			configuracion.setPagoEntidad(campoPagoEntidad.getText());
			configuracion.setPagoInstrucciones(campoPagoInstrucciones.getText());
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
			Navegacion.recargarConfiguracion();
                        limpiarAparienciaPendiente();
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
                seleccionarAcento(ACENTO_PREDETERMINADO);
        }

	private void actualizarEjemploSenia() {
		String moneda = comboMoneda.getValue() == null ? "ARS" : comboMoneda.getValue();

		try {
			BigDecimal porcentaje = parsearPorcentaje(campoPorcentajeSenia.getText());

			BigDecimal ejemplo = new BigDecimal("40000").multiply(porcentaje).divide(BigDecimal.valueOf(100), 2,
					java.math.RoundingMode.HALF_UP);

			NumberFormat formato = NumberFormat.getNumberInstance(
			        new Locale("es", "AR"));
			formato.setMinimumFractionDigits(2);
			formato.setMaximumFractionDigits(2);
			etiquetaEjemploSenia.setText(
			        "Ejemplo: para una reserva de "
			                + moneda + " 40.000,00, la seña será "
			                + moneda + " "
			                + formato.format(ejemplo) + ".");
		} catch (IllegalArgumentException exception) {
			etiquetaEjemploSenia.setText("Ingresá un porcentaje válido entre 0 y 100.");
		}
	}

	private void actualizarTextoVencimiento() {
		Integer minutos = spinnerMinutosReservaPendiente.getValue();

		if (minutos == null) {
			etiquetaVencimiento.setText("Ingresá el plazo para pagar la seña.");
			return;
		}

		etiquetaVencimiento.setText("Las reservas iniciadas desde la web se liberarán " + "automáticamente después de "
				+ minutos + (minutos == 1 ? " minuto" : " minutos") + " si la seña no fue acreditada. "
				+ "Las reservas creadas por el personal no vencen automáticamente.");
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

        private record Acento(String nombre, String color) {
        }

        @FXML
        private void volver() {
		Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
	}
        // modernizar-configuracion-unificada-v1
        // cerrar-configuracion-unificada-v5
}
