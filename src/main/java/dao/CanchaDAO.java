package dao;

import java.util.List;

import negocio.Cancha;

public interface CanchaDAO {

    void guardar(Cancha cancha);

    void eliminar(long id);

    Cancha buscar(long id);

    List<Cancha> listar();

    default List<Cancha> listarTodas() {
        return listar();
    }

    default void reactivar(long id) {
        throw new UnsupportedOperationException(
                "La reactivación no está disponible.");
    }

    default void subirOrden(long id) {
        throw new UnsupportedOperationException(
                "El orden visual no está disponible.");
    }

    default void bajarOrden(long id) {
        throw new UnsupportedOperationException(
                "El orden visual no está disponible.");
    }

    default void eliminarDefinitivamente(long id) {
        throw new UnsupportedOperationException(
                "La eliminación definitiva no está disponible.");
    }

    default boolean existeNombreInactivo(
            String nombre,
            long canchaExcluidaId) {
        return false;
    }

    boolean existeNombre(
            String nombre,
            long canchaExcluidaId);
}
