package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.CorreccionResultadoTorneoDAO;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoEnlaceDAO;
import dao.TorneoPartidoSetDAO;
import negocio.EstadoPartidoTorneo;
import negocio.FaseTorneo;
import negocio.PosicionPartidoSiguiente;
import negocio.TipoSetTorneo;
import negocio.TorneoPartido;
import negocio.TorneoPartidoSet;

class ResultadoPartidoTorneoServiceAvanceTest {
    private final TorneoPartidoDAO partidoDAO = mock(TorneoPartidoDAO.class);
    private final TorneoPartidoSetDAO setDAO = mock(TorneoPartidoSetDAO.class);
    private final TorneoPartidoEnlaceDAO enlaceDAO =
            mock(TorneoPartidoEnlaceDAO.class);
    private final CorreccionResultadoTorneoDAO correccionDAO =
            mock(CorreccionResultadoTorneoDAO.class);
    private final Connection conexion = mock(Connection.class);
    private ResultadoPartidoTorneoService service;

    @BeforeEach
    void configurar() throws Exception {
        when(enlaceDAO.listarPorOrigen(any(), any(Long.class)))
                .thenReturn(List.of());
        service = new ResultadoPartidoTorneoService(partidoDAO, setDAO,
                enlaceDAO, correccionDAO, () -> conexion, partido -> { });
    }

    @Test
    void avanzaGanadoraAParejaUno() throws Exception {
        TorneoPartido origen = origen(10, PosicionPartidoSiguiente.PAREJA_1);
        TorneoPartido destino = destino();
        when(partidoDAO.buscarParaActualizar(conexion, 10)).thenReturn(origen);
        when(partidoDAO.buscarParaActualizar(conexion, 20)).thenReturn(destino);
        when(partidoDAO.buscar(10)).thenReturn(origen);

        service.registrarResultado(10, ganaParejaUno(), 7, null);

        assertEquals(101L, destino.getPareja1InscripcionId());
        verify(partidoDAO).guardar(conexion, destino);
        verify(conexion).commit();
        verify(conexion, never()).rollback();
    }

    @Test
    void avanzaGanadoraAParejaDosDesdeFasePrevia() throws Exception {
        TorneoPartido origen = origen(10, PosicionPartidoSiguiente.PAREJA_2);
        origen.setFase(FaseTorneo.ACCESO_1);
        TorneoPartido destino = destino();
        when(partidoDAO.buscarParaActualizar(conexion, 10)).thenReturn(origen);
        when(partidoDAO.buscarParaActualizar(conexion, 20)).thenReturn(destino);
        when(partidoDAO.buscar(10)).thenReturn(origen);

        service.registrarResultado(10, ganaParejaUno(), 7, null);

        assertEquals(101L, destino.getPareja2InscripcionId());
        verify(conexion).commit();
    }

    @Test
    void rechazaDestinoOcupadoYHaceRollback() throws Exception {
        TorneoPartido origen = origen(10, PosicionPartidoSiguiente.PAREJA_1);
        TorneoPartido destino = destino();
        destino.setPareja1InscripcionId(999L);
        when(partidoDAO.buscarParaActualizar(conexion, 10)).thenReturn(origen);
        when(partidoDAO.buscarParaActualizar(conexion, 20)).thenReturn(destino);

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarResultado(
                        10, ganaParejaUno(), 7, null));

        verify(conexion).rollback();
        verify(conexion, never()).commit();
    }

    private TorneoPartido origen(long id,
            PosicionPartidoSiguiente posicion) {
        TorneoPartido partido = new TorneoPartido();
        partido.setId(id);
        partido.setTorneoCategoriaId(5);
        partido.setFase(FaseTorneo.SEMIFINAL);
        partido.setOrdenFase(1);
        partido.setPareja1InscripcionId(101L);
        partido.setPareja2InscripcionId(102L);
        partido.setEstado(EstadoPartidoTorneo.EN_CURSO);
        partido.setPartidoSiguienteId(20L);
        partido.setPosicionSiguiente(posicion);
        return partido;
    }

    private TorneoPartido destino() {
        TorneoPartido partido = new TorneoPartido();
        partido.setId(20);
        partido.setTorneoCategoriaId(5);
        partido.setFase(FaseTorneo.FINAL);
        partido.setOrdenFase(1);
        partido.setEstado(EstadoPartidoTorneo.PENDIENTE);
        return partido;
    }

    private List<TorneoPartidoSet> ganaParejaUno() {
        return List.of(set(1, 6, 2), set(2, 6, 3));
    }

    private TorneoPartidoSet set(int numero, int uno, int dos) {
        TorneoPartidoSet set = new TorneoPartidoSet();
        set.setNumeroSet(numero);
        set.setTipo(TipoSetTorneo.NORMAL);
        set.setPuntosPareja1(uno);
        set.setPuntosPareja2(dos);
        return set;
    }
}
