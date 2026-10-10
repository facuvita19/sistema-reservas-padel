package api.publica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import dao.TorneoCatalogoPublicoDAO;
import dao.TorneoCategoriaDAO;
import dao.TorneoDAO;

class TorneoPublicoServiceTest {
    @Test void construyeCatalogoAgregadoSinConsultarDetalles(){
        TorneoDAO torneos=mock(TorneoDAO.class);TorneoCategoriaDAO categorias=mock(TorneoCategoriaDAO.class);TorneoCatalogoPublicoDAO catalogo=mock(TorneoCatalogoPublicoDAO.class);
        var fila=new TorneoCatalogoPublicoDAO.Resumen(1,"Copa Premium","Descripcion",LocalDate.now().plusDays(10),LocalDate.now().plusDays(12),LocalDateTime.now().minusDays(1),LocalDateTime.now().plusDays(5),"INSCRIPCION_ABIERTA",2,2,32,10,22,new BigDecimal("20000"),new BigDecimal("25000"),List.of("4ta","5ta"),List.of("MASCULINA"),List.of("GRUPOS_ELIMINACION"),0,0,0,false,false,false);
        when(catalogo.listar()).thenReturn(List.of(fila));var r=new TorneoPublicoService(torneos,categorias,catalogo).listar().get(0);
        assertEquals("PROXIMO",r.estadoPortal());assertEquals("INSCRIPCION",r.etapaCompetitiva());assertTrue(r.inscripcionDisponible());assertEquals(22,r.cuposDisponibles());assertFalse(r.resultadosDisponibles());
    }
    @Test void clasificaEnCursoYFinalizado(){
        assertEquals("EN_CURSO",TorneoPublicoService.estadoPortal("EN_CURSO"));assertEquals("FINALIZADO",TorneoPublicoService.estadoPortal("FINALIZADO"));assertEquals("PROXIMO",TorneoPublicoService.estadoPortal("PUBLICADO"));
    }
}
