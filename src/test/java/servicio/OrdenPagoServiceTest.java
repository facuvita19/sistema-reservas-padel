package servicio;
import static org.junit.jupiter.api.Assertions.*;import java.math.BigDecimal;import org.junit.jupiter.api.Test;import negocio.*;
class OrdenPagoServiceTest { private final OrdenPagoService service=new OrdenPagoService(); private OrdenPago orden(){OrdenPago o=new OrdenPago();o.setReservaId(7);o.setImporte(new BigDecimal("6250"));o.setClaveIdempotencia("reserva-7-intento-1");return o;}
 @Test void preparaOrdenManualSinAlterarPagoExistente(){OrdenPago o=service.preparar(orden());assertEquals(new BigDecimal("6250.00"),o.getImporte());assertEquals(ProveedorPago.MANUAL,o.getProveedor());assertEquals(MetodoOrdenPago.TRANSFERENCIA_MANUAL,o.getMetodo());}
 @Test void flujoManualEsExplicito(){OrdenPago o=orden();service.cambiarEstado(o,EstadoOrdenPago.ESPERANDO_VERIFICACION);assertTrue(service.debeEsperarVerificacionManual(o));service.cambiarEstado(o,EstadoOrdenPago.APROBADA);assertNotNull(o.getFechaAprobacion());}
 @Test void flujoOnlineAdmiteProcesamiento(){OrdenPago o=orden();o.setProveedor(ProveedorPago.MERCADO_PAGO);o.setMetodo(MetodoOrdenPago.PAGO_ONLINE);service.cambiarEstado(o,EstadoOrdenPago.ESPERANDO_PAGO);service.cambiarEstado(o,EstadoOrdenPago.PROCESANDO);service.cambiarEstado(o,EstadoOrdenPago.APROBADA);assertEquals(EstadoOrdenPago.APROBADA,o.getEstado());}
 @Test void estadoTerminalNoRetrocede(){OrdenPago o=orden();service.cambiarEstado(o,EstadoOrdenPago.CANCELADA);assertThrows(IllegalStateException.class,()->service.cambiarEstado(o,EstadoOrdenPago.PROCESANDO));}
 @Test void exigeIdempotencia(){OrdenPago o=orden();o.setClaveIdempotencia(" ");assertThrows(IllegalArgumentException.class,()->service.preparar(o));}
}
