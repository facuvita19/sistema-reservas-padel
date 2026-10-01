package servicio;
import java.time.*; import java.util.*; import dao.BloqueoCanchaDAO; import negocio.BloqueoCancha;
abstract class BloqueoCanchaDAOAdapter implements BloqueoCanchaDAO {
 public void guardar(BloqueoCancha b){throw new UnsupportedOperationException();}
 public void eliminar(long id){throw new UnsupportedOperationException();}
 public BloqueoCancha buscar(long id){throw new UnsupportedOperationException();}
 public List<BloqueoCancha> listar(){throw new UnsupportedOperationException();}
 public List<BloqueoCancha> listarPorCancha(long id){throw new UnsupportedOperationException();}
 public boolean horarioBloqueado(long c,LocalDate f,LocalTime i,LocalTime n,long x){return false;}
}
