package servicio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import dao.BloqueoCanchaDAO;
import dao.ReservaDAO;
import dao.TorneoPartidoDAO;

class DisponibilidadCanchaServiceTest {

    @Test
    void rechazaCuandoExistePartidoSuperpuesto() {
        DisponibilidadCanchaService service = new DisponibilidadCanchaService(
                new ReservaDAOStub(false),
                new BloqueoDAOStub(false),
                new PartidoDAOStub(true));

        assertFalse(service.estaDisponibleParaReserva(
                1, LocalDate.now().plusDays(1),
                LocalTime.of(18, 0), LocalTime.of(19, 30), 0));
    }

    @Test
    void permiteCuandoNoHayOcupaciones() {
        DisponibilidadCanchaService service = new DisponibilidadCanchaService(
                new ReservaDAOStub(false),
                new BloqueoDAOStub(false),
                new PartidoDAOStub(false));

        assertTrue(service.estaDisponibleParaPartido(
                1, LocalDate.now().plusDays(1),
                LocalTime.of(18, 0), LocalTime.of(19, 30), 0));
    }

    private static final class ReservaDAOStub
            extends ReservaDAOAdapter {
        private final boolean ocupado;
        ReservaDAOStub(boolean ocupado) { this.ocupado = ocupado; }
        @Override public boolean horarioOcupado(long canchaId,
                LocalDate fecha, LocalTime inicio, LocalTime fin,
                long excluido) { return ocupado; }
    }

    private static final class BloqueoDAOStub
            extends BloqueoCanchaDAOAdapter {
        private final boolean ocupado;
        BloqueoDAOStub(boolean ocupado) { this.ocupado = ocupado; }
        @Override public boolean horarioBloqueado(long canchaId,
                LocalDate fecha, LocalTime inicio, LocalTime fin,
                long excluido) { return ocupado; }
    }

    private static final class PartidoDAOStub
            extends TorneoPartidoDAOAdapter {
        private final boolean ocupado;
        PartidoDAOStub(boolean ocupado) { this.ocupado = ocupado; }
        @Override public boolean horarioOcupado(long canchaId,
                LocalDate fecha, LocalTime inicio, LocalTime fin,
                long excluido) { return ocupado; }

        @Override
        public boolean existenPartidosDeGrupos(
                java.sql.Connection conexion,
                long categoriaId) {
            return false;
        }

        @Override
        public java.util.List<negocio.TorneoPartido> listarPorGrupo(
                long grupoId) {
            return java.util.List.of();
        }
}
}
