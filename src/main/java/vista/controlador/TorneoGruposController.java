package vista.controlador;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.StringConverter;
import negocio.ModoAsignacionGrupoTorneo;
import negocio.TorneoGrupo;
import negocio.TorneoGrupoIntegrante;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import servicio.GestionGruposTorneoService;
import vista.Dialogos;
import vista.Navegacion;

public class TorneoGruposController {

    private final GestionGruposTorneoService service =
            new GestionGruposTorneoService();
    private long categoriaId;
    private GestionGruposTorneoService.Vista vista;
    private Long grupoSeleccionadoId;

    @FXML private Label titulo;
    @FXML private Label resumen;
    @FXML private Label mensaje;
    @FXML private ListView<TorneoInscripcion> sinAsignar;
    @FXML private ListView<TorneoGrupo> listaGrupos;
    @FXML private ListView<TorneoGrupoIntegrante> integrantes;
    @FXML private ComboBox<TorneoInscripcion> cabeza;
    @FXML private ComboBox<ModoAsignacionGrupoTorneo> modo;
    @FXML private Button confirmar;

    @FXML
    private void initialize() {
        categoriaId = Navegacion.consumirCategoriaGruposTorneo();
        modo.setItems(FXCollections.observableArrayList(
                ModoAsignacionGrupoTorneo.values()));
        modo.setValue(ModoAsignacionGrupoTorneo.SORTEO_DIRIGIDO);
        configurar();
        configurarDeseleccion(sinAsignar);
        configurarDeseleccion(listaGrupos);
        configurarDeseleccion(integrantes);
        cargar();
    }

    private <T> void configurarDeseleccion(ListView<T> lista) {
        lista.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                evento -> {
                    javafx.scene.Node nodo = evento.getPickResult()
                            .getIntersectedNode();
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

    private void configurar() {
        StringConverter<TorneoInscripcion> conversor =
                new StringConverter<>() {
                    @Override
                    public String toString(TorneoInscripcion valor) {
                        return valor == null ? "" : nombre(valor);
                    }
                    @Override
                    public TorneoInscripcion fromString(String valor) {
                        return null;
                    }
                };
        cabeza.setConverter(conversor);
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
                    }
                    mostrar(actual);
                });
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

    private void cargar() {
        TorneoGrupo seleccionAntes =
                listaGrupos.getSelectionModel().getSelectedItem();
        if (seleccionAntes != null) {
            grupoSeleccionadoId = seleccionAntes.getId();
        }
        try {
            vista = service.cargar(categoriaId);
            titulo.setText(vista.categoria().getNombre() + " - "
                    + vista.categoria().getRama());
            Set<Long> asignadas = new HashSet<>();
            vista.grupos().forEach(grupo -> grupo.getIntegrantes()
                    .forEach(i -> asignadas.add(i.getInscripcionId())));
            List<TorneoInscripcion> libres = vista.inscripciones().stream()
                    .filter(i -> !asignadas.contains(i.getId()))
                    .toList();
            sinAsignar.setItems(FXCollections.observableArrayList(libres));
            listaGrupos.setItems(FXCollections.observableArrayList(
                    vista.grupos()));
            cabeza.setItems(FXCollections.observableArrayList(
                    vista.inscripciones()));
            resumen.setText(vista.inscripciones().size()
                    + " confirmadas · " + vista.grupos().size()
                    + " grupos · " + libres.size() + " sin asignar");
            boolean cerrado = vista.grupos().stream()
                    .anyMatch(TorneoGrupo::isConfirmado);
            confirmar.setDisable(cerrado);
            if (cerrado) mensaje.setText("Los grupos estan confirmados.");
            if (!vista.grupos().isEmpty()) {
                TorneoGrupo grupoASeleccionar = vista.grupos().stream()
                        .filter(grupo -> grupoSeleccionadoId != null
                                && grupo.getId() == grupoSeleccionadoId)
                        .findFirst()
                        .orElse(vista.grupos().get(0));
                listaGrupos.getSelectionModel().select(grupoASeleccionar);
                grupoSeleccionadoId = grupoASeleccionar.getId();
            } else {
                grupoSeleccionadoId = null;
                integrantes.getItems().clear();
            }
        } catch (RuntimeException exception) {
            String texto = mensaje(exception);
            mensaje.setText(texto);
            Dialogos.error("No se pudieron cargar los grupos", texto);
        }
    }

    private void mostrar(TorneoGrupo grupo) {
        integrantes.setItems(FXCollections.observableArrayList(
                grupo == null ? List.of() : grupo.getIntegrantes()));
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
    private void agregarCabeza() {
        TorneoGrupo grupo = listaGrupos.getSelectionModel().getSelectedItem();
        TorneoInscripcion inscripcion = cabeza.getValue();
        if (grupo == null || inscripcion == null) {
            informar("Seleccion requerida",
                    "Selecciona un grupo y una pareja como cabeza de serie.");
            return;
        }
        ejecutar("No se pudo marcar el cabeza de serie", () -> {
            service.asignar(categoriaId, grupo.getId(),
                    inscripcion.getId(), true);
            cargar();
            exito("Cabeza de serie definido para " + grupo.getNombre() + ".");
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
            exito("La pareja volvio a la lista de parejas sin asignar.");
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
            exito("Sorteo realizado. Revisa la distribucion antes de confirmar.");
        });
    }

    @FXML
    private void confirmar() {
        if (modo.getValue() == null) {
            informar("Modo requerido",
                    "Selecciona Sorteo con cabezas de serie o Armado manual.");
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

    private void ejecutar(String tituloError, Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException exception) {
            String texto = mensaje(exception);
            mensaje.setText(texto);
            Dialogos.error(tituloError, texto);
        }
    }

    private void informar(String titulo, String texto) {
        mensaje.setText(texto);
        Dialogos.informacion(titulo, texto);
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
    private void volver() {
        Navegacion.mostrarTorneos();
    }
}
