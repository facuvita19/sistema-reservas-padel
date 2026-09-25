package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.RolUsuario;
import negocio.Usuario;

public class UsuarioDAOMySQL implements UsuarioDAO {

    @Override
    public void guardar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario no puede ser nulo."
            );
        }

        if (usuario.getId() <= 0) {
            insertar(usuario);
        } else {
            actualizar(usuario);
        }
    }

    private void insertar(Usuario usuario) {
        String sql =
                "INSERT INTO usuarios "
                + "(nombre_usuario, password_hash, rol, "
                + "cliente_id, activo) "
                + "VALUES (?, ?, ?, ?, TRUE)";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            cargarParametros(sentencia, usuario, true);

            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException(
                        "No se pudo crear el usuario."
                );
            }

            try (ResultSet claves =
                    sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setId(claves.getLong(1));
                }
            }

        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo guardar el usuario.",
                    exception
            );
        }
    }

    private void actualizar(Usuario usuario) {
        boolean actualizarPassword =
                usuario.getPasswordHash() != null
                && !usuario.getPasswordHash().isBlank();

        String sql = actualizarPassword
                ? "UPDATE usuarios SET nombre_usuario = ?, "
                        + "password_hash = ?, rol = ?, cliente_id = ? "
                        + "WHERE id = ? AND activo = TRUE"
                : "UPDATE usuarios SET nombre_usuario = ?, "
                        + "rol = ?, cliente_id = ? "
                        + "WHERE id = ? AND activo = TRUE";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            cargarParametros(
                    sentencia,
                    usuario,
                    actualizarPassword
            );

            int indiceId = actualizarPassword ? 5 : 4;
            sentencia.setLong(indiceId, usuario.getId());

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El usuario no existe o esta inactivo."
                );
            }

        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo actualizar el usuario.",
                    exception
            );
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            Usuario usuario,
            boolean incluirPassword)
            throws SQLException {

        int indice = 1;
        sentencia.setString(indice++, usuario.getNombreUsuario());

        if (incluirPassword) {
            sentencia.setString(indice++, usuario.getPasswordHash());
        }

        sentencia.setString(indice++, usuario.getRol().name());

        if (usuario.getClienteId() == null) {
            sentencia.setNull(indice, java.sql.Types.BIGINT);
        } else {
            sentencia.setLong(indice, usuario.getClienteId());
        }
    }

    @Override
    public void eliminar(long id) {
        String sql =
                "UPDATE usuarios SET activo = FALSE "
                + "WHERE id = ? AND activo = TRUE";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, id);

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El usuario no existe o ya esta inactivo."
                );
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo desactivar el usuario.",
                    exception
            );
        }
    }

    @Override
    public Usuario buscar(long id) {
        String sql = consultaBase() + " WHERE id = ?";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, id);

            try (ResultSet resultado =
                    sentencia.executeQuery()) {
                return resultado.next()
                        ? convertirResultado(resultado)
                        : null;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el usuario.",
                    exception
            );
        }
    }

    @Override
    public Usuario buscarPorNombreUsuario(
            String nombreUsuario) {

        String sql = consultaBase()
                + " WHERE LOWER(nombre_usuario) = LOWER(?) "
                + "AND activo = TRUE";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, nombreUsuario);

            try (ResultSet resultado =
                    sentencia.executeQuery()) {
                return resultado.next()
                        ? convertirResultado(resultado)
                        : null;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el nombre de usuario.",
                    exception
            );
        }
    }

    @Override
    public List<Usuario> listar() {
        String sql = consultaBase()
                + " WHERE activo = TRUE ORDER BY nombre_usuario";

        List<Usuario> usuarios = new ArrayList<>();

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()
        ) {
            while (resultado.next()) {
                usuarios.add(convertirResultado(resultado));
            }

            return usuarios;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar los usuarios.",
                    exception
            );
        }
    }

    @Override
    public boolean existeNombreUsuario(
            String nombreUsuario,
            long usuarioExcluidoId) {

        String sql =
                "SELECT COUNT(*) FROM usuarios "
                + "WHERE LOWER(nombre_usuario) = LOWER(?) "
                + "AND id <> ?";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, nombreUsuario);
            sentencia.setLong(2, usuarioExcluidoId);

            try (ResultSet resultado =
                    sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar el nombre de usuario.",
                    exception
            );
        }
    }

    private String consultaBase() {
        return "SELECT id, nombre_usuario, password_hash, "
                + "rol, cliente_id, activo, fecha_creacion "
                + "FROM usuarios";
    }

    private Usuario convertirResultado(ResultSet resultado)
            throws SQLException {

        Usuario usuario = new Usuario();
        usuario.setId(resultado.getLong("id"));
        usuario.setNombreUsuario(
                resultado.getString("nombre_usuario")
        );
        usuario.setPasswordHash(
                resultado.getString("password_hash")
        );
        usuario.setRol(RolUsuario.valueOf(
                resultado.getString("rol")
        ));

        long clienteId = resultado.getLong("cliente_id");
        usuario.setClienteId(
                resultado.wasNull() ? null : clienteId
        );

        usuario.setActivo(resultado.getBoolean("activo"));

        Timestamp fecha = resultado.getTimestamp("fecha_creacion");
        if (fecha != null) {
            usuario.setFechaCreacion(fecha.toLocalDateTime());
        }

        return usuario;
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {

        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe el nombre de usuario o el cliente "
                            + "ya esta vinculado a otra cuenta.",
                    exception
            );
        }

        return new RuntimeException(mensaje, exception);
    }
}
