package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.BloqueoCanchaDAO;
import dao.CanchaDAO;
import dao.ConfiguracionComplejoDAO;
import dao.ReservaDAO;
import negocio.BloqueoCancha;
import negocio.Cancha;
import negocio.ConfiguracionComplejo;
import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.TipoCancha;

class ReservaVencimientoServiceTest {

    private ReservaDAODoble reservaDAO;
    private ReservaService service;
    private Cancha cancha;

    @BeforeEach
    void preparar() {
        reservaDAO = new ReservaDAODoble();
        cancha = crearCancha();

        CanchaDAODoble canchaDAO = new CanchaDAODoble();
        canchaDAO.cancha = cancha;

        ConfiguracionComplejo configuracion = new ConfiguracionComplejo();
        configuracion.setMinutosReservaPendiente(20);

        ConfiguracionDAODoble configuracionDAO =
                new ConfiguracionDAODoble();
        configuracionDAO.configuracion = configuracion;

        service = new ReservaService(
                reservaDAO,
                new BloqueoDAODoble(),
                new CanchaService(canchaDAO),
                new ConfiguracionComplejoService(configuracionDAO));
    }

    @Test
    void creaPendienteWebConVencimientoConfigurado() {
        Reserva reserva = crearReserva();
        LocalDateTime antes = LocalDateTime.now().plusMinutes(19);

        Reserva resultado = service.guardarPendienteWeb(reserva);

        assertEquals(EstadoReserva.PENDIENTE, resultado.getEstado());
        assertNotNull(resultado.getFechaVencimiento());
        assertTrue(resultado.getFechaVencimiento().isAfter(antes));
        assertNull(resultado.getFechaExpiracion());
        assertTrue(reservaDAO.guardarInvocado);
    }

    @Test
    void reservaConfirmadaNoConservaVencimiento() {
        Reserva reserva = crearReserva();
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaVencimiento(LocalDateTime.now().plusMinutes(15));

        service.guardar(reserva);

        assertNull(reserva.getFechaVencimiento());
        assertNull(reserva.getFechaExpiracion());
    }

    @Test
    void ejecutaExpiracionMedianteDao() {
        reservaDAO.cantidadExpiradas = 3;

        int cantidad = service.expirarReservasPendientes();

        assertEquals(3, cantidad);
        assertNotNull(reservaDAO.momentoExpiracion);
    }

    @Test
    void estadoExpiradaEsFinal() {
        assertTrue(EstadoReserva.EXPIRADA.esFinal());
    }

    private Reserva crearReserva() {
        Reserva reserva = new Reserva();
        reserva.setClienteId(20L);
        reserva.setCanchaId(cancha.getId());
        reserva.setUsuarioId(30L);
        reserva.setFecha(LocalDate.now().with(
                TemporalAdjusters.next(DayOfWeek.MONDAY)));
        reserva.setHoraInicio(LocalTime.of(18, 0));
        reserva.setCantidadJugadores(4);
        return reserva;
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
                DayOfWeek.SATURDAY));
        resultado.setActivo(true);
        return resultado;
    }

    private static final class ReservaDAODoble implements ReservaDAO {
        private boolean guardarInvocado;
        private int cantidadExpiradas;
        private LocalDateTime momentoExpiracion;

        @Override public void guardar(Reserva reserva) { guardarInvocado = true; }
        @Override public Reserva buscar(long id) { return null; }
        @Override public List<Reserva> listar() { return new ArrayList<>(); }
        @Override public List<Reserva> listarPorCliente(long id) { return new ArrayList<>(); }
        @Override public List<Reserva> listarPorFecha(LocalDate fecha) { return new ArrayList<>(); }
        @Override public void actualizarEstado(long id, EstadoReserva estado) { }
        @Override public boolean horarioOcupado(long canchaId, LocalDate fecha,
                LocalTime inicio, LocalTime fin, long excluida) { return false; }
        @Override
        public int expirarPendientesVencidas(LocalDateTime momento) {
            momentoExpiracion = momento;
            return cantidadExpiradas;
        }
    }

    private static final class BloqueoDAODoble implements BloqueoCanchaDAO {
        @Override public void guardar(BloqueoCancha bloqueo) { }
        @Override public void eliminar(long id) { }
        @Override public BloqueoCancha buscar(long id) { return null; }
        @Override public List<BloqueoCancha> listar() { return new ArrayList<>(); }
        @Override public List<BloqueoCancha> listarPorCancha(long id) { return new ArrayList<>(); }
        @Override public boolean horarioBloqueado(long canchaId, LocalDate fecha,
                LocalTime inicio, LocalTime fin, long excluido) { return false; }
    }

    private static final class CanchaDAODoble implements CanchaDAO {
        private Cancha cancha;
        @Override public void guardar(Cancha valor) { cancha = valor; }
        @Override public void eliminar(long id) { }
        @Override public Cancha buscar(long id) { return cancha; }
        @Override public List<Cancha> listar() { return List.of(cancha); }
        @Override public boolean existeNombre(String nombre, long id) { return false; }
    }

    private static final class ConfiguracionDAODoble
            implements ConfiguracionComplejoDAO {
        private ConfiguracionComplejo configuracion;
        @Override public ConfiguracionComplejo obtener() { return configuracion; }
        @Override public void guardar(ConfiguracionComplejo valor) {
            configuracion = valor;
        }
    }
}
