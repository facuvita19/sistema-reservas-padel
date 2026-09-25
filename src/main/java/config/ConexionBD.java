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

    private static final String ARCHIVO_CONFIGURACION =
            "database.properties";

    private static final String CLAVE_URL = "db.url";
    private static final String CLAVE_USUARIO = "db.user";
    private static final String CLAVE_PASSWORD = "db.password";

    private ConexionBD() {
    }

    public static Connection obtenerConexion() {
        Properties propiedades = cargarPropiedades();

        String url = obtenerObligatoria(propiedades, CLAVE_URL);
        String usuario = obtenerObligatoria(
                propiedades,
                CLAVE_USUARIO
        );
        String password = propiedades.getProperty(
                CLAVE_PASSWORD,
                ""
        );

        try {
            return DriverManager.getConnection(
                    url,
                    usuario,
                    password
            );
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo conectar con la base de datos "
                            + "padel_reservas. Revise MySQL y el archivo "
                            + ARCHIVO_CONFIGURACION + ".",
                    exception
            );
        }
    }

    public static boolean probarConexion() {
        try (Connection conexion = obtenerConexion()) {
            return conexion != null && conexion.isValid(3);
        } catch (SQLException | RuntimeException exception) {
            return false;
        }
    }

    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();
        Path rutaExterna = Paths.get(ARCHIVO_CONFIGURACION)
                .toAbsolutePath()
                .normalize();

        if (Files.isRegularFile(rutaExterna)) {
            try (InputStream entrada =
                    Files.newInputStream(rutaExterna)) {
                propiedades.load(entrada);
                return propiedades;
            } catch (IOException exception) {
                throw new RuntimeException(
                        "No se pudo leer " + rutaExterna + ".",
                        exception
                );
            }
        }

        try (InputStream entrada = ConexionBD.class
                .getClassLoader()
                .getResourceAsStream(ARCHIVO_CONFIGURACION)) {

            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontro " + ARCHIVO_CONFIGURACION
                                + " en el directorio de ejecucion. "
                                + "Copie database.properties.example, "
                                + "renombre la copia y complete sus "
                                + "credenciales de MySQL."
                );
            }

            propiedades.load(entrada);
            return propiedades;

        } catch (IOException exception) {
            throw new RuntimeException(
                    "No se pudo cargar "
                            + ARCHIVO_CONFIGURACION + ".",
                    exception
            );
        }
    }

    private static String obtenerObligatoria(
            Properties propiedades,
            String clave) {

        String valor = propiedades.getProperty(clave);

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Falta configurar la propiedad "
                            + clave
                            + " en "
                            + ARCHIVO_CONFIGURACION
                            + "."
            );
        }

        return valor.trim();
    }
}
