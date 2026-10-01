package vista.controlador;

import java.math.BigDecimal;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import negocio.RamaTorneo;
import negocio.TorneoCategoria;
import vista.Dialogos;

public class TorneoCategoriaDialog {
    private final Dialog<ButtonType> dialogo = new Dialog<>();
    private final TextField nombre = new TextField();
    private final ComboBox<RamaTorneo> rama = new ComboBox<>();
    private final Spinner<Integer> cupo = new Spinner<>(1, 512, 16);
    private final TextField precio = new TextField("0");
    private final Label error = new Label();
    private final long torneoId;
    private final TorneoCategoria original;
    private final ButtonType guardar = new ButtonType("GUARDAR CATEGORÍA", ButtonBar.ButtonData.OK_DONE);

    public TorneoCategoriaDialog(long torneoId, TorneoCategoria categoria) {
        this.torneoId = torneoId;
        original = categoria;
        configurar();
        if (categoria != null) cargar(categoria);
    }

    public Optional<TorneoCategoria> mostrar() {
        while (true) {
            Optional<ButtonType> resultado = dialogo.showAndWait();
            if (resultado.isEmpty() || resultado.get() != guardar) return Optional.empty();
            try {
                error.setText("");
                return Optional.of(construir());
            } catch (IllegalArgumentException exception) {
                error.setText(exception.getMessage());
            }
        }
    }

    private void configurar() {
        dialogo.setTitle(original == null ? "Nueva categoría" : "Editar categoría");
        dialogo.setHeaderText(original == null ? "Agregar categoría" : "Actualizar categoría");
        dialogo.getDialogPane().getButtonTypes().addAll(
                guardar, new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        Dialogos.preparar(dialogo, "category-editor-dialog");

        nombre.setPromptText("Ejemplo: 6ta");
        precio.setPromptText("Ejemplo: 24000");
        rama.setPromptText("Seleccionar rama");
        rama.setItems(FXCollections.observableArrayList(RamaTorneo.values()));
        rama.getSelectionModel().selectFirst();
        cupo.setEditable(true);
        nombre.getStyleClass().add("dialog-field");
        rama.getStyleClass().add("dialog-field");
        cupo.getStyleClass().add("dialog-field");
        precio.getStyleClass().add("dialog-field");

        GridPane grilla = new GridPane();
        grilla.setHgap(14);
        grilla.setVgap(12);
        grilla.getColumnConstraints().addAll(new ColumnConstraints(155), flexible());
        agregar(grilla, 0, "Nombre *", nombre);
        agregar(grilla, 1, "Rama *", rama);
        agregar(grilla, 2, "Cupo de parejas *", cupo);
        agregar(grilla, 3, "Precio de inscripción *", precio);

        Label ayuda = new Label("El precio es informativo y se ingresa como número, sin símbolo de moneda.");
        ayuda.getStyleClass().add("dialog-help");
        ayuda.setWrapText(true);
        error.getStyleClass().add("dialog-validation-error");
        error.setWrapText(true);
        VBox contenido = new VBox(14, grilla, ayuda, error);
        contenido.setPadding(new Insets(5, 2, 2, 2));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(590, 430);
    }

    private ColumnConstraints flexible() {
        ColumnConstraints columna = new ColumnConstraints();
        columna.setHgrow(Priority.ALWAYS);
        return columna;
    }

    private void agregar(GridPane grilla, int fila, String texto, javafx.scene.Node control) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("dialog-field-label");
        grilla.add(etiqueta, 0, fila);
        grilla.add(control, 1, fila);
        GridPane.setHgrow(control, Priority.ALWAYS);
    }

    private void cargar(TorneoCategoria categoria) {
        nombre.setText(categoria.getNombre());
        rama.setValue(categoria.getRama());
        cupo.getValueFactory().setValue(categoria.getCupoParejas());
        precio.setText(categoria.getPrecioInscripcion().stripTrailingZeros().toPlainString());
    }

    private TorneoCategoria construir() {
        if (nombre.getText() == null || nombre.getText().isBlank()) {
            throw new IllegalArgumentException("Ingresá el nombre de la categoría.");
        }
        if (rama.getValue() == null) throw new IllegalArgumentException("Seleccioná la rama.");
        BigDecimal importe;
        try {
            importe = new BigDecimal(precio.getText().trim().replace(',', '.'));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("El precio debe ser un número válido.");
        }
        if (importe.signum() < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");

        TorneoCategoria categoria = new TorneoCategoria();
        if (original != null) categoria.setId(original.getId());
        categoria.setTorneoId(torneoId);
        categoria.setNombre(nombre.getText().trim());
        categoria.setRama(rama.getValue());
        categoria.setCupoParejas(cupo.getValue());
        categoria.setPrecioInscripcion(importe);
        return categoria;
    }
}
