package servicio;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import negocio.ConfiguracionComplejo;
import negocio.Reserva;

public class PoliticaReservaService {

    private final ConfiguracionComplejoService configuracionService;
    private final Clock reloj;

    public PoliticaReservaService() {
        this(
                new ConfiguracionComplejoService(),
                Clock.systemDefaultZone()
        );
    }

    public PoliticaReservaService(
            ConfiguracionComplejoService configuracionService,
            Clock reloj) {

        if (configuracionService == null || reloj == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de políticas no pueden ser nulas."
            );
        }

        this.configuracionService = configuracionService;
        this.reloj = reloj;
    }

    public void validarAnticipacionMinima(
            LocalDate fecha,
            LocalTime horaInicio) {

        if (fecha == null || horaInicio == null) {
            throw new IllegalArgumentException(
                    "La fecha y la hora de la reserva son obligatorias."
            );
        }

        ConfiguracionComplejo configuracion =
                configuracionService.obtener();

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime inicioReserva = LocalDateTime.of(
                fecha,
                horaInicio
        );

        LocalDateTime primerHorarioPermitido = ahora.plusHours(
                configuracion.getAnticipacionMinimaHoras()
        );

        if (inicioReserva.isBefore(primerHorarioPermitido)) {
            throw new IllegalArgumentException(
                    "La reserva debe realizarse con al menos "
                            + configuracion.getAnticipacionMinimaHoras()
                            + " hora(s) de anticipación."
            );
        }
    }

    public void validarPlazoCancelacion(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException(
                    "La reserva es obligatoria."
            );
        }
        if (reserva.getFecha() == null
                || reserva.getHoraInicio() == null) {
            throw new IllegalArgumentException(
                    "La reserva no tiene fecha u hora válida."
            );
        }

        ConfiguracionComplejo configuracion =
                configuracionService.obtener();

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime inicioReserva = LocalDateTime.of(
                reserva.getFecha(),
                reserva.getHoraInicio()
        );

        long horasRestantes = Duration.between(
                ahora,
                inicioReserva
        ).toHours();

        if (horasRestantes
                < configuracion.getCancelacionMinimaHoras()) {
            throw new IllegalArgumentException(
                    "La cancelación debe realizarse con al menos "
                            + configuracion.getCancelacionMinimaHoras()
                            + " hora(s) de anticipación."
            );
        }
    }

    public boolean puedeCancelarSinExcepcion(Reserva reserva) {
        try {
            validarPlazoCancelacion(reserva);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
