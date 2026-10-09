package api.publica;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.publica.CompetenciaTorneoPublicaDTO.Categoria;
import api.publica.CompetenciaTorneoPublicaDTO.Competencia;
import api.publica.CompetenciaTorneoPublicaDTO.Grupo;
import api.publica.CompetenciaTorneoPublicaDTO.Integrante;
import api.publica.CompetenciaTorneoPublicaDTO.Pareja;
import api.publica.CompetenciaTorneoPublicaDTO.Partido;
import api.publica.CompetenciaTorneoPublicaDTO.Posicion;
import api.publica.CompetenciaTorneoPublicaDTO.Resumen;
import api.publica.CompetenciaTorneoPublicaDTO.Set;
import api.publica.CompetenciaTorneoPublicaDTO.Torneo;
import config.ConexionBD;
import dao.CanchaDAO;
import dao.CanchaDAOMySQL;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoGrupoDAO;
import dao.TorneoGrupoDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.Cancha;
import negocio.EstadoPartidoTorneo;
import negocio.EstadoTorneo;
import negocio.FaseTorneo;
import negocio.PosicionGrupoTorneo;
import negocio.TorneoCategoria;
import negocio.TorneoGrupo;
import negocio.TorneoInscripcion;
import negocio.TorneoPartido;
import negocio.TorneoPartidoSet;
import servicio.PosicionesGrupoTorneoService;

public class CompetenciaTorneoPublicaService {
    private final TorneoDAO torneoDAO = new TorneoDAOMySQL();
    private final TorneoCategoriaDAO categoriaDAO = new TorneoCategoriaDAOMySQL();
    private final TorneoGrupoDAO grupoDAO = new TorneoGrupoDAOMySQL();
    private final TorneoPartidoDAO partidoDAO = new TorneoPartidoDAOMySQL();
    private final TorneoInscripcionDAO inscripcionDAO = new TorneoInscripcionDAOMySQL();
    private final CanchaDAO canchaDAO = new CanchaDAOMySQL();
    private final PosicionesGrupoTorneoService posicionesService = new PosicionesGrupoTorneoService();
    private final CuadroTorneoPublicoService cuadroService = new CuadroTorneoPublicoService();

    public Competencia buscar(long categoriaId) {
        if (categoriaId <= 0) throw new IllegalArgumentException(
                "El ID de la categoria debe ser positivo.");
        TorneoCategoria categoria = categoriaDAO.buscar(categoriaId);
        if (categoria == null || !categoria.isActivo()) throw noEncontrada();
        negocio.Torneo torneo = torneoDAO.buscar(categoria.getTorneoId());
        if (torneo == null || !torneo.isActivo()
                || torneo.getEstado() == EstadoTorneo.BORRADOR
                || torneo.getEstado() == EstadoTorneo.CANCELADO) throw noEncontrada();

        List<TorneoPartido> todos = partidoDAO.listarPorCategoria(categoriaId);
        List<TorneoPartido> eliminatorios = todos.stream()
                .filter(x -> x.getFase() != FaseTorneo.GRUPOS).toList();
        List<Grupo> grupos = categoria.usaFaseGrupos()
                ? convertirGrupos(categoriaId) : List.of();
        boolean confirmados = !grupos.isEmpty() && grupos.stream().allMatch(Grupo::confirmado);
        boolean definitivas = !grupos.isEmpty() && grupos.stream().allMatch(Grupo::posicionesDefinitivas);
        boolean desempate = grupos.stream().anyMatch(Grupo::desempatePendiente);
        long gruposFin = todos.stream().filter(x -> x.getFase() == FaseTorneo.GRUPOS
                && x.getEstado() == EstadoPartidoTorneo.FINALIZADO).count();
        long elimFin = eliminatorios.stream().filter(x ->
                x.getEstado() == EstadoPartidoTorneo.FINALIZADO).count();
        boolean hayEliminatorias = !eliminatorios.isEmpty();
        String etapa = etapa(categoria, grupos, confirmados, definitivas,
                desempate, hayEliminatorias, eliminatorios);
        CuadroTorneoPublicoDTO.CuadroCategoria cuadro = hayEliminatorias
                ? cuadroService.buscarPorCategoria(categoriaId) : null;

        return new Competencia(
                new Torneo(torneo.getId(), torneo.getNombre(),
                        torneo.getEstado().name(), torneo.getFechaInicio(), torneo.getFechaFin()),
                new Categoria(categoria.getId(), categoria.getNombre(),
                        categoria.getRama().name(), categoria.getFormatoCompetencia().name(),
                        categoria.getCupoParejas(), categoria.getCantidadGruposTres(),
                        categoria.getCantidadGruposCuatro(), categoria.getClasificadosProyectados()),
                new Resumen(etapa, !grupos.isEmpty(), confirmados, definitivas,
                        desempate, hayEliminatorias,
                        (int) todos.stream().filter(x -> x.getFase() == FaseTorneo.GRUPOS).count(),
                        (int) gruposFin, eliminatorios.size(), (int) elimFin),
                grupos, cuadro);
    }

    private List<Grupo> convertirGrupos(long categoriaId) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return grupoDAO.listar(conexion, categoriaId).stream()
                    .map(this::convertirGrupo).toList();
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo consultar la competencia publica.", exception);
        }
    }

    private Grupo convertirGrupo(TorneoGrupo grupo) {
        Map<Long,String> nombres = new HashMap<>();
        List<Integrante> integrantes = grupo.getIntegrantes().stream().map(x -> {
            nombres.put(x.getInscripcionId(), x.getNombrePareja());
            return new Integrante(x.getInscripcionId(), x.getNombrePareja(),
                    x.isCabezaSerie(), x.getOrdenSorteo());
        }).toList();
        var calculo = posicionesService.calcular(grupo);
        List<Posicion> posiciones = calculo.posiciones().stream()
                .map(x -> convertirPosicion(x, nombres)).toList();
        List<Partido> partidos = partidoDAO.listarPorGrupo(grupo.getId()).stream()
                .map(this::convertirPartido).toList();
        return new Grupo(grupo.getId(), grupo.getNombre(), grupo.getOrden(),
                grupo.getCapacidad(), grupo.getModoAsignacion().name(),
                grupo.isConfirmado(), grupo.getCapacidad() == 3 ? 2 : 3,
                calculo.definitivo(), calculo.desempatePendiente(),
                integrantes, posiciones, partidos);
    }

    private Posicion convertirPosicion(PosicionGrupoTorneo x, Map<Long,String> nombres) {
        return new Posicion(x.posicion(), x.inscripcionId(),
                nombres.getOrDefault(x.inscripcionId(), "Pareja por definir"),
                x.partidosJugados(), x.partidosGanados(), x.partidosPerdidos(),
                x.setsGanados(), x.setsPerdidos(), x.diferenciaSets(),
                x.gamesGanados(), x.gamesPerdidos(), x.diferenciaGames(),
                x.estado().name());
    }

    private Partido convertirPartido(TorneoPartido x) {
        Cancha cancha = x.getCanchaId() == null ? null : canchaDAO.buscar(x.getCanchaId());
        return new Partido(x.getId(), x.getTipoPartidoGrupo() == null ? null
                : x.getTipoPartidoGrupo().name(), x.getOrdenFase(), x.getEstado().name(),
                pareja(x.getPareja1InscripcionId()), pareja(x.getPareja2InscripcionId()),
                x.getGanadoraInscripcionId(), resultado(x),
                x.getSets().stream().map(this::convertirSet).toList(),
                x.getFecha(), x.getHoraInicio(), x.getHoraFin(), x.getCanchaId(),
                cancha == null ? null : cancha.getNombre(), x.getFechaFinalizacion());
    }

    private Pareja pareja(Long id) {
        if (id == null) return null;
        TorneoInscripcion inscripcion = inscripcionDAO.buscar(id);
        if (inscripcion == null) return null;
        List<String> jugadores = inscripcion.getJugadores().stream()
                .map(x -> x.getNombreCompleto()).filter(x -> x != null && !x.isBlank()).toList();
        return new Pareja(id, jugadores);
    }

    private Set convertirSet(TorneoPartidoSet x) {
        return new Set(x.getNumeroSet(), x.getTipo().name(),
                x.getPuntosPareja1(), x.getPuntosPareja2());
    }

    private String resultado(TorneoPartido x) {
        if (x.getSets().isEmpty()) return null;
        return x.getSets().stream().map(s -> s.getPuntosPareja1() + "-" + s.getPuntosPareja2())
                .reduce((a,b) -> a + " / " + b).orElse(null);
    }

    private String etapa(TorneoCategoria categoria, List<Grupo> grupos,
            boolean confirmados, boolean definitivas, boolean desempate,
            boolean hayEliminatorias, List<TorneoPartido> eliminatorios) {
        if (!categoria.usaFaseGrupos()) return hayEliminatorias
                ? (eliminatorios.stream().allMatch(x -> x.getEstado().esFinal())
                        ? "FINALIZADA" : "ELIMINATORIAS") : "PREPARACION";
        if (hayEliminatorias) return eliminatorios.stream().allMatch(x -> x.getEstado().esFinal())
                ? "FINALIZADA" : "ELIMINATORIAS";
        if (desempate) return "DESEMPATE_PENDIENTE";
        if (definitivas) return "GRUPOS_FINALIZADOS";
        if (confirmados) return "GRUPOS_EN_CURSO";
        if (!grupos.isEmpty()) return "GRUPOS_EN_PREPARACION";
        return "PREPARACION";
    }

    private CompetenciaNoEncontradaException noEncontrada() {
        return new CompetenciaNoEncontradaException();
    }

    public static class CompetenciaNoEncontradaException extends RuntimeException {
        public CompetenciaNoEncontradaException() {
            super("La competencia solicitada no existe o no esta publicada.");
        }
    }
}
