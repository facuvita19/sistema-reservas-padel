package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import config.ConexionBD;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.FaseTorneo;
import negocio.TorneoPartido;

public class BloqueoCorreccionGrupoService {
    private final TorneoPartidoDAO partidoDAO = new TorneoPartidoDAOMySQL();

    public void validar(long partidoId) {
        TorneoPartido partido = partidoDAO.buscar(partidoId);
        if (partido == null) {
            throw new IllegalArgumentException("El partido no existe.");
        }
        if (partido.getFase() != FaseTorneo.GRUPOS) return;
        if (existeCuadroEliminatorio(partido.getTorneoCategoriaId())) {
            throw new IllegalArgumentException(
                    "No puede corregirse un resultado grupal porque el cuadro "
                            + "eliminatorio ya fue generado. Primero debe "
                            + "invalidarse el cuadro sin partidos disputados.");
        }
    }

    private boolean existeCuadroEliminatorio(long categoriaId) {
        String sql = "SELECT COUNT(*) FROM torneo_partidos "
                + "WHERE torneo_categoria_id = ? AND fase <> 'GRUPOS'";
        try (Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo verificar el cuadro eliminatorio.", exception);
        }
    }
}
