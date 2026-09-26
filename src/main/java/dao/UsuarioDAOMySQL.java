package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.RolUsuario;
import negocio.Usuario;

public class UsuarioDAOMySQL implements UsuarioDAO {

    @Override
    public void guardar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        if (usuario.getId() <= 0) insertar(usuario);
        else actualizar(usuario);
    }

    private void insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios "
                + "(nombre_usuario, password_hash, rol, cliente_id, activo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, usuario.getNombreUsuario());
            sentencia.setString(2, usuario.getPasswordHash());
            sentencia.setString(3, usuario.getRol().name());
            cargarCliente(sentencia, 4, usuario.getClienteId());
            sentencia.setBoolean(5, usuario.isActivo());

            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException("No se pudo crear el usuario.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) usuario.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw traducirError("No se pudo guardar el usuario.", exception);
        }
    }

    private void actualizar(Usuario usuario) {
        boolean cambiarPassword = usuario.getPasswordHash() != null
                && !usuario.getPasswordHash().isBlank();

        String sql = cambiarPassword
                ? "UPDATE usuarios SET nombre_usuario = ?, password_hash = ?, "
                        + "rol = ?, cliente_id = ?, activo = ? WHERE id = ?"
                : "UPDATE usuarios SET nombre_usuario = ?, rol = ?, "
                        + "cliente_id = ?, activo = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            int indice = 1;
            sentencia.setString(indice++, usuario.getNombreUsuario());
            if (cambiarPassword) {
                sentencia.setString(indice++, usuario.getPasswordHash());
            }
            sentencia.setString(indice++, usuario.getRol().name());
            cargarCliente(sentencia, indice++, usuario.getClienteId());
            sentencia.setBoolean(indice++, usuario.isActivo());
            sentencia.setLong(indice, usuario.getId());

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("El usuario no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError("No se pudo actualizar el usuario.", exception);
        }
    }

    private void cargarCliente(
            PreparedStatement sentencia, int indice, Long clienteId)
            throws SQLException {
        if (clienteId == null) sentencia.setNull(indice, Types.BIGINT);
        else sentencia.setLong(indice, clienteId);
    }

    @Override
    public void eliminar(long id) {
        cambiarActivo(id, false,
                "El usuario no existe o ya está inactivo.");
    }

    @Override
    public void activar(long id) {
        cambiarActivo(id, true,
                "El usuario no existe o ya está activo.");
    }

    private void cambiarActivo(long id, boolean activo, String mensaje) {
        String sql = "UPDATE usuarios SET activo = ? "
                + "WHERE id = ? AND activo <> ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setBoolean(1, activo);
            sentencia.setLong(2, id);
            sentencia.setBoolean(3, activo);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(mensaje);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    activo ? "No se pudo activar el usuario."
                            : "No se pudo desactivar el usuario.",
                    exception);
        }
    }

    @Override
    public Usuario buscar(long id) {
        String sql = consultaBase() + " WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertirResultado(resultado) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo buscar el usuario.", exception);
        }
    }

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        String sql = consultaBase()
                + " WHERE LOWER(nombre_usuario) = LOWER(?) AND activo = TRUE";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombreUsuario);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertirResultado(resultado) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el nombre de usuario.", exception);
        }
    }

    @Override
    public List<Usuario> listar() {
        String sql = consultaBase()
                + " ORDER BY activo DESC, nombre_usuario";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                usuarios.add(convertirResultado(resultado));
            }
            return usuarios;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar los usuarios.", exception);
        }
    }

    @Override
    public boolean existeNombreUsuario(
            String nombreUsuario, long usuarioExcluidoId) {
        String sql = "SELECT COUNT(*) FROM usuarios "
                + "WHERE LOWER(nombre_usuario) = LOWER(?) AND id <> ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombreUsuario);
            sentencia.setLong(2, usuarioExcluidoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar el nombre de usuario.", exception);
        }
    }

    @Override
    public long contarAdministradoresActivos() {
        String sql = "SELECT COUNT(*) FROM usuarios "
                + "WHERE activo = TRUE AND rol = 'ADMINISTRADOR'";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            resultado.next();
            return resultado.getLong(1);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron contar los administradores.", exception);
        }
    }

    private String consultaBase() {
        return "SELECT id, nombre_usuario, password_hash, rol, cliente_id, "
                + "activo, fecha_creacion FROM usuarios";
    }

    private Usuario convertirResultado(ResultSet resultado)
            throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(resultado.getLong("id"));
        usuario.setNombreUsuario(resultado.getString("nombre_usuario"));
        usuario.setPasswordHash(resultado.getString("password_hash"));
        usuario.setRol(RolUsuario.valueOf(resultado.getString("rol")));
        long clienteId = resultado.getLong("cliente_id");
        usuario.setClienteId(resultado.wasNull() ? null : clienteId);
        usuario.setActivo(resultado.getBoolean("activo"));
        Timestamp fecha = resultado.getTimestamp("fecha_creacion");
        if (fecha != null) usuario.setFechaCreacion(fecha.toLocalDateTime());
        return usuario;
    }

    private RuntimeException traducirError(
            String mensaje, SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe el nombre de usuario o el cliente "
                            + "ya está vinculado a otra cuenta.",
                    exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
