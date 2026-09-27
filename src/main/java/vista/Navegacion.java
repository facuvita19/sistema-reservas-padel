package vista;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import negocio.ConfiguracionComplejo;
import negocio.Usuario;
import servicio.ConfiguracionComplejoService;
import vista.controlador.DashboardAdminController;

public final class Navegacion {

	private static final String CSS_GLOBAL = "/css/tema-padel.css";

	private static final String[] CSS_OPCIONALES = { "/css/canchas.css", "/css/clientes.css", "/css/reservas.css",
			"/css/bloqueos.css", "/css/pagos.css", "/css/dashboard.css", "/css/estadisticas.css",
			"/css/configuracion.css", "/css/agenda.css", "/css/usuarios.css", "/css/cierre-caja.css",
			"/css/solicitudes-web.css" };

	private static final ConfiguracionComplejoService configuracionService = new ConfiguracionComplejoService();

	private static ConfiguracionComplejo configuracionActual = new ConfiguracionComplejo();

	private static SolicitudReservaAgenda solicitudReservaAgenda;
	private static SolicitudPagoReserva solicitudPagoReserva;
	private static SolicitudFiltroReservas solicitudFiltroReservas;
	private static SolicitudFiltroPagos solicitudFiltroPagos;

	private static Stage escenario;
	private static Usuario usuarioActual;
	private static Object controladorActual;

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
		recargarConfiguracion();
	}

	public static void recargarConfiguracion() {
		try {
			configuracionActual = configuracionService.obtener();

			if (escenario != null && escenario.getScene() != null) {
				TemaDinamico.aplicar(escenario.getScene().getRoot(), configuracionActual);
			}
		} catch (RuntimeException exception) {
			exception.printStackTrace();
			configuracionActual = new ConfiguracionComplejo();
		}
	}

	public static ConfiguracionComplejo getConfiguracionActual() {
		return configuracionActual;
	}

	public static void mostrarLogin() {
		usuarioActual = null;
		limpiarSolicitudes();
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

	public static void actualizarDashboardSiEstaVisible() {
		Object controlador = controladorActual;

		if (controlador instanceof DashboardAdminController dashboard) {
			dashboard.actualizarDashboard();
		}
	}

	public static void mostrarCanchas() {
		verificarSesion();
		verificarAdministrador();
		mostrarVista("/fxml/canchas.fxml", 1360, 820, true);
		escenario.setTitle("Padel Reservas - Canchas");
	}

	public static void mostrarClientes() {
		verificarSesion();
		mostrarVista("/fxml/clientes.fxml", 1360, 820, true);
		escenario.setTitle("Padel Reservas - Clientes");
	}

	public static void mostrarReservas() {
		verificarSesion();
		mostrarVista("/fxml/reservas.fxml", 1400, 840, true);
		escenario.setTitle("Padel Reservas - Reservas");
	}

	public static void mostrarBloqueos() {
		verificarSesion();
		mostrarVista("/fxml/bloqueos.fxml", 1360, 820, true);
		escenario.setTitle("Padel Reservas - Bloqueos");
	}

	public static void mostrarPagos() {
		verificarSesion();
		mostrarVista("/fxml/pagos.fxml", 1420, 850, true);
		escenario.setTitle("Padel Reservas - Pagos");
	}

	public static void mostrarEstadisticas() {
		verificarSesion();
		verificarAdministrador();
		mostrarVista("/fxml/estadisticas.fxml", 1320, 780, true);
		escenario.setTitle("Padel Reservas - Estadísticas");
	}

	public static void mostrarConfiguracion() {
		verificarSesion();
		verificarAdministrador();
		mostrarVista("/fxml/configuracion.fxml", 1280, 800, true);
		escenario.setTitle("Padel Reservas - Configuración");
	}

	public static void mostrarAgenda() {
		verificarSesion();
		mostrarVista("/fxml/agenda.fxml", 1380, 840, true);
		escenario.setTitle(configuracionActual.getNombreComercial() + " - Agenda");
	}

	public static void mostrarCierreCaja() {
		verificarSesion();
		mostrarVista("/fxml/cierre-caja.fxml", 1220, 820, true);
		escenario.setTitle("Padel Reservas - Cierre diario de caja");
	}

	public static void mostrarSolicitudesWeb() {
		verificarSesion();
		mostrarVista("/fxml/solicitudes-web.fxml", 1400, 840, true);
		escenario.setTitle("Padel Reservas - Solicitudes web");
	}

	public static void mostrarUsuarios() {
		verificarSesion();
		verificarAdministrador();
		mostrarVista("/fxml/usuarios.fxml", 1180, 760, true);
		escenario.setTitle("Padel Reservas - Usuarios");
	}

	public static void mostrarReservasConFiltro(FiltroReservas filtro) {
		solicitudFiltroReservas = new SolicitudFiltroReservas(filtro);
		mostrarReservas();
	}

	public static SolicitudFiltroReservas consumirSolicitudFiltroReservas() {
		SolicitudFiltroReservas solicitud = solicitudFiltroReservas;
		solicitudFiltroReservas = null;
		return solicitud;
	}

	public static void mostrarPagosConFiltro(FiltroPagos filtro) {
		solicitudFiltroPagos = new SolicitudFiltroPagos(filtro);
		mostrarPagos();
	}

	public static SolicitudFiltroPagos consumirSolicitudFiltroPagos() {
		SolicitudFiltroPagos solicitud = solicitudFiltroPagos;
		solicitudFiltroPagos = null;
		return solicitud;
	}

	public static void mostrarNuevaReservaDesdeAgenda(java.time.LocalDate fecha, long canchaId,
			java.time.LocalTime horaInicio) {

		solicitudReservaAgenda = SolicitudReservaAgenda.nueva(fecha, canchaId, horaInicio);
		mostrarReservas();
	}

	public static void mostrarReservaDesdeAgenda(long reservaId) {
		solicitudReservaAgenda = SolicitudReservaAgenda.existente(reservaId);
		mostrarReservas();
	}

	public static SolicitudReservaAgenda consumirSolicitudReservaAgenda() {
		SolicitudReservaAgenda solicitud = solicitudReservaAgenda;
		solicitudReservaAgenda = null;
		return solicitud;
	}

	public static void mostrarPagosDeReserva(long reservaId) {
		solicitudPagoReserva = new SolicitudPagoReserva(reservaId);
		mostrarPagos();
	}

	public static SolicitudPagoReserva consumirSolicitudPagoReserva() {
		SolicitudPagoReserva solicitud = solicitudPagoReserva;
		solicitudPagoReserva = null;
		return solicitud;
	}

	public static Usuario getUsuarioActual() {
		return usuarioActual;
	}

	public static void cerrarSesion() {
		mostrarLogin();
	}

	private static void limpiarSolicitudes() {
		solicitudReservaAgenda = null;
		solicitudPagoReserva = null;
		solicitudFiltroReservas = null;
		solicitudFiltroPagos = null;
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
			Object nuevoControlador = cargador.getController();

			TemaDinamico.aplicar(raiz, configuracionActual);

			Scene escena = new Scene(raiz, ancho, alto);
			agregarCssObligatorio(escena, CSS_GLOBAL);

			for (String css : CSS_OPCIONALES) {
				agregarCssOpcional(escena, css);
			}

			Rectangle2D areaVisible = Screen.getPrimary().getVisualBounds();

			double anchoSeguro = Math.min(ancho, areaVisible.getWidth() * 0.95);
			double altoSeguro = Math.min(alto, areaVisible.getHeight() * 0.92);

			boolean estabaMaximizada = escenario.isMaximized();
			boolean estabaVisible = escenario.isShowing();

			escenario.setFullScreen(false);
			escenario.setResizable(redimensionable);
			escenario.setScene(escena);
			controladorActual = nuevoControlador;

			if (!estabaVisible) {
				escenario.setWidth(anchoSeguro);
				escenario.setHeight(altoSeguro);
				escenario.centerOnScreen();
			}

			escenario.show();

			if (estabaMaximizada) {
				escenario.setMaximized(true);
			}

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

	private static void verificarAdministrador() {
		if (usuarioActual == null || !usuarioActual.esAdministrador()) {
			throw new IllegalStateException("Esta función requiere permisos de administrador.");
		}
	}
}
