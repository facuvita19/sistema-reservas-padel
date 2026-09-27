package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import dao.DashboardDAO;
import negocio.Reserva;
import negocio.ResumenDashboard;

class DashboardServiceTest {
    @Test
    void delegaResumenYProximasReservas() {
        DashboardDAODoble dao = new DashboardDAODoble();
        ResumenDashboard resumen = new ResumenDashboard();
        Reserva reserva = new Reserva();
        dao.resumen = resumen;
        dao.reservas = List.of(reserva);
        DashboardService service = new DashboardService(dao);
        assertSame(resumen, service.obtenerResumen());
        assertEquals(List.of(reserva), service.listarProximasReservas());
        assertEquals(8, dao.limite);
    }

    private static final class DashboardDAODoble implements DashboardDAO {
        private ResumenDashboard resumen;
        private List<Reserva> reservas;
        private int limite;
        @Override public ResumenDashboard obtenerResumen(LocalDateTime momento) { return resumen; }
        @Override public List<Reserva> listarProximasReservas(LocalDateTime momento, int valor) {
            limite = valor;
            return reservas;
        }
    }
}
