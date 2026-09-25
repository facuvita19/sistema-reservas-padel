package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import negocio.AgendaDiaria;
import negocio.BloqueoCancha;
import negocio.Cancha;
import negocio.CeldaAgenda;
import negocio.EstadoCeldaAgenda;
import negocio.EstadoReserva;
import negocio.FilaAgenda;
import negocio.Reserva;
import negocio.TipoCancha;

class AgendaServiceTest {

	private CanchaDAODoble canchaDAO;
	private ReservaDAODoble reservaDAO;
	private BloqueoDAODoble bloqueoDAO;
	private AgendaService service;
	private LocalDate lunesFuturo;

	@BeforeEach
	void preparar() {
		canchaDAO = new CanchaDAODoble();
		reservaDAO = new ReservaDAODoble();
		bloqueoDAO = new BloqueoDAODoble();
		service = new AgendaService(canchaDAO, reservaDAO, bloqueoDAO);
		lunesFuturo = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		canchaDAO.canchas.add(crearCancha());
	}

	@Test
	void rechazaFechaNula() {
		assertThrows(IllegalArgumentException.class, () -> service.obtener(null));
	}

	@Test
	void generaFilasCadaTreintaMinutos() {
		AgendaDiaria agenda = service.obtener(lunesFuturo);
		assertEquals(6, agenda.getFilas().size());
		assertEquals(LocalTime.of(8, 0), agenda.getFilas().get(0).getHoraInicio());
		assertEquals(LocalTime.of(10, 30), agenda.getFilas().get(5).getHoraInicio());
	}

	@Test
	void marcaHorarioDisponible() {
		AgendaDiaria agenda = service.obtener(lunesFuturo);
		assertEquals(EstadoCeldaAgenda.DISPONIBLE, buscarCelda(agenda, LocalTime.of(8, 0)).getEstado());
	}

	@Test
	void solamenteMarcaComoDisponiblesLosIniciosValidos() {
		AgendaDiaria agenda = service.obtener(lunesFuturo);

		assertEquals(EstadoCeldaAgenda.DISPONIBLE, buscarCelda(agenda, LocalTime.of(8, 0)).getEstado());

		assertEquals(EstadoCeldaAgenda.NO_DISPONIBLE, buscarCelda(agenda, LocalTime.of(8, 30)).getEstado());

		assertEquals(EstadoCeldaAgenda.NO_DISPONIBLE, buscarCelda(agenda, LocalTime.of(9, 0)).getEstado());

		assertEquals(EstadoCeldaAgenda.DISPONIBLE, buscarCelda(agenda, LocalTime.of(9, 30)).getEstado());
	}

	@Test
	void marcaTodosLosIntervalosDeUnaReserva() {
		reservaDAO.reservas.add(crearReserva(LocalTime.of(8, 0), LocalTime.of(9, 30), EstadoReserva.CONFIRMADA));

		AgendaDiaria agenda = service.obtener(lunesFuturo);

		assertEquals(EstadoCeldaAgenda.CONFIRMADA, buscarCelda(agenda, LocalTime.of(8, 0)).getEstado());
		assertEquals(EstadoCeldaAgenda.CONFIRMADA, buscarCelda(agenda, LocalTime.of(8, 30)).getEstado());
		assertEquals(EstadoCeldaAgenda.CONFIRMADA, buscarCelda(agenda, LocalTime.of(9, 0)).getEstado());
		assertEquals(EstadoCeldaAgenda.DISPONIBLE, buscarCelda(agenda, LocalTime.of(9, 30)).getEstado());
	}

	@Test
	void ignoraReservasCanceladas() {
		reservaDAO.reservas.add(crearReserva(LocalTime.of(8, 0), LocalTime.of(9, 30), EstadoReserva.CANCELADA));

		AgendaDiaria agenda = service.obtener(lunesFuturo);
		assertEquals(EstadoCeldaAgenda.DISPONIBLE, buscarCelda(agenda, LocalTime.of(8, 0)).getEstado());
	}

	@Test
	void marcaTodosLosIntervalosDeUnBloqueo() {
		bloqueoDAO.bloqueos.add(crearBloqueo(LocalTime.of(9, 0), LocalTime.of(10, 0)));

		AgendaDiaria agenda = service.obtener(lunesFuturo);
		assertEquals(EstadoCeldaAgenda.BLOQUEADA, buscarCelda(agenda, LocalTime.of(9, 0)).getEstado());
		assertEquals(EstadoCeldaAgenda.BLOQUEADA, buscarCelda(agenda, LocalTime.of(9, 30)).getEstado());
	}

	@Test
	void devuelveAgendaVaciaSinCanchasActivas() {
		canchaDAO.canchas.clear();
		AgendaDiaria agenda = service.obtener(lunesFuturo);
		assertTrue(agenda.getCanchas().isEmpty());
		assertTrue(agenda.getFilas().isEmpty());
	}

	private CeldaAgenda buscarCelda(AgendaDiaria agenda, LocalTime hora) {
		FilaAgenda fila = agenda.getFilas().stream().filter(valor -> hora.equals(valor.getHoraInicio())).findFirst()
				.orElseThrow();
		return fila.getCeldas().get(0);
	}

	private Cancha crearCancha() {
		Cancha cancha = new Cancha();
		cancha.setId(10L);
		cancha.setNombre("Cancha Central");
		cancha.setTipo(TipoCancha.CUBIERTA);
		cancha.setHoraApertura(LocalTime.of(8, 0));
		cancha.setHoraCierre(LocalTime.of(11, 0));
		cancha.setDuracionReserva(90);
		cancha.setPrecio(new BigDecimal("25000"));
		cancha.setDiasDisponibles(EnumSet.of(DayOfWeek.MONDAY));
		cancha.setActivo(true);
		return cancha;
	}

	private Reserva crearReserva(LocalTime inicio, LocalTime fin, EstadoReserva estado) {
		Reserva reserva = new Reserva();
		reserva.setId(50L);
		reserva.setCanchaId(10L);
		reserva.setFecha(lunesFuturo);
		reserva.setHoraInicio(inicio);
		reserva.setHoraFin(fin);
		reserva.setEstado(estado);
		reserva.setNombreCliente("Cliente Prueba");
		return reserva;
	}

	private BloqueoCancha crearBloqueo(LocalTime inicio, LocalTime fin) {
		BloqueoCancha bloqueo = new BloqueoCancha();
		bloqueo.setId(70L);
		bloqueo.setCanchaId(10L);
		bloqueo.setFecha(lunesFuturo);
		bloqueo.setHoraInicio(inicio);
		bloqueo.setHoraFin(fin);
		bloqueo.setMotivo("Mantenimiento");
		return bloqueo;
	}

	private static final class CanchaDAODoble implements CanchaDAO {
		private final List<Cancha> canchas = new ArrayList<>();

		@Override
		public void guardar(Cancha cancha) {
		}

		@Override
		public void eliminar(long id) {
		}

		@Override
		public Cancha buscar(long id) {
			return null;
		}

		@Override
		public List<Cancha> listar() {
			return new ArrayList<>(canchas);
		}

		@Override
		public boolean existeNombre(String nombre, long id) {
			return false;
		}
	}

	private final class ReservaDAODoble implements ReservaDAO {
		private final List<Reserva> reservas = new ArrayList<>();

		@Override
		public void guardar(Reserva reserva) {
		}

		@Override
		public Reserva buscar(long id) {
			return null;
		}

		@Override
		public List<Reserva> listar() {
			return new ArrayList<>(reservas);
		}

		@Override
		public List<Reserva> listarPorCliente(long id) {
			return new ArrayList<>(reservas);
		}

		@Override
		public List<Reserva> listarPorFecha(LocalDate fecha) {
			return reservas.stream().filter(r -> fecha.equals(r.getFecha())).toList();
		}

		@Override
		public void actualizarEstado(long id, EstadoReserva estado) {
		}

		@Override
		public boolean horarioOcupado(long canchaId, LocalDate fecha, LocalTime inicio, LocalTime fin,
				long reservaExcluidaId) {
			return false;
		}
	}

	private final class BloqueoDAODoble implements BloqueoCanchaDAO {
		private final List<BloqueoCancha> bloqueos = new ArrayList<>();

		@Override
		public void guardar(BloqueoCancha bloqueo) {
		}

		@Override
		public void eliminar(long id) {
		}

		@Override
		public BloqueoCancha buscar(long id) {
			return null;
		}

		@Override
		public List<BloqueoCancha> listar() {
			return new ArrayList<>(bloqueos);
		}

		@Override
		public List<BloqueoCancha> listarPorCancha(long canchaId) {
			return bloqueos.stream().filter(b -> b.getCanchaId() == canchaId).toList();
		}

		@Override
		public boolean horarioBloqueado(long canchaId, LocalDate fecha, LocalTime inicio, LocalTime fin,
				long bloqueoExcluidoId) {
			return false;
		}
	}
}
