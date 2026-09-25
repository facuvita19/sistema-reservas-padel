package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.EstadisticasPadel;
import negocio.EstadisticasPadel.DatoGrafico;

public class EstadisticasDAOMySQL implements EstadisticasDAO {

    @Override
    public EstadisticasPadel obtenerEstadisticas(
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        validarFechas(fechaDesde, fechaHasta);
        EstadisticasPadel estadisticas = new EstadisticasPadel();

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            cargarResumen(conexion, fechaDesde, fechaHasta, estadisticas);
            estadisticas.setReservasPorEstado(consultarReservasPorEstado(
                    conexion, fechaDesde, fechaHasta));
            estadisticas.setReservasPorCancha(consultarReservasPorCancha(
                    conexion, fechaDesde, fechaHasta));
            estadisticas.setIngresosPorMes(consultarIngresosPorMes(
                    conexion, fechaDesde, fechaHasta));
            estadisticas.setHorariosMasSolicitados(consultarHorarios(
                    conexion, fechaDesde, fechaHasta));
            return estadisticas;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar las estadísticas.",
                    exception);
        }
    }

    private void cargarResumen(Connection conexion, LocalDate desde,
            LocalDate hasta, EstadisticasPadel estadisticas)
            throws SQLException {

        String sqlReservas = "SELECT COUNT(*) total, "
                + "SUM(estado = 'COMPLETADA') completadas, "
                + "SUM(estado = 'CANCELADA') canceladas "
                + "FROM reservas WHERE fecha BETWEEN ? AND ?";

        try (PreparedStatement st = conexion.prepareStatement(sqlReservas)) {
            fechas(st, desde, hasta);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                estadisticas.setTotalReservas(rs.getInt("total"));
                estadisticas.setReservasCompletadas(rs.getInt("completadas"));
                estadisticas.setReservasCanceladas(rs.getInt("canceladas"));
            }
        }

        String sqlPagos = "SELECT COALESCE(SUM(p.importe), 0) ingresos, "
                + "COALESCE(AVG(p.importe), 0) promedio "
                + "FROM pagos p INNER JOIN reservas r ON r.id = p.reserva_id "
                + "WHERE p.estado = 'ACREDITADO' AND r.fecha BETWEEN ? AND ?";

        try (PreparedStatement st = conexion.prepareStatement(sqlPagos)) {
            fechas(st, desde, hasta);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                estadisticas.setIngresosAcreditados(rs.getBigDecimal("ingresos"));
                estadisticas.setTicketPromedio(rs.getBigDecimal("promedio"));
            }
        }
    }

    private List<DatoGrafico> consultarReservasPorEstado(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT estado etiqueta, COUNT(*) valor FROM reservas "
                + "WHERE fecha BETWEEN ? AND ? GROUP BY estado ORDER BY valor DESC";
        return consultar(conexion, sql, desde, hasta, 10);
    }

    private List<DatoGrafico> consultarReservasPorCancha(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT c.nombre etiqueta, COUNT(*) valor "
                + "FROM reservas r INNER JOIN canchas c ON c.id = r.cancha_id "
                + "WHERE r.fecha BETWEEN ? AND ? AND r.estado <> 'CANCELADA' "
                + "GROUP BY c.id, c.nombre ORDER BY valor DESC";
        return consultar(conexion, sql, desde, hasta, 10);
    }

    private List<DatoGrafico> consultarIngresosPorMes(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT DATE_FORMAT(r.fecha, '%Y-%m') etiqueta, "
                + "COALESCE(SUM(p.importe), 0) valor "
                + "FROM pagos p INNER JOIN reservas r ON r.id = p.reserva_id "
                + "WHERE p.estado = 'ACREDITADO' AND r.fecha BETWEEN ? AND ? "
                + "GROUP BY DATE_FORMAT(r.fecha, '%Y-%m') ORDER BY etiqueta";
        return consultar(conexion, sql, desde, hasta, 24);
    }

    private List<DatoGrafico> consultarHorarios(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT DATE_FORMAT(hora_inicio, '%H:%i') etiqueta, "
                + "COUNT(*) valor FROM reservas "
                + "WHERE fecha BETWEEN ? AND ? AND estado <> 'CANCELADA' "
                + "GROUP BY hora_inicio ORDER BY valor DESC, hora_inicio LIMIT 8";
        return consultar(conexion, sql, desde, hasta, 8);
    }

    private List<DatoGrafico> consultar(Connection conexion, String sql,
            LocalDate desde, LocalDate hasta, int limite) throws SQLException {
        List<DatoGrafico> datos = new ArrayList<>();
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            fechas(st, desde, hasta);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next() && datos.size() < limite) {
                    datos.add(new DatoGrafico(
                            rs.getString("etiqueta"),
                            rs.getBigDecimal("valor")));
                }
            }
        }
        return datos;
    }

    private void fechas(PreparedStatement st, LocalDate desde,
            LocalDate hasta) throws SQLException {
        st.setDate(1, Date.valueOf(desde));
        st.setDate(2, Date.valueOf(hasta));
    }

    private void validarFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException(
                    "La fecha final no puede ser anterior a la inicial.");
        }
    }
}
