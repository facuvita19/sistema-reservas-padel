package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import api.publica.CuentaClienteDTO.ActualizarPerfil;
import api.publica.CuentaClienteDTO.CambiarPassword;
import api.publica.CuentaClienteDTO.HistorialReservas;
import api.publica.CuentaClienteDTO.Perfil;
import api.publica.CuentaClienteDTO.ReservaResumen;
import config.ConexionBD;
import negocio.Cliente;
import negocio.ConfiguracionComplejo;
import negocio.PosicionJugador;

public class CuentaClienteService {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TELEFONO =
            Pattern.compile("^[0-9+()\\-\\s]{6,30}$");

    private final ConfiguracionComplejoService configuracionService;
    private final UsuarioService usuarioService;

    public CuentaClienteService() {
        this(new ConfiguracionComplejoService(), new UsuarioService());
    }

    public CuentaClienteService(
            ConfiguracionComplejoService configuracionService) {
        this(configuracionService, new UsuarioService());
    }

    public CuentaClienteService(
            ConfiguracionComplejoService configuracionService,
            UsuarioService usuarioService) {
        if (configuracionService == null || usuarioService == null) {
            throw new IllegalArgumentException(
                    "Los servicios de cuenta no pueden ser nulos.");
        }
        this.configuracionService = configuracionService;
        this.usuarioService = usuarioService;
    }

    public Perfil obtenerPerfil(long clienteId) {
        validarClienteId(clienteId);
        String sql = "SELECT id, nombre, apellido, documento, telefono, "
                + "email, posicion_preferida, activo "
                + "FROM clientes WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, clienteId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()
                        || !resultado.getBoolean("activo")) {
                    throw new RecursoClienteNoEncontradoException(
                            "El perfil del cliente no esta disponible.");
                }
                return convertirPerfil(resultado);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo recuperar el perfil del cliente.",
                    exception);
        }
    }

    public Perfil obtenerPerfil(Cliente cliente) {
        if (cliente == null
                || cliente.getId() <= 0
                || !cliente.isActivo()) {
            throw new RecursoClienteNoEncontradoException(
                    "El perfil del cliente no esta disponible.");
        }
        return new Perfil(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDocumento(),
                cliente.getTelefono(),
                cliente.getEmail(),
                cliente.getPosicionPreferida() == null
                        ? null
                        : cliente.getPosicionPreferida().name());
    }

    public Perfil actualizarPerfil(
            long clienteId,
            ActualizarPerfil entrada) {
        validarClienteId(clienteId);
        DatosPerfil datos = validarYNormalizarPerfil(entrada);

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                String documento = bloquearClienteActivo(
                        conexion, clienteId);
                validarCorreoDisponible(
                        conexion, clienteId, datos.email());
                actualizarCliente(conexion, clienteId, datos);
                actualizarUsuarioCliente(
                        conexion, clienteId, datos.email());
                conexion.commit();
                return new Perfil(
                        clienteId,
                        datos.nombre(),
                        datos.apellido(),
                        documento,
                        datos.telefono(),
                        datos.email(),
                        datos.posicionPreferida() == null
                                ? null
                                : datos.posicionPreferida().name());
            } catch (SQLException | RuntimeException exception) {
                rollbackSeguro(conexion, exception);
                if (exception instanceof SQLException sql
                        && sql.getErrorCode() == 1062) {
                    throw new ConflictoPerfilException();
                }
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo actualizar el perfil del cliente.",
                    exception);
        }
    }

    private String bloquearClienteActivo(
            Connection conexion,
            long clienteId) throws SQLException {
        String sql = "SELECT documento FROM clientes "
                + "WHERE id = ? AND activo = TRUE FOR UPDATE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, clienteId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getString("documento");
                }
            }
        }
        throw new RecursoClienteNoEncontradoException(
                "El perfil del cliente no esta disponible.");
    }

    private void validarCorreoDisponible(
            Connection conexion,
            long clienteId,
            String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes "
                + "WHERE LOWER(email) = LOWER(?) AND id <> ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, email);
            sentencia.setLong(2, clienteId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                if (resultado.getInt(1) > 0) {
                    throw new ConflictoPerfilException();
                }
            }
        }

        String usuarios = "SELECT COUNT(*) FROM usuarios "
                + "WHERE LOWER(nombre_usuario) = LOWER(?) "
                + "AND (cliente_id IS NULL OR cliente_id <> ?)";
        try (PreparedStatement sentencia =
                conexion.prepareStatement(usuarios)) {
            sentencia.setString(1, email);
            sentencia.setLong(2, clienteId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                if (resultado.getInt(1) > 0) {
                    throw new ConflictoPerfilException();
                }
            }
        }
    }

    private void actualizarCliente(
            Connection conexion,
            long clienteId,
            DatosPerfil datos) throws SQLException {
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, "
                + "telefono = ?, email = ?, posicion_preferida = ? "
                + "WHERE id = ? AND activo = TRUE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, datos.nombre());
            sentencia.setString(2, datos.apellido());
            sentencia.setString(3, datos.telefono());
            sentencia.setString(4, datos.email());
            if (datos.posicionPreferida() == null) {
                sentencia.setNull(5, java.sql.Types.VARCHAR);
            } else {
                sentencia.setString(5, datos.posicionPreferida().name());
            }
            sentencia.setLong(6, clienteId);
            if (sentencia.executeUpdate() == 0) {
                throw new RecursoClienteNoEncontradoException(
                        "El perfil del cliente no esta disponible.");
            }
        }
    }

    private void actualizarUsuarioCliente(
            Connection conexion,
            long clienteId,
            String email) throws SQLException {
        String sql = "UPDATE usuarios SET nombre_usuario = ? "
                + "WHERE cliente_id = ? AND rol = 'CLIENTE' "
                + "AND activo = TRUE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, email);
            sentencia.setLong(2, clienteId);
            int filas = sentencia.executeUpdate();
            if (filas == 0 && !existeUsuarioCliente(
                    conexion, clienteId, email)) {
                throw new RecursoClienteNoEncontradoException(
                        "La cuenta del cliente no esta disponible.");
            }
        }
    }

    private boolean existeUsuarioCliente(
            Connection conexion,
            long clienteId,
            String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios "
                + "WHERE cliente_id = ? AND rol = 'CLIENTE' "
                + "AND activo = TRUE "
                + "AND LOWER(nombre_usuario) = LOWER(?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, clienteId);
            sentencia.setString(2, email);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }

    private DatosPerfil validarYNormalizarPerfil(
            ActualizarPerfil entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException(
                    "Los datos del perfil son obligatorios.");
        }

        String nombre = normalizarNombre(entrada.nombre());
        String apellido = normalizarNombre(entrada.apellido());
        String telefono = entrada.telefono() == null
                ? ""
                : entrada.telefono().trim();
        String email = entrada.email() == null
                ? ""
                : entrada.email().trim().toLowerCase(Locale.ROOT);
        PosicionJugador posicionPreferida = normalizarPosicion(
                entrada.posicionPreferida());

        if (nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio.");
        }
        if (apellido.isBlank()) {
            throw new IllegalArgumentException(
                    "El apellido es obligatorio.");
        }
        if (!TELEFONO.matcher(telefono).matches()) {
            throw new IllegalArgumentException(
                    "El telefono contiene caracteres no validos.");
        }
        if (email.length() > 150 || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "El correo electronico no tiene un formato valido.");
        }
        return new DatosPerfil(
                nombre, apellido, telefono, email, posicionPreferida);
    }

    private PosicionJugador normalizarPosicion(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return PosicionJugador.valueOf(
                    valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "La posicion preferida debe ser DRIVE o REVES.");
        }
    }

    private String normalizarNombre(String valor) {
        if (valor == null || valor.isBlank()) return "";
        String[] palabras = valor.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .split(" ");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.isBlank()) continue;
            if (resultado.length() > 0) resultado.append(' ');
            resultado.append(Character.toUpperCase(palabra.charAt(0)))
                    .append(palabra.substring(1));
        }
        return resultado.toString();
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

    private record DatosPerfil(
            String nombre,
            String apellido,
            String telefono,
            String email,
            PosicionJugador posicionPreferida) {
    }

    // cambiarPasswordClienteV1
    public void cambiarPassword(
            long usuarioId,
            CambiarPassword entrada) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del usuario debe ser positivo.");
        }
        if (entrada == null) {
            throw new IllegalArgumentException(
                    "Los datos para cambiar la contrasena son obligatorios.");
        }

        String actual = entrada.passwordActual();
        String nueva = entrada.passwordNuevo();
        String repetida = entrada.passwordRepetido();

        if (actual == null || actual.isBlank()) {
            throw new IllegalArgumentException(
                    "La contrasena actual es obligatoria.");
        }
        if (nueva == null || nueva.length() < 8) {
            throw new IllegalArgumentException(
                    "La contrasena nueva debe tener al menos 8 caracteres.");
        }
        if (repetida == null || !nueva.equals(repetida)) {
            throw new IllegalArgumentException(
                    "Las contrasenas nuevas no coinciden.");
        }

        usuarioService.cambiarPassword(usuarioId, actual, nueva);
    }

    public HistorialReservas listarReservas(long clienteId) {
        validarClienteId(clienteId);
        ConfiguracionComplejo configuracion = configuracionService.obtener();
        List<ReservaResumen> todas = consultarReservas(
                clienteId, null, configuracion);

        LocalDateTime ahora = LocalDateTime.now();
        List<ReservaResumen> proximas = new ArrayList<>();
        List<ReservaResumen> anteriores = new ArrayList<>();

        for (ReservaResumen reserva : todas) {
            if (esAnterior(reserva, ahora)) {
                anteriores.add(reserva);
            } else {
                proximas.add(reserva);
            }
        }

        Comparator<ReservaResumen> cronologico = Comparator
                .comparing(ReservaResumen::fecha)
                .thenComparing(ReservaResumen::horaInicio)
                .thenComparingLong(ReservaResumen::reservaId);
        proximas.sort(cronologico);
        anteriores.sort(cronologico.reversed());

        return new HistorialReservas(
                proximas,
                anteriores,
                todas.size());
    }

    public ReservaResumen buscarReserva(
            long clienteId,
            long reservaId) {
        validarClienteId(clienteId);
        validarReservaId(reservaId);
        ConfiguracionComplejo configuracion = configuracionService.obtener();
        List<ReservaResumen> resultados = consultarReservas(
                clienteId, reservaId, configuracion);

        if (resultados.isEmpty()) {
            throw new RecursoClienteNoEncontradoException(
                    "La reserva no existe o no pertenece a la cuenta.");
        }
        return resultados.getFirst();
    }

    private List<ReservaResumen> consultarReservas(
            long clienteId,
            Long reservaId,
            ConfiguracionComplejo configuracion) {
        String sql = consultaReservas()
                + " WHERE r.cliente_id = ?"
                + (reservaId == null ? "" : " AND r.id = ?")
                + " GROUP BY r.id, s.codigo_publico, r.estado, c.nombre, "
                + "r.fecha, r.hora_inicio, r.hora_fin, r.precio_total, "
                + "r.fecha_vencimiento, r.fecha_expiracion, "
                + "r.fecha_cancelacion, r.fecha_creacion "
                + "ORDER BY r.fecha DESC, r.hora_inicio DESC";

        List<ReservaResumen> reservas = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, clienteId);
            if (reservaId != null) {
                sentencia.setLong(2, reservaId);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    reservas.add(convertirReserva(
                            resultado, configuracion));
                }
            }
            return reservas;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar las reservas del cliente.",
                    exception);
        }
    }

    private String consultaReservas() {
        return "SELECT r.id, s.codigo_publico, r.estado, "
                + "c.nombre AS cancha, r.fecha, r.hora_inicio, "
                + "r.hora_fin, r.precio_total, r.fecha_vencimiento, "
                + "r.fecha_expiracion, r.fecha_cancelacion, "
                + "r.fecha_creacion, "
                + "COALESCE(SUM(CASE WHEN p.estado = 'ACREDITADO' "
                + "THEN p.importe ELSE 0 END), 0) AS acreditado "
                + "FROM reservas r "
                + "INNER JOIN canchas c ON c.id = r.cancha_id "
                + "LEFT JOIN solicitudes_web_seguimiento s "
                + "ON s.reserva_id = r.id "
                + "LEFT JOIN pagos p ON p.reserva_id = r.id";
    }

    private ReservaResumen convertirReserva(
            ResultSet resultado,
            ConfiguracionComplejo configuracion) throws SQLException {
        BigDecimal precio = resultado.getBigDecimal("precio_total")
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal acreditado = resultado.getBigDecimal("acreditado")
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal senia = precio
                .multiply(configuracion.getPorcentajeSenia())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal saldo = precio.subtract(acreditado)
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        String estado = resultado.getString("estado");

        return new ReservaResumen(
                resultado.getLong("id"),
                resultado.getString("codigo_publico"),
                estado,
                resultado.getString("cancha"),
                resultado.getDate("fecha").toLocalDate(),
                resultado.getTime("hora_inicio").toLocalTime(),
                resultado.getTime("hora_fin").toLocalTime(),
                precio,
                acreditado,
                senia,
                saldo,
                fecha(resultado, "fecha_vencimiento"),
                fecha(resultado, "fecha_expiracion"),
                fecha(resultado, "fecha_cancelacion"),
                fecha(resultado, "fecha_creacion"),
                "PENDIENTE".equals(estado),
                "CONFIRMADA".equals(estado),
                "EXPIRADA".equals(estado),
                configuracion.getMoneda(),
                mensaje(estado));
    }

    private Perfil convertirPerfil(ResultSet resultado)
            throws SQLException {
        return new Perfil(
                resultado.getLong("id"),
                resultado.getString("nombre"),
                resultado.getString("apellido"),
                resultado.getString("documento"),
                resultado.getString("telefono"),
                resultado.getString("email"),
                resultado.getString("posicion_preferida"));
    }

    private boolean esAnterior(
            ReservaResumen reserva,
            LocalDateTime ahora) {
        if (esEstadoFinal(reserva.estado())) return true;
        LocalDateTime fin = LocalDateTime.of(
                reserva.fecha(), reserva.horaFin());
        return !fin.isAfter(ahora);
    }

    private boolean esEstadoFinal(String estado) {
        return "EXPIRADA".equals(estado)
                || "CANCELADA".equals(estado)
                || "COMPLETADA".equals(estado)
                || "AUSENTE".equals(estado);
    }

    private LocalDateTime fecha(
            ResultSet resultado,
            String columna) throws SQLException {
        Timestamp valor = resultado.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private String mensaje(String estado) {
        return switch (estado) {
            case "PENDIENTE" ->
                    "La solicitud espera la acreditacion de la senia.";
            case "CONFIRMADA" -> "La reserva esta confirmada.";
            case "EXPIRADA" ->
                    "La solicitud expiro y el turno fue liberado.";
            case "CANCELADA" -> "La reserva fue cancelada.";
            case "COMPLETADA" -> "La reserva fue completada.";
            case "AUSENTE" ->
                    "La reserva fue marcada como ausencia.";
            default ->
                    "Comunicate con el complejo para mas informacion.";
        };
    }

    private void validarClienteId(long clienteId) {
        if (clienteId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del cliente debe ser positivo.");
        }
    }

    private void validarReservaId(long reservaId) {
        if (reservaId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la reserva debe ser positivo.");
        }
    }

    public static class ConflictoPerfilException
            extends IllegalArgumentException {

        public ConflictoPerfilException() {
            super("El correo electronico ya pertenece a otra cuenta.");
        }
    }

    public static class RecursoClienteNoEncontradoException
            extends RuntimeException {

        public RecursoClienteNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }
}
