package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
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
import negocio.TipoCancha;

class ReservaServiceTest {

    private ReservaDAODoble reservaDAO;
    private BloqueoDAODoble bloqueoDAO;
    private CanchaDAODoble canchaDAO;
    private ReservaService service;
    private Cancha cancha;

    @BeforeEach
    void preparar() {
        reservaDAO = new ReservaDAODoble();
        bloqueoDAO = new BloqueoDAODoble();
        canchaDAO = new CanchaDAODoble();

        cancha = crearCancha();
        canchaDAO.cancha = cancha;

        service = new ReservaService(
                reservaDAO,
                bloqueoDAO,
                new CanchaService(canchaDAO)
        );
    }

    @Test
    void guardaReservaValidaCalculandoHoraYPrecio() {
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.MONDAY),
                LocalTime.of(18, 0)
        );
        reserva.setComentarios("  Partido   amistoso  ");

        service.guardar(reserva);

        assertTrue(reservaDAO.guardarInvocado);
        assertSame(reserva, reservaDAO.ultimaGuardada);
        assertEquals(LocalTime.of(19, 30), reserva.getHoraFin());
        assertEquals(new BigDecimal("25000.00"), reserva.getPrecioTotal());
        assertEquals("Partido amistoso", reserva.getComentarios());
    }

    @Test
    void asignaEstadoPendienteCuandoEsNulo() {
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.MONDAY),
                LocalTime.of(18, 0)
        );
        reserva.setEstado(null);

        service.guardar(reserva);

        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void rechazaReservaNula() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(null)
        );
        assertFalse(reservaDAO.guardarInvocado);
    }

    @Test
    void rechazaIdentificadoresInvalidos() {
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.MONDAY),
                LocalTime.of(18, 0)
        );
        reserva.setClienteId(0L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva)
        );
    }

    @Test
    void rechazaCantidadDeJugadoresInvalida() {
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.MONDAY),
                LocalTime.of(18, 0)
        );
        reserva.setCantidadJugadores(9);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva)
        );
    }

    @Test
    void rechazaDiaSinDisponibilidad() {
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.SUNDAY),
                LocalTime.of(18, 0)
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva)
        );

        assertTrue(error.getMessage().contains("disponible"));
    }

    @Test
    void rechazaHorarioFueraDeJornada() {
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.MONDAY),
                LocalTime.of(23, 0)
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva)
        );

        assertTrue(error.getMessage().contains("jornada"));
    }

    @Test
    void rechazaSuperposicionConOtraReserva() {
        reservaDAO.ocupado = true;
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.MONDAY),
                LocalTime.of(18, 0)
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva)
        );

        assertTrue(error.getMessage().contains("superpone"));
        assertFalse(reservaDAO.guardarInvocado);
    }

    @Test
    void rechazaHorarioBloqueado() {
        bloqueoDAO.bloqueado = true;
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.MONDAY),
                LocalTime.of(18, 0)
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva)
        );

        assertTrue(error.getMessage().contains("bloqueada"));
    }

    @Test
    void listaSolamenteHorariosLibres() {
        LocalDate fecha = proximo(DayOfWeek.MONDAY);
        reservaDAO.iniciosOcupados.add(LocalTime.of(11, 0));
        bloqueoDAO.iniciosBloqueados.add(LocalTime.of(14, 0));

        List<LocalTime> horarios = service.listarHorariosDisponibles(
                cancha.getId(),
                fecha,
                0L
        );

        assertTrue(horarios.contains(LocalTime.of(8, 0)));
        assertFalse(horarios.contains(LocalTime.of(11, 0)));
        assertFalse(horarios.contains(LocalTime.of(14, 0)));
        assertTrue(horarios.contains(LocalTime.of(21, 30)));
        assertEquals(8, horarios.size());
    }

    @Test
    void devuelveListaVaciaParaFechaPasadaODiaNoDisponible() {
        assertTrue(service.listarHorariosDisponibles(
                cancha.getId(),
                LocalDate.now().minusDays(1),
                0L
        ).isEmpty());

        assertTrue(service.listarHorariosDisponibles(
                cancha.getId(),
                proximo(DayOfWeek.SUNDAY),
                0L
        ).isEmpty());
    }

    @Test
    void buscaListaPorClienteYFecha() {
        Reserva reserva = crearReserva(
                proximo(DayOfWeek.MONDAY),
                LocalTime.of(18, 0)
        );
        reservaDAO.buscada = reserva;
        reservaDAO.reservas.add(reserva);

        assertSame(reserva, service.buscar(1L));
        assertEquals(1, service.listar().size());
        assertEquals(1, service.listarPorCliente(20L).size());
        assertEquals(1, service.listarPorFecha(reserva.getFecha()).size());
        assertNull(service.buscar(0L));
    }

    private Cancha crearCancha() {
        Cancha resultado = new Cancha();
        resultado.setId(10L);
        resultado.setNombre("Cancha Central");
        resultado.setTipo(TipoCancha.CUBIERTA);
        resultado.setHoraApertura(LocalTime.of(8, 0));
        resultado.setHoraCierre(LocalTime.of(23, 0));
        resultado.setDuracionReserva(90);
        resultado.setPrecio(new BigDecimal("25000"));
        resultado.setDiasDisponibles(EnumSet.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        ));
        resultado.setActivo(true);
        return resultado;
    }

    private Reserva crearReserva(LocalDate fecha, LocalTime inicio) {
        Reserva reserva = new Reserva();
        reserva.setClienteId(20L);
        reserva.setCanchaId(cancha.getId());
        reserva.setUsuarioId(30L);
        reserva.setFecha(fecha);
        reserva.setHoraInicio(inicio);
        reserva.setCantidadJugadores(4);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        return reserva;
    }

    private LocalDate proximo(DayOfWeek dia) {
        return LocalDate.now().with(TemporalAdjusters.next(dia));
    }

    private static final class ReservaDAODoble implements ReservaDAO {
        private boolean guardarInvocado;
        private boolean ocupado;
        private Reserva ultimaGuardada;
        private Reserva buscada;
        private EstadoReserva estadoActualizado;
        private final List<Reserva> reservas = new ArrayList<>();
        private final List<LocalTime> iniciosOcupados = new ArrayList<>();

        @Override
        public void guardar(Reserva reserva) {
            guardarInvocado = true;
            ultimaGuardada = reserva;
        }

        @Override
        public Reserva buscar(long id) {
            return buscada;
        }

        @Override
        public List<Reserva> listar() {
            return new ArrayList<>(reservas);
        }

        @Override
        public List<Reserva> listarPorCliente(long clienteId) {
            return new ArrayList<>(reservas);
        }

        @Override
        public List<Reserva> listarPorFecha(LocalDate fecha) {
            return new ArrayList<>(reservas);
        }

        @Override
        public void actualizarEstado(long id, EstadoReserva estado) {
            estadoActualizado = estado;
        }

        @Override
        public boolean horarioOcupado(
                long canchaId,
                LocalDate fecha,
                LocalTime inicio,
                LocalTime fin,
                long reservaExcluidaId) {
            return ocupado || iniciosOcupados.contains(inicio);
        }
    }

    private static final class BloqueoDAODoble
            implements BloqueoCanchaDAO {
        private boolean bloqueado;
        private final List<LocalTime> iniciosBloqueados =
                new ArrayList<>();

        @Override public void guardar(BloqueoCancha bloqueo) { }
        @Override public void eliminar(long id) { }
        @Override public BloqueoCancha buscar(long id) { return null; }
        @Override public List<BloqueoCancha> listar() {
            return new ArrayList<>();
        }
        @Override public List<BloqueoCancha> listarPorCancha(long id) {
            return new ArrayList<>();
        }
        @Override
        public boolean horarioBloqueado(
                long canchaId,
                LocalDate fecha,
                LocalTime inicio,
                LocalTime fin,
                long bloqueoExcluidoId) {
            return bloqueado || iniciosBloqueados.contains(inicio);
        }
    }

    private static final class CanchaDAODoble implements CanchaDAO {
        private Cancha cancha;
        @Override public void guardar(Cancha cancha) { this.cancha = cancha; }
        @Override public void eliminar(long id) { }
        @Override public Cancha buscar(long id) { return cancha; }
        @Override public List<Cancha> listar() {
            return cancha == null
                    ? new ArrayList<>()
                    : new ArrayList<>(List.of(cancha));
        }
        @Override public boolean existeNombre(String nombre, long id) {
            return false;
        }
    }
}
