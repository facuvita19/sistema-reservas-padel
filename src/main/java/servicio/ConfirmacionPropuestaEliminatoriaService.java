package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.ConexionBD;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.ClasificadoEtapaEliminatoria;
import negocio.CrucePropuestoTorneo;
import negocio.EstadoPartidoTorneo;
import negocio.FaseTorneo;
import negocio.PosicionPartidoSiguiente;
import negocio.PropuestaEtapaEliminatoria;
import negocio.TorneoPartido;

public class ConfirmacionPropuestaEliminatoriaService {
    @FunctionalInterface
    interface ProveedorConexion {
        Connection obtener() throws SQLException;
    }

    private final TorneoPartidoDAO partidoDAO;
    private final ValidadorEstructuraEliminatoriaService validador;
    private final ProveedorConexion proveedorConexion;

    public ConfirmacionPropuestaEliminatoriaService() {
        this(new TorneoPartidoDAOMySQL(),
                new ValidadorEstructuraEliminatoriaService(),
                ConexionBD::obtenerConexion);
    }

    ConfirmacionPropuestaEliminatoriaService(
            TorneoPartidoDAO partidoDAO,
            ValidadorEstructuraEliminatoriaService validador,
            ProveedorConexion proveedorConexion) {
        if (partidoDAO == null || validador == null
                || proveedorConexion == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de confirmacion son obligatorias.");
        }
        this.partidoDAO = partidoDAO;
        this.validador = validador;
        this.proveedorConexion = proveedorConexion;
    }

    public List<TorneoPartido> confirmar(long categoriaId,
            PropuestaEtapaEliminatoria propuesta) {
        validarEntrada(categoriaId, propuesta);
        try (Connection conexion = proveedorConexion.obtener()) {
            conexion.setAutoCommit(false);
            try {
                validarSinCuadro(conexion, categoriaId);
                Map<String, Long> participantes = referencias(propuesta);
                Map<String, TorneoPartido> creados = new HashMap<>();
                List<CrucePropuestoTorneo> cruces = propuesta.getCruces();
                for (int i = cruces.size() - 1; i >= 0; i--) {
                    CrucePropuestoTorneo cruce = cruces.get(i);
                    TorneoPartido partido = crearPartido(categoriaId, cruce,
                            participantes);
                    partidoDAO.guardar(conexion, partido);
                    creados.put(clave(cruce), partido);
                }
                conectar(conexion, cruces, creados);
                conexion.commit();
            } catch (RuntimeException | SQLException exception) {
                try { conexion.rollback(); } catch (SQLException rollback) {
                    exception.addSuppressed(rollback);
                }
                throw exception;
            } finally {
                try { conexion.setAutoCommit(true); } catch (SQLException ignored) { }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo confirmar el cuadro.", exception);
        }
        return partidoDAO.listarPorCategoria(categoriaId);
    }

    private TorneoPartido crearPartido(long categoriaId,
            CrucePropuestoTorneo cruce,
            Map<String, Long> participantes) {
        TorneoPartido partido = new TorneoPartido();
        partido.setTorneoCategoriaId(categoriaId);
        partido.setFase(fase(cruce.getInstancia()));
        partido.setOrdenFase(cruce.getOrden());
        partido.setEstado(EstadoPartidoTorneo.PENDIENTE);
        Long pareja1 = participantes.get(cruce.getParticipante1());
        Long pareja2 = participantes.get(cruce.getParticipante2());
        if (!esReferenciaGanador(cruce.getParticipante1()) && pareja1 == null) {
            throw new IllegalArgumentException("Participante no reconocido: "
                    + cruce.getParticipante1());
        }
        if (!esReferenciaGanador(cruce.getParticipante2()) && pareja2 == null) {
            throw new IllegalArgumentException("Participante no reconocido: "
                    + cruce.getParticipante2());
        }
        partido.setPareja1InscripcionId(pareja1);
        partido.setPareja2InscripcionId(pareja2);
        return partido;
    }

    private boolean esReferenciaGanador(String referencia) {
        return referencia != null && referencia.startsWith("Ganador ");
    }

    private void conectar(Connection conexion,
            List<CrucePropuestoTorneo> cruces,
            Map<String, TorneoPartido> creados) {
        for (CrucePropuestoTorneo destinoDef : cruces) {
            enlazarReferencia(conexion, destinoDef.getParticipante1(),
                    creados.get(clave(destinoDef)),
                    PosicionPartidoSiguiente.PAREJA_1, creados);
            enlazarReferencia(conexion, destinoDef.getParticipante2(),
                    creados.get(clave(destinoDef)),
                    PosicionPartidoSiguiente.PAREJA_2, creados);
        }
    }

    private void enlazarReferencia(Connection conexion, String referencia,
            TorneoPartido destino, PosicionPartidoSiguiente posicion,
            Map<String, TorneoPartido> creados) {
        if (!referencia.startsWith("Ganador ")) return;
        String resto = referencia.substring("Ganador ".length());
        int separador = resto.lastIndexOf(" #");
        if (separador < 0) throw new IllegalArgumentException(
                "Referencia de ganador invalida: " + referencia);
        String instancia = resto.substring(0, separador);
        int orden = Integer.parseInt(resto.substring(separador + 2));
        TorneoPartido origen = creados.get(instancia + "#" + orden);
        if (origen == null) throw new IllegalArgumentException(
                "No se encontro el partido de origen " + referencia + ".");
        origen.setPartidoSiguienteId(destino.getId());
        origen.setPosicionSiguiente(posicion);
        partidoDAO.guardar(conexion, origen);
    }

    private Map<String, Long> referencias(
            PropuestaEtapaEliminatoria propuesta) {
        Map<String, Long> resultado = new HashMap<>();
        for (ClasificadoEtapaEliminatoria c : propuesta.getClasificados()) {
            if (resultado.put(c.referencia(), c.inscripcionId()) != null) {
                throw new IllegalArgumentException(
                        "Hay referencias deportivas repetidas.");
            }
        }
        return resultado;
    }

    private FaseTorneo fase(String instancia) {
        if (instancia.startsWith("Acceso R")) {
            int numero = Integer.parseInt(instancia.substring(8));
            if (numero < 1 || numero > 5) {
                throw new IllegalArgumentException(
                        "Solo se admiten hasta cinco rondas de acceso.");
            }
            return FaseTorneo.valueOf("ACCESO_" + numero);
        }
        return switch (instancia) {
            case "Dieciseisavos" -> FaseTorneo.DIECISEISAVOS;
            case "Octavos" -> FaseTorneo.OCTAVOS;
            case "Cuartos" -> FaseTorneo.CUARTOS;
            case "Semifinal" -> FaseTorneo.SEMIFINAL;
            case "Final" -> FaseTorneo.FINAL;
            default -> throw new IllegalArgumentException(
                    "Instancia no reconocida: " + instancia);
        };
    }

    private String clave(CrucePropuestoTorneo cruce) {
        return cruce.getInstancia() + "#" + cruce.getOrden();
    }

    private void validarEntrada(long categoriaId,
            PropuestaEtapaEliminatoria propuesta) {
        if (categoriaId <= 0 || propuesta == null || !propuesta.isValida()) {
            throw new IllegalArgumentException(
                    "La categoria y una propuesta valida son obligatorias.");
        }
        List<String> errores = validador.validar(propuesta.getCruces(),
                propuesta.getClasificados());
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(
                    "La estructura eliminatoria no es valida: "
                    + String.join(" ", errores));
        }
    }

    private void validarSinCuadro(Connection conexion, long categoriaId)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM torneo_partidos "
                + "WHERE torneo_categoria_id = ? AND fase <> 'GRUPOS'";
        try (var sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (var resultado = sentencia.executeQuery()) {
                resultado.next();
                if (resultado.getInt(1) > 0) throw new IllegalArgumentException(
                        "La categoria ya tiene un cuadro eliminatorio.");
            }
        }
    }
}
