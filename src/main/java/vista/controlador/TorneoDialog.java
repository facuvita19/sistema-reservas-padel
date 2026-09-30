package vista.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import negocio.Torneo;

public class TorneoDialog {
    private final Dialog<Torneo> dialogo = new Dialog<>();
    private final TextField nombre = new TextField();
    private final TextArea descripcion = new TextArea();
    private final DatePicker fechaInicio = new DatePicker();
    private final DatePicker fechaFin = new DatePicker();
    private final DatePicker inscripcionDesde = new DatePicker();
    private final DatePicker inscripcionHasta = new DatePicker();
    private final TextField horaDesde = new TextField("08:00");
    private final TextField horaHasta = new TextField("23:00");
    private final TextArea reglamento = new TextArea();
    private final Torneo original;

    public TorneoDialog(Torneo torneo) {
        original = torneo;
        configurar();
        if (torneo != null) cargar(torneo);
    }

    public Optional<Torneo> mostrar() { return dialogo.showAndWait(); }

    private void configurar() {
        dialogo.setTitle(original == null ? "Nuevo torneo" : "Editar torneo");
        dialogo.setHeaderText(original == null
                ? "Completá los datos generales del torneo."
                : "Actualizá los datos generales del torneo.");
        ButtonType guardar = new ButtonType("GUARDAR", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        descripcion.setPrefRowCount(3);
        descripcion.setWrapText(true);
        reglamento.setPrefRowCount(4);
        reglamento.setWrapText(true);
        horaDesde.setPromptText("HH:mm");
        horaHasta.setPromptText("HH:mm");

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        grilla.setPadding(new Insets(10));
        agregar(grilla, 0, "Nombre", nombre);
        agregar(grilla, 1, "Descripción", descripcion);
        agregar(grilla, 2, "Inicio del torneo", fechaInicio);
        agregar(grilla, 3, "Fin del torneo", fechaFin);
        agregar(grilla, 4, "Inscripción desde", inscripcionDesde);
        grilla.add(horaDesde, 2, 4);
        agregar(grilla, 5, "Inscripción hasta", inscripcionHasta);
        grilla.add(horaHasta, 2, 5);
        agregar(grilla, 6, "Reglamento", reglamento);
        dialogo.getDialogPane().setContent(grilla);
        dialogo.getDialogPane().setPrefSize(720, 650);
        dialogo.setResultConverter(tipo -> tipo == guardar ? construir() : null);
    }

    private void agregar(GridPane grilla, int fila, String etiqueta, javafx.scene.Node control) {
        grilla.add(new Label(etiqueta), 0, fila);
        grilla.add(control, 1, fila);
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
        LocalDate inicio = requerido(fechaInicio.getValue(), "La fecha de inicio es obligatoria.");
        LocalDate fin = requerido(fechaFin.getValue(), "La fecha de fin es obligatoria.");
        LocalDate desde = requerido(inscripcionDesde.getValue(), "La apertura de inscripción es obligatoria.");
        LocalDate hasta = requerido(inscripcionHasta.getValue(), "El cierre de inscripción es obligatorio.");
        LocalTime horaApertura = LocalTime.parse(horaDesde.getText().trim());
        LocalTime horaCierre = LocalTime.parse(horaHasta.getText().trim());

        Torneo torneo = new Torneo();
        if (original != null) torneo.setId(original.getId());
        torneo.setNombre(nombre.getText());
        torneo.setDescripcion(descripcion.getText());
        torneo.setFechaInicio(inicio);
        torneo.setFechaFin(fin);
        torneo.setInscripcionDesde(LocalDateTime.of(desde, horaApertura));
        torneo.setInscripcionHasta(LocalDateTime.of(hasta, horaCierre));
        torneo.setReglamento(reglamento.getText());
        return torneo;
    }

    private <T> T requerido(T valor, String mensaje) {
        if (valor == null) throw new IllegalArgumentException(mensaje);
        return valor;
    }

    private String valor(String texto) { return texto == null ? "" : texto; }
}
