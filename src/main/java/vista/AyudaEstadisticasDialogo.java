package vista;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class AyudaEstadisticasDialogo {
    private static final Map<String, Seccion> SECCIONES = crearSecciones();

    private AyudaEstadisticasDialogo() { }

    public static void mostrar(String seccionInicial) {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Cómo leer las estadísticas");
        dialogo.setHeaderText("Guía de interpretación");
        dialogo.setGraphic(null);

        ButtonType entendido = new ButtonType(
                "ENTENDIDO", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().add(entendido);

        VBox contenido = new VBox(14);
        contenido.getStyleClass().add("stats-help-root");
        contenido.setPadding(new Insets(4));

        Label introduccion = new Label(
                "Consultá qué representa cada indicador, qué fecha utiliza "
                + "y qué limitaciones deben tenerse en cuenta.");
        introduccion.setWrapText(true);
        introduccion.getStyleClass().add("stats-help-intro");

        HBox navegacion = new HBox(7);
        navegacion.setAlignment(Pos.CENTER_LEFT);
        navegacion.getStyleClass().add("stats-help-navigation");
        ToggleGroup grupo = new ToggleGroup();

        VBox cuerpo = new VBox(12);
        cuerpo.getStyleClass().add("stats-help-body");

        String inicial = SECCIONES.containsKey(seccionInicial)
                ? seccionInicial : "RESUMEN";
        for (Map.Entry<String, Seccion> entrada : SECCIONES.entrySet()) {
            ToggleButton boton = new ToggleButton(entrada.getKey());
            boton.setToggleGroup(grupo);
            boton.getStyleClass().add("stats-help-tab");
            boton.setOnAction(evento -> {
                if (!boton.isSelected()) boton.setSelected(true);
                cargar(cuerpo, entrada.getValue());
            });
            navegacion.getChildren().add(boton);
            if (entrada.getKey().equals(inicial)) {
                boton.setSelected(true);
            }
        }
        cargar(cuerpo, SECCIONES.get(inicial));

        ScrollPane scroll = new ScrollPane(cuerpo);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportWidth(900);
        scroll.setPrefViewportHeight(560);
        scroll.getStyleClass().add("stats-help-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        contenido.getChildren().addAll(introduccion, navegacion, scroll);

        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setPrefSize(980, 700);
        Dialogos.preparar(dialogo, "stats-help-dialog");
        Node cerrar = dialogo.getDialogPane().lookupButton(entendido);
        cerrar.getStyleClass().add("dialog-action-primary");
        dialogo.showAndWait();
    }

    private static void cargar(VBox cuerpo, Seccion seccion) {
        cuerpo.getChildren().clear();
        Label titulo = new Label(seccion.titulo());
        titulo.getStyleClass().add("stats-help-title");
        Label fecha = new Label(seccion.fecha());
        fecha.setWrapText(true);
        fecha.getStyleClass().add("stats-help-period");
        cuerpo.getChildren().addAll(titulo, fecha);
        for (Bloque bloque : seccion.bloques()) {
            VBox tarjeta = new VBox(5);
            tarjeta.getStyleClass().add("stats-help-card");
            Label nombre = new Label(bloque.nombre());
            nombre.getStyleClass().add("stats-help-card-title");
            Label texto = new Label(bloque.texto());
            texto.setWrapText(true);
            texto.getStyleClass().add("stats-help-card-copy");
            tarjeta.getChildren().addAll(nombre, texto);
            cuerpo.getChildren().add(tarjeta);
        }
    }

    private static Map<String, Seccion> crearSecciones() {
        Map<String, Seccion> mapa = new LinkedHashMap<>();
        mapa.put("RESUMEN", new Seccion("Resumen ejecutivo",
                "Las reservas se filtran por fecha del turno. Los pagos y "
                + "reembolsos utilizan sus respectivas fechas financieras.",
                new Bloque[] {
                    b("Solicitudes y reservas efectivas", "Solicitudes incluye todos los registros. Reservas efectivas suma pendientes, confirmadas, completadas y ausencias; excluye canceladas y expiradas."),
                    b("Ingresos y resultado", "Ingresos netos resta reembolsos a los pagos recibidos. Resultado operativo agrega ingresos manuales y descuenta egresos manuales de caja."),
                    b("Ocupación teórica", "Compara minutos reservados con la capacidad calculada mediante la configuración actual de las canchas."),
                    b("Comparación anterior", "Compara el período seleccionado con otro de igual duración inmediatamente anterior.")
                }));
        mapa.put("RESERVAS", new Seccion("Reservas",
                "Todas las métricas se organizan por la fecha del turno, no por la fecha en que se creó la reserva.",
                new Bloque[] {
                    b("Estados", "Pendientes esperan resolución; confirmadas tienen la seña necesaria; completadas finalizaron; ausencias no se presentaron; canceladas y expiradas dejaron de ocupar el turno."),
                    b("Confirmación", "Porcentaje de solicitudes que llegaron a confirmarse o a un estado posterior demostrable."),
                    b("Finalización", "Reservas completadas sobre reservas efectivas."),
                    b("Cancelación y ausencia", "Ambas tasas usan como base todas las solicitudes del período."),
                    b("Confirmación web", "Solicitudes web que actualmente están confirmadas, completadas o ausentes. El estado final no permite reconstruir todas las transiciones históricas.")
                }));
        mapa.put("FINANZAS", new Seccion("Finanzas",
                "Pagos usa fecha de acreditación; reembolsos usa fecha de devolución; caja manual y cierres usan su fecha operativa.",
                new Bloque[] {
                    b("Pagos recibidos", "Incluye pagos acreditados, aunque posteriormente hayan sido reembolsados."),
                    b("Dinero reembolsado", "Suma devoluciones realizadas dentro del período seleccionado."),
                    b("Resultado del período", "Pagos recibidos menos reembolsos, más ingresos manuales y menos egresos manuales."),
                    b("Diferencia de efectivo", "Suma sobrantes y faltantes declarados en los cierres de caja."),
                    b("Importe promedio", "Promedio por pago acreditado, no promedio por reserva ni por cliente.")
                }));
        mapa.put("CLIENTES", new Seccion("Clientes",
                "Clientes nuevos usa la fecha de creación de la ficha. La actividad por reservas usa la fecha del turno.",
                new Bloque[] {
                    b("Clientes que reservaron", "Clientes distintos con al menos una reserva efectiva dentro del período."),
                    b("Clientes que volvieron", "Clientes con dos o más reservas efectivas en el período seleccionado."),
                    b("Porcentaje que volvió", "Clientes que volvieron dividido por clientes que reservaron."),
                    b("Cuentas web", "Una cuenta puede vincularse con una ficha administrativa existente; no significa necesariamente que la ficha se creó desde la web."),
                    b("Torneos y facturación", "Participación en torneos se informa por separado. La facturación por cliente incluye pagos de reservas, no precios de inscripción a torneos.")
                }));
        mapa.put("CANCHAS", new Seccion("Canchas",
                "Reservas utiliza fecha del turno. Pagos por cancha utiliza fecha de acreditación.",
                new Bloque[] {
                    b("Cancha más reservada", "Cancha con mayor cantidad de reservas efectivas durante el período."),
                    b("Cancha con más ingresos", "Cancha cuyas reservas acumularon más pagos recibidos."),
                    b("Uso por reservas", "Minutos reservados sobre capacidad disponible después de descontar bloqueos y partidos de torneo."),
                    b("Uso total", "Combina reservas, bloqueos y partidos de torneo sobre la capacidad configurada."),
                    b("Limitación histórica", "La capacidad usa horarios, días disponibles y estado activo actuales. Los cambios históricos de configuración no pueden reconstruirse exactamente.")
                }));
        mapa.put("WEB", new Seccion("Solicitudes web",
                "Esta sección se filtra por fecha de creación de la solicitud, no por la fecha futura del turno.",
                new Bloque[] {
                    b("Solicitudes con pago", "Solicitudes que tuvieron al menos un pago acreditado. Un pago después reembolsado conserva ese antecedente."),
                    b("Acreditación web", "Solicitudes con pago divididas por solicitudes web creadas en el período."),
                    b("Demora promedio del pago", "Tiempo entre la creación real de la solicitud y su primera acreditación registrada."),
                    b("Expiración", "Solicitudes que vencieron sin completar la acreditación necesaria, divididas por solicitudes creadas."),
                    b("Embudo", "Muestra solicitudes creadas, con pago, confirmadas o resueltas y completadas. Estado actual y acreditación histórica se presentan por separado.")
                }));
        return mapa;
    }

    private static Bloque b(String nombre, String texto) {
        return new Bloque(nombre, texto);
    }

    private record Seccion(String titulo, String fecha, Bloque[] bloques) { }
    private record Bloque(String nombre, String texto) { }
}
