package api.publica;

import java.time.LocalDateTime;
import java.util.List;

import api.publica.TorneoPublicoDTO.Categoria;
import api.publica.TorneoPublicoDTO.TorneoDetalle;
import api.publica.TorneoPublicoDTO.TorneoResumen;
import dao.TorneoCatalogoPublicoDAO;
import dao.TorneoCatalogoPublicoDAOMySQL;
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
    private final TorneoCatalogoPublicoDAO catalogoDAO;

    public TorneoPublicoService(){this(new TorneoDAOMySQL(),new TorneoCategoriaDAOMySQL(),new TorneoCatalogoPublicoDAOMySQL());}
    public TorneoPublicoService(TorneoDAO torneoDAO,TorneoCategoriaDAO categoriaDAO){this(torneoDAO,categoriaDAO,new TorneoCatalogoPublicoDAOMySQL());}
    public TorneoPublicoService(TorneoDAO torneoDAO,TorneoCategoriaDAO categoriaDAO,TorneoCatalogoPublicoDAO catalogoDAO){
        if(torneoDAO==null||categoriaDAO==null||catalogoDAO==null)throw new IllegalArgumentException("Los DAO de torneos no pueden ser nulos.");
        this.torneoDAO=torneoDAO;this.categoriaDAO=categoriaDAO;this.catalogoDAO=catalogoDAO;
    }

    public List<TorneoResumen> listar(){LocalDateTime ahora=LocalDateTime.now();return catalogoDAO.listar().stream().map(x->new TorneoResumen(
            x.id(),x.nombre(),x.descripcion(),x.fechaInicio(),x.fechaFin(),x.inscripcionDesde(),x.inscripcionHasta(),x.estado(),
            estadoPortal(x.estado()),inscripcionDisponible(x,ahora),x.cantidadCategorias(),x.categoriasDisponibles(),x.cupoTotal(),
            x.parejasConfirmadas(),x.cuposDisponibles(),x.precioMinimo(),x.precioMaximo(),x.categorias(),x.ramas(),x.formatos(),
            etapa(x),x.partidosTotales(),x.partidosFinalizados(),x.partidosCancelados(),x.partidosFinalizados()>0,x.finalDisputada())).toList();}

    public TorneoDetalle buscar(long id){if(id<=0)throw new IllegalArgumentException("El ID del torneo debe ser positivo.");Torneo t=torneoDAO.buscar(id);
        if(t==null||!t.isActivo()||!esPublico(t))throw new TorneoPublicoNoEncontradoException();LocalDateTime ahora=LocalDateTime.now();
        List<Categoria> categorias=categoriaDAO.listarActivasPorTorneo(t.getId()).stream().map(c->convertirCategoria(c,t.inscripcionDisponible(ahora))).toList();
        return new TorneoDetalle(t.getId(),t.getNombre(),t.getDescripcion(),t.getFechaInicio(),t.getFechaFin(),t.getInscripcionDesde(),t.getInscripcionHasta(),t.getEstado().name(),t.getReglamento(),t.inscripcionDisponible(ahora),categorias);}

    static String estadoPortal(String e){return "FINALIZADO".equals(e)?"FINALIZADO":"EN_CURSO".equals(e)?"EN_CURSO":"PROXIMO";}
    static String etapa(TorneoCatalogoPublicoDAO.Resumen x){if("FINALIZADO".equals(x.estado()))return "FINALIZADA";if("EN_CURSO".equals(x.estado())){if(x.tieneEliminatorias())return "ELIMINATORIAS";if(x.tieneGrupos())return "GRUPOS";return "PREPARACION";}if("INSCRIPCION_ABIERTA".equals(x.estado()))return "INSCRIPCION";return "PREPARACION";}
    private static boolean inscripcionDisponible(TorneoCatalogoPublicoDAO.Resumen x,LocalDateTime a){return "INSCRIPCION_ABIERTA".equals(x.estado())&&!a.isBefore(x.inscripcionDesde())&&!a.isAfter(x.inscripcionHasta())&&x.cuposDisponibles()>0;}
    private Categoria convertirCategoria(TorneoCategoria c,boolean abierta){boolean disponible=abierta&&c.isActivo()&&c.tieneCupoDisponible();return new Categoria(c.getId(),c.getNombre(),c.getRama().name(),c.getCupoParejas(),c.getParejasConfirmadas(),c.getCuposDisponibles(),c.getPrecioInscripcion(),c.getPremioCampeon(),c.getPremioSubcampeon(),c.getPremioDescripcion(),disponible,c.getFormatoCompetencia().name(),c.getCantidadGruposTres(),c.getCantidadGruposCuatro(),c.getClasificadosProyectados());}
    private boolean esPublico(Torneo t){return t.getEstado()!=EstadoTorneo.BORRADOR&&t.getEstado()!=EstadoTorneo.CANCELADO;}
    public static class TorneoPublicoNoEncontradoException extends RuntimeException{public TorneoPublicoNoEncontradoException(){super("El torneo solicitado no existe o no esta publicado.");}}
}
