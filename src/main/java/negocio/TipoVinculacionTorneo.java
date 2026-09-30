package negocio;

public enum TipoVinculacionTorneo {
    AUTOMATICA("Automática"),
    MANUAL("Manual"),
    SIN_VINCULAR("Sin vincular");

    private final String descripcion;

    TipoVinculacionTorneo(String descripcion) {
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
