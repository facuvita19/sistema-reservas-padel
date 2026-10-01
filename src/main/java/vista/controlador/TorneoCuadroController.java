package vista.controlador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import negocio.Cancha;
import negocio.EstadoPartidoTorneo;
import negocio.FaseTorneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoPartido;
import servicio.CanchaService;
import servicio.CuadroEliminacionTorneoService;
import servicio.ProgramacionPartidoTorneoService;
import vista.Dialogos;
import vista.Navegacion;

public class TorneoCuadroController {

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TorneoPartidoDAO partidoDAO =
            new TorneoPartidoDAOMySQL();
    private final TorneoCategoriaDAO categoriaDAO =
            new TorneoCategoriaDAOMySQL();
    private final TorneoInscripcionDAO inscripcionDAO =
            new TorneoInscripcionDAOMySQL();
    private final CanchaService canchaService = new CanchaService();
    private final CuadroEliminacionTorneoService cuadroService =
            new CuadroEliminacionTorneoService();
    private final ProgramacionPartidoTorneoService programacionService =
            new ProgramacionPartidoTorneoService();
    private final ObservableList<TorneoPartido> partidos =
            FXCollections.observableArrayList();

    private FilteredList<TorneoPartido> filtrados;
    private long categoriaId;
    private TorneoCategoria categoria;

    @FXML private Label etiquetaContexto;
    @FXML private Label etiquetaResumen;
    @FXML private Label etiquetaPartido;
    @FXML private Label etiquetaMensaje;
    @FXML private Label etiquetaCampeona;
    @FXML private Label etiquetaResultadoFinal;
    @FXML private Label etiquetaTrofeo;
    @FXML private Label etiquetaTituloPanel;
    @FXML private Label etiquetaMarcador;
    @FXML private Label etiquetaGanadora;
    @FXML private Label etiquetaFinalizacion;
    @FXML private Label etiquetaObservaciones;
    @FXML private javafx.scene.layout.VBox panelResultado;
    @FXML private ComboBox<FaseTorneo> filtroFase;
    @FXML private ComboBox<EstadoPartidoTorneo> filtroEstado;
    @FXML private TableView<TorneoPartido> tablaPartidos;
    @FXML private TableColumn<TorneoPartido, FaseTorneo> colFase;
    @FXML private TableColumn<TorneoPartido, Integer> colOrden;
    @FXML private TableColumn<TorneoPartido, String> colPareja1;
    @FXML private TableColumn<TorneoPartido, String> colPareja2;
    @FXML private TableColumn<TorneoPartido, String> colResultado;
    @FXML private TableColumn<TorneoPartido, EstadoPartidoTorneo> colEstado;
    @FXML private ComboBox<Cancha> comboCancha;
    @FXML private DatePicker selectorFecha;
    @FXML private ComboBox<LocalTime> comboInicio;
    @FXML private ComboBox<LocalTime> comboFin;
    @FXML private Button botonGenerar;
    @FXML private Button botonProgramar;
    @FXML private Button botonQuitar;
    @FXML private Button botonResultado;

    @FXML
    private void initialize() {
        categoriaId = Navegacion.consumirCategoriaCuadroTorneo();
        if (categoriaId <= 0) {
            throw new IllegalStateException(
                    "No se indico una categoria para gestionar el cuadro.");
        }
        configurarTabla();
        configurarFiltros();
        cargarCanchasYHorarios();
        cargar();
    }

    private void configurarTabla() {
        colFase.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getFase()));
        colOrden.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getOrdenFase())
                        .asObject());
        colPareja1.setCellValueFactory(d ->
                new SimpleStringProperty(nombrePareja(
                        d.getValue().getPareja1InscripcionId())));
        colPareja2.setCellValueFactory(d ->
                new SimpleStringProperty(nombrePareja(
                        d.getValue().getPareja2InscripcionId())));
        colResultado.setCellValueFactory(d ->
                new SimpleStringProperty(resultado(d.getValue())));
        colEstado.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getEstado()));
        tablaPartidos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colPareja1.setCellFactory(columna -> crearCeldaPareja(true));
        colPareja2.setCellFactory(columna -> crearCeldaPareja(false));
        colResultado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vacia) {
                super.updateItem(texto, vacia);
                setText(vacia ? null : texto);
                setWrapText(true);
                getStyleClass().remove("bracket-result-cell");
                if (!vacia) getStyleClass().add("bracket-result-cell");
            }
        });
        colEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(
                    EstadoPartidoTorneo estado, boolean vacia) {
                super.updateItem(estado, vacia);
                setText(vacia || estado == null ? null : estado.toString());
                getStyleClass().removeIf(
                        clase -> clase.startsWith("bracket-status-"));
                if (!vacia && estado != null) {
                    getStyleClass().add("bracket-status-"
                            + estado.name().toLowerCase()
                                .replace('_', '-'));
                }
            }
        });
        filtrados = new FilteredList<>(partidos, p -> true);
        tablaPartidos.setItems(filtrados);
        tablaPartidos.getSelectionModel().selectedItemProperty()
                .addListener((o, anterior, actual) -> mostrar(actual));
    }

    private void configurarFiltros() {
        filtroFase.setItems(FXCollections.observableArrayList(
                FaseTorneo.values()));
        filtroEstado.setItems(FXCollections.observableArrayList(
                EstadoPartidoTorneo.values()));
        filtroFase.valueProperty().addListener((o, a, n) -> filtrar());
        filtroEstado.valueProperty().addListener((o, a, n) -> filtrar());
    }

    private void cargarCanchasYHorarios() {
        List<Cancha> activas = canchaService.listar().stream()
                .filter(Cancha::isActivo)
                .toList();
        comboCancha.setItems(FXCollections.observableArrayList(activas));
        ObservableList<LocalTime> horas = FXCollections.observableArrayList();
        for (int hora = 6; hora <= 23; hora++) {
            horas.add(LocalTime.of(hora, 0));
            if (hora < 23) horas.add(LocalTime.of(hora, 30));
        }
        comboInicio.setItems(horas);
        comboFin.setItems(FXCollections.observableArrayList(horas));
    }

    @FXML
    public void cargar() {
        try {
            categoria = categoriaDAO.buscar(categoriaId);
            if (categoria == null) {
                throw new IllegalArgumentException(
                        "La categoria ya no existe.");
            }
            etiquetaContexto.setText(categoria.getNombreTorneo()
                    + " · " + categoria.getNombre()
                    + " · " + categoria.getRama());
            partidos.setAll(partidoDAO.listarPorCategoria(categoriaId));
            filtrar();
            botonGenerar.setDisable(!partidos.isEmpty());
            etiquetaResumen.setText(partidos.size() + " partido(s)");
            actualizarCampeona();
            etiquetaMensaje.setText(partidos.isEmpty()
                    ? "El cuadro todavía no fue generado."
                    : "Cuadro actualizado correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception);
        }
    }

    @FXML
    private void generarCuadro() {
        if (!Dialogos.confirmar("Generar cuadro",
                "Las parejas confirmadas se sortearan aleatoriamente.\n\n"
                + "Esta accion no puede repetirse para la categoria.\n\n"
                + "¿Queres continuar?")) return;
        try {
            cuadroService.generarCuadro(categoriaId);
            cargar();
            Dialogos.exito("Cuadro generado",
                    "El cuadro se genero correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception);
        }
    }

    @FXML
    private void programar() {
        TorneoPartido partido = seleccionado();
        if (partido == null) return;
        try {
            if (comboCancha.getValue() == null
                    || selectorFecha.getValue() == null
                    || comboInicio.getValue() == null
                    || comboFin.getValue() == null) {
                throw new IllegalArgumentException(
                        "Selecciona cancha, fecha y horario completo.");
            }
            long id = partido.getId();
            programacionService.programar(id,
                    comboCancha.getValue().getId(),
                    selectorFecha.getValue(), comboInicio.getValue(),
                    comboFin.getValue());
            cargar();
            seleccionarPorId(id);
            Dialogos.exito("Partido programado",
                    "La programacion se guardo correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception);
        }
    }

    @FXML
    private void registrarResultado() {
        TorneoPartido partido = seleccionado();
        if (partido == null) return;
        TorneoPartido actualizado =
                new ResultadoPartidoTorneoDialog(partido).mostrar();
        if (actualizado != null) {
            cargar();
            seleccionarPorId(actualizado.getId());
            Dialogos.exito("Resultado registrado",
                    "El resultado y el avance del cuadro se guardaron correctamente.");
        }
    }

    @FXML
    private void quitarProgramacion() {
        TorneoPartido partido = seleccionado();
        if (partido == null) return;
        if (!Dialogos.confirmarPeligro("Quitar programacion",
                "El partido volvera a estado Pendiente.\n\n"
                + "¿Queres continuar?")) return;
        try {
            programacionService.quitarProgramacion(partido.getId());
            cargar();
            Dialogos.exito("Programacion eliminada",
                    "El partido volvio a estado Pendiente.");
        } catch (RuntimeException exception) {
            mostrarError(exception);
        }
    }

    private void mostrar(TorneoPartido partido) {
        boolean hay = partido != null;
        botonProgramar.setDisable(!hay || partido.isBye()
                || !partido.tieneDosParejas()
                || (partido.getEstado() != EstadoPartidoTorneo.PENDIENTE
                    && partido.getEstado()
                        != EstadoPartidoTorneo.PROGRAMADO));
        botonQuitar.setDisable(!hay
                || partido.getEstado() != EstadoPartidoTorneo.PROGRAMADO);
        botonResultado.setDisable(!hay || partido.isBye()
                || !partido.tieneDosParejas()
                || (partido.getEstado() != EstadoPartidoTorneo.PROGRAMADO
                    && partido.getEstado() != EstadoPartidoTorneo.EN_CURSO));
        if (!hay) {
            etiquetaTituloPanel.setText("DETALLE DEL PARTIDO");
            etiquetaPartido.setText("Selecciona un partido");
            panelResultado.setVisible(false);
            panelResultado.setManaged(false);
            limpiarFormulario();
            return;
        }
        etiquetaPartido.setText(partido.getFase() + " #"
                + partido.getOrdenFase() + " · "
                + nombrePareja(partido.getPareja1InscripcionId())
                + " vs "
                + nombrePareja(partido.getPareja2InscripcionId()));
        selectorFecha.setValue(partido.getFecha());
        comboInicio.setValue(partido.getHoraInicio());
        comboFin.setValue(partido.getHoraFin());
        comboCancha.setValue(partido.getCanchaId() == null ? null
                : comboCancha.getItems().stream()
                    .filter(c -> c.getId() == partido.getCanchaId())
                    .findFirst().orElse(null));
        mostrarResultado(partido);
    }

    private String nombrePareja(Long inscripcionId) {
        if (inscripcionId == null) return "Por definir";
        TorneoInscripcion inscripcion = inscripcionDAO.buscar(inscripcionId);
        if (inscripcion == null) return "Inscripcion #" + inscripcionId;
        String nombres = inscripcion.getJugadores().stream()
                .map(j -> j.getNombreCompleto())
                .filter(n -> !n.isBlank())
                .reduce((a, b) -> a + " / " + b)
                .orElse("Inscripcion #" + inscripcionId);
        return nombres;
    }

    private TableCell<TorneoPartido, String> crearCeldaPareja(
            boolean pareja1) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String texto, boolean vacia) {
                super.updateItem(texto, vacia);
                setText(vacia ? null : texto);
                setWrapText(true);
                getStyleClass().removeAll(
                        "bracket-winner-cell", "bracket-loser-cell");
                if (vacia || getTableRow() == null
                        || getTableRow().getItem() == null) return;
                TorneoPartido partido = getTableRow().getItem();
                Long lado = pareja1 ? partido.getPareja1InscripcionId()
                        : partido.getPareja2InscripcionId();
                if (partido.getGanadoraInscripcionId() != null) {
                    getStyleClass().add(
                            partido.getGanadoraInscripcionId().equals(lado)
                                    ? "bracket-winner-cell"
                                    : "bracket-loser-cell");
                    if (partido.getGanadoraInscripcionId().equals(lado)) {
                        setText("★ " + texto);
                    }
                }
            }
        };
    }

    private String resultado(TorneoPartido partido) {
        if (partido.isBye()) return "Clasifica por BYE";
        if (partido.getSets().isEmpty()) return "Sin resultado";
        return partido.getSets().stream()
                .map(set -> set.getPuntosPareja1() + "-"
                        + set.getPuntosPareja2())
                .reduce((a, b) -> a + " / " + b)
                .orElse("Sin resultado");
    }

    private void actualizarCampeona() {
        TorneoPartido finalPartido = partidos.stream()
                .filter(p -> p.getFase() == FaseTorneo.FINAL)
                .findFirst().orElse(null);
        boolean definida = finalPartido != null
                && finalPartido.getGanadoraInscripcionId() != null
                && finalPartido.getEstado()
                    == EstadoPartidoTorneo.FINALIZADO;
        etiquetaCampeona.setText(definida
                ? nombrePareja(finalPartido.getGanadoraInscripcionId())
                : "Campeona por definir");
        etiquetaResultadoFinal.setText(definida
                ? "Resultado de la final: " + resultado(finalPartido)
                : "La final todavía no tiene un resultado registrado.");
        etiquetaTrofeo.setVisible(definida);
        etiquetaTrofeo.setManaged(definida);
    }

    private void mostrarResultado(TorneoPartido partido) {
        boolean finalizado = partido.getEstado()
                == EstadoPartidoTorneo.FINALIZADO;
        panelResultado.setVisible(finalizado);
        panelResultado.setManaged(finalizado);
        etiquetaTituloPanel.setText(finalizado
                ? "RESULTADO DEL PARTIDO"
                : partido.getEstado() == EstadoPartidoTorneo.PROGRAMADO
                    ? "PROGRAMACIÓN Y RESULTADO"
                    : "PROGRAMAR PARTIDO");
        if (!finalizado) return;
        etiquetaMarcador.setText(resultado(partido));
        etiquetaGanadora.setText(nombrePareja(
                partido.getGanadoraInscripcionId()));
        etiquetaFinalizacion.setText(partido.getFechaFinalizacion() == null
                ? "Finalización sin fecha registrada"
                : "Finalizado: "
                    + FECHA_HORA.format(partido.getFechaFinalizacion()));
        etiquetaObservaciones.setText(
                partido.getObservaciones() == null
                    || partido.getObservaciones().isBlank()
                        ? "Sin observaciones"
                        : partido.getObservaciones());
    }

    private String programacion(TorneoPartido partido) {
        if (partido.isBye()) return "Clasifica por BYE";
        if (!partido.estaProgramado()) return "Sin programar";
        String cancha = comboCancha.getItems().stream()
                .filter(c -> c.getId() == partido.getCanchaId())
                .map(Cancha::getNombre)
                .findFirst().orElse("Cancha #" + partido.getCanchaId());
        return FECHA.format(partido.getFecha()) + " · "
                + HORA.format(partido.getHoraInicio()) + "-"
                + HORA.format(partido.getHoraFin()) + "\n" + cancha;
    }

    @FXML
    private void limpiarFiltros() {
        filtroFase.getSelectionModel().clearSelection();
        filtroEstado.getSelectionModel().clearSelection();
    }

    private void filtrar() {
        FaseTorneo fase = filtroFase.getValue();
        EstadoPartidoTorneo estado = filtroEstado.getValue();
        filtrados.setPredicate(p -> (fase == null || p.getFase() == fase)
                && (estado == null || p.getEstado() == estado));
        etiquetaResumen.setText(filtrados.size() + " de "
                + partidos.size() + " partido(s)");
    }

    private TorneoPartido seleccionado() {
        TorneoPartido partido = tablaPartidos.getSelectionModel()
                .getSelectedItem();
        if (partido == null) {
            Dialogos.informacion("Seleccion requerida",
                    "Selecciona un partido de la tabla.");
        }
        return partido;
    }

    private void seleccionarPorId(long id) {
        partidos.stream().filter(p -> p.getId() == id).findFirst()
                .ifPresent(p -> tablaPartidos.getSelectionModel().select(p));
    }

    private void limpiarFormulario() {
        comboCancha.getSelectionModel().clearSelection();
        selectorFecha.setValue(null);
        comboInicio.getSelectionModel().clearSelection();
        comboFin.getSelectionModel().clearSelection();
    }

    private void mostrarError(RuntimeException exception) {
        String mensaje = exception.getMessage() == null
                ? "No se pudo completar la operacion."
                : exception.getMessage();
        etiquetaMensaje.setText(mensaje);
        Dialogos.error("No se pudo completar la operacion", mensaje);
    }

    @FXML
    private void volver() {
        Navegacion.mostrarTorneos();
    }
}
