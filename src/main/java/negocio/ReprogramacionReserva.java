package negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReprogramacionReserva {

    private long id;
    private long reservaId;
    private long canchaAnteriorId;
    private LocalDate fechaAnterior;
    private LocalTime horaInicioAnterior;
    private LocalTime horaFinAnterior;
    private long canchaNuevaId;
    private LocalDate fechaNueva;
    private LocalTime horaInicioNueva;
    private LocalTime horaFinNueva;
    private BigDecimal precioAnterior;
    private BigDecimal precioNuevo;
    private String motivo;
    private long usuarioId;
    private LocalDateTime fechaReprogramacion;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getReservaId() { return reservaId; }
    public void setReservaId(long valor) { reservaId = valor; }
    public long getCanchaAnteriorId() { return canchaAnteriorId; }
    public void setCanchaAnteriorId(long valor) { canchaAnteriorId = valor; }
    public LocalDate getFechaAnterior() { return fechaAnterior; }
    public void setFechaAnterior(LocalDate valor) { fechaAnterior = valor; }
    public LocalTime getHoraInicioAnterior() { return horaInicioAnterior; }
    public void setHoraInicioAnterior(LocalTime valor) { horaInicioAnterior = valor; }
    public LocalTime getHoraFinAnterior() { return horaFinAnterior; }
    public void setHoraFinAnterior(LocalTime valor) { horaFinAnterior = valor; }
    public long getCanchaNuevaId() { return canchaNuevaId; }
    public void setCanchaNuevaId(long valor) { canchaNuevaId = valor; }
    public LocalDate getFechaNueva() { return fechaNueva; }
    public void setFechaNueva(LocalDate valor) { fechaNueva = valor; }
    public LocalTime getHoraInicioNueva() { return horaInicioNueva; }
    public void setHoraInicioNueva(LocalTime valor) { horaInicioNueva = valor; }
    public LocalTime getHoraFinNueva() { return horaFinNueva; }
    public void setHoraFinNueva(LocalTime valor) { horaFinNueva = valor; }
    public BigDecimal getPrecioAnterior() { return precioAnterior; }
    public void setPrecioAnterior(BigDecimal valor) { precioAnterior = valor; }
    public BigDecimal getPrecioNuevo() { return precioNuevo; }
    public void setPrecioNuevo(BigDecimal valor) { precioNuevo = valor; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String valor) { motivo = valor; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long valor) { usuarioId = valor; }
    public LocalDateTime getFechaReprogramacion() { return fechaReprogramacion; }
    public void setFechaReprogramacion(LocalDateTime valor) { fechaReprogramacion = valor; }
}
