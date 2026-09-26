package negocio;

import java.math.BigDecimal;

public class ResumenDashboard {

    private int reservasHoy;
    private int canchasActivas;
    private BigDecimal ingresosDia = BigDecimal.ZERO;
    private BigDecimal ingresosMes = BigDecimal.ZERO;
    private int pagosPendientes;

    public int getReservasHoy() {
        return reservasHoy;
    }

    public void setReservasHoy(int reservasHoy) {
        this.reservasHoy = reservasHoy;
    }

    public int getCanchasActivas() {
        return canchasActivas;
    }

    public void setCanchasActivas(int canchasActivas) {
        this.canchasActivas = canchasActivas;
    }

    public BigDecimal getIngresosDia() {
        return ingresosDia;
    }

    public void setIngresosDia(BigDecimal ingresosDia) {
        this.ingresosDia = ingresosDia == null
                ? BigDecimal.ZERO
                : ingresosDia;
    }

    public BigDecimal getIngresosMes() {
        return ingresosMes;
    }

    public void setIngresosMes(BigDecimal ingresosMes) {
        this.ingresosMes = ingresosMes == null
                ? BigDecimal.ZERO
                : ingresosMes;
    }

    public int getPagosPendientes() {
        return pagosPendientes;
    }

    public void setPagosPendientes(int pagosPendientes) {
        this.pagosPendientes = pagosPendientes;
    }
}
