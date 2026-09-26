package vista;

public record SolicitudPagoReserva(long reservaId) {

    public SolicitudPagoReserva {
        if (reservaId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la reserva no es válido."
            );
        }
    }
}
