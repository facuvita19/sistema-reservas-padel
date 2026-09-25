package negocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class BloqueoCancha {

    private long id;
    private long canchaId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivo;
    private LocalDateTime fechaCreacion;
    private String nombreCancha;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCanchaId() {
        return canchaId;
    }

    public void setCanchaId(long canchaId) {
        this.canchaId = canchaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreCancha() {
        return nombreCancha;
    }

    public void setNombreCancha(String nombreCancha) {
        this.nombreCancha = nombreCancha;
    }

    public boolean seSuperpone(LocalTime inicio, LocalTime fin) {
        if (horaInicio == null || horaFin == null
                || inicio == null || fin == null) {
            return false;
        }
        return inicio.isBefore(horaFin)
                && fin.isAfter(horaInicio);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof BloqueoCancha)) {
            return false;
        }
        BloqueoCancha otro = (BloqueoCancha) objeto;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
