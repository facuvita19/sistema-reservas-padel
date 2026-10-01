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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import negocio.Cliente;
import negocio.EstadoInscripcionTorneo;
import servicio.ConsultaInscripcionTorneoService;
import servicio.ConsultaInscripcionTorneoService.InscripcionResumen;
import servicio.ConsultaInscripcionTorneoService.JugadorResumen;
import servicio.GestionInscripcionTorneoService;
import servicio.GestionVinculacionJugadorTorneoService;
import servicio.WhatsAppService;
import util.FormateadorMoneda;
import vista.Navegacion;

public class TorneosInscripcionesController {
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

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFiltros();
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
        tablaInscripciones.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) mostrarDetalle(actual);
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
            etiquetaMensaje.setText(inscripciones.size() + " inscripcion(es) cargada(s).");
            if (seleccionada != null) seleccionarPorId(seleccionada.id());
        } catch (RuntimeException exception) {
            etiquetaMensaje.setText(exception.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (filtradas == null) return;
        String texto = campoBuscar.getText() == null ? ""
                : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        EstadoInscripcionTorneo estado = filtroEstado.getValue();
        filtradas.setPredicate(valor -> (estado == null || valor.estado() == estado)
                && (texto.isBlank()
                || contiene(String.valueOf(valor.id()), texto)
                || contiene(valor.torneo(), texto)
                || contiene(valor.categoriaCompleta(), texto)
                || contiene(valor.responsableNombre(), texto)
                || contiene(valor.parejaNombre(), texto)
                || contiene(valor.responsable().telefono(), texto)
                || contiene(valor.pareja().telefono(), texto)));
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    @FXML private void limpiarFiltros() {
        campoBuscar.clear();
        filtroEstado.getSelectionModel().clearSelection();
    }

    private void actualizarMetricas() {
        etiquetaTotal.setText(String.valueOf(inscripciones.size()));
        etiquetaPendientes.setText(String.valueOf(contar(EstadoInscripcionTorneo.PENDIENTE)));
        etiquetaConfirmadas.setText(String.valueOf(contar(EstadoInscripcionTorneo.CONFIRMADA)));
        etiquetaEspera.setText(String.valueOf(contar(EstadoInscripcionTorneo.LISTA_ESPERA)));
    }

    private long contar(EstadoInscripcionTorneo estado) {
        return inscripciones.stream().filter(i -> i.estado() == estado).count();
    }

    private void mostrarDetalle(InscripcionResumen valor) {
        seleccionada = valor;
        detalleTitulo.setText("Inscripcion #" + valor.id());
        detalleTorneo.setText(valor.torneo());
        detalleCategoria.setText(valor.categoriaCompleta());
        detalleEstado.setText(valor.estado().toString());
        detalleOrigen.setText(valor.origen());
        detalleFecha.setText(valor.fechaSolicitud() == null ? "-" : valor.fechaSolicitud().format(FECHA_HORA));
        detallePrecio.setText(FormateadorMoneda.pesos(valor.precioInscripcion()));
        cargarJugador(valor.responsable(), responsableNombre, responsableTelefono,
                responsableVinculacion, botonWhatsappResponsable,
                botonVincularResponsable, botonDesvincularResponsable);
        cargarJugador(valor.pareja(), parejaNombre, parejaTelefono,
                parejaVinculacion, botonWhatsappPareja,
                botonVincularPareja, botonDesvincularPareja);
        detalleComentarios.setText(valor.comentarios() == null ? "" : valor.comentarios());
        detalleObservaciones.setText(valor.observacionesAdministrativas() == null ? "" : valor.observacionesAdministrativas());
        actualizarAcciones(valor.estado());
    }

    private void cargarJugador(JugadorResumen jugador, Label nombre, Label telefono,
            Label vinculacion, Button whatsapp, Button vincular, Button desvincular) {
        nombre.setText(jugador.nombreCompleto());
        telefono.setText(jugador.telefono());
        vinculacion.setText(jugador.estadoVinculacion());
        whatsapp.setVisible(jugador.telefono() != null && !jugador.telefono().isBlank());
        whatsapp.setManaged(whatsapp.isVisible());
        vincular.setText(jugador.vinculado() ? "CAMBIAR VINCULACION" : "VINCULAR CLIENTE");
        desvincular.setVisible(jugador.vinculado());
        desvincular.setManaged(jugador.vinculado());
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

        botonConfirmar.setDisable(!(pendiente || espera));
        botonListaEspera.setDisable(!pendiente);
        botonRechazar.setDisable(!(pendiente || espera));
        botonCancelar.setDisable(!gestionable);
        detalleObservaciones.setEditable(gestionable);
    }

    @FXML private void confirmarInscripcion() {
        gestionar(EstadoInscripcionTorneo.CONFIRMADA);
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

    private boolean confirmarAccion(String titulo, String mensaje) {
        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                mensaje,
                ButtonType.OK,
                ButtonType.CANCEL);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
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

    private void mostrarError(RuntimeException exception, String alternativo) {
        String mensaje = exception.getMessage();
        String texto = mensaje == null || mensaje.isBlank()
                ? alternativo
                : mensaje;

        etiquetaMensaje.setText(texto);

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("No se pudo completar la operación");
        alerta.setHeaderText("Revisá la información e intentá nuevamente.");
        alerta.setContentText(texto);
        alerta.showAndWait();
    }

    private void mostrarExito(String titulo, String mensaje) {
        etiquetaMensaje.setText(mensaje);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
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
}
