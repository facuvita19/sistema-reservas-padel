package vista.controlador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.ResumenDashboard;
import negocio.Usuario;
import servicio.DashboardService;
import vista.FiltroPagos;
import vista.FiltroReservas;
import vista.Navegacion;

public class DashboardAdminController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final DashboardService dashboardService = new DashboardService();

    @FXML private Label etiquetaUsuario;
    @FXML private Label etiquetaFecha;
    @FXML private Label etiquetaReservasHoy;
    @FXML private Label etiquetaCanchasActivas;
    @FXML private Label etiquetaIngresosDia;
    @FXML private Label etiquetaIngresosMes;
    @FXML private Label etiquetaPagosPendientes;
    @FXML private Label etiquetaEstadoCarga;
    @FXML private Label alertaSenias;
    @FXML private Label alertaVencimientos;
    @FXML private Label alertaPagos;
    @FXML private Label alertaTurnos;
    @FXML private Label alertaCaja;
    @FXML private ProgressIndicator indicadorCarga;
    @FXML private Button botonActualizar;
    @FXML private Button botonUsuarios;
    @FXML private Button botonConfiguracion;
    @FXML private Button botonCanchas;
    @FXML private Button botonBloqueos;
    @FXML private Button botonEstadisticas;
    @FXML private VBox tarjetaIngresosMes;
    @FXML private Button accesoRapidoCanchas;
    @FXML private TableView<Reserva> tablaProximasReservas;
    @FXML private TableColumn<Reserva, LocalDate> columnaFecha;
    @FXML private TableColumn<Reserva, LocalTime> columnaHora;
    @FXML private TableColumn<Reserva, String> columnaCancha;
    @FXML private TableColumn<Reserva, String> columnaCliente;
    @FXML private TableColumn<Reserva, EstadoReserva> columnaEstado;

    @FXML
    private void initialize() {
        mostrarDatosSesion();
        configurarTabla();
        aplicarPermisos();
        Platform.runLater(this::actualizarDashboard);
    }

    private void aplicarPermisos() {
        Usuario usuario = Navegacion.getUsuarioActual();
        boolean administrador = usuario != null && usuario.esAdministrador();

        controlarAcceso(botonUsuarios, administrador);
        controlarAcceso(botonConfiguracion, administrador);
        controlarAcceso(botonCanchas, administrador);
        controlarAcceso(botonEstadisticas, administrador);
        controlarAcceso(tarjetaIngresosMes, administrador);
        controlarAcceso(accesoRapidoCanchas, administrador);
        controlarAcceso(botonBloqueos, true);
    }

    private void controlarAcceso(Node nodo, boolean permitido) {
        if (nodo == null) {
            throw new IllegalStateException(
                    "Falta conectar un control de permisos en dashboard-admin.fxml."
            );
        }

        nodo.setVisible(permitido);
        nodo.setManaged(permitido);
    }

    private void mostrarDatosSesion() {
        Usuario usuario = Navegacion.getUsuarioActual();

        etiquetaUsuario.setText(
                usuario == null ? "Personal" : usuario.getNombreUsuario()
        );

        DateTimeFormatter formato = DateTimeFormatter.ofPattern(
                "EEEE d 'de' MMMM 'de' yyyy",
                new Locale("es", "AR")
        );

        etiquetaFecha.setText(
                capitalizar(LocalDate.now().format(formato))
        );
    }

    private void configurarTabla() {
        columnaFecha.setCellValueFactory(
                new PropertyValueFactory<>("fecha")
        );
        columnaFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null
                        ? null
                        : valor.format(FORMATO_FECHA));
            }
        });

        columnaHora.setCellValueFactory(
                new PropertyValueFactory<>("horaInicio")
        );
        columnaHora.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null
                        ? null
                        : valor.format(FORMATO_HORA));
            }
        });

        columnaCancha.setCellValueFactory(
                new PropertyValueFactory<>("nombreCancha")
        );
        columnaCliente.setCellValueFactory(
                new PropertyValueFactory<>("nombreCliente")
        );
        columnaEstado.setCellValueFactory(
                new PropertyValueFactory<>("estado")
        );

        tablaProximasReservas.setRowFactory(tabla -> {
            javafx.scene.control.TableRow<Reserva> fila =
                    new javafx.scene.control.TableRow<>();

            fila.setOnMouseClicked(evento -> {
                if (evento.getButton() == MouseButton.PRIMARY
                        && evento.getClickCount() == 2
                        && !fila.isEmpty()) {

                    abrirReservaSeleccionada(fila.getItem());
                }
            });

            return fila;
        });
    }

    private void abrirReservaSeleccionada(Reserva reserva) {
        if (reserva == null || reserva.getId() <= 0) {
            cambiarEstadoCarga(
                    false,
                    "No se pudo identificar la reserva seleccionada."
            );
            return;
        }

        Navegacion.mostrarReservaDesdeAgenda(reserva.getId());
    }

    @FXML
    public void actualizarDashboard() {
        cambiarEstadoCarga(true, "Actualizando información...");

        Task<DatosDashboard> tarea = new Task<>() {
            @Override
            protected DatosDashboard call() {
                return new DatosDashboard(
                        dashboardService.obtenerResumen(),
                        dashboardService.listarProximasReservas()
                );
            }
        };

        tarea.setOnSucceeded(evento -> {
            DatosDashboard datos = tarea.getValue();
            mostrarResumen(datos.resumen());
            tablaProximasReservas.getItems().setAll(datos.proximas());
            cambiarEstadoCarga(false, "Datos actualizados correctamente.");
        });

        tarea.setOnFailed(evento -> {
            Throwable error = tarea.getException();

            if (error != null) {
                error.printStackTrace();
            }

            cambiarEstadoCarga(
                    false,
                    error == null || error.getMessage() == null
                            ? "No se pudo actualizar el dashboard."
                            : error.getMessage()
            );
        });

        Thread hilo = new Thread(tarea, "dashboard-padel-reservas");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarResumen(ResumenDashboard resumen) {
        etiquetaReservasHoy.setText(
                String.valueOf(resumen.getReservasHoy())
        );
        etiquetaCanchasActivas.setText(
                String.valueOf(resumen.getCanchasActivas())
        );
        etiquetaIngresosDia.setText(
                formatearMoneda(resumen.getIngresosDia())
        );
        etiquetaIngresosMes.setText(
                formatearMoneda(resumen.getIngresosMes())
        );
        etiquetaPagosPendientes.setText(
                String.valueOf(resumen.getPagosPendientes())
        );

        alertaSenias.setText(
                resumen.getReservasPendientesSenia()
                        + " reserva(s) esperando seña"
        );
        alertaVencimientos.setText(
                resumen.getReservasProximasAVencer()
                        + " solicitud(es) web vencen en menos de 5 minutos"
        );
        alertaPagos.setText(
                resumen.getPagosPendientes()
                        + " pago(s) pendientes de acreditar"
        );
        alertaTurnos.setText(
                resumen.getTurnosPendientesCierre()
                        + " turno(s) pendientes de cerrar"
        );
        alertaCaja.setText(
                resumen.isCajaCerradaHoy()
                        ? "Caja del día cerrada"
                        : "Caja del día pendiente de cierre"
        );

        aplicarClaseAlerta(
                alertaVencimientos,
                resumen.getReservasProximasAVencer() > 0,
                "alert-critical"
        );
        aplicarClaseAlerta(
                alertaTurnos,
                resumen.getTurnosPendientesCierre() > 0,
                "alert-warning"
        );
        aplicarClaseAlerta(
                alertaCaja,
                !resumen.isCajaCerradaHoy(),
                "alert-warning"
        );
    }

    private void aplicarClaseAlerta(
            Label etiqueta,
            boolean activa,
            String claseActiva) {

        etiqueta.getStyleClass().removeAll(
                "alert-critical",
                "alert-warning",
                "alert-ok"
        );
        etiqueta.getStyleClass().add(
                activa ? claseActiva : "alert-ok"
        );
    }

    private String formatearMoneda(BigDecimal importe) {
        BigDecimal valor = importe == null
                ? BigDecimal.ZERO
                : importe.setScale(2, RoundingMode.HALF_UP);

        return NumberFormat.getCurrencyInstance(
                new Locale("es", "AR")
        ).format(valor);
    }

    private void cambiarEstadoCarga(boolean cargando, String mensaje) {
        indicadorCarga.setVisible(cargando);
        botonActualizar.setDisable(cargando);
        etiquetaEstadoCarga.setText(mensaje);

        etiquetaEstadoCarga.getStyleClass().removeAll(
                "dashboard-status-ok",
                "dashboard-status-error"
        );

        if (!cargando) {
            String texto = mensaje == null
                    ? ""
                    : mensaje.toLowerCase(Locale.ROOT);

            etiquetaEstadoCarga.getStyleClass().add(
                    texto.contains("no se pudo") || texto.contains("error")
                            ? "dashboard-status-error"
                            : "dashboard-status-ok"
            );
        }
    }

    @FXML
    private void verEsperandoSenia() {
        Navegacion.mostrarReservasConFiltro(
                FiltroReservas.PENDIENTES_SENIA
        );
    }

    @FXML
    private void verSolicitudesWeb() {
        Navegacion.mostrarReservasConFiltro(
                FiltroReservas.PROXIMAS_A_VENCER
        );
    }

    @FXML
    private void verPagosPendientes() {
        Navegacion.mostrarPagosConFiltro(
                FiltroPagos.PENDIENTES_ACREDITACION
        );
    }

    @FXML
    private void verTurnosPendientes() {
        Navegacion.mostrarReservasConFiltro(
                FiltroReservas.PENDIENTES_CIERRE
        );
    }

    @FXML private void abrirCanchas() { Navegacion.mostrarCanchas(); }
    @FXML private void abrirClientes() { Navegacion.mostrarClientes(); }
    @FXML private void abrirAgenda() { Navegacion.mostrarAgenda(); }
    @FXML private void abrirReservas() { Navegacion.mostrarReservas(); }
    @FXML private void abrirPagos() { Navegacion.mostrarPagos(); }
    @FXML private void abrirCierreCaja() { Navegacion.mostrarCierreCaja(); }
    @FXML private void abrirBloqueos() { Navegacion.mostrarBloqueos(); }
    @FXML private void abrirEstadisticas() { Navegacion.mostrarEstadisticas(); }
    @FXML private void abrirConfiguracion() { Navegacion.mostrarConfiguracion(); }
    @FXML private void abrirUsuarios() { Navegacion.mostrarUsuarios(); }
    @FXML private void cerrarSesion() { Navegacion.cerrarSesion(); }

    private String capitalizar(String texto) {
        return texto == null || texto.isBlank()
                ? ""
                : Character.toUpperCase(texto.charAt(0))
                        + texto.substring(1);
    }

    private record DatosDashboard(
            ResumenDashboard resumen,
            List<Reserva> proximas) {
    }
}
