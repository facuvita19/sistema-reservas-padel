package dao;
import negocio.EventoPago;
public interface EventoPagoDAO { boolean registrarSiNuevo(EventoPago evento); EventoPago buscar(String proveedor,String eventoExternoId); void marcarProcesado(long id,String detalle); void marcarError(long id,String detalle); }
