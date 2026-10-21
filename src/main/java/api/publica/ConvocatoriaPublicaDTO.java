package api.publica;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class ConvocatoriaPublicaDTO {
    private ConvocatoriaPublicaDTO() { }

    public record Categoria(long id, String nombre, String color) { }

    public record Convocante(
            long perfilId,
            String nombre,
            String iniciales,
            String posicion,
            Categoria categoria) { }

    public record Turno(
            long convocatoriaId,
            long reservaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            String cancha,
            int participantesConfirmados,
            int lugaresBuscados,
            int lugaresDisponibles,
            String resumenCupo,
            Categoria categoriaBuscada,
            String posicionBuscada,
            String comentario,
            Convocante convocante) { }

    public record Pagina(
            List<Turno> convocatorias,
            int pagina,
            int tamano,
            long total,
            int totalPaginas) { }

    public record Detalle(
            long convocatoriaId,
            String estado,
            boolean activa,
            boolean aceptaSolicitudes,
            String mensajeEstado,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            String cancha,
            int participantesConfirmados,
            int lugaresBuscados,
            int lugaresDisponibles,
            String resumenCupo,
            Categoria categoriaBuscada,
            String posicionBuscada,
            String comentario,
            Convocante convocante) { }

    public record MiEstado(
            boolean autenticado,
            boolean titular,
            boolean participa,
            boolean puedeSolicitar,
            String motivoBloqueo,
            Solicitud solicitud) { }

    public record Solicitud(
            long solicitudId,
            long convocatoriaId,
            String estado,
            String mensaje,
            boolean puedeRetirar,
            long version) { }

    public record CrearSolicitud(String mensaje) { }
}
