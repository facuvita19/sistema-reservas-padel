package negocio;

public enum EstadoPago {
    PENDIENTE("Pendiente"),
    ACREDITADO("Acreditado"),
    REEMBOLSADO("Reembolsado"),
    ANULADO("Anulado");

    private final String descripcion;

    EstadoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
