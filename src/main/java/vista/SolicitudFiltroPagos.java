package vista;

public record SolicitudFiltroPagos(FiltroPagos filtro) {
    public SolicitudFiltroPagos {
        if (filtro == null) {
            throw new IllegalArgumentException("El filtro de pagos es obligatorio.");
        }
    }
}
