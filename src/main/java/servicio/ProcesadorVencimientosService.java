package servicio;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;

public final class ProcesadorVencimientosService {

    private static final long INTERVALO_SEGUNDOS = 60L;

    private final ReservaService reservaService;
    private final AtomicBoolean iniciado = new AtomicBoolean(false);

    private ScheduledExecutorService ejecutor;
    private volatile IntConsumer alProcesarVencimientos;

    public ProcesadorVencimientosService() {
        this(new ReservaService());
    }

    public ProcesadorVencimientosService(ReservaService reservaService) {
        if (reservaService == null) {
            throw new IllegalArgumentException(
                    "El servicio de reservas no puede ser nulo."
            );
        }

        this.reservaService = reservaService;
    }

    public void setAlProcesarVencimientos(
            IntConsumer alProcesarVencimientos) {

        this.alProcesarVencimientos = alProcesarVencimientos;
    }

    public void iniciar() {
        if (!iniciado.compareAndSet(false, true)) {
            return;
        }

        ThreadFactory fabricaHilos = tarea -> {
            Thread hilo = new Thread(
                    tarea,
                    "procesador-vencimientos-reservas"
            );
            hilo.setDaemon(true);
            hilo.setUncaughtExceptionHandler(
                    (thread, error) -> error.printStackTrace()
            );
            return hilo;
        };

        ejecutor = Executors.newSingleThreadScheduledExecutor(
                fabricaHilos
        );

        ejecutor.scheduleWithFixedDelay(
                this::procesarConSeguridad,
                0L,
                INTERVALO_SEGUNDOS,
                TimeUnit.SECONDS
        );
    }

    private void procesarConSeguridad() {
        try {
            int cantidad = reservaService.expirarReservasPendientes();

            if (cantidad > 0) {
                System.out.println(
                        "Reservas pendientes expiradas automáticamente: "
                                + cantidad
                );
            }

            notificarProcesamiento(cantidad);

        } catch (RuntimeException exception) {
            System.err.println(
                    "No se pudieron procesar los vencimientos "
                            + "de reservas pendientes."
            );
            exception.printStackTrace();
        }
    }

    private void notificarProcesamiento(int cantidadExpirada) {
        IntConsumer listener = alProcesarVencimientos;

        if (listener == null) {
            return;
        }

        try {
            listener.accept(cantidadExpirada);
        } catch (RuntimeException exception) {
            System.err.println(
                    "No se pudo notificar la actualización del dashboard."
            );
            exception.printStackTrace();
        }
    }

    public void detener() {
        if (!iniciado.compareAndSet(true, false)) {
            return;
        }

        if (ejecutor == null) {
            return;
        }

        ejecutor.shutdown();

        try {
            if (!ejecutor.awaitTermination(3L, TimeUnit.SECONDS)) {
                ejecutor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            ejecutor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            ejecutor = null;
        }
    }

    public boolean estaIniciado() {
        return iniciado.get();
    }
}
