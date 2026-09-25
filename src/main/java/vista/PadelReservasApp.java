package vista;

import javafx.application.Application;
import javafx.stage.Stage;

public class PadelReservasApp extends Application {

    @Override
    public void start(Stage escenarioPrincipal) {
        Navegacion.inicializar(escenarioPrincipal);
        Navegacion.mostrarLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
