package api.publica;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import negocio.PosicionJugador;

class ConsultaJugadoresComunidadFiltrosTest {
    @Test
    void normalizaYProtege() {
        assertEquals("maria rodriguez",
                ConsultaJugadoresComunidadService.normalizarBusqueda(
                        "  MARIA   RODRIGUEZ "));
        String barra = Character.toString(92);
        assertEquals("100" + barra + "% drive" + barra + "_",
                ConsultaJugadoresComunidadService.escaparLike("100% drive_"));
        assertThrows(IllegalArgumentException.class,
                () -> ConsultaJugadoresComunidadService.normalizarBusqueda(
                        "x".repeat(81)));
    }

    @Test
    void validaValoresPublicos() {
        assertEquals(ConsultaJugadoresComunidadService.Objetivo.TURNOS,
                ConsultaJugadoresComunidadService.objetivo("turnos"));
        assertEquals(ConsultaJugadoresComunidadService.Orden.CATEGORIA,
                ConsultaJugadoresComunidadService.orden("categoria"));
        assertEquals(PosicionJugador.REVES,
                ConsultaJugadoresComunidadService.posicion("reves"));
        assertThrows(IllegalArgumentException.class,
                () -> ConsultaJugadoresComunidadService.orden("nombre desc"));
    }

    @Test
    void categoriaDebeSerPositiva() {
        assertThrows(IllegalArgumentException.class,
                () -> new ConsultaJugadoresComunidadService.Filtros(
                        null, 0L, null, null, null, null));
    }
}
