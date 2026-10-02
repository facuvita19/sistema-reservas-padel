package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import config.ConexionBD;
import dao.TorneoGrupoDAO;
import dao.TorneoGrupoDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import dao.TorneoPartidoEnlaceDAO;
import dao.TorneoPartidoEnlaceDAOMySQL;
import negocio.EstadoPartidoTorneo;
import negocio.FaseTorneo;
import negocio.PosicionPartidoSiguiente;
import negocio.ResultadoOrigenPartido;
import negocio.TipoPartidoGrupo;
import negocio.TorneoGrupo;
import negocio.TorneoGrupoIntegrante;
import negocio.TorneoPartido;
import negocio.TorneoPartidoEnlace;

public class GeneracionPartidosGrupoService {
    private final TorneoGrupoDAO grupoDAO = new TorneoGrupoDAOMySQL();
    private final TorneoPartidoDAO partidoDAO = new TorneoPartidoDAOMySQL();
    private final TorneoPartidoEnlaceDAO enlaceDAO =
            new TorneoPartidoEnlaceDAOMySQL();

    public int generar(long categoriaId) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                List<TorneoGrupo> grupos = grupoDAO.listar(conexion, categoriaId);
                if (grupos.isEmpty()) {
                    throw new IllegalArgumentException("La categoria no tiene grupos configurados.");
                }
                if (grupos.stream().anyMatch(grupo -> !grupo.isConfirmado())) {
                    throw new IllegalArgumentException("Todos los grupos deben estar confirmados.");
                }
                if (partidoDAO.existenPartidosDeGrupos(conexion, categoriaId)) {
                    throw new IllegalArgumentException("Los partidos de grupos ya fueron generados.");
                }
                int orden = 1;
                int cantidad = 0;
                for (TorneoGrupo grupo : grupos) {
                    if (grupo.getIntegrantes().size() != grupo.getCapacidad()) {
                        throw new IllegalArgumentException(
                                grupo.getNombre() + " no esta completo.");
                    }
                    List<Long> ids = grupo.getIntegrantes().stream()
                            .map(TorneoGrupoIntegrante::getInscripcionId)
                            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                    if (grupo.getCapacidad() == 3) {
                        crear(conexion, categoriaId, grupo.getId(), orden++,
                                TipoPartidoGrupo.TODOS_CONTRA_TODOS,
                                ids.get(0), ids.get(1));
                        crear(conexion, categoriaId, grupo.getId(), orden++,
                                TipoPartidoGrupo.TODOS_CONTRA_TODOS,
                                ids.get(0), ids.get(2));
                        crear(conexion, categoriaId, grupo.getId(), orden++,
                                TipoPartidoGrupo.TODOS_CONTRA_TODOS,
                                ids.get(1), ids.get(2));
                        cantidad += 3;
                    } else if (grupo.getCapacidad() == 4) {
                        Collections.shuffle(ids);
                        TorneoPartido inicial1 = crear(conexion, categoriaId,
                                grupo.getId(), orden++,
                                TipoPartidoGrupo.CRUCE_INICIAL,
                                ids.get(0), ids.get(1));
                        TorneoPartido inicial2 = crear(conexion, categoriaId,
                                grupo.getId(), orden++,
                                TipoPartidoGrupo.CRUCE_INICIAL,
                                ids.get(2), ids.get(3));
                        TorneoPartido definicion12 = crear(conexion, categoriaId,
                                grupo.getId(), orden++,
                                TipoPartidoGrupo.DEFINICION_PRIMERO_SEGUNDO,
                                null, null);
                        TorneoPartido definicion34 = crear(conexion, categoriaId,
                                grupo.getId(), orden++,
                                TipoPartidoGrupo.DEFINICION_TERCERO_CUARTO,
                                null, null);
                        enlazar(conexion, inicial1, ResultadoOrigenPartido.GANADOR,
                                definicion12, PosicionPartidoSiguiente.PAREJA_1);
                        enlazar(conexion, inicial2, ResultadoOrigenPartido.GANADOR,
                                definicion12, PosicionPartidoSiguiente.PAREJA_2);
                        enlazar(conexion, inicial1, ResultadoOrigenPartido.PERDEDOR,
                                definicion34, PosicionPartidoSiguiente.PAREJA_1);
                        enlazar(conexion, inicial2, ResultadoOrigenPartido.PERDEDOR,
                                definicion34, PosicionPartidoSiguiente.PAREJA_2);
                        cantidad += 4;
                    } else {
                        throw new IllegalArgumentException("Solo se admiten grupos de 3 o 4 equipos.");
                    }
                }
                conexion.commit();
                return cantidad;
            } catch (SQLException | RuntimeException exception) {
                conexion.rollback();
                throw exception;
            } finally {
                conexion.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron generar los partidos de grupos.", exception);
        }
    }

    private TorneoPartido crear(Connection conexion, long categoriaId,
            long grupoId, int orden, TipoPartidoGrupo tipo,
            Long pareja1, Long pareja2) {
        TorneoPartido partido = new TorneoPartido();
        partido.setTorneoCategoriaId(categoriaId);
        partido.setGrupoId(grupoId);
        partido.setFase(FaseTorneo.GRUPOS);
        partido.setTipoPartidoGrupo(tipo);
        partido.setOrdenFase(orden);
        partido.setPareja1InscripcionId(pareja1);
        partido.setPareja2InscripcionId(pareja2);
        partido.setEstado(EstadoPartidoTorneo.PENDIENTE);
        partidoDAO.guardar(conexion, partido);
        return partido;
    }

    private void enlazar(Connection conexion, TorneoPartido origen,
            ResultadoOrigenPartido resultado, TorneoPartido destino,
            PosicionPartidoSiguiente posicion) {
        enlaceDAO.guardar(conexion, new TorneoPartidoEnlace(
                origen.getId(), resultado, destino.getId(), posicion));
    }
}
