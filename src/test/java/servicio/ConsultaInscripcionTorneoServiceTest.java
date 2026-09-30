package servicio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConsultaInscripcionTorneoServiceTest {
    @Test
    void nombreCompletoEliminaEspaciosExternos() {
        var jugador = new ConsultaInscripcionTorneoService.JugadorResumen(
                10L, " Ana ", " Perez ", "1160001001",
                null, "SIN_VINCULAR", false);
        assertTrue(jugador.nombreCompleto().equals("Ana Perez"));
        assertTrue(!jugador.vinculado());
        assertTrue(jugador.estadoVinculacion().equals("Sin vincular"));
    }
}
