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

import api.publica.CuentaClienteDTO.HistorialReservas;
import api.publica.CuentaClienteDTO.Perfil;
import api.publica.CuentaClienteDTO.ReservaResumen;
import config.ConexionBD;
import negocio.Cliente;
import negocio.ConfiguracionComplejo;

public class CuentaClienteService {

    private final ConfiguracionComplejoService configuracionService;

    public CuentaClienteService() {
        this(new ConfiguracionComplejoService());
    }

    public CuentaClienteService(
            ConfiguracionComplejoService configuracionService) {
        if (configuracionService == null) {
            throw new IllegalArgumentException(
                    "El servicio de configuracion no puede ser nulo.");
        }
        this.configuracionService = configuracionService;
    }

    public Perfil obtenerPerfil(long clienteId) {
        validarClienteId(clienteId);
        String sql = "SELECT id, nombre, apellido, documento, telefono, "
                + "email, activo FROM clientes WHERE id = ?";

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
                cliente.getEmail());
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
                resultado.getString("email"));
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

    public static class RecursoClienteNoEncontradoException
            extends RuntimeException {

        public RecursoClienteNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }
}
