package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.ResumenDashboard;

public class DashboardDAOMySQL implements DashboardDAO {

    @Override
    public ResumenDashboard obtenerResumen(LocalDate fechaReferencia) {
        if (fechaReferencia == null) {
            throw new IllegalArgumentException(
                    "La fecha de referencia es obligatoria."
            );
        }

        ResumenDashboard resumen = new ResumenDashboard();

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            resumen.setReservasHoy(contarReservasHoy(
                    conexion,
                    fechaReferencia
            ));
            resumen.setCanchasActivas(contarCanchasActivas(conexion));
            resumen.setIngresosMes(obtenerIngresosMes(
                    conexion,
                    fechaReferencia
            ));
            resumen.setPagosPendientes(contarPagosPendientes(conexion));
            return resumen;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo recuperar el resumen del dashboard.",
                    exception
            );
        }
    }

    private int contarReservasHoy(
            Connection conexion,
            LocalDate fecha) throws SQLException {

        String sql = "SELECT COUNT(*) FROM reservas "
                + "WHERE fecha = ? AND estado <> 'CANCELADA'";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setDate(1, Date.valueOf(fecha));
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }

    private int contarCanchasActivas(Connection conexion)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM canchas WHERE activo = TRUE";

        try (
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()
        ) {
            resultado.next();
            return resultado.getInt(1);
        }
    }

    private BigDecimal obtenerIngresosMes(
            Connection conexion,
            LocalDate fechaReferencia) throws SQLException {

        LocalDate inicioMes = fechaReferencia.withDayOfMonth(1);
        LocalDate inicioMesSiguiente = inicioMes.plusMonths(1);

        String sql = "SELECT COALESCE(SUM(importe), 0) FROM pagos "
                + "WHERE estado = 'ACREDITADO' "
                + "AND fecha_pago >= ? AND fecha_pago < ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setTimestamp(
                    1,
                    Timestamp.valueOf(inicioMes.atStartOfDay())
            );
            sentencia.setTimestamp(
                    2,
                    Timestamp.valueOf(inicioMesSiguiente.atStartOfDay())
            );

            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getBigDecimal(1);
            }
        }
    }

    private int contarPagosPendientes(Connection conexion)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM pagos "
                + "WHERE estado = 'PENDIENTE'";

        try (
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()
        ) {
            resultado.next();
            return resultado.getInt(1);
        }
    }

    @Override
    public List<Reserva> listarProximasReservas(
            LocalDate fechaReferencia,
            int limite) {

        if (fechaReferencia == null) {
            throw new IllegalArgumentException(
                    "La fecha de referencia es obligatoria."
            );
        }
        if (limite <= 0) {
            throw new IllegalArgumentException(
                    "El límite debe ser mayor que cero."
            );
        }

        String sql = "SELECT r.id, r.cliente_id, r.cancha_id, "
                + "r.usuario_id, r.fecha, r.hora_inicio, r.hora_fin, "
                + "r.estado, r.cantidad_jugadores, r.comentarios, "
                + "r.observaciones_administrativas, r.precio_total, "
                + "r.fecha_creacion, "
                + "CONCAT(cl.nombre, ' ', cl.apellido) AS nombre_cliente, "
                + "c.nombre AS nombre_cancha, "
                + "u.nombre_usuario AS nombre_usuario "
                + "FROM reservas r "
                + "INNER JOIN clientes cl ON cl.id = r.cliente_id "
                + "INNER JOIN canchas c ON c.id = r.cancha_id "
                + "INNER JOIN usuarios u ON u.id = r.usuario_id "
                + "WHERE r.fecha >= ? "
                + "AND r.estado IN ('PENDIENTE', 'CONFIRMADA') "
                + "ORDER BY r.fecha, r.hora_inicio LIMIT ?";

        List<Reserva> reservas = new ArrayList<>();

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setDate(1, Date.valueOf(fechaReferencia));
            sentencia.setInt(2, limite);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    reservas.add(convertirReserva(resultado));
                }
            }
            return reservas;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar las próximas reservas.",
                    exception
            );
        }
    }

    private Reserva convertirReserva(ResultSet resultado)
            throws SQLException {

        Reserva reserva = new Reserva();
        reserva.setId(resultado.getLong("id"));
        reserva.setClienteId(resultado.getLong("cliente_id"));
        reserva.setCanchaId(resultado.getLong("cancha_id"));
        reserva.setUsuarioId(resultado.getLong("usuario_id"));
        reserva.setFecha(resultado.getDate("fecha").toLocalDate());
        reserva.setHoraInicio(
                resultado.getTime("hora_inicio").toLocalTime()
        );
        reserva.setHoraFin(
                resultado.getTime("hora_fin").toLocalTime()
        );
        reserva.setEstado(EstadoReserva.valueOf(
                resultado.getString("estado")
        ));
        reserva.setCantidadJugadores(
                resultado.getInt("cantidad_jugadores")
        );
        reserva.setComentarios(resultado.getString("comentarios"));
        reserva.setObservacionesAdministrativas(
                resultado.getString("observaciones_administrativas")
        );
        reserva.setPrecioTotal(
                resultado.getBigDecimal("precio_total")
        );
        reserva.setNombreCliente(
                resultado.getString("nombre_cliente")
        );
        reserva.setNombreCancha(
                resultado.getString("nombre_cancha")
        );
        reserva.setNombreUsuario(
                resultado.getString("nombre_usuario")
        );

        Timestamp fechaCreacion =
                resultado.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            reserva.setFechaCreacion(
                    fechaCreacion.toLocalDateTime()
            );
        }
        return reserva;
    }
}
