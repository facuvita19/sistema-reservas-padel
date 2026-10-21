package api.publica;

import java.time.LocalDateTime;
import java.util.List;

public final class GestionSolicitudesComunidadDTO {
    private GestionSolicitudesComunidadDTO() { }

    public record Perfil(
            long perfilId,
            String nombre,
            String iniciales,
            String posicion,
            String categoria,
            String categoriaColor) { }

    public record Solicitud(
            long solicitudId,
            long convocatoriaId,
            String estado,
            String mensaje,
            LocalDateTime fechaSolicitud,
            LocalDateTime fechaResolucion,
            long version,
            boolean puedeAceptar,
            boolean puedeRechazar,
            Perfil perfil) { }

    public record Listado(
            long convocatoriaId,
            String estadoConvocatoria,
            int participantesConfirmados,
            int lugaresDisponibles,
            List<Solicitud> solicitudes) {
        public Listado {
            solicitudes = solicitudes == null
                    ? List.of() : List.copyOf(solicitudes);
        }
    }

    public record Resultado(
            Solicitud solicitud,
            String estadoConvocatoria,
            int participantesConfirmados,
            int lugaresDisponibles,
            boolean convocatoriaCompleta) { }
}
