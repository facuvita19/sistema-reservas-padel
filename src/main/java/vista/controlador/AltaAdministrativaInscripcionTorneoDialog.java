package vista.controlador;

import java.util.List;

import dao.ClienteDAOMySQL;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import negocio.Cliente;
import negocio.EstadoInscripcionTorneo;
import negocio.EstadoTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import servicio.AltaAdministrativaInscripcionTorneoService;
import servicio.AltaAdministrativaInscripcionTorneoService.DatosJugador;
import servicio.AltaAdministrativaInscripcionTorneoService.Solicitud;
import vista.Dialogos;
import vista.Navegacion;

public class AltaAdministrativaInscripcionTorneoDialog {
        // dialogos-inscripciones-cierre-visual-v1
        // alta-inscripcion-verde-maximizada-v1
        // alta-inscripcion-moderna-v1
        private final TorneoDAO torneoDAO = new TorneoDAOMySQL();
        private final TorneoCategoriaDAO categoriaDAO = new TorneoCategoriaDAOMySQL();
        private final ClienteDAOMySQL clienteDAO = new ClienteDAOMySQL();
        private final AltaAdministrativaInscripcionTorneoService service = new AltaAdministrativaInscripcionTorneoService();
        private final Dialog<Long> dialogo = new Dialog<>();
        private final ComboBox<Torneo> torneo = new ComboBox<>();
        private final ComboBox<TorneoCategoria> categoria = new ComboBox<>();
        private final ComboBox<EstadoInscripcionTorneo> estado = new ComboBox<>();
        private final TextField n1 = new TextField();
        private final TextField a1 = new TextField();
        private final TextField t1 = new TextField();
        private final TextField n2 = new TextField();
        private final TextField a2 = new TextField();
        private final TextField t2 = new TextField();
        private final TextArea comentarios = new TextArea();
        private final TextArea observaciones = new TextArea();
        private final SeleccionJugador seleccionResponsable = new SeleccionJugador("RESPONSABLE", n1, a1, t1);
        private final SeleccionJugador seleccionPareja = new SeleccionJugador("SEGUNDO INTEGRANTE", n2, a2, t2);
        private Long creadaId;

        public AltaAdministrativaInscripcionTorneoDialog() {
                construir();
        }

        public Long mostrar() {
                return dialogo.showAndWait().orElse(null);
        }

        private void construir() {
                dialogo.setTitle("Agregar inscripción");
                dialogo.setHeaderText("Nueva inscripción administrativa");
                ButtonType guardar = new ButtonType(
                                "AGREGAR INSCRIPCIÓN", ButtonBar.ButtonData.OK_DONE);
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
                dialogo.getDialogPane().setPrefSize(980, 760);
                dialogo.setOnShown(evento -> {
                        Stage ventana = (Stage) dialogo.getDialogPane()
                                        .getScene().getWindow();
                        ventana.setMaximized(true);
                });

                List<Torneo> administrables = torneoDAO.listarActivos().stream()
                                .filter(valor -> valor.getEstado() == EstadoTorneo.PUBLICADO
                                                || valor.getEstado() == EstadoTorneo.INSCRIPCION_ABIERTA
                                                || valor.getEstado() == EstadoTorneo.INSCRIPCION_CERRADA)
                                .toList();
                torneo.setItems(FXCollections.observableArrayList(administrables));
                torneo.setConverter(new javafx.util.StringConverter<>() {
                        public String toString(Torneo valor) {
                                return valor == null ? ""
                                                : valor.getNombre() + " - " + valor.getEstado();
                        }

                        public Torneo fromString(String valor) {
                                return null;
                        }
                });
                categoria.setConverter(new javafx.util.StringConverter<>() {
                        public String toString(TorneoCategoria valor) {
                                return valor == null ? ""
                                                : valor.getNombre() + " - " + valor.getRama();
                        }

                        public TorneoCategoria fromString(String valor) {
                                return null;
                        }
                });
                torneo.valueProperty()
                                .addListener((obs, anterior,
                                                actual) -> categoria.setItems(FXCollections.observableArrayList(
                                                                actual == null ? List.of()
                                                                                : categoriaDAO.listarActivasPorTorneo(
                                                                                                actual.getId()))));
                estado.setItems(FXCollections.observableArrayList(
                                EstadoInscripcionTorneo.CONFIRMADA,
                                EstadoInscripcionTorneo.PENDIENTE,
                                EstadoInscripcionTorneo.LISTA_ESPERA));
                estado.setValue(EstadoInscripcionTorneo.CONFIRMADA);

                var boton = dialogo.getDialogPane().lookupButton(guardar);
                boton.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
                        try {
                                if (torneo.getValue() == null
                                                || categoria.getValue() == null) {
                                        throw new IllegalArgumentException(
                                                        "Selecciona torneo y categoria.");
                                }
                                if (torneo.getValue().getEstado() != EstadoTorneo.INSCRIPCION_ABIERTA
                                                && !Dialogos.confirmar("Alta administrativa",
                                                                "El torneo no tiene las inscripciones abiertas.\n\n"
                                                                                + "Queres agregar la pareja igualmente?")) {
                                        evento.consume();
                                        return;
                                }
                                if (Navegacion.getUsuarioActual() == null) {
                                        throw new IllegalArgumentException(
                                                        "La sesion administrativa finalizo.");
                                }
                                var creada = service.crear(new Solicitud(
                                                categoria.getValue().getId(), estado.getValue(),
                                                seleccionResponsable.datos(),
                                                seleccionPareja.datos(),
                                                comentarios.getText(), observaciones.getText(),
                                                Navegacion.getUsuarioActual().getId()));
                                creadaId = creada.getId();
                        } catch (RuntimeException exception) {
                                evento.consume();
                                Dialogos.error("No se pudo agregar la inscripción",
                                                exception.getMessage());
                        }
                });
                dialogo.setResultConverter(tipo -> tipo == guardar ? creadaId : null);
        }

        private ScrollPane contenido() {
                prepararControles();

                VBox tarjetaTorneo = tarjeta("TORNEO Y CATEGORÍA",
                                camposTorneo());
                VBox responsable = seleccionResponsable.tarjeta();
                VBox segundo = seleccionPareja.tarjeta();
                HBox jugadores = new HBox(12, responsable, segundo);
                jugadores.setAlignment(Pos.TOP_CENTER);
                HBox.setHgrow(responsable, Priority.ALWAYS);
                HBox.setHgrow(segundo, Priority.ALWAYS);

                VBox adicional = tarjeta("INFORMACIÓN ADICIONAL",
                                camposAdicionales());
                Label intro = new Label(
                                "Seleccioná clientes registrados o completá "
                                                + "los datos manualmente. No es necesario volver "
                                                + "a cargar la información de clientes existentes.");
                intro.getStyleClass().add("admin-registration-intro");
                intro.setWrapText(true);

                VBox cuerpo = new VBox(12, intro, tarjetaTorneo,
                                jugadores, adicional);
                cuerpo.getStyleClass().add("admin-registration-root");
                ScrollPane scroll = new ScrollPane(cuerpo);
                scroll.setFitToWidth(true);
                scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scroll.getStyleClass().add("admin-registration-scroll");
                scroll.setPrefViewportHeight(630);
                return scroll;
        }

        private HBox camposTorneo() {
                VBox campoTorneo = campo("Torneo", torneo);
                VBox campoCategoria = campo("Categoría", categoria);
                VBox campoEstado = campo("Estado inicial", estado);
                HBox fila = new HBox(10,
                                campoTorneo, campoCategoria, campoEstado);
                HBox.setHgrow(campoTorneo, Priority.ALWAYS);
                HBox.setHgrow(campoCategoria, Priority.ALWAYS);
                campoTorneo.setMaxWidth(Double.MAX_VALUE);
                campoCategoria.setMaxWidth(Double.MAX_VALUE);
                campoEstado.setPrefWidth(205);
                return fila;
        }

        private HBox camposAdicionales() {
                VBox campoComentarios = campo("Comentarios", comentarios);
                VBox campoObservaciones = campo(
                                "Observación interna", observaciones);
                HBox fila = new HBox(12, campoComentarios, campoObservaciones);
                HBox.setHgrow(campoComentarios, Priority.ALWAYS);
                HBox.setHgrow(campoObservaciones, Priority.ALWAYS);
                campoComentarios.setMaxWidth(Double.MAX_VALUE);
                campoObservaciones.setMaxWidth(Double.MAX_VALUE);
                return fila;
        }

        private void prepararControles() {
                for (TextField actual : List.of(n1, a1, t1, n2, a2, t2)) {
                        actual.getStyleClass().add("admin-registration-field");
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
                n1.setPromptText("Nombre");
                a1.setPromptText("Apellido");
                t1.setPromptText("Telefono");
                n2.setPromptText("Nombre");
                a2.setPromptText("Apellido");
                t2.setPromptText("Telefono");
                comentarios.setPromptText("Comentarios opcionales");
                observaciones.setPromptText(
                                "Información interna opcional");
        }

        private VBox tarjeta(String titulo, javafx.scene.Node... contenido) {
                Label encabezado = new Label(titulo);
                encabezado.getStyleClass().add("admin-registration-section-title");
                VBox caja = new VBox(9);
                caja.getChildren().add(encabezado);
                caja.getChildren().addAll(contenido);
                caja.getStyleClass().add("admin-registration-card");
                return caja;
        }

        private VBox campo(String etiqueta, Control control) {
                Label texto = new Label(etiqueta.toUpperCase());
                texto.getStyleClass().add("admin-registration-field-label");
                control.setMaxWidth(Double.MAX_VALUE);
                return new VBox(5, texto, control);
        }

        private void animarBoton(Button boton) {
                boton.getStyleClass().add("admin-registration-animated-button");
                boton.setOnMouseEntered(evento -> animarEscala(boton, 1.018, 110));
                boton.setOnMouseExited(evento -> animarEscala(boton, 1.0, 125));
                boton.setOnMousePressed(evento -> animarEscala(boton, .975, 70));
                boton.setOnMouseReleased(evento -> animarEscala(
                                boton, boton.isHover() ? 1.018 : 1.0, 90));
        }

        private void animarEscala(
                        Button boton, double escala, double milisegundos) {
                ScaleTransition transicion = new ScaleTransition(
                                Duration.millis(milisegundos), boton);
                transicion.setToX(escala);
                transicion.setToY(escala);
                transicion.play();
        }

        private final class SeleccionJugador {
                private final String titulo;
                private final TextField nombre;
                private final TextField apellido;
                private final TextField telefono;
                private final Label clienteNombre = new Label();
                private final Label clienteDetalle = new Label();
                private final VBox resumenCliente = new VBox(4);
                private final Button buscar = new Button("BUSCAR CLIENTE");
                private final Button quitar = new Button("QUITAR SELECCION");
                private Cliente cliente;

                private SeleccionJugador(String titulo, TextField nombre,
                                TextField apellido, TextField telefono) {
                        this.titulo = titulo;
                        this.nombre = nombre;
                        this.apellido = apellido;
                        this.telefono = telefono;
                }

                private VBox tarjeta() {
                        buscar.getStyleClass().add("admin-registration-search-button");
                        quitar.getStyleClass().add("admin-registration-clear-button");
                        quitar.getStyleClass().add("admin-registration-remove-selection");
                        buscar.setOnAction(evento -> buscarCliente());
                        quitar.setOnAction(evento -> seleccionar(null));
                        animarBoton(buscar);
                        animarBoton(quitar);

                        clienteNombre.getStyleClass().add(
                                        "admin-registration-selected-name");
                        clienteDetalle.getStyleClass().add(
                                        "admin-registration-selected-detail");
                        clienteDetalle.setWrapText(true);
                        resumenCliente.getChildren().addAll(
                                        clienteNombre, clienteDetalle, quitar);
                        resumenCliente.getStyleClass().add(
                                        "admin-registration-selected-card");
                        resumenCliente.setVisible(false);
                        resumenCliente.setManaged(false);

                        Label alternativa = new Label(
                                        "O completa los datos manualmente");
                        alternativa.getStyleClass().add("admin-registration-hint");
                        Region lineaIzquierda = new Region();
                        Region lineaDerecha = new Region();
                        lineaIzquierda.getStyleClass().add(
                                        "admin-registration-divider-line");
                        lineaDerecha.getStyleClass().add(
                                        "admin-registration-divider-line");
                        HBox.setHgrow(lineaIzquierda, Priority.ALWAYS);
                        HBox.setHgrow(lineaDerecha, Priority.ALWAYS);
                        HBox divisor = new HBox(8,
                                        lineaIzquierda, alternativa, lineaDerecha);
                        divisor.setAlignment(Pos.CENTER);

                        HBox accionesCliente = new HBox(8, buscar);
                        accionesCliente.setAlignment(Pos.CENTER_LEFT);
                        VBox caja = AltaAdministrativaInscripcionTorneoDialog.this.tarjeta(
                                        titulo,
                                        accionesCliente,
                                        resumenCliente,
                                        divisor,
                                        campo("Nombre", nombre),
                                        campo("Apellido", apellido),
                                        campo("Teléfono", telefono));
                        caja.setMaxWidth(Double.MAX_VALUE);
                        caja.setPrefWidth(420);
                        return caja;
                }

                private void buscarCliente() {
                        List<Cliente> clientes = clienteDAO.listar().stream()
                                        .filter(Cliente::isActivo)
                                        .filter(valor -> otraSeleccion().cliente == null
                                                        || valor.getId() != otraSeleccion().cliente.getId())
                                        .toList();
                        Cliente elegido = new BusquedaClienteTorneoDialog(
                                        clientes, titulo, "SELECCIONAR").mostrar().orElse(null);
                        if (elegido != null)
                                seleccionar(elegido);
                }

                private SeleccionJugador otraSeleccion() {
                        return this == seleccionResponsable
                                        ? seleccionPareja
                                        : seleccionResponsable;
                }

                private void seleccionar(Cliente valor) {
                        cliente = valor;
                        boolean seleccionado = valor != null;
                        if (seleccionado) {
                                nombre.setText(valor.getNombre());
                                apellido.setText(valor.getApellido());
                                telefono.setText(valor.getTelefono());
                                clienteNombre.setText(valor.getNombreCompleto());
                                clienteDetalle.setText("Cliente #" + valor.getId()
                                                + " · Documento: " + texto(valor.getDocumento())
                                                + " · Telefono: " + texto(valor.getTelefono()));
                                buscar.setText("CAMBIAR CLIENTE");
                        } else {
                                nombre.clear();
                                apellido.clear();
                                telefono.clear();
                                clienteNombre.setText("");
                                clienteDetalle.setText("");
                                buscar.setText("BUSCAR CLIENTE");
                        }
                        nombre.setDisable(seleccionado);
                        apellido.setDisable(seleccionado);
                        telefono.setDisable(seleccionado);
                        resumenCliente.setVisible(seleccionado);
                        resumenCliente.setManaged(seleccionado);
                }

                private DatosJugador datos() {
                        return new DatosJugador(
                                        cliente == null ? null : cliente.getId(),
                                        nombre.getText(), apellido.getText(), telefono.getText());
                }

                private String texto(String valor) {
                        return valor == null || valor.isBlank() ? "-" : valor;
                }
        }
}
