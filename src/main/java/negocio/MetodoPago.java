package negocio;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    TARJETA("Tarjeta"),
    OTRO("Otro");

    private final String descripcion;

    MetodoPago(String descripcion) {
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
