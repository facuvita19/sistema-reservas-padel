package vista.controlador;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import servicio.ConsultaJugadoresService.CategoriaResumen;
import vista.Dialogos;

public class JugadorCategoriaOperacionDialog {
    public record Datos(CategoriaResumen categoria, LocalDate fecha,
            String motivo, String observaciones) { }

    public Optional<Datos> asignar(List<CategoriaResumen> categorias,
            String jugador, String actual) {
        boolean cambio = actual != null && !actual.isBlank();
        Dialog<Datos> dialogo = base(cambio ? "Cambiar categoría" : "Asignar categoría");
        Label contexto = new Label(cambio
                ? jugador + " pasará de " + actual + " a una nueva categoría. La vigencia anterior se cerrará el día previo."
                : "Asigná la categoría vigente de " + jugador + ".");
        contexto.setWrapText(true);
        contexto.getStyleClass().add("player-dialog-intro");

        ComboBox<CategoriaResumen> categoria = new ComboBox<>();
        categoria.getItems().setAll(categorias);
        categoria.setPromptText("Seleccionar categoría");
        categoria.setMaxWidth(Double.MAX_VALUE);
        categoria.getStyleClass().add("player-dialog-combo");

        DatePicker fecha = new DatePicker(LocalDate.now());
        fecha.setMaxWidth(Double.MAX_VALUE);
        fecha.getStyleClass().add("player-dialog-date");

        TextField motivo = new TextField(cambio
                ? "Cambio de categoría" : "Evaluación administrativa");
        motivo.getStyleClass().add("player-dialog-field");

        TextArea observaciones = new TextArea();
        observaciones.setPromptText("Información adicional opcional");
        observaciones.setPrefRowCount(3);
        observaciones.setWrapText(true);
        observaciones.getStyleClass().add("player-dialog-area");

        VBox contenido = campos(contexto,
                etiqueta("NUEVA CATEGORÍA"), categoria,
                etiqueta("VIGENTE DESDE"), fecha,
                etiqueta("MOTIVO"), motivo,
                etiqueta("OBSERVACIONES"), observaciones);
        return mostrar(dialogo, contenido,
                categoria, fecha, motivo,
                () -> new Datos(categoria.getValue(), fecha.getValue(),
                        limpiar(motivo.getText()), limpiarOpcional(observaciones.getText())));
    }

    public Optional<Datos> cerrar(String titulo, String jugador, String categoriaActual) {
        Dialog<Datos> dialogo = base(titulo);
        Label contexto = new Label(jugador + " · Categoría vigente: " + categoriaActual);
        contexto.setWrapText(true);
        contexto.getStyleClass().add("player-dialog-intro");
        DatePicker fecha = new DatePicker(LocalDate.now());
        fecha.setMaxWidth(Double.MAX_VALUE);
        fecha.getStyleClass().add("player-dialog-date");
        TextField motivo = new TextField();
        motivo.setPromptText("Motivo obligatorio");
        motivo.getStyleClass().add("player-dialog-field");
        TextArea observaciones = new TextArea();
        observaciones.setPromptText("Información adicional opcional");
        observaciones.setPrefRowCount(3);
        observaciones.setWrapText(true);
        observaciones.getStyleClass().add("player-dialog-area");
        VBox contenido = campos(contexto,
                etiqueta("FECHA"), fecha,
                etiqueta("MOTIVO"), motivo,
                etiqueta("OBSERVACIONES"), observaciones);
        return mostrar(dialogo, contenido, null, fecha, motivo,
                () -> new Datos(null, fecha.getValue(), limpiar(motivo.getText()),
                        limpiarOpcional(observaciones.getText())));
    }

    private Dialog<Datos> base(String titulo) {
        Dialog<Datos> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        Dialogos.preparar(dialogo, "player-category-dialog");
        return dialogo;
    }
    private VBox campos(javafx.scene.Node... nodos) {
        VBox contenido = new VBox(9, nodos);
        contenido.setPadding(new Insets(18, 20, 12, 20));
        contenido.setPrefWidth(500);
        contenido.getStyleClass().add("player-category-content");
        return contenido;
    }
    private Label etiqueta(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("player-dialog-label");
        return label;
    }
    private Optional<Datos> mostrar(Dialog<Datos> dialogo, VBox contenido,
            ComboBox<CategoriaResumen> categoria, DatePicker fecha,
            TextField motivo, java.util.function.Supplier<Datos> resultado) {
        dialogo.getDialogPane().setContent(contenido);
        ButtonType confirmar = new ButtonType("CONFIRMAR", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(confirmar, ButtonType.CANCEL);
        Button boton = (Button) dialogo.getDialogPane().lookupButton(confirmar);
        boton.getStyleClass().add("dialog-action-primary");
        if (categoria == null) {
            boton.disableProperty().bind(fecha.valueProperty().isNull()
                    .or(motivo.textProperty().isEmpty()));
        } else {
            boton.disableProperty().bind(categoria.valueProperty().isNull()
                    .or(fecha.valueProperty().isNull())
                    .or(motivo.textProperty().isEmpty()));
        }
        dialogo.setResultConverter(tipo -> tipo == confirmar ? resultado.get() : null);
        return dialogo.showAndWait();
    }
    private String limpiar(String valor) {
        return valor.trim().replaceAll("\s+", " ");
    }
    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : limpiar(valor);
    }
}
