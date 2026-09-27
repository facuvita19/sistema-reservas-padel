package dao;

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
import negocio.CierreCaja;
import negocio.EstadisticasPadel;
import negocio.EstadisticasPadel.DatoGrafico;
import negocio.EstadoCierreCaja;

public class EstadisticasDAOMySQL implements EstadisticasDAO {

    @Override
    public EstadisticasPadel obtenerEstadisticas(
            LocalDate fechaDesde,
            LocalDate fechaHasta) {
        validarFechas(fechaDesde, fechaHasta);
        EstadisticasPadel estadisticas = new EstadisticasPadel();

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            cargarResumenReservas(conexion, fechaDesde, fechaHasta, estadisticas);
            cargarResumenPagos(conexion, fechaDesde, fechaHasta, estadisticas);
            estadisticas.setReservasPorEstado(consultarReservasPorEstado(
                    conexion, fechaDesde, fechaHasta));
            estadisticas.setReservasPorCancha(consultarReservasPorCancha(
                    conexion, fechaDesde, fechaHasta));
            estadisticas.setIngresosPorMes(consultarIngresosPorMes(
                    conexion, fechaDesde, fechaHasta));
            estadisticas.setIngresosPorMetodo(consultarIngresosPorMetodo(
                    conexion, fechaDesde, fechaHasta));
            estadisticas.setHorariosMasSolicitados(consultarHorarios(
                    conexion, fechaDesde, fechaHasta));
            estadisticas.setCierresCaja(consultarCierres(
                    conexion, fechaDesde, fechaHasta));
            return estadisticas;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar las estadísticas.", exception);
        }
    }

    private void cargarResumenReservas(
            Connection conexion, LocalDate desde, LocalDate hasta,
            EstadisticasPadel estadisticas) throws SQLException {
        String sql = "SELECT COUNT(*) total, "
                + "COALESCE(SUM(estado = 'COMPLETADA'), 0) completadas, "
                + "COALESCE(SUM(estado = 'CANCELADA'), 0) canceladas, "
                + "COALESCE(SUM(estado = 'AUSENTE'), 0) ausentes "
                + "FROM reservas WHERE fecha BETWEEN ? AND ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            fechas(st, desde, hasta);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                estadisticas.setTotalReservas(rs.getInt("total"));
                estadisticas.setReservasCompletadas(rs.getInt("completadas"));
                estadisticas.setReservasCanceladas(rs.getInt("canceladas"));
                estadisticas.setReservasAusentes(rs.getInt("ausentes"));
            }
        }
    }

    private void cargarResumenPagos(
            Connection conexion, LocalDate desde, LocalDate hasta,
            EstadisticasPadel estadisticas) throws SQLException {
        String sqlAcreditados = "SELECT COUNT(*) cantidad, "
                + "COALESCE(SUM(importe), 0) ingresos, "
                + "COALESCE(AVG(importe), 0) promedio FROM pagos "
                + "WHERE estado = 'ACREDITADO' "
                + "AND fecha_pago >= ? AND fecha_pago < ?";
        try (PreparedStatement st = conexion.prepareStatement(sqlAcreditados)) {
            rango(st, desde, hasta);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                estadisticas.setCantidadPagosAcreditados(rs.getInt("cantidad"));
                estadisticas.setIngresosAcreditados(rs.getBigDecimal("ingresos"));
                estadisticas.setTicketPromedio(rs.getBigDecimal("promedio"));
            }
        }

        String sqlReembolsos = "SELECT COALESCE(SUM(importe), 0) total "
                + "FROM pagos WHERE estado = 'REEMBOLSADO' "
                + "AND fecha_reembolso >= ? AND fecha_reembolso < ?";
        try (PreparedStatement st = conexion.prepareStatement(sqlReembolsos)) {
            rango(st, desde, hasta);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                estadisticas.setTotalReembolsado(rs.getBigDecimal("total"));
            }
        }
    }

    private List<DatoGrafico> consultarReservasPorEstado(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT estado etiqueta, COUNT(*) valor FROM reservas "
                + "WHERE fecha BETWEEN ? AND ? GROUP BY estado ORDER BY valor DESC";
        return consultarFechas(conexion, sql, desde, hasta, 10);
    }

    private List<DatoGrafico> consultarReservasPorCancha(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT c.nombre etiqueta, COUNT(*) valor "
                + "FROM reservas r INNER JOIN canchas c ON c.id = r.cancha_id "
                + "WHERE r.fecha BETWEEN ? AND ? AND r.estado <> 'CANCELADA' "
                + "GROUP BY c.id, c.nombre ORDER BY valor DESC";
        return consultarFechas(conexion, sql, desde, hasta, 12);
    }

    private List<DatoGrafico> consultarIngresosPorMes(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT DATE_FORMAT(fecha_pago, '%Y-%m') etiqueta, "
                + "COALESCE(SUM(importe), 0) valor FROM pagos "
                + "WHERE estado = 'ACREDITADO' "
                + "AND fecha_pago >= ? AND fecha_pago < ? "
                + "GROUP BY DATE_FORMAT(fecha_pago, '%Y-%m') ORDER BY etiqueta";
        return consultarRango(conexion, sql, desde, hasta, 24);
    }

    private List<DatoGrafico> consultarIngresosPorMetodo(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT metodo_pago etiqueta, COALESCE(SUM(importe), 0) valor "
                + "FROM pagos WHERE estado = 'ACREDITADO' "
                + "AND fecha_pago >= ? AND fecha_pago < ? "
                + "GROUP BY metodo_pago ORDER BY valor DESC";
        return consultarRango(conexion, sql, desde, hasta, 10);
    }

    private List<DatoGrafico> consultarHorarios(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT DATE_FORMAT(hora_inicio, '%H:%i') etiqueta, "
                + "COUNT(*) valor FROM reservas "
                + "WHERE fecha BETWEEN ? AND ? AND estado <> 'CANCELADA' "
                + "GROUP BY hora_inicio ORDER BY valor DESC, hora_inicio LIMIT 8";
        return consultarFechas(conexion, sql, desde, hasta, 8);
    }

    private List<CierreCaja> consultarCierres(
            Connection conexion, LocalDate desde, LocalDate hasta)
            throws SQLException {
        String sql = "SELECT c.id, c.fecha, c.estado, c.total_acreditado, "
                + "c.total_efectivo_calculado, c.efectivo_declarado, "
                + "c.diferencia_efectivo, c.total_reembolsado, "
                + "c.usuario_cierre_id, c.fecha_cierre, c.observaciones "
                + "FROM cierres_caja c WHERE c.fecha BETWEEN ? AND ? "
                + "ORDER BY c.fecha DESC";
        List<CierreCaja> cierres = new ArrayList<>();
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            fechas(st, desde, hasta);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    CierreCaja cierre = new CierreCaja();
                    cierre.setId(rs.getLong("id"));
                    cierre.setFecha(rs.getDate("fecha").toLocalDate());
                    cierre.setEstado(EstadoCierreCaja.valueOf(rs.getString("estado")));
                    cierre.setTotalAcreditado(rs.getBigDecimal("total_acreditado"));
                    cierre.setTotalEfectivoCalculado(
                            rs.getBigDecimal("total_efectivo_calculado"));
                    cierre.setEfectivoDeclarado(rs.getBigDecimal("efectivo_declarado"));
                    cierre.setDiferenciaEfectivo(rs.getBigDecimal("diferencia_efectivo"));
                    cierre.setTotalReembolsado(rs.getBigDecimal("total_reembolsado"));
                    cierre.setUsuarioCierreId(rs.getLong("usuario_cierre_id"));
                    Timestamp timestamp = rs.getTimestamp("fecha_cierre");
                    if (timestamp != null) {
                        cierre.setFechaCierre(timestamp.toLocalDateTime());
                    }
                    cierre.setObservaciones(rs.getString("observaciones"));
                    cierres.add(cierre);
                }
            }
        }
        return cierres;
    }

    private List<DatoGrafico> consultarFechas(
            Connection conexion, String sql, LocalDate desde,
            LocalDate hasta, int limite) throws SQLException {
        return consultar(conexion, sql, desde, hasta, limite, false);
    }

    private List<DatoGrafico> consultarRango(
            Connection conexion, String sql, LocalDate desde,
            LocalDate hasta, int limite) throws SQLException {
        return consultar(conexion, sql, desde, hasta, limite, true);
    }

    private List<DatoGrafico> consultar(
            Connection conexion, String sql, LocalDate desde,
            LocalDate hasta, int limite, boolean usarRango) throws SQLException {
        List<DatoGrafico> datos = new ArrayList<>();
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            if (usarRango) rango(st, desde, hasta); else fechas(st, desde, hasta);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next() && datos.size() < limite) {
                    datos.add(new DatoGrafico(
                            rs.getString("etiqueta"), rs.getBigDecimal("valor")));
                }
            }
        }
        return datos;
    }

    private void fechas(PreparedStatement st, LocalDate desde, LocalDate hasta)
            throws SQLException {
        st.setDate(1, Date.valueOf(desde));
        st.setDate(2, Date.valueOf(hasta));
    }

    private void rango(PreparedStatement st, LocalDate desde, LocalDate hasta)
            throws SQLException {
        st.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
        st.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
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
