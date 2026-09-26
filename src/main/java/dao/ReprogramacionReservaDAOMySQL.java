package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.ReprogramacionReserva;

public class ReprogramacionReservaDAOMySQL
        implements ReprogramacionReservaDAO {

    @Override
    public void guardar(ReprogramacionReserva valor) {
        String sql = "INSERT INTO reprogramaciones_reserva "
                + "(reserva_id, cancha_anterior_id, fecha_anterior, "
                + "hora_inicio_anterior, hora_fin_anterior, cancha_nueva_id, "
                + "fecha_nueva, hora_inicio_nueva, hora_fin_nueva, "
                + "precio_anterior, precio_nuevo, motivo, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setLong(1, valor.getReservaId());
            st.setLong(2, valor.getCanchaAnteriorId());
            st.setDate(3, Date.valueOf(valor.getFechaAnterior()));
            st.setTime(4, Time.valueOf(valor.getHoraInicioAnterior()));
            st.setTime(5, Time.valueOf(valor.getHoraFinAnterior()));
            st.setLong(6, valor.getCanchaNuevaId());
            st.setDate(7, Date.valueOf(valor.getFechaNueva()));
            st.setTime(8, Time.valueOf(valor.getHoraInicioNueva()));
            st.setTime(9, Time.valueOf(valor.getHoraFinNueva()));
            st.setBigDecimal(10, valor.getPrecioAnterior());
            st.setBigDecimal(11, valor.getPrecioNuevo());
            st.setString(12, valor.getMotivo());
            st.setLong(13, valor.getUsuarioId());
            st.executeUpdate();
            try (ResultSet claves = st.getGeneratedKeys()) {
                if (claves.next()) valor.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar la reprogramación.", exception);
        }
    }

    @Override
    public List<ReprogramacionReserva> listarPorReserva(long reservaId) {
        String sql = "SELECT * FROM reprogramaciones_reserva "
                + "WHERE reserva_id = ? ORDER BY fecha_reprogramacion DESC";
        List<ReprogramacionReserva> resultado = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setLong(1, reservaId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) resultado.add(convertir(rs));
            }
            return resultado;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo recuperar el historial.", exception);
        }
    }

    private ReprogramacionReserva convertir(ResultSet rs) throws SQLException {
        ReprogramacionReserva valor = new ReprogramacionReserva();
        valor.setId(rs.getLong("id"));
        valor.setReservaId(rs.getLong("reserva_id"));
        valor.setCanchaAnteriorId(rs.getLong("cancha_anterior_id"));
        valor.setFechaAnterior(rs.getDate("fecha_anterior").toLocalDate());
        valor.setHoraInicioAnterior(rs.getTime("hora_inicio_anterior").toLocalTime());
        valor.setHoraFinAnterior(rs.getTime("hora_fin_anterior").toLocalTime());
        valor.setCanchaNuevaId(rs.getLong("cancha_nueva_id"));
        valor.setFechaNueva(rs.getDate("fecha_nueva").toLocalDate());
        valor.setHoraInicioNueva(rs.getTime("hora_inicio_nueva").toLocalTime());
        valor.setHoraFinNueva(rs.getTime("hora_fin_nueva").toLocalTime());
        valor.setPrecioAnterior(rs.getBigDecimal("precio_anterior"));
        valor.setPrecioNuevo(rs.getBigDecimal("precio_nuevo"));
        valor.setMotivo(rs.getString("motivo"));
        valor.setUsuarioId(rs.getLong("usuario_id"));
        Timestamp fecha = rs.getTimestamp("fecha_reprogramacion");
        if (fecha != null) valor.setFechaReprogramacion(fecha.toLocalDateTime());
        return valor;
    }
}
