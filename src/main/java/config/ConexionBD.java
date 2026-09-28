package config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {
    private static final String ARCHIVO_CONFIGURACION = "database.properties";
    private static final String CLAVE_URL = "db.url";
    private static final String CLAVE_USUARIO = "db.user";
    private static final String CLAVE_PASSWORD = "db.password";

    private ConexionBD() { }

    public static Connection obtenerConexion() {
        Properties propiedades = cargarPropiedades();
        String url = obtenerConfiguracion("DB_URL", propiedades, CLAVE_URL, true);
        String usuario = obtenerConfiguracion("DB_USER", propiedades, CLAVE_USUARIO, true);
        String password = obtenerConfiguracion("DB_PASSWORD", propiedades, CLAVE_PASSWORD, false);
        try {
            return DriverManager.getConnection(url, usuario, password);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo conectar con la base de datos. Revisá MySQL y la configuración DB_URL, DB_USER y DB_PASSWORD.",
                    exception);
        }
    }

    public static boolean probarConexion() {
        try (Connection conexion = obtenerConexion()) {
            return conexion != null && conexion.isValid(3);
        } catch (SQLException | RuntimeException exception) {
            return false;
        }
    }

    private static String obtenerConfiguracion(String variable,
            Properties propiedades, String clave, boolean obligatoria) {
        String entorno = System.getenv(variable);
        if (entorno != null && !entorno.isBlank()) return entorno.trim();
        String sistema = System.getProperty(clave);
        if (sistema != null && !sistema.isBlank()) return sistema.trim();
        String archivo = propiedades.getProperty(clave);
        if (archivo != null && !archivo.isBlank()) return archivo.trim();
        if (obligatoria) {
            throw new IllegalStateException(
                    "Falta configurar " + variable + " o " + clave + ".");
        }
        return "";
    }

    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        Path rutaExterna = Paths.get(ARCHIVO_CONFIGURACION)
                .toAbsolutePath().normalize();
        if (Files.isRegularFile(rutaExterna)) {
            try (InputStream entrada = Files.newInputStream(rutaExterna)) {
                propiedades.load(entrada);
                return propiedades;
            } catch (IOException exception) {
                throw new RuntimeException("No se pudo leer " + rutaExterna + ".", exception);
            }
        }
        try (InputStream entrada = ConexionBD.class.getClassLoader()
                .getResourceAsStream(ARCHIVO_CONFIGURACION)) {
            if (entrada != null) propiedades.load(entrada);
            return propiedades;
        } catch (IOException exception) {
            throw new RuntimeException(
                    "No se pudo cargar " + ARCHIVO_CONFIGURACION + ".", exception);
        }
    }
}
