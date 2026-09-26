package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.PagoDAO;
import dao.ReservaDAO;
import negocio.EstadoPago;
import negocio.EstadoReserva;
import negocio.MetodoPago;
import negocio.Pago;
import negocio.Reserva;

class PagoServiceTest {

	private PagoDAODoble pagoDAO;
	private ReservaDAODoble reservaDAO;
	private PagoService service;
	private Reserva reserva;

	@BeforeEach
	void preparar() {
		pagoDAO = new PagoDAODoble();
		reservaDAO = new ReservaDAODoble();
		service = new PagoService(pagoDAO, reservaDAO);

		reserva = crearReserva(10L, new BigDecimal("30000.00"), EstadoReserva.CONFIRMADA);
		reservaDAO.reserva = reserva;
	}

	@Test
	void guardaPagoPendienteValido() {
		Pago pago = crearPago(new BigDecimal("10000"), EstadoPago.PENDIENTE);
		pago.setReferencia("  Transferencia   cliente  ");

		service.guardar(pago);

		assertTrue(pagoDAO.guardarInvocado);
		assertSame(pago, pagoDAO.ultimoGuardado);
		assertEquals(new BigDecimal("10000.00"), pago.getImporte());
		assertEquals("Transferencia cliente", pago.getReferencia());
	}

	@Test
	void guardaPagoAcreditadoConFechaAutomatica() {
		Pago pago = crearPago(new BigDecimal("12000"), EstadoPago.ACREDITADO);

		service.guardar(pago);

		assertTrue(pagoDAO.guardarInvocado);
		assertTrue(pago.getFechaPago() != null);
	}

	@Test
	void asignaEstadoPendienteCuandoEsNulo() {
		Pago pago = crearPago(new BigDecimal("5000"), null);

		service.guardar(pago);

		assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
	}

	@Test
	void rechazaImporteNuloCeroONegativo() {
		Pago pagoNulo = crearPago(null, EstadoPago.PENDIENTE);
		Pago pagoCero = crearPago(BigDecimal.ZERO, EstadoPago.PENDIENTE);
		Pago pagoNegativo = crearPago(new BigDecimal("-100"), EstadoPago.PENDIENTE);

		assertThrows(IllegalArgumentException.class, () -> service.guardar(pagoNulo));
		assertThrows(IllegalArgumentException.class, () -> service.guardar(pagoCero));
		assertThrows(IllegalArgumentException.class, () -> service.guardar(pagoNegativo));
		assertFalse(pagoDAO.guardarInvocado);
	}

	@Test
	void rechazaPagoSinMetodo() {
		Pago pago = crearPago(new BigDecimal("5000"), EstadoPago.PENDIENTE);
		pago.setMetodoPago(null);

		assertThrows(IllegalArgumentException.class, () -> service.guardar(pago));
	}

	@Test
	void rechazaPagoSobreReservaCancelada() {
		reserva.setEstado(EstadoReserva.CANCELADA);
		Pago pago = crearPago(new BigDecimal("5000"), EstadoPago.PENDIENTE);

		IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.guardar(pago));

		assertTrue(error.getMessage().contains("cancelada"));
	}

	@Test
	void rechazaImporteSuperiorAlSaldo() {
		pagoDAO.totalAcreditado = new BigDecimal("10000.00");
		Pago pago = crearPago(new BigDecimal("25000.00"), EstadoPago.PENDIENTE);

		IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.guardar(pago));

		assertTrue(error.getMessage().contains("saldo pendiente"));
	}

	@Test
	void calculaSaldoYDetectaReservaPagada() {
		pagoDAO.totalAcreditado = new BigDecimal("10000.00");

		assertEquals(new BigDecimal("20000.00"), service.calcularSaldo(reserva.getId()));
		assertFalse(service.estaPagada(reserva.getId()));

		pagoDAO.totalAcreditado = new BigDecimal("30000.00");

		assertEquals(new BigDecimal("0.00"), service.calcularSaldo(reserva.getId()));
		assertTrue(service.estaPagada(reserva.getId()));
	}

	@Test
	void saldoNuncaEsNegativo() {
		pagoDAO.totalAcreditado = new BigDecimal("35000.00");

		assertEquals(new BigDecimal("0.00"), service.calcularSaldo(reserva.getId()));
	}

	@Test
	void acreditaPagoPendiente() {
		Pago pago = crearPago(new BigDecimal("10000.00"), EstadoPago.PENDIENTE);
		pago.setId(20L);
		pagoDAO.pagoBuscado = pago;

		service.acreditar(20L);

		assertEquals(20L, pagoDAO.idEstadoActualizado);
		assertEquals(EstadoPago.ACREDITADO, pagoDAO.estadoActualizado);
	}

	@Test
	void rechazaAcreditarPagoNoPendiente() {
		Pago pago = crearPago(new BigDecimal("10000.00"), EstadoPago.ACREDITADO);
		pago.setId(20L);
		pagoDAO.pagoBuscado = pago;

		assertThrows(IllegalArgumentException.class, () -> service.acreditar(20L));
	}

	@Test
	void anulaPagoPendiente() {
		Pago pago = crearPago(new BigDecimal("10000.00"), EstadoPago.PENDIENTE);
		pago.setId(20L);
		pagoDAO.pagoBuscado = pago;

		service.anular(20L);

		assertEquals(EstadoPago.ANULADO, pagoDAO.estadoActualizado);
	}

	@Test
	void rechazaAnularPagoAcreditado() {
		Pago pago = crearPago(new BigDecimal("10000.00"), EstadoPago.ACREDITADO);
		pago.setId(20L);
		pagoDAO.pagoBuscado = pago;

		assertThrows(IllegalArgumentException.class, () -> service.anular(20L));
	}

	@Test
	void reembolsaTodosLosPagosAcreditadosDeReservaCancelada() {
		reserva.setEstado(EstadoReserva.CANCELADA);

		Pago primerPago = crearPago(new BigDecimal("10000.00"), EstadoPago.ACREDITADO);
		primerPago.setId(20L);

		Pago segundoPago = crearPago(new BigDecimal("5000.00"), EstadoPago.ACREDITADO);
		segundoPago.setId(21L);

		Pago pendiente = crearPago(new BigDecimal("2000.00"), EstadoPago.PENDIENTE);
		pendiente.setId(22L);

		pagoDAO.pagos.add(primerPago);
		pagoDAO.pagos.add(segundoPago);
		pagoDAO.pagos.add(pendiente);

		service.reembolsarPagosDeReservaAutorizado(reserva.getId());

		assertEquals(List.of(20L, 21L), pagoDAO.idsReembolsados);
	}

	@Test
	void rechazaReembolsoSiLaReservaNoEstaCancelada() {
		Pago pago = crearPago(new BigDecimal("10000.00"), EstadoPago.ACREDITADO);
		pago.setId(20L);
		pagoDAO.pagos.add(pago);

		assertThrows(IllegalArgumentException.class, () -> service.reembolsarPagosDeReservaAutorizado(reserva.getId()));
	}

	@Test
	void rechazaReembolsoSinPagosAcreditados() {
		reserva.setEstado(EstadoReserva.CANCELADA);

		Pago pendiente = crearPago(new BigDecimal("10000.00"), EstadoPago.PENDIENTE);
		pendiente.setId(20L);
		pagoDAO.pagos.add(pendiente);

		assertThrows(IllegalArgumentException.class, () -> service.reembolsarPagosDeReservaAutorizado(reserva.getId()));
	}

	@Test
	void buscaListaYListaPorReserva() {
		Pago pago = crearPago(new BigDecimal("10000.00"), EstadoPago.PENDIENTE);
		pago.setId(20L);
		pagoDAO.pagoBuscado = pago;
		pagoDAO.pagos.add(pago);

		assertSame(pago, service.buscar(20L));
		assertEquals(1, service.listar().size());
		assertEquals(1, service.listarPorReserva(10L).size());
	}

	private Pago crearPago(BigDecimal importe, EstadoPago estado) {
		Pago pago = new Pago();
		pago.setReservaId(10L);
		pago.setImporte(importe);
		pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
		pago.setEstado(estado);
		return pago;
	}

	private Reserva crearReserva(long id, BigDecimal precio, EstadoReserva estado) {

		Reserva resultado = new Reserva();
		resultado.setId(id);
		resultado.setClienteId(1L);
		resultado.setCanchaId(1L);
		resultado.setUsuarioId(1L);
		resultado.setFecha(LocalDate.now().plusDays(1));
		resultado.setHoraInicio(LocalTime.of(18, 0));
		resultado.setHoraFin(LocalTime.of(19, 30));
		resultado.setPrecioTotal(precio);
		resultado.setEstado(estado);
		return resultado;
	}

	private static final class PagoDAODoble implements PagoDAO {
		private boolean guardarInvocado;
		private Pago ultimoGuardado;
		private Pago pagoBuscado;
		private long idEstadoActualizado;
		private EstadoPago estadoActualizado;
		private BigDecimal totalAcreditado = BigDecimal.ZERO;
		private final List<Pago> pagos = new ArrayList<>();
		private final List<Long> idsReembolsados = new ArrayList<>();

		@Override
		public void guardar(Pago pago) {
			guardarInvocado = true;
			ultimoGuardado = pago;
		}

		@Override
		public Pago buscar(long id) {
			if (pagoBuscado != null && pagoBuscado.getId() == id) {
				return pagoBuscado;
			}

			return pagos.stream().filter(pago -> pago.getId() == id).findFirst().orElse(null);
		}

		@Override
		public List<Pago> listar() {
			return new ArrayList<>(pagos);
		}

		@Override
		public List<Pago> listarPorReserva(long reservaId) {
			return new ArrayList<>(pagos);
		}

		@Override
		public void actualizarEstado(long id, EstadoPago estado) {

			idEstadoActualizado = id;
			estadoActualizado = estado;

			if (estado == EstadoPago.REEMBOLSADO) {
				idsReembolsados.add(id);
			}

			pagos.stream().filter(pago -> pago.getId() == id).findFirst().ifPresent(pago -> pago.setEstado(estado));
		}

		@Override
		public BigDecimal totalAcreditado(long reservaId) {
			return totalAcreditado;
		}
	}

	private static final class ReservaDAODoble implements ReservaDAO {
		private Reserva reserva;

		@Override
		public void guardar(Reserva reserva) {
		}

		@Override
		public Reserva buscar(long id) {
			return reserva;
		}

		@Override
		public List<Reserva> listar() {
			return new ArrayList<>();
		}

		@Override
		public List<Reserva> listarPorCliente(long clienteId) {
			return new ArrayList<>();
		}

		@Override
		public List<Reserva> listarPorFecha(LocalDate fecha) {
			return new ArrayList<>();
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
}
