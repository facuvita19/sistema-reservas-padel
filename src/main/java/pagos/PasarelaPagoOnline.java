package pagos;
public interface PasarelaPagoOnline { boolean disponible(); CheckoutCreado crearCheckout(SolicitudCheckout solicitud); PagoExterno consultarPago(String pagoExternoId); default java.util.List<PagoExterno> buscarPagosPorReferencia(String referencia){return java.util.List.of();} }
