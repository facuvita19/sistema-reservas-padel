package dao;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import negocio.FaseTorneo;
import negocio.TorneoPartido;

class TorneoPartidoDAOMySQLTest {

    private final TorneoPartidoDAO dao = new TorneoPartidoDAOMySQL();

    @Test
    void rechazaHorarioIncompletoAntesDeAccederABase() {
        TorneoPartido partido = partidoBase();
        partido.setFecha(LocalDate.of(2026, 10, 13));
        partido.setHoraInicio(LocalTime.of(18, 0));

        assertThrows(IllegalArgumentException.class,
                () -> dao.guardar(null, partido));
    }

    @Test
    void rechazaDatosDeDisponibilidadInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> dao.horarioOcupado(
                        0, LocalDate.now(), LocalTime.NOON,
                        LocalTime.NOON.plusHours(1), 0));
    }

    private TorneoPartido partidoBase() {
        TorneoPartido partido = new TorneoPartido();
        partido.setTorneoCategoriaId(1);
        partido.setFase(FaseTorneo.FINAL);
        partido.setOrdenFase(1);
        partido.setPareja1InscripcionId(1L);
        partido.setPareja2InscripcionId(2L);
        return partido;
    }
}
