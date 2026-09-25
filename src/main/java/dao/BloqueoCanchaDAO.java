package dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import negocio.BloqueoCancha;

public interface BloqueoCanchaDAO {

    void guardar(BloqueoCancha bloqueo);

    void eliminar(long id);

    BloqueoCancha buscar(long id);

    List<BloqueoCancha> listar();

    List<BloqueoCancha> listarPorCancha(long canchaId);

    boolean horarioBloqueado(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long bloqueoExcluidoId);
}
