package negocio;

public enum OrigenInscripcionTorneo {
    WEB("Web"),
    ADMINISTRACION("Administración");

    private final String descripcion;

    OrigenInscripcionTorneo(String descripcion) {
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
