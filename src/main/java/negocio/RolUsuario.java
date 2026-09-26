package negocio;

public enum RolUsuario {
    ADMINISTRADOR("Administrador"),
    OPERADOR("Operador"),
    CLIENTE("Cliente");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esPersonalDelComplejo() {
        return this == ADMINISTRADOR || this == OPERADOR;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
