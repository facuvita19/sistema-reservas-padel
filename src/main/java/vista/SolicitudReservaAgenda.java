package vista;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitudReservaAgenda(
        Tipo tipo,
        LocalDate fecha,
        Long canchaId,
        LocalTime horaInicio,
        Long reservaId) {

    public enum Tipo {
        NUEVA,
        EXISTENTE
    }

    public static SolicitudReservaAgenda nueva(
            LocalDate fecha,
            long canchaId,
            LocalTime horaInicio) {

        if (fecha == null || horaInicio == null || canchaId <= 0) {
            throw new IllegalArgumentException(
                    "Los datos de la nueva reserva no son válidos."
            );
        }

        return new SolicitudReservaAgenda(
                Tipo.NUEVA,
                fecha,
                canchaId,
                horaInicio,
                null
        );
    }

    public static SolicitudReservaAgenda existente(long reservaId) {
        if (reservaId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la reserva no es válido."
            );
        }

        return new SolicitudReservaAgenda(
                Tipo.EXISTENTE,
                null,
                null,
                null,
                reservaId
        );
    }
}
