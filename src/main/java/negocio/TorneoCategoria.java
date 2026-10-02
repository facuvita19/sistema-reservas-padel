package negocio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class TorneoCategoria {
    private long id;
    private long torneoId;
    private String nombre;
    private RamaTorneo rama;
    private int cupoParejas;
    private BigDecimal precioInscripcion = BigDecimal.ZERO;
    private BigDecimal premioCampeon;
    private BigDecimal premioSubcampeon;
    private String premioDescripcion;
    private FormatoCompetenciaTorneo formatoCompetencia =
            FormatoCompetenciaTorneo.ELIMINACION_DIRECTA;
    private int cantidadGruposTres;
    private int cantidadGruposCuatro;
    private boolean activo = true;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String nombreTorneo;
    private int parejasConfirmadas;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getTorneoId() { return torneoId; }
    public void setTorneoId(long valor) { torneoId = valor; }
    public String getNombre() { return nombre; }
    public void setNombre(String valor) { nombre = valor; }
    public RamaTorneo getRama() { return rama; }
    public void setRama(RamaTorneo valor) { rama = valor; }
    public int getCupoParejas() { return cupoParejas; }
    public void setCupoParejas(int valor) { cupoParejas = valor; }
    public BigDecimal getPrecioInscripcion() { return precioInscripcion; }
    public void setPrecioInscripcion(BigDecimal valor) {
        precioInscripcion = valor == null ? BigDecimal.ZERO : valor;
    }
    public BigDecimal getPremioCampeon() { return premioCampeon; }
    public void setPremioCampeon(BigDecimal valor) { premioCampeon = valor; }
    public BigDecimal getPremioSubcampeon() { return premioSubcampeon; }
    public void setPremioSubcampeon(BigDecimal valor) { premioSubcampeon = valor; }
    public String getPremioDescripcion() { return premioDescripcion; }
    public void setPremioDescripcion(String valor) {
        premioDescripcion = valor == null || valor.isBlank()
                ? null : valor.trim();
    }
    public FormatoCompetenciaTorneo getFormatoCompetencia() {
        return formatoCompetencia;
    }
    public void setFormatoCompetencia(FormatoCompetenciaTorneo valor) {
        formatoCompetencia = valor == null
                ? FormatoCompetenciaTorneo.ELIMINACION_DIRECTA : valor;
    }
    public int getCantidadGruposTres() { return cantidadGruposTres; }
    public void setCantidadGruposTres(int valor) {
        cantidadGruposTres = Math.max(0, valor);
    }
    public int getCantidadGruposCuatro() { return cantidadGruposCuatro; }
    public void setCantidadGruposCuatro(int valor) {
        cantidadGruposCuatro = Math.max(0, valor);
    }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean valor) { activo = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime valor) { fechaActualizacion = valor; }
    public String getNombreTorneo() { return nombreTorneo; }
    public void setNombreTorneo(String valor) { nombreTorneo = valor; }
    public int getParejasConfirmadas() { return parejasConfirmadas; }
    public void setParejasConfirmadas(int valor) { parejasConfirmadas = Math.max(0, valor); }

    public boolean usaFaseGrupos() {
        return formatoCompetencia == FormatoCompetenciaTorneo.GRUPOS_ELIMINACION;
    }

    public int getCantidadGrupos() {
        return cantidadGruposTres + cantidadGruposCuatro;
    }

    public int getCapacidadGrupos() {
        return cantidadGruposTres * 3 + cantidadGruposCuatro * 4;
    }

    public int getClasificadosProyectados() {
        return cantidadGruposTres * 2 + cantidadGruposCuatro * 3;
    }

    public int getCuposDisponibles() {
        return Math.max(0, cupoParejas - parejasConfirmadas);
    }

    public boolean tieneCupoDisponible() {
        return activo && getCuposDisponibles() > 0;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof TorneoCategoria otra)) return false;
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
