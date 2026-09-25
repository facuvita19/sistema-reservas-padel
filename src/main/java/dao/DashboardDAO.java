package dao;

import java.time.LocalDate;
import java.util.List;

import negocio.Reserva;
import negocio.ResumenDashboard;

public interface DashboardDAO {

    ResumenDashboard obtenerResumen(LocalDate fechaReferencia);

    List<Reserva> listarProximasReservas(
            LocalDate fechaReferencia,
            int limite);
}
