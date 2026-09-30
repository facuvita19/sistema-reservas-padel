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
import javafx.scene.layout.GridPane;
import negocio.RamaTorneo;
import negocio.TorneoCategoria;

public class TorneoCategoriaDialog {
    private final Dialog<TorneoCategoria> dialogo = new Dialog<>();
    private final TextField nombre = new TextField();
    private final ComboBox<RamaTorneo> rama = new ComboBox<>();
    private final Spinner<Integer> cupo = new Spinner<>(1, 512, 16);
    private final TextField precio = new TextField("0");
    private final long torneoId;
    private final TorneoCategoria original;

    public TorneoCategoriaDialog(long torneoId, TorneoCategoria categoria) {
        this.torneoId = torneoId;
        original = categoria;
        configurar();
        if (categoria != null) cargar(categoria);
    }

    public Optional<TorneoCategoria> mostrar() { return dialogo.showAndWait(); }

    private void configurar() {
        dialogo.setTitle(original == null ? "Nueva categoría" : "Editar categoría");
        dialogo.setHeaderText("Definí la rama, el cupo y el precio informativo.");
        ButtonType guardar = new ButtonType("GUARDAR", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        rama.setItems(FXCollections.observableArrayList(RamaTorneo.values()));
        rama.getSelectionModel().selectFirst();
        cupo.setEditable(true);

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.setPadding(new Insets(12));
        grilla.addRow(0, new Label("Nombre"), nombre);
        grilla.addRow(1, new Label("Rama"), rama);
        grilla.addRow(2, new Label("Cupo de parejas"), cupo);
        grilla.addRow(3, new Label("Precio de inscripción"), precio);
        dialogo.getDialogPane().setContent(grilla);
        dialogo.setResultConverter(tipo -> tipo == guardar ? construir() : null);
    }

    private void cargar(TorneoCategoria categoria) {
        nombre.setText(categoria.getNombre());
        rama.setValue(categoria.getRama());
        cupo.getValueFactory().setValue(categoria.getCupoParejas());
        precio.setText(categoria.getPrecioInscripcion().toPlainString());
    }

    private TorneoCategoria construir() {
        TorneoCategoria categoria = new TorneoCategoria();
        if (original != null) categoria.setId(original.getId());
        categoria.setTorneoId(torneoId);
        categoria.setNombre(nombre.getText());
        categoria.setRama(rama.getValue());
        categoria.setCupoParejas(cupo.getValue());
        categoria.setPrecioInscripcion(new BigDecimal(precio.getText().trim().replace(',', '.')));
        return categoria;
    }
}
