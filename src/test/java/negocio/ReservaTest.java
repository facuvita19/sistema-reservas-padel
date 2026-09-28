package negocio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class ReservaTest {

    @Test
    void detectaVencimientoPendiente() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaVencimiento(LocalDateTime.now().minusSeconds(1));

        assertTrue(reserva.estaVencida(LocalDateTime.now()));
    }

    @Test
    void noConsideraVencidaUnaReservaConfirmada() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaVencimiento(LocalDateTime.now().minusMinutes(5));

        assertFalse(reserva.estaVencida(LocalDateTime.now()));
    }

    @Test
    void detectaSuperposicionDeHorario() {
        Reserva reserva = new Reserva();
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFin(LocalTime.of(11, 30));

        assertTrue(reserva.seSuperpone(
                LocalTime.of(11, 0), LocalTime.of(12, 0)));
        assertFalse(reserva.seSuperpone(
                LocalTime.of(11, 30), LocalTime.of(13, 0)));
    }
}
