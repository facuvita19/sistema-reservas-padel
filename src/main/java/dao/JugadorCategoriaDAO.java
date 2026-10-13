package dao;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import negocio.EstadoJugadorCategoria;
import negocio.JugadorCategoria;
public interface JugadorCategoriaDAO {
 void guardar(Connection c,JugadorCategoria v);
 JugadorCategoria buscarVigente(Connection c,long perfilId);
 List<JugadorCategoria> listarHistorial(Connection c,long perfilId);
 void finalizarVigente(Connection c,long perfilId,LocalDate hasta);
 void cerrarVigente(Connection c,long perfilId,LocalDate hasta,EstadoJugadorCategoria estado,String motivo,String observaciones,Long usuarioId);
}
