package pagos;
import config.ConfiguracionMercadoPago;
public final class PasarelaPagoFactory {private PasarelaPagoFactory(){}public static PasarelaPagoOnline crear(ConfiguracionMercadoPago c){if(c==null||!c.disponible())return new PasarelaPagoNoDisponible("Mercado Pago no está configurado.");return new MercadoPagoPasarela(c);}}
