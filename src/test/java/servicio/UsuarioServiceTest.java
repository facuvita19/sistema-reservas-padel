package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.UsuarioDAO;
import negocio.RolUsuario;
import negocio.Usuario;
import util.ProtectorPassword;

class UsuarioServiceTest {

    private UsuarioDAODoble dao;
    private UsuarioService service;

    @BeforeEach
    void preparar() {
        dao = new UsuarioDAODoble();
        service = new UsuarioService(dao);
    }

    @Test
    void registraClienteConPasswordProtegida() {
        Usuario usuario = service.registrar(
                "  Facundo.Vitale  ",
                "ClaveSegura123",
                RolUsuario.CLIENTE,
                20L
        );

        assertTrue(dao.guardarInvocado);
        assertEquals("facundo.vitale", usuario.getNombreUsuario());
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
        assertEquals(20L, usuario.getClienteId());
        assertFalse(usuario.getPasswordHash().contains("ClaveSegura123"));
        assertTrue(ProtectorPassword.verificar(
                "ClaveSegura123", usuario.getPasswordHash()));
    }

    @Test
    void registraAdministradorSinCliente() {
        Usuario usuario = service.registrar(
                "administrador2",
                "ClaveSegura123",
                RolUsuario.ADMINISTRADOR,
                null
        );

        assertNull(usuario.getClienteId());
        assertTrue(usuario.esAdministrador());
    }

    @Test
    void rechazaNombreDuplicado() {
        dao.nombreDuplicado = true;
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(
                        "facundo",
                        "ClaveSegura123",
                        RolUsuario.CLIENTE,
                        20L));
    }

    @Test
    void rechazaNombreInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(
                        "facundo vitale",
                        "ClaveSegura123",
                        RolUsuario.CLIENTE,
                        20L));
    }

    @Test
    void rechazaClienteSinVinculacion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(
                        "facundo",
                        "ClaveSegura123",
                        RolUsuario.CLIENTE,
                        null));
    }

    @Test
    void rechazaAdministradorVinculado() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(
                        "administrador2",
                        "ClaveSegura123",
                        RolUsuario.ADMINISTRADOR,
                        20L));
    }

    @Test
    void iniciaSesionConCredencialesCorrectas() {
        Usuario usuario = crearUsuario(
                5L,
                "facundo",
                "ClaveSegura123",
                RolUsuario.CLIENTE
        );
        dao.porNombre = usuario;

        Usuario resultado = service.iniciarSesion(
                " FACUNDO ", "ClaveSegura123");

        assertSame(usuario, resultado);
    }

    @Test
    void rechazaLoginIncorrecto() {
        dao.porNombre = crearUsuario(
                5L,
                "facundo",
                "ClaveSegura123",
                RolUsuario.CLIENTE
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.iniciarSesion(
                        "facundo", "Incorrecta123"));
    }

    @Test
    void cambiaPasswordConPasswordActualCorrecta() {
        Usuario usuario = crearUsuario(
                5L,
                "facundo",
                "ClaveSegura123",
                RolUsuario.CLIENTE
        );
        dao.buscado = usuario;

        service.cambiarPassword(
                5L, "ClaveSegura123", "ClaveNueva456");

        assertTrue(ProtectorPassword.verificar(
                "ClaveNueva456", usuario.getPasswordHash()));
        assertFalse(ProtectorPassword.verificar(
                "ClaveSegura123", usuario.getPasswordHash()));
    }

    @Test
    void protegeLaCuentaAdmin() {
        dao.buscado = crearUsuario(
                1L,
                "admin",
                "ClaveSegura123",
                RolUsuario.ADMINISTRADOR
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.eliminar(1L));
        assertFalse(dao.eliminarInvocado);
    }

    @Test
    void buscaListaYEliminaUsuarioComun() {
        Usuario usuario = crearUsuario(
                5L,
                "facundo",
                "ClaveSegura123",
                RolUsuario.CLIENTE
        );
        dao.buscado = usuario;
        dao.usuarios.add(usuario);

        assertSame(usuario, service.buscar(5L));
        assertEquals(1, service.listar().size());
        service.eliminar(5L);
        assertTrue(dao.eliminarInvocado);
        assertNull(service.buscar(0L));
    }

    private Usuario crearUsuario(
            long id,
            String nombre,
            String password,
            RolUsuario rol) {

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreUsuario(nombre);
        usuario.setPasswordHash(
                ProtectorPassword.generarHash(password));
        usuario.setRol(rol);
        usuario.setClienteId(
                rol == RolUsuario.CLIENTE ? 20L : null);
        usuario.setActivo(true);
        return usuario;
    }

    private static final class UsuarioDAODoble implements UsuarioDAO {
        private boolean guardarInvocado;
        private boolean eliminarInvocado;
        private boolean nombreDuplicado;
        private Usuario buscado;
        private Usuario porNombre;
        private final List<Usuario> usuarios = new ArrayList<>();

        @Override
        public void guardar(Usuario usuario) {
            guardarInvocado = true;
        }

        @Override
        public void eliminar(long id) {
            eliminarInvocado = true;
        }

        @Override
        public Usuario buscar(long id) {
            return buscado;
        }

        @Override
        public Usuario buscarPorNombreUsuario(String nombreUsuario) {
            return porNombre;
        }

        @Override
        public List<Usuario> listar() {
            return new ArrayList<>(usuarios);
        }

        @Override
        public boolean existeNombreUsuario(String nombre, long id) {
            return nombreDuplicado;
        }
    }
}
