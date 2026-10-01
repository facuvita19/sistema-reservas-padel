package negocio;

public enum PosicionPartidoSiguiente {
    PAREJA_1("Pareja 1"),
    PAREJA_2("Pareja 2");

    private final String descripcion;

    PosicionPartidoSiguiente(String descripcion) {
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
