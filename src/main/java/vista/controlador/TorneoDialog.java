package vista.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import negocio.Torneo;
import vista.Dialogos;

public class TorneoDialog {
    // cerrar-ciclo-competitivo-torneos-v7
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
    private final ButtonType guardar = new ButtonType("GUARDAR CAMBIOS", ButtonBar.ButtonData.OK_DONE);

    public TorneoDialog(Torneo torneo) { this(torneo, false); }

    public TorneoDialog(Torneo torneo, boolean soloLectura) {
        original = torneo;
        this.soloLectura = soloLectura;
        configurar();
        if (torneo != null) cargar(torneo);
        if (soloLectura) aplicarSoloLectura();
    }

    public Optional<Torneo> mostrar() {
        while (true) {
            Optional<ButtonType> resultado = dialogo.showAndWait();
            if (resultado.isEmpty() || resultado.get() != guardar) return Optional.empty();
            try {
                error.setText("");
                return Optional.of(construir());
            } catch (IllegalArgumentException exception) {
                error.setText(exception.getMessage());
            }
        }
    }

    private void configurar() {
        boolean nuevo = original == null;
        dialogo.setTitle(soloLectura ? "Datos del torneo" : (nuevo ? "Nuevo torneo" : "Editar torneo"));
        dialogo.setHeaderText(soloLectura ? "Información general del torneo" : (nuevo ? "Crear un nuevo torneo" : "Actualizar datos del torneo"));
        if (soloLectura) {
            dialogo.getDialogPane().getButtonTypes().add(
                    new ButtonType("CERRAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        } else {
            dialogo.getDialogPane().getButtonTypes().addAll(guardar,
                    new ButtonType("CANCELAR", ButtonBar.ButtonData.CANCEL_CLOSE));
        }
        Dialogos.preparar(dialogo, "tournament-editor-dialog");

        nombre.setPromptText("Ejemplo: Copa Primavera 2026");
        descripcion.setPromptText("Descripción visible para los participantes");
        reglamento.setPromptText("Reglamento, condiciones y aclaraciones");
        descripcion.setPrefRowCount(3);
        descripcion.setWrapText(true);
        reglamento.setPrefRowCount(4);
        reglamento.setWrapText(true);
        horaDesde.setPromptText("HH:mm");
        horaHasta.setPromptText("HH:mm");
        aplicarCampo(nombre, descripcion, fechaInicio, fechaFin, inscripcionDesde,
                inscripcionHasta, horaDesde, horaHasta, reglamento);

        GridPane grilla = new GridPane();
        grilla.setHgap(12);
        grilla.setVgap(10);
        ColumnConstraints etiqueta = new ColumnConstraints(150);
        ColumnConstraints campo = new ColumnConstraints();
        campo.setHgrow(Priority.ALWAYS);
        ColumnConstraints hora = new ColumnConstraints(105);
        grilla.getColumnConstraints().addAll(etiqueta, campo, hora);

        agregar(grilla, 0, "Nombre *", nombre);
        agregar(grilla, 1, "Descripción", descripcion);
        agregar(grilla, 2, "Inicio del torneo *", fechaInicio);
        agregar(grilla, 3, "Fin del torneo *", fechaFin);
        agregar(grilla, 4, "Inscripción desde *", inscripcionDesde);
        grilla.add(horaDesde, 2, 4);
        agregar(grilla, 5, "Inscripción hasta *", inscripcionHasta);
        grilla.add(horaHasta, 2, 5);
        agregar(grilla, 6, "Reglamento", reglamento);

        Label ayuda = new Label("Las horas deben ingresarse en formato HH:mm, por ejemplo 08:00 o 23:30.");
        ayuda.getStyleClass().add("dialog-help");
        ayuda.setWrapText(true);
        error.getStyleClass().add("dialog-validation-error");
        error.setWrapText(true);

        VBox contenido = new VBox(14, grilla, ayuda, error);
        contenido.setPadding(new Insets(4, 2, 2, 2));
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(760, 680);
    }

    private void aplicarSoloLectura() {
        nombre.setEditable(false);
        descripcion.setEditable(false);
        fechaInicio.setDisable(true);
        fechaFin.setDisable(true);
        inscripcionDesde.setDisable(true);
        inscripcionHasta.setDisable(true);
        horaDesde.setEditable(false);
        horaHasta.setEditable(false);
        reglamento.setEditable(false);
        for (javafx.scene.Node control : new javafx.scene.Node[] { nombre,
                descripcion, fechaInicio, fechaFin, inscripcionDesde,
                inscripcionHasta, horaDesde, horaHasta, reglamento }) {
            control.getStyleClass().add("tournament-readonly-field-v7");
        }
    }

    private void aplicarCampo(javafx.scene.Node... controles) {
        for (javafx.scene.Node control : controles) control.getStyleClass().add("dialog-field");
    }

    private void agregar(GridPane grilla, int fila, String texto, javafx.scene.Node control) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("dialog-field-label");
        grilla.add(etiqueta, 0, fila);
        grilla.add(control, 1, fila);
        GridPane.setHgrow(control, Priority.ALWAYS);
    }

    private void cargar(Torneo torneo) {
        nombre.setText(torneo.getNombre());
        descripcion.setText(valor(torneo.getDescripcion()));
        fechaInicio.setValue(torneo.getFechaInicio());
        fechaFin.setValue(torneo.getFechaFin());
        if (torneo.getInscripcionDesde() != null) {
            inscripcionDesde.setValue(torneo.getInscripcionDesde().toLocalDate());
            horaDesde.setText(torneo.getInscripcionDesde().toLocalTime().toString());
        }
        if (torneo.getInscripcionHasta() != null) {
            inscripcionHasta.setValue(torneo.getInscripcionHasta().toLocalDate());
            horaHasta.setText(torneo.getInscripcionHasta().toLocalTime().toString());
        }
        reglamento.setText(valor(torneo.getReglamento()));
    }

    private Torneo construir() {
        if (nombre.getText() == null || nombre.getText().isBlank()) {
            throw new IllegalArgumentException("Ingresá el nombre del torneo.");
        }
        LocalDate inicio = requerido(fechaInicio.getValue(), "La fecha de inicio es obligatoria.");
        LocalDate fin = requerido(fechaFin.getValue(), "La fecha de fin es obligatoria.");
        LocalDate desde = requerido(inscripcionDesde.getValue(), "La apertura de inscripción es obligatoria.");
        LocalDate hasta = requerido(inscripcionHasta.getValue(), "El cierre de inscripción es obligatorio.");
        LocalTime horaApertura = hora(horaDesde.getText(), "La hora de apertura no es válida.");
        LocalTime horaCierre = hora(horaHasta.getText(), "La hora de cierre no es válida.");
        if (fin.isBefore(inicio)) throw new IllegalArgumentException("La fecha de fin no puede ser anterior al inicio.");
        if (LocalDateTime.of(hasta, horaCierre).isBefore(LocalDateTime.of(desde, horaApertura))) {
            throw new IllegalArgumentException("El cierre de inscripción no puede ser anterior a la apertura.");
        }

        Torneo torneo = new Torneo();
        if (original != null) torneo.setId(original.getId());
        torneo.setNombre(nombre.getText().trim());
        torneo.setDescripcion(descripcion.getText());
        torneo.setFechaInicio(inicio);
        torneo.setFechaFin(fin);
        torneo.setInscripcionDesde(LocalDateTime.of(desde, horaApertura));
        torneo.setInscripcionHasta(LocalDateTime.of(hasta, horaCierre));
        torneo.setReglamento(reglamento.getText());
        return torneo;
    }

    private LocalTime hora(String texto, String mensaje) {
        try { return LocalTime.parse(texto == null ? "" : texto.trim()); }
        catch (DateTimeParseException exception) { throw new IllegalArgumentException(mensaje); }
    }

    private <T> T requerido(T valor, String mensaje) {
        if (valor == null) throw new IllegalArgumentException(mensaje);
        return valor;
    }

    private String valor(String texto) { return texto == null ? "" : texto; }
}
