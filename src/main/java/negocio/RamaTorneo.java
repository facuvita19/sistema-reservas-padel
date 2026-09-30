package negocio;

public enum RamaTorneo {
    MASCULINA("Masculina"),
    FEMENINA("Femenina"),
    MIXTA("Mixta");

    private final String descripcion;

    RamaTorneo(String descripcion) {
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
