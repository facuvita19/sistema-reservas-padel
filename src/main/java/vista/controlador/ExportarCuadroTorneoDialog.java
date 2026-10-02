package vista.controlador;

import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import vista.Dialogos;

public class ExportarCuadroTorneoDialog {

    public enum Modo {
        PLANILLA,
        ACTUALIZADO
    }

    public Optional<Modo> mostrar() {
        Dialog<Modo> dialogo = new Dialog<>();
        dialogo.setTitle("Generar cuadro para imprimir");
        dialogo.setHeaderText("Elegir contenido del cuadro mural");
        ButtonType generar = new ButtonType(
                "GENERAR PDF", ButtonBar.ButtonData.OK_DONE);
        ButtonType volver = new ButtonType(
                "VOLVER", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(generar, volver);
        Dialogos.preparar(dialogo, "dialog-confirm");

        ToggleGroup grupo = new ToggleGroup();
        RadioButton planilla = new RadioButton(
                "Planilla para completar a mano");
        RadioButton actualizado = new RadioButton(
                "Cuadro actualizado con resultados registrados");
        planilla.setToggleGroup(grupo);
        actualizado.setToggleGroup(grupo);
        planilla.setSelected(true);
        planilla.getStyleClass().add("dialog-field-label");
        actualizado.getStyleClass().add("dialog-field-label");

        Label ayuda = new Label(
                "La planilla deja los sets vacios. El cuadro actualizado "
                + "imprime los resultados cargados y deja vacios los pendientes.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("dialog-help");
        VBox contenido = new VBox(13, planilla, actualizado, ayuda);
        contenido.setPadding(new Insets(8, 5, 5, 5));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(620, 310);
        dialogo.setResultConverter(boton -> {
            if (boton != generar) return null;
            return actualizado.isSelected()
                    ? Modo.ACTUALIZADO : Modo.PLANILLA;
        });
        return dialogo.showAndWait();
    }
}
