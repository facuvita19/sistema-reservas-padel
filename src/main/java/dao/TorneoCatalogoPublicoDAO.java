package dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TorneoCatalogoPublicoDAO {
    List<Resumen> listar();

    record Resumen(long id, String nombre, String descripcion,
            LocalDate fechaInicio, LocalDate fechaFin,
            LocalDateTime inscripcionDesde, LocalDateTime inscripcionHasta,
            String estado, int cantidadCategorias, int categoriasDisponibles,
            int cupoTotal, int parejasConfirmadas, int cuposDisponibles,
            BigDecimal precioMinimo, BigDecimal precioMaximo,
            List<String> categorias, List<String> ramas, List<String> formatos,
            int partidosTotales, int partidosFinalizados,
            int partidosCancelados, boolean tieneGrupos,
            boolean tieneEliminatorias, boolean finalDisputada) { }
}
