package servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dao.PerfilJugadorDAO;
import negocio.EstadoPerfilJugador;
import negocio.PerfilJugador;

class ResolucionPerfilJugadorServiceTest {
    private PerfilJugadorDAO dao;
    private Connection conexion;
    private ResolucionPerfilJugadorService servicio;

    @BeforeEach void preparar() {
        dao = mock(PerfilJugadorDAO.class);
        conexion = mock(Connection.class);
        servicio = new ResolucionPerfilJugadorService(dao);
    }

    @Test void reutilizaPerfilDelCliente() {
        PerfilJugador perfil = perfil(4, 8L, EstadoPerfilJugador.DEFINITIVO);
        when(dao.buscarPorCliente(conexion, 8)).thenReturn(perfil);
        assertSame(perfil, servicio.resolverParaCliente(conexion, 8, "Ana"));
        verify(dao, never()).guardar(any(), any());
    }

    @Test void creaPerfilDefinitivo() {
        PerfilJugador perfil = servicio.resolverParaCliente(conexion, 8, " Ana  Perez ");
        assertEquals("Ana Perez", perfil.getNombrePublico());
        assertEquals(EstadoPerfilJugador.DEFINITIVO, perfil.getEstado());
        verify(dao).guardar(conexion, perfil);
    }

    @Test void creaPerfilSinFicha() {
        PerfilJugador perfil = servicio.crearSinFicha(conexion, "Maria Lopez");
        assertNull(perfil.getClienteId());
        assertEquals(EstadoPerfilJugador.PROVISIONAL, perfil.getEstado());
    }

    @Test void consolidaPerfilProvisional() {
        PerfilJugador perfil = perfil(4, null, EstadoPerfilJugador.PROVISIONAL);
        when(dao.buscar(conexion, 4)).thenReturn(perfil);
        assertSame(perfil, servicio.consolidarConCliente(conexion, 4L, 8, "Ana", 2));
        assertEquals(8, perfil.getClienteId());
        assertTrue(perfil.isVinculacionRevisada());
    }

    @Test void bloqueaConflictoEntrePerfiles() {
        PerfilJugador actual = perfil(4, null, EstadoPerfilJugador.PROVISIONAL);
        PerfilJugador cliente = perfil(5, 8L, EstadoPerfilJugador.DEFINITIVO);
        when(dao.buscarPorCliente(conexion, 8)).thenReturn(cliente);
        when(dao.buscar(conexion, 4)).thenReturn(actual);
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consolidarConCliente(conexion, 4L, 8, "Ana", 2));
    }

    private PerfilJugador perfil(long id, Long cliente, EstadoPerfilJugador estado) {
        PerfilJugador perfil = new PerfilJugador();
        perfil.setId(id); perfil.setClienteId(cliente); perfil.setEstado(estado);
        perfil.setNombrePublico("Jugador"); return perfil;
    }
}
