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

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String valor) { nombreUsuario = valor; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String valor) { passwordHash = valor; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario valor) { rol = valor; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long valor) { clienteId = valor; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean valor) { activo = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }

    public boolean esAdministrador() {
        return rol == RolUsuario.ADMINISTRADOR;
    }

    public boolean esOperador() {
        return rol == RolUsuario.OPERADOR;
    }

    public boolean esPersonalDelComplejo() {
        return rol != null && rol.esPersonalDelComplejo();
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
        if (this == objeto) return true;
        if (!(objeto instanceof Usuario otro)) return false;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
