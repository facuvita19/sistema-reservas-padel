package servicio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import dao.BloqueoCanchaDAO;
import dao.BloqueoCanchaDAOMySQL;
import dao.ReservaDAO;
import dao.ReservaDAOMySQL;
import negocio.BloqueoCancha;
import negocio.Cancha;
import negocio.EstadoReserva;
import negocio.Reserva;

public class BloqueoCanchaService {

	private final BloqueoCanchaDAO bloqueoDAO;
	private final CanchaService canchaService;
	private final ReservaDAO reservaDAO;

	public BloqueoCanchaService() {
		this(new BloqueoCanchaDAOMySQL(), new CanchaService(), new ReservaDAOMySQL());
	}

	public BloqueoCanchaService(BloqueoCanchaDAO bloqueoDAO, CanchaService canchaService, ReservaDAO reservaDAO) {

		if (bloqueoDAO == null || canchaService == null || reservaDAO == null) {

			throw new IllegalArgumentException("Las dependencias de bloqueos no pueden ser nulas.");
		}

		this.bloqueoDAO = bloqueoDAO;
		this.canchaService = canchaService;
		this.reservaDAO = reservaDAO;
	}

	public void guardar(BloqueoCancha bloqueo) {
		validar(bloqueo);

		bloqueo.setMotivo(bloqueo.getMotivo().trim().replaceAll("\\s+", " "));

		Cancha cancha = canchaService.buscar(bloqueo.getCanchaId());

		if (cancha == null || !cancha.isActivo()) {
			throw new IllegalArgumentException("La cancha no existe o está inactiva.");
		}

		if (!cancha.estaDisponibleElDia(bloqueo.getFecha().getDayOfWeek())) {

			throw new IllegalArgumentException("La cancha no se encuentra disponible ese día.");
		}

		if (!cancha.contieneHorario(bloqueo.getHoraInicio(), bloqueo.getHoraFin())) {

			throw new IllegalArgumentException(
					"El bloqueo debe estar dentro del horario " + "de funcionamiento de la cancha.");
		}

		if (bloqueoDAO.horarioBloqueado(bloqueo.getCanchaId(), bloqueo.getFecha(), bloqueo.getHoraInicio(),
				bloqueo.getHoraFin(), bloqueo.getId())) {

			throw new IllegalArgumentException("El horario ya tiene otro bloqueo superpuesto.");
		}

		validarReservasSuperpuestas(bloqueo);
		bloqueoDAO.guardar(bloqueo);
	}

	public void eliminar(long id) {
		validarId(id);
		bloqueoDAO.eliminar(id);
	}

	public BloqueoCancha buscar(long id) {
		return id <= 0 ? null : bloqueoDAO.buscar(id);
	}

	public List<BloqueoCancha> listar() {
		return bloqueoDAO.listar();
	}

	public List<BloqueoCancha> listarPorCancha(long canchaId) {
		validarId(canchaId);
		return bloqueoDAO.listarPorCancha(canchaId);
	}

	public boolean estaBloqueado(long canchaId, LocalDate fecha, LocalTime inicio, LocalTime fin) {

		return bloqueoDAO.horarioBloqueado(canchaId, fecha, inicio, fin, 0L);
	}

	private void validar(BloqueoCancha bloqueo) {
		if (bloqueo == null) {
			throw new IllegalArgumentException("El bloqueo no puede ser nulo.");
		}

		validarId(bloqueo.getCanchaId());

		if (bloqueo.getFecha() == null) {
			throw new IllegalArgumentException("La fecha del bloqueo es obligatoria.");
		}

		if (bloqueo.getFecha().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("No se puede crear un bloqueo en una fecha pasada.");
		}

		if (bloqueo.getHoraInicio() == null || bloqueo.getHoraFin() == null
				|| !bloqueo.getHoraFin().isAfter(bloqueo.getHoraInicio())) {

			throw new IllegalArgumentException("El horario del bloqueo no es válido.");
		}

		if (bloqueo.getMotivo() == null || bloqueo.getMotivo().isBlank()) {

			throw new IllegalArgumentException("El motivo del bloqueo es obligatorio.");
		}
	}

	private void validarId(long id) {
		if (id <= 0) {
			throw new IllegalArgumentException("El ID de la cancha o bloqueo no es válido.");
		}
	}

	private void validarReservasSuperpuestas(BloqueoCancha bloqueo) {

		List<Reserva> reservasDelDia = reservaDAO.listarPorFecha(bloqueo.getFecha());

		boolean existeSuperposicion = reservasDelDia.stream()
				.filter(reserva -> reserva.getCanchaId() == bloqueo.getCanchaId())
				.filter(reserva -> reserva.getEstado() != EstadoReserva.CANCELADA
                                && reserva.getEstado() != EstadoReserva.EXPIRADA)
                                .anyMatch(reserva -> seSuperpone(bloqueo.getHoraInicio(), bloqueo.getHoraFin(), reserva.getHoraInicio(),
						reserva.getHoraFin()));

		if (existeSuperposicion) {
			throw new IllegalArgumentException(
					"No se puede bloquear la franja porque " + "existe una reserva activa superpuesta.");
		}
	}

	private boolean seSuperpone(LocalTime inicioA, LocalTime finA, LocalTime inicioB, LocalTime finB) {

		return inicioA.isBefore(finB) && finA.isAfter(inicioB);
	}
}
