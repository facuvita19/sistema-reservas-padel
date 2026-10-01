package negocio;

public enum FaseTorneo {
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
