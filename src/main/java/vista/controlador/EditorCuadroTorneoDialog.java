package vista.controlador;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import negocio.CrucePropuestoTorneo;
import negocio.PropuestaEtapaEliminatoria;
import servicio.PropuestaEtapaEliminatoriaService;
import vista.Dialogos;
import vista.Navegacion;

/** Editor unico: cruces seguros y acceso a estructura avanzada. */
public class EditorCuadroTorneoDialog {
    private final PropuestaEtapaEliminatoriaService service =
            new PropuestaEtapaEliminatoriaService();
    private final PropuestaEtapaEliminatoria borrador;
    private final PropuestaEtapaEliminatoria automatica;
    private final Map<String, String> nombres;
    private final Dialog<PropuestaEtapaEliminatoria> dialogo = new Dialog<>();
    private final StackPane vistas = new StackPane();
    private final Label estado = new Label();
    private VBox vistaCruces;
    private VBox vistaAvanzada;
    private Node botonAplicar;

    public EditorCuadroTorneoDialog(PropuestaEtapaEliminatoria origen,
            PropuestaEtapaEliminatoria automatica,
            Map<String, String> nombres) {
        this.borrador = service.copiar(origen);
        this.automatica = service.copiar(automatica);
        this.nombres = Map.copyOf(nombres);
        construir();
    }

    public PropuestaEtapaEliminatoria mostrar() {
        return dialogo.showAndWait().orElse(null);
    }

    private void construir() {
        dialogo.setTitle("Editor del cuadro");
        dialogo.setHeaderText("Editor del cuadro");
        ButtonType aplicar = new ButtonType("APLICAR CAMBIOS",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("CANCELAR",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(aplicar, cancelar);

        ToggleButton cruces = new ToggleButton("CRUCES");
        ToggleButton avanzada = new ToggleButton("ESTRUCTURA AVANZADA");
        cruces.getStyleClass().add("bracket-editor-tab");
        avanzada.getStyleClass().add("bracket-editor-tab");
        ToggleGroup grupo = new ToggleGroup();
        cruces.setToggleGroup(grupo);
        avanzada.setToggleGroup(grupo);
        cruces.setSelected(true);

        Label modo = etiqueta("MODO SEGURO", "bracket-editor-mode-badge");
        VBox contextoModo = new VBox(modo);
        contextoModo.setAlignment(Pos.CENTER_RIGHT);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox navegacion = new HBox(8, cruces, avanzada, espacio, contextoModo);
        navegacion.setAlignment(Pos.CENTER_LEFT);
        navegacion.getStyleClass().add("bracket-editor-navigation");

        vistaCruces = crearVistaCruces();
        vistaAvanzada = crearVistaAvanzada();
        vistas.getChildren().setAll(vistaCruces, vistaAvanzada);
        mostrar(vistaCruces, vistaAvanzada);
        grupo.selectedToggleProperty().addListener((o, anterior, actual) -> {
            if (actual == null) {
                cruces.setSelected(true);
                return;
            }
            boolean seguro = actual == cruces;
            modo.setText(seguro ? "MODO SEGURO" : "MODO AVANZADO");
            contextoModo.getStyleClass().removeAll("advanced");
            if (!seguro) contextoModo.getStyleClass().add("advanced");
            mostrar(seguro ? vistaCruces : vistaAvanzada,
                    seguro ? vistaAvanzada : vistaCruces);
        });

        estado.getStyleClass().add("bracket-editor-status");
        VBox raiz = new VBox(10, navegacion, vistas, estado);
        VBox.setVgrow(vistas, Priority.ALWAYS);
        raiz.setPadding(new Insets(12, 16, 10, 16));
        raiz.getStyleClass().add("bracket-editor-root");
        dialogo.getDialogPane().setContent(raiz);
        dialogo.getDialogPane().setPrefSize(1240, 800);
        dialogo.getDialogPane().setMinSize(920, 650);
        dialogo.setResizable(true);
        Dialogos.preparar(dialogo, "dialog-bracket-editor-new");
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());

        botonAplicar = dialogo.getDialogPane().lookupButton(aplicar);
        botonAplicar.getStyleClass().add("dialog-action-primary");
        botonAplicar.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            List<String> errores = service.validarEdicionManual(borrador);
            if (!errores.isEmpty()) {
                e.consume();
                Dialogos.error("Cuadro no válido",
                        "• " + String.join("\n\n• ", errores));
            }
        });
        dialogo.setResultConverter(tipo -> tipo == aplicar
                ? service.copiar(borrador) : null);
        actualizarEstado();
    }

    private VBox crearVistaCruces() {
        VBox informacion = crearInformacionSegura();

        Label ajustesTitulo = etiqueta("AJUSTES RÁPIDOS",
                "bracket-editor-tools-title");
        Button rotar = new Button("ROTAR PRIMEROS");
        Button iniciales = new Button("ROTAR RIVALES INICIALES");
        Button restaurar = new Button("RESTAURAR DISTRIBUCIÓN AUTOMÁTICA");
        rotar.getStyleClass().addAll("bracket-adjustment-button", "bracket-rotation-button");
        iniciales.getStyleClass().addAll("bracket-adjustment-button", "bracket-rotation-button");
        restaurar.getStyleClass().addAll("bracket-adjustment-button",
                "bracket-restore-button");
        // revision-final-editor-cuadro-v1
        for (Button boton : new Button[] { rotar, iniciales }) {
            boton.setMinSize(225, 38);
            boton.setPrefSize(225, 38);
            boton.setMaxSize(225, 38);
        }
        restaurar.setMinSize(270, 38);
        restaurar.setPrefSize(270, 38);
        restaurar.setMaxSize(270, 38);
        rotar.setOnAction(e -> {
            service.rotarPrimeros(borrador);
            refrescarCruces();
        });
        iniciales.setOnAction(e -> {
            service.intercambiarRivalesAcceso(borrador);
            refrescarCruces();
        });
        restaurar.setOnAction(e -> {
            if (!Dialogos.confirmar("Restaurar distribución",
                    "Se recuperará la propuesta automática dentro del editor. "
                    + "¿Queres continuar?")) return;
            service.restaurarPropuesta(borrador, automatica);
            refrescarCruces();
        });
        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);
        HBox acciones = new HBox(9, rotar, iniciales, separador, restaurar);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.getStyleClass().add("bracket-editor-tools");
        VBox herramientas = new VBox(5, ajustesTitulo, acciones);

        VBox lista = crearCrucesAgrupados();
        ScrollPane scroll = new ScrollPane(lista);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("bracket-editor-scroll");
        VBox contenido = new VBox(10, informacion, herramientas, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        contenido.getStyleClass().add("bracket-editor-mode");
        return contenido;
    }

    private VBox crearInformacionSegura() {
        Label titulo = etiqueta("EDICIÓN SEGURA DE CRUCES",
                "bracket-safe-title");
        Label texto = etiqueta(
                "Cambia las parejas de las plazas iniciales. Los ganadores "
                + "de rondas anteriores permanecen bloqueados para proteger la llave.",
                "bracket-safe-copy");
        VBox informacion = new VBox(3, titulo, texto);
        informacion.getStyleClass().add("bracket-safe-card");
        return informacion;
    }

    private VBox crearCrucesAgrupados() {
        VBox contenido = new VBox(13);
        Map<String, List<CrucePropuestoTorneo>> porFase =
                new LinkedHashMap<>();
        borrador.getCruces().stream()
                .sorted(Comparator.comparingInt(CrucePropuestoTorneo::getRonda)
                        .thenComparingInt(CrucePropuestoTorneo::getOrden))
                .forEach(cruce -> porFase.computeIfAbsent(
                        cruce.getInstancia(), clave -> new java.util.ArrayList<>())
                        .add(cruce));
        for (Map.Entry<String, List<CrucePropuestoTorneo>> entrada
                : porFase.entrySet()) {
            contenido.getChildren().add(crearSeccionFase(
                    entrada.getKey(), entrada.getValue()));
        }
        contenido.setPadding(new Insets(1, 9, 12, 0));
        return contenido;
    }

    private VBox crearSeccionFase(String fase,
            List<CrucePropuestoTorneo> cruces) {
        Label titulo = etiqueta(nombreVisibleFase(fase).toUpperCase(),
                "bracket-phase-title");
        titulo.getStyleClass().add(claseFase(fase));
        Label cantidad = etiqueta(cruces.size()
                + (cruces.size() == 1 ? " PARTIDO" : " PARTIDOS"),
                "bracket-phase-count");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox encabezado = new HBox(8, titulo, espacio, cantidad);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.getStyleClass().add("bracket-phase-header");

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        for (int i = 0; i < cruces.size(); i++) {
            Node tarjeta = crearTarjetaCruce(cruces.get(i));
            int columna = i % 2;
            int fila = i / 2;
            grilla.add(tarjeta, columna, fila);
            GridPane.setHgrow(tarjeta, Priority.ALWAYS);
            if (cruces.size() == 1) {
                GridPane.setColumnSpan(tarjeta, 2);
            }
        }
        javafx.scene.layout.ColumnConstraints izquierda =
                new javafx.scene.layout.ColumnConstraints();
        javafx.scene.layout.ColumnConstraints derecha =
                new javafx.scene.layout.ColumnConstraints();
        izquierda.setPercentWidth(50);
        derecha.setPercentWidth(50);
        izquierda.setHgrow(Priority.ALWAYS);
        derecha.setHgrow(Priority.ALWAYS);
        grilla.getColumnConstraints().setAll(izquierda, derecha);
        VBox seccion = new VBox(7, encabezado, grilla);
        seccion.getStyleClass().add("bracket-phase-section");
        return seccion;
    }

    private Node crearTarjetaCruce(CrucePropuestoTorneo cruce) {
        Label partido = etiqueta("PARTIDO " + cruce.getOrden(),
                "bracket-editor-match-title");
        HBox cabecera = new HBox(partido);
        cabecera.getStyleClass().add("bracket-match-header");
        GridPane plazas = new GridPane();
        plazas.setHgap(8);
        plazas.setVgap(7);
        plazas.setPadding(new Insets(9, 10, 10, 10));
        List<String> referencias = borrador.getClasificados().stream()
                .map(c -> c.referencia()).toList();
        agregarPlaza(plazas, referencias, cruce, true, 0);
        agregarPlaza(plazas, referencias, cruce, false, 1);
        javafx.scene.layout.ColumnConstraints etiqueta =
                new javafx.scene.layout.ColumnConstraints();
        etiqueta.setMinWidth(28);
        etiqueta.setPrefWidth(28);
        javafx.scene.layout.ColumnConstraints control =
                new javafx.scene.layout.ColumnConstraints();
        control.setHgrow(Priority.ALWAYS);
        plazas.getColumnConstraints().setAll(etiqueta, control);
        VBox tarjeta = new VBox(0, cabecera, plazas);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.getStyleClass().add("bracket-editor-match");
        return tarjeta;
    }

    private void agregarPlaza(GridPane grid, List<String> referencias,
            CrucePropuestoTorneo cruce, boolean primera, int fila) {
        String actual = primera
                ? cruce.getParticipante1() : cruce.getParticipante2();
        Label lado = etiqueta(primera ? "P1" : "P2",
                "bracket-editor-field-label");
        lado.setAlignment(Pos.CENTER);
        grid.add(lado, 0, fila);
        if (actual != null && actual.startsWith("Ganador ")) {
            Label origen = etiqueta(texto(actual),
                    "bracket-protected-origin");
            Label insignia = etiqueta("PROTEGIDA",
                    "bracket-protected-badge");
            Region espacioProtegido = new Region();
            HBox.setHgrow(espacioProtegido, Priority.ALWAYS);
            HBox protegida = new HBox(10, origen, espacioProtegido, insignia);
            protegida.setAlignment(Pos.CENTER_LEFT);
            protegida.setMaxWidth(Double.MAX_VALUE);
            protegida.getStyleClass().add("bracket-editor-protected");
            grid.add(protegida, 1, fila);
            GridPane.setHgrow(protegida, Priority.ALWAYS);
            return;
        }
        ComboBox<String> combo = new ComboBox<>(
                FXCollections.observableArrayList(referencias));
        combo.setValue(actual);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(String valor) {
                return texto(valor);
            }
            @Override public String fromString(String valor) {
                return valor;
            }
        });
        combo.setCellFactory(lista -> celdaReferencia(false));
        combo.setButtonCell(celdaReferencia(true));
        combo.valueProperty().addListener((o, anterior, valor) -> {
            if (primera) cruce.setParticipante1(valor);
            else cruce.setParticipante2(valor);
            actualizarEstado();
        });
        combo.getStyleClass().add("bracket-editor-combo");
        grid.add(combo, 1, fila);
        GridPane.setHgrow(combo, Priority.ALWAYS);
    }

    private javafx.scene.control.ListCell<String> celdaReferencia(
            boolean boton) {
        return new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(String valor,
                    boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : texto(valor));
                setTooltip(vacia || valor == null ? null
                        : new javafx.scene.control.Tooltip(texto(valor)));
                getStyleClass().removeAll(
                        "bracket-reference-cell", "button-cell");
                getStyleClass().add("bracket-reference-cell");
                if (boton) getStyleClass().add("button-cell");
            }
        };
    }

    private VBox crearVistaAvanzada() {
        Label titulo = etiqueta("ESTRUCTURA AVANZADA",
                "bracket-editor-section");
        Label ayuda = etiqueta(
                "Modificá fases, partidos y conexiones de la llave. "
                + "Usá esta herramienta solamente cuando necesites una "
                + "estructura no estándar.",
                "bracket-editor-help");

        VBox resumen = crearResumenEstructura();

        Label cambiosTitulo = etiqueta("CAMBIOS NO DEFINITIVOS",
                "bracket-advanced-note-title");
        Label cambiosTexto = etiqueta(
                "La estructura se modificará dentro del borrador. "
                + "Podés cancelar sin alterar la propuesta actual.",
                "bracket-advanced-note-copy");
        VBox aviso = new VBox(3, cambiosTitulo, cambiosTexto);
        aviso.getStyleClass().add("bracket-advanced-note");

        Label alternativaTitulo = etiqueta(
                "¿NECESITÁS CAMBIAR SOLAMENTE LAS PAREJAS?",
                "bracket-advanced-alternative-title");
        Label alternativaTexto = etiqueta(
                "Usá la pestaña CRUCES. Es el modo recomendado y mantiene "
                + "protegidas todas las conexiones entre ganadores.",
                "bracket-advanced-alternative-copy");
        VBox alternativa = new VBox(3, alternativaTitulo, alternativaTexto);
        alternativa.getStyleClass().add("bracket-advanced-alternative");

        Button abrir = new Button("ABRIR EDITOR ESTRUCTURAL");
        abrir.getStyleClass().add("bracket-advanced-open-button");
        // Animacion breve para comunicar que el editor estructural es interactivo.
        abrir.setOnMouseEntered(e -> animarBotonEstructural(abrir, 1.01, -1));
        abrir.setOnMouseExited(e -> animarBotonEstructural(abrir, 1.0, 0));
        abrir.setOnAction(e -> {
            List<CrucePropuestoTorneo> nuevos =
                    new EstructuraManualTorneoDialog(
                            borrador, nombres).mostrar();
            if (nuevos == null) return;
            borrador.setCruces(nuevos);
            borrador.setPases(List.of());
            refrescarCruces();
            actualizarEstado();
        });
        // advanced-layout-panel-v2
        resumen.setMaxWidth(Double.MAX_VALUE);
        resumen.getStyleClass().add("advanced-summary-wide");

        aviso.setMaxWidth(Double.MAX_VALUE);
        alternativa.setMaxWidth(Double.MAX_VALUE);
        aviso.setMinHeight(88);
        alternativa.setMinHeight(88);
        HBox.setHgrow(aviso, Priority.ALWAYS);
        HBox.setHgrow(alternativa, Priority.ALWAYS);
        aviso.getStyleClass().add("advanced-equal-notice");
        alternativa.getStyleClass().add("advanced-equal-notice");

        HBox avisos = new HBox(14, aviso, alternativa);
        avisos.setAlignment(Pos.TOP_LEFT);
        avisos.setFillHeight(true);
        avisos.setMaxWidth(Double.MAX_VALUE);
        avisos.getStyleClass().add("advanced-notices-row");

        Label ayudaEditor = etiqueta(
                "Permite agregar, eliminar y reconectar partidos.",
                "advanced-editor-help");
        VBox bloqueAccion = new VBox(5, ayudaEditor, abrir);
        bloqueAccion.setAlignment(Pos.CENTER_RIGHT);
        bloqueAccion.getStyleClass().add("advanced-editor-action");

        Region espacioAccion = new Region();
        HBox.setHgrow(espacioAccion, Priority.ALWAYS);
        HBox filaAccion = new HBox(12, espacioAccion, bloqueAccion);
        filaAccion.setAlignment(Pos.CENTER_RIGHT);
        filaAccion.setMaxWidth(Double.MAX_VALUE);

        VBox panel = new VBox(14, resumen, avisos, filaAccion);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setFillWidth(true);
        panel.getStyleClass().add("advanced-operation-panel");

        VBox tarjeta = new VBox(8, titulo, ayuda, panel);
        tarjeta.setMaxWidth(1350);
        tarjeta.setPrefWidth(1240);
        tarjeta.setFillWidth(true);
        tarjeta.getStyleClass().add("advanced-structure-card");
        tarjeta.getStyleClass().add("advanced-structure-card-v2");

        StackPane centro = new StackPane(tarjeta);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(48, 34, 22, 34));
        centro.setMaxHeight(Region.USE_PREF_SIZE);

        VBox contenido = new VBox(centro);
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setFillWidth(true);
        contenido.getStyleClass().add("editor-advanced-view");
        return contenido;
    }

    private VBox crearResumenEstructura() {
        Map<String, Long> cantidades = borrador.getCruces().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        CrucePropuestoTorneo::getInstancia,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()));
        int fases = cantidades.size();
        int partidos = borrador.getCruces().size();

        Label titulo = etiqueta("ESTRUCTURA ACTUAL",
                "bracket-structure-summary-title");
        Label general = etiqueta(partidos + " partidos   ·   "
                + fases + (fases == 1 ? " fase" : " fases"),
                "bracket-structure-summary-main");

        HBox fasesVisuales = new HBox(8);
        fasesVisuales.setAlignment(Pos.CENTER_LEFT);
        for (Map.Entry<String, Long> entrada : cantidades.entrySet()) {
            Label fase = etiqueta(nombreVisibleFase(entrada.getKey())
                    + " · " + entrada.getValue(),
                    "bracket-structure-phase-chip");
            fase.getStyleClass().add(claseFase(entrada.getKey()));
            fasesVisuales.getChildren().add(fase);
        }

        VBox resumen = new VBox(6, titulo, general, fasesVisuales);
        resumen.getStyleClass().add("bracket-structure-summary");
        return resumen;
    }

    private void refrescarCruces() {
        VBox nueva = crearVistaCruces();
        int indice = vistas.getChildren().indexOf(vistaCruces);
        if (indice >= 0) vistas.getChildren().set(indice, nueva);
        vistaCruces = nueva;
        actualizarEstado();
    }

    private void actualizarEstado() {
        List<String> errores = service.validarEdicionManual(borrador);
        boolean valido = errores.isEmpty();
        estado.setText(valido
                ? "BORRADOR VÁLIDO   ·   " + borrador.getCruces().size()
                        + " PARTIDOS   ·   0 PROBLEMAS"
                : "REQUIERE ATENCIÓN   ·   " + errores.size()
                        + " PROBLEMA(S) PENDIENTE(S)");
        estado.getStyleClass().removeAll("valid", "warning");
        estado.getStyleClass().add(valido ? "valid" : "warning");
        if (botonAplicar != null) botonAplicar.setDisable(!valido);
    }

    private void mostrar(Node visible, Node oculto) {
        visible.setVisible(true);
        visible.setManaged(true);
        oculto.setVisible(false);
        oculto.setManaged(false);
    }

    private String texto(String referencia) {
        if (referencia == null) return "Sin asignar";
        String nombre = nombres.get(referencia);
        if (nombre != null) return referencia + " · " + nombre;
        if (referencia.startsWith("Ganador ")) {
            String clave = referencia.substring("Ganador ".length());
            int indice = clave.lastIndexOf(" #");
            if (indice >= 0) {
                return "Ganador de " + clave.substring(0, indice)
                        + " · Partido " + clave.substring(indice + 2);
            }
        }
        return referencia;
    }

    private String nombreVisibleFase(String fase) {
        if (fase == null) return "Fase";
        if (fase.startsWith("Acceso R")) {
            return "Fase previa " + fase.substring("Acceso R".length());
        }
        return switch (fase) {
            case "Cuartos" -> "Cuartos de final";
            case "Semifinal" -> "Semifinales";
            default -> fase;
        };
    }

    private String claseFase(String fase) {
        String valor = fase == null ? "" : fase.toLowerCase();
        if (valor.equals("final")) return "phase-final";
        if (valor.contains("semifinal")) return "phase-semifinal";
        if (valor.contains("cuartos")) return "phase-quarter";
        if (valor.contains("octavos")) return "phase-round16";
        if (valor.contains("dieciseisavos")) return "phase-round32";
        return "phase-access";
    }

    private Label etiqueta(String texto, String clase) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add(clase);
        return label;
    }

    private void animarBotonEstructural(Button boton, double escala, double desplazamientoY) {
        javafx.animation.ScaleTransition escalaTransicion =
                new javafx.animation.ScaleTransition(javafx.util.Duration.millis(125), boton);
        escalaTransicion.setToX(escala);
        escalaTransicion.setToY(escala);

        javafx.animation.TranslateTransition desplazamientoTransicion =
                new javafx.animation.TranslateTransition(javafx.util.Duration.millis(125), boton);
        desplazamientoTransicion.setToY(desplazamientoY);

        javafx.animation.ParallelTransition animacion =
                new javafx.animation.ParallelTransition(escalaTransicion, desplazamientoTransicion);
        animacion.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        animacion.play();
    }

}
