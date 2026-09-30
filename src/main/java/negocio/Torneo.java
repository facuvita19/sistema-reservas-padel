package negocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Torneo {

    private long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime inscripcionDesde;
    private LocalDateTime inscripcionHasta;
    private EstadoTorneo estado = EstadoTorneo.BORRADOR;
    private String reglamento;
    private boolean activo = true;
    private long usuarioCreacionId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public String getNombre() { return nombre; }
    public void setNombre(String valor) { nombre = valor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String valor) { descripcion = valor; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate valor) { fechaInicio = valor; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate valor) { fechaFin = valor; }
    public LocalDateTime getInscripcionDesde() { return inscripcionDesde; }
    public void setInscripcionDesde(LocalDateTime valor) { inscripcionDesde = valor; }
    public LocalDateTime getInscripcionHasta() { return inscripcionHasta; }
    public void setInscripcionHasta(LocalDateTime valor) { inscripcionHasta = valor; }
    public EstadoTorneo getEstado() { return estado; }
    public void setEstado(EstadoTorneo valor) { estado = valor == null ? EstadoTorneo.BORRADOR : valor; }
    public String getReglamento() { return reglamento; }
    public void setReglamento(String valor) { reglamento = valor; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean valor) { activo = valor; }
    public long getUsuarioCreacionId() { return usuarioCreacionId; }
    public void setUsuarioCreacionId(long valor) { usuarioCreacionId = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime valor) { fechaActualizacion = valor; }

    public boolean inscripcionDisponible(LocalDateTime momento) {
        return activo
                && estado.permiteInscripciones()
                && momento != null
                && inscripcionDesde != null
                && inscripcionHasta != null
                && !momento.isBefore(inscripcionDesde)
                && !momento.isAfter(inscripcionHasta);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Torneo otro)) return false;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
