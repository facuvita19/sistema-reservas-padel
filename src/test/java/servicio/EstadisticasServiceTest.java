package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.EstadisticasDAO;
import negocio.EstadisticasPadel;

class EstadisticasServiceTest {

    private EstadisticasDAODoble dao;
    private EstadisticasService service;

    @BeforeEach
    void preparar() {
        dao = new EstadisticasDAODoble();
        service = new EstadisticasService(dao);
    }

    @Test
    void obtieneEstadisticasParaRangoValido() {
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.minusMonths(1);
        EstadisticasPadel resultado = service.obtener(desde, hasta);
        assertSame(dao.resultado, resultado);
        assertEquals(desde, dao.desde);
        assertEquals(hasta, dao.hasta);
    }

    @Test
    void rechazaFechasNulasInvertidasYFuturas() {
        assertThrows(IllegalArgumentException.class,
                () -> service.obtener(null, LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> service.obtener(LocalDate.now(), null));
        assertThrows(IllegalArgumentException.class,
                () -> service.obtener(LocalDate.now(), LocalDate.now().minusDays(1)));
        assertThrows(IllegalArgumentException.class,
                () -> service.obtener(LocalDate.now(), LocalDate.now().plusDays(1)));
    }

    @Test
    void calculaTasasDeCancelacionYAusencia() {
        EstadisticasPadel estadisticas = new EstadisticasPadel();
        estadisticas.setTotalReservas(20);
        estadisticas.setReservasCanceladas(3);
        estadisticas.setReservasAusentes(2);
        assertEquals(new BigDecimal("15.00"), estadisticas.getTasaCancelacion());
        assertEquals(new BigDecimal("10.00"), estadisticas.getTasaAusencia());
    }

    @Test
    void tasasSonCeroSinReservas() {
        EstadisticasPadel estadisticas = new EstadisticasPadel();
        assertEquals(BigDecimal.ZERO, estadisticas.getTasaCancelacion());
        assertEquals(BigDecimal.ZERO, estadisticas.getTasaAusencia());
    }

    private static final class EstadisticasDAODoble implements EstadisticasDAO {
        private LocalDate desde;
        private LocalDate hasta;
        private final EstadisticasPadel resultado = new EstadisticasPadel();

        @Override
        public EstadisticasPadel obtenerEstadisticas(
                LocalDate fechaDesde, LocalDate fechaHasta) {
            desde = fechaDesde;
            hasta = fechaHasta;
            return resultado;
        }
    }
}
