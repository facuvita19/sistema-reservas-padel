package api.publica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import negocio.Cancha;
import negocio.ConfiguracionComplejo;
import servicio.CanchaService;
import servicio.ConfiguracionComplejoService;
import servicio.ReservaService;

@ExtendWith(MockitoExtension.class)
class DisponibilidadPublicaServiceTest {

    @Mock
    private CanchaService canchaService;
    @Mock
    private ReservaService reservaService;
    @Mock
    private ConfiguracionComplejoService configuracionService;

    private DisponibilidadPublicaService service;
    private Cancha cancha;

    @BeforeEach
    void preparar() {
        service = new DisponibilidadPublicaService(
                canchaService,
                reservaService,
                configuracionService);

        cancha = new Cancha();
        cancha.setId(1L);
        cancha.setNombre("Cancha Test");
        cancha.setActivo(true);
        cancha.setHoraApertura(LocalTime.of(8, 0));
        cancha.setHoraCierre(LocalTime.of(23, 0));
        cancha.setDuracionReserva(90);
        cancha.setPrecio(new BigDecimal("10000.00"));
        cancha.setDiasDisponibles(EnumSet.allOf(DayOfWeek.class));
    }

    @Test
    void listaSoloCanchasActivasOrdenadas() {
        Cancha inactiva = new Cancha();
        inactiva.setId(2L);
        inactiva.setNombre("Cancha Inactiva");
        inactiva.setActivo(false);

        Cancha anterior = new Cancha();
        anterior.setId(3L);
        anterior.setNombre("Arena 1");
        anterior.setActivo(true);
        anterior.setPrecio(new BigDecimal("8000.00"));
        anterior.setDiasDisponibles(EnumSet.allOf(DayOfWeek.class));

        when(canchaService.listar()).thenReturn(
                List.of(cancha, inactiva, anterior));
        when(configuracionService.calcularSenia(
                new BigDecimal("10000.00")))
                .thenReturn(new BigDecimal("2500.00"));
        when(configuracionService.calcularSenia(
                new BigDecimal("8000.00")))
                .thenReturn(new BigDecimal("2000.00"));

        var resultado = service.listarCanchas();

        assertEquals(2, resultado.size());
        assertEquals("Arena 1", resultado.get(0).nombre());
        assertEquals("Cancha Test", resultado.get(1).nombre());
    }

    @Test
    void calculaHorarioYSenia() {
        LocalDate fecha = LocalDate.now().plusDays(2);
        ConfiguracionComplejo configuracion = new ConfiguracionComplejo();
        configuracion.setAnticipacionMinimaHoras(0);

        when(canchaService.buscar(1L)).thenReturn(cancha);
        when(configuracionService.obtener()).thenReturn(configuracion);
        when(configuracionService.calcularSenia(cancha.getPrecio()))
                .thenReturn(new BigDecimal("2500.00"));
        when(reservaService.listarHorariosDisponibles(1L, fecha, 0L))
                .thenReturn(List.of(LocalTime.of(10, 0)));

        var resultado = service.obtenerDisponibilidad(1L, fecha);

        assertEquals(1, resultado.horarios().size());
        assertEquals(LocalTime.of(10, 0),
                resultado.horarios().get(0).horaInicio());
        assertEquals(LocalTime.of(11, 30),
                resultado.horarios().get(0).horaFin());
        assertEquals(new BigDecimal("2500.00"),
                resultado.horarios().get(0).importeSenia());
    }

    @Test
    void rechazaFechaPasada() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.obtenerDisponibilidad(
                        1L, LocalDate.now().minusDays(1)));
    }
}
