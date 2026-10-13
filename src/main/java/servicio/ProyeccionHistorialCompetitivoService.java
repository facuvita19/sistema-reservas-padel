package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import negocio.TorneoPartido;

/** Proyecta un resultado finalizado hacia el historial inmutable por perfil. */
public class ProyeccionHistorialCompetitivoService {
    public void reconstruir(Connection conexion, TorneoPartido partido) {
        if (conexion == null || partido == null || partido.getId() <= 0
                || partido.getGanadoraInscripcionId() == null
                || partido.getUsuarioResultadoId() == null
                || partido.getFechaFinalizacion() == null) {
            throw new IllegalArgumentException("El resultado no esta completo para proyectar su historial competitivo.");
        }
        eliminar(conexion, partido.getId());
        insertarLado(conexion, partido, 1, partido.getPareja1InscripcionId());
        insertarLado(conexion, partido, 2, partido.getPareja2InscripcionId());
    }

    private void eliminar(Connection c, long partidoId) {
        try (PreparedStatement s=c.prepareStatement("DELETE FROM historial_competitivo_perfiles WHERE partido_id=?")) {
            s.setLong(1,partidoId); s.executeUpdate();
        } catch(SQLException e) { throw new RuntimeException("No se pudo invalidar la proyeccion competitiva anterior.",e); }
    }

    private void insertarLado(Connection c,TorneoPartido p,int lado,Long inscripcionId) {
        if(inscripcionId==null) throw new IllegalArgumentException("El partido finalizado debe conservar ambas inscripciones.");
        String q="INSERT INTO historial_competitivo_perfiles (partido_id,perfil_jugador_id,torneo_categoria_id,categoria_deportiva_id,inscripcion_id,lado,orden_integrante,resultado,nombre_perfil_snapshot,categoria_snapshot,fecha_partido,fecha_resultado,usuario_resultado_id) "
            +"SELECT ?,pj.id,?,jc.categoria_deportiva_id,tij.inscripcion_id,?,tij.orden_integrante,?,pj.nombre_publico,cd.nombre,?,?,? "
            +"FROM torneo_inscripcion_jugadores tij JOIN perfiles_jugador pj ON pj.id=tij.perfil_jugador_id "
            +"LEFT JOIN jugador_categorias jc ON jc.perfil_jugador_id=pj.id AND jc.estado='VIGENTE' "
            +"LEFT JOIN categorias_deportivas cd ON cd.id=jc.categoria_deportiva_id "
            +"WHERE tij.inscripcion_id=? ORDER BY tij.orden_integrante";
        try(PreparedStatement s=c.prepareStatement(q)){
            s.setLong(1,p.getId());s.setLong(2,p.getTorneoCategoriaId());s.setInt(3,lado);
            s.setString(4,inscripcionId.equals(p.getGanadoraInscripcionId())?"GANADOR":"PERDEDOR");
            if(p.getFecha()==null)s.setNull(5,java.sql.Types.DATE);else s.setDate(5,java.sql.Date.valueOf(p.getFecha()));
            s.setTimestamp(6,java.sql.Timestamp.valueOf(p.getFechaFinalizacion()));s.setLong(7,p.getUsuarioResultadoId());s.setLong(8,inscripcionId);
            int insertados=s.executeUpdate();if(insertados!=2)throw new IllegalArgumentException("Cada pareja debe aportar exactamente dos perfiles deportivos al historial.");
        }catch(SQLException e){throw new RuntimeException("No se pudo proyectar el resultado por perfil deportivo.",e);}
    }
}
