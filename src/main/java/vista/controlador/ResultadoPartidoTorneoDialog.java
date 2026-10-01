package vista.controlador;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import negocio.TipoSetTorneo;
import negocio.TorneoPartido;
import negocio.TorneoPartidoSet;
import servicio.ResultadoPartidoTorneoService;
import vista.Dialogos;
import vista.Navegacion;

public class ResultadoPartidoTorneoDialog {

    private final TorneoPartido partido;
    private final ResultadoPartidoTorneoService service;
    private final Dialog<TorneoPartido> dialogo = new Dialog<>();
    private final ComboBox<Integer> set1Pareja1 = puntosSet();
    private final ComboBox<Integer> set1Pareja2 = puntosSet();
    private final ComboBox<Integer> set2Pareja1 = puntosSet();
    private final ComboBox<Integer> set2Pareja2 = puntosSet();
    private final ComboBox<Integer> set3Pareja1 = puntosSet();
    private final ComboBox<Integer> set3Pareja2 = puntosSet();
    private final CheckBox incluirTercero =
            new CheckBox("Incluir tercer set");
    private final CheckBox superTieBreak =
            new CheckBox("Tercer set como super tie-break");
    private final TextArea observaciones = new TextArea();
    private String error;

    public ResultadoPartidoTorneoDialog(TorneoPartido partido) {
        this(partido, new ResultadoPartidoTorneoService());
    }

    ResultadoPartidoTorneoDialog(
            TorneoPartido partido,
            ResultadoPartidoTorneoService service) {
        if (partido == null || service == null) {
            throw new IllegalArgumentException(
                    "El partido y el servicio son obligatorios.");
        }
        this.partido = partido;
        this.service = service;
        construir();
    }

    public TorneoPartido mostrar() {
        return dialogo.showAndWait().orElse(null);
    }

    private void construir() {
        dialogo.setTitle("Registrar resultado");
        dialogo.setHeaderText(partido.getFase() + " #"
                + partido.getOrdenFase());
        ButtonType guardar = new ButtonType(
                "REGISTRAR RESULTADO", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "VOLVER", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(guardar, cancelar);
        dialogo.getDialogPane().setContent(contenido());
        Dialogos.preparar(dialogo, "dialog-tournament-result");
        var cssResultado = ResultadoPartidoTorneoDialog.class
                .getResource("/css/torneo-resultado.css");
        if (cssResultado != null) {
            dialogo.getDialogPane().getStylesheets().add(
                    cssResultado.toExternalForm());
        }
        vista.TemaDinamico.aplicar(
                dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());

        incluirTercero.selectedProperty().addListener(
                (o, anterior, actual) -> actualizarTercerSet(actual));
        actualizarTercerSet(false);

        var botonGuardar = dialogo.getDialogPane().lookupButton(guardar);
        botonGuardar.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                evento -> {
                    try {
                        validarSesion();
                        error = null;
                    } catch (RuntimeException exception) {
                        error = exception.getMessage();
                        evento.consume();
                        Dialogos.error("Resultado invalido", error);
                    }
                });

        dialogo.setResultConverter(tipo -> {
            if (tipo != guardar || error != null) return null;
            try {
                return service.registrarResultado(
                        partido.getId(), crearSets(),
                        Navegacion.getUsuarioActual().getId(),
                        observaciones.getText());
            } catch (RuntimeException exception) {
                Dialogos.error("No se pudo registrar el resultado",
                        exception.getMessage());
                return null;
            }
        });
    }

    private VBox contenido() {
        GridPane sets = new GridPane();
        sets.setHgap(10);
        sets.setVgap(10);
        sets.getStyleClass().add("result-sets-grid");
        sets.add(new Label("SET"), 0, 0);
        sets.add(new Label("PAREJA 1"), 1, 0);
        sets.add(new Label("PAREJA 2"), 2, 0);
        agregarFila(sets, 1, set1Pareja1, set1Pareja2);
        agregarFila(sets, 2, set2Pareja1, set2Pareja2);
        agregarFila(sets, 3, set3Pareja1, set3Pareja2);

        observaciones.setPromptText("Observaciones opcionales");
        observaciones.setPrefRowCount(3);
        observaciones.setWrapText(true);

        VBox caja = new VBox(12,
                new Label("Cargá los sets ganados por cada pareja."),
                sets, incluirTercero, superTieBreak,
                new Label("OBSERVACIONES"), observaciones);
        caja.setPadding(new Insets(4));
        caja.setPrefWidth(480);
        return caja;
    }

    private void agregarFila(
            GridPane grilla,
            int numero,
            ComboBox<Integer> pareja1,
            ComboBox<Integer> pareja2) {
        grilla.add(new Label("Set " + numero), 0, numero);
        grilla.add(pareja1, 1, numero);
        grilla.add(pareja2, 2, numero);
    }

    private void actualizarTercerSet(boolean visible) {
        set3Pareja1.setDisable(!visible);
        set3Pareja2.setDisable(!visible);
        superTieBreak.setDisable(!visible);
        if (!visible) {
            set3Pareja1.getSelectionModel().clearSelection();
            set3Pareja2.getSelectionModel().clearSelection();
            superTieBreak.setSelected(false);
        }
    }

    private List<TorneoPartidoSet> crearSets() {
        List<TorneoPartidoSet> sets = new ArrayList<>();
        sets.add(crearSet(1, set1Pareja1, set1Pareja2,
                TipoSetTorneo.NORMAL));
        sets.add(crearSet(2, set2Pareja1, set2Pareja2,
                TipoSetTorneo.NORMAL));
        if (incluirTercero.isSelected()) {
            sets.add(crearSet(3, set3Pareja1, set3Pareja2,
                    superTieBreak.isSelected()
                            ? TipoSetTorneo.SUPER_TIE_BREAK
                            : TipoSetTorneo.NORMAL));
        }
        return sets;
    }

    private TorneoPartidoSet crearSet(
            int numero,
            ComboBox<Integer> pareja1,
            ComboBox<Integer> pareja2,
            TipoSetTorneo tipo) {
        if (pareja1.getValue() == null || pareja2.getValue() == null) {
            throw new IllegalArgumentException(
                    "Completá el marcador del set " + numero + ".");
        }
        TorneoPartidoSet set = new TorneoPartidoSet();
        set.setPartidoId(partido.getId());
        set.setNumeroSet(numero);
        set.setTipo(tipo);
        set.setPuntosPareja1(pareja1.getValue());
        set.setPuntosPareja2(pareja2.getValue());
        return set;
    }

    private void validarSesion() {
        if (Navegacion.getUsuarioActual() == null
                || Navegacion.getUsuarioActual().getId() <= 0) {
            throw new IllegalArgumentException(
                    "La sesion administrativa finalizo.");
        }
        crearSets();
    }

    private static ComboBox<Integer> puntosSet() {
        ComboBox<Integer> combo = new ComboBox<>();
        combo.setItems(FXCollections.observableArrayList(
                java.util.stream.IntStream.rangeClosed(0, 30)
                        .boxed().toList()));
        combo.setPrefWidth(130);
        return combo;
    }
}
