package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import config.ConexionBD;
import negocio.CorreccionResultadoTorneo;

public class CorreccionResultadoTorneoDAOMySQL implements CorreccionResultadoTorneoDAO {
    @Override
    public void guardar(Connection c, CorreccionResultadoTorneo x) {
        if (c == null || x == null) throw new IllegalArgumentException("La correccion es obligatoria.");
        String sql = "INSERT INTO torneo_resultado_correcciones "
                + "(partido_id, usuario_id, motivo, ganadora_anterior_inscripcion_id, "
                + "ganadora_nueva_inscripcion_id, resultado_anterior, resultado_nuevo, "
                + "sets_anteriores_json, sets_nuevos_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setLong(1, x.getPartidoId());
            s.setLong(2, x.getUsuarioId());
            s.setString(3, x.getMotivo());
            setLong(s, 4, x.getGanadoraAnteriorInscripcionId());
            setLong(s, 5, x.getGanadoraNuevaInscripcionId());
            s.setString(6, x.getResultadoAnterior());
            s.setString(7, x.getResultadoNuevo());
            s.setString(8, x.getSetsAnterioresJson());
            s.setString(9, x.getSetsNuevosJson());
            s.executeUpdate();
            try (ResultSet k = s.getGeneratedKeys()) { if (k.next()) x.setId(k.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException("No se pudo auditar la correccion.", e); }
    }

    @Override
    public List<CorreccionResultadoTorneo> listarPorPartido(long partidoId) {
        if (partidoId <= 0) throw new IllegalArgumentException("El partido debe ser positivo.");
        String sql = "SELECT c.*, u.nombre_usuario FROM torneo_resultado_correcciones c "
                + "INNER JOIN usuarios u ON u.id = c.usuario_id WHERE c.partido_id = ? "
                + "ORDER BY c.fecha_correccion DESC, c.id DESC";
        List<CorreccionResultadoTorneo> lista = new ArrayList<>();
        try (Connection c = ConexionBD.obtenerConexion(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, partidoId);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    CorreccionResultadoTorneo x = new CorreccionResultadoTorneo();
                    x.setId(r.getLong("id")); x.setPartidoId(r.getLong("partido_id"));
                    x.setUsuarioId(r.getLong("usuario_id")); x.setUsuario(r.getString("nombre_usuario"));
                    x.setMotivo(r.getString("motivo"));
                    x.setGanadoraAnteriorInscripcionId(nullable(r, "ganadora_anterior_inscripcion_id"));
                    x.setGanadoraNuevaInscripcionId(nullable(r, "ganadora_nueva_inscripcion_id"));
                    x.setResultadoAnterior(r.getString("resultado_anterior"));
                    x.setResultadoNuevo(r.getString("resultado_nuevo"));
                    x.setSetsAnterioresJson(r.getString("sets_anteriores_json"));
                    x.setSetsNuevosJson(r.getString("sets_nuevos_json"));
                    x.setFechaCorreccion(r.getTimestamp("fecha_correccion").toLocalDateTime());
                    lista.add(x);
                }
            }
            return lista;
        } catch (SQLException e) { throw new RuntimeException("No se pudo consultar el historial.", e); }
    }

    private void setLong(PreparedStatement s, int i, Long v) throws SQLException {
        if (v == null) s.setNull(i, java.sql.Types.BIGINT); else s.setLong(i, v);
    }
    private Long nullable(ResultSet r, String c) throws SQLException {
        long v = r.getLong(c); return r.wasNull() ? null : v;
    }
}
