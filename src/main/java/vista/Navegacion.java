package vista;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import negocio.Usuario;

public final class Navegacion {

	private static final String CSS_GLOBAL = "/css/tema-padel.css";
	private static final String CSS_CANCHAS = "/css/canchas.css";

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
		escenario.setTitle("Padel Reservas - Iniciar sesión");
	}

	public static void mostrarDashboard(Usuario usuario) {
		if (usuario == null) {
			throw new IllegalArgumentException("El usuario autenticado no puede ser nulo.");
		}

		usuarioActual = usuario;
		mostrarVista("/fxml/dashboard-admin.fxml", 1280, 780, true);
		escenario.setTitle("Padel Reservas - Administración");
	}

	public static void mostrarCanchas() {
		verificarSesion();
		mostrarVista("/fxml/canchas.fxml", 1360, 820, true);
		escenario.setTitle("Padel Reservas - Canchas");
	}

	public static Usuario getUsuarioActual() {
		return usuarioActual;
	}

	public static void cerrarSesion() {
		mostrarLogin();
	}

	private static void mostrarVista(String recursoFXML, double ancho, double alto, boolean redimensionable) {

		verificarInicializacion();

		try {
			URL ubicacion = Navegacion.class.getResource(recursoFXML);
			if (ubicacion == null) {
				throw new IllegalStateException("No se encontró " + recursoFXML);
			}

			FXMLLoader cargador = new FXMLLoader(ubicacion);
			Parent raiz = cargador.load();
			Scene escena = new Scene(raiz, ancho, alto);

			agregarCssObligatorio(escena, CSS_GLOBAL);
			agregarCssOpcional(escena, CSS_CANCHAS);

			escenario.setScene(escena);
			escenario.setResizable(redimensionable);
			escenario.centerOnScreen();
			escenario.show();

		} catch (IOException exception) {
			exception.printStackTrace();
			throw new IllegalStateException("No se pudo cargar la vista " + recursoFXML, exception);
		}
	}

	private static void agregarCssObligatorio(Scene escena, String recurso) {

		URL css = Navegacion.class.getResource(recurso);
		if (css == null) {
			throw new IllegalStateException("No se encontró " + recurso);
		}
		escena.getStylesheets().add(css.toExternalForm());
	}

	private static void agregarCssOpcional(Scene escena, String recurso) {

		URL css = Navegacion.class.getResource(recurso);
		if (css != null) {
			escena.getStylesheets().add(css.toExternalForm());
		}
	}

	private static void verificarInicializacion() {
		if (escenario == null) {
			throw new IllegalStateException("La navegación no fue inicializada.");
		}
	}

	private static void verificarSesion() {
		if (usuarioActual == null) {
			mostrarLogin();
			throw new IllegalStateException("La sesión administrativa finalizó.");
		}
	}
}
