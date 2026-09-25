package vista;

import java.util.Locale;

import javafx.scene.Parent;
import javafx.scene.paint.Color;
import negocio.ConfiguracionComplejo;

public final class TemaDinamico {

	private static final String COLOR_PREDETERMINADO = "#486B86";

	private TemaDinamico() {
	}

	public static void aplicar(Parent raiz, ConfiguracionComplejo configuracion) {

		if (raiz == null) {
			return;
		}

		String colorPrincipal = obtenerColor(configuracion);
		String colorHover = ajustarBrillo(colorPrincipal, 1.18);
		String colorPresionado = ajustarBrillo(colorPrincipal, 0.78);
		String colorTransparente = convertirRgba(colorPrincipal, 0.22);

		String estilo = """
				-color-principal: %s;
				-color-principal-hover: %s;
				-color-principal-presionado: %s;
				-color-principal-transparente: %s;
				""".formatted(colorPrincipal, colorHover, colorPresionado, colorTransparente);

		raiz.setStyle(estilo);
	}

	private static String obtenerColor(ConfiguracionComplejo configuracion) {

		if (configuracion == null || configuracion.getColorPrincipal() == null
				|| configuracion.getColorPrincipal().isBlank()) {

			return COLOR_PREDETERMINADO;
		}

		try {
			Color.web(configuracion.getColorPrincipal());
			return configuracion.getColorPrincipal().trim().toUpperCase(Locale.ROOT);

		} catch (IllegalArgumentException exception) {
			return COLOR_PREDETERMINADO;
		}
	}

	private static String ajustarBrillo(String colorHexadecimal, double factor) {

		Color color = Color.web(colorHexadecimal);

		int rojo = limitar((int) Math.round(color.getRed() * 255 * factor));

		int verde = limitar((int) Math.round(color.getGreen() * 255 * factor));

		int azul = limitar((int) Math.round(color.getBlue() * 255 * factor));

		return String.format("#%02X%02X%02X", rojo, verde, azul);
	}

	private static String convertirRgba(String colorHexadecimal, double opacidad) {

		Color color = Color.web(colorHexadecimal);

		int rojo = (int) Math.round(color.getRed() * 255);
		int verde = (int) Math.round(color.getGreen() * 255);
		int azul = (int) Math.round(color.getBlue() * 255);

		return String.format(Locale.ROOT, "rgba(%d, %d, %d, %.2f)", rojo, verde, azul, opacidad);
	}

	private static int limitar(int valor) {
		return Math.max(0, Math.min(255, valor));
	}
}
