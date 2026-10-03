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
import java.util.Locale;

import config.ConexionBD;
import negocio.Cliente;
import negocio.PosicionJugador;
import util.NormalizadorTelefono;

public class ClienteDAOMySQL implements ClienteDAO {

    @Override
    public void guardar(Cliente cliente) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, cliente);
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo guardar el cliente en MySQL.",
                    exception);
        }
    }

    @Override
    public void guardar(Connection conexion, Cliente cliente) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente no puede ser nulo.");
        }

        if (cliente.getId() <= 0) {
            insertar(conexion, cliente);
        } else {
            actualizar(conexion, cliente);
        }
    }

    private void insertar(Connection conexion, Cliente cliente) {
        String sql = "INSERT INTO clientes "
                + "(nombre, apellido, documento, telefono, email, "
                + "posicion_preferida, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, cliente);
            cargarPosicion(sentencia, 6, cliente.getPosicionPreferida());
            sentencia.setBoolean(7, cliente.isActivo());

            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException(
                        "No se pudo crear el cliente.");
            }

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    cliente.setId(claves.getLong(1));
                }
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo guardar el cliente en MySQL.",
                    exception);
        }
    }

    private void actualizar(Connection conexion, Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, "
                + "documento = ?, telefono = ?, email = ?, "
                + "posicion_preferida = ?, activo = ? "
                + "WHERE id = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, cliente);
            cargarPosicion(sentencia, 6, cliente.getPosicionPreferida());
            sentencia.setBoolean(7, cliente.isActivo());
            sentencia.setLong(8, cliente.getId());

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El cliente no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo actualizar el cliente en MySQL.",
                    exception);
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            Cliente cliente) throws SQLException {
        sentencia.setString(1, cliente.getNombre());
        sentencia.setString(2, cliente.getApellido());
        sentencia.setString(3, cliente.getDocumento());
        sentencia.setString(4, cliente.getTelefono());

        if (cliente.getEmail() == null
                || cliente.getEmail().isBlank()) {
            sentencia.setNull(5, Types.VARCHAR);
        } else {
            sentencia.setString(5, normalizarEmail(cliente.getEmail()));
        }
    }

    private void cargarPosicion(
            PreparedStatement sentencia,
            int indice,
            PosicionJugador posicion) throws SQLException {
        if (posicion == null) {
            sentencia.setNull(indice, Types.VARCHAR);
        } else {
            sentencia.setString(indice, posicion.name());
        }
    }

    @Override
    public void eliminar(long id) {
        String sql = "UPDATE clientes SET activo = FALSE "
                + "WHERE id = ? AND activo = TRUE";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El cliente no existe o ya esta inactivo.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo desactivar el cliente en MySQL.",
                    exception);
        }
    }

    @Override
    public Cliente buscar(long id) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscar(conexion, id);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el cliente en MySQL.",
                    exception);
        }
    }

    @Override
    public Cliente buscar(Connection conexion, long id) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID del cliente debe ser positivo.");
        }
        String sql = consultaBase() + " WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                        ? convertirResultado(resultado)
                        : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el cliente en MySQL.",
                    exception);
        }
    }

    @Override
    public Cliente buscarPorDocumento(String documento) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscarPorDocumento(conexion, documento);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el documento del cliente.",
                    exception);
        }
    }

    @Override
    public Cliente buscarPorDocumento(
            Connection conexion,
            String documento) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        String valor = soloDigitos(documento);
        if (valor.isBlank()) return null;
        return buscarPorValor(conexion, "documento", valor);
    }

    @Override
    public Cliente buscarPorEmail(String email) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscarPorEmail(conexion, email);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el correo del cliente.",
                    exception);
        }
    }

    @Override
    public Cliente buscarPorEmail(
            Connection conexion,
            String email) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        String valor = normalizarEmail(email);
        if (valor == null) return null;
        return buscarPorValor(conexion, "LOWER(email)", valor);
    }

    private Cliente buscarPorValor(
            Connection conexion,
            String columna,
            String valor) {
        String sql = consultaBase() + " WHERE " + columna + " = ? LIMIT 1";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, valor);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                        ? convertirResultado(resultado)
                        : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el cliente.",
                    exception);
        }
    }

    @Override
    public List<Cliente> buscarActivosPorTelefonoNormalizado(
            Connection conexion,
            String telefonoNormalizado) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        String numero = NormalizadorTelefono.normalizar(
                telefonoNormalizado);
        String expresion = "REGEXP_REPLACE(telefono, '[^0-9]', '')";
        String sql = consultaBase()
                + " WHERE activo = TRUE AND " + expresion + " = ? "
                + "ORDER BY id ASC";
        List<Cliente> clientes = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, numero);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    clientes.add(convertirResultado(resultado));
                }
            }
            return clientes;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron buscar clientes por telefono.",
                    exception);
        }
    }

    @Override
    public List<Cliente> listar() {
        String sql = consultaBase()
                + " WHERE activo = TRUE ORDER BY apellido, nombre";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                clientes.add(convertirResultado(resultado));
            }
            return clientes;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar los clientes.",
                    exception);
        }
    }

    @Override
    public List<Cliente> listarTodos() {
        String sql = consultaBase()
                + " ORDER BY activo DESC, apellido, nombre";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) clientes.add(convertirResultado(resultado));
            return clientes;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar todos los clientes.", exception);
        }
    }

    @Override
    public void reactivar(long id) {
        String sql = "UPDATE clientes SET activo = TRUE "
                + "WHERE id = ? AND activo = FALSE";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El cliente no existe o ya está activo.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo reactivar el cliente.", exception);
        }
    }

    @Override
    public void eliminarDefinitivamente(long id) {
        String sql = "DELETE FROM clientes "
                + "WHERE id = ? AND activo = FALSE";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El cliente no existe o todavía está activo.");
            }
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1451) {
                throw new IllegalArgumentException(
                        "El cliente no se puede eliminar definitivamente porque "
                                + "tiene reservas, pagos, inscripciones u otros datos relacionados. "
                                + "Podés mantenerlo inactivo.", exception);
            }
            throw new RuntimeException(
                    "No se pudo eliminar definitivamente el cliente.", exception);
        }
    }

    @Override
    public boolean existeDocumento(
            String documento,
            long clienteExcluidoId) {
        return existeValorUnico(
                "documento",
                soloDigitos(documento),
                clienteExcluidoId);
    }

    @Override
    public boolean existeEmail(
            String email,
            long clienteExcluidoId) {
        String valor = normalizarEmail(email);
        if (valor == null) return false;
        return existeValorUnico(
                "LOWER(email)",
                valor,
                clienteExcluidoId);
    }

    private boolean existeValorUnico(
            String columna,
            String valor,
            long clienteExcluidoId) {
        String sql = "SELECT COUNT(*) FROM clientes "
                + "WHERE " + columna + " = ? AND id <> ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, valor);
            sentencia.setLong(2, clienteExcluidoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar un dato unico del cliente.",
                    exception);
        }
    }

    private String consultaBase() {
        return "SELECT id, nombre, apellido, documento, telefono, email, "
                + "posicion_preferida, activo, fecha_creacion FROM clientes";
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

        String posicion = resultado.getString("posicion_preferida");
        if (posicion != null && !posicion.isBlank()) {
            cliente.setPosicionPreferida(PosicionJugador.valueOf(posicion));
        }

        cliente.setActivo(resultado.getBoolean("activo"));

        Timestamp fecha = resultado.getTimestamp("fecha_creacion");
        if (fecha != null) {
            cliente.setFechaCreacion(fecha.toLocalDateTime());
        }
        return cliente;
    }

    private String soloDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private String normalizarEmail(String valor) {
        return valor == null || valor.isBlank()
                ? null
                : valor.trim().toLowerCase(Locale.ROOT);
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe un cliente con el mismo documento "
                            + "o correo electronico.",
                    exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
