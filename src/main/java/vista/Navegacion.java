package vista;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import negocio.Usuario;

public final class Navegacion {

	private static final String CSS_GLOBAL = "/css/tema-padel.css";
	private static Stage escenario;
	private static Usuario usuarioActual;

	private Navegacion() {
	}

	public static void inicializar(Stage escenarioPrincipal) {
		if (escenarioPrincipal == null) {
			throw new IllegalArgumentException("El escenario principal no puede ser nulo.");
		}

		escenario = escenarioPrincipal;
		escenario.setTitle("Padel Reservas");
		escenario.setMinWidth(980);
		escenario.setMinHeight(660);
	}

	public static void mostrarLogin() {
		usuarioActual = null;
		mostrarVista("/fxml/login.fxml", 1100, 720, true);
		escenario.setTitle("Padel Reservas - Iniciar sesion");
	}

	public static void mostrarDashboard(Usuario usuario) {
		if (usuario == null) {
			throw new IllegalArgumentException("El usuario autenticado no puede ser nulo.");
		}

		usuarioActual = usuario;
		mostrarVista("/fxml/dashboard-admin.fxml", 1280, 780, true);
		escenario.setTitle("Padel Reservas - Administracion");
	}

	public static Usuario getUsuarioActual() {
		return usuarioActual;
	}

	public static void cerrarSesion() {
		mostrarLogin();
	}

	private static void mostrarVista(String recursoFXML, double ancho, double alto, boolean maximizable) {

		verificarInicializacion();

		try {
			FXMLLoader cargador = new FXMLLoader(Navegacion.class.getResource(recursoFXML));
			Parent raiz = cargador.load();
			Scene escena = new Scene(raiz, ancho, alto);

			var css = Navegacion.class.getResource(CSS_GLOBAL);
			if (css == null) {
				throw new IllegalStateException("No se encontro " + CSS_GLOBAL);
			}

			escena.getStylesheets().add(css.toExternalForm());
			escenario.setScene(escena);
			escenario.setResizable(maximizable);
			escenario.centerOnScreen();
			escenario.show();

		} catch (IOException exception) {
			exception.printStackTrace();

			throw new IllegalStateException(
					"No se pudo cargar la vista " + recursoFXML + ". Revisá la ubicación del FXML y su controlador.",
					exception);
		}
	}

	private static void verificarInicializacion() {
		if (escenario == null) {
			throw new IllegalStateException("La navegacion no fue inicializada.");
		}
	}
}
