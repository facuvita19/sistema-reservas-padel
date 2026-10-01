package negocio;

import java.time.LocalTime;

public class CeldaAgenda {

    private long canchaId;
    private String nombreCancha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoCeldaAgenda estado;
    private Long reservaId;
    private Long bloqueoId;
    private Long partidoId;
    private Long torneoCategoriaId;
    private String detalle;

    public long getCanchaId() { return canchaId; }
    public void setCanchaId(long valor) { canchaId = valor; }
    public String getNombreCancha() { return nombreCancha; }
    public void setNombreCancha(String valor) { nombreCancha = valor; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime valor) { horaInicio = valor; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime valor) { horaFin = valor; }
    public EstadoCeldaAgenda getEstado() { return estado; }
    public void setEstado(EstadoCeldaAgenda valor) { estado = valor; }
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long valor) { reservaId = valor; }
    public Long getBloqueoId() { return bloqueoId; }
    public void setBloqueoId(Long valor) { bloqueoId = valor; }
    public Long getPartidoId() { return partidoId; }
    public void setPartidoId(Long valor) { partidoId = valor; }
    public Long getTorneoCategoriaId() { return torneoCategoriaId; }
    public void setTorneoCategoriaId(Long valor) { torneoCategoriaId = valor; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String valor) { detalle = valor; }

    public boolean estaDisponible() {
        return estado == EstadoCeldaAgenda.DISPONIBLE;
    }
}
