package dao;

import java.sql.Connection;
import java.util.List;

import negocio.EstadoInscripcionTorneo;
import negocio.TorneoInscripcion;

public interface TorneoInscripcionDAO {

    void guardar(TorneoInscripcion inscripcion);

    void guardar(
            Connection conexion,
            TorneoInscripcion inscripcion);

    TorneoInscripcion buscar(long id);

    TorneoInscripcion buscar(
            Connection conexion,
            long id);

    List<TorneoInscripcion> listarPorCategoria(
            long torneoCategoriaId);

    List<TorneoInscripcion> listarPorEstado(
            EstadoInscripcionTorneo estado);

    List<TorneoInscripcion> listarPorCliente(
            long clienteId);

    int contarPorCategoriaYEstados(
            Connection conexion,
            long torneoCategoriaId,
            List<EstadoInscripcionTorneo> estados);

    boolean clienteParticipaEnCategoria(
            Connection conexion,
            long torneoCategoriaId,
            long clienteId,
            Long inscripcionExcluidaId);
}
