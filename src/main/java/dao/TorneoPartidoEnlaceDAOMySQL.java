package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import negocio.PosicionPartidoSiguiente;
import negocio.ResultadoOrigenPartido;
import negocio.TorneoPartidoEnlace;

public class TorneoPartidoEnlaceDAOMySQL implements TorneoPartidoEnlaceDAO {
    @Override
    public void guardar(Connection conexion, TorneoPartidoEnlace enlace) {
        String sql = "INSERT INTO torneo_partido_enlaces "
                + "(partido_origen_id, resultado_origen, "
                + "partido_destino_id, posicion_destino) VALUES (?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, enlace.partidoOrigenId());
            sentencia.setString(2, enlace.resultadoOrigen().name());
            sentencia.setLong(3, enlace.partidoDestinoId());
            sentencia.setString(4, enlace.posicionDestino().name());
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo guardar el enlace del partido.", exception);
        }
    }

    @Override
    public List<TorneoPartidoEnlace> listarPorOrigen(
            Connection conexion, long partidoOrigenId) {
        String sql = "SELECT partido_origen_id, resultado_origen, "
                + "partido_destino_id, posicion_destino "
                + "FROM torneo_partido_enlaces WHERE partido_origen_id = ?";
        List<TorneoPartidoEnlace> enlaces = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, partidoOrigenId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    enlaces.add(new TorneoPartidoEnlace(
                            resultado.getLong("partido_origen_id"),
                            ResultadoOrigenPartido.valueOf(
                                    resultado.getString("resultado_origen")),
                            resultado.getLong("partido_destino_id"),
                            PosicionPartidoSiguiente.valueOf(
                                    resultado.getString("posicion_destino"))));
                }
            }
            return enlaces;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron consultar los enlaces.", exception);
        }
    }
}
