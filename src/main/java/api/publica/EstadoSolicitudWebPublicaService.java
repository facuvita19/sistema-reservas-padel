package api.publica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import config.ConexionBD;
import negocio.ConfiguracionComplejo;
import servicio.ConfiguracionComplejoService;

public class EstadoSolicitudWebPublicaService {
    private final ConfiguracionComplejoService configuracionService;

    public EstadoSolicitudWebPublicaService() {
        this(new ConfiguracionComplejoService());
    }

    public EstadoSolicitudWebPublicaService(
            ConfiguracionComplejoService configuracionService) {
        if (configuracionService == null) {
            throw new IllegalArgumentException(
                    "El servicio de configuración no puede ser nulo.");
        }
        this.configuracionService = configuracionService;
    }

    public String obtenerCodigo(long reservaId) {
        if (reservaId <= 0) {
            throw new IllegalArgumentException("La solicitud no es válida.");
        }
        String sql = "SELECT codigo_publico FROM solicitudes_web_seguimiento "
                + "WHERE reserva_id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setLong(1, reservaId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo obtener el código de seguimiento.", exception);
        }
        throw new IllegalStateException(
                "No se generó el código de seguimiento. Verificá la migración 12.");
    }

    public EstadoSolicitudWebDTO.EstadoSolicitud consultar(String codigo) {
        String codigoNormalizado = validarCodigo(codigo);
        String sql = "SELECT s.codigo_publico, r.id, r.estado, r.fecha, "
                + "r.hora_inicio, r.hora_fin, r.precio_total, "
                + "r.fecha_vencimiento, r.fecha_expiracion, "
                + "r.fecha_cancelacion, c.nombre AS cancha, "
                + "COALESCE(SUM(CASE WHEN p.estado = 'ACREDITADO' "
                + "THEN p.importe ELSE 0 END), 0) AS acreditado "
                + "FROM solicitudes_web_seguimiento s "
                + "INNER JOIN reservas r ON r.id = s.reserva_id "
                + "INNER JOIN canchas c ON c.id = r.cancha_id "
                + "LEFT JOIN pagos p ON p.reserva_id = r.id "
                + "WHERE s.codigo_publico = ? AND r.origen = 'WEB' "
                + "GROUP BY s.codigo_publico, r.id, r.estado, r.fecha, "
                + "r.hora_inicio, r.hora_fin, r.precio_total, "
                + "r.fecha_vencimiento, r.fecha_expiracion, "
                + "r.fecha_cancelacion, c.nombre";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setString(1, codigoNormalizado);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    throw new DisponibilidadPublicaService
                            .RecursoNoEncontradoException(
                                    "La solicitud no existe o el código es incorrecto.");
                }
                return convertir(rs);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo consultar el estado de la solicitud.", exception);
        }
    }

    private EstadoSolicitudWebDTO.EstadoSolicitud convertir(ResultSet rs)
            throws SQLException {
        ConfiguracionComplejo configuracion = configuracionService.obtener();
        BigDecimal precio = rs.getBigDecimal("precio_total");
        BigDecimal acreditado = rs.getBigDecimal("acreditado");
        BigDecimal senia = precio.multiply(configuracion.getPorcentajeSenia())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal saldo = precio.subtract(acreditado)
                .max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        String estado = rs.getString("estado");
        return new EstadoSolicitudWebDTO.EstadoSolicitud(
                rs.getString("codigo_publico"), rs.getLong("id"), estado,
                rs.getString("cancha"), rs.getDate("fecha").toLocalDate(),
                rs.getTime("hora_inicio").toLocalTime(),
                rs.getTime("hora_fin").toLocalTime(), precio, acreditado,
                senia, saldo, fecha(rs, "fecha_vencimiento"),
                fecha(rs, "fecha_expiracion"),
                fecha(rs, "fecha_cancelacion"),
                "PENDIENTE".equals(estado), "CONFIRMADA".equals(estado),
                "EXPIRADA".equals(estado), configuracion.getMoneda(),
                mensaje(estado));
    }

    private String validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de seguimiento es obligatorio.");
        }
        try {
            return UUID.fromString(codigo.trim()).toString();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "El código de seguimiento no tiene un formato válido.");
        }
    }

    private java.time.LocalDateTime fecha(ResultSet rs, String columna)
            throws SQLException {
        Timestamp valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private String mensaje(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "La solicitud está esperando la acreditación de la seña.";
            case "CONFIRMADA" -> "La reserva está confirmada.";
            case "EXPIRADA" -> "La solicitud expiró y el turno fue liberado.";
            case "CANCELADA" -> "La reserva fue cancelada.";
            case "COMPLETADA" -> "La reserva fue completada.";
            case "AUSENTE" -> "La reserva fue marcada como ausencia.";
            default -> "Consultá al complejo para obtener más información.";
        };
    }
}
