package negocio;

public enum EstadoTorneo {
    BORRADOR("Borrador"),
    PUBLICADO("Publicado"),
    INSCRIPCION_ABIERTA("Inscripción abierta"),
    INSCRIPCION_CERRADA("Inscripción cerrada"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoTorneo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean permiteInscripciones() {
        return this == INSCRIPCION_ABIERTA;
    }

    public boolean esFinal() {
        return this == FINALIZADO || this == CANCELADO;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
