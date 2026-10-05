package vista.controlador;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import negocio.TipoSetTorneo;
import negocio.TorneoInscripcion;
import negocio.TorneoPartido;
import negocio.TorneoPartidoSet;
import servicio.ResultadoPartidoTorneoService;
import vista.Dialogos;
import vista.Navegacion;

public class ResultadoPartidoTorneoDialog {

    // resultado-dialog-scroll-botones-v1

    // separar-parejas-centrar-marcadores-v1

    // mostrar-parejas-compactar-marcadores-v1

    // pulir-dialogos-resultado-torneo-v1

    // modernizar-dialogos-resultado-torneo-v1

    private final TorneoPartido partido;
    private final ResultadoPartidoTorneoService service;
    private final TorneoInscripcionDAO inscripcionDAO =
            new TorneoInscripcionDAOMySQL();
    private final boolean correccion;
    private final Dialog<TorneoPartido> dialogo = new Dialog<>();
    private final ComboBox<Integer> set1Pareja1 = puntosSet();
    private final ComboBox<Integer> set1Pareja2 = puntosSet();
    private final ComboBox<Integer> set2Pareja1 = puntosSet();
    private final ComboBox<Integer> set2Pareja2 = puntosSet();
    private final ComboBox<Integer> set3Pareja1 = puntosSet();
    private final ComboBox<Integer> set3Pareja2 = puntosSet();
    private final Label etiquetaSet3 = new Label("Set 3");
    private final CheckBox incluirTercero =
            new CheckBox("Incluir tercer set");
    private final CheckBox superTieBreak =
            new CheckBox("Tercer set como super tie-break");
    private final TextArea observaciones = new TextArea();
    private final TextArea motivoCorreccion = new TextArea();
    private String error;
    private TorneoPartido resultadoGuardado;

    public ResultadoPartidoTorneoDialog(TorneoPartido partido) {
        this(partido, new ResultadoPartidoTorneoService(), false);
    }

    public ResultadoPartidoTorneoDialog(
            TorneoPartido partido, boolean correccion) {
        this(partido, new ResultadoPartidoTorneoService(), correccion);
    }

    ResultadoPartidoTorneoDialog(
            TorneoPartido partido,
            ResultadoPartidoTorneoService service) {
        this(partido, service, false);
    }

    ResultadoPartidoTorneoDialog(
            TorneoPartido partido,
            ResultadoPartidoTorneoService service,
            boolean correccion) {
        if (partido == null || service == null) {
            throw new IllegalArgumentException(
                    "El partido y el servicio son obligatorios.");
        }
        this.partido = partido;
        this.service = service;
        this.correccion = correccion;
        construir();
    }

    public TorneoPartido mostrar() {
        return dialogo.showAndWait().orElse(null);
    }

    private void construir() {
        dialogo.setTitle(correccion
                ? "Corregir resultado" : "Registrar resultado");
        dialogo.setHeaderText(partido.getFase() + " · PARTIDO "
                + partido.getOrdenFase());
        ButtonType guardar = new ButtonType(
                correccion ? "GUARDAR CORRECCIÓN" : "REGISTRAR RESULTADO",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(guardar, cancelar);
        VBox formulario = contenido();
        ScrollPane desplazamiento = new ScrollPane(formulario);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamiento.setPrefViewportHeight(correccion ? 555 : 515);
        desplazamiento.setMaxHeight(correccion ? 555 : 515);
        desplazamiento.getStyleClass().add("result-dialog-scroll");
        dialogo.getDialogPane().setContent(desplazamiento);
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
        if (correccion) cargarResultadoActual();

        var botonGuardar = dialogo.getDialogPane().lookupButton(guardar);
        botonGuardar.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                evento -> {
                    try {
                        validarSesion();
                        resultadoGuardado = correccion
                                ? service.corregirResultado(
                                        partido.getId(), crearSets(),
                                        Navegacion.getUsuarioActual().getId(),
                                        observaciones.getText(),
                                        motivoCorreccion.getText())
                                : service.registrarResultado(
                                        partido.getId(), crearSets(),
                                        Navegacion.getUsuarioActual().getId(),
                                        observaciones.getText());
                        error = null;
                    } catch (RuntimeException exception) {
                        error = exception.getMessage();
                        resultadoGuardado = null;
                        evento.consume();
                        Dialogos.error(correccion
                                ? "No se pudo corregir el resultado"
                                : "No se pudo registrar el resultado",
                                error == null
                                        ? "Revisa los datos ingresados."
                                        : error);
                    }
                });

        dialogo.setResultConverter(tipo ->
                tipo == guardar ? resultadoGuardado : null);
    }

    private void cargarResultadoActual() {
        List<TorneoPartidoSet> actuales = partido.getSets();
        if (actuales.size() < 2) return;
        cargarSet(actuales.get(0), set1Pareja1, set1Pareja2);
        cargarSet(actuales.get(1), set2Pareja1, set2Pareja2);
        if (actuales.size() >= 3) {
            incluirTercero.setSelected(true);
            actualizarTercerSet(true);
            cargarSet(actuales.get(2), set3Pareja1, set3Pareja2);
            superTieBreak.setSelected(actuales.get(2).getTipo()
                    == TipoSetTorneo.SUPER_TIE_BREAK);
        }
        observaciones.setText(partido.getObservaciones());
    }

    private void cargarSet(TorneoPartidoSet set,
            ComboBox<Integer> pareja1, ComboBox<Integer> pareja2) {
        pareja1.setValue(set.getPuntosPareja1());
        pareja2.setValue(set.getPuntosPareja2());
    }

    private VBox contenido() {
        HBox enfrentamiento = crearFranjaEnfrentamiento();

        GridPane sets = new GridPane();
        sets.setHgap(10);
        sets.setVgap(9);
        sets.setAlignment(Pos.CENTER);
        sets.getStyleClass().add("result-sets-grid");

        ColumnConstraints columnaSet = new ColumnConstraints();
        columnaSet.setMinWidth(68);
        columnaSet.setPrefWidth(78);
        columnaSet.setHalignment(HPos.LEFT);
        ColumnConstraints columnaPareja1 = new ColumnConstraints();
        columnaPareja1.setMinWidth(118);
        columnaPareja1.setPrefWidth(128);
        columnaPareja1.setHalignment(HPos.CENTER);
        ColumnConstraints columnaPareja2 = new ColumnConstraints();
        columnaPareja2.setMinWidth(118);
        columnaPareja2.setPrefWidth(128);
        columnaPareja2.setHalignment(HPos.CENTER);
        sets.getColumnConstraints().setAll(
                columnaSet, columnaPareja1, columnaPareja2);

        Label encabezadoSet = new Label("SET");
        Label encabezadoP1 = new Label("P1");
        Label encabezadoP2 = new Label("P2");
        encabezadoSet.getStyleClass().add("result-grid-header");
        encabezadoP1.getStyleClass().add("result-grid-header");
        encabezadoP2.getStyleClass().add("result-grid-header");
        sets.add(encabezadoSet, 0, 0);
        sets.add(encabezadoP1, 1, 0);
        sets.add(encabezadoP2, 2, 0);
        agregarFila(sets, 1, set1Pareja1, set1Pareja2);
        agregarFila(sets, 2, set2Pareja1, set2Pareja2);
        agregarFila(sets, 3, set3Pareja1, set3Pareja2);

        motivoCorreccion.setPromptText(
                "Describe por qué se modifica el resultado");
        motivoCorreccion.setPrefRowCount(2);
        motivoCorreccion.setWrapText(true);
        motivoCorreccion.getStyleClass().add("result-notes-area");
        observaciones.setPromptText("Observaciones adicionales (opcional)");
        observaciones.setPrefRowCount(2);
        observaciones.setWrapText(true);
        observaciones.getStyleClass().add("result-notes-area");

        Label ayuda = new Label(correccion
                ? "Modificá el marcador. El cambio quedará registrado "
                    + "en el historial."
                : "Ingresá el marcador de cada set.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("result-helper-text");

        Label tituloMotivo = new Label("MOTIVO DE LA CORRECCIÓN");
        tituloMotivo.getStyleClass().add("result-field-title");
        Label tituloObservaciones = new Label("OBSERVACIONES ADICIONALES");
        tituloObservaciones.getStyleClass().add("result-field-title");

        VBox caja = new VBox(10);
        caja.getStyleClass().add(correccion
                ? "result-dialog-correction" : "result-dialog-register");
        caja.getChildren().addAll(
                ayuda, enfrentamiento, sets,
                incluirTercero, superTieBreak);
        if (correccion) {
            caja.getChildren().addAll(tituloMotivo, motivoCorreccion);
        }
        caja.getChildren().addAll(tituloObservaciones, observaciones);
        caja.setPadding(new Insets(6));
        caja.setMinWidth(560);
        caja.setPrefWidth(620);
        return caja;
    }

    private HBox crearFranjaEnfrentamiento() {
        Label pareja1 = crearTarjetaPareja(
                "PAREJA 1", partido.getPareja1InscripcionId());
        Label pareja2 = crearTarjetaPareja(
                "PAREJA 2", partido.getPareja2InscripcionId());
        Label versus = new Label("VS");
        versus.getStyleClass().add("result-versus");

        HBox franja = new HBox(10, pareja1, versus, pareja2);
        franja.setAlignment(Pos.CENTER);
        franja.getStyleClass().add("result-matchup-strip");
        HBox.setHgrow(pareja1, Priority.ALWAYS);
        HBox.setHgrow(pareja2, Priority.ALWAYS);
        return franja;
    }

    private Label crearTarjetaPareja(String rotulo, Long inscripcionId) {
        String nombre = nombrePareja(inscripcionId);
        Label etiqueta = new Label(rotulo + "\n" + nombre);
        etiqueta.setWrapText(true);
        etiqueta.setAlignment(Pos.CENTER);
        etiqueta.setMaxWidth(Double.MAX_VALUE);
        etiqueta.setMinWidth(0);
        etiqueta.setTooltip(new Tooltip(nombre));
        etiqueta.getStyleClass().add("result-team-card");
        return etiqueta;
    }

    private String nombrePareja(Long inscripcionId) {
        if (inscripcionId == null) return "Por definir";
        TorneoInscripcion inscripcion = inscripcionDAO.buscar(inscripcionId);
        if (inscripcion == null) return "Inscripción #" + inscripcionId;
        return inscripcion.getJugadores().stream()
                .map(jugador -> jugador.getNombreCompleto())
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .reduce((primero, segundo) -> primero + " / " + segundo)
                .orElse("Inscripción #" + inscripcionId);
    }

    private void agregarFila(
            GridPane grilla,
            int numero,
            ComboBox<Integer> pareja1,
            ComboBox<Integer> pareja2) {
        Label etiqueta = numero == 3
                ? etiquetaSet3 : new Label("Set " + numero);
        etiqueta.getStyleClass().add("result-set-label");
        pareja1.getStyleClass().add("result-score-combo");
        pareja2.getStyleClass().add("result-score-combo");
        grilla.add(etiqueta, 0, numero);
        grilla.add(pareja1, 1, numero);
        grilla.add(pareja2, 2, numero);
    }

    private void actualizarTercerSet(boolean visible) {
        etiquetaSet3.setVisible(visible);
        etiquetaSet3.setManaged(visible);
        set3Pareja1.setVisible(visible);
        set3Pareja1.setManaged(visible);
        set3Pareja2.setVisible(visible);
        set3Pareja2.setManaged(visible);
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
        if (correccion && (motivoCorreccion.getText() == null
                || motivoCorreccion.getText().isBlank())) {
            throw new IllegalArgumentException(
                    "Indica el motivo de la corrección.");
        }
        if (correccion && motivoCorreccion.getText().length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar los 500 caracteres.");
        }
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
        combo.setMinWidth(92);
        combo.setPrefWidth(104);
        combo.setMaxWidth(112);
        return combo;
    }
}
