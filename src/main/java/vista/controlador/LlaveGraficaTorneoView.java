package vista.controlador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Scale;
import javafx.scene.input.ScrollEvent;
import negocio.PropuestaEtapaEliminatoria;
import negocio.visual.ConexionLlaveVisual;
import negocio.visual.EstadoConexionLlave;
import negocio.visual.FaseLlaveVisual;
import negocio.visual.LlaveTorneoVisual;
import negocio.visual.PartidoLlaveVisual;
import servicio.ConstructorLlaveTorneoVisualService;
import vista.Dialogos;
import vista.Navegacion;

public class LlaveGraficaTorneoView {
    private static final double ANCHO_TARJETA = 380;
    private static final double ALTO_TARJETA = 126;
    private static final double ANCHO_COLUMNA = 405;
    private static final double MARGEN_X = 28;
    private static final double Y_ENCABEZADO = 18;
    private static final double Y_INICIO = 88;
    private static final double ALTO_MINIMO = 590;
    private static final double SEPARACION_MINIMA = 28;

    private final PropuestaEtapaEliminatoria propuesta;
    private final Map<String, String> nombres;
    private final BiConsumer<String, CambioPlaza> alCambiarPlaza;
    private final Consumer<String> alIntercambiarPartido;
    private final Runnable alAgregarPartido;
    private final Runnable alCrearNuevoCuadro;
    private final Consumer<String> alEliminarPartido;
    private final boolean soloLectura;
    private final ConstructorLlaveTorneoVisualService constructor =
            new ConstructorLlaveTorneoVisualService();
    private final Map<String, PosicionTarjeta> posiciones = new HashMap<>();
    private final Pane tablero = new Pane();
    private final Pane capaLineas = new Pane();
    private final Pane capaTarjetas = new Pane();
    private final Scale escala = new Scale(1, 1, 0, 0);
    private double anchoTablero;
    private double altoTablero;
    private boolean ajusteAutomatico = true;

    public LlaveGraficaTorneoView(PropuestaEtapaEliminatoria propuesta,
            Map<String, String> nombres,
            BiConsumer<String, CambioPlaza> alCambiarPlaza,
            Consumer<String> alIntercambiarPartido,
            Runnable alAgregarPartido,
            Runnable alCrearNuevoCuadro,
            Consumer<String> alEliminarPartido) {
        this(propuesta, nombres, alCambiarPlaza, alIntercambiarPartido,
                alAgregarPartido, alCrearNuevoCuadro, alEliminarPartido, false);
    }

    public LlaveGraficaTorneoView(PropuestaEtapaEliminatoria propuesta,
            Map<String, String> nombres,
            BiConsumer<String, CambioPlaza> alCambiarPlaza,
            Consumer<String> alIntercambiarPartido,
            Runnable alAgregarPartido,
            Runnable alCrearNuevoCuadro,
            Consumer<String> alEliminarPartido,
            boolean soloLectura) {
        this.propuesta = propuesta;
        this.nombres = new HashMap<>(nombres);
        this.alCambiarPlaza = alCambiarPlaza;
        this.alIntercambiarPartido = alIntercambiarPartido;
        this.alAgregarPartido = alAgregarPartido;
        this.alCrearNuevoCuadro = alCrearNuevoCuadro;
        this.alEliminarPartido = alEliminarPartido;
        this.soloLectura = soloLectura;
    }

    public Node crear() {
        LlaveTorneoVisual modelo = constructor.construir(propuesta);
        int mayorCantidad = modelo.fases().stream()
                .mapToInt(f -> f.partidos().size()).max().orElse(1);
        double alto = Math.max(ALTO_MINIMO,
                Y_INICIO + mayorCantidad * ALTO_TARJETA
                + Math.max(0, mayorCantidad - 1) * SEPARACION_MINIMA + 45);
        double ancho = Math.max(1060,
                MARGEN_X * 2 + modelo.fases().size() * ANCHO_COLUMNA);
        anchoTablero = ancho;
        altoTablero = alto;

        tablero.setPrefSize(ancho, alto);
        tablero.setMinSize(ancho, alto);
        tablero.setMaxSize(ancho, alto);
        tablero.getStyleClass().add("structural-bracket-board");
        capaLineas.setPrefSize(ancho, alto);
        capaTarjetas.setPrefSize(ancho, alto);
        capaLineas.setMouseTransparent(true);
        tablero.getChildren().setAll(capaLineas, capaTarjetas);
        tablero.getTransforms().add(escala);

        for (int i = 0; i < modelo.fases().size(); i++) {
            crearFase(modelo.fases().get(i), i, alto);
        }
        Platform.runLater(() -> dibujarConexiones(modelo));

        StackPane contenidoEscalado = new StackPane(tablero);
        contenidoEscalado.setAlignment(Pos.TOP_LEFT);
        actualizarTamanoEscalado(contenidoEscalado);

        ScrollPane scroll = new ScrollPane(contenidoEscalado);
        scroll.setPannable(true);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setPrefViewportHeight(510);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("structural-bracket-scroll");
        scroll.addEventFilter(ScrollEvent.SCROLL, evento -> {
            double delta = Math.abs(evento.getDeltaY()) > 0.01
                    ? evento.getDeltaY() : evento.getDeltaX();
            if (Math.abs(delta) < 0.01) return;
            if (evento.isShiftDown()) {
                double paso = delta > 0 ? -0.07 : 0.07;
                scroll.setHvalue(limitar(scroll.getHvalue() + paso));
            } else {
                double paso = delta > 0 ? -0.07 : 0.07;
                scroll.setVvalue(limitar(scroll.getVvalue() + paso));
            }
            evento.consume();
        });

        Label porcentaje = new Label("100%");
        porcentaje.setMinWidth(48);
        porcentaje.setMouseTransparent(true);
        porcentaje.setAlignment(Pos.CENTER);
        porcentaje.getStyleClass().add("structural-zoom-value");
        Button reducir = botonZoom("-");
        reducir.setTooltip(new Tooltip("Alejar"));
        Button aumentar = botonZoom("+");
        aumentar.setTooltip(new Tooltip("Acercar"));
        Button ajustar = botonZoom("AJUSTAR A VENTANA");
        reducir.setOnAction(e -> {
            ajusteAutomatico = false;
            cambiarZoom(Math.max(0.60, escala.getX() - 0.10),
                    contenidoEscalado, porcentaje);
        });
        aumentar.setOnAction(e -> {
            ajusteAutomatico = false;
            cambiarZoom(Math.min(1.30, escala.getX() + 0.10),
                    contenidoEscalado, porcentaje);
        });
        ajustar.setOnAction(e -> {
            ajusteAutomatico = false;
            ajustar(scroll, contenidoEscalado, porcentaje, true);
            scroll.setVvalue(0.0);
            scroll.setHvalue(0.0);
        });
        Button nuevoCuadro = botonZoom("NUEVO CUADRO");
        nuevoCuadro.getStyleClass().add("structural-tool-new");
        nuevoCuadro.setOnAction(evento -> {
            if (alCrearNuevoCuadro != null) alCrearNuevoCuadro.run();
        });

        Button agregarPartido = botonZoom("+  AGREGAR PARTIDO");
        agregarPartido.getStyleClass().add("structural-tool-add");
        agregarPartido.setOnAction(evento -> {
            if (alAgregarPartido != null) alAgregarPartido.run();
        });
        ajustar.getStyleClass().add("structural-tool-fit");
        HBox grupoEstructura = new HBox(8, nuevoCuadro, agregarPartido);
        grupoEstructura.setAlignment(Pos.CENTER_LEFT);
        grupoEstructura.getStyleClass().add("structural-toolbar-group");

        HBox grupoZoom = new HBox(7, ajustar, reducir, porcentaje, aumentar);
        grupoZoom.setAlignment(Pos.CENTER_LEFT);
        grupoZoom.getStyleClass().add("structural-toolbar-group");

        HBox leyenda = crearLeyenda();
        leyenda.getStyleClass().add("structural-toolbar-legend");
        if (soloLectura) {
            Label tituloLeyenda = new Label("ESTADO ESTRUCTURAL");
            tituloLeyenda.getStyleClass().add("structural-legend-title");
            leyenda.getChildren().add(0, tituloLeyenda);
        }
        javafx.scene.layout.Region espacio = new javafx.scene.layout.Region();
        HBox.setHgrow(espacio, javafx.scene.layout.Priority.ALWAYS);
        HBox herramientas = soloLectura
                ? new HBox(18, grupoZoom, espacio, leyenda)
                : new HBox(18, grupoEstructura, grupoZoom, espacio, leyenda);
        herramientas.setAlignment(Pos.CENTER_LEFT);
        herramientas.setPadding(new Insets(8, 10, 8, 10));
        herramientas.getStyleClass().add("structural-toolbar");

        Platform.runLater(() -> {
            ajustar(scroll, contenidoEscalado, porcentaje, true);
            ajusteAutomatico = false;
        });

        BorderPane contenedor = new BorderPane(scroll);
        contenedor.setTop(herramientas);
        contenedor.setPrefHeight(560);
        contenedor.setMinHeight(390);
        contenedor.getStyleClass().add("structural-bracket-shell");
        return contenedor;
    }

    private HBox crearLeyenda() {
        HBox leyenda = new HBox(14,
                itemLeyenda("#65b998", "Valido"),
                itemLeyenda("#d8b36d", "Requiere atencion"),
                itemLeyenda("#d97882", "Estructura invalida"));
        leyenda.setAlignment(Pos.CENTER_LEFT);
        return leyenda;
    }

    private HBox itemLeyenda(String color, String texto) {
        Label punto = new Label("●");
        punto.setStyle("-fx-text-fill:" + color
                + ";-fx-font-size:11px;-fx-font-weight:900;");
        Label descripcion = new Label(texto);
        descripcion.getStyleClass().add("structural-legend-text");
        HBox item = new HBox(6, punto, descripcion);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private double limitar(double valor) {
        return Math.max(0, Math.min(1, valor));
    }

    private Button botonZoom(String texto) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("structural-tool-button");
        if ("-".equals(texto) || "+".equals(texto)) {
            boton.getStyleClass().add("structural-tool-icon");
        }
        return boton;
    }

    private void ajustar(ScrollPane scroll, StackPane contenedor,
            Label porcentaje, boolean volverAlInicio) {
        double disponibleX = Math.max(100,
                scroll.getViewportBounds().getWidth() - 24);
        double nuevo = Math.min(1.0,
                disponibleX / anchoTablero);
        nuevo = Math.max(0.85, nuevo);
        cambiarZoom(nuevo, contenedor, porcentaje);
        if (volverAlInicio) {
            scroll.setHvalue(0.5);
            scroll.setVvalue(0.0);
        }
    }

    private void cambiarZoom(double valor, StackPane contenedor,
            Label porcentaje) {
        escala.setX(valor);
        escala.setY(valor);
        porcentaje.setText(Math.round(valor * 100) + "%");
        actualizarTamanoEscalado(contenedor);
    }

    private void actualizarTamanoEscalado(StackPane contenedor) {
        double ancho = anchoTablero * escala.getX();
        double alto = altoTablero * escala.getY();
        contenedor.setPrefSize(ancho, alto);
        contenedor.setMinSize(ancho, alto);
        contenedor.setMaxSize(ancho, alto);
    }

    private void crearFase(FaseLlaveVisual fase, int indice, double alto) {
        double x = MARGEN_X + indice * ANCHO_COLUMNA;
        Label titulo = new Label(nombreFase(fase.nombre()));
        titulo.setStyle("-fx-text-fill:" + colorFase(fase.nombre())
                + ";-fx-font-weight:900;-fx-font-size:12px;");

        int cantidadPartidosFase = fase.partidos().size();
        Label contador = new Label(cantidadPartidosFase
                + (cantidadPartidosFase == 1
                        ? " PARTIDO" : " PARTIDOS"));
        contador.setStyle("-fx-text-fill:#9ba6aa;"
                + "-fx-font-size:9px;-fx-font-weight:900;");

        javafx.scene.layout.Region espacioEncabezado =
                new javafx.scene.layout.Region();
        HBox.setHgrow(espacioEncabezado,
                javafx.scene.layout.Priority.ALWAYS);
        HBox encabezadoFase = new HBox(8, titulo,
                espacioEncabezado, contador);
        encabezadoFase.setAlignment(Pos.CENTER_LEFT);
        encabezadoFase.setPadding(new Insets(0, 12, 0, 12));
        encabezadoFase.setPrefSize(ANCHO_TARJETA, 38);
        encabezadoFase.setMaxSize(ANCHO_TARJETA, 38);
        encabezadoFase.setLayoutX(x);
        encabezadoFase.setLayoutY(Y_ENCABEZADO + 5);
        encabezadoFase.setStyle("-fx-background-color:#1b2024;"
                + "-fx-border-color:" + colorFase(fase.nombre())
                + ";-fx-border-width:0 0 3 0;"
                + "-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),"
                + "7,0.08,0,2);");
        capaTarjetas.getChildren().add(encabezadoFase);

        int cantidad = Math.max(1, fase.partidos().size());
        double zona = alto - Y_INICIO - 30;
        double paso = zona / cantidad;
        for (int i = 0; i < fase.partidos().size(); i++) {
            PartidoLlaveVisual partido = fase.partidos().get(i);
            double centro = Y_INICIO + paso * (i + 0.5);
            double y = centro - ALTO_TARJETA / 2;
            StackPane tarjeta = crearTarjeta(partido);
            tarjeta.setLayoutX(x);
            tarjeta.setLayoutY(y);
            capaTarjetas.getChildren().add(tarjeta);
            posiciones.put(partido.clave(),
                    new PosicionTarjeta(x, y, ANCHO_TARJETA, ALTO_TARJETA));
        }
    }

    private StackPane crearTarjeta(PartidoLlaveVisual partido) {
        String tituloPartido = "Final".equalsIgnoreCase(partido.fase())
                ? nombreFase(partido.fase())
                : nombreFase(partido.fase()) + "  ·  PARTIDO "
                        + partido.orden();
        Label cabecera = new Label(tituloPartido);
        cabecera.setMinWidth(0);
        cabecera.setMaxWidth(Double.MAX_VALUE);
        cabecera.setTextOverrun(
                javafx.scene.control.OverrunStyle.ELLIPSIS);
        cabecera.setStyle("-fx-text-fill:" + colorFase(partido.fase())
                + ";-fx-font-size:10px;-fx-font-weight:900;");
        Button intercambiar = new Button("INTERCAMBIAR");
        intercambiar.setFocusTraversable(false);
        aplicarEstiloAccionTarjeta(intercambiar, false);
        intercambiar.getStyleClass().add("structural-card-swap-v2");
        intercambiar.setCursor(javafx.scene.Cursor.HAND);
        intercambiar.setMinSize(104, 28);
        intercambiar.setPrefSize(104, 28);
        intercambiar.setMaxSize(104, 28);
        Button eliminar = new Button("ELIMINAR");
        eliminar.setFocusTraversable(false);
        aplicarEstiloAccionTarjeta(eliminar, true);
        eliminar.getStyleClass().add("structural-card-delete-v2");
        eliminar.setCursor(javafx.scene.Cursor.HAND);
        eliminar.setMinSize(76, 28);
        eliminar.setPrefSize(76, 28);
        eliminar.setMaxSize(76, 28);
        eliminar.setOnAction(evento -> confirmarEliminacion(partido));
        HBox acciones = new HBox(5, intercambiar, eliminar);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        acciones.setVisible(!soloLectura);
        acciones.setManaged(!soloLectura);
        javafx.scene.layout.Region espacioCabecera =
                new javafx.scene.layout.Region();
        HBox.setHgrow(espacioCabecera,
                javafx.scene.layout.Priority.ALWAYS);
        HBox encabezado = new HBox(6, cabecera,
                espacioCabecera, acciones);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.setPadding(new Insets(5, 7, 5, 10));
        encabezado.setMinHeight(38);
        encabezado.setPrefHeight(38);
        encabezado.setMaxHeight(38);
        encabezado.setStyle("-fx-background-color:#20262a;"
                + "-fx-border-color:" + colorFase(partido.fase())
                + ";-fx-border-width:0 0 2 0;"
                + "-fx-background-radius:8 8 0 0;");

        Label uno = plaza(partido.clave(), 1,
                partido.plaza1().referencia(),
                partido.plaza1().textoVisible());
        Label dos = plaza(partido.clave(), 2,
                partido.plaza2().referencia(),
                partido.plaza2().textoVisible());
        intercambiar.setOnAction(evento -> {
            if (alIntercambiarPartido != null) {
                alIntercambiarPartido.accept(partido.clave());
            }
            intercambiarEnModelo(partido.clave());
            String textoUno = uno.getText();
            Tooltip tooltipUno = uno.getTooltip();
            uno.setText(dos.getText());
            uno.setTooltip(dos.getTooltip());
            dos.setText(textoUno);
            dos.setTooltip(tooltipUno);
            dibujarConexiones(constructor.construir(propuesta));
        });
        Label vs = new Label("VS");
        vs.setMaxWidth(Double.MAX_VALUE);
        vs.setAlignment(Pos.CENTER);
        vs.setStyle("-fx-text-fill:#7f8b90;-fx-font-size:8.5px;"
                + "-fx-font-weight:900;-fx-padding:1 0;");
        VBox contenido = new VBox(1, uno, vs, dos);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(7, 9, 8, 9));

        EstadoTarjeta estado = estadoTarjeta(partido);
        VBox caja = new VBox(0, encabezado, contenido);
        caja.setPrefSize(ANCHO_TARJETA, ALTO_TARJETA);
        caja.setMaxSize(ANCHO_TARJETA, ALTO_TARJETA);
        caja.setStyle("-fx-background-color:#1b2024;"
                + "-fx-border-color:" + estado.color()
                + ";-fx-border-width:" + estado.grosor()
                + ";-fx-border-radius:9;-fx-background-radius:9;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.20),"
                + "8,0.08,0,2);");
        if (estado.mensaje() != null) {
            Tooltip problema = new Tooltip(estado.mensaje());
            problema.setWrapText(true);
            problema.setMaxWidth(480);
            Tooltip.install(caja, problema);
        }
        StackPane tarjeta = new StackPane(caja);
        tarjeta.setPrefSize(ANCHO_TARJETA, ALTO_TARJETA);
        return tarjeta;
    }

    private Label plaza(String partidoClave, int posicion,
            String referencia, String texto) {
        String nombreCompleto = nombres.get(referencia);
        String visible = nombreCompleto == null ? texto : nombreCompleto;
        Label label = new Label(visible == null ? "Sin asignar" : visible);
        label.setWrapText(false);
        label.setTextOverrun(
                javafx.scene.control.OverrunStyle.ELLIPSIS);
        label.setMaxWidth(ANCHO_TARJETA - 20);
        label.setPrefWidth(ANCHO_TARJETA - 20);
        label.setMinHeight(31);
        label.setPrefHeight(31);
        label.setMaxHeight(31);
        String detalle = visible == null ? "Sin asignar" : visible;
        if (nombreCompleto != null && referencia != null) {
            detalle = referencia + " · " + nombreCompleto;
        }
        Tooltip tooltip = new Tooltip(detalle);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(520);
        label.setTooltip(tooltip);
        String estiloNormal = estiloPlazaLlave(
                referencia == null, false);
        String estiloActivo = estiloPlazaLlave(
                referencia == null, true);
        label.setStyle(estiloNormal);
        if (!soloLectura) {
            label.setOnMouseEntered(evento -> label.setStyle(estiloActivo));
            label.setOnMouseExited(evento -> label.setStyle(estiloNormal));
            label.setOnMouseClicked(evento -> {
                label.setStyle(estiloActivo);
                seleccionarOrigen(partidoClave, posicion, referencia, label);
            });
        } else {
            label.setStyle(estiloNormal.replace("-fx-cursor:hand;", ""));
        }
        return label;
    }

    private void seleccionarOrigen(String partidoClave, int posicion,
            String actual, Label etiquetaPlaza) {
        Dialog<String> dialogo = new Dialog<>();
        dialogo.setTitle("Cambiar participante");
        dialogo.setHeaderText(tituloCambio(partidoClave, posicion));
        ButtonType aplicar = new ButtonType("ASIGNAR",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType limpiar = new ButtonType("LIMPIAR PLAZA",
                ButtonBar.ButtonData.OTHER);
        ButtonType cancelar = new ButtonType("CANCELAR",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(
                limpiar, aplicar, cancelar);

        Set<String> valores = opcionesValidas(partidoClave, actual);
        ComboBox<String> selector = new ComboBox<>(
                FXCollections.observableArrayList(valores));
        selector.setValue(actual);
        selector.setVisibleRowCount(6);
        selector.setMaxWidth(Double.MAX_VALUE);
        selector.setPrefWidth(620);
        selector.getStyleClass().addAll(
                "structural-add-combo",
                "structural-participant-combo");
        selector.setCellFactory(lista -> celdaOrigen(false));
        selector.setButtonCell(celdaOrigen(true));

        Label insignia = new Label("CAMBIAR PARTICIPANTE");
        insignia.getStyleClass().add("structural-add-badge");
        Label ayuda = new Label(
                "Selecciona una opcion habilitada para esta fase. "
                + "Se muestran solamente las parejas que ingresan en "
                + "esta ronda y los ganadores de la ronda anterior.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("structural-add-help");
        VBox contexto = new VBox(6, insignia, ayuda);
        contexto.getStyleClass().add("structural-add-context");

        Label etiquetaSelector = new Label("PARTICIPANTE U ORIGEN");
        etiquetaSelector.getStyleClass().add(
                "structural-participant-field-label");
        VBox campo = new VBox(7, etiquetaSelector, selector);
        campo.getStyleClass().add("structural-add-fields");

        VBox contenido = new VBox(10, contexto, campo);
        contenido.getStyleClass().add("structural-add-root");
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(760, 405);
        dialogo.getDialogPane().setMinSize(700, 385);
        Dialogos.preparar(dialogo, "structural-add-dialog");
        dialogo.getDialogPane().getStyleClass().add(
                "structural-participant-dialog");
        dialogo.setResizable(false);

        javafx.scene.Node botonAplicar = dialogo.getDialogPane()
                .lookupButton(aplicar);
        javafx.scene.Node botonLimpiar = dialogo.getDialogPane()
                .lookupButton(limpiar);
        javafx.scene.Node botonCancelar = dialogo.getDialogPane()
                .lookupButton(cancelar);
        botonAplicar.getStyleClass().add(
                "structural-participant-confirm");
        botonAplicar.setCursor(javafx.scene.Cursor.HAND);
        botonLimpiar.getStyleClass().add(
                "structural-participant-clear");
        botonLimpiar.setCursor(javafx.scene.Cursor.HAND);
        botonCancelar.getStyleClass().add(
                "structural-add-cancel");
        botonCancelar.setCursor(javafx.scene.Cursor.HAND);

        final String limpiarMarca = "__LIMPIAR_PLAZA__";
        botonLimpiar.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            boolean confirmado = Dialogos.confirmarPeligroPersonalizado(
                    "Limpiar plaza",
                    "Dejar esta plaza sin participante",
                    "La plaza quedará sin participante y la estructura "
                            + "requerirá atención hasta asignar un origen válido."
                            + "\n\n¿Deseás limpiar esta plaza?",
                    "LIMPIAR PLAZA",
                    "CANCELAR");
            if (!confirmado) evento.consume();
        });
        dialogo.setResultConverter(tipo -> {
            if (tipo == aplicar) return selector.getValue();
            if (tipo == limpiar) return limpiarMarca;
            return null;
        });
        String elegido = dialogo.showAndWait().orElse(null);
        if (elegido != null) {
            String referenciaNueva = limpiarMarca.equals(elegido)
                    ? null : elegido;
            if (alCambiarPlaza != null) {
                alCambiarPlaza.accept(partidoClave,
                        new CambioPlaza(posicion, referenciaNueva));
            } else {
                actualizarPlazaEnModelo(partidoClave, posicion,
                        referenciaNueva);
                actualizarEtiquetaPlaza(etiquetaPlaza, referenciaNueva);
                dibujarConexiones(constructor.construir(propuesta));
            }
        }
    }

    private void confirmarEliminacion(PartidoLlaveVisual partido) {
        long destinos = propuesta.getCruces().stream()
                .flatMap(c -> java.util.stream.Stream.of(
                        c.getParticipante1(), c.getParticipante2()))
                .filter(java.util.Objects::nonNull)
                .filter(("Ganador " + partido.clave())::equals)
                .count();
        String texto = destinos > 0
                ? "El ganador de este partido alimenta " + destinos
                    + " plaza(s). Si lo eliminas, esas plazas quedaran "
                    + "sin un origen valido."
                : "Este partido no alimenta ninguna plaza posterior.";
        String mensaje = texto
                + "\n\nLa estructura debera quedar valida antes "
                + "de aplicarla.";

        boolean confirmado = Dialogos.confirmarPeligroPersonalizado(
                "Eliminar partido",
                "Eliminar " + partido.fase()
                        + " (Partido " + partido.orden() + ")",
                mensaje,
                "ELIMINAR PARTIDO",
                "CANCELAR");
        if (confirmado && alEliminarPartido != null) {
            alEliminarPartido.accept(partido.clave());
        }
    }

    private void actualizarPlazaEnModelo(String clavePartido,
            int posicion, String referencia) {
        var cruce = propuesta.getCruces().stream()
                .filter(c -> (c.getInstancia() + " #" + c.getOrden())
                        .equals(clavePartido))
                .findFirst().orElse(null);
        if (cruce == null) return;
        if (posicion == 1) {
            cruce.setParticipante1(referencia);
        } else {
            cruce.setParticipante2(referencia);
        }
    }

    private void actualizarEtiquetaPlaza(Label etiqueta,
            String referencia) {
        String visible;
        if (referencia == null || referencia.isBlank()) {
            visible = "Sin asignar";
        } else {
            visible = nombres.getOrDefault(referencia,
                    textoOrigen(referencia));
        }
        etiqueta.setText(visible);
        String detalle = visible;
        if (referencia != null && nombres.containsKey(referencia)) {
            detalle = referencia + " · " + nombres.get(referencia);
        }
        Tooltip tooltip = new Tooltip(detalle);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(520);
        etiqueta.setTooltip(tooltip);
    }

    private Set<String> opcionesValidas(String partidoClave,
            String actual) {
        Set<String> valores = new LinkedHashSet<>();
        var destino = propuesta.getCruces().stream()
                .filter(c -> (c.getInstancia() + " #" + c.getOrden())
                        .equals(partidoClave))
                .findFirst().orElse(null);
        if (destino == null) {
            if (actual != null) valores.add(actual);
            return valores;
        }

        int rondaDestino = destino.getRonda();
        int rondaAnterior = propuesta.getCruces().stream()
                .mapToInt(c -> c.getRonda())
                .filter(r -> r < rondaDestino)
                .max().orElse(-1);

        Set<String> clasificadosUsadosAntes = new LinkedHashSet<>();
        propuesta.getCruces().stream()
                .filter(c -> c.getRonda() < rondaDestino)
                .flatMap(c -> java.util.stream.Stream.of(
                        c.getParticipante1(), c.getParticipante2()))
                .filter(java.util.Objects::nonNull)
                .filter(r -> !r.startsWith("Ganador "))
                .forEach(clasificadosUsadosAntes::add);

        propuesta.getClasificados().stream()
                .map(c -> c.referencia())
                .filter(r -> !clasificadosUsadosAntes.contains(r))
                .forEach(valores::add);

        if (rondaAnterior >= 0) {
            propuesta.getCruces().stream()
                    .filter(c -> c.getRonda() == rondaAnterior)
                    .map(c -> "Ganador " + c.getInstancia()
                            + " #" + c.getOrden())
                    .forEach(valores::add);
        }
        if (actual != null) valores.add(actual);
        valores.remove("Ganador " + partidoClave);
        return valores;
    }

    private String tituloCambio(String partidoClave, int posicion) {
        String partido = partidoClave;
        int separador = partidoClave.lastIndexOf(" #");
        if (separador >= 0) {
            partido = partidoClave.substring(0, separador)
                    + " (Partido "
                    + partidoClave.substring(separador + 2) + ")";
        }
        return "Cambiar " + (posicion == 1
                ? "primer participante" : "segundo participante")
                + " de " + partido;
    }

    private javafx.scene.control.ListCell<String> celdaOrigen(
            boolean boton) {
        return new javafx.scene.control.ListCell<>() {
            {
                getStyleClass().add(boton
                        ? "structural-add-button-cell"
                        : "structural-add-popup-cell");
            }

            @Override
            protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null
                        ? null : textoOrigen(valor));
                setGraphic(null);
            }
        };
    }

    private String textoOrigen(String referencia) {
        String nombre = nombres.get(referencia);
        if (nombre != null) return referencia + " · " + nombre;
        if (referencia.startsWith("Ganador ")) {
            String clave = referencia.substring("Ganador ".length());
            int separador = clave.lastIndexOf(" #");
            if (separador >= 0) {
                return "Ganador de " + clave.substring(0, separador)
                        + " (Partido " + clave.substring(separador + 2)
                        + ")";
            }
        }
        return referencia;
    }

    private EstadoTarjeta estadoTarjeta(PartidoLlaveVisual partido) {
        java.util.List<String> problemas = new ArrayList<>();
        boolean invalida = false;
        boolean atencion = false;

        if (partido.plaza1().referencia() == null
                || partido.plaza1().referencia().isBlank()) {
            problemas.add("La primera plaza esta sin asignar.");
            atencion = true;
        }
        if (partido.plaza2().referencia() == null
                || partido.plaza2().referencia().isBlank()) {
            problemas.add("La segunda plaza esta sin asignar.");
            atencion = true;
        }
        for (ConexionLlaveVisual conexion : constructor.construir(propuesta)
                .conexiones()) {
            if (!partido.clave().equals(conexion.partidoDestinoClave())
                    && !partido.clave().equals(
                            conexion.partidoOrigenClave())) {
                continue;
            }
            if (conexion.estado() == EstadoConexionLlave.SIN_DESTINO) {
                problemas.add("El ganador de este partido no tiene destino.");
                atencion = true;
            } else if (conexion.estado()
                    != EstadoConexionLlave.VALIDA) {
                problemas.add(mensajeEstado(conexion.estado()));
                invalida = true;
            }
        }
        long veces1 = contarUso(partido.plaza1().referencia());
        long veces2 = contarUso(partido.plaza2().referencia());
        if (partido.plaza1().ganadorPartido() && veces1 > 1) {
            problemas.add("El primer origen esta asignado " + veces1
                    + " veces dentro de la llave.");
            invalida = true;
        }
        if (partido.plaza2().ganadorPartido() && veces2 > 1) {
            problemas.add("El segundo origen esta asignado " + veces2
                    + " veces dentro de la llave.");
            invalida = true;
        }
        String mensaje = problemas.isEmpty() ? null
                : String.join("\n", problemas.stream().distinct().toList());
        if (invalida) return new EstadoTarjeta("#d97882", 2.2, mensaje);
        if (atencion) return new EstadoTarjeta("#d8b36d", 2.0, mensaje);
        return new EstadoTarjeta("#65b998", 1.3, null);
    }

    private long contarUso(String referencia) {
        if (referencia == null || !referencia.startsWith("Ganador ")) {
            return 0;
        }
        return propuesta.getCruces().stream()
                .flatMap(c -> java.util.stream.Stream.of(
                        c.getParticipante1(), c.getParticipante2()))
                .filter(referencia::equals).count();
    }

    private String mensajeEstado(EstadoConexionLlave estado) {
        return switch (estado) {
            case ORIGEN_INEXISTENTE ->
                    "Uno de los origenes apunta a un partido inexistente.";
            case DUPLICADA ->
                    "Un ganador esta asignado mas de una vez.";
            case RONDA_INVALIDA ->
                    "La conexion no avanza hacia una ronda posterior.";
            case SIN_DESTINO ->
                    "El ganador no tiene un partido de destino.";
            case VALIDA -> "Conexion valida.";
        };
    }

    private void intercambiarEnModelo(String clavePartido) {
        var cruce = propuesta.getCruces().stream()
                .filter(c -> (c.getInstancia() + " #" + c.getOrden())
                        .equals(clavePartido))
                .findFirst().orElse(null);
        if (cruce == null) return;
        String primero = cruce.getParticipante1();
        cruce.setParticipante1(cruce.getParticipante2());
        cruce.setParticipante2(primero);
    }

    private void dibujarConexiones(LlaveTorneoVisual modelo) {
        capaLineas.getChildren().clear();
        for (ConexionLlaveVisual conexion : modelo.conexiones()) {
            if (conexion.partidoDestinoClave() == null) continue;
            PosicionTarjeta origen = posiciones.get(
                    conexion.partidoOrigenClave());
            PosicionTarjeta destino = posiciones.get(
                    conexion.partidoDestinoClave());
            if (origen == null || destino == null) continue;

            double x1 = origen.x() + origen.ancho();
            double y1 = origen.y() + origen.alto() / 2;
            double x2 = destino.x();
            double y2 = destino.y()
                    + (conexion.posicionDestino() == 1
                        ? destino.alto() * 0.53
                        : destino.alto() * 0.80);
            double medio = x1 + (x2 - x1) * 0.50;
            Color color = color(conexion.estado());

            Path linea = new Path(new MoveTo(x1, y1),
                    new LineTo(medio, y1),
                    new LineTo(medio, y2),
                    new LineTo(x2 - 11, y2));
            linea.setStroke(color);
            linea.setStrokeWidth(switch (conexion.estado()) {
                case VALIDA -> 2.0;
                case SIN_DESTINO -> 2.6;
                case ORIGEN_INEXISTENTE, DUPLICADA, RONDA_INVALIDA -> 2.8;
            });
            linea.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            linea.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
            linea.setFill(Color.TRANSPARENT);

            Polygon flecha = new Polygon(x2 - 9, y2 - 4,
                    x2, y2, x2 - 9, y2 + 4);
            flecha.setFill(color);
            capaLineas.getChildren().addAll(linea, flecha);
        }
    }

    private Color color(EstadoConexionLlave estado) {
        return switch (estado) {
            case VALIDA -> Color.web("#65b998");
            case SIN_DESTINO -> Color.web("#d8b36d");
            case ORIGEN_INEXISTENTE, DUPLICADA, RONDA_INVALIDA ->
                    Color.web("#d97882");
        };
    }

    private void aplicarEstiloAccionTarjeta(
            Button boton, boolean peligrosa) {
        String normal = peligrosa
                ? "-fx-background-color:#332427;-fx-border-color:#7d454c;"
                  + "-fx-text-fill:#e8b0b5;"
                : "-fx-background-color:#252b2f;-fx-border-color:#515c62;"
                  + "-fx-text-fill:#d7dddf;";
        String hover = peligrosa
                ? "-fx-background-color:#432b2f;-fx-border-color:#a85c65;"
                  + "-fx-text-fill:#ffd5d8;"
                : "-fx-background-color:#302a38;-fx-border-color:#78638d;"
                  + "-fx-text-fill:#eee3f6;";
        String comun = "-fx-border-radius:6;-fx-background-radius:6;"
                + "-fx-font-size:8px;-fx-font-weight:900;"
                + "-fx-padding:4 7;-fx-cursor:hand;";
        boton.setStyle(normal + comun);
        boton.setOnMouseEntered(e ->
                boton.setStyle(hover + comun + "-fx-translate-y:-1;"));
        boton.setOnMouseExited(e -> boton.setStyle(normal + comun));
        boton.setOnMousePressed(e -> boton.setStyle(hover + comun));
        boton.setOnMouseReleased(e ->
                boton.setStyle(hover + comun + "-fx-translate-y:-1;"));
    }
    private String estiloPlazaLlave(boolean vacia, boolean activa) {
        if (activa) {
            return "-fx-text-fill:#ffffff;-fx-font-size:11px;"
                    + "-fx-font-weight:800;-fx-background-color:#302a38;"
                    + "-fx-border-color:#78638d;-fx-border-radius:6;"
                    + "-fx-background-radius:6;-fx-padding:4 7;"
                    + "-fx-cursor:hand;";
        }
        return "-fx-text-fill:" + (vacia ? "#d8b36d" : "#e3e8e9")
                + ";-fx-font-size:11px;-fx-font-weight:700;"
                + "-fx-background-color:#22292d;"
                + "-fx-border-color:transparent;-fx-border-radius:6;"
                + "-fx-background-radius:6;-fx-padding:4 7;"
                + "-fx-cursor:hand;";
    }

    private String nombreFase(String fase) {
        if (fase == null) return "FASE";
        if (fase.startsWith("Acceso R")) {
            return ("Fase previa "
                    + fase.substring("Acceso R".length())).toUpperCase();
        }
        return fase.toUpperCase();
    }

    private String estiloFase(String fase) {
        return "-fx-background-color:" + colorFase(fase) + ";";
    }

    private String colorFase(String fase) {
        String valor = fase == null ? "" : fase.toLowerCase();
        if (valor.equals("final")) return "#b08a2e";
        if (valor.contains("semifinal")) return "#7a5aa0";
        if (valor.contains("cuartos")) return "#4f829d";
        if (valor.contains("octavos")) return "#4f9290";
        if (valor.contains("dieciseisavos")) return "#5b8b7f";
        if (valor.contains("acceso")) return "#a66f3b";
        return "#687b86";
    }

    public record CambioPlaza(int posicion, String referencia) {
    }

    private record EstadoTarjeta(
            String color, double grosor, String mensaje) {
    }

    private record PosicionTarjeta(
            double x, double y, double ancho, double alto) {
    }
}
