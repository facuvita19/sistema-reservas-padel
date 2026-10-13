package api.publica;
import java.time.LocalDateTime;import java.util.List;
public final class RankingPublicoDTO {private RankingPublicoDTO(){}
 public record Categoria(long id,String codigo,String nombre,String color,int participantes){}
 public record Fila(int posicion,long perfilId,String jugador,String iniciales,int puntos,int partidos,int victorias,int derrotas,int efectividad,int diferenciaSets,int diferenciaGames){}
 public record Aporte(long partidoId,String torneo,String categoria,String fase,String resultado,int base,int bonus,int total,LocalDateTime fecha){}
 public record Ranking(Categoria categoria,int participantes,int partidosAuditados,LocalDateTime ultimoResultado,List<Fila> posiciones){}
 public record Auditoria(long perfilId,long categoriaId,LocalDateTime actualizado,List<Aporte> aportes){}
}
