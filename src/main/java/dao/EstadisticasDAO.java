package dao;

import java.time.LocalDate;

import negocio.EstadisticasPadel;

public interface EstadisticasDAO {
    EstadisticasPadel obtenerEstadisticas(
            LocalDate fechaDesde,
            LocalDate fechaHasta);
}
