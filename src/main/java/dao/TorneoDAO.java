package dao;

import java.sql.Connection;
import java.util.List;

import negocio.EstadoTorneo;
import negocio.Torneo;

public interface TorneoDAO {

    void guardar(Torneo torneo);

    void guardar(Connection conexion, Torneo torneo);

    Torneo buscar(long id);

    Torneo buscar(Connection conexion, long id);

    List<Torneo> listar();

    List<Torneo> listarActivos();

    List<Torneo> listarPorEstado(EstadoTorneo estado);

    void desactivar(long id);
}
