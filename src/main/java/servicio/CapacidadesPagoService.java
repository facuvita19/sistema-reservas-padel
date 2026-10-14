package servicio;
import api.publica.PagoClienteDTO.CapacidadesPago;import config.ConfiguracionMercadoPago;
public class CapacidadesPagoService {private final ConfiguracionMercadoPago mercadoPago;public CapacidadesPagoService(){this(ConfiguracionMercadoPago.cargar());}public CapacidadesPagoService(ConfiguracionMercadoPago c){mercadoPago=c;}public CapacidadesPago obtener(){boolean online=mercadoPago!=null&&mercadoPago.disponible();return new CapacidadesPago("ARS",online,true,online?"MERCADO_PAGO":null,"MANUAL",true,true);}}
