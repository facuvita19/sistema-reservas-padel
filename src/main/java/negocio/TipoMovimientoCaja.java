package negocio;

public enum TipoMovimientoCaja {
    INGRESO("Ingreso"),
    EGRESO("Egreso");

    private final String descripcion;

    TipoMovimientoCaja(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
