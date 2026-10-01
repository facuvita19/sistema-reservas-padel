package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class FormateadorMonedaTest {

    @Test
    void formateaImportesEnterosSinCentavos() {
        assertEquals("ARS 20.000", FormateadorMoneda.pesos(
                new BigDecimal("20000.00")));
        assertEquals("ARS 18.750", FormateadorMoneda.pesos(
                new BigDecimal("18750")));
    }

    @Test
    void conservaCentavosReales() {
        assertEquals("ARS 20.000,50", FormateadorMoneda.pesos(
                new BigDecimal("20000.50")));
        assertEquals("ARS 20.000,05", FormateadorMoneda.pesos(
                new BigDecimal("20000.05")));
    }

    @Test
    void aceptaCeroYValorNulo() {
        assertEquals("ARS 0", FormateadorMoneda.pesos(BigDecimal.ZERO));
        assertEquals("ARS 0", FormateadorMoneda.pesos(null));
    }

    @Test
    void redondeaAComoMaximoDosDecimales() {
        assertEquals("ARS 1.234,57", FormateadorMoneda.pesos(
                new BigDecimal("1234.567")));
    }
}
