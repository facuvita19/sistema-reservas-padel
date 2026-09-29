package negocio;

import java.time.LocalDateTime;
import java.util.Objects;

public class SesionCliente {

    private long id;
    private long usuarioId;
    private String tokenHash;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaUltimoUso;
    private boolean revocada;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDateTime getFechaUltimoUso() {
        return fechaUltimoUso;
    }

    public void setFechaUltimoUso(LocalDateTime fechaUltimoUso) {
        this.fechaUltimoUso = fechaUltimoUso;
    }

    public boolean isRevocada() {
        return revocada;
    }

    public void setRevocada(boolean revocada) {
        this.revocada = revocada;
    }

    public boolean estaVigente(LocalDateTime momento) {
        return !revocada
                && momento != null
                && fechaVencimiento != null
                && fechaVencimiento.isAfter(momento);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof SesionCliente otra)) return false;
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
