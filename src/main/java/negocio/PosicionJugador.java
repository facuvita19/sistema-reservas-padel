package negocio;

public enum PosicionJugador {

    DRIVE("Drive"),
    REVES("Revés");

    private final String nombreVisible;

    PosicionJugador(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
