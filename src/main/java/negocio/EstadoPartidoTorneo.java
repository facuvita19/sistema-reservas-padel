package negocio;

public enum EstadoPartidoTorneo {
    PENDIENTE("Pendiente"),
    PROGRAMADO("Programado"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoPartidoTorneo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean bloqueaCancha() {
        return this == PROGRAMADO
                || this == EN_CURSO
                || this == FINALIZADO;
    }

    public boolean esFinal() {
        return this == FINALIZADO || this == CANCELADO;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
