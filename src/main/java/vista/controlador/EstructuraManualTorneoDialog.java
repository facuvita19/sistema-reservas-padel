package vista.controlador;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import negocio.CrucePropuestoTorneo;
import negocio.PropuestaEtapaEliminatoria;
import servicio.ValidadorEstructuraEliminatoriaService;
import vista.Dialogos;
import vista.Navegacion;

public class EstructuraManualTorneoDialog {
    private static final List<String> INSTANCIAS = List.of(
            "Acceso R1", "Acceso R2", "Acceso R3", "Acceso R4",
            "Acceso R5", "Dieciseisavos", "Octavos", "Cuartos",
            "Semifinal", "Final");

    private final PropuestaEtapaEliminatoria propuesta;
    private final Map<String, String> nombres;
    private final Dialog<List<CrucePropuestoTorneo>> dialogo = new Dialog<>();
    private final VBox filas = new VBox(10);
    private final List<Fila> ediciones = new ArrayList<>();
    private final Label estado = etiqueta("", "#d7e4e9", 12, true);
    private final ValidadorEstructuraEliminatoriaService validador =
            new ValidadorEstructuraEliminatoriaService();
    private Button aplicar;

    public EstructuraManualTorneoDialog(
            PropuestaEtapaEliminatoria propuesta,
            Map<String, String> nombresPorReferencia) {
        this.propuesta = propuesta;
        this.nombres = new HashMap<>(nombresPorReferencia);
        construir();
    }

    public List<CrucePropuestoTorneo> mostrar() {
        return dialogo.showAndWait().orElse(null);
    }

    private void construir() {
        dialogo.setTitle("Configuracion estructural manual");
        dialogo.setHeaderText("Crea, elimina y conecta los partidos del cuadro");
        ButtonType aplicarTipo = new ButtonType("APLICAR ESTRUCTURA",
                ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().setAll(aplicarTipo,
                new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        aplicar = (Button) dialogo.getDialogPane().lookupButton(aplicarTipo);
        aplicar.setStyle("-fx-background-color:#4f91b5;"
                + "-fx-text-fill:#ffffff;-fx-font-weight:900;"
                + "-fx-background-radius:9;-fx-padding:11 24;");

        Label seccion = etiqueta("EDITOR ESTRUCTURAL DEL CUADRO",
                "#91d7f4", 12, false);
        seccion.setStyle(seccion.getStyle()
                + "-fx-font-weight:900;-fx-letter-spacing:0.8px;");
        Label ayuda = etiqueta(
                "Organiza las fases, el orden de los partidos y el origen "
                + "de cada plaza. Podes usar parejas clasificadas o ganadores "
                + "de encuentros anteriores.", "#d7e4e9", 13, true);
        Label aviso = etiqueta(
                "Los cambios se guardan solamente al aplicar la estructura. "
                + "Cancelar conserva la propuesta anterior.",
                "#ffd58a", 12, true);
        VBox introduccion = new VBox(6, seccion, ayuda, aviso);
        introduccion.setPadding(new Insets(14));
        introduccion.setStyle("-fx-background-color:#102532;"
                + "-fx-border-color:#315f79;-fx-border-radius:10;"
                + "-fx-background-radius:10;");
        Button agregar = new Button("+  AGREGAR PARTIDO");
        agregar.setStyle("-fx-background-color:#245875;"
                + "-fx-text-fill:#ffffff;-fx-font-weight:900;"
                + "-fx-background-radius:8;-fx-padding:10 18;"
                + "-fx-cursor:hand;");
        agregar.setOnAction(e -> agregarFila(null));

        for (CrucePropuestoTorneo cruce : propuesta.getCruces()) {
            agregarFila(cruce);
        }

        ScrollPane scroll = new ScrollPane(filas);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(520);
        scroll.setStyle("-fx-background:#08151e;"
                + "-fx-background-color:#08151e;"
                + "-fx-border-color:transparent;");
        VBox barraAcciones = new VBox(8, agregar);
        barraAcciones.setPadding(new Insets(2, 0, 2, 0));
        VBox contenido = new VBox(14, introduccion, barraAcciones, scroll,
                estado);
        contenido.setPadding(new Insets(18));
        contenido.setStyle("-fx-background-color:#08151e;");
        contenido.setPrefWidth(1040);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(1120, 760);
        dialogo.setResizable(true);
        Dialogos.preparar(dialogo, "dialog-tournament-bracket-manual");
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());

        aplicar.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            List<String> errores = validar();
            if (!errores.isEmpty()) {
                evento.consume();
                Dialogos.error("Estructura incompleta",
                        mensaje("Revisa los siguientes problemas:", errores));
            }
        });
        dialogo.setResultConverter(tipo ->
                tipo == aplicarTipo ? construirResultado() : null);
        actualizarOpciones();
    }

    private void agregarFila(CrucePropuestoTorneo cruce) {
        Fila fila = new Fila(cruce);
        ediciones.add(fila);
        filas.getChildren().add(fila.raiz);
        actualizarOpciones();
    }

    private void quitar(Fila fila) {
        ediciones.remove(fila);
        filas.getChildren().remove(fila.raiz);
        actualizarOpciones();
    }

    private void actualizarOpciones() {
        Set<String> opciones = new LinkedHashSet<>();
        propuesta.getClasificados().stream()
                .map(c -> c.referencia()).forEach(opciones::add);
        ediciones.stream()
                .map(Fila::referenciaGanador)
                .filter(r -> r != null)
                .forEach(opciones::add);
        for (Fila fila : ediciones) fila.actualizarOpciones(opciones);
        estado.setText("Partidos configurados: " + ediciones.size()
                + " | Clasificados disponibles: "
                + propuesta.getClasificados().size());
    }

    private List<String> validar() {
        List<String> errores = new ArrayList<>();
        if (ediciones.isEmpty()) errores.add("La estructura no contiene partidos.");
        Set<String> claves = new LinkedHashSet<>();
        Set<String> ganadores = new LinkedHashSet<>();
        for (Fila fila : ediciones) {
            String clave = fila.clave();
            if (!claves.add(clave)) {
                errores.add("PARTIDO REPETIDO:\n"
                        + describirClaveAmigable(clave)
                        + " aparece mas de una vez dentro de la misma llave.\n"
                        + "Cambia el orden de una de las tarjetas.");
            }
            ganadores.add(fila.referenciaGanador());
            if (fila.p1.getValue() == null || fila.p2.getValue() == null) {
                errores.add(clave + " tiene una plaza sin asignar.");
            } else if (fila.p1.getValue().equals(fila.p2.getValue())) {
                errores.add(clave + " repite el mismo origen.");
            }
        }
        Set<String> clasificados = propuesta.getClasificados().stream()
                .map(c -> c.referencia())
                .collect(java.util.stream.Collectors.toCollection(
                        LinkedHashSet::new));
        List<String> directos = ediciones.stream()
                .flatMap(f -> java.util.stream.Stream.of(
                        f.p1.getValue(), f.p2.getValue()))
                .filter(java.util.Objects::nonNull)
                .filter(r -> !r.startsWith("Ganador "))
                .toList();
        for (String clasificado : clasificados) {
            long cantidad = directos.stream().filter(clasificado::equals).count();
            if (cantidad == 0) errores.add("Falta " + clasificado + ".");
            if (cantidad > 1) errores.add(clasificado + " esta repetido.");
        }
        for (Fila fila : ediciones) {
            for (String origen : List.of(fila.p1.getValue(), fila.p2.getValue())) {
                if (origen != null && origen.startsWith("Ganador ")
                        && !ganadores.contains(origen)) {
                    errores.add("ORIGEN INEXISTENTE:\n"
                            + describirClaveAmigable(fila.clave())
                            + " intenta usar " + describirGanadorAmigable(origen)
                            + ", pero ese partido ya no existe en la llave.\n"
                            + "Selecciona otro origen o corrige el orden del "
                            + "partido de origen.");
                }
                if (origen != null && origen.equals(fila.referenciaGanador())) {
                    errores.add(fila.clave() + " se referencia a si mismo.");
                }
            }
        }
        long finales = ediciones.stream()
                .filter(f -> "Final".equals(f.instancia.getValue())).count();
        if (finales != 1) errores.add("Debe existir exactamente una final.");
        if (errores.isEmpty()) {
            errores.addAll(validador.validar(construirResultado(),
                    propuesta.getClasificados()));
        }
        return errores.stream().distinct().toList();
    }

    private List<CrucePropuestoTorneo> construirResultado() {
        List<CrucePropuestoTorneo> resultado = ediciones.stream()
                .map(Fila::crearCruce)
                .sorted(Comparator.comparingInt(CrucePropuestoTorneo::getRonda)
                        .thenComparingInt(CrucePropuestoTorneo::getOrden))
                .toList();
        return new ArrayList<>(resultado);
    }

    private String describirGanadorAmigable(String referencia) {
        if (referencia == null || !referencia.startsWith("Ganador ")) {
            return referencia == null ? "un ganador sin identificar"
                    : referencia;
        }
        return "el ganador de " + describirClaveAmigable(
                referencia.substring("Ganador ".length()));
    }

    private String describirClaveAmigable(String clave) {
        if (clave == null || clave.isBlank()) {
            return "un partido sin identificar";
        }
        int separador = clave.lastIndexOf(" #");
        if (separador < 0) return clave;
        return clave.substring(0, separador) + " (Partido "
                + clave.substring(separador + 2) + ")";
    }

    private String mostrar(String referencia) {
        if (referencia == null) return null;
        String nombre = nombres.get(referencia);
        return nombre == null ? referencia : referencia + "  ·  " + nombre;
    }

    private String mensaje(String titulo, List<String> errores) {
        StringBuilder texto = new StringBuilder(titulo);
        for (String error : errores) {
            texto.append("\n\n").append("• ").append(error);
        }
        return texto.toString();
    }

    private static void estiloCombo(ComboBox<String> combo, double ancho) {
        combo.setPrefWidth(ancho);
        combo.setStyle("-fx-background-color:#0a1922;"
                + "-fx-border-color:#3d6f88;-fx-border-radius:7;"
                + "-fx-background-radius:7;-fx-mark-color:#91d7f4;"
                + "-fx-text-fill:#f4fbff;-fx-padding:2 6;");
        combo.setButtonCell(celdaTexto(true));
        combo.setCellFactory(lista -> celdaTexto(false));
    }

    private static javafx.scene.control.ListCell<String> celdaTexto(
            boolean boton) {
        return new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : valor);
                setTextFill(javafx.scene.paint.Color.web("#f4fbff"));
                setStyle((boton
                        ? "-fx-background-color:#0a1922;"
                        : isSelected()
                            ? "-fx-background-color:#315f79;"
                            : "-fx-background-color:#102532;")
                        + "-fx-text-fill:#f4fbff;-fx-padding:7 10;");
            }
        };
    }

    private static void estiloComboNumerico(ComboBox<Integer> combo,
            double ancho) {
        combo.setPrefWidth(ancho);
        combo.setStyle("-fx-background-color:#0a1922;"
                + "-fx-border-color:#3d6f88;-fx-border-radius:7;"
                + "-fx-background-radius:7;-fx-mark-color:#91d7f4;"
                + "-fx-text-fill:#f4fbff;-fx-padding:2 6;");
        combo.setButtonCell(celdaNumero(true));
        combo.setCellFactory(lista -> celdaNumero(false));
    }

    private static javafx.scene.control.ListCell<Integer> celdaNumero(
            boolean boton) {
        return new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Integer valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : valor.toString());
                setTextFill(javafx.scene.paint.Color.web("#f4fbff"));
                setStyle((boton
                        ? "-fx-background-color:#0a1922;"
                        : isSelected()
                            ? "-fx-background-color:#315f79;"
                            : "-fx-background-color:#102532;")
                        + "-fx-text-fill:#f4fbff;-fx-padding:7 10;"
                        + "-fx-font-weight:800;");
            }
        };
    }

    private static Label etiqueta(String texto, String color,
            int tamano, boolean wrap) {
        Label label = new Label(texto);
        label.setWrapText(wrap);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-text-fill:" + color + ";-fx-font-size:"
                + tamano + "px;");
        return label;
    }

    private final class Fila {
        private final VBox raiz = new VBox(8);
        private final ComboBox<String> instancia = new ComboBox<>();
        private final ComboBox<Integer> ronda = new ComboBox<>();
        private final ComboBox<Integer> orden = new ComboBox<>();
        private final ComboBox<String> p1 = new ComboBox<>();
        private final ComboBox<String> p2 = new ComboBox<>();

        private Fila(CrucePropuestoTorneo cruce) {
            instancia.setItems(FXCollections.observableArrayList(INSTANCIAS));
            instancia.setValue(cruce == null ? "Cuartos" : cruce.getInstancia());
            ronda.setItems(FXCollections.observableArrayList(
                    java.util.stream.IntStream.rangeClosed(1, 10)
                            .boxed().toList()));
            orden.setItems(FXCollections.observableArrayList(
                    java.util.stream.IntStream.rangeClosed(1, 32)
                            .boxed().toList()));
            ronda.setValue(cruce == null ? 1 : cruce.getRonda());
            orden.setValue(cruce == null ? 1 : cruce.getOrden());
            estiloCombo(instancia, 170);
            estiloComboNumerico(ronda, 92);
            estiloComboNumerico(orden, 92);
            Button eliminar = new Button("ELIMINAR");
            eliminar.setStyle("-fx-background-color:#43262b;"
                    + "-fx-border-color:#a95660;-fx-text-fill:#ffd7da;"
                    + "-fx-font-weight:900;-fx-border-radius:7;"
                    + "-fx-background-radius:7;-fx-padding:8 14;"
                    + "-fx-cursor:hand;");
            eliminar.setOnAction(e -> quitar(this));
            Label faseLabel = etiqueta("FASE", "#91b3c3", 11, false);
            Label rondaLabel = etiqueta("RONDA", "#91b3c3", 11, false);
            Label ordenLabel = etiqueta("ORDEN", "#91b3c3", 11, false);
            HBox encabezado = new HBox(10, faseLabel, instancia,
                    rondaLabel, ronda, ordenLabel, orden, eliminar);
            encabezado.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            GridPane plazas = new GridPane();
            plazas.setHgap(10);
            plazas.setVgap(8);
            plazas.add(etiqueta("Origen 1", "#b9ced8", 12, false), 0, 0);
            plazas.add(p1, 1, 0);
            plazas.add(etiqueta("Origen 2", "#b9ced8", 12, false), 0, 1);
            plazas.add(p2, 1, 1);
            estiloCombo(p1, 760);
            estiloCombo(p2, 760);
            p1.setMaxWidth(Double.MAX_VALUE);
            p2.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(p1, Priority.ALWAYS);
            GridPane.setHgrow(p2, Priority.ALWAYS);
            raiz.getChildren().addAll(encabezado, plazas);
            raiz.setPadding(new Insets(14));
            raiz.setStyle("-fx-background-color:#0f2633;"
                    + "-fx-border-color:#315f79;-fx-border-width:1;"
                    + "-fx-border-radius:11;-fx-background-radius:11;"
                    + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.26),"
                    + "10,0.12,0,3);");
            if (cruce != null) {
                p1.setValue(cruce.getParticipante1());
                p2.setValue(cruce.getParticipante2());
            }
            instancia.valueProperty().addListener((o, a, b) -> EstructuraManualTorneoDialog.this.actualizarOpciones());
            ronda.valueProperty().addListener((o, a, b) -> EstructuraManualTorneoDialog.this.actualizarOpciones());
            orden.valueProperty().addListener((o, a, b) -> EstructuraManualTorneoDialog.this.actualizarOpciones());
        }

        private String clave() {
            return instancia.getValue() + " #" + orden.getValue();
        }
        private String referenciaGanador() {
            if (instancia.getValue() == null) return null;
            return "Ganador " + instancia.getValue() + " #" + orden.getValue();
        }
        private void actualizarOpciones(Set<String> opciones) {
            String actual1 = p1.getValue();
            String actual2 = p2.getValue();
            List<String> lista = new ArrayList<>(opciones);
            p1.setItems(FXCollections.observableArrayList(lista));
            p2.setItems(FXCollections.observableArrayList(lista));
            p1.setValue(actual1);
            p2.setValue(actual2);
            configurarCeldas(p1);
            configurarCeldas(p2);
        }
        private void configurarCeldas(ComboBox<String> combo) {
            combo.setCellFactory(v -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String valor, boolean vacia) {
                    super.updateItem(valor, vacia);
                    setText(vacia || valor == null ? null : mostrar(valor));
                    setTextFill(javafx.scene.paint.Color.web("#f4fbff"));
                    setStyle((isSelected()
                            ? "-fx-background-color:#315f79;"
                            : "-fx-background-color:#102532;")
                            + "-fx-text-fill:#f4fbff;-fx-padding:8 10;");
                }
            });
            combo.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(String valor, boolean vacia) {
                    super.updateItem(valor, vacia);
                    setText(vacia || valor == null ? null : mostrar(valor));
                    setTextFill(javafx.scene.paint.Color.web("#f4fbff"));
                    setStyle("-fx-background-color:#0a1922;"
                            + "-fx-text-fill:#f4fbff;-fx-padding:7 10;");
                }
            });
        }
        private CrucePropuestoTorneo crearCruce() {
            return new CrucePropuestoTorneo(instancia.getValue(),
                    ronda.getValue(), orden.getValue(),
                    p1.getValue(), p2.getValue());
        }
    }
}
