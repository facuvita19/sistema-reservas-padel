package pagos;
import java.math.BigDecimal;import java.time.LocalDateTime;
public record PagoExterno(String pagoId,String referencia,String estado,String detalleEstado,BigDecimal importe,String moneda,LocalDateTime fechaAprobacion){public boolean aprobado(){return "approved".equalsIgnoreCase(estado);}}
