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
import javafx.scene.control.TextArea;
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
    private final TextField premioCampeon = new TextField();
    private final TextField premioSubcampeon = new TextField();
    private final TextArea premioDescripcion = new TextArea();
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
        premioCampeon.setPromptText("Ejemplo: 300000");
        premioSubcampeon.setPromptText("Ejemplo: 100000");
        premioDescripcion.setPromptText(
                "Ejemplo: trofeos y paletas para los campeones");
        premioDescripcion.setWrapText(true);
        premioDescripcion.setPrefRowCount(3);
        rama.setPromptText("Seleccionar rama");
        rama.setItems(FXCollections.observableArrayList(RamaTorneo.values()));
        rama.getSelectionModel().selectFirst();
        cupo.setEditable(true);
        nombre.getStyleClass().add("dialog-field");
        rama.getStyleClass().add("dialog-field");
        cupo.getStyleClass().add("dialog-field");
        precio.getStyleClass().add("dialog-field");
        premioCampeon.getStyleClass().add("dialog-field");
        premioSubcampeon.getStyleClass().add("dialog-field");
        premioDescripcion.getStyleClass().add("dialog-field");

        GridPane grilla = new GridPane();
        grilla.setHgap(14);
        grilla.setVgap(12);
        grilla.getColumnConstraints().addAll(new ColumnConstraints(205), flexible());
        agregar(grilla, 0, "Nombre *", nombre);
        agregar(grilla, 1, "Rama *", rama);
        agregar(grilla, 2, "Cupo de parejas *", cupo);
        agregar(grilla, 3, "Precio de inscripción *", precio);

        Label tituloPremios = new Label("PREMIOS OPCIONALES");
        tituloPremios.getStyleClass().add("dialog-field-label");
        grilla.add(tituloPremios, 0, 4, 2, 1);
        agregar(grilla, 5, "Premio para campeones", premioCampeon);
        agregar(grilla, 6, "Premio para subcampeones", premioSubcampeon);
        agregar(grilla, 7, "Descripción adicional", premioDescripcion);

        Label ayuda = new Label("Los premios son opcionales e independientes. "
                + "La descripción adicional puede usarse para trofeos, "
                + "paletas, indumentaria u otros premios.");
        ayuda.getStyleClass().add("dialog-help");
        ayuda.setWrapText(true);
        error.getStyleClass().add("dialog-validation-error");
        error.setWrapText(true);
        VBox contenido = new VBox(14, grilla, ayuda, error);
        contenido.setPadding(new Insets(5, 2, 2, 2));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(735, 650);
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
        precio.setText(categoria.getPrecioInscripcion()
                .stripTrailingZeros().toPlainString());
        premioCampeon.setText(textoImporte(categoria.getPremioCampeon()));
        premioSubcampeon.setText(textoImporte(
                categoria.getPremioSubcampeon()));
        premioDescripcion.setText(categoria.getPremioDescripcion() == null
                ? "" : categoria.getPremioDescripcion());
    }

    private String textoImporte(BigDecimal valor) {
        return valor == null ? "" : valor.stripTrailingZeros().toPlainString();
    }

    private BigDecimal leerPremio(TextField campo, String etiqueta) {
        String texto = campo.getText();
        if (texto == null || texto.isBlank()) return null;
        try {
            BigDecimal valor = new BigDecimal(
                    texto.trim().replace(',', '.'));
            if (valor.signum() < 0) {
                throw new IllegalArgumentException(
                        etiqueta + " no puede ser negativo.");
            }
            if (valor.scale() > 2) {
                throw new IllegalArgumentException(
                        etiqueta + " admite hasta dos decimales.");
            }
            if (valor.precision() - Math.max(valor.scale(), 0) > 10) {
                throw new IllegalArgumentException(
                        etiqueta + " supera el maximo permitido.");
            }
            return valor;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    etiqueta + " debe ser un numero valido.");
        }
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
        categoria.setPremioCampeon(leerPremio(
                premioCampeon, "El premio para los campeones"));
        categoria.setPremioSubcampeon(leerPremio(
                premioSubcampeon, "El premio para los subcampeones"));
        String descripcion = premioDescripcion.getText();
        if (descripcion != null && descripcion.trim().length() > 500) {
            throw new IllegalArgumentException(
                    "La descripcion de premios no puede superar 500 caracteres.");
        }
        categoria.setPremioDescripcion(descripcion);
        return categoria;
    }
}
