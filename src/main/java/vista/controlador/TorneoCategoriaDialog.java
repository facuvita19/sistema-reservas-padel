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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import negocio.FormatoCompetenciaTorneo;
import negocio.RamaTorneo;
import negocio.TorneoCategoria;
import vista.Dialogos;

public class TorneoCategoriaDialog {
    private final Dialog<ButtonType> dialogo = new Dialog<>();
    private final TextField nombre = new TextField();
    private final ComboBox<RamaTorneo> rama = new ComboBox<>();
    private final Spinner<Integer> cupo = new Spinner<>(1, 512, 16);
    private final TextField precio = new TextField("0");
    private final ComboBox<FormatoCompetenciaTorneo> formato = new ComboBox<>();
    private final Spinner<Integer> gruposTres = new Spinner<>(0, 64, 0);
    private final Spinner<Integer> gruposCuatro = new Spinner<>(0, 64, 0);
    private final Label resumenGrupos = new Label();
    private final VBox panelGrupos = new VBox(10);
    private final TextField premioCampeon = new TextField();
    private final TextField premioSubcampeon = new TextField();
    private final TextArea premioDescripcion = new TextArea();
    private final Label error = new Label();
    private final long torneoId;
    private final TorneoCategoria original;
    private final ButtonType guardar = new ButtonType(
            "GUARDAR CATEGORÍA", ButtonBar.ButtonData.OK_DONE);

    public TorneoCategoriaDialog(long torneoId, TorneoCategoria categoria) {
        this.torneoId = torneoId;
        original = categoria;
        configurar();
        if (categoria != null) cargar(categoria);
        actualizarFormato();
    }

    public Optional<TorneoCategoria> mostrar() {
        while (true) {
            Optional<ButtonType> resultado = dialogo.showAndWait();
            if (resultado.isEmpty() || resultado.get() != guardar) {
                return Optional.empty();
            }
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
        dialogo.getDialogPane().getButtonTypes().addAll(guardar,
                new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        Dialogos.preparar(dialogo, "category-editor-dialog");

        nombre.setPromptText("Ejemplo: 6ta");
        precio.setPromptText("Ejemplo: 24000");
        premioCampeon.setPromptText("Ejemplo: 300000");
        premioSubcampeon.setPromptText("Ejemplo: 100000");
        premioDescripcion.setPromptText("Ejemplo: trofeos y paletas para los campeones");
        premioDescripcion.setWrapText(true);
        premioDescripcion.setPrefRowCount(3);
        rama.setPromptText("Seleccionar rama");
        rama.setItems(FXCollections.observableArrayList(RamaTorneo.values()));
        rama.getSelectionModel().selectFirst();
        formato.setItems(FXCollections.observableArrayList(
                FormatoCompetenciaTorneo.values()));
        formato.setValue(FormatoCompetenciaTorneo.ELIMINACION_DIRECTA);
        cupo.setEditable(true);
        gruposTres.setEditable(true);
        gruposCuatro.setEditable(true);

        for (javafx.scene.Node control : new javafx.scene.Node[] {
                nombre, rama, cupo, precio, formato, gruposTres,
                gruposCuatro, premioCampeon, premioSubcampeon,
                premioDescripcion }) {
            control.getStyleClass().add("dialog-field");
        }

        GridPane datos = grilla();
        agregar(datos, 0, "Nombre *", nombre);
        agregar(datos, 1, "Rama *", rama);
        agregar(datos, 2, "Cupo de parejas *", cupo);
        agregar(datos, 3, "Precio de inscripción *", precio);
        agregar(datos, 4, "Formato de competencia *", formato);

        GridPane grupos = grilla();
        agregar(grupos, 0, "Grupos de 3 equipos", gruposTres);
        agregar(grupos, 1, "Grupos de 4 equipos", gruposCuatro);
        Label reglaTres = ayuda("Grupos de 3: todos contra todos, clasifican 1° y 2°.");
        Label reglaCuatro = ayuda("Grupos de 4: dos cruces y definiciones; clasifican 1°, 2° y 3°.");
        resumenGrupos.getStyleClass().add("dialog-validation-error");
        resumenGrupos.setWrapText(true);
        panelGrupos.getChildren().addAll(titulo("CONFIGURACIÓN DE GRUPOS"),
                grupos, reglaTres, reglaCuatro, resumenGrupos);

        GridPane premios = grilla();
        agregar(premios, 0, "Premio para campeones", premioCampeon);
        agregar(premios, 1, "Premio para subcampeones", premioSubcampeon);
        agregar(premios, 2, "Descripción adicional", premioDescripcion);

        Label ayudaPremios = ayuda("Los premios son opcionales e independientes.");
        error.getStyleClass().add("dialog-validation-error");
        error.setWrapText(true);
        VBox contenido = new VBox(14, titulo("DATOS DE LA CATEGORÍA"),
                datos, panelGrupos, titulo("PREMIOS OPCIONALES"), premios,
                ayudaPremios, error);
        contenido.setPadding(new Insets(5, 2, 2, 2));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(760, 790);

        formato.valueProperty().addListener((o, a, n) -> actualizarFormato());
        gruposTres.valueProperty().addListener((o, a, n) -> actualizarProyeccion());
        gruposCuatro.valueProperty().addListener((o, a, n) -> actualizarProyeccion());
    }

    private GridPane grilla() {
        GridPane grilla = new GridPane();
        grilla.setHgap(14);
        grilla.setVgap(12);
        grilla.getColumnConstraints().addAll(
                new ColumnConstraints(215), flexible());
        return grilla;
    }

    private Label titulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("dialog-field-label");
        return label;
    }

    private Label ayuda(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("dialog-help");
        label.setWrapText(true);
        return label;
    }

    private void actualizarFormato() {
        boolean grupos = formato.getValue()
                == FormatoCompetenciaTorneo.GRUPOS_ELIMINACION;
        panelGrupos.setVisible(grupos);
        panelGrupos.setManaged(grupos);
        cupo.setDisable(grupos);
        if (!grupos) {
            resumenGrupos.setText("");
        }
        actualizarProyeccion();
    }

    private void actualizarProyeccion() {
        if (formato.getValue()
                != FormatoCompetenciaTorneo.GRUPOS_ELIMINACION) return;
        int tres = gruposTres.getValue();
        int cuatro = gruposCuatro.getValue();
        int capacidad = tres * 3 + cuatro * 4;
        int clasificados = tres * 2 + cuatro * 3;
        if (capacidad > 0) cupo.getValueFactory().setValue(capacidad);
        resumenGrupos.setText("Capacidad: " + capacidad + " equipos · "
                + "Clasificados proyectados: " + clasificados + " · "
                + "Grupos totales: " + (tres + cuatro));
    }

    private ColumnConstraints flexible() {
        ColumnConstraints columna = new ColumnConstraints();
        columna.setHgrow(Priority.ALWAYS);
        return columna;
    }

    private void agregar(GridPane grilla, int fila, String texto,
            javafx.scene.Node control) {
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
        formato.setValue(categoria.getFormatoCompetencia());
        gruposTres.getValueFactory().setValue(categoria.getCantidadGruposTres());
        gruposCuatro.getValueFactory().setValue(categoria.getCantidadGruposCuatro());
        premioCampeon.setText(textoImporte(categoria.getPremioCampeon()));
        premioSubcampeon.setText(textoImporte(categoria.getPremioSubcampeon()));
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
            BigDecimal valor = new BigDecimal(texto.trim().replace(',', '.'));
            if (valor.signum() < 0) {
                throw new IllegalArgumentException(etiqueta + " no puede ser negativo.");
            }
            if (valor.scale() > 2) {
                throw new IllegalArgumentException(etiqueta + " admite hasta dos decimales.");
            }
            return valor;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(etiqueta + " debe ser un numero valido.");
        }
    }

    private TorneoCategoria construir() {
        if (nombre.getText() == null || nombre.getText().isBlank()) {
            throw new IllegalArgumentException("Ingresá el nombre de la categoría.");
        }
        if (rama.getValue() == null) {
            throw new IllegalArgumentException("Seleccioná la rama.");
        }
        BigDecimal importe;
        try {
            importe = new BigDecimal(precio.getText().trim().replace(',', '.'));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("El precio debe ser un número válido.");
        }
        if (importe.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        int tres = gruposTres.getValue();
        int cuatro = gruposCuatro.getValue();
        if (formato.getValue() == FormatoCompetenciaTorneo.GRUPOS_ELIMINACION
                && tres + cuatro == 0) {
            throw new IllegalArgumentException("Configurá al menos un grupo.");
        }

        TorneoCategoria categoria = new TorneoCategoria();
        if (original != null) categoria.setId(original.getId());
        categoria.setTorneoId(torneoId);
        categoria.setNombre(nombre.getText().trim());
        categoria.setRama(rama.getValue());
        categoria.setCupoParejas(cupo.getValue());
        categoria.setPrecioInscripcion(importe);
        categoria.setFormatoCompetencia(formato.getValue());
        categoria.setCantidadGruposTres(formato.getValue()
                == FormatoCompetenciaTorneo.GRUPOS_ELIMINACION ? tres : 0);
        categoria.setCantidadGruposCuatro(formato.getValue()
                == FormatoCompetenciaTorneo.GRUPOS_ELIMINACION ? cuatro : 0);
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
