package vista.controlador;

import java.util.Locale;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import servicio.ConsultaClientesJugadoresService;
import servicio.ConsultaClientesJugadoresService.ClienteDisponible;
import vista.Dialogos;

public class JugadorClienteDialog {
    public Optional<ClienteDisponible> mostrar(String titulo) {
        Dialog<ClienteDisponible> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);

        Label explicacion = new Label(
                "Buscá y seleccioná la ficha que corresponde a este jugador. La categoría y el historial deportivo se conservarán.");
        explicacion.setWrapText(true);
        explicacion.getStyleClass().add("player-dialog-intro");

        TextField buscar = new TextField();
        buscar.setPromptText("Buscar por nombre, documento o teléfono...");
        buscar.getStyleClass().add("player-client-search");

        TableView<ClienteDisponible> tabla = new TableView<>();
        tabla.getStyleClass().add("player-client-table");
        tabla.setFixedCellSize(46);
        tabla.setPrefHeight(252);
        tabla.setMinHeight(210);
        tabla.setMaxHeight(330);
        Label vacio = new Label("No hay clientes disponibles para vincular.\nTodos los clientes activos ya tienen un perfil deportivo.\nCreá primero una nueva ficha en Gestión de clientes y volvé a intentarlo.");
        vacio.setWrapText(true);
        vacio.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        vacio.getStyleClass().add("player-client-empty");
        tabla.setPlaceholder(vacio);

        TableColumn<ClienteDisponible, String> cliente = columna("CLIENTE", 0.46);
        cliente.setCellValueFactory(x -> texto(x.getValue().nombre()));
        TableColumn<ClienteDisponible, String> documento = columna("DOCUMENTO", 0.24);
        documento.setCellValueFactory(x -> texto(valor(x.getValue().documento())));
        TableColumn<ClienteDisponible, String> telefono = columna("TELÉFONO", 0.30);
        telefono.setCellValueFactory(x -> texto(valor(x.getValue().telefono())));
        tabla.getColumns().setAll(cliente, documento, telefono);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        ObservableList<ClienteDisponible> items = FXCollections.observableArrayList(
                new ConsultaClientesJugadoresService().listarDisponibles(""));
        FilteredList<ClienteDisponible> filtrados = new FilteredList<>(items, x -> true);
        tabla.setItems(filtrados);

        Label resultados = new Label();
        resultados.getStyleClass().add("player-client-results");
        actualizarResultados(resultados, filtrados.size());
        if (items.isEmpty()) resultados.setText("Sin fichas de cliente libres para vincular");
        buscar.textProperty().addListener((o, anterior, valor) -> {
            String consulta = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
            filtrados.setPredicate(x -> consulta.isBlank()
                    || contiene(x.nombre(), consulta)
                    || contiene(x.documento(), consulta)
                    || contiene(x.telefono(), consulta));
            actualizarResultados(resultados, filtrados.size());
        });

        VBox contenido = new VBox(10, explicacion, buscar, resultados, tabla);
        contenido.setPadding(new Insets(18, 20, 12, 20));
        contenido.setPrefWidth(650);
        contenido.getStyleClass().add("player-client-content");
        dialogo.getDialogPane().setContent(contenido);

        ButtonType seleccionar = new ButtonType("SELECCIONAR", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(seleccionar, ButtonType.CANCEL);
        Dialogos.preparar(dialogo, "dialog-client-search");
        Node boton = dialogo.getDialogPane().lookupButton(seleccionar);
        boton.getStyleClass().add("dialog-action-primary");
        boton.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        tabla.setRowFactory(tv -> {
            TableRow<ClienteDisponible> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2
                        && !fila.isEmpty()) {
                    dialogo.setResult(fila.getItem());
                    dialogo.close();
                }
            });
            return fila;
        });
        dialogo.setOnShown(e -> buscar.requestFocus());
        dialogo.setResultConverter(tipo -> tipo == seleccionar
                ? tabla.getSelectionModel().getSelectedItem() : null);
        return dialogo.showAndWait();
    }

    private TableColumn<ClienteDisponible, String> columna(String titulo, double proporcion) {
        TableColumn<ClienteDisponible, String> columna = new TableColumn<>(titulo);
        columna.setReorderable(false);
        columna.setResizable(false);
        columna.setMinWidth(110);
        columna.setPrefWidth(640 * proporcion);
        columna.setStyle("-fx-alignment: CENTER-LEFT;");
        return columna;
    }

    private javafx.beans.property.SimpleStringProperty texto(String valor) {
        return new javafx.beans.property.SimpleStringProperty(valor);
    }
    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "Sin registrar" : texto;
    }
    private boolean contiene(String valor, String consulta) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(consulta);
    }
    private void actualizarResultados(Label label, int cantidad) {
        label.setText(cantidad + (cantidad == 1
                ? " cliente disponible" : " clientes disponibles"));
    }
}
