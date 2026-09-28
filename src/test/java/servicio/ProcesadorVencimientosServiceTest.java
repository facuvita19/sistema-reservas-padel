package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcesadorVencimientosServiceTest {

    @Mock
    private ReservaService reservaService;

    @Test
    void procesaAlIniciarYNotificaLaCantidad() throws Exception {
        when(reservaService.expirarReservasPendientes()).thenReturn(2);
        CountDownLatch procesado = new CountDownLatch(1);
        AtomicInteger cantidad = new AtomicInteger();
        ProcesadorVencimientosService procesador =
                new ProcesadorVencimientosService(reservaService, 5L);
        procesador.setAlProcesarVencimientos(valor -> {
            cantidad.set(valor);
            procesado.countDown();
        });

        try {
            procesador.iniciar();
            assertTrue(procesado.await(2L, TimeUnit.SECONDS));
            assertEquals(2, cantidad.get());
            verify(reservaService).expirarReservasPendientes();
        } finally {
            procesador.detener();
        }
    }

    @Test
    void rechazaIntervaloMenorAlPermitido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProcesadorVencimientosService(
                        reservaService, 4L));
    }
}
