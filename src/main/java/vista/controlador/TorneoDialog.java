package vista.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import negocio.Torneo;
import vista.Dialogos;

public class TorneoDialog {
    private final Dialog<ButtonType> dialogo = new Dialog<>();
    private final TextField nombre = new TextField();
    private final TextArea descripcion = new TextArea();
    private final DatePicker fechaInicio = new DatePicker();
    private final DatePicker fechaFin = new DatePicker();
    private final DatePicker inscripcionDesde = new DatePicker();
    private final DatePicker inscripcionHasta = new DatePicker();
    private final TextField horaDesde = new TextField("08:00");
    private final TextField horaHasta = new TextField("23:00");
    private final TextArea reglamento = new TextArea();
    private final Label error = new Label();
    private final Torneo original;
    private final boolean soloLectura;
    private final ButtonType guardar;

    public TorneoDialog(Torneo torneo) { this(torneo, false); }

    public TorneoDialog(Torneo torneo, boolean soloLectura) {
        original = torneo;
        this.soloLectura = soloLectura;
        guardar = new ButtonType(torneo == null ? "CREAR TORNEO" : "GUARDAR CAMBIOS",
                ButtonBar.ButtonData.OK_DONE);
        configurar();
        if (torneo != null) cargar(torneo);
        if (soloLectura) aplicarSoloLectura();
        actualizarEstadoGuardar();
    }

    public Optional<Torneo> mostrar() {
        while (true) {
            Optional<ButtonType> resultado = dialogo.showAndWait();
            if (resultado.isEmpty() || resultado.get() != guardar) return Optional.empty();
            try {
                ocultarError();
                return Optional.of(construir());
            } catch (IllegalArgumentException exception) {
                mostrarError(exception.getMessage());
            }
        }
    }

    private void configurar() {
        boolean nuevo = original == null;
        dialogo.setTitle(soloLectura ? "Datos del torneo" : nuevo ? "Nuevo torneo" : "Editar torneo");
        dialogo.setHeaderText(soloLectura ? "Información general del torneo"
                : nuevo ? "Crear un nuevo torneo" : "Actualizar datos del torneo");
        if (soloLectura) {
            dialogo.getDialogPane().getButtonTypes().add(
                    new ButtonType("CERRAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        } else {
            dialogo.getDialogPane().getButtonTypes().addAll(guardar,
                    new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        }
        Dialogos.preparar(dialogo, "tournament-editor-dialog");
        dialogo.getDialogPane().getStyleClass().add("tournament-layout-final-v1");

        nombre.setPromptText("Ejemplo: Copa Primavera 2026");
        descripcion.setPromptText("Descripción visible para los participantes");
        reglamento.setPromptText("Reglamento, condiciones y aclaraciones");
        descripcion.setPrefRowCount(2);
        descripcion.setMinHeight(64); descripcion.setPrefHeight(64); descripcion.setMaxHeight(64);
        descripcion.setWrapText(true);
        reglamento.setPrefRowCount(2);
        reglamento.setMinHeight(70); reglamento.setPrefHeight(70); reglamento.setMaxHeight(70);
        reglamento.setWrapText(true);
        horaDesde.setPromptText("HH:mm"); horaHasta.setPromptText("HH:mm");
        horaDesde.setPrefWidth(100); horaDesde.setMinWidth(100); horaDesde.setMaxWidth(100);
        horaHasta.setPrefWidth(100); horaHasta.setMinWidth(100); horaHasta.setMaxWidth(100);
        aplicarCampo(nombre, descripcion, fechaInicio, fechaFin, inscripcionDesde,
                inscripcionHasta, horaDesde, horaHasta, reglamento);

        VBox general = seccion("INFORMACIÓN GENERAL");
        general.getChildren().addAll(campoVertical("Nombre *", nombre),
                campoVertical("Descripción", descripcion));

        GridPane calendario = dosColumnas();
        calendario.add(campoVertical("Inicio del torneo *", fechaInicio), 0, 0);
        calendario.add(campoVertical("Fin del torneo *", fechaFin), 1, 0);
        expandir(fechaInicio); expandir(fechaFin);
        VBox calendarioSeccion = seccion("CALENDARIO DEL TORNEO");
        calendarioSeccion.getChildren().add(calendario);

        GridPane inscripciones = dosColumnas();
        inscripciones.add(campoVertical("Inscripción desde *",
                fechaHora(inscripcionDesde, horaDesde)), 0, 0);
        inscripciones.add(campoVertical("Inscripción hasta *",
                fechaHora(inscripcionHasta, horaHasta)), 1, 0);
        Label ayuda = new Label("Formato horario de 24 horas: HH:mm. Ejemplos: 08:00 y 23:30.");
        ayuda.getStyleClass().add("dialog-help"); ayuda.setWrapText(true);
        VBox inscripcionSeccion = seccion("PERÍODO DE INSCRIPCIÓN");
        inscripcionSeccion.getChildren().addAll(inscripciones, ayuda);

        VBox participantes = seccion("INFORMACIÓN PARA PARTICIPANTES");
        participantes.getChildren().add(campoVertical("Reglamento", reglamento));

        error.getStyleClass().add("dialog-validation-error");
        error.setWrapText(true); ocultarError();
        VBox contenido = new VBox(10, general, calendarioSeccion, inscripcionSeccion,
                participantes, error);
        contenido.setPadding(new Insets(2, 2, 2, 2));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setMinWidth(820);
        dialogo.getDialogPane().setPrefWidth(860);
        dialogo.getDialogPane().setMaxWidth(900);
        dialogo.getDialogPane().setPrefHeight(705);

        if (!soloLectura) {
            Node botonGuardar = dialogo.getDialogPane().lookupButton(guardar);
            Runnable validar = this::actualizarEstadoGuardar;
            nombre.textProperty().addListener((o,a,n) -> validar.run());
            fechaInicio.valueProperty().addListener((o,a,n) -> validar.run());
            fechaFin.valueProperty().addListener((o,a,n) -> validar.run());
            inscripcionDesde.valueProperty().addListener((o,a,n) -> validar.run());
            inscripcionHasta.valueProperty().addListener((o,a,n) -> validar.run());
            horaDesde.textProperty().addListener((o,a,n) -> validar.run());
            horaHasta.textProperty().addListener((o,a,n) -> validar.run());
            Platform.runLater(() -> { botonGuardar.requestFocus(); nombre.requestFocus(); });
        }
    }

    private VBox seccion(String texto) {
        Label titulo = new Label(texto); titulo.getStyleClass().add("tournament-dialog-section-title-v1");
        VBox caja = new VBox(8, titulo); caja.getStyleClass().add("tournament-dialog-section-v1");
        return caja;
    }
    private VBox campoVertical(String texto, Node control) {
        Label etiqueta = new Label(texto); etiqueta.getStyleClass().add("dialog-field-label");
        VBox caja = new VBox(5, etiqueta, control); VBox.setVgrow(control, Priority.NEVER);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        return caja;
    }
    private GridPane dosColumnas() {
        GridPane g = new GridPane(); g.setHgap(14); g.setVgap(8);
        ColumnConstraints a = new ColumnConstraints(); a.setPercentWidth(50); a.setHgrow(Priority.ALWAYS);
        ColumnConstraints b = new ColumnConstraints(); b.setPercentWidth(50); b.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(a,b); return g;
    }
    private HBox fechaHora(DatePicker fecha, TextField hora) {
        expandir(fecha); HBox fila = new HBox(8, fecha, hora); HBox.setHgrow(fecha, Priority.ALWAYS);
        fila.setFillHeight(true); fila.setMaxWidth(Double.MAX_VALUE); return fila;
    }
    private void expandir(Region r) { r.setMaxWidth(Double.MAX_VALUE); GridPane.setHgrow(r, Priority.ALWAYS); }
    private void aplicarCampo(Node... controles) { for (Node c : controles) c.getStyleClass().add("dialog-field"); }

    private void aplicarSoloLectura() {
        nombre.setEditable(false); descripcion.setEditable(false); fechaInicio.setDisable(true);
        fechaFin.setDisable(true); inscripcionDesde.setDisable(true); inscripcionHasta.setDisable(true);
        horaDesde.setEditable(false); horaHasta.setEditable(false); reglamento.setEditable(false);
        for (Node control : new Node[] { nombre, descripcion, fechaInicio, fechaFin,
                inscripcionDesde, inscripcionHasta, horaDesde, horaHasta, reglamento }) {
            control.getStyleClass().add("tournament-readonly-field-v7");
        }
    }
    private void cargar(Torneo t) {
        nombre.setText(t.getNombre()); descripcion.setText(valor(t.getDescripcion()));
        fechaInicio.setValue(t.getFechaInicio()); fechaFin.setValue(t.getFechaFin());
        if (t.getInscripcionDesde()!=null) { inscripcionDesde.setValue(t.getInscripcionDesde().toLocalDate()); horaDesde.setText(t.getInscripcionDesde().toLocalTime().toString()); }
        if (t.getInscripcionHasta()!=null) { inscripcionHasta.setValue(t.getInscripcionHasta().toLocalDate()); horaHasta.setText(t.getInscripcionHasta().toLocalTime().toString()); }
        reglamento.setText(valor(t.getReglamento()));
    }
    private void actualizarEstadoGuardar() {
        if (soloLectura || dialogo.getDialogPane().lookupButton(guardar)==null) return;
        boolean valido = !texto(nombre).isBlank() && fechaInicio.getValue()!=null && fechaFin.getValue()!=null
                && inscripcionDesde.getValue()!=null && inscripcionHasta.getValue()!=null
                && horaValida(horaDesde.getText()) && horaValida(horaHasta.getText());
        dialogo.getDialogPane().lookupButton(guardar).setDisable(!valido);
    }
    private boolean horaValida(String t) { try { LocalTime.parse(t==null?"":t.trim()); return true; } catch (RuntimeException e) { return false; } }
    private String texto(TextField c) { return c.getText()==null?"":c.getText().trim(); }
    private void mostrarError(String m) { error.setText(m); error.setVisible(true); error.setManaged(true); }
    private void ocultarError() { error.setText(""); error.setVisible(false); error.setManaged(false); }
    private Torneo construir() {
        if (texto(nombre).isBlank()) throw new IllegalArgumentException("Ingresá el nombre del torneo.");
        LocalDate inicio=requerido(fechaInicio.getValue(),"La fecha de inicio es obligatoria.");
        LocalDate fin=requerido(fechaFin.getValue(),"La fecha de fin es obligatoria.");
        LocalDate desde=requerido(inscripcionDesde.getValue(),"La apertura de inscripción es obligatoria.");
        LocalDate hasta=requerido(inscripcionHasta.getValue(),"El cierre de inscripción es obligatorio.");
        LocalTime hDesde=hora(horaDesde.getText(),"La hora de apertura no es válida.");
        LocalTime hHasta=hora(horaHasta.getText(),"La hora de cierre no es válida.");
        if (fin.isBefore(inicio)) throw new IllegalArgumentException("La fecha de fin no puede ser anterior al inicio.");
        if (LocalDateTime.of(hasta,hHasta).isBefore(LocalDateTime.of(desde,hDesde))) throw new IllegalArgumentException("El cierre de inscripción no puede ser anterior a la apertura.");
        Torneo t=new Torneo(); if(original!=null)t.setId(original.getId()); t.setNombre(texto(nombre));
        t.setDescripcion(descripcion.getText()); t.setFechaInicio(inicio); t.setFechaFin(fin);
        t.setInscripcionDesde(LocalDateTime.of(desde,hDesde)); t.setInscripcionHasta(LocalDateTime.of(hasta,hHasta)); t.setReglamento(reglamento.getText()); return t;
    }
    private LocalTime hora(String t,String m){try{return LocalTime.parse(t==null?"":t.trim());}catch(DateTimeParseException e){throw new IllegalArgumentException(m);}}
    private <T> T requerido(T v,String m){if(v==null)throw new IllegalArgumentException(m);return v;}
    private String valor(String t){return t==null?"":t;}
}
