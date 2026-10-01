package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import config.ConexionBD;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.EstadoInscripcionTorneo;
import negocio.EstadoTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoPartido;
import negocio.EstadoPartidoTorneo;
import negocio.FaseTorneo;

public class GestionTorneoService {

    private static final List<EstadoInscripcionTorneo> ESTADOS_ACTIVOS = List.of(
            EstadoInscripcionTorneo.PENDIENTE,
            EstadoInscripcionTorneo.LISTA_ESPERA,
            EstadoInscripcionTorneo.CONFIRMADA);

    private final TorneoDAO torneoDAO;
    private final TorneoCategoriaDAO categoriaDAO;
    private final TorneoInscripcionDAO inscripcionDAO;
    private final TorneoPartidoDAO partidoDAO;
    private final ProveedorConexion proveedorConexion;

    public GestionTorneoService() {
        this(
                new TorneoDAOMySQL(),
                new TorneoCategoriaDAOMySQL(),
                new TorneoInscripcionDAOMySQL(),
                new TorneoPartidoDAOMySQL(),
                ConexionBD::obtenerConexion);
    }

    public GestionTorneoService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoInscripcionDAO inscripcionDAO,
            ProveedorConexion proveedorConexion) {
        this(torneoDAO, categoriaDAO, inscripcionDAO,
                new TorneoPartidoDAOMySQL(), proveedorConexion);
    }

    public GestionTorneoService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoInscripcionDAO inscripcionDAO,
            TorneoPartidoDAO partidoDAO,
            ProveedorConexion proveedorConexion) {
        if (torneoDAO == null || categoriaDAO == null
                || inscripcionDAO == null || partidoDAO == null
                || proveedorConexion == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de gestion no pueden ser nulas.");
        }
        this.torneoDAO = torneoDAO;
        this.categoriaDAO = categoriaDAO;
        this.inscripcionDAO = inscripcionDAO;
        this.partidoDAO = partidoDAO;
        this.proveedorConexion = proveedorConexion;
    }

    public Torneo crearTorneo(Torneo torneo, long usuarioCreadorId) {
        if (torneo == null) {
            throw new IllegalArgumentException("El torneo no puede ser nulo.");
        }
        if (torneo.getId() > 0) {
            throw new IllegalArgumentException("El torneo nuevo no puede tener ID.");
        }
        validarId(usuarioCreadorId, "El usuario creador es obligatorio.");

        torneo.setEstado(EstadoTorneo.BORRADOR);
        torneo.setActivo(true);
        torneo.setUsuarioCreacionId(usuarioCreadorId);

        return ejecutar(conexion -> {
            torneoDAO.guardar(conexion, torneo);
            return torneo;
        }, "No se pudo crear el torneo.");
    }

    public Torneo editarTorneo(Torneo cambios) {
        if (cambios == null) {
            throw new IllegalArgumentException("El torneo no puede ser nulo.");
        }
        validarId(cambios.getId(), "El ID del torneo debe ser positivo.");

        return ejecutar(conexion -> {
            Torneo existente = buscarTorneo(conexion, cambios.getId());
            validarEditable(existente);

            existente.setNombre(cambios.getNombre());
            existente.setDescripcion(cambios.getDescripcion());
            existente.setFechaInicio(cambios.getFechaInicio());
            existente.setFechaFin(cambios.getFechaFin());
            existente.setInscripcionDesde(cambios.getInscripcionDesde());
            existente.setInscripcionHasta(cambios.getInscripcionHasta());
            existente.setReglamento(cambios.getReglamento());

            torneoDAO.guardar(conexion, existente);
            return existente;
        }, "No se pudo editar el torneo.");
    }

    public Torneo publicar(long torneoId) {
        return cambiarEstado(
                torneoId,
                EstadoTorneo.PUBLICADO,
                EstadoTorneo.BORRADOR);
    }

    public Torneo abrirInscripciones(long torneoId) {
        validarId(torneoId, "El ID del torneo debe ser positivo.");
        if (categoriaDAO.listarActivasPorTorneo(torneoId).isEmpty()) {
            throw new IllegalArgumentException(
                    "El torneo necesita al menos una categoria activa.");
        }
        return cambiarEstado(
                torneoId,
                EstadoTorneo.INSCRIPCION_ABIERTA,
                EstadoTorneo.PUBLICADO);
    }

    public Torneo cerrarInscripciones(long torneoId) {
        return cambiarEstado(
                torneoId,
                EstadoTorneo.INSCRIPCION_CERRADA,
                EstadoTorneo.INSCRIPCION_ABIERTA);
    }

    public Torneo iniciar(long torneoId) {
        validarId(torneoId, "El ID del torneo debe ser positivo.");
        return ejecutar(conexion -> {
            Torneo torneo = buscarTorneo(conexion, torneoId);
            validarEditable(torneo);
            validarInicio(torneo);
            torneo.setEstado(EstadoTorneo.EN_CURSO);
            torneoDAO.guardar(conexion, torneo);
            return torneo;
        }, "No se pudo iniciar el torneo.");
    }

    public Torneo finalizar(long torneoId) {
        validarId(torneoId, "El ID del torneo debe ser positivo.");
        return ejecutar(conexion -> {
            Torneo torneo = buscarTorneo(conexion, torneoId);
            validarEditable(torneo);
            validarFinalizacion(torneo);
            torneo.setEstado(EstadoTorneo.FINALIZADO);
            torneoDAO.guardar(conexion, torneo);
            return torneo;
        }, "No se pudo finalizar el torneo.");
    }

    public ResumenCiclo resumenCiclo(long torneoId) {
        validarId(torneoId, "El ID del torneo debe ser positivo.");
        List<TorneoCategoria> categorias = categoriaDAO
                .listarActivasPorTorneo(torneoId);
        int categoriasCompetitivas = 0;
        int categoriasConCuadro = 0;
        int categoriasConCampeona = 0;
        int partidos = 0;
        int finalizados = 0;
        for (TorneoCategoria categoria : categorias) {
            int confirmadas = categoria.getParejasConfirmadas();
            if (confirmadas < 2) continue;
            categoriasCompetitivas++;
            List<TorneoPartido> cuadro = partidoDAO
                    .listarPorCategoria(categoria.getId());
            if (!cuadro.isEmpty()) categoriasConCuadro++;
            partidos += cuadro.size();
            finalizados += (int) cuadro.stream()
                    .filter(p -> p.getEstado()
                            == EstadoPartidoTorneo.FINALIZADO)
                    .count();
            boolean campeona = cuadro.stream()
                    .filter(p -> p.getFase() == FaseTorneo.FINAL)
                    .anyMatch(p -> p.getEstado()
                            == EstadoPartidoTorneo.FINALIZADO
                            && p.getGanadoraInscripcionId() != null);
            if (campeona) categoriasConCampeona++;
        }
        return new ResumenCiclo(categoriasCompetitivas,
                categoriasConCuadro, categoriasConCampeona,
                partidos, finalizados);
    }

    private void validarInicio(Torneo torneo) {
        long torneoId = torneo.getId();
        if (torneo.getEstado() != EstadoTorneo.INSCRIPCION_CERRADA) {
            throw new IllegalArgumentException(
                    "El torneo debe tener las inscripciones cerradas.");
        }
        List<TorneoCategoria> categorias = categoriaDAO
                .listarActivasPorTorneo(torneoId);
        if (categorias.isEmpty()) {
            throw new IllegalArgumentException(
                    "El torneo necesita al menos una categoria activa.");
        }
        int competitivas = 0;
        for (TorneoCategoria categoria : categorias) {
            int confirmadas = categoria.getParejasConfirmadas();
            if (confirmadas == 1) {
                throw new IllegalArgumentException(
                        "La categoria " + categoria.getNombre()
                                + " tiene una sola pareja confirmada.");
            }
            if (confirmadas < 2) continue;
            competitivas++;
            if (partidoDAO.listarPorCategoria(
                    categoria.getId()).isEmpty()) {
                throw new IllegalArgumentException(
                        "Falta generar el cuadro de la categoria "
                                + categoria.getNombre() + ".");
            }
        }
        if (competitivas == 0) {
            throw new IllegalArgumentException(
                    "No hay categorias con al menos dos parejas confirmadas.");
        }
    }

    private void validarFinalizacion(Torneo torneo) {
        long torneoId = torneo.getId();
        if (torneo.getEstado() != EstadoTorneo.EN_CURSO) {
            throw new IllegalArgumentException(
                    "El torneo debe estar en curso para finalizarlo.");
        }
        ResumenCiclo resumen = resumenCiclo(torneoId);
        if (resumen.categoriasCompetitivas() == 0) {
            throw new IllegalArgumentException(
                    "El torneo no tiene categorias competitivas.");
        }
        if (resumen.categoriasConCuadro()
                != resumen.categoriasCompetitivas()) {
            throw new IllegalArgumentException(
                    "Hay categorias sin cuadro generado.");
        }
        if (resumen.partidosFinalizados() != resumen.partidos()) {
            throw new IllegalArgumentException(
                    "Todavia hay " + resumen.partidosPendientes()
                            + " partido(s) sin finalizar.");
        }
        if (resumen.categoriasConCampeona()
                != resumen.categoriasCompetitivas()) {
            throw new IllegalArgumentException(
                    "Todas las categorias deben tener una campeona.");
        }
    }

    public record ResumenCiclo(
            int categoriasCompetitivas,
            int categoriasConCuadro,
            int categoriasConCampeona,
            int partidos,
            int partidosFinalizados) {
        public int partidosPendientes() {
            return Math.max(0, partidos - partidosFinalizados);
        }
    }

    public Torneo cancelar(long torneoId) {
        validarId(torneoId, "El ID del torneo debe ser positivo.");
        return ejecutar(conexion -> {
            Torneo torneo = buscarTorneo(conexion, torneoId);
            validarEditable(torneo);
            torneo.setEstado(EstadoTorneo.CANCELADO);
            torneoDAO.guardar(conexion, torneo);
            return torneo;
        }, "No se pudo cancelar el torneo.");
    }

    private Torneo cambiarEstado(
            long torneoId,
            EstadoTorneo nuevoEstado,
            EstadoTorneo estadoEsperado) {
        validarId(torneoId, "El ID del torneo debe ser positivo.");
        return ejecutar(conexion -> {
            Torneo torneo = buscarTorneo(conexion, torneoId);
            validarEditable(torneo);
            if (torneo.getEstado() != estadoEsperado) {
                throw new IllegalArgumentException(
                        "El cambio de estado del torneo no esta permitido.");
            }
            torneo.setEstado(nuevoEstado);
            torneoDAO.guardar(conexion, torneo);
            return torneo;
        }, "No se pudo cambiar el estado del torneo.");
    }

    public TorneoCategoria crearCategoria(TorneoCategoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria no puede ser nula.");
        }
        if (categoria.getId() > 0) {
            throw new IllegalArgumentException("La categoria nueva no puede tener ID.");
        }

        return ejecutar(conexion -> {
            Torneo torneo = buscarTorneo(conexion, categoria.getTorneoId());
            validarAdministracionCategorias(torneo);
            categoria.setActivo(true);
            categoriaDAO.guardar(conexion, categoria);
            return categoria;
        }, "No se pudo crear la categoria del torneo.");
    }

    public TorneoCategoria editarCategoria(TorneoCategoria cambios) {
        if (cambios == null) {
            throw new IllegalArgumentException("La categoria no puede ser nula.");
        }
        validarId(cambios.getId(), "El ID de la categoria debe ser positivo.");

        return ejecutar(conexion -> {
            TorneoCategoria existente = buscarCategoria(conexion, cambios.getId());
            Torneo torneo = buscarTorneo(conexion, existente.getTorneoId());
            validarAdministracionCategorias(torneo);

            if (cambios.getTorneoId() != existente.getTorneoId()) {
                throw new IllegalArgumentException(
                        "No se puede trasladar una categoria a otro torneo.");
            }
            if (cambios.getCupoParejas() < existente.getParejasConfirmadas()) {
                throw new IllegalArgumentException(
                        "El cupo no puede ser menor que las parejas confirmadas.");
            }

            existente.setNombre(cambios.getNombre());
            existente.setRama(cambios.getRama());
            existente.setCupoParejas(cambios.getCupoParejas());
            existente.setPrecioInscripcion(cambios.getPrecioInscripcion());
            categoriaDAO.guardar(conexion, existente);
            return existente;
        }, "No se pudo editar la categoria del torneo.");
    }

    public TorneoCategoria desactivarCategoria(long categoriaId) {
        validarId(categoriaId, "El ID de la categoria debe ser positivo.");

        return ejecutar(conexion -> {
            TorneoCategoria categoria = buscarCategoria(conexion, categoriaId);
            Torneo torneo = buscarTorneo(conexion, categoria.getTorneoId());
            validarAdministracionCategorias(torneo);

            int inscripcionesActivas = inscripcionDAO.contarPorCategoriaYEstados(
                    conexion, categoriaId, ESTADOS_ACTIVOS);
            if (inscripcionesActivas > 0) {
                throw new IllegalArgumentException(
                        "No se puede desactivar una categoria con inscripciones activas.");
            }

            categoria.setActivo(false);
            categoriaDAO.guardar(conexion, categoria);
            return categoria;
        }, "No se pudo desactivar la categoria del torneo.");
    }

    private Torneo buscarTorneo(Connection conexion, long torneoId) {
        validarId(torneoId, "El ID del torneo debe ser positivo.");
        Torneo torneo = torneoDAO.buscar(conexion, torneoId);
        if (torneo == null) {
            throw new IllegalArgumentException("El torneo no existe.");
        }
        return torneo;
    }

    private TorneoCategoria buscarCategoria(Connection conexion, long categoriaId) {
        TorneoCategoria categoria = categoriaDAO.buscar(conexion, categoriaId);
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria del torneo no existe.");
        }
        return categoria;
    }

    private void validarEditable(Torneo torneo) {
        if (!torneo.isActivo()) {
            throw new IllegalArgumentException("El torneo no esta activo.");
        }
        if (torneo.getEstado().esFinal()) {
            throw new IllegalArgumentException(
                    "No se puede editar un torneo finalizado o cancelado.");
        }
    }

    private void validarAdministracionCategorias(Torneo torneo) {
        validarEditable(torneo);
        if (torneo.getEstado() == EstadoTorneo.EN_CURSO) {
            throw new IllegalArgumentException(
                    "No se pueden modificar categorias con el torneo en curso.");
        }
    }

    private void validarId(long id, String mensaje) {
        if (id <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private <T> T ejecutar(Operacion<T> operacion, String mensajeSql) {
        try (Connection conexion = proveedorConexion.obtener()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (SQLException | RuntimeException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(mensajeSql, exception);
        }
    }

    private void rollbackSeguro(Connection conexion, Exception original) {
        try {
            conexion.rollback();
        } catch (SQLException rollback) {
            original.addSuppressed(rollback);
        }
    }

    private void restaurarAutoCommit(Connection conexion) {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }

    @FunctionalInterface
    public interface ProveedorConexion {
        Connection obtener();
    }
}
