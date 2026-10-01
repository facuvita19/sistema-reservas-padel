package dao;

import java.sql.Connection;
import java.util.List;
import negocio.CorreccionResultadoTorneo;

public interface CorreccionResultadoTorneoDAO {
    void guardar(Connection conexion, CorreccionResultadoTorneo correccion);
    List<CorreccionResultadoTorneo> listarPorPartido(long partidoId);
}
