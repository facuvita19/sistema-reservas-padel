package vista.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import negocio.Usuario;
import vista.Navegacion;

public class DashboardAdminController {

	@FXML
	private Label etiquetaUsuario;

	@FXML
	private Label etiquetaFecha;

	@FXML
	private void initialize() {
		Usuario usuario = Navegacion.getUsuarioActual();
		etiquetaUsuario.setText(usuario == null ? "Administrador" : usuario.getNombreUsuario());

		DateTimeFormatter formato = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "AR"));
		etiquetaFecha.setText(capitalizar(LocalDate.now().format(formato)));
	}

	@FXML
	private void abrirCanchas() {
		Navegacion.mostrarCanchas();
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
}
