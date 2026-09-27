package servicio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.ConfiguracionComplejoDAO;
import negocio.Cliente;
import negocio.ConfiguracionComplejo;
import negocio.Reserva;

class MensajeReservaServiceTest {

    private MensajeReservaService service;
    private Cliente cliente;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        ConfiguracionComplejo configuracion =
                new ConfiguracionComplejo();
        configuracion.setNombreComercial("Arena Padel");
        configuracion.setMoneda("ARS");

        ConfiguracionComplejoDAO dao =
                new ConfiguracionComplejoDAO() {
                    @Override
                    public ConfiguracionComplejo obtener() {
                        return configuracion;
                    }

                    @Override
                    public void guardar(
                            ConfiguracionComplejo valor) {
                    }
                };

        service = new MensajeReservaService(
                new ConfiguracionComplejoService(dao)
        );

        cliente = new Cliente();
        cliente.setNombre("Facundo");

        reserva = new Reserva();
        reserva.setFecha(LocalDate.of(2026, 9, 27));
        reserva.setHoraInicio(LocalTime.of(18, 0));
        reserva.setNombreCancha("Cancha Central");
    }

    @Test
    void creaConfirmacionConDatosDeReserva() {
        String mensaje = service.crearConfirmacion(
                cliente,
                reserva
        );

        assertTrue(mensaje.contains("Facundo"));
        assertTrue(mensaje.contains("27/09/2026"));
        assertTrue(mensaje.contains("18:00"));
        assertTrue(mensaje.contains("Cancha Central"));
    }

    @Test
    void creaSolicitudDeSeniaConVencimiento() {
        reserva.setFechaVencimiento(
                LocalDateTime.of(2026, 9, 26, 21, 15)
        );

        String mensaje = service.crearSolicitudSenia(
                cliente,
                reserva,
                new BigDecimal("10000.00")
        );

        assertTrue(mensaje.contains("ARS 10000.00"));
        assertTrue(mensaje.contains("26/09/2026"));
        assertTrue(mensaje.contains("21:15"));
    }

    @Test
    void creaAvisoDeSaldo() {
        String mensaje = service.crearAvisoSaldo(
                cliente,
                reserva,
                new BigDecimal("30000.00")
        );

        assertTrue(mensaje.contains("ARS 30000.00"));
    }
}
