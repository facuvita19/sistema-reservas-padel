package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import negocio.TipoSetTorneo;
import negocio.TorneoPartidoSet;

class ResultadoPartidoTorneoServiceTest {

    @Test
    void determinaGanadoraEnDosSets() {
        assertEquals(1, ResultadoPartidoTorneoService.determinarLadoGanador(
                List.of(set(1, 6, 4), set(2, 7, 5))));
    }

    @Test
    void determinaGanadoraEnTresSetsConSuperTieBreak() {
        assertEquals(2, ResultadoPartidoTorneoService.determinarLadoGanador(
                List.of(set(1, 6, 3), set(2, 4, 6),
                        superTieBreak(3, 8, 10))));
    }

    @Test
    void rechazaSuperTieBreakSinDiferenciaDeDos() {
        assertThrows(IllegalArgumentException.class,
                () -> ResultadoPartidoTorneoService.determinarLadoGanador(
                        List.of(set(1, 6, 3), set(2, 4, 6),
                                superTieBreak(3, 10, 9))));
    }

    @Test
    void rechazaResultadoSinDosSetsGanados() {
        assertThrows(IllegalArgumentException.class,
                () -> ResultadoPartidoTorneoService.determinarLadoGanador(
                        List.of(set(1, 6, 3), set(2, 4, 6))));
    }

    @Test
    void rechazaNumeracionNoConsecutiva() {
        assertThrows(IllegalArgumentException.class,
                () -> ResultadoPartidoTorneoService.determinarLadoGanador(
                        List.of(set(1, 6, 3), set(3, 6, 2))));
    }

    private TorneoPartidoSet set(int numero, int pareja1, int pareja2) {
        TorneoPartidoSet set = new TorneoPartidoSet();
        set.setNumeroSet(numero);
        set.setTipo(TipoSetTorneo.NORMAL);
        set.setPuntosPareja1(pareja1);
        set.setPuntosPareja2(pareja2);
        return set;
    }

    private TorneoPartidoSet superTieBreak(
            int numero, int pareja1, int pareja2) {
        TorneoPartidoSet set = set(numero, pareja1, pareja2);
        set.setTipo(TipoSetTorneo.SUPER_TIE_BREAK);
        return set;
    }
}
