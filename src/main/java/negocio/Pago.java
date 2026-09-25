package negocio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Pago {

    private long id;
    private long reservaId;
    private BigDecimal importe = BigDecimal.ZERO;
    private MetodoPago metodoPago;
    private EstadoPago estado = EstadoPago.PENDIENTE;
    private String referencia;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaCreacion;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReservaId() {
        return reservaId;
    }

    public void setReservaId(long reservaId) {
        this.reservaId = reservaId;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Pago)) {
            return false;
        }
        Pago otro = (Pago) objeto;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
