package api.publica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.regex.Pattern;

import config.ConexionBD;
import negocio.Cancha;
import negocio.ConfiguracionComplejo;
import servicio.CanchaService;
import servicio.ConfiguracionComplejoService;

public class SolicitudWebPublicaService {
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TELEFONO =
            Pattern.compile("^[0-9+()\\-\\s]{6,30}$");
    private static final Pattern DOCUMENTO = Pattern.compile("^[0-9]{7,10}$");
    private static final String USUARIO_WEB = "web-reservas";

    private final CanchaService canchaService;
    private final ConfiguracionComplejoService configuracionService;

    public SolicitudWebPublicaService() {
        this(new CanchaService(), new ConfiguracionComplejoService());
    }

    public SolicitudWebPublicaService(
            CanchaService canchaService,
            ConfiguracionComplejoService configuracionService) {
        if (canchaService == null || configuracionService == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de solicitudes no pueden ser nulas.");
        }
        this.canchaService = canchaService;
        this.configuracionService = configuracionService;
    }

    public SolicitudWebDTO.SolicitudCreada crear(
            SolicitudWebDTO.CrearSolicitud entrada) {
        validarEntrada(entrada);
        Cancha cancha = obtenerCancha(entrada.canchaId());
        ConfiguracionComplejo configuracion = configuracionService.obtener();
        LocalTime fin = entrada.horaInicio()
                .plusMinutes(cancha.getDuracionReserva());
        validarFechaYHorario(entrada.fecha(), entrada.horaInicio(), fin,
                cancha, configuracion);

        String llave = crearLlave(cancha.getId(), entrada.fecha(),
                entrada.horaInicio());
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                obtenerBloqueo(conexion, llave);
                expirarPendientes(conexion);
                validarDisponibilidad(conexion, cancha.getId(), entrada.fecha(),
                        entrada.horaInicio(), fin);
                long clienteId = obtenerOCrearCliente(
                        conexion, entrada.cliente());
                long usuarioId = obtenerUsuarioWeb(conexion);
                LocalDateTime vencimiento = LocalDateTime.now().plusMinutes(
                        configuracion.getMinutosReservaPendiente());
                long reservaId = insertarReserva(conexion, clienteId, usuarioId,
                        cancha, entrada, fin, vencimiento);
                conexion.commit();
                return crearRespuesta(reservaId, clienteId, cancha, entrada,
                        fin, vencimiento, configuracion);
            } catch (SQLException | RuntimeException exception) {
                conexion.rollback();
                throw exception;
            } finally {
                liberarBloqueo(conexion, llave);
                conexion.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo crear la solicitud web.", exception);
        }
    }

    private void validarEntrada(SolicitudWebDTO.CrearSolicitud entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria.");
        }
        if (entrada.canchaId() <= 0 || entrada.fecha() == null
                || entrada.horaInicio() == null) {
            throw new IllegalArgumentException(
                    "Cancha, fecha y hora de inicio son obligatorias.");
        }
        if (entrada.cantidadJugadores() < 1
                || entrada.cantidadJugadores() > 8) {
            throw new IllegalArgumentException(
                    "La cantidad de jugadores debe estar entre 1 y 8.");
        }
        validarCliente(entrada.cliente());
    }

    private void validarCliente(SolicitudWebDTO.ClienteEntrada cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException(
                    "Los datos del cliente son obligatorios.");
        }
        obligatorio(cliente.nombre(), "El nombre es obligatorio.");
        obligatorio(cliente.apellido(), "El apellido es obligatorio.");
        obligatorio(cliente.documento(), "El documento es obligatorio.");
        obligatorio(cliente.telefono(), "El teléfono es obligatorio.");
        String documento = soloDigitos(cliente.documento());
        if (!DOCUMENTO.matcher(documento).matches()) {
            throw new IllegalArgumentException(
                    "El documento debe contener entre 7 y 10 números.");
        }
        if (!TELEFONO.matcher(cliente.telefono().trim()).matches()) {
            throw new IllegalArgumentException(
                    "El teléfono contiene caracteres no válidos.");
        }
        if (cliente.email() != null && !cliente.email().isBlank()
                && !EMAIL.matcher(cliente.email().trim()).matches()) {
            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido.");
        }
    }

    private Cancha obtenerCancha(long id) {
        Cancha cancha = canchaService.buscar(id);
        if (cancha == null || !cancha.isActivo()) {
            throw new DisponibilidadPublicaService.RecursoNoEncontradoException(
                    "La cancha no existe o está inactiva.");
        }
        return cancha;
    }

    private void validarFechaYHorario(LocalDate fecha, LocalTime inicio,
            LocalTime fin, Cancha cancha, ConfiguracionComplejo configuracion) {
        LocalDateTime fechaHora = LocalDateTime.of(fecha, inicio);
        if (!fechaHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "El turno debe comenzar en una fecha y hora futura.");
        }
        if (fechaHora.isBefore(LocalDateTime.now().plusHours(
                configuracion.getAnticipacionMinimaHoras()))) {
            throw new IllegalArgumentException(
                    "El turno no cumple la anticipación mínima requerida.");
        }
        if (!cancha.estaDisponibleElDia(fecha.getDayOfWeek())) {
            throw new IllegalArgumentException(
                    "La cancha no está disponible ese día.");
        }
        if (!cancha.contieneHorario(inicio, fin)) {
            throw new IllegalArgumentException(
                    "El horario está fuera de la jornada de la cancha.");
        }
        long minutos = java.time.Duration.between(
                cancha.getHoraApertura(), inicio).toMinutes();
        if (minutos < 0 || minutos % cancha.getDuracionReserva() != 0) {
            throw new IllegalArgumentException(
                    "La hora seleccionada no es un inicio de turno válido.");
        }
    }

    private void obtenerBloqueo(Connection conexion, String llave)
            throws SQLException {
        try (PreparedStatement st = conexion.prepareStatement(
                "SELECT GET_LOCK(?, 5)")) {
            st.setString(1, llave);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    throw new ConflictoDisponibilidadException(
                            "El turno está siendo reservado. Intentá nuevamente.");
                }
            }
        }
    }

    private void liberarBloqueo(Connection conexion, String llave) {
        try (PreparedStatement st = conexion.prepareStatement(
                "SELECT RELEASE_LOCK(?)")) {
            st.setString(1, llave);
            st.executeQuery();
        } catch (SQLException ignored) { }
    }

    private void expirarPendientes(Connection conexion) throws SQLException {
        String sql = "UPDATE reservas r SET r.estado = 'EXPIRADA', "
                + "r.fecha_expiracion = CURRENT_TIMESTAMP, "
                + "r.fecha_vencimiento = NULL "
                + "WHERE r.origen = 'WEB' "
                + "AND r.estado = 'PENDIENTE' "
                + "AND r.fecha_vencimiento IS NOT NULL "
                + "AND r.fecha_vencimiento <= CURRENT_TIMESTAMP "
                + "AND NOT EXISTS (SELECT 1 FROM pagos p "
                + "WHERE p.reserva_id = r.id AND p.estado = 'ACREDITADO')";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.executeUpdate();
        }
    }

    private void validarDisponibilidad(Connection conexion, long canchaId,
            LocalDate fecha, LocalTime inicio, LocalTime fin)
            throws SQLException {
        String reservas = "SELECT COUNT(*) FROM reservas "
                + "WHERE cancha_id = ? AND fecha = ? "
                + "AND estado NOT IN ('CANCELADA', 'EXPIRADA') "
                + "AND hora_inicio < ? AND hora_fin > ?";
        try (PreparedStatement st = conexion.prepareStatement(reservas)) {
            st.setLong(1, canchaId);
            st.setDate(2, Date.valueOf(fecha));
            st.setTime(3, Time.valueOf(fin));
            st.setTime(4, Time.valueOf(inicio));
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new ConflictoDisponibilidadException(
                            "El turno acaba de ser ocupado.");
                }
            }
        }
        String bloqueos = "SELECT COUNT(*) FROM bloqueos_cancha "
                + "WHERE cancha_id = ? AND fecha = ? "
                + "AND hora_inicio < ? AND hora_fin > ?";
        try (PreparedStatement st = conexion.prepareStatement(bloqueos)) {
            st.setLong(1, canchaId);
            st.setDate(2, Date.valueOf(fecha));
            st.setTime(3, Time.valueOf(fin));
            st.setTime(4, Time.valueOf(inicio));
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    throw new ConflictoDisponibilidadException(
                            "La cancha está bloqueada en ese horario.");
                }
            }
        }
    }

    private long obtenerOCrearCliente(Connection conexion,
            SolicitudWebDTO.ClienteEntrada entrada) throws SQLException {
        String documento = soloDigitos(entrada.documento());
        String email = limpiarEmail(entrada.email());
        String buscar = "SELECT id, activo FROM clientes "
                + "WHERE documento = ? OR (? IS NOT NULL AND email = ?) "
                + "ORDER BY documento = ? DESC LIMIT 1 FOR UPDATE";
        try (PreparedStatement st = conexion.prepareStatement(buscar)) {
            st.setString(1, documento);
            cargarNullable(st, 2, email);
            cargarNullable(st, 3, email);
            st.setString(4, documento);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    actualizarCliente(conexion, id, entrada, documento, email);
                    return id;
                }
            }
        }
        String insertar = "INSERT INTO clientes "
                + "(nombre, apellido, documento, telefono, email, activo) "
                + "VALUES (?, ?, ?, ?, ?, TRUE)";
        try (PreparedStatement st = conexion.prepareStatement(
                insertar, Statement.RETURN_GENERATED_KEYS)) {
            cargarCliente(st, entrada, documento, email);
            st.executeUpdate();
            try (ResultSet claves = st.getGeneratedKeys()) {
                if (claves.next()) return claves.getLong(1);
            }
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1062) {
                return buscarClientePorDocumento(conexion, documento);
            }
            throw exception;
        }
        throw new SQLException("No se obtuvo el ID del cliente.");
    }

    private void actualizarCliente(Connection conexion, long id,
            SolicitudWebDTO.ClienteEntrada entrada, String documento,
            String email) throws SQLException {
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, "
                + "telefono = ?, email = COALESCE(?, email), activo = TRUE "
                + "WHERE id = ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setString(1, normalizarNombre(entrada.nombre()));
            st.setString(2, normalizarNombre(entrada.apellido()));
            st.setString(3, entrada.telefono().trim());
            cargarNullable(st, 4, email);
            st.setLong(5, id);
            st.executeUpdate();
        }
    }

    private void cargarCliente(PreparedStatement st,
            SolicitudWebDTO.ClienteEntrada entrada, String documento,
            String email) throws SQLException {
        st.setString(1, normalizarNombre(entrada.nombre()));
        st.setString(2, normalizarNombre(entrada.apellido()));
        st.setString(3, documento);
        st.setString(4, entrada.telefono().trim());
        cargarNullable(st, 5, email);
    }

    private long buscarClientePorDocumento(Connection conexion,
            String documento) throws SQLException {
        try (PreparedStatement st = conexion.prepareStatement(
                "SELECT id FROM clientes WHERE documento = ?")) {
            st.setString(1, documento);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("No se pudo identificar al cliente.");
    }

    private long obtenerUsuarioWeb(Connection conexion) throws SQLException {
        try (PreparedStatement st = conexion.prepareStatement(
                "SELECT id FROM usuarios WHERE nombre_usuario = ? LIMIT 1")) {
            st.setString(1, USUARIO_WEB);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException(
                "Falta ejecutar la migración 11_usuario_sistema_web.sql.");
    }

    private long insertarReserva(Connection conexion, long clienteId,
            long usuarioId, Cancha cancha,
            SolicitudWebDTO.CrearSolicitud entrada, LocalTime fin,
            LocalDateTime vencimiento) throws SQLException {
        String sql = "INSERT INTO reservas "
                + "(cliente_id, cancha_id, usuario_id, origen, fecha, "
                + "hora_inicio, hora_fin, estado, fecha_vencimiento, "
                + "fecha_expiracion, cantidad_jugadores, comentarios, "
                + "observaciones_administrativas, precio_total) "
                + "VALUES (?, ?, ?, 'WEB', ?, ?, ?, 'PENDIENTE', ?, "
                + "NULL, ?, ?, NULL, ?)";
        try (PreparedStatement st = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setLong(1, clienteId);
            st.setLong(2, cancha.getId());
            st.setLong(3, usuarioId);
            st.setDate(4, Date.valueOf(entrada.fecha()));
            st.setTime(5, Time.valueOf(entrada.horaInicio()));
            st.setTime(6, Time.valueOf(fin));
            st.setTimestamp(7, Timestamp.valueOf(vencimiento));
            st.setInt(8, entrada.cantidadJugadores());
            cargarNullable(st, 9, limpiarOpcional(entrada.comentarios()));
            st.setBigDecimal(10, cancha.getPrecio().setScale(
                    2, RoundingMode.HALF_UP));
            st.executeUpdate();
            try (ResultSet claves = st.getGeneratedKeys()) {
                if (claves.next()) return claves.getLong(1);
            }
        }
        throw new SQLException("No se obtuvo el ID de la solicitud.");
    }

    private SolicitudWebDTO.SolicitudCreada crearRespuesta(long reservaId,
            long clienteId, Cancha cancha,
            SolicitudWebDTO.CrearSolicitud entrada, LocalTime fin,
            LocalDateTime vencimiento, ConfiguracionComplejo configuracion) {
        BigDecimal precio = cancha.getPrecio().setScale(
                2, RoundingMode.HALF_UP);
        BigDecimal senia = precio.multiply(configuracion.getPorcentajeSenia())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new SolicitudWebDTO.SolicitudCreada(
                reservaId, "PENDIENTE", clienteId, cancha.getId(),
                cancha.getNombre(), entrada.fecha(), entrada.horaInicio(), fin,
                precio, senia, vencimiento,
                configuracion.getMinutosReservaPendiente(),
                configuracion.getMoneda(),
                "La solicitud fue creada. El turno quedará reservado "
                        + "temporalmente hasta acreditar la seña.");
    }

    private String crearLlave(long canchaId, LocalDate fecha,
            LocalTime hora) {
        return "reserva-web:" + canchaId + ":" + fecha + ":" + hora;
    }

    private void cargarNullable(PreparedStatement st, int indice,
            String valor) throws SQLException {
        if (valor == null) st.setNull(indice, Types.VARCHAR);
        else st.setString(indice, valor);
    }

    private void obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private String soloDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private String limpiarEmail(String valor) {
        return valor == null || valor.isBlank()
                ? null : valor.trim().toLowerCase(Locale.ROOT);
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank()
                ? null : valor.trim().replaceAll("\\s+", " ");
    }

    private String normalizarNombre(String valor) {
        String[] palabras = valor.trim().toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ").split(" ");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (resultado.length() > 0) resultado.append(' ');
            resultado.append(Character.toUpperCase(palabra.charAt(0)))
                    .append(palabra.substring(1));
        }
        return resultado.toString();
    }

    public static class ConflictoDisponibilidadException
            extends IllegalArgumentException {
        public ConflictoDisponibilidadException(String mensaje) {
            super(mensaje);
        }
    }
}
