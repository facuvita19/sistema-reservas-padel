package negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MovimientoCaja {
    private long id;
    private LocalDate fecha;
    private TipoMovimientoCaja tipo;
    private String concepto;
    private BigDecimal importe = BigDecimal.ZERO;
    private MetodoPago medioPago;
    private String observaciones;
    private long usuarioId;
    private String nombreUsuario;
    private LocalDateTime fechaCreacion;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate valor) { fecha = valor; }
    public TipoMovimientoCaja getTipo() { return tipo; }
    public void setTipo(TipoMovimientoCaja valor) { tipo = valor; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String valor) { concepto = valor; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal valor) { importe = valor == null ? BigDecimal.ZERO : valor; }
    public MetodoPago getMedioPago() { return medioPago; }
    public void setMedioPago(MetodoPago valor) { medioPago = valor; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String valor) { observaciones = valor; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long valor) { usuarioId = valor; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String valor) { nombreUsuario = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }
}
