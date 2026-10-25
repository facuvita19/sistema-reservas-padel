package api.publica;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class SolicitudCreadaPublicaContratoTest {
    @Test
    void laRespuestaPublicaNoExponeElIdentificadorInternoDelCliente() {
        Set<String> campos = Arrays.stream(
                EstadoSolicitudWebDTO.SolicitudCreadaPublica.class.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());

        assertFalse(campos.contains("clienteId"));
        assertTrue(campos.containsAll(Set.of(
                "solicitudId", "codigoSeguimiento", "canchaId", "cancha",
                "fecha", "horaInicio", "horaFin", "importeSenia", "vencimiento")));
    }
}
