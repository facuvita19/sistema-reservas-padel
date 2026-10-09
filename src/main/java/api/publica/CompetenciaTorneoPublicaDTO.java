package api.publica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class CompetenciaTorneoPublicaDTO {
    private CompetenciaTorneoPublicaDTO() { }

    public record Competencia(
            Torneo torneo,
            Categoria categoria,
            Resumen resumen,
            List<Grupo> grupos,
            CuadroTorneoPublicoDTO.CuadroCategoria eliminatorias) { }

    public record Torneo(long id, String nombre, String estado,
            LocalDate fechaInicio, LocalDate fechaFin) { }

    public record Categoria(long id, String nombre, String rama,
            String formatoCompetencia, int cupoParejas,
            int cantidadGruposTres, int cantidadGruposCuatro,
            int clasificadosProyectados) { }

    public record Resumen(String etapa, boolean gruposConfigurados,
            boolean gruposConfirmados, boolean posicionesDefinitivas,
            boolean desempatePendiente, boolean eliminatoriasGeneradas,
            int partidosGrupos, int partidosGruposFinalizados,
            int partidosEliminatorios, int partidosEliminatoriosFinalizados) { }

    public record Grupo(long id, String nombre, int orden, int capacidad,
            String modoAsignacion, boolean confirmado, int clasifican,
            boolean posicionesDefinitivas, boolean desempatePendiente,
            List<Integrante> integrantes, List<Posicion> posiciones,
            List<Partido> partidos) { }

    public record Integrante(long inscripcionId, String pareja,
            boolean cabezaSerie, int ordenSorteo) { }

    public record Posicion(int posicion, long inscripcionId, String pareja,
            int partidosJugados, int partidosGanados, int partidosPerdidos,
            int setsGanados, int setsPerdidos, int diferenciaSets,
            int gamesGanados, int gamesPerdidos, int diferenciaGames,
            String estado) { }

    public record Partido(long id, String tipo, int orden, String estado,
            Pareja pareja1, Pareja pareja2, Long ganadoraInscripcionId,
            String resultado, List<Set> sets, LocalDate fecha,
            LocalTime horaInicio, LocalTime horaFin, Long canchaId,
            String cancha, LocalDateTime fechaFinalizacion) { }

    public record Pareja(long inscripcionId, List<String> jugadores) { }
    public record Set(int numero, String tipo, int puntosPareja1,
            int puntosPareja2) { }
}
