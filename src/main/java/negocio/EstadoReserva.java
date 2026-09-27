package negocio;

public enum EstadoReserva {
    PENDIENTE("Esperando seña"),
    CONFIRMADA("Confirmada"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    AUSENTE("Ausente"),
    EXPIRADA("Expirada");

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
                || this == AUSENTE
                || this == EXPIRADA;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
