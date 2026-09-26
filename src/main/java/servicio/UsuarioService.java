package servicio;

import java.util.List;
import java.util.Locale;

import dao.UsuarioDAO;
import dao.UsuarioDAOMySQL;
import negocio.RolUsuario;
import negocio.Usuario;
import util.ProtectorPassword;

public class UsuarioService {

    private static final String USUARIO_ADMIN = "admin";
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
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre);
        usuario.setPasswordHash(ProtectorPassword.generarHash(password));
        usuario.setRol(rol);
        usuario.setClienteId(clienteId);
        usuario.setActivo(true);
        usuarioDAO.guardar(usuario);
        return usuario;
    }

    public Usuario iniciarSesion(String nombreUsuario, String password) {
        if (nombreUsuario == null || nombreUsuario.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el usuario y la contraseña.");
        }
        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(
                normalizarNombreUsuario(nombreUsuario));
        if (usuario == null || !usuario.isActivo()
                || !ProtectorPassword.verificar(
                        password, usuario.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "El usuario o la contraseña son incorrectos.");
        }
        return usuario;
    }

    public void actualizarDatos(Usuario usuario) {
        if (usuario == null || usuario.getId() <= 0) {
            throw new IllegalArgumentException("El usuario no es válido.");
        }
        Usuario original = obtenerUsuarioObligatorio(usuario.getId(), false);
        validarNombreUsuario(usuario.getNombreUsuario());
        validarVinculacion(usuario.getRol(), usuario.getClienteId());
        protegerUltimoAdministrador(original, usuario.getRol(), usuario.isActivo());

        String nombre = normalizarNombreUsuario(usuario.getNombreUsuario());
        if (usuarioDAO.existeNombreUsuario(nombre, usuario.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con ese nombre de usuario.");
        }
        usuario.setNombreUsuario(nombre);
        usuario.setPasswordHash(null);
        usuarioDAO.guardar(usuario);
    }

    public void cambiarPassword(
            long usuarioId, String passwordActual, String passwordNuevo) {
        Usuario usuario = obtenerUsuarioObligatorio(usuarioId, true);
        if (!ProtectorPassword.verificar(
                passwordActual, usuario.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "La contraseña actual es incorrecta.");
        }
        validarPasswordNueva(passwordNuevo);
        if (passwordNuevo.equals(passwordActual)) {
            throw new IllegalArgumentException(
                    "La contraseña nueva debe ser diferente.");
        }
        usuario.setPasswordHash(ProtectorPassword.generarHash(passwordNuevo));
        usuarioDAO.guardar(usuario);
    }

    public void restablecerPassword(long usuarioId, String passwordNuevo) {
        Usuario usuario = obtenerUsuarioObligatorio(usuarioId, false);
        validarPasswordNueva(passwordNuevo);
        usuario.setPasswordHash(ProtectorPassword.generarHash(passwordNuevo));
        usuarioDAO.guardar(usuario);
    }

    public void desactivar(long id, long usuarioActualId) {
        if (id == usuarioActualId) {
            throw new IllegalArgumentException(
                    "No podés desactivar tu propia cuenta.");
        }
        Usuario usuario = obtenerUsuarioObligatorio(id, true);
        if (USUARIO_ADMIN.equalsIgnoreCase(usuario.getNombreUsuario())) {
            throw new IllegalArgumentException(
                    "No se puede desactivar la cuenta administrativa principal.");
        }
        protegerUltimoAdministrador(usuario, usuario.getRol(), false);
        usuarioDAO.eliminar(id);
    }

    public void activar(long id) {
        Usuario usuario = obtenerUsuarioObligatorio(id, false);
        if (usuario.isActivo()) {
            throw new IllegalArgumentException("El usuario ya está activo.");
        }
        usuarioDAO.activar(id);
    }

    public void eliminar(long id) {
        Usuario usuario = obtenerUsuarioObligatorio(id, true);
        if (USUARIO_ADMIN.equalsIgnoreCase(usuario.getNombreUsuario())) {
            throw new IllegalArgumentException(
                    "No se puede desactivar la cuenta administrativa principal.");
        }
        protegerUltimoAdministrador(usuario, usuario.getRol(), false);
        usuarioDAO.eliminar(id);
    }

    public Usuario buscar(long id) {
        return id <= 0 ? null : usuarioDAO.buscar(id);
    }

    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) return null;
        return usuarioDAO.buscarPorNombreUsuario(
                normalizarNombreUsuario(nombreUsuario));
    }

    public List<Usuario> listar() {
        return usuarioDAO.listar();
    }

    private void protegerUltimoAdministrador(
            Usuario original, RolUsuario nuevoRol, boolean activoNuevo) {
        if (original.esAdministrador()
                && (!activoNuevo || nuevoRol != RolUsuario.ADMINISTRADOR)
                && usuarioDAO.contarAdministradoresActivos() <= 1) {
            throw new IllegalArgumentException(
                    "Debe permanecer al menos un administrador activo.");
        }
    }

    private void validarDatosRegistro(
            String nombreUsuario, String password,
            RolUsuario rol, Long clienteId) {
        validarNombreUsuario(nombreUsuario);
        validarPasswordNueva(password);
        validarVinculacion(rol, clienteId);
    }

    private void validarPasswordNueva(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 8 caracteres.");
        }
    }

    private void validarNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio.");
        }
        String limpio = nombreUsuario.trim();
        if (limpio.length() < 4 || limpio.length() > 40) {
            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener entre 4 y 40 caracteres.");
        }
        if (!limpio.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException(
                    "El nombre de usuario solo puede contener letras, "
                            + "números, puntos, guiones y guiones bajos.");
        }
    }

    private void validarVinculacion(RolUsuario rol, Long clienteId) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }
        if (rol == RolUsuario.CLIENTE
                && (clienteId == null || clienteId <= 0)) {
            throw new IllegalArgumentException(
                    "Una cuenta de cliente debe estar vinculada a una ficha.");
        }
        if (rol != RolUsuario.CLIENTE && clienteId != null) {
            throw new IllegalArgumentException(
                    "Una cuenta del personal no debe estar vinculada a un cliente.");
        }
    }

    private String normalizarNombreUsuario(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase(Locale.ROOT);
    }

    private Usuario obtenerUsuarioObligatorio(
            long usuarioId, boolean exigirActivo) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del usuario no es válido.");
        }
        Usuario usuario = usuarioDAO.buscar(usuarioId);
        if (usuario == null || (exigirActivo && !usuario.isActivo())) {
            throw new IllegalArgumentException(
                    "El usuario no existe"
                            + (exigirActivo ? " o está inactivo." : "."));
        }
        return usuario;
    }
}
