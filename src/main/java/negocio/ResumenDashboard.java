package negocio;

import java.math.BigDecimal;

public class ResumenDashboard {
    private int reservasHoy;
    private int canchasActivas;
    private BigDecimal ingresosDia = BigDecimal.ZERO;
    private BigDecimal ingresosMes = BigDecimal.ZERO;
    private int pagosPendientes;
    private int reservasPendientesSenia;
    private int reservasProximasAVencer;
    private int turnosPendientesCierre;
    private boolean cajaCerradaHoy;

    public int getReservasHoy() { return reservasHoy; }
    public void setReservasHoy(int valor) { reservasHoy = valor; }
    public int getCanchasActivas() { return canchasActivas; }
    public void setCanchasActivas(int valor) { canchasActivas = valor; }
    public BigDecimal getIngresosDia() { return ingresosDia; }
    public void setIngresosDia(BigDecimal valor) { ingresosDia = valor == null ? BigDecimal.ZERO : valor; }
    public BigDecimal getIngresosMes() { return ingresosMes; }
    public void setIngresosMes(BigDecimal valor) { ingresosMes = valor == null ? BigDecimal.ZERO : valor; }
    public int getPagosPendientes() { return pagosPendientes; }
    public void setPagosPendientes(int valor) { pagosPendientes = valor; }
    public int getReservasPendientesSenia() { return reservasPendientesSenia; }
    public void setReservasPendientesSenia(int valor) { reservasPendientesSenia = valor; }
    public int getReservasProximasAVencer() { return reservasProximasAVencer; }
    public void setReservasProximasAVencer(int valor) { reservasProximasAVencer = valor; }
    public int getTurnosPendientesCierre() { return turnosPendientesCierre; }
    public void setTurnosPendientesCierre(int valor) { turnosPendientesCierre = valor; }
    public boolean isCajaCerradaHoy() { return cajaCerradaHoy; }
    public void setCajaCerradaHoy(boolean valor) { cajaCerradaHoy = valor; }
}
