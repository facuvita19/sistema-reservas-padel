package negocio;

public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    AUSENTE("Ausente");

    private final String descripcion;

    EstadoReserva(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esFinal() {
        return this == COMPLETADA
                || this == CANCELADA
                || this == AUSENTE;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
