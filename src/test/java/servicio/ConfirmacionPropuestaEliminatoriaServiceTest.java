package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.TorneoPartidoDAO;
import negocio.ClasificadoEtapaEliminatoria;
import negocio.CrucePropuestoTorneo;
import negocio.FaseTorneo;
import negocio.PosicionPartidoSiguiente;
import negocio.PropuestaEtapaEliminatoria;
import negocio.TorneoPartido;

class ConfirmacionPropuestaEliminatoriaServiceTest {
    private final TorneoPartidoDAO dao = mock(TorneoPartidoDAO.class);
    private final ValidadorEstructuraEliminatoriaService validador =
            mock(ValidadorEstructuraEliminatoriaService.class);
    private final Connection conexion = mock(Connection.class);
    private final PreparedStatement sentencia = mock(PreparedStatement.class);
    private final ResultSet resultado = mock(ResultSet.class);
    private final List<TorneoPartido> guardados = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(100);
    private ConfirmacionPropuestaEliminatoriaService service;

    @BeforeEach
    void configurar() throws Exception {
        when(conexion.prepareStatement(any(String.class)))
                .thenReturn(sentencia);
        when(sentencia.executeQuery()).thenReturn(resultado);
        when(resultado.next()).thenReturn(true);
        when(resultado.getInt(1)).thenReturn(0);
        when(validador.validar(any(), any())).thenReturn(List.of());
        when(dao.listarPorCategoria(77L)).thenAnswer(x ->
                new ArrayList<>(guardados));
        doAnswer(invocacion -> {
            TorneoPartido partido = invocacion.getArgument(1);
            if (partido.getId() <= 0) {
                partido.setId(secuencia.incrementAndGet());
                guardados.add(partido);
            }
            return null;
        }).when(dao).guardar(same(conexion), any(TorneoPartido.class));
        service = new ConfirmacionPropuestaEliminatoriaService(
                dao, validador, () -> conexion);
    }

    @Test
    void creaCuadroYConectaAmbasPosiciones() throws Exception {
        PropuestaEtapaEliminatoria propuesta = propuestaCompleta();

        List<TorneoPartido> creados = service.confirmar(77L, propuesta);

        assertEquals(3, creados.size());
        TorneoPartido semifinal1 = buscar(creados, FaseTorneo.SEMIFINAL, 1);
        TorneoPartido semifinal2 = buscar(creados, FaseTorneo.SEMIFINAL, 2);
        TorneoPartido finalPartido = buscar(creados, FaseTorneo.FINAL, 1);
        assertEquals(finalPartido.getId(),
                semifinal1.getPartidoSiguienteId());
        assertEquals(PosicionPartidoSiguiente.PAREJA_1,
                semifinal1.getPosicionSiguiente());
        assertEquals(finalPartido.getId(),
                semifinal2.getPartidoSiguienteId());
        assertEquals(PosicionPartidoSiguiente.PAREJA_2,
                semifinal2.getPosicionSiguiente());
        assertNull(finalPartido.getPareja1InscripcionId());
        assertNull(finalPartido.getPareja2InscripcionId());
        verify(conexion).commit();
        verify(conexion, never()).rollback();
    }

    @Test
    void convierteFasePreviaEnAccesoUno() throws Exception {
        PropuestaEtapaEliminatoria propuesta = propuestaCompleta();
        propuesta.setCruces(List.of(
                new CrucePropuestoTorneo("Acceso R1", 1, 1,
                        "1° A", "2° B"),
                new CrucePropuestoTorneo("Final", 2, 1,
                        "Ganador Acceso R1 #1", "1° B")));

        List<TorneoPartido> creados = service.confirmar(77L, propuesta);

        TorneoPartido acceso = buscar(creados, FaseTorneo.ACCESO_1, 1);
        TorneoPartido finalPartido = buscar(creados, FaseTorneo.FINAL, 1);
        assertEquals(finalPartido.getId(), acceso.getPartidoSiguienteId());
        assertEquals(PosicionPartidoSiguiente.PAREJA_1,
                acceso.getPosicionSiguiente());
    }

    @Test
    void haceRollbackSiFallaLaPersistencia() throws Exception {
        doAnswer(invocacion -> {
            throw new RuntimeException("fallo simulado");
        }).when(dao).guardar(same(conexion), any(TorneoPartido.class));

        assertThrows(RuntimeException.class,
                () -> service.confirmar(77L, propuestaCompleta()));

        verify(conexion).rollback();
        verify(conexion, never()).commit();
    }

    @Test
    void rechazaCategoriaConCuadroExistente() throws Exception {
        when(resultado.getInt(1)).thenReturn(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.confirmar(77L, propuestaCompleta()));

        verify(dao, never()).guardar(
                same(conexion), any(TorneoPartido.class));
        verify(conexion).rollback();
    }

    private PropuestaEtapaEliminatoria propuestaCompleta() {
        PropuestaEtapaEliminatoria propuesta = new PropuestaEtapaEliminatoria();
        propuesta.setValida(true);
        propuesta.setClasificados(List.of(
                clasificado(11, "A", 1), clasificado(12, "A", 2),
                clasificado(21, "B", 1), clasificado(22, "B", 2)));
        propuesta.setCruces(List.of(
                new CrucePropuestoTorneo("Semifinal", 1, 1,
                        "1° A", "2° B"),
                new CrucePropuestoTorneo("Semifinal", 1, 2,
                        "1° B", "2° A"),
                new CrucePropuestoTorneo("Final", 2, 1,
                        "Ganador Semifinal #1",
                        "Ganador Semifinal #2")));
        return propuesta;
    }

    private ClasificadoEtapaEliminatoria clasificado(
            long id, String grupo, int posicion) {
        return new ClasificadoEtapaEliminatoria(id, id + 100,
                grupo, posicion, 0, 0, 0, 0, 0);
    }

    private TorneoPartido buscar(List<TorneoPartido> partidos,
            FaseTorneo fase, int orden) {
        return partidos.stream()
                .filter(p -> p.getFase() == fase
                        && p.getOrdenFase() == orden)
                .findFirst().orElseThrow();
    }
}
