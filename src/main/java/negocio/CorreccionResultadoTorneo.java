package negocio;

import java.time.LocalDateTime;

public class CorreccionResultadoTorneo {
    private long id;
    private long partidoId;
    private long usuarioId;
    private String usuario;
    private String motivo;
    private Long ganadoraAnteriorInscripcionId;
    private Long ganadoraNuevaInscripcionId;
    private String resultadoAnterior;
    private String resultadoNuevo;
    private String setsAnterioresJson;
    private String setsNuevosJson;
    private LocalDateTime fechaCorreccion;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getPartidoId() { return partidoId; }
    public void setPartidoId(long valor) { partidoId = valor; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long valor) { usuarioId = valor; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String valor) { usuario = valor; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String valor) { motivo = valor; }
    public Long getGanadoraAnteriorInscripcionId() { return ganadoraAnteriorInscripcionId; }
    public void setGanadoraAnteriorInscripcionId(Long valor) { ganadoraAnteriorInscripcionId = valor; }
    public Long getGanadoraNuevaInscripcionId() { return ganadoraNuevaInscripcionId; }
    public void setGanadoraNuevaInscripcionId(Long valor) { ganadoraNuevaInscripcionId = valor; }
    public String getResultadoAnterior() { return resultadoAnterior; }
    public void setResultadoAnterior(String valor) { resultadoAnterior = valor; }
    public String getResultadoNuevo() { return resultadoNuevo; }
    public void setResultadoNuevo(String valor) { resultadoNuevo = valor; }
    public String getSetsAnterioresJson() { return setsAnterioresJson; }
    public void setSetsAnterioresJson(String valor) { setsAnterioresJson = valor; }
    public String getSetsNuevosJson() { return setsNuevosJson; }
    public void setSetsNuevosJson(String valor) { setsNuevosJson = valor; }
    public LocalDateTime getFechaCorreccion() { return fechaCorreccion; }
    public void setFechaCorreccion(LocalDateTime valor) { fechaCorreccion = valor; }
}
