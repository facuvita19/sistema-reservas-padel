package negocio;

public enum TipoPartidoGrupo {
    TODOS_CONTRA_TODOS("Todos contra todos"),
    CRUCE_INICIAL("Cruce inicial"),
    DEFINICION_PRIMERO_SEGUNDO("Definicion de 1ro y 2do"),
    DEFINICION_TERCERO_CUARTO("Definicion de 3ro y 4to");

    private final String descripcion;

    TipoPartidoGrupo(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
