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
import negocio.ConfiguracionComplejo;
import negocio.EstadoReserva;
import negocio.OrigenReserva;
import negocio.Reserva;
import negocio.TipoCancelacion;

public class ReservaService {

    private final ReservaDAO reservaDAO;
    private final BloqueoCanchaDAO bloqueoDAO;
    private final CanchaService canchaService;
    private final ConfiguracionComplejoService configuracionService;

    public ReservaService() {
        this(new ReservaDAOMySQL(), new BloqueoCanchaDAOMySQL(),
                new CanchaService(), new ConfiguracionComplejoService());
    }

    public ReservaService(
            ReservaDAO reservaDAO,
            BloqueoCanchaDAO bloqueoDAO,
            CanchaService canchaService) {
        this(reservaDAO, bloqueoDAO, canchaService,
                new ConfiguracionComplejoService());
    }

    public ReservaService(
            ReservaDAO reservaDAO,
            BloqueoCanchaDAO bloqueoDAO,
            CanchaService canchaService,
            ConfiguracionComplejoService configuracionService) {
        if (reservaDAO == null || bloqueoDAO == null
                || canchaService == null || configuracionService == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de reservas no pueden ser nulas.");
        }
        this.reservaDAO = reservaDAO;
        this.bloqueoDAO = bloqueoDAO;
        this.canchaService = canchaService;
        this.configuracionService = configuracionService;
    }

    public void guardar(Reserva reserva) {
        expirarReservasPendientes();
        if (reserva != null && reserva.getId() <= 0) {
            reserva.setOrigen(OrigenReserva.PERSONAL);
        }
        prepararYGuardar(reserva);
    }

    public Reserva guardarPendienteWeb(Reserva reserva) {
        expirarReservasPendientes();
        if (reserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula.");
        }

        ConfiguracionComplejo configuracion = configuracionService.obtener();
        LocalDateTime ahora = LocalDateTime.now();

        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setOrigen(OrigenReserva.WEB);
        reserva.setFechaVencimiento(
                ahora.plusMinutes(configuracion.getMinutosReservaPendiente()));
        reserva.setFechaExpiracion(null);

        prepararYGuardar(reserva);
        return reserva;
    }

    private void prepararYGuardar(Reserva reserva) {
        validarDatosBasicos(reserva);
        Cancha cancha = obtenerCancha(reserva.getCanchaId());
        reserva.setHoraFin(
                reserva.getHoraInicio().plusMinutes(cancha.getDuracionReserva()));
        reserva.setPrecioTotal(
                cancha.getPrecio().setScale(2, RoundingMode.HALF_UP));
        validarFechaYHorario(reserva, cancha);
        validarDisponibilidad(reserva);
        normalizarTextos(reserva);

        if (reserva.getEstado() == null) {
            reserva.setEstado(EstadoReserva.PENDIENTE);
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            reserva.setFechaVencimiento(null);
            reserva.setFechaExpiracion(null);
        }

        reservaDAO.guardar(reserva);
    }

    public int expirarReservasPendientes() {
        return reservaDAO.expirarPendientesVencidas(LocalDateTime.now());
    }

    public List<LocalTime> listarHorariosDisponibles(
            long canchaId, LocalDate fecha, long reservaExcluidaId) {
        expirarReservasPendientes();
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            return new ArrayList<>();
        }
        Cancha cancha = obtenerCancha(canchaId);
        if (!cancha.estaDisponibleElDia(fecha.getDayOfWeek())) {
            return new ArrayList<>();
        }
        List<LocalTime> horarios = new ArrayList<>();
        LocalTime inicio = cancha.getHoraApertura();
        int duracion = cancha.getDuracionReserva();
        while (inicio.isBefore(cancha.getHoraCierre())
                && Duration.between(inicio, cancha.getHoraCierre()).toMinutes()
                        >= duracion) {
            LocalTime fin = inicio.plusMinutes(duracion);
            boolean pasado = fecha.equals(LocalDate.now())
                    && !inicio.isAfter(LocalTime.now());
            boolean ocupado = reservaDAO.horarioOcupado(
                    canchaId, fecha, inicio, fin, reservaExcluidaId);
            boolean bloqueado = bloqueoDAO.horarioBloqueado(
                    canchaId, fecha, inicio, fin, 0L);
            if (!pasado && !ocupado && !bloqueado) horarios.add(inicio);
            inicio = fin;
        }
        return horarios;
    }

    public void cambiarEstado(long reservaId, EstadoReserva nuevoEstado) {
        Reserva reserva = obtenerReservaParaCambio(reservaId, nuevoEstado);
        validarTransicion(reserva, nuevoEstado);
        if (nuevoEstado == EstadoReserva.CANCELADA) {
            cancelarNormal(reservaId);
            return;
        }
        reservaDAO.actualizarEstado(reservaId, nuevoEstado);
    }

    public void cancelarNormal(long reservaId) {
        Reserva reserva = obtenerReservaParaCambio(
                reservaId, EstadoReserva.CANCELADA);
        validarTransicion(reserva, EstadoReserva.CANCELADA);
        reservaDAO.cancelar(reservaId, TipoCancelacion.CLIENTE,
                null, LocalDateTime.now(), null);
    }

    public void cancelarAdministrativamente(
            long reservaId, String motivo, long usuarioCancelacionId) {
        Reserva reserva = obtenerReservaParaCambio(
                reservaId, EstadoReserva.CANCELADA);
        validarTransicion(reserva, EstadoReserva.CANCELADA);

        String motivoNormalizado = limpiarOpcional(motivo);
        if (motivoNormalizado == null) {
            throw new IllegalArgumentException(
                    "El motivo de la cancelación administrativa es obligatorio.");
        }
        if (usuarioCancelacionId <= 0) {
            throw new IllegalArgumentException(
                    "El usuario que cancela es obligatorio.");
        }

        reservaDAO.cancelar(reservaId, TipoCancelacion.ADMINISTRATIVA,
                motivoNormalizado, LocalDateTime.now(), usuarioCancelacionId);
    }

    private Reserva obtenerReservaParaCambio(
            long reservaId, EstadoReserva nuevoEstado) {
        if (reservaId <= 0 || nuevoEstado == null) {
            throw new IllegalArgumentException(
                    "La reserva y el nuevo estado son obligatorios.");
        }
        Reserva reserva = reservaDAO.buscar(reservaId);
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        return reserva;
    }

    private void validarTransicion(
            Reserva reserva, EstadoReserva nuevoEstado) {
        EstadoReserva actual = reserva.getEstado();
        if (actual == nuevoEstado) {
            throw new IllegalArgumentException(
                    "La reserva ya tiene el estado seleccionado.");
        }
        if (actual != null && actual.esFinal()) {
            throw new IllegalArgumentException(
                    "No se puede modificar una reserva finalizada.");
        }
        if (!transicionPermitida(actual, nuevoEstado)) {
            throw new IllegalArgumentException(
                    "El cambio de estado no está permitido.");
        }
    }

    public Reserva buscar(long id) {
        return id <= 0 ? null : reservaDAO.buscar(id);
    }

    public List<Reserva> listar() {
        expirarReservasPendientes();
        return reservaDAO.listar();
    }

    public List<Reserva> listarPorCliente(long clienteId) {
        if (clienteId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del cliente no es válido.");
        }
        expirarReservasPendientes();
        return reservaDAO.listarPorCliente(clienteId);
    }

    public List<Reserva> listarPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        expirarReservasPendientes();
        return reservaDAO.listarPorFecha(fecha);
    }

    private void validarDatosBasicos(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula.");
        }
        if (reserva.getClienteId() <= 0 || reserva.getCanchaId() <= 0
                || reserva.getUsuarioId() <= 0) {
            throw new IllegalArgumentException(
                    "Cliente, cancha y usuario son obligatorios.");
        }
        if (reserva.getFecha() == null || reserva.getHoraInicio() == null) {
            throw new IllegalArgumentException(
                    "La fecha y la hora de inicio son obligatorias.");
        }
        if (reserva.getCantidadJugadores() < 1
                || reserva.getCantidadJugadores() > 8) {
            throw new IllegalArgumentException(
                    "La cantidad de jugadores debe estar entre 1 y 8.");
        }
    }

    private Cancha obtenerCancha(long canchaId) {
        Cancha cancha = canchaService.buscar(canchaId);
        if (cancha == null || !cancha.isActivo()) {
            throw new IllegalArgumentException(
                    "La cancha no existe o está inactiva.");
        }
        return cancha;
    }

    private void validarFechaYHorario(Reserva reserva, Cancha cancha) {
        if (reserva.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se puede reservar en una fecha pasada.");
        }
        LocalDateTime inicio = LocalDateTime.of(
                reserva.getFecha(), reserva.getHoraInicio());
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "La reserva debe comenzar en una fecha y hora futura.");
        }
        if (!cancha.estaDisponibleElDia(
                reserva.getFecha().getDayOfWeek())) {
            throw new IllegalArgumentException(
                    "La cancha no se encuentra disponible ese día.");
        }
        if (!cancha.contieneHorario(
                reserva.getHoraInicio(), reserva.getHoraFin())) {
            throw new IllegalArgumentException(
                    "El horario está fuera de la jornada de la cancha.");
        }
    }

    private void validarDisponibilidad(Reserva reserva) {
        if (reservaDAO.horarioOcupado(
                reserva.getCanchaId(), reserva.getFecha(),
                reserva.getHoraInicio(), reserva.getHoraFin(),
                reserva.getId())) {
            throw new IllegalArgumentException(
                    "El horario se superpone con otra reserva.");
        }
        if (bloqueoDAO.horarioBloqueado(
                reserva.getCanchaId(), reserva.getFecha(),
                reserva.getHoraInicio(), reserva.getHoraFin(), 0L)) {
            throw new IllegalArgumentException(
                    "La cancha se encuentra bloqueada en ese horario.");
        }
    }

    private void normalizarTextos(Reserva reserva) {
        reserva.setComentarios(limpiarOpcional(reserva.getComentarios()));
        reserva.setObservacionesAdministrativas(
                limpiarOpcional(reserva.getObservacionesAdministrativas()));
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank()
                ? null
                : valor.trim().replaceAll("\\s+", " ");
    }

    private boolean transicionPermitida(
            EstadoReserva actual, EstadoReserva nuevo) {
        if (actual == null || actual == EstadoReserva.PENDIENTE) {
            return nuevo == EstadoReserva.CONFIRMADA
                    || nuevo == EstadoReserva.CANCELADA;
        }
        if (actual == EstadoReserva.CONFIRMADA) {
            return nuevo == EstadoReserva.COMPLETADA
                    || nuevo == EstadoReserva.CANCELADA
                    || nuevo == EstadoReserva.AUSENTE;
        }
        return false;
    }
}
