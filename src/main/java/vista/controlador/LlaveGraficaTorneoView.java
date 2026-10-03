package vista.controlador;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
import negocio.PropuestaEtapaEliminatoria;
import negocio.visual.ConexionLlaveVisual;
import negocio.visual.EstadoConexionLlave;
import negocio.visual.FaseLlaveVisual;
import negocio.visual.LlaveTorneoVisual;
import negocio.visual.PartidoLlaveVisual;
import servicio.ConstructorLlaveTorneoVisualService;

public class LlaveGraficaTorneoView {
    private static final double ANCHO_TARJETA = 270;
    private static final double ALTO_TARJETA = 126;
    private static final double ANCHO_COLUMNA = 340;
    private static final double MARGEN_X = 28;
    private static final double Y_ENCABEZADO = 18;
    private static final double Y_INICIO = 88;
    private static final double ALTO_MINIMO = 590;
    private static final double SEPARACION_MINIMA = 28;

    private final PropuestaEtapaEliminatoria propuesta;
    private final Map<String, String> nombres;
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
            Map<String, String> nombres) {
        this.propuesta = propuesta;
        this.nombres = new HashMap<>(nombres);
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
            ajusteAutomatico = true;
            ajustar(scroll, contenidoEscalado, porcentaje);
        });
        HBox herramientas = new HBox(8, ajustar, new Separator(),
                reducir, porcentaje, aumentar);
        herramientas.setAlignment(Pos.CENTER_LEFT);
        herramientas.setPadding(new Insets(8, 10, 8, 10));
        herramientas.setStyle("-fx-background-color:#0e202b;"
                + "-fx-border-color:#294351;"
                + "-fx-border-width:0 0 1 0;");

        scroll.viewportBoundsProperty().addListener((o, a, b) -> {
            if (ajusteAutomatico) {
                Platform.runLater(() -> ajustar(scroll,
                        contenidoEscalado, porcentaje));
            }
        });
        Platform.runLater(() -> ajustar(scroll,
                contenidoEscalado, porcentaje));

        BorderPane contenedor = new BorderPane(scroll);
        contenedor.setTop(herramientas);
        contenedor.setPrefHeight(560);
        contenedor.setMinHeight(390);
        contenedor.setStyle("-fx-background-color:#08151e;"
                + "-fx-border-color:#294351;-fx-border-radius:10;"
                + "-fx-background-radius:10;");
        return contenedor;
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
            Label porcentaje) {
        double disponibleX = Math.max(100,
                scroll.getViewportBounds().getWidth() - 24);
        double nuevo = Math.min(1.0,
                disponibleX / anchoTablero);
        nuevo = Math.max(0.72, nuevo);
        cambiarZoom(nuevo, contenedor, porcentaje);
        scroll.setHvalue(0.5);
        scroll.setVvalue(0.0);
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
        Label cabecera = new Label(partido.fase().toUpperCase()
                + "  ·  PARTIDO " + partido.orden());
        cabecera.setMaxWidth(Double.MAX_VALUE);
        cabecera.setPadding(new Insets(7, 10, 7, 10));
        cabecera.setStyle(estiloFase(partido.fase())
                + "-fx-text-fill:#ffffff;-fx-font-size:11px;"
                + "-fx-font-weight:900;-fx-background-radius:8 8 0 0;");

        Label uno = plaza(partido.plaza1().referencia(),
                partido.plaza1().textoVisible());
        Label dos = plaza(partido.plaza2().referencia(),
                partido.plaza2().textoVisible());
        Label vs = new Label("VS");
        vs.setStyle("-fx-text-fill:#79a8bb;-fx-font-size:10px;"
                + "-fx-font-weight:900;");
        VBox contenido = new VBox(3, uno, vs, dos);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(9, 11, 10, 11));

        VBox caja = new VBox(0, cabecera, contenido);
        caja.setPrefSize(ANCHO_TARJETA, ALTO_TARJETA);
        caja.setMaxSize(ANCHO_TARJETA, ALTO_TARJETA);
        caja.setStyle("-fx-background-color:#102532;"
                + "-fx-border-color:#315f79;-fx-border-radius:9;"
                + "-fx-background-radius:9;"
                + "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.30),"
                + "10,0.12,0,3);");
        StackPane tarjeta = new StackPane(caja);
        tarjeta.setPrefSize(ANCHO_TARJETA, ALTO_TARJETA);
        return tarjeta;
    }

    private Label plaza(String referencia, String texto) {
        String visible = nombres.getOrDefault(referencia, texto);
        Label label = new Label(visible == null ? "Sin asignar" : visible);
        label.setWrapText(true);
        label.setMaxWidth(ANCHO_TARJETA - 24);
        label.setPrefHeight(36);
        label.setStyle("-fx-text-fill:"
                + (referencia == null ? "#ffd58a" : "#f4fbff")
                + ";-fx-font-size:12px;-fx-font-weight:700;");
        return label;
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
        return fase == null ? "FASE" : fase.toUpperCase();
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

    private record PosicionTarjeta(
            double x, double y, double ancho, double alto) {
    }
}
