package dao;

import java.sql.Connection;
import java.util.List;

import negocio.TorneoPartidoSet;

public interface TorneoPartidoSetDAO {

    void guardar(Connection conexion, TorneoPartidoSet set);

    List<TorneoPartidoSet> listarPorPartido(long partidoId);

    List<TorneoPartidoSet> listarPorPartido(
            Connection conexion,
            long partidoId);

    void reemplazarPorPartido(
            Connection conexion,
            long partidoId,
            List<TorneoPartidoSet> sets);

    void eliminarPorPartido(Connection conexion, long partidoId);
}
