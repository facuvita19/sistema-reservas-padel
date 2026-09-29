package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

import config.ConexionBD;
import negocio.SesionCliente;

public class SesionClienteDAOMySQL implements SesionClienteDAO {

    @Override
    public void guardar(SesionCliente sesion) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, sesion);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar la sesion del cliente.",
                    exception);
        }
    }

    @Override
    public void guardar(Connection conexion, SesionCliente sesion) {
        validarSesion(sesion);
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        if (sesion.getId() <= 0) {
            insertar(conexion, sesion);
        } else {
            actualizar(conexion, sesion);
        }
    }

    private void insertar(Connection conexion, SesionCliente sesion) {
        String sql = "INSERT INTO sesiones_cliente "
                + "(usuario_id, token_hash, fecha_vencimiento, "
                + "fecha_ultimo_uso, revocada) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setLong(1, sesion.getUsuarioId());
            sentencia.setString(2, sesion.getTokenHash());
            sentencia.setTimestamp(3,
                    Timestamp.valueOf(sesion.getFechaVencimiento()));
            cargarTimestamp(sentencia, 4, sesion.getFechaUltimoUso());
            sentencia.setBoolean(5, sesion.isRevocada());

            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException(
                        "No se pudo crear la sesion del cliente.");
            }

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    sesion.setId(claves.getLong(1));
                }
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo crear la sesion del cliente.",
                    exception);
        }
    }

    private void actualizar(Connection conexion, SesionCliente sesion) {
        String sql = "UPDATE sesiones_cliente SET usuario_id = ?, "
                + "token_hash = ?, fecha_vencimiento = ?, "
                + "fecha_ultimo_uso = ?, revocada = ? WHERE id = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, sesion.getUsuarioId());
            sentencia.setString(2, sesion.getTokenHash());
            sentencia.setTimestamp(3,
                    Timestamp.valueOf(sesion.getFechaVencimiento()));
            cargarTimestamp(sentencia, 4, sesion.getFechaUltimoUso());
            sentencia.setBoolean(5, sesion.isRevocada());
            sentencia.setLong(6, sesion.getId());

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "La sesion del cliente no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo actualizar la sesion del cliente.",
                    exception);
        }
    }

    @Override
    public SesionCliente buscarVigentePorTokenHash(
            String tokenHash,
            LocalDateTime momento) {
        if (tokenHash == null || tokenHash.isBlank() || momento == null) {
            return null;
        }

        String sql = consultaBase()
                + " WHERE token_hash = ? AND revocada = FALSE "
                + "AND fecha_vencimiento > ? LIMIT 1";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, tokenHash);
            sentencia.setTimestamp(2, Timestamp.valueOf(momento));

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                        ? convertirResultado(resultado)
                        : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar la sesion del cliente.",
                    exception);
        }
    }

    @Override
    public void actualizarUltimoUso(long id, LocalDateTime momento) {
        validarId(id);
        if (momento == null) {
            throw new IllegalArgumentException(
                    "El momento de ultimo uso es obligatorio.");
        }

        String sql = "UPDATE sesiones_cliente SET fecha_ultimo_uso = ? "
                + "WHERE id = ? AND revocada = FALSE";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setTimestamp(1, Timestamp.valueOf(momento));
            sentencia.setLong(2, id);
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo actualizar el ultimo uso de la sesion.",
                    exception);
        }
    }

    @Override
    public void revocarPorTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) return;

        String sql = "UPDATE sesiones_cliente SET revocada = TRUE "
                + "WHERE token_hash = ? AND revocada = FALSE";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, tokenHash);
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo revocar la sesion del cliente.",
                    exception);
        }
    }

    @Override
    public void revocarPorUsuario(long usuarioId) {
        validarId(usuarioId);

        String sql = "UPDATE sesiones_cliente SET revocada = TRUE "
                + "WHERE usuario_id = ? AND revocada = FALSE";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, usuarioId);
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron revocar las sesiones del usuario.",
                    exception);
        }
    }

    @Override
    public int eliminarVencidas(LocalDateTime momento) {
        if (momento == null) {
            throw new IllegalArgumentException(
                    "El momento de limpieza es obligatorio.");
        }

        String sql = "DELETE FROM sesiones_cliente "
                + "WHERE fecha_vencimiento <= ? OR revocada = TRUE";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setTimestamp(1, Timestamp.valueOf(momento));
            return sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron eliminar las sesiones vencidas.",
                    exception);
        }
    }

    private String consultaBase() {
        return "SELECT id, usuario_id, token_hash, fecha_creacion, "
                + "fecha_vencimiento, fecha_ultimo_uso, revocada "
                + "FROM sesiones_cliente";
    }

    private SesionCliente convertirResultado(ResultSet resultado)
            throws SQLException {
        SesionCliente sesion = new SesionCliente();
        sesion.setId(resultado.getLong("id"));
        sesion.setUsuarioId(resultado.getLong("usuario_id"));
        sesion.setTokenHash(resultado.getString("token_hash"));
        sesion.setFechaCreacion(
                convertirTimestamp(resultado, "fecha_creacion"));
        sesion.setFechaVencimiento(
                convertirTimestamp(resultado, "fecha_vencimiento"));
        sesion.setFechaUltimoUso(
                convertirTimestamp(resultado, "fecha_ultimo_uso"));
        sesion.setRevocada(resultado.getBoolean("revocada"));
        return sesion;
    }

    private LocalDateTime convertirTimestamp(
            ResultSet resultado,
            String columna) throws SQLException {
        Timestamp timestamp = resultado.getTimestamp(columna);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void cargarTimestamp(
            PreparedStatement sentencia,
            int indice,
            LocalDateTime valor) throws SQLException {
        if (valor == null) {
            sentencia.setNull(indice, Types.TIMESTAMP);
        } else {
            sentencia.setTimestamp(indice, Timestamp.valueOf(valor));
        }
    }

    private void validarSesion(SesionCliente sesion) {
        if (sesion == null) {
            throw new IllegalArgumentException(
                    "La sesion del cliente no puede ser nula.");
        }
        validarId(sesion.getUsuarioId());
        if (sesion.getTokenHash() == null
                || !sesion.getTokenHash().matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(
                    "El hash del token de sesion no es valido.");
        }
        if (sesion.getFechaVencimiento() == null) {
            throw new IllegalArgumentException(
                    "El vencimiento de la sesion es obligatorio.");
        }
    }

    private void validarId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID debe ser un numero positivo.");
        }
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe una sesion con el mismo token.",
                    exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
