package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import negocio.FaseTorneo;

class CuadroEliminacionTorneoServiceTest {

    @ParameterizedTest
    @CsvSource({
        "2, 2",
        "3, 4",
        "4, 4",
        "5, 8",
        "8, 8",
        "9, 16",
        "16, 16",
        "17, 32",
        "32, 32"
    })
    void calculaPotenciaDeDosParaElCuadro(
            int parejas,
            int esperado) {
        assertEquals(esperado,
                CuadroEliminacionTorneoService
                        .calcularTamanoCuadro(parejas));
    }

    @ParameterizedTest
    @CsvSource({
        "2, FINAL",
        "4, SEMIFINAL",
        "8, CUARTOS",
        "16, OCTAVOS",
        "32, DIECISEISAVOS"
    })
    void determinaLaFaseInicial(
            int tamano,
            FaseTorneo fase) {
        assertEquals(fase,
                CuadroEliminacionTorneoService.faseInicial(tamano));
    }

    @Test
    void rechazaCantidadesFueraDeRango() {
        assertThrows(IllegalArgumentException.class,
                () -> CuadroEliminacionTorneoService
                        .calcularTamanoCuadro(1));
        assertThrows(IllegalArgumentException.class,
                () -> CuadroEliminacionTorneoService
                        .calcularTamanoCuadro(33));
    }
}
