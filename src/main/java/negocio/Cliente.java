package negocio;

import java.time.LocalDateTime;
import java.util.Objects;

public class Cliente {

    private long id;
    private String nombre;
    private String apellido;
    private String documento;
    private String telefono;
    private String email;
    private PosicionJugador posicionPreferida;
    private boolean activo = true;
    private LocalDateTime fechaCreacion;

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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PosicionJugador getPosicionPreferida() {
        return posicionPreferida;
    }

    public void setPosicionPreferida(PosicionJugador posicionPreferida) {
        this.posicionPreferida = posicionPreferida;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreCompleto() {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        String apellidoLimpio = apellido == null ? "" : apellido.trim();
        return (nombreLimpio + " " + apellidoLimpio).trim();
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Cliente)) {
            return false;
        }
        Cliente otro = (Cliente) objeto;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
