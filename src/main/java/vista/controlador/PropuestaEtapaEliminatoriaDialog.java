package vista.controlador;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import negocio.CrucePropuestoTorneo;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import negocio.PropuestaEtapaEliminatoria;
import servicio.ConfirmacionPropuestaEliminatoriaService;
import servicio.PropuestaEtapaEliminatoriaService;
import vista.Dialogos;
import vista.Navegacion;

public class PropuestaEtapaEliminatoriaDialog {
    private final TorneoInscripcionDAO inscripcionDAO =
            new TorneoInscripcionDAOMySQL();
    private final java.util.Map<String, String> nombresPorReferencia =
            new java.util.HashMap<>();
    private final PropuestaEtapaEliminatoriaService service =
            new PropuestaEtapaEliminatoriaService();
    private final ConfirmacionPropuestaEliminatoriaService confirmacionService =
            new ConfirmacionPropuestaEliminatoriaService();
    private final PropuestaEtapaEliminatoria propuesta;
    private final PropuestaEtapaEliminatoria automaticaOriginal;
    private final Dialog<PropuestaEtapaEliminatoria> dialogo = new Dialog<>();
    private final VBox contenedorCruces = new VBox(8);
    private final Label estado = etiqueta("", "#d7e4e9", 13, true);
    private final VBox tarjetaAdvertencias = new VBox(8);
    private final VBox listaAdvertencias = new VBox(5);
    private List<String> advertenciasActuales = List.of();
    private final long categoriaId;
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
        dialogo.setHeaderText("Revisa la estructura antes de generar partidos");
        ButtonType continuar = new ButtonType("CONFIRMAR Y GENERAR",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cerrar = new ButtonType("CERRAR",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(continuar, cerrar);

        Label introduccion = etiqueta(
                "Generacion automatica de los cruces basada en terminos "
                + "deportivos preestablecidos, de no estar de acuerdo "
                + "configurarlo manualmente.", "#eaf4f8", 14, true);
        introduccion.setStyle(introduccion.getStyle() + "-fx-font-weight:800;");
        Label resumen = etiqueta(resumen(), "#91b3c3", 13, false);
        Label explicacion = etiqueta(propuesta.getExplicacion(),
                "#d7e4e9", 13, true);
        Label pases = etiqueta(propuesta.getPases().isEmpty()
                ? "Inicio directo en el cuadro principal."
                : "Pases: " + propuesta.getPases().stream()
                    .map(c -> c.referencia()).reduce((a,b) -> a + " · " + b)
                    .orElse(""), "#d7e4e9", 13, true);

        Button intercambiar = new Button("INTERCAMBIAR RIVALES DE ACCESO");
        Button rotar = new Button("ROTAR CABEZAS DE SERIE");
        Button manual = new Button("EDITAR ENFRENTAMIENTOS");
        Button estructura = new Button("CONFIGURAR ESTRUCTURA MANUAL");
        Button restaurar = new Button("RESTAURAR AUTOMATICA");
        intercambiar.setOnAction(e -> {
            service.intercambiarRivalesAcceso(propuesta); validarYActualizar();
        });
        rotar.setOnAction(e -> {
            service.rotarPrimeros(propuesta); validarYActualizar();
        });
        manual.setOnAction(e -> abrirEditorManual());
        estructura.setOnAction(e -> abrirEditorEstructural());
        restaurar.setOnAction(e -> {
            service.restaurarPropuesta(propuesta, automaticaOriginal);
            validarYActualizar();
        });
        FlowPane acciones = new FlowPane(10, 8,
                intercambiar, rotar, manual, estructura, restaurar);

        Label tituloAdvertencias = etiqueta(
                "⚠  ADVERTENCIAS DEPORTIVAS", "#ffd58a", 13, false);
        tituloAdvertencias.setStyle(tituloAdvertencias.getStyle()
                + "-fx-font-weight:900;");
        Label ayudaAdvertencias = etiqueta(
                "La propuesta puede confirmarse, pero se recomienda "
                        + "revisar estos cruces.", "#f1d6a3", 12, true);
        Button revisarAdvertencias = new Button("REVISAR CRUCES");
        revisarAdvertencias.setOnAction(e -> abrirEditorManual());
        tarjetaAdvertencias.getChildren().setAll(tituloAdvertencias,
                listaAdvertencias, ayudaAdvertencias, revisarAdvertencias);
        tarjetaAdvertencias.setPadding(new Insets(12));
        tarjetaAdvertencias.setStyle(
                "-fx-background-color:#3a2b16;"
                + "-fx-border-color:#b98232;"
                + "-fx-border-radius:10;"
                + "-fx-background-radius:10;");
        tarjetaAdvertencias.setVisible(false);
        tarjetaAdvertencias.setManaged(false);

        contenedorCruces.setFillWidth(true);
        VBox contenido = new VBox(12, introduccion, resumen, explicacion,
                etiqueta("ESTRUCTURA PROPUESTA", "#91b3c3", 12, false),
                pases, contenedorCruces, acciones,
                tarjetaAdvertencias, estado);
        contenido.setPadding(new Insets(8));
        contenido.setPrefWidth(760);
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(560);
        scroll.setStyle("-fx-background:#0b1821;-fx-background-color:#0b1821;");
        dialogo.getDialogPane().setContent(scroll);
        Dialogos.preparar(dialogo, "dialog-tournament-bracket-proposal");
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        dialogo.getDialogPane().setPrefSize(840, 700);
        dialogo.setResizable(true);
        botonConfirmar = dialogo.getDialogPane().lookupButton(continuar);
        botonConfirmar.addEventFilter(javafx.event.ActionEvent.ACTION,
                evento -> confirmar(evento));
        dialogo.setResultConverter(tipo ->
                tipo == continuar && propuesta.isValida() ? propuesta : null);
        validarYActualizar();
    }

    private void abrirEditorEstructural() {
        List<CrucePropuestoTorneo> nuevos =
                new EstructuraManualTorneoDialog(propuesta,
                        nombresPorReferencia).mostrar();
        if (nuevos == null) return;
        propuesta.setCruces(nuevos);
        propuesta.setPases(List.of());
        propuesta.setExplicacion("Estructura configurada manualmente por el administrador.");
        validarYActualizar();
    }

    private void abrirEditorManual() {
        Dialog<Boolean> editor = new Dialog<>();
        editor.setTitle("Configuracion manual de cruces");
        editor.setHeaderText(null);
        ButtonType aplicar = new ButtonType("APLICAR CAMBIOS",
                ButtonBar.ButtonData.OK_DONE);
        editor.getDialogPane().getButtonTypes().setAll(aplicar,
                new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        List<String> referencias = propuesta.getClasificados().stream()
                .map(c -> c.referencia()).toList();
        Label tituloEditor = etiqueta(
                "CONFIGURACION MANUAL DE CRUCES", "#91b3c3", 12, false);
        tituloEditor.setStyle(tituloEditor.getStyle()
                + "-fx-font-weight:900;");
        Label subtituloEditor = etiqueta(
                "Asigna cada clasificado a una posicion inicial. "
                        + "Las conexiones entre ganadores permanecen protegidas.",
                "#d7e4e9", 14, true);
        GridPane grilla = new GridPane();
        grilla.getStyleClass().add("manual-editor-grid");
        grilla.setHgap(14);
        grilla.setVgap(10);
        grilla.setPadding(new Insets(14));
        grilla.setStyle("-fx-background-color:#10212b;"
                + "-fx-border-color:#294351;"
                + "-fx-border-radius:10;"
                + "-fx-background-radius:10;");
        List<Edicion> ediciones = new ArrayList<>();
        int fila = 0;
        for (CrucePropuestoTorneo cruce : propuesta.getCruces()) {
            if (!esGanador(cruce.getParticipante1())) {
                fila = agregarEditor(grilla, ediciones, referencias,
                        cruce, true, fila);
            }
            if (!esGanador(cruce.getParticipante2())) {
                fila = agregarEditor(grilla, ediciones, referencias,
                        cruce, false, fila);
            }
        }
        ScrollPane scroll = new ScrollPane(grilla);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        int posicionesEditables = ediciones.size();
        double altoLista = Math.min(390,
                Math.max(150, posicionesEditables * 48 + 32));
        scroll.setPrefViewportHeight(altoLista);
        scroll.setMaxHeight(altoLista + 8);
        scroll.setStyle("-fx-background:#0b1821;"
                + "-fx-background-color:#0b1821;"
                + "-fx-border-color:transparent;");
        VBox contenidoEditor = new VBox(12,
                tituloEditor, subtituloEditor, scroll);
        contenidoEditor.getStyleClass().add("manual-editor-root");
        contenidoEditor.setPadding(new Insets(16));
        contenidoEditor.setStyle("-fx-background-color:#0b1821;");
        editor.getDialogPane().setContent(contenidoEditor);
        editor.getDialogPane().setPrefSize(760,
                Math.min(680, altoLista + 210));
        editor.setResizable(true);
        Dialogos.preparar(editor, "dialog-tournament-bracket-manual");
        editor.getDialogPane().setStyle("-fx-background-color:#0b1821;"
                + "-fx-border-color:#315f79;"
                + "-fx-border-width:1;");
        vista.TemaDinamico.aplicar(editor.getDialogPane(),
                Navegacion.getConfiguracionActual());
        editor.getDialogPane().lookupButton(aplicar).addEventFilter(
                javafx.event.ActionEvent.ACTION, evento -> {
                    for (Edicion edicion : ediciones) edicion.aplicar();
                    List<String> errores = service.validarEdicionManual(propuesta);
                    if (!errores.isEmpty()) {
                        evento.consume();
                        Dialogos.error("Configuracion no valida",
                                mensajeConVinetas("Se encontraron "
                                        + errores.size() + " problemas:",
                                        errores));
                    }
                });
        if (editor.showAndWait().isPresent()) validarYActualizar();
    }

    private int agregarEditor(GridPane grilla, List<Edicion> ediciones,
            List<String> referencias, CrucePropuestoTorneo cruce,
            boolean primero, int fila) {
        String lado = primero ? "Pareja 1" : "Pareja 2";
        String actual = primero ? cruce.getParticipante1()
                : cruce.getParticipante2();
        ComboBox<String> combo = new ComboBox<>(
                FXCollections.observableArrayList(referencias));
        combo.setValue(actual);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPrefWidth(480);
        combo.setStyle("-fx-background-color:#071018;"
                + "-fx-border-color:#315f79;"
                + "-fx-border-radius:7;"
                + "-fx-background-radius:7;"
                + "-fx-mark-color:#91b3c3;"
                + "-fx-text-fill:#eaf4f8;"
                + "-fx-prompt-text-fill:#91b3c3;");
        combo.setCellFactory(lista -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null
                        : mostrarReferencia(valor));
                setTextFill(javafx.scene.paint.Color.web("#eaf4f8"));
                setStyle(manualComboCellStyle(isSelected()));
            }
        });
        combo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null
                        : mostrarReferencia(valor));
                setTextFill(javafx.scene.paint.Color.web("#eaf4f8"));
                setStyle("-fx-background-color:#071018;"
                        + "-fx-text-fill:#eaf4f8;"
                        + "-fx-padding:6 10;");
            }
        });
        GridPane.setHgrow(combo, Priority.ALWAYS);
        Label etiquetaPosicion = etiqueta(
                cruce.getInstancia() + " #" + cruce.getOrden()
                        + " · " + lado,
                "#b9ced8", 13, false);
        etiquetaPosicion.setStyle(etiquetaPosicion.getStyle()
                + "-fx-font-weight:800;");
        grilla.add(etiquetaPosicion, 0, fila);
        grilla.add(combo, 1, fila);
        ediciones.add(new Edicion(cruce, primero, combo));
        return fila + 1;
    }

    private void confirmar(javafx.event.ActionEvent evento) {
        List<String> errores = service.validarEdicionManual(propuesta);
        if (!errores.isEmpty()) {
            evento.consume();
            Dialogos.error("Propuesta no valida",
                    mensajeConVinetas("Revisa los siguientes problemas:",
                            errores));
            validarYActualizar();
            return;
        }
        String tituloConfirmacion = advertenciasActuales.isEmpty()
                ? "Confirmar cuadro"
                : "Confirmar propuesta con advertencias";
        String textoConfirmacion = advertenciasActuales.isEmpty()
                ? "Se crearan los partidos de la propuesta. "
                        + "¿Queres continuar?"
                : mensajeConVinetas(
                        "La estructura es valida, pero contiene "
                                + advertenciasActuales.size()
                                + " advertencias deportivas. "
                                + "¿Queres generar el cuadro igualmente?",
                        advertenciasActuales);
        if (!Dialogos.confirmar(tituloConfirmacion, textoConfirmacion)) {
            evento.consume(); return;
        }
        try {
            confirmacionService.confirmar(categoriaId, propuesta);
        } catch (RuntimeException exception) {
            evento.consume();
            Dialogos.error("No se pudo generar el cuadro",
                    exception.getMessage() == null
                            ? "Revisa la propuesta." : exception.getMessage());
        }
    }

    private void validarYActualizar() {
        service.validarEdicionManual(propuesta);
        contenedorCruces.getChildren().clear();
        for (CrucePropuestoTorneo cruce : propuesta.getCruces()) {
            String faseVisible = nombreVisibleInstancia(cruce);
            Label titulo = etiqueta(faseVisible.toUpperCase()
                    + "  ·  PARTIDO " + cruce.getOrden(),
                    "#ffffff", 13, false);
            titulo.setMaxWidth(Double.MAX_VALUE);
            titulo.setPadding(new Insets(8, 12, 8, 12));
            titulo.setStyle(estiloEncabezadoFase(faseVisible));
            VBox cuerpoTarjeta = new VBox(4,
                    etiqueta(mostrarReferencia(cruce.getParticipante1()),
                            "#eaf4f8", 14, true),
                    etiqueta("VS", "#6f93a5", 11, false),
                    etiqueta(mostrarReferencia(cruce.getParticipante2()),
                            "#eaf4f8", 14, true));
            cuerpoTarjeta.setPadding(new Insets(10, 12, 12, 12));
            VBox tarjeta = new VBox(0, titulo, cuerpoTarjeta);
            tarjeta.setMaxWidth(Double.MAX_VALUE);
            tarjeta.setStyle("-fx-background-color:#10212b;"
                    + "-fx-border-color:#294351;-fx-border-radius:9;"
                    + "-fx-background-radius:9;");
            contenedorCruces.getChildren().add(tarjeta);
        }
        advertenciasActuales = service.advertenciasDeportivas(propuesta);
        listaAdvertencias.getChildren().clear();
        for (String aviso : advertenciasActuales) {
            listaAdvertencias.getChildren().add(
                    etiqueta("• " + aviso, "#ffe2ab", 12, true));
        }
        boolean hayAdvertencias = !advertenciasActuales.isEmpty();
        tarjetaAdvertencias.setVisible(hayAdvertencias);
        tarjetaAdvertencias.setManaged(hayAdvertencias);
        if (!propuesta.isValida()) {
            estado.setText("PROPUSESTA NO VALIDA: " + propuesta.getError());
            estado.setStyle("-fx-text-fill:#ff8f8f;-fx-font-size:13px;"
                    + "-fx-font-weight:800;");
        } else if (hayAdvertencias) {
            estado.setText("PROPUESTA VALIDA CON ADVERTENCIAS");
            estado.setStyle("-fx-text-fill:#ffd58a;-fx-font-size:13px;"
                    + "-fx-font-weight:800;");
        } else {
            estado.setText("PROPUESTA VALIDA · LISTA PARA CONFIRMAR");
            estado.setStyle("-fx-text-fill:#86e0ad;-fx-font-size:13px;"
                    + "-fx-font-weight:800;");
        }
        if (botonConfirmar != null) botonConfirmar.setDisable(!propuesta.isValida());
    }

    private String estiloEncabezadoFase(String fase) {
        String normalizada = fase == null ? "" : fase.toLowerCase();
        String fondo;
        String borde;
        if (normalizada.contains("final")
                && !normalizada.contains("semi")
                && !normalizada.contains("cuartos")
                && !normalizada.contains("octavos")
                && !normalizada.contains("dieciseisavos")) {
            fondo = "#80651f";
            borde = "#d8b84d";
        } else if (normalizada.contains("semifinal")) {
            fondo = "#563c78";
            borde = "#8f70b7";
        } else if (normalizada.contains("cuartos")) {
            fondo = "#245875";
            borde = "#4d88a8";
        } else if (normalizada.contains("octavos")) {
            fondo = "#23656a";
            borde = "#4b9297";
        } else if (normalizada.contains("dieciseisavos")) {
            fondo = "#2d6260";
            borde = "#57918e";
        } else if (normalizada.contains("clasificacion")) {
            fondo = "#7a4f28";
            borde = "#b77c43";
        } else {
            fondo = "#344f60";
            borde = "#5f8091";
        }
        return "-fx-background-color:" + fondo + ";"
                + "-fx-border-color:" + borde + ";"
                + "-fx-border-width:0 0 1 0;"
                + "-fx-background-radius:8 8 0 0;"
                + "-fx-font-weight:900;"
                + "-fx-letter-spacing:0.4px;"
                + "-fx-text-fill:#ffffff;";
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
            return "Fase previa · Ronda " + cruce.getRonda();
        }
        String destino = propuesta.getCruces().stream()
                .filter(c -> c.getRonda() > cruce.getRonda())
                .map(CrucePropuestoTorneo::getInstancia)
                .filter(i -> !i.startsWith("Acceso R"))
                .findFirst().orElse("cuadro principal");
        return "Clasificacion a " + nombreDestino(destino);
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

    private String manualComboCellStyle(boolean seleccionada) {
        return seleccionada
                ? "-fx-background-color:#315f79;"
                    + "-fx-text-fill:#ffffff;"
                    + "-fx-padding:7 10;"
                : "-fx-background-color:#0d1b24;"
                    + "-fx-text-fill:#eaf4f8;"
                    + "-fx-padding:7 10;";
    }

    private void cargarNombres() {
        nombresPorReferencia.clear();
        for (var clasificado : propuesta.getClasificados()) {
            TorneoInscripcion inscripcion = inscripcionDAO.buscar(
                    clasificado.inscripcionId());
            String nombre = inscripcion == null
                    ? "Inscripcion #" + clasificado.inscripcionId()
                    : inscripcion.getJugadores().stream()
                        .sorted(java.util.Comparator.comparingInt(
                                TorneoInscripcionJugador::getOrdenIntegrante))
                        .map(j -> j.getNombre() + " " + j.getApellido())
                        .reduce((a, b) -> a + " / " + b)
                        .orElse("Inscripcion #" + clasificado.inscripcionId());
            nombresPorReferencia.put(clasificado.referencia(), nombre);
        }
    }

    private String mostrarReferencia(String referencia) {
        if (referencia == null) return "Sin asignar";
        String nombre = nombresPorReferencia.get(referencia);
        return nombre == null ? referencia : referencia + "  ·  " + nombre;
    }

    private String mensajeConVinetas(String encabezado,
            List<String> mensajes) {
        StringBuilder texto = new StringBuilder(encabezado);
        for (String mensaje : mensajes) {
            texto.append("\n\n• ").append(mensaje);
        }
        return texto.toString();
    }

    private String resumen() {
        long primeros = contar(1), segundos = contar(2), terceros = contar(3);
        return "Clasificados: " + propuesta.getClasificados().size()
                + " | Primeros: " + primeros + " | Segundos: " + segundos
                + " | Terceros: " + terceros;
    }
    private long contar(int posicion) {
        return propuesta.getClasificados().stream()
                .filter(c -> c.posicionGrupo() == posicion).count();
    }
    private boolean esGanador(String valor) {
        return valor != null && valor.startsWith("Ganador ");
    }
    private static Label etiqueta(String texto, String color,
            int tamano, boolean wrap) {
        Label label = new Label(texto); label.setWrapText(wrap);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-text-fill:" + color + ";-fx-font-size:"
                + tamano + "px;"); return label;
    }
    private record Edicion(CrucePropuestoTorneo cruce, boolean primero,
            ComboBox<String> combo) {
        private void aplicar() {
            if (primero) cruce.setParticipante1(combo.getValue());
            else cruce.setParticipante2(combo.getValue());
        }
    }
}
