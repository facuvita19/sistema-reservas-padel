package negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DetallePagoCaja {
    private long pagoId;
    private long reservaId;
    private String nombreCliente;
    private String nombreCancha;
    private LocalDate fechaTurno;
    private LocalTime horaTurno;
    private LocalDateTime fechaAcreditacion;
    private MetodoPago metodoPago;
    private BigDecimal importe = BigDecimal.ZERO;
    private String referencia;
    private String nombreUsuario;

    public long getPagoId() { return pagoId; }
    public void setPagoId(long valor) { pagoId = valor; }
    public long getReservaId() { return reservaId; }
    public void setReservaId(long valor) { reservaId = valor; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String valor) { nombreCliente = valor; }
    public String getNombreCancha() { return nombreCancha; }
    public void setNombreCancha(String valor) { nombreCancha = valor; }
    public LocalDate getFechaTurno() { return fechaTurno; }
    public void setFechaTurno(LocalDate valor) { fechaTurno = valor; }
    public LocalTime getHoraTurno() { return horaTurno; }
    public void setHoraTurno(LocalTime valor) { horaTurno = valor; }
    public LocalDateTime getFechaAcreditacion() { return fechaAcreditacion; }
    public void setFechaAcreditacion(LocalDateTime valor) { fechaAcreditacion = valor; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago valor) { metodoPago = valor; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal valor) { importe = valor == null ? BigDecimal.ZERO : valor; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String valor) { referencia = valor; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String valor) { nombreUsuario = valor; }
}
