package negocio;

import java.math.BigDecimal;

public class DetalleMedioPago {

    private MetodoPago metodoPago;
    private int cantidadMovimientos;
    private BigDecimal total = BigDecimal.ZERO;

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago valor) { metodoPago = valor; }
    public int getCantidadMovimientos() { return cantidadMovimientos; }
    public void setCantidadMovimientos(int valor) { cantidadMovimientos = valor; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal valor) {
        total = valor == null ? BigDecimal.ZERO : valor;
    }
}
