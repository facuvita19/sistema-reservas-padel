package negocio;

public enum OrigenReserva {
    PERSONAL("Personal"),
    WEB("Web");

    private final String descripcion;

    OrigenReserva(String descripcion) {
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
