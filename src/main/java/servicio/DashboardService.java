package servicio;

import java.time.LocalDateTime;
import java.util.List;

import dao.DashboardDAO;
import dao.DashboardDAOMySQL;
import negocio.Reserva;
import negocio.ResumenDashboard;

public class DashboardService {
    private static final int LIMITE_PROXIMAS_RESERVAS = 8;
    private final DashboardDAO dashboardDAO;

    public DashboardService() { this(new DashboardDAOMySQL()); }

    public DashboardService(DashboardDAO dashboardDAO) {
        if (dashboardDAO == null) throw new IllegalArgumentException("El DAO del dashboard no puede ser nulo.");
        this.dashboardDAO = dashboardDAO;
    }

    public ResumenDashboard obtenerResumen() {
        return dashboardDAO.obtenerResumen(LocalDateTime.now());
    }

    public List<Reserva> listarProximasReservas() {
        return dashboardDAO.listarProximasReservas(LocalDateTime.now(), LIMITE_PROXIMAS_RESERVAS);
    }
}
