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
import negocio.RecuperacionPasswordCliente;

public class RecuperacionPasswordClienteDAOMySQL
        implements RecuperacionPasswordClienteDAO {

    private static final int LONGITUD_TOKEN_HASH = 64;

    @Override
    public void guardar(RecuperacionPasswordCliente recuperacion) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, recuperacion);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar la recuperacion de contrasena.",
                    exception);
        }
    }

    @Override
    public void guardar(
            Connection conexion,
            RecuperacionPasswordCliente recuperacion) {
        validarConexion(conexion);
        validarRecuperacion(recuperacion);
        if (recuperacion.getId() <= 0) {
            insertar(conexion, recuperacion);
        } else {
            actualizar(conexion, recuperacion);
        }
    }

    private void insertar(
            Connection conexion,
            RecuperacionPasswordCliente recuperacion) {
        String sql = "INSERT INTO recuperaciones_password_cliente "
                + "(usuario_id, token_hash, fecha_vencimiento, "
                + "fecha_uso, revocada) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, recuperacion);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException(
                        "No se pudo crear la recuperacion de contrasena.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) recuperacion.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo guardar la recuperacion de contrasena.",
                    exception);
        }
    }

    private void actualizar(
            Connection conexion,
            RecuperacionPasswordCliente recuperacion) {
        String sql = "UPDATE recuperaciones_password_cliente SET "
                + "usuario_id = ?, token_hash = ?, fecha_vencimiento = ?, "
                + "fecha_uso = ?, revocada = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, recuperacion);
            sentencia.setLong(6, recuperacion.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "La recuperacion de contrasena no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo actualizar la recuperacion de contrasena.",
                    exception);
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            RecuperacionPasswordCliente recuperacion) throws SQLException {
        sentencia.setLong(1, recuperacion.getUsuarioId());
        sentencia.setString(2, recuperacion.getTokenHash());
        sentencia.setTimestamp(3, Timestamp.valueOf(
                recuperacion.getFechaVencimiento()));
        if (recuperacion.getFechaUso() == null) {
            sentencia.setNull(4, Types.TIMESTAMP);
        } else {
            sentencia.setTimestamp(4, Timestamp.valueOf(
                    recuperacion.getFechaUso()));
        }
        sentencia.setBoolean(5, recuperacion.isRevocada());
    }

    @Override
    public RecuperacionPasswordCliente buscarVigentePorTokenHash(
            String tokenHash,
            LocalDateTime momento) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscarVigentePorTokenHash(
                    conexion, tokenHash, momento);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar la recuperacion de contrasena.",
                    exception);
        }
    }

    @Override
    public RecuperacionPasswordCliente buscarVigentePorTokenHash(
            Connection conexion,
            String tokenHash,
            LocalDateTime momento) {
        validarConexion(conexion);
        validarTokenHash(tokenHash);
        if (momento == null) {
            throw new IllegalArgumentException(
                    "El momento de validacion es obligatorio.");
        }

        String sql = consultaBase()
                + " WHERE token_hash = ? "
                + "AND revocada = FALSE "
                + "AND fecha_uso IS NULL "
                + "AND fecha_vencimiento > ? LIMIT 1";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, tokenHash);
            sentencia.setTimestamp(2, Timestamp.valueOf(momento));
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertir(resultado) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar la recuperacion de contrasena.",
                    exception);
        }
    }

    @Override
    public void marcarUsada(
            Connection conexion,
            long id,
            LocalDateTime fechaUso) {
        validarConexion(conexion);
        if (id <= 0 || fechaUso == null) {
            throw new IllegalArgumentException(
                    "La recuperacion y su fecha de uso son obligatorias.");
        }
        String sql = "UPDATE recuperaciones_password_cliente "
                + "SET fecha_uso = ?, revocada = TRUE "
                + "WHERE id = ? AND revocada = FALSE AND fecha_uso IS NULL";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setTimestamp(1, Timestamp.valueOf(fechaUso));
            sentencia.setLong(2, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El enlace de recuperacion ya no esta disponible.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo consumir la recuperacion de contrasena.",
                    exception);
        }
    }

    @Override
    public int revocarPendientesPorUsuario(
            Connection conexion,
            long usuarioId) {
        validarConexion(conexion);
        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del usuario debe ser positivo.");
        }
        String sql = "UPDATE recuperaciones_password_cliente "
                + "SET revocada = TRUE "
                + "WHERE usuario_id = ? AND revocada = FALSE "
                + "AND fecha_uso IS NULL";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, usuarioId);
            return sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron revocar las recuperaciones anteriores.",
                    exception);
        }
    }

    @Override
    public int eliminarVencidasOConsumidas(LocalDateTime momento) {
        if (momento == null) {
            throw new IllegalArgumentException(
                    "El momento de limpieza es obligatorio.");
        }
        String sql = "DELETE FROM recuperaciones_password_cliente "
                + "WHERE fecha_vencimiento <= ? OR revocada = TRUE "
                + "OR fecha_uso IS NOT NULL";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setTimestamp(1, Timestamp.valueOf(momento));
            return sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron limpiar las recuperaciones antiguas.",
                    exception);
        }
    }

    private String consultaBase() {
        return "SELECT id, usuario_id, token_hash, fecha_creacion, "
                + "fecha_vencimiento, fecha_uso, revocada "
                + "FROM recuperaciones_password_cliente";
    }

    private RecuperacionPasswordCliente convertir(ResultSet resultado)
            throws SQLException {
        RecuperacionPasswordCliente recuperacion =
                new RecuperacionPasswordCliente();
        recuperacion.setId(resultado.getLong("id"));
        recuperacion.setUsuarioId(resultado.getLong("usuario_id"));
        recuperacion.setTokenHash(resultado.getString("token_hash"));
        recuperacion.setFechaCreacion(
                fecha(resultado, "fecha_creacion"));
        recuperacion.setFechaVencimiento(
                fecha(resultado, "fecha_vencimiento"));
        recuperacion.setFechaUso(fecha(resultado, "fecha_uso"));
        recuperacion.setRevocada(resultado.getBoolean("revocada"));
        return recuperacion;
    }

    private LocalDateTime fecha(ResultSet resultado, String columna)
            throws SQLException {
        Timestamp valor = resultado.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private void validarConexion(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
    }

    private void validarRecuperacion(
            RecuperacionPasswordCliente recuperacion) {
        if (recuperacion == null) {
            throw new IllegalArgumentException(
                    "La recuperacion no puede ser nula.");
        }
        if (recuperacion.getUsuarioId() <= 0) {
            throw new IllegalArgumentException(
                    "El usuario de la recuperacion no es valido.");
        }
        validarTokenHash(recuperacion.getTokenHash());
        if (recuperacion.getFechaVencimiento() == null) {
            throw new IllegalArgumentException(
                    "El vencimiento de la recuperacion es obligatorio.");
        }
    }

    private void validarTokenHash(String tokenHash) {
        if (tokenHash == null
                || !tokenHash.matches("^[a-f0-9]{64}$")) {
            throw new IllegalArgumentException(
                    "El hash del token no es valido.");
        }
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe una recuperacion con el mismo token.",
                    exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
