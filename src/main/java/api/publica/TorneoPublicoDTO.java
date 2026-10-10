package api.publica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class TorneoPublicoDTO {
    private TorneoPublicoDTO() { }

    public record TorneoResumen(long id, String nombre, String descripcion,
            LocalDate fechaInicio, LocalDate fechaFin,
            LocalDateTime inscripcionDesde, LocalDateTime inscripcionHasta,
            String estado, String estadoPortal, boolean inscripcionDisponible,
            int cantidadCategorias, int categoriasDisponibles,
            int cupoTotal, int parejasConfirmadas, int cuposDisponibles,
            BigDecimal precioMinimo, BigDecimal precioMaximo,
            List<String> categorias, List<String> ramas, List<String> formatos,
            String etapaCompetitiva, int partidosTotales, int partidosFinalizados,
            int partidosCancelados, boolean resultadosDisponibles,
            boolean campeonesDisponibles) { }

    public record TorneoDetalle(long id, String nombre, String descripcion,
            LocalDate fechaInicio, LocalDate fechaFin,
            LocalDateTime inscripcionDesde, LocalDateTime inscripcionHasta,
            String estado, String reglamento, boolean inscripcionDisponible,
            List<Categoria> categorias) { }

    public record Categoria(long id, String nombre, String rama,
            int cupoParejas, int parejasConfirmadas, int cuposDisponibles,
            BigDecimal precioInscripcion, BigDecimal premioCampeon,
            BigDecimal premioSubcampeon, String premioDescripcion,
            boolean disponible, String formatoCompetencia,
            int cantidadGruposTres, int cantidadGruposCuatro,
            int clasificadosProyectados) { }
}
