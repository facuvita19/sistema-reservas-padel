package api.publica;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import api.publica.CuadroTorneoPublicoDTO.Campeona;
import api.publica.CuadroTorneoPublicoDTO.Categoria;
import api.publica.CuadroTorneoPublicoDTO.CuadroCategoria;
import api.publica.CuadroTorneoPublicoDTO.CuadroTorneo;
import api.publica.CuadroTorneoPublicoDTO.Fase;
import api.publica.CuadroTorneoPublicoDTO.Pareja;
import api.publica.CuadroTorneoPublicoDTO.Partido;
import api.publica.CuadroTorneoPublicoDTO.Set;
import api.publica.CuadroTorneoPublicoDTO.Torneo;
import dao.CanchaDAO;
import dao.CanchaDAOMySQL;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.Cancha;
import negocio.EstadoTorneo;
import negocio.FaseTorneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoPartido;
import negocio.TorneoPartidoSet;

public class CuadroTorneoPublicoService {

    private final TorneoDAO torneoDAO;
    private final TorneoCategoriaDAO categoriaDAO;
    private final TorneoPartidoDAO partidoDAO;
    private final TorneoInscripcionDAO inscripcionDAO;
    private final CanchaDAO canchaDAO;

    public CuadroTorneoPublicoService() {
        this(new TorneoDAOMySQL(), new TorneoCategoriaDAOMySQL(),
                new TorneoPartidoDAOMySQL(),
                new TorneoInscripcionDAOMySQL(),
                new CanchaDAOMySQL());
    }

    public CuadroTorneoPublicoService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoPartidoDAO partidoDAO,
            TorneoInscripcionDAO inscripcionDAO,
            CanchaDAO canchaDAO) {
        if (torneoDAO == null || categoriaDAO == null
                || partidoDAO == null || inscripcionDAO == null
                || canchaDAO == null) {
            throw new IllegalArgumentException(
                    "Las dependencias del cuadro publico no pueden ser nulas.");
        }
        this.torneoDAO = torneoDAO;
        this.categoriaDAO = categoriaDAO;
        this.partidoDAO = partidoDAO;
        this.inscripcionDAO = inscripcionDAO;
        this.canchaDAO = canchaDAO;
    }

    public CuadroTorneo buscarPorTorneo(long torneoId) {
        negocio.Torneo torneo = buscarTorneoPublico(torneoId);
        List<CuadroCategoria> categorias = categoriaDAO
                .listarActivasPorTorneo(torneoId).stream()
                .map(categoria -> convertir(torneo, categoria))
                .toList();
        return new CuadroTorneo(convertir(torneo), categorias);
    }

    public CuadroCategoria buscarPorCategoria(long categoriaId) {
        if (categoriaId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la categoria debe ser positivo.");
        }
        TorneoCategoria categoria = categoriaDAO.buscar(categoriaId);
        if (categoria == null || !categoria.isActivo()) {
            throw new CuadroNoEncontradoException(
                    "La categoria solicitada no existe.");
        }
        negocio.Torneo torneo = buscarTorneoPublico(
                categoria.getTorneoId());
        return convertir(torneo, categoria);
    }

    private CuadroCategoria convertir(
            negocio.Torneo torneo,
            TorneoCategoria categoria) {
        List<TorneoPartido> partidos = partidoDAO.listarPorCategoria(
                categoria.getId());
        Map<FaseTorneo, List<Partido>> agrupados = new LinkedHashMap<>();
        partidos.stream()
                .sorted(Comparator
                        .comparingInt((TorneoPartido p) ->
                                p.getFase().ordinal())
                        .thenComparingInt(TorneoPartido::getOrdenFase))
                .forEach(partido -> agrupados
                        .computeIfAbsent(partido.getFase(),
                                clave -> new ArrayList<>())
                        .add(convertirPartido(partido)));

        List<Fase> fases = agrupados.entrySet().stream()
                .map(entrada -> new Fase(
                        entrada.getKey().name(), entrada.getValue()))
                .toList();
        TorneoPartido finalPartido = partidos.stream()
                .filter(p -> p.getFase() == FaseTorneo.FINAL)
                .findFirst().orElse(null);
        Campeona campeona = convertirCampeona(finalPartido);
        return new CuadroCategoria(
                convertir(torneo),
                new Categoria(categoria.getId(), categoria.getNombre(),
                        categoria.getRama().name()),
                campeona,
                fases);
    }

    private Partido convertirPartido(TorneoPartido partido) {
        List<Set> sets = partido.getSets().stream()
                .map(this::convertirSet)
                .toList();
        Cancha cancha = partido.getCanchaId() == null
                ? null : canchaDAO.buscar(partido.getCanchaId());
        return new Partido(
                partido.getId(),
                partido.getFase().name(),
                partido.getOrdenFase(),
                partido.getEstado().name(),
                partido.isBye(),
                convertirPareja(partido.getPareja1InscripcionId()),
                convertirPareja(partido.getPareja2InscripcionId()),
                partido.getGanadoraInscripcionId(),
                resultado(partido),
                sets,
                partido.getFecha(),
                partido.getHoraInicio(),
                partido.getHoraFin(),
                partido.getCanchaId(),
                cancha == null ? null : cancha.getNombre(),
                partido.getFechaFinalizacion());
    }

    private Set convertirSet(TorneoPartidoSet set) {
        return new Set(set.getNumeroSet(), set.getTipo().name(),
                set.getPuntosPareja1(), set.getPuntosPareja2());
    }

    private Pareja convertirPareja(Long inscripcionId) {
        if (inscripcionId == null) return null;
        TorneoInscripcion inscripcion = inscripcionDAO.buscar(inscripcionId);
        if (inscripcion == null) return null;
        List<String> jugadores = inscripcion.getJugadores().stream()
                .map(jugador -> jugador.getNombreCompleto())
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .toList();
        return new Pareja(inscripcionId, jugadores);
    }

    private Campeona convertirCampeona(TorneoPartido finalPartido) {
        if (finalPartido == null
                || finalPartido.getGanadoraInscripcionId() == null) {
            return null;
        }
        Pareja pareja = convertirPareja(
                finalPartido.getGanadoraInscripcionId());
        return pareja == null ? null : new Campeona(
                pareja.inscripcionId(), pareja.jugadores(),
                resultado(finalPartido));
    }

    private String resultado(TorneoPartido partido) {
        if (partido.isBye()) return "Clasifica por BYE";
        if (partido.getSets().isEmpty()) return null;
        return partido.getSets().stream()
                .map(set -> set.getPuntosPareja1() + "-"
                        + set.getPuntosPareja2())
                .reduce((a, b) -> a + " / " + b)
                .orElse(null);
    }

    private Torneo convertir(negocio.Torneo torneo) {
        return new Torneo(torneo.getId(), torneo.getNombre(),
                torneo.getEstado().name(), torneo.getFechaInicio(),
                torneo.getFechaFin());
    }

    private negocio.Torneo buscarTorneoPublico(long torneoId) {
        if (torneoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del torneo debe ser positivo.");
        }
        negocio.Torneo torneo = torneoDAO.buscar(torneoId);
        if (torneo == null || !torneo.isActivo()
                || torneo.getEstado() == EstadoTorneo.BORRADOR
                || torneo.getEstado() == EstadoTorneo.CANCELADO) {
            throw new CuadroNoEncontradoException(
                    "El torneo solicitado no existe o no esta publicado.");
        }
        return torneo;
    }

    public static class CuadroNoEncontradoException
            extends RuntimeException {
        public CuadroNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }
}
