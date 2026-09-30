package dao;

import java.sql.Connection;
import java.util.List;

import negocio.TorneoInscripcionJugador;

public interface TorneoInscripcionJugadorDAO {

    void guardar(TorneoInscripcionJugador jugador);

    void guardar(
            Connection conexion,
            TorneoInscripcionJugador jugador);

    TorneoInscripcionJugador buscar(long id);

    TorneoInscripcionJugador buscar(
            Connection conexion,
            long id);

    List<TorneoInscripcionJugador> listarPorInscripcion(
            long inscripcionId);

    List<TorneoInscripcionJugador> listarPorInscripcion(
            Connection conexion,
            long inscripcionId);

    List<TorneoInscripcionJugador> buscarPorTelefonoNormalizado(
            Connection conexion,
            String telefonoNormalizado);

    void eliminarPorInscripcion(
            Connection conexion,
            long inscripcionId);
}
