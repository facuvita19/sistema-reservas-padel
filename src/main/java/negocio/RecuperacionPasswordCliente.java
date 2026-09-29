package negocio;

import java.time.LocalDateTime;
import java.util.Objects;

public class RecuperacionPasswordCliente {

    private long id;
    private long usuarioId;
    private String tokenHash;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaUso;
    private boolean revocada;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(long valor) { usuarioId = valor; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String valor) { tokenHash = valor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime valor) { fechaCreacion = valor; }
    public LocalDateTime getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime valor) { fechaVencimiento = valor; }
    public LocalDateTime getFechaUso() { return fechaUso; }
    public void setFechaUso(LocalDateTime valor) { fechaUso = valor; }
    public boolean isRevocada() { return revocada; }
    public void setRevocada(boolean valor) { revocada = valor; }

    public boolean estaVigente(LocalDateTime momento) {
        return !revocada
                && fechaUso == null
                && fechaVencimiento != null
                && momento != null
                && fechaVencimiento.isAfter(momento);
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof RecuperacionPasswordCliente otra)) {
            return false;
        }
        return id > 0 && id == otra.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
