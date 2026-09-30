package api.publica;

public final class TorneoInscripcionPublicaDTO {

    private TorneoInscripcionPublicaDTO() {
    }

    public record Jugador(
            String nombre,
            String apellido,
            String telefono) {
    }

    public record CrearSolicitud(
            long torneoCategoriaId,
            Jugador responsable,
            Jugador pareja,
            String comentarios) {
    }

    public record SolicitudCreada(
            long inscripcionId,
            String estado,
            String mensaje) {
    }
}
