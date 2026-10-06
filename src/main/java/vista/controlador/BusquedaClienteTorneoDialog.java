package vista.controlador;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import negocio.Cliente;
import vista.Dialogos;

public class BusquedaClienteTorneoDialog {
    // busqueda-cliente-css-separado-v1
    // buscador-clientes-ventana-normal-v1
    // dialogos-inscripciones-cierre-visual-v1
    private final Dialog<Cliente> dialogo = new Dialog<>();
    private final TableView<Cliente> tabla = new TableView<>();
    private final FilteredList<Cliente> filtrados;

    public BusquedaClienteTorneoDialog(
            List<Cliente> clientes,
            String integrante) {
        this(clientes, integrante, "VINCULAR");
    }

    public BusquedaClienteTorneoDialog(
            List<Cliente> clientes,
            String integrante,
            String textoAccion) {
        filtrados = new FilteredList<>(
                FXCollections.observableArrayList(clientes), valor -> true);
        configurarDialogo(integrante, textoAccion);
    }

    public Optional<Cliente> mostrar() {
        return dialogo.showAndWait();
    }

    private void configurarDialogo(String integrante, String textoAccion) {
        dialogo.setTitle("Seleccionar cliente");
        dialogo.setHeaderText("Buscar cliente para " + integrante);
        ButtonType seleccionar = new ButtonType(
                textoAccion, ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(
                seleccionar,
                new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        Dialogos.preparar(dialogo, "dialog-client-search");
        java.net.URL cssBuscador = BusquedaClienteTorneoDialog.class
                .getResource("/css/busqueda-cliente-torneo.css");
        if (cssBuscador != null) {
            String externo = cssBuscador.toExternalForm();
            if (!dialogo.getDialogPane().getStylesheets().contains(externo)) {
                dialogo.getDialogPane().getStylesheets().add(externo);
            }
        }

        TextField buscar = new TextField();
        buscar.setPromptText(
                "Buscar por ID, nombre, apellido, documento o telefono...");
        buscar.getStyleClass().add("dialog-field");
        buscar.textProperty().addListener((obs, anterior, actual) ->
                aplicarFiltro(actual));

        configurarTabla();
        tabla.setItems(filtrados);
        tabla.setPlaceholder(new Label("No hay clientes para mostrar."));
        tabla.setPrefSize(860, 470);
        tabla.setFixedCellSize(48);
        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        VBox contenido = new VBox(10, buscar, tabla);
        contenido.setPadding(new Insets(8));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(920, 620);
        dialogo.setResizable(true);
        dialogo.setOnShown(evento -> {
            Stage ventana = (Stage) dialogo.getDialogPane()
                    .getScene().getWindow();
            ventana.setMaximized(false);
            ventana.setFullScreen(false);
            ventana.setWidth(920);
            ventana.setHeight(620);
            ventana.centerOnScreen();
        });

        javafx.scene.Node botonSeleccionar =
                dialogo.getDialogPane().lookupButton(seleccionar);
        botonSeleccionar.disableProperty().bind(
                tabla.getSelectionModel().selectedItemProperty().isNull());
        dialogo.setResultConverter(tipo ->
                tipo == seleccionar
                        ? tabla.getSelectionModel().getSelectedItem()
                        : null);
        tabla.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2
                    && tabla.getSelectionModel().getSelectedItem() != null) {
                botonSeleccionar.fireEvent(new javafx.event.ActionEvent());
            }
        });
    }

    private void configurarTabla() {
        TableColumn<Cliente, Long> id = new TableColumn<>("ID");
        id.setCellValueFactory(datos ->
                new SimpleLongProperty(datos.getValue().getId()).asObject());
        id.setPrefWidth(70);

        TableColumn<Cliente, String> nombre =
                new TableColumn<>("NOMBRE COMPLETO");
        nombre.setCellValueFactory(datos ->
                new SimpleStringProperty(
                        datos.getValue().getNombreCompleto()));
        nombre.setPrefWidth(250);

        TableColumn<Cliente, String> documento =
                new TableColumn<>("DOCUMENTO");
        documento.setCellValueFactory(datos ->
                new SimpleStringProperty(
                        valor(datos.getValue().getDocumento())));
        documento.setPrefWidth(150);

        TableColumn<Cliente, String> telefono =
                new TableColumn<>("TELEFONO");
        telefono.setCellValueFactory(datos ->
                new SimpleStringProperty(
                        valor(datos.getValue().getTelefono())));
        telefono.setPrefWidth(190);

        tabla.getColumns().addAll(id, nombre, documento, telefono);
    }

    private void aplicarFiltro(String texto) {
        String filtro = texto == null ? ""
                : texto.trim().toLowerCase(Locale.ROOT);
        filtrados.setPredicate(cliente -> filtro.isBlank()
                || contiene(String.valueOf(cliente.getId()), filtro)
                || contiene(cliente.getNombre(), filtro)
                || contiene(cliente.getApellido(), filtro)
                || contiene(cliente.getNombreCompleto(), filtro)
                || contiene(cliente.getDocumento(), filtro)
                || contiene(cliente.getTelefono(), filtro));
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null
                && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }
}
