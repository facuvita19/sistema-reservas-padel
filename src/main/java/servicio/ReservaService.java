package servicio;

import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import dao.BloqueoCanchaDAO;
import dao.BloqueoCanchaDAOMySQL;
import dao.ReservaDAO;
import dao.ReservaDAOMySQL;
import negocio.Cancha;
import negocio.EstadoReserva;
import negocio.Reserva;

public class ReservaService {

	private final ReservaDAO reservaDAO;
	private final BloqueoCanchaDAO bloqueoDAO;
	private final CanchaService canchaService;

	public ReservaService() {
		this(new ReservaDAOMySQL(), new BloqueoCanchaDAOMySQL(), new CanchaService());
	}

	public ReservaService(ReservaDAO reservaDAO, BloqueoCanchaDAO bloqueoDAO, CanchaService canchaService) {

		if (reservaDAO == null || bloqueoDAO == null || canchaService == null) {
			throw new IllegalArgumentException("Las dependencias de reservas no pueden ser nulas.");
		}

		this.reservaDAO = reservaDAO;
		this.bloqueoDAO = bloqueoDAO;
		this.canchaService = canchaService;
	}

	public void guardar(Reserva reserva) {
		validarDatosBasicos(reserva);

		Cancha cancha = obtenerCancha(reserva.getCanchaId());
		LocalTime horaFin = reserva.getHoraInicio().plusMinutes(cancha.getDuracionReserva());

		reserva.setHoraFin(horaFin);
		reserva.setPrecioTotal(cancha.getPrecio().setScale(2, RoundingMode.HALF_UP));

		validarFechaYHorario(reserva, cancha);
		validarDisponibilidad(reserva);
		normalizarTextos(reserva);

		if (reserva.getEstado() == null) {
			reserva.setEstado(EstadoReserva.PENDIENTE);
		}

		reservaDAO.guardar(reserva);
	}

	public List<LocalTime> listarHorariosDisponibles(long canchaId, LocalDate fecha, long reservaExcluidaId) {

		if (fecha == null || fecha.isBefore(LocalDate.now())) {
			return new ArrayList<>();
		}

		Cancha cancha = obtenerCancha(canchaId);

		if (!cancha.estaDisponibleElDia(fecha.getDayOfWeek())) {
			return new ArrayList<>();
		}

		List<LocalTime> horarios = new ArrayList<>();
		LocalTime inicio = cancha.getHoraApertura();
		int duracionReserva = cancha.getDuracionReserva();

		while (inicio.isBefore(cancha.getHoraCierre())
				&& Duration.between(inicio, cancha.getHoraCierre()).toMinutes() >= duracionReserva) {

			LocalTime fin = inicio.plusMinutes(duracionReserva);

			boolean esPasado = fecha.equals(LocalDate.now()) && !inicio.isAfter(LocalTime.now());

			boolean ocupado = reservaDAO.horarioOcupado(canchaId, fecha, inicio, fin, reservaExcluidaId);

			boolean bloqueado = bloqueoDAO.horarioBloqueado(canchaId, fecha, inicio, fin, 0L);

			if (!esPasado && !ocupado && !bloqueado) {
				horarios.add(inicio);
			}

			inicio = fin;
		}

		return horarios;
	}

	public void cambiarEstado(long reservaId, EstadoReserva nuevoEstado) {

		if (reservaId <= 0 || nuevoEstado == null) {
			throw new IllegalArgumentException("La reserva y el nuevo estado son obligatorios.");
		}

		Reserva reserva = reservaDAO.buscar(reservaId);

		if (reserva == null) {
			throw new IllegalArgumentException("La reserva no existe.");
		}

		EstadoReserva actual = reserva.getEstado();

		if (actual == nuevoEstado) {
			throw new IllegalArgumentException("La reserva ya tiene el estado seleccionado.");
		}

		if (actual != null && actual.esFinal()) {
			throw new IllegalArgumentException("No se puede modificar una reserva finalizada.");
		}

		if (!transicionPermitida(actual, nuevoEstado)) {
			throw new IllegalArgumentException("El cambio de estado no esta permitido.");
		}

		reservaDAO.actualizarEstado(reservaId, nuevoEstado);
	}

	public Reserva buscar(long id) {
		return id <= 0 ? null : reservaDAO.buscar(id);
	}

	public List<Reserva> listar() {
		return reservaDAO.listar();
	}

	public List<Reserva> listarPorCliente(long clienteId) {
		if (clienteId <= 0) {
			throw new IllegalArgumentException("El ID del cliente no es valido.");
		}
		return reservaDAO.listarPorCliente(clienteId);
	}

	public List<Reserva> listarPorFecha(LocalDate fecha) {
		if (fecha == null) {
			throw new IllegalArgumentException("La fecha es obligatoria.");
		}
		return reservaDAO.listarPorFecha(fecha);
	}

	private void validarDatosBasicos(Reserva reserva) {
		if (reserva == null) {
			throw new IllegalArgumentException("La reserva no puede ser nula.");
		}

		if (reserva.getClienteId() <= 0 || reserva.getCanchaId() <= 0 || reserva.getUsuarioId() <= 0) {
			throw new IllegalArgumentException("Cliente, cancha y usuario son obligatorios.");
		}

		if (reserva.getFecha() == null || reserva.getHoraInicio() == null) {
			throw new IllegalArgumentException("La fecha y la hora de inicio son obligatorias.");
		}

		if (reserva.getCantidadJugadores() < 1 || reserva.getCantidadJugadores() > 8) {
			throw new IllegalArgumentException("La cantidad de jugadores debe estar entre 1 y 8.");
		}
	}

	private Cancha obtenerCancha(long canchaId) {
		Cancha cancha = canchaService.buscar(canchaId);

		if (cancha == null || !cancha.isActivo()) {
			throw new IllegalArgumentException("La cancha no existe o esta inactiva.");
		}

		return cancha;
	}

	private void validarFechaYHorario(Reserva reserva, Cancha cancha) {

		if (reserva.getFecha().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("No se puede reservar en una fecha pasada.");
		}

		LocalDateTime inicioReserva = LocalDateTime.of(reserva.getFecha(), reserva.getHoraInicio());

		if (!inicioReserva.isAfter(LocalDateTime.now())) {
			throw new IllegalArgumentException("La reserva debe comenzar en una fecha y hora futura.");
		}

		if (!cancha.estaDisponibleElDia(reserva.getFecha().getDayOfWeek())) {
			throw new IllegalArgumentException("La cancha no se encuentra disponible ese dia.");
		}

		if (!cancha.contieneHorario(reserva.getHoraInicio(), reserva.getHoraFin())) {
			throw new IllegalArgumentException("El horario esta fuera de la jornada de la cancha.");
		}
	}

	private void validarDisponibilidad(Reserva reserva) {
		if (reservaDAO.horarioOcupado(reserva.getCanchaId(), reserva.getFecha(), reserva.getHoraInicio(),
				reserva.getHoraFin(), reserva.getId())) {
			throw new IllegalArgumentException("El horario se superpone con otra reserva.");
		}

		if (bloqueoDAO.horarioBloqueado(reserva.getCanchaId(), reserva.getFecha(), reserva.getHoraInicio(),
				reserva.getHoraFin(), 0L)) {
			throw new IllegalArgumentException("La cancha se encuentra bloqueada en ese horario.");
		}
	}

	private void normalizarTextos(Reserva reserva) {
		reserva.setComentarios(limpiarOpcional(reserva.getComentarios()));
		reserva.setObservacionesAdministrativas(limpiarOpcional(reserva.getObservacionesAdministrativas()));
	}

	private String limpiarOpcional(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim().replaceAll("\\s+", " ");
	}

	private boolean transicionPermitida(EstadoReserva actual, EstadoReserva nuevo) {

		if (actual == null || actual == EstadoReserva.PENDIENTE) {
			return nuevo == EstadoReserva.CONFIRMADA || nuevo == EstadoReserva.CANCELADA;
		}

		if (actual == EstadoReserva.CONFIRMADA) {
			return nuevo == EstadoReserva.COMPLETADA || nuevo == EstadoReserva.CANCELADA
					|| nuevo == EstadoReserva.AUSENTE;
		}

		return false;
	}
}
