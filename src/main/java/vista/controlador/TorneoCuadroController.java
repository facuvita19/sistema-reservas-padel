package vista.controlador;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
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
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.scene.control.TableRow;
import javafx.scene.Node;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import negocio.Cancha;
import negocio.EstadoPartidoTorneo;
import negocio.FaseTorneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoPartido;
import negocio.Torneo;
import negocio.EstadoTorneo;
import negocio.CorreccionResultadoTorneo;
import servicio.CanchaService;
import servicio.CuadroEliminacionTorneoService;
import servicio.ConfiguracionComplejoService;
import servicio.CuadroTorneoPdfService;
import servicio.ProgramacionPartidoTorneoService;
import servicio.HistorialCorreccionResultadoTorneoService;
import servicio.InvalidacionCuadroTorneoService;
import vista.Dialogos;
import vista.Navegacion;

public class TorneoCuadroController {

    // guardar-pdf-cuadro-seguro-v5

    // filtros-opciones-disponibles-cuadro-v1

    // reorganizar-filtros-campeon-cuadro-v1

    // separar-enfrentamiento-cuadro-paso2-v1

    // mejoras-seguras-cuadro-paso1-v1

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TorneoPartidoDAO partidoDAO =
            new TorneoPartidoDAOMySQL();
    private final TorneoDAO torneoDAO = new TorneoDAOMySQL();
    private final TorneoCategoriaDAO categoriaDAO =
            new TorneoCategoriaDAOMySQL();
    private final TorneoInscripcionDAO inscripcionDAO =
            new TorneoInscripcionDAOMySQL();
    private final CanchaService canchaService = new CanchaService();
    private final CuadroEliminacionTorneoService cuadroService =
            new CuadroEliminacionTorneoService();
    private final ProgramacionPartidoTorneoService programacionService =
            new ProgramacionPartidoTorneoService();
    private final ConfiguracionComplejoService configuracionService =
            new ConfiguracionComplejoService();
    private final CuadroTorneoPdfService pdfService =
            new CuadroTorneoPdfService();
    private final HistorialCorreccionResultadoTorneoService historialService =
            new HistorialCorreccionResultadoTorneoService();
    private final InvalidacionCuadroTorneoService invalidacionService =
            new InvalidacionCuadroTorneoService();
    private final ObservableList<TorneoPartido> partidos =
            FXCollections.observableArrayList();

    private FilteredList<TorneoPartido> filtrados;
    private long categoriaId;
    private TorneoCategoria categoria;
    private Torneo torneo;

    @FXML private Label etiquetaContexto;
    @FXML private Label etiquetaResumen;
    @FXML private Label etiquetaPartido;
    @FXML private Label etiquetaEnfrentamiento;
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
    @FXML private VBox panelDetalleVacio;
    @FXML private VBox panelGestionPartido;
    @FXML private VBox panelProgramacion;
    @FXML private javafx.scene.layout.VBox panelHistorial;
    @FXML private javafx.scene.layout.VBox contenedorHistorial;
    @FXML private Label etiquetaSinHistorial;
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
    @FXML private Button botonInvalidar;
    @FXML private Button botonIrAGrupos;
    @FXML private Button botonProgramar;
    @FXML private Button botonQuitar;
    @FXML private Button botonResultado;
    @FXML private Button botonCorregirResultado;
    @FXML private Button botonImprimir;
    @FXML private javafx.scene.layout.HBox tarjetaCampeona;
    @FXML private javafx.scene.control.ScrollPane scrollHistorial;

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
        colFase.setStyle("-fx-alignment: CENTER;");
        colOrden.setStyle("-fx-alignment: CENTER;");
        colPareja1.setStyle("-fx-alignment: CENTER_LEFT;");
        colPareja2.setStyle("-fx-alignment: CENTER_LEFT;");
        colResultado.setStyle("-fx-alignment: CENTER;");
        colEstado.setStyle("-fx-alignment: CENTER;");
        colFase.setCellValueFactory(d ->
                new SimpleObjectProperty<>(d.getValue().getFase()));
        colFase.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(FaseTorneo fase, boolean vacia) {
                super.updateItem(fase, vacia);
                if (vacia || fase == null || getTableRow() == null
                        || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setAlignment(Pos.CENTER);
                TorneoPartido partido = getTableRow().getItem();
                setText(nombreFaseVisible(partido));
                setStyle("-fx-text-fill:" + colorFase(partido.getFase()) + ";-fx-font-weight:900;");
            }
        });
        colOrden.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getOrdenFase())
                        .asObject());
        colOrden.setCellFactory(columna -> new TableCell<>() {
            @Override protected void updateItem(Integer valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setText(vacia || valor == null ? null : String.valueOf(valor));
            }
        });
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
                setAlignment(Pos.CENTER);
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
                setAlignment(Pos.CENTER);
                setStyle("");
                setText(vacia || estado == null ? null : estado.toString());
                getStyleClass().removeIf(
                        clase -> clase.startsWith("bracket-status-"));
                if (!vacia && estado != null) {
                    getStyleClass().add("bracket-status-"
                            + estado.name().toLowerCase()
                                .replace('_', '-'));
                    setStyle("-fx-text-fill:" + colorEstado(estado)
                            + ";-fx-font-weight:900;");
                }
            }
        });
        filtrados = new FilteredList<>(partidos, p -> true);
        tablaPartidos.setItems(filtrados);
        tablaPartidos.getSelectionModel().selectedItemProperty()
                .addListener((o, anterior, actual) -> mostrar(actual));
        tablaPartidos.setOnMousePressed(evento -> {
            Node nodo = evento.getPickResult().getIntersectedNode();
            while (nodo != null
                    && nodo != tablaPartidos
                    && !(nodo instanceof TableRow<?>)) {
                nodo = nodo.getParent();
            }
            if (!(nodo instanceof TableRow<?> fila) || fila.isEmpty()) {
                tablaPartidos.getSelectionModel().clearSelection();
            }
        });
    }

    private void configurarFiltros() {
        filtroFase.setItems(FXCollections.observableArrayList());
        filtroEstado.setItems(FXCollections.observableArrayList());
        filtroFase.valueProperty().addListener((o, a, n) -> filtrar());
        filtroEstado.valueProperty().addListener((o, a, n) -> filtrar());
    }

    private void actualizarOpcionesFiltros() {
        FaseTorneo faseSeleccionada = filtroFase.getValue();
        EstadoPartidoTorneo estadoSeleccionado = filtroEstado.getValue();

        List<FaseTorneo> fasesDisponibles = partidos.stream()
                .map(TorneoPartido::getFase)
                .filter(fase -> fase != null && fase != FaseTorneo.GRUPOS)
                .distinct()
                .sorted(java.util.Comparator.comparingInt(Enum::ordinal))
                .toList();
        List<EstadoPartidoTorneo> estadosDisponibles = partidos.stream()
                .filter(partido -> partido.getFase() != FaseTorneo.GRUPOS)
                .map(TorneoPartido::getEstado)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted(java.util.Comparator.comparingInt(Enum::ordinal))
                .toList();

        filtroFase.setItems(
                FXCollections.observableArrayList(fasesDisponibles));
        filtroEstado.setItems(
                FXCollections.observableArrayList(estadosDisponibles));

        if (faseSeleccionada != null
                && fasesDisponibles.contains(faseSeleccionada)) {
            filtroFase.setValue(faseSeleccionada);
        } else {
            filtroFase.getSelectionModel().clearSelection();
        }
        if (estadoSeleccionado != null
                && estadosDisponibles.contains(estadoSeleccionado)) {
            filtroEstado.setValue(estadoSeleccionado);
        } else {
            filtroEstado.getSelectionModel().clearSelection();
        }
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
            torneo = torneoDAO.buscar(categoria.getTorneoId());
            boolean usaGrupos = categoria.usaFaseGrupos();
            botonIrAGrupos.setVisible(usaGrupos);
            botonIrAGrupos.setManaged(usaGrupos);
            if (torneo == null) {
                throw new IllegalArgumentException(
                        "El torneo de la categoria ya no existe.");
            }
            partidos.setAll(partidoDAO.listarPorCategoria(categoriaId));
            actualizarOpcionesFiltros();
            filtrar();
            boolean existeEliminatorio = partidos.stream()
                    .anyMatch(p -> p.getFase() != FaseTorneo.GRUPOS);
            configurarAccionesCuadro(existeEliminatorio);
            long partidosEliminatorios = partidos.stream()
                    .filter(p -> p.getFase() != FaseTorneo.GRUPOS).count();
            etiquetaResumen.setText(textoPartidos(partidosEliminatorios));
            actualizarCampeona();
            etiquetaMensaje.setText(partidos.isEmpty()
                    ? "El cuadro todavía no fue generado."
                    : "Cuadro actualizado correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception);
        }
    }

    @FXML
    private void imprimirCuadro() {
        boolean existeEliminatorio = partidos.stream()
                .anyMatch(p -> p.getFase() != FaseTorneo.GRUPOS);
        if (!existeEliminatorio) {
            Dialogos.informacion("Cuadro no disponible",
                    "Genera el cuadro eliminatorio antes de crear la "
                            + "planilla imprimible.");
            return;
        }
        ExportarCuadroTorneoDialog.Modo modo =
                new ExportarCuadroTorneoDialog().mostrar().orElse(null);
        if (modo == null) return;

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar cuadro mural");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));
        selector.setInitialFileName(nombreArchivoPdf(modo));
        File destino = selector.showSaveDialog(
                tablaPartidos.getScene().getWindow());
        if (destino == null) return;
        if (!destino.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            destino = new File(destino.getParentFile(),
                    destino.getName() + ".pdf");
        }

        Path temporal = null;
        try {
            File archivo = destino;
            Path destinoPath = archivo.toPath().toAbsolutePath();
            Path carpeta = destinoPath.getParent();
            if (carpeta == null) {
                carpeta = Path.of(".").toAbsolutePath().normalize();
            }
            temporal = Files.createTempFile(
                    carpeta, ".cuadro-torneo-", ".pdf.tmp");
            pdfService.generar(
                    temporal.toFile(), torneo, categoria,
                    List.copyOf(partidos), this::nombrePareja,
                    this::nombreCancha, configuracionService.obtener(),
                    modo == ExportarCuadroTorneoDialog.Modo.ACTUALIZADO);
            reemplazarPdfSeguro(temporal, destinoPath);
            temporal = null;
            etiquetaMensaje.setText("PDF generado: " + archivo.getName());
            Dialogos.exito("Cuadro listo para imprimir",
                    "El PDF se guardo correctamente.");
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
            }
        } catch (IOException exception) {
            Dialogos.error("No se pudo guardar el PDF",
                    "No se pudo reemplazar el archivo seleccionado. "
                            + "Si el PDF esta abierto, cerralo e intenta "
                            + "nuevamente.\n\nDetalle: "
                            + exception.getMessage());
        } catch (Exception exception) {
            mostrarError(new RuntimeException(
                    "No se pudo generar o abrir el PDF: "
                            + exception.getMessage(), exception));
        } finally {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException ignored) {
                    // No se oculta el error principal por la limpieza.
                }
            }
        }
    }

    private String nombreFaseVisible(TorneoPartido partido) {
        FaseTorneo fase = partido.getFase();
        if (!fase.name().startsWith("ACCESO_")) return fase.toString();
        boolean tieneOtraPreviaPosterior = partidos.stream()
                .anyMatch(p -> p.getFase().name().startsWith("ACCESO_")
                        && numeroPrevia(p.getFase()) > numeroPrevia(fase));
        if (tieneOtraPreviaPosterior) return fase.toString();
        Long siguienteId = partido.getPartidoSiguienteId();
        FaseTorneo destino = siguienteId == null ? null : partidos.stream()
                .filter(p -> p.getId() == siguienteId)
                .map(TorneoPartido::getFase).findFirst().orElse(null);
        if (destino == null || destino.name().startsWith("ACCESO_")) {
            return fase.toString();
        }
        return "Clasificacion a " + switch (destino) {
            case FINAL -> "la final";
            case SEMIFINAL -> "semifinales";
            case CUARTOS -> "cuartos de final";
            case OCTAVOS -> "octavos de final";
            case DIECISEISAVOS -> "dieciseisavos de final";
            default -> destino.toString().toLowerCase();
        };
    }

    private int numeroPrevia(FaseTorneo fase) {
        return fase.name().startsWith("ACCESO_")
                ? Integer.parseInt(fase.name().substring(7)) : 0;
    }

    private String nombreCancha(Long canchaId) {
        if (canchaId == null) return "";
        return comboCancha.getItems().stream()
                .filter(cancha -> cancha.getId() == canchaId)
                .map(Cancha::getNombre)
                .findFirst().orElse("Cancha #" + canchaId);
    }

    private String nombreArchivoPdf(
            ExportarCuadroTorneoDialog.Modo modo) {
        String base = torneo.getNombre() + "-" + categoria.getNombre()
                + "-" + categoria.getRama();
        String sufijo = modo == ExportarCuadroTorneoDialog.Modo.ACTUALIZADO
                ? "cuadro-actualizado" : "planilla-resultados";
        return "cuadro-" + base.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "")
                + "-" + sufijo + ".pdf";
    }

    private void reemplazarPdfSeguro(Path temporal, Path destino)
            throws IOException {
        try {
            Files.move(temporal, destino,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporal, destino,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @FXML
    private void generarCuadro() {
        if (categoria == null) {
            Dialogos.informacion("Categoria no disponible",
                    "No se pudo identificar la categoria seleccionada.");
            return;
        }

        if (!categoria.usaFaseGrupos()) {
            generarCuadroEliminacionDirecta();
            return;
        }

        if (!Dialogos.confirmar("Generar etapa eliminatoria",
                "Se utilizaran las posiciones definitivas y los "
                        + "clasificados de los grupos.\n\n"
                        + "Esta accion no puede repetirse para la categoria."
                        + "\n\n¿Queres continuar?")) {
            return;
        }

        try {
            var propuesta = new PropuestaEtapaEliminatoriaDialog(
                    categoriaId).mostrar();
            if (propuesta == null) return;
            cargar();
            Dialogos.exito("Cuadro generado",
                    "La propuesta fue confirmada y los partidos se crearon "
                            + "correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception);
        }
    }

    private void generarCuadroEliminacionDirecta() {
        if (!Dialogos.confirmar("Generar cuadro de eliminacion directa",
                "Se sortearan directamente las parejas confirmadas de la "
                        + "categoria. No es necesario configurar grupos."
                        + "\n\nSi la cantidad de parejas no completa una "
                        + "potencia de dos, se asignaran pases libres."
                        + "\n\nEsta accion no puede repetirse para la "
                        + "categoria.\n\n¿Queres continuar?")) {
            return;
        }

        try {
            cuadroService.generarCuadro(categoriaId);
            cargar();
            Dialogos.exito("Cuadro generado",
                    "Las parejas confirmadas fueron sorteadas y el cuadro "
                            + "de eliminacion directa se creo correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception);
        }
    }

    @FXML
    private void invalidarCuadro() {
        try {
            var resumen = invalidacionService.analizar(categoriaId);
            if (resumen.tieneActividadDeportiva()) {
                Dialogos.error("Invalidacion no disponible",
                        "El cuadro tiene partidos disputados, en curso o con "
                                + "resultados. No puede eliminarse.");
                return;
            }
            String detalle = resumen.tieneProgramacion()
                    ? "El cuadro tiene partidos programados. Se eliminaran "
                            + "canchas, fechas, horarios y todos los cruces. "
                            + "Los grupos y sus resultados se conservaran."
                    : "Se eliminaran exclusivamente los partidos del cuadro "
                            + "eliminatorio. Los grupos y sus resultados se "
                            + "conservaran.";
            if (!Dialogos.confirmarPeligro("Invalidar cuadro",
                    detalle + "\n\n¿Queres continuar?")) return;
            invalidacionService.invalidar(categoriaId);
            cargar();
            Dialogos.exito("Cuadro invalidado",
                    "Ahora podes corregir resultados grupales o generar una "
                            + "nueva propuesta eliminatoria.");
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
    private void corregirResultado() {
        TorneoPartido partido = seleccionado();
        if (partido == null) return;
        if (torneo == null || torneo.getEstado() != EstadoTorneo.EN_CURSO) {
            Dialogos.informacion("Correccion no disponible",
                    "Solo pueden corregirse resultados de un torneo en curso.");
            return;
        }
        if (!Dialogos.confirmarPeligro("Corregir resultado",
                "La correccion reemplazara el marcador y puede cambiar "
                        + "los ganadores.\n\nQueres continuar?")) {
            return;
        }
        TorneoPartido actualizado =
                new ResultadoPartidoTorneoDialog(partido, true).mostrar();
        if (actualizado != null) {
            cargar();
            seleccionarPorId(actualizado.getId());
            Dialogos.exito("Resultado corregido",
                    "El marcador y los ganadores fueron actualizados.");
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
        boolean torneoEnCurso = torneo != null
                && torneo.getEstado() == EstadoTorneo.EN_CURSO;
        botonCorregirResultado.setDisable(!hay || partido.isBye()
                || partido.getEstado() != EstadoPartidoTorneo.FINALIZADO
                || !torneoEnCurso);
        panelDetalleVacio.setVisible(!hay);
        panelDetalleVacio.setManaged(!hay);
        panelGestionPartido.setVisible(hay);
        panelGestionPartido.setManaged(hay);
        if (!hay) {
            panelProgramacion.setVisible(false);
            panelProgramacion.setManaged(false);
            scrollHistorial.setVisible(false);
            scrollHistorial.setManaged(false);
            etiquetaTituloPanel.setText("DETALLE DEL PARTIDO");
            etiquetaPartido.setText("Selecciona un partido");
            etiquetaEnfrentamiento.setText("");
            panelResultado.setVisible(false);
            panelResultado.setManaged(false);
            limpiarHistorial();
            limpiarFormulario();
            return;
        }
        boolean finalizado = partido.getEstado()
                == EstadoPartidoTorneo.FINALIZADO;
        boolean puedeRegistrar = !partido.isBye()
                && (partido.getEstado() == EstadoPartidoTorneo.PROGRAMADO
                    || partido.getEstado() == EstadoPartidoTorneo.EN_CURSO);
        panelProgramacion.setVisible(!finalizado && !partido.isBye());
        panelProgramacion.setManaged(!finalizado && !partido.isBye());
        botonQuitar.setVisible(
                partido.getEstado() == EstadoPartidoTorneo.PROGRAMADO);
        botonQuitar.setManaged(botonQuitar.isVisible());
        botonResultado.setVisible(puedeRegistrar);
        botonResultado.setManaged(puedeRegistrar);
        botonCorregirResultado.setVisible(finalizado);
        botonCorregirResultado.setManaged(finalizado);
        scrollHistorial.setVisible(finalizado);
        scrollHistorial.setManaged(finalizado);
        etiquetaPartido.setText(nombreFaseVisible(partido)
                + " · PARTIDO " + partido.getOrdenFase());
        etiquetaEnfrentamiento.setText(
                nombrePareja(partido.getPareja1InscripcionId())
                + "\nVS\n"
                + nombrePareja(partido.getPareja2InscripcionId()));
        botonProgramar.setText(partido.getEstado()
                == EstadoPartidoTorneo.PROGRAMADO
                        ? "ACTUALIZAR PROGRAMACIÓN"
                        : "GUARDAR PROGRAMACIÓN");
        selectorFecha.setValue(partido.getFecha());
        comboInicio.setValue(partido.getHoraInicio());
        comboFin.setValue(partido.getHoraFin());
        comboCancha.setValue(partido.getCanchaId() == null ? null
                : comboCancha.getItems().stream()
                    .filter(c -> c.getId() == partido.getCanchaId())
                    .findFirst().orElse(null));
        mostrarResultado(partido);
        cargarHistorial(partido);
    }

    private void cargarHistorial(TorneoPartido partido) {
        contenedorHistorial.getChildren().clear();
        try {
            List<CorreccionResultadoTorneo> historial =
                    historialService.listar(partido.getId());
            boolean tiene = !historial.isEmpty();
            panelHistorial.setVisible(tiene);
            panelHistorial.setManaged(tiene);
            etiquetaSinHistorial.setVisible(!tiene);
            etiquetaSinHistorial.setManaged(!tiene);
            for (CorreccionResultadoTorneo correccion : historial) {
                contenedorHistorial.getChildren().add(
                        crearTarjetaCorreccion(correccion));
            }
        } catch (RuntimeException exception) {
            panelHistorial.setVisible(true);
            panelHistorial.setManaged(true);
            etiquetaSinHistorial.setVisible(false);
            etiquetaSinHistorial.setManaged(false);
            Label error = new Label(exception.getMessage() == null
                    ? "No se pudo consultar el historial."
                    : exception.getMessage());
            error.setWrapText(true);
            error.getStyleClass().add("bracket-history-error");
            contenedorHistorial.getChildren().add(error);
        }
    }

    private javafx.scene.layout.VBox crearTarjetaCorreccion(
            CorreccionResultadoTorneo correccion) {
        Label fechaUsuario = new Label(formatearFechaUsuario(correccion));
        fechaUsuario.getStyleClass().add("bracket-history-meta");
        Label anterior = new Label(valorO(correccion.getResultadoAnterior(), "Sin resultado"));
        anterior.setWrapText(true);
        anterior.getStyleClass().add("bracket-history-old-score");
        Label flecha = new Label("↓");
        flecha.getStyleClass().add("bracket-history-arrow");
        Label nuevo = new Label(valorO(correccion.getResultadoNuevo(), "Sin resultado"));
        nuevo.setWrapText(true);
        nuevo.getStyleClass().add("bracket-history-new-score");
        Label ganadora = new Label("Ganadores anteriores: "
                + nombrePareja(correccion.getGanadoraAnteriorInscripcionId())
                + "\nGanadores nuevos: " + nombrePareja(correccion.getGanadoraNuevaInscripcionId()));
        ganadora.setWrapText(true);
        ganadora.getStyleClass().add("bracket-history-winner");
        String motivo = correccion.getMotivo() == null || correccion.getMotivo().isBlank()
                ? "Sin motivo informado" : correccion.getMotivo();
        Label motivoLabel = new Label("Motivo: " + motivo);
        motivoLabel.setWrapText(true);
        motivoLabel.getStyleClass().add("bracket-history-reason");
        javafx.scene.layout.VBox tarjeta = new javafx.scene.layout.VBox(
                5, fechaUsuario, anterior, flecha, nuevo, ganadora, motivoLabel);
        tarjeta.getStyleClass().add("bracket-history-entry");
        return tarjeta;
    }

    private String formatearFechaUsuario(CorreccionResultadoTorneo correccion) {
        String fecha = correccion.getFechaCorreccion() == null
                ? "Fecha sin registrar" : FECHA_HORA.format(correccion.getFechaCorreccion());
        return fecha + " · " + valorO(correccion.getUsuario(), "Usuario desconocido");
    }

    private String valorO(String valor, String alternativo) {
        return valor == null || valor.isBlank() ? alternativo : valor;
    }

    private void limpiarHistorial() {
        panelHistorial.setVisible(false);
        panelHistorial.setManaged(false);
        etiquetaSinHistorial.setVisible(false);
        etiquetaSinHistorial.setManaged(false);
        contenedorHistorial.getChildren().clear();
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
                setTooltip(vacia || texto == null
                        ? null : new Tooltip(texto));
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
                : "Campeones por definir");
        etiquetaResultadoFinal.setText(definida
                ? "Resultado de la final: " + resultado(finalPartido)
                : "La final todavía no tiene un resultado registrado.");
        tarjetaCampeona.setVisible(definida);
        tarjetaCampeona.setManaged(definida);
        etiquetaTrofeo.setVisible(definida);
        etiquetaTrofeo.setManaged(definida);
        tarjetaCampeona.getStyleClass().removeAll(
                "bracket-champion-pending-v2",
                "bracket-champion-defined-v2");
        tarjetaCampeona.getStyleClass().add(definida
                ? "bracket-champion-defined-v2"
                : "bracket-champion-pending-v2");
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
        filtrados.setPredicate(p -> p.getFase() != FaseTorneo.GRUPOS
                && (fase == null || p.getFase() == fase)
                && (estado == null || p.getEstado() == estado));
        long totalCuadro = partidos.stream()
                .filter(p -> p.getFase() != FaseTorneo.GRUPOS).count();
        long visiblesCuadro = filtrados.stream()
                .filter(p -> p.getFase() != FaseTorneo.GRUPOS).count();
        etiquetaResumen.setText(visiblesCuadro == totalCuadro
                ? textoPartidos(totalCuadro)
                : visiblesCuadro + " de " + textoPartidos(totalCuadro));
    }

    private void configurarAccionesCuadro(boolean existe) {
        botonGenerar.setVisible(!existe);
        botonGenerar.setManaged(!existe);
        botonGenerar.setDisable(existe);
        botonImprimir.setVisible(existe);
        botonImprimir.setManaged(existe);
        botonInvalidar.setVisible(existe);
        botonInvalidar.setManaged(existe);
        botonInvalidar.setDisable(!existe);
    }

    private String textoPartidos(long cantidad) {
        return cantidad + (cantidad == 1
                ? " partido del cuadro" : " partidos del cuadro");
    }

    private String colorFase(FaseTorneo fase) {
        if (fase == null) return "#aebbc1";
        return switch (fase) {
            case ACCESO_1, ACCESO_2, ACCESO_3, ACCESO_4, ACCESO_5 -> "#d79a55";
            case DIECISEISAVOS -> "#75aaa4";
            case OCTAVOS -> "#66b0b5";
            case CUARTOS -> "#6fa6c8";
            case SEMIFINAL -> "#a78bd1";
            case FINAL -> "#d8b84d";
            case GRUPOS -> "#8fa0a8";
        };
    }

    private String colorEstado(EstadoPartidoTorneo estado) {
        return switch (estado) {
            case PENDIENTE -> "#deb86a";
            case PROGRAMADO -> "#73b9df";
            case EN_CURSO -> "#7bd4ae";
            case FINALIZADO -> "#b6a9ec";
            case CANCELADO -> "#e88e99";
        };
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

    // corregir-cuadro-torneo-v2
    private void mostrarError(RuntimeException exception) {
        String mensaje = exception.getMessage() == null
                ? "No se pudo completar la operacion."
                : exception.getMessage();
        etiquetaMensaje.setText(mensaje);
        Dialogos.error("No se pudo completar la operacion", mensaje);
        if (!(exception instanceof IllegalArgumentException)) {
            exception.printStackTrace(System.err);
        }
    }

    @FXML
    private void irAGrupos() {
        if (categoria == null || !categoria.usaFaseGrupos()) {
            Dialogos.informacion("Grupos no disponibles",
                    "La categoria seleccionada no utiliza fase de grupos.");
            return;
        }
        Navegacion.mostrarGruposTorneo(categoriaId);
    }

    @FXML
    private void volver() {
        Navegacion.mostrarTorneos();
    }
}
