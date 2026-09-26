package negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResumenCajaDiaria {

    private LocalDate fecha;
    private BigDecimal totalAcreditado = BigDecimal.ZERO;
    private BigDecimal totalEfectivo = BigDecimal.ZERO;
    private BigDecimal totalReembolsado = BigDecimal.ZERO;
    private int cantidadPagosAcreditados;
    private int pagosPendientes;
    private int reservasCompletadas;
    private int reservasAusentes;
    private int reservasCanceladas;
    private List<DetalleMedioPago> detalles = new ArrayList<>();

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate valor) { fecha = valor; }
    public BigDecimal getTotalAcreditado() { return totalAcreditado; }
    public void setTotalAcreditado(BigDecimal valor) {
        totalAcreditado = valor == null ? BigDecimal.ZERO : valor;
    }
    public BigDecimal getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(BigDecimal valor) {
        totalEfectivo = valor == null ? BigDecimal.ZERO : valor;
    }
    public BigDecimal getTotalReembolsado() { return totalReembolsado; }
    public void setTotalReembolsado(BigDecimal valor) {
        totalReembolsado = valor == null ? BigDecimal.ZERO : valor;
    }
    public int getCantidadPagosAcreditados() { return cantidadPagosAcreditados; }
    public void setCantidadPagosAcreditados(int valor) { cantidadPagosAcreditados = valor; }
    public int getPagosPendientes() { return pagosPendientes; }
    public void setPagosPendientes(int valor) { pagosPendientes = valor; }
    public int getReservasCompletadas() { return reservasCompletadas; }
    public void setReservasCompletadas(int valor) { reservasCompletadas = valor; }
    public int getReservasAusentes() { return reservasAusentes; }
    public void setReservasAusentes(int valor) { reservasAusentes = valor; }
    public int getReservasCanceladas() { return reservasCanceladas; }
    public void setReservasCanceladas(int valor) { reservasCanceladas = valor; }
    public List<DetalleMedioPago> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleMedioPago> valor) {
        detalles = valor == null ? new ArrayList<>() : new ArrayList<>(valor);
    }
}
