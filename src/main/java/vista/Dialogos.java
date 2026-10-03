package vista;

import java.net.URL;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
        alerta.setGraphic(null);
        preparar(alerta, peligro ? "dialog-danger" : "dialog-confirm");
        Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado.orElse(cancelar) == aceptar;
    }

    public enum Opcion {
        PRINCIPAL,
        ALTERNATIVA,
        VOLVER
    }

    public static Opcion elegir(
            String titulo,
            String encabezado,
            String mensaje,
            String textoPrincipal,
            String textoAlternativa,
            boolean principalPeligrosa,
            boolean alternativaPeligrosa) {
        ButtonType principal = new ButtonType(
                textoPrincipal, ButtonBar.ButtonData.OK_DONE);
        ButtonType alternativa = new ButtonType(
                textoAlternativa, ButtonBar.ButtonData.OTHER);
        ButtonType volver = new ButtonType(
                "VOLVER", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.NONE,
                mensaje, principal, alternativa, volver);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setGraphic(null);
        preparar(alerta, "dialog-choice");
        javafx.scene.Node botonPrincipal = alerta.getDialogPane()
                .lookupButton(principal);
        javafx.scene.Node botonAlternativa = alerta.getDialogPane()
                .lookupButton(alternativa);
        botonPrincipal.getStyleClass().add(principalPeligrosa
                ? "dialog-action-danger" : "dialog-action-primary");
        botonAlternativa.getStyleClass().add(alternativaPeligrosa
                ? "dialog-action-danger" : "dialog-action-alternative");
        Optional<ButtonType> resultado = alerta.showAndWait();
        ButtonType elegido = resultado.orElse(volver);
        if (elegido == principal) return Opcion.PRINCIPAL;
        if (elegido == alternativa) return Opcion.ALTERNATIVA;
        return Opcion.VOLVER;
    }
    public static boolean confirmarAccion(
            String titulo,
            String encabezado,
            String mensaje,
            String textoAccion) {
        ButtonType aceptar = new ButtonType(
                textoAccion, ButtonBar.ButtonData.OK_DONE);
        ButtonType volver = new ButtonType(
                "VOLVER", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(
                Alert.AlertType.NONE, mensaje, aceptar, volver);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setGraphic(null);
        preparar(alerta, "dialog-custom-action");
        javafx.scene.Node botonAceptar = alerta.getDialogPane()
                .lookupButton(aceptar);
        botonAceptar.getStyleClass().add("dialog-action-primary");
        return alerta.showAndWait().orElse(volver) == aceptar;
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

    private static void mostrar(Alert.AlertType tipo, String titulo,
            String mensaje, String clase) {
        ButtonType cerrar = new ButtonType("ENTENDIDO",
                ButtonBar.ButtonData.OK_DONE);
        Alert alerta = new Alert(tipo, "", cerrar);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setGraphic(null);

        Label contenido = new Label(mensaje == null ? "" : mensaje);
        contenido.setWrapText(true);
        contenido.setMaxWidth(Double.MAX_VALUE);
        contenido.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        contenido.setStyle("-fx-text-fill:#eaf4f8;"
                + "-fx-font-size:13px;-fx-line-spacing:3px;"
                + "-fx-padding:4 8 4 4;");

        int cantidadLineas = mensaje == null || mensaje.isBlank()
                ? 1 : mensaje.split("\\R", -1).length;
        boolean mensajeLargo = cantidadLineas > 7
                || (mensaje != null && mensaje.length() > 520);

        if (mensajeLargo) {
            ScrollPane desplazamiento = new ScrollPane(contenido);
            desplazamiento.setFitToWidth(true);
            desplazamiento.setHbarPolicy(
                    ScrollPane.ScrollBarPolicy.NEVER);
            desplazamiento.setPrefViewportWidth(620);
            desplazamiento.setPrefViewportHeight(300);
            desplazamiento.setMaxHeight(340);
            desplazamiento.setStyle("-fx-background:#0b1821;"
                    + "-fx-background-color:#0b1821;"
                    + "-fx-border-color:#315f79;"
                    + "-fx-border-radius:8;"
                    + "-fx-background-radius:8;");
            alerta.getDialogPane().setContent(desplazamiento);
            alerta.getDialogPane().setPrefWidth(700);
        } else {
            contenido.setPrefWidth(520);
            alerta.getDialogPane().setContent(contenido);
            alerta.getDialogPane().setPrefWidth(600);
        }

        preparar(alerta, clase);
        javafx.scene.Node botonCerrar = alerta.getDialogPane()
                .lookupButton(cerrar);
        botonCerrar.getStyleClass().add("dialog-action-primary");
        alerta.setResizable(true);
        alerta.showAndWait();
    }
}
