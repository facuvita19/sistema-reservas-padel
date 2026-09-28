package api.publica;

import java.util.concurrent.CountDownLatch;

import servicio.ProcesadorVencimientosService;

public final class ApiPublicaApp {
    private ApiPublicaApp() {
    }

    public static void main(String[] args) {
        ApiPublicaServer servidor = new ApiPublicaServer();
        ProcesadorVencimientosService procesadorVencimientos =
                new ProcesadorVencimientosService();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            procesadorVencimientos.detener();
            servidor.detener();
        }, "api-publica-apagado"));

        servidor.iniciar();
        procesadorVencimientos.iniciar();

        System.out.println(
                "API pública independiente en ejecución. "
                        + "Para detenerla, presioná Ctrl + C.");

        esperarIndefinidamente();
    }

    private static void esperarIndefinidamente() {
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
