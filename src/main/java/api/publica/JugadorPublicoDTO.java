package api.publica;
import java.time.*;import java.util.List;
public final class JugadorPublicoDTO {private JugadorPublicoDTO(){}
 public record Categoria(long id,String nombre,String color,LocalDate desde){}
 public record Ranking(int posicion,int puntos,int partidos,int victorias,int derrotas,int efectividad,int diferenciaSets,int diferenciaGames){}
 public record Partido(long partidoId,String resultado,String torneo,String categoria,String fase,String companero,String rivales,String marcador,LocalDate fecha){}
 public record MovimientoCategoria(String categoria,String estado,LocalDate desde,LocalDate hasta){}
 public record Perfil(long perfilId,String nombre,String iniciales,String posicion,boolean disponibleParaJugar,boolean aceptaInvitaciones,boolean buscaTurnos,boolean buscaTorneos,Categoria categoria,Ranking ranking,int partidos,int victorias,int derrotas,int efectividad,List<Partido> historial,List<MovimientoCategoria> categorias){}
}
