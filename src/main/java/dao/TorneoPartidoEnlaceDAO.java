package dao;

import java.sql.Connection;
import java.util.List;
import negocio.TorneoPartidoEnlace;

public interface TorneoPartidoEnlaceDAO {
    void guardar(Connection conexion, TorneoPartidoEnlace enlace);
    List<TorneoPartidoEnlace> listarPorOrigen(
            Connection conexion, long partidoOrigenId);
}
