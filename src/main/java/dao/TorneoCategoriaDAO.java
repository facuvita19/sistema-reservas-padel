package dao;

import java.sql.Connection;
import java.util.List;

import negocio.TorneoCategoria;

public interface TorneoCategoriaDAO {

    void guardar(TorneoCategoria categoria);

    void guardar(
            Connection conexion,
            TorneoCategoria categoria);

    TorneoCategoria buscar(long id);

    TorneoCategoria buscar(
            Connection conexion,
            long id);

    List<TorneoCategoria> listarPorTorneo(long torneoId);

    List<TorneoCategoria> listarActivasPorTorneo(
            long torneoId);

    boolean existeNombreYRama(
            long torneoId,
            String nombre,
            negocio.RamaTorneo rama,
            long categoriaExcluidaId);

    void desactivar(long id);
}
