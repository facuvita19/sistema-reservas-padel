package dao;

import java.math.BigDecimal;
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
import negocio.CierreCaja;
import negocio.DetalleMedioPago;
import negocio.EstadoCierreCaja;
import negocio.MetodoPago;
import negocio.ResumenCajaDiaria;

public class CierreCajaDAOMySQL implements CierreCajaDAO {

    @Override
    public ResumenCajaDiaria calcularResumen(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            ResumenCajaDiaria resumen = new ResumenCajaDiaria();
            resumen.setFecha(fecha);
            cargarPagosAcreditados(conexion, fecha, resumen);
            resumen.setTotalReembolsado(sumarReembolsos(conexion, fecha));
            resumen.setPagosPendientes(contarPagosPendientes(conexion, fecha));
            resumen.setReservasCompletadas(contarReservas(conexion, fecha, "COMPLETADA"));
            resumen.setReservasAusentes(contarReservas(conexion, fecha, "AUSENTE"));
            resumen.setReservasCanceladas(contarCancelaciones(conexion, fecha));
            return resumen;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo calcular el resumen diario.", exception);
        }
    }

    private void cargarPagosAcreditados(
            Connection conexion, LocalDate fecha, ResumenCajaDiaria resumen)
            throws SQLException {
        String sql = "SELECT metodo_pago, COUNT(*) cantidad, "
                + "COALESCE(SUM(importe), 0) total FROM pagos "
                + "WHERE estado = 'ACREDITADO' AND fecha_pago >= ? "
                + "AND fecha_pago < ? GROUP BY metodo_pago ORDER BY metodo_pago";
        List<DetalleMedioPago> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal efectivo = BigDecimal.ZERO;
        int cantidad = 0;
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            cargarRango(st, fecha);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    DetalleMedioPago detalle = new DetalleMedioPago();
                    MetodoPago metodo = MetodoPago.valueOf(rs.getString("metodo_pago"));
                    detalle.setMetodoPago(metodo);
                    detalle.setCantidadMovimientos(rs.getInt("cantidad"));
                    detalle.setTotal(rs.getBigDecimal("total"));
                    detalles.add(detalle);
                    cantidad += detalle.getCantidadMovimientos();
                    total = total.add(detalle.getTotal());
                    if (metodo == MetodoPago.EFECTIVO) efectivo = detalle.getTotal();
                }
            }
        }
        resumen.setDetalles(detalles);
        resumen.setCantidadPagosAcreditados(cantidad);
        resumen.setTotalAcreditado(total);
        resumen.setTotalEfectivo(efectivo);
    }

    private BigDecimal sumarReembolsos(Connection conexion, LocalDate fecha)
            throws SQLException {
        String sql = "SELECT COALESCE(SUM(importe), 0) FROM pagos "
                + "WHERE estado = 'REEMBOLSADO' "
                + "AND fecha_reembolso >= ? AND fecha_reembolso < ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            cargarRango(st, fecha);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private int contarPagosPendientes(Connection conexion, LocalDate fecha)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM pagos WHERE estado = 'PENDIENTE' "
                + "AND fecha_creacion >= ? AND fecha_creacion < ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            cargarRango(st, fecha);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int contarReservas(Connection conexion, LocalDate fecha, String estado)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE fecha = ? AND estado = ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setDate(1, Date.valueOf(fecha));
            st.setString(2, estado);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int contarCancelaciones(Connection conexion, LocalDate fecha)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE estado = 'CANCELADA' "
                + "AND fecha_cancelacion >= ? AND fecha_cancelacion < ?";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            cargarRango(st, fecha);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void cargarRango(PreparedStatement st, LocalDate fecha)
            throws SQLException {
        cargarRango(st, fecha, 1);
    }

    private void cargarRango(PreparedStatement st, LocalDate fecha, int inicio)
            throws SQLException {
        st.setTimestamp(inicio, Timestamp.valueOf(fecha.atStartOfDay()));
        st.setTimestamp(inicio + 1, Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));
    }

    @Override
    public CierreCaja buscarPorFecha(LocalDate fecha) {
        String sql = consultaBase() + " WHERE fecha = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return null;
                CierreCaja cierre = convertir(rs);
                cierre.setDetalles(listarDetalles(conexion, cierre.getId()));
                return cierre;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo buscar el cierre de caja.", exception);
        }
    }

    @Override
    public List<CierreCaja> listar() {
        List<CierreCaja> cierres = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(
                     consultaBase() + " ORDER BY fecha DESC");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                CierreCaja cierre = convertir(rs);
                cierre.setDetalles(listarDetalles(conexion, cierre.getId()));
                cierres.add(cierre);
            }
            return cierres;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron listar los cierres.", exception);
        }
    }

    @Override
    public void guardar(CierreCaja cierre) {
        String sql = "INSERT INTO cierres_caja (fecha, estado, total_acreditado, "
                + "total_efectivo_calculado, efectivo_declarado, diferencia_efectivo, "
                + "total_reembolsado, cantidad_pagos_acreditados, pagos_pendientes, "
                + "reservas_completadas, reservas_ausentes, reservas_canceladas, "
                + "observaciones, usuario_cierre_id, fecha_cierre) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conexion = null;
        try {
            conexion = ConexionBD.obtenerConexion();
            conexion.setAutoCommit(false);
            try (PreparedStatement st = conexion.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                st.setDate(1, Date.valueOf(cierre.getFecha()));
                st.setString(2, cierre.getEstado().name());
                st.setBigDecimal(3, cierre.getTotalAcreditado());
                st.setBigDecimal(4, cierre.getTotalEfectivoCalculado());
                st.setBigDecimal(5, cierre.getEfectivoDeclarado());
                st.setBigDecimal(6, cierre.getDiferenciaEfectivo());
                st.setBigDecimal(7, cierre.getTotalReembolsado());
                st.setInt(8, cierre.getCantidadPagosAcreditados());
                st.setInt(9, cierre.getPagosPendientes());
                st.setInt(10, cierre.getReservasCompletadas());
                st.setInt(11, cierre.getReservasAusentes());
                st.setInt(12, cierre.getReservasCanceladas());
                st.setString(13, cierre.getObservaciones());
                st.setLong(14, cierre.getUsuarioCierreId());
                st.setTimestamp(15, Timestamp.valueOf(cierre.getFechaCierre()));
                st.executeUpdate();
                try (ResultSet claves = st.getGeneratedKeys()) {
                    if (claves.next()) cierre.setId(claves.getLong(1));
                }
            }
            guardarDetalles(conexion, cierre);
            conexion.commit();
        } catch (SQLException exception) {
            if (conexion != null) {
                try { conexion.rollback(); } catch (SQLException ignored) { }
            }
            if (exception.getErrorCode() == 1062) {
                throw new IllegalArgumentException("La caja de esa fecha ya fue cerrada.", exception);
            }
            throw new RuntimeException("No se pudo guardar el cierre de caja.", exception);
        } finally {
            if (conexion != null) {
                try { conexion.setAutoCommit(true); conexion.close(); }
                catch (SQLException ignored) { }
            }
        }
    }

    private void guardarDetalles(Connection conexion, CierreCaja cierre)
            throws SQLException {
        String sql = "INSERT INTO cierres_caja_detalle "
                + "(cierre_caja_id, metodo_pago, cantidad_movimientos, total) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            for (DetalleMedioPago detalle : cierre.getDetalles()) {
                st.setLong(1, cierre.getId());
                st.setString(2, detalle.getMetodoPago().name());
                st.setInt(3, detalle.getCantidadMovimientos());
                st.setBigDecimal(4, detalle.getTotal());
                st.addBatch();
            }
            st.executeBatch();
        }
    }

    private List<DetalleMedioPago> listarDetalles(Connection conexion, long cierreId)
            throws SQLException {
        String sql = "SELECT metodo_pago, cantidad_movimientos, total "
                + "FROM cierres_caja_detalle WHERE cierre_caja_id = ? "
                + "ORDER BY metodo_pago";
        List<DetalleMedioPago> detalles = new ArrayList<>();
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setLong(1, cierreId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    DetalleMedioPago detalle = new DetalleMedioPago();
                    detalle.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
                    detalle.setCantidadMovimientos(rs.getInt("cantidad_movimientos"));
                    detalle.setTotal(rs.getBigDecimal("total"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    private String consultaBase() {
        return "SELECT id, fecha, estado, total_acreditado, "
                + "total_efectivo_calculado, efectivo_declarado, diferencia_efectivo, "
                + "total_reembolsado, cantidad_pagos_acreditados, pagos_pendientes, "
                + "reservas_completadas, reservas_ausentes, reservas_canceladas, "
                + "observaciones, usuario_cierre_id, fecha_cierre FROM cierres_caja";
    }

    private CierreCaja convertir(ResultSet rs) throws SQLException {
        CierreCaja cierre = new CierreCaja();
        cierre.setId(rs.getLong("id"));
        cierre.setFecha(rs.getDate("fecha").toLocalDate());
        cierre.setEstado(EstadoCierreCaja.valueOf(rs.getString("estado")));
        cierre.setTotalAcreditado(rs.getBigDecimal("total_acreditado"));
        cierre.setTotalEfectivoCalculado(rs.getBigDecimal("total_efectivo_calculado"));
        cierre.setEfectivoDeclarado(rs.getBigDecimal("efectivo_declarado"));
        cierre.setDiferenciaEfectivo(rs.getBigDecimal("diferencia_efectivo"));
        cierre.setTotalReembolsado(rs.getBigDecimal("total_reembolsado"));
        cierre.setCantidadPagosAcreditados(rs.getInt("cantidad_pagos_acreditados"));
        cierre.setPagosPendientes(rs.getInt("pagos_pendientes"));
        cierre.setReservasCompletadas(rs.getInt("reservas_completadas"));
        cierre.setReservasAusentes(rs.getInt("reservas_ausentes"));
        cierre.setReservasCanceladas(rs.getInt("reservas_canceladas"));
        cierre.setObservaciones(rs.getString("observaciones"));
        cierre.setUsuarioCierreId(rs.getLong("usuario_cierre_id"));
        Timestamp fechaCierre = rs.getTimestamp("fecha_cierre");
        if (fechaCierre != null) cierre.setFechaCierre(fechaCierre.toLocalDateTime());
        return cierre;
    }
}
