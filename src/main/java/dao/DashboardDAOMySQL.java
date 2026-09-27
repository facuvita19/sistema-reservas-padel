package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.ResumenDashboard;

public class DashboardDAOMySQL implements DashboardDAO {

    @Override
    public ResumenDashboard obtenerResumen(LocalDateTime momento) {
        if (momento == null) throw new IllegalArgumentException("El momento de referencia es obligatorio.");
        ResumenDashboard resumen = new ResumenDashboard();
        LocalDate fecha = momento.toLocalDate();
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            resumen.setReservasHoy(contar(conexion,
                    "SELECT COUNT(*) FROM reservas WHERE fecha = ? AND estado NOT IN ('CANCELADA','EXPIRADA')",
                    Date.valueOf(fecha)));
            resumen.setCanchasActivas(contar(conexion,
                    "SELECT COUNT(*) FROM canchas WHERE activo = TRUE"));
            resumen.setIngresosDia(sumarRango(conexion, fecha, fecha.plusDays(1)));
            LocalDate inicioMes = fecha.withDayOfMonth(1);
            resumen.setIngresosMes(sumarRango(conexion, inicioMes, inicioMes.plusMonths(1)));
            resumen.setPagosPendientes(contar(conexion,
                    "SELECT COUNT(*) FROM pagos WHERE estado = 'PENDIENTE'"));
            resumen.setReservasPendientesSenia(contar(conexion,
                    "SELECT COUNT(*) FROM reservas WHERE estado = 'PENDIENTE' "
                    + "AND (fecha_vencimiento IS NULL OR fecha_vencimiento > ?)", Timestamp.valueOf(momento)));
            resumen.setReservasProximasAVencer(contar(conexion,
                    "SELECT COUNT(*) FROM reservas WHERE estado = 'PENDIENTE' "
                    + "AND fecha_vencimiento > ? AND fecha_vencimiento <= ?",
                    Timestamp.valueOf(momento), Timestamp.valueOf(momento.plusMinutes(5))));
            resumen.setTurnosPendientesCierre(contar(conexion,
                    "SELECT COUNT(*) FROM reservas WHERE estado = 'CONFIRMADA' "
                    + "AND TIMESTAMP(fecha, hora_fin) <= ?", Timestamp.valueOf(momento)));
            resumen.setCajaCerradaHoy(contar(conexion,
                    "SELECT COUNT(*) FROM cierres_caja WHERE fecha = ? AND estado = 'CERRADA'",
                    Date.valueOf(fecha)) > 0);
            return resumen;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo recuperar el resumen del dashboard.", exception);
        }
    }

    private int contar(Connection conexion, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) sentencia.setObject(i + 1, parametros[i]);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    private BigDecimal sumarRango(Connection conexion, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT COALESCE(SUM(importe),0) FROM pagos WHERE estado='ACREDITADO' "
                + "AND fecha_pago >= ? AND fecha_pago < ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            sentencia.setTimestamp(2, Timestamp.valueOf(hasta.atStartOfDay()));
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getBigDecimal(1);
            }
        }
    }

    @Override
    public List<Reserva> listarProximasReservas(LocalDateTime momento, int limite) {
        if (momento == null) throw new IllegalArgumentException("El momento de referencia es obligatorio.");
        if (limite <= 0) throw new IllegalArgumentException("El límite debe ser mayor que cero.");
        String sql = "SELECT r.id,r.cliente_id,r.cancha_id,r.usuario_id,r.fecha,r.hora_inicio,r.hora_fin,"
                + "r.estado,r.fecha_vencimiento,r.fecha_expiracion,r.cantidad_jugadores,r.comentarios,"
                + "r.observaciones_administrativas,r.precio_total,r.fecha_creacion,"
                + "CONCAT(cl.nombre,' ',cl.apellido) nombre_cliente,c.nombre nombre_cancha,"
                + "u.nombre_usuario nombre_usuario FROM reservas r "
                + "INNER JOIN clientes cl ON cl.id=r.cliente_id "
                + "INNER JOIN canchas c ON c.id=r.cancha_id "
                + "INNER JOIN usuarios u ON u.id=r.usuario_id "
                + "WHERE r.estado IN ('PENDIENTE','CONFIRMADA') "
                + "AND TIMESTAMP(r.fecha,r.hora_inicio) >= ? "
                + "AND (r.estado <> 'PENDIENTE' OR r.fecha_vencimiento IS NULL OR r.fecha_vencimiento > ?) "
                + "ORDER BY r.fecha,r.hora_inicio LIMIT ?";
        List<Reserva> reservas = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            Timestamp referencia = Timestamp.valueOf(momento);
            sentencia.setTimestamp(1, referencia);
            sentencia.setTimestamp(2, referencia);
            sentencia.setInt(3, limite);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) reservas.add(convertirReserva(resultado));
            }
            return reservas;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron recuperar las próximas reservas.", exception);
        }
    }

    private Reserva convertirReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setId(rs.getLong("id"));
        reserva.setClienteId(rs.getLong("cliente_id"));
        reserva.setCanchaId(rs.getLong("cancha_id"));
        reserva.setUsuarioId(rs.getLong("usuario_id"));
        reserva.setFecha(rs.getDate("fecha").toLocalDate());
        reserva.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        reserva.setHoraFin(rs.getTime("hora_fin").toLocalTime());
        reserva.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
        Timestamp vencimiento = rs.getTimestamp("fecha_vencimiento");
        if (vencimiento != null) reserva.setFechaVencimiento(vencimiento.toLocalDateTime());
        Timestamp expiracion = rs.getTimestamp("fecha_expiracion");
        if (expiracion != null) reserva.setFechaExpiracion(expiracion.toLocalDateTime());
        reserva.setCantidadJugadores(rs.getInt("cantidad_jugadores"));
        reserva.setComentarios(rs.getString("comentarios"));
        reserva.setObservacionesAdministrativas(rs.getString("observaciones_administrativas"));
        reserva.setPrecioTotal(rs.getBigDecimal("precio_total"));
        reserva.setNombreCliente(rs.getString("nombre_cliente"));
        reserva.setNombreCancha(rs.getString("nombre_cancha"));
        reserva.setNombreUsuario(rs.getString("nombre_usuario"));
        Timestamp creacion = rs.getTimestamp("fecha_creacion");
        if (creacion != null) reserva.setFechaCreacion(creacion.toLocalDateTime());
        return reserva;
    }
}
