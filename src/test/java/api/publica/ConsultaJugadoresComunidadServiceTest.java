package api.publica;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class ConsultaJugadoresComunidadServiceTest {
    @Test
    void validaLimitesDePaginacion() {
        assertDoesNotThrow(() -> ConsultaJugadoresComunidadService.validarPaginacion(1, 12));
        assertThrows(IllegalArgumentException.class,
                () -> ConsultaJugadoresComunidadService.validarPaginacion(0, 12));
        assertThrows(IllegalArgumentException.class,
                () -> ConsultaJugadoresComunidadService.validarPaginacion(1, 49));
    }

    @Test
    void normalizaNombreEInicialesSinExponerIdentidadPrivada() {
        assertEquals("Maria de los Angeles Rodriguez",
                ConsultaJugadoresComunidadService.limpiarNombre("  Maria   de los Angeles Rodriguez "));
        assertEquals("MR", ConsultaJugadoresComunidadService.iniciales(
                "Maria de los Angeles Rodriguez"));
    }

    @Test
    void dtoPublicoNoIncluyeDatosSensibles() {
        Set<String> componentes = Arrays.stream(
                JugadorComunidadPublicoDTO.Jugador.class.getRecordComponents())
                .map(RecordComponent::getName).collect(Collectors.toSet());
        assertTrue(componentes.containsAll(Set.of("perfilId", "nombre", "iniciales",
                "posicion", "categoria", "buscaTurnos", "buscaTorneos")));
        assertFalse(componentes.contains("telefono"));
        assertFalse(componentes.contains("email"));
        assertFalse(componentes.contains("documento"));
        assertFalse(componentes.contains("clienteId"));
    }

    @Test
    void consultaExigeOptInPerfilDefinitivoActivoYDisponible() throws Exception {
        String codigo = Files.readString(Path.of(
                "src/main/java/api/publica/ConsultaJugadoresComunidadService.java"));
        assertTrue(codigo.contains("p.activo=TRUE"));
        assertTrue(codigo.contains("p.estado='DEFINITIVO'"));
        assertTrue(codigo.contains("p.disponible_para_jugar=TRUE"));
        assertTrue(codigo.contains("cp.buscar_turnos=TRUE OR cp.buscar_torneos=TRUE"));
        assertTrue(codigo.contains("ORDER BY LOWER(TRIM(p.nombre_publico)),p.id"));
        for (String privado : new String[]{"c.documento", "c.telefono", "c.email"}) {
            assertFalse(codigo.contains(privado));
        }
    }
}
