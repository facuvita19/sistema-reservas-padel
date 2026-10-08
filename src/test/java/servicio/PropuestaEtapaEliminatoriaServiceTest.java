package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import negocio.ClasificadoEtapaEliminatoria;
import negocio.CrucePropuestoTorneo;
import negocio.PropuestaEtapaEliminatoria;

class PropuestaEtapaEliminatoriaServiceTest {
    private final PropuestaEtapaEliminatoriaService service =
            new PropuestaEtapaEliminatoriaService();

    @Test
    void construyeEscenarioCincoPorTresSinFalsoPositivo() {
        PropuestaEtapaEliminatoria propuesta =
                service.construirDesdeClasificados(clasificadosCincoGrupos());

        assertTrue(propuesta.isValida());
        assertEquals(10, propuesta.getClasificados().size());
        assertEquals(5, propuesta.getPases().size());
        assertEquals(9, propuesta.getCruces().size());
        assertEquals(2, cantidadFase(propuesta, "Octavos"));
        assertEquals(4, cantidadFase(propuesta, "Cuartos"));
        assertEquals(2, cantidadFase(propuesta, "Semifinal"));
        assertEquals(1, cantidadFase(propuesta, "Final"));
        assertEquals(1, crucesEntrePrimeros(propuesta));
        assertTrue(service.advertenciasDeportivas(propuesta).isEmpty());
    }

    @Test
    void advierteCruceAdicionalEntrePrimerosCuandoEsEvitable() {
        PropuestaEtapaEliminatoria propuesta =
                service.construirDesdeClasificados(clasificadosCincoGrupos());
        List<CrucePropuestoTorneo> cruces = nuevaLista(propuesta);
        CrucePropuestoTorneo cuarto1 = buscar(cruces, "Cuartos", 1);
        CrucePropuestoTorneo cuarto2 = buscar(cruces, "Cuartos", 2);
        String rival1 = cuarto1.getParticipante2();
        cuarto1.setParticipante2(cuarto2.getParticipante1());
        cuarto2.setParticipante1(rival1);
        propuesta.setCruces(cruces);

        List<String> advertencias = service.advertenciasDeportivas(propuesta);

        assertEquals(2, crucesEntrePrimeros(propuesta));
        assertTrue(advertencias.stream().anyMatch(
                texto -> texto.contains("puede redistribuirse")));
    }

    @Test
    void advierteCruceDeParejasDelMismoGrupo() {
        PropuestaEtapaEliminatoria propuesta =
                service.construirDesdeClasificados(clasificadosCincoGrupos());
        List<CrucePropuestoTorneo> cruces = nuevaLista(propuesta);
        CrucePropuestoTorneo cuartos = cruces.stream()
                .filter(c -> "Cuartos".equals(c.getInstancia()))
                .filter(c -> c.getParticipante1().startsWith("1° Grupo "))
                .findFirst().orElseThrow();
        ClasificadoEtapaEliminatoria primero = propuesta.getClasificados()
                .stream()
                .filter(c -> c.referencia().equals(
                        cuartos.getParticipante1()))
                .findFirst().orElseThrow();
        ClasificadoEtapaEliminatoria segundoMismoGrupo = propuesta
                .getClasificados().stream()
                .filter(c -> c.grupoId() == primero.grupoId())
                .filter(c -> c.posicionGrupo() > 1)
                .findFirst().orElseThrow();
        cuartos.setParticipante2(segundoMismoGrupo.referencia());
        propuesta.setCruces(cruces);

        assertTrue(service.advertenciasDeportivas(propuesta).stream()
                .anyMatch(texto -> texto.contains("mismo grupo")));
    }

    @Test
    void intercambiaParticipanteOcupadoSinDuplicarlo() {
        PropuestaEtapaEliminatoria propuesta =
                service.construirDesdeClasificados(clasificadosCincoGrupos());
        CrucePropuestoTorneo primero = propuesta.getCruces().stream()
                .filter(c -> !c.getParticipante1().startsWith("Ganador "))
                .findFirst().orElseThrow();
        CrucePropuestoTorneo segundo = propuesta.getCruces().stream()
                .filter(c -> c != primero)
                .filter(c -> !c.getParticipante1().startsWith("Ganador "))
                .findFirst().orElseThrow();
        String anteriorPrimero = primero.getParticipante1();
        String anteriorSegundo = segundo.getParticipante1();

        assertTrue(service.reasignarIntercambiando(
                propuesta, primero, true, anteriorSegundo));

        assertEquals(anteriorSegundo, primero.getParticipante1());
        assertEquals(anteriorPrimero, segundo.getParticipante1());
        assertEquals(1, propuesta.getCruces().stream()
                .flatMap(c -> java.util.stream.Stream.of(
                        c.getParticipante1(), c.getParticipante2()))
                .filter(anteriorSegundo::equals).count());
    }

    @Test
    void noPermiteAsignarReferenciaProtegidaComoParticipanteConcreto() {
        PropuestaEtapaEliminatoria propuesta =
                service.construirDesdeClasificados(clasificadosCincoGrupos());
        CrucePropuestoTorneo destino = propuesta.getCruces().stream()
                .filter(c -> !c.getParticipante1().startsWith("Ganador "))
                .findFirst().orElseThrow();
        String anterior = destino.getParticipante1();

        assertFalse(service.reasignarIntercambiando(
                propuesta, destino, true, "Ganador Octavos #1"));
        assertEquals(anterior, destino.getParticipante1());
    }

    @Test
    void construccionAutomaticaEsDeterminista() {
        PropuestaEtapaEliminatoria primera =
                service.construirDesdeClasificados(clasificadosCincoGrupos());
        PropuestaEtapaEliminatoria segunda =
                service.construirDesdeClasificados(clasificadosCincoGrupos());

        assertEquals(representacion(primera), representacion(segunda));
    }

    @Test
    void restaurarRecuperaLaPropuestaAutomatica() {
        PropuestaEtapaEliminatoria original =
                service.construirDesdeClasificados(clasificadosCincoGrupos());
        PropuestaEtapaEliminatoria modificada = service.copiar(original);
        List<CrucePropuestoTorneo> cruces = nuevaLista(modificada);
        buscar(cruces, "Cuartos", 1).setParticipante1("2° Grupo E");
        modificada.setCruces(cruces);
        assertFalse(representacion(original).equals(representacion(modificada)));

        service.restaurarPropuesta(modificada, original);

        assertEquals(representacion(original), representacion(modificada));
    }

    private List<ClasificadoEtapaEliminatoria> clasificadosCincoGrupos() {
        List<ClasificadoEtapaEliminatoria> valores = new ArrayList<>();
        long id = 1;
        for (char grupo = 'A'; grupo <= 'E'; grupo++) {
            valores.add(clasificado(id++, grupo, 1, 3, 8, 20));
            valores.add(clasificado(id++, grupo, 2, 1, 1, 3));
        }
        return valores;
    }

    private ClasificadoEtapaEliminatoria clasificado(long id, char grupo,
            int posicion, int ganados, int diferenciaSets,
            int diferenciaGames) {
        long grupoId = grupo - 'A' + 1L;
        return new ClasificadoEtapaEliminatoria(id, grupoId,
                "Grupo " + grupo, posicion, ganados, diferenciaSets,
                diferenciaGames, 4, 24);
    }

    private long cantidadFase(PropuestaEtapaEliminatoria propuesta,
            String fase) {
        return propuesta.getCruces().stream()
                .filter(cruce -> fase.equals(cruce.getInstancia())).count();
    }

    private long crucesEntrePrimeros(PropuestaEtapaEliminatoria propuesta) {
        return propuesta.getCruces().stream()
                .filter(cruce -> cruce.getParticipante1().startsWith("1°")
                        && cruce.getParticipante2().startsWith("1°"))
                .count();
    }

    private List<CrucePropuestoTorneo> nuevaLista(
            PropuestaEtapaEliminatoria propuesta) {
        return propuesta.getCruces().stream()
                .map(c -> new CrucePropuestoTorneo(c.getInstancia(),
                        c.getRonda(), c.getOrden(), c.getParticipante1(),
                        c.getParticipante2()))
                .collect(java.util.stream.Collectors.toCollection(
                        ArrayList::new));
    }

    private CrucePropuestoTorneo buscar(List<CrucePropuestoTorneo> cruces,
            String fase, int orden) {
        return cruces.stream()
                .filter(c -> fase.equals(c.getInstancia())
                        && c.getOrden() == orden)
                .findFirst().orElseThrow();
    }

    private List<String> representacion(
            PropuestaEtapaEliminatoria propuesta) {
        return propuesta.getCruces().stream()
                .map(c -> c.getInstancia() + "|" + c.getRonda() + "|"
                        + c.getOrden() + "|" + c.getParticipante1() + "|"
                        + c.getParticipante2())
                .toList();
    }
}
