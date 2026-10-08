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
    private final PropuestaEtapaEliminatoria inicial;
    private final PropuestaEtapaEliminatoria borrador;
    private final PropuestaEtapaEliminatoria automatica;
    private final Map<String, String> nombres;
    private final Dialog<PropuestaEtapaEliminatoria> dialogo = new Dialog<>();
    private final StackPane vistas = new StackPane();
    private final Label estado = new Label();
    private VBox vistaCruces;
    private VBox vistaAvanzada;
    private Node botonAplicar;
    private ScrollPane scrollCrucesSeguro;
    private final java.util.Set<String> plazasModificadas =
            new java.util.LinkedHashSet<>();
    private boolean hayCambiosPendientes;

    public EditorCuadroTorneoDialog(PropuestaEtapaEliminatoria origen,
            PropuestaEtapaEliminatoria automatica,
            Map<String, String> nombres) {
        this.inicial = service.copiar(origen);
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

        Label contextoCompetencia = new Label(cargarContextoCompetencia());
        contextoCompetencia.getStyleClass().add("bracket-editor-context-v2a");
        estado.getStyleClass().add("bracket-editor-status");
        Label consecuencia = new Label(
                "Los cambios se aplicarán solamente al borrador de la propuesta.");
        consecuencia.getStyleClass().add("bracket-editor-consequence-v2a");
        Region espacioEstado = new Region();
        HBox.setHgrow(espacioEstado, Priority.ALWAYS);
        HBox pieBorrador = new HBox(10, estado, espacioEstado, consecuencia);
        pieBorrador.setAlignment(Pos.CENTER_LEFT);
        pieBorrador.getStyleClass().add("bracket-editor-draft-bar-v2a");
        VBox raiz = new VBox(8, contextoCompetencia, navegacion, vistas,
                pieBorrador);
        VBox.setVgrow(vistas, Priority.ALWAYS);
        raiz.setPadding(new Insets(12, 16, 10, 16));
        raiz.getStyleClass().add("bracket-editor-root");
        dialogo.getDialogPane().setContent(raiz);
        dialogo.getDialogPane().setPrefSize(1240, 800);
        dialogo.getDialogPane().setMinSize(920, 650);
        dialogo.setResizable(true);
        dialogo.setOnShown(e -> javafx.application.Platform.runLater(() -> {
            javafx.stage.Window ventana = dialogo.getDialogPane()
                    .getScene().getWindow();
            if (ventana instanceof javafx.stage.Stage stage) {
                stage.setMaximized(true);
            }
        }));
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
                return;
            }
            if (!confirmarCambios()) {
                e.consume();
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
        Button iniciales = new Button("ROTAR RIVALES");
        Button restaurar = new Button("RESTABLECER");
        rotar.getStyleClass().addAll("bracket-adjustment-button",
                "bracket-rotation-button", "bracket-rotate-first-v3");
        iniciales.getStyleClass().addAll("bracket-adjustment-button",
                "bracket-rotation-button", "bracket-rotate-rivals-v3");
        restaurar.getStyleClass().addAll("bracket-adjustment-button",
                "bracket-restore-button", "bracket-restore-v3");
        rotar.setMinSize(145, 36);
        rotar.setPrefSize(145, 36);
        rotar.setMaxSize(145, 36);
        iniciales.setMinSize(155, 36);
        iniciales.setPrefSize(155, 36);
        iniciales.setMaxSize(155, 36);
        restaurar.setMinSize(155, 36);
        restaurar.setPrefSize(155, 36);
        restaurar.setMaxSize(155, 36);
        rotar.setTooltip(new javafx.scene.control.Tooltip(
                "Rota los primeros clasificados entre las plazas protegidas."));
        iniciales.setTooltip(new javafx.scene.control.Tooltip(
                "Intercambia los rivales iniciales sin modificar conexiones protegidas."));
        restaurar.setTooltip(new javafx.scene.control.Tooltip(
                "Descarta los cambios del editor y recupera la distribución automática."));
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
            plazasModificadas.clear();
            hayCambiosPendientes = false;
            refrescarCruces();
            refrescarVistaAvanzada();
        });
        HBox acciones = new HBox(9, rotar, iniciales, restaurar);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.getStyleClass().add("bracket-editor-tools");
        VBox herramientas = new VBox(5, ajustesTitulo, acciones);

        VBox lista = crearCrucesAgrupados();
        ScrollPane scroll = new ScrollPane(lista);
        scrollCrucesSeguro = scroll;
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
        Label cantidad = etiqueta(String.valueOf(cruces.size()),
                "bracket-phase-count");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox encabezado = new HBox(8, titulo, espacio, cantidad);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.getStyleClass().add("bracket-phase-header");

        GridPane grilla = new GridPane();
        grilla.setHgap(12);
        grilla.setVgap(12);
        grilla.setAlignment(Pos.TOP_LEFT);
        int columnas = columnasParaFase(fase, cruces.size());
        int maximoColumna = maximoPorColumna(fase, cruces.size());
        grilla.setMaxWidth(Region.USE_PREF_SIZE);
        grilla.setPrefWidth(columnas * 720.0
                + Math.max(0, columnas - 1) * 12.0);
        for (int indice = 0; indice < cruces.size(); indice++) {
            Node tarjeta = crearTarjetaCruce(cruces.get(indice));
            int columna;
            int fila;
            if (columnas == 1) {
                columna = 0;
                fila = indice;
            } else if (fase.toLowerCase().contains("octavos")
                    || fase.toLowerCase().contains("dieciseisavos")) {
                columna = indice / maximoColumna;
                fila = indice % maximoColumna;
            } else {
                columna = indice % columnas;
                fila = indice / columnas;
            }
            grilla.add(tarjeta, columna, fila);
            GridPane.setHgrow(tarjeta, Priority.NEVER);
            GridPane.setFillWidth(tarjeta, false);
        }
        for (int indice = 0; indice < columnas; indice++) {
            javafx.scene.layout.ColumnConstraints columna =
                    new javafx.scene.layout.ColumnConstraints();
            columna.setMinWidth(720);
            columna.setPrefWidth(720);
            columna.setMaxWidth(720);
            columna.setHgrow(Priority.NEVER);
            grilla.getColumnConstraints().add(columna);
        }
        VBox seccion = new VBox(7, encabezado, grilla);
        seccion.getStyleClass().add("bracket-phase-section");
        return seccion;
    }

    private int columnasParaFase(String fase, int cantidad) {
        String valor = fase == null ? "" : fase.toLowerCase();
        if (valor.equals("final") || valor.contains("semifinal")) return 1;
        if (valor.contains("cuartos")) return cantidad > 1 ? 2 : 1;
        if (valor.contains("octavos")) return cantidad > 4 ? 2 : 1;
        if (valor.contains("dieciseisavos")) return cantidad > 8 ? 2 : 1;
        return cantidad > 4 ? 2 : 1;
    }

    private int maximoPorColumna(String fase, int cantidad) {
        String valor = fase == null ? "" : fase.toLowerCase();
        if (valor.contains("octavos")) return 4;
        if (valor.contains("dieciseisavos")) return 8;
        return Math.max(1, (int) Math.ceil(cantidad / 2.0));
    }

    private Node crearTarjetaCruce(CrucePropuestoTorneo cruce) {
        Label partido = etiqueta("PARTIDO " + cruce.getOrden(),
                "bracket-editor-match-title");
        boolean mostrarPartido = !"Final".equalsIgnoreCase(
                cruce.getInstancia());
        partido.setVisible(mostrarPartido);
        partido.setManaged(mostrarPartido);
        HBox cabecera = new HBox(partido);
        cabecera.getStyleClass().add("bracket-match-header");
        GridPane plazas = new GridPane();
        plazas.setHgap(8);
        plazas.setVgap(5);
        plazas.setPadding(new Insets(6, 10, 7, 10));
        List<String> referencias = borrador.getClasificados().stream()
                .map(c -> c.referencia()).toList();
        agregarPlaza(plazas, referencias, cruce, true, 0);
        agregarPlaza(plazas, referencias, cruce, false, 1);
        javafx.scene.layout.ColumnConstraints etiqueta =
                new javafx.scene.layout.ColumnConstraints();
        etiqueta.setMinWidth(76);
        etiqueta.setPrefWidth(76);
        etiqueta.setMaxWidth(76);
        javafx.scene.layout.ColumnConstraints control =
                new javafx.scene.layout.ColumnConstraints();
        control.setHgrow(Priority.ALWAYS);
        plazas.getColumnConstraints().setAll(etiqueta, control);
        VBox tarjeta = new VBox(0, cabecera, plazas);
        String claveCruce = claveCruce(cruce);
        boolean modificada = plazasModificadas.stream()
                .anyMatch(clave -> clave.startsWith(claveCruce + "|"));
        if (modificada) tarjeta.getStyleClass().add("modified");
        List<String> avisos = advertenciasDelCruce(cruce);
        if (!avisos.isEmpty()) {
            Label advertencia = etiqueta(String.join("  ", avisos),
                    "bracket-match-warning-v2b");
            tarjeta.getChildren().add(advertencia);
            tarjeta.getStyleClass().add("warning");
        }
        tarjeta.setMinSize(720, 140);
        tarjeta.setPrefSize(720, 140);
        tarjeta.setMaxSize(720, 140);
        tarjeta.getStyleClass().add("bracket-editor-match");
        return tarjeta;
    }

    private void agregarPlaza(GridPane grid, List<String> referencias,
            CrucePropuestoTorneo cruce, boolean primera, int fila) {
        String actual = primera
                ? cruce.getParticipante1() : cruce.getParticipante2();
        Label lado = etiqueta(primera ? "PLAZA 1" : "PLAZA 2",
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
            javafx.scene.shape.SVGPath candado = new javafx.scene.shape.SVGPath();
            candado.setContent("M7 7V5a5 5 0 0 1 10 0v2h1a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1zm2 0h6V5a3 3 0 0 0-6 0v2z");
            candado.getStyleClass().add("bracket-protected-lock-v2a");
            HBox protegida = new HBox(9, candado, origen,
                    espacioProtegido, insignia);
            protegida.setAlignment(Pos.CENTER_LEFT);
            protegida.setMinSize(0, 40);
            protegida.setPrefHeight(40);
            protegida.setMinWidth(590);
            protegida.setPrefWidth(590);
            protegida.setMaxWidth(590);
            protegida.setCursor(javafx.scene.Cursor.DEFAULT);
            javafx.scene.control.Tooltip tooltipProtegida =
                    new javafx.scene.control.Tooltip(
                            "Esta plaza depende de un partido anterior y no puede modificarse en modo seguro.");
            javafx.scene.control.Tooltip.install(protegida, tooltipProtegida);
            protegida.getStyleClass().add("bracket-editor-protected");
            grid.add(protegida, 1, fila);
            GridPane.setHgrow(protegida, Priority.ALWAYS);
            return;
        }
        ComboBox<String> combo = new ComboBox<>(
                FXCollections.observableArrayList(referencias));
        combo.setValue(actual);
        combo.setVisibleRowCount(6);
        combo.setMinHeight(40);
        combo.setPrefHeight(40);
        combo.setMaxHeight(40);
        combo.setMinWidth(590);
        combo.setPrefWidth(590);
        combo.setMaxWidth(590);
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
        String clavePlaza = clavePlaza(cruce, primera);
        if (plazasModificadas.contains(clavePlaza)) {
            combo.getStyleClass().add("modified");
            combo.setPromptText("MODIFICADA");
        }
        combo.valueProperty().addListener((o, anterior, valor) -> {
            if (valor == null || java.util.Objects.equals(anterior, valor)) return;
            if (service.reasignarIntercambiando(
                    borrador, cruce, primera, valor)) {
                plazasModificadas.add(clavePlaza);
                marcarPlazaDe(anterior);
                hayCambiosPendientes = true;
                refrescarCruces();
            }
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
                getStyleClass().removeAll("bracket-reference-cell",
                        "button-cell", "bracket-popup-row-v4");
                getStyleClass().add("bracket-reference-cell");
                if (boton) getStyleClass().add("button-cell");
                else getStyleClass().add("bracket-popup-row-v4");
            }
        };
    }

    private VBox crearVistaAvanzada() {
        Label titulo = etiqueta("ESTRUCTURA AVANZADA",
                "bracket-editor-section");
        Label ayuda = etiqueta(
                "Modificá fases, partidos y conexiones solamente cuando "
                + "necesites una llave no estándar. Los cambios permanecerán "
                + "en el borrador hasta confirmar la propuesta.",
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
        // La respuesta visual se controla exclusivamente desde CSS.
        abrir.setOnAction(e -> {
            List<CrucePropuestoTorneo> nuevos =
                    new EstructuraManualTorneoDialog(
                            borrador, nombres).mostrar();
            if (nuevos == null) return;
            borrador.setCruces(nuevos);
            borrador.setPases(List.of());
            hayCambiosPendientes = !mismaEstructuraInicial();
            plazasModificadas.clear();
            refrescarCruces();
            refrescarVistaAvanzada();
            normalizarVistaActiva();
            actualizarEstado();
        });
        resumen.setMaxWidth(Double.MAX_VALUE);
        resumen.getStyleClass().add("advanced-summary-wide");

        aviso.setMaxWidth(Double.MAX_VALUE);
        alternativa.setMaxWidth(Double.MAX_VALUE);
        aviso.setMinHeight(74);
        aviso.setPrefHeight(74);
        alternativa.setMinHeight(74);
        alternativa.setPrefHeight(74);
        HBox.setHgrow(aviso, Priority.ALWAYS);
        HBox.setHgrow(alternativa, Priority.ALWAYS);
        aviso.getStyleClass().add("advanced-equal-notice");
        alternativa.getStyleClass().add("advanced-equal-notice");

        HBox avisos = new HBox(14, aviso, alternativa);
        avisos.setAlignment(Pos.TOP_LEFT);
        avisos.setFillHeight(true);
        avisos.setMaxWidth(Double.MAX_VALUE);
        avisos.getStyleClass().add("advanced-notices-row");

        Label tituloEditor = etiqueta("EDITOR ESTRUCTURAL",
                "advanced-editor-action-title-v2c");
        Label ayudaEditor = etiqueta(
                "Agregá, eliminá o reconectá partidos. Los cambios quedarán "
                + "en el borrador hasta confirmar la propuesta.",
                "advanced-editor-help");
        VBox textoAccion = new VBox(2, tituloEditor, ayudaEditor);
        Region espacioAccion = new Region();
        HBox.setHgrow(espacioAccion, Priority.ALWAYS);
        HBox filaAccion = new HBox(14, textoAccion, espacioAccion, abrir);
        filaAccion.setAlignment(Pos.CENTER_LEFT);
        filaAccion.setMaxWidth(Double.MAX_VALUE);
        filaAccion.getStyleClass().add("advanced-editor-action-bar-v2c");

        VBox panel = new VBox(14, resumen, avisos, filaAccion);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setFillWidth(true);
        panel.getStyleClass().add("advanced-operation-panel");

        VBox tarjeta = new VBox(8, titulo, ayuda, panel);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.setPrefWidth(Region.USE_COMPUTED_SIZE);
        tarjeta.setFillWidth(true);
        tarjeta.getStyleClass().add("advanced-structure-card");
        tarjeta.getStyleClass().add("advanced-structure-card-v2");

        StackPane centro = new StackPane(tarjeta);
        centro.setAlignment(Pos.TOP_CENTER);
        centro.setPadding(new Insets(18, 0, 10, 0));
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
        Label valorPartidos = etiqueta(String.valueOf(partidos),
                "bracket-structure-metric-number-v2c");
        Label textoPartidos = etiqueta("PARTIDOS",
                "bracket-structure-metric-label-v2c");
        VBox metricaPartidos = new VBox(0, valorPartidos, textoPartidos);
        Label divisor = etiqueta("·", "bracket-structure-metric-divider-v2c");
        Label valorFases = etiqueta(String.valueOf(fases),
                "bracket-structure-metric-number-v2c");
        Label textoFases = etiqueta("FASES",
                "bracket-structure-metric-label-v2c");
        VBox metricaFases = new VBox(0, valorFases, textoFases);
        HBox general = new HBox(12, metricaPartidos, divisor, metricaFases);
        general.setAlignment(Pos.CENTER_LEFT);
        general.getStyleClass().add("bracket-structure-summary-main");

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

    private String cargarContextoCompetencia() {
        String sql = "SELECT CONCAT(t.nombre, ' · ', c.nombre) "
                + "FROM torneo_categorias c "
                + "INNER JOIN torneos t ON t.id=c.torneo_id "
                + "WHERE c.id=?";
        try (java.sql.Connection conexion = config.ConexionBD.obtenerConexion();
                java.sql.PreparedStatement sentencia =
                        conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId());
            try (java.sql.ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    String valor = resultado.getString(1);
                    if (valor != null && !valor.isBlank()) return valor;
                }
            }
        } catch (java.sql.SQLException exception) {
            return "Editor de propuesta eliminatoria";
        }
        return "Editor de propuesta eliminatoria";
    }

    private long categoriaId() {
        return borrador.getClasificados().stream()
                .mapToLong(c -> c.inscripcionId())
                .findFirst().isPresent() ? buscarCategoriaId() : 0L;
    }

    private long buscarCategoriaId() {
        Long inscripcionId = borrador.getClasificados().stream()
                .map(c -> c.inscripcionId()).findFirst().orElse(null);
        if (inscripcionId == null) return 0L;
        String sql = "SELECT torneo_categoria_id FROM torneo_inscripciones "
                + "WHERE id=?";
        try (java.sql.Connection conexion = config.ConexionBD.obtenerConexion();
                java.sql.PreparedStatement sentencia =
                        conexion.prepareStatement(sql)) {
            sentencia.setLong(1, inscripcionId);
            try (java.sql.ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getLong(1) : 0L;
            }
        } catch (java.sql.SQLException exception) {
            return 0L;
        }
    }

    private String claveCruce(CrucePropuestoTorneo cruce) {
        return cruce.getInstancia() + "#" + cruce.getOrden();
    }

    private String clavePlaza(CrucePropuestoTorneo cruce,
            boolean primera) {
        return claveCruce(cruce) + "|" + (primera ? "1" : "2");
    }

    private void marcarPlazaDe(String referencia) {
        if (referencia == null || referencia.startsWith("Ganador ")) return;
        for (CrucePropuestoTorneo cruce : borrador.getCruces()) {
            if (referencia.equals(cruce.getParticipante1())) {
                plazasModificadas.add(clavePlaza(cruce, true));
            }
            if (referencia.equals(cruce.getParticipante2())) {
                plazasModificadas.add(clavePlaza(cruce, false));
            }
        }
    }

    private List<String> advertenciasDelCruce(
            CrucePropuestoTorneo cruce) {
        String prefijo = nombreVisibleFase(cruce.getInstancia())
                + " #" + cruce.getOrden();
        return service.advertenciasDeportivas(borrador).stream()
                .filter(aviso -> aviso.startsWith(prefijo))
                .map(aviso -> aviso.substring(prefijo.length()).trim())
                .toList();
    }

    private boolean mismaEstructuraInicial() {
        List<String> actual = borrador.getCruces().stream()
                .map(this::firmaEstructural)
                .sorted().toList();
        List<String> original = inicial.getCruces().stream()
                .map(this::firmaEstructural)
                .sorted().toList();
        return actual.equals(original);
    }

    private String firmaEstructural(CrucePropuestoTorneo cruce) {
        return cruce.getInstancia() + "|" + cruce.getRonda() + "|"
                + cruce.getOrden() + "|" + cruce.getParticipante1() + "|"
                + cruce.getParticipante2();
    }

    private boolean confirmarCambios() {
        List<String> cambios = resumenCambios();
        if (cambios.isEmpty()) {
            return Dialogos.confirmar("Aplicar cambios",
                    "No se detectaron modificaciones en los cruces. "
                            + "¿Querés cerrar el editor igualmente?");
        }
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Se aplicarán ").append(cambios.size())
                .append(cambios.size() == 1
                        ? " cambio al borrador:\n\n"
                        : " cambios al borrador:\n\n");
        for (String cambio : cambios) {
            mensaje.append("• ").append(cambio).append("\n\n");
        }
        mensaje.append("La estructura todavía no se generará. "
                + "¿Deseás confirmar estos cambios?");
        return Dialogos.confirmar("Confirmar cambios del cuadro",
                mensaje.toString().trim());
    }

    private List<String> resumenCambios() {
        Map<String, CrucePropuestoTorneo> originales = inicial.getCruces()
                .stream().collect(java.util.stream.Collectors.toMap(
                        this::claveCruce, cruce -> cruce, (a, b) -> a,
                        LinkedHashMap::new));
        List<String> cambios = new java.util.ArrayList<>();
        for (CrucePropuestoTorneo actual : borrador.getCruces()) {
            CrucePropuestoTorneo anterior = originales.get(claveCruce(actual));
            if (anterior == null) {
                cambios.add(nombreVisibleFase(actual.getInstancia())
                        + " · Partido " + actual.getOrden()
                        + ": partido agregado o reestructurado");
                continue;
            }
            if (anterior.getRonda() != actual.getRonda()) {
                cambios.add(nombreVisibleFase(actual.getInstancia())
                        + " · Partido " + actual.getOrden()
                        + ": ronda " + anterior.getRonda()
                        + " → " + actual.getRonda());
            }
            agregarCambioPlaza(cambios, actual, 1,
                    anterior.getParticipante1(), actual.getParticipante1());
            agregarCambioPlaza(cambios, actual, 2,
                    anterior.getParticipante2(), actual.getParticipante2());
        }
        for (CrucePropuestoTorneo anterior : inicial.getCruces()) {
            if (borrador.getCruces().stream()
                    .noneMatch(actual -> claveCruce(actual)
                            .equals(claveCruce(anterior)))) {
                cambios.add(nombreVisibleFase(anterior.getInstancia())
                        + " · Partido " + anterior.getOrden()
                        + ": partido eliminado o reestructurado");
            }
        }
        return cambios;
    }

    private void agregarCambioPlaza(List<String> cambios,
            CrucePropuestoTorneo cruce, int plaza,
            String anterior, String actual) {
        if (java.util.Objects.equals(anterior, actual)) return;
        cambios.add(nombreVisibleFase(cruce.getInstancia())
                + " · Partido " + cruce.getOrden()
                + " · Plaza " + plaza + ":\n    "
                + texto(anterior) + "\n    → " + texto(actual));
    }

    private void normalizarVistaActiva() {
        boolean avanzadaActiva = vistaAvanzada != null
                && vistaAvanzada.isVisible();
        if (vistaCruces != null) {
            vistaCruces.setVisible(!avanzadaActiva);
            vistaCruces.setManaged(!avanzadaActiva);
        }
        if (vistaAvanzada != null) {
            vistaAvanzada.setVisible(avanzadaActiva);
            vistaAvanzada.setManaged(avanzadaActiva);
        }
    }

    private void refrescarVistaAvanzada() {
        VBox nueva = crearVistaAvanzada();
        int indice = vistas.getChildren().indexOf(vistaAvanzada);
        boolean visible = vistaAvanzada != null && vistaAvanzada.isVisible();
        boolean managed = vistaAvanzada != null && vistaAvanzada.isManaged();
        if (indice >= 0) {
            vistas.getChildren().set(indice, nueva);
        }
        nueva.setVisible(visible);
        nueva.setManaged(managed);
        vistaAvanzada = nueva;
    }

    private void refrescarCruces() {
        double posicionVertical = scrollCrucesSeguro == null
                ? 0.0 : scrollCrucesSeguro.getVvalue();
        double posicionHorizontal = scrollCrucesSeguro == null
                ? 0.0 : scrollCrucesSeguro.getHvalue();
        boolean visible = vistaCruces != null && vistaCruces.isVisible();
        boolean managed = vistaCruces != null && vistaCruces.isManaged();
        VBox nueva = crearVistaCruces();
        ScrollPane nuevoScroll = scrollCrucesSeguro;
        int indice = vistas.getChildren().indexOf(vistaCruces);
        if (indice >= 0) vistas.getChildren().set(indice, nueva);
        nueva.setVisible(visible);
        nueva.setManaged(managed);
        vistaCruces = nueva;
        actualizarEstado();
        javafx.application.Platform.runLater(() -> {
            if (nuevoScroll == null) return;
            nuevoScroll.applyCss();
            nuevoScroll.layout();
            nuevoScroll.setVvalue(posicionVertical);
            nuevoScroll.setHvalue(posicionHorizontal);
        });
    }

    private void actualizarEstado() {
        List<String> errores = service.validarEdicionManual(borrador);
        boolean valido = errores.isEmpty();
        estado.setText(valido
                ? (hayCambiosPendientes ? "CAMBIOS PENDIENTES   ·   " : "")
                        + "BORRADOR VÁLIDO   ·   "
                        + borrador.getCruces().size() + " PARTIDOS"
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
