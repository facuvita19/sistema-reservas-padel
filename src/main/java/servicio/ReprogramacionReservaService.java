package servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import dao.ReprogramacionReservaDAO;
import dao.ReprogramacionReservaDAOMySQL;
import negocio.EstadoReserva;
import negocio.ReprogramacionReserva;
import negocio.Reserva;

public class ReprogramacionReservaService {

    private final ReservaService reservaService;
    private final PoliticaReservaService politicaService;
    private final ReprogramacionReservaDAO historialDAO;

    public ReprogramacionReservaService() {
        this(new ReservaService(), new PoliticaReservaService(),
                new ReprogramacionReservaDAOMySQL());
    }

    public ReprogramacionReservaService(
            ReservaService reservaService,
            PoliticaReservaService politicaService,
            ReprogramacionReservaDAO historialDAO) {
        if (reservaService == null || politicaService == null
                || historialDAO == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de reprogramación no pueden ser nulas.");
        }
        this.reservaService = reservaService;
        this.politicaService = politicaService;
        this.historialDAO = historialDAO;
    }

    public void reprogramar(
            long reservaId,
            long nuevaCanchaId,
            LocalDate nuevaFecha,
            LocalTime nuevaHora,
            String motivo,
            long usuarioId) {

        ejecutarReprogramacion(
                reservaId,
                nuevaCanchaId,
                nuevaFecha,
                nuevaHora,
                motivo,
                usuarioId,
                false
        );
    }

    public void reprogramarAdministrativamente(
            long reservaId,
            long nuevaCanchaId,
            LocalDate nuevaFecha,
            LocalTime nuevaHora,
            String motivo,
            long usuarioId) {

        ejecutarReprogramacion(
                reservaId,
                nuevaCanchaId,
                nuevaFecha,
                nuevaHora,
                motivo,
                usuarioId,
                true
        );
    }
    
    private void ejecutarReprogramacion(
            long reservaId,
            long nuevaCanchaId,
            LocalDate nuevaFecha,
            LocalTime nuevaHora,
            String motivo,
            long usuarioId,
            boolean administrativa) {

        Reserva reserva = reservaService.buscar(reservaId);

        validar(
                reserva,
                nuevaCanchaId,
                nuevaFecha,
                nuevaHora,
                motivo,
                usuarioId
        );

        if (!administrativa) {
            politicaService.validarPlazoCancelacion(reserva);
        }

        politicaService.validarAnticipacionMinima(
                nuevaFecha,
                nuevaHora
        );

        String motivoNormalizado = motivo
                .trim()
                .replaceAll("\\s+", " ");

        if (administrativa) {
            motivoNormalizado =
                    "Reprogramación administrativa: "
                            + motivoNormalizado;
        }

        ReprogramacionReserva historial = crearHistorial(
                reserva,
                nuevaCanchaId,
                nuevaFecha,
                nuevaHora,
                motivoNormalizado,
                usuarioId
        );

        reserva.setCanchaId(nuevaCanchaId);
        reserva.setFecha(nuevaFecha);
        reserva.setHoraInicio(nuevaHora);
        reserva.setUsuarioId(usuarioId);

        reservaService.guardar(reserva);

        historial.setHoraFinNueva(
                reserva.getHoraFin()
        );

        historial.setPrecioNuevo(
                reserva.getPrecioTotal()
        );

        historialDAO.guardar(historial);
    }

    public List<ReprogramacionReserva> listarHistorial(long reservaId) {
        if (reservaId <= 0) {
            throw new IllegalArgumentException("El ID de reserva no es válido.");
        }
        return historialDAO.listarPorReserva(reservaId);
    }

    private void validar(Reserva reserva, long canchaId, LocalDate fecha,
            LocalTime hora, String motivo, long usuarioId) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalArgumentException(
                    "Solo se pueden reprogramar reservas pendientes o confirmadas.");
        }
        if (canchaId <= 0 || fecha == null || hora == null) {
            throw new IllegalArgumentException(
                    "La nueva cancha, fecha y hora son obligatorias.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "El motivo de la reprogramación es obligatorio.");
        }
        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El usuario que reprograma es obligatorio.");
        }
    }

    private ReprogramacionReserva crearHistorial(
            Reserva reserva, long canchaId, LocalDate fecha,
            LocalTime hora, String motivo, long usuarioId) {
        ReprogramacionReserva historial = new ReprogramacionReserva();
        historial.setReservaId(reserva.getId());
        historial.setCanchaAnteriorId(reserva.getCanchaId());
        historial.setFechaAnterior(reserva.getFecha());
        historial.setHoraInicioAnterior(reserva.getHoraInicio());
        historial.setHoraFinAnterior(reserva.getHoraFin());
        historial.setCanchaNuevaId(canchaId);
        historial.setFechaNueva(fecha);
        historial.setHoraInicioNueva(hora);
        historial.setHoraFinNueva(hora);
        historial.setPrecioAnterior(
                reserva.getPrecioTotal() == null
                        ? BigDecimal.ZERO : reserva.getPrecioTotal());
        historial.setPrecioNuevo(BigDecimal.ZERO);
        historial.setMotivo(motivo);
        historial.setUsuarioId(usuarioId);
        return historial;
    }
}
