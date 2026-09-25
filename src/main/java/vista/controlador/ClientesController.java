package vista.controlador;

import java.util.List;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.Cliente;
import servicio.ClienteService;
import vista.Navegacion;

public class ClientesController {

	private final ClienteService clienteService = new ClienteService();
	private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();

	private FilteredList<Cliente> clientesFiltrados;
	private Cliente clienteSeleccionado;

	@FXML
	private TextField campoBuscar;
	@FXML
	private TableView<Cliente> tablaClientes;
	@FXML
	private TableColumn<Cliente, String> columnaNombre;
	@FXML
	private TableColumn<Cliente, String> columnaDocumento;
	@FXML
	private TableColumn<Cliente, String> columnaTelefono;
	@FXML
	private TableColumn<Cliente, String> columnaEmail;
	@FXML
	private TableColumn<Cliente, String> columnaEstado;

	@FXML
	private Label tituloFormulario;
	@FXML
	private Label etiquetaMensaje;
	@FXML
	private TextField campoNombre;
	@FXML
	private TextField campoApellido;
	@FXML
	private TextField campoDocumento;
	@FXML
	private TextField campoTelefono;
	@FXML
	private TextField campoEmail;
	@FXML
	private Button botonGuardar;
	@FXML
	private Button botonDesactivar;

	@FXML
	private void initialize() {
		configurarTabla();
		configurarBusqueda();
		cargarClientes();
		nuevo();
	}

	private void configurarTabla() {
		columnaNombre.setCellValueFactory(
				datos -> new javafx.beans.property.SimpleStringProperty(datos.getValue().getNombreCompleto()));
		columnaDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));
		columnaTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
		columnaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		columnaEstado.setCellValueFactory(datos -> new javafx.beans.property.SimpleStringProperty(
				datos.getValue().isActivo() ? "Activo" : "Inactivo"));

		tablaClientes.getSelectionModel().selectedItemProperty().addListener((observable, anterior, actual) -> {
			if (actual != null) {
				editar(actual);
			}
		});
	}

	private void configurarBusqueda() {
		clientesFiltrados = new FilteredList<>(clientes, cliente -> true);
		tablaClientes.setItems(clientesFiltrados);

		campoBuscar.textProperty().addListener((obs, anterior, texto) -> {
			String filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);

			clientesFiltrados.setPredicate(cliente -> filtro.isBlank() || contiene(cliente.getNombreCompleto(), filtro)
					|| contiene(cliente.getDocumento(), filtro) || contiene(cliente.getTelefono(), filtro)
					|| contiene(cliente.getEmail(), filtro));
		});
	}

	private boolean contiene(String valor, String filtro) {
		return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
	}

	@FXML
	private void cargarClientes() {
		try {
			List<Cliente> resultado = clienteService.listar();
			clientes.setAll(resultado);
			mostrarInfo(resultado.size() + " cliente(s) cargado(s).");
		} catch (RuntimeException exception) {
			mostrarError(exception.getMessage());
		}
	}

	@FXML
	private void nuevo() {
		clienteSeleccionado = null;
		tablaClientes.getSelectionModel().clearSelection();
		tituloFormulario.setText("Nuevo cliente");
		botonGuardar.setText("GUARDAR CLIENTE");
		botonDesactivar.setVisible(false);
		botonDesactivar.setManaged(false);

		campoNombre.clear();
		campoApellido.clear();
		campoDocumento.clear();
		campoTelefono.clear();
		campoEmail.clear();
		etiquetaMensaje.setText("");
		campoNombre.requestFocus();
	}

	private void editar(Cliente cliente) {
		clienteSeleccionado = cliente;
		tituloFormulario.setText("Editar cliente");
		botonGuardar.setText("GUARDAR CAMBIOS");
		botonDesactivar.setVisible(true);
		botonDesactivar.setManaged(true);

		campoNombre.setText(cliente.getNombre());
		campoApellido.setText(cliente.getApellido());
		campoDocumento.setText(cliente.getDocumento());
		campoTelefono.setText(cliente.getTelefono());
		campoEmail.setText(cliente.getEmail());
		etiquetaMensaje.setText("");
	}

	@FXML
	private void guardar() {
		try {
			Cliente cliente = clienteSeleccionado == null ? new Cliente() : clienteSeleccionado;

			cliente.setNombre(campoNombre.getText());
			cliente.setApellido(campoApellido.getText());
			cliente.setDocumento(campoDocumento.getText());
			cliente.setTelefono(campoTelefono.getText());
			cliente.setEmail(campoEmail.getText());
			cliente.setActivo(true);

			clienteService.guardar(cliente);
			cargarClientes();
			nuevo();
			mostrarInfo("El cliente se guardó correctamente.");

		} catch (IllegalArgumentException exception) {
			mostrarError(exception.getMessage());
		} catch (RuntimeException exception) {
			mostrarError("No se pudo guardar el cliente: " + exception.getMessage());
		}
	}

	@FXML
	private void desactivar() {
		if (clienteSeleccionado == null) {
			return;
		}

		Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
		confirmacion.setTitle("Desactivar cliente");
		confirmacion.setHeaderText("¿Desactivar a " + clienteSeleccionado.getNombreCompleto() + "?");
		confirmacion.setContentText(
				"El historial se conservará, pero el cliente dejará " + "de aparecer en los listados activos.");

		confirmacion.showAndWait().ifPresent(respuesta -> {
			if (respuesta == javafx.scene.control.ButtonType.OK) {
				try {
					clienteService.eliminar(clienteSeleccionado.getId());
					cargarClientes();
					nuevo();
					mostrarInfo("El cliente fue desactivado.");
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
