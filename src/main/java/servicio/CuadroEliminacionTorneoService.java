package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import config.ConexionBD;
import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoGrupoDAO;
import dao.TorneoGrupoDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.EstadoClasificacionGrupo;
import negocio.EstadoInscripcionTorneo;
import negocio.EstadoPartidoTorneo;
import negocio.EstadoTorneo;
import negocio.FaseTorneo;
import negocio.PosicionGrupoTorneo;
import negocio.PosicionPartidoSiguiente;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoGrupo;
import negocio.TorneoPartido;

public class CuadroEliminacionTorneoService {
    private static final int MAXIMO_PAREJAS = 32;

    private final TorneoDAO torneoDAO;
    private final TorneoCategoriaDAO categoriaDAO;
    private final TorneoPartidoDAO partidoDAO;
    private final TorneoGrupoDAO grupoDAO;
    private final PosicionesGrupoTorneoService posicionesService;

    public CuadroEliminacionTorneoService() {
        this(new TorneoDAOMySQL(), new TorneoCategoriaDAOMySQL(),
                new TorneoPartidoDAOMySQL(), new TorneoGrupoDAOMySQL(),
                new PosicionesGrupoTorneoService());
    }

    public CuadroEliminacionTorneoService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoPartidoDAO partidoDAO) {
        this(torneoDAO, categoriaDAO, partidoDAO,
                new TorneoGrupoDAOMySQL(),
                new PosicionesGrupoTorneoService());
    }

    CuadroEliminacionTorneoService(
            TorneoDAO torneoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoPartidoDAO partidoDAO,
            TorneoGrupoDAO grupoDAO,
            PosicionesGrupoTorneoService posicionesService) {
        if (torneoDAO == null || categoriaDAO == null
                || partidoDAO == null || grupoDAO == null
                || posicionesService == null) {
            throw new IllegalArgumentException(
                    "Las dependencias del cuadro no pueden ser nulas.");
        }
        this.torneoDAO = torneoDAO;
        this.categoriaDAO = categoriaDAO;
        this.partidoDAO = partidoDAO;
        this.grupoDAO = grupoDAO;
        this.posicionesService = posicionesService;
    }

    public List<TorneoPartido> generarCuadro(long categoriaId) {
        if (categoriaId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la categoria debe ser positivo.");
        }
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                bloquearCategoria(conexion, categoriaId);
                TorneoCategoria categoria = categoriaDAO.buscar(
                        conexion, categoriaId);
                if (categoria == null || !categoria.isActivo()) {
                    throw new IllegalArgumentException(
                            "La categoria no existe o esta inactiva.");
                }
                Torneo torneo = torneoDAO.buscar(
                        conexion, categoria.getTorneoId());
                validarTorneo(torneo);
                if (existeCuadroEliminatorio(conexion, categoriaId)) {
                    throw new IllegalArgumentException(
                            "La categoria ya tiene un cuadro eliminatorio generado.");
                }

                List<Clasificada> clasificadas = categoria.usaFaseGrupos()
                        ? listarClasificadasDeGrupos(conexion, categoriaId)
                        : listarConfirmadas(conexion, categoriaId);
                validarCantidadParejas(clasificadas.size());
                List<Long> parejas = ordenarParaPrimeraRonda(clasificadas,
                        calcularTamanoCuadro(clasificadas.size()));

                int tamano = calcularTamanoCuadro(parejas.size());
                FaseTorneo faseInicial = faseInicial(tamano);
                Map<FaseTorneo, List<TorneoPartido>> rondas = crearRondas(conexion, categoriaId, faseInicial);
                asignarPrimeraRonda(conexion, rondas.get(faseInicial),
                        parejas, tamano);
                conexion.commit();
            } catch (RuntimeException | SQLException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo generar el cuadro del torneo.", exception);
        }
        return partidoDAO.listarPorCategoria(categoriaId);
    }

    public static int calcularTamanoCuadro(int cantidadParejas) {
        if (cantidadParejas < 2 || cantidadParejas > MAXIMO_PAREJAS) {
            throw new IllegalArgumentException(
                    "La cantidad de parejas debe estar entre 2 y 32.");
        }
        int tamano = 2;
        while (tamano < cantidadParejas)
            tamano *= 2;
        return tamano;
    }

    public static FaseTorneo faseInicial(int tamanoCuadro) {
        return switch (tamanoCuadro) {
            case 2 -> FaseTorneo.FINAL;
            case 4 -> FaseTorneo.SEMIFINAL;
            case 8 -> FaseTorneo.CUARTOS;
            case 16 -> FaseTorneo.OCTAVOS;
            case 32 -> FaseTorneo.DIECISEISAVOS;
            default -> throw new IllegalArgumentException(
                    "El tamano del cuadro debe ser 2, 4, 8, 16 o 32.");
        };
    }

    private List<Clasificada> listarClasificadasDeGrupos(
            Connection conexion,
            long categoriaId) {
        List<TorneoGrupo> grupos = grupoDAO.listar(conexion, categoriaId);
        if (grupos.isEmpty()) {
            throw new IllegalArgumentException(
                    "La categoria no tiene grupos configurados.");
        }
        List<Clasificada> clasificadas = new ArrayList<>();
        for (TorneoGrupo grupo : grupos) {
            if (!grupo.isConfirmado()) {
                throw new IllegalArgumentException(
                        grupo.getNombre() + " no esta confirmado.");
            }
            PosicionesGrupoTorneoService.Resultado resultado = posicionesService.calcular(grupo);
            if (!resultado.definitivo()) {
                throw new IllegalArgumentException(
                        "Faltan finalizar partidos de " + grupo.getNombre() + ".");
            }
            if (resultado.desempatePendiente()) {
                throw new IllegalArgumentException(
                        grupo.getNombre()
                                + " tiene un desempate administrativo pendiente.");
            }
            for (PosicionGrupoTorneo posicion : resultado.posiciones()) {
                if (posicion.estado() == EstadoClasificacionGrupo.CLASIFICADO) {
                    clasificadas.add(new Clasificada(
                            posicion.inscripcionId(), grupo.getId(),
                            posicion.posicion()));
                }
            }
        }
        return clasificadas;
    }

    private List<Clasificada> listarConfirmadas(
            Connection conexion,
            long categoriaId) throws SQLException {
        List<Clasificada> resultado = new ArrayList<>();
        for (Long id : listarParejasConfirmadas(conexion, categoriaId)) {
            resultado.add(new Clasificada(id, 0, 1));
        }
        Collections.shuffle(resultado);
        return resultado;
    }

    private List<Long> ordenarParaPrimeraRonda(
            List<Clasificada> clasificadas,
            int tamanoCuadro) {
        List<Clasificada> primeros = clasificadas.stream()
                .filter(c -> c.posicionGrupo() == 1)
                .collect(java.util.stream.Collectors.toCollection(
                        ArrayList::new));
        List<Clasificada> resto = clasificadas.stream()
                .filter(c -> c.posicionGrupo() != 1)
                .collect(java.util.stream.Collectors.toCollection(
                        ArrayList::new));
        Collections.shuffle(primeros);
        Collections.shuffle(resto);

        int byes = tamanoCuadro - clasificadas.size();
        List<Long> orden = new ArrayList<>();
        Set<Long> usados = new HashSet<>();
        for (int i = 0; i < byes; i++) {
            Clasificada elegida = i < primeros.size()
                    ? primeros.get(i)
                    : primeraNoUsada(clasificadas, usados);
            orden.add(elegida.inscripcionId());
            usados.add(elegida.inscripcionId());
        }

        List<Clasificada> pendientes = new ArrayList<>();
        pendientes.addAll(primeros);
        pendientes.addAll(resto);
        pendientes.removeIf(c -> usados.contains(c.inscripcionId()));
        while (!pendientes.isEmpty()) {
            Clasificada a = pendientes.remove(0);
            Clasificada b = elegirRival(a, pendientes);
            pendientes.remove(b);
            orden.add(a.inscripcionId());
            orden.add(b.inscripcionId());
        }
        return orden;
    }

    private Clasificada primeraNoUsada(
            List<Clasificada> valores,
            Set<Long> usados) {
        return valores.stream().filter(c -> !usados.contains(c.inscripcionId()))
                .findFirst().orElseThrow();
    }

    private Clasificada elegirRival(
            Clasificada origen,
            List<Clasificada> candidatas) {
        return candidatas.stream()
                .filter(c -> origen.grupoId() == 0
                        || c.grupoId() != origen.grupoId())
                .findFirst().orElse(candidatas.get(0));
    }

    private Map<FaseTorneo, List<TorneoPartido>> crearRondas(
            Connection conexion, long categoriaId,
            FaseTorneo faseInicial) {
        List<FaseTorneo> fases = fasesDesde(faseInicial);
        Map<FaseTorneo, List<TorneoPartido>> rondas = new EnumMap<>(FaseTorneo.class);
        for (int indice = fases.size() - 1; indice >= 0; indice--) {
            FaseTorneo fase = fases.get(indice);
            int cantidadPartidos = fase.getCantidadParejas() / 2;
            List<TorneoPartido> partidos = new ArrayList<>();
            List<TorneoPartido> siguiente = fase.siguiente() == null
                    ? List.of()
                    : rondas.get(fase.siguiente());
            for (int orden = 0; orden < cantidadPartidos; orden++) {
                TorneoPartido partido = new TorneoPartido();
                partido.setTorneoCategoriaId(categoriaId);
                partido.setFase(fase);
                partido.setOrdenFase(orden + 1);
                partido.setEstado(EstadoPartidoTorneo.PENDIENTE);
                if (!siguiente.isEmpty()) {
                    TorneoPartido destino = siguiente.get(orden / 2);
                    partido.setPartidoSiguienteId(destino.getId());
                    partido.setPosicionSiguiente(orden % 2 == 0
                            ? PosicionPartidoSiguiente.PAREJA_1
                            : PosicionPartidoSiguiente.PAREJA_2);
                }
                partidoDAO.guardar(conexion, partido);
                partidos.add(partido);
            }
            rondas.put(fase, partidos);
        }
        return rondas;
    }

    private List<FaseTorneo> fasesDesde(FaseTorneo inicial) {
        List<FaseTorneo> fases = new ArrayList<>();
        FaseTorneo actual = inicial;
        while (actual != null) {
            fases.add(actual);
            actual = actual.siguiente();
        }
        return fases;
    }

    private void asignarPrimeraRonda(Connection conexion,
            List<TorneoPartido> partidos, List<Long> parejas,
            int tamanoCuadro) {
        int cantidadByes = tamanoCuadro - parejas.size();
        int cursor = 0;
        for (int indice = 0; indice < partidos.size(); indice++) {
            TorneoPartido partido = partidos.get(indice);
            partido.setPareja1InscripcionId(parejas.get(cursor++));
            if (indice < cantidadByes) {
                partido.setBye(true);
                partido.setGanadoraInscripcionId(
                        partido.getPareja1InscripcionId());
                partido.setEstado(EstadoPartidoTorneo.FINALIZADO);
                partido.setFechaFinalizacion(LocalDateTime.now());
            } else {
                partido.setPareja2InscripcionId(parejas.get(cursor++));
            }
            partidoDAO.guardar(conexion, partido);
            if (partido.isBye())
                avanzarGanadora(conexion, partido);
        }
    }

    private void avanzarGanadora(Connection conexion,
            TorneoPartido partido) {
        if (partido.getPartidoSiguienteId() == null)
            return;
        TorneoPartido siguiente = partidoDAO.buscarParaActualizar(
                conexion, partido.getPartidoSiguienteId());
        if (siguiente == null) {
            throw new IllegalStateException(
                    "No se encontro el partido siguiente.");
        }
        Long ganadora = partido.getGanadoraInscripcionId();
        if (partido.getPosicionSiguiente() == PosicionPartidoSiguiente.PAREJA_1) {
            if (siguiente.getPareja1InscripcionId() != null) {
                throw new IllegalStateException(
                        "La posicion de destino ya esta ocupada.");
            }
            siguiente.setPareja1InscripcionId(ganadora);
        } else {
            if (siguiente.getPareja2InscripcionId() != null) {
                throw new IllegalStateException(
                        "La posicion de destino ya esta ocupada.");
            }
            siguiente.setPareja2InscripcionId(ganadora);
        }
        partidoDAO.guardar(conexion, siguiente);
    }

    private List<Long> listarParejasConfirmadas(Connection conexion,
            long categoriaId) throws SQLException {
        String sql = "SELECT ti.id FROM torneo_inscripciones ti "
                + "WHERE ti.torneo_categoria_id = ? AND ti.estado = ? "
                + "AND (SELECT COUNT(*) FROM torneo_inscripcion_jugadores tij "
                + "WHERE tij.inscripcion_id = ti.id "
                + "AND tij.orden_integrante IN (1, 2)) = 2 "
                + "ORDER BY ti.fecha_confirmacion ASC, ti.id ASC";
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            sentencia.setString(2,
                    EstadoInscripcionTorneo.CONFIRMADA.name());
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next())
                    ids.add(resultado.getLong(1));
            }
        }
        return ids;
    }

    private void bloquearCategoria(Connection conexion,
            long categoriaId) throws SQLException {
        String sql = "SELECT id FROM torneo_categorias WHERE id = ? FOR UPDATE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    throw new IllegalArgumentException(
                            "La categoria no existe.");
                }
            }
        }
    }

    private boolean existeCuadroEliminatorio(Connection conexion,
            long categoriaId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM torneo_partidos "
                + "WHERE torneo_categoria_id = ? AND fase <> 'GRUPOS'";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }

    private void validarTorneo(Torneo torneo) {
        if (torneo == null || !torneo.isActivo()) {
            throw new IllegalArgumentException(
                    "El torneo no existe o esta inactivo.");
        }
        if (torneo.getEstado() != EstadoTorneo.INSCRIPCION_CERRADA
                && torneo.getEstado() != EstadoTorneo.EN_CURSO) {
            throw new IllegalArgumentException(
                    "El cuadro solo puede generarse con la inscripcion "
                            + "cerrada o el torneo en curso.");
        }
    }

    private void validarCantidadParejas(int cantidad) {
        if (cantidad < 2) {
            throw new IllegalArgumentException(
                    "Se necesitan al menos dos parejas clasificadas.");
        }
        if (cantidad > MAXIMO_PAREJAS) {
            throw new IllegalArgumentException(
                    "La primera version admite hasta 32 parejas.");
        }
    }

    private void rollbackSeguro(Connection conexion,
            Exception original) {
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

    private record Clasificada(
            long inscripcionId,
            long grupoId,
            int posicionGrupo) {
    }
}
