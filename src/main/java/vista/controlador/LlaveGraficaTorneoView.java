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
    private static final double ALTO_TARJETA = 150;
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
        this.propuesta = propuesta;
        this.nombres = new HashMap<>(nombres);
        this.alCambiarPlaza = alCambiarPlaza;
        this.alIntercambiarPartido = alIntercambiarPartido;
        this.alAgregarPartido = alAgregarPartido;
        this.alCrearNuevoCuadro = alCrearNuevoCuadro;
        this.alEliminarPartido = alEliminarPartido;
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
        tablero.setStyle("-fx-background-color:#08151e;");
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
        scroll.setStyle("-fx-background:#08151e;"
                + "-fx-background-color:#08151e;"
                + "-fx-border-color:#294351;"
                + "-fx-border-radius:10;"
                + "-fx-background-radius:10;");
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
        porcentaje.setMinWidth(54);
        porcentaje.setAlignment(Pos.CENTER);
        porcentaje.setStyle("-fx-text-fill:#d7e4e9;"
                + "-fx-font-weight:900;-fx-font-size:12px;");
        Button reducir = botonZoom("−");
        Button aumentar = botonZoom("+");
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
        });
        Button nuevoCuadro = botonZoom("NUEVO CUADRO");
        nuevoCuadro.setStyle("-fx-background-color:#173646;"
                + "-fx-border-color:#5e879a;-fx-border-radius:7;"
                + "-fx-background-radius:7;-fx-text-fill:#eaf4f8;"
                + "-fx-font-weight:900;-fx-padding:7 13;"
                + "-fx-cursor:hand;");
        nuevoCuadro.setOnAction(evento -> {
            if (alCrearNuevoCuadro != null) alCrearNuevoCuadro.run();
        });

        Button agregarPartido = botonZoom("+  AGREGAR PARTIDO");
        agregarPartido.setStyle("-fx-background-color:#245875;"
                + "-fx-border-color:#65b9d7;-fx-border-radius:7;"
                + "-fx-background-radius:7;-fx-text-fill:#ffffff;"
                + "-fx-font-weight:900;-fx-padding:7 13;"
                + "-fx-cursor:hand;");
        agregarPartido.setOnAction(evento -> {
            if (alAgregarPartido != null) alAgregarPartido.run();
        });
        HBox leyenda = crearLeyenda();
        HBox herramientas = new HBox(12, nuevoCuadro, agregarPartido,
                new Separator(), ajustar, new Separator(),
                reducir, porcentaje, aumentar, new Separator(), leyenda);
        herramientas.setAlignment(Pos.CENTER_LEFT);
        herramientas.setPadding(new Insets(8, 10, 8, 10));
        herramientas.setStyle("-fx-background-color:#0e202b;"
                + "-fx-border-color:#294351;"
                + "-fx-border-width:0 0 1 0;");

        Platform.runLater(() -> {
            ajustar(scroll, contenidoEscalado, porcentaje, true);
            ajusteAutomatico = false;
        });

        BorderPane contenedor = new BorderPane(scroll);
        contenedor.setTop(herramientas);
        contenedor.setPrefHeight(560);
        contenedor.setMinHeight(390);
        contenedor.setStyle("-fx-background-color:#08151e;"
                + "-fx-border-color:#294351;-fx-border-radius:10;"
                + "-fx-background-radius:10;");
        return contenedor;
    }

    private HBox crearLeyenda() {
        HBox leyenda = new HBox(12,
                itemLeyenda("#65b9d7", "Valido"),
                itemLeyenda("#e6b85c", "Requiere atencion"),
                itemLeyenda("#e56d76", "Estructura invalida"));
        leyenda.setAlignment(Pos.CENTER_LEFT);
        return leyenda;
    }

    private HBox itemLeyenda(String color, String texto) {
        Label punto = new Label("●");
        punto.setStyle("-fx-text-fill:" + color
                + ";-fx-font-size:14px;-fx-font-weight:900;");
        Label descripcion = new Label(texto);
        descripcion.setStyle("-fx-text-fill:#c7d8df;"
                + "-fx-font-size:11px;-fx-font-weight:700;");
        HBox item = new HBox(5, punto, descripcion);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private double limitar(double valor) {
        return Math.max(0, Math.min(1, valor));
    }

    private Button botonZoom(String texto) {
        Button boton = new Button(texto);
        boton.setStyle("-fx-background-color:#173747;"
                + "-fx-border-color:#3d6f88;-fx-border-radius:7;"
                + "-fx-background-radius:7;-fx-text-fill:#f4fbff;"
                + "-fx-font-weight:900;-fx-padding:7 12;"
                + "-fx-cursor:hand;");
        return boton;
    }

    private void ajustar(ScrollPane scroll, StackPane contenedor,
            Label porcentaje, boolean volverAlInicio) {
        double disponibleX = Math.max(100,
                scroll.getViewportBounds().getWidth() - 24);
        double nuevo = Math.min(1.0,
                disponibleX / anchoTablero);
        nuevo = Math.max(0.72, nuevo);
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
        titulo.setAlignment(Pos.CENTER);
        titulo.setPrefSize(ANCHO_TARJETA, 48);
        titulo.setLayoutX(x);
        titulo.setLayoutY(Y_ENCABEZADO);
        titulo.setStyle(estiloFase(fase.nombre())
                + "-fx-text-fill:#ffffff;-fx-font-weight:900;"
                + "-fx-font-size:13px;-fx-background-radius:9;"
                + "-fx-border-radius:9;");
        capaTarjetas.getChildren().add(titulo);

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
        Label cabecera = new Label(nombreFase(partido.fase())
                + "  ·  PARTIDO " + partido.orden());
        cabecera.setMaxWidth(Double.MAX_VALUE);
        cabecera.setPadding(new Insets(7, 10, 7, 10));
        cabecera.setStyle(estiloFase(partido.fase())
                + "-fx-text-fill:#ffffff;-fx-font-size:11px;"
                + "-fx-font-weight:900;-fx-background-radius:8 8 0 0;");
        Button intercambiar = new Button("⇅  INTERCAMBIAR");
        intercambiar.setFocusTraversable(false);
        intercambiar.setStyle("-fx-background-color:rgba(7,16,24,0.35);"
                + "-fx-border-color:rgba(255,255,255,0.25);"
                + "-fx-border-radius:6;-fx-background-radius:6;"
                + "-fx-text-fill:#eaf4f8;-fx-font-size:9px;"
                + "-fx-font-weight:900;-fx-padding:3 7;"
                + "-fx-cursor:hand;");
        Button eliminar = new Button("ELIMINAR");
        eliminar.setFocusTraversable(false);
        eliminar.setStyle("-fx-background-color:rgba(80,30,36,0.72);"
                + "-fx-border-color:#a95660;-fx-border-radius:6;"
                + "-fx-background-radius:6;-fx-text-fill:#ffd7da;"
                + "-fx-font-size:9px;-fx-font-weight:900;"
                + "-fx-padding:3 7;-fx-cursor:hand;");
        eliminar.setOnAction(evento -> confirmarEliminacion(partido));
        HBox acciones = new HBox(5, intercambiar, eliminar);
        acciones.setAlignment(Pos.CENTER_RIGHT);
        StackPane encabezado = new StackPane(cabecera, acciones);
        StackPane.setAlignment(cabecera, Pos.CENTER_LEFT);
        StackPane.setAlignment(acciones, Pos.CENTER_RIGHT);
        encabezado.setPadding(new Insets(0, 6, 0, 0));

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
        vs.setStyle("-fx-text-fill:#79a8bb;-fx-font-size:10px;"
                + "-fx-font-weight:900;");
        VBox contenido = new VBox(1, uno, vs, dos);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(9, 11, 10, 11));

        EstadoTarjeta estado = estadoTarjeta(partido);
        VBox caja = new VBox(0, encabezado, contenido);
        caja.setPrefSize(ANCHO_TARJETA, ALTO_TARJETA);
        caja.setMaxSize(ANCHO_TARJETA, ALTO_TARJETA);
        caja.setStyle("-fx-background-color:#102532;"
                + "-fx-border-color:" + estado.color()
                + ";-fx-border-width:" + estado.grosor()
                + ";-fx-border-radius:9;-fx-background-radius:9;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.30),"
                + "10,0.12,0,3);");
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
        label.setWrapText(true);
        label.setTextOverrun(javafx.scene.control.OverrunStyle.CLIP);
        label.setMaxWidth(ANCHO_TARJETA - 24);
        label.setPrefWidth(ANCHO_TARJETA - 24);
        label.setMinHeight(46);
        label.setPrefHeight(46);
        label.setMaxHeight(46);
        String detalle = visible == null ? "Sin asignar" : visible;
        if (nombreCompleto != null && referencia != null) {
            detalle = referencia + " · " + nombreCompleto;
        }
        Tooltip tooltip = new Tooltip(detalle);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(520);
        label.setTooltip(tooltip);
        String estiloNormal = "-fx-text-fill:"
                + (referencia == null ? "#ffd58a" : "#f4fbff")
                + ";-fx-font-size:12.5px;-fx-font-weight:700;"
                + "-fx-background-color:rgba(255,255,255,0.035);"
                + "-fx-border-color:transparent;-fx-border-radius:6;"
                + "-fx-background-radius:6;-fx-padding:4 6;"
                + "-fx-cursor:hand;";
        String estiloActivo = "-fx-text-fill:#ffffff;"
                + "-fx-font-size:12.5px;-fx-font-weight:800;"
                + "-fx-background-color:#214b60;"
                + "-fx-border-color:#91d7f4;-fx-border-radius:6;"
                + "-fx-background-radius:6;-fx-padding:4 6;"
                + "-fx-cursor:hand;";
        label.setStyle(estiloNormal);
        label.setOnMouseEntered(evento -> label.setStyle(estiloActivo));
        label.setOnMouseExited(evento -> label.setStyle(estiloNormal));
        label.setOnMouseClicked(evento -> {
            label.setStyle(estiloActivo);
            seleccionarOrigen(partidoClave, posicion, referencia, label);
        });
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
                aplicar, limpiar, cancelar);

        Set<String> valores = opcionesValidas(partidoClave, actual);
        ComboBox<String> selector = new ComboBox<>(
                FXCollections.observableArrayList(valores));
        selector.setValue(actual);
        selector.setMaxWidth(Double.MAX_VALUE);
        selector.setPrefWidth(620);
        selector.setStyle("-fx-background-color:#0a1922;"
                + "-fx-border-color:#3d6f88;-fx-border-radius:7;"
                + "-fx-background-radius:7;-fx-mark-color:#91d7f4;"
                + "-fx-text-fill:#f4fbff;-fx-padding:3 7;");
        selector.setCellFactory(lista -> celdaOrigen(false));
        selector.setButtonCell(celdaOrigen(true));

        Label ayuda = new Label("Selecciona una opcion habilitada para esta "
                + "fase. Se muestran solamente las parejas que ingresan en "
                + "esta ronda y los ganadores de la ronda anterior.");
        ayuda.setWrapText(true);
        ayuda.setStyle("-fx-text-fill:#d7e4e9;-fx-font-size:13px;");
        VBox contenido = new VBox(12, ayuda, selector);
        contenido.setPadding(new Insets(16));
        contenido.setStyle("-fx-background-color:#0b1821;");
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefWidth(700);
        Dialogos.preparar(dialogo, "dialog-tournament-bracket-manual");
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        final String limpiarMarca = "__LIMPIAR_PLAZA__";
        dialogo.setResultConverter(tipo -> {
            if (tipo == aplicar) return selector.getValue();
            if (tipo == limpiar) return limpiarMarca;
            return null;
        });
        String elegido = dialogo.showAndWait().orElse(null);
        if (elegido != null && alCambiarPlaza != null) {
            String referenciaNueva = limpiarMarca.equals(elegido)
                    ? null : elegido;
            alCambiarPlaza.accept(partidoClave,
                    new CambioPlaza(posicion, referenciaNueva));
            actualizarPlazaEnModelo(partidoClave, posicion,
                    referenciaNueva);
            actualizarEtiquetaPlaza(etiquetaPlaza, referenciaNueva);
            dibujarConexiones(constructor.construir(propuesta));
        }
    }

    private void confirmarEliminacion(PartidoLlaveVisual partido) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Eliminar partido");
        dialogo.setHeaderText("Eliminar " + partido.fase()
                + " (Partido " + partido.orden() + ")");
        ButtonType eliminar = new ButtonType("ELIMINAR PARTIDO",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("CANCELAR",
                ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(eliminar, cancelar);

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
        Label mensaje = new Label(texto
                + "\n\nLa estructura debera quedar valida antes de aplicarla.");
        mensaje.setWrapText(true);
        mensaje.setStyle("-fx-text-fill:#eaf4f8;-fx-font-size:13px;"
                + "-fx-line-spacing:3px;");
        VBox contenido = new VBox(mensaje);
        contenido.setPadding(new Insets(16));
        contenido.setStyle("-fx-background-color:#0b1821;");
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefWidth(610);
        Dialogos.preparar(dialogo, "dialog-error");
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        dialogo.setResultConverter(tipo -> tipo);
        ButtonType resultado = dialogo.showAndWait().orElse(cancelar);
        if (resultado == eliminar && alEliminarPartido != null) {
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
            @Override
            protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : textoOrigen(valor));
                setTextFill(Color.web("#f4fbff"));
                setStyle((boton
                        ? "-fx-background-color:#0a1922;"
                        : isSelected()
                            ? "-fx-background-color:#315f79;"
                            : "-fx-background-color:#102532;")
                        + "-fx-text-fill:#f4fbff;-fx-padding:8 10;");
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
        if (invalida) return new EstadoTarjeta("#e56d76", 2.4, mensaje);
        if (atencion) return new EstadoTarjeta("#e6b85c", 2.2, mensaje);
        return new EstadoTarjeta("#315f79", 1.2, null);
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
                        ? destino.alto() * 0.54
                        : destino.alto() * 0.82);
            double medio = x1 + (x2 - x1) * 0.50;
            Color color = color(conexion.estado());

            Path linea = new Path(new MoveTo(x1, y1),
                    new LineTo(medio, y1),
                    new LineTo(medio, y2),
                    new LineTo(x2 - 11, y2));
            linea.setStroke(color);
            linea.setStrokeWidth(conexion.estado() == EstadoConexionLlave.VALIDA
                    ? 2.4 : 3.2);
            linea.setFill(Color.TRANSPARENT);

            Polygon flecha = new Polygon(x2 - 11, y2 - 5,
                    x2, y2, x2 - 11, y2 + 5);
            flecha.setFill(color);
            capaLineas.getChildren().addAll(linea, flecha);
        }
    }

    private Color color(EstadoConexionLlave estado) {
        return switch (estado) {
            case VALIDA -> Color.web("#65b9d7");
            case SIN_DESTINO -> Color.web("#e6b85c");
            case ORIGEN_INEXISTENTE, DUPLICADA, RONDA_INVALIDA ->
                    Color.web("#e56d76");
        };
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
        String valor = fase == null ? "" : fase.toLowerCase();
        if (valor.equals("final")) return "-fx-background-color:#80651f;";
        if (valor.contains("semifinal")) return "-fx-background-color:#563c78;";
        if (valor.contains("cuartos")) return "-fx-background-color:#245875;";
        if (valor.contains("octavos")) return "-fx-background-color:#23656a;";
        if (valor.contains("dieciseisavos")) return "-fx-background-color:#2d6260;";
        if (valor.contains("acceso")) return "-fx-background-color:#7a4f28;";
        return "-fx-background-color:#344f60;";
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
