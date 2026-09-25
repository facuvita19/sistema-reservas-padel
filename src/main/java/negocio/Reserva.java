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
    private int cantidadJugadores = 4;
    private String comentarios;
    private String observacionesAdministrativas;
    private BigDecimal precioTotal = BigDecimal.ZERO;
    private LocalDateTime fechaCreacion;

    private String nombreCliente;
    private String nombreCancha;
    private String nombreUsuario;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClienteId() {
        return clienteId;
    }

    public void setClienteId(long clienteId) {
        this.clienteId = clienteId;
    }

    public long getCanchaId() {
        return canchaId;
    }

    public void setCanchaId(long canchaId) {
        this.canchaId = canchaId;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
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

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public int getCantidadJugadores() {
        return cantidadJugadores;
    }

    public void setCantidadJugadores(int cantidadJugadores) {
        this.cantidadJugadores = cantidadJugadores;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public String getObservacionesAdministrativas() {
        return observacionesAdministrativas;
    }

    public void setObservacionesAdministrativas(
            String observacionesAdministrativas) {
        this.observacionesAdministrativas = observacionesAdministrativas;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreCancha() {
        return nombreCancha;
    }

    public void setNombreCancha(String nombreCancha) {
        this.nombreCancha = nombreCancha;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
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
        if (!(objeto instanceof Reserva)) {
            return false;
        }
        Reserva otra = (Reserva) objeto;
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
