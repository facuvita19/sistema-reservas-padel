package dao;
import java.sql.Connection;
import negocio.PerfilJugador;
public interface PerfilJugadorDAO {
 void guardar(Connection c,PerfilJugador p);
 PerfilJugador buscar(Connection c,long id);
 PerfilJugador buscarPorCliente(Connection c,long clienteId);
}
