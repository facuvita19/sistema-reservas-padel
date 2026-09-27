package negocio;

public enum AccionAuditoriaReserva {
    CREACION("Creación"),
    MODIFICACION("Modificación"),
    CAMBIO_ESTADO("Cambio de estado"),
    CONFIRMACION("Confirmación por seña"),
    REPROGRAMACION("Reprogramación"),
    REPROGRAMACION_ADMINISTRATIVA("Reprogramación administrativa"),
    CANCELACION("Cancelación"),
    CANCELACION_ADMINISTRATIVA("Cancelación administrativa"),
    EXPIRACION_AUTOMATICA("Expiración automática"),
    CIERRE_COMPLETADA("Turno completado"),
    CIERRE_AUSENTE("Ausencia");

    private final String descripcion;

    AccionAuditoriaReserva(String descripcion) {
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
