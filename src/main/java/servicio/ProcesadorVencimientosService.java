package servicio;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;

public final class ProcesadorVencimientosService {

    private static final long INTERVALO_PREDETERMINADO_SEGUNDOS = 60L;
    private static final long INTERVALO_MINIMO_SEGUNDOS = 5L;
    private static final long INTERVALO_MAXIMO_SEGUNDOS = 3600L;
    private static final String PROPIEDAD_INTERVALO =
            "vencimientos.intervalo.segundos";
    private static final String VARIABLE_INTERVALO =
            "VENCIMIENTOS_INTERVALO_SEGUNDOS";

    private final ReservaService reservaService;
    private final long intervaloSegundos;
    private final AtomicBoolean iniciado = new AtomicBoolean(false);
    private final AtomicBoolean procesando = new AtomicBoolean(false);

    private ScheduledExecutorService ejecutor;
    private volatile IntConsumer alProcesarVencimientos;

    public ProcesadorVencimientosService() {
        this(new ReservaService(), obtenerIntervaloConfigurado());
    }

    public ProcesadorVencimientosService(ReservaService reservaService) {
        this(reservaService, obtenerIntervaloConfigurado());
    }

    public ProcesadorVencimientosService(
            ReservaService reservaService,
            long intervaloSegundos) {
        if (reservaService == null) {
            throw new IllegalArgumentException(
                    "El servicio de reservas no puede ser nulo.");
        }
        if (intervaloSegundos < INTERVALO_MINIMO_SEGUNDOS
                || intervaloSegundos > INTERVALO_MAXIMO_SEGUNDOS) {
            throw new IllegalArgumentException(
                    "El intervalo de vencimientos debe estar entre "
                            + INTERVALO_MINIMO_SEGUNDOS + " y "
                            + INTERVALO_MAXIMO_SEGUNDOS + " segundos.");
        }
        this.reservaService = reservaService;
        this.intervaloSegundos = intervaloSegundos;
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
                    "procesador-vencimientos-reservas");
            hilo.setDaemon(true);
            hilo.setUncaughtExceptionHandler(
                    (thread, error) -> error.printStackTrace());
            return hilo;
        };

        ejecutor = Executors.newSingleThreadScheduledExecutor(
                fabricaHilos);
        ejecutor.scheduleWithFixedDelay(
                this::procesarConSeguridad,
                0L,
                intervaloSegundos,
                TimeUnit.SECONDS);

        System.out.println(
                "Procesador de vencimientos iniciado. Intervalo: "
                        + intervaloSegundos + " segundos.");
    }

    private void procesarConSeguridad() {
        if (!iniciado.get() || !procesando.compareAndSet(false, true)) {
            return;
        }

        try {
            int cantidad = reservaService.expirarReservasPendientes();
            if (cantidad > 0) {
                System.out.println(
                        "Reservas web pendientes expiradas automáticamente: "
                                + cantidad);
            }
            notificarProcesamiento(cantidad);
        } catch (RuntimeException exception) {
            System.err.println(
                    "No se pudieron procesar los vencimientos "
                            + "de reservas pendientes.");
            exception.printStackTrace();
        } finally {
            procesando.set(false);
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
                    "No se pudo notificar el procesamiento de vencimientos.");
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
            procesando.set(false);
            System.out.println("Procesador de vencimientos detenido.");
        }
    }

    public boolean estaIniciado() {
        return iniciado.get();
    }

    public boolean estaProcesando() {
        return procesando.get();
    }

    public long getIntervaloSegundos() {
        return intervaloSegundos;
    }

    private static long obtenerIntervaloConfigurado() {
        String valor = System.getProperty(PROPIEDAD_INTERVALO);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(VARIABLE_INTERVALO);
        }
        if (valor == null || valor.isBlank()) {
            return INTERVALO_PREDETERMINADO_SEGUNDOS;
        }

        try {
            long intervalo = Long.parseLong(valor.trim());
            if (intervalo < INTERVALO_MINIMO_SEGUNDOS
                    || intervalo > INTERVALO_MAXIMO_SEGUNDOS) {
                throw new NumberFormatException();
            }
            return intervalo;
        } catch (NumberFormatException exception) {
            System.err.println(
                    "Intervalo de vencimientos inválido: " + valor
                            + ". Se utilizarán "
                            + INTERVALO_PREDETERMINADO_SEGUNDOS
                            + " segundos.");
            return INTERVALO_PREDETERMINADO_SEGUNDOS;
        }
    }
}
