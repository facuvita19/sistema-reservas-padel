package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.MetodoPago;
import negocio.MovimientoCaja;
import negocio.TipoMovimientoCaja;

public class MovimientoCajaDAOMySQL implements MovimientoCajaDAO {
    @Override
    public void guardar(MovimientoCaja movimiento) {
        String sql = "INSERT INTO movimientos_caja "
                + "(fecha, tipo, concepto, importe, medio_pago, observaciones, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setDate(1, Date.valueOf(movimiento.getFecha()));
            st.setString(2, movimiento.getTipo().name());
            st.setString(3, movimiento.getConcepto());
            st.setBigDecimal(4, movimiento.getImporte());
            st.setString(5, movimiento.getMedioPago().name());
            st.setString(6, movimiento.getObservaciones());
            st.setLong(7, movimiento.getUsuarioId());
            st.executeUpdate();
            try (ResultSet claves = st.getGeneratedKeys()) {
                if (claves.next()) movimiento.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo registrar el movimiento de caja.", exception);
        }
    }

    @Override
    public List<MovimientoCaja> listarPorFecha(LocalDate fecha) {
        String sql = "SELECT m.id, m.fecha, m.tipo, m.concepto, m.importe, "
                + "m.medio_pago, m.observaciones, m.usuario_id, m.fecha_creacion, "
                + "u.nombre_usuario FROM movimientos_caja m "
                + "INNER JOIN usuarios u ON u.id = m.usuario_id "
                + "WHERE m.fecha = ? ORDER BY m.fecha_creacion DESC, m.id DESC";
        List<MovimientoCaja> movimientos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    MovimientoCaja movimiento = new MovimientoCaja();
                    movimiento.setId(rs.getLong("id"));
                    movimiento.setFecha(rs.getDate("fecha").toLocalDate());
                    movimiento.setTipo(TipoMovimientoCaja.valueOf(rs.getString("tipo")));
                    movimiento.setConcepto(rs.getString("concepto"));
                    movimiento.setImporte(rs.getBigDecimal("importe"));
                    movimiento.setMedioPago(MetodoPago.valueOf(rs.getString("medio_pago")));
                    movimiento.setObservaciones(rs.getString("observaciones"));
                    movimiento.setUsuarioId(rs.getLong("usuario_id"));
                    movimiento.setNombreUsuario(rs.getString("nombre_usuario"));
                    Timestamp creado = rs.getTimestamp("fecha_creacion");
                    if (creado != null) movimiento.setFechaCreacion(creado.toLocalDateTime());
                    movimientos.add(movimiento);
                }
            }
            return movimientos;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron recuperar los movimientos de caja.", exception);
        }
    }
}
