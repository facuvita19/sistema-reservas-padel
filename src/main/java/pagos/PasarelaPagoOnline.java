package pagos;
public interface PasarelaPagoOnline { boolean disponible(); CheckoutCreado crearCheckout(SolicitudCheckout solicitud); PagoExterno consultarPago(String pagoExternoId); }
