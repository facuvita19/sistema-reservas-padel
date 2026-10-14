package pagos;
import static org.junit.jupiter.api.Assertions.*;import java.util.Map;import org.junit.jupiter.api.Test;import config.ConfiguracionMercadoPago;
class PasarelaPagoFactoryTest {
 @Test void sinConfiguracionDevuelvePasarelaCerrada(){var p=PasarelaPagoFactory.crear(ConfiguracionMercadoPago.desde(Map.of("MP_ENABLED","false")));assertFalse(p.disponible());assertThrows(IllegalStateException.class,()->p.consultarPago("1"));}
 @Test void configuradaCreaPasarelaReal(){var c=ConfiguracionMercadoPago.desde(Map.of("MP_ENABLED","true","MP_ACCESS_TOKEN","x","MP_WEBHOOK_SECRET","y","MP_PUBLIC_BASE_URL","https://club.example"));var p=PasarelaPagoFactory.crear(c);assertTrue(p.disponible());assertInstanceOf(MercadoPagoPasarela.class,p);}
}
