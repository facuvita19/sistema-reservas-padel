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

        javafx.scene.Node grafica = crearVistaGrafica();
        contenedorVistas.getChildren().setAll(grafica, scroll);
        grafica.setVisible(true);
        grafica.setManaged(true);
        scroll.setVisible(false);
        scroll.setManaged(false);

        VBox barraAcciones = new VBox(8, agregar);
        barraAcciones.setPadding(new Insets(2, 0, 2, 0));

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
            grafica.setVisible(llave);
            grafica.setManaged(llave);
            scroll.setVisible(!llave);
            scroll.setManaged(!llave);
            barraAcciones.setVisible(!llave);
            barraAcciones.setManaged(!llave);
            introduccion.setVisible(!llave);
            introduccion.setManaged(!llave);
        });
        HBox selectorVista = new HBox(8, botonLlave, botonDetalle);
        selectorVista.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        barraAcciones.setVisible(false);
        barraAcciones.setManaged(false);
        introduccion.setVisible(false);
        introduccion.setManaged(false);
        VBox contenido = new VBox(10, introduccion, selectorVista,
                barraAcciones, contenedorVistas, estado);
        VBox.setVgrow(contenedorVistas, Priority.ALWAYS);
        contenido.setPadding(new Insets(18));
        contenido.setStyle("-fx-background-color:#08151e;");
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
        incluirPrevias.setStyle("-fx-text-fill:#b9ced8;"
                + "-fx-font-size:11px;-fx-font-weight:800;"
                + "-fx-cursor:hand;");
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
        fase.setCellFactory(lista -> celdaSelectorAnimada(
                valor -> textoCapacidadFase(valor)));
        fase.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null
                        ? null : textoCapacidadFase(valor));
                setStyle("-fx-background-color:#0a1922;"
                        + "-fx-text-fill:#f4fbff;-fx-padding:6 9;");
            }
        });

        ComboBox<Integer> rondaNueva = new ComboBox<>();
        rondaNueva.setItems(FXCollections.observableArrayList(
                java.util.stream.IntStream.rangeClosed(1, 10)
                        .boxed().toList()));
        estiloComboNumerico(rondaNueva, 130);
        rondaNueva.setCellFactory(lista -> celdaSelectorAnimada(
                valor -> String.valueOf(valor)));
        rondaNueva.setButtonCell(celdaBotonSelector(
                valor -> String.valueOf(valor)));

        ComboBox<Integer> ordenNuevo = new ComboBox<>();
        ordenNuevo.setItems(FXCollections.observableArrayList(
                java.util.stream.IntStream.rangeClosed(1, 32)
                        .boxed().toList()));
        estiloComboNumerico(ordenNuevo, 130);
        ordenNuevo.setCellFactory(lista -> celdaSelectorAnimada(
                valor -> String.valueOf(valor)));
        ordenNuevo.setButtonCell(celdaBotonSelector(
                valor -> String.valueOf(valor)));

        Label capacidad = etiqueta("", "#91d7f4", 12, true);
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

        Label ayuda = etiqueta(
                "Selecciona una fase con lugares disponibles. Las fases "
                + "previas permanecen ocultas salvo que decidas incluirlas. "
                + "La ronda y el numero se sugieren automaticamente.",
                "#d7e4e9", 13, true);
        GridPane campos = new GridPane();
        campos.setHgap(12);
        campos.setVgap(12);
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
                "#ffd58a", 12, true);
        VBox contenidoNuevo = new VBox(13, ayuda, capacidad, campos, aviso);
        contenidoNuevo.setPadding(new Insets(18));
        contenidoNuevo.setStyle("-fx-background-color:#0b1821;");
        dialogoNuevo.getDialogPane().setContent(contenidoNuevo);
        dialogoNuevo.getDialogPane().setPrefWidth(680);
        Dialogos.preparar(dialogoNuevo,
                "dialog-tournament-bracket-manual");
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

    private static void estiloSelectorVista(ToggleButton boton) {
        boton.setStyle("-fx-background-color:#102532;"
                + "-fx-border-color:#3d6f88;-fx-border-radius:8;"
                + "-fx-background-radius:8;-fx-text-fill:#eaf4f8;"
                + "-fx-font-weight:900;-fx-padding:9 16;"
                + "-fx-cursor:hand;");
        boton.selectedProperty().addListener((obs, antes, seleccionado) ->
                boton.setStyle(seleccionado
                        ? "-fx-background-color:#315f79;"
                            + "-fx-border-color:#91d7f4;"
                            + "-fx-border-radius:8;-fx-background-radius:8;"
                            + "-fx-text-fill:#ffffff;-fx-font-weight:900;"
                            + "-fx-padding:9 16;-fx-cursor:hand;"
                        : "-fx-background-color:#102532;"
                            + "-fx-border-color:#3d6f88;"
                            + "-fx-border-radius:8;-fx-background-radius:8;"
                            + "-fx-text-fill:#eaf4f8;-fx-font-weight:900;"
                            + "-fx-padding:9 16;-fx-cursor:hand;"));
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
