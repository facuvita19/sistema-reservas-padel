package negocio;

public enum FaseTorneo {
    GRUPOS("Grupos", 0),
    ACCESO_1("Fase previa · Ronda 1", 0),
    ACCESO_2("Fase previa · Ronda 2", 0),
    ACCESO_3("Fase previa · Ronda 3", 0),
    ACCESO_4("Fase previa · Ronda 4", 0),
    ACCESO_5("Fase previa · Ronda 5", 0),
    DIECISEISAVOS("Dieciseisavos", 32),
    OCTAVOS("Octavos", 16),
    CUARTOS("Cuartos", 8),
    SEMIFINAL("Semifinal", 4),
    FINAL("Final", 2);

    private final String descripcion;
    private final int cantidadParejas;

    FaseTorneo(String descripcion, int cantidadParejas) {
        this.descripcion = descripcion;
        this.cantidadParejas = cantidadParejas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidadParejas() {
        return cantidadParejas;
    }

    public FaseTorneo siguiente() {
        return switch (this) {
            case GRUPOS, ACCESO_1, ACCESO_2, ACCESO_3,
                    ACCESO_4, ACCESO_5 -> null;
            case DIECISEISAVOS -> OCTAVOS;
            case OCTAVOS -> CUARTOS;
            case CUARTOS -> SEMIFINAL;
            case SEMIFINAL -> FINAL;
            case FINAL -> null;
        };
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
