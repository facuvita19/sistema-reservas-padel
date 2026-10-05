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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
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
    private final StackPane contenedorVistas = new StackPane();
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
        dialogo.initStyle(javafx.stage.StageStyle.DECORATED);
        dialogo.setHeaderText("Editor estructural del cuadro");
        ButtonType aplicarTipo = new ButtonType("APLICAR ESTRUCTURA",
                ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().setAll(aplicarTipo,
                new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        aplicar = (Button) dialogo.getDialogPane().lookupButton(aplicarTipo);
        aplicar.getStyleClass().add("structural-editor-apply");
        Button cancelar = (Button) dialogo.getDialogPane().lookupButton(
                dialogo.getDialogPane().getButtonTypes().get(1));
        cancelar.getStyleClass().add("structural-editor-cancel");

        // cabecera-modo-reutilizada-v1
        Label modo = new Label("MODO AVANZADO");
        modo.setWrapText(true);
        modo.setMaxWidth(Double.MAX_VALUE);
        modo.getStyleClass().add("bracket-editor-mode-badge");
        Tooltip ayudaModo = new Tooltip(
                "Crea, elimina y conecta partidos para construir "
                + "una llave personalizada.\n\n"
                + "Los cambios solo se guardan al aplicar. "
                + "Cancelar conserva la propuesta anterior.");
        ayudaModo.setWrapText(true);
        ayudaModo.setMaxWidth(430);
        modo.setTooltip(ayudaModo);
        VBox contextoModo = new VBox(modo);
        contextoModo.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        contextoModo.getStyleClass().add("advanced");
        Button agregar = new Button("+  AGREGAR PARTIDO");
        agregar.getStyleClass().addAll("structural-editor-button",
                "structural-editor-button-primary");
        agregar.setOnAction(e -> agregarFila(null));

        for (CrucePropuestoTorneo cruce : propuesta.getCruces()) {
            agregarFila(cruce);
        }

        ScrollPane scroll = new ScrollPane(filas);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(520);
        scroll.getStyleClass().add("structural-editor-detail-scroll");

        javafx.scene.Node grafica = crearVistaGrafica();
        contenedorVistas.getChildren().setAll(grafica, scroll);
        grafica.setVisible(true);
        grafica.setManaged(true);
        scroll.setVisible(false);
        scroll.setManaged(false);

        HBox barraAcciones = new HBox(agregar);
        barraAcciones.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        barraAcciones.getStyleClass().add("structural-editor-context-actions");

        ToggleButton botonLlave = new ToggleButton("VISTA DE LLAVE");
        ToggleButton botonDetalle = new ToggleButton("VISTA DETALLADA");
        ToggleGroup vistas = new ToggleGroup();
        botonLlave.setToggleGroup(vistas);
        botonDetalle.setToggleGroup(vistas);
        botonLlave.setSelected(true);
        estiloSelectorVista(botonLlave);
        estiloSelectorVista(botonDetalle);
        vistas.selectedToggleProperty().addListener((obs, anterior, actual) -> {
            if (actual == null) {
                botonLlave.setSelected(true);
                return;
            }
            boolean llave = actual == botonLlave;
            javafx.scene.Node vistaGraficaActual = contenedorVistas.getChildren().isEmpty()
                    ? null : contenedorVistas.getChildren().get(0);
            if (vistaGraficaActual != null) {
                vistaGraficaActual.setVisible(llave);
                vistaGraficaActual.setManaged(llave);
            }
            scroll.setVisible(!llave);
            scroll.setManaged(!llave);
            barraAcciones.setVisible(!llave);
            barraAcciones.setManaged(!llave);
        });
        javafx.scene.layout.Region separadorNavegacion =
                new javafx.scene.layout.Region();
        HBox.setHgrow(separadorNavegacion, Priority.ALWAYS);
        HBox selectorVista = new HBox(8, botonLlave, botonDetalle,
                separadorNavegacion, barraAcciones, contextoModo);
        selectorVista.getStyleClass().addAll(
                "structural-editor-tabs",
                "bracket-editor-navigation");
        selectorVista.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        barraAcciones.setVisible(false);
        barraAcciones.setManaged(false);
        VBox contenido = new VBox(8, selectorVista,
                contenedorVistas, estado);
        VBox.setVgrow(contenedorVistas, Priority.ALWAYS);
        contenido.setPadding(new Insets(14, 18, 14, 18));
        contenido.getStyleClass().add("structural-editor-root");
        contenido.setPrefWidth(1040);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(1380, 780);
        dialogo.getDialogPane().setMinSize(900, 620);
        dialogo.setOnShown(evento -> {
            javafx.stage.Window ventana = dialogo.getDialogPane().getScene()
                    .getWindow();
            javafx.geometry.Rectangle2D area = javafx.stage.Screen
                    .getPrimary().getVisualBounds();
            double ancho = Math.min(1680, area.getWidth() * 0.92);
            double alto = Math.min(920, area.getHeight() * 0.88);
            ventana.setWidth(ancho);
            ventana.setHeight(alto);
            ventana.setX(area.getMinX() + (area.getWidth() - ancho) / 2);
            ventana.setY(area.getMinY() + (area.getHeight() - alto) / 2);
        });
        dialogo.setResizable(true);
        Dialogos.preparar(dialogo, "dialog-tournament-bracket-manual");
        dialogo.getDialogPane().getStyleClass().add("structural-editor-dialog");
        estado.getStyleClass().add("structural-editor-status");
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

    private javafx.scene.Node crearVistaGrafica() {
        PropuestaEtapaEliminatoria temporal = new PropuestaEtapaEliminatoria();
        temporal.setClasificados(propuesta.getClasificados());
        temporal.setCruces(construirResultado());
        return new LlaveGraficaTorneoView(temporal, nombres,
                this::actualizarDesdeLlave,
                this::intercambiarDesdeLlave,
                this::agregarDesdeLlave,
                this::nuevoCuadroDesdeLlave,
                this::eliminarDesdeLlave).crear();
    }

    private void nuevoCuadroDesdeLlave() {
        if (ediciones.isEmpty()) {
            Dialogos.informacion("Cuadro vacio",
                    "La estructura ya esta vacia. Usa AGREGAR PARTIDO "
                    + "para comenzar a construirla.");
            return;
        }
        boolean confirmar = Dialogos.confirmar(
                "Crear un cuadro nuevo",
                "Se quitaran los " + ediciones.size()
                + " partidos de esta edicion y comenzaras con una llave "
                + "vacia. Los clasificados seguiran disponibles.\n\n"
                + "El cambio no sera definitivo hasta pulsar APLICAR "
                + "ESTRUCTURA. Si pulsas CANCELAR, se recuperara la "
                + "propuesta aplicada anteriormente.\n\n"
                + "¿Queres continuar?");
        if (!confirmar) return;

        ediciones.clear();
        filas.getChildren().clear();
        actualizarOpciones();
        refrescarVistaGrafica();
    }

    private void agregarDesdeLlave() {
        List<String> fasesDisponibles = INSTANCIAS.stream()
                .filter(this::faseConCapacidad)
                .toList();
        if (fasesDisponibles.isEmpty()) {
            Dialogos.informacion("Llave completa",
                    "Todas las fases eliminatorias alcanzaron su cantidad "
                    + "maxima de partidos. Para modificar la estructura, "
                    + "edita o elimina un partido existente.");
            return;
        }

        Dialog<ConfiguracionNuevoPartido> dialogoNuevo = new Dialog<>();
        dialogoNuevo.setTitle("Agregar partido");
        dialogoNuevo.setHeaderText("Agregar un partido a la llave");
        ButtonType agregarTipo = new ButtonType("AGREGAR PARTIDO",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelarTipo = new ButtonType("CANCELAR",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogoNuevo.getDialogPane().getButtonTypes().setAll(
                agregarTipo, cancelarTipo);

        List<String> fasesPrincipales = fasesDisponibles.stream()
                .filter(f -> !f.startsWith("Acceso R"))
                .toList();
        List<String> fasesIniciales = fasesPrincipales.isEmpty()
                ? fasesDisponibles : fasesPrincipales;
        ComboBox<String> fase = new ComboBox<>(
                FXCollections.observableArrayList(fasesIniciales));
        fase.setValue(fasesIniciales.contains(faseSugerida())
                ? faseSugerida() : fasesIniciales.get(0));
        fase.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(String valor) {
                return valor == null ? "" : textoCapacidadFase(valor);
            }
            @Override
            public String fromString(String texto) {
                return texto;
            }
        });
        estiloCombo(fase, 370);
        javafx.scene.control.CheckBox incluirPrevias =
                new javafx.scene.control.CheckBox("INCLUIR FASES PREVIAS");
        incluirPrevias.setFocusTraversable(false);
        incluirPrevias.getStyleClass().add("structural-add-previous");
        incluirPrevias.selectedProperty().addListener((o, anterior, incluir) -> {
            String seleccionActual = fase.getValue();
            List<String> opciones = incluir
                    ? fasesDisponibles : fasesPrincipales;
            if (opciones.isEmpty()) opciones = fasesDisponibles;
            fase.setItems(FXCollections.observableArrayList(opciones));
            if (seleccionActual != null
                    && opciones.contains(seleccionActual)) {
                fase.setValue(seleccionActual);
            } else {
                fase.setValue(opciones.get(0));
            }
        });
        fase.setCellFactory(lista -> celdaAgregarPartido(
                valor -> textoCapacidadFase(valor), false));
        fase.setButtonCell(celdaAgregarPartido(
                valor -> textoCapacidadFase(valor), true));

        ComboBox<Integer> rondaNueva = new ComboBox<>();
        rondaNueva.setItems(FXCollections.observableArrayList(
                java.util.stream.IntStream.rangeClosed(1, 10)
                        .boxed().toList()));
        estiloComboNumerico(rondaNueva, 130);
        rondaNueva.setCellFactory(lista -> celdaAgregarPartido(
                valor -> String.valueOf(valor), false));
        rondaNueva.setButtonCell(celdaAgregarPartido(
                valor -> String.valueOf(valor), true));

        ComboBox<Integer> ordenNuevo = new ComboBox<>();
        ordenNuevo.setItems(FXCollections.observableArrayList(
                java.util.stream.IntStream.rangeClosed(1, 32)
                        .boxed().toList()));
        estiloComboNumerico(ordenNuevo, 130);
        ordenNuevo.setCellFactory(lista -> celdaAgregarPartido(
                valor -> String.valueOf(valor), false));
        ordenNuevo.setButtonCell(celdaAgregarPartido(
                valor -> String.valueOf(valor), true));

        Label capacidad = etiqueta("", "#bfa9d3", 12, true);
        capacidad.getStyleClass().add("structural-add-capacity");
        Runnable actualizarSugerencias = () -> {
            String seleccionada = fase.getValue();
            if (seleccionada == null || seleccionada.isBlank()) return;
            rondaNueva.setValue(rondaDeFase(seleccionada));
            ordenNuevo.setValue(siguienteOrden(seleccionada));
            capacidad.setText(descripcionCapacidad(seleccionada));
        };
        fase.valueProperty().addListener((o, anterior, actual) ->
                actualizarSugerencias.run());
        actualizarSugerencias.run();

        Label insigniaAgregar = new Label("NUEVO PARTIDO");
        insigniaAgregar.getStyleClass().add("structural-add-badge");
        Label ayuda = etiqueta(
                "Selecciona una fase con lugares disponibles. La ronda y el "
                + "numero se sugieren automaticamente.",
                "#c2c9cc", 12, true);
        ayuda.getStyleClass().add("structural-add-help");
        GridPane campos = new GridPane();
        campos.setHgap(16);
        campos.setVgap(10);
        campos.getStyleClass().add("structural-add-fields");
        javafx.scene.layout.ColumnConstraints etiquetasColumna =
                new javafx.scene.layout.ColumnConstraints();
        etiquetasColumna.setMinWidth(145);
        etiquetasColumna.setPrefWidth(145);
        javafx.scene.layout.ColumnConstraints controlesColumna =
                new javafx.scene.layout.ColumnConstraints();
        controlesColumna.setHgrow(Priority.ALWAYS);
        campos.getColumnConstraints().setAll(
                etiquetasColumna, controlesColumna);
        campos.add(etiqueta("FASE", "#91b3c3", 11, false), 0, 0);
        campos.add(fase, 1, 0);
        campos.add(incluirPrevias, 1, 1);
        campos.add(etiqueta("RONDA", "#91b3c3", 11, false), 0, 2);
        campos.add(rondaNueva, 1, 2);
        campos.add(etiqueta("NUMERO DE PARTIDO", "#91b3c3", 11, false),
                0, 3);
        campos.add(ordenNuevo, 1, 3);

        Label aviso = etiqueta(
                "Las dos plazas se agregaran sin asignar y deberan "
                + "completarse antes de aplicar la estructura.",
                "#d8b36d", 11, true);
        aviso.getStyleClass().add("structural-add-note");
        VBox contexto = new VBox(6, insigniaAgregar, ayuda, capacidad);
        contexto.getStyleClass().add("structural-add-context");
        fase.setStyle("");
        rondaNueva.setStyle("");
        ordenNuevo.setStyle("");
        fase.getStyleClass().addAll("structural-add-combo",
                "structural-add-phase");
        rondaNueva.getStyleClass().addAll("structural-add-combo",
                "structural-add-number");
        ordenNuevo.getStyleClass().addAll("structural-add-combo",
                "structural-add-number");
        VBox contenidoNuevo = new VBox(12, contexto, campos, aviso);
        contenidoNuevo.getStyleClass().add("structural-add-root");
        dialogoNuevo.getDialogPane().setContent(contenidoNuevo);
        dialogoNuevo.getDialogPane().setPrefSize(760, 560);
        dialogoNuevo.getDialogPane().setMinSize(700, 530);
        Dialogos.preparar(dialogoNuevo,
                "dialog-tournament-bracket-manual");
        dialogoNuevo.getDialogPane().getStyleClass().add(
                "structural-add-dialog");
        Button botonAgregar = (Button) dialogoNuevo.getDialogPane()
                .lookupButton(agregarTipo);
        Button botonCancelar = (Button) dialogoNuevo.getDialogPane()
                .lookupButton(cancelarTipo);
        botonAgregar.getStyleClass().add("structural-add-confirm");
        botonCancelar.getStyleClass().add("structural-add-cancel");
        vista.TemaDinamico.aplicar(dialogoNuevo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        dialogoNuevo.setResultConverter(tipo -> tipo == agregarTipo
                ? new ConfiguracionNuevoPartido(fase.getValue(),
                        rondaNueva.getValue(), ordenNuevo.getValue())
                : null);

        ConfiguracionNuevoPartido configuracion = dialogoNuevo.showAndWait()
                .orElse(null);
        if (configuracion == null) return;
        if (!faseConCapacidad(configuracion.fase())) {
            Dialogos.error("Fase completa",
                    mensajeFaseCompleta(configuracion.fase()));
            return;
        }
        boolean repetido = ediciones.stream().anyMatch(f ->
                configuracion.fase().equals(f.instancia.getValue())
                && configuracion.orden().equals(f.orden.getValue()));
        if (repetido) {
            Dialogos.error("Partido repetido",
                    configuracion.fase() + " (Partido "
                    + configuracion.orden()
                    + ") ya existe dentro de la llave.");
            return;
        }

        if (configuracion.fase().startsWith("Acceso ")) {
            desplazarRondasParaFasePrevia(configuracion.fase(),
                    configuracion.ronda());
        }
        Fila nueva = agregarFila(null);
        nueva.instancia.setValue(configuracion.fase());
        nueva.ronda.setValue(configuracion.ronda());
        nueva.orden.setValue(configuracion.orden());
        nueva.p1.setValue(null);
        nueva.p2.setValue(null);
        normalizarRondasPorOrdenDeportivo();
        actualizarOpciones();
        refrescarVistaGrafica();
    }

    private void eliminarDesdeLlave(String clave) {
        Fila encontrada = ediciones.stream()
                .filter(f -> f.clave().equals(clave))
                .findFirst().orElse(null);
        if (encontrada == null) return;
        ediciones.remove(encontrada);
        filas.getChildren().remove(encontrada.raiz);
        normalizarRondasPorOrdenDeportivo();
        actualizarOpciones();
        refrescarVistaGrafica();
    }

    private String faseSugerida() {
        return INSTANCIAS.stream()
                .filter(this::faseConCapacidad)
                .filter(f -> !f.startsWith("Acceso"))
                .findFirst().orElseGet(() ->
                        INSTANCIAS.stream()
                                .filter(this::faseConCapacidad)
                                .findFirst().orElse("Cuartos"));
    }

    private boolean faseConCapacidad(String fase) {
        Integer maximo = maximoPartidosFase(fase);
        return maximo == null || contarPartidosFase(fase) < maximo;
    }

    private int contarPartidosFase(String fase) {
        return (int) ediciones.stream()
                .filter(f -> fase.equals(f.instancia.getValue())).count();
    }

    private Integer maximoPartidosFase(String fase) {
        return switch (fase) {
            case "Final" -> 1;
            case "Semifinal" -> 2;
            case "Cuartos" -> 4;
            case "Octavos" -> 8;
            case "Dieciseisavos" -> 16;
            default -> null;
        };
    }

    private String textoCapacidadFase(String fase) {
        Integer maximo = maximoPartidosFase(fase);
        if (maximo == null) return nombreVisibleFase(fase)
                + " · capacidad variable";
        return nombreVisibleFase(fase) + " · " + contarPartidosFase(fase)
                + " de " + maximo + " partidos";
    }

    private String descripcionCapacidad(String fase) {
        if (fase == null || fase.isBlank()) {
            return "Selecciona una fase para ver su capacidad.";
        }
        Integer maximo = maximoPartidosFase(fase);
        if (maximo == null) {
            return "Esta fase de acceso tiene capacidad variable segun "
                    + "el tamaño del cuadro.";
        }
        int actuales = contarPartidosFase(fase);
        int disponibles = Math.max(0, maximo - actuales);
        return "Capacidad de " + fase + ": " + actuales + " de "
                + maximo + " partidos. Lugares disponibles: "
                + disponibles + ".";
    }

    private String mensajeFaseCompleta(String fase) {
        Integer maximo = maximoPartidosFase(fase);
        return "La llave ya contiene los " + maximo
                + " partidos permitidos para " + fase
                + ". Para modificar esta fase, edita o elimina uno de "
                + "los partidos existentes.";
    }

    private int rondaDeFase(String instancia) {
        if (instancia == null || instancia.isBlank()) return 1;
        return ediciones.stream()
                .filter(f -> instancia.equals(f.instancia.getValue()))
                .mapToInt(f -> f.ronda.getValue()).findFirst()
                .orElseGet(() -> rondaNuevaParaFase(instancia));
    }

    private int rondaNuevaParaFase(String instancia) {
        if (instancia.startsWith("Acceso ")) {
            int numero = numeroFasePrevia(instancia);
            int rondasPreviasAnteriores = ediciones.stream()
                    .filter(f -> f.instancia.getValue().startsWith("Acceso "))
                    .filter(f -> numeroFasePrevia(
                            f.instancia.getValue()) < numero)
                    .mapToInt(f -> f.ronda.getValue()).max().orElse(0);
            return rondasPreviasAnteriores + 1;
        }
        java.util.Map<String, Integer> ordenFases = java.util.Map.of(
                "Dieciseisavos", 6, "Octavos", 7, "Cuartos", 8,
                "Semifinal", 9, "Final", 10);
        int objetivo = ordenFases.getOrDefault(instancia, 8);
        int anterior = ediciones.stream()
                .filter(f -> ordenFases.getOrDefault(
                        f.instancia.getValue(), 0) < objetivo)
                .mapToInt(f -> f.ronda.getValue()).max().orElse(0);
        return Math.max(1, Math.min(10, anterior + 1));
    }

    private void desplazarRondasParaFasePrevia(String fase, int ronda) {
        boolean yaExiste = ediciones.stream()
                .anyMatch(f -> fase.equals(f.instancia.getValue()));
        if (yaExiste) return;
        ediciones.stream()
                .filter(f -> f.ronda.getValue() >= ronda)
                .forEach(f -> f.ronda.setValue(f.ronda.getValue() + 1));
    }

    private int numeroFasePrevia(String fase) {
        if (fase == null || !fase.startsWith("Acceso R")) return 0;
        try {
            return Integer.parseInt(fase.substring("Acceso R".length()));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String nombreVisibleFase(String fase) {
        int numero = numeroFasePrevia(fase);
        return numero > 0 ? "Fase previa " + numero : fase;
    }

    private void normalizarRondasPorOrdenDeportivo() {
        java.util.List<String> fasesPresentes = ediciones.stream()
                .map(f -> f.instancia.getValue())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted(java.util.Comparator
                        .comparingInt(this::ordenDeportivoFase))
                .toList();

        java.util.Map<String, Integer> rondaPorFase =
                new java.util.LinkedHashMap<>();
        for (int indice = 0; indice < fasesPresentes.size(); indice++) {
            rondaPorFase.put(fasesPresentes.get(indice), indice + 1);
        }
        ediciones.forEach(fila -> {
            Integer ronda = rondaPorFase.get(fila.instancia.getValue());
            if (ronda != null) fila.ronda.setValue(ronda);
        });
    }

    private int ordenDeportivoFase(String fase) {
        if (fase == null) return Integer.MAX_VALUE;
        if (fase.startsWith("Acceso R")) {
            return numeroFasePrevia(fase);
        }
        return switch (fase) {
            case "Dieciseisavos" -> 100;
            case "Octavos" -> 200;
            case "Cuartos" -> 300;
            case "Semifinal" -> 400;
            case "Final" -> 500;
            default -> 900;
        };
    }

    private int siguienteOrden(String instancia) {
        if (instancia == null || instancia.isBlank()) return 1;
        return ediciones.stream()
                .filter(f -> instancia.equals(f.instancia.getValue()))
                .mapToInt(f -> f.orden.getValue()).max().orElse(0) + 1;
    }

    private void intercambiarDesdeLlave(String clave) {
        Fila encontrada = ediciones.stream()
                .filter(f -> f.clave().equals(clave))
                .findFirst().orElse(null);
        if (encontrada == null) return;
        String primero = encontrada.p1.getValue();
        encontrada.p1.setValue(encontrada.p2.getValue());
        encontrada.p2.setValue(primero);
    }

    private void refrescarVistaGrafica() {
        javafx.scene.Node anterior = contenedorVistas.getChildren().isEmpty()
                ? null : contenedorVistas.getChildren().get(0);
        ScrollPane scrollAnterior = buscarScrollGrafico(anterior);
        double horizontal = scrollAnterior == null
                ? 0.0 : scrollAnterior.getHvalue();
        double vertical = scrollAnterior == null
                ? 0.0 : scrollAnterior.getVvalue();

        javafx.stage.Window ventana = dialogo.getDialogPane().getScene()
                == null ? null
                : dialogo.getDialogPane().getScene().getWindow();
        double anchoVentana = ventana == null ? 0 : ventana.getWidth();
        double altoVentana = ventana == null ? 0 : ventana.getHeight();
        double posicionX = ventana == null ? 0 : ventana.getX();
        double posicionY = ventana == null ? 0 : ventana.getY();
        javafx.stage.Stage escenario = ventana instanceof javafx.stage.Stage
                ? (javafx.stage.Stage) ventana : null;
        if (escenario != null && anchoVentana > 0 && altoVentana > 0) {
            escenario.setMinWidth(anchoVentana);
            escenario.setMaxWidth(anchoVentana);
            escenario.setMinHeight(altoVentana);
            escenario.setMaxHeight(altoVentana);
        }

        contenedorVistas.setMinSize(
                contenedorVistas.getWidth(), contenedorVistas.getHeight());
        javafx.scene.Node nueva = crearVistaGrafica();
        nueva.setOpacity(0);
        if (contenedorVistas.getChildren().isEmpty()) {
            contenedorVistas.getChildren().add(0, nueva);
        } else {
            contenedorVistas.getChildren().set(0, nueva);
        }
        restaurarVentana(ventana, anchoVentana, altoVentana,
                posicionX, posicionY);

        javafx.application.Platform.runLater(() -> {
            restaurarVentana(ventana, anchoVentana, altoVentana,
                    posicionX, posicionY);
            ScrollPane nuevoScroll = buscarScrollGrafico(nueva);
            if (nuevoScroll != null) {
                nuevoScroll.setHvalue(horizontal);
                nuevoScroll.setVvalue(vertical);
            }
            javafx.application.Platform.runLater(() -> {
                restaurarVentana(ventana, anchoVentana, altoVentana,
                        posicionX, posicionY);
                if (nuevoScroll != null) {
                    nuevoScroll.setHvalue(horizontal);
                    nuevoScroll.setVvalue(vertical);
                }
                contenedorVistas.setMinSize(
                        javafx.scene.layout.Region.USE_COMPUTED_SIZE,
                        javafx.scene.layout.Region.USE_COMPUTED_SIZE);
                nueva.setOpacity(1);
                javafx.application.Platform.runLater(() -> {
                    if (escenario != null) {
                        escenario.setMinWidth(0);
                        escenario.setMinHeight(0);
                        escenario.setMaxWidth(Double.MAX_VALUE);
                        escenario.setMaxHeight(Double.MAX_VALUE);
                        restaurarVentana(escenario, anchoVentana,
                                altoVentana, posicionX, posicionY);
                    }
                });
            });
        });
    }

    private void restaurarVentana(javafx.stage.Window ventana,
            double ancho, double alto, double x, double y) {
        if (ventana == null || ancho <= 0 || alto <= 0) return;
        ventana.setWidth(ancho);
        ventana.setHeight(alto);
        ventana.setX(x);
        ventana.setY(y);
    }

    private ScrollPane buscarScrollGrafico(javafx.scene.Node raiz) {
        if (raiz == null) return null;
        if (raiz instanceof ScrollPane scroll && scroll.isPannable()) {
            return scroll;
        }
        if (raiz instanceof javafx.scene.Parent padre) {
            for (javafx.scene.Node hijo : padre.getChildrenUnmodifiable()) {
                ScrollPane encontrado = buscarScrollGrafico(hijo);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }

    private void actualizarDesdeLlave(String clave,
            LlaveGraficaTorneoView.CambioPlaza cambio) {
        Fila encontrada = ediciones.stream()
                .filter(f -> f.clave().equals(clave))
                .findFirst().orElse(null);
        if (encontrada == null) return;
        if (cambio.posicion() == 1) {
            encontrada.p1.setValue(cambio.referencia());
        } else {
            encontrada.p2.setValue(cambio.referencia());
        }
    }

    private Fila agregarFila(CrucePropuestoTorneo cruce) {
        Fila fila = new Fila(cruce);
        ediciones.add(fila);
        filas.getChildren().add(fila.raiz);
        actualizarOpciones();
        return fila;
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
        actualizarEstadoGeneral();
    }

    private void actualizarEstadoGeneral() {
        List<String> errores = validar();
        estado.getStyleClass().removeAll(
                "structural-status-valid",
                "structural-status-warning",
                "structural-status-invalid");

        String resumen = ediciones.size() + " PARTIDOS  ·  "
                + propuesta.getClasificados().size() + " CLASIFICADOS";
        if (errores.isEmpty()) {
            estado.setText("ESTRUCTURA VALIDA  ·  " + resumen
                    + "  ·  SIN PROBLEMAS");
            estado.getStyleClass().add("structural-status-valid");
            return;
        }

        boolean invalida = errores.stream().anyMatch(error -> {
            String valor = error == null ? "" : error.toUpperCase();
            return valor.contains("PARTIDO REPETIDO")
                    || valor.contains("ORIGEN INEXISTENTE")
                    || valor.contains("SE REFERENCIA A SI MISMO")
                    || valor.contains("EXACTAMENTE UNA FINAL")
                    || valor.contains("RONDA")
                    || valor.contains("CICLO");
        });
        estado.setText((invalida ? "ESTRUCTURA INVALIDA"
                : "REQUIERE ATENCION") + "  ·  " + resumen
                + "  ·  " + errores.size()
                + (errores.size() == 1 ? " PROBLEMA" : " PROBLEMAS"));
        estado.getStyleClass().add(invalida
                ? "structural-status-invalid"
                : "structural-status-warning");
        estado.setTooltip(new javafx.scene.control.Tooltip(
                String.join("\n", errores)));
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
            for (String origen : new String[] {
                    fila.p1.getValue(), fila.p2.getValue() }) {
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

    private static void estiloSelectorVista(ToggleButton boton) {
        boton.getStyleClass().add("structural-editor-tab");
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

    private <T> javafx.scene.control.ListCell<T> celdaSelectorAnimada(
            java.util.function.Function<T, String> textoVisible) {
        return new javafx.scene.control.ListCell<>() {
            private final String normal = "-fx-background-color:#102532;"
                    + "-fx-text-fill:#f4fbff;-fx-padding:9 11;"
                    + "-fx-cursor:hand;";
            private final String sobre = "-fx-background-color:#214b60;"
                    + "-fx-text-fill:#ffffff;-fx-padding:9 11;"
                    + "-fx-font-weight:800;-fx-cursor:hand;";
            private final String seleccionado =
                    "-fx-background-color:#376f8e;"
                    + "-fx-text-fill:#ffffff;-fx-padding:9 11;"
                    + "-fx-font-weight:800;-fx-cursor:hand;";

            {
                setOnMouseEntered(evento -> actualizarEstilo(true));
                setOnMouseExited(evento -> actualizarEstilo(false));
            }

            @Override
            protected void updateItem(T valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null
                        ? null : textoVisible.apply(valor));
                setMouseTransparent(vacia);
                actualizarEstilo(false);
            }

            private void actualizarEstilo(boolean punteroEncima) {
                if (isEmpty()) {
                    setStyle("-fx-background-color:#102532;");
                } else if (isSelected()) {
                    setStyle(seleccionado);
                } else {
                    setStyle(punteroEncima ? sobre : normal);
                }
            }
        };
    }

    private <T> javafx.scene.control.ListCell<T> celdaBotonSelector(
            java.util.function.Function<T, String> textoVisible) {
        return new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(T valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null
                        ? null : textoVisible.apply(valor));
                setStyle("-fx-background-color:#0a1922;"
                        + "-fx-text-fill:#f4fbff;-fx-padding:6 9;"
                        + "-fx-cursor:hand;");
            }
        };
    }

    private <T> javafx.scene.control.ListCell<T> celdaAgregarPartido(
            java.util.function.Function<T, String> textoVisible,
            boolean celdaBoton) {
        return new javafx.scene.control.ListCell<>() {
            {
                getStyleClass().add(celdaBoton
                        ? "structural-add-button-cell"
                        : "structural-add-popup-cell");
            }

            @Override
            protected void updateItem(T valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null
                        ? null : textoVisible.apply(valor));
                setGraphic(null);
            }
        };
    }

    private record ConfiguracionNuevoPartido(
            String fase, Integer ronda, Integer orden) {
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
            instancia.setStyle("");
            ronda.setStyle("");
            orden.setStyle("");
            instancia.getStyleClass().addAll(
                    "structural-detail-combo", "structural-detail-phase");
            ronda.getStyleClass().addAll(
                    "structural-detail-combo", "structural-detail-number");
            orden.getStyleClass().addAll(
                    "structural-detail-combo", "structural-detail-number");
            configurarComboDetalle(instancia, valor -> valor);
            configurarComboDetalle(ronda, valor -> String.valueOf(valor));
            configurarComboDetalle(orden, valor -> String.valueOf(valor));

            Button eliminar = new Button("ELIMINAR");
            eliminar.getStyleClass().add("structural-detail-delete");
            eliminar.setOnAction(e -> quitar(this));

            Label tituloPartido = new Label();
            tituloPartido.getStyleClass().add("structural-detail-card-title");
            javafx.scene.layout.Region espacioTitulo =
                    new javafx.scene.layout.Region();
            HBox.setHgrow(espacioTitulo, Priority.ALWAYS);
            HBox cabeceraTarjeta = new HBox(10, tituloPartido,
                    espacioTitulo, eliminar);
            cabeceraTarjeta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            cabeceraTarjeta.getStyleClass().add(
                    "structural-detail-card-header");

            Label faseLabel = etiqueta("FASE", "#aeb8bc", 10, false);
            Label rondaLabel = etiqueta("RONDA", "#aeb8bc", 10, false);
            Label ordenLabel = etiqueta("ORDEN", "#aeb8bc", 10, false);
            faseLabel.getStyleClass().add("structural-detail-field-label");
            rondaLabel.getStyleClass().add("structural-detail-field-label");
            ordenLabel.getStyleClass().add("structural-detail-field-label");
            HBox controles = new HBox(10, faseLabel, instancia,
                    rondaLabel, ronda, ordenLabel, orden);
            controles.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            controles.getStyleClass().add("structural-detail-controls");
            GridPane plazas = new GridPane();
            plazas.setHgap(12);
            plazas.setVgap(7);
            plazas.getStyleClass().add("structural-detail-origins");
            Label origen1Label = etiqueta("ORIGEN 1", "#aeb8bc", 10, false);
            Label origen2Label = etiqueta("ORIGEN 2", "#aeb8bc", 10, false);
            origen1Label.getStyleClass().add("structural-detail-origin-label");
            origen2Label.getStyleClass().add("structural-detail-origin-label");
            plazas.add(origen1Label, 0, 0);
            plazas.add(p1, 1, 0);
            plazas.add(origen2Label, 0, 1);
            plazas.add(p2, 1, 1);
            estiloCombo(p1, 760);
            estiloCombo(p2, 760);
            p1.setStyle("");
            p2.setStyle("");
            p1.getStyleClass().addAll(
                    "structural-detail-combo", "structural-detail-origin");
            p2.getStyleClass().addAll(
                    "structural-detail-combo", "structural-detail-origin");
            p1.setMaxWidth(Double.MAX_VALUE);
            p2.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(p1, Priority.ALWAYS);
            GridPane.setHgrow(p2, Priority.ALWAYS);
            javafx.scene.layout.ColumnConstraints etiquetaOrigen =
                    new javafx.scene.layout.ColumnConstraints();
            etiquetaOrigen.setMinWidth(72);
            etiquetaOrigen.setPrefWidth(72);
            javafx.scene.layout.ColumnConstraints controlOrigen =
                    new javafx.scene.layout.ColumnConstraints();
            controlOrigen.setHgrow(Priority.ALWAYS);
            plazas.getColumnConstraints().setAll(
                    etiquetaOrigen, controlOrigen);
            raiz.getChildren().addAll(cabeceraTarjeta, controles, plazas);
            raiz.getStyleClass().add("structural-detail-card");
            Runnable actualizarTitulo = () -> tituloPartido.setText(
                    nombrePartidoDetalle(instancia.getValue(),
                            orden.getValue()));
            actualizarTitulo.run();
            if (cruce != null) {
                p1.setValue(cruce.getParticipante1());
                p2.setValue(cruce.getParticipante2());
            }
            instancia.valueProperty().addListener((o, a, b) -> {
                actualizarTitulo.run();
                EstructuraManualTorneoDialog.this.actualizarOpciones();
            });
            ronda.valueProperty().addListener((o, a, b) ->
                    EstructuraManualTorneoDialog.this.actualizarOpciones());
            orden.valueProperty().addListener((o, a, b) -> {
                actualizarTitulo.run();
                EstructuraManualTorneoDialog.this.actualizarOpciones();
            });
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
            configurarComboDetalle(combo, valor -> mostrar(valor));
        }

        private <T> void configurarComboDetalle(ComboBox<T> combo,
                java.util.function.Function<T, String> textoVisible) {
            combo.setCellFactory(v -> celdaComboDetalle(
                    textoVisible, false));
            combo.setButtonCell(celdaComboDetalle(textoVisible, true));
        }

        private <T> javafx.scene.control.ListCell<T> celdaComboDetalle(
                java.util.function.Function<T, String> textoVisible,
                boolean boton) {
            return new javafx.scene.control.ListCell<>() {
                {
                    getStyleClass().add(boton
                            ? "structural-detail-button-cell"
                            : "structural-detail-popup-cell");
                }

                @Override
                protected void updateItem(T valor, boolean vacia) {
                    super.updateItem(valor, vacia);
                    setText(vacia || valor == null
                            ? null : textoVisible.apply(valor));
                    setGraphic(null);
                    if (boton && valor != null) {
                        Tooltip tooltip = new Tooltip(
                                textoVisible.apply(valor));
                        tooltip.setWrapText(true);
                        tooltip.setMaxWidth(520);
                        setTooltip(tooltip);
                    } else {
                        setTooltip(null);
                    }
                }
            };
        }

        private String nombrePartidoDetalle(String fase, Integer numero) {
            String faseVisible = fase == null || fase.isBlank()
                    ? "PARTIDO SIN FASE" : fase.toUpperCase();
            return faseVisible + "  ·  PARTIDO "
                    + (numero == null ? "?" : numero);
        }

        private CrucePropuestoTorneo crearCruce() {
            return new CrucePropuestoTorneo(instancia.getValue(),
                    ronda.getValue(), orden.getValue(),
                    p1.getValue(), p2.getValue());
        }
    }
}
