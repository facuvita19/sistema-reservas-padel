package servicio;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import dao.UsuarioDAO;
import dao.UsuarioDAOMySQL;
import negocio.RolUsuario;
import negocio.Usuario;
import util.ProtectorPassword;

public class UsuarioService {

    private static final String USUARIO_ADMIN = "admin";
    private static final Pattern EMAIL_ACCESO = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this(new UsuarioDAOMySQL());
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        if (usuarioDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de usuarios no puede ser nulo.");
        }
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario registrar(
            String nombreUsuario,
            String password,
            RolUsuario rol,
            Long clienteId) {
        validarDatosRegistro(nombreUsuario, password, rol, clienteId);
        String nombre = normalizarNombreUsuario(nombreUsuario);
        if (usuarioDAO.existeNombreUsuario(nombre, 0L)) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con ese nombre de usuario.");
        }
        Usuario usuario = crearUsuario(
                nombre, password, rol, clienteId);
        usuarioDAO.guardar(usuario);
        return usuario;
    }

    public Usuario registrarCliente(
            Connection conexion,
            String email,
            String password,
            long clienteId) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        validarClienteId(clienteId);
        validarEmailAccesoCliente(email);
        validarPasswordNueva(password);

        String correo = normalizarNombreUsuario(email);
        Usuario porNombre = usuarioDAO.buscarPorNombreUsuario(
                conexion, correo, false);
        if (porNombre != null) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con ese correo electronico.");
        }

        Usuario porCliente = usuarioDAO.buscarPorClienteId(
                conexion, clienteId);
        if (porCliente != null) {
            throw new IllegalArgumentException(
                    "El cliente ya tiene una cuenta asociada.");
        }

        Usuario usuario = crearUsuario(
                correo,
                password,
                RolUsuario.CLIENTE,
                clienteId);
        usuarioDAO.guardar(conexion, usuario);
        return usuario;
    }

    public Usuario iniciarSesion(
            String nombreUsuario,
            String password) {
        validarCredencialesPresentes(nombreUsuario, password);
        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(
                normalizarNombreUsuario(nombreUsuario));
        validarPassword(usuario, password);
        return usuario;
    }

    public Usuario autenticarCliente(
            String email,
            String password) {
        validarCredencialesPresentes(email, password);
        String correo = normalizarNombreUsuario(email);
        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(correo);
        validarPassword(usuario, password);

        if (usuario.getRol() != RolUsuario.CLIENTE
                || !usuario.estaVinculadoACliente()) {
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    public void actualizarDatos(Usuario usuario) {
        if (usuario == null || usuario.getId() <= 0) {
            throw new IllegalArgumentException(
                    "El usuario no es valido.");
        }
        Usuario original = obtenerUsuarioObligatorio(
                usuario.getId(), false);
        validarIdentificadorPorRol(
                usuario.getNombreUsuario(), usuario.getRol());
        validarVinculacion(usuario.getRol(), usuario.getClienteId());
        protegerUltimoAdministrador(
                original, usuario.getRol(), usuario.isActivo());

        String nombre = normalizarNombreUsuario(
                usuario.getNombreUsuario());
        if (usuarioDAO.existeNombreUsuario(
                nombre, usuario.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con ese nombre de usuario.");
        }
        usuario.setNombreUsuario(nombre);
        usuario.setPasswordHash(null);
        usuarioDAO.guardar(usuario);
    }

    public void cambiarPassword(
            long usuarioId,
            String passwordActual,
            String passwordNuevo) {
        Usuario usuario = obtenerUsuarioObligatorio(usuarioId, true);
        if (!ProtectorPassword.verificar(
                passwordActual, usuario.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "La contrasena actual es incorrecta.");
        }
        validarPasswordNueva(passwordNuevo);
        if (passwordNuevo.equals(passwordActual)) {
            throw new IllegalArgumentException(
                    "La contrasena nueva debe ser diferente.");
        }
        usuario.setPasswordHash(
                ProtectorPassword.generarHash(passwordNuevo));
        usuarioDAO.guardar(usuario);
    }

    public void restablecerPassword(
            long usuarioId,
            String passwordNuevo) {
        Usuario usuario = obtenerUsuarioObligatorio(
                usuarioId, false);
        validarPasswordNueva(passwordNuevo);
        usuario.setPasswordHash(
                ProtectorPassword.generarHash(passwordNuevo));
        usuarioDAO.guardar(usuario);
    }

    public void desactivar(long id, long usuarioActualId) {
        if (id == usuarioActualId) {
            throw new IllegalArgumentException(
                    "No podes desactivar tu propia cuenta.");
        }
        Usuario usuario = obtenerUsuarioObligatorio(id, true);
        if (USUARIO_ADMIN.equalsIgnoreCase(
                usuario.getNombreUsuario())) {
            throw new IllegalArgumentException(
                    "No se puede desactivar la cuenta "
                            + "administrativa principal.");
        }
        protegerUltimoAdministrador(
                usuario, usuario.getRol(), false);
        usuarioDAO.eliminar(id);
    }

    public void activar(long id) {
        Usuario usuario = obtenerUsuarioObligatorio(id, false);
        if (usuario.isActivo()) {
            throw new IllegalArgumentException(
                    "El usuario ya esta activo.");
        }
        usuarioDAO.activar(id);
    }

    public void eliminar(long id) {
        Usuario usuario = obtenerUsuarioObligatorio(id, true);
        if (USUARIO_ADMIN.equalsIgnoreCase(
                usuario.getNombreUsuario())) {
            throw new IllegalArgumentException(
                    "No se puede desactivar la cuenta "
                            + "administrativa principal.");
        }
        protegerUltimoAdministrador(
                usuario, usuario.getRol(), false);
        usuarioDAO.eliminar(id);
    }

    public Usuario buscar(long id) {
        return id <= 0 ? null : usuarioDAO.buscar(id);
    }

    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            return null;
        }
        return usuarioDAO.buscarPorNombreUsuario(
                normalizarNombreUsuario(nombreUsuario));
    }

    public Usuario buscarPorClienteId(long clienteId) {
        return clienteId <= 0
                ? null
                : usuarioDAO.buscarPorClienteId(clienteId);
    }

    public List<Usuario> listar() {
        return usuarioDAO.listar();
    }

    private Usuario crearUsuario(
            String nombreUsuario,
            String password,
            RolUsuario rol,
            Long clienteId) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setPasswordHash(
                ProtectorPassword.generarHash(password));
        usuario.setRol(rol);
        usuario.setClienteId(clienteId);
        usuario.setActivo(true);
        return usuario;
    }

    private void validarPassword(
            Usuario usuario,
            String password) {
        if (usuario == null
                || !usuario.isActivo()
                || !ProtectorPassword.verificar(
                        password, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }
    }

    private void validarCredencialesPresentes(
            String nombreUsuario,
            String password) {
        if (nombreUsuario == null || nombreUsuario.isBlank()
                || password == null || password.isBlank()) {
            throw new CredencialesInvalidasException();
        }
    }

    private void protegerUltimoAdministrador(
            Usuario original,
            RolUsuario nuevoRol,
            boolean activoNuevo) {
        if (original.esAdministrador()
                && (!activoNuevo
                        || nuevoRol != RolUsuario.ADMINISTRADOR)
                && usuarioDAO.contarAdministradoresActivos() <= 1) {
            throw new IllegalArgumentException(
                    "Debe permanecer al menos un administrador activo.");
        }
    }

    private void validarDatosRegistro(
            String nombreUsuario,
            String password,
            RolUsuario rol,
            Long clienteId) {
        validarIdentificadorPorRol(nombreUsuario, rol);
        validarPasswordNueva(password);
        validarVinculacion(rol, clienteId);
    }

    private void validarPasswordNueva(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contrasena es obligatoria.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener al menos 8 caracteres.");
        }
    }

    private void validarIdentificadorPorRol(
            String nombreUsuario,
            RolUsuario rol) {
        if (rol == null) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio.");
        }
        if (rol == RolUsuario.CLIENTE
                && nombreUsuario != null
                && EMAIL_ACCESO.matcher(nombreUsuario.trim()).matches()) {
            validarEmailAccesoCliente(nombreUsuario);
            return;
        }
        validarNombreUsuarioPersonal(nombreUsuario);
    }

    private void validarNombreUsuarioPersonal(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio.");
        }
        String limpio = nombreUsuario.trim();
        if (limpio.length() < 4 || limpio.length() > 40) {
            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener entre 4 "
                            + "y 40 caracteres.");
        }
        if (!limpio.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException(
                    "El nombre de usuario solo puede contener letras, "
                            + "numeros, puntos, guiones y guiones bajos.");
        }
    }

    private void validarEmailAccesoCliente(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo electronico es obligatorio.");
        }
        String limpio = email.trim();
        if (limpio.length() > 150
                || !EMAIL_ACCESO.matcher(limpio).matches()) {
            throw new IllegalArgumentException(
                    "El correo electronico no tiene un formato valido.");
        }
    }

    private void validarVinculacion(
            RolUsuario rol,
            Long clienteId) {
        if (rol == null) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio.");
        }
        if (rol == RolUsuario.CLIENTE
                && (clienteId == null || clienteId <= 0)) {
            throw new IllegalArgumentException(
                    "Una cuenta de cliente debe estar vinculada "
                            + "a una ficha.");
        }
        if (rol != RolUsuario.CLIENTE && clienteId != null) {
            throw new IllegalArgumentException(
                    "Una cuenta del personal no debe estar vinculada "
                            + "a un cliente.");
        }
    }

    private void validarClienteId(long clienteId) {
        if (clienteId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del cliente debe ser positivo.");
        }
    }

    private String normalizarNombreUsuario(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase(Locale.ROOT);
    }

    private Usuario obtenerUsuarioObligatorio(
            long usuarioId,
            boolean exigirActivo) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del usuario no es valido.");
        }
        Usuario usuario = usuarioDAO.buscar(usuarioId);
        if (usuario == null
                || (exigirActivo && !usuario.isActivo())) {
            throw new IllegalArgumentException(
                    "El usuario no existe"
                            + (exigirActivo
                                    ? " o esta inactivo."
                                    : "."));
        }
        return usuario;
    }

    public static class CredencialesInvalidasException
            extends IllegalArgumentException {

        public CredencialesInvalidasException() {
            super("El usuario o la contrasena son incorrectos.");
        }
    }
}
