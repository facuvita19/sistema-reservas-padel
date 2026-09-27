package servicio;

import java.time.LocalDate;

import dao.EstadisticasDAO;
import dao.EstadisticasDAOMySQL;
import negocio.EstadisticasPadel;

public class EstadisticasService {

    private final EstadisticasDAO estadisticasDAO;

    public EstadisticasService() {
        this(new EstadisticasDAOMySQL());
    }

    public EstadisticasService(EstadisticasDAO estadisticasDAO) {
        if (estadisticasDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de estadísticas no puede ser nulo.");
        }
        this.estadisticasDAO = estadisticasDAO;
    }

    public EstadisticasPadel obtenerUltimosDoceMeses() {
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.minusMonths(11).withDayOfMonth(1);
        return obtener(desde, hasta);
    }

    public EstadisticasPadel obtener(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (desde.isAfter(LocalDate.now()) || hasta.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "El período no puede incluir fechas futuras.");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException(
                    "La fecha final no puede ser anterior a la inicial.");
        }
        return estadisticasDAO.obtenerEstadisticas(desde, hasta);
    }
}
