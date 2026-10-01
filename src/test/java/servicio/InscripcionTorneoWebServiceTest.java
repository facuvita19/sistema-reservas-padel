package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import dao.TorneoCategoriaDAO;
import dao.TorneoDAO;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionJugadorDAO;
import negocio.Cliente;
import negocio.EstadoInscripcionTorneo;
import negocio.EstadoTorneo;
import negocio.OrigenInscripcionTorneo;
import negocio.RamaTorneo;
import negocio.TipoVinculacionTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoInscripcionJugador;
import servicio.InscripcionTorneoWebService.DatosJugador;
import servicio.InscripcionTorneoWebService.SolicitudInscripcion;
import servicio.VinculacionClienteTelefonoService.ResultadoVinculacion;

class InscripcionTorneoWebServiceTest {

    private TorneoDAO torneoDAO;
    private TorneoCategoriaDAO categoriaDAO;
    private TorneoInscripcionDAO inscripcionDAO;
    private TorneoInscripcionJugadorDAO jugadorDAO;
    private VinculacionClienteTelefonoService vinculacionService;
    private Connection conexion;
    private InscripcionTorneoWebService servicio;

    @BeforeEach
    void preparar() throws Exception {
        torneoDAO = mock(TorneoDAO.class);
        categoriaDAO = mock(TorneoCategoriaDAO.class);
        inscripcionDAO = mock(TorneoInscripcionDAO.class);
        jugadorDAO = mock(TorneoInscripcionJugadorDAO.class);
        vinculacionService = mock(VinculacionClienteTelefonoService.class);
        conexion = mock(Connection.class);
        servicio = new InscripcionTorneoWebService(
                torneoDAO,
                categoriaDAO,
                inscripcionDAO,
                jugadorDAO,
                vinculacionService,
                () -> conexion);
        doNothing().when(conexion).setAutoCommit(false);
        doNothing().when(conexion).setAutoCommit(true);
        doNothing().when(conexion).commit();
        doNothing().when(conexion).rollback();
    }

    @Test
    void creaSolicitudPendienteConDosIntegrantes() throws Exception {
        prepararEscenarioValido();
        Cliente responsable = cliente(10L, "Ana", "Perez");
        when(vinculacionService.buscar(conexion, "+54 9 11 5555-1111"))
                .thenReturn(ResultadoVinculacion.encontrado(
                        "5491155551111", responsable));
        when(vinculacionService.buscar(conexion, "+54 9 11 5555-2222"))
                .thenReturn(ResultadoVinculacion.sinCoincidencias(
                        "5491155552222"));
        when(inscripcionDAO.clienteParticipaEnCategoria(
                conexion, 7L, 10L, null)).thenReturn(false);
        doAnswer(invocacion -> {
            TorneoInscripcion inscripcion = invocacion.getArgument(1);
            inscripcion.setId(90L);
            return null;
        }).when(inscripcionDAO).guardar(
                any(Connection.class), any(TorneoInscripcion.class));

        TorneoInscripcion resultado = servicio.solicitar(solicitudValida());

        assertEquals(90L, resultado.getId());
        assertEquals(EstadoInscripcionTorneo.PENDIENTE,
                resultado.getEstado());
        assertEquals(OrigenInscripcionTorneo.WEB, resultado.getOrigen());
        assertEquals(new BigDecimal("24000.00"),
                resultado.getPrecioInscripcion());
        assertEquals(2, resultado.getJugadores().size());
        assertTrue(resultado.tieneParejaCompleta());
        assertEquals(10L, resultado.getResponsableClienteId());
        verify(jugadorDAO, times(2)).guardar(
                any(Connection.class), any(TorneoInscripcionJugador.class));
        verify(conexion).commit();
        verify(conexion, never()).rollback();
    }

    @Test
    void copiaVinculacionYRevisionEnLosJugadores() throws Exception {
        prepararEscenarioValido();
        Cliente responsable = cliente(10L, "Ana", "Perez");
        when(vinculacionService.buscar(conexion, "+54 9 11 5555-1111"))
                .thenReturn(ResultadoVinculacion.encontrado(
                        "5491155551111", responsable));
        when(vinculacionService.buscar(conexion, "+54 9 11 5555-2222"))
                .thenReturn(ResultadoVinculacion.ambigua(
                        "5491155552222", 2));
        when(inscripcionDAO.clienteParticipaEnCategoria(
                conexion, 7L, 10L, null)).thenReturn(false);
        doAnswer(invocacion -> {
            ((TorneoInscripcion) invocacion.getArgument(1)).setId(91L);
            return null;
        }).when(inscripcionDAO).guardar(
                any(Connection.class), any(TorneoInscripcion.class));

        servicio.solicitar(solicitudValida());

        ArgumentCaptor<TorneoInscripcionJugador> captor =
                ArgumentCaptor.forClass(TorneoInscripcionJugador.class);
        verify(jugadorDAO, times(2)).guardar(
                any(Connection.class), captor.capture());
        List<TorneoInscripcionJugador> jugadores = captor.getAllValues();
        assertEquals(TipoVinculacionTorneo.AUTOMATICA,
                jugadores.get(0).getTipoVinculacion());
        assertEquals(10L, jugadores.get(0).getClienteId());
        assertEquals(TipoVinculacionTorneo.SIN_VINCULAR,
                jugadores.get(1).getTipoVinculacion());
        assertTrue(jugadores.get(1).isRequiereRevision());
    }

    @Test
    void rechazaTelefonosIgualesAntesDeAbrirTransaccion()
            throws Exception {
        SolicitudInscripcion solicitud = new SolicitudInscripcion(
                7L,
                new DatosJugador("Ana", "Perez", "11 5555-1111"),
                new DatosJugador("Maria", "Lopez", "11 5555-1111"),
                null);

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.solicitar(solicitud));

        verify(conexion, never()).setAutoCommit(false);
    }

    @Test
    void rechazaClienteYaInscriptoYRevierte() throws Exception {
        prepararEscenarioValido();
        Cliente responsable = cliente(10L, "Ana", "Perez");
        when(vinculacionService.buscar(conexion, "+54 9 11 5555-1111"))
                .thenReturn(ResultadoVinculacion.encontrado(
                        "5491155551111", responsable));
        when(vinculacionService.buscar(conexion, "+54 9 11 5555-2222"))
                .thenReturn(ResultadoVinculacion.sinCoincidencias(
                        "5491155552222"));
        when(inscripcionDAO.clienteParticipaEnCategoria(
                conexion, 7L, 10L, null)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.solicitar(solicitudValida()));

        verify(conexion).rollback();
        verify(inscripcionDAO, never()).guardar(
                any(Connection.class), any(TorneoInscripcion.class));
    }

    @Test
    void revierteSiFallaElSegundoIntegrante() throws Exception {
        prepararEscenarioValido();
        when(vinculacionService.buscar(conexion, "+54 9 11 5555-1111"))
                .thenReturn(ResultadoVinculacion.sinCoincidencias(
                        "5491155551111"));
        when(vinculacionService.buscar(conexion, "+54 9 11 5555-2222"))
                .thenReturn(ResultadoVinculacion.sinCoincidencias(
                        "5491155552222"));
        doAnswer(invocacion -> {
            ((TorneoInscripcion) invocacion.getArgument(1)).setId(92L);
            return null;
        }).when(inscripcionDAO).guardar(
                any(Connection.class), any(TorneoInscripcion.class));
        doNothing()
                .doThrow(new RuntimeException("Fallo controlado"))
                .when(jugadorDAO).guardar(
                        any(Connection.class),
                        any(TorneoInscripcionJugador.class));

        assertThrows(
                RuntimeException.class,
                () -> servicio.solicitar(solicitudValida()));

        verify(jugadorDAO, times(2)).guardar(
                any(Connection.class), any(TorneoInscripcionJugador.class));
        verify(conexion).rollback();
        verify(conexion, never()).commit();
    }

    private void prepararEscenarioValido() {
        TorneoCategoria categoria = new TorneoCategoria();
        categoria.setId(7L);
        categoria.setTorneoId(3L);
        categoria.setNombre("6ta");
        categoria.setRama(RamaTorneo.MASCULINA);
        categoria.setCupoParejas(16);
        categoria.setPrecioInscripcion(new BigDecimal("24000.00"));
        categoria.setActivo(true);
        when(categoriaDAO.buscar(conexion, 7L)).thenReturn(categoria);

        Torneo torneo = new Torneo();
        torneo.setId(3L);
        torneo.setNombre("Copa Primavera");
        torneo.setFechaInicio(LocalDate.now().plusDays(10));
        torneo.setFechaFin(LocalDate.now().plusDays(12));
        torneo.setInscripcionDesde(LocalDateTime.now().minusDays(2));
        torneo.setInscripcionHasta(LocalDateTime.now().plusDays(5));
        torneo.setEstado(EstadoTorneo.INSCRIPCION_ABIERTA);
        torneo.setActivo(true);
        when(torneoDAO.buscar(conexion, 3L)).thenReturn(torneo);
    }

    private SolicitudInscripcion solicitudValida() {
        return new SolicitudInscripcion(
                7L,
                new DatosJugador(
                        "Ana", "Perez", "+54 9 11 5555-1111"),
                new DatosJugador(
                        "Maria", "Lopez", "+54 9 11 5555-2222"),
                "Disponibles por la tarde");
    }

    private Cliente cliente(long id, String nombre, String apellido) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setTelefono("+54 9 11 5555-1111");
        cliente.setActivo(true);
        return cliente;
    }
}
