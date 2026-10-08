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
import javafx.scene.control.ScrollPane;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
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
    private String estadoFormularioInicial = "";
    private boolean actualizandoFormulario;

    @FXML private TextField campoBuscar;
    @FXML private ScrollPane scrollDetalleUsuario;
    @FXML private Label etiquetaResultados;
    @FXML private Label etiquetaAyudaListado;
    @FXML private Label etiquetaModoUsuario;
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
    @FXML private HBox contenedorPasswordEdicion;
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
    @FXML private SVGPath iconoEstadoUsuario;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarFiltros();
        configurarFormulario();
        configurarCambiosFormulario();
        mostrarSesion();
        cargarUsuarios();
        nuevoUsuario();
    }

    private void configurarTabla() {
        tablaUsuarios.setFixedCellSize(61);
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
            fila.itemProperty().addListener((obs, anterior, actual) -> actualizarClaseFila(fila, actual));
            fila.selectedProperty().addListener((obs, anterior, actual) -> actualizarClaseFila(fila, fila.getItem()));
            fila.emptyProperty().addListener((obs, anterior, actual) -> actualizarClaseFila(fila, fila.getItem()));
            fila.setOnMousePressed(evento -> seleccionarFilaUsuario(fila));
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

    private void seleccionarFilaUsuario(TableRow<Usuario> fila) {
        if (fila == null || fila.isEmpty() || fila.getItem() == null) return;
        tablaUsuarios.getSelectionModel().select(fila.getItem());
        tablaUsuarios.requestFocus();
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
        etiquetaSesion.setText(actual == null ? "No disponible"
                : actual.getNombreUsuario() + " · " + actual.getRol());
    }

    private void actualizarPermisos(RolUsuario rol) {
        if (rol == RolUsuario.ADMINISTRADOR) {
            etiquetaPermisos.setText("Acceso completo: usuarios, configuración, canchas, estadísticas y operación diaria.");
        } else {
            etiquetaPermisos.setText("Acceso operativo: agenda, reservas, clientes, pagos, caja y bloqueos. Sin Estadísticas ni resúmenes financieros mensuales.");
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
        actualizarResultadosVisibles();
    }

    private void actualizarResultadosVisibles() {
        if (usuariosFiltrados == null || etiquetaResultados == null) return;
        int visibles = usuariosFiltrados.size();
        String texto = visibles + (visibles == 1 ? " usuario visible" : " usuarios visibles");
        if (checkMostrarInactivos.isSelected()) texto += " · Incluye inactivos";
        etiquetaResultados.setText(texto);
    }

    private void actualizarAyudaListado(Usuario usuario) {
        if (etiquetaAyudaListado == null) return;
        etiquetaAyudaListado.setText(usuario == null ? "Seleccioná un usuario para editarlo" : "Seleccionado: " + usuario.getNombreUsuario() + " · " + usuario.getRol());
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    @FXML private void limpiarFiltros() { campoBuscar.clear(); checkMostrarInactivos.setSelected(false); limpiarMensajeContextual(); nuevoUsuario(); campoBuscar.requestFocus(); }

    @FXML
    private void nuevoUsuario() {
        limpiarMensajeContextual();
        actualizandoFormulario = true;
        usuarioSeleccionado = null;
        etiquetaModoUsuario.setText("NUEVO USUARIO");
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
        mostrar(contenedorPasswordEdicion, false);
        campoPassword.setDisable(false);
        campoConfirmarPassword.setDisable(false);
        campoNombreUsuario.setDisable(false);
        comboRol.setDisable(false);
        etiquetaAyudaPassword.setText("La contraseña es obligatoria y debe tener al menos 8 caracteres.");
        botonGuardar.setText("CREAR USUARIO");
        configurarBoton(botonRestablecer, false);
        configurarBoton(botonEstado, false);
        estadoFormularioInicial = estadoFormularioActual();
        actualizandoFormulario = false;
        actualizarEstadoGuardar();
        actualizarAyudaListado(null);
        volverArribaDetalle();
        actualizarAyudaPassword();
        limpiarMensajeContextual();
    }

    private void mostrarDetalle(Usuario usuario) {
        limpiarMensajeContextual();
        actualizandoFormulario = true;
        usuarioSeleccionado = usuario;
        boolean protegida = "admin".equalsIgnoreCase(
                usuario.getNombreUsuario());
        etiquetaModoUsuario.setText(protegida ? "CUENTA PROTEGIDA" : "USUARIO SELECCIONADO");
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
        mostrar(contenedorPasswordEdicion, true);
        campoPassword.setDisable(true);
        campoConfirmarPassword.setDisable(true);
        campoNombreUsuario.setDisable(protegida);
        comboRol.setDisable(protegida);
        etiquetaAyudaPassword.setText("Para cambiar la contraseña usá Restablecer contraseña.");
        botonGuardar.setText("GUARDAR CAMBIOS");
        configurarBoton(botonRestablecer, true);
        configurarBoton(botonEstado, !protegida);
        configurarBotonEstado(usuario.isActivo());

        Usuario actual = Navegacion.getUsuarioActual();
        boolean propiaCuenta = actual != null && actual.getId() == usuario.getId();
        botonEstado.setDisable(protegida
                || propiaCuenta && usuario.isActivo());
        botonEstado.setTooltip(propiaCuenta && usuario.isActivo()
                ? new Tooltip("No podés desactivar tu propia cuenta.")
                : null);
        estadoFormularioInicial = estadoFormularioActual();
        actualizandoFormulario = false;
        actualizarEstadoGuardar();
        actualizarAyudaListado(usuario);
        volverArribaDetalle();
        limpiarMensajeContextual();
    }

    private void configurarBotonEstado(boolean activo) {
        botonEstado.setText(activo ? "DESACTIVAR USUARIO" : "REACTIVAR USUARIO");
        botonEstado.getStyleClass().removeAll("danger-button", "activate-button", "user-state-button-v1", "user-reactivate-button-v1");
        botonEstado.getStyleClass().add(activo ? "user-state-button-v1" : "user-reactivate-button-v1");
        if (iconoEstadoUsuario != null) iconoEstadoUsuario.setContent(activo
                ? "M12 3 C7 3 3 7 3 12 C3 17 7 21 12 21 C17 21 21 17 21 12 C21 7 17 3 12 3 M7 12 L17 12"
                : "M20 7 L20 2 L15 2 M20 2 C15 -1 7 1 4 7 M4 17 L4 22 L9 22 M4 22 C9 25 17 23 20 17");
    }

    private void volverArribaDetalle() {
        if (scrollDetalleUsuario == null) return;
        javafx.application.Platform.runLater(() -> { scrollDetalleUsuario.setVvalue(0); scrollDetalleUsuario.setHvalue(0); });
    }

    private void configurarCambiosFormulario() {
        javafx.beans.InvalidationListener listener = obs -> { actualizarEstadoGuardar(); limpiarMensajeContextual(); };
        campoNombreUsuario.textProperty().addListener(listener);
        comboRol.valueProperty().addListener(listener);
        campoPassword.textProperty().addListener(listener);
        campoConfirmarPassword.textProperty().addListener(listener);
        campoPassword.textProperty().addListener((obs, ant, act) -> actualizarAyudaPassword());
        campoConfirmarPassword.textProperty().addListener((obs, ant, act) -> actualizarAyudaPassword());
    }

    private void actualizarEstadoGuardar() {
        if (actualizandoFormulario || botonGuardar == null) return;
        boolean nombreValido = !texto(campoNombreUsuario).isBlank() && comboRol.getValue() != null;
        boolean habilitado;
        if (usuarioSeleccionado == null) {
            String password = campoPassword.getText() == null ? "" : campoPassword.getText();
            String confirmar = campoConfirmarPassword.getText() == null ? "" : campoConfirmarPassword.getText();
            habilitado = nombreValido && password.length() >= 8 && password.equals(confirmar);
        } else {
            boolean protegida = "admin".equalsIgnoreCase(usuarioSeleccionado.getNombreUsuario());
            habilitado = !protegida && usuarioSeleccionado.isActivo() && nombreValido && !estadoFormularioActual().equals(estadoFormularioInicial);
        }
        botonGuardar.setDisable(!habilitado);
    }

    private void actualizarAyudaPassword() {
        if (usuarioSeleccionado != null || etiquetaAyudaPassword == null) return;
        String password = campoPassword.getText() == null ? "" : campoPassword.getText();
        String confirmar = campoConfirmarPassword.getText() == null ? "" : campoConfirmarPassword.getText();
        etiquetaAyudaPassword.getStyleClass().removeAll("password-help-warning-v1", "password-help-error-v1", "password-help-ok-v1");
        if (password.isBlank() && confirmar.isBlank()) {
            etiquetaAyudaPassword.setText("La contraseña es obligatoria y debe tener al menos 8 caracteres.");
        } else if (password.length() < 8) {
            etiquetaAyudaPassword.setText("La contraseña todavía no alcanza los 8 caracteres.");
            etiquetaAyudaPassword.getStyleClass().add("password-help-warning-v1");
        } else if (!password.equals(confirmar)) {
            etiquetaAyudaPassword.setText("Las contraseñas no coinciden.");
            etiquetaAyudaPassword.getStyleClass().add("password-help-error-v1");
        } else {
            etiquetaAyudaPassword.setText("Las contraseñas coinciden y cumplen la longitud mínima.");
            etiquetaAyudaPassword.getStyleClass().add("password-help-ok-v1");
        }
    }

    private String estadoFormularioActual() {
        return String.join("\u001F", texto(campoNombreUsuario), String.valueOf(comboRol.getValue()));
    }

    private String texto(TextField campo) { return campo == null || campo.getText() == null ? "" : campo.getText().trim(); }

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
        if (usuarioSeleccionado == null) {
            mostrarError("Seleccioná un usuario.");
            return;
        }

        Dialog<String> dialogo = new Dialog<>();
        dialogo.setTitle("Restablecer contraseña");
        dialogo.setHeaderText("Nueva contraseña para "
                + usuarioSeleccionado.getNombreUsuario());

        ButtonType guardar = new ButtonType(
                "RESTABLECER", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(
                guardar, ButtonType.CANCEL);

        PasswordField nueva = new PasswordField();
        PasswordField repetir = new PasswordField();
        nueva.setPromptText("Mínimo 8 caracteres");
        repetir.setPromptText("Repetir contraseña");
        nueva.getStyleClass().add("dialog-field");
        repetir.getStyleClass().add("dialog-field");

        TextField nuevaVisible = crearCampoPasswordVisible(
                nueva, "Mínimo 8 caracteres");
        TextField repetirVisible = crearCampoPasswordVisible(
                repetir, "Repetir contraseña");

        StackPane campoNueva = crearCampoPasswordConOjo(
                nueva, nuevaVisible, "Mostrar contraseña nueva");
        StackPane campoRepetir = crearCampoPasswordConOjo(
                repetir, repetirVisible, "Mostrar confirmación");

        Label ayudaPassword = new Label(
                "Ingresá la contraseña nueva y repetila para confirmarla.");
        ayudaPassword.setWrapText(true);
        ayudaPassword.setMinHeight(18);
        ayudaPassword.setPrefHeight(18);
        ayudaPassword.setMaxHeight(18);
        ayudaPassword.getStyleClass().add("dialog-password-help-v3");

        GridPane panel = new GridPane();
        panel.setHgap(10);
        panel.setVgap(10);
        panel.setPadding(new Insets(10));
        panel.add(new Label("Contraseña nueva:"), 0, 0);
        panel.add(campoNueva, 1, 0);
        panel.add(new Label("Confirmación:"), 0, 1);
        panel.add(campoRepetir, 1, 1);
        panel.add(ayudaPassword, 1, 2);
        GridPane.setHgrow(campoNueva, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(campoRepetir, javafx.scene.layout.Priority.ALWAYS);
        panel.getChildren().stream()
                .filter(nodo -> nodo instanceof Label
                        && nodo != ayudaPassword)
                .forEach(nodo -> nodo.getStyleClass()
                        .add("dialog-field-label"));

        dialogo.getDialogPane().setContent(panel);
        Dialogos.preparar(dialogo, "dialog-custom-action");
        vista.TemaDinamico.aplicar(dialogo.getDialogPane(),
                Navegacion.getConfiguracionActual());

        Node botonGuardarPassword = dialogo.getDialogPane()
                .lookupButton(guardar);
        botonGuardarPassword.getStyleClass()
                .add("dialog-action-primary");
        botonGuardarPassword.setDisable(false);

        Runnable restaurarAyudaNeutral = () -> {
            ayudaPassword.setText(
                    "Ingresá la contraseña nueva y repetila para confirmarla.");
            ayudaPassword.getStyleClass().removeAll(
                    "dialog-password-error-v3");
        };
        nueva.textProperty().addListener((obs, anterior, actual) ->
                restaurarAyudaNeutral.run());
        repetir.textProperty().addListener((obs, anterior, actual) ->
                restaurarAyudaNeutral.run());

        botonGuardarPassword.addEventFilter(
                javafx.event.ActionEvent.ACTION, evento -> {
                    String password = nueva.getText() == null
                            ? "" : nueva.getText();
                    String confirmacion = repetir.getText() == null
                            ? "" : repetir.getText();
                    String error = null;
                    if (password.length() < 8) {
                        error = "La contraseña debe tener al menos 8 caracteres.";
                    } else if (confirmacion.isBlank()) {
                        error = "Repetí la contraseña para confirmarla.";
                    } else if (!password.equals(confirmacion)) {
                        error = "Las contraseñas no coinciden.";
                    }
                    if (error != null) {
                        ayudaPassword.setText(error);
                        if (!ayudaPassword.getStyleClass().contains(
                                "dialog-password-error-v3")) {
                            ayudaPassword.getStyleClass().add(
                                    "dialog-password-error-v3");
                        }
                        evento.consume();
                    }
                });

        dialogo.setOnShown(evento ->
                javafx.application.Platform.runLater(() -> {
                    nueva.requestFocus();
                    nueva.positionCaret(nueva.getText() == null
                            ? 0 : nueva.getText().length());
                }));
        dialogo.setResultConverter(boton ->
                boton == guardar ? nueva.getText() : null);
        dialogo.showAndWait().ifPresent(password -> {
            try {
                usuarioService.restablecerPassword(
                        usuarioSeleccionado.getId(), password);
                mostrarInfo(
                        "La contraseña se restableció correctamente.");
            } catch (RuntimeException exception) {
                mostrarError(exception.getMessage());
            }
        });
    }

    private TextField crearCampoPasswordVisible(
            PasswordField oculto, String prompt) {
        TextField visible = new TextField();
        visible.setPromptText(prompt);
        visible.getStyleClass().add("dialog-field");
        visible.textProperty().bindBidirectional(
                oculto.textProperty());
        visible.setVisible(false);
        visible.setManaged(false);
        return visible;
    }

    private StackPane crearCampoPasswordConOjo(
            PasswordField oculto,
            TextField visible,
            String textoAccesible) {
        javafx.scene.paint.Color trazoOjoPassword =
                javafx.scene.paint.Color.web("#aebbc0");
        javafx.scene.paint.Color trazoOjoActivo =
                javafx.scene.paint.Color.web("#82d1aa");

        SVGPath contornoOjo = new SVGPath();
        contornoOjo.setContent(
                "M3 12 C6 7 9 5 12 5 C15 5 18 7 21 12 "
                + "C18 17 15 19 12 19 C9 19 6 17 3 12 Z");
        contornoOjo.setFill(javafx.scene.paint.Color.TRANSPARENT);
        contornoOjo.setStroke(trazoOjoPassword);
        contornoOjo.setStrokeWidth(1.55);
        contornoOjo.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        contornoOjo.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        contornoOjo.setMouseTransparent(true);

        javafx.scene.shape.Circle pupilaOjo =
                new javafx.scene.shape.Circle(12, 12, 2.7);
        pupilaOjo.setFill(javafx.scene.paint.Color.TRANSPARENT);
        pupilaOjo.setStroke(trazoOjoPassword);
        pupilaOjo.setStrokeWidth(1.55);
        pupilaOjo.setMouseTransparent(true);

        javafx.scene.shape.Line tachadoOjo =
                new javafx.scene.shape.Line(5, 5, 19, 19);
        tachadoOjo.setStroke(trazoOjoActivo);
        tachadoOjo.setStrokeWidth(1.8);
        tachadoOjo.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        tachadoOjo.setVisible(false);
        tachadoOjo.setMouseTransparent(true);

        javafx.scene.Group graficoOjo = new javafx.scene.Group(
                contornoOjo, pupilaOjo, tachadoOjo);
        graficoOjo.setScaleX(0.76);
        graficoOjo.setScaleY(0.76);
        graficoOjo.setMouseTransparent(true);

        StackPane ojo = new StackPane(graficoOjo);
        ojo.setMinSize(30, 30);
        ojo.setPrefSize(30, 30);
        ojo.setMaxSize(30, 30);
        ojo.setFocusTraversable(true);
        ojo.setAccessibleRole(javafx.scene.AccessibleRole.BUTTON);
        ojo.setAccessibleText(textoAccesible);
        ojo.setCursor(javafx.scene.Cursor.HAND);
        ojo.getStyleClass().add("dialog-password-eye-plain-v8");

        final Tooltip tooltipOjo = new Tooltip(textoAccesible);
        Tooltip.install(ojo, tooltipOjo);

        final boolean[] mostrando = { false };
        Runnable alternarVisibilidad = () -> {
            mostrando[0] = !mostrando[0];
            boolean mostrar = mostrando[0];
            tachadoOjo.setVisible(mostrar);
            contornoOjo.setStroke(mostrar
                    ? trazoOjoActivo : trazoOjoPassword);
            pupilaOjo.setVisible(!mostrar);
            ojo.getStyleClass().remove("dialog-password-eye-visible-v8");
            if (mostrar) ojo.getStyleClass().add(
                    "dialog-password-eye-visible-v8");
            visible.setVisible(mostrar);
            visible.setManaged(mostrar);
            oculto.setVisible(!mostrar);
            oculto.setManaged(!mostrar);
            String ayuda = mostrar
                    ? "Ocultar contraseña" : textoAccesible;
            ojo.setAccessibleText(ayuda);
            tooltipOjo.setText(ayuda);
            TextField destino = mostrar ? visible : oculto;
            destino.requestFocus();
            destino.positionCaret(destino.getText() == null
                    ? 0 : destino.getText().length());
        };
        ojo.setOnMouseClicked(evento -> {
            alternarVisibilidad.run();
            evento.consume();
        });
        ojo.setOnKeyPressed(evento -> {
            if (evento.getCode() == javafx.scene.input.KeyCode.SPACE
                    || evento.getCode() == javafx.scene.input.KeyCode.ENTER) {
                alternarVisibilidad.run();
                evento.consume();
            }
        });

        StackPane contenedor = new StackPane(
                oculto, visible, ojo);
        contenedor.setMinWidth(300);
        contenedor.setPrefWidth(360);
        contenedor.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(ojo,
                javafx.geometry.Pos.CENTER_RIGHT);
        StackPane.setMargin(ojo,
                new Insets(0, 7, 0, 0));
        return contenedor;
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
            tablaUsuarios.getSelectionModel().select(u); javafx.application.Platform.runLater(() -> tablaUsuarios.scrollTo(u)); mostrarDetalle(u);
        });
    }
    private void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }
    private void configurarBoton(Button boton, boolean visible) { boton.setVisible(visible); boton.setManaged(visible); }
    @FXML private void volver() { Navegacion.mostrarDashboard(Navegacion.getUsuarioActual()); }
    private void limpiarMensajeContextual() {
        if (etiquetaMensaje == null) return;
        etiquetaMensaje.setText(""); etiquetaMensaje.setVisible(false); etiquetaMensaje.setManaged(false);
        etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito");
    }
    private void mostrarError(String m) { actualizarMensaje(m == null ? "Ocurrió un error." : m, true); }
    private void mostrarInfo(String m) { actualizarMensaje(m, false); }
    private void actualizarMensaje(String mensaje, boolean error) {
        String texto = mensaje == null ? "" : mensaje.trim(); etiquetaMensaje.setText(texto); etiquetaMensaje.setVisible(!texto.isBlank()); etiquetaMensaje.setManaged(!texto.isBlank()); etiquetaMensaje.getStyleClass().removeAll("mensaje-error", "mensaje-exito"); if (!texto.isBlank()) etiquetaMensaje.getStyleClass().add(error ? "mensaje-error" : "mensaje-exito");
    }
}
