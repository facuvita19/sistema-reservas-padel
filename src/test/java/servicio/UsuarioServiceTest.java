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
                20L);

        assertTrue(dao.guardarInvocado);
        assertEquals("facundo.vitale", usuario.getNombreUsuario());
        assertEquals(RolUsuario.CLIENTE, usuario.getRol());
        assertEquals(20L, usuario.getClienteId());
        assertFalse(usuario.getPasswordHash().contains("ClaveSegura123"));
        assertTrue(ProtectorPassword.verificar(
                "ClaveSegura123", usuario.getPasswordHash()));
    }

    @Test
    void registraAdministradorYOperadorSinCliente() {
        Usuario administrador = service.registrar(
                "administrador2",
                "ClaveSegura123",
                RolUsuario.ADMINISTRADOR,
                null);

        Usuario operador = service.registrar(
                "operador1",
                "ClaveSegura123",
                RolUsuario.OPERADOR,
                null);

        assertNull(administrador.getClienteId());
        assertTrue(administrador.esAdministrador());
        assertNull(operador.getClienteId());
        assertTrue(operador.esOperador());
        assertTrue(operador.esPersonalDelComplejo());
    }

    @Test
    void rechazaPersonalVinculadoACliente() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(
                        "operador1",
                        "ClaveSegura123",
                        RolUsuario.OPERADOR,
                        20L));
    }

    @Test
    void rechazaNombreDuplicadoEInvalido() {
        dao.nombreDuplicado = true;
        assertThrows(IllegalArgumentException.class,
                () -> service.registrar(
                        "facundo",
                        "ClaveSegura123",
                        RolUsuario.CLIENTE,
                        20L));

        dao.nombreDuplicado = false;
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
    void iniciaSesionConCredencialesCorrectas() {
        Usuario usuario = crearUsuario(
                5L,
                "operador1",
                "ClaveSegura123",
                RolUsuario.OPERADOR,
                true);
        dao.porNombre = usuario;

        Usuario resultado = service.iniciarSesion(
                " OPERADOR1 ", "ClaveSegura123");

        assertSame(usuario, resultado);
    }

    @Test
    void rechazaLoginIncorrecto() {
        dao.porNombre = crearUsuario(
                5L,
                "facundo",
                "ClaveSegura123",
                RolUsuario.CLIENTE,
                true);

        assertThrows(IllegalArgumentException.class,
                () -> service.iniciarSesion(
                        "facundo", "Incorrecta123"));
    }

    @Test
    void cambiaYRestablecePassword() {
        Usuario usuario = crearUsuario(
                5L,
                "operador1",
                "ClaveSegura123",
                RolUsuario.OPERADOR,
                true);
        dao.buscado = usuario;

        service.cambiarPassword(
                5L, "ClaveSegura123", "ClaveNueva456");

        assertTrue(ProtectorPassword.verificar(
                "ClaveNueva456", usuario.getPasswordHash()));

        service.restablecerPassword(5L, "OtraClave789");

        assertTrue(ProtectorPassword.verificar(
                "OtraClave789", usuario.getPasswordHash()));
    }

    @Test
    void impideAutodesactivacion() {
        dao.buscado = crearUsuario(
                5L,
                "operador1",
                "ClaveSegura123",
                RolUsuario.OPERADOR,
                true);

        assertThrows(IllegalArgumentException.class,
                () -> service.desactivar(5L, 5L));
        assertFalse(dao.eliminarInvocado);
    }

    @Test
    void protegeCuentaAdminPrincipal() {
        dao.buscado = crearUsuario(
                1L,
                "admin",
                "ClaveSegura123",
                RolUsuario.ADMINISTRADOR,
                true);
        dao.administradoresActivos = 2L;

        assertThrows(IllegalArgumentException.class,
                () -> service.desactivar(1L, 9L));
        assertFalse(dao.eliminarInvocado);
    }

    @Test
    void protegeUltimoAdministradorActivo() {
        dao.buscado = crearUsuario(
                2L,
                "administrador2",
                "ClaveSegura123",
                RolUsuario.ADMINISTRADOR,
                true);
        dao.administradoresActivos = 1L;

        assertThrows(IllegalArgumentException.class,
                () -> service.desactivar(2L, 9L));
        assertFalse(dao.eliminarInvocado);
    }

    @Test
    void permiteDesactivarAdministradorSiExisteOtro() {
        dao.buscado = crearUsuario(
                2L,
                "administrador2",
                "ClaveSegura123",
                RolUsuario.ADMINISTRADOR,
                true);
        dao.administradoresActivos = 2L;

        service.desactivar(2L, 9L);

        assertTrue(dao.eliminarInvocado);
    }

    @Test
    void activaUsuarioInactivo() {
        dao.buscado = crearUsuario(
                5L,
                "operador1",
                "ClaveSegura123",
                RolUsuario.OPERADOR,
                false);

        service.activar(5L);

        assertTrue(dao.activarInvocado);
    }

    @Test
    void impideCambiarRolAlUltimoAdministrador() {
        Usuario original = crearUsuario(
                2L,
                "administrador2",
                "ClaveSegura123",
                RolUsuario.ADMINISTRADOR,
                true);
        dao.buscado = original;
        dao.administradoresActivos = 1L;

        Usuario modificado = crearUsuario(
                2L,
                "administrador2",
                "ClaveSegura123",
                RolUsuario.OPERADOR,
                true);

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarDatos(modificado));
    }

    @Test
    void buscaListaYDesactivaUsuarioComun() {
        Usuario usuario = crearUsuario(
                5L,
                "operador1",
                "ClaveSegura123",
                RolUsuario.OPERADOR,
                true);
        dao.buscado = usuario;
        dao.usuarios.add(usuario);

        assertSame(usuario, service.buscar(5L));
        assertEquals(1, service.listar().size());
        service.desactivar(5L, 9L);
        assertTrue(dao.eliminarInvocado);
        assertNull(service.buscar(0L));
    }

    private Usuario crearUsuario(
            long id,
            String nombre,
            String password,
            RolUsuario rol,
            boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreUsuario(nombre);
        usuario.setPasswordHash(
                ProtectorPassword.generarHash(password));
        usuario.setRol(rol);
        usuario.setClienteId(
                rol == RolUsuario.CLIENTE ? 20L : null);
        usuario.setActivo(activo);
        return usuario;
    }

    private static final class UsuarioDAODoble implements UsuarioDAO {
        private boolean guardarInvocado;
        private boolean eliminarInvocado;
        private boolean activarInvocado;
        private boolean nombreDuplicado;
        private long administradoresActivos = 1L;
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
        public void activar(long id) {
            activarInvocado = true;
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

        @Override
        public long contarAdministradoresActivos() {
            return administradoresActivos;
        }
    }
}
