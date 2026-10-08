package vista.controlador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import negocio.EstadoTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import servicio.GestionTorneoService;
import util.FormateadorMoneda;
import vista.Dialogos;
import vista.Navegacion;

public class TorneosController {
    // corregir-tooltip-categoria-deshabilitada-v10
    // corregir-compilacion-torneos-v6
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final TorneoDAO torneoDAO = new TorneoDAOMySQL();
    private final TorneoCategoriaDAO categoriaDAO = new TorneoCategoriaDAOMySQL();
    private final GestionTorneoService gestionService = new GestionTorneoService();
    private final ObservableList<Torneo> torneos = FXCollections.observableArrayList();
    private final ObservableList<TorneoCategoria> categorias = FXCollections.observableArrayList();
    private FilteredList<Torneo> filtrados;
    private Torneo seleccionado;

    @FXML private TextField campoBuscar;
    @FXML private ComboBox<EstadoTorneo> filtroEstado;
    @FXML private TableView<Torneo> tablaTorneos;
    @FXML private TableColumn<Torneo, String> colNombre;
    @FXML private TableColumn<Torneo, LocalDate> colInicio;
    @FXML private TableColumn<Torneo, LocalDate> colFin;
    @FXML private TableColumn<Torneo, EstadoTorneo> colEstado;
    @FXML private TableView<TorneoCategoria> tablaCategorias;
    @FXML private TableColumn<TorneoCategoria, String> colCategoria;
    @FXML private TableColumn<TorneoCategoria, String> colRama;
    @FXML private TableColumn<TorneoCategoria, String> colFormato;
    @FXML private TableColumn<TorneoCategoria, String> colCategoriaEstado;
    @FXML private TableColumn<TorneoCategoria, Integer> colCupo;
    @FXML private TableColumn<TorneoCategoria, Integer> colConfirmadas;
    @FXML private TableColumn<TorneoCategoria, Integer> colDisponibles;
    @FXML private TableColumn<TorneoCategoria, String> colPrecio;
    @FXML private Label detalleNombre;
    @FXML private Label detalleEstado;
    @FXML private Label detalleFechas;
    @FXML private Label detalleInscripcion;
    @FXML private Label etiquetaMensaje;
    @FXML private Label detalleCantidadCategorias;
    @FXML private Label detalleParejas;
    @FXML private Label detalleCupos;
    @FXML private Label etiquetaAccionTitulo;
    @FXML private Label etiquetaAccionAyuda;
    @FXML private Label etiquetaCategoriaSeleccionada;
    @FXML private Label etiquetaFormatoCategoria;
    @FXML private Label etiquetaPreparacion;
    @FXML private Button botonLimpiar;
    @FXML private Button botonActualizar;
    @FXML private VBox panelSinSeleccion;
    @FXML private VBox panelDetalle;
    @FXML private HBox panelAccionesCategoria;
    @FXML private ScrollPane scrollDetalle;
    @FXML private Button botonEditar;
    @FXML private StackPane contenedorNuevaCategoria;
    @FXML private Button botonNuevaCategoria;
    @FXML private Button botonEditarCategoria;
    @FXML private Button botonDesactivarCategoria;
    @FXML private Button botonGestionarCuadro;
    @FXML private Button botonGestionarGrupos;
    @FXML private Button botonPublicar;
    @FXML private Button botonAbrir;
    @FXML private Button botonCerrar;
    @FXML private Button botonIniciar;
    @FXML private Button botonFinalizar;
    @FXML private Button botonCancelar;

    @FXML private void initialize() {
        configurarTablas();
        configurarFiltros();
        cargarTorneos();
        mostrarSinSeleccion();
        javafx.application.Platform.runLater(this::configurarDeseleccion);
    }

    private void configurarTablas() {
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colInicio.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFechaInicio()));
        colFin.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFechaFin()));
        colEstado.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getEstado()));
        configurarCeldaFecha(colInicio);
        configurarCeldaFecha(colFin);
        colNombre.setStyle("-fx-alignment: CENTER_LEFT;");
        colInicio.setStyle("-fx-alignment: CENTER;");
        colFin.setStyle("-fx-alignment: CENTER;");
        colEstado.setStyle("-fx-alignment: CENTER;");
        colEstado.setCellFactory(columna -> new TableCell<>() {
            @Override protected void updateItem(EstadoTorneo estado, boolean vacia) {
                super.updateItem(estado, vacia);
                setAlignment(Pos.CENTER);
                setStyle("");
                setText(vacia || estado == null ? null : estado.toString());
                getStyleClass().removeIf(clase -> clase.startsWith("tournament-status-"));
                if (!vacia && estado != null) {
                    getStyleClass().add("tournament-status-"
                            + estado.name().toLowerCase(Locale.ROOT).replace('_', '-'));
                    setStyle("-fx-text-fill: " + colorEstado(estado) + "; -fx-font-weight: 900;");
                }
            }
        });
        tablaTorneos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaTorneos.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> seleccionar(n));
        tablaTorneos.setRowFactory(tabla -> {
            TableRow<Torneo> fila = new TableRow<>();
            fila.setOnMousePressed(evento -> {
                if (!fila.isEmpty() && fila.getItem() != null) {
                    tablaTorneos.getSelectionModel().select(fila.getItem());
                    tablaTorneos.requestFocus();
                }
            });
            Tooltip tooltip = new Tooltip();
            fila.itemProperty().addListener((o, anterior, actual) -> {
                if (actual == null) fila.setTooltip(null);
                else { tooltip.setText(actual.getNombre()); fila.setTooltip(tooltip); }
            });
            return fila;
        });

        colCategoria.setStyle("-fx-alignment: CENTER;");
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colCategoria.setCellFactory(c -> celdaTexto(Pos.CENTER));
        colRama.setStyle("-fx-alignment: CENTER;");
        colRama.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRama())));
        colRama.setCellFactory(c -> celdaTexto(Pos.CENTER));
        colFormato.setStyle("-fx-alignment: CENTER;");
        colFormato.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().usaFaseGrupos()
                        ? "Grupos + eliminación" : "Eliminación directa"));
        colFormato.setCellFactory(c -> celdaTexto(Pos.CENTER));
        colCategoriaEstado.setStyle("-fx-alignment: CENTER;");
        colCategoriaEstado.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isActivo() ? "Activa" : "Inactiva"));
        colCategoriaEstado.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                getStyleClass().removeAll("tournaments-category-active-v4",
                        "tournaments-category-inactive-v4");
                setText(vacia ? null : valor);
                if (!vacia && valor != null) getStyleClass().add(
                        "Activa".equals(valor)
                                ? "tournaments-category-active-v4"
                                : "tournaments-category-inactive-v4");
            }
        });
        colCupo.setStyle("-fx-alignment: CENTER;");
        colCupo.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCupoParejas()).asObject());
        colCupo.setCellFactory(c -> celdaEnteroCentrada());
        colConfirmadas.setStyle("-fx-alignment: CENTER;");
        colConfirmadas.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getParejasConfirmadas()).asObject());
        colConfirmadas.setCellFactory(c -> celdaEnteroCentrada());
        colDisponibles.setStyle("-fx-alignment: CENTER;");
        colDisponibles.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCuposDisponibles()).asObject());
        colDisponibles.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Integer valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                getStyleClass().removeAll("tournaments-available-ok-v4",
                        "tournaments-available-low-v4",
                        "tournaments-available-full-v4");
                setText(vacia || valor == null ? null : String.valueOf(valor));
                if (!vacia && valor != null) getStyleClass().add(
                        valor == 0 ? "tournaments-available-full-v4"
                                : valor <= 2 ? "tournaments-available-low-v4"
                                : "tournaments-available-ok-v4");
            }
        });
        colPrecio.setStyle("-fx-alignment: CENTER;");
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty(
                formatearMoneda(d.getValue().getPrecioInscripcion())));
        colPrecio.setCellFactory(c -> celdaTexto(Pos.CENTER));
        tablaCategorias.setRowFactory(tabla -> new TableRow<>() {
            @Override protected void updateItem(TorneoCategoria categoria, boolean vacia) {
                super.updateItem(categoria, vacia);
                pseudoClassStateChanged(
                        javafx.css.PseudoClass.getPseudoClass("inactive"),
                        !vacia && categoria != null && !categoria.isActivo());
            }
            {
                setOnMousePressed(evento -> {
                    if (!isEmpty() && getItem() != null) {
                        tablaCategorias.getSelectionModel().select(getItem());
                        tablaCategorias.requestFocus();
                    }
                });
            }
        });
        tablaCategorias.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> actualizarBotonesCategoria());
    }

    private void configurarCeldaFecha(TableColumn<Torneo, LocalDate> columna) {
        columna.setCellFactory(valor -> new TableCell<>() {
            @Override protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setAlignment(Pos.CENTER);
                setText(vacia || fecha == null ? null : FECHA.format(fecha));
            }
        });
    }

    private TableCell<TorneoCategoria, String> celdaTexto(Pos alineacion) {
        return new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(alineacion);
                setText(vacia || valor == null ? null : valor);
            }
        };
    }

    private TableCell<TorneoCategoria, Integer> celdaEnteroCentrada() {
        return new TableCell<>() {
            @Override protected void updateItem(Integer valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(Pos.CENTER);
                setText(vacia || valor == null ? null : String.valueOf(valor));
            }
        };
    }

    private String colorEstado(EstadoTorneo estado) {
        return switch (estado) {
            case BORRADOR -> "#a9bac3";
            case PUBLICADO -> "#86bde0";
            case INSCRIPCION_ABIERTA -> "#7bd4ae";
            case INSCRIPCION_CERRADA -> "#e0bf72";
            case EN_CURSO -> "#77c9dc";
            case FINALIZADO -> "#aaa4d7";
            case CANCELADO -> "#e88e99";
        };
    }

    private String formatearMoneda(BigDecimal valor) {
        return FormateadorMoneda.pesos(valor);
    }
    private void configurarFiltros() {
        filtroEstado.setItems(FXCollections.observableArrayList(EstadoTorneo.values()));
        filtroEstado.setCellFactory(lista -> crearCeldaEstado(false));
        filtroEstado.setButtonCell(crearCeldaEstado(true));
        filtrados = new FilteredList<>(torneos, t -> true);
        tablaTorneos.setItems(filtrados);
        campoBuscar.textProperty().addListener((o, a, n) -> aplicarFiltros());
        filtroEstado.valueProperty().addListener((o, a, n) -> aplicarFiltros());
        campoBuscar.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) limpiarFiltros(); });
        botonLimpiar.setDisable(false);
    }

    private ListCell<EstadoTorneo> crearCeldaEstado(boolean boton) {
        return new ListCell<>() {
            @Override protected void updateItem(EstadoTorneo estado, boolean vacia) {
                super.updateItem(estado, vacia);
                setText(vacia || estado == null
                        ? (boton ? "Todos los estados" : null)
                        : estado.toString());
                setGraphic(null);
            }
        };
    }

    @FXML private void cargarTorneos() {
        try {
            long id = seleccionado == null ? 0 : seleccionado.getId();
            torneos.setAll(torneoDAO.listar());
            aplicarFiltros();
            if (id > 0) torneos.stream().filter(t -> t.getId() == id).findFirst()
                    .ifPresent(t -> tablaTorneos.getSelectionModel().select(t));
            actualizarMensajeResultados();
        } catch (RuntimeException ex) { mostrarError(ex); }
    }

    private void aplicarFiltros() {
        String texto = campoBuscar.getText() == null ? "" : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        EstadoTorneo estado = filtroEstado.getValue();
        filtrados.setPredicate(t -> (estado == null || t.getEstado() == estado)
                && (texto.isBlank() || t.getNombre().toLowerCase(Locale.ROOT).contains(texto)));
        actualizarMensajeResultados();
    }

    @FXML private void limpiarFiltros() { campoBuscar.clear(); filtroEstado.getSelectionModel().clearSelection(); }

    private void actualizarMensajeResultados() {
        int cantidad = filtrados == null ? 0 : filtrados.size();
        String busqueda = campoBuscar.getText() == null
                ? "" : campoBuscar.getText().trim();
        EstadoTorneo estado = filtroEstado.getValue();
        String texto = cantidad
                + (cantidad == 1 ? " torneo visible" : " torneos visibles");
        if (estado != null) texto += " · " + estado;
        if (!busqueda.isBlank()) texto += " para \"" + busqueda + "\"";
        etiquetaMensaje.setText(texto);
    }

    private void seleccionar(Torneo torneo) {
        seleccionado = torneo;
        if (torneo == null) { categorias.clear(); mostrarSinSeleccion(); actualizarBotones(); return; }
        mostrarDetalle();
        detalleNombre.setText(torneo.getNombre());
        detalleEstado.setText(torneo.getEstado().toString());
        detalleEstado.getStyleClass().removeIf(
                clase -> clase.startsWith("tournament-state-"));
        detalleEstado.getStyleClass().add("tournament-state-"
                + torneo.getEstado().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        detalleEstado.setStyle("-fx-text-fill: " + colorEstado(torneo.getEstado()) + "; -fx-font-weight: 900;");
        detalleFechas.setText(FECHA.format(torneo.getFechaInicio()) + " al " + FECHA.format(torneo.getFechaFin()));
        detalleInscripcion.setText(
                "Desde " + FECHA_HORA.format(torneo.getInscripcionDesde())
                        + "\nHasta "
                        + FECHA_HORA.format(torneo.getInscripcionHasta()));
        categorias.setAll(categoriaDAO.listarPorTorneo(torneo.getId()));
        tablaCategorias.setItems(categorias);
        actualizarMetricas();
        tablaCategorias.getSelectionModel().clearSelection();
        configurarAccionContextual();
        actualizarPreparacionCiclo();
        actualizarBotones();
    }

    private void mostrarSinSeleccion() {
        panelSinSeleccion.setVisible(true); panelSinSeleccion.setManaged(true);
        scrollDetalle.setVisible(false); scrollDetalle.setManaged(false);
    }

    private void mostrarDetalle() {
        panelSinSeleccion.setVisible(false); panelSinSeleccion.setManaged(false);
        scrollDetalle.setVisible(true); scrollDetalle.setManaged(true);
    }

    private void actualizarMetricas() {
        int activas = (int) categorias.stream().filter(TorneoCategoria::isActivo).count();
        int parejas = categorias.stream().filter(TorneoCategoria::isActivo).mapToInt(TorneoCategoria::getParejasConfirmadas).sum();
        int cupos = categorias.stream().filter(TorneoCategoria::isActivo).mapToInt(TorneoCategoria::getCuposDisponibles).sum();
        detalleCantidadCategorias.setText(String.valueOf(activas));
        detalleParejas.setText(String.valueOf(parejas));
        detalleCupos.setText(String.valueOf(cupos));
        detalleCupos.getStyleClass().removeAll("tournaments-cupos-ok-v4",
                "tournaments-cupos-low-v4", "tournaments-cupos-full-v4");
        detalleCupos.getStyleClass().add(cupos == 0
                ? "tournaments-cupos-full-v4"
                : cupos <= 2 ? "tournaments-cupos-low-v4"
                : "tournaments-cupos-ok-v4");
        int filas = Math.max(2, Math.min(6, categorias.size()));
        double alto = 38 + filas * 46;
        tablaCategorias.setMinHeight(alto);
        tablaCategorias.setPrefHeight(alto);
        tablaCategorias.setMaxHeight(alto);
    }

    private void actualizarPreparacionCiclo() {
        if (seleccionado == null || etiquetaPreparacion == null) return;
        try {
            GestionTorneoService.ResumenCiclo resumen =
                    gestionService.resumenCiclo(seleccionado.getId());
            if (seleccionado.getEstado() == EstadoTorneo.INSCRIPCION_CERRADA) {
                int faltan = Math.max(0, resumen.categoriasCompetitivas()
                        - resumen.categoriasConCuadro());
                etiquetaPreparacion.setText(faltan == 0
                        ? resumen.categoriasCompetitivas() + " de "
                                + resumen.categoriasCompetitivas()
                                + " categorías con estructura competitiva lista."
                        : faltan + (faltan == 1
                                ? " categoría todavía necesita cuadro."
                                : " categorías todavía necesitan cuadro."));
            } else if (seleccionado.getEstado() == EstadoTorneo.EN_CURSO) {
                etiquetaPreparacion.setText(resumen.partidosFinalizados()
                        + " de " + resumen.partidos()
                        + " partidos finalizados · "
                        + resumen.categoriasConCampeona() + " de "
                        + resumen.categoriasCompetitivas()
                        + " categorías con campeones definidos.");
            } else {
                etiquetaPreparacion.setText("");
            }
        } catch (RuntimeException exception) {
            etiquetaPreparacion.setText("");
        }
    }

    private void configurarAccionContextual() {
        EstadoTorneo estado = seleccionado.getEstado();
        botonPublicar.setVisible(estado == EstadoTorneo.BORRADOR); botonPublicar.setManaged(botonPublicar.isVisible());
        botonAbrir.setVisible(estado == EstadoTorneo.PUBLICADO); botonAbrir.setManaged(botonAbrir.isVisible());
        botonCerrar.setVisible(estado == EstadoTorneo.INSCRIPCION_ABIERTA); botonCerrar.setManaged(botonCerrar.isVisible());
        botonIniciar.setVisible(estado == EstadoTorneo.INSCRIPCION_CERRADA); botonIniciar.setManaged(botonIniciar.isVisible());
        botonFinalizar.setVisible(estado == EstadoTorneo.EN_CURSO); botonFinalizar.setManaged(botonFinalizar.isVisible());
        botonCancelar.setVisible(!estado.esFinal()); botonCancelar.setManaged(botonCancelar.isVisible());
        switch (estado) {
            case BORRADOR -> { etiquetaAccionTitulo.setText("Publicar torneo"); etiquetaAccionAyuda.setText("Publicalo para dejarlo listo antes de abrir las inscripciones."); }
            case PUBLICADO -> { etiquetaAccionTitulo.setText("Abrir inscripciones"); etiquetaAccionAyuda.setText("Necesitás al menos una categoría activa para comenzar a recibir parejas."); }
            case INSCRIPCION_ABIERTA -> { etiquetaAccionTitulo.setText("Inscripciones abiertas"); etiquetaAccionAyuda.setText("Cerralas cuando termine el período de inscripción."); }
            case INSCRIPCION_CERRADA -> { etiquetaAccionTitulo.setText("Preparar inicio"); etiquetaAccionAyuda.setText("Generá grupos o cuadros de las categorías competitivas antes de iniciar."); }
            case EN_CURSO -> { etiquetaAccionTitulo.setText("Torneo en curso"); etiquetaAccionAyuda.setText("Finalizalo cuando todos los partidos y campeones estén definidos."); }
            case FINALIZADO -> { etiquetaAccionTitulo.setText("Torneo finalizado"); etiquetaAccionAyuda.setText("El ciclo competitivo está completo y no requiere más acciones."); }
            case CANCELADO -> { etiquetaAccionTitulo.setText("Torneo cancelado"); etiquetaAccionAyuda.setText("El torneo quedó cerrado y no admite nuevas modificaciones."); }
        }
    }

    private void actualizarBotones() {
        boolean hay = seleccionado != null;
        EstadoTorneo e = hay ? seleccionado.getEstado() : null;
        boolean consulta = hay && (e.esFinal() || e == EstadoTorneo.EN_CURSO);
        botonEditar.setDisable(!hay);
        botonEditar.setText(consulta ? "VER DATOS" : "EDITAR DATOS");
        botonEditar.setTooltip(hay && e == EstadoTorneo.EN_CURSO
                ? new Tooltip("El torneo en curso solo permite consultar sus datos generales.")
                : hay && e.esFinal()
                        ? new Tooltip("Los torneos cerrados no admiten modificaciones.")
                        : null);
        boolean permiteNuevaCategoria = hay && (e == EstadoTorneo.BORRADOR
                || e == EstadoTorneo.PUBLICADO
                || e == EstadoTorneo.INSCRIPCION_ABIERTA);
        botonNuevaCategoria.setDisable(!permiteNuevaCategoria);
        String ayudaNuevaCategoria;
        if (!hay) {
            ayudaNuevaCategoria =
                    "Seleccioná un torneo para agregar una categoría.";
        } else if (e == EstadoTorneo.INSCRIPCION_CERRADA) {
            ayudaNuevaCategoria =
                    "No se pueden crear categorías después del cierre de inscripciones.";
        } else if (e == EstadoTorneo.EN_CURSO) {
            ayudaNuevaCategoria =
                    "No se pueden crear categorías con el torneo en curso.";
        } else if (e.esFinal()) {
            ayudaNuevaCategoria =
                    "Los torneos finalizados o cancelados son de solo lectura.";
        } else {
            ayudaNuevaCategoria =
                    "Agregar una categoría al torneo seleccionado.";
        }
        Tooltip tooltipNuevaCategoria = new Tooltip(ayudaNuevaCategoria);
        tooltipNuevaCategoria.setShowDelay(javafx.util.Duration.millis(300));
        Tooltip.uninstall(contenedorNuevaCategoria,
                contenedorNuevaCategoria.getProperties().get(
                        "tooltip-nueva-categoria") instanceof Tooltip anterior
                                ? anterior : null);
        Tooltip.install(contenedorNuevaCategoria, tooltipNuevaCategoria);
        contenedorNuevaCategoria.getProperties().put(
                "tooltip-nueva-categoria", tooltipNuevaCategoria);
        botonNuevaCategoria.setTooltip(null);
        botonPublicar.setDisable(e != EstadoTorneo.BORRADOR);
        botonAbrir.setDisable(e != EstadoTorneo.PUBLICADO);
        botonCerrar.setDisable(e != EstadoTorneo.INSCRIPCION_ABIERTA);
        botonIniciar.setDisable(e != EstadoTorneo.INSCRIPCION_CERRADA);
        boolean finalizable = false;
        String bloqueoFinal = null;
        if (e == EstadoTorneo.EN_CURSO) {
            try {
                GestionTorneoService.ResumenCiclo r = gestionService.resumenCiclo(seleccionado.getId());
                finalizable = r.categoriasCompetitivas() > 0
                        && r.categoriasConCuadro() == r.categoriasCompetitivas()
                        && r.partidosPendientes() == 0
                        && r.categoriasConCampeona() == r.categoriasCompetitivas();
                if (!finalizable) bloqueoFinal = r.partidosPendientes() > 0
                        ? "Todavía hay " + r.partidosPendientes() + " partido(s) pendientes."
                        : "Definí los campeones de todas las categorías antes de finalizar.";
            } catch (RuntimeException ex) { bloqueoFinal = "No se pudo validar el cierre competitivo."; }
        }
        botonFinalizar.setDisable(!finalizable);
        botonFinalizar.setTooltip(bloqueoFinal == null ? null : new Tooltip(bloqueoFinal));
        botonCancelar.setDisable(!hay || e.esFinal());
        if (hay) configurarAccionContextual();
        actualizarBotonesCategoria();
    }

    private void actualizarBotonesCategoria() {
        TorneoCategoria c = tablaCategorias.getSelectionModel().getSelectedItem();
        boolean editable = seleccionado != null && c != null && c.isActivo()
                && seleccionado.getEstado() != EstadoTorneo.INSCRIPCION_CERRADA
                && seleccionado.getEstado() != EstadoTorneo.EN_CURSO
                && !seleccionado.getEstado().esFinal();
        botonEditarCategoria.setDisable(c == null || !editable);
        botonDesactivarCategoria.setDisable(c == null || !c.isActivo() || !editable);
        botonEditarCategoria.setTooltip(c != null && !editable
                ? new Tooltip("No se puede editar una categoría en este estado del torneo.") : null);
        botonDesactivarCategoria.setTooltip(c != null && !editable
                ? new Tooltip("No se puede desactivar una categoría en este estado del torneo.") : null);
        botonGestionarGrupos.setDisable(c == null || !c.isActivo() || seleccionado == null || !c.usaFaseGrupos()
                || seleccionado.getEstado() == EstadoTorneo.BORRADOR
                || seleccionado.getEstado() == EstadoTorneo.PUBLICADO
                || seleccionado.getEstado() == EstadoTorneo.INSCRIPCION_ABIERTA
                || seleccionado.getEstado() == EstadoTorneo.CANCELADO);
        panelAccionesCategoria.setVisible(c != null);
        panelAccionesCategoria.setManaged(true);
        etiquetaCategoriaSeleccionada.setText(
                c == null ? "ACCIONES DE CATEGORÍA"
                        : "ACCIONES PARA: " + c.getNombre());
        etiquetaFormatoCategoria.setText(c == null
                ? "Seleccioná una categoría"
                : "Formato: " + (c.usaFaseGrupos()
                        ? "Grupos + eliminación" : "Eliminación directa"));
        botonGestionarGrupos.getStyleClass().remove("tournaments-category-primary-v4");
        botonGestionarCuadro.getStyleClass().remove("tournaments-category-primary-v4");
        if (c != null) {
            if (c.usaFaseGrupos()
                    && seleccionado.getEstado() == EstadoTorneo.INSCRIPCION_CERRADA) {
                botonGestionarGrupos.getStyleClass().add("tournaments-category-primary-v4");
            } else {
                botonGestionarCuadro.getStyleClass().add("tournaments-category-primary-v4");
            }
        }
        botonGestionarGrupos.setVisible(c != null && c.usaFaseGrupos());
        botonGestionarGrupos.setManaged(botonGestionarGrupos.isVisible());
        boolean historico = seleccionado != null && seleccionado.getEstado().esFinal();
        botonGestionarGrupos.setText(historico ? "VER GRUPOS" : "GRUPOS");
        botonGestionarCuadro.setText(historico ? "VER CUADRO" : "CUADRO");
        botonGestionarCuadro.setDisable(c == null || !c.isActivo() || seleccionado == null
                || seleccionado.getEstado() == EstadoTorneo.BORRADOR
                || seleccionado.getEstado() == EstadoTorneo.PUBLICADO
                || seleccionado.getEstado() == EstadoTorneo.INSCRIPCION_ABIERTA
                || seleccionado.getEstado() == EstadoTorneo.CANCELADO);
    }

    @FXML private void nuevoTorneo() {
        try {
            Torneo torneo = new TorneoDialog(null).mostrar().orElse(null);
            if (torneo == null) return;
            gestionService.crearTorneo(torneo, Navegacion.getUsuarioActual().getId());
            cargarTorneos();
            mostrarExito("Torneo creado correctamente.");
        } catch (RuntimeException ex) { mostrarError(ex); }
    }

    @FXML private void editarTorneo() {
        if (seleccionado == null) return;
        try {
            boolean consulta = seleccionado.getEstado().esFinal()
                    || seleccionado.getEstado() == EstadoTorneo.EN_CURSO;
            Torneo cambios = new TorneoDialog(seleccionado, consulta).mostrar().orElse(null);
            if (consulta) return;
            if (cambios == null) return;
            gestionService.editarTorneo(cambios);
            cargarTorneos();
            mostrarExito("Torneo actualizado correctamente.");
        } catch (RuntimeException ex) { mostrarError(ex); }
    }

    @FXML private void nuevaCategoria() { editarCategoriaInterna(null); }
    @FXML private void editarCategoria() { editarCategoriaInterna(tablaCategorias.getSelectionModel().getSelectedItem()); }

    private void editarCategoriaInterna(TorneoCategoria original) {
        if (seleccionado == null) return;
        try {
            TorneoCategoria cambios = new TorneoCategoriaDialog(seleccionado.getId(), original).mostrar().orElse(null);
            if (cambios == null) return;
            if (original == null) gestionService.crearCategoria(cambios); else gestionService.editarCategoria(cambios);
            seleccionar(seleccionado);
            mostrarExito("Categoría guardada correctamente.");
        } catch (RuntimeException ex) { mostrarError(ex); }
    }

    @FXML private void desactivarCategoria() {
        TorneoCategoria categoria = tablaCategorias.getSelectionModel().getSelectedItem();
        if (categoria == null) return;
        if (!Dialogos.confirmarPeligro("Desactivar categoría",
                "La categoría dejará de estar disponible para nuevas inscripciones.\n\n¿Querés continuar?")) return;
        try {
            gestionService.desactivarCategoria(categoria.getId());
            seleccionar(seleccionado);
            mostrarExito("Categoría desactivada correctamente.");
        } catch (RuntimeException ex) { mostrarError(ex); }
    }

    @FXML private void publicar() { cambiar(() -> gestionService.publicar(seleccionado.getId())); }
    @FXML private void abrirInscripciones() { cambiar(() -> gestionService.abrirInscripciones(seleccionado.getId())); }
    @FXML private void cerrarInscripciones() {
        if (Dialogos.confirmar("Cerrar inscripciones",
                "El torneo dejará de aceptar nuevas parejas desde la web.\n\n¿Querés continuar?"))
            cambiar(() -> gestionService.cerrarInscripciones(seleccionado.getId()));
    }
    @FXML private void iniciarTorneo() {
        if (seleccionado == null) return;
        try {
            GestionTorneoService.ResumenCiclo resumen =
                    gestionService.resumenCiclo(seleccionado.getId());
            String mensaje = "Categorias con cuadro: "
                    + resumen.categoriasConCuadro() + " de "
                    + resumen.categoriasCompetitivas()
                    + "\nPartidos generados: " + resumen.partidos()
                    + "\n\nEl torneo pasara a estado En curso."
                    + "\n\nQueres continuar?";
            if (Dialogos.confirmar("Iniciar torneo", mensaje)) {
                cambiar(() -> gestionService.iniciar(seleccionado.getId()));
            }
        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    @FXML private void finalizarTorneo() {
        if (seleccionado == null) return;
        try {
            GestionTorneoService.ResumenCiclo resumen =
                    gestionService.resumenCiclo(seleccionado.getId());
            String mensaje = "Categorias con campeones: "
                    + resumen.categoriasConCampeona() + " de "
                    + resumen.categoriasCompetitivas()
                    + "\nPartidos finalizados: "
                    + resumen.partidosFinalizados() + " de "
                    + resumen.partidos()
                    + "\nPartidos pendientes: "
                    + resumen.partidosPendientes()
                    + "\n\nEl torneo quedara finalizado y no podra reabrirse."
                    + "\n\nQueres continuar?";
            if (Dialogos.confirmarPeligro("Finalizar torneo", mensaje)) {
                cambiar(() -> gestionService.finalizar(seleccionado.getId()));
            }
        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }
    @FXML private void cancelarTorneo() {
        if (seleccionado == null) return;
        String impacto = "Torneo: " + seleccionado.getNombre()
                + "\nEstado actual: " + seleccionado.getEstado()
                + "\nCategorías registradas: " + categorias.size()
                + "\nParejas confirmadas: " + categorias.stream()
                        .mapToInt(TorneoCategoria::getParejasConfirmadas).sum()
                + "\n\nEl torneo quedará cancelado y no podrá reabrirse."
                + "\nLas inscripciones existentes no se modificarán automáticamente."
                + "\n\n¿Querés continuar?";
        if (Dialogos.confirmarPeligro("Cancelar torneo", impacto))
            cambiar(() -> gestionService.cancelar(seleccionado.getId()));
    }
    private void cambiar(Runnable accion) {
        if (seleccionado == null) return;
        try { accion.run(); cargarTorneos(); mostrarExito("Estado actualizado correctamente."); }
        catch (RuntimeException ex) { mostrarError(ex); }
    }

    @FXML private void gestionarGrupos() {
        TorneoCategoria categoria = tablaCategorias.getSelectionModel().getSelectedItem();
        if (categoria != null) Navegacion.mostrarGruposTorneo(categoria.getId());
    }

    @FXML private void gestionarCuadro() {
        TorneoCategoria categoria = tablaCategorias.getSelectionModel().getSelectedItem();
        if (categoria == null) {
            Dialogos.informacion("Seleccion requerida",
                    "Selecciona una categoria para gestionar su cuadro.");
            return;
        }
        Navegacion.mostrarCuadroTorneo(categoria.getId());
    }

    private void configurarDeseleccion() {
        if (tablaTorneos.getScene() == null) return;
        tablaTorneos.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, evento -> {
            boolean enTorneo = perteneceAFilaConDatos(evento.getTarget(), tablaTorneos);
            boolean enCategoria = perteneceAFilaConDatos(evento.getTarget(), tablaCategorias);
            boolean dentroDetalle = perteneceA(evento.getTarget(), panelDetalle);
            if (!enTorneo && !enCategoria && !dentroDetalle) javafx.application.Platform.runLater(this::limpiarSelecciones);
            else if (perteneceA(evento.getTarget(), tablaTorneos) && !enTorneo) javafx.application.Platform.runLater(this::limpiarTorneo);
            else if (perteneceA(evento.getTarget(), tablaCategorias) && !enCategoria) javafx.application.Platform.runLater(() -> tablaCategorias.getSelectionModel().clearSelection());
        });
    }
    private boolean perteneceAFilaConDatos(Object objetivo, TableView<?> tabla) {
        if (!(objetivo instanceof Node nodo)) return false;
        Node actual = nodo;
        while (actual != null && actual != tabla) {
            if (actual instanceof TableRow<?> fila) return !fila.isEmpty();
            actual = actual.getParent();
        }
        return false;
    }
    private boolean perteneceA(Object objetivo, Node contenedor) {
        if (!(objetivo instanceof Node nodo)) return false;
        Node actual = nodo;
        while (actual != null) { if (actual == contenedor) return true; Parent padre = actual.getParent(); actual = padre; }
        return false;
    }
    private void limpiarTorneo() { tablaTorneos.getSelectionModel().clearSelection(); seleccionar(null); }
    private void limpiarSelecciones() { tablaCategorias.getSelectionModel().clearSelection(); }

    @FXML private void verInscripciones() { Navegacion.mostrarTorneosInscripciones(); }
    @FXML private void volver() { Navegacion.mostrarDashboard(Navegacion.getUsuarioActual()); }

    private void mostrarError(RuntimeException ex) {
        String mensaje = ex.getMessage() == null ? "No se pudo completar la operación." : ex.getMessage();
        etiquetaMensaje.setText(mensaje);
        Dialogos.error("No se pudo completar la operación", mensaje);
    }

    private void mostrarExito(String mensaje) {
        etiquetaMensaje.setText(mensaje);
        Dialogos.exito("Operación completada", mensaje);
    }
}
