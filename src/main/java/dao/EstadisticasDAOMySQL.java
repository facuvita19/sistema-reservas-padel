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
            cargarResumenReservas(c,desde,hasta,e); cargarResumenPagos(c,desde,hasta,e); cargarMovimientosCaja(c,desde,hasta,e); cargarIndicadoresFinancieros(c,desde,hasta,e); cargarIndicadoresClientes(c,desde,hasta,e); cargarIndicadoresCanchas(c,desde,hasta,e); cargarIndicadoresWeb(c,desde,hasta,e);
            e.setReservasPorEstado(consultarFechas(c,"SELECT estado etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? GROUP BY estado ORDER BY valor DESC",desde,hasta,10));
            e.setReservasPorOrigen(consultarFechas(c,"SELECT CONCAT(estado,'_',origen) etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? GROUP BY estado,origen ORDER BY origen,estado",desde,hasta,20));
            e.setReservasPorDia(consultarFechas(c,"SELECT DATE_FORMAT(fecha,'%Y-%m-%d') etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? GROUP BY fecha ORDER BY fecha",desde,hasta,400));
            e.setCancelacionesPorTipo(consultarFechas(c,"SELECT COALESCE(tipo_cancelacion,'SIN_CLASIFICAR') etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado='CANCELADA' GROUP BY tipo_cancelacion ORDER BY valor DESC",desde,hasta,5));
            e.setEvolucionIncidencias(consultarFechas(c,"SELECT CONCAT(DATE_FORMAT(fecha,'%Y-%m-%d'),'|',estado) etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado IN ('CANCELADA','EXPIRADA','AUSENTE') GROUP BY fecha,estado ORDER BY fecha,estado",desde,hasta,800));
            e.setReservasPorCancha(consultarFechas(c,"SELECT c.nombre etiqueta,COUNT(*) valor FROM reservas r JOIN canchas c ON c.id=r.cancha_id WHERE r.fecha BETWEEN ? AND ? AND r.estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY c.id,c.nombre ORDER BY valor DESC",desde,hasta,12));
            e.setIngresosPorMes(consultarRango(c,"SELECT DATE_FORMAT(fecha_pago,'%Y-%m') etiqueta,COALESCE(SUM(importe),0) valor FROM pagos WHERE estado IN ('ACREDITADO','REEMBOLSADO') AND fecha_pago>=? AND fecha_pago<? GROUP BY DATE_FORMAT(fecha_pago,'%Y-%m') ORDER BY etiqueta",desde,hasta,24));
            e.setIngresosPorMetodo(consultarRango(c,"SELECT metodo_pago etiqueta,COALESCE(SUM(importe),0) valor FROM pagos WHERE estado IN ('ACREDITADO','REEMBOLSADO') AND fecha_pago>=? AND fecha_pago<? GROUP BY metodo_pago ORDER BY valor DESC",desde,hasta,10));
            e.setPagosRecibidosPorDia(consultarRango(c,"SELECT DATE_FORMAT(fecha_pago,'%Y-%m-%d') etiqueta,COALESCE(SUM(importe),0) valor FROM pagos WHERE estado IN ('ACREDITADO','REEMBOLSADO') AND fecha_pago>=? AND fecha_pago<? GROUP BY DATE_FORMAT(fecha_pago,'%Y-%m-%d') ORDER BY DATE_FORMAT(fecha_pago,'%Y-%m-%d')",desde,hasta,400));
            e.setReembolsosPorDia(consultarRangoReembolso(c,"SELECT DATE_FORMAT(fecha_reembolso,'%Y-%m-%d') etiqueta,COALESCE(SUM(importe),0) valor FROM pagos WHERE estado='REEMBOLSADO' AND fecha_reembolso>=? AND fecha_reembolso<? GROUP BY DATE_FORMAT(fecha_reembolso,'%Y-%m-%d') ORDER BY DATE_FORMAT(fecha_reembolso,'%Y-%m-%d')",desde,hasta,400));
            e.setMovimientosCajaPorTipo(consultarFechas(c,"SELECT tipo etiqueta,COALESCE(SUM(importe),0) valor FROM movimientos_caja WHERE fecha BETWEEN ? AND ? GROUP BY tipo ORDER BY tipo",desde,hasta,5));
            e.setMovimientosCajaPorMedio(consultarFechas(c,"SELECT medio_pago etiqueta,COALESCE(SUM(CASE WHEN tipo='INGRESO' THEN importe ELSE -importe END),0) valor FROM movimientos_caja WHERE fecha BETWEEN ? AND ? GROUP BY medio_pago ORDER BY ABS(valor) DESC",desde,hasta,10));
            e.setDiferenciasCajaPorDia(consultarFechas(c,"SELECT DATE_FORMAT(fecha,'%Y-%m-%d') etiqueta,diferencia_efectivo valor FROM cierres_caja WHERE fecha BETWEEN ? AND ? ORDER BY fecha",desde,hasta,400));
            e.setHorariosMasSolicitados(consultarFechas(c,"SELECT DATE_FORMAT(hora_inicio,'%H:%i') etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY hora_inicio ORDER BY valor DESC,hora_inicio LIMIT 8",desde,hasta,8));
            e.setDiasMasSolicitados(consultarFechas(c,"SELECT ELT(DAYOFWEEK(fecha),'Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado') etiqueta,COUNT(*) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY ELT(DAYOFWEEK(fecha),'Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado') ORDER BY valor DESC",desde,hasta,7));
            e.setClientesFrecuentes(consultarFechas(c,"SELECT CONCAT(cl.nombre,' ',cl.apellido) etiqueta,COUNT(*) valor FROM reservas r JOIN clientes cl ON cl.id=r.cliente_id WHERE r.fecha BETWEEN ? AND ? AND r.estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY cl.id,cl.nombre,cl.apellido ORDER BY valor DESC LIMIT 8",desde,hasta,8));
            e.setClientesPorFacturacion(consultarRango(c,"SELECT CONCAT(cl.nombre,' ',cl.apellido) etiqueta,COALESCE(SUM(p.importe),0) valor FROM pagos p JOIN reservas r ON r.id=p.reserva_id JOIN clientes cl ON cl.id=r.cliente_id WHERE p.estado IN ('ACREDITADO','REEMBOLSADO') AND p.fecha_pago>=? AND p.fecha_pago<? GROUP BY cl.id,cl.nombre,cl.apellido ORDER BY valor DESC LIMIT 10",desde,hasta,10));
            e.setClientesNuevosPorMes(consultarRangoCliente(c,"SELECT DATE_FORMAT(fecha_creacion,'%Y-%m') etiqueta,COUNT(*) valor FROM clientes WHERE fecha_creacion>=? AND fecha_creacion<? GROUP BY DATE_FORMAT(fecha_creacion,'%Y-%m') ORDER BY etiqueta",desde,hasta,36));
            e.setClientesPorPosicion(consultarSinFechas(c,"SELECT COALESCE(posicion_preferida,'SIN_DEFINIR') etiqueta,COUNT(*) valor FROM clientes WHERE activo=TRUE GROUP BY posicion_preferida ORDER BY valor DESC",5));
            e.setActividadClientes(consultarActividadClientes(c,desde,hasta));
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
        try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) cantidad,COALESCE(SUM(importe),0) ingresos,COALESCE(AVG(importe),0) promedio FROM pagos WHERE estado IN ('ACREDITADO','REEMBOLSADO') AND fecha_pago>=? AND fecha_pago<?")){rango(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setCantidadPagosAcreditados(r.getInt("cantidad"));e.setIngresosAcreditados(r.getBigDecimal("ingresos"));e.setTicketPromedio(r.getBigDecimal("promedio"));}}
        try(PreparedStatement s=c.prepareStatement("SELECT COALESCE(SUM(importe),0) total FROM pagos WHERE estado='REEMBOLSADO' AND fecha_reembolso>=? AND fecha_reembolso<?")){rango(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setTotalReembolsado(r.getBigDecimal("total"));}}
    }
    private void cargarMovimientosCaja(Connection c,LocalDate d,LocalDate h,EstadisticasPadel e)throws SQLException{
        String q="SELECT COALESCE(SUM(CASE WHEN tipo='INGRESO' THEN importe ELSE 0 END),0) ingresos,COALESCE(SUM(CASE WHEN tipo='EGRESO' THEN importe ELSE 0 END),0) egresos FROM movimientos_caja WHERE fecha BETWEEN ? AND ?";
        try(PreparedStatement s=c.prepareStatement(q)){fechas(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setIngresosManuales(r.getBigDecimal("ingresos"));e.setEgresosManuales(r.getBigDecimal("egresos"));}}
    }
        private void cargarIndicadoresFinancieros(Connection c,LocalDate d,LocalDate h,EstadisticasPadel e)throws SQLException{
        String pendientes="SELECT COUNT(*) FROM pagos WHERE estado='PENDIENTE' AND fecha_creacion>=? AND fecha_creacion<?";
        try(PreparedStatement s=c.prepareStatement(pendientes)){rango(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setPagosPendientes(r.getInt(1));}}
        String diferencias="SELECT COALESCE(SUM(diferencia_efectivo),0) FROM cierres_caja WHERE fecha BETWEEN ? AND ?";
        try(PreparedStatement s=c.prepareStatement(diferencias)){fechas(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setDiferenciaEfectivoAcumulada(r.getBigDecimal(1));}}
    }
    private void cargarIndicadoresClientes(Connection c,LocalDate d,LocalDate h,EstadisticasPadel e)throws SQLException{
        String base="SELECT COUNT(*) total,COALESCE(SUM(activo=TRUE),0) activos,COALESCE(SUM(activo=FALSE),0) inactivos FROM clientes";
        try(PreparedStatement s=c.prepareStatement(base);ResultSet r=s.executeQuery()){r.next();e.setTotalClientes(r.getInt("total"));e.setClientesActivos(r.getInt("activos"));e.setClientesInactivos(r.getInt("inactivos"));}
        String nuevos="SELECT COUNT(*) FROM clientes WHERE fecha_creacion>=? AND fecha_creacion<?";
        try(PreparedStatement s=c.prepareStatement(nuevos)){rango(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setClientesNuevos(r.getInt(1));}}
        String actividad="SELECT COUNT(*) clientes,COALESCE(SUM(cantidad=1),0) una,COALESCE(SUM(cantidad>=2),0) recurrentes,COALESCE(AVG(cantidad),0) promedio FROM (SELECT cliente_id,COUNT(*) cantidad FROM reservas WHERE fecha BETWEEN ? AND ? AND estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY cliente_id) x";
        try(PreparedStatement s=c.prepareStatement(actividad)){fechas(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setClientesConReservas(r.getInt("clientes"));e.setClientesUnaReserva(r.getInt("una"));e.setClientesRecurrentes(r.getInt("recurrentes"));e.setPromedioReservasPorCliente(r.getBigDecimal("promedio"));}}
        String cuentas="SELECT COUNT(*) total,COALESCE(SUM(fecha_creacion>=? AND fecha_creacion<?),0) nuevas FROM usuarios WHERE rol='CLIENTE' AND cliente_id IS NOT NULL AND activo=TRUE";
        try(PreparedStatement s=c.prepareStatement(cuentas)){rango(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setClientesConCuentaWeb(r.getInt("total"));e.setCuentasWebNuevas(r.getInt("nuevas"));}}
        String torneos="SELECT COUNT(DISTINCT tij.cliente_id) FROM torneo_inscripcion_jugadores tij JOIN torneo_inscripciones ti ON ti.id=tij.inscripcion_id WHERE tij.cliente_id IS NOT NULL AND ti.fecha_solicitud>=? AND ti.fecha_solicitud<? AND ti.estado IN ('PENDIENTE','CONFIRMADA','LISTA_ESPERA')";
        try(PreparedStatement s=c.prepareStatement(torneos)){rango(s,d,h);try(ResultSet r=s.executeQuery()){r.next();e.setParticipantesTorneos(r.getInt(1));}}
    }
    private List<DatoGrafico> consultarSinFechas(Connection c,String q,int limite)throws SQLException{List<DatoGrafico> out=new ArrayList<>();try(PreparedStatement s=c.prepareStatement(q);ResultSet r=s.executeQuery()){while(r.next()&&out.size()<limite)out.add(new DatoGrafico(r.getString("etiqueta"),r.getBigDecimal("valor")));}return out;}
    private List<DatoGrafico> consultarActividadClientes(Connection c,LocalDate d,LocalDate h)throws SQLException{List<DatoGrafico> out=new ArrayList<>();String q="SELECT 'RESERVAS' etiqueta,COUNT(DISTINCT cliente_id) valor FROM reservas WHERE fecha BETWEEN ? AND ? AND estado NOT IN ('CANCELADA','EXPIRADA') UNION ALL SELECT 'TORNEOS',COUNT(DISTINCT tij.cliente_id) FROM torneo_inscripcion_jugadores tij JOIN torneo_inscripciones ti ON ti.id=tij.inscripcion_id WHERE tij.cliente_id IS NOT NULL AND ti.fecha_solicitud>=? AND ti.fecha_solicitud<? AND ti.estado IN ('PENDIENTE','CONFIRMADA','LISTA_ESPERA')";try(PreparedStatement s=c.prepareStatement(q)){fechas(s,d,h);s.setTimestamp(3,Timestamp.valueOf(d.atStartOfDay()));s.setTimestamp(4,Timestamp.valueOf(h.plusDays(1).atStartOfDay()));try(ResultSet r=s.executeQuery()){while(r.next())out.add(new DatoGrafico(r.getString("etiqueta"),r.getBigDecimal("valor")));}}return out;}
    private void cargarIndicadoresCanchas(Connection c,LocalDate desde,LocalDate hasta,EstadisticasPadel e)throws SQLException{
      try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) total,COALESCE(SUM(activo),0) activas,COALESCE(SUM(NOT activo),0) inactivas FROM canchas");ResultSet q=s.executeQuery()){q.next();e.setTotalCanchas(q.getInt("total"));e.setCanchasActivas(q.getInt("activas"));e.setCanchasInactivas(q.getInt("inactivas"));}
      String cap="WITH RECURSIVE dias AS (SELECT ? fecha UNION ALL SELECT DATE_ADD(fecha,INTERVAL 1 DAY) FROM dias WHERE fecha<?) SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE,c.hora_apertura,c.hora_cierre)),0) FROM dias d JOIN cancha_dias_disponibles cd ON cd.dia_semana=ELT(WEEKDAY(d.fecha)+1,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') JOIN canchas c ON c.id=cd.cancha_id AND c.activo=TRUE";try(PreparedStatement s=c.prepareStatement(cap)){fechas(s,desde,hasta);try(ResultSet q=s.executeQuery()){q.next();e.setMinutosCapacidad(q.getBigDecimal(1));}}
      String res="SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE,hora_inicio,hora_fin)),0) FROM reservas WHERE fecha BETWEEN ? AND ? AND estado NOT IN ('CANCELADA','EXPIRADA')";try(PreparedStatement s=c.prepareStatement(res)){fechas(s,desde,hasta);try(ResultSet q=s.executeQuery()){q.next();e.setMinutosReservados(q.getBigDecimal(1));}}
      String blo="SELECT COUNT(*),COALESCE(SUM(TIMESTAMPDIFF(MINUTE,hora_inicio,hora_fin)),0) FROM bloqueos_cancha WHERE fecha BETWEEN ? AND ?";try(PreparedStatement s=c.prepareStatement(blo)){fechas(s,desde,hasta);try(ResultSet q=s.executeQuery()){q.next();e.setBloqueosCancha(q.getInt(1));e.setMinutosBloqueados(q.getBigDecimal(2));}}
      String tor="SELECT COUNT(*),COALESCE(SUM(TIMESTAMPDIFF(MINUTE,hora_inicio,hora_fin)),0) FROM torneo_partidos WHERE fecha BETWEEN ? AND ? AND estado IN ('PROGRAMADO','EN_CURSO','FINALIZADO')";try(PreparedStatement s=c.prepareStatement(tor)){fechas(s,desde,hasta);try(ResultSet q=s.executeQuery()){q.next();e.setPartidosTorneo(q.getInt(1));e.setMinutosTorneos(q.getBigDecimal(2));}}
      e.setIngresosPorCancha(consultarRango(c,"SELECT ca.nombre etiqueta,COALESCE(SUM(p.importe),0) valor FROM pagos p JOIN reservas r ON r.id=p.reserva_id JOIN canchas ca ON ca.id=r.cancha_id WHERE p.estado IN ('ACREDITADO','REEMBOLSADO') AND p.fecha_pago>=? AND p.fecha_pago<? GROUP BY ca.id,ca.nombre ORDER BY valor DESC",desde,hasta,20));
      e.setUsoMinutosPorCancha(consultarFechas(c,"SELECT ca.nombre etiqueta,COALESCE(SUM(TIMESTAMPDIFF(MINUTE,r.hora_inicio,r.hora_fin)),0) valor FROM canchas ca LEFT JOIN reservas r ON r.cancha_id=ca.id AND r.fecha BETWEEN ? AND ? AND r.estado NOT IN ('CANCELADA','EXPIRADA') GROUP BY ca.id,ca.nombre,ca.orden_visual ORDER BY ca.orden_visual,ca.nombre",desde,hasta,30));
      e.setBloqueosPorCancha(consultarFechas(c,"SELECT ca.nombre etiqueta,COUNT(b.id) valor FROM canchas ca LEFT JOIN bloqueos_cancha b ON b.cancha_id=ca.id AND b.fecha BETWEEN ? AND ? GROUP BY ca.id,ca.nombre,ca.orden_visual ORDER BY ca.orden_visual,ca.nombre",desde,hasta,30));
      e.setTorneosPorCancha(consultarFechas(c,"SELECT ca.nombre etiqueta,COUNT(tp.id) valor FROM canchas ca LEFT JOIN torneo_partidos tp ON tp.cancha_id=ca.id AND tp.fecha BETWEEN ? AND ? AND tp.estado IN ('PROGRAMADO','EN_CURSO','FINALIZADO') GROUP BY ca.id,ca.nombre,ca.orden_visual ORDER BY ca.orden_visual,ca.nombre",desde,hasta,30));
    }
    private void cargarIndicadoresWeb(Connection c,LocalDate d,LocalDate h,EstadisticasPadel e)throws SQLException{
        String resumen="SELECT COUNT(*) total,COALESCE(SUM(r.estado='PENDIENTE'),0) pendientes,COALESCE(SUM(r.estado='CONFIRMADA'),0) confirmadas,COALESCE(SUM(r.estado='EXPIRADA'),0) expiradas,COALESCE(SUM(r.estado='CANCELADA'),0) canceladas,COALESCE(SUM(r.estado='COMPLETADA'),0) completadas,COALESCE(SUM(r.estado='AUSENTE'),0) ausentes FROM solicitudes_web_seguimiento s JOIN reservas r ON r.id=s.reserva_id WHERE s.fecha_creacion>=? AND s.fecha_creacion<?";
        try(PreparedStatement s=c.prepareStatement(resumen)){rango(s,d,h);try(ResultSet q=s.executeQuery()){q.next();e.setWebSolicitudesCreadas(q.getInt("total"));e.setWebPendientes(q.getInt("pendientes"));e.setWebConfirmadasActuales(q.getInt("confirmadas"));e.setWebExpiradas(q.getInt("expiradas"));e.setWebCanceladas(q.getInt("canceladas"));e.setWebCompletadas(q.getInt("completadas"));e.setWebAusentes(q.getInt("ausentes"));}}
        String pagos="SELECT COUNT(DISTINCT CASE WHEN p.id IS NOT NULL THEN r.id END) con_pago,COALESCE(SUM(p.importe),0) recibido,COALESCE(AVG(GREATEST(TIMESTAMPDIFF(MINUTE,r.fecha_creacion,primer.primer_pago),0)),0) promedio FROM solicitudes_web_seguimiento s JOIN reservas r ON r.id=s.reserva_id LEFT JOIN pagos p ON p.reserva_id=r.id AND p.estado IN ('ACREDITADO','REEMBOLSADO') LEFT JOIN (SELECT reserva_id,MIN(fecha_pago) primer_pago FROM pagos WHERE estado IN ('ACREDITADO','REEMBOLSADO') AND fecha_pago IS NOT NULL GROUP BY reserva_id) primer ON primer.reserva_id=r.id WHERE s.fecha_creacion>=? AND s.fecha_creacion<?";
        try(PreparedStatement s=c.prepareStatement(pagos)){rango(s,d,h);try(ResultSet q=s.executeQuery()){q.next();e.setWebConPago(q.getInt("con_pago"));e.setWebDineroRecibido(q.getBigDecimal("recibido"));e.setWebPromedioMinutosPrimerPago(q.getBigDecimal("promedio"));}}
        String reembolsos="SELECT COALESCE(SUM(p.importe),0) FROM pagos p JOIN reservas r ON r.id=p.reserva_id WHERE r.origen='WEB' AND p.estado='REEMBOLSADO' AND p.fecha_reembolso>=? AND p.fecha_reembolso<?";
        try(PreparedStatement s=c.prepareStatement(reembolsos)){rango(s,d,h);try(ResultSet q=s.executeQuery()){q.next();e.setWebDineroReembolsado(q.getBigDecimal(1));}}
        e.setWebSolicitudesPorDia(consultarRango(c,"SELECT DATE_FORMAT(s.fecha_creacion,'%Y-%m-%d') etiqueta,COUNT(*) valor FROM solicitudes_web_seguimiento s WHERE s.fecha_creacion>=? AND s.fecha_creacion<? GROUP BY DATE_FORMAT(s.fecha_creacion,'%Y-%m-%d') ORDER BY etiqueta",d,h,400));
        e.setWebHorarios(consultarRango(c,"SELECT DATE_FORMAT(r.hora_inicio,'%H:%i') etiqueta,COUNT(*) valor FROM solicitudes_web_seguimiento s JOIN reservas r ON r.id=s.reserva_id WHERE s.fecha_creacion>=? AND s.fecha_creacion<? GROUP BY r.hora_inicio ORDER BY valor DESC,r.hora_inicio LIMIT 8",d,h,8));
        e.setWebDias(consultarRango(c,"SELECT ELT(DAYOFWEEK(r.fecha),'Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado') etiqueta,COUNT(*) valor FROM solicitudes_web_seguimiento s JOIN reservas r ON r.id=s.reserva_id WHERE s.fecha_creacion>=? AND s.fecha_creacion<? GROUP BY DAYOFWEEK(r.fecha),etiqueta ORDER BY valor DESC",d,h,7));
        e.setWebMetodosPago(consultarRango(c,"SELECT p.metodo_pago etiqueta,COALESCE(SUM(p.importe),0) valor FROM solicitudes_web_seguimiento s JOIN pagos p ON p.reserva_id=s.reserva_id WHERE s.fecha_creacion>=? AND s.fecha_creacion<? AND p.estado IN ('ACREDITADO','REEMBOLSADO') GROUP BY p.metodo_pago ORDER BY valor DESC",d,h,10));
        e.setWebEstados(consultarRango(c,"SELECT r.estado etiqueta,COUNT(*) valor FROM solicitudes_web_seguimiento s JOIN reservas r ON r.id=s.reserva_id WHERE s.fecha_creacion>=? AND s.fecha_creacion<? GROUP BY r.estado ORDER BY valor DESC",d,h,10));
        List<DatoGrafico> embudo=new ArrayList<>();embudo.add(new DatoGrafico("SOLICITUDES",BigDecimal.valueOf(e.getWebSolicitudesCreadas())));embudo.add(new DatoGrafico("CON_PAGO",BigDecimal.valueOf(e.getWebConPago())));embudo.add(new DatoGrafico("CONFIRMADAS_O_RESUELTAS",BigDecimal.valueOf(e.getWebConfirmadasActuales()+e.getWebCompletadas()+e.getWebAusentes())));embudo.add(new DatoGrafico("COMPLETADAS",BigDecimal.valueOf(e.getWebCompletadas())));e.setWebEmbudo(embudo);
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
    private List<DatoGrafico> consultarRangoReembolso(Connection c,String q,LocalDate d,LocalDate h,int l)throws SQLException{return consultar(c,q,d,h,l,true);}
    private List<DatoGrafico> consultarRangoCliente(Connection c,String q,LocalDate d,LocalDate h,int l)throws SQLException{return consultar(c,q,d,h,l,true);}
    private List<DatoGrafico> consultar(Connection c,String q,LocalDate d,LocalDate h,int l,boolean rg)throws SQLException{List<DatoGrafico> out=new ArrayList<>();try(PreparedStatement s=c.prepareStatement(q)){if(rg)rango(s,d,h);else fechas(s,d,h);try(ResultSet r=s.executeQuery()){while(r.next()&&out.size()<l)out.add(new DatoGrafico(r.getString("etiqueta"),r.getBigDecimal("valor")));}}return out;}
    private void fechas(PreparedStatement s,LocalDate d,LocalDate h)throws SQLException{s.setDate(1,Date.valueOf(d));s.setDate(2,Date.valueOf(h));} private void rango(PreparedStatement s,LocalDate d,LocalDate h)throws SQLException{s.setTimestamp(1,Timestamp.valueOf(d.atStartOfDay()));s.setTimestamp(2,Timestamp.valueOf(h.plusDays(1).atStartOfDay()));}
    private void validarFechas(LocalDate d,LocalDate h){if(d==null||h==null)throw new IllegalArgumentException("Las fechas son obligatorias.");if(h.isBefore(d))throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");}
}
