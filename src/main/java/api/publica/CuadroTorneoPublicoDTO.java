package api.publica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class CuadroTorneoPublicoDTO {

    private CuadroTorneoPublicoDTO() {
    }

    public record Torneo(
            long id,
            String nombre,
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
    }

    public record Categoria(
            long id,
            String nombre,
            String rama) {
    }

    public record Pareja(
            long inscripcionId,
            List<String> jugadores) {
    }

    public record Set(
            int numero,
            String tipo,
            int puntosPareja1,
            int puntosPareja2) {
    }

    public record Partido(
            long id,
            String fase,
            int orden,
            String estado,
            boolean bye,
            Pareja pareja1,
            Pareja pareja2,
            Long ganadoraInscripcionId,
            String resultado,
            List<Set> sets,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            Long canchaId,
            String cancha,
            LocalDateTime fechaFinalizacion,
            Long partidoSiguienteId,
            String posicionSiguiente) {
    }

    public record Fase(
            String nombre,
            List<Partido> partidos) {
    }

    public record Campeona(
            long inscripcionId,
            List<String> jugadores,
            String resultadoFinal) {
    }

    public record CuadroCategoria(
            Torneo torneo,
            Categoria categoria,
            Campeona campeona,
            List<Fase> fases) {
    }

    public record CuadroTorneo(
            Torneo torneo,
            List<CuadroCategoria> categorias) {
    }
}
