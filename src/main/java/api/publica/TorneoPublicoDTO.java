package api.publica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class TorneoPublicoDTO {

    private TorneoPublicoDTO() {
    }

    public record TorneoResumen(
            long id,
            String nombre,
            String descripcion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            LocalDateTime inscripcionDesde,
            LocalDateTime inscripcionHasta,
            String estado,
            boolean inscripcionDisponible,
            int cantidadCategorias) {
    }

    public record TorneoDetalle(
            long id,
            String nombre,
            String descripcion,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            LocalDateTime inscripcionDesde,
            LocalDateTime inscripcionHasta,
            String estado,
            String reglamento,
            boolean inscripcionDisponible,
            List<Categoria> categorias) {
    }

    public record Categoria(
            long id,
            String nombre,
            String rama,
            int cupoParejas,
            int parejasConfirmadas,
            int cuposDisponibles,
            BigDecimal precioInscripcion,
            boolean disponible) {
    }
}
