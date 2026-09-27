package vista;

public record SolicitudFiltroReservas(FiltroReservas filtro) {
    public SolicitudFiltroReservas {
        if (filtro == null) {
            throw new IllegalArgumentException("El filtro de reservas es obligatorio.");
        }
    }
}
