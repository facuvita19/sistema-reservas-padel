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
import negocio.Cliente;

public class ClienteDAOMySQL implements ClienteDAO {

    @Override
    public void guardar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente no puede ser nulo."
            );
        }

        if (cliente.getId() <= 0) {
            insertar(cliente);
        } else {
            actualizar(cliente);
        }
    }

    private void insertar(Cliente cliente) {
        String sql =
                "INSERT INTO clientes "
                + "(nombre, apellido, documento, telefono, "
                + "email, activo) "
                + "VALUES (?, ?, ?, ?, ?, TRUE)";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            cargarParametros(sentencia, cliente);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                throw new RuntimeException(
                        "No se pudo crear el cliente."
                );
            }

            try (ResultSet claves =
                    sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    cliente.setId(claves.getLong(1));
                }
            }

        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo guardar el cliente en MySQL.",
                    exception
            );
        }
    }

    private void actualizar(Cliente cliente) {
        String sql =
                "UPDATE clientes SET "
                + "nombre = ?, apellido = ?, documento = ?, "
                + "telefono = ?, email = ? "
                + "WHERE id = ? AND activo = TRUE";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            cargarParametros(sentencia, cliente);
            sentencia.setLong(6, cliente.getId());

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El cliente no existe o esta inactivo."
                );
            }

        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo actualizar el cliente en MySQL.",
                    exception
            );
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            Cliente cliente)
            throws SQLException {

        sentencia.setString(1, cliente.getNombre());
        sentencia.setString(2, cliente.getApellido());
        sentencia.setString(3, cliente.getDocumento());
        sentencia.setString(4, cliente.getTelefono());

        if (cliente.getEmail() == null
                || cliente.getEmail().isBlank()) {
            sentencia.setNull(5, java.sql.Types.VARCHAR);
        } else {
            sentencia.setString(5, cliente.getEmail());
        }
    }

    @Override
    public void eliminar(long id) {
        String sql =
                "UPDATE clientes SET activo = FALSE "
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
                        "El cliente no existe o ya esta inactivo."
                );
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo desactivar el cliente en MySQL.",
                    exception
            );
        }
    }

    @Override
    public Cliente buscar(long id) {
        String sql =
                "SELECT id, nombre, apellido, documento, "
                + "telefono, email, activo, fecha_creacion "
                + "FROM clientes WHERE id = ?";

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
                    "No se pudo buscar el cliente en MySQL.",
                    exception
            );
        }
    }

    @Override
    public List<Cliente> listar() {
        String sql =
                "SELECT id, nombre, apellido, documento, "
                + "telefono, email, activo, fecha_creacion "
                + "FROM clientes WHERE activo = TRUE "
                + "ORDER BY apellido, nombre";

        List<Cliente> clientes = new ArrayList<>();

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()
        ) {
            while (resultado.next()) {
                clientes.add(convertirResultado(resultado));
            }

            return clientes;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar los clientes.",
                    exception
            );
        }
    }

    @Override
    public boolean existeDocumento(
            String documento,
            long clienteExcluidoId) {

        return existeValorUnico(
                "documento",
                documento,
                clienteExcluidoId
        );
    }

    @Override
    public boolean existeEmail(
            String email,
            long clienteExcluidoId) {

        if (email == null || email.isBlank()) {
            return false;
        }

        return existeValorUnico(
                "email",
                email,
                clienteExcluidoId
        );
    }

    private boolean existeValorUnico(
            String columna,
            String valor,
            long clienteExcluidoId) {

        String sql =
                "SELECT COUNT(*) FROM clientes "
                + "WHERE " + columna + " = ? AND id <> ?";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, valor);
            sentencia.setLong(2, clienteExcluidoId);

            try (ResultSet resultado =
                    sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar un dato unico "
                            + "del cliente.",
                    exception
            );
        }
    }

    private Cliente convertirResultado(ResultSet resultado)
            throws SQLException {

        Cliente cliente = new Cliente();
        cliente.setId(resultado.getLong("id"));
        cliente.setNombre(resultado.getString("nombre"));
        cliente.setApellido(resultado.getString("apellido"));
        cliente.setDocumento(resultado.getString("documento"));
        cliente.setTelefono(resultado.getString("telefono"));
        cliente.setEmail(resultado.getString("email"));
        cliente.setActivo(resultado.getBoolean("activo"));

        Timestamp fecha = resultado.getTimestamp("fecha_creacion");
        if (fecha != null) {
            cliente.setFechaCreacion(fecha.toLocalDateTime());
        }

        return cliente;
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {

        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe un cliente con el mismo "
                            + "documento o correo electronico.",
                    exception
            );
        }

        return new RuntimeException(mensaje, exception);
    }
}
