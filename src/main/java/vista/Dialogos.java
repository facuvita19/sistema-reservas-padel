package vista;

import javafx.application.Platform;

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
    private static final String CSS_INTERFAZ = "/css/interfaz-unificada.css";
    private static final String CSS_SIMPLES = "/css/dialogos-simples.css";

    private Dialogos() {
    }

    public static void preparar(Dialog<?> dialogo, String clase) {
        if (dialogo == null) return;
        DialogPane panel = dialogo.getDialogPane();
        agregarEstilo(panel, CSS);
        agregarEstilo(panel, CSS_INTERFAZ);
        agregarEstilo(panel, CSS_SIMPLES);
        panel.getStyleClass().add("admin-dialog");
        if (clase != null && !clase.isBlank()) panel.getStyleClass().add(clase);
        TemaDinamico.aplicar(panel, Navegacion.getConfiguracionActual());
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.setResizable(true);
        dialogo.setOnShown(evento -> {
            if (dialogo.getDialogPane().getScene() == null
                    || !(dialogo.getDialogPane().getScene().getWindow()
                            instanceof Stage stage)) {
                return;
            }
            stage.setMinWidth(Math.min(stage.getWidth(), 520));
            if (!esDialogoDeTrabajoGrande(dialogo)) {
                // dialogos-pequenos-desmaximizar-v1
                // Un dialogo auxiliar no debe heredar el estado maximizado
                // de una herramienta grande propietaria.
                Platform.runLater(() -> {
                    stage.setFullScreen(false);
                    stage.setMaximized(false);
                    stage.sizeToScene();
                    stage.centerOnScreen();
                });
                return;
            }
            // Los Dialog crean el Stage al mostrarse. Se maximiza en el
            // siguiente pulso para conservar el marco normal de Windows.
            Platform.runLater(() -> {
                stage.setFullScreen(false);
                stage.setMaximized(true);
            });
        });
    }

        private static boolean esDialogoDeTrabajoGrande(Dialog<?> dialogo) {
        // dialogos-maximizacion-explicita-v1
        // Solo herramientas visuales complejas usan el espacio completo.
        // Alertas y formularios administrativos conservan tamaño normal.
        if (dialogo instanceof Alert) return false;

        DialogPane panel = dialogo.getDialogPane();

        // dialogos-auxiliares-estructura-v2
        // Los formularios auxiliares de la estructura conservan tamaño normal,
        // aunque reutilicen una clase visual de la herramienta principal.
        boolean auxiliarEstructura = panel.getStyleClass().stream()
                .anyMatch(clase -> clase.equals("structural-add-dialog")
                        || clase.equals("structural-participant-dialog"));
        if (auxiliarEstructura) return false;

        return panel.getStyleClass().stream().anyMatch(clase ->
                clase.equals("dialog-bracket-editor-new")
                || clase.equals("structural-editor-dialog")
                || clase.equals("dialog-tournament-bracket-manual")
                || clase.equals("dialog-tournament-bracket-proposal")
                || clase.equals("proposal-review-dialog")
                || clase.equals("proposal-preview-dialog"));
    }

    private static void agregarEstilo(DialogPane panel, String recurso) {
        URL css = Dialogos.class.getResource(recurso);
        if (css == null) return;
        String externo = css.toExternalForm();
        if (!panel.getStylesheets().contains(externo)) {
            panel.getStylesheets().add(externo);
        }
    }

    private static void configurarContenido(
            Alert alerta, String mensaje, double ancho) {
        Label contenido = new Label(mensaje == null ? "" : mensaje);
        contenido.setWrapText(true);
        contenido.setMaxWidth(Double.MAX_VALUE);
        contenido.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        contenido.setPrefWidth(ancho);
        contenido.getStyleClass().add("dialog-message-label");
        alerta.getDialogPane().setContent(contenido);
        alerta.getDialogPane().setPrefWidth(ancho + 80);
    }

    public static boolean confirmar(String titulo, String mensaje) {
        return confirmar(titulo, mensaje, false);
    }

    public static boolean confirmarPeligro(String titulo, String mensaje) {
        return confirmar(titulo, mensaje, true);
    }

    public static boolean confirmarPeligroPersonalizado(
            String titulo,
            String encabezado,
            String mensaje,
            String textoAceptar,
            String textoCancelar) {
        ButtonType aceptar = new ButtonType(
                textoAceptar, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                textoCancelar, ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                mensaje, aceptar, cancelar);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setGraphic(null);
        configurarContenido(alerta, mensaje, 520);
        preparar(alerta, "dialog-danger");
        return alerta.showAndWait().orElse(cancelar) == aceptar;
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
        configurarContenido(alerta, mensaje, 520);
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
        configurarContenido(alerta, mensaje, 700);
        preparar(alerta, "dialog-choice");
        javafx.scene.Node botonPrincipal = alerta.getDialogPane()
                .lookupButton(principal);
        javafx.scene.Node botonAlternativa = alerta.getDialogPane()
                .lookupButton(alternativa);
        botonPrincipal.getStyleClass().add(principalPeligrosa
                ? "dialog-action-danger" : "dialog-action-primary");
        botonAlternativa.getStyleClass().add(alternativaPeligrosa
                ? "dialog-action-danger" : "dialog-action-alternative");
        boolean excepcionAdministrativa = textoAlternativa != null
                && textoAlternativa.strip().equalsIgnoreCase(
                        "EXCEPCION ADMINISTRATIVA");
        if (excepcionAdministrativa) {
            alerta.getDialogPane().getStyleClass().add(
                    "dialog-late-cancellation-v6");
            botonAlternativa.getStyleClass().add(
                    "dialog-action-administrative-v6");
        }
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
        configurarContenido(alerta, mensaje, 590);
        preparar(alerta, "dialog-custom-action");
        boolean accionLarga = textoAccion != null
                && textoAccion.length() >= 24;
        javafx.scene.Node botonAceptar = alerta.getDialogPane()
                .lookupButton(aceptar);
        botonAceptar.getStyleClass().add("dialog-action-primary");
        if (accionLarga) {
            alerta.getDialogPane().getStyleClass().add(
                    "dialog-custom-action-wide-v15");
            botonAceptar.getStyleClass().add("dialog-long-primary-v15");
        }
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
        contenido.getStyleClass().add("dialog-message-label");

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
            desplazamiento.getStyleClass().add(
                    "dialog-message-scroll");
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
