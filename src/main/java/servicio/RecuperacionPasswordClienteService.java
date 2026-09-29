package servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import config.ConexionBD;
import dao.RecuperacionPasswordClienteDAO;
import dao.RecuperacionPasswordClienteDAOMySQL;
import negocio.RecuperacionPasswordCliente;
import util.ProtectorPassword;

public class RecuperacionPasswordClienteService {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final int TOKEN_BYTES = 32;
    private static final Duration DURACION_PREDETERMINADA =
            Duration.ofMinutes(30);
    private static final SecureRandom GENERADOR = new SecureRandom();

    private final RecuperacionPasswordClienteDAO recuperacionDAO;
    private final Duration duracionToken;

    public RecuperacionPasswordClienteService() {
        this(new RecuperacionPasswordClienteDAOMySQL(),
                DURACION_PREDETERMINADA);
    }

    public RecuperacionPasswordClienteService(
            RecuperacionPasswordClienteDAO recuperacionDAO,
            Duration duracionToken) {
        if (recuperacionDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de recuperaciones no puede ser nulo.");
        }
        if (duracionToken == null
                || duracionToken.isZero()
                || duracionToken.isNegative()) {
            throw new IllegalArgumentException(
                    "La duracion del token debe ser positiva.");
        }
        this.recuperacionDAO = recuperacionDAO;
        this.duracionToken = duracionToken;
    }

    public SolicitudRecuperacion solicitar(String email) {
        String correo = normalizarEmail(email);
        LocalDateTime ahora = LocalDateTime.now();

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Long usuarioId = buscarUsuarioClienteActivo(
                        conexion, correo);
                if (usuarioId == null) {
                    conexion.commit();
                    return SolicitudRecuperacion.noGenerada();
                }

                recuperacionDAO.revocarPendientesPorUsuario(
                        conexion, usuarioId);

                String token = generarToken();
                RecuperacionPasswordCliente recuperacion =
                        new RecuperacionPasswordCliente();
                recuperacion.setUsuarioId(usuarioId);
                recuperacion.setTokenHash(hashToken(token));
                recuperacion.setFechaVencimiento(
                        ahora.plus(duracionToken));
                recuperacion.setRevocada(false);
                recuperacionDAO.guardar(conexion, recuperacion);

                conexion.commit();
                return new SolicitudRecuperacion(
                        true,
                        token,
                        recuperacion.getFechaVencimiento());
            } catch (SQLException | RuntimeException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo iniciar la recuperacion de contrasena.",
                    exception);
        }
    }

    public void restablecer(
            String token,
            String passwordNuevo,
            String passwordRepetido) {
        String tokenLimpio = validarToken(token);
        validarPasswords(passwordNuevo, passwordRepetido);
        LocalDateTime ahora = LocalDateTime.now();

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                RecuperacionPasswordCliente recuperacion =
                        recuperacionDAO.buscarVigentePorTokenHash(
                                conexion,
                                hashToken(tokenLimpio),
                                ahora);
                if (recuperacion == null) {
                    throw new TokenRecuperacionInvalidoException();
                }

                actualizarPassword(
                        conexion,
                        recuperacion.getUsuarioId(),
                        passwordNuevo);
                recuperacionDAO.marcarUsada(
                        conexion,
                        recuperacion.getId(),
                        ahora);
                revocarSesiones(
                        conexion,
                        recuperacion.getUsuarioId());

                conexion.commit();
            } catch (SQLException | RuntimeException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo restablecer la contrasena.",
                    exception);
        }
    }

    public boolean tokenVigente(String token) {
        try {
            String limpio = validarToken(token);
            return recuperacionDAO.buscarVigentePorTokenHash(
                    hashToken(limpio),
                    LocalDateTime.now()) != null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public int limpiarAntiguas() {
        return recuperacionDAO.eliminarVencidasOConsumidas(
                LocalDateTime.now());
    }

    public Duration getDuracionToken() {
        return duracionToken;
    }

    private Long buscarUsuarioClienteActivo(
            Connection conexion,
            String email) throws SQLException {
        String sql = "SELECT u.id FROM usuarios u "
                + "INNER JOIN clientes c ON c.id = u.cliente_id "
                + "WHERE LOWER(u.nombre_usuario) = LOWER(?) "
                + "AND u.rol = 'CLIENTE' "
                + "AND u.activo = TRUE "
                + "AND c.activo = TRUE LIMIT 1 FOR UPDATE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, email);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getLong(1) : null;
            }
        }
    }

    private void actualizarPassword(
            Connection conexion,
            long usuarioId,
            String passwordNuevo) throws SQLException {
        String sql = "UPDATE usuarios SET password_hash = ? "
                + "WHERE id = ? AND rol = 'CLIENTE' AND activo = TRUE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(
                    1,
                    ProtectorPassword.generarHash(passwordNuevo));
            sentencia.setLong(2, usuarioId);
            if (sentencia.executeUpdate() == 0) {
                throw new TokenRecuperacionInvalidoException();
            }
        }
    }

    private void revocarSesiones(
            Connection conexion,
            long usuarioId) throws SQLException {
        String sql = "UPDATE sesiones_cliente SET revocada = TRUE "
                + "WHERE usuario_id = ? AND revocada = FALSE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, usuarioId);
            sentencia.executeUpdate();
        }
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo electronico es obligatorio.");
        }
        String correo = email.trim().toLowerCase(Locale.ROOT);
        if (correo.length() > 150 || !EMAIL.matcher(correo).matches()) {
            throw new IllegalArgumentException(
                    "El correo electronico no tiene un formato valido.");
        }
        return correo;
    }

    private String validarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenRecuperacionInvalidoException();
        }
        String limpio = token.trim();
        if (limpio.length() < 32 || limpio.length() > 100
                || !limpio.matches("^[A-Za-z0-9_-]+$")) {
            throw new TokenRecuperacionInvalidoException();
        }
        return limpio;
    }

    private void validarPasswords(
            String passwordNuevo,
            String passwordRepetido) {
        if (passwordNuevo == null || passwordNuevo.length() < 8) {
            throw new IllegalArgumentException(
                    "La contrasena nueva debe tener al menos 8 caracteres.");
        }
        if (passwordRepetido == null
                || !passwordNuevo.equals(passwordRepetido)) {
            throw new IllegalArgumentException(
                    "Las contrasenas nuevas no coinciden.");
        }
    }

    private String generarToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        GENERADOR.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 no esta disponible.",
                    exception);
        }
    }

    private void rollbackSeguro(
            Connection conexion,
            Exception original) {
        try {
            conexion.rollback();
        } catch (SQLException rollback) {
            original.addSuppressed(rollback);
        }
    }

    private void restaurarAutoCommit(Connection conexion) {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    public record SolicitudRecuperacion(
            boolean generada,
            String token,
            LocalDateTime vencimiento) {

        public static SolicitudRecuperacion noGenerada() {
            return new SolicitudRecuperacion(false, null, null);
        }
    }

    public static class TokenRecuperacionInvalidoException
            extends IllegalArgumentException {

        public TokenRecuperacionInvalidoException() {
            super("El enlace de recuperacion no es valido o ha vencido.");
        }
    }
}
