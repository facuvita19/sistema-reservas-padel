package vista.controlador;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import negocio.EstadoInscripcionTorneo;
import negocio.EstadoTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import servicio.AltaAdministrativaInscripcionTorneoService;
import servicio.AltaAdministrativaInscripcionTorneoService.DatosJugador;
import servicio.AltaAdministrativaInscripcionTorneoService.Solicitud;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import vista.Dialogos;
import vista.Navegacion;

public class AltaAdministrativaInscripcionTorneoDialog {
    private final TorneoDAO torneoDAO = new TorneoDAOMySQL();
    private final TorneoCategoriaDAO categoriaDAO = new TorneoCategoriaDAOMySQL();
    private final AltaAdministrativaInscripcionTorneoService service =
            new AltaAdministrativaInscripcionTorneoService();
    private final Dialog<Long> dialogo = new Dialog<>();
    private final ComboBox<Torneo> torneo = new ComboBox<>();
    private final ComboBox<TorneoCategoria> categoria = new ComboBox<>();
    private final ComboBox<EstadoInscripcionTorneo> estado = new ComboBox<>();
    private final TextField n1 = new TextField(), a1 = new TextField(), t1 = new TextField();
    private final TextField n2 = new TextField(), a2 = new TextField(), t2 = new TextField();
    private final TextArea comentarios = new TextArea(), observaciones = new TextArea();
    private Long creadaId;

    public AltaAdministrativaInscripcionTorneoDialog() { construir(); }
    public Long mostrar() { return dialogo.showAndWait().orElse(null); }

    private void construir() {
        dialogo.setTitle("Agregar pareja al torneo");
        dialogo.setHeaderText("Alta administrativa de inscripcion");
        ButtonType guardar = new ButtonType(
                "AGREGAR PAREJA", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().setAll(guardar, cancelar);
        dialogo.getDialogPane().setContent(contenido());
        Dialogos.preparar(dialogo, "dialog-tournament-registration");
        var css = AltaAdministrativaInscripcionTorneoDialog.class
                .getResource("/css/torneo-alta-pareja.css");
        if (css != null) {
            dialogo.getDialogPane().getStylesheets().add(
                    css.toExternalForm());
        }
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        dialogo.setResizable(true);
        dialogo.getDialogPane().setPrefSize(820, 700);

        List<Torneo> administrables = torneoDAO.listarActivos().stream()
                .filter(x -> x.getEstado() == EstadoTorneo.PUBLICADO
                        || x.getEstado() == EstadoTorneo.INSCRIPCION_ABIERTA
                        || x.getEstado() == EstadoTorneo.INSCRIPCION_CERRADA)
                .toList();
        torneo.setItems(FXCollections.observableArrayList(administrables));
        torneo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Torneo x) {
                return x == null ? "" : x.getNombre() + " - " + x.getEstado();
            }
            public Torneo fromString(String x) { return null; }
        });
        categoria.setConverter(new javafx.util.StringConverter<>() {
            public String toString(TorneoCategoria x) {
                return x == null ? "" : x.getNombre() + " - " + x.getRama();
            }
            public TorneoCategoria fromString(String x) { return null; }
        });
        torneo.valueProperty().addListener((o, a, n) -> categoria.setItems(
                FXCollections.observableArrayList(n == null ? List.of()
                        : categoriaDAO.listarActivasPorTorneo(n.getId()))));
        estado.setItems(FXCollections.observableArrayList(
                EstadoInscripcionTorneo.CONFIRMADA,
                EstadoInscripcionTorneo.PENDIENTE,
                EstadoInscripcionTorneo.LISTA_ESPERA));
        estado.setValue(EstadoInscripcionTorneo.CONFIRMADA);

        var boton = dialogo.getDialogPane().lookupButton(guardar);
        boton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            try {
                if (torneo.getValue() == null || categoria.getValue() == null) {
                    throw new IllegalArgumentException(
                            "Selecciona torneo y categoria.");
                }
                if (torneo.getValue().getEstado()
                        != EstadoTorneo.INSCRIPCION_ABIERTA
                        && !Dialogos.confirmar("Alta administrativa",
                        "El torneo no tiene las inscripciones abiertas.\n\n"
                                + "Queres agregar la pareja igualmente?")) {
                    e.consume();
                    return;
                }
                var creada = service.crear(new Solicitud(
                        categoria.getValue().getId(), estado.getValue(),
                        new DatosJugador(n1.getText(), a1.getText(), t1.getText()),
                        new DatosJugador(n2.getText(), a2.getText(), t2.getText()),
                        comentarios.getText(), observaciones.getText(),
                        Navegacion.getUsuarioActual().getId()));
                creadaId = creada.getId();
            } catch (RuntimeException ex) {
                e.consume();
                Dialogos.error("No se pudo agregar la pareja", ex.getMessage());
            }
        });
        dialogo.setResultConverter(x -> x == guardar ? creadaId : null);
    }

    private javafx.scene.control.ScrollPane contenido() {
        prepararControles();
        VBox torneoCard = tarjeta("TORNEO Y CATEGORIA",
                campo("Torneo", torneo), campo("Categoria", categoria),
                campo("Estado inicial", estado));
        VBox responsable = tarjetaJugador("RESPONSABLE", n1, a1, t1);
        VBox segundo = tarjetaJugador("SEGUNDO INTEGRANTE", n2, a2, t2);
        javafx.scene.layout.HBox jugadores = new javafx.scene.layout.HBox(
                14, responsable, segundo);
        javafx.scene.layout.HBox.setHgrow(responsable,
                javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.HBox.setHgrow(segundo,
                javafx.scene.layout.Priority.ALWAYS);
        VBox adicional = tarjeta("INFORMACION ADICIONAL",
                campo("Comentarios", comentarios),
                campo("Observaciones administrativas", observaciones));
        Label intro = new Label(
                "Carga una pareja presencialmente. Si un telefono coincide "
                        + "con un cliente activo, la vinculacion sera automatica.");
        intro.getStyleClass().add("admin-registration-intro");
        intro.setWrapText(true);
        VBox cuerpo = new VBox(14, intro, torneoCard, jugadores, adicional);
        cuerpo.getStyleClass().add("admin-registration-root");
        javafx.scene.control.ScrollPane scroll =
                new javafx.scene.control.ScrollPane(cuerpo);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(
                javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(
                javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.getStyleClass().add("admin-registration-scroll");
        scroll.setPrefViewportHeight(580);
        return scroll;
    }

    private void prepararControles() {
        for (TextField campo : List.of(n1, a1, t1, n2, a2, t2)) {
            campo.getStyleClass().add("admin-registration-field");
        }
        for (ComboBox<?> combo : List.of(torneo, categoria, estado)) {
            combo.getStyleClass().add("admin-registration-combo");
            combo.setMaxWidth(Double.MAX_VALUE);
        }
        for (TextArea area : List.of(comentarios, observaciones)) {
            area.getStyleClass().add("admin-registration-area");
            area.setWrapText(true);
            area.setPrefRowCount(3);
        }
        n1.setPromptText("Nombre"); a1.setPromptText("Apellido");
        t1.setPromptText("Telefono"); n2.setPromptText("Nombre");
        a2.setPromptText("Apellido"); t2.setPromptText("Telefono");
        comentarios.setPromptText("Comentarios opcionales");
        observaciones.setPromptText(
                "Observaciones administrativas opcionales");
    }

    private VBox tarjeta(String titulo, javafx.scene.Node... contenido) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("admin-registration-section-title");
        VBox caja = new VBox(10);
        caja.getChildren().add(encabezado);
        caja.getChildren().addAll(contenido);
        caja.getStyleClass().add("admin-registration-card");
        return caja;
    }

    private VBox tarjetaJugador(String titulo, TextField nombre,
            TextField apellido, TextField telefono) {
        VBox caja = tarjeta(titulo, campo("Nombre", nombre),
                campo("Apellido", apellido), campo("Telefono", telefono));
        caja.setMaxWidth(Double.MAX_VALUE);
        caja.setPrefWidth(360);
        return caja;
    }

    private VBox campo(String etiqueta, Control control) {
        Label texto = new Label(etiqueta.toUpperCase());
        texto.getStyleClass().add("admin-registration-field-label");
        control.setMaxWidth(Double.MAX_VALUE);
        VBox caja = new VBox(5, texto, control);
        return caja;
    }}
