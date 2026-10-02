package negocio;

public enum EstadoClasificacionGrupo {
    CLASIFICADO("Clasificado"),
    ELIMINADO("Eliminado"),
    PENDIENTE("Pendiente"),
    DESEMPATE_PENDIENTE("Desempate pendiente");

    private final String descripcion;

    EstadoClasificacionGrupo(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
