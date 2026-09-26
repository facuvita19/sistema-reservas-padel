package servicio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

class BloqueoCanchaServiceTest {

	private BloqueoDAODoble bloqueoDAO;
	private CanchaDAODoble canchaDAO;
	private ReservaDAODoble reservaDAO;
	private CanchaService canchaService;
	private BloqueoCanchaService service;

	@BeforeEach
	void preparar() {
		bloqueoDAO = new BloqueoDAODoble();
		canchaDAO = new CanchaDAODoble();
		reservaDAO = new ReservaDAODoble();

		canchaDAO.cancha = crearCancha();
		canchaService = new CanchaService(canchaDAO);

		service = new BloqueoCanchaService(bloqueoDAO, canchaService, reservaDAO);
	}

	@Test
	void guardaYNormalizaBloqueoValido() {
		BloqueoCancha bloqueo = crearBloqueo();
		bloqueo.setMotivo("  Mantenimiento   de luces  ");

		service.guardar(bloqueo);

		assertTrue(bloqueoDAO.guardarInvocado);
		assertSame(bloqueo, bloqueoDAO.ultimoGuardado);
		assertEquals("Mantenimiento de luces", bloqueo.getMotivo());
	}

	@Test
	void rechazaBloqueoNulo() {
		assertThrows(IllegalArgumentException.class, () -> service.guardar(null));
		assertFalse(bloqueoDAO.guardarInvocado);
	}

	@Test
	void rechazaFechaPasada() {
		BloqueoCancha bloqueo = crearBloqueo();
		bloqueo.setFecha(LocalDate.now().minusDays(1));

		assertThrows(IllegalArgumentException.class, () -> service.guardar(bloqueo));
	}

	@Test
	void rechazaHorarioInvertido() {
		BloqueoCancha bloqueo = crearBloqueo();
		bloqueo.setHoraInicio(LocalTime.of(20, 0));
		bloqueo.setHoraFin(LocalTime.of(19, 0));

		assertThrows(IllegalArgumentException.class, () -> service.guardar(bloqueo));
	}

	@Test
	void rechazaMotivoVacio() {
		BloqueoCancha bloqueo = crearBloqueo();
		bloqueo.setMotivo("   ");

		assertThrows(IllegalArgumentException.class, () -> service.guardar(bloqueo));
	}

	@Test
	void rechazaDiaNoDisponible() {
		BloqueoCancha bloqueo = crearBloqueo();
		bloqueo.setFecha(proximo(DayOfWeek.SUNDAY));

		assertThrows(IllegalArgumentException.class, () -> service.guardar(bloqueo));
	}

	@Test
	void rechazaBloqueoFueraDeJornada() {
		BloqueoCancha bloqueo = crearBloqueo();
		bloqueo.setHoraInicio(LocalTime.of(7, 0));
		bloqueo.setHoraFin(LocalTime.of(8, 0));

		assertThrows(IllegalArgumentException.class, () -> service.guardar(bloqueo));
	}

	@Test
	void rechazaSuperposicionConOtroBloqueo() {
		bloqueoDAO.bloqueado = true;

		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> service.guardar(crearBloqueo()));

		assertTrue(error.getMessage().contains("superpuesto"));
	}

	@Test
	void buscaListaPorCanchaYElimina() {
		BloqueoCancha bloqueo = crearBloqueo();
		bloqueoDAO.buscado = bloqueo;
		bloqueoDAO.bloqueos.add(bloqueo);

		assertSame(bloqueo, service.buscar(1L));
		assertEquals(1, service.listar().size());
		assertEquals(1, service.listarPorCancha(10L).size());

		service.eliminar(1L);

		assertEquals(1L, bloqueoDAO.idEliminado);
		assertNull(service.buscar(0L));
	}

	@Test
	void rechazaBloqueoSuperpuestoConReservaActiva() {
		BloqueoCancha bloqueo = crearBloqueo(LocalTime.of(20, 30), LocalTime.of(22, 0));

		Reserva reserva = crearReserva(bloqueo.getFecha(), LocalTime.of(20, 0), LocalTime.of(21, 30),
				EstadoReserva.CONFIRMADA);

		reservaDAO.reservas.add(reserva);

		IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.guardar(bloqueo));

		assertTrue(error.getMessage().contains("reserva activa superpuesta"));
	}

	@Test
	void permiteBloqueoSiLaReservaEstaCancelada() {
		BloqueoCancha bloqueo = crearBloqueo(LocalTime.of(20, 30), LocalTime.of(22, 0));

		Reserva reserva = crearReserva(bloqueo.getFecha(), LocalTime.of(20, 0), LocalTime.of(21, 30),
				EstadoReserva.CANCELADA);

		reservaDAO.reservas.add(reserva);

		assertDoesNotThrow(() -> service.guardar(bloqueo));
		assertTrue(bloqueoDAO.guardarInvocado);
	}

	@Test
	void permiteBloqueoDespuesDeUnaReserva() {
		BloqueoCancha bloqueo = crearBloqueo(LocalTime.of(21, 30), LocalTime.of(22, 30));

		Reserva reserva = crearReserva(bloqueo.getFecha(), LocalTime.of(20, 0), LocalTime.of(21, 30),
				EstadoReserva.CONFIRMADA);

		reservaDAO.reservas.add(reserva);

		assertDoesNotThrow(() -> service.guardar(bloqueo));
		assertTrue(bloqueoDAO.guardarInvocado);
	}

	private BloqueoCancha crearBloqueo() {
		return crearBloqueo(LocalTime.of(18, 0), LocalTime.of(19, 30));
	}

	private BloqueoCancha crearBloqueo(LocalTime horaInicio, LocalTime horaFin) {

		BloqueoCancha bloqueo = new BloqueoCancha();
		bloqueo.setId(1L);
		bloqueo.setCanchaId(10L);
		bloqueo.setFecha(proximo(DayOfWeek.MONDAY));
		bloqueo.setHoraInicio(horaInicio);
		bloqueo.setHoraFin(horaFin);
		bloqueo.setMotivo("Mantenimiento");
		return bloqueo;
	}

	private Cancha crearCancha() {
		Cancha cancha = new Cancha();
		cancha.setId(10L);
		cancha.setNombre("Cancha Central");
		cancha.setTipo(TipoCancha.CUBIERTA);
		cancha.setHoraApertura(LocalTime.of(8, 0));
		cancha.setHoraCierre(LocalTime.of(23, 0));
		cancha.setDuracionReserva(90);
		cancha.setPrecio(new BigDecimal("25000"));
		cancha.setDiasDisponibles(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
				DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));
		cancha.setActivo(true);
		return cancha;
	}

	private LocalDate proximo(DayOfWeek dia) {
		return LocalDate.now().with(TemporalAdjusters.next(dia));
	}

	private Reserva crearReserva(LocalDate fecha, LocalTime inicio, LocalTime fin, EstadoReserva estado) {

		Reserva reserva = new Reserva();
		reserva.setId(50L);
		reserva.setCanchaId(10L);
		reserva.setClienteId(20L);
		reserva.setUsuarioId(1L);
		reserva.setFecha(fecha);
		reserva.setHoraInicio(inicio);
		reserva.setHoraFin(fin);
		reserva.setEstado(estado);
		reserva.setPrecioTotal(new BigDecimal("40000.00"));
		return reserva;
	}

	private static final class BloqueoDAODoble implements BloqueoCanchaDAO {

		private boolean guardarInvocado;
		private boolean bloqueado;
		private long idEliminado;
		private BloqueoCancha ultimoGuardado;
		private BloqueoCancha buscado;
		private final List<BloqueoCancha> bloqueos = new ArrayList<>();

		@Override
		public void guardar(BloqueoCancha bloqueo) {
			guardarInvocado = true;
			ultimoGuardado = bloqueo;
		}

		@Override
		public void eliminar(long id) {
			idEliminado = id;
		}

		@Override
		public BloqueoCancha buscar(long id) {
			return buscado;
		}

		@Override
		public List<BloqueoCancha> listar() {
			return new ArrayList<>(bloqueos);
		}

		@Override
		public List<BloqueoCancha> listarPorCancha(long id) {
			return new ArrayList<>(bloqueos);
		}

		@Override
		public boolean horarioBloqueado(long canchaId, LocalDate fecha, LocalTime inicio, LocalTime fin,
				long bloqueoExcluidoId) {

			return bloqueado;
		}
	}

	private static final class ReservaDAODoble implements ReservaDAO {

		private final List<Reserva> reservas = new ArrayList<>();

		@Override
		public void guardar(Reserva reserva) {
		}

		@Override
		public Reserva buscar(long id) {
			return reservas.stream().filter(reserva -> reserva.getId() == id).findFirst().orElse(null);
		}

		@Override
		public List<Reserva> listar() {
			return new ArrayList<>(reservas);
		}

		@Override
		public List<Reserva> listarPorCliente(long clienteId) {
			return reservas.stream().filter(reserva -> reserva.getClienteId() == clienteId).toList();
		}

		@Override
		public List<Reserva> listarPorFecha(LocalDate fecha) {
			return reservas.stream().filter(reserva -> fecha.equals(reserva.getFecha())).toList();
		}

		@Override
		public void actualizarEstado(long id, EstadoReserva estado) {
		}

		@Override
		public boolean horarioOcupado(long canchaId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
				long reservaExcluidaId) {

			return false;
		}
	}

	private static final class CanchaDAODoble implements CanchaDAO {

		private Cancha cancha;

		@Override
		public void guardar(Cancha cancha) {
			this.cancha = cancha;
		}

		@Override
		public void eliminar(long id) {
		}

		@Override
		public Cancha buscar(long id) {
			return cancha;
		}

		@Override
		public List<Cancha> listar() {
			if (cancha == null) {
				return new ArrayList<>();
			}
			return new ArrayList<>(List.of(cancha));
		}

		@Override
		public boolean existeNombre(String nombre, long id) {
			return false;
		}
	}
}
