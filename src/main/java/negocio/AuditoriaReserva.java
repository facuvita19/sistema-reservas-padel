package negocio;

import java.time.LocalDateTime;

public class AuditoriaReserva {
    private long id;
    private long reservaId;
    private Long usuarioId;
    private AccionAuditoriaReserva accion;
    private EstadoReserva estadoAnterior;
    private EstadoReserva estadoNuevo;
    private String detalle;
    private LocalDateTime fechaEvento;
    private String nombreUsuario;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getReservaId() { return reservaId; }
    public void setReservaId(long valor) { reservaId = valor; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long valor) { usuarioId = valor; }
    public AccionAuditoriaReserva getAccion() { return accion; }
    public void setAccion(AccionAuditoriaReserva valor) { accion = valor; }
    public EstadoReserva getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoReserva valor) { estadoAnterior = valor; }
    public EstadoReserva getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoReserva valor) { estadoNuevo = valor; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String valor) { detalle = valor; }
    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime valor) { fechaEvento = valor; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String valor) { nombreUsuario = valor; }

    public String getResponsable() {
        return nombreUsuario == null || nombreUsuario.isBlank()
                ? "SISTEMA"
                : nombreUsuario;
    }
}
