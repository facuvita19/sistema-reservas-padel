package dao;

import java.util.List;

import negocio.Cancha;

public interface CanchaDAO {

    void guardar(Cancha cancha);

    void eliminar(long id);

    Cancha buscar(long id);

    List<Cancha> listar();

    boolean existeNombre(
            String nombre,
            long canchaExcluidaId);
}
