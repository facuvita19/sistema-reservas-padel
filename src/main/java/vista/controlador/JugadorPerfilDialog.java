package vista.controlador;

import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import negocio.PosicionJugador;
import vista.Dialogos;

public class JugadorPerfilDialog {
    public record Datos(String nombre, PosicionJugador posicion,
            boolean disponible, boolean invitaciones) { }

    public Optional<Datos> mostrar(String titulo, String nombre,
            PosicionJugador posicion, boolean disponible,
            boolean invitaciones) {
        Dialog<Datos> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);

        Label introduccion = new Label(nombre == null || nombre.isBlank()
                ? "Usá esta opción cuando todavía no exista una ficha en Gestión de clientes. Podrás asociarla después sin perder la categoría ni el historial."
                : "Actualizá la información visible y las preferencias del jugador.");
        introduccion.setWrapText(true);
        introduccion.getStyleClass().add("player-dialog-intro");

        TextField campoNombre = new TextField(nombre);
        campoNombre.setPromptText("Nombre y apellido");
        campoNombre.setTextFormatter(new TextFormatter<String>(cambio -> cambio.getControlNewText().length() <= 220 ? cambio : null));
        campoNombre.getStyleClass().add("player-dialog-field");

        ComboBox<PosicionJugador> comboPosicion = new ComboBox<>();
        comboPosicion.getItems().setAll(PosicionJugador.values());
        comboPosicion.setValue(posicion);
        comboPosicion.setPromptText("Sin definir");
        comboPosicion.setMaxWidth(Double.MAX_VALUE);
        comboPosicion.getStyleClass().add("player-dialog-combo");

        CheckBox disponibleCheck = new CheckBox("Disponible para jugar");
        disponibleCheck.setSelected(disponible);
        CheckBox invitacionesCheck = new CheckBox("Acepta invitaciones de otros jugadores");
        invitacionesCheck.setSelected(invitaciones);
        VBox preferencias = new VBox(9, disponibleCheck, invitacionesCheck);
        preferencias.getStyleClass().add("player-dialog-preferences");

        VBox contenido = new VBox(10,
                introduccion,
                etiqueta("NOMBRE PÚBLICO"), campoNombre,
                etiqueta("POSICIÓN PREFERIDA"), comboPosicion,
                etiqueta("COMUNIDAD"), preferencias);
        contenido.setPadding(new Insets(18, 20, 12, 20));
        contenido.setPrefWidth(500);
        contenido.getStyleClass().add("player-editor-content");
        dialogo.getDialogPane().setContent(contenido);

        ButtonType guardar = new ButtonType("GUARDAR", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        Dialogos.preparar(dialogo, "player-editor-dialog");
        Node botonGuardar = dialogo.getDialogPane().lookupButton(guardar);
        botonGuardar.getStyleClass().add("dialog-action-primary");
        botonGuardar.disableProperty().bind(campoNombre.textProperty().isEmpty());
        dialogo.setOnShown(e -> campoNombre.requestFocus());
        dialogo.setResultConverter(tipo -> tipo == guardar
                ? new Datos(campoNombre.getText().trim().replaceAll("\\s+", " "), comboPosicion.getValue(),
                        disponibleCheck.isSelected(), invitacionesCheck.isSelected())
                : null);
        return dialogo.showAndWait();
    }

    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("player-dialog-label");
        return label;
    }
}
