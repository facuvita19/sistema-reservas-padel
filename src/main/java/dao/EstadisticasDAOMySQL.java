package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Override public EstadisticasPadel obtenerEstadisticas(LocalDate desde, LocalDate hasta) {
        validarFechas(desde,hasta); EstadisticasPadel e=new EstadisticasPadel();
        try(Connection c=ConexionBD.obtenerConexion()){
            cargarResumenReservas(c,desde,hasta,e); cargarResumenPagos(c,desde,hasta,e); cargarMovimientosCaja(c,desde,hasta,e);
            e.setReservasPorEstado(consultarFechas(c,"SELECT estado etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? GROUP BY estado ORDER BY valor DESC",desde,hasta,10));
            e.setReservasPorOrigen(consultarFechas(c,"SELECT CONCAT(estado,'_',origen) etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? GROUP BY estado,origen ORDER BY origen,estado",desde,hasta,20));
            e.setReservasPorDia(consultarFechas(c,"SELECT DATE_FORMAT(fecha,'%Y-%m-%d') etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? GROUP BY fecha ORDER BY fecha",desde,hasta,400));
            e.setCancelacionesPorTipo(consultarFechas(c,"SELECT COALESCE(tipo_cancelacion,'SIN_CLASIFICAR') etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado='CANCELADA' GROUP BY tipo_cancelacion ORDER BY valor DESC",desde,hasta,5));
            e.setEvolucionIncidencias(consultarFechas(c,"SELECT CONCAT(DATE_FORMAT(fecha,'%Y-%m-%d'),'|',estado) etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado IN ('CANCELADA','EXPIRADA','AUSENTE') GROUP BY fecha,estado ORDER BY fecha,estado",desde,hasta,800));
            e.setReservasPorCancha(consultarFechas(c,"SELECT c.nombre etiqueta,COUNT(*) valor FROM reservas r JOIN canchas c ON c.id=r.cancha_id WHERE r.fecha BETWEEN ? AND ? AND r.estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY c.id,c.nombre ORDER BY valor DESC",desde,hasta,12));
            e.setIngresosPorMes(consultarRango(c,"SELECT DATE_FORMAT(fecha_pago,'%Y-%m') etiqueta,COALESCE(SUM(importe),0) valor FROM pagos WHERE estado='ACREDITADO' AND fecha_pago>=? AND fecha_pago<? GROUP BY DATE_FORMAT(fecha_pago,'%Y-%m') ORDER BY etiqueta",desde,hasta,24));
            e.setIngresosPorMetodo(consultarRango(c,"SELECT metodo_pago etiqueta,COALESCE(SUM(importe),0) valor FROM pagos WHERE estado='ACREDITADO' AND fecha_pago>=? AND fecha_pago<? GROUP BY metodo_pago ORDER BY valor DESC",desde,hasta,10));
            e.setHorariosMasSolicitados(consultarFechas(c,"SELECT DATE_FORMAT(hora_inicio,'%H:%i') etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY hora_inicio ORDER BY valor DESC,hora_inicio LIMIT 8",desde,hasta,8));
            e.setDiasMasSolicitados(consultarFechas(c,"SELECT ELT(DAYOFWEEK(fecha),'Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado') etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY ELT(DAYOFWEEK(fecha),'Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado') ORDER BY valor DESC",desde,hasta,7));
            e.setClientesFrecuentes(consultarFechas(c,"SELECT CONCAT(cl.nombre,' ',cl.apellido) etiqueta,COUNT(*) valor FROM reservas r JOIN clientes cl ON cl.id=r.cliente_id WHERE r.fecha BETWEEN ? AND ? AND r.estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY cl.id,cl.nombre,cl.apellido ORDER BY valor DESC LIMIT 8",desde,hasta,8));
            e.setPorcentajeOcupacion(calcularOcupacion(c,desde,hasta));
            e.setCierresCaja(consultarCierres(c,desde,hasta)); return e;
        }catch(SQLException x){throw new RuntimeException("No se pudieron recuperar las estadísticas.",x);}
    }
    private void cargarResumenReservas(Connection c,LocalDate d,LocalDate h,EstadisticasPadel e)throws SQLException{
        String q="SELECT COUNT(*) total,"
                + "COALESCE(SUM(estado='PENDIENTE'),0) pendientes,"
                + "COALESCE(SUM(estado='CONFIRMADA'),0) confirmadas,"
                + "COALESCE(SUM(estado='COMPLETADA'),0) completadas,"
                + "COALESCE(SUM(estado='CANCELADA'),0) canceladas,"
                + "COALESCE(SUM(estado='AUSENTE'),0) ausentes,"
                + "COALESCE(SUM(estado='EXPIRADA'),0) expiradas,"
                + "COALESCE(SUM(estado IN ('PENDIENTE','CONFIRMADA','COMPLETADA','AUSENTE')),0) efectivas,"
                + "COALESCE(SUM(origen='PERSONAL'),0) personal,"
                + "COALESCE(SUM(origen='WEB'),0) web,"
                + "COALESCE(SUM(estado='CANCELADA' AND tipo_cancelacion='CLIENTE'),0) cancel_cliente,"
                + "COALESCE(SUM(estado='CANCELADA' AND tipo_cancelacion='ADMINISTRATIVA'),0) cancel_admin,"
                + "COALESCE(SUM(estado='CANCELADA' AND tipo_cancelacion IS NULL),0) cancel_sin_tipo "
                + "FROM reservas WHERE fecha BETWEEN ? AND ?";
        try(PreparedStatement s=c.prepareStatement(q)){fechas(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setTotalReservas(r.getInt("total"));e.setReservasPendientes(r.getInt("pendientes"));e.setReservasConfirmadas(r.getInt("confirmadas"));e.setReservasCompletadas(r.getInt("completadas"));e.setReservasCanceladas(r.getInt("canceladas"));e.setReservasAusentes(r.getInt("ausentes"));e.setReservasExpiradas(r.getInt("expiradas"));e.setReservasEfectivas(r.getInt("efectivas"));e.setReservasPersonal(r.getInt("personal"));e.setReservasWeb(r.getInt("web"));e.setCancelacionesCliente(r.getInt("cancel_cliente"));e.setCancelacionesAdministrativas(r.getInt("cancel_admin"));e.setCancelacionesSinClasificar(r.getInt("cancel_sin_tipo"));}}
    }
    private void cargarResumenPagos(Connection c,LocalDate d,LocalDate h,EstadisticasPadel e)throws SQLException{
        try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) cantidad,COALESCE(SUM(importe),0) ingresos,COALESCE(AVG(importe),0) promedio FROM pagos WHERE estado='ACREDITADO' AND fecha_pago>=? AND fecha_pago<?")){rango(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setCantidadPagosAcreditados(r.getInt("cantidad"));e.setIngresosAcreditados(r.getBigDecimal("ingresos"));e.setTicketPromedio(r.getBigDecimal("promedio"));}}
        try(PreparedStatement s=c.prepareStatement("SELECT COALESCE(SUM(importe),0) total FROM pagos WHERE estado='REEMBOLSADO' AND fecha_reembolso>=? AND fecha_reembolso<?")){rango(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setTotalReembolsado(r.getBigDecimal("total"));}}
    }
    private void cargarMovimientosCaja(Connection c,LocalDate d,LocalDate h,EstadisticasPadel e)throws SQLException{
        String q="SELECT COALESCE(SUM(CASE WHEN tipo='INGRESO' THEN importe ELSE 0 END),0) ingresos,COALESCE(SUM(CASE WHEN tipo='EGRESO' THEN importe ELSE 0 END),0) egresos FROM movimientos_caja WHERE fecha BETWEEN ? AND ?";
        try(PreparedStatement s=c.prepareStatement(q)){fechas(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setIngresosManuales(r.getBigDecimal("ingresos"));e.setEgresosManuales(r.getBigDecimal("egresos"));}}
    }
        private BigDecimal calcularOcupacion(Connection c,LocalDate d,LocalDate h)throws SQLException{
        String q="WITH RECURSIVE dias AS (SELECT ? fecha UNION ALL SELECT DATE_ADD(fecha,INTERVAL 1 DAY) FROM dias WHERE fecha<?), capacidad AS (SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE,ca.hora_apertura,ca.hora_cierre)),0) minutos FROM dias d JOIN cancha_dias_disponibles cd ON cd.dia_semana=ELT(WEEKDAY(d.fecha)+1,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') JOIN canchas ca ON ca.id=cd.cancha_id AND ca.activo=TRUE), uso AS (SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE,hora_inicio,hora_fin)),0) minutos FROM reservas WHERE fecha BETWEEN ? AND ? AND estado NOT IN ('CANCELADA','EXPIRADA')) SELECT uso.minutos usados,capacidad.minutos capacidad FROM uso,capacidad";
        try(PreparedStatement s=c.prepareStatement(q)){s.setDate(1,Date.valueOf(d));s.setDate(2,Date.valueOf(h));s.setDate(3,Date.valueOf(d));s.setDate(4,Date.valueOf(h));try(ResultSet r=s.executeQuery()){r.next();long cap=r.getLong("capacidad");if(cap<=0)return BigDecimal.ZERO;return BigDecimal.valueOf(r.getLong("usados")).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(cap),2,RoundingMode.HALF_UP);}}
    }
    private List<CierreCaja> consultarCierres(Connection c,LocalDate d,LocalDate h)throws SQLException{
        String q="SELECT cc.id,cc.fecha,cc.estado,cc.total_acreditado,cc.total_efectivo_calculado,cc.efectivo_declarado,cc.diferencia_efectivo,cc.total_reembolsado,cc.usuario_cierre_id,cc.fecha_cierre,cc.observaciones,u.nombre_usuario FROM cierres_caja cc LEFT JOIN usuarios u ON u.id=cc.usuario_cierre_id WHERE cc.fecha BETWEEN ? AND ? ORDER BY cc.fecha DESC";List<CierreCaja> out=new ArrayList<>();
        try(PreparedStatement s=c.prepareStatement(q)){fechas(s,d,h);try(ResultSet r=s.executeQuery()){while(r.next()){CierreCaja x=new CierreCaja();x.setId(r.getLong("id"));x.setFecha(r.getDate("fecha").toLocalDate());x.setEstado(EstadoCierreCaja.valueOf(r.getString("estado")));x.setTotalAcreditado(r.getBigDecimal("total_acreditado"));x.setTotalEfectivoCalculado(r.getBigDecimal("total_efectivo_calculado"));x.setEfectivoDeclarado(r.getBigDecimal("efectivo_declarado"));x.setDiferenciaEfectivo(r.getBigDecimal("diferencia_efectivo"));x.setTotalReembolsado(r.getBigDecimal("total_reembolsado"));x.setUsuarioCierreId(r.getLong("usuario_cierre_id"));x.setNombreUsuarioCierre(r.getString("nombre_usuario"));Timestamp t=r.getTimestamp("fecha_cierre");if(t!=null)x.setFechaCierre(t.toLocalDateTime());x.setObservaciones(r.getString("observaciones"));out.add(x);}}}return out;
    }
    private List<DatoGrafico> consultarFechas(Connection c,String q,LocalDate d,LocalDate h,int l)throws SQLException{return consultar(c,q,d,h,l,false);} private List<DatoGrafico> consultarRango(Connection c,String q,LocalDate d,LocalDate h,int l)throws SQLException{return consultar(c,q,d,h,l,true);}
    private List<DatoGrafico> consultar(Connection c,String q,LocalDate d,LocalDate h,int l,boolean rg)throws SQLException{List<DatoGrafico> out=new ArrayList<>();try(PreparedStatement s=c.prepareStatement(q)){if(rg)rango(s,d,h);else fechas(s,d,h);try(ResultSet r=s.executeQuery()){while(r.next()&&out.size()<l)out.add(new DatoGrafico(r.getString("etiqueta"),r.getBigDecimal("valor")));}}return out;}
    private void fechas(PreparedStatement s,LocalDate d,LocalDate h)throws SQLException{s.setDate(1,Date.valueOf(d));s.setDate(2,Date.valueOf(h));} private void rango(PreparedStatement s,LocalDate d,LocalDate h)throws SQLException{s.setTimestamp(1,Timestamp.valueOf(d.atStartOfDay()));s.setTimestamp(2,Timestamp.valueOf(h.plusDays(1).atStartOfDay()));}
    private void validarFechas(LocalDate d,LocalDate h){if(d==null||h==null)throw new IllegalArgumentException("Las fechas son obligatorias.");if(h.isBefore(d))throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");}
}
