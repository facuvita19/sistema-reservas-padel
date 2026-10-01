package servicio;
import java.sql.*; import java.time.*; import java.util.*; import dao.TorneoPartidoDAO; import negocio.*;
abstract class TorneoPartidoDAOAdapter implements TorneoPartidoDAO {
 public void guardar(TorneoPartido p){throw new UnsupportedOperationException();}
 public void guardar(Connection c,TorneoPartido p){throw new UnsupportedOperationException();}
 public TorneoPartido buscar(long id){throw new UnsupportedOperationException();}
 public TorneoPartido buscar(Connection c,long id){throw new UnsupportedOperationException();}
 public TorneoPartido buscarParaActualizar(Connection c,long id){throw new UnsupportedOperationException();}
 public List<TorneoPartido> listarPorCategoria(long id){throw new UnsupportedOperationException();}
 public List<TorneoPartido> listarPorFase(long id,FaseTorneo f){throw new UnsupportedOperationException();}
 public List<TorneoPartido> listarPorFecha(LocalDate f){throw new UnsupportedOperationException();}
 public boolean existeCuadroPorCategoria(long id){return false;}
 public void eliminarCuadroPorCategoria(Connection c,long id){throw new UnsupportedOperationException();}
 public boolean horarioOcupado(long c,LocalDate f,LocalTime i,LocalTime n,long x){return false;}
 public boolean parejaOcupadaEnHorario(long p,LocalDate f,LocalTime i,LocalTime n,long x){return false;}
}
