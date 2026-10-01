package servicio;

import java.time.LocalDate;
import java.time.LocalTime;

import dao.BloqueoCanchaDAO;
import dao.BloqueoCanchaDAOMySQL;
import dao.ReservaDAO;
import dao.ReservaDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;

public class DisponibilidadCanchaService {

    private final ReservaDAO reservaDAO;
    private final BloqueoCanchaDAO bloqueoDAO;
    private final TorneoPartidoDAO partidoDAO;

    public DisponibilidadCanchaService() {
        this(new ReservaDAOMySQL(),
                new BloqueoCanchaDAOMySQL(),
                new TorneoPartidoDAOMySQL());
    }

    public DisponibilidadCanchaService(
            ReservaDAO reservaDAO,
            BloqueoCanchaDAO bloqueoDAO,
            TorneoPartidoDAO partidoDAO) {
        if (reservaDAO == null || bloqueoDAO == null
                || partidoDAO == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de disponibilidad no pueden ser nulas.");
        }
        this.reservaDAO = reservaDAO;
        this.bloqueoDAO = bloqueoDAO;
        this.partidoDAO = partidoDAO;
    }

    public boolean estaDisponibleParaReserva(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long reservaExcluidaId) {
        validarHorario(canchaId, fecha, horaInicio, horaFin);
        if (reservaExcluidaId < 0) {
            throw new IllegalArgumentException(
                    "La reserva excluida no es valida.");
        }
        return !reservaDAO.horarioOcupado(
                    canchaId, fecha, horaInicio, horaFin,
                    reservaExcluidaId)
                && !bloqueoDAO.horarioBloqueado(
                    canchaId, fecha, horaInicio, horaFin, 0L)
                && !partidoDAO.horarioOcupado(
                    canchaId, fecha, horaInicio, horaFin, 0L);
    }

    public boolean estaDisponibleParaPartido(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long partidoExcluidoId) {
        validarHorario(canchaId, fecha, horaInicio, horaFin);
        if (partidoExcluidoId < 0) {
            throw new IllegalArgumentException(
                    "El partido excluido no es valido.");
        }
        return !reservaDAO.horarioOcupado(
                    canchaId, fecha, horaInicio, horaFin, 0L)
                && !bloqueoDAO.horarioBloqueado(
                    canchaId, fecha, horaInicio, horaFin, 0L)
                && !partidoDAO.horarioOcupado(
                    canchaId, fecha, horaInicio, horaFin,
                    partidoExcluidoId);
    }

    private void validarHorario(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {
        if (canchaId <= 0 || fecha == null || horaInicio == null
                || horaFin == null || !horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                    "La cancha, fecha y horario deben ser validos.");
        }
    }
}
