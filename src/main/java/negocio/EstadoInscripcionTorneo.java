package negocio;

public enum EstadoInscripcionTorneo {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    LISTA_ESPERA("Lista de espera"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoInscripcionTorneo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean ocupaCupo() {
        return this == CONFIRMADA;
    }

    public boolean esFinal() {
        return this == RECHAZADA || this == CANCELADA;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
