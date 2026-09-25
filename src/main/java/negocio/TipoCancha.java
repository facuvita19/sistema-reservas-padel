package negocio;

public enum TipoCancha {
    CUBIERTA("Cubierta"),
    DESCUBIERTA("Descubierta");

    private final String descripcion;

    TipoCancha(String descripcion) {
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
