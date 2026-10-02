package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import config.ConexionBD;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;

public class InvalidacionCuadroTorneoService {
    private final TorneoPartidoDAO partidoDAO = new TorneoPartidoDAOMySQL();

    public Resumen analizar(long categoriaId) {
        if (categoriaId <= 0) throw new IllegalArgumentException(
                "La categoria no es valida.");
        String sql = "SELECT COUNT(*) total, "
                + "SUM(estado = 'PROGRAMADO') programados, "
                + "SUM(estado = 'EN_CURSO') en_curso, "
                + "SUM(estado = 'FINALIZADO') finalizados, "
                + "SUM(ganadora_inscripcion_id IS NOT NULL) con_ganadora, "
                + "(SELECT COUNT(*) FROM torneo_partido_sets tps "
                + "INNER JOIN torneo_partidos tp2 ON tp2.id=tps.partido_id "
                + "WHERE tp2.torneo_categoria_id=? AND tp2.fase<>'GRUPOS') sets "
                + "FROM torneo_partidos WHERE torneo_categoria_id=? "
                + "AND fase<>'GRUPOS'";
        try (Connection c=ConexionBD.obtenerConexion();
                PreparedStatement s=c.prepareStatement(sql)) {
            s.setLong(1,categoriaId); s.setLong(2,categoriaId);
            try(ResultSet r=s.executeQuery()) { r.next();
                return new Resumen(r.getInt("total"),r.getInt("programados"),
                        r.getInt("en_curso"),r.getInt("finalizados"),
                        r.getInt("con_ganadora"),r.getInt("sets"));
            }
        } catch(SQLException e) { throw new RuntimeException(
                "No se pudo analizar el cuadro eliminatorio.",e); }
    }

    public void invalidar(long categoriaId) {
        Resumen resumen=analizar(categoriaId);
        if(resumen.total()==0) throw new IllegalArgumentException(
                "La categoria no tiene un cuadro eliminatorio.");
        if(resumen.tieneActividadDeportiva()) throw new IllegalArgumentException(
                "No puede invalidarse el cuadro porque tiene partidos en curso, "
                + "finalizados, ganadores o resultados registrados.");
        try(Connection c=ConexionBD.obtenerConexion()) {
            c.setAutoCommit(false);
            try { partidoDAO.eliminarCuadroPorCategoria(c,categoriaId); c.commit(); }
            catch(RuntimeException|SQLException e) {
                try { c.rollback(); } catch(SQLException rb) { e.addSuppressed(rb); }
                throw e;
            } finally { try { c.setAutoCommit(true); } catch(SQLException ignored) {} }
        } catch(SQLException e) { throw new RuntimeException(
                "No se pudo invalidar el cuadro eliminatorio.",e); }
    }

    public record Resumen(int total,int programados,int enCurso,
            int finalizados,int conGanadora,int sets) {
        public boolean tieneActividadDeportiva() {
            return enCurso>0 || finalizados>0 || conGanadora>0 || sets>0;
        }
        public boolean tieneProgramacion() { return programados>0; }
    }
}
