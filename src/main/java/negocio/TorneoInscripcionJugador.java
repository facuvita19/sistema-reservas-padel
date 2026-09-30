package negocio;

import java.time.LocalDateTime;
import java.util.Objects;

public class TorneoInscripcionJugador {

    private long id;
    private long inscripcionId;
    private int ordenIntegrante;
    private Long clienteId;
    private String nombre;
    private String apellido;
    private String telefono;
    private String telefonoNormalizado;
    private boolean responsable;
    private TipoVinculacionTorneo tipoVinculacion = TipoVinculacionTorneo.SIN_VINCULAR;
    private boolean requiereRevision;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getInscripcionId() { return inscripcionId; }
    public void setInscripcionId(long valor) { inscripcionId = valor; }
    public int getOrdenIntegrante() { return ordenIntegrante; }
    public void setOrdenIntegrante(int valor) { ordenIntegrante = valor; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long valor) { clienteId = valor; }
    public String getNombre() { return nombre; }
    public void setNombre(String valor) { nombre = valor; }
    public String getApellido() { return apellido; }
    public void setApellido(String valor) { apellido = valor; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String valor) { telefono = valor; }
    public String getTelefonoNormalizado() { return telefonoNormalizado; }
    public void setTelefonoNormalizado(String valor) { telefonoNormalizado = valor; }
    public boolean isResponsable() { return responsable; }
    public void setResponsable(boolean valor) { responsable = valor; }
    public TipoVinculacionTorneo getTipoVinculacion() { return tipoVinculacion; }
    public void setTipoVinculacion(TipoVinculacionTorneo valor) { tipoVinculacion = valor == null ? TipoVinculacionTorneo.SIN_VINCULAR : valor; }
    public boolean isRequiereRevision() { return requiereRevision; }
    public void setRequiereRevision(boolean valor) { requiereRevision = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime valor) { fechaActualizacion = valor; }

    public boolean estaVinculado() {
        return clienteId != null && clienteId > 0;
    }

    public String getNombreCompleto() {
        return ((nombre == null ? "" : nombre) + " "
                + (apellido == null ? "" : apellido)).trim();
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof TorneoInscripcionJugador otro)) return false;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
