package vista.controlador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import negocio.Cliente;
import negocio.EstadoReserva;
import negocio.Reserva;
import servicio.ClienteService;
import servicio.PagoService;
import servicio.ReservaService;
import util.FormateadorMoneda;
import vista.Navegacion;
import vista.Dialogos;

public class ClientesController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ClienteService clienteService = new ClienteService();
    private final ReservaService reservaService = new ReservaService();
    private final PagoService pagoService = new PagoService();
    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private final ObservableList<Reserva> historial = FXCollections.observableArrayList();
    private final Map<Long, BigDecimal> acreditadoPorReserva = new HashMap<>();
    private final Map<Long, BigDecimal> saldoPorReserva = new HashMap<>();

    private FilteredList<Cliente> clientesFiltrados;
    private Cliente clienteSeleccionado;
    private long solicitudHistorialActual;

    @FXML private TextField campoBuscar;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> columnaNombre;
    @FXML private TableColumn<Cliente, String> columnaDocumento;
    @FXML private TableColumn<Cliente, String> columnaTelefono;
    @FXML private TableColumn<Cliente, String> columnaEmail;
    @FXML private TableColumn<Cliente, String> columnaEstado;
    @FXML private Label tituloFormulario;
    @FXML private Label subtituloFormulario;
    @FXML private Label insigniaEstadoCliente;
    @FXML private Label etiquetaMensaje;
    @FXML private TextField campoNombre;
    @FXML private TextField campoApellido;
    @FXML private TextField campoDocumento;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoEmail;
    @FXML private Button botonGuardar;
    @FXML private Button botonDesactivar;
    @FXML private Button botonReactivar;
    @FXML private Button botonEliminarDefinitivamente;
    @FXML private Button botonNuevaReserva;
    @FXML private Label etiquetaTotalReservas;
    @FXML private Label etiquetaCompletadas;
    @FXML private Label etiquetaCanceladas;
    @FXML private Label etiquetaAusencias;
    @FXML private Label etiquetaTotalAcreditado;
    @FXML private Label etiquetaSaldoPendiente;
    @FXML private Label etiquetaProximaReserva;
    @FXML private TableView<Reserva> tablaHistorial;
    @FXML private TableColumn<Reserva, LocalDate> columnaHistorialFecha;
    @FXML private TableColumn<Reserva, String> columnaHistorialHorario;
    @FXML private TableColumn<Reserva, String> columnaHistorialCancha;
    @FXML private TableColumn<Reserva, EstadoReserva> columnaHistorialEstado;
    @FXML private TableColumn<Reserva, BigDecimal> columnaHistorialPrecio;
    @FXML private TableColumn<Reserva, BigDecimal> columnaHistorialAcreditado;
    @FXML private TableColumn<Reserva, BigDecimal> columnaHistorialSaldo;

    @FXML
    private void initialize() {
        configurarTablaClientes();
        configurarTablaHistorial();
        configurarBusqueda();
        cargarClientes();
        nuevo();
        tablaClientes.setOnKeyPressed(evento -> {
            if (evento.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                limpiarSeleccion();
                evento.consume();
            }
        });
    }

    private void configurarTablaClientes() {
        columnaNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombreCompleto()));
        columnaDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));
        columnaTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        columnaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        columnaEstado.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().isActivo() ? "Activo" : "Inactivo"));
        columnaEstado.setCellFactory(columna -> new TableCell<>() {
            private final Label insignia = new Label();
            { insignia.getStyleClass().add("client-status-badge"); setText(null); }
            @Override protected void updateItem(String estado, boolean vacia) {
                super.updateItem(estado, vacia);
                if (vacia || estado == null) { setGraphic(null); return; }
                boolean activo = "Activo".equals(estado);
                insignia.setText(estado.toUpperCase(Locale.ROOT));
                insignia.getStyleClass().removeAll("client-status-active", "client-status-inactive");
                insignia.getStyleClass().add(activo ? "client-status-active" : "client-status-inactive");
                setGraphic(insignia);
            }
        });
        tablaClientes.setRowFactory(tabla -> {
            TableRow<Cliente> fila = new TableRow<>();
            fila.itemProperty().addListener((obs, anterior, actual) -> actualizarClaseFilaCliente(fila, actual));
            fila.selectedProperty().addListener((obs, anterior, actual) -> actualizarClaseFilaCliente(fila, fila.getItem()));
            return fila;
        });
        tablaClientes.setOnMouseClicked(evento -> {
            if (clicEnFondoTabla(evento.getTarget())) nuevo();
        });
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((o, a, actual) -> {
            if (actual != null) editar(actual);
        });
    }

    private void configurarTablaHistorial() {
        columnaHistorialFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columnaHistorialFecha.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : fecha.format(FORMATO_FECHA));
            }
        });
        columnaHistorialHorario.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(formatearHorario(d.getValue())));
        columnaHistorialCancha.setCellValueFactory(new PropertyValueFactory<>("nombreCancha"));
        columnaHistorialEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columnaHistorialEstado.setCellFactory(columna -> new TableCell<>() {
            private final Label insignia = new Label();
            { insignia.getStyleClass().add("client-history-status"); setText(null); }
            @Override protected void updateItem(EstadoReserva estado, boolean vacia) {
                super.updateItem(estado, vacia);
                if (vacia || estado == null) { setGraphic(null); return; }
                insignia.setText(nombreEstado(estado));
                insignia.getStyleClass().removeIf(clase -> clase.startsWith("client-history-status-") && !clase.equals("client-history-status"));
                insignia.getStyleClass().add("client-history-status-" + estado.name().toLowerCase(Locale.ROOT));
                setGraphic(insignia);
            }
        });
        columnaHistorialPrecio.setCellValueFactory(new PropertyValueFactory<>("precioTotal"));
        columnaHistorialAcreditado.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(acreditadoPorReserva.getOrDefault(d.getValue().getId(), BigDecimal.ZERO)));
        columnaHistorialSaldo.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(saldoPorReserva.getOrDefault(d.getValue().getId(), BigDecimal.ZERO)));
        configurarColumnaMoneda(columnaHistorialPrecio);
        configurarColumnaMoneda(columnaHistorialAcreditado);
        configurarColumnaMoneda(columnaHistorialSaldo);
        tablaHistorial.setItems(historial);
        tablaHistorial.setRowFactory(t -> {
            javafx.scene.control.TableRow<Reserva> fila = new javafx.scene.control.TableRow<>();
            fila.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !fila.isEmpty()) abrirReserva(fila.getItem()); });
            return fila;
        });
    }

    private void configurarColumnaMoneda(TableColumn<Reserva, BigDecimal> columna) {
        columna.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal importe, boolean vacia) {
                super.updateItem(importe, vacia);
                setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                setText(vacia || importe == null ? null : formatearMoneda(importe));
            }
        });
    }

    private String nombreEstado(EstadoReserva estado) {
        return switch (estado) {
            case PENDIENTE -> "Esperando seña";
            case CONFIRMADA -> "Confirmada";
            case COMPLETADA -> "Completada";
            case CANCELADA -> "Cancelada";
            case AUSENTE -> "Ausente";
            case EXPIRADA -> "Expirada";
        };
    }

    private String formatearHorario(Reserva r) {
        if (r == null || r.getHoraInicio() == null) return "";
        String inicio = r.getHoraInicio().format(FORMATO_HORA);
        return r.getHoraFin() == null ? inicio : inicio + " - " + r.getHoraFin().format(FORMATO_HORA);
    }

    private void actualizarClaseFilaCliente(TableRow<Cliente> fila, Cliente cliente) {
        fila.getStyleClass().removeAll("client-row-inactive", "client-row-selected");
        if (cliente == null || fila.isEmpty()) return;
        if (!cliente.isActivo()) fila.getStyleClass().add("client-row-inactive");
        if (fila.isSelected()) fila.getStyleClass().add("client-row-selected");
    }

    private boolean clicEnFondoTabla(Object objetivo) {
        if (!(objetivo instanceof Node nodo)) return false;
        Node actual = nodo;
        while (actual != null && actual != tablaClientes) {
            if (actual instanceof TableRow<?> fila) return fila.isEmpty();
            actual = actual.getParent();
        }
        return actual == tablaClientes;
    }

    private void configurarBusqueda() {
        clientesFiltrados = new FilteredList<>(clientes, c -> true);
        tablaClientes.setItems(clientesFiltrados);
        campoBuscar.textProperty().addListener((o, a, texto) -> {
            String f = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
            clientesFiltrados.setPredicate(c -> f.isBlank() || contiene(c.getNombreCompleto(), f)
                    || contiene(c.getDocumento(), f) || contiene(c.getTelefono(), f) || contiene(c.getEmail(), f));
        });
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    @FXML
    private void limpiarSeleccion() {
        nuevo();
        campoBuscar.requestFocus();
    }

    @FXML
    private void cargarClientes() {
        try {
            List<Cliente> resultado = clienteService.listarTodos();
            clientes.setAll(resultado);
            mostrarInfo(resultado.size() + " cliente(s) cargado(s).");
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    @FXML
    private void nuevo() {
        clienteSeleccionado = null;
        tablaClientes.getSelectionModel().clearSelection();
        tituloFormulario.setText("Nuevo cliente");
        subtituloFormulario.setText("Registrá los datos personales y de contacto.");
        insigniaEstadoCliente.setVisible(false);
        insigniaEstadoCliente.setManaged(false);
        botonGuardar.setText("GUARDAR CLIENTE");
        mostrarBoton(botonDesactivar, false);
        mostrarBoton(botonReactivar, false);
        mostrarBoton(botonEliminarDefinitivamente, false);
        mostrarBoton(botonNuevaReserva, false);
        botonGuardar.setDisable(false);
        campoNombre.clear(); campoApellido.clear(); campoDocumento.clear(); campoTelefono.clear(); campoEmail.clear();
        etiquetaMensaje.setText("");
        limpiarHistorial();
        campoNombre.requestFocus();
    }

    private void editar(Cliente cliente) {
        clienteSeleccionado = cliente;
        tituloFormulario.setText("Editar cliente");
        subtituloFormulario.setText(
                cliente.getNombreCompleto()
                        + "\nDocumento "
                        + cliente.getDocumento()
                        + " · "
                        + cliente.getTelefono());
        insigniaEstadoCliente.setText(cliente.isActivo() ? "ACTIVO" : "INACTIVO");
        insigniaEstadoCliente.getStyleClass().removeAll("client-detail-active", "client-detail-inactive");
        insigniaEstadoCliente.getStyleClass().add(cliente.isActivo() ? "client-detail-active" : "client-detail-inactive");
        insigniaEstadoCliente.setVisible(true);
        insigniaEstadoCliente.setManaged(true);
        botonGuardar.setText("GUARDAR CAMBIOS");
        mostrarBoton(botonDesactivar, cliente.isActivo());
        mostrarBoton(botonReactivar, !cliente.isActivo());
        mostrarBoton(botonEliminarDefinitivamente, !cliente.isActivo());
        mostrarBoton(botonNuevaReserva, cliente.isActivo());
        botonGuardar.setDisable(!cliente.isActivo());
        campoNombre.setText(cliente.getNombre());
        campoApellido.setText(cliente.getApellido());
        campoDocumento.setText(cliente.getDocumento());
        campoTelefono.setText(cliente.getTelefono());
        campoEmail.setText(cliente.getEmail());
        etiquetaMensaje.setText("");
        cargarHistorial(cliente.getId());
    }

    private void cargarHistorial(long clienteId) {
        long solicitud = ++solicitudHistorialActual;
        prepararCargaHistorial();

        Task<ResultadoHistorial> tarea = new Task<>() {
            @Override
            protected ResultadoHistorial call() {
                List<Reserva> lista = reservaService.listarPorCliente(clienteId);
                Map<Long, BigDecimal> acreditados = new HashMap<>();
                Map<Long, BigDecimal> saldos = new HashMap<>();
                BigDecimal totalAcreditado = BigDecimal.ZERO;
                BigDecimal saldoPendiente = BigDecimal.ZERO;

                for (Reserva reserva : lista) {
                    BigDecimal acreditado = pagoService.totalAcreditado(
                            reserva.getId());
                    BigDecimal saldo = pagoService.calcularSaldo(
                            reserva.getId());
                    acreditado = acreditado == null
                            ? BigDecimal.ZERO : acreditado;
                    saldo = saldo == null ? BigDecimal.ZERO : saldo;
                    acreditados.put(reserva.getId(), acreditado);
                    saldos.put(reserva.getId(), saldo);
                    totalAcreditado = totalAcreditado.add(acreditado);
                    if (reserva.getEstado() != EstadoReserva.CANCELADA
                            && reserva.getEstado() != EstadoReserva.EXPIRADA) {
                        saldoPendiente = saldoPendiente.add(saldo);
                    }
                }

                return new ResultadoHistorial(
                        lista, acreditados, saldos,
                        totalAcreditado, saldoPendiente);
            }
        };

        tarea.setOnSucceeded(evento -> {
            if (solicitud != solicitudHistorialActual
                    || clienteSeleccionado == null
                    || clienteSeleccionado.getId() != clienteId) return;

            ResultadoHistorial resultado = tarea.getValue();
            acreditadoPorReserva.clear();
            acreditadoPorReserva.putAll(resultado.acreditados());
            saldoPorReserva.clear();
            saldoPorReserva.putAll(resultado.saldos());
            historial.setAll(resultado.reservas());
            actualizarResumen(
                    resultado.reservas(),
                    resultado.totalAcreditado(),
                    resultado.saldoPendiente());
            tablaHistorial.setDisable(false);
            mostrarInfo(resultado.reservas().size()
                    + " reserva(s) cargada(s) para el cliente.");
        });

        tarea.setOnFailed(evento -> {
            if (solicitud != solicitudHistorialActual) return;
            limpiarHistorial();
            tablaHistorial.setDisable(false);
            Throwable error = tarea.getException();
            mostrarError("No se pudo cargar el historial: "
                    + (error == null ? "Error desconocido."
                            : error.getMessage()));
        });

        Thread hilo = new Thread(tarea,
                "historial-cliente-" + clienteId);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void prepararCargaHistorial() {
        historial.clear();
        acreditadoPorReserva.clear();
        saldoPorReserva.clear();
        etiquetaTotalReservas.setText("...");
        etiquetaCompletadas.setText("...");
        etiquetaCanceladas.setText("...");
        etiquetaAusencias.setText("...");
        etiquetaTotalAcreditado.setText("Calculando...");
        etiquetaSaldoPendiente.setText("Calculando...");
        etiquetaProximaReserva.setText("Cargando historial...");
        tablaHistorial.setDisable(true);
        mostrarInfo("Cargando historial y pagos del cliente...");
    }

    private record ResultadoHistorial(
            List<Reserva> reservas,
            Map<Long, BigDecimal> acreditados,
            Map<Long, BigDecimal> saldos,
            BigDecimal totalAcreditado,
            BigDecimal saldoPendiente) {
    }

    private void actualizarResumen(List<Reserva> lista, BigDecimal total, BigDecimal saldo) {
        etiquetaTotalReservas.setText(String.valueOf(lista.size()));
        etiquetaCompletadas.setText(String.valueOf(contar(lista, EstadoReserva.COMPLETADA)));
        etiquetaCanceladas.setText(String.valueOf(contar(lista, EstadoReserva.CANCELADA)));
        etiquetaAusencias.setText(String.valueOf(contar(lista, EstadoReserva.AUSENTE)));
        etiquetaTotalAcreditado.setText(formatearMoneda(total));
        etiquetaSaldoPendiente.setText(formatearMoneda(saldo));
        etiquetaProximaReserva.setText(buscarProxima(lista));
    }

    private long contar(List<Reserva> lista, EstadoReserva estado) {
        return lista.stream().filter(r -> r.getEstado() == estado).count();
    }

    private String buscarProxima(List<Reserva> lista) {
        LocalDateTime ahora = LocalDateTime.now();
        return lista.stream()
                .filter(r -> r.getFecha() != null && r.getHoraInicio() != null)
                .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA)
                .filter(r -> LocalDateTime.of(r.getFecha(), r.getHoraInicio()).isAfter(ahora))
                .min(Comparator.comparing(r -> LocalDateTime.of(r.getFecha(), r.getHoraInicio())))
                .map(r -> r.getFecha().format(FORMATO_FECHA) + " " + r.getHoraInicio().format(FORMATO_HORA) + " · " + r.getNombreCancha())
                .orElse("Sin próximas reservas");
    }

    private String formatearMoneda(BigDecimal valor) {
        return FormateadorMoneda.pesos(valor);
    }

    private void limpiarHistorial() {
        solicitudHistorialActual++;
        tablaHistorial.setDisable(false);
        historial.clear(); acreditadoPorReserva.clear(); saldoPorReserva.clear();
        etiquetaTotalReservas.setText("0"); etiquetaCompletadas.setText("0"); etiquetaCanceladas.setText("0"); etiquetaAusencias.setText("0");
        etiquetaTotalAcreditado.setText("ARS 0.00"); etiquetaSaldoPendiente.setText("ARS 0.00"); etiquetaProximaReserva.setText("Seleccioná un cliente");
    }

    private void mostrarBoton(Button boton, boolean mostrar) { boton.setVisible(mostrar); boton.setManaged(mostrar); }

    @FXML
    private void guardar() {
        try {
            Cliente c = clienteSeleccionado == null ? new Cliente() : clienteSeleccionado;
            c.setNombre(campoNombre.getText()); c.setApellido(campoApellido.getText()); c.setDocumento(campoDocumento.getText());
            c.setTelefono(campoTelefono.getText()); c.setEmail(campoEmail.getText());
            if (clienteSeleccionado == null) c.setActivo(true);
            clienteService.guardar(c);
            cargarClientes();
            clientes.stream().filter(x -> x.getId() == c.getId()).findFirst().ifPresent(x -> tablaClientes.getSelectionModel().select(x));
            mostrarInfo("El cliente se guardó correctamente.");
        } catch (IllegalArgumentException e) { mostrarError(e.getMessage()); }
        catch (RuntimeException e) { mostrarError("No se pudo guardar el cliente: " + e.getMessage()); }
    }

    @FXML
    private void eliminarDefinitivamente() {
        if (clienteSeleccionado == null) return;
        if (clienteSeleccionado.isActivo()) {
            mostrarError("Primero desactivá el cliente.");
            return;
        }
        if (!Dialogos.confirmarPeligro(
                "Eliminar cliente definitivamente",
                "¿Eliminar definitivamente a "
                        + clienteSeleccionado.getNombreCompleto()
                        + "?\n\nEsta acción solo continuará si el cliente no tiene "
                        + "reservas, pagos, inscripciones u otros datos relacionados. "
                        + "No se puede deshacer.")) return;
        try {
            clienteService.eliminarDefinitivamente(
                    clienteSeleccionado.getId());
            cargarClientes();
            nuevo();
            mostrarInfo("El cliente fue eliminado definitivamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    @FXML
    private void reactivar() {
        if (clienteSeleccionado == null) return;
        if (!Dialogos.confirmarAccion(
                "Reactivar cliente",
                "¿Reactivar a " + clienteSeleccionado.getNombreCompleto() + "?",
                "El cliente volverá a estar disponible para reservas e inscripciones.",
                "REACTIVAR")) return;
        try {
            clienteService.reactivar(clienteSeleccionado.getId());
            cargarClientes();
            nuevo();
            mostrarInfo("El cliente fue reactivado correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    @FXML
    private void desactivar() {
        if (clienteSeleccionado == null) return;
        if (!Dialogos.confirmarPeligro("Desactivar cliente",
                "¿Desactivar a " + clienteSeleccionado.getNombreCompleto()
                        + "?\n\nEl historial se conservará, pero el cliente "
                        + "dejará de aparecer en los listados activos.")) return;
        try {
            clienteService.eliminar(clienteSeleccionado.getId());
            cargarClientes();
            nuevo();
            mostrarInfo("El cliente fue desactivado.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML private void nuevaReservaParaCliente() { Navegacion.mostrarReservas(); }

    @FXML
    private void abrirReservaSeleccionada() {
        Reserva r = tablaHistorial.getSelectionModel().getSelectedItem();
        if (r == null) { mostrarError("Seleccioná una reserva del historial."); return; }
        abrirReserva(r);
    }

    private void abrirReserva(Reserva r) { Navegacion.mostrarReservaDesdeAgenda(r.getId()); }
    @FXML private void volver() { Navegacion.mostrarDashboard(Navegacion.getUsuarioActual()); }

    private void mostrarError(String m) {
        etiquetaMensaje.setText(m == null ? "Ocurrió un error." : m);
        etiquetaMensaje.getStyleClass().remove("mensaje-exito");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-error")) etiquetaMensaje.getStyleClass().add("mensaje-error");
    }
    private void mostrarInfo(String m) {
        etiquetaMensaje.setText(m); etiquetaMensaje.getStyleClass().remove("mensaje-error");
        if (!etiquetaMensaje.getStyleClass().contains("mensaje-exito")) etiquetaMensaje.getStyleClass().add("mensaje-exito");
    }
}
