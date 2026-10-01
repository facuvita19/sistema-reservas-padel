package servicio;
import java.time.*; import java.util.*; import dao.ReservaDAO; import negocio.*;
abstract class ReservaDAOAdapter implements ReservaDAO {
 public void guardar(Reserva r){throw new UnsupportedOperationException();}
 public Reserva buscar(long id){throw new UnsupportedOperationException();}
 public List<Reserva> listar(){throw new UnsupportedOperationException();}
 public List<Reserva> listarPorCliente(long id){throw new UnsupportedOperationException();}
 public List<Reserva> listarPorFecha(LocalDate f){throw new UnsupportedOperationException();}
 public void actualizarEstado(long id,EstadoReserva e){throw new UnsupportedOperationException();}
 public boolean horarioOcupado(long c,LocalDate f,LocalTime i,LocalTime n,long x){return false;}
}
