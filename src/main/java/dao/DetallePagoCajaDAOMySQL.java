package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.DetallePagoCaja;
import negocio.MetodoPago;

public class DetallePagoCajaDAOMySQL implements DetallePagoCajaDAO {
    @Override
    public List<DetallePagoCaja> listarAcreditadosPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        String sql = "SELECT p.id pago_id, p.reserva_id, p.importe, "
                + "p.metodo_pago, p.referencia, p.fecha_pago, "
                + "r.fecha fecha_turno, r.hora_inicio hora_turno, "
                + "CONCAT(cl.nombre, ' ', cl.apellido) nombre_cliente, "
                + "c.nombre nombre_cancha, u.nombre_usuario "
                + "FROM pagos p "
                + "INNER JOIN reservas r ON r.id = p.reserva_id "
                + "INNER JOIN clientes cl ON cl.id = r.cliente_id "
                + "INNER JOIN canchas c ON c.id = r.cancha_id "
                + "INNER JOIN usuarios u ON u.id = r.usuario_id "
                + "WHERE p.estado = 'ACREDITADO' "
                + "AND p.fecha_pago >= ? AND p.fecha_pago < ? "
                + "ORDER BY p.fecha_pago DESC, p.id DESC";

        List<DetallePagoCaja> detalles = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setTimestamp(1, Timestamp.valueOf(fecha.atStartOfDay()));
            st.setTimestamp(2, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    DetallePagoCaja detalle = new DetallePagoCaja();
                    detalle.setPagoId(rs.getLong("pago_id"));
                    detalle.setReservaId(rs.getLong("reserva_id"));
                    detalle.setImporte(rs.getBigDecimal("importe"));
                    detalle.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                    detalle.setReferencia(rs.getString("referencia"));
                    Timestamp acreditado = rs.getTimestamp("fecha_pago");
                    if (acreditado != null) {
                        detalle.setFechaAcreditacion(acreditado.toLocalDateTime());
                    }
                    detalle.setFechaTurno(rs.getDate("fecha_turno").toLocalDate());
                    detalle.setHoraTurno(rs.getTime("hora_turno").toLocalTime());
                    detalle.setNombreCliente(rs.getString("nombre_cliente"));
                    detalle.setNombreCancha(rs.getString("nombre_cancha"));
                    detalle.setNombreUsuario(rs.getString("nombre_usuario"));
                    detalles.add(detalle);
                }
            }
            return detalles;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo recuperar el detalle de pagos acreditados.",
                    exception);
        }
    }
}
