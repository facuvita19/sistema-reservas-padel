package vista.controlador;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import negocio.Cancha;
import negocio.EstadoPartidoTorneo;
import negocio.EstadoTorneo;
import negocio.ModoAsignacionGrupoTorneo;
import negocio.PosicionGrupoTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoGrupo;
import negocio.TorneoGrupoIntegrante;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import negocio.TorneoPartido;
import servicio.CanchaService;
import servicio.GeneracionPartidosGrupoService;
import servicio.GestionGruposTorneoService;
import servicio.ProgramacionPartidoTorneoService;
import servicio.BloqueoCorreccionGrupoService;
import servicio.PosicionesGrupoTorneoService;
import vista.Dialogos;
import vista.Navegacion;

public class TorneoGruposController {
    private final GestionGruposTorneoService service =
            new GestionGruposTorneoService();
    private final GeneracionPartidosGrupoService partidosService =
            new GeneracionPartidosGrupoService();
    private final BloqueoCorreccionGrupoService bloqueoCorreccionService =
            new BloqueoCorreccionGrupoService();
    private final ProgramacionPartidoTorneoService programacionService =
            new ProgramacionPartidoTorneoService();
    private final TorneoPartidoDAO partidoDAO = new TorneoPartidoDAOMySQL();
    private final TorneoInscripcionDAO inscripcionDAO =
            new TorneoInscripcionDAOMySQL();
    private final TorneoCategoriaDAO categoriaDAO =
            new TorneoCategoriaDAOMySQL();
    private final TorneoDAO torneoDAO = new TorneoDAOMySQL();
    private final CanchaService canchaService = new CanchaService();
    private final PosicionesGrupoTorneoService posicionesService =
            new PosicionesGrupoTorneoService();

    private long categoriaId;
    private GestionGruposTorneoService.Vista vista;
    private TorneoCategoria categoria;
    private Torneo torneo;
    private Long grupoSeleccionadoId;
    private Long partidoSeleccionadoId;

    @FXML private Label titulo;
    @FXML private Label resumen;
    @FXML private Label etiquetaModo;
    @FXML private Label mensaje;
    @FXML private Label resumenPartidos;
    @FXML private ComboBox<TorneoGrupo> selectorGrupoPartidos;
    @FXML private Label estadoPosiciones;
    @FXML private ListView<TorneoInscripcion> sinAsignar;
    @FXML private ListView<TorneoGrupo> listaGrupos;
    @FXML private ListView<TorneoGrupoIntegrante> integrantes;
    @FXML private ComboBox<ModoAsignacionGrupoTorneo> modo;
    @FXML private Button confirmar;
    @FXML private Button generarPartidos;
    @FXML private TableView<TorneoPartido> tablaPartidos;
    @FXML private TableColumn<TorneoPartido, String> colTipo;
    @FXML private TableColumn<TorneoPartido, Integer> colOrden;
    @FXML private TableColumn<TorneoPartido, String> colPareja1;
    @FXML private TableColumn<TorneoPartido, String> colPareja2;
    @FXML private TableColumn<TorneoPartido, String> colResultado;
    @FXML private TableColumn<TorneoPartido, EstadoPartidoTorneo> colEstado;
    @FXML private ComboBox<Cancha> comboCancha;
    @FXML private DatePicker selectorFecha;
    @FXML private ComboBox<LocalTime> comboInicio;
    @FXML private ComboBox<LocalTime> comboFin;
    @FXML private Button botonProgramar;
    @FXML private Button botonQuitarProgramacion;
    @FXML private Button botonResultado;
    @FXML private Button botonCorregir;
    @FXML private TableView<PosicionGrupoTorneo> tablaPosiciones;
    @FXML private TableColumn<PosicionGrupoTorneo, Integer> colPosicion;
    @FXML private TableColumn<PosicionGrupoTorneo, String> colParejaPosicion;
    @FXML private TableColumn<PosicionGrupoTorneo, Integer> colPJ;
    @FXML private TableColumn<PosicionGrupoTorneo, Integer> colPG;
    @FXML private TableColumn<PosicionGrupoTorneo, Integer> colPP;
    @FXML private TableColumn<PosicionGrupoTorneo, String> colSets;
    @FXML private TableColumn<PosicionGrupoTorneo, Integer> colDifSets;
    @FXML private TableColumn<PosicionGrupoTorneo, String> colGames;
    @FXML private TableColumn<PosicionGrupoTorneo, Integer> colDifGames;
    @FXML private TableColumn<PosicionGrupoTorneo, String> colClasificacion;
    @FXML private VBox panelArmado;
    @FXML private HBox panelPartidos;
    @FXML private VBox panelPosiciones;
    @FXML private VBox panelPartidoVacio;
    @FXML private VBox panelPartido;
    @FXML private VBox panelProgramacion;
    @FXML private HBox panelEdicionGrupos;
    @FXML private HBox panelConfirmado;
    @FXML private HBox accionesIntegrantes;
    @FXML private ToggleButton pestanaArmado;
    @FXML private ToggleButton pestanaPartidos;
    @FXML private ToggleButton pestanaPosiciones;
    @FXML private ComboBox<TorneoGrupo> selectorGrupoPosiciones;
    @FXML private Label etiquetaDisponibles;
    @FXML private Label etiquetaEstadoGrupos;
    @FXML private Label tituloIntegrantes;
    @FXML private Label etiquetaOcupacion;
    @FXML private Label ayudaModo;
    @FXML private Label ayudaPosiciones;
    @FXML private Label resumenClasificacion;
    @FXML private Label tituloPanelConfirmado;
    @FXML private Label ayudaPanelConfirmado;
    @FXML private Label tituloPartido;
    @FXML private Label pareja1Detalle;
    @FXML private Label pareja2Detalle;
    @FXML private Label estadoPartidoDetalle;
    @FXML private Button botonAgregar;
    @FXML private Button botonQuitar;
    @FXML private Button botonCabeza;
    @FXML private Button botonSortear;
    @FXML private Button botonIrCuadro;
    @FXML private Button botonEtapaConfirmada;
    @FXML private HBox bloqueCorreccion;

    @FXML
    private void initialize() {
        categoriaId = Navegacion.consumirCategoriaGruposTorneo();
        modo.setItems(FXCollections.observableArrayList(
                ModoAsignacionGrupoTorneo.values()));
        modo.setValue(ModoAsignacionGrupoTorneo.SORTEO_DIRIGIDO);
        configurarListas();
        configurarSelectorGrupoPartidos();
        configurarTablaPartidos();
        configurarTablaPosiciones();
        cargarCanchasYHorarios();
        configurarDeseleccion(sinAsignar);
        configurarDeseleccion(listaGrupos);
        configurarDeseleccion(integrantes);
        configurarInteraccionesModernas();
        configurarEstadosVacios();
        cargar();
    }

    private void configurarEstadosVacios() {
        sinAsignar.setPlaceholder(crearEstadoVacio(
                "DISTRIBUCIÓN COMPLETA",
                "Todas las parejas confirmadas fueron asignadas."));
        listaGrupos.setPlaceholder(crearEstadoVacio(
                "SIN GRUPOS",
                "La categoría todavía no tiene estructura grupal."));
        integrantes.setPlaceholder(crearEstadoVacio(
                "SIN INTEGRANTES",
                "Seleccioná un grupo para consultar sus parejas."));
    }

    private VBox crearEstadoVacio(String titulo, String detalle) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("groups-empty-title-v13");
        Label texto = new Label(detalle);
        texto.setWrapText(true);
        texto.setMaxWidth(260);
        texto.getStyleClass().add("groups-empty-copy-v13");
        VBox estado = new VBox(6, encabezado, texto);
        estado.setAlignment(Pos.CENTER);
        estado.getStyleClass().add("groups-empty-state-v13");
        return estado;
    }

    private void configurarInteraccionesModernas() {
        selectorGrupoPosiciones.setConverter(new StringConverter<>() {
            @Override public String toString(TorneoGrupo grupo) {
                return grupo == null ? "" : grupo.getNombre();
            }
            @Override public TorneoGrupo fromString(String texto) { return null; }
        });
        selectorGrupoPosiciones.valueProperty().addListener((o, a, grupo) -> {
            if (grupo != null) {
                listaGrupos.getSelectionModel().select(grupo);
                mostrarGrupo(grupo);
                javafx.application.Platform.runLater(() -> {
                    tablaPosiciones.getSelectionModel().clearSelection();
                    tablaPosiciones.requestFocus();
                });
            }
        });
        sinAsignar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && sinAsignar.getSelectionModel().getSelectedItem() != null) agregar();
        });
        integrantes.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && integrantes.getSelectionModel().getSelectedItem() != null) quitar();
        });
        modo.valueProperty().addListener((o, a, actual) -> actualizarModo());
        sinAsignar.getSelectionModel().selectedItemProperty()
                .addListener((o, anterior, actual) -> actualizarBotonAgregar());
        listaGrupos.getSelectionModel().selectedItemProperty()
                .addListener((o, anterior, actual) -> actualizarBotonAgregar());
    }

    @FXML private void mostrarArmado() { mostrarEtapa(panelArmado, pestanaArmado); actualizarMensajeEtapa(true); }
    @FXML private void mostrarPartidos() { mostrarEtapa(panelPartidos, pestanaPartidos); actualizarMensajeEtapa(false); }
    @FXML private void mostrarPosiciones() { mostrarEtapa(panelPosiciones, pestanaPosiciones); actualizarMensajeEtapa(false); }
    private void mostrarEtapa(Node panel, ToggleButton pestana) {
        for (Node nodo : List.of(panelArmado, panelPartidos, panelPosiciones)) {
            boolean visible = nodo == panel; nodo.setVisible(visible); nodo.setManaged(visible);
        }
        for (ToggleButton boton : List.of(pestanaArmado, pestanaPartidos, pestanaPosiciones)) boton.setSelected(boton == pestana);
    }
    private void actualizarMensajeEtapa(boolean armado) {
        boolean confirmado = vista != null && vista.grupos().stream()
                .anyMatch(TorneoGrupo::isConfirmado);
        if (armado && confirmado) mensaje.setText("GRUPOS CONFIRMADOS");
        else if (!armado && "Los grupos estan confirmados.".equals(mensaje.getText())) mensaje.setText("");
        else if (!armado && "GRUPOS CONFIRMADOS".equals(mensaje.getText())) mensaje.setText("");
    }

    private void actualizarModo() {
        boolean sorteo = modo.getValue() == ModoAsignacionGrupoTorneo.SORTEO_DIRIGIDO;
        botonSortear.setVisible(sorteo); botonSortear.setManaged(sorteo);
        ayudaModo.setText(sorteo ? "Definí una cabeza por grupo y sorteá las parejas restantes." : "Asigná manualmente todas las parejas y confirmá la distribución.");
    }

    private <T> void configurarDeseleccion(ListView<T> lista) {
        lista.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                evento -> {
                    Node nodo = evento.getPickResult().getIntersectedNode();
                    while (nodo != null && nodo != lista
                            && !(nodo instanceof ListCell<?>)) {
                        nodo = nodo.getParent();
                    }
                    if (!(nodo instanceof ListCell<?> celda)
                            || celda.isEmpty()) {
                        lista.getSelectionModel().clearSelection();
                        lista.getFocusModel().focus(-1);
                    }
                });
    }

    private void configurarListas() {
        sinAsignar.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(TorneoInscripcion valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : nombre(valor));
            }
        });
        listaGrupos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) {
                        grupoSeleccionadoId = actual.getId();
                        TorneoGrupo selectorActual = selectorGrupoPartidos.getValue();
                        if (selectorActual == null
                                || selectorActual.getId() != actual.getId()) {
                            selectorGrupoPartidos.setValue(actual);
                        }
                    }
                    mostrarGrupo(actual);
                });
    }

    private void configurarSelectorGrupoPartidos() {
        selectorGrupoPartidos.setConverter(new StringConverter<>() {
            @Override
            public String toString(TorneoGrupo grupo) {
                return grupo == null ? "" : grupo.getNombre();
            }

            @Override
            public TorneoGrupo fromString(String texto) {
                return null;
            }
        });
        selectorGrupoPartidos.valueProperty().addListener(
                (obs, anterior, actual) -> {
                    if (actual == null) return;
                    javafx.application.Platform.runLater(tablaPartidos::requestFocus);
                    TorneoGrupo seleccionado = listaGrupos
                            .getSelectionModel().getSelectedItem();
                    if (seleccionado == null
                            || seleccionado.getId() != actual.getId()) {
                        grupoSeleccionadoId = actual.getId();
                        listaGrupos.getSelectionModel().select(actual);
                        listaGrupos.scrollTo(actual);
                    }
                });
    }

    private void configurarTablaPartidos() {
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTipoPartidoGrupo() == null ? "-"
                        : d.getValue().getTipoPartidoGrupo().toString()));
        colOrden.setCellValueFactory(d -> new SimpleIntegerProperty(
                d.getValue().getOrdenFase()).asObject());
        colPareja1.setCellValueFactory(d -> new SimpleStringProperty(
                nombrePareja(d.getValue().getPareja1InscripcionId())));
        colPareja2.setCellValueFactory(d -> new SimpleStringProperty(
                nombrePareja(d.getValue().getPareja2InscripcionId())));
        colResultado.setCellValueFactory(d -> new SimpleStringProperty(
                resultado(d.getValue())));
        colEstado.setCellValueFactory(d -> new SimpleObjectProperty<>(
                d.getValue().getEstado()));
        colOrden.setStyle("-fx-alignment:CENTER;");
        colPareja1.setStyle("-fx-alignment:CENTER;");
        colPareja2.setStyle("-fx-alignment:CENTER;");
        colResultado.setStyle("-fx-alignment:CENTER;");
        colEstado.setStyle("-fx-alignment:CENTER;");
        colEstado.setCellFactory(x -> new TableCell<>() {
            @Override protected void updateItem(EstadoPartidoTorneo estado, boolean vacia) {
                super.updateItem(estado, vacia); setAlignment(Pos.CENTER); setStyle("");
                setText(vacia || estado == null ? null : estado.toString());
                if (!vacia && estado != null) setStyle("-fx-text-fill:" + colorPartido(estado) + ";-fx-font-weight:900;");
            }
        });
        colOrden.setCellFactory(columna -> new TableCell<>() {
            @Override protected void updateItem(Integer valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setStyle("-fx-font-weight:800;-fx-text-fill:#cdd6da;");
                setText(vacia || valor == null ? null : String.valueOf(valor));
            }
        });
        colPareja1.setCellFactory(columna -> celdaPareja());
        colPareja2.setCellFactory(columna -> celdaPareja());
        colResultado.setCellFactory(columna -> new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setWrapText(true);
                setText(vacia ? null : valor);
            }
        });
        tablaPartidos.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY);
        vincularAnchoPartido(colOrden, 0.07);
        vincularAnchoPartido(colPareja1, 0.305);
        vincularAnchoPartido(colPareja2, 0.305);
        vincularAnchoPartido(colResultado, 0.14);
        vincularAnchoPartido(colEstado, 0.14);
        tablaPartidos.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) partidoSeleccionadoId = actual.getId();
                    mostrarPartido(actual);
                });
        tablaPartidos.setRowFactory(tabla -> new TableRow<>());
        tablaPartidos.setOnMousePressed(evento -> {
            Node nodo = evento.getPickResult().getIntersectedNode();
            while (nodo != null && nodo != tablaPartidos
                    && !(nodo instanceof TableRow<?>)) {
                nodo = nodo.getParent();
            }
            if (!(nodo instanceof TableRow<?> fila) || fila.isEmpty()) {
                tablaPartidos.getSelectionModel().clearSelection();
                partidoSeleccionadoId = null;
            }
        });
    }

    private void configurarTablaPosiciones() {
        colPosicion.setCellValueFactory(d -> new SimpleIntegerProperty(
                d.getValue().posicion()).asObject());
        colParejaPosicion.setCellValueFactory(d -> new SimpleStringProperty(
                nombrePareja(d.getValue().inscripcionId())));
        colPJ.setCellValueFactory(d -> new SimpleIntegerProperty(
                d.getValue().partidosJugados()).asObject());
        colPG.setCellValueFactory(d -> new SimpleIntegerProperty(
                d.getValue().partidosGanados()).asObject());
        colPP.setCellValueFactory(d -> new SimpleIntegerProperty(
                d.getValue().partidosPerdidos()).asObject());
        colSets.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().setsResumen()));
        colDifSets.setCellValueFactory(d -> new SimpleIntegerProperty(
                d.getValue().diferenciaSets()).asObject());
        colGames.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().gamesResumen()));
        colDifGames.setCellValueFactory(d -> new SimpleIntegerProperty(
                d.getValue().diferenciaGames()).asObject());
        colClasificacion.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().estado().toString()));
        colClasificacion.setCellFactory(x -> new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setStyle("");
                setText(vacia ? null : valor);
                if (!vacia && valor != null) {
                    setStyle("-fx-text-fill:" + colorClasificacion(valor)
                            + ";-fx-font-weight:900;");
                }
            }
        });
        colParejaPosicion.setCellFactory(x -> new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setWrapText(true);
                setText(vacia ? null : valor);
                setStyle(vacia ? ""
                        : "-fx-font-size:12px;-fx-font-weight:700;"
                        + "-fx-text-fill:#e5eaec;");
                setPadding(new javafx.geometry.Insets(6, 12, 6, 12));
            }
        });
        configurarCeldaEntera(colPosicion, true);
        configurarCeldaEntera(colPJ, false);
        configurarCeldaEntera(colPG, false);
        configurarCeldaEntera(colPP, false);
        configurarCeldaEntera(colDifSets, false);
        configurarCeldaEntera(colDifGames, false);
        configurarCeldaTextoCentrada(colSets);
        configurarCeldaTextoCentrada(colGames);
        colParejaPosicion.setStyle("-fx-alignment:CENTER;");
        for (TableColumn<PosicionGrupoTorneo, ?> columna : List.of(
                colPosicion, colPJ, colPG, colPP, colSets,
                colDifSets, colGames, colDifGames, colClasificacion)) {
            columna.setStyle("-fx-alignment:CENTER;");
        }
        tablaPosiciones.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY);
        tablaPosiciones.getSelectionModel().setSelectionMode(
                javafx.scene.control.SelectionMode.SINGLE);
        tablaPosiciones.setOnMousePressed(evento ->
                tablaPosiciones.getSelectionModel().clearSelection());
        vincularAncho(colPosicion, 0.04);
        vincularAncho(colParejaPosicion, 0.30);
        vincularAncho(colPJ, 0.06);
        vincularAncho(colPG, 0.06);
        vincularAncho(colPP, 0.06);
        vincularAncho(colSets, 0.08);
        vincularAncho(colDifSets, 0.09);
        vincularAncho(colGames, 0.09);
        vincularAncho(colDifGames, 0.10);
        vincularAncho(colClasificacion, 0.085);
    }

    private void cargarCanchasYHorarios() {
        comboCancha.setItems(FXCollections.observableArrayList(
                canchaService.listar().stream().filter(Cancha::isActivo)
                        .toList()));
        var horas = FXCollections.<LocalTime>observableArrayList();
        for (int hora = 6; hora <= 23; hora++) {
            horas.add(LocalTime.of(hora, 0));
            if (hora < 23) horas.add(LocalTime.of(hora, 30));
        }
        comboInicio.setItems(horas);
        comboFin.setItems(FXCollections.observableArrayList(horas));
    }

    private String nombre(TorneoInscripcion inscripcion) {
        return inscripcion.getJugadores().stream()
                .sorted(Comparator.comparingInt(
                        TorneoInscripcionJugador::getOrdenIntegrante))
                .map(jugador -> jugador.getNombre() + " "
                        + jugador.getApellido())
                .reduce((a, b) -> a + " / " + b)
                .orElse("Inscripcion #" + inscripcion.getId());
    }

    private String nombrePareja(Long inscripcionId) {
        if (inscripcionId == null) return "Por definir";
        TorneoInscripcion inscripcion = inscripcionDAO.buscar(inscripcionId);
        return inscripcion == null ? "Inscripcion #" + inscripcionId
                : nombre(inscripcion);
    }

    private void cargar() {
        conservarSelecciones();
        try {
            vista = service.cargar(categoriaId);
            categoria = categoriaDAO.buscar(categoriaId);
            torneo = categoria == null ? null
                    : torneoDAO.buscar(categoria.getTorneoId());
            titulo.setText(vista.categoria().getNombre() + " - "
                    + vista.categoria().getRama());
            Set<Long> asignadas = new HashSet<>();
            vista.grupos().forEach(grupo -> grupo.getIntegrantes()
                    .forEach(i -> asignadas.add(i.getInscripcionId())));
            List<TorneoInscripcion> libres = vista.inscripciones().stream()
                    .filter(i -> !asignadas.contains(i.getId())).toList();
            sinAsignar.setItems(FXCollections.observableArrayList(libres));
            listaGrupos.setItems(FXCollections.observableArrayList(
                    vista.grupos()));
            selectorGrupoPartidos.setItems(FXCollections.observableArrayList(
                    vista.grupos()));
            selectorGrupoPosiciones.setItems(FXCollections.observableArrayList(vista.grupos()));
            resumen.setText(vista.inscripciones().size()
                    + " confirmadas · " + vista.grupos().size()
                    + " grupos · " + libres.size() + " sin asignar");
            boolean preparacion = torneo != null && torneo.getEstado() == EstadoTorneo.INSCRIPCION_CERRADA && categoria.isActivo();
            boolean competencia = torneo != null && torneo.getEstado() == EstadoTorneo.EN_CURSO && categoria.isActivo();
            boolean consulta = !preparacion && !competencia;
            boolean cerrado = vista.grupos().stream()
                    .anyMatch(TorneoGrupo::isConfirmado);
            boolean hayPartidos = vista.grupos().stream()
                    .anyMatch(g -> !partidoDAO.listarPorGrupo(g.getId()).isEmpty());
            boolean faseCompleta = hayPartidos && vista.grupos().stream()
                    .allMatch(this::grupoConPosicionesDefinitivas);
            boolean existeCuadro = partidoDAO.existeCuadroPorCategoria(categoriaId);
            actualizarEstadoCiclo(preparacion, competencia, consulta,
                    cerrado, faseCompleta, existeCuadro);
            actualizarPanelConfirmado(cerrado, hayPartidos, faseCompleta,
                    existeCuadro, consulta);
            confirmar.setDisable(cerrado);
            generarPartidos.setDisable(!cerrado);
            panelEdicionGrupos.setVisible(!cerrado && preparacion); panelEdicionGrupos.setManaged(!cerrado && preparacion);
            panelConfirmado.setVisible(cerrado); panelConfirmado.setManaged(cerrado);
            accionesIntegrantes.setVisible(!cerrado && preparacion); accionesIntegrantes.setManaged(!cerrado && preparacion);
            botonAgregar.setVisible(!cerrado && preparacion); botonAgregar.setManaged(!cerrado && preparacion);
            actualizarBotonAgregar();
            etiquetaEstadoGrupos.setText(cerrado ? "CONFIRMADOS" : "EN EDICIÓN");
            etiquetaDisponibles.setText(String.valueOf(libres.size()));
            actualizarModo();
            restaurarGrupo();
            generarPartidos.setVisible(cerrado && !hayPartidos && preparacion); generarPartidos.setManaged(cerrado && !hayPartidos && preparacion);
            pestanaPartidos.setDisable(!cerrado || !hayPartidos);
            pestanaPosiciones.setDisable(!cerrado || !hayPartidos);
            if (cerrado) mensaje.setText("Los grupos estan confirmados.");
            if (hayPartidos) mostrarPartidos(); else mostrarArmado();
        } catch (RuntimeException exception) {
            mostrarError("No se pudieron cargar los grupos", exception);
        }
    }

    private boolean grupoConPosicionesDefinitivas(TorneoGrupo grupo) {
        try {
            var resultado = posicionesService.calcular(grupo);
            return resultado.definitivo() && !resultado.desempatePendiente();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private void actualizarEstadoCiclo(boolean preparacion,
            boolean competencia, boolean consulta, boolean cerrado,
            boolean faseCompleta, boolean existeCuadro) {
        etiquetaModo.getStyleClass().removeAll(
                "groups-mode-manage-v7", "groups-mode-readonly-v7",
                "groups-mode-ready-v13", "groups-mode-live-v13");
        if (consulta) {
            etiquetaModo.setText("MODO CONSULTA");
            etiquetaModo.getStyleClass().add("groups-mode-readonly-v7");
        } else if (competencia) {
            etiquetaModo.setText(faseCompleta
                    ? "FASE DE GRUPOS COMPLETADA"
                    : "COMPETENCIA EN CURSO");
            etiquetaModo.getStyleClass().add(faseCompleta
                    ? "groups-mode-ready-v13" : "groups-mode-live-v13");
        } else if (cerrado) {
            etiquetaModo.setText("ESTRUCTURA LISTA");
            etiquetaModo.getStyleClass().add("groups-mode-ready-v13");
        } else {
            etiquetaModo.setText("PREPARACIÓN");
            etiquetaModo.getStyleClass().add("groups-mode-manage-v7");
        }
        botonIrCuadro.getStyleClass().remove("groups-bracket-primary-v13");
        if (existeCuadro) {
            botonIrCuadro.setText(consulta ? "VER CUADRO" : "IR AL CUADRO");
            botonIrCuadro.getStyleClass().add("groups-bracket-primary-v13");
        } else if (faseCompleta) {
            botonIrCuadro.setText("GENERAR CUADRO");
            botonIrCuadro.getStyleClass().add("groups-bracket-primary-v13");
        } else {
            botonIrCuadro.setText("CUADRO");
        }
    }

    private void actualizarPanelConfirmado(boolean cerrado,
            boolean hayPartidos, boolean faseCompleta,
            boolean existeCuadro, boolean consulta) {
        if (!cerrado) return;
        if (faseCompleta) {
            tituloPanelConfirmado.setText("FASE DE GRUPOS COMPLETADA");
            ayudaPanelConfirmado.setText(existeCuadro
                    ? "Las posiciones son definitivas y la etapa eliminatoria ya está disponible."
                    : "Las posiciones son definitivas. Continuá con la etapa eliminatoria.");
            botonEtapaConfirmada.setText(existeCuadro
                    ? (consulta ? "VER CUADRO" : "IR AL CUADRO")
                    : "VER POSICIONES");
        } else if (hayPartidos) {
            tituloPanelConfirmado.setText("FASE DE GRUPOS EN CURSO");
            ayudaPanelConfirmado.setText(
                    "Completá los partidos para definir las posiciones y clasificados.");
            botonEtapaConfirmada.setText("VER PARTIDOS");
        } else {
            tituloPanelConfirmado.setText("GRUPOS CONFIRMADOS");
            ayudaPanelConfirmado.setText(
                    "La distribución quedó bloqueada. Generá los partidos para continuar.");
            botonEtapaConfirmada.setText("VER ARMADO");
        }
    }

    @FXML
    private void abrirEtapaConfirmada() {
        boolean existeCuadro = partidoDAO.existeCuadroPorCategoria(categoriaId);
        boolean faseCompleta = vista != null && !vista.grupos().isEmpty()
                && vista.grupos().stream()
                        .allMatch(this::grupoConPosicionesDefinitivas);
        if (faseCompleta && existeCuadro) {
            irAlCuadro();
        } else if (faseCompleta) {
            mostrarPosiciones();
        } else if (vista != null && vista.grupos().stream()
                .anyMatch(g -> !partidoDAO.listarPorGrupo(g.getId()).isEmpty())) {
            mostrarPartidos();
        } else {
            mostrarArmado();
        }
    }

    private void conservarSelecciones() {
        TorneoGrupo grupo = listaGrupos.getSelectionModel().getSelectedItem();
        if (grupo != null) grupoSeleccionadoId = grupo.getId();
        TorneoPartido partido = tablaPartidos.getSelectionModel()
                .getSelectedItem();
        if (partido != null) partidoSeleccionadoId = partido.getId();
    }

    private void restaurarGrupo() {
        if (vista.grupos().isEmpty()) {
            grupoSeleccionadoId = null;
            integrantes.getItems().clear();
            tablaPartidos.getItems().clear();
            return;
        }
        TorneoGrupo grupo = vista.grupos().stream()
                .filter(g -> grupoSeleccionadoId != null
                        && g.getId() == grupoSeleccionadoId)
                .findFirst().orElse(vista.grupos().get(0));
        listaGrupos.getSelectionModel().select(grupo);
        selectorGrupoPartidos.setValue(grupo);
        grupoSeleccionadoId = grupo.getId();
    }

    private void mostrarGrupo(TorneoGrupo grupo) {
        integrantes.setItems(FXCollections.observableArrayList(
                grupo == null ? List.of() : grupo.getIntegrantes()));
        tituloIntegrantes.setText(grupo == null ? "INTEGRANTES" : grupo.getNombre().toUpperCase());
        etiquetaOcupacion.setText(grupo == null ? "0/0" : grupo.getIntegrantes().size() + "/" + grupo.getCapacidad());
        if (grupo != null) selectorGrupoPosiciones.setValue(grupo);
        cargarPartidos(grupo);
        cargarPosiciones(grupo);
    }

    private void cargarPosiciones(TorneoGrupo grupo) {
        if (grupo == null) {
            tablaPosiciones.getItems().clear();
            estadoPosiciones.setText("Selecciona un grupo");
            resumenClasificacion.setText("Sin clasificación");
            ayudaPosiciones.setText("Seleccioná un grupo para consultar la tabla");
            return;
        }
        var resultado = posicionesService.calcular(grupo);
        tablaPosiciones.setItems(FXCollections.observableArrayList(
                resultado.posiciones()));
        String estadoTabla = resultado.desempatePendiente()
                ? "Desempate pendiente"
                : resultado.definitivo()
                    ? "Posiciones definitivas"
                    : "Posiciones provisorias";
        estadoPosiciones.setText(estadoTabla.toUpperCase());
        estadoPosiciones.setStyle("-fx-text-fill:"
                + (resultado.desempatePendiente() ? "#deb86a"
                    : resultado.definitivo() ? "#7bd4ae" : "#86bde0")
                + ";-fx-font-weight:900;");
        int clasifican = grupo.getCapacidad() == 3 ? 2 : 3;
        resumenClasificacion.setText("Clasifican " + clasifican
                + (clasifican == 1 ? " pareja" : " parejas"));
        long pendientes = partidoDAO.listarPorGrupo(grupo.getId()).stream()
                .filter(p -> p.getEstado() != EstadoPartidoTorneo.FINALIZADO
                        && p.getEstado() != EstadoPartidoTorneo.CANCELADO)
                .count();
        String progreso = resultado.definitivo()
                ? "Fase completada"
                : pendientes + (pendientes == 1
                    ? " partido pendiente" : " partidos pendientes");
        ayudaPosiciones.setText(grupo.getNombre() + " · "
                + grupo.getIntegrantes().size() + " parejas · " + progreso);
    }

    private void cargarPartidos(TorneoGrupo grupo) {
        if (grupo == null) {
            tablaPartidos.getItems().clear();
            resumenPartidos.setText("Selecciona un grupo");
            mostrarPartido(null);
            return;
        }
        List<TorneoPartido> valores = partidoDAO.listarPorGrupo(grupo.getId());
        tablaPartidos.setItems(FXCollections.observableArrayList(valores));
        resumenPartidos.setText(valores.size() + (valores.size() == 1 ? " partido" : " partidos"));
        TorneoPartido seleccionar = valores.stream()
                .filter(p -> partidoSeleccionadoId != null
                        && p.getId() == partidoSeleccionadoId)
                .findFirst().orElse(null);
        if (seleccionar != null) {
            tablaPartidos.getSelectionModel().select(seleccionar);
        } else {
            partidoSeleccionadoId = null;
            tablaPartidos.getSelectionModel().clearSelection();
            mostrarPartido(null);
        }
        generarPartidos.setDisable(!grupo.isConfirmado()
                || !valores.isEmpty());
    }

    private void mostrarPartido(TorneoPartido partido) {
        boolean hay = partido != null;
        boolean completo = hay && partido.tieneDosParejas();
        boolean competencia = torneo != null && torneo.getEstado() == EstadoTorneo.EN_CURSO
                && categoria != null && categoria.isActivo();
        botonProgramar.setDisable(!competencia || !completo
                || partido.getEstado() != EstadoPartidoTorneo.PENDIENTE);
        botonQuitarProgramacion.setDisable(!competencia || !hay
                || partido.getEstado() != EstadoPartidoTorneo.PROGRAMADO);
        botonResultado.setDisable(!competencia || !completo
                || (partido.getEstado() != EstadoPartidoTorneo.PROGRAMADO
                    && partido.getEstado()
                        != EstadoPartidoTorneo.EN_CURSO));
        boolean permiteCorreccion = competencia;
        botonCorregir.setDisable(!hay || !permiteCorreccion
                || partido.getEstado() != EstadoPartidoTorneo.FINALIZADO);
        panelPartidoVacio.setVisible(!hay); panelPartidoVacio.setManaged(!hay);
        panelPartido.setVisible(hay); panelPartido.setManaged(hay);
        if (!hay) {
            limpiarProgramacion();
            return;
        }
        boolean finalizado = partido.getEstado() == EstadoPartidoTorneo.FINALIZADO;
        boolean puedeResultado = partido.getEstado() == EstadoPartidoTorneo.PROGRAMADO || partido.getEstado() == EstadoPartidoTorneo.EN_CURSO;
        panelProgramacion.setVisible(!finalizado); panelProgramacion.setManaged(!finalizado);
        botonQuitarProgramacion.setVisible(partido.getEstado() == EstadoPartidoTorneo.PROGRAMADO); botonQuitarProgramacion.setManaged(botonQuitarProgramacion.isVisible());
        botonResultado.setVisible(puedeResultado); botonResultado.setManaged(puedeResultado);
        botonCorregir.setVisible(finalizado); botonCorregir.setManaged(finalizado);
        bloqueCorreccion.setVisible(finalizado); bloqueCorreccion.setManaged(finalizado);
        TorneoGrupo grupoActual = listaGrupos.getSelectionModel().getSelectedItem();
        tituloPartido.setText("PARTIDO " + partido.getOrdenFase()
                + (grupoActual == null ? "" : " · " + grupoActual.getNombre().toUpperCase()));
        pareja1Detalle.setText(nombrePareja(partido.getPareja1InscripcionId()));
        pareja2Detalle.setText(nombrePareja(partido.getPareja2InscripcionId()));
        estadoPartidoDetalle.setText(partido.getEstado().toString().toUpperCase());
        estadoPartidoDetalle.setStyle("-fx-text-fill:" + colorPartido(partido.getEstado())
                + ";-fx-font-weight:900;");
        comboCancha.setValue(partido.getCanchaId() == null ? null
                : comboCancha.getItems().stream()
                    .filter(c -> c.getId() == partido.getCanchaId())
                    .findFirst().orElse(null));
        selectorFecha.setValue(partido.getFecha());
        comboInicio.setValue(partido.getHoraInicio());
        comboFin.setValue(partido.getHoraFin());
    }

    private void actualizarBotonAgregar() {
        if (botonAgregar == null) return;
        boolean seleccionCompleta = sinAsignar != null
                && sinAsignar.getSelectionModel().getSelectedItem() != null
                && listaGrupos != null
                && listaGrupos.getSelectionModel().getSelectedItem() != null;
        botonAgregar.setDisable(!seleccionCompleta || !botonAgregar.isVisible());
    }

    @FXML
    private void agregar() {
        TorneoGrupo grupo = listaGrupos.getSelectionModel().getSelectedItem();
        TorneoInscripcion inscripcion =
                sinAsignar.getSelectionModel().getSelectedItem();
        if (grupo == null || inscripcion == null) {
            informar("Seleccion requerida",
                    "Selecciona una pareja sin asignar y el grupo de destino.");
            return;
        }
        ejecutar("No se pudo agregar la pareja", () -> {
            service.asignar(categoriaId, grupo.getId(),
                    inscripcion.getId(), false);
            cargar();
            exito("Pareja agregada a " + grupo.getNombre() + ".");
        });
    }

    @FXML
    private void agregarCabezaSeleccionada() {
        TorneoGrupo grupo = listaGrupos.getSelectionModel().getSelectedItem();
        TorneoGrupoIntegrante integrante = integrantes.getSelectionModel().getSelectedItem();
        if (grupo == null || integrante == null) {
            informar("Seleccion requerida", "Selecciona una pareja del grupo.");
            return;
        }
        if (grupo.getIntegrantes().stream().anyMatch(TorneoGrupoIntegrante::isCabezaSerie)) {
            informar("Cabeza ya definida", "El grupo ya tiene una cabeza de serie. Quitala y volve a asignar la pareja si queres cambiarla.");
            return;
        }
        ejecutar("No se pudo marcar la cabeza de serie", () -> {
            service.quitar(categoriaId, integrante.getInscripcionId());
            service.asignar(categoriaId, grupo.getId(), integrante.getInscripcionId(), true);
            cargar();
            exito("Cabeza de serie definida para " + grupo.getNombre() + ".");
        });
    }

    @FXML
    private void quitar() {
        TorneoGrupoIntegrante integrante =
                integrantes.getSelectionModel().getSelectedItem();
        if (integrante == null) {
            informar("Seleccion requerida",
                    "Selecciona una pareja del grupo para quitarla.");
            return;
        }
        ejecutar("No se pudo quitar la pareja", () -> {
            service.quitar(categoriaId, integrante.getInscripcionId());
            cargar();
            exito("La pareja volvio a la lista sin asignar.");
        });
    }

    @FXML
    private void sortear() {
        Map<Long, Long> cabezas = new LinkedHashMap<>();
        for (TorneoGrupo grupo : vista.grupos()) {
            TorneoGrupoIntegrante seleccionado = grupo.getIntegrantes().stream()
                    .filter(TorneoGrupoIntegrante::isCabezaSerie)
                    .findFirst().orElse(null);
            if (seleccionado != null) {
                cabezas.put(grupo.getId(), seleccionado.getInscripcionId());
            }
        }
        if (cabezas.size() != vista.grupos().size()) {
            informar("Cabezas de serie",
                    "Marca un cabeza de serie en cada grupo antes de sortear.");
            return;
        }
        ejecutar("No se pudo realizar el sorteo", () -> {
            service.sortear(categoriaId, cabezas);
            cargar();
            exito("Sorteo realizado. Revisa la distribucion.");
        });
    }

    @FXML
    private void confirmar() {
        if (modo.getValue() == null) {
            informar("Modo requerido", "Selecciona el modo de asignacion.");
            return;
        }
        if (!Dialogos.confirmar("Confirmar grupos",
                "La composicion quedara bloqueada. ¿Queres continuar?")) {
            return;
        }
        ejecutar("No se pudieron confirmar los grupos", () -> {
            service.confirmar(categoriaId, modo.getValue());
            cargar();
            Dialogos.exito("Grupos confirmados",
                    "La composicion quedo guardada y bloqueada.");
        });
    }

    @FXML
    private void generarPartidos() {
        if (!Dialogos.confirmar("Generar partidos de grupos",
                "Se crearan los cruces de todos los grupos confirmados. "
                        + "Esta accion no puede repetirse. ¿Queres continuar?")) {
            return;
        }
        ejecutar("No se pudieron generar los partidos", () -> {
            int cantidad = partidosService.generar(categoriaId);
            cargar();
            exito(cantidad + " partidos generados correctamente.");
            Dialogos.exito("Partidos generados",
                    cantidad + " partidos quedaron preparados.");
        });
    }

    @FXML
    private void programarPartido() {
        TorneoPartido partido = partidoSeleccionado();
        if (partido == null) return;
        ejecutar("No se pudo programar el partido", () -> {
            if (comboCancha.getValue() == null
                    || selectorFecha.getValue() == null
                    || comboInicio.getValue() == null
                    || comboFin.getValue() == null) {
                throw new IllegalArgumentException(
                        "Selecciona cancha, fecha y horario completo.");
            }
            partidoSeleccionadoId = partido.getId();
            programacionService.programar(partido.getId(),
                    comboCancha.getValue().getId(),
                    selectorFecha.getValue(), comboInicio.getValue(),
                    comboFin.getValue());
            cargar();
            Dialogos.exito("Partido programado",
                    "La programacion se guardo correctamente.");
        });
    }

    @FXML
    private void quitarProgramacion() {
        TorneoPartido partido = partidoSeleccionado();
        if (partido == null) return;
        if (!Dialogos.confirmarPeligro("Quitar programacion",
                "El partido volvera a estado Pendiente. ¿Queres continuar?")) {
            return;
        }
        ejecutar("No se pudo quitar la programacion", () -> {
            partidoSeleccionadoId = partido.getId();
            programacionService.quitarProgramacion(partido.getId());
            cargar();
            Dialogos.exito("Programacion eliminada",
                    "El partido volvio a estado Pendiente.");
        });
    }

    @FXML
    private void registrarResultado() {
        TorneoPartido partido = partidoSeleccionado();
        if (partido == null) return;
        TorneoPartido actualizado =
                new ResultadoPartidoTorneoDialog(partido).mostrar();
        if (actualizado != null) {
            partidoSeleccionadoId = actualizado.getId();
            cargar();
            Dialogos.exito("Resultado registrado",
                    "El resultado y los cruces dependientes se actualizaron.");
        }
    }

    @FXML
    private void corregirResultado() {
        TorneoPartido partido = partidoSeleccionado();
        if (partido == null) return;
        if (torneo == null
                || (torneo.getEstado() != EstadoTorneo.INSCRIPCION_CERRADA
                    && torneo.getEstado() != EstadoTorneo.EN_CURSO)) {
            informar("Correccion no disponible",
                    "El torneo debe tener las inscripciones cerradas "
                            + "o estar en curso.");
            return;
        }
        try {
            bloqueoCorreccionService.validar(partido.getId());
        } catch (RuntimeException exception) {
            mostrarError("Correccion bloqueada", exception);
            return;
        }
        if (!Dialogos.confirmarPeligro("Corregir resultado",
                "La correccion puede modificar cruces dependientes. "
                        + "¿Queres continuar?")) return;
        TorneoPartido actualizado =
                new ResultadoPartidoTorneoDialog(partido, true).mostrar();
        if (actualizado != null) {
            partidoSeleccionadoId = actualizado.getId();
            cargar();
            Dialogos.exito("Resultado corregido",
                    "El resultado fue actualizado correctamente.");
        }
    }

    private TorneoPartido partidoSeleccionado() {
        TorneoPartido partido = tablaPartidos.getSelectionModel()
                .getSelectedItem();
        if (partido == null) {
            informar("Seleccion requerida",
                    "Selecciona un partido del grupo.");
        }
        return partido;
    }

    private String resultado(TorneoPartido partido) {
        if (partido.getSets().isEmpty()) return "Sin resultado";
        return partido.getSets().stream()
                .map(set -> set.getPuntosPareja1() + "-"
                        + set.getPuntosPareja2())
                .reduce((a, b) -> a + " / " + b)
                .orElse("Sin resultado");
    }

    private void limpiarProgramacion() {
        comboCancha.getSelectionModel().clearSelection();
        selectorFecha.setValue(null);
        comboInicio.getSelectionModel().clearSelection();
        comboFin.getSelectionModel().clearSelection();
        botonProgramar.setDisable(true);
        botonQuitarProgramacion.setDisable(true);
        botonResultado.setDisable(true);
        botonCorregir.setDisable(true);
        if (bloqueCorreccion != null) {
            bloqueCorreccion.setVisible(false);
            bloqueCorreccion.setManaged(false);
        }
    }

    private void vincularAnchoPartido(
            TableColumn<TorneoPartido, ?> columna,
            double porcentaje) {
        columna.prefWidthProperty().bind(
                tablaPartidos.widthProperty().multiply(porcentaje));
        columna.setResizable(false);
        columna.setReorderable(false);
    }

    private TableCell<TorneoPartido, String> celdaPareja() {
        return new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setWrapText(true);
                setText(vacia ? null : valor);
                setStyle(vacia ? ""
                        : "-fx-font-size:11.5px;-fx-font-weight:700;"
                        + "-fx-text-fill:#e5eaec;");
                setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
            }
        };
    }

    private void vincularAncho(
            TableColumn<PosicionGrupoTorneo, ?> columna,
            double porcentaje) {
        columna.prefWidthProperty().bind(
                tablaPosiciones.widthProperty().multiply(porcentaje));
        columna.setResizable(false);
        columna.setReorderable(false);
    }

    private int cantidadClasificadosGrupoActual() {
        TorneoGrupo grupo = selectorGrupoPosiciones == null
                ? null : selectorGrupoPosiciones.getValue();
        if (grupo == null) {
            grupo = listaGrupos.getSelectionModel().getSelectedItem();
        }
        return grupo != null && grupo.getCapacidad() == 4 ? 3 : 2;
    }

    private void configurarCeldaEntera(
            TableColumn<PosicionGrupoTorneo, Integer> columna,
            boolean destacarPosicion) {
        columna.setCellFactory(x -> new TableCell<>() {
            @Override protected void updateItem(Integer valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setStyle("");
                setText(vacia || valor == null ? null : String.valueOf(valor));
                if (!vacia && destacarPosicion && valor != null) {
                    int clasifican = cantidadClasificadosGrupoActual();
                    String color = valor <= clasifican
                            ? "#78c9a3"
                            : "#d9858f";
                    setStyle("-fx-text-fill:" + color
                            + ";-fx-font-weight:900;");
                }
            }
        });
    }

    private void configurarCeldaTextoCentrada(
            TableColumn<PosicionGrupoTorneo, String> columna) {
        columna.setCellFactory(x -> new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setText(vacia ? null : valor);
            }
        });
    }

    private String colorPartido(EstadoPartidoTorneo estado) {
        return switch (estado) { case PENDIENTE -> "#deb86a"; case PROGRAMADO -> "#73b9df"; case EN_CURSO -> "#7bd4ae"; case FINALIZADO -> "#b6a9ec"; case CANCELADO -> "#e88e99"; };
    }
    private String colorClasificacion(String estado) {
        if (estado.contains("Clasificado")) return "#7bd4ae";
        if (estado.contains("Desempate")) return "#deb86a";
        if (estado.contains("Eliminado")) return "#d9828d";
        return "#86bde0";
    }

    private void ejecutar(String tituloError, Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException exception) {
            mostrarError(tituloError, exception);
        }
    }

    private void mostrarError(String tituloError, RuntimeException exception) {
        String texto = mensaje(exception);
        mensaje.setText(texto);
        Dialogos.error(tituloError, texto);
    }

    private void informar(String tituloInfo, String texto) {
        mensaje.setText(texto);
        Dialogos.informacion(tituloInfo, texto);
    }

    private void exito(String texto) {
        mensaje.setText(texto);
    }

    private String mensaje(RuntimeException exception) {
        if (exception.getMessage() != null
                && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }
        Throwable causa = exception.getCause();
        if (causa != null && causa.getMessage() != null
                && !causa.getMessage().isBlank()) {
            return causa.getMessage();
        }
        return "No se pudo completar la operacion.";
    }

    @FXML
    private void irAlCuadro() {
        Navegacion.mostrarCuadroTorneo(categoriaId);
    }

    @FXML
    private void volver() {
        Navegacion.mostrarTorneos();
    }
}
