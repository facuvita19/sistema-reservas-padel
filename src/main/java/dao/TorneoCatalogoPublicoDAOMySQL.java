package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import config.ConexionBD;

public class TorneoCatalogoPublicoDAOMySQL implements TorneoCatalogoPublicoDAO {
    private static final String SQL = """
        SELECT t.id, t.nombre, t.descripcion, t.fecha_inicio, t.fecha_fin,
               t.inscripcion_desde, t.inscripcion_hasta, t.estado,
               c.cantidad_categorias, c.categorias_disponibles,
               c.cupo_total, c.parejas_confirmadas,
               GREATEST(c.cupo_total-c.parejas_confirmadas,0) cupos_disponibles,
               c.precio_minimo, c.precio_maximo, c.categorias, c.ramas, c.formatos,
               COALESCE(p.partidos_totales,0) partidos_totales,
               COALESCE(p.partidos_finalizados,0) partidos_finalizados,
               COALESCE(p.partidos_cancelados,0) partidos_cancelados,
               COALESCE(p.tiene_grupos,0) tiene_grupos,
               COALESCE(p.tiene_eliminatorias,0) tiene_eliminatorias,
               COALESCE(p.final_disputada,0) final_disputada
          FROM torneos t
          JOIN (
                SELECT tc.torneo_id,
                       COUNT(*) cantidad_categorias,
                       SUM(CASE WHEN tc.cupo_parejas > COALESCE(i.confirmadas,0) THEN 1 ELSE 0 END) categorias_disponibles,
                       SUM(tc.cupo_parejas) cupo_total,
                       SUM(COALESCE(i.confirmadas,0)) parejas_confirmadas,
                       MIN(tc.precio_inscripcion) precio_minimo,
                       MAX(tc.precio_inscripcion) precio_maximo,
                       GROUP_CONCAT(DISTINCT tc.nombre ORDER BY tc.nombre SEPARATOR '|') categorias,
                       GROUP_CONCAT(DISTINCT tc.rama ORDER BY tc.rama SEPARATOR '|') ramas,
                       GROUP_CONCAT(DISTINCT tc.formato_competencia ORDER BY tc.formato_competencia SEPARATOR '|') formatos
                  FROM torneo_categorias tc
                  LEFT JOIN (
                       SELECT torneo_categoria_id, COUNT(*) confirmadas
                         FROM torneo_inscripciones
                        WHERE estado='CONFIRMADA'
                        GROUP BY torneo_categoria_id
                  ) i ON i.torneo_categoria_id=tc.id
                 WHERE tc.activo=TRUE
                 GROUP BY tc.torneo_id
          ) c ON c.torneo_id=t.id
          LEFT JOIN (
                SELECT tc.torneo_id,
                       COUNT(tp.id) partidos_totales,
                       SUM(tp.estado='FINALIZADO') partidos_finalizados,
                       SUM(tp.estado='CANCELADO') partidos_cancelados,
                       MAX(tp.fase='GRUPOS') tiene_grupos,
                       MAX(tp.fase<>'GRUPOS') tiene_eliminatorias,
                       MAX(tp.fase='FINAL' AND tp.estado='FINALIZADO') final_disputada
                  FROM torneo_categorias tc
                  LEFT JOIN torneo_partidos tp ON tp.torneo_categoria_id=tc.id
                 WHERE tc.activo=TRUE
                 GROUP BY tc.torneo_id
          ) p ON p.torneo_id=t.id
         WHERE t.activo=TRUE AND t.estado NOT IN ('BORRADOR','CANCELADO')
         ORDER BY CASE t.estado WHEN 'EN_CURSO' THEN 0 WHEN 'INSCRIPCION_ABIERTA' THEN 1
                  WHEN 'PUBLICADO' THEN 2 WHEN 'INSCRIPCION_CERRADA' THEN 3 ELSE 4 END,
                  t.fecha_inicio ASC, t.id ASC
        """;

    @Override
    public List<Resumen> listar() {
        try (Connection conexion=ConexionBD.obtenerConexion();
             PreparedStatement sentencia=conexion.prepareStatement(SQL);
             ResultSet r=sentencia.executeQuery()) {
            var salida=new java.util.ArrayList<Resumen>();
            while(r.next()) salida.add(new Resumen(r.getLong("id"),r.getString("nombre"),
                    r.getString("descripcion"),r.getDate("fecha_inicio").toLocalDate(),
                    r.getDate("fecha_fin").toLocalDate(),r.getTimestamp("inscripcion_desde").toLocalDateTime(),
                    r.getTimestamp("inscripcion_hasta").toLocalDateTime(),r.getString("estado"),
                    r.getInt("cantidad_categorias"),r.getInt("categorias_disponibles"),
                    r.getInt("cupo_total"),r.getInt("parejas_confirmadas"),r.getInt("cupos_disponibles"),
                    nz(r.getBigDecimal("precio_minimo")),nz(r.getBigDecimal("precio_maximo")),
                    lista(r.getString("categorias")),lista(r.getString("ramas")),lista(r.getString("formatos")),
                    r.getInt("partidos_totales"),r.getInt("partidos_finalizados"),r.getInt("partidos_cancelados"),
                    r.getBoolean("tiene_grupos"),r.getBoolean("tiene_eliminatorias"),r.getBoolean("final_disputada")));
            return List.copyOf(salida);
        } catch(SQLException e){throw new RuntimeException("No se pudo consultar el catalogo publico de torneos.",e);}
    }
    private static BigDecimal nz(BigDecimal v){return v==null?BigDecimal.ZERO:v;}
    private static List<String> lista(String v){return v==null||v.isBlank()?List.of():Arrays.stream(v.split("\\|" )).filter(x->!x.isBlank()).toList();}
}
