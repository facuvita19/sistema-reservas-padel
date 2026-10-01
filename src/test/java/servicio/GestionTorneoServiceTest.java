package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.TorneoCategoriaDAO;
import dao.TorneoDAO;
import dao.TorneoInscripcionDAO;
import dao.TorneoPartidoDAO;
import negocio.EstadoInscripcionTorneo;
import negocio.EstadoTorneo;
import negocio.EstadoPartidoTorneo;
import negocio.FaseTorneo;
import negocio.TorneoPartido;
import negocio.RamaTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;

class GestionTorneoServiceTest {

    private TorneoDAO torneoDAO;
    private TorneoCategoriaDAO categoriaDAO;
    private TorneoInscripcionDAO inscripcionDAO;
    private TorneoPartidoDAO partidoDAO;
    private Connection conexion;
    private GestionTorneoService servicio;

    @BeforeEach
    void preparar() {
        torneoDAO = mock(TorneoDAO.class);
        categoriaDAO = mock(TorneoCategoriaDAO.class);
        inscripcionDAO = mock(TorneoInscripcionDAO.class);
        partidoDAO = mock(TorneoPartidoDAO.class);
        conexion = mock(Connection.class);
        servicio = new GestionTorneoService(
                torneoDAO,
                categoriaDAO,
                inscripcionDAO,
                partidoDAO,
                () -> conexion);
    }

    @Test
    void creaTorneoComoBorrador() throws Exception {
        Torneo torneo = torneoValido();

        Torneo resultado = servicio.crearTorneo(torneo, 7L);

        assertEquals(EstadoTorneo.BORRADOR, resultado.getEstado());
        assertEquals(7L, resultado.getUsuarioCreacionId());
        verify(torneoDAO).guardar(conexion, torneo);
        verify(conexion).commit();
        verify(conexion, never()).rollback();
    }

    @Test
    void editaTorneoYConservaCreadorYEstado() throws Exception {
        Torneo existente = torneoValido();
        existente.setId(10L);
        existente.setEstado(EstadoTorneo.PUBLICADO);
        existente.setUsuarioCreacionId(3L);
        Torneo cambios = torneoValido();
        cambios.setId(10L);
        cambios.setNombre("Torneo actualizado");
        cambios.setUsuarioCreacionId(99L);
        cambios.setEstado(EstadoTorneo.CANCELADO);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(existente);

        Torneo resultado = servicio.editarTorneo(cambios);

        assertEquals("Torneo actualizado", resultado.getNombre());
        assertEquals(3L, resultado.getUsuarioCreacionId());
        assertEquals(EstadoTorneo.PUBLICADO, resultado.getEstado());
        verify(torneoDAO).guardar(conexion, existente);
        verify(conexion).commit();
    }

    @Test
    void rechazaEdicionDeTorneoFinal() throws Exception {
        Torneo existente = torneoValido();
        existente.setId(10L);
        existente.setEstado(EstadoTorneo.FINALIZADO);
        Torneo cambios = torneoValido();
        cambios.setId(10L);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(existente);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.editarTorneo(cambios));

        verify(torneoDAO, never()).guardar(
                any(Connection.class), any(Torneo.class));
        verify(conexion).rollback();
    }

    @Test
    void publicaUnTorneoBorrador() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);

        Torneo resultado = servicio.publicar(10L);

        assertEquals(EstadoTorneo.PUBLICADO, resultado.getEstado());
        verify(torneoDAO).guardar(conexion, torneo);
        verify(conexion).commit();
    }

    @Test
    void abreInscripcionesConCategoriaActiva() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        torneo.setEstado(EstadoTorneo.PUBLICADO);
        when(categoriaDAO.listarActivasPorTorneo(10L))
                .thenReturn(List.of(categoriaValida()));
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);

        Torneo resultado = servicio.abrirInscripciones(10L);

        assertEquals(EstadoTorneo.INSCRIPCION_ABIERTA,
                resultado.getEstado());
        verify(torneoDAO).guardar(conexion, torneo);
        verify(conexion).commit();
    }

    @Test
    void rechazaAbrirSinCategoriasActivas() throws Exception {
        when(categoriaDAO.listarActivasPorTorneo(10L))
                .thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> servicio.abrirInscripciones(10L));

        verify(torneoDAO, never()).buscar(
                any(Connection.class), anyLong());
    }

    @Test
    void completaElCicloDeEstadosPermitido() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        torneo.setEstado(EstadoTorneo.INSCRIPCION_ABIERTA);
        TorneoCategoria categoria = categoriaValida();
        categoria.setId(20L);
        categoria.setTorneoId(10L);
        categoria.setParejasConfirmadas(2);
        TorneoPartido finalPartido = new TorneoPartido();
        finalPartido.setId(30L);
        finalPartido.setTorneoCategoriaId(20L);
        finalPartido.setFase(FaseTorneo.FINAL);
        finalPartido.setEstado(EstadoPartidoTorneo.PENDIENTE);

        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);
        when(categoriaDAO.listarActivasPorTorneo(10L))
                .thenReturn(List.of(categoria));
        when(partidoDAO.listarPorCategoria(20L))
                .thenReturn(List.of(finalPartido));

        assertEquals(EstadoTorneo.INSCRIPCION_CERRADA,
                servicio.cerrarInscripciones(10L).getEstado());
        assertEquals(EstadoTorneo.EN_CURSO,
                servicio.iniciar(10L).getEstado());

        finalPartido.setEstado(EstadoPartidoTorneo.FINALIZADO);
        finalPartido.setGanadoraInscripcionId(100L);

        assertEquals(EstadoTorneo.FINALIZADO,
                servicio.finalizar(10L).getEstado());
    }

    @Test
    void rechazaUnaTransicionNoPermitida() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        torneo.setEstado(EstadoTorneo.BORRADOR);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.iniciar(10L));

        verify(torneoDAO, never()).guardar(
                any(Connection.class), any(Torneo.class));
        verify(conexion).rollback();
    }

    @Test
    void cancelaUnTorneoNoFinal() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        torneo.setEstado(EstadoTorneo.INSCRIPCION_ABIERTA);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);

        Torneo resultado = servicio.cancelar(10L);

        assertEquals(EstadoTorneo.CANCELADO, resultado.getEstado());
        verify(torneoDAO).guardar(conexion, torneo);
        verify(conexion).commit();
    }

    @Test
    void creaCategoriaEnTorneoActivo() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        TorneoCategoria categoria = categoriaValida();
        categoria.setTorneoId(10L);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);

        TorneoCategoria resultado = servicio.crearCategoria(categoria);

        assertEquals(10L, resultado.getTorneoId());
        verify(categoriaDAO).guardar(conexion, categoria);
        verify(conexion).commit();
    }

    @Test
    void rechazaReducirCupoBajoConfirmadas() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        TorneoCategoria existente = categoriaValida();
        existente.setId(20L);
        existente.setTorneoId(10L);
        existente.setParejasConfirmadas(8);
        TorneoCategoria cambios = categoriaValida();
        cambios.setId(20L);
        cambios.setTorneoId(10L);
        cambios.setCupoParejas(7);
        when(categoriaDAO.buscar(conexion, 20L)).thenReturn(existente);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.editarCategoria(cambios));

        verify(categoriaDAO, never()).guardar(
                any(Connection.class), any(TorneoCategoria.class));
        verify(conexion).rollback();
    }

    @Test
    void rechazaTrasladarCategoriaAOtroTorneo() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        TorneoCategoria existente = categoriaValida();
        existente.setId(20L);
        existente.setTorneoId(10L);
        TorneoCategoria cambios = categoriaValida();
        cambios.setId(20L);
        cambios.setTorneoId(11L);
        when(categoriaDAO.buscar(conexion, 20L)).thenReturn(existente);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.editarCategoria(cambios));

        verify(categoriaDAO, never()).guardar(
                any(Connection.class), any(TorneoCategoria.class));
        verify(conexion).rollback();
    }

    @Test
    void desactivaCategoriaSinInscripcionesActivas() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        TorneoCategoria categoria = categoriaValida();
        categoria.setId(20L);
        categoria.setTorneoId(10L);
        when(categoriaDAO.buscar(conexion, 20L)).thenReturn(categoria);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);
        when(inscripcionDAO.contarPorCategoriaYEstados(
                conexion,
                20L,
                List.of(
                        EstadoInscripcionTorneo.PENDIENTE,
                        EstadoInscripcionTorneo.LISTA_ESPERA,
                        EstadoInscripcionTorneo.CONFIRMADA)))
                .thenReturn(0);

        TorneoCategoria resultado = servicio.desactivarCategoria(20L);

        assertFalse(resultado.isActivo());
        verify(categoriaDAO).guardar(conexion, categoria);
        verify(conexion).commit();
    }

    @Test
    void rechazaDesactivarCategoriaConInscripcionesActivas() throws Exception {
        Torneo torneo = torneoValido();
        torneo.setId(10L);
        TorneoCategoria categoria = categoriaValida();
        categoria.setId(20L);
        categoria.setTorneoId(10L);
        when(categoriaDAO.buscar(conexion, 20L)).thenReturn(categoria);
        when(torneoDAO.buscar(conexion, 10L)).thenReturn(torneo);
        when(inscripcionDAO.contarPorCategoriaYEstados(
                conexion,
                20L,
                List.of(
                        EstadoInscripcionTorneo.PENDIENTE,
                        EstadoInscripcionTorneo.LISTA_ESPERA,
                        EstadoInscripcionTorneo.CONFIRMADA)))
                .thenReturn(1);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.desactivarCategoria(20L));

        verify(categoriaDAO, never()).guardar(
                any(Connection.class), any(TorneoCategoria.class));
        verify(conexion).rollback();
    }

    private Torneo torneoValido() {
        Torneo torneo = new Torneo();
        torneo.setNombre("Copa de prueba");
        torneo.setDescripcion("Descripcion");
        torneo.setFechaInicio(LocalDate.of(2026, 11, 20));
        torneo.setFechaFin(LocalDate.of(2026, 11, 22));
        torneo.setInscripcionDesde(LocalDateTime.of(2026, 10, 1, 8, 0));
        torneo.setInscripcionHasta(LocalDateTime.of(2026, 11, 19, 23, 0));
        torneo.setReglamento("Reglamento");
        torneo.setActivo(true);
        torneo.setEstado(EstadoTorneo.BORRADOR);
        torneo.setUsuarioCreacionId(3L);
        return torneo;
    }

    private TorneoCategoria categoriaValida() {
        TorneoCategoria categoria = new TorneoCategoria();
        categoria.setNombre("6ta");
        categoria.setRama(RamaTorneo.MASCULINA);
        categoria.setCupoParejas(16);
        categoria.setPrecioInscripcion(new BigDecimal("24000"));
        categoria.setActivo(true);
        return categoria;
    }
}
