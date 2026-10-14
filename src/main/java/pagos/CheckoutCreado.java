package pagos;
import java.net.URI;import java.time.LocalDateTime;
public record CheckoutCreado(String preferenciaId,URI checkoutUrl,LocalDateTime vencimiento,String referencia){public CheckoutCreado{if(preferenciaId==null||preferenciaId.isBlank())throw new IllegalArgumentException("La preferencia es obligatoria.");if(checkoutUrl==null||!"https".equalsIgnoreCase(checkoutUrl.getScheme()))throw new IllegalArgumentException("El checkout debe usar HTTPS.");}}
