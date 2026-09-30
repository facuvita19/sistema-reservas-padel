package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.ClienteDAO;
import negocio.Cliente;
import negocio.TipoVinculacionTorneo;
import servicio.VinculacionClienteTelefonoService.EstadoVinculacion;
import servicio.VinculacionClienteTelefonoService.ResultadoVinculacion;

class VinculacionClienteTelefonoServiceTest {

    private ClienteDAO clienteDAO;
    private Connection conexion;
    private VinculacionClienteTelefonoService servicio;

    @BeforeEach
    void preparar() {
        clienteDAO = mock(ClienteDAO.class);
        conexion = mock(Connection.class);
        servicio = new VinculacionClienteTelefonoService(clienteDAO);
    }

    @Test
    void vinculaCuandoExisteUnaCoincidencia() {
        Cliente cliente = cliente(25L, "Ana", "Perez");
        when(clienteDAO.buscarActivosPorTelefonoNormalizado(
                conexion, "5491155551234"))
                .thenReturn(List.of(cliente));

        ResultadoVinculacion resultado = servicio.buscar(
                conexion, "+54 9 11 5555-1234");

        assertEquals(EstadoVinculacion.ENCONTRADO, resultado.estado());
        assertEquals(25L, resultado.clienteId());
        assertEquals(cliente, resultado.cliente());
        assertEquals(1, resultado.cantidadCoincidencias());
        assertEquals(
                TipoVinculacionTorneo.AUTOMATICA,
                resultado.tipoVinculacion());
        assertTrue(resultado.estaVinculado());
        assertFalse(resultado.requiereRevision());
    }

    @Test
    void quedaSinVincularCuandoNoHayCoincidencias() {
        when(clienteDAO.buscarActivosPorTelefonoNormalizado(
                conexion, "1155551234"))
                .thenReturn(List.of());

        ResultadoVinculacion resultado = servicio.buscar(
                conexion, "11 5555-1234");

        assertEquals(
                EstadoVinculacion.SIN_COINCIDENCIAS,
                resultado.estado());
        assertNull(resultado.clienteId());
        assertEquals(
                TipoVinculacionTorneo.SIN_VINCULAR,
                resultado.tipoVinculacion());
        assertFalse(resultado.requiereRevision());
    }

    @Test
    void marcaRevisionCuandoHayVariasCoincidencias() {
        when(clienteDAO.buscarActivosPorTelefonoNormalizado(
                conexion, "1155551234"))
                .thenReturn(List.of(
                        cliente(10L, "Ana", "Perez"),
                        cliente(11L, "Maria", "Lopez")));

        ResultadoVinculacion resultado = servicio.buscar(
                conexion, "11 5555-1234");

        assertEquals(EstadoVinculacion.AMBIGUA, resultado.estado());
        assertNull(resultado.clienteId());
        assertEquals(2, resultado.cantidadCoincidencias());
        assertTrue(resultado.requiereRevision());
    }

    @Test
    void rechazaConexionNulaYTelefonoInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.buscar(null, "1155551234"));
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.buscar(conexion, "123"));
    }

    private Cliente cliente(long id, String nombre, String apellido) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setTelefono("11 5555-1234");
        cliente.setActivo(true);
        return cliente;
    }
}
