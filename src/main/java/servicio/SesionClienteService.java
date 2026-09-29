package servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import dao.SesionClienteDAO;
import dao.SesionClienteDAOMySQL;
import negocio.SesionCliente;

public class SesionClienteService {

    private static final int LONGITUD_TOKEN_BYTES = 32;
    private static final Duration DURACION_PREDETERMINADA =
            Duration.ofDays(30);
    private static final SecureRandom GENERADOR_SEGURO =
            new SecureRandom();

    private final SesionClienteDAO sesionDAO;
    private final Duration duracionSesion;

    public SesionClienteService() {
        this(new SesionClienteDAOMySQL(), DURACION_PREDETERMINADA);
    }

    public SesionClienteService(
            SesionClienteDAO sesionDAO,
            Duration duracionSesion) {
        if (sesionDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de sesiones no puede ser nulo.");
        }
        if (duracionSesion == null
                || duracionSesion.isZero()
                || duracionSesion.isNegative()) {
            throw new IllegalArgumentException(
                    "La duracion de la sesion debe ser positiva.");
        }
        this.sesionDAO = sesionDAO;
        this.duracionSesion = duracionSesion;
    }

    public SesionCreada crear(long usuarioId) {
        validarUsuarioId(usuarioId);
        String token = generarToken();
        LocalDateTime ahora = LocalDateTime.now();
        SesionCliente sesion = prepararSesion(usuarioId, token, ahora);
        sesionDAO.guardar(sesion);
        return new SesionCreada(token, sesion);
    }

    public SesionCreada crear(
            Connection conexion,
            long usuarioId) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        validarUsuarioId(usuarioId);
        String token = generarToken();
        LocalDateTime ahora = LocalDateTime.now();
        SesionCliente sesion = prepararSesion(usuarioId, token, ahora);
        sesionDAO.guardar(conexion, sesion);
        return new SesionCreada(token, sesion);
    }

    public SesionCliente validar(String token) {
        if (token == null || token.isBlank()) return null;

        LocalDateTime ahora = LocalDateTime.now();
        SesionCliente sesion = sesionDAO.buscarVigentePorTokenHash(
                calcularHashToken(token.trim()),
                ahora);

        if (sesion == null || !sesion.estaVigente(ahora)) {
            return null;
        }

        sesionDAO.actualizarUltimoUso(sesion.getId(), ahora);
        sesion.setFechaUltimoUso(ahora);
        return sesion;
    }

    public void revocar(String token) {
        if (token == null || token.isBlank()) return;
        sesionDAO.revocarPorTokenHash(
                calcularHashToken(token.trim()));
    }

    public void revocarTodas(long usuarioId) {
        validarUsuarioId(usuarioId);
        sesionDAO.revocarPorUsuario(usuarioId);
    }

    public int limpiarVencidas() {
        return sesionDAO.eliminarVencidas(LocalDateTime.now());
    }

    public Duration getDuracionSesion() {
        return duracionSesion;
    }

    public static String calcularHashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "El token de sesion es obligatorio.");
        }
        try {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            byte[] hash = resumen.digest(
                    token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 no esta disponible.",
                    exception);
        }
    }

    private SesionCliente prepararSesion(
            long usuarioId,
            String token,
            LocalDateTime ahora) {
        SesionCliente sesion = new SesionCliente();
        sesion.setUsuarioId(usuarioId);
        sesion.setTokenHash(calcularHashToken(token));
        sesion.setFechaVencimiento(ahora.plus(duracionSesion));
        sesion.setFechaUltimoUso(ahora);
        sesion.setRevocada(false);
        return sesion;
    }

    private String generarToken() {
        byte[] bytes = new byte[LONGITUD_TOKEN_BYTES];
        GENERADOR_SEGURO.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private void validarUsuarioId(long usuarioId) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del usuario debe ser positivo.");
        }
    }

    public record SesionCreada(
            String token,
            SesionCliente sesion) {

        public SesionCreada {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException(
                        "El token creado no puede estar vacio.");
            }
            if (sesion == null) {
                throw new IllegalArgumentException(
                        "La sesion creada no puede ser nula.");
            }
        }
    }
}
