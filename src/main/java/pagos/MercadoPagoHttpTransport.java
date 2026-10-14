package pagos;
import java.net.URI;import java.util.Map;
public interface MercadoPagoHttpTransport { Respuesta ejecutar(String metodo,URI uri,Map<String,String> cabeceras,String cuerpo);record Respuesta(int estado,String cuerpo){public boolean exitosa(){return estado>=200&&estado<300;}}}
