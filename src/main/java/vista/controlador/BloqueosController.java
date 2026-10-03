package vista.controlador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.BloqueoCancha;
import negocio.Cancha;
import servicio.BloqueoCanchaService;
import servicio.CanchaService;
import vista.Navegacion;
import vista.Dialogos;

public class BloqueosController {

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BloqueoCanchaService bloqueoService =
            new BloqueoCanchaService();
    private final CanchaService canchaService = new CanchaService();

    private final ObservableList<BloqueoCancha> bloqueos =
            FXCollections.observableArrayList();
    private FilteredList<BloqueoCancha> bloqueosFiltrados;
    private BloqueoCancha bloqueoSeleccionado;

    @FXML private TextField campoBuscar;
    @FXML private TableView<BloqueoCancha> tablaBloqueos;
    @FXML private TableColumn<BloqueoCancha, LocalDate> columnaFecha;
    @FXML private TableColumn<BloqueoCancha, String> columnaHorario;
    @FXML private TableColumn<BloqueoCancha, String> columnaCancha;
    @FXML private TableColumn<BloqueoCancha, String> columnaMotivo;

    @FXML private Label tituloFormulario;
    @FXML private Label subtituloFormulario;
    @FXML private Label insigniaEstadoBloqueo;
    @FXML private Label etiquetaMensaje;
    @FXML private ComboBox<Cancha> comboCancha;
    @FXML private DatePicker selectorFecha;
    @FXML private ComboBox<String> comboHoraInicio;
    @FXML private ComboBox<String> comboHoraFin;
    @FXML private ComboBox<String> comboMotivoRapido;
    @FXML private TextArea campoMotivo;
    @FXML private Button botonGuardar;
    @FXML private Button botonEliminar;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarBusqueda();
        configurarFormulario();
        cargarCanchas();
        cargarBloqueos();
        nuevo();
    }

    private void configurarTabla() {
        columnaFecha.setCellValueFactory(
                new PropertyValueFactory<>("fecha")
        );
        columnaFecha.setCellFactory(columna -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null
                        ? null
                        : fecha.format(FORMATO_FECHA));
            }
        });

        columnaHorario.setCellValueFactory(datos ->
                new javafx.beans.property.SimpleStringProperty(
                        datos.getValue().getHoraInicio().format(FORMATO_HORA)
                                + " - "
                                + datos.getValue().getHoraFin().format(FORMATO_HORA)
                )
        );
        columnaCancha.setCellValueFactory(
                new PropertyValueFactory<>("nombreCancha")
        );
        columnaMotivo.setCellValueFactory(
                new PropertyValueFactory<>("motivo")
        );

        tablaBloqueos.setRowFactory(tabla -> {
            TableRow<BloqueoCancha> fila = new TableRow<>();
            fila.itemProperty().addListener((obs, anterior, actual) ->
                    actualizarClaseFila(fila, actual));
            fila.selectedProperty().addListener((obs, anterior, actual) ->
                    actualizarClaseFila(fila, fila.getItem()));
            return fila;
        });
        tablaBloqueos.setOnMouseClicked(evento -> {
            if (clicEnFondoTabla(evento.getTarget())) limpiarSeleccion();
        });
        tablaBloqueos.setOnKeyPressed(evento -> {
            if (evento.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                limpiarSeleccion();
                evento.consume();
            }
        });
        tablaBloqueos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) editar(actual);
                });
    }

    private void actualizarClaseFila(
            TableRow<BloqueoCancha> fila,
            BloqueoCancha bloqueo) {
        fila.getStyleClass().removeAll(
                "block-row-past", "block-row-selected");
        if (bloqueo == null || fila.isEmpty()) return;
        if (bloqueo.getFecha().isBefore(LocalDate.now())) {
            fila.getStyleClass().add("block-row-past");
        }
        if (fila.isSelected()) fila.getStyleClass().add("block-row-selected");
    }

    private boolean clicEnFondoTabla(Object objetivo) {
        if (!(objetivo instanceof Node nodo)) return false;
        Node actual = nodo;
        while (actual != null && actual != tablaBloqueos) {
            if (actual instanceof TableRow<?> fila) return fila.isEmpty();
            actual = actual.getParent();
        }
        return actual == tablaBloqueos;
    }

    @FXML
    private void limpiarSeleccion() {
        nuevo();
        campoBuscar.requestFocus();
    }

    private void configurarBusqueda() {
        bloqueosFiltrados = new FilteredList<>(bloqueos, bloqueo -> true);
        tablaBloqueos.setItems(bloqueosFiltrados);

        campoBuscar.textProperty().addListener((obs, anterior, texto) -> {
            String filtro = texto == null
                    ? ""
                    : texto.trim().toLowerCase(Locale.ROOT);

            bloqueosFiltrados.setPredicate(bloqueo ->
                    filtro.isBlank()
                            || contiene(bloqueo.getNombreCancha(), filtro)
                            || contiene(bloqueo.getMotivo(), filtro)
                            || bloqueo.getFecha().format(FORMATO_FECHA).contains(filtro)
            );
        });
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null
                && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    private void configurarFormulario() {
        comboMotivoRapido.setItems(FXCollections.observableArrayList(
                "Mantenimiento",
                "Torneo",
                "Clase",
                "Evento privado",
                "Reparación",
                "Problemas climáticos",
                "Otro"
        ));
        comboMotivoRapido.valueProperty().addListener(
                (obs, anterior, actual) -> {
                    if (actual != null && !"Otro".equals(actual)) {
                        campoMotivo.setText(actual);
                    }
                }
        );

        selectorFecha.setDayCellFactory(control -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isBefore(LocalDate.now()));
            }
        });

        comboCancha.valueProperty().addListener(
                (obs, anterior, actual) -> cargarHorariosDeCancha(actual)
        );
    }

    private void cargarCanchas() {
        try {
            comboCancha.setItems(FXCollections.observableArrayList(
                    canchaService.listar()
            ));
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    @FXML
    private void cargarBloqueos() {
        try {
            List<BloqueoCancha> resultado = bloqueoService.listar();
            bloqueos.setAll(resultado);
            mostrarInfo(resultado.size() + " bloqueo(s) cargado(s).");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    @FXML
    private void nuevo() {
        bloqueoSeleccionado = null;
        tablaBloqueos.getSelectionModel().clearSelection();
        tituloFormulario.setText("Nuevo bloqueo");
        subtituloFormulario.setText(
                "Reservá temporalmente una franja de la cancha.");
        insigniaEstadoBloqueo.setVisible(false);
        insigniaEstadoBloqueo.setManaged(false);
        botonGuardar.setText("GUARDAR BLOQUEO");
        configurarFormularioSoloLectura(false);
        botonEliminar.setVisible(false);
        botonEliminar.setManaged(false);

        comboCancha.getSelectionModel().clearSelection();
        selectorFecha.setValue(LocalDate.now().plusDays(1));
        comboHoraInicio.getItems().clear();
        comboHoraFin.getItems().clear();
        comboMotivoRapido.getSelectionModel().clearSelection();
        campoMotivo.clear();
        etiquetaMensaje.setText("");
    }

    private void editar(BloqueoCancha bloqueo) {
        bloqueoSeleccionado = bloqueo;
        boolean finalizado = bloqueo.getFecha().isBefore(LocalDate.now());
        tituloFormulario.setText(finalizado
                ? "Bloqueo finalizado" : "Editar bloqueo");
        subtituloFormulario.setText(
                bloqueo.getNombreCancha() + " · "
                        + bloqueo.getFecha().format(FORMATO_FECHA)
                        + "\n" + bloqueo.getHoraInicio().format(FORMATO_HORA)
                        + " a " + bloqueo.getHoraFin().format(FORMATO_HORA));
        insigniaEstadoBloqueo.setText(finalizado ? "FINALIZADO" : "PROGRAMADO");
        insigniaEstadoBloqueo.getStyleClass().removeAll(
                "block-detail-finished", "block-detail-scheduled");
        insigniaEstadoBloqueo.getStyleClass().add(finalizado
                ? "block-detail-finished" : "block-detail-scheduled");
        insigniaEstadoBloqueo.setVisible(true);
        insigniaEstadoBloqueo.setManaged(true);
        botonGuardar.setText("GUARDAR CAMBIOS");
        configurarFormularioSoloLectura(finalizado);
        botonEliminar.setVisible(true);
        botonEliminar.setManaged(true);

        Cancha cancha = buscarCancha(bloqueo.getCanchaId());
        comboCancha.setValue(cancha);
        selectorFecha.setValue(bloqueo.getFecha());
        cargarHorariosDeCancha(cancha);
        comboHoraInicio.setValue(
                bloqueo.getHoraInicio().format(FORMATO_HORA)
        );
        comboHoraFin.setValue(
                bloqueo.getHoraFin().format(FORMATO_HORA)
        );
        campoMotivo.setText(bloqueo.getMotivo());
        etiquetaMensaje.setText("");
    }

    private void configurarFormularioSoloLectura(boolean soloLectura) {
        comboCancha.setDisable(soloLectura);
        selectorFecha.setDisable(soloLectura);
        comboHoraInicio.setDisable(soloLectura);
        comboHoraFin.setDisable(soloLectura);
        comboMotivoRapido.setDisable(soloLectura);
        campoMotivo.setEditable(!soloLectura);
        botonGuardar.setDisable(soloLectura);
    }

    private Cancha buscarCancha(long id) {
        return comboCancha.getItems().stream()
                .filter(cancha -> cancha.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void cargarHorariosDeCancha(Cancha cancha) {
        comboHoraInicio.getItems().clear();
        comboHoraFin.getItems().clear();

        if (cancha == null) {
            return;
        }

        ObservableList<String> horarios = FXCollections.observableArrayList();
        int inicio = cancha.getHoraApertura().getHour() * 60
                + cancha.getHoraApertura().getMinute();
        int fin = cancha.getHoraCierre().getHour() * 60
                + cancha.getHoraCierre().getMinute();

        for (int minutos = inicio; minutos <= fin; minutos += 30) {
            LocalTime hora = LocalTime.of(minutos / 60, minutos % 60);
            horarios.add(hora.format(FORMATO_HORA));
        }

        comboHoraInicio.setItems(horarios);
        comboHoraFin.setItems(FXCollections.observableArrayList(horarios));

        if (horarios.size() >= 2 && bloqueoSeleccionado == null) {
            comboHoraInicio.getSelectionModel().selectFirst();
            comboHoraFin.getSelectionModel().select(1);
        }
    }

    @FXML
    private void guardar() {
        try {
            validarFormulario();

            BloqueoCancha bloqueo = bloqueoSeleccionado == null
                    ? new BloqueoCancha()
                    : bloqueoSeleccionado;

            bloqueo.setCanchaId(comboCancha.getValue().getId());
            bloqueo.setFecha(selectorFecha.getValue());
            bloqueo.setHoraInicio(LocalTime.parse(
                    comboHoraInicio.getValue(),
                    FORMATO_HORA
            ));
            bloqueo.setHoraFin(LocalTime.parse(
                    comboHoraFin.getValue(),
                    FORMATO_HORA
            ));
            bloqueo.setMotivo(campoMotivo.getText());

            bloqueoService.guardar(bloqueo);
            cargarBloqueos();
            nuevo();
            mostrarInfo("El bloqueo se guardó correctamente.");

        } catch (IllegalArgumentException exception) {
            mostrarError(exception.getMessage());
        } catch (RuntimeException exception) {
            mostrarError("No se pudo guardar el bloqueo: "
                    + exception.getMessage());
        }
    }

    private void validarFormulario() {
        if (comboCancha.getValue() == null) {
            throw new IllegalArgumentException("Seleccioná una cancha.");
        }
        if (selectorFecha.getValue() == null) {
            throw new IllegalArgumentException("Seleccioná una fecha.");
        }
        if (comboHoraInicio.getValue() == null
                || comboHoraFin.getValue() == null) {
            throw new IllegalArgumentException(
                    "Seleccioná el horario completo del bloqueo."
            );
        }
        if (campoMotivo.getText() == null
                || campoMotivo.getText().isBlank()) {
            throw new IllegalArgumentException(
                    "Ingresá el motivo del bloqueo."
            );
        }
    }

    @FXML
    private void eliminar() {
        if (bloqueoSeleccionado == null) {
            mostrarError("Seleccioná un bloqueo de la tabla.");
            return;
        }
        String detalle = bloqueoSeleccionado.getNombreCancha()
                + " · " + bloqueoSeleccionado.getFecha().format(FORMATO_FECHA)
                + " · " + bloqueoSeleccionado.getMotivo();
        if (!Dialogos.confirmarPeligro("Eliminar bloqueo",
                "Se eliminará el bloqueo seleccionado.\n\n" + detalle)) return;
        try {
            bloqueoService.eliminar(bloqueoSeleccionado.getId());
            cargarBloqueos();
            nuevo();
            mostrarInfo("El bloqueo fue eliminado.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    @FXML
    private void volver() {
        Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());
    }

    private void mostrarError(String mensaje) {
        etiquetaMensaje.setText(mensaje == null ? "Ocurrió un error." : mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-exito");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) {
            etiquetaMensaje.getStyleClass().add("mensaje-error");
        }
    }

    private void mostrarInfo(String mensaje) {
        etiquetaMensaje.setText(mensaje);
        etiquetaMensaje.getStyleClass().remove("mensaje-error");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-exito")) {
            etiquetaMensaje.getStyleClass().add("mensaje-exito");
        }
    }
}
