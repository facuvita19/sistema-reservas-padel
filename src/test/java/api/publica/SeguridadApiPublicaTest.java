package api.publica;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import api.publica.SeguridadApiPublica.CuerpoDemasiadoGrandeException;
import api.publica.SeguridadApiPublica.DemasiadasSolicitudesException;

class SeguridadApiPublicaTest {

    @Test
    void aceptaCuerpoDentroDelLimite() throws Exception {
        SeguridadApiPublica seguridad = new SeguridadApiPublica();
        byte[] esperado = "{\"dato\":\"ok\"}"
                .getBytes(StandardCharsets.UTF_8);

        byte[] resultado = seguridad.leerCuerpoLimitado(
                new ByteArrayInputStream(esperado));

        assertArrayEquals(esperado, resultado);
    }

    @Test
    void rechazaCuerpoSuperiorAlLimite() {
        SeguridadApiPublica seguridad = new SeguridadApiPublica();
        byte[] cuerpo = new byte[
                SeguridadApiPublica.MAXIMO_JSON_BYTES + 1];

        assertThrows(
                CuerpoDemasiadoGrandeException.class,
                () -> seguridad.leerCuerpoLimitado(
                        new ByteArrayInputStream(cuerpo)));
    }

    @Test
    void limitaCreacionesPorIp() {
        SeguridadApiPublica seguridad = new SeguridadApiPublica();
        String ip = "192.168.1.50";

        for (int i = 0; i < 8; i++) {
            seguridad.validarCreacion(ip);
        }

        DemasiadasSolicitudesException error = assertThrows(
                DemasiadasSolicitudesException.class,
                () -> seguridad.validarCreacion(ip));

        assertTrue(error.getSegundosEspera() > 0);
    }

    @Test
    void normalizaDireccionIp() throws Exception {
        SeguridadApiPublica seguridad = new SeguridadApiPublica();

        assertEquals("127.0.0.1", seguridad.normalizarIp(
                InetAddress.getByName("127.0.0.1")));
        assertEquals("desconocida", seguridad.normalizarIp(null));
    }
}
