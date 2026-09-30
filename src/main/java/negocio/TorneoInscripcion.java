package negocio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TorneoInscripcion {

    private long id;
    private long torneoCategoriaId;
    private EstadoInscripcionTorneo estado = EstadoInscripcionTorneo.PENDIENTE;
    private OrigenInscripcionTorneo origen = OrigenInscripcionTorneo.WEB;
    private Long responsableClienteId;
    private BigDecimal precioInscripcion = BigDecimal.ZERO;
    private String comentarios;
    private String observacionesAdministrativas;
    private Long usuarioGestionId;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaCancelacion;
    private LocalDateTime fechaActualizacion;
    private final List<TorneoInscripcionJugador> jugadores = new ArrayList<>();

    private String nombreTorneo;
    private String nombreCategoria;
    private RamaTorneo rama;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getTorneoCategoriaId() { return torneoCategoriaId; }
    public void setTorneoCategoriaId(long valor) { torneoCategoriaId = valor; }
    public EstadoInscripcionTorneo getEstado() { return estado; }
    public void setEstado(EstadoInscripcionTorneo valor) { estado = valor == null ? EstadoInscripcionTorneo.PENDIENTE : valor; }
    public OrigenInscripcionTorneo getOrigen() { return origen; }
    public void setOrigen(OrigenInscripcionTorneo valor) { origen = valor == null ? OrigenInscripcionTorneo.WEB : valor; }
    public Long getResponsableClienteId() { return responsableClienteId; }
    public void setResponsableClienteId(Long valor) { responsableClienteId = valor; }
    public BigDecimal getPrecioInscripcion() { return precioInscripcion; }
    public void setPrecioInscripcion(BigDecimal valor) { precioInscripcion = valor == null ? BigDecimal.ZERO : valor; }
    public String getComentarios() { return comentarios; }
    public void setComentarios(String valor) { comentarios = valor; }
    public String getObservacionesAdministrativas() { return observacionesAdministrativas; }
    public void setObservacionesAdministrativas(String valor) { observacionesAdministrativas = valor; }
    public Long getUsuarioGestionId() { return usuarioGestionId; }
    public void setUsuarioGestionId(Long valor) { usuarioGestionId = valor; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime valor) { fechaSolicitud = valor; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime valor) { fechaConfirmacion = valor; }
    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(LocalDateTime valor) { fechaCancelacion = valor; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime valor) { fechaActualizacion = valor; }
    public List<TorneoInscripcionJugador> getJugadores() { return new ArrayList<>(jugadores); }
    public void setJugadores(List<TorneoInscripcionJugador> valores) { jugadores.clear(); if (valores != null) jugadores.addAll(valores); }
    public void agregarJugador(TorneoInscripcionJugador jugador) { if (jugador != null) jugadores.add(jugador); }
    public String getNombreTorneo() { return nombreTorneo; }
    public void setNombreTorneo(String valor) { nombreTorneo = valor; }
    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String valor) { nombreCategoria = valor; }
    public RamaTorneo getRama() { return rama; }
    public void setRama(RamaTorneo valor) { rama = valor; }

    public boolean tieneParejaCompleta() {
        return jugadores.size() == 2
                && jugadores.stream().anyMatch(j -> j.getOrdenIntegrante() == 1)
                && jugadores.stream().anyMatch(j -> j.getOrdenIntegrante() == 2);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof TorneoInscripcion otra)) return false;
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
