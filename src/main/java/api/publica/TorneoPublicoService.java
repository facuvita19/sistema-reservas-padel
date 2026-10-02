package api.publica;

import java.time.LocalDateTime;
import java.util.List;

import api.publica.TorneoPublicoDTO.Categoria;
import api.publica.TorneoPublicoDTO.TorneoDetalle;
import api.publica.TorneoPublicoDTO.TorneoResumen;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import negocio.EstadoTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;

public class TorneoPublicoService {

    private final TorneoDAO torneoDAO;
    private final TorneoCategoriaDAO categoriaDAO;

    public TorneoPublicoService() {
        this(new TorneoDAOMySQL(), new TorneoCategoriaDAOMySQL());
    }

    public TorneoPublicoService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO) {
        if (torneoDAO == null || categoriaDAO == null) {
            throw new IllegalArgumentException(
                    "Los DAO de torneos no pueden ser nulos.");
        }
        this.torneoDAO = torneoDAO;
        this.categoriaDAO = categoriaDAO;
    }

    public List<TorneoResumen> listar() {
        LocalDateTime ahora = LocalDateTime.now();
        return torneoDAO.listarActivos().stream()
                .filter(this::esPublico)
                .map(torneo -> convertirResumen(torneo, ahora))
                .toList();
    }

    public TorneoDetalle buscar(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID del torneo debe ser positivo.");
        }
        Torneo torneo = torneoDAO.buscar(id);
        if (torneo == null || !torneo.isActivo() || !esPublico(torneo)) {
            throw new TorneoPublicoNoEncontradoException();
        }
        LocalDateTime ahora = LocalDateTime.now();
        List<Categoria> categorias = categoriaDAO
                .listarActivasPorTorneo(torneo.getId())
                .stream()
                .map(categoria -> convertirCategoria(
                        categoria,
                        torneo.inscripcionDisponible(ahora)))
                .toList();
        return new TorneoDetalle(
                torneo.getId(),
                torneo.getNombre(),
                torneo.getDescripcion(),
                torneo.getFechaInicio(),
                torneo.getFechaFin(),
                torneo.getInscripcionDesde(),
                torneo.getInscripcionHasta(),
                torneo.getEstado().name(),
                torneo.getReglamento(),
                torneo.inscripcionDisponible(ahora),
                categorias);
    }

    private TorneoResumen convertirResumen(
            Torneo torneo,
            LocalDateTime ahora) {
        int cantidadCategorias = categoriaDAO
                .listarActivasPorTorneo(torneo.getId())
                .size();
        return new TorneoResumen(
                torneo.getId(),
                torneo.getNombre(),
                torneo.getDescripcion(),
                torneo.getFechaInicio(),
                torneo.getFechaFin(),
                torneo.getInscripcionDesde(),
                torneo.getInscripcionHasta(),
                torneo.getEstado().name(),
                torneo.inscripcionDisponible(ahora),
                cantidadCategorias);
    }

    private Categoria convertirCategoria(
            TorneoCategoria categoria,
            boolean inscripcionTorneoDisponible) {
        boolean disponible = inscripcionTorneoDisponible
                && categoria.isActivo()
                && categoria.tieneCupoDisponible();
        return new Categoria(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getRama().name(),
                categoria.getCupoParejas(),
                categoria.getParejasConfirmadas(),
                categoria.getCuposDisponibles(),
                categoria.getPrecioInscripcion(),
                categoria.getPremioCampeon(),
                categoria.getPremioSubcampeon(),
                categoria.getPremioDescripcion(),
                disponible);
    }

    private boolean esPublico(Torneo torneo) {
        EstadoTorneo estado = torneo.getEstado();
        return estado != EstadoTorneo.BORRADOR
                && estado != EstadoTorneo.CANCELADO;
    }

    public static class TorneoPublicoNoEncontradoException
            extends RuntimeException {

        public TorneoPublicoNoEncontradoException() {
            super("El torneo solicitado no existe o no esta publicado.");
        }
    }
}
