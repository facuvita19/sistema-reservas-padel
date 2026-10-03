package negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CierreCaja {

    private long id;
    private LocalDate fecha;
    private EstadoCierreCaja estado = EstadoCierreCaja.CERRADA;
    private BigDecimal totalAcreditado = BigDecimal.ZERO;
    private BigDecimal totalEfectivoCalculado = BigDecimal.ZERO;
    private BigDecimal efectivoDeclarado = BigDecimal.ZERO;
    private BigDecimal diferenciaEfectivo = BigDecimal.ZERO;
    private BigDecimal totalReembolsado = BigDecimal.ZERO;
    private int cantidadPagosAcreditados;
    private int pagosPendientes;
    private int reservasCompletadas;
    private int reservasAusentes;
    private int reservasCanceladas;
    private String observaciones;
    private long usuarioCierreId;
    private String nombreUsuarioCierre;
    private LocalDateTime fechaCierre;
    private List<DetalleMedioPago> detalles = new ArrayList<>();

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate valor) { fecha = valor; }
    public EstadoCierreCaja getEstado() { return estado; }
    public void setEstado(EstadoCierreCaja valor) { estado = valor; }
    public BigDecimal getTotalAcreditado() { return totalAcreditado; }
    public void setTotalAcreditado(BigDecimal valor) { totalAcreditado = valor; }
    public BigDecimal getTotalEfectivoCalculado() { return totalEfectivoCalculado; }
    public void setTotalEfectivoCalculado(BigDecimal valor) { totalEfectivoCalculado = valor; }
    public BigDecimal getEfectivoDeclarado() { return efectivoDeclarado; }
    public void setEfectivoDeclarado(BigDecimal valor) { efectivoDeclarado = valor; }
    public BigDecimal getDiferenciaEfectivo() { return diferenciaEfectivo; }
    public void setDiferenciaEfectivo(BigDecimal valor) { diferenciaEfectivo = valor; }
    public BigDecimal getTotalReembolsado() { return totalReembolsado; }
    public void setTotalReembolsado(BigDecimal valor) { totalReembolsado = valor; }
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
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String valor) { observaciones = valor; }
    public long getUsuarioCierreId() { return usuarioCierreId; }
    public void setUsuarioCierreId(long valor) { usuarioCierreId = valor; }
    public String getNombreUsuarioCierre() { return nombreUsuarioCierre; }
    public void setNombreUsuarioCierre(String valor) { nombreUsuarioCierre = valor; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime valor) { fechaCierre = valor; }
    public List<DetalleMedioPago> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleMedioPago> valor) {
        detalles = valor == null ? new ArrayList<>() : new ArrayList<>(valor);
    }
}
