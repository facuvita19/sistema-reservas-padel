package servicio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.ConfiguracionComplejoDAO;
import negocio.ConfiguracionComplejo;
import negocio.Reserva;

class PoliticaReservaServiceTest {

    private static final ZoneId ZONA =
            ZoneId.of("America/Argentina/Buenos_Aires");

    private PoliticaReservaService service;
    private ConfiguracionComplejo configuracion;

    @BeforeEach
    void preparar() {
        configuracion = new ConfiguracionComplejo();
        configuracion.setAnticipacionMinimaHoras(2);
        configuracion.setCancelacionMinimaHoras(12);

        ConfiguracionComplejoService configuracionService =
                new ConfiguracionComplejoService(
                        new ConfiguracionDAODoble(configuracion)
                );

        Clock reloj = Clock.fixed(
                Instant.parse("2026-09-25T16:00:00Z"),
                ZONA
        );

        service = new PoliticaReservaService(
                configuracionService,
                reloj
        );
    }

    @Test
    void permiteReservaConAnticipacionSuficiente() {
        assertDoesNotThrow(() ->
                service.validarAnticipacionMinima(
                        LocalDate.of(2026, 9, 25),
                        LocalTime.of(15, 0)
                )
        );
    }

    @Test
    void rechazaReservaSinAnticipacionSuficiente() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarAnticipacionMinima(
                        LocalDate.of(2026, 9, 25),
                        LocalTime.of(14, 30)
                )
        );
    }

    @Test
    void permiteCancelarAntesDelLimite() {
        Reserva reserva = crearReserva(
                LocalDate.of(2026, 9, 26),
                LocalTime.of(8, 0)
        );

        assertDoesNotThrow(() ->
                service.validarPlazoCancelacion(reserva)
        );
        assertTrue(service.puedeCancelarSinExcepcion(reserva));
    }

    @Test
    void rechazaCancelarDentroDelLimite() {
        Reserva reserva = crearReserva(
                LocalDate.of(2026, 9, 25),
                LocalTime.of(20, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarPlazoCancelacion(reserva)
        );
        assertFalse(service.puedeCancelarSinExcepcion(reserva));
    }

    @Test
    void permitePoliticaSinAnticipacion() {
        configuracion.setAnticipacionMinimaHoras(0);

        assertDoesNotThrow(() ->
                service.validarAnticipacionMinima(
                        LocalDate.of(2026, 9, 25),
                        LocalTime.of(13, 0)
                )
        );
    }

    @Test
    void rechazaDatosNulos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarAnticipacionMinima(null, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarPlazoCancelacion(null)
        );
    }

    private Reserva crearReserva(
            LocalDate fecha,
            LocalTime horaInicio) {

        Reserva reserva = new Reserva();
        reserva.setFecha(fecha);
        reserva.setHoraInicio(horaInicio);
        return reserva;
    }

    private static final class ConfiguracionDAODoble
            implements ConfiguracionComplejoDAO {

        private final ConfiguracionComplejo configuracion;

        private ConfiguracionDAODoble(
                ConfiguracionComplejo configuracion) {
            this.configuracion = configuracion;
        }

        @Override
        public ConfiguracionComplejo obtener() {
            return configuracion;
        }

        @Override
        public void guardar(
                ConfiguracionComplejo configuracion) {
        }
    }
}
