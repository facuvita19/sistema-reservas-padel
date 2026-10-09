package vista.controlador;

import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import dao.ClienteDAOMySQL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import negocio.Cliente;
import negocio.EstadoInscripcionTorneo;
import servicio.ConsultaInscripcionTorneoService;
import servicio.ConsultaInscripcionTorneoService.InscripcionResumen;
import servicio.ConsultaInscripcionTorneoService.JugadorResumen;
import servicio.GestionInscripcionTorneoService;
import servicio.GestionVinculacionJugadorTorneoService;
import servicio.WhatsAppService;
import util.FormateadorMoneda;
import vista.Dialogos;
import vista.Navegacion;

public class TorneosInscripcionesController {
    // inscripciones-texto-en-espera-v2
    // inscripciones-animaciones-panel-v1
    // inscripciones-principal-pulido-final-v1
    // inscripciones-estado-inicial-v2
    // inscripciones-principal-moderna-v3
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ConsultaInscripcionTorneoService consultaService =
            new ConsultaInscripcionTorneoService();
    private final WhatsAppService whatsAppService = new WhatsAppService();
    private final GestionInscripcionTorneoService gestionService =
            new GestionInscripcionTorneoService();
    private final GestionVinculacionJugadorTorneoService vinculacionService =
            new GestionVinculacionJugadorTorneoService();
    private final ClienteDAOMySQL clienteDAO = new ClienteDAOMySQL();
    private final ObservableList<InscripcionResumen> inscripciones =
            FXCollections.observableArrayList();
    private FilteredList<InscripcionResumen> filtradas;
    private InscripcionResumen seleccionada;
    private boolean filtroFinalizadasActivo;

    @FXML private TextField campoBuscar;
    @FXML private ComboBox<EstadoInscripcionTorneo> filtroEstado;
    @FXML private TableView<InscripcionResumen> tablaInscripciones;
    @FXML private TableColumn<InscripcionResumen, Long> columnaId;
    @FXML private TableColumn<InscripcionResumen, String> columnaTorneo;
    @FXML private TableColumn<InscripcionResumen, String> columnaCategoria;
    @FXML private TableColumn<InscripcionResumen, String> columnaResponsable;
    @FXML private TableColumn<InscripcionResumen, String> columnaPareja;
    @FXML private TableColumn<InscripcionResumen, EstadoInscripcionTorneo> columnaEstado;
    @FXML private TableColumn<InscripcionResumen, LocalDateTime> columnaFecha;
    @FXML private Label etiquetaTotal;
    @FXML private Label etiquetaPendientes;
    @FXML private Label etiquetaConfirmadas;
    @FXML private Label etiquetaEspera;
    @FXML private Label etiquetaFinalizadas;
    @FXML private javafx.scene.layout.VBox metricaTotal;
    @FXML private javafx.scene.layout.VBox metricaPendientes;
    @FXML private javafx.scene.layout.VBox metricaConfirmadas;
    @FXML private javafx.scene.layout.VBox metricaEspera;
    @FXML private javafx.scene.layout.VBox metricaFinalizadas;
    @FXML private Label etiquetaMensaje;
    @FXML private Label detalleTitulo;
    @FXML private Label detalleTorneo;
    @FXML private Label detalleCategoria;
    @FXML private Label detalleEstado;
    @FXML private Label detalleOrigen;
    @FXML private Label detalleFecha;
    @FXML private Label detallePrecio;
    @FXML private Label responsableNombre;
    @FXML private Label responsableTelefono;
    @FXML private Label responsableVinculacion;
    @FXML private Label parejaNombre;
    @FXML private Label parejaTelefono;
    @FXML private Label parejaVinculacion;
    @FXML private TextArea detalleComentarios;
    @FXML private TextArea detalleObservaciones;
    @FXML private Button botonWhatsappResponsable;
    @FXML private Button botonWhatsappPareja;
    @FXML private Button botonVincularResponsable;
    @FXML private Button botonDesvincularResponsable;
    @FXML private Button botonVincularPareja;
    @FXML private Button botonDesvincularPareja;
    @FXML private Button botonConfirmar;
    @FXML private Button botonListaEspera;
    @FXML private Button botonRechazar;
    @FXML private Button botonCancelar;
    @FXML private Button botonGuardarObservacion;
    private String observacionOriginal = "";

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFiltros();
        campoBuscar.setOnKeyPressed(evento -> {
            if (evento.getCode() == KeyCode.ESCAPE) limpiarFiltros();
        });
        detalleObservaciones.textProperty().addListener((o, a, actual) -> actualizarBotonObservacion());
        botonRechazar.setTooltip(new Tooltip("Deniega la solicitud. La pareja no será incorporada."));
        botonCancelar.setTooltip(new Tooltip("Cancela administrativamente la inscripción y conserva el registro histórico."));
        botonGuardarObservacion.setTooltip(new Tooltip("Modificá la observación interna para habilitar el guardado."));
        // tooltips-acciones-inscripciones-v2
        cargarInscripciones();
    }

    private void configurarTabla() {
        columnaId.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleLongProperty(
                        datos.getValue().id()).asObject());
        columnaTorneo.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleStringProperty(
                        datos.getValue().torneo()));
        columnaCategoria.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleStringProperty(datos.getValue().categoriaCompleta()));
        columnaResponsable.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleStringProperty(datos.getValue().responsableNombre()));
        columnaPareja.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleStringProperty(datos.getValue().parejaNombre()));
        columnaEstado.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        datos.getValue().estado()));
        columnaFecha.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        datos.getValue().fechaSolicitud()));
        columnaFecha.setCellFactory(columna -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : valor.format(FECHA_HORA));
            }
        });
        configurarCeldaTexto(columnaTorneo);
        configurarCeldaTexto(columnaCategoria);
        configurarCeldaTexto(columnaResponsable);
        configurarCeldaTexto(columnaPareja);
        configurarAnchosProporcionales();
        columnaEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(
                    EstadoInscripcionTorneo estado, boolean vacia) {
                super.updateItem(estado, vacia);
                setText(vacia || estado == null ? null : textoEstado(estado));
                getStyleClass().removeAll(
                        "state-pending", "state-confirmed", "state-wait",
                        "state-rejected", "state-cancelled");
                if (!vacia && estado != null) {
                    getStyleClass().add(switch (estado) {
                        case PENDIENTE -> "state-pending";
                        case CONFIRMADA -> "state-confirmed";
                        case LISTA_ESPERA -> "state-wait";
                        case RECHAZADA -> "state-rejected";
                        case CANCELADA -> "state-cancelled";
                    });
                }
            }
        });
        tablaInscripciones.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) mostrarDetalle(actual);
                });
    }

    private void configurarAnchosProporcionales() {
        vincularAncho(columnaId, 0.04);
        vincularAncho(columnaTorneo, 0.15);
        vincularAncho(columnaCategoria, 0.14);
        vincularAncho(columnaResponsable, 0.20);
        vincularAncho(columnaPareja, 0.21);
        vincularAncho(columnaEstado, 0.11);
        vincularAncho(columnaFecha, 0.15);
    }

    private void vincularAncho(
            TableColumn<InscripcionResumen, ?> columna,
            double proporcion) {
        columna.prefWidthProperty().bind(
                tablaInscripciones.widthProperty()
                        .subtract(14)
                        .multiply(proporcion));
        columna.setResizable(false);
        columna.setReorderable(false);
    }

    private void configurarCeldaTexto(
            TableColumn<InscripcionResumen, String> columna) {
        columna.setCellFactory(valor -> new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vacia) {
                super.updateItem(texto, vacia);
                setText(vacia ? null : texto);
                setTooltip(vacia || texto == null || texto.isBlank()
                        ? null : new Tooltip(texto));
            }
        });
    }

    private void configurarFiltros() {
        filtroEstado.setItems(FXCollections.observableArrayList(EstadoInscripcionTorneo.values()));
        filtradas = new FilteredList<>(inscripciones, valor -> true);
        tablaInscripciones.setItems(filtradas);
        campoBuscar.textProperty().addListener((obs, ant, act) -> aplicarFiltros());
        filtroEstado.valueProperty().addListener((obs, ant, act) -> aplicarFiltros());
    }

    @FXML
    private void cargarInscripciones() {
        try {
            inscripciones.setAll(consultaService.listar());
            aplicarFiltros();
            actualizarMetricas();
            etiquetaMensaje.setText(inscripciones.size()
                    + (inscripciones.size() == 1
                            ? " inscripción encontrada"
                            : " inscripciones encontradas"));
            if (seleccionada != null) {
                seleccionarPorId(seleccionada.id());
            } else if (!filtradas.isEmpty()) {
                tablaInscripciones.getSelectionModel().selectFirst();
                javafx.application.Platform.runLater(() -> tablaInscripciones.scrollTo(tablaInscripciones.getSelectionModel().getSelectedIndex()));
            }
        } catch (RuntimeException exception) {
            etiquetaMensaje.setText(exception.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (filtradas == null) return;
        String texto = campoBuscar.getText() == null ? ""
                : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        EstadoInscripcionTorneo estado = filtroEstado.getValue();
        filtradas.setPredicate(valor -> (!filtroFinalizadasActivo || valor.estado().esFinal())
                && (estado == null || valor.estado() == estado)
                && (texto.isBlank()
                || contiene(String.valueOf(valor.id()), texto)
                || contiene(valor.torneo(), texto)
                || contiene(valor.categoriaCompleta(), texto)
                || contiene(valor.responsableNombre(), texto)
                || contiene(valor.parejaNombre(), texto)
                || contiene(valor.responsable().telefono(), texto)
                || contiene(valor.pareja().telefono(), texto)));
        mantenerSeleccionVisible();
        actualizarMensajeResultados();
    }

    private void actualizarMensajeResultados() {
        if (filtradas == null) return;
        String texto = filtradas.size()
                + (filtradas.size() == 1
                        ? " inscripción encontrada" : " inscripciones encontradas");
        if (filtroEstado.getValue() != null) texto += " · " + textoEstado(filtroEstado.getValue());
        String busqueda = campoBuscar.getText() == null ? "" : campoBuscar.getText().trim();
        if (!busqueda.isBlank()) texto += " para \"" + busqueda + "\"";
        etiquetaMensaje.setText(texto);
    }

    private void mantenerSeleccionVisible() {
        if (filtradas == null || tablaInscripciones == null) return;
        if (filtradas.isEmpty()) {
            tablaInscripciones.getSelectionModel().clearSelection();
            return;
        }
        if (seleccionada != null && filtradas.contains(seleccionada)) {
            tablaInscripciones.getSelectionModel().select(seleccionada);
            return;
        }
        tablaInscripciones.getSelectionModel().selectFirst();
        tablaInscripciones.scrollTo(0);
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    @FXML private void agregarPareja() {
        Long id = new AltaAdministrativaInscripcionTorneoDialog().mostrar();
        if (id != null) {
            cargarInscripciones();
            seleccionarPorId(id);
            Dialogos.exito(
             "Pareja agregada",
            "La inscripción administrativa fue creada correctamente.");
        }
    }

    @FXML private void limpiarFiltros() {
        campoBuscar.clear();
        filtroFinalizadasActivo = false;
        actualizarMetricaActiva(null);
        filtroEstado.getSelectionModel().clearSelection();
    }
    @FXML private void filtrarTotal() { aplicarFiltroMetrica(null, false, metricaTotal); }
    @FXML private void filtrarPendientes() { aplicarFiltroMetrica(EstadoInscripcionTorneo.PENDIENTE, false, metricaPendientes); }
    @FXML private void filtrarConfirmadas() { aplicarFiltroMetrica(EstadoInscripcionTorneo.CONFIRMADA, false, metricaConfirmadas); }
    @FXML private void filtrarEspera() { aplicarFiltroMetrica(EstadoInscripcionTorneo.LISTA_ESPERA, false, metricaEspera); }
    @FXML private void filtrarFinalizadas() { aplicarFiltroMetrica(null, true, metricaFinalizadas); }

    private void aplicarFiltroMetrica(EstadoInscripcionTorneo estado, boolean finales, javafx.scene.layout.VBox tarjeta) {
        boolean repetir = tarjeta != null && tarjeta.getStyleClass().contains("metric-filter-active-v2");
        filtroFinalizadasActivo = !repetir && finales;
        filtroEstado.setValue(repetir || finales ? null : estado);
        actualizarMetricaActiva(repetir ? null : tarjeta);
        aplicarFiltros();
    }

    private void actualizarMetricaActiva(javafx.scene.layout.VBox activa) {
        for (javafx.scene.layout.VBox tarjeta : java.util.List.of(
                metricaTotal, metricaPendientes, metricaConfirmadas, metricaEspera, metricaFinalizadas)) {
            if (tarjeta != null) tarjeta.getStyleClass().remove("metric-filter-active-v2");
        }
        if (activa != null) activa.getStyleClass().add("metric-filter-active-v2");
    }



    private void actualizarMetricas() {
        etiquetaTotal.setText(String.valueOf(inscripciones.size()));
        etiquetaPendientes.setText(String.valueOf(contar(EstadoInscripcionTorneo.PENDIENTE)));
        etiquetaConfirmadas.setText(String.valueOf(contar(EstadoInscripcionTorneo.CONFIRMADA)));
        etiquetaEspera.setText(String.valueOf(contar(EstadoInscripcionTorneo.LISTA_ESPERA)));
        etiquetaFinalizadas.setText(String.valueOf(
                contar(EstadoInscripcionTorneo.RECHAZADA)
                + contar(EstadoInscripcionTorneo.CANCELADA)));
    }

    private long contar(EstadoInscripcionTorneo estado) {
        return inscripciones.stream().filter(i -> i.estado() == estado).count();
    }

    private void mostrarDetalle(InscripcionResumen valor) {
        seleccionada = valor;
        detalleTitulo.setText("Inscripcion #" + valor.id());
        detalleTorneo.setText(valor.torneo());
        detalleCategoria.setText(valor.categoriaCompleta());
        detalleEstado.setText(textoEstado(valor.estado()));
        aplicarEstiloEstadoDetalle(valor.estado());
        detalleOrigen.setText(valor.origen());
        detalleFecha.setText(valor.fechaSolicitud() == null ? "-" : valor.fechaSolicitud().format(FECHA_HORA));
        detallePrecio.setText(valor.precioInscripcion() == null
                || valor.precioInscripcion().signum() == 0
                        ? "Sin cargo"
                        : FormateadorMoneda.pesos(valor.precioInscripcion()));
        cargarJugador(valor.responsable(), responsableNombre, responsableTelefono,
                responsableVinculacion, botonWhatsappResponsable,
                botonVincularResponsable, botonDesvincularResponsable);
        cargarJugador(valor.pareja(), parejaNombre, parejaTelefono,
                parejaVinculacion, botonWhatsappPareja,
                botonVincularPareja, botonDesvincularPareja);
        detalleComentarios.setText(valor.comentarios() == null ? "" : valor.comentarios());
        observacionOriginal = valor.observacionesAdministrativas() == null ? "" : valor.observacionesAdministrativas();
        detalleObservaciones.setText(observacionOriginal);
        actualizarAcciones(valor.estado());
    }

    private String textoEstado(EstadoInscripcionTorneo estado) {
        if (estado == null) return "";
        return estado == EstadoInscripcionTorneo.LISTA_ESPERA
                ? "En espera" : estado.toString();
    }

    private void aplicarEstiloEstadoDetalle(
            EstadoInscripcionTorneo estado) {
        detalleEstado.getStyleClass().removeAll(
                "detail-state-pending", "detail-state-confirmed",
                "detail-state-wait", "detail-state-rejected",
                "detail-state-cancelled");
        if (estado == null) return;
        detalleEstado.getStyleClass().add(switch (estado) {
            case PENDIENTE -> "detail-state-pending";
            case CONFIRMADA -> "detail-state-confirmed";
            case LISTA_ESPERA -> "detail-state-wait";
            case RECHAZADA -> "detail-state-rejected";
            case CANCELADA -> "detail-state-cancelled";
        });
    }

    private void cargarJugador(JugadorResumen jugador, Label nombre, Label telefono,
            Label vinculacion, Button whatsapp, Button vincular, Button desvincular) {
        nombre.setText(jugador.nombreCompleto());
        telefono.setText(formatearTelefonoVisible(jugador.telefono()));
        vinculacion.setText(jugador.vinculado()
                ? "Cliente registrado #" + jugador.clienteId()
                : "Sin vincular");
        vinculacion.getStyleClass().removeAll("link-status-linked", "link-status-pending");
        vinculacion.getStyleClass().add(jugador.vinculado()
                ? "link-status-linked" : "link-status-pending");
        whatsapp.setVisible(jugador.telefono() != null && !jugador.telefono().isBlank());
        whatsapp.setManaged(whatsapp.isVisible());
        vincular.setText(jugador.vinculado()
                ? "CAMBIAR VINCULACIÓN" : "VINCULAR CLIENTE");
        vincular.getStyleClass().removeAll("tournament-link-primary", "tournament-link-secondary");
        vincular.getStyleClass().add(jugador.vinculado()
                ? "tournament-link-secondary" : "tournament-link-primary");
        desvincular.setVisible(jugador.vinculado());
        desvincular.setManaged(jugador.vinculado());
    }

    private String formatearTelefonoVisible(String telefono) {
        if (telefono == null) return "";
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.length() == 10 && digitos.startsWith("11")) {
            return digitos.substring(0, 2) + " "
                    + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }
        return telefono;
    }

    private void seleccionarPorId(long id) {
        inscripciones.stream().filter(i -> i.id() == id).findFirst().ifPresent(valor -> {
            tablaInscripciones.getSelectionModel().select(valor);
            tablaInscripciones.scrollTo(valor);
        });
    }

    private void actualizarAcciones(EstadoInscripcionTorneo estado) {
        boolean pendiente = estado == EstadoInscripcionTorneo.PENDIENTE;
        boolean espera = estado == EstadoInscripcionTorneo.LISTA_ESPERA;
        boolean confirmada = estado == EstadoInscripcionTorneo.CONFIRMADA;
        boolean gestionable = pendiente || espera || confirmada;
        mostrarAccion(botonConfirmar, pendiente || espera);
        mostrarAccion(botonListaEspera, pendiente);
        mostrarAccion(botonRechazar, pendiente || espera);
        mostrarAccion(botonCancelar, gestionable);
        detalleObservaciones.setEditable(gestionable);
        actualizarBotonObservacion();
    }

    private void mostrarAccion(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
        boton.setDisable(!visible);
    }

    private void actualizarBotonObservacion() {
        if (botonGuardarObservacion == null) return;
        String actual = detalleObservaciones.getText() == null ? "" : detalleObservaciones.getText();
        botonGuardarObservacion.setDisable(seleccionada == null
                || !detalleObservaciones.isEditable()
                || actual.equals(observacionOriginal));
    }

    @FXML
    private void guardarObservacion() {
        if (seleccionada == null || Navegacion.getUsuarioActual() == null) return;
        try {
            gestionService.guardarObservaciones(seleccionada.id(),
                    Navegacion.getUsuarioActual().getId(),
                    detalleObservaciones.getText());
            observacionOriginal = detalleObservaciones.getText() == null
                    ? "" : detalleObservaciones.getText().trim();
            actualizarBotonObservacion();
            etiquetaMensaje.setText("Observación interna guardada correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception, "No se pudo guardar la observación.");
        }
    }

    @FXML private void confirmarInscripcion() {
        // confirmar-inscripcion-contextual-v1
        if (seleccionada == null) {
            etiquetaMensaje.setText("Seleccioná una inscripción para confirmarla.");
            return;
        }
        String estadoActual = textoEstado(seleccionada.estado());
        String mensaje = "La inscripción #" + seleccionada.id()
                + " pasará de " + estadoActual + " a Confirmada.\n\n"
                + "Torneo: " + seleccionada.torneo() + "\n"
                + "Categoría: " + seleccionada.categoriaCompleta() + "\n"
                + "Pareja: " + seleccionada.responsableNombre()
                + " / " + seleccionada.parejaNombre() + "\n\n"
                + "La confirmación ocupará un cupo de la categoría.\n"
                + "¿Querés confirmar la inscripción?";
        if (Dialogos.confirmar("Confirmar inscripción", mensaje)) {
            gestionar(EstadoInscripcionTorneo.CONFIRMADA);
        }
    }

    @FXML private void enviarAListaEspera() {
        gestionar(EstadoInscripcionTorneo.LISTA_ESPERA);
    }

    @FXML private void rechazarInscripcion() {
        if (confirmarAccion("Rechazar inscripcion",
                "La solicitud quedara rechazada y no podra reabrirse.")) {
            gestionar(EstadoInscripcionTorneo.RECHAZADA);
        }
    }

    @FXML private void cancelarInscripcion() {
        if (confirmarAccion("Cancelar inscripcion",
                "La inscripcion quedara cancelada y no podra reabrirse.")) {
            gestionar(EstadoInscripcionTorneo.CANCELADA);
        }
    }

    private void gestionar(EstadoInscripcionTorneo nuevoEstado) {
        if (seleccionada == null) {
            etiquetaMensaje.setText("Selecciona una inscripcion para gestionarla.");
            return;
        }
        if (Navegacion.getUsuarioActual() == null
                || Navegacion.getUsuarioActual().getId() <= 0) {
            etiquetaMensaje.setText("No se pudo identificar al usuario administrativo.");
            return;
        }

        long id = seleccionada.id();
        long usuarioId = Navegacion.getUsuarioActual().getId();
        String observaciones = detalleObservaciones.getText();

        try {
            switch (nuevoEstado) {
                case CONFIRMADA -> gestionService.confirmar(
                        id, usuarioId, observaciones);
                case LISTA_ESPERA -> gestionService.enviarAListaEspera(
                        id, usuarioId, observaciones);
                case RECHAZADA -> gestionService.rechazar(
                        id, usuarioId, observaciones);
                case CANCELADA -> gestionService.cancelar(
                        id, usuarioId, observaciones);
                default -> throw new IllegalArgumentException(
                        "El estado seleccionado no se puede gestionar desde esta pantalla.");
            }
            cargarInscripciones();
            seleccionarPorId(id);
            etiquetaMensaje.setText(
                    "Inscripcion #" + id + " actualizada correctamente.");
        } catch (RuntimeException exception) {
            String mensaje = exception.getMessage();
            etiquetaMensaje.setText(mensaje == null || mensaje.isBlank()
                    ? "No se pudo actualizar la inscripcion."
                    : mensaje);
        }
    }

    private boolean confirmarAccion(
            String titulo,
            String mensaje) {
        if (seleccionada == null) return false;
        String detalle = "Inscripción #" + seleccionada.id()
                + "\nTorneo: " + seleccionada.torneo()
                + "\nCategoría: " + seleccionada.categoriaCompleta()
                + "\nPareja: " + seleccionada.responsableNombre()
                + " / " + seleccionada.parejaNombre()
                + "\n\n" + mensaje;
        return Dialogos.confirmarPeligro(titulo, detalle);
    }
    @FXML private void vincularResponsable() {
        vincular(seleccionada == null ? null : seleccionada.responsable());
    }

    @FXML private void vincularPareja() {
        vincular(seleccionada == null ? null : seleccionada.pareja());
    }

    @FXML private void desvincularResponsable() {
        desvincular(seleccionada == null ? null : seleccionada.responsable());
    }

    @FXML private void desvincularPareja() {
        desvincular(seleccionada == null ? null : seleccionada.pareja());
    }

    private void vincular(JugadorResumen jugador) {
        if (jugador == null || seleccionada == null) {
            etiquetaMensaje.setText("Selecciona una inscripcion y un integrante.");
            return;
        }
        try {
            BusquedaClienteTorneoDialog dialogo = new BusquedaClienteTorneoDialog(
                    clienteDAO.listar(), jugador.nombreCompleto());
            Cliente cliente = dialogo.mostrar().orElse(null);
            if (cliente == null) return;

            String mensaje = "Vincular a " + jugador.nombreCompleto()
                    + " con el cliente #" + cliente.getId() + " - "
                    + cliente.getNombreCompleto() + "?";
            if (!confirmarAccion("Vincular cliente", mensaje)) return;

            long inscripcionId = seleccionada.id();
            vinculacionService.vincularManualmente(
                    jugador.jugadorId(), cliente.getId());
            cargarInscripciones();
            seleccionarPorId(inscripcionId);
            mostrarExito("Vinculación completada", "Cliente vinculado correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception, "No se pudo vincular el cliente.");
        }
    }

    private void desvincular(JugadorResumen jugador) {
        if (jugador == null || !jugador.vinculado() || seleccionada == null) return;
        if (!confirmarAccion("Desvincular cliente",
                "Quitar la vinculacion de " + jugador.nombreCompleto() + "?")) return;
        try {
            long inscripcionId = seleccionada.id();
            vinculacionService.desvincular(jugador.jugadorId());
            cargarInscripciones();
            seleccionarPorId(inscripcionId);
            mostrarExito("Desvinculación completada", "Cliente desvinculado correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception, "No se pudo desvincular el cliente.");
        }
    }

    private void mostrarError(
            RuntimeException exception,
            String alternativo) {
        String mensaje = exception.getMessage();
        String texto = mensaje == null || mensaje.isBlank()
                ? alternativo
                : mensaje;

        etiquetaMensaje.setText(texto);
        Dialogos.error(
                "No se pudo completar la operacion",
                texto);
    }

    private void mostrarExito(
            String titulo,
            String mensaje) {
        etiquetaMensaje.setText(mensaje);
        Dialogos.exito(titulo, mensaje);
    }
    @FXML private void abrirWhatsappResponsable() { abrirWhatsapp(seleccionada == null ? null : seleccionada.responsable()); }
    @FXML private void abrirWhatsappPareja() { abrirWhatsapp(seleccionada == null ? null : seleccionada.pareja()); }

    private void abrirWhatsapp(JugadorResumen jugador) {
        if (jugador == null) return;
        try {
            String mensaje = "Hola " + jugador.nombreCompleto()
                    + ", nos comunicamos desde Padel Reservas por la inscripcion al torneo "
                    + seleccionada.torneo() + " - " + seleccionada.categoriaCompleta() + ".";
            URI enlace = whatsAppService.crearEnlace(jugador.telefono(), mensaje);
            if (!Desktop.isDesktopSupported()) throw new IllegalStateException("No se pudo abrir el navegador.");
            Desktop.getDesktop().browse(enlace);
        } catch (Exception exception) {
            etiquetaMensaje.setText(exception.getMessage());
        }
    }

    @FXML private void volver() {
        Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
    }
    // inscripciones-torneos-integral-v3
    // inscripciones-tabla-proporcional-v1
}
