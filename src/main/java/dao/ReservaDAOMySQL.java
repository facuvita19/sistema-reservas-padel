package dao;

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
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.TipoCancelacion;

public class ReservaDAOMySQL implements ReservaDAO {

    @Override
    public void guardar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }
        if (reserva.getId() <= 0) insertar(reserva);
        else actualizar(reserva);
    }

    private void insertar(Reserva reserva) {
        String sql = "INSERT INTO reservas "
                + "(cliente_id, cancha_id, usuario_id, fecha, hora_inicio, "
                + "hora_fin, estado, cantidad_jugadores, comentarios, "
                + "observaciones_administrativas, precio_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, reserva);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException("No se pudo crear la reserva.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) reserva.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar la reserva en MySQL.", exception);
        }
    }

    private void actualizar(Reserva reserva) {
        String sql = "UPDATE reservas SET cliente_id = ?, cancha_id = ?, "
                + "usuario_id = ?, fecha = ?, hora_inicio = ?, hora_fin = ?, "
                + "estado = ?, cantidad_jugadores = ?, comentarios = ?, "
                + "observaciones_administrativas = ?, precio_total = ? "
                + "WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, reserva);
            sentencia.setLong(12, reserva.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("La reserva no existe.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo actualizar la reserva en MySQL.", exception);
        }
    }

    private void cargarParametros(PreparedStatement sentencia, Reserva reserva)
            throws SQLException {
        sentencia.setLong(1, reserva.getClienteId());
        sentencia.setLong(2, reserva.getCanchaId());
        sentencia.setLong(3, reserva.getUsuarioId());
        sentencia.setDate(4, Date.valueOf(reserva.getFecha()));
        sentencia.setTime(5, Time.valueOf(reserva.getHoraInicio()));
        sentencia.setTime(6, Time.valueOf(reserva.getHoraFin()));
        sentencia.setString(7, reserva.getEstado().name());
        sentencia.setInt(8, reserva.getCantidadJugadores());
        sentencia.setString(9, reserva.getComentarios());
        sentencia.setString(10, reserva.getObservacionesAdministrativas());
        sentencia.setBigDecimal(11, reserva.getPrecioTotal());
    }

    @Override
    public Reserva buscar(long id) {
        String sql = consultaBase() + " WHERE r.id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertirResultado(resultado) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo buscar la reserva.", exception);
        }
    }

    @Override
    public List<Reserva> listar() {
        return listarConConsulta(
                consultaBase() + " ORDER BY r.fecha DESC, r.hora_inicio DESC",
                null, null);
    }

    @Override
    public List<Reserva> listarPorCliente(long clienteId) {
        return listarConConsulta(
                consultaBase() + " WHERE r.cliente_id = ? "
                        + "ORDER BY r.fecha DESC, r.hora_inicio DESC",
                clienteId, null);
    }

    @Override
    public List<Reserva> listarPorFecha(LocalDate fecha) {
        return listarConConsulta(
                consultaBase() + " WHERE r.fecha = ? ORDER BY r.hora_inicio",
                null, fecha);
    }

    private List<Reserva> listarConConsulta(
            String sql, Long clienteId, LocalDate fecha) {
        List<Reserva> reservas = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (clienteId != null) sentencia.setLong(1, clienteId);
            else if (fecha != null) sentencia.setDate(1, Date.valueOf(fecha));
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    reservas.add(convertirResultado(resultado));
                }
            }
            return reservas;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar las reservas.", exception);
        }
    }

    @Override
    public void actualizarEstado(long id, EstadoReserva estado) {
        String sql = "UPDATE reservas SET estado = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, estado.name());
            sentencia.setLong(2, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("La reserva no existe.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo actualizar el estado de la reserva.", exception);
        }
    }

    @Override
    public void cancelar(
            long id,
            TipoCancelacion tipo,
            String motivo,
            LocalDateTime fechaCancelacion,
            Long usuarioCancelacionId) {

        String sql = "UPDATE reservas SET estado = 'CANCELADA', "
                + "tipo_cancelacion = ?, motivo_cancelacion = ?, "
                + "fecha_cancelacion = ?, usuario_cancelacion_id = ? "
                + "WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, tipo.name());
            sentencia.setString(2, motivo);
            sentencia.setTimestamp(3, Timestamp.valueOf(fechaCancelacion));
            if (usuarioCancelacionId == null) {
                sentencia.setNull(4, Types.BIGINT);
            } else {
                sentencia.setLong(4, usuarioCancelacionId);
            }
            sentencia.setLong(5, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("La reserva no existe.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo cancelar la reserva.", exception);
        }
    }

    @Override
    public boolean horarioOcupado(
            long canchaId, LocalDate fecha, LocalTime horaInicio,
            LocalTime horaFin, long reservaExcluidaId) {
        String sql = "SELECT COUNT(*) FROM reservas "
                + "WHERE cancha_id = ? AND fecha = ? "
                + "AND estado <> 'CANCELADA' AND id <> ? "
                + "AND hora_inicio < ? AND hora_fin > ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, canchaId);
            sentencia.setDate(2, Date.valueOf(fecha));
            sentencia.setLong(3, reservaExcluidaId);
            sentencia.setTime(4, Time.valueOf(horaFin));
            sentencia.setTime(5, Time.valueOf(horaInicio));
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar la disponibilidad.", exception);
        }
    }

    private String consultaBase() {
        return "SELECT r.id, r.cliente_id, r.cancha_id, r.usuario_id, "
                + "r.fecha, r.hora_inicio, r.hora_fin, r.estado, "
                + "r.tipo_cancelacion, r.motivo_cancelacion, "
                + "r.fecha_cancelacion, r.usuario_cancelacion_id, "
                + "r.cantidad_jugadores, r.comentarios, "
                + "r.observaciones_administrativas, r.precio_total, "
                + "r.fecha_creacion, "
                + "CONCAT(cl.nombre, ' ', cl.apellido) AS nombre_cliente, "
                + "c.nombre AS nombre_cancha, "
                + "u.nombre_usuario AS nombre_usuario "
                + "FROM reservas r "
                + "INNER JOIN clientes cl ON cl.id = r.cliente_id "
                + "INNER JOIN canchas c ON c.id = r.cancha_id "
                + "INNER JOIN usuarios u ON u.id = r.usuario_id";
    }

    private Reserva convertirResultado(ResultSet resultado)
            throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setId(resultado.getLong("id"));
        reserva.setClienteId(resultado.getLong("cliente_id"));
        reserva.setCanchaId(resultado.getLong("cancha_id"));
        reserva.setUsuarioId(resultado.getLong("usuario_id"));
        reserva.setFecha(resultado.getDate("fecha").toLocalDate());
        reserva.setHoraInicio(resultado.getTime("hora_inicio").toLocalTime());
        reserva.setHoraFin(resultado.getTime("hora_fin").toLocalTime());
        reserva.setEstado(EstadoReserva.valueOf(resultado.getString("estado")));

        String tipo = resultado.getString("tipo_cancelacion");
        if (tipo != null) {
            reserva.setTipoCancelacion(TipoCancelacion.valueOf(tipo));
        }
        reserva.setMotivoCancelacion(resultado.getString("motivo_cancelacion"));

        Timestamp fechaCancelacion = resultado.getTimestamp("fecha_cancelacion");
        if (fechaCancelacion != null) {
            reserva.setFechaCancelacion(fechaCancelacion.toLocalDateTime());
        }
        long usuarioCancelacion = resultado.getLong("usuario_cancelacion_id");
        if (!resultado.wasNull()) {
            reserva.setUsuarioCancelacionId(usuarioCancelacion);
        }

        reserva.setCantidadJugadores(resultado.getInt("cantidad_jugadores"));
        reserva.setComentarios(resultado.getString("comentarios"));
        reserva.setObservacionesAdministrativas(
                resultado.getString("observaciones_administrativas"));
        reserva.setPrecioTotal(resultado.getBigDecimal("precio_total"));
        reserva.setNombreCliente(resultado.getString("nombre_cliente"));
        reserva.setNombreCancha(resultado.getString("nombre_cancha"));
        reserva.setNombreUsuario(resultado.getString("nombre_usuario"));

        Timestamp fechaCreacion = resultado.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            reserva.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        return reserva;
    }
}
