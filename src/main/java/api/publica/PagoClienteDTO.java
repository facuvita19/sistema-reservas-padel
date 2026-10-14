package api.publica;
import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.List;
public final class PagoClienteDTO {private PagoClienteDTO(){}
 public record CapacidadesPago(String moneda,boolean pagoOnlineDisponible,boolean pagoTransferenciaDisponible,String proveedorOnline,String entornoOnline,String confirmacionTransferencia,boolean permiteReintento,boolean requiereSenia){}
 public record OrdenPagoPublica(long ordenId,long reservaId,String proveedor,String metodo,String estado,BigDecimal importe,String moneda,LocalDateTime fechaExpiracion,long segundosRestantes,boolean puedePagarOnline,boolean puedeInformarTransferencia,boolean puedeReintentar,boolean requiereActualizacion,String checkoutUrl,String mensaje){}
 public record CrearOrdenPago(String metodo){}
 public record HistorialOrdenes(long reservaId,List<OrdenPagoPublica> ordenes){}
}
