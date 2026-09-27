package negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Reserva {

    private long id;
    private long clienteId;
    private long canchaId;
    private long usuarioId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaExpiracion;
    private TipoCancelacion tipoCancelacion;
    private String motivoCancelacion;
    private LocalDateTime fechaCancelacion;
    private Long usuarioCancelacionId;
    private int cantidadJugadores = 4;
    private String comentarios;
    private String observacionesAdministrativas;
    private BigDecimal precioTotal = BigDecimal.ZERO;
    private LocalDateTime fechaCreacion;

    private String nombreCliente;
    private String nombreCancha;
    private String nombreUsuario;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getClienteId() { return clienteId; }
    public void setClienteId(long valor) { clienteId = valor; }
    public long getCanchaId() { return canchaId; }
    public void setCanchaId(long valor) { canchaId = valor; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long valor) { usuarioId = valor; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate valor) { fecha = valor; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime valor) { horaInicio = valor; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime valor) { horaFin = valor; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva valor) { estado = valor; }
    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime valor) { fechaVencimiento = valor; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime valor) { fechaExpiracion = valor; }
    public TipoCancelacion getTipoCancelacion() { return tipoCancelacion; }
    public void setTipoCancelacion(TipoCancelacion valor) { tipoCancelacion = valor; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String valor) { motivoCancelacion = valor; }
    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(LocalDateTime valor) { fechaCancelacion = valor; }
    public Long getUsuarioCancelacionId() { return usuarioCancelacionId; }
    public void setUsuarioCancelacionId(Long valor) { usuarioCancelacionId = valor; }
    public int getCantidadJugadores() { return cantidadJugadores; }
    public void setCantidadJugadores(int valor) { cantidadJugadores = valor; }
    public String getComentarios() { return comentarios; }
    public void setComentarios(String valor) { comentarios = valor; }
    public String getObservacionesAdministrativas() { return observacionesAdministrativas; }
    public void setObservacionesAdministrativas(String valor) { observacionesAdministrativas = valor; }
    public BigDecimal getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(BigDecimal valor) { precioTotal = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String valor) { nombreCliente = valor; }
    public String getNombreCancha() { return nombreCancha; }
    public void setNombreCancha(String valor) { nombreCancha = valor; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String valor) { nombreUsuario = valor; }

    public boolean tieneVencimiento() {
        return fechaVencimiento != null;
    }

    public boolean estaVencida(LocalDateTime momento) {
        return estado == EstadoReserva.PENDIENTE
                && fechaVencimiento != null
                && momento != null
                && !fechaVencimiento.isAfter(momento);
    }

    public boolean seSuperpone(LocalTime inicio, LocalTime fin) {
        if (horaInicio == null || horaFin == null
                || inicio == null || fin == null) {
            return false;
        }
        return inicio.isBefore(horaFin) && fin.isAfter(horaInicio);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Reserva otra)) return false;
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
