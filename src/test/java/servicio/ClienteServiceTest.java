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

import dao.ClienteDAO;
import negocio.Cliente;

class ClienteServiceTest {

    private ClienteDAODoble dao;
    private ClienteService service;

    @BeforeEach
    void preparar() {
        dao = new ClienteDAODoble();
        service = new ClienteService(dao);
    }

    @Test
    void guardaYNormalizaClienteValido() {
        Cliente cliente = crearCliente();
        cliente.setNombre("  facundo   ");
        cliente.setApellido("  vitale  ");
        cliente.setDocumento("12.345.678");
        cliente.setEmail("  FACU@EMAIL.COM  ");

        service.guardar(cliente);

        assertTrue(dao.guardarInvocado);
        assertSame(cliente, dao.ultimoGuardado);
        assertEquals("Facundo", cliente.getNombre());
        assertEquals("Vitale", cliente.getApellido());
        assertEquals("12345678", cliente.getDocumento());
        assertEquals("facu@email.com", cliente.getEmail());
    }

    @Test
    void convierteEmailVacioEnNulo() {
        Cliente cliente = crearCliente();
        cliente.setEmail("   ");

        service.guardar(cliente);

        assertNull(cliente.getEmail());
    }

    @Test
    void rechazaClienteNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(null)
        );
        assertFalse(dao.guardarInvocado);
    }

    @Test
    void rechazaDocumentoInvalido() {
        Cliente cliente = crearCliente();
        cliente.setDocumento("123");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(cliente)
        );
    }

    @Test
    void rechazaTelefonoInvalido() {
        Cliente cliente = crearCliente();
        cliente.setTelefono("abc@@@");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(cliente)
        );
    }

    @Test
    void rechazaEmailInvalido() {
        Cliente cliente = crearCliente();
        cliente.setEmail("correo-invalido");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(cliente)
        );
    }

    @Test
    void rechazaDocumentoDuplicado() {
        dao.documentoDuplicado = true;

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(crearCliente())
        );

        assertTrue(error.getMessage().contains("documento"));
        assertFalse(dao.guardarInvocado);
    }

    @Test
    void rechazaEmailDuplicado() {
        dao.emailDuplicado = true;

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(crearCliente())
        );

        assertTrue(error.getMessage().contains("correo"));
    }

    @Test
    void buscaListaYElimina() {
        Cliente cliente = crearCliente();
        dao.clientes.add(cliente);
        dao.buscado = cliente;

        assertSame(cliente, service.buscar(10L));
        assertEquals(1, service.listar().size());

        service.eliminar(10L);
        assertEquals(10L, dao.idEliminado);
    }

    @Test
    void buscarIdInvalidoDevuelveNulo() {
        assertNull(service.buscar(0L));
    }

    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNombre("Facundo");
        cliente.setApellido("Vitale");
        cliente.setDocumento("12345678");
        cliente.setTelefono("2244 429221");
        cliente.setEmail("facu@email.com");
        return cliente;
    }

    private static final class ClienteDAODoble implements ClienteDAO {
        private boolean guardarInvocado;
        private boolean documentoDuplicado;
        private boolean emailDuplicado;
        private long idEliminado;
        private Cliente ultimoGuardado;
        private Cliente buscado;
        private final List<Cliente> clientes = new ArrayList<>();

        @Override
        public void guardar(Cliente cliente) {
            guardarInvocado = true;
            ultimoGuardado = cliente;
        }

        // doblesDaoCompletosV1
        @Override
        public void guardar(
                java.sql.Connection conexion,
                Cliente cliente) {
            guardar(cliente);
        }

        @Override
        public Cliente buscarPorDocumento(String documento) {
            if (documento == null) return null;
            return clientes.stream()
                    .filter(cliente -> documento.equals(
                            cliente.getDocumento()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Cliente buscarPorDocumento(
                java.sql.Connection conexion,
                String documento) {
            return buscarPorDocumento(documento);
        }

        @Override
        public Cliente buscarPorEmail(String email) {
            if (email == null) return null;
            return clientes.stream()
                    .filter(cliente -> cliente.getEmail() != null)
                    .filter(cliente -> email.equalsIgnoreCase(
                            cliente.getEmail()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void eliminar(long id) {
            idEliminado = id;
        }

        @Override
        public Cliente buscar(long id) {
            return buscado;
        }

        // doblesDaoConexionV1
        @Override
        public Cliente buscarPorEmail(
                java.sql.Connection conexion,
                String email) {
            if (email == null) return null;
            return clientes.stream()
                    .filter(cliente -> cliente.getEmail() != null)
                    .filter(cliente -> email.equalsIgnoreCase(
                            cliente.getEmail()))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Cliente> listar() {
            return new ArrayList<>(clientes);
        }

        @Override
        public boolean existeDocumento(String documento, long id) {
            return documentoDuplicado;
        }

        @Override
        public boolean existeEmail(String email, long id) {
            return emailDuplicado;
        }
    }
}
