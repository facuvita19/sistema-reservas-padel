package servicio;

import config.ConexionBD;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Clock;
import java.time.LocalDateTime;
import negocio.EstadoOrdenPago;
import negocio.EstadoPago;
import negocio.EventoPago;
import negocio.MetodoOrdenPago;
import negocio.MetodoPago;
import negocio.OrigenAcreditacion;
import negocio.ProveedorPago;

/** Acredita una orden, crea el pago, confirma la reserva y procesa el evento en una sola transacción. */
public class AcreditacionOrdenPagoTransaccionalService {
    @FunctionalInterface
    public interface ConexionFactory { Connection abrir(); }

    private final ConexionFactory conexiones;
    private final Clock clock;

    public AcreditacionOrdenPagoTransaccionalService() {
        this(ConexionBD::obtenerConexion, Clock.systemDefaultZone());
    }

    public AcreditacionOrdenPagoTransaccionalService(ConexionFactory conexiones, Clock clock) {
        if (conexiones == null || clock == null) throw new IllegalArgumentException("Las dependencias son obligatorias.");
        this.conexiones = conexiones;
        this.clock = clock;
    }

    public boolean procesarAprobacion(EventoPago evento, String referenciaPago) {
        validar(evento, referenciaPago);
        try (Connection conexion = conexiones.abrir()) {
            boolean autoCommit = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            try {
                Long eventoId = registrarEvento(conexion, evento);
                if (eventoId == null) { conexion.rollback(); return false; }
                evento.setId(eventoId);
                OrdenBloqueada orden = bloquearOrden(conexion, evento.getOrdenPagoId());
                ReservaBloqueada reserva = bloquearReserva(conexion, orden.reservaId());
                if ("CANCELADA".equals(reserva.estado()) || "EXPIRADA".equals(reserva.estado()))
                    throw new IllegalStateException("La reserva ya no admite acreditaciones.");
                if (!"APROBADA".equals(orden.estado())) aprobarOrden(conexion, orden, referenciaPago);
                insertarPagoSiFalta(conexion, orden, evento, referenciaPago);
                confirmarReserva(conexion, reserva.id());
                marcarEventoProcesado(conexion, eventoId,
                        "APROBADA".equals(orden.estado()) ? "Acreditación reparada o ya registrada." : "Acreditación registrada.");
                conexion.commit();
                return true;
            } catch (Exception ex) {
                try { conexion.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                if (ex instanceof RuntimeException runtime) throw runtime;
                throw new RuntimeException("No se pudo acreditar la orden de pago.", ex);
            } finally {
                try { conexion.setAutoCommit(autoCommit); } catch (SQLException ignored) { }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo completar la transacción de pago.", ex);
        }
    }

    private void validar(EventoPago e, String referencia) {
        if (e == null || !e.isFirmaValida()) throw new IllegalArgumentException("El evento debe tener firma válida.");
        if (e.getProveedor() == null || e.getEventoExternoId() == null || e.getEventoExternoId().isBlank())
            throw new IllegalArgumentException("El proveedor y el ID externo del evento son obligatorios.");
        if (e.getOrdenPagoId() <= 0) throw new IllegalArgumentException("La orden del evento es obligatoria.");
        if (referencia == null || referencia.isBlank()) throw new IllegalArgumentException("La referencia del pago es obligatoria.");
    }

    private Long registrarEvento(Connection c, EventoPago e) throws SQLException {
        String sql = "INSERT IGNORE INTO eventos_pago(orden_pago_id,proveedor,evento_externo_id,tipo,estado_procesamiento,firma_valida,payload_hash,payload_json,fecha_evento) VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement s=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setLong(1,e.getOrdenPagoId()); s.setString(2,e.getProveedor().name()); s.setString(3,e.getEventoExternoId());
            s.setString(4,e.getTipo()==null?"payment.updated":e.getTipo()); s.setString(5,"VALIDADO"); s.setBoolean(6,true);
            s.setString(7,e.getPayloadHash()==null?"0".repeat(64):e.getPayloadHash());
            if(e.getPayloadJson()==null)s.setNull(8,Types.VARCHAR);else s.setString(8,e.getPayloadJson());
            if(e.getFechaEvento()==null)s.setNull(9,Types.TIMESTAMP);else s.setTimestamp(9,Timestamp.valueOf(e.getFechaEvento()));
            if(s.executeUpdate()!=1)return null;
            try(ResultSet r=s.getGeneratedKeys()){if(r.next())return r.getLong(1);}
        }
        throw new SQLException("No se obtuvo el ID del evento de pago.");
    }

    private OrdenBloqueada bloquearOrden(Connection c,long id)throws SQLException{
        try(PreparedStatement s=c.prepareStatement("SELECT id,reserva_id,proveedor,metodo,estado,importe,moneda,version FROM ordenes_pago WHERE id=? FOR UPDATE")){
            s.setLong(1,id);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalArgumentException("La orden no existe.");
                return new OrdenBloqueada(r.getLong("id"),r.getLong("reserva_id"),r.getString("proveedor"),r.getString("metodo"),r.getString("estado"),r.getBigDecimal("importe"),r.getString("moneda"),r.getLong("version"));}}
    }

    private ReservaBloqueada bloquearReserva(Connection c,long id)throws SQLException{
        try(PreparedStatement s=c.prepareStatement("SELECT id,estado FROM reservas WHERE id=? FOR UPDATE")){
            s.setLong(1,id);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalArgumentException("La reserva no existe.");return new ReservaBloqueada(r.getLong("id"),r.getString("estado"));}}
    }

    private void aprobarOrden(Connection c,OrdenBloqueada o,String referencia)throws SQLException{
        EstadoOrdenPago actual=EstadoOrdenPago.valueOf(o.estado());
        if(!actual.puedeCambiarA(EstadoOrdenPago.APROBADA))throw new IllegalStateException("La orden no admite aprobación desde "+actual);
        String sql="UPDATE ordenes_pago SET estado='APROBADA',pago_externo_id=COALESCE(pago_externo_id,?),fecha_aprobacion=COALESCE(fecha_aprobacion,?),version=version+1 WHERE id=? AND version=?";
        try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,referencia);s.setTimestamp(2,Timestamp.valueOf(LocalDateTime.now(clock)));s.setLong(3,o.id());s.setLong(4,o.version());if(s.executeUpdate()!=1)throw new IllegalStateException("La orden cambió durante el procesamiento.");}
    }

    private void insertarPagoSiFalta(Connection c,OrdenBloqueada o,EventoPago e,String referencia)throws SQLException{
        String sql="INSERT IGNORE INTO pagos(reserva_id,orden_pago_id,importe,metodo_pago,origen_acreditacion,estado,referencia,referencia_externa,fecha_pago) VALUES(?,?,?,?,?,'ACREDITADO',?,?,?)";
        try(PreparedStatement s=c.prepareStatement(sql)){s.setLong(1,o.reservaId());s.setLong(2,o.id());s.setBigDecimal(3,o.importe());s.setString(4,MetodoOrdenPago.TRANSFERENCIA_MANUAL.name().equals(o.metodo())?MetodoPago.TRANSFERENCIA.name():MetodoPago.TARJETA.name());s.setString(5,e.getProveedor()==ProveedorPago.MANUAL?OrigenAcreditacion.ADMINISTRACION.name():OrigenAcreditacion.WEBHOOK.name());s.setString(6,referencia);s.setString(7,referencia);s.setTimestamp(8,Timestamp.valueOf(LocalDateTime.now(clock)));s.executeUpdate();}
        try(PreparedStatement s=c.prepareStatement("SELECT importe,estado FROM pagos WHERE orden_pago_id=? FOR UPDATE")){s.setLong(1,o.id());try(ResultSet r=s.executeQuery()){if(!r.next())throw new SQLException("No se registró el pago de la orden.");if(!"ACREDITADO".equals(r.getString("estado"))||r.getBigDecimal("importe").compareTo(o.importe())!=0)throw new IllegalStateException("El pago existente no coincide con la orden.");}}
    }

    private void confirmarReserva(Connection c,long id)throws SQLException{
        try(PreparedStatement s=c.prepareStatement("UPDATE reservas SET estado='CONFIRMADA',fecha_vencimiento=NULL,fecha_expiracion=NULL WHERE id=? AND estado='PENDIENTE'")){s.setLong(1,id);s.executeUpdate();}
        try(PreparedStatement s=c.prepareStatement("SELECT estado FROM reservas WHERE id=?")){s.setLong(1,id);try(ResultSet r=s.executeQuery()){if(!r.next()||!"CONFIRMADA".equals(r.getString(1)))throw new IllegalStateException("La reserva no pudo confirmarse.");}}
    }

    private void marcarEventoProcesado(Connection c,long id,String detalle)throws SQLException{
        try(PreparedStatement s=c.prepareStatement("UPDATE eventos_pago SET estado_procesamiento='PROCESADO',detalle_resultado=?,fecha_procesamiento=? WHERE id=?")){s.setString(1,detalle);s.setTimestamp(2,Timestamp.valueOf(LocalDateTime.now(clock)));s.setLong(3,id);if(s.executeUpdate()!=1)throw new SQLException("No se pudo marcar el evento como procesado.");}
    }

    private record OrdenBloqueada(long id,long reservaId,String proveedor,String metodo,String estado,BigDecimal importe,String moneda,long version){}
    private record ReservaBloqueada(long id,String estado){}
}
