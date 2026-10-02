package negocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TorneoPartido {

    private long id;
    private long torneoCategoriaId;
    private Long grupoId;
    private FaseTorneo fase;
    private TipoPartidoGrupo tipoPartidoGrupo;
    private int ordenFase;
    private Long pareja1InscripcionId;
    private Long pareja2InscripcionId;
    private EstadoPartidoTorneo estado = EstadoPartidoTorneo.PENDIENTE;
    private boolean bye;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Long canchaId;
    private Long ganadoraInscripcionId;
    private Long partidoSiguienteId;
    private PosicionPartidoSiguiente posicionSiguiente;
    private Long usuarioResultadoId;
    private LocalDateTime fechaFinalizacion;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private final List<TorneoPartidoSet> sets = new ArrayList<>();

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getTorneoCategoriaId() { return torneoCategoriaId; }
    public void setTorneoCategoriaId(long valor) { torneoCategoriaId = valor; }
    public Long getGrupoId() { return grupoId; }
    public void setGrupoId(Long valor) { grupoId = valor; }
    public FaseTorneo getFase() { return fase; }
    public void setFase(FaseTorneo valor) { fase = valor; }
    public TipoPartidoGrupo getTipoPartidoGrupo() { return tipoPartidoGrupo; }
    public void setTipoPartidoGrupo(TipoPartidoGrupo valor) {
        tipoPartidoGrupo = valor;
    }
    public int getOrdenFase() { return ordenFase; }
    public void setOrdenFase(int valor) { ordenFase = valor; }
    public Long getPareja1InscripcionId() { return pareja1InscripcionId; }
    public void setPareja1InscripcionId(Long valor) {
        pareja1InscripcionId = valor;
    }
    public Long getPareja2InscripcionId() { return pareja2InscripcionId; }
    public void setPareja2InscripcionId(Long valor) {
        pareja2InscripcionId = valor;
    }
    public EstadoPartidoTorneo getEstado() { return estado; }
    public void setEstado(EstadoPartidoTorneo valor) {
        estado = valor == null ? EstadoPartidoTorneo.PENDIENTE : valor;
    }
    public boolean isBye() { return bye; }
    public void setBye(boolean valor) { bye = valor; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate valor) { fecha = valor; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime valor) { horaInicio = valor; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime valor) { horaFin = valor; }
    public Long getCanchaId() { return canchaId; }
    public void setCanchaId(Long valor) { canchaId = valor; }
    public Long getGanadoraInscripcionId() { return ganadoraInscripcionId; }
    public void setGanadoraInscripcionId(Long valor) {
        ganadoraInscripcionId = valor;
    }
    public Long getPartidoSiguienteId() { return partidoSiguienteId; }
    public void setPartidoSiguienteId(Long valor) {
        partidoSiguienteId = valor;
    }
    public PosicionPartidoSiguiente getPosicionSiguiente() {
        return posicionSiguiente;
    }
    public void setPosicionSiguiente(PosicionPartidoSiguiente valor) {
        posicionSiguiente = valor;
    }
    public Long getUsuarioResultadoId() { return usuarioResultadoId; }
    public void setUsuarioResultadoId(Long valor) {
        usuarioResultadoId = valor;
    }
    public LocalDateTime getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(LocalDateTime valor) {
        fechaFinalizacion = valor;
    }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String valor) { observaciones = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime valor) {
        fechaActualizacion = valor;
    }
    public List<TorneoPartidoSet> getSets() {
        return sets.stream()
                .sorted(Comparator.comparingInt(TorneoPartidoSet::getNumeroSet))
                .toList();
    }
    public void setSets(List<TorneoPartidoSet> valores) {
        sets.clear();
        if (valores != null) sets.addAll(valores);
    }
    public void agregarSet(TorneoPartidoSet set) {
        if (set != null) sets.add(set);
    }

    public boolean estaProgramado() {
        return fecha != null && horaInicio != null
                && horaFin != null && canchaId != null;
    }

    public boolean tieneDosParejas() {
        return pareja1InscripcionId != null
                && pareja2InscripcionId != null;
    }

    public Long unicaPareja() {
        if (pareja1InscripcionId != null
                && pareja2InscripcionId == null) {
            return pareja1InscripcionId;
        }
        if (pareja1InscripcionId == null
                && pareja2InscripcionId != null) {
            return pareja2InscripcionId;
        }
        return null;
    }

    public int setsGanadosPareja1() {
        return (int) sets.stream()
                .filter(set -> set.parejaGanadora() == 1)
                .count();
    }

    public int setsGanadosPareja2() {
        return (int) sets.stream()
                .filter(set -> set.parejaGanadora() == 2)
                .count();
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof TorneoPartido otro)) return false;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
