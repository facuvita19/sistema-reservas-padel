package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import dao.TorneoCategoriaDAO;
import dao.TorneoInscripcionDAO;
import negocio.EstadoInscripcionTorneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import servicio.GestionInscripcionTorneoService.CupoTorneoCompletoException;

class GestionInscripcionTorneoServiceTest {

    private TorneoInscripcionDAO inscripcionDAO;
    private TorneoCategoriaDAO categoriaDAO;
    private Connection conexion;
    private GestionInscripcionTorneoService servicio;

    @BeforeEach
    void preparar() throws Exception {
        inscripcionDAO = mock(TorneoInscripcionDAO.class);
        categoriaDAO = mock(TorneoCategoriaDAO.class);
        conexion = mock(Connection.class);
        servicio = new GestionInscripcionTorneoService(
                inscripcionDAO,
                categoriaDAO,
                () -> conexion);
    }

    @Test
    void confirmaCuandoHayCupo() throws Exception {
        TorneoInscripcion inscripcion = inscripcionPendiente();
        prepararConfirmacion(inscripcion, 3, 16);

        TorneoInscripcion resultado = servicio.confirmar(
                20L, 5L, "  Datos verificados  ");

        assertEquals(EstadoInscripcionTorneo.CONFIRMADA,
                resultado.getEstado());
        assertEquals(5L, resultado.getUsuarioGestionId());
        assertEquals("Datos verificados",
                resultado.getObservacionesAdministrativas());
        assertNotNull(resultado.getFechaConfirmacion());
        assertNull(resultado.getFechaCancelacion());
        verify(inscripcionDAO).guardar(conexion, inscripcion);
        verify(conexion).commit();
        verify(conexion, never()).rollback();
    }

    @Test
    void rechazaConfirmacionSinCupoYRevierte() throws Exception {
        TorneoInscripcion inscripcion = inscripcionPendiente();
        prepararConfirmacion(inscripcion, 16, 16);

        assertThrows(
                CupoTorneoCompletoException.class,
                () -> servicio.confirmar(20L, 5L, null));

        verify(inscripcionDAO, never()).guardar(
                any(Connection.class), any(TorneoInscripcion.class));
        verify(conexion).rollback();
        verify(conexion, never()).commit();
    }

    @Test
    void enviaPendienteAListaDeEspera() throws Exception {
        TorneoInscripcion inscripcion = inscripcionPendiente();
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);

        TorneoInscripcion resultado = servicio.enviarAListaEspera(
                20L, 5L, "Sin cupo por el momento");

        assertEquals(EstadoInscripcionTorneo.LISTA_ESPERA,
                resultado.getEstado());
        assertNull(resultado.getFechaConfirmacion());
        verify(inscripcionDAO).guardar(conexion, inscripcion);
        verify(categoriaDAO, never()).buscar(
                any(Connection.class), anyLong());
        verify(conexion).commit();
    }

    @Test
    void rechazaSolicitudPendiente() throws Exception {
        TorneoInscripcion inscripcion = inscripcionPendiente();
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);

        TorneoInscripcion resultado = servicio.rechazar(
                20L, 5L, "Datos incompletos");

        assertEquals(EstadoInscripcionTorneo.RECHAZADA,
                resultado.getEstado());
        assertEquals("Datos incompletos",
                resultado.getObservacionesAdministrativas());
        verify(conexion).commit();
    }

    @Test
    void cancelarConfirmadaLiberaElCupoPorEstado() throws Exception {
        TorneoInscripcion inscripcion = inscripcionPendiente();
        inscripcion.setEstado(EstadoInscripcionTorneo.CONFIRMADA);
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);

        TorneoInscripcion resultado = servicio.cancelar(
                20L, 5L, "La pareja no participa");

        assertEquals(EstadoInscripcionTorneo.CANCELADA,
                resultado.getEstado());
        assertNotNull(resultado.getFechaCancelacion());
        assertNull(resultado.getFechaConfirmacion());
        verify(inscripcionDAO).guardar(conexion, inscripcion);
        verify(conexion).commit();
    }

    @Test
    void rechazaCambiosDesdeUnEstadoFinal() throws Exception {
        TorneoInscripcion inscripcion = inscripcionPendiente();
        inscripcion.setEstado(EstadoInscripcionTorneo.RECHAZADA);
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.confirmar(20L, 5L, null));

        verify(conexion).rollback();
        verify(inscripcionDAO, never()).guardar(
                any(Connection.class), any(TorneoInscripcion.class));
    }

    @Test
    void rechazaParejaIncompleta() throws Exception {
        TorneoInscripcion inscripcion = inscripcionPendiente();
        inscripcion.setJugadores(List.of(inscripcion.getJugadores().get(0)));
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.confirmar(20L, 5L, null));

        verify(conexion).rollback();
        verify(categoriaDAO, never()).buscar(
                any(Connection.class), anyLong());
    }

    @Test
    void rechazaClienteVinculadoEnOtraPareja() throws Exception {
        TorneoInscripcion inscripcion = inscripcionPendiente();
        prepararConfirmacion(inscripcion, 2, 16);
        when(inscripcionDAO.clienteParticipaEnCategoria(
                conexion, 7L, 100L, 20L)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.confirmar(20L, 5L, null));

        verify(conexion).rollback();
        verify(inscripcionDAO, never()).guardar(
                any(Connection.class), any(TorneoInscripcion.class));
    }

    private void prepararConfirmacion(
            TorneoInscripcion inscripcion,
            int confirmadas,
            int cupo) {
        when(inscripcionDAO.buscar(conexion, 20L))
                .thenReturn(inscripcion);
        TorneoCategoria categoria = new TorneoCategoria();
        categoria.setId(7L);
        categoria.setTorneoId(3L);
        categoria.setNombre("6ta");
        categoria.setCupoParejas(cupo);
        categoria.setActivo(true);
        when(categoriaDAO.buscar(conexion, 7L))
                .thenReturn(categoria);
        when(inscripcionDAO.contarPorCategoriaYEstados(
                conexion,
                7L,
                List.of(EstadoInscripcionTorneo.CONFIRMADA)))
                .thenReturn(confirmadas);
        when(inscripcionDAO.clienteParticipaEnCategoria(
                conexion, 7L, 100L, 20L)).thenReturn(false);
    }

    private TorneoInscripcion inscripcionPendiente() {
        TorneoInscripcion inscripcion = new TorneoInscripcion();
        inscripcion.setId(20L);
        inscripcion.setTorneoCategoriaId(7L);
        inscripcion.setEstado(EstadoInscripcionTorneo.PENDIENTE);

        TorneoInscripcionJugador responsable =
                new TorneoInscripcionJugador();
        responsable.setOrdenIntegrante(1);
        responsable.setResponsable(true);
        responsable.setClienteId(100L);

        TorneoInscripcionJugador pareja =
                new TorneoInscripcionJugador();
        pareja.setOrdenIntegrante(2);
        pareja.setResponsable(false);

        inscripcion.setJugadores(List.of(responsable, pareja));
        return inscripcion;
    }
}
