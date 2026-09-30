package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.ClienteDAO;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionJugadorDAO;
import negocio.Cliente;
import negocio.TipoVinculacionTorneo;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;

class GestionVinculacionJugadorTorneoServiceTest {

    private TorneoInscripcionJugadorDAO jugadorDAO;
    private TorneoInscripcionDAO inscripcionDAO;
    private ClienteDAO clienteDAO;
    private Connection conexion;
    private GestionVinculacionJugadorTorneoService servicio;

    @BeforeEach
    void preparar() {
        jugadorDAO = mock(TorneoInscripcionJugadorDAO.class);
        inscripcionDAO = mock(TorneoInscripcionDAO.class);
        clienteDAO = mock(ClienteDAO.class);
        conexion = mock(Connection.class);
        servicio = new GestionVinculacionJugadorTorneoService(
                jugadorDAO,
                inscripcionDAO,
                clienteDAO,
                () -> conexion);
    }

    @Test
    void vinculaManualmenteUnIntegrante() throws Exception {
        TorneoInscripcionJugador responsable = jugador(10L, 1, true);
        TorneoInscripcionJugador pareja = jugador(11L, 2, false);
        TorneoInscripcion inscripcion = inscripcion(responsable, pareja);
        Cliente cliente = cliente(50L, true);

        when(jugadorDAO.buscar(conexion, 11L)).thenReturn(pareja);
        when(clienteDAO.buscar(conexion, 50L)).thenReturn(cliente);
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);
        when(inscripcionDAO.clienteParticipaEnCategoria(
                conexion, 7L, 50L, 20L)).thenReturn(false);

        TorneoInscripcionJugador resultado =
                servicio.vincularManualmente(11L, 50L);

        assertEquals(50L, resultado.getClienteId());
        assertEquals(TipoVinculacionTorneo.MANUAL,
                resultado.getTipoVinculacion());
        assertFalse(resultado.isRequiereRevision());
        verify(jugadorDAO).guardar(conexion, pareja);
        verify(inscripcionDAO, never()).guardar(
                conexion, inscripcion);
        verify(conexion).commit();
        verify(conexion, never()).rollback();
    }

    @Test
    void sincronizaResponsableAlVincular() throws Exception {
        TorneoInscripcionJugador responsable = jugador(10L, 1, true);
        TorneoInscripcionJugador pareja = jugador(11L, 2, false);
        TorneoInscripcion inscripcion = inscripcion(responsable, pareja);
        Cliente cliente = cliente(50L, true);

        when(jugadorDAO.buscar(conexion, 10L)).thenReturn(responsable);
        when(clienteDAO.buscar(conexion, 50L)).thenReturn(cliente);
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);
        when(inscripcionDAO.clienteParticipaEnCategoria(
                conexion, 7L, 50L, 20L)).thenReturn(false);

        servicio.vincularManualmente(10L, 50L);

        assertEquals(50L, inscripcion.getResponsableClienteId());
        verify(inscripcionDAO).guardar(conexion, inscripcion);
        verify(conexion).commit();
    }

    @Test
    void rechazaClienteInactivo() throws Exception {
        TorneoInscripcionJugador pareja = jugador(11L, 2, false);
        when(jugadorDAO.buscar(conexion, 11L)).thenReturn(pareja);
        when(clienteDAO.buscar(conexion, 50L))
                .thenReturn(cliente(50L, false));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.vincularManualmente(11L, 50L));

        verify(jugadorDAO, never()).guardar(
                any(Connection.class),
                any(TorneoInscripcionJugador.class));
        verify(conexion).rollback();
        verify(conexion, never()).commit();
    }

    @Test
    void rechazaMismoClienteParaLosDosIntegrantes() throws Exception {
        TorneoInscripcionJugador responsable = jugador(10L, 1, true);
        responsable.setClienteId(50L);
        responsable.setTipoVinculacion(
                TipoVinculacionTorneo.AUTOMATICA);
        TorneoInscripcionJugador pareja = jugador(11L, 2, false);
        TorneoInscripcion inscripcion = inscripcion(responsable, pareja);

        when(jugadorDAO.buscar(conexion, 11L)).thenReturn(pareja);
        when(clienteDAO.buscar(conexion, 50L))
                .thenReturn(cliente(50L, true));
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.vincularManualmente(11L, 50L));

        verify(inscripcionDAO, never())
                .clienteParticipaEnCategoria(
                        any(Connection.class),
                        anyLong(),
                        anyLong(),
                        anyLong());
        verify(conexion).rollback();
    }

    @Test
    void rechazaClienteEnOtraParejaDeLaCategoria() throws Exception {
        TorneoInscripcionJugador responsable = jugador(10L, 1, true);
        TorneoInscripcionJugador pareja = jugador(11L, 2, false);
        TorneoInscripcion inscripcion = inscripcion(responsable, pareja);

        when(jugadorDAO.buscar(conexion, 11L)).thenReturn(pareja);
        when(clienteDAO.buscar(conexion, 50L))
                .thenReturn(cliente(50L, true));
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);
        when(inscripcionDAO.clienteParticipaEnCategoria(
                conexion, 7L, 50L, 20L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.vincularManualmente(11L, 50L));

        verify(jugadorDAO, never()).guardar(
                any(Connection.class),
                any(TorneoInscripcionJugador.class));
        verify(conexion).rollback();
    }

    @Test
    void desvinculaYReseteaElResponsable() throws Exception {
        TorneoInscripcionJugador responsable = jugador(10L, 1, true);
        responsable.setClienteId(50L);
        responsable.setTipoVinculacion(
                TipoVinculacionTorneo.MANUAL);
        responsable.setRequiereRevision(true);
        TorneoInscripcionJugador pareja = jugador(11L, 2, false);
        TorneoInscripcion inscripcion = inscripcion(responsable, pareja);
        inscripcion.setResponsableClienteId(50L);

        when(jugadorDAO.buscar(conexion, 10L)).thenReturn(responsable);
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);

        TorneoInscripcionJugador resultado = servicio.desvincular(10L);

        assertNull(resultado.getClienteId());
        assertEquals(TipoVinculacionTorneo.SIN_VINCULAR,
                resultado.getTipoVinculacion());
        assertFalse(resultado.isRequiereRevision());
        assertNull(inscripcion.getResponsableClienteId());
        verify(jugadorDAO).guardar(conexion, responsable);
        verify(inscripcionDAO).guardar(conexion, inscripcion);
        verify(conexion).commit();
    }

    private TorneoInscripcionJugador jugador(
            long id,
            int orden,
            boolean responsable) {
        TorneoInscripcionJugador jugador =
                new TorneoInscripcionJugador();
        jugador.setId(id);
        jugador.setInscripcionId(20L);
        jugador.setOrdenIntegrante(orden);
        jugador.setResponsable(responsable);
        jugador.setNombre("Jugador");
        jugador.setApellido(String.valueOf(orden));
        jugador.setTelefono("1123456789");
        jugador.setTelefonoNormalizado("1123456789");
        return jugador;
    }

    private TorneoInscripcion inscripcion(
            TorneoInscripcionJugador responsable,
            TorneoInscripcionJugador pareja) {
        TorneoInscripcion inscripcion = new TorneoInscripcion();
        inscripcion.setId(20L);
        inscripcion.setTorneoCategoriaId(7L);
        inscripcion.setJugadores(List.of(responsable, pareja));
        return inscripcion;
    }

    private Cliente cliente(long id, boolean activo) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre("Cliente");
        cliente.setApellido("Prueba");
        cliente.setActivo(activo);
        return cliente;
    }
}