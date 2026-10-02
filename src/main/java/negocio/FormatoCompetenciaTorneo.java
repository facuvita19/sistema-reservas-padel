package negocio;

public enum FormatoCompetenciaTorneo {
    ELIMINACION_DIRECTA("Eliminación directa"),
    GRUPOS_ELIMINACION("Fase de grupos + eliminación");

    private final String descripcion;

    FormatoCompetenciaTorneo(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
