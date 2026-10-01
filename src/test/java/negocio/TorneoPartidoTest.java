package negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class TorneoPartidoTest {

    @Test
    void reconoceProgramacionCompleta() {
        TorneoPartido partido = new TorneoPartido();
        assertFalse(partido.estaProgramado());

        partido.setFecha(LocalDate.of(2026, 10, 10));
        partido.setHoraInicio(LocalTime.of(18, 0));
        partido.setHoraFin(LocalTime.of(19, 30));
        partido.setCanchaId(1L);

        assertTrue(partido.estaProgramado());
    }

    @Test
    void identificaUnaUnicaParejaParaBye() {
        TorneoPartido partido = new TorneoPartido();
        partido.setPareja1InscripcionId(8L);

        assertEquals(8L, partido.unicaPareja());

        partido.setPareja2InscripcionId(9L);
        assertNull(partido.unicaPareja());
    }

    @Test
    void cuentaSetsGanados() {
        TorneoPartido partido = new TorneoPartido();
        partido.agregarSet(set(1, 6, 4));
        partido.agregarSet(set(2, 3, 6));
        partido.agregarSet(set(3, 10, 7));

        assertEquals(2, partido.setsGanadosPareja1());
        assertEquals(1, partido.setsGanadosPareja2());
    }

    @Test
    void conoceLaSiguienteFase() {
        assertEquals(FaseTorneo.CUARTOS, FaseTorneo.OCTAVOS.siguiente());
        assertEquals(FaseTorneo.FINAL, FaseTorneo.SEMIFINAL.siguiente());
        assertNull(FaseTorneo.FINAL.siguiente());
    }

    private TorneoPartidoSet set(int numero, int pareja1, int pareja2) {
        TorneoPartidoSet set = new TorneoPartidoSet();
        set.setNumeroSet(numero);
        set.setPuntosPareja1(pareja1);
        set.setPuntosPareja2(pareja2);
        return set;
    }
}
