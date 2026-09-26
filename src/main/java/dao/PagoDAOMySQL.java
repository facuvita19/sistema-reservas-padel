package dao;

import java.math.BigDecimal;
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
import negocio.EstadoPago;
import negocio.MetodoPago;
import negocio.Pago;

public class PagoDAOMySQL implements PagoDAO {

    @Override
    public void guardar(Pago pago) {
        if (pago == null) throw new IllegalArgumentException("El pago no puede ser nulo.");
        if (pago.getId() <= 0) insertar(pago); else actualizar(pago);
    }

    private void insertar(Pago pago) {
        String sql = "INSERT INTO pagos (reserva_id, importe, metodo_pago, "
                + "estado, referencia, fecha_pago) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, pago);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException("No se pudo crear el pago.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) pago.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo guardar el pago en MySQL.", exception);
        }
    }

    private void actualizar(Pago pago) {
        String sql = "UPDATE pagos SET reserva_id = ?, importe = ?, "
                + "metodo_pago = ?, estado = ?, referencia = ?, fecha_pago = ? "
                + "WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, pago);
            sentencia.setLong(7, pago.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("El pago no existe.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo actualizar el pago en MySQL.", exception);
        }
    }

    private void cargarParametros(PreparedStatement sentencia, Pago pago)
            throws SQLException {
        sentencia.setLong(1, pago.getReservaId());
        sentencia.setBigDecimal(2, pago.getImporte());
        sentencia.setString(3, pago.getMetodoPago().name());
        sentencia.setString(4, pago.getEstado().name());
        if (pago.getReferencia() == null || pago.getReferencia().isBlank()) {
            sentencia.setNull(5, Types.VARCHAR);
        } else sentencia.setString(5, pago.getReferencia());
        if (pago.getFechaPago() == null) sentencia.setNull(6, Types.TIMESTAMP);
        else sentencia.setTimestamp(6, Timestamp.valueOf(pago.getFechaPago()));
    }

    @Override
    public Pago buscar(long id) {
        String sql = consultaBase() + " WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertir(resultado) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo buscar el pago.", exception);
        }
    }

    @Override
    public List<Pago> listar() {
        return listarConConsulta(
                consultaBase() + " ORDER BY fecha_creacion DESC, id DESC", null);
    }

    @Override
    public List<Pago> listarPorReserva(long reservaId) {
        return listarConConsulta(
                consultaBase() + " WHERE reserva_id = ? ORDER BY fecha_creacion, id",
                reservaId);
    }

    private List<Pago> listarConConsulta(String sql, Long reservaId) {
        List<Pago> pagos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (reservaId != null) sentencia.setLong(1, reservaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) pagos.add(convertir(resultado));
            }
            return pagos;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron recuperar los pagos.", exception);
        }
    }

    @Override
    public void actualizarEstado(long id, EstadoPago estado) {
        if (estado == null) throw new IllegalArgumentException("El estado es obligatorio.");
        String sql = "UPDATE pagos SET estado = ?, "
                + "fecha_pago = CASE WHEN ? = 'ACREDITADO' "
                + "THEN COALESCE(fecha_pago, CURRENT_TIMESTAMP) ELSE fecha_pago END, "
                + "fecha_reembolso = CASE WHEN ? = 'REEMBOLSADO' "
                + "THEN COALESCE(fecha_reembolso, CURRENT_TIMESTAMP) "
                + "ELSE fecha_reembolso END WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, estado.name());
            sentencia.setString(2, estado.name());
            sentencia.setString(3, estado.name());
            sentencia.setLong(4, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("El pago no existe.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo actualizar el estado del pago.", exception);
        }
    }

    @Override
    public BigDecimal totalAcreditado(long reservaId) {
        String sql = "SELECT COALESCE(SUM(importe), 0) FROM pagos "
                + "WHERE reserva_id = ? AND estado = 'ACREDITADO'";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, reservaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getBigDecimal(1);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo calcular el total acreditado.", exception);
        }
    }

    private String consultaBase() {
        return "SELECT id, reserva_id, importe, metodo_pago, estado, "
                + "referencia, fecha_pago, fecha_creacion FROM pagos";
    }

    private Pago convertir(ResultSet resultado) throws SQLException {
        Pago pago = new Pago();
        pago.setId(resultado.getLong("id"));
        pago.setReservaId(resultado.getLong("reserva_id"));
        pago.setImporte(resultado.getBigDecimal("importe"));
        pago.setMetodoPago(MetodoPago.valueOf(resultado.getString("metodo_pago")));
        pago.setEstado(EstadoPago.valueOf(resultado.getString("estado")));
        pago.setReferencia(resultado.getString("referencia"));
        Timestamp fechaPago = resultado.getTimestamp("fecha_pago");
        if (fechaPago != null) pago.setFechaPago(fechaPago.toLocalDateTime());
        Timestamp fechaCreacion = resultado.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) pago.setFechaCreacion(fechaCreacion.toLocalDateTime());
        return pago;
    }
}
