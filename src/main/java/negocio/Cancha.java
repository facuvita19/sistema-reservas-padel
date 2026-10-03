package negocio;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class Cancha {

    private long id;
    private String nombre;
    private String descripcion;
    private TipoCancha tipo;
    private String superficie;
    private boolean tieneIluminacion = true;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private int duracionReserva = 90;
    private BigDecimal precio = BigDecimal.ZERO;
    private boolean activo = true;
    private int ordenVisual;
    private LocalDateTime fechaCreacion;
    private Set<DayOfWeek> diasDisponibles =
            EnumSet.noneOf(DayOfWeek.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoCancha getTipo() {
        return tipo;
    }

    public void setTipo(TipoCancha tipo) {
        this.tipo = tipo;
    }

    public String getSuperficie() {
        return superficie;
    }

    public void setSuperficie(String superficie) {
        this.superficie = superficie;
    }

    public boolean isTieneIluminacion() {
        return tieneIluminacion;
    }

    public void setTieneIluminacion(boolean tieneIluminacion) {
        this.tieneIluminacion = tieneIluminacion;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public int getDuracionReserva() {
        return duracionReserva;
    }

    public void setDuracionReserva(int duracionReserva) {
        this.duracionReserva = duracionReserva;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getOrdenVisual() {
        return ordenVisual;
    }

    public void setOrdenVisual(int ordenVisual) {
        this.ordenVisual = ordenVisual;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Set<DayOfWeek> getDiasDisponibles() {
        return Collections.unmodifiableSet(diasDisponibles);
    }

    public void setDiasDisponibles(Set<DayOfWeek> diasDisponibles) {
        this.diasDisponibles = diasDisponibles == null
                || diasDisponibles.isEmpty()
                ? EnumSet.noneOf(DayOfWeek.class)
                : EnumSet.copyOf(diasDisponibles);
    }

    public boolean estaDisponibleElDia(DayOfWeek dia) {
        return dia != null && diasDisponibles.contains(dia);
    }

    public boolean contieneHorario(LocalTime inicio, LocalTime fin) {
        if (horaApertura == null || horaCierre == null
                || inicio == null || fin == null) {
            return false;
        }
        return !inicio.isBefore(horaApertura)
                && !fin.isAfter(horaCierre)
                && fin.isAfter(inicio);
    }

    @Override
    public String toString() {
        return nombre == null ? "" : nombre;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Cancha)) {
            return false;
        }
        Cancha otra = (Cancha) objeto;
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
