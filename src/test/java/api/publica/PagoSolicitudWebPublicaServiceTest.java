package api.publica;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import api.publica.EstadoSolicitudWebDTO.EstadoSolicitud;
import api.publica.PagoClienteDTO.OrdenPagoPublica;
import negocio.OrdenPago;
import servicio.CapacidadesPagoService;
import servicio.ConsultaOrdenPagoClienteService;
import servicio.CreacionOrdenPagoClienteService;

class PagoSolicitudWebPublicaServiceTest {
    private static final String CODIGO = "0f65fba1-5f4f-4c69-b45d-29f753a2ef21";

    @Test
    void priorizaLaOrdenAprobadaSobreUnaOrdenActivaMasReciente() {
        var fixture = fixture(reserva(true, LocalDateTime.now().plusMinutes(10)));
        var activa = orden(22, "ESPERANDO_PAGO", true);
        var aprobada = orden(21, "APROBADA", false);
        when(fixture.consulta().historial(901)).thenReturn(
                new PagoClienteDTO.HistorialOrdenes(901, List.of(activa, aprobada)));

        assertEquals("APROBADA", fixture.service().actual(CODIGO).estado());
    }

    @Test
    void devuelveSinOrdenCuandoLaReservaTodaviaNoTieneOrdenes() {
        var fixture = fixture(reserva(true, LocalDateTime.now().plusMinutes(10)));
        when(fixture.consulta().historial(901)).thenReturn(
                new PagoClienteDTO.HistorialOrdenes(901, List.of()));

        var resultado = fixture.service().actual(CODIGO);
        assertEquals("SIN_ORDEN", resultado.estado());
        assertEquals(901, resultado.reservaId());
        assertEquals(0, resultado.ordenId());
    }

    @Test
    void impideCrearOrdenParaReservaNoPendiente() {
        var fixture = fixture(reserva(false, LocalDateTime.now().plusMinutes(10)));
        var error = assertThrows(IllegalArgumentException.class,
                () -> fixture.service().crear(CODIGO, "TRANSFERENCIA_MANUAL"));
        assertTrue(error.getMessage().contains("ya no admite"));
        verifyNoInteractions(fixture.creacion());
    }

    @Test
    void impideCrearOrdenCuandoLaProteccionVencio() {
        var fixture = fixture(reserva(true, LocalDateTime.now().minusSeconds(1)));
        var error = assertThrows(IllegalArgumentException.class,
                () -> fixture.service().crear(CODIGO, "TRANSFERENCIA_MANUAL"));
        assertTrue(error.getMessage().contains("ya venció"));
        verifyNoInteractions(fixture.creacion());
    }

    @Test
    void creaTransferenciaManualPorDefecto() {
        var estado = reserva(true, LocalDateTime.now().plusMinutes(10));
        var fixture = fixture(estado);
        var dominio = new OrdenPago();
        when(fixture.creacion().crearManual(901, estado.importeSenia(), estado.vencimiento()))
                .thenReturn(dominio);
        when(fixture.consulta().convertir(dominio)).thenReturn(
                orden(31, "ESPERANDO_VERIFICACION", true));

        var resultado = fixture.service().crear(CODIGO, " ");
        assertEquals("ESPERANDO_VERIFICACION", resultado.estado());
        verify(fixture.creacion()).crearManual(901, estado.importeSenia(), estado.vencimiento());
    }

    @Test
    void creaPagoOnlineConDescripcionDeLaCancha() {
        var estado = reserva(true, LocalDateTime.now().plusMinutes(10));
        var fixture = fixture(estado);
        var dominio = new OrdenPago();
        when(fixture.creacion().crearOnline(901, estado.importeSenia(), estado.vencimiento(),
                "Seña de reserva Cancha Central")).thenReturn(dominio);
        when(fixture.consulta().convertir(dominio)).thenReturn(
                orden(32, "ESPERANDO_PAGO", true));

        var resultado = fixture.service().crear(CODIGO, "pago_online");
        assertEquals("ESPERANDO_PAGO", resultado.estado());
    }

    @Test
    void rechazaMetodoDesconocido() {
        var fixture = fixture(reserva(true, LocalDateTime.now().plusMinutes(10)));
        assertThrows(IllegalArgumentException.class,
                () -> fixture.service().crear(CODIGO, "EFECTIVO"));
        verifyNoInteractions(fixture.creacion());
    }

    private Fixture fixture(EstadoSolicitud estado) {
        var estados = mock(EstadoSolicitudWebPublicaService.class);
        var capacidades = mock(CapacidadesPagoService.class);
        var creacion = mock(CreacionOrdenPagoClienteService.class);
        var consulta = mock(ConsultaOrdenPagoClienteService.class);
        when(estados.consultar(CODIGO)).thenReturn(estado);
        return new Fixture(new PagoSolicitudWebPublicaService(
                estados, capacidades, creacion, consulta), creacion, consulta);
    }

    private EstadoSolicitud reserva(boolean pendiente, LocalDateTime vencimiento) {
        return new EstadoSolicitud(CODIGO, 901, pendiente ? "PENDIENTE" : "CONFIRMADA",
                "Cancha Central", LocalDate.of(2026, 10, 25), LocalTime.of(17, 0),
                LocalTime.of(18, 30), new BigDecimal("25000.00"), BigDecimal.ZERO,
                new BigDecimal("6250.00"), new BigDecimal("25000.00"), vencimiento,
                null, null, pendiente, !pendiente, false, "ARS", "Estado de prueba");
    }

    private OrdenPagoPublica orden(long id, String estado, boolean actualizable) {
        return new OrdenPagoPublica(id, 901, "MERCADO_PAGO", "PAGO_ONLINE", estado,
                new BigDecimal("6250.00"), "ARS", LocalDateTime.now().plusMinutes(10),
                600, true, false, false, actualizable,
                "https://example.test/checkout", "Prueba");
    }

    private record Fixture(PagoSolicitudWebPublicaService service,
            CreacionOrdenPagoClienteService creacion,
            ConsultaOrdenPagoClienteService consulta) { }
}
