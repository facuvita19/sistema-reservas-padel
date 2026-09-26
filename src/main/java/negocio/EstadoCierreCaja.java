package negocio;

public enum EstadoCierreCaja {
    ABIERTA("Abierta"),
    CERRADA("Cerrada");

    private final String descripcion;

    EstadoCierreCaja(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
