package vista.controlador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import negocio.RolUsuario;
import negocio.Usuario;
import servicio.UsuarioService;
import vista.Navegacion;
import vista.Dialogos;

public class UsuariosController {
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioService usuarioService = new UsuarioService();
    private final ObservableList<Usuario> usuarios =
            FXCollections.observableArrayList();
    private FilteredList<Usuario> usuariosFiltrados;
    private Usuario usuarioSeleccionado;

    @FXML private TextField campoBuscar;
    @FXML private CheckBox checkMostrarInactivos;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> columnaNombre;
    @FXML private TableColumn<Usuario, RolUsuario> columnaRol;
    @FXML private TableColumn<Usuario, Boolean> columnaActivo;
    @FXML private TableColumn<Usuario, LocalDateTime> columnaFecha;
    @FXML private Label tituloFormulario;
    @FXML private Label subtituloFormulario;
    @FXML private Label insigniaEstadoUsuario;
    @FXML private Label etiquetaPasswordEdicion;
    @FXML private TextField campoNombreUsuario;
    @FXML private ComboBox<RolUsuario> comboRol;
    @FXML private VBox contenedorPasswordAlta;
    @FXML private PasswordField campoPassword;
    @FXML private PasswordField campoConfirmarPassword;
    @FXML private Label etiquetaAyudaPassword;
    @FXML private Label etiquetaMensaje;
    @FXML private Label etiquetaSesion;
    @FXML private Label etiquetaPermisos;
    @FXML private Button botonGuardar;
    @FXML private Button botonRestablecer;
    @FXML private Button botonEstado;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFiltros();
        configurarFormulario();
        mostrarSesion();
        cargarUsuarios();
        nuevoUsuario();
    }

    private void configurarTabla() {
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        columnaRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        columnaActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        columnaActivo.setCellFactory(columna -> new TableCell<>() {
            private final Label insignia = new Label();
            { insignia.getStyleClass().add("user-status-badge"); }
            @Override protected void updateItem(Boolean activo, boolean vacia) {
                super.updateItem(activo, vacia);
                if (vacia || activo == null) { setGraphic(null); return; }
                insignia.setText(activo ? "ACTIVO" : "INACTIVO");
                insignia.getStyleClass().removeAll(
                        "user-status-active", "user-status-inactive");
                insignia.getStyleClass().add(activo
                        ? "user-status-active" : "user-status-inactive");
                setGraphic(insignia);
            }
        });
        columnaRol.setCellFactory(columna -> new TableCell<>() {
            private final Label insignia = new Label();
            { insignia.getStyleClass().add("user-role-badge"); }
            @Override protected void updateItem(RolUsuario rol, boolean vacia) {
                super.updateItem(rol, vacia);
                if (vacia || rol == null) { setGraphic(null); return; }
                insignia.setText(rol.toString().toUpperCase(Locale.ROOT));
                insignia.getStyleClass().removeAll(
                        "user-role-admin", "user-role-operator");
                insignia.getStyleClass().add(rol == RolUsuario.ADMINISTRADOR
                        ? "user-role-admin" : "user-role-operator");
                setGraphic(insignia);
            }
        });
        columnaFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : fecha.format(FORMATO_FECHA));
            }
        });
        tablaUsuarios.setRowFactory(tabla -> {
            TableRow<Usuario> fila = new TableRow<>();
            fila.itemProperty().addListener((obs, anterior, actual) ->
                    actualizarClaseFila(fila, actual));
            fila.selectedProperty().addListener((obs, anterior, actual) ->
                    actualizarClaseFila(fila, fila.getItem()));
            return fila;
        });
        tablaUsuarios.setOnMouseClicked(evento -> {
            if (clicEnFondoTabla(evento.getTarget())) nuevoUsuario();
        });
        tablaUsuarios.setOnKeyPressed(evento -> {
            if (evento.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                nuevoUsuario();
                evento.consume();
            }
        });
        tablaUsuarios.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) mostrarDetalle(actual);
                });
    }

    private void actualizarClaseFila(TableRow<Usuario> fila, Usuario usuario) {
        fila.getStyleClass().removeAll(
                "user-row-inactive", "user-row-selected");
        if (usuario == null || fila.isEmpty()) return;
        if (!usuario.isActivo()) fila.getStyleClass().add("user-row-inactive");
        if (fila.isSelected()) fila.getStyleClass().add("user-row-selected");
    }

    private boolean clicEnFondoTabla(Object objetivo) {
        if (!(objetivo instanceof Node nodo)) return false;
        Node actual = nodo;
        while (actual != null && actual != tablaUsuarios) {
            if (actual instanceof TableRow<?> fila) return fila.isEmpty();
            actual = actual.getParent();
        }
        return actual == tablaUsuarios;
    }

    private void configurarFiltros() {
        usuariosFiltrados = new FilteredList<>(usuarios, usuario -> true);
        tablaUsuarios.setItems(usuariosFiltrados);
        campoBuscar.textProperty().addListener((obs, ant, act) -> aplicarFiltros());
        checkMostrarInactivos.selectedProperty().addListener((obs, ant, act) -> aplicarFiltros());
    }

    private void configurarFormulario() {
        comboRol.setItems(FXCollections.observableArrayList(
                RolUsuario.ADMINISTRADOR, RolUsuario.OPERADOR));
        comboRol.setValue(RolUsuario.OPERADOR);
        comboRol.valueProperty().addListener((obs, ant, actual) -> actualizarPermisos(actual));
    }

    private void mostrarSesion() {
        Usuario actual = Navegacion.getUsuarioActual();
        etiquetaSesion.setText(actual == null ? "Sesión no disponible"
                : "Sesión actual: " + actual.getNombreUsuario() + " · " + actual.getRol());
    }

    private void actualizarPermisos(RolUsuario rol) {
        if (rol == RolUsuario.ADMINISTRADOR) {
            etiquetaPermisos.setText("Acceso completo: usuarios, configuración, canchas, estadísticas y operación diaria.");
        } else {
            etiquetaPermisos.setText("Acceso operativo: agenda, reservas, clientes, pagos, caja y bloqueos.");
        }
    }

    @FXML
    private void cargarUsuarios() {
        try {
            usuarios.setAll(usuarioService.listarPersonal());
            aplicarFiltros();
            mostrarInfo(usuarios.size() + " usuario(s) cargado(s).");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void aplicarFiltros() {
        if (usuariosFiltrados == null) return;
        String texto = campoBuscar.getText() == null ? ""
                : campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        boolean mostrarInactivos = checkMostrarInactivos.isSelected();
        usuariosFiltrados.setPredicate(usuario ->
                (mostrarInactivos || usuario.isActivo())
                && (texto.isBlank()
                || contiene(usuario.getNombreUsuario(), texto)
                || contiene(usuario.getRol().toString(), texto)
                || contiene(usuario.isActivo() ? "activo" : "inactivo", texto)));
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    @FXML private void limpiarFiltros() { campoBuscar.clear(); checkMostrarInactivos.setSelected(false); }

    @FXML
    private void nuevoUsuario() {
        usuarioSeleccionado = null;
        tablaUsuarios.getSelectionModel().clearSelection();
        tituloFormulario.setText("Nuevo usuario del personal");
        subtituloFormulario.setText(
                "Creá una cuenta individual para el personal.");
        insigniaEstadoUsuario.setVisible(false);
        insigniaEstadoUsuario.setManaged(false);
        campoNombreUsuario.clear();
        comboRol.setValue(RolUsuario.OPERADOR);
        campoPassword.clear();
        campoConfirmarPassword.clear();
        mostrar(contenedorPasswordAlta, true);
        mostrar(etiquetaPasswordEdicion, false);
        campoPassword.setDisable(false);
        campoConfirmarPassword.setDisable(false);
        campoNombreUsuario.setDisable(false);
        comboRol.setDisable(false);
        etiquetaAyudaPassword.setText("La contraseña es obligatoria y debe tener al menos 8 caracteres.");
        botonGuardar.setText("CREAR USUARIO");
        configurarBoton(botonRestablecer, false);
        configurarBoton(botonEstado, false);
        limpiarMensaje();
    }

    private void mostrarDetalle(Usuario usuario) {
        usuarioSeleccionado = usuario;
        boolean protegida = "admin".equalsIgnoreCase(
                usuario.getNombreUsuario());
        tituloFormulario.setText(protegida
                ? "Cuenta administrativa" : "Editar usuario");
        subtituloFormulario.setText(protegida
                ? "admin · Administrador\nCuenta principal del sistema"
                : usuario.getNombreUsuario() + " · " + usuario.getRol()
                        + (usuario.getFechaCreacion() == null ? ""
                                : "\nCreado el " + usuario.getFechaCreacion()
                                        .format(FORMATO_FECHA)));
        insigniaEstadoUsuario.setText(protegida
                ? "PROTEGIDA" : usuario.isActivo() ? "ACTIVO" : "INACTIVO");
        insigniaEstadoUsuario.getStyleClass().removeAll(
                "user-detail-protected", "user-detail-active",
                "user-detail-inactive");
        insigniaEstadoUsuario.getStyleClass().add(protegida
                ? "user-detail-protected"
                : usuario.isActivo() ? "user-detail-active"
                        : "user-detail-inactive");
        insigniaEstadoUsuario.setVisible(true);
        insigniaEstadoUsuario.setManaged(true);
        campoNombreUsuario.setText(usuario.getNombreUsuario());
        comboRol.setValue(usuario.getRol());
        campoPassword.clear();
        campoConfirmarPassword.clear();
        mostrar(contenedorPasswordAlta, false);
        mostrar(etiquetaPasswordEdicion, true);
        campoPassword.setDisable(true);
        campoConfirmarPassword.setDisable(true);
        campoNombreUsuario.setDisable(protegida);
        comboRol.setDisable(protegida);
        etiquetaAyudaPassword.setText("Para cambiar la contraseña usá Restablecer contraseña.");
        botonGuardar.setText("GUARDAR CAMBIOS");
        configurarBoton(botonRestablecer, true);
        configurarBoton(botonEstado, true);
        botonEstado.setText(usuario.isActivo() ? "DESACTIVAR USUARIO" : "ACTIVAR USUARIO");
        botonEstado.getStyleClass().removeAll("danger-button", "activate-button");
        botonEstado.getStyleClass().add(usuario.isActivo() ? "danger-button" : "activate-button");

        Usuario actual = Navegacion.getUsuarioActual();
        boolean propiaCuenta = actual != null && actual.getId() == usuario.getId();
        botonEstado.setDisable(protegida
                || propiaCuenta && usuario.isActivo());
        botonEstado.setTooltip(protegida
                ? new Tooltip("La cuenta administrativa principal está protegida.")
                : propiaCuenta && usuario.isActivo()
                        ? new Tooltip("No podés desactivar tu propia cuenta.")
                        : null);
        limpiarMensaje();
    }

    @FXML
    private void guardar() {
        try {
            if (usuarioSeleccionado == null) crearUsuario(); else actualizarUsuario();
        } catch (IllegalArgumentException exception) {
            mostrarError(exception.getMessage());
        } catch (RuntimeException exception) {
            mostrarError("No se pudo guardar el usuario: " + exception.getMessage());
        }
    }

    private void crearUsuario() {
        String password = campoPassword.getText();
        if (password == null || !password.equals(campoConfirmarPassword.getText())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
        usuarioService.registrar(campoNombreUsuario.getText(), password, comboRol.getValue(), null);
        cargarUsuarios();
        nuevoUsuario();
        mostrarInfo("El usuario se creó correctamente.");
    }

    private void actualizarUsuario() {
        Usuario modificado = new Usuario();
        modificado.setId(usuarioSeleccionado.getId());
        modificado.setNombreUsuario(campoNombreUsuario.getText());
        modificado.setRol(comboRol.getValue());
        modificado.setClienteId(null);
        modificado.setActivo(usuarioSeleccionado.isActivo());
        modificado.setFechaCreacion(usuarioSeleccionado.getFechaCreacion());
        usuarioService.actualizarDatos(modificado);
        cargarUsuarios();
        seleccionarUsuario(modificado.getId());
        mostrarInfo("Los datos del usuario se actualizaron.");
    }

    @FXML
    private void restablecerPassword() {
        if (usuarioSeleccionado == null) { mostrarError("Seleccioná un usuario."); return; }
        Dialog<String> dialogo = new Dialog<>();
        dialogo.setTitle("Restablecer contraseña");
        dialogo.setHeaderText("Nueva contraseña para " + usuarioSeleccionado.getNombreUsuario());
        ButtonType guardar = new ButtonType("RESTABLECER", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        PasswordField nueva = new PasswordField();
        PasswordField repetir = new PasswordField();
        nueva.setPromptText("Mínimo 8 caracteres");
        repetir.setPromptText("Repetir contraseña");
        GridPane panel = new GridPane();
        panel.setHgap(10); panel.setVgap(10); panel.setPadding(new Insets(10));
        panel.add(new Label("Contraseña nueva:"), 0, 0); panel.add(nueva, 1, 0);
        panel.add(new Label("Confirmación:"), 0, 1); panel.add(repetir, 1, 1);
                nueva.getStyleClass().add("dialog-field");
        repetir.getStyleClass().add("dialog-field");
        panel.getChildren().stream().filter(n -> n instanceof Label)
                .forEach(n -> n.getStyleClass().add("dialog-field-label"));
dialogo.getDialogPane().setContent(panel);
        Dialogos.preparar(dialogo, "dialog-custom-action");
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());
        dialogo.getDialogPane().lookupButton(guardar)
                .getStyleClass().add("dialog-action-primary");
        dialogo.setResultConverter(boton -> boton == guardar ? nueva.getText() : null);
        dialogo.showAndWait().ifPresent(password -> {
            try {
                if (!password.equals(repetir.getText())) throw new IllegalArgumentException("Las contraseñas no coinciden.");
                usuarioService.restablecerPassword(usuarioSeleccionado.getId(), password);
                mostrarInfo("La contraseña se restableció correctamente.");
            } catch (RuntimeException exception) { mostrarError(exception.getMessage()); }
        });
    }

    @FXML
    private void cambiarEstado() {
        if (usuarioSeleccionado == null) { mostrarError("Seleccioná un usuario."); return; }
        boolean activar = !usuarioSeleccionado.isActivo();
        String titulo = activar ? "Activar usuario" : "Desactivar usuario";
        String mensaje = (activar ? "¿Activar la cuenta?" : "¿Desactivar la cuenta?")
                + "\n\n" + usuarioSeleccionado.getNombreUsuario();
        boolean confirmado = activar
                ? Dialogos.confirmar(titulo, mensaje)
                : Dialogos.confirmarPeligro(titulo, mensaje);
        if (!confirmado) return;
        try {
            long id = usuarioSeleccionado.getId();
            if (activar) usuarioService.activar(id);
            else {
                Usuario actual = Navegacion.getUsuarioActual();
                if (actual == null) throw new IllegalArgumentException(
                        "La sesión administrativa finalizó.");
                usuarioService.desactivar(id, actual.getId());
            }
            cargarUsuarios();
            seleccionarUsuario(id);
            mostrarInfo("La cuenta se " + (activar ? "activó" : "desactivó")
                    + " correctamente.");
        } catch (RuntimeException exception) {
            mostrarError(exception.getMessage());
        }
    }

    private void seleccionarUsuario(long id) {
        usuarios.stream().filter(u -> u.getId() == id).findFirst().ifPresent(u -> {
            tablaUsuarios.getSelectionModel().select(u); tablaUsuarios.scrollTo(u); mostrarDetalle(u);
        });
    }
    private void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }
    private void configurarBoton(Button boton, boolean visible) { boton.setVisible(visible); boton.setManaged(visible); }
    @FXML private void volver() { Navegacion.mostrarDashboard(Navegacion.getUsuarioActual()); }
    private void mostrarError(String m) { etiquetaMensaje.setText(m == null ? "Ocurrió un error." : m); etiquetaMensaje.getStyleClass().remove("mensaje-exito"); if(!etiquetaMensaje.getStyleClass().contains("mensaje-error"))etiquetaMensaje.getStyleClass().add("mensaje-error"); }
    private void mostrarInfo(String m) { etiquetaMensaje.setText(m); etiquetaMensaje.getStyleClass().remove("mensaje-error"); if(!etiquetaMensaje.getStyleClass().contains("mensaje-exito"))etiquetaMensaje.getStyleClass().add("mensaje-exito"); }
    private void limpiarMensaje() { etiquetaMensaje.setText(""); etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito"); }
}
