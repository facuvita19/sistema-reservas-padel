package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.BloqueoCanchaDAO;
import dao.CanchaDAO;
import dao.ReservaDAO;
import negocio.BloqueoCancha;
import negocio.Cancha;
import negocio.EstadoReserva;
import negocio.Reserva;

class ReservaEstadoServiceTest {

    private ReservaDAODoble reservaDAO;
    private ReservaService service;

    @BeforeEach
    void preparar() {
        reservaDAO = new ReservaDAODoble();
        service = new ReservaService(
                reservaDAO,
                new BloqueoDAOVacio(),
                new CanchaService(new CanchaDAOVacio())
        );
    }

    @Test
    void permitePendienteAConfirmada() {
        reservaDAO.reserva = crearReserva(EstadoReserva.PENDIENTE);

        service.cambiarEstado(1L, EstadoReserva.CONFIRMADA);

        assertTrue(reservaDAO.actualizarInvocado);
        assertEquals(EstadoReserva.CONFIRMADA, reservaDAO.nuevoEstado);
    }

    @Test
    void permitePendienteACancelada() {
        reservaDAO.reserva = crearReserva(EstadoReserva.PENDIENTE);
        service.cambiarEstado(1L, EstadoReserva.CANCELADA);
        assertEquals(EstadoReserva.CANCELADA, reservaDAO.nuevoEstado);
    }

    @Test
    void permiteConfirmadaACompletada() {
        reservaDAO.reserva = crearReserva(EstadoReserva.CONFIRMADA);
        service.cambiarEstado(1L, EstadoReserva.COMPLETADA);
        assertEquals(EstadoReserva.COMPLETADA, reservaDAO.nuevoEstado);
    }

    @Test
    void permiteConfirmadaAAusente() {
        reservaDAO.reserva = crearReserva(EstadoReserva.CONFIRMADA);
        service.cambiarEstado(1L, EstadoReserva.AUSENTE);
        assertEquals(EstadoReserva.AUSENTE, reservaDAO.nuevoEstado);
    }

    @Test
    void rechazaPendienteACompletada() {
        reservaDAO.reserva = crearReserva(EstadoReserva.PENDIENTE);
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(
                        1L, EstadoReserva.COMPLETADA));
        assertFalse(reservaDAO.actualizarInvocado);
    }

    @Test
    void rechazaMismoEstado() {
        reservaDAO.reserva = crearReserva(EstadoReserva.PENDIENTE);
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(
                        1L, EstadoReserva.PENDIENTE));
    }

    @Test
    void rechazaCambiosSobreEstadosFinales() {
        for (EstadoReserva estado : List.of(
                EstadoReserva.COMPLETADA,
                EstadoReserva.CANCELADA,
                EstadoReserva.AUSENTE)) {
            reservaDAO.reserva = crearReserva(estado);
            assertThrows(IllegalArgumentException.class,
                    () -> service.cambiarEstado(
                            1L, EstadoReserva.CONFIRMADA));
        }
    }

    @Test
    void rechazaIdEstadoNuloYReservaInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(0L, EstadoReserva.CONFIRMADA));
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(1L, null));
        reservaDAO.reserva = null;
        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstado(1L, EstadoReserva.CONFIRMADA));
    }

    private Reserva crearReserva(EstadoReserva estado) {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setEstado(estado);
        return reserva;
    }

    private static final class ReservaDAODoble implements ReservaDAO {
        private Reserva reserva;
        private boolean actualizarInvocado;
        private EstadoReserva nuevoEstado;

        @Override public void guardar(Reserva reserva) { }
        @Override public Reserva buscar(long id) { return reserva; }
        @Override public List<Reserva> listar() { return new ArrayList<>(); }
        @Override public List<Reserva> listarPorCliente(long id) {
            return new ArrayList<>();
        }
        @Override public List<Reserva> listarPorFecha(LocalDate fecha) {
            return new ArrayList<>();
        }
        @Override public void actualizarEstado(long id, EstadoReserva estado) {
            actualizarInvocado = true;
            nuevoEstado = estado;
        }
        @Override public boolean horarioOcupado(
                long canchaId, LocalDate fecha, LocalTime inicio,
                LocalTime fin, long reservaExcluidaId) {
            return false;
        }
    }

    private static final class BloqueoDAOVacio
            implements BloqueoCanchaDAO {
        @Override public void guardar(BloqueoCancha bloqueo) { }
        @Override public void eliminar(long id) { }
        @Override public BloqueoCancha buscar(long id) { return null; }
        @Override public List<BloqueoCancha> listar() {
            return new ArrayList<>();
        }
        @Override public List<BloqueoCancha> listarPorCancha(long id) {
            return new ArrayList<>();
        }
        @Override public boolean horarioBloqueado(
                long canchaId, LocalDate fecha, LocalTime inicio,
                LocalTime fin, long bloqueoExcluidoId) {
            return false;
        }
    }

    private static final class CanchaDAOVacio implements CanchaDAO {
        @Override public void guardar(Cancha cancha) { }
        @Override public void eliminar(long id) { }
        @Override public Cancha buscar(long id) { return null; }
        @Override public List<Cancha> listar() { return new ArrayList<>(); }
        @Override public boolean existeNombre(String nombre, long id) {
            return false;
        }
    }
}
