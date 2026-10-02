package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import config.ConexionBD;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import dao.TorneoPartidoSetDAO;
import dao.TorneoPartidoSetDAOMySQL;
import dao.CorreccionResultadoTorneoDAO;
import dao.CorreccionResultadoTorneoDAOMySQL;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAOMySQL;
import negocio.CorreccionResultadoTorneo;
import negocio.EstadoTorneo;
import negocio.EstadoPartidoTorneo;
import negocio.PosicionPartidoSiguiente;
import negocio.TipoSetTorneo;
import negocio.TorneoPartido;
import negocio.TorneoPartidoSet;

public class ResultadoPartidoTorneoService {

    private final TorneoPartidoDAO partidoDAO;
    private final TorneoPartidoSetDAO setDAO;
    private final CorreccionResultadoTorneoDAO correccionDAO =
            new CorreccionResultadoTorneoDAOMySQL();

    public ResultadoPartidoTorneoService() {
        this(new TorneoPartidoDAOMySQL(),
                new TorneoPartidoSetDAOMySQL());
    }

    public ResultadoPartidoTorneoService(
            TorneoPartidoDAO partidoDAO,
            TorneoPartidoSetDAO setDAO) {
        if (partidoDAO == null || setDAO == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de resultados no pueden ser nulas.");
        }
        this.partidoDAO = partidoDAO;
        this.setDAO = setDAO;
    }

    public TorneoPartido registrarResultado(
            long partidoId,
            List<TorneoPartidoSet> sets,
            long usuarioId,
            String observaciones) {
        if (partidoId <= 0 || usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El partido y el usuario son obligatorios.");
        }
        int ladoGanador = determinarLadoGanador(sets);

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                TorneoPartido partido = partidoDAO.buscarParaActualizar(
                        conexion, partidoId);
                validarPartido(partido);

                Long ganadora = ladoGanador == 1
                        ? partido.getPareja1InscripcionId()
                        : partido.getPareja2InscripcionId();

                setDAO.reemplazarPorPartido(conexion, partidoId, sets);
                partido.setSets(sets);
                partido.setGanadoraInscripcionId(ganadora);
                partido.setUsuarioResultadoId(usuarioId);
                partido.setFechaFinalizacion(LocalDateTime.now());
                partido.setObservaciones(limpiarObservaciones(observaciones));
                partido.setEstado(EstadoPartidoTorneo.FINALIZADO);
                partidoDAO.guardar(conexion, partido);

                avanzarGanadora(conexion, partido);
                conexion.commit();
            } catch (RuntimeException | SQLException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo registrar el resultado.", exception);
        }

        return partidoDAO.buscar(partidoId);
    }

    public TorneoPartido corregirResultado(
            long partidoId,
            List<TorneoPartidoSet> sets,
            long usuarioId,
            String observaciones,
            String motivo) {
        if (partidoId <= 0 || usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El partido y el usuario son obligatorios.");
        }
        int ladoGanador = determinarLadoGanador(sets);
        String motivoLimpio = validarMotivo(motivo);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                TorneoPartido partido = partidoDAO.buscarParaActualizar(
                        conexion, partidoId);
                validarCorreccion(partido);
                validarTorneoEnCurso(partido);
                List<TorneoPartidoSet> setsAnteriores = partido.getSets();
                Long ganadoraAnterior = partido.getGanadoraInscripcionId();
                Long ganadoraNueva = ladoGanador == 1
                        ? partido.getPareja1InscripcionId()
                        : partido.getPareja2InscripcionId();
                corregirAvance(conexion, partido,
                        ganadoraAnterior, ganadoraNueva);
                setDAO.reemplazarPorPartido(conexion, partidoId, sets);
                partido.setSets(sets);
                partido.setGanadoraInscripcionId(ganadoraNueva);
                partido.setUsuarioResultadoId(usuarioId);
                partido.setFechaFinalizacion(LocalDateTime.now());
                partido.setObservaciones(limpiarObservaciones(observaciones));
                partidoDAO.guardar(conexion, partido);
                guardarAuditoria(conexion, partidoId, usuarioId, motivoLimpio,
                        ganadoraAnterior, ganadoraNueva, setsAnteriores, sets);
                conexion.commit();
            } catch (RuntimeException | SQLException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo corregir el resultado.", exception);
        }
        return partidoDAO.buscar(partidoId);
    }

    private void validarTorneoEnCurso(TorneoPartido partido) {
        var categoria = new TorneoCategoriaDAOMySQL().buscar(
                partido.getTorneoCategoriaId());
        var torneo = categoria == null ? null
                : new TorneoDAOMySQL().buscar(categoria.getTorneoId());
        if (torneo == null || torneo.getEstado() != EstadoTorneo.EN_CURSO) {
            throw new IllegalArgumentException(
                    "Solo pueden corregirse resultados de un torneo en curso.");
        }
    }

    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) return null;
        String limpio = motivo.trim().replaceAll("\s+", " ");
        if (limpio.length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar 500 caracteres.");
        }
        return limpio;
    }

    private void guardarAuditoria(Connection conexion, long partidoId,
            long usuarioId, String motivo, Long anterior, Long nueva,
            List<TorneoPartidoSet> setsAnteriores,
            List<TorneoPartidoSet> setsNuevos) {
        CorreccionResultadoTorneo c = new CorreccionResultadoTorneo();
        c.setPartidoId(partidoId); c.setUsuarioId(usuarioId); c.setMotivo(motivo);
        c.setGanadoraAnteriorInscripcionId(anterior);
        c.setGanadoraNuevaInscripcionId(nueva);
        c.setResultadoAnterior(resultado(setsAnteriores));
        c.setResultadoNuevo(resultado(setsNuevos));
        c.setSetsAnterioresJson(jsonSets(setsAnteriores));
        c.setSetsNuevosJson(jsonSets(setsNuevos));
        correccionDAO.guardar(conexion, c);
    }

    private String resultado(List<TorneoPartidoSet> sets) {
        return sets.stream().map(x -> x.getPuntosPareja1() + "-"
                + x.getPuntosPareja2()).reduce((a,b) -> a + " / " + b)
                .orElse("Sin resultado");
    }

    private String jsonSets(List<TorneoPartidoSet> sets) {
        return sets.stream().map(x -> "{\"numero\":" + x.getNumeroSet()
                + ",\"tipo\":\"" + x.getTipo().name()
                + "\",\"pareja1\":" + x.getPuntosPareja1()
                + ",\"pareja2\":" + x.getPuntosPareja2() + "}")
                .reduce((a,b) -> a + "," + b).map(x -> "[" + x + "]")
                .orElse("[]");
    }

    private void validarCorreccion(TorneoPartido partido) {
        if (partido == null) {
            throw new IllegalArgumentException("El partido no existe.");
        }
        if (partido.isBye() || !partido.tieneDosParejas()
                || partido.getEstado() != EstadoPartidoTorneo.FINALIZADO
                || partido.getGanadoraInscripcionId() == null) {
            throw new IllegalArgumentException(
                    "Solo puede corregirse un partido finalizado manualmente.");
        }
    }

    private void corregirAvance(Connection conexion,
            TorneoPartido partido, Long anterior, Long nueva) {
        if (partido.getPartidoSiguienteId() == null) return;
        TorneoPartido siguiente = partidoDAO.buscarParaActualizar(
                conexion, partido.getPartidoSiguienteId());
        if (siguiente == null) {
            throw new IllegalStateException(
                    "No se encontro el partido siguiente.");
        }
        if (siguiente.getEstado() != EstadoPartidoTorneo.PENDIENTE
                || siguiente.estaProgramado()
                || siguiente.getGanadoraInscripcionId() != null) {
            throw new IllegalArgumentException(
                    "No puede corregirse porque el partido siguiente ya fue programado o disputado.");
        }
        if (partido.getPosicionSiguiente()
                == PosicionPartidoSiguiente.PAREJA_1) {
            if (siguiente.getPareja1InscripcionId() != null
                    && !siguiente.getPareja1InscripcionId().equals(anterior)) {
                throw new IllegalArgumentException(
                        "La posicion siguiente fue modificada por otro proceso.");
            }
            siguiente.setPareja1InscripcionId(nueva);
        } else if (partido.getPosicionSiguiente()
                == PosicionPartidoSiguiente.PAREJA_2) {
            if (siguiente.getPareja2InscripcionId() != null
                    && !siguiente.getPareja2InscripcionId().equals(anterior)) {
                throw new IllegalArgumentException(
                        "La posicion siguiente fue modificada por otro proceso.");
            }
            siguiente.setPareja2InscripcionId(nueva);
        } else {
            throw new IllegalStateException(
                    "El partido no tiene una posicion de avance valida.");
        }
        partidoDAO.guardar(conexion, siguiente);
    }

    public static int determinarLadoGanador(List<TorneoPartidoSet> sets) {
        validarSets(sets);
        int ganados1 = 0;
        int ganados2 = 0;
        for (TorneoPartidoSet set : sets) {
            validarMarcador(set);
            if (set.parejaGanadora() == 1) ganados1++;
            else ganados2++;
        }
        if (ganados1 == 2 && ganados2 <= 1) return 1;
        if (ganados2 == 2 && ganados1 <= 1) return 2;
        throw new IllegalArgumentException(
                "El resultado debe definir ganadores por dos sets.");
    }

    private static void validarSets(List<TorneoPartidoSet> sets) {
        if (sets == null || sets.size() < 2 || sets.size() > 3) {
            throw new IllegalArgumentException(
                    "El partido debe tener dos o tres sets.");
        }
        Set<Integer> numeros = new HashSet<>();
        for (TorneoPartidoSet set : sets) {
            if (set == null || set.getNumeroSet() < 1
                    || set.getNumeroSet() > 3
                    || !numeros.add(set.getNumeroSet())) {
                throw new IllegalArgumentException(
                        "Los sets deben numerarse del 1 al 3 sin repetirse.");
            }
        }
        for (int numero = 1; numero <= sets.size(); numero++) {
            if (!numeros.contains(numero)) {
                throw new IllegalArgumentException(
                        "La numeracion de los sets debe ser consecutiva.");
            }
        }
    }

    private static void validarMarcador(TorneoPartidoSet set) {
        int mayor = Math.max(set.getPuntosPareja1(), set.getPuntosPareja2());
        int menor = Math.min(set.getPuntosPareja1(), set.getPuntosPareja2());
        if (menor < 0 || mayor == menor) {
            throw new IllegalArgumentException(
                    "El marcador del set no es valido.");
        }
        if (set.getTipo() == TipoSetTorneo.SUPER_TIE_BREAK) {
            if (mayor < 10 || mayor - menor < 2) {
                throw new IllegalArgumentException(
                        "El super tie-break requiere al menos 10 puntos y diferencia de 2.");
            }
            return;
        }
        boolean normal = (mayor == 6 && menor <= 4)
                || (mayor == 7 && (menor == 5 || menor == 6));
        if (!normal) {
            throw new IllegalArgumentException(
                    "El set normal debe terminar 6-0 a 6-4, 7-5 o 7-6.");
        }
    }

    private void validarPartido(TorneoPartido partido) {
        if (partido == null) {
            throw new IllegalArgumentException("El partido no existe.");
        }
        if (partido.isBye() || !partido.tieneDosParejas()) {
            throw new IllegalArgumentException(
                    "El partido no admite un resultado manual.");
        }
        if (partido.getEstado() != EstadoPartidoTorneo.PROGRAMADO
                && partido.getEstado() != EstadoPartidoTorneo.EN_CURSO) {
            throw new IllegalArgumentException(
                    "El partido debe estar programado o en curso.");
        }
    }

    private void avanzarGanadora(
            Connection conexion,
            TorneoPartido partido) {
        if (partido.getPartidoSiguienteId() == null) return;
        TorneoPartido siguiente = partidoDAO.buscarParaActualizar(
                conexion, partido.getPartidoSiguienteId());
        if (siguiente == null) {
            throw new IllegalStateException(
                    "No se encontro el partido siguiente.");
        }
        if (siguiente.getEstado() != EstadoPartidoTorneo.PENDIENTE) {
            throw new IllegalArgumentException(
                    "No puede modificarse un partido posterior ya iniciado.");
        }

        Long ganadora = partido.getGanadoraInscripcionId();
        if (partido.getPosicionSiguiente()
                == PosicionPartidoSiguiente.PAREJA_1) {
            validarDestino(siguiente.getPareja1InscripcionId(), ganadora);
            siguiente.setPareja1InscripcionId(ganadora);
        } else if (partido.getPosicionSiguiente()
                == PosicionPartidoSiguiente.PAREJA_2) {
            validarDestino(siguiente.getPareja2InscripcionId(), ganadora);
            siguiente.setPareja2InscripcionId(ganadora);
        } else {
            throw new IllegalStateException(
                    "El partido no tiene una posicion de avance valida.");
        }
        partidoDAO.guardar(conexion, siguiente);
    }

    private void validarDestino(Long actual, Long ganadora) {
        if (actual != null && !actual.equals(ganadora)) {
            throw new IllegalArgumentException(
                    "La posicion del partido siguiente ya esta ocupada.");
        }
    }

    private String limpiarObservaciones(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim().replaceAll("\\s+", " ");
        if (limpio.length() > 500) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar 500 caracteres.");
        }
        return limpio;
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
}
