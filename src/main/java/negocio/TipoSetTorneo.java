package negocio;

public enum TipoSetTorneo {
    NORMAL("Set normal"),
    SUPER_TIE_BREAK("Super tie-break");

    private final String descripcion;

    TipoSetTorneo(String descripcion) {
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
