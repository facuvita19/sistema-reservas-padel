package dao;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import negocio.FaseTorneo;
import negocio.TorneoPartido;

public interface TorneoPartidoDAO {

    void guardar(TorneoPartido partido);

    void guardar(Connection conexion, TorneoPartido partido);

    TorneoPartido buscar(long id);

    TorneoPartido buscar(Connection conexion, long id);

    TorneoPartido buscarParaActualizar(Connection conexion, long id);

    List<TorneoPartido> listarPorCategoria(long categoriaId);

    List<TorneoPartido> listarPorFase(
            long categoriaId,
            FaseTorneo fase);

    List<TorneoPartido> listarPorFecha(LocalDate fecha);

    boolean existeCuadroPorCategoria(long categoriaId);

    void eliminarCuadroPorCategoria(
            Connection conexion,
            long categoriaId);

    boolean horarioOcupado(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long partidoExcluidoId);

    boolean parejaOcupadaEnHorario(
            long inscripcionId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long partidoExcluidoId);
}
