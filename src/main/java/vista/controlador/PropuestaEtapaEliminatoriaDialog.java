package vista.controlador;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import negocio.ClasificadoEtapaEliminatoria;
import negocio.CrucePropuestoTorneo;
import negocio.PropuestaEtapaEliminatoria;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import servicio.ConfirmacionPropuestaEliminatoriaService;
import servicio.PropuestaEtapaEliminatoriaService;
import vista.Dialogos;
import vista.Navegacion;

public class PropuestaEtapaEliminatoriaDialog {
    private final TorneoInscripcionDAO inscripcionDAO =
            new TorneoInscripcionDAOMySQL();
    private final Map<String, String> nombresPorReferencia =
            new java.util.HashMap<>();
    private final PropuestaEtapaEliminatoriaService service =
            new PropuestaEtapaEliminatoriaService();
    private final ConfirmacionPropuestaEliminatoriaService confirmacionService =
            new ConfirmacionPropuestaEliminatoriaService();
    private final PropuestaEtapaEliminatoria propuesta;
    private final PropuestaEtapaEliminatoria automaticaOriginal;
    private final Dialog<PropuestaEtapaEliminatoria> dialogo = new Dialog<>();
    private final HBox selectorFases = new HBox(7);
    private final ToggleGroup grupoFases = new ToggleGroup();
    private final GridPane grillaCruces = new GridPane();
    private ScrollPane scrollCruces;
    private final VBox listaAdvertencias = new VBox(4);
    private final VBox tarjetaAdvertencias = new VBox(7);
    private final Label estado = new Label();
    private final Label metricaClasificados = new Label();
    private final Label metricaPartidos = new Label();
    private final Label metricaPases = new Label();
    private final Label metricaAdvertencias = new Label();
    private final Label explicacion = new Label();
    private final Label pases = new Label();
    private final long categoriaId;
    private List<String> advertenciasActuales = List.of();
    private String faseSeleccionada;
    private int columnasActuales = 2;
    private javafx.scene.Node botonConfirmar;

    public PropuestaEtapaEliminatoriaDialog(long categoriaId) {
        this.categoriaId = categoriaId;
        propuesta = service.proponer(categoriaId);
        automaticaOriginal = service.proponer(categoriaId);
        cargarNombres();
        construir();
    }

    public PropuestaEtapaEliminatoria mostrar() {
        return dialogo.showAndWait().orElse(null);
    }

    private void construir() {
        dialogo.setTitle("Propuesta de etapa eliminatoria");
        dialogo.setHeaderText("Propuesta eliminatoria");
        ButtonType continuar = new ButtonType("CONFIRMAR Y GENERAR",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cerrar = new ButtonType("CANCELAR",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(continuar, cerrar);

        Label tituloContexto = new Label("DISTRIBUCIÓN AUTOMÁTICA");
        tituloContexto.getStyleClass().add("proposal-eyebrow");
        Label ayuda = new Label("Revisá la distribución deportiva antes de "
                + "generar los partidos. Podés editar los cruces o consultar "
                + "la llave completa sin modificar el borrador.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("proposal-intro-copy");
        explicacion.setWrapText(true);
        explicacion.getStyleClass().add("proposal-detail-copy");
        pases.setVisible(false);
        pases.setManaged(false);
        Label subtituloContexto = new Label("ETAPA ELIMINATORIA");
        subtituloContexto.getStyleClass().add("proposal-context-badge-v1b");
        Label contextoCompetencia = new Label(cargarContextoCompetencia());
        contextoCompetencia.getStyleClass().add("proposal-competition-context-v4");
        Region espacioContexto = new Region();
        HBox.setHgrow(espacioContexto, Priority.ALWAYS);
        HBox cabeceraContexto = new HBox(10, tituloContexto,
                espacioContexto, subtituloContexto);
        cabeceraContexto.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.layout.FlowPane protegidos =
                new javafx.scene.layout.FlowPane(6, 5);
        protegidos.getStyleClass().add("proposal-protected-flow-v4");
        actualizarProtegidos(protegidos);
        VBox contexto = new VBox(3, cabeceraContexto, contextoCompetencia,
                ayuda, protegidos, pases);
        contexto.getStyleClass().add("proposal-intro-card");

        for (Label metrica : new Label[] { metricaClasificados,
                metricaPartidos, metricaPases, metricaAdvertencias }) {
            metrica.getStyleClass().add("proposal-metric");
        }
        Label separadorMetrica1 = new Label("·");
        Label separadorMetrica2 = new Label("·");
        Label separadorMetrica3 = new Label("·");
        for (Label separador : new Label[] { separadorMetrica1,
                separadorMetrica2, separadorMetrica3 }) {
            separador.getStyleClass().add("proposal-metric-separator");
        }
        HBox metricas = new HBox(9, metricaClasificados, separadorMetrica1,
                metricaPartidos, separadorMetrica2, metricaPases,
                separadorMetrica3, metricaAdvertencias);
        metricas.setAlignment(Pos.CENTER_LEFT);
        metricas.getStyleClass().add("proposal-metrics");

        Label tituloAdvertencias = new Label("ADVERTENCIAS DEPORTIVAS");
        tituloAdvertencias.getStyleClass().add("proposal-warning-title");
        Label cantidadAdvertencias = new Label();
        cantidadAdvertencias.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(() -> {
                    int cantidad = listaAdvertencias.getChildren().size();
                    return cantidad + (cantidad == 1
                            ? " DETECTADA" : " DETECTADAS");
                }, listaAdvertencias.getChildren()));
        cantidadAdvertencias.getStyleClass().add("proposal-warning-count");
        Region espacioAdvertencia = new Region();
        HBox.setHgrow(espacioAdvertencia, Priority.ALWAYS);
        HBox encabezadoAdvertencias = new HBox(10, tituloAdvertencias,
                espacioAdvertencia, cantidadAdvertencias);
        encabezadoAdvertencias.setAlignment(Pos.CENTER_LEFT);
        tarjetaAdvertencias.getChildren().setAll(encabezadoAdvertencias,
                listaAdvertencias);
        tarjetaAdvertencias.getStyleClass().add("proposal-warning-card");
        tarjetaAdvertencias.setVisible(false);
        tarjetaAdvertencias.setManaged(false);

        Label tituloFases = new Label("PARTIDOS POR FASE");
        tituloFases.getStyleClass().add("proposal-section-title");
        selectorFases.setAlignment(Pos.CENTER_LEFT);
        selectorFases.getStyleClass().add("proposal-phase-tabs");
        VBox navegacionFases = new VBox(6, tituloFases, selectorFases);

        grillaCruces.setHgap(10);
        grillaCruces.setVgap(10);
        grillaCruces.getStyleClass().add("proposal-match-grid");
        scrollCruces = new ScrollPane(grillaCruces);
        scrollCruces.setFitToWidth(true);
        scrollCruces.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCruces.getStyleClass().add("proposal-matches-scroll");
        scrollCruces.setPrefViewportHeight(250);
        scrollCruces.setMinViewportHeight(150);
        VBox.setVgrow(scrollCruces, Priority.ALWAYS);
        scrollCruces.setMaxHeight(Double.MAX_VALUE);
        scrollCruces.viewportBoundsProperty().addListener((obs, anterior, actual) -> {
            int nuevasColumnas = actual.getWidth() >= 1320 ? 3
                    : actual.getWidth() >= 760 ? 2 : 1;
            if (nuevasColumnas != columnasActuales) {
                columnasActuales = nuevasColumnas;
                renderizarCruces();
            }
        });

        estado.getStyleClass().add("proposal-status-badge");
        Button editar = new Button("EDITAR CRUCES");
        editar.getStyleClass().add("proposal-edit-button");
        editar.setOnAction(e -> abrirEditorUnificado());
        Button vistaPreliminar = new Button("VISTA PRELIMINAR");
        vistaPreliminar.getStyleClass().add("proposal-preview-button");
        vistaPreliminar.setOnAction(e -> abrirVistaPreliminar());
        Button restaurar = new Button("RESTABLECER");
        restaurar.getStyleClass().add("proposal-restore-button");
        restaurar.setOnAction(e -> restaurarPropuesta());
        Region separadorAcciones = new Region();
        HBox.setHgrow(separadorAcciones, Priority.ALWAYS);
        Label ayudaConfirmacion = new Label("Se crearán "
                + propuesta.getCruces().size()
                + " partidos eliminatorios.");
        ayudaConfirmacion.getStyleClass().add("proposal-confirm-copy-v1b");
        HBox bloqueDecision = new HBox(9, ayudaConfirmacion, estado);
        bloqueDecision.setAlignment(Pos.CENTER_RIGHT);
        HBox acciones = new HBox(9, editar, vistaPreliminar, restaurar,
                separadorAcciones, bloqueDecision);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.getStyleClass().add("proposal-local-actions");

        VBox superior = new VBox(8, contexto, metricas,
                tarjetaAdvertencias, navegacionFases);
        superior.getStyleClass().add("proposal-fixed-summary");

        VBox raiz = new VBox(8, superior, scrollCruces, acciones);
        raiz.setPadding(new Insets(12, 16, 10, 16));
        raiz.getStyleClass().add("proposal-review-root");
        VBox.setVgrow(scrollCruces, Priority.ALWAYS);
        dialogo.getDialogPane().setContent(raiz);

        Dialogos.preparar(dialogo, "dialog-tournament-bracket-proposal");
        dialogo.getDialogPane().getStyleClass().add("proposal-review-dialog");
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        int maximoPorFase = fasesConCantidad().values().stream()
                .mapToInt(Long::intValue).max().orElse(1);
        double anchoPreferido = maximoPorFase >= 4 ? 1320 : 1180;
        double altoPreferido = maximoPorFase >= 4 ? 760 : 700;
        dialogo.getDialogPane().setPrefSize(anchoPreferido, altoPreferido);
        dialogo.getDialogPane().setMinSize(900, 610);
        dialogo.setResizable(true);
        dialogo.setOnShown(e -> javafx.application.Platform.runLater(() -> {
            javafx.stage.Window ventana = dialogo.getDialogPane()
                    .getScene().getWindow();
            if (ventana instanceof javafx.stage.Stage stage) {
                stage.setMaximized(true);
            }
        }));

        botonConfirmar = dialogo.getDialogPane().lookupButton(continuar);
        botonConfirmar.getStyleClass().add("proposal-confirm-final-v5");
        javafx.scene.Node botonCancelar =
                dialogo.getDialogPane().lookupButton(cerrar);
        botonCancelar.getStyleClass().add("proposal-cancel-final-v5");
        botonConfirmar.addEventFilter(javafx.event.ActionEvent.ACTION,
                this::confirmar);

        dialogo.setResultConverter(tipo ->
                tipo == continuar && propuesta.isValida() ? propuesta : null);
        validarYActualizar();
    }

    private void abrirVistaPreliminar() {
        Dialog<Void> dialogoVista = new Dialog<>();
        dialogoVista.setTitle("Vista preliminar del cuadro");
        dialogoVista.setHeaderText("Vista preliminar de la llave");
        ButtonType cerrarVista = new ButtonType("CERRAR",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogoVista.getDialogPane().getButtonTypes().setAll(cerrarVista);
        Label soloLectura = new Label("SOLO LECTURA");
        soloLectura.getStyleClass().add("proposal-preview-readonly-badge");
        Label estadoEstructural = new Label(
                "Vista sin edición · Los colores indican validez estructural");
        estadoEstructural.getStyleClass().add("proposal-preview-structural-note");
        Region espacioCabecera = new Region();
        HBox.setHgrow(espacioCabecera, Priority.ALWAYS);
        HBox cabeceraVista = new HBox(10, soloLectura,
                espacioCabecera, estadoEstructural);
        cabeceraVista.setAlignment(Pos.CENTER_LEFT);
        cabeceraVista.getStyleClass().add("proposal-preview-info-bar");

        HBox avisoDeportivo = new HBox(10);
        avisoDeportivo.setAlignment(Pos.CENTER_LEFT);
        avisoDeportivo.getStyleClass().add("proposal-preview-warning-card");
        avisoDeportivo.setVisible(!advertenciasActuales.isEmpty());
        avisoDeportivo.setManaged(!advertenciasActuales.isEmpty());
        if (!advertenciasActuales.isEmpty()) {
            Label tituloAviso = new Label(advertenciasActuales.size()
                    + (advertenciasActuales.size() == 1
                            ? " ADVERTENCIA DEPORTIVA" : " ADVERTENCIAS DEPORTIVAS"));
            tituloAviso.getStyleClass().add("proposal-preview-warning-title");
            Label detalleAviso = new Label(String.join("  ·  ", advertenciasActuales));
            detalleAviso.setWrapText(false);
            detalleAviso.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(detalleAviso, Priority.ALWAYS);
            detalleAviso.getStyleClass().add("proposal-preview-warning-item");
            avisoDeportivo.getChildren().setAll(tituloAviso, detalleAviso);
        }

        javafx.scene.Node llave = new LlaveGraficaTorneoView(
                propuesta, nombresPorReferencia, null, null,
                null, null, null, true).crear();
        VBox contenido = new VBox(8, cabeceraVista, avisoDeportivo, llave);
        contenido.setPadding(new Insets(10, 12, 10, 12));
        contenido.getStyleClass().add("proposal-preview-root");
        VBox.setVgrow(llave, Priority.ALWAYS);
        dialogoVista.getDialogPane().setContent(contenido);
        Dialogos.preparar(dialogoVista, "structural-editor-dialog");
        dialogoVista.getDialogPane().getStyleClass().add(
                "proposal-preview-dialog");
        vista.TemaDinamico.aplicar(dialogoVista.getDialogPane(),
                Navegacion.getConfiguracionActual());
        if (dialogo.getDialogPane().getScene() != null) {
            dialogoVista.initOwner(
                    dialogo.getDialogPane().getScene().getWindow());
        }
        dialogoVista.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialogoVista.getDialogPane().setPrefSize(1280, 700);
        dialogoVista.getDialogPane().setMinSize(920, 600);
        dialogoVista.setResizable(true);
        dialogoVista.showAndWait();
    }

    private void restaurarPropuesta() {
        if (!Dialogos.confirmar("Restaurar propuesta",
                "Se descartarán los cambios realizados y se recuperará "
                + "la distribución automática. ¿Querés continuar?")) return;
        service.restaurarPropuesta(propuesta, automaticaOriginal);
        validarYActualizar();
    }

    private void abrirEditorUnificado() {
        PropuestaEtapaEliminatoria cambios = new EditorCuadroTorneoDialog(
                propuesta, automaticaOriginal, nombresPorReferencia).mostrar();
        if (cambios == null) return;
        service.restaurarPropuesta(propuesta, cambios);
        propuesta.setExplicacion("Propuesta revisada por el administrador.");
        validarYActualizar();
    }

    private void confirmar(javafx.event.ActionEvent evento) {
        List<String> errores = service.validarEdicionManual(propuesta);
        if (!errores.isEmpty()) {
            evento.consume();
            Dialogos.error("Propuesta no válida",
                    mensajeConVinetas("Revisa los siguientes problemas:",
                            errores));
            validarYActualizar();
            return;
        }
        String tituloConfirmacion = advertenciasActuales.isEmpty()
                ? "Confirmar cuadro"
                : "Confirmar propuesta con advertencias";
        String textoConfirmacion = advertenciasActuales.isEmpty()
                ? "Se crearan los partidos de la propuesta. Queres continuar?"
                : mensajeConVinetas("La estructura es válida, pero contiene "
                        + advertenciasActuales.size()
                        + " advertencias deportivas. ¿Querés generar el cuadro "
                        + "igualmente?", advertenciasActuales);
        if (!Dialogos.confirmar(tituloConfirmacion, textoConfirmacion)) {
            evento.consume();
            return;
        }
        try {
            confirmacionService.confirmar(categoriaId, propuesta);
        } catch (RuntimeException exception) {
            evento.consume();
            Dialogos.error("No se pudo generar el cuadro",
                    exception.getMessage() == null
                            ? "Revisá la propuesta." : exception.getMessage());
        }
    }

    private void validarYActualizar() {
        service.validarEdicionManual(propuesta);
        advertenciasActuales = service.advertenciasDeportivas(propuesta);
        actualizarResumen();
        actualizarAdvertencias();
        actualizarFases();
        renderizarCruces();
        actualizarEstado();
        if (botonConfirmar != null) {
            botonConfirmar.setDisable(!propuesta.isValida());
        }
    }

    private void actualizarResumen() {
        metricaClasificados.setText(propuesta.getClasificados().size()
                + "  CLASIFICADOS");
        metricaPartidos.setText(propuesta.getCruces().size() + "  PARTIDOS");
        metricaPases.setText(propuesta.getPases().size()
                + " PRIMEROS PROTEGIDOS");
        metricaAdvertencias.setText(advertenciasActuales.size()
                + (advertenciasActuales.size() == 1
                        ? "  ADVERTENCIA" : "  ADVERTENCIAS"));
        metricaAdvertencias.getStyleClass().removeAll(
                "proposal-metric-warning", "proposal-metric-ok");
        metricaAdvertencias.getStyleClass().add(advertenciasActuales.isEmpty()
                ? "proposal-metric-ok" : "proposal-metric-warning");
        String textoExplicacion = propuesta.getExplicacion();
        if (textoExplicacion == null || textoExplicacion.isBlank()
                || textoExplicacion.toLowerCase().contains(
                        "generacion automatica de los cruces")) {
            textoExplicacion = "Distribución calculada automáticamente con "
                    + "criterios deportivos.";
        }
        explicacion.setText(textoExplicacion);
        pases.setText(propuesta.getPases().isEmpty()
                ? "Sin primeros protegidos: todos ingresan en la primera fase."
                : "Primeros protegidos: " + propuesta.getPases().stream()
                        .map(ClasificadoEtapaEliminatoria::referencia)
                        .collect(java.util.stream.Collectors.joining(" | ")));
    }

    private void actualizarAdvertencias() {
        listaAdvertencias.getChildren().clear();
        for (String aviso : advertenciasActuales) {
            Label item = new Label("\u2022  " + aviso);
            item.setWrapText(true);
            item.getStyleClass().add("proposal-warning-item");
            listaAdvertencias.getChildren().add(item);
        }
        boolean mostrar = !advertenciasActuales.isEmpty();
        tarjetaAdvertencias.setVisible(mostrar);
        tarjetaAdvertencias.setManaged(mostrar);
    }

    private void actualizarFases() {
        Map<String, Long> fases = fasesConCantidad();
        if (faseSeleccionada == null || !fases.containsKey(faseSeleccionada)) {
            faseSeleccionada = fases.keySet().stream().findFirst().orElse(null);
        }
        selectorFases.getChildren().clear();
        grupoFases.getToggles().clear();
        for (Map.Entry<String, Long> entrada : fases.entrySet()) {
            Label nombreFase = new Label(entrada.getKey().toUpperCase());
            nombreFase.getStyleClass().add("proposal-phase-name-v1b");
            Label cantidadFase = new Label(String.valueOf(entrada.getValue()));
            cantidadFase.getStyleClass().add("proposal-phase-count-v1b");
            HBox contenidoFase = new HBox(7, nombreFase, cantidadFase);
            contenidoFase.setAlignment(Pos.CENTER);
            ToggleButton boton = new ToggleButton();
            boton.setGraphic(contenidoFase);
            boton.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
            boton.setToggleGroup(grupoFases);
            boton.getStyleClass().addAll("proposal-phase-tab",
                    claseFase(entrada.getKey()));
            boton.setSelected(entrada.getKey().equals(faseSeleccionada));
            boton.setOnAction(e -> {
                if (!boton.isSelected()) {
                    boton.setSelected(true);
                    return;
                }
                faseSeleccionada = entrada.getKey();
                renderizarCruces();
            });
            selectorFases.getChildren().add(boton);
        }
    }

    private Map<String, Long> fasesConCantidad() {
        Map<String, Long> fases = new LinkedHashMap<>();
        propuesta.getCruces().stream()
                .sorted(Comparator.comparingInt(CrucePropuestoTorneo::getRonda)
                        .thenComparingInt(CrucePropuestoTorneo::getOrden))
                .forEach(cruce -> fases.merge(nombreVisibleInstancia(cruce),
                        1L, Long::sum));
        return fases;
    }

    private void renderizarCruces() {
        grillaCruces.getChildren().clear();
        grillaCruces.getColumnConstraints().clear();
        grillaCruces.getRowConstraints().clear();
        if (faseSeleccionada == null) return;
        List<CrucePropuestoTorneo> cruces = propuesta.getCruces().stream()
                .filter(c -> faseSeleccionada.equals(nombreVisibleInstancia(c)))
                .sorted(Comparator.comparingInt(CrucePropuestoTorneo::getOrden))
                .toList();

        String fase = faseSeleccionada.toUpperCase();
        int columnas = columnasParaFase(fase, cruces.size());
        int maximoPorColumna = maximoPorColumna(fase, cruces.size());
        grillaCruces.setAlignment(Pos.TOP_LEFT);
        grillaCruces.setMaxWidth(Region.USE_PREF_SIZE);
        grillaCruces.setPrefWidth(columnas * 720.0
                + Math.max(0, columnas - 1) * 12.0);

        for (int indice = 0; indice < cruces.size(); indice++) {
            CrucePropuestoTorneo cruce = cruces.get(indice);
            VBox tarjeta = crearTarjeta(cruce);
            tarjeta.setMinWidth(720);
            tarjeta.setPrefWidth(720);
            tarjeta.setMaxWidth(720);
            tarjeta.setMinHeight(150);
            tarjeta.setPrefHeight(150);
            tarjeta.setMaxHeight(150);

            int columna;
            int fila;
            if (columnas == 1) {
                columna = 0;
                fila = indice;
            } else if ("OCTAVOS".equals(fase)
                    || "DIECISEISAVOS".equals(fase)) {
                columna = indice / maximoPorColumna;
                fila = indice % maximoPorColumna;
            } else {
                columna = indice % columnas;
                fila = indice / columnas;
            }
            grillaCruces.add(tarjeta, columna, fila);
            GridPane.setHgrow(tarjeta, Priority.NEVER);
            GridPane.setFillWidth(tarjeta, false);
            GridPane.setHalignment(tarjeta, javafx.geometry.HPos.LEFT);
        }

        for (int indice = 0; indice < columnas; indice++) {
            javafx.scene.layout.ColumnConstraints columna =
                    new javafx.scene.layout.ColumnConstraints();
            columna.setMinWidth(720);
            columna.setPrefWidth(720);
            columna.setMaxWidth(720);
            columna.setHgrow(Priority.NEVER);
            grillaCruces.getColumnConstraints().add(columna);
        }
        int filas = columnas == 1 ? cruces.size()
                : (int) Math.ceil(cruces.size() / (double) columnas);
        double alto = Math.max(170, filas * 150.0
                + Math.max(0, filas - 1) * 12.0 + 12.0);
        scrollCruces.setMinHeight(170);
        scrollCruces.setPrefHeight(Math.min(alto, 660));
        scrollCruces.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(scrollCruces, Priority.ALWAYS);
    }

    private int columnasParaFase(String fase, int cantidad) {
        if ("FINAL".equals(fase) || "SEMIFINAL".equals(fase)) return 1;
        if ("CUARTOS".equals(fase)) return cantidad > 1 ? 2 : 1;
        if ("OCTAVOS".equals(fase)) return cantidad > 4 ? 2 : 1;
        if ("DIECISEISAVOS".equals(fase)) return cantidad > 8 ? 2 : 1;
        return cantidad > 4 ? 2 : 1;
    }

    private int maximoPorColumna(String fase, int cantidad) {
        if ("OCTAVOS".equals(fase)) return 4;
        if ("DIECISEISAVOS".equals(fase)) return 8;
        return Math.max(1, (int) Math.ceil(cantidad / 2.0));
    }

    private VBox crearTarjeta(CrucePropuestoTorneo cruce) {
        String fase = nombreVisibleInstancia(cruce);
        Label faseLabel = new Label(fase.toUpperCase());
        faseLabel.getStyleClass().addAll("proposal-match-phase", claseFase(fase));
        Label numero = new Label("PARTIDO " + cruce.getOrden());
        numero.getStyleClass().add("proposal-match-number");
        boolean mostrarNumero = !"FINAL".equalsIgnoreCase(fase);
        numero.setVisible(mostrarNumero);
        numero.setManaged(mostrarNumero);
        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);
        HBox cabecera = new HBox(7, faseLabel, separador, numero);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.getStyleClass().add("proposal-match-header");

        VBox primero = crearParticipante(cruce.getParticipante1());
        Label vs = new Label("VS");
        vs.getStyleClass().add("proposal-versus");
        VBox segundo = crearParticipante(cruce.getParticipante2());
        VBox cuerpo = new VBox(4, primero, vs, segundo);
        cuerpo.getStyleClass().add("proposal-match-body");

        VBox tarjeta = new VBox(0, cabecera, cuerpo);
        tarjeta.setMinHeight(126);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.getStyleClass().addAll("proposal-match-card", claseFase(fase));
        return tarjeta;
    }

    private String cargarContextoCompetencia() {
        String sql = "SELECT CONCAT(t.nombre, ' · ', c.nombre) "
                + "FROM torneo_categorias c "
                + "INNER JOIN torneos t ON t.id=c.torneo_id WHERE c.id=?";
        try (java.sql.Connection conexion = config.ConexionBD.obtenerConexion();
                java.sql.PreparedStatement sentencia =
                        conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (java.sql.ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    String valor = resultado.getString(1);
                    if (valor != null && !valor.isBlank()) return valor;
                }
            }
        } catch (java.sql.SQLException exception) {
            return "Categoría " + categoriaId;
        }
        return "Categoría " + categoriaId;
    }

    private void actualizarProtegidos(
            javafx.scene.layout.FlowPane contenedor) {
        Label titulo = new Label(propuesta.getPases().isEmpty()
                ? "SIN PRIMEROS PROTEGIDOS" : "PRIMEROS PROTEGIDOS");
        titulo.getStyleClass().add("proposal-protected-title-v4");
        contenedor.getChildren().add(titulo);
        for (ClasificadoEtapaEliminatoria clasificado : propuesta.getPases()) {
            Label badge = new Label(clasificado.referencia());
            badge.getStyleClass().add("proposal-protected-badge-v4");
            contenedor.getChildren().add(badge);
        }
    }

    private String nombreParticipanteVisible(String referencia) {
        String visible = mostrarReferencia(referencia);
        if (referencia == null || referencia.isBlank()
                || referencia.startsWith("Ganador ")) return visible;
        int separador = visible.indexOf(" - ");
        return separador >= 0 && separador + 3 < visible.length()
                ? visible.substring(separador + 3) : visible;
    }

    private VBox crearParticipante(String referencia) {
        String visible = nombreParticipanteVisible(referencia);
        String origen = origenVisible(referencia);
        Label origenLabel = new Label(origen);
        origenLabel.getStyleClass().add("proposal-participant-origin-v1b");
        Label nombreLabel = new Label(visible);
        nombreLabel.setWrapText(true);
        nombreLabel.getStyleClass().add("proposal-participant-name-v1b");
        VBox bloque = new VBox(1, origenLabel, nombreLabel);
        bloque.setMinHeight(38);
        bloque.getStyleClass().add("proposal-participant");
        return bloque;
    }

    private String origenVisible(String referencia) {
        if (referencia == null || referencia.isBlank()) return "ORIGEN PENDIENTE";
        if (referencia.startsWith("Ganador ")) {
            return referencia.substring("Ganador ".length()).toUpperCase();
        }
        int indiceGrupo = referencia.indexOf("Grupo ");
        if (indiceGrupo >= 0) {
            int finGrupo = referencia.indexOf(" · ", indiceGrupo);
            return (finGrupo > indiceGrupo
                    ? referencia.substring(0, finGrupo)
                    : referencia.substring(0)).toUpperCase();
        }
        return "CLASIFICADO";
    }

    private void actualizarEstado() {
        estado.getStyleClass().removeAll("valid", "warning", "invalid");
        if (!propuesta.isValida()) {
            estado.setText("PROPUESTA NO VÁLIDA");
            estado.getStyleClass().add("invalid");
            estado.setTooltip(new javafx.scene.control.Tooltip(
                    propuesta.getError() == null ? "Revisá la propuesta."
                            : propuesta.getError()));
        } else if (!advertenciasActuales.isEmpty()) {
            estado.setText("VÁLIDA CON ADVERTENCIAS");
            estado.getStyleClass().add("warning");
            estado.setTooltip(new javafx.scene.control.Tooltip(
                    String.join("\n", advertenciasActuales)));
        } else {
            estado.setText("LISTA PARA GENERAR");
            estado.getStyleClass().add("valid");
            estado.setTooltip(null);
        }
    }

    private String claseFase(String fase) {
        String valor = fase == null ? "" : fase.toLowerCase();
        if (valor.contains("semifinal")) return "phase-semifinal";
        if (valor.contains("cuartos")) return "phase-quarter";
        if (valor.contains("octavos")) return "phase-round16";
        if (valor.contains("dieciseisavos")) return "phase-round32";
        if (valor.contains("final")) return "phase-final";
        if (valor.contains("clasificacion") || valor.contains("fase previa")) {
            return "phase-qualifying";
        }
        return "phase-generic";
    }

    private String nombreVisibleInstancia(CrucePropuestoTorneo cruce) {
        if (!cruce.getInstancia().startsWith("Acceso R")) {
            return cruce.getInstancia();
        }
        int ultimaRondaPrevia = propuesta.getCruces().stream()
                .filter(c -> c.getInstancia().startsWith("Acceso R"))
                .mapToInt(CrucePropuestoTorneo::getRonda)
                .max().orElse(cruce.getRonda());
        if (cruce.getRonda() < ultimaRondaPrevia) {
            return "Fase previa - Ronda " + cruce.getRonda();
        }
        String destino = propuesta.getCruces().stream()
                .filter(c -> c.getRonda() > cruce.getRonda())
                .map(CrucePropuestoTorneo::getInstancia)
                .filter(i -> !i.startsWith("Acceso R"))
                .findFirst().orElse("cuadro principal");
        return "Clasificación a " + nombreDestino(destino);
    }

    private String nombreDestino(String instancia) {
        return switch (instancia) {
            case "Final" -> "la final";
            case "Semifinal" -> "semifinales";
            case "Cuartos" -> "cuartos de final";
            case "Octavos" -> "octavos de final";
            case "Dieciseisavos" -> "dieciseisavos de final";
            default -> instancia;
        };
    }

    private void cargarNombres() {
        nombresPorReferencia.clear();
        for (var clasificado : propuesta.getClasificados()) {
            TorneoInscripcion inscripcion = inscripcionDAO.buscar(
                    clasificado.inscripcionId());
            String nombre = inscripcion == null
                    ? "Inscripción #" + clasificado.inscripcionId()
                    : inscripcion.getJugadores().stream()
                        .sorted(Comparator.comparingInt(
                                TorneoInscripcionJugador::getOrdenIntegrante))
                        .map(j -> j.getNombre() + " " + j.getApellido())
                        .reduce((a, b) -> a + " / " + b)
                        .orElse("Inscripción #" + clasificado.inscripcionId());
            nombresPorReferencia.put(clasificado.referencia(), nombre);
        }
    }

    private String mostrarReferencia(String referencia) {
        if (referencia == null) return "Sin asignar";
        String nombre = nombresPorReferencia.get(referencia);
        return nombre == null ? referencia : referencia + "  -  " + nombre;
    }

    private String mensajeConVinetas(String encabezado,
            List<String> mensajes) {
        StringBuilder texto = new StringBuilder(encabezado);
        for (String mensaje : mensajes) {
            texto.append("\n\n\u2022 ").append(mensaje);
        }
        return texto.toString();
    }
}
