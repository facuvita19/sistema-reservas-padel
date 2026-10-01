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
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
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

    @FXML private TextField campoBuscar;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> columnaNombre;
    @FXML private TableColumn<Cliente, String> columnaDocumento;
    @FXML private TableColumn<Cliente, String> columnaTelefono;
    @FXML private TableColumn<Cliente, String> columnaEmail;
    @FXML private TableColumn<Cliente, String> columnaEstado;
    @FXML private Label tituloFormulario;
    @FXML private Label etiquetaMensaje;
    @FXML private TextField campoNombre;
    @FXML private TextField campoApellido;
    @FXML private TextField campoDocumento;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoEmail;
    @FXML private Button botonGuardar;
    @FXML private Button botonDesactivar;
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
    }

    private void configurarTablaClientes() {
        columnaNombre.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNombreCompleto()));
        columnaDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));
        columnaTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        columnaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        columnaEstado.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().isActivo() ? "Activo" : "Inactivo"));
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
                setText(vacia || importe == null ? null : formatearMoneda(importe));
            }
        });
    }

    private String formatearHorario(Reserva r) {
        if (r == null || r.getHoraInicio() == null) return "";
        String inicio = r.getHoraInicio().format(FORMATO_HORA);
        return r.getHoraFin() == null ? inicio : inicio + " - " + r.getHoraFin().format(FORMATO_HORA);
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
    private void cargarClientes() {
        try {
            List<Cliente> resultado = clienteService.listar();
            clientes.setAll(resultado);
            mostrarInfo(resultado.size() + " cliente(s) cargado(s).");
        } catch (RuntimeException e) { mostrarError(e.getMessage()); }
    }

    @FXML
    private void nuevo() {
        clienteSeleccionado = null;
        tablaClientes.getSelectionModel().clearSelection();
        tituloFormulario.setText("Nuevo cliente");
        botonGuardar.setText("GUARDAR CLIENTE");
        mostrarBoton(botonDesactivar, false);
        mostrarBoton(botonNuevaReserva, false);
        campoNombre.clear(); campoApellido.clear(); campoDocumento.clear(); campoTelefono.clear(); campoEmail.clear();
        etiquetaMensaje.setText("");
        limpiarHistorial();
        campoNombre.requestFocus();
    }

    private void editar(Cliente cliente) {
        clienteSeleccionado = cliente;
        tituloFormulario.setText("Editar cliente");
        botonGuardar.setText("GUARDAR CAMBIOS");
        mostrarBoton(botonDesactivar, true);
        mostrarBoton(botonNuevaReserva, true);
        campoNombre.setText(cliente.getNombre());
        campoApellido.setText(cliente.getApellido());
        campoDocumento.setText(cliente.getDocumento());
        campoTelefono.setText(cliente.getTelefono());
        campoEmail.setText(cliente.getEmail());
        etiquetaMensaje.setText("");
        cargarHistorial(cliente.getId());
    }

    private void cargarHistorial(long clienteId) {
        try {
            List<Reserva> lista = reservaService.listarPorCliente(clienteId);
            acreditadoPorReserva.clear(); saldoPorReserva.clear();
            BigDecimal totalAcreditado = BigDecimal.ZERO;
            BigDecimal saldoPendiente = BigDecimal.ZERO;
            for (Reserva r : lista) {
                BigDecimal acreditado = pagoService.totalAcreditado(r.getId());
                BigDecimal saldo = pagoService.calcularSaldo(r.getId());
                acreditadoPorReserva.put(r.getId(), acreditado);
                saldoPorReserva.put(r.getId(), saldo);
                totalAcreditado = totalAcreditado.add(acreditado);
                if (r.getEstado() != EstadoReserva.CANCELADA && r.getEstado() != EstadoReserva.EXPIRADA)
                    saldoPendiente = saldoPendiente.add(saldo);
            }
            historial.setAll(lista);
            actualizarResumen(lista, totalAcreditado, saldoPendiente);
        } catch (RuntimeException e) {
            limpiarHistorial();
            mostrarError("No se pudo cargar el historial: " + e.getMessage());
        }
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
            c.setTelefono(campoTelefono.getText()); c.setEmail(campoEmail.getText()); c.setActivo(true);
            clienteService.guardar(c);
            cargarClientes();
            clientes.stream().filter(x -> x.getId() == c.getId()).findFirst().ifPresent(x -> tablaClientes.getSelectionModel().select(x));
            mostrarInfo("El cliente se guardó correctamente.");
        } catch (IllegalArgumentException e) { mostrarError(e.getMessage()); }
        catch (RuntimeException e) { mostrarError("No se pudo guardar el cliente: " + e.getMessage()); }
    }

    @FXML
    private void desactivar() {
        if (clienteSeleccionado == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Desactivar cliente");
        a.setHeaderText("¿Desactivar a " + clienteSeleccionado.getNombreCompleto() + "?");
        a.setContentText("El historial se conservará, pero el cliente dejará de aparecer en los listados activos.");
        a.showAndWait().ifPresent(r -> {
            if (r == javafx.scene.control.ButtonType.OK) try {
                clienteService.eliminar(clienteSeleccionado.getId()); cargarClientes(); nuevo(); mostrarInfo("El cliente fue desactivado.");
            } catch (RuntimeException e) { mostrarError(e.getMessage()); }
        });
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
