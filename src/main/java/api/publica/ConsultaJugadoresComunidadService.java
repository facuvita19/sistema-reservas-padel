package api.publica;

import java.sql.*;
import java.util.*;
import api.publica.JugadorComunidadPublicoDTO.*;
import config.ConexionBD;
import negocio.PosicionJugador;

public class ConsultaJugadoresComunidadService {
 public static final int TAMANO_PREDETERMINADO=12,TAMANO_MAXIMO=48,BUSQUEDA_MAXIMA=80;
 public enum Objetivo { CUALQUIERA,TURNOS,TORNEOS }
 public enum Orden { NOMBRE,CATEGORIA }
 public record Filtros(String buscar,Long categoriaId,PosicionJugador posicion,Objetivo objetivo,Boolean aceptaInvitaciones,Orden orden){
  public Filtros{buscar=normalizarBusqueda(buscar);if(categoriaId!=null&&categoriaId<=0)throw new IllegalArgumentException("La categoria debe ser un ID positivo.");objetivo=objetivo==null?Objetivo.CUALQUIERA:objetivo;orden=orden==null?Orden.NOMBRE:orden;}
  public static Filtros vacios(){return new Filtros(null,null,null,Objetivo.CUALQUIERA,null,Orden.NOMBRE);}
 }
 @FunctionalInterface interface ProveedorConexion{Connection obtener()throws SQLException;}
 private final ProveedorConexion conexiones;
 public ConsultaJugadoresComunidadService(){this(ConexionBD::obtenerConexion);}
 ConsultaJugadoresComunidadService(ProveedorConexion c){if(c==null)throw new IllegalArgumentException("La conexion es obligatoria.");conexiones=c;}
 public Pagina listar(int pagina,int tamano){return listar(Filtros.vacios(),pagina,tamano);}
 public Pagina listar(Filtros filtros,int pagina,int tamano){validarPaginacion(pagina,tamano);Filtros f=filtros==null?Filtros.vacios():filtros;int offset=Math.multiplyExact(pagina-1,tamano);Consulta q=construir(f);try(Connection c=conexiones.obtener()){long total=contar(c,q);List<Jugador> js=total==0?List.of():consultar(c,q,tamano,offset);int paginas=total==0?0:(int)Math.ceil((double)total/tamano);return new Pagina(js,pagina,tamano,total,paginas);}catch(SQLException e){throw new RuntimeException("No se pudo consultar el catalogo publico de Comunidad.",e);}}
 static void validarPaginacion(int p,int t){if(p<1)throw new IllegalArgumentException("La pagina debe ser mayor o igual a 1.");if(t<1||t>TAMANO_MAXIMO)throw new IllegalArgumentException("El tamano debe estar entre 1 y "+TAMANO_MAXIMO+".");}
 static String normalizarBusqueda(String v){if(v==null||v.isBlank())return null;String x=v.trim().replaceAll("\s+"," ");if(x.length()>BUSQUEDA_MAXIMA)throw new IllegalArgumentException("La busqueda no puede superar "+BUSQUEDA_MAXIMA+" caracteres.");return x.toLowerCase(Locale.ROOT);}
 static Objetivo objetivo(String v){if(v==null||v.isBlank())return Objetivo.CUALQUIERA;try{return Objetivo.valueOf(v.trim().toUpperCase(Locale.ROOT));}catch(RuntimeException e){throw new IllegalArgumentException("El objetivo debe ser CUALQUIERA, TURNOS o TORNEOS.");}}
 static Orden orden(String v){if(v==null||v.isBlank())return Orden.NOMBRE;try{return Orden.valueOf(v.trim().toUpperCase(Locale.ROOT));}catch(RuntimeException e){throw new IllegalArgumentException("El orden debe ser NOMBRE o CATEGORIA.");}}
 static PosicionJugador posicion(String v){if(v==null||v.isBlank())return null;try{return PosicionJugador.valueOf(v.trim().toUpperCase(Locale.ROOT));}catch(RuntimeException e){throw new IllegalArgumentException("La posicion debe ser DRIVE o REVES.");}}
 static String escaparLike(String v){StringBuilder salida=new StringBuilder();for(char caracter:v.toCharArray()){if(caracter==92||caracter=='%'||caracter=='_')salida.append((char)92);salida.append(caracter);}return salida.toString();}
 private long contar(Connection c,Consulta q)throws SQLException{try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) "+q.sql)){bind(s,q.params,1);try(ResultSet r=s.executeQuery()){r.next();return r.getLong(1);}}}
 private List<Jugador> consultar(Connection c,Consulta q,int limite,int offset)throws SQLException{String sql="SELECT p.id perfil_id,p.nombre_publico,p.posicion_preferida,p.disponible_para_jugar,p.acepta_invitaciones,cp.buscar_turnos,cp.buscar_torneos,cp.contacto_whatsapp,cd.id categoria_id,cd.nombre categoria_nombre,cd.color_hex categoria_color "+q.sql+q.order+" LIMIT ? OFFSET ?";try(PreparedStatement s=c.prepareStatement(sql)){int i=bind(s,q.params,1);s.setInt(i++,limite);s.setInt(i,offset);try(ResultSet r=s.executeQuery()){List<Jugador> o=new ArrayList<>();while(r.next())o.add(map(r));return List.copyOf(o);}}}
 private Consulta construir(Filtros f){StringBuilder q=new StringBuilder(base());List<Object> p=new ArrayList<>();if(f.buscar()!=null){q.append(" AND LOWER(TRIM(p.nombre_publico)) LIKE ? ESCAPE '\\'");p.add("%"+escaparLike(f.buscar())+"%");}if(f.categoriaId()!=null){q.append(" AND cd.id=?");p.add(f.categoriaId());}if(f.posicion()!=null){q.append(" AND p.posicion_preferida=?");p.add(f.posicion().name());}if(f.objetivo()==Objetivo.TURNOS)q.append(" AND cp.buscar_turnos=TRUE");if(f.objetivo()==Objetivo.TORNEOS)q.append(" AND cp.buscar_torneos=TRUE");if(f.aceptaInvitaciones()!=null){q.append(" AND p.acepta_invitaciones=?");p.add(f.aceptaInvitaciones());}String order=f.orden()==Orden.CATEGORIA?" ORDER BY cd.nivel_orden,p.nombre_publico,p.id":" ORDER BY LOWER(TRIM(p.nombre_publico)),p.id";return new Consulta(q.toString(),List.copyOf(p),order);}
 private String base(){return "FROM perfiles_jugador p JOIN comunidad_preferencias cp ON cp.cliente_id=p.cliente_id LEFT JOIN jugador_categorias jc ON jc.perfil_jugador_id=p.id AND jc.estado='VIGENTE' LEFT JOIN categorias_deportivas cd ON cd.id=jc.categoria_deportiva_id AND cd.activo=TRUE WHERE p.activo=TRUE AND p.estado='DEFINITIVO' AND p.cliente_id IS NOT NULL AND p.disponible_para_jugar=TRUE AND (cp.buscar_turnos=TRUE OR cp.buscar_torneos=TRUE)";}
 private int bind(PreparedStatement s,List<Object> vs,int i)throws SQLException{for(Object v:vs){if(v instanceof Long n)s.setLong(i++,n);else if(v instanceof Boolean b)s.setBoolean(i++,b);else s.setString(i++,v.toString());}return i;}
 private Jugador map(ResultSet r)throws SQLException{long id=r.getLong("categoria_id");Categoria c=r.wasNull()?null:new Categoria(id,r.getString("categoria_nombre"),r.getString("categoria_color"));String n=limpiarNombre(r.getString("nombre_publico"));return new Jugador(r.getLong("perfil_id"),n,iniciales(n),r.getString("posicion_preferida"),c,r.getBoolean("disponible_para_jugar"),r.getBoolean("acepta_invitaciones"),r.getBoolean("buscar_turnos"),r.getBoolean("buscar_torneos"),r.getBoolean("contacto_whatsapp"));}
 static String limpiarNombre(String n){return n==null||n.isBlank()?"Jugador":n.trim().replaceAll("\s+"," ");}
 static String iniciales(String n){String[] p=limpiarNombre(n).split("\s+");return(p[0].substring(0,1)+(p.length>1?p[p.length-1].substring(0,1):"")).toUpperCase(Locale.ROOT);}
 private record Consulta(String sql,List<Object> params,String order){}
}
