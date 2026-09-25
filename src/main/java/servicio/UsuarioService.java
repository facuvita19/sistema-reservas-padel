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
                    "El DAO de usuarios no puede ser nulo."
            );
        }
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario registrar(
            String nombreUsuario,
            String password,
            RolUsuario rol,
            Long clienteId) {

        validarDatosRegistro(
                nombreUsuario,
                password,
                rol,
                clienteId
        );

        String nombreNormalizado = normalizarNombreUsuario(
                nombreUsuario
        );

        if (usuarioDAO.existeNombreUsuario(
                nombreNormalizado,
                0L)) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con ese nombre de usuario."
            );
        }

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreNormalizado);
        usuario.setPasswordHash(
                ProtectorPassword.generarHash(password)
        );
        usuario.setRol(rol);
        usuario.setClienteId(clienteId);
        usuario.setActivo(true);

        usuarioDAO.guardar(usuario);
        return usuario;
    }

    public Usuario iniciarSesion(
            String nombreUsuario,
            String password) {

        if (nombreUsuario == null
                || nombreUsuario.isBlank()
                || password == null
                || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar el usuario y la contrasena."
            );
        }

        Usuario usuario = usuarioDAO.buscarPorNombreUsuario(
                normalizarNombreUsuario(nombreUsuario)
        );

        if (usuario == null || !usuario.isActivo()
                || !ProtectorPassword.verificar(
                        password,
                        usuario.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "El usuario o la contrasena son incorrectos."
            );
        }

        return usuario;
    }

    public void actualizarDatos(Usuario usuario) {
        if (usuario == null || usuario.getId() <= 0) {
            throw new IllegalArgumentException(
                    "El usuario no es valido."
            );
        }

        validarNombreUsuario(usuario.getNombreUsuario());
        validarVinculacion(usuario.getRol(), usuario.getClienteId());

        String nombreNormalizado = normalizarNombreUsuario(
                usuario.getNombreUsuario()
        );

        if (usuarioDAO.existeNombreUsuario(
                nombreNormalizado,
                usuario.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con ese nombre de usuario."
            );
        }

        usuario.setNombreUsuario(nombreNormalizado);
        usuarioDAO.guardar(usuario);
    }

    public void cambiarPassword(
            long usuarioId,
            String passwordActual,
            String passwordNuevo) {

        Usuario usuario = obtenerUsuarioObligatorio(usuarioId);

        if (!ProtectorPassword.verificar(
                passwordActual,
                usuario.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "La contrasena actual es incorrecta."
            );
        }

        if (passwordNuevo != null
                && passwordNuevo.equals(passwordActual)) {
            throw new IllegalArgumentException(
                    "La contrasena nueva debe ser diferente."
            );
        }

        usuario.setPasswordHash(
                ProtectorPassword.generarHash(passwordNuevo)
        );
        usuarioDAO.guardar(usuario);
    }

    public void restablecerPassword(
            long usuarioId,
            String passwordNuevo) {

        Usuario usuario = obtenerUsuarioObligatorio(usuarioId);
        usuario.setPasswordHash(
                ProtectorPassword.generarHash(passwordNuevo)
        );
        usuarioDAO.guardar(usuario);
    }

    public void eliminar(long id) {
        Usuario usuario = obtenerUsuarioObligatorio(id);

        if (USUARIO_ADMIN.equalsIgnoreCase(
                usuario.getNombreUsuario())) {
            throw new IllegalArgumentException(
                    "No se puede desactivar la cuenta "
                            + "administrativa principal."
            );
        }

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
                normalizarNombreUsuario(nombreUsuario)
        );
    }

    public List<Usuario> listar() {
        return usuarioDAO.listar();
    }

    private void validarDatosRegistro(
            String nombreUsuario,
            String password,
            RolUsuario rol,
            Long clienteId) {

        validarNombreUsuario(nombreUsuario);

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contrasena es obligatoria."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener al menos 8 caracteres."
            );
        }

        validarVinculacion(rol, clienteId);
    }

    private void validarNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        String limpio = nombreUsuario.trim();

        if (limpio.length() < 4 || limpio.length() > 40) {
            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener entre "
                            + "4 y 40 caracteres."
            );
        }

        if (!limpio.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException(
                    "El nombre de usuario solo puede contener "
                            + "letras, numeros, puntos, guiones "
                            + "y guiones bajos."
            );
        }
    }

    private void validarVinculacion(
            RolUsuario rol,
            Long clienteId) {

        if (rol == null) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio."
            );
        }

        if (rol == RolUsuario.CLIENTE
                && (clienteId == null || clienteId <= 0)) {
            throw new IllegalArgumentException(
                    "Una cuenta de cliente debe estar vinculada "
                            + "a una ficha de cliente."
            );
        }

        if (rol == RolUsuario.ADMINISTRADOR
                && clienteId != null) {
            throw new IllegalArgumentException(
                    "Una cuenta administrativa no debe estar "
                            + "vinculada a un cliente."
            );
        }
    }

    private String normalizarNombreUsuario(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase(Locale.ROOT);
    }

    private Usuario obtenerUsuarioObligatorio(long usuarioId) {
        if (usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del usuario no es valido."
            );
        }

        Usuario usuario = usuarioDAO.buscar(usuarioId);

        if (usuario == null || !usuario.isActivo()) {
            throw new IllegalArgumentException(
                    "El usuario no existe o esta inactivo."
            );
        }

        return usuario;
    }
}
