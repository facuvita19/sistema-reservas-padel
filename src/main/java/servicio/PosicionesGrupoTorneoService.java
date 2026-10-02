package servicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.EstadoClasificacionGrupo;
import negocio.EstadoPartidoTorneo;
import negocio.PosicionGrupoTorneo;
import negocio.TipoPartidoGrupo;
import negocio.TorneoGrupo;
import negocio.TorneoGrupoIntegrante;
import negocio.TorneoPartido;
import negocio.TorneoPartidoSet;

public class PosicionesGrupoTorneoService {
    private final TorneoPartidoDAO partidoDAO;

    public PosicionesGrupoTorneoService() {
        this(new TorneoPartidoDAOMySQL());
    }

    PosicionesGrupoTorneoService(TorneoPartidoDAO partidoDAO) {
        if (partidoDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de partidos es obligatorio.");
        }
        this.partidoDAO = partidoDAO;
    }

    public Resultado calcular(TorneoGrupo grupo) {
        if (grupo == null || grupo.getId() <= 0) {
            throw new IllegalArgumentException("El grupo no es valido.");
        }
        List<TorneoPartido> partidos = partidoDAO.listarPorGrupo(grupo.getId());
        return grupo.getCapacidad() == 3
                ? calcularGrupoTres(grupo, partidos)
                : calcularGrupoCuatro(grupo, partidos);
    }

    private Resultado calcularGrupoTres(
            TorneoGrupo grupo,
            List<TorneoPartido> partidos) {
        Map<Long, Estadistica> tabla = estadisticasBase(grupo);
        List<TorneoPartido> jugados = partidos.stream()
                .filter(p -> p.getTipoPartidoGrupo()
                        == TipoPartidoGrupo.TODOS_CONTRA_TODOS)
                .filter(this::finalizado)
                .toList();
        for (TorneoPartido partido : jugados) {
            acumular(tabla, partido);
        }
        List<Estadistica> ordenadas = new ArrayList<>(tabla.values());
        ordenadas.sort(comparadorGeneral());
        aplicarEnfrentamientoDirecto(ordenadas, jugados);
        boolean completo = partidos.size() == 3 && jugados.size() == 3;
        marcarEmpatesAbsolutos(ordenadas, completo);
        return convertir(ordenadas, completo, 2);
    }

    private Resultado calcularGrupoCuatro(
            TorneoGrupo grupo,
            List<TorneoPartido> partidos) {
        Map<Long, Estadistica> tabla = estadisticasBase(grupo);
        for (TorneoPartido partido : partidos) {
            if (finalizado(partido)) acumular(tabla, partido);
        }
        TorneoPartido final12 = buscar(partidos,
                TipoPartidoGrupo.DEFINICION_PRIMERO_SEGUNDO);
        TorneoPartido final34 = buscar(partidos,
                TipoPartidoGrupo.DEFINICION_TERCERO_CUARTO);
        boolean completo = finalizado(final12) && finalizado(final34);
        List<Estadistica> ordenadas = new ArrayList<>();
        if (completo) {
            Long primera = final12.getGanadoraInscripcionId();
            Long segunda = perdedora(final12);
            Long tercera = final34.getGanadoraInscripcionId();
            Long cuarta = perdedora(final34);
            for (Long id : List.of(primera, segunda, tercera, cuarta)) {
                Estadistica valor = tabla.get(id);
                if (valor != null) ordenadas.add(valor);
            }
        } else {
            ordenadas.addAll(tabla.values());
            ordenadas.sort(comparadorGeneral());
        }
        return convertir(ordenadas, completo, 3);
    }

    private Map<Long, Estadistica> estadisticasBase(TorneoGrupo grupo) {
        Map<Long, Estadistica> tabla = new HashMap<>();
        for (TorneoGrupoIntegrante integrante : grupo.getIntegrantes()) {
            tabla.put(integrante.getInscripcionId(),
                    new Estadistica(integrante.getInscripcionId()));
        }
        return tabla;
    }

    private void acumular(
            Map<Long, Estadistica> tabla,
            TorneoPartido partido) {
        Estadistica pareja1 = tabla.get(partido.getPareja1InscripcionId());
        Estadistica pareja2 = tabla.get(partido.getPareja2InscripcionId());
        if (pareja1 == null || pareja2 == null) return;
        pareja1.pj++;
        pareja2.pj++;
        if (partido.getGanadoraInscripcionId().equals(pareja1.id)) {
            pareja1.pg++;
            pareja2.pp++;
        } else {
            pareja2.pg++;
            pareja1.pp++;
        }
        for (TorneoPartidoSet set : partido.getSets()) {
            pareja1.gg += set.getPuntosPareja1();
            pareja1.gp += set.getPuntosPareja2();
            pareja2.gg += set.getPuntosPareja2();
            pareja2.gp += set.getPuntosPareja1();
            if (set.parejaGanadora() == 1) {
                pareja1.sg++;
                pareja2.sp++;
            } else {
                pareja2.sg++;
                pareja1.sp++;
            }
        }
    }

    private Comparator<Estadistica> comparadorGeneral() {
        return Comparator.comparingInt((Estadistica e) -> e.pg).reversed()
                .thenComparing(Comparator.comparingInt(
                        (Estadistica e) -> e.sg - e.sp).reversed())
                .thenComparing(Comparator.comparingInt(
                        (Estadistica e) -> e.gg - e.gp).reversed())
                .thenComparing(Comparator.comparingInt(
                        (Estadistica e) -> e.sg).reversed())
                .thenComparing(Comparator.comparingInt(
                        (Estadistica e) -> e.gg).reversed())
                .thenComparingLong(e -> e.id);
    }

    private void aplicarEnfrentamientoDirecto(
            List<Estadistica> tabla,
            List<TorneoPartido> partidos) {
        int indice = 0;
        while (indice < tabla.size()) {
            int fin = indice + 1;
            while (fin < tabla.size()
                    && tabla.get(fin).pg == tabla.get(indice).pg) {
                fin++;
            }
            if (fin - indice == 2) {
                Estadistica a = tabla.get(indice);
                Estadistica b = tabla.get(indice + 1);
                TorneoPartido directo = partidos.stream()
                        .filter(p -> contiene(p, a.id) && contiene(p, b.id))
                        .findFirst().orElse(null);
                if (directo != null
                        && directo.getGanadoraInscripcionId().equals(b.id)) {
                    tabla.set(indice, b);
                    tabla.set(indice + 1, a);
                }
            }
            indice = fin;
        }
    }

    private void marcarEmpatesAbsolutos(
            List<Estadistica> tabla,
            boolean completo) {
        if (!completo) return;
        for (int i = 0; i < tabla.size() - 1; i++) {
            Estadistica a = tabla.get(i);
            Estadistica b = tabla.get(i + 1);
            if (a.pg == b.pg
                    && a.sg - a.sp == b.sg - b.sp
                    && a.gg - a.gp == b.gg - b.gp
                    && a.sg == b.sg
                    && a.gg == b.gg) {
                a.desempate = true;
                b.desempate = true;
            }
        }
    }

    private Resultado convertir(
            List<Estadistica> tabla,
            boolean definitivo,
            int clasifican) {
        List<PosicionGrupoTorneo> filas = new ArrayList<>();
        for (int i = 0; i < tabla.size(); i++) {
            Estadistica e = tabla.get(i);
            EstadoClasificacionGrupo estado;
            if (!definitivo) {
                estado = EstadoClasificacionGrupo.PENDIENTE;
            } else if (e.desempate) {
                estado = EstadoClasificacionGrupo.DESEMPATE_PENDIENTE;
            } else {
                estado = i < clasifican
                        ? EstadoClasificacionGrupo.CLASIFICADO
                        : EstadoClasificacionGrupo.ELIMINADO;
            }
            filas.add(new PosicionGrupoTorneo(i + 1, e.id, e.pj, e.pg,
                    e.pp, e.sg, e.sp, e.gg, e.gp, estado, e.desempate));
        }
        boolean desempate = filas.stream()
                .anyMatch(PosicionGrupoTorneo::desempatePendiente);
        return new Resultado(filas, definitivo, desempate);
    }

    private boolean finalizado(TorneoPartido partido) {
        return partido != null
                && partido.getEstado() == EstadoPartidoTorneo.FINALIZADO
                && partido.getGanadoraInscripcionId() != null;
    }

    private TorneoPartido buscar(
            List<TorneoPartido> partidos,
            TipoPartidoGrupo tipo) {
        return partidos.stream()
                .filter(p -> p.getTipoPartidoGrupo() == tipo)
                .findFirst().orElse(null);
    }

    private Long perdedora(TorneoPartido partido) {
        return partido.getGanadoraInscripcionId().equals(
                partido.getPareja1InscripcionId())
                        ? partido.getPareja2InscripcionId()
                        : partido.getPareja1InscripcionId();
    }

    private boolean contiene(TorneoPartido partido, long id) {
        return Long.valueOf(id).equals(partido.getPareja1InscripcionId())
                || Long.valueOf(id).equals(
                        partido.getPareja2InscripcionId());
    }

    public record Resultado(
            List<PosicionGrupoTorneo> posiciones,
            boolean definitivo,
            boolean desempatePendiente) {
    }

    private static final class Estadistica {
        private final long id;
        private int pj;
        private int pg;
        private int pp;
        private int sg;
        private int sp;
        private int gg;
        private int gp;
        private boolean desempate;

        private Estadistica(long id) {
            this.id = id;
        }
    }
}
