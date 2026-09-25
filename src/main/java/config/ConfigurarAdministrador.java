package config;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

import util.ProtectorPassword;

public final class ConfigurarAdministrador {

    private static final String USUARIO_ADMIN = "admin";

    private ConfigurarAdministrador() {
    }

    public static void main(String[] args) {
        try {
            char[] password = solicitarPassword();

            try {
                String hash = ProtectorPassword.generarHash(
                        new String(password)
                );

                guardarAdministrador(hash);

                System.out.println();
                System.out.println(
                        "La cuenta administrativa se configuro "
                                + "correctamente."
                );
                System.out.println(
                        "Usuario: " + USUARIO_ADMIN
                );

            } finally {
                Arrays.fill(password, '\0');
            }

        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            System.exit(1);

        } catch (RuntimeException exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
            System.exit(1);
        }
    }

    private static char[] solicitarPassword() {
        Console consola = System.console();

        char[] password;
        char[] confirmacion;

        if (consola != null) {
            password = consola.readPassword(
                    "Ingrese la contrasena para admin: "
            );
            confirmacion = consola.readPassword(
                    "Repita la contrasena: "
            );
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.print(
                    "Ingrese la contrasena para admin: "
            );
            password = scanner.nextLine().toCharArray();
            System.out.print("Repita la contrasena: ");
            confirmacion = scanner.nextLine().toCharArray();
        }

        try {
            if (!Arrays.equals(password, confirmacion)) {
                throw new IllegalArgumentException(
                        "Las contrasenas no coinciden."
                );
            }

            if (password.length < 8) {
                throw new IllegalArgumentException(
                        "La contrasena debe tener al menos "
                                + "8 caracteres."
                );
            }

            return password;

        } finally {
            Arrays.fill(confirmacion, '\0');
        }
    }

    private static void guardarAdministrador(String hash) {
        try (Connection conexion =
                ConexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {
                Long idExistente = buscarAdministrador(conexion);

                if (idExistente == null) {
                    insertarAdministrador(conexion, hash);
                } else {
                    actualizarAdministrador(
                            conexion,
                            idExistente,
                            hash
                    );
                }

                conexion.commit();

            } catch (SQLException exception) {
                conexion.rollback();
                throw exception;

            } finally {
                conexion.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo configurar la cuenta "
                            + "administrativa en MySQL.",
                    exception
            );
        }
    }

    private static Long buscarAdministrador(
            Connection conexion)
            throws SQLException {

        String sql =
                "SELECT id FROM usuarios "
                + "WHERE nombre_usuario = ?";

        try (PreparedStatement sentencia =
                conexion.prepareStatement(sql)) {

            sentencia.setString(1, USUARIO_ADMIN);

            try (ResultSet resultado =
                    sentencia.executeQuery()) {

                return resultado.next()
                        ? resultado.getLong("id")
                        : null;
            }
        }
    }

    private static void insertarAdministrador(
            Connection conexion,
            String hash)
            throws SQLException {

        String sql =
                "INSERT INTO usuarios "
                + "(nombre_usuario, password_hash, rol, "
                + "cliente_id, activo) "
                + "VALUES (?, ?, 'ADMINISTRADOR', NULL, TRUE)";

        try (PreparedStatement sentencia =
                conexion.prepareStatement(sql)) {

            sentencia.setString(1, USUARIO_ADMIN);
            sentencia.setString(2, hash);
            sentencia.executeUpdate();
        }
    }

    private static void actualizarAdministrador(
            Connection conexion,
            long id,
            String hash)
            throws SQLException {

        String sql =
                "UPDATE usuarios "
                + "SET password_hash = ?, "
                + "rol = 'ADMINISTRADOR', "
                + "cliente_id = NULL, "
                + "activo = TRUE "
                + "WHERE id = ?";

        try (PreparedStatement sentencia =
                conexion.prepareStatement(sql)) {

            sentencia.setString(1, hash);
            sentencia.setLong(2, id);
            sentencia.executeUpdate();
        }
    }
}
