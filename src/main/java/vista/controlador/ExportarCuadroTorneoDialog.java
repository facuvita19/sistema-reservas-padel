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
        dialogo.setHeaderText("Elegir versión del cuadro");
        ButtonType generar = new ButtonType(
                "GENERAR PDF", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(generar, cancelar);
        Dialogos.preparar(dialogo, "dialog-confirm");

        ToggleGroup grupo = new ToggleGroup();
        RadioButton planilla = new RadioButton(
                "Planilla de resultados");
        RadioButton actualizado = new RadioButton(
                "Cuadro actualizado");
        planilla.setToggleGroup(grupo);
        actualizado.setToggleGroup(grupo);
        planilla.setSelected(true);
        planilla.getStyleClass().add("dialog-field-label");
        actualizado.getStyleClass().add("dialog-field-label");

        Label ayudaPlanilla = new Label(
                "Mantiene la programación disponible y deja vacíos los "
                + "casilleros de sets para completar a mano.");
        ayudaPlanilla.setWrapText(true);
        ayudaPlanilla.getStyleClass().add("dialog-help");
        Label ayudaActualizado = new Label(
                "Imprime marcadores, ganadores y referencias de avance "
                + "registradas en el sistema.");
        ayudaActualizado.setWrapText(true);
        ayudaActualizado.getStyleClass().add("dialog-help");

        VBox opcionPlanilla = new VBox(4, planilla, ayudaPlanilla);
        VBox opcionActualizada = new VBox(4, actualizado, ayudaActualizado);
        VBox contenido = new VBox(15, opcionPlanilla, opcionActualizada);
        contenido.setPadding(new Insets(8, 5, 5, 5));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(620, 330);
        dialogo.setResultConverter(boton -> {
            if (boton != generar) return null;
            return actualizado.isSelected()
                    ? Modo.ACTUALIZADO : Modo.PLANILLA;
        });
        return dialogo.showAndWait();
    }
}
