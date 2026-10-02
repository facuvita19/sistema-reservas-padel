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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    @FXML private TableColumn<TorneoCategoria, Integer> colCupo;
    @FXML private TableColumn<TorneoCategoria, Integer> colConfirmadas;
    @FXML private TableColumn<TorneoCategoria, Integer> colDisponibles;
    @FXML private TableColumn<TorneoCategoria, String> colPrecio;
    @FXML private Label detalleNombre;
    @FXML private Label detalleEstado;
    @FXML private Label detalleFechas;
    @FXML private Label detalleInscripcion;
    @FXML private Label etiquetaMensaje;
    @FXML private Button botonEditar;
    @FXML private Button botonNuevaCategoria;
    @FXML private Button botonEditarCategoria;
    @FXML private Button botonDesactivarCategoria;
    @FXML private Button botonGestionarCuadro;
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
    }

    private void configurarTablas() {
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colInicio.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFechaInicio()));
        colFin.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFechaFin()));
        colEstado.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getEstado()));
        configurarCeldaFecha(colInicio);
        configurarCeldaFecha(colFin);
        colEstado.setCellFactory(columna -> new TableCell<>() {
            @Override protected void updateItem(EstadoTorneo estado, boolean vacia) {
                super.updateItem(estado, vacia);
                setText(vacia || estado == null ? null : estado.toString());
                getStyleClass().removeIf(clase -> clase.startsWith("tournament-status-"));
                if (!vacia && estado != null) {
                    getStyleClass().add("tournament-status-"
                            + estado.name().toLowerCase(Locale.ROOT).replace('_', '-'));
                }
            }
        });
        tablaTorneos.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> seleccionar(n));

        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colRama.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getRama())));
        colCupo.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCupoParejas()).asObject());
        colConfirmadas.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getParejasConfirmadas()).asObject());
        colDisponibles.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCuposDisponibles()).asObject());
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty(
                formatearMoneda(d.getValue().getPrecioInscripcion())));
        tablaCategorias.setRowFactory(tabla -> new TableRow<>() {
            @Override protected void updateItem(TorneoCategoria categoria, boolean vacia) {
                super.updateItem(categoria, vacia);
                pseudoClassStateChanged(
                        javafx.css.PseudoClass.getPseudoClass("inactive"),
                        !vacia && categoria != null && !categoria.isActivo());
            }
        });
        tablaCategorias.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> actualizarBotonesCategoria());
    }

    private void configurarCeldaFecha(TableColumn<Torneo, LocalDate> columna) {
        columna.setCellFactory(valor -> new TableCell<>() {
            @Override protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : FECHA.format(fecha));
            }
        });
    }

    private String formatearMoneda(BigDecimal valor) {
        return FormateadorMoneda.pesos(valor);
    }
    private void configurarFiltros() {
        filtroEstado.setItems(FXCollections.observableArrayList(EstadoTorneo.values()));
        filtrados = new FilteredList<>(torneos, t -> true);
        tablaTorneos.setItems(filtrados);
        campoBuscar.textProperty().addListener((o, a, n) -> aplicarFiltros());
        filtroEstado.valueProperty().addListener((o, a, n) -> aplicarFiltros());
    }

    @FXML private void cargarTorneos() {
        try {
            long id = seleccionado == null ? 0 : seleccionado.getId();
            torneos.setAll(torneoDAO.listar());
            aplicarFiltros();
            if (id > 0) torneos.stream().filter(t -> t.getId() == id).findFirst()
                    .ifPresent(t -> tablaTorneos.getSelectionModel().select(t));
            etiquetaMensaje.setText(torneos.size() + " torneo(s) cargado(s).");
        } catch (RuntimeException ex) { mostrarError(ex); }
    }

    private void aplicarFiltros() {
        String texto = campoBuscar.getText() == null ? "" : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        EstadoTorneo estado = filtroEstado.getValue();
        filtrados.setPredicate(t -> (estado == null || t.getEstado() == estado)
                && (texto.isBlank() || t.getNombre().toLowerCase(Locale.ROOT).contains(texto)));
    }

    @FXML private void limpiarFiltros() { campoBuscar.clear(); filtroEstado.getSelectionModel().clearSelection(); }

    private void seleccionar(Torneo torneo) {
        seleccionado = torneo;
        if (torneo == null) { categorias.clear(); actualizarBotones(); return; }
        detalleNombre.setText(torneo.getNombre());
        detalleEstado.setText(torneo.getEstado().toString());
        detalleEstado.getStyleClass().removeIf(
                clase -> clase.startsWith("tournament-state-"));
        detalleEstado.getStyleClass().add("tournament-state-"
                + torneo.getEstado().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        detalleFechas.setText(FECHA.format(torneo.getFechaInicio()) + " al " + FECHA.format(torneo.getFechaFin()));
        detalleInscripcion.setText(
                FECHA_HORA.format(torneo.getInscripcionDesde())
                        + " al "
                        + FECHA_HORA.format(torneo.getInscripcionHasta()));
        categorias.setAll(categoriaDAO.listarPorTorneo(torneo.getId()));
        tablaCategorias.setItems(categorias);
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean hay = seleccionado != null;
        EstadoTorneo e = hay ? seleccionado.getEstado() : null;
        botonEditar.setDisable(!hay || e.esFinal());
        botonNuevaCategoria.setDisable(!hay || e.esFinal() || e == EstadoTorneo.EN_CURSO);
        botonPublicar.setDisable(e != EstadoTorneo.BORRADOR);
        botonAbrir.setDisable(e != EstadoTorneo.PUBLICADO);
        botonCerrar.setDisable(e != EstadoTorneo.INSCRIPCION_ABIERTA);
        botonIniciar.setDisable(e != EstadoTorneo.INSCRIPCION_CERRADA);
        botonFinalizar.setDisable(e != EstadoTorneo.EN_CURSO);
        botonCancelar.setDisable(!hay || e.esFinal());
        actualizarBotonesCategoria();
    }

    private void actualizarBotonesCategoria() {
        TorneoCategoria c = tablaCategorias.getSelectionModel().getSelectedItem();
        boolean editable = seleccionado != null && !seleccionado.getEstado().esFinal()
                && seleccionado.getEstado() != EstadoTorneo.EN_CURSO;
        botonEditarCategoria.setDisable(c == null || !editable);
        botonDesactivarCategoria.setDisable(c == null || !c.isActivo() || !editable);
        botonGestionarCuadro.setDisable(c == null || seleccionado == null
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
            Torneo cambios = new TorneoDialog(seleccionado).mostrar().orElse(null);
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
        if (Dialogos.confirmarPeligro("Cancelar torneo",
                "El torneo quedará cancelado y no podrá reabrirse.\nLas inscripciones existentes no se modificarán automáticamente.\n\n¿Querés continuar?"))
            cambiar(() -> gestionService.cancelar(seleccionado.getId()));
    }
    private void cambiar(Runnable accion) {
        if (seleccionado == null) return;
        try { accion.run(); cargarTorneos(); mostrarExito("Estado actualizado correctamente."); }
        catch (RuntimeException ex) { mostrarError(ex); }
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
