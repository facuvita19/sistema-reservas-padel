package vista;

import api.publica.ApiPublicaServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import servicio.ProcesadorVencimientosService;

public class PadelReservasApp extends Application {
    private final ProcesadorVencimientosService procesadorVencimientos =
            new ProcesadorVencimientosService();
    private final ApiPublicaServer apiPublica = new ApiPublicaServer();

    @Override
    public void start(Stage escenarioPrincipal) {
        Navegacion.inicializar(escenarioPrincipal);
        procesadorVencimientos.setAlProcesarVencimientos(
                cantidadExpirada -> Platform.runLater(
                        Navegacion::actualizarDashboardSiEstaVisible));
        procesadorVencimientos.iniciar();
        iniciarApiPublicaConSeguridad();
        Navegacion.mostrarLogin();
    }

    private void iniciarApiPublicaConSeguridad() {
        try {
            apiPublica.iniciar();
        } catch (RuntimeException exception) {
            System.err.println(
                    "La aplicación administrativa continuará sin la API pública.");
            exception.printStackTrace();
        }
    }

    @Override
    public void stop() {
        apiPublica.detener();
        procesadorVencimientos.detener();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
