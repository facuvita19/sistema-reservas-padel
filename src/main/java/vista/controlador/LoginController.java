package vista.controlador;

import java.util.Arrays;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import negocio.Usuario;
import servicio.UsuarioService;
import vista.Navegacion;

public class LoginController {

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoPassword;
    @FXML private TextField campoPasswordVisible;
    @FXML private StackPane contenedorPassword;
    @FXML private CheckBox checkMostrarPassword;
    @FXML private Button botonIngresar;
    @FXML private Label etiquetaMensaje;
    @FXML private ProgressIndicator indicadorCarga;

    @FXML
    private void initialize() {
        campoPasswordVisible.textProperty().bindBidirectional(
                campoPassword.textProperty());
        campoPasswordVisible.setVisible(false);
        campoPasswordVisible.setManaged(false);
        etiquetaMensaje.setText("");
        indicadorCarga.setVisible(false);
        Platform.runLater(campoUsuario::requestFocus);
    }

    @FXML
    private void alternarPassword() {
        boolean mostrar = checkMostrarPassword.isSelected();
        campoPasswordVisible.setVisible(mostrar);
        campoPasswordVisible.setManaged(mostrar);
        campoPassword.setVisible(!mostrar);
        campoPassword.setManaged(!mostrar);

        if (mostrar) {
            campoPasswordVisible.requestFocus();
            campoPasswordVisible.positionCaret(
                    campoPasswordVisible.getText().length());
        } else {
            campoPassword.requestFocus();
            campoPassword.positionCaret(campoPassword.getText().length());
        }
    }

    @FXML
    private void iniciarSesion() {
        limpiarMensaje();

        String nombreUsuario = campoUsuario.getText() == null
                ? ""
                : campoUsuario.getText().trim();
        char[] password = obtenerPassword().toCharArray();

        if (nombreUsuario.isBlank() || password.length == 0) {
            Arrays.fill(password, '\0');
            mostrarError("Ingresá el usuario y la contraseña.");
            return;
        }

        cambiarEstadoCarga(true);

        Task<Usuario> tarea = new Task<>() {
            @Override
            protected Usuario call() {
                return usuarioService.iniciarSesion(
                        nombreUsuario,
                        new String(password));
            }
        };

        tarea.setOnSucceeded(evento -> {
            Arrays.fill(password, '\0');
            cambiarEstadoCarga(false);

            Usuario usuario = tarea.getValue();

            if (!usuario.esPersonalDelComplejo()) {
                mostrarError(
                        "Esta aplicación está destinada al personal del complejo.");
                return;
            }

            try {
                Navegacion.mostrarDashboard(usuario);
                limpiarFormulario();
            } catch (RuntimeException exception) {
                exception.printStackTrace();
                mostrarError(exception.getMessage() == null
                        ? "No se pudo abrir el panel administrativo."
                        : exception.getMessage());
            }
        });

        tarea.setOnFailed(evento -> {
            Arrays.fill(password, '\0');
            cambiarEstadoCarga(false);
            Throwable error = tarea.getException();
            mostrarError(error == null || error.getMessage() == null
                    ? "No se pudo iniciar sesión."
                    : error.getMessage());
            campoPassword.selectAll();
            campoPassword.requestFocus();
        });

        Thread hilo = new Thread(tarea, "login-padel-reservas");
        hilo.setDaemon(true);
        hilo.start();
    }

    private String obtenerPassword() {
        return checkMostrarPassword.isSelected()
                ? campoPasswordVisible.getText()
                : campoPassword.getText();
    }

    private void cambiarEstadoCarga(boolean cargando) {
        campoUsuario.setDisable(cargando);
        contenedorPassword.setDisable(cargando);
        checkMostrarPassword.setDisable(cargando);
        botonIngresar.setDisable(cargando);
        indicadorCarga.setVisible(cargando);
        botonIngresar.setText(cargando
                ? "VALIDANDO..."
                : "INICIAR SESIÓN");
    }

    private void mostrarError(String mensaje) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-exito");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) {
            etiquetaMensaje.getStyleClass().add("mensaje-error");
        }
    }

    private void limpiarMensaje() {
        etiquetaMensaje.setText("");
        etiquetaMensaje.getStyleClass().removeAll(
                "mensaje-error",
                "mensaje-exito");
    }

    private void limpiarFormulario() {
        campoUsuario.clear();
        campoPassword.clear();
        campoPasswordVisible.clear();
        checkMostrarPassword.setSelected(false);
    }
}
