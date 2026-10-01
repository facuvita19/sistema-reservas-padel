package vista;

import java.net.URL;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class Dialogos {

    private static final String CSS = "/css/dialogos.css";

    private Dialogos() {
    }

    public static void preparar(Dialog<?> dialogo, String clase) {
        if (dialogo == null) return;
        DialogPane panel = dialogo.getDialogPane();
        URL css = Dialogos.class.getResource(CSS);
        if (css != null && !panel.getStylesheets().contains(css.toExternalForm())) {
            panel.getStylesheets().add(css.toExternalForm());
        }
        panel.getStyleClass().add("admin-dialog");
        if (clase != null && !clase.isBlank()) panel.getStyleClass().add(clase);
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.setResizable(true);
        dialogo.setOnShown(evento -> {
            if (dialogo.getDialogPane().getScene() != null
                    && dialogo.getDialogPane().getScene().getWindow() instanceof Stage stage) {
                stage.setMinWidth(Math.min(stage.getWidth(), 520));
                stage.centerOnScreen();
            }
        });
    }

    public static boolean confirmar(String titulo, String mensaje) {
        return confirmar(titulo, mensaje, false);
    }

    public static boolean confirmarPeligro(String titulo, String mensaje) {
        return confirmar(titulo, mensaje, true);
    }

    private static boolean confirmar(String titulo, String mensaje, boolean peligro) {
        ButtonType aceptar = new ButtonType(
                peligro ? "CONTINUAR" : "CONFIRMAR",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("VOLVER", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, aceptar, cancelar);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        preparar(alerta, peligro ? "dialog-danger" : "dialog-confirm");
        Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado.orElse(cancelar) == aceptar;
    }

    public static void error(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje, "dialog-error");
    }

    public static void exito(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje, "dialog-success");
    }

    public static void informacion(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje, "dialog-info");
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje, String clase) {
        ButtonType cerrar = new ButtonType("ENTENDIDO", ButtonBar.ButtonData.OK_DONE);
        Alert alerta = new Alert(tipo, mensaje, cerrar);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        preparar(alerta, clase);
        alerta.showAndWait();
    }
}
