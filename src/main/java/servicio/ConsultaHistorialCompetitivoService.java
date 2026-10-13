package servicio;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import config.ConexionBD;

public class ConsultaHistorialCompetitivoService {
 public Resumen consultar(long perfilId){
  if(perfilId<=0)throw new IllegalArgumentException("El perfil no es valido.");
  String q="SELECT h.partido_id,h.resultado,h.nombre_perfil_snapshot,h.categoria_snapshot,h.fecha_partido,h.fecha_resultado,"+
   "t.nombre torneo,tc.nombre categoria_torneo,tp.fase,h.inscripcion_id,h.lado,"+
   "GROUP_CONCAT(DISTINCT CASE WHEN hc.inscripcion_id=h.inscripcion_id AND hc.perfil_jugador_id<>h.perfil_jugador_id THEN hc.nombre_perfil_snapshot END SEPARATOR ' / ') companero,"+
   "GROUP_CONCAT(DISTINCT CASE WHEN hc.inscripcion_id<>h.inscripcion_id THEN hc.nombre_perfil_snapshot END ORDER BY hc.inscripcion_id,hc.orden_integrante SEPARATOR ' / ') rivales,"+
   "GROUP_CONCAT(CONCAT(s.puntos_pareja_1,'-',s.puntos_pareja_2) ORDER BY s.numero_set SEPARATOR ' / ') marcador "+
   "FROM historial_competitivo_perfiles h JOIN torneo_partidos tp ON tp.id=h.partido_id "+
   "JOIN torneo_categorias tc ON tc.id=h.torneo_categoria_id JOIN torneos t ON t.id=tc.torneo_id "+
   "LEFT JOIN historial_competitivo_perfiles hc ON hc.partido_id=h.partido_id "+
   "LEFT JOIN torneo_partido_sets s ON s.partido_id=h.partido_id WHERE h.perfil_jugador_id=? "+
   "GROUP BY h.partido_id,h.resultado,h.nombre_perfil_snapshot,h.categoria_snapshot,h.fecha_partido,h.fecha_resultado,t.nombre,tc.nombre,tp.fase,h.inscripcion_id,h.lado "+
   "ORDER BY COALESCE(h.fecha_partido,DATE(h.fecha_resultado)) DESC,h.fecha_resultado DESC,h.partido_id DESC";
  try(Connection c=ConexionBD.obtenerConexion();PreparedStatement s=c.prepareStatement(q)){s.setLong(1,perfilId);try(ResultSet r=s.executeQuery()){List<Partido> items=new ArrayList<>();while(r.next())items.add(new Partido(r.getLong("partido_id"),r.getString("resultado"),r.getString("torneo"),r.getString("categoria_torneo"),formatearFase(r.getString("fase")),r.getString("companero"),r.getString("rivales"),r.getString("marcador"),r.getDate("fecha_partido")==null?null:r.getDate("fecha_partido").toLocalDate(),r.getTimestamp("fecha_resultado").toLocalDateTime()));int victorias=(int)items.stream().filter(x->"GANADOR".equals(x.resultado())).count();return new Resumen(items.size(),victorias,items.size()-victorias,items.isEmpty()?0:(int)Math.round(victorias*100.0/items.size()),items);}}catch(SQLException e){throw new RuntimeException("No se pudo cargar el historial competitivo.",e);}
 }
 private String formatearFase(String x){return x==null?"-":x.replace('_',' ');}
 public record Resumen(int partidos,int victorias,int derrotas,int efectividad,List<Partido> historial){}
 public record Partido(long partidoId,String resultado,String torneo,String categoria,String fase,String companero,String rivales,String marcador,LocalDate fecha,LocalDateTime fechaResultado){}
}
