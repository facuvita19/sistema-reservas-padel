package vista;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import servicio.ProcesadorVencimientosService;

public class PadelReservasApp extends Application {

    private final ProcesadorVencimientosService procesadorVencimientos =
            new ProcesadorVencimientosService();

    @Override
    public void start(Stage escenarioPrincipal) {
        Navegacion.inicializar(escenarioPrincipal);

        procesadorVencimientos.setAlProcesarVencimientos(
                cantidadExpirada -> Platform.runLater(
                        Navegacion::actualizarDashboardSiEstaVisible
                )
        );

        procesadorVencimientos.iniciar();
        Navegacion.mostrarLogin();
    }

    @Override
    public void stop() {
        procesadorVencimientos.detener();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
