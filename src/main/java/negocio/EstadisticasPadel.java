package negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EstadisticasPadel {

    private int totalReservas;
    private int reservasCompletadas;
    private int reservasCanceladas;
    private int reservasAusentes;
    private int cantidadPagosAcreditados;
    private BigDecimal ingresosAcreditados = BigDecimal.ZERO;
    private BigDecimal totalReembolsado = BigDecimal.ZERO;
    private BigDecimal ticketPromedio = BigDecimal.ZERO;
    private List<DatoGrafico> reservasPorEstado = new ArrayList<>();
    private List<DatoGrafico> reservasPorCancha = new ArrayList<>();
    private List<DatoGrafico> ingresosPorMes = new ArrayList<>();
    private List<DatoGrafico> ingresosPorMetodo = new ArrayList<>();
    private List<DatoGrafico> horariosMasSolicitados = new ArrayList<>();
    private List<CierreCaja> cierresCaja = new ArrayList<>();

    public int getTotalReservas() { return totalReservas; }
    public void setTotalReservas(int valor) { totalReservas = valor; }
    public int getReservasCompletadas() { return reservasCompletadas; }
    public void setReservasCompletadas(int valor) { reservasCompletadas = valor; }
    public int getReservasCanceladas() { return reservasCanceladas; }
    public void setReservasCanceladas(int valor) { reservasCanceladas = valor; }
    public int getReservasAusentes() { return reservasAusentes; }
    public void setReservasAusentes(int valor) { reservasAusentes = valor; }
    public int getCantidadPagosAcreditados() { return cantidadPagosAcreditados; }
    public void setCantidadPagosAcreditados(int valor) { cantidadPagosAcreditados = valor; }
    public BigDecimal getIngresosAcreditados() { return ingresosAcreditados; }
    public void setIngresosAcreditados(BigDecimal valor) {
        ingresosAcreditados = valor == null ? BigDecimal.ZERO : valor;
    }
    public BigDecimal getTotalReembolsado() { return totalReembolsado; }
    public void setTotalReembolsado(BigDecimal valor) {
        totalReembolsado = valor == null ? BigDecimal.ZERO : valor;
    }
    public BigDecimal getTicketPromedio() { return ticketPromedio; }
    public void setTicketPromedio(BigDecimal valor) {
        ticketPromedio = valor == null ? BigDecimal.ZERO : valor;
    }
    public List<DatoGrafico> getReservasPorEstado() {
        return Collections.unmodifiableList(reservasPorEstado);
    }
    public void setReservasPorEstado(List<DatoGrafico> valores) {
        reservasPorEstado = copiar(valores);
    }
    public List<DatoGrafico> getReservasPorCancha() {
        return Collections.unmodifiableList(reservasPorCancha);
    }
    public void setReservasPorCancha(List<DatoGrafico> valores) {
        reservasPorCancha = copiar(valores);
    }
    public List<DatoGrafico> getIngresosPorMes() {
        return Collections.unmodifiableList(ingresosPorMes);
    }
    public void setIngresosPorMes(List<DatoGrafico> valores) {
        ingresosPorMes = copiar(valores);
    }
    public List<DatoGrafico> getIngresosPorMetodo() {
        return Collections.unmodifiableList(ingresosPorMetodo);
    }
    public void setIngresosPorMetodo(List<DatoGrafico> valores) {
        ingresosPorMetodo = copiar(valores);
    }
    public List<DatoGrafico> getHorariosMasSolicitados() {
        return Collections.unmodifiableList(horariosMasSolicitados);
    }
    public void setHorariosMasSolicitados(List<DatoGrafico> valores) {
        horariosMasSolicitados = copiar(valores);
    }
    public List<CierreCaja> getCierresCaja() {
        return Collections.unmodifiableList(cierresCaja);
    }
    public void setCierresCaja(List<CierreCaja> valores) {
        cierresCaja = valores == null ? new ArrayList<>() : new ArrayList<>(valores);
    }

    public BigDecimal getTasaCancelacion() {
        return porcentaje(reservasCanceladas);
    }

    public BigDecimal getTasaAusencia() {
        return porcentaje(reservasAusentes);
    }

    private BigDecimal porcentaje(int cantidad) {
        if (totalReservas == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(cantidad)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalReservas), 2,
                        java.math.RoundingMode.HALF_UP);
    }

    private List<DatoGrafico> copiar(List<DatoGrafico> valores) {
        return valores == null ? new ArrayList<>() : new ArrayList<>(valores);
    }

    public record DatoGrafico(String etiqueta, BigDecimal valor) {
        public DatoGrafico {
            if (etiqueta == null || etiqueta.isBlank()) {
                throw new IllegalArgumentException("La etiqueta es obligatoria.");
            }
            valor = valor == null ? BigDecimal.ZERO : valor;
        }
    }
}
