package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.AccionAuditoriaReserva;
import negocio.AuditoriaReserva;
import negocio.EstadoReserva;

public class AuditoriaReservaDAOMySQL implements AuditoriaReservaDAO {

    @Override
    public void guardar(AuditoriaReserva valor) {
        if (valor == null) {
            throw new IllegalArgumentException("La auditoría no puede ser nula.");
        }

        String sql = "INSERT INTO auditoria_reservas "
                + "(reserva_id, usuario_id, accion, estado_anterior, "
                + "estado_nuevo, detalle, fecha_evento) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setLong(1, valor.getReservaId());
            if (valor.getUsuarioId() == null) {
                sentencia.setNull(2, Types.BIGINT);
            } else {
                sentencia.setLong(2, valor.getUsuarioId());
            }
            sentencia.setString(3, valor.getAccion().name());
            cargarEstado(sentencia, 4, valor.getEstadoAnterior());
            cargarEstado(sentencia, 5, valor.getEstadoNuevo());
            sentencia.setString(6, valor.getDetalle());
            sentencia.setTimestamp(7, Timestamp.valueOf(
                    valor.getFechaEvento() == null
                            ? java.time.LocalDateTime.now()
                            : valor.getFechaEvento()));
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) valor.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar la auditoría de la reserva.",
                    exception);
        }
    }

    @Override
    public List<AuditoriaReserva> listarPorReserva(long reservaId) {
        String sql = "SELECT a.id, a.reserva_id, a.usuario_id, a.accion, "
                + "a.estado_anterior, a.estado_nuevo, a.detalle, "
                + "a.fecha_evento, u.nombre_usuario "
                + "FROM auditoria_reservas a "
                + "LEFT JOIN usuarios u ON u.id = a.usuario_id "
                + "WHERE a.reserva_id = ? "
                + "ORDER BY a.fecha_evento DESC, a.id DESC";

        List<AuditoriaReserva> resultado = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, reservaId);
            try (ResultSet rs = sentencia.executeQuery()) {
                while (rs.next()) resultado.add(convertir(rs));
            }
            return resultado;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo recuperar la auditoría de la reserva.",
                    exception);
        }
    }

    private void cargarEstado(
            PreparedStatement sentencia,
            int indice,
            EstadoReserva estado) throws SQLException {
        if (estado == null) sentencia.setNull(indice, Types.VARCHAR);
        else sentencia.setString(indice, estado.name());
    }

    private AuditoriaReserva convertir(ResultSet rs) throws SQLException {
        AuditoriaReserva valor = new AuditoriaReserva();
        valor.setId(rs.getLong("id"));
        valor.setReservaId(rs.getLong("reserva_id"));
        long usuarioId = rs.getLong("usuario_id");
        if (!rs.wasNull()) valor.setUsuarioId(usuarioId);
        valor.setAccion(AccionAuditoriaReserva.valueOf(rs.getString("accion")));

        String anterior = rs.getString("estado_anterior");
        if (anterior != null) valor.setEstadoAnterior(EstadoReserva.valueOf(anterior));
        String nuevo = rs.getString("estado_nuevo");
        if (nuevo != null) valor.setEstadoNuevo(EstadoReserva.valueOf(nuevo));

        valor.setDetalle(rs.getString("detalle"));
        Timestamp fecha = rs.getTimestamp("fecha_evento");
        if (fecha != null) valor.setFechaEvento(fecha.toLocalDateTime());
        valor.setNombreUsuario(rs.getString("nombre_usuario"));
        return valor;
    }
}
