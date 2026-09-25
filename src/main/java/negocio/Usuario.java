package negocio;

import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario {

    private long id;
    private String nombreUsuario;
    private String passwordHash;
    private RolUsuario rol = RolUsuario.CLIENTE;
    private Long clienteId;
    private boolean activo = true;
    private LocalDateTime fechaCreacion;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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

    public boolean esAdministrador() {
        return rol == RolUsuario.ADMINISTRADOR;
    }

    public boolean estaVinculadoACliente() {
        return clienteId != null && clienteId > 0;
    }

    @Override
    public String toString() {
        return nombreUsuario == null ? "" : nombreUsuario;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Usuario)) {
            return false;
        }
        Usuario otro = (Usuario) objeto;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
