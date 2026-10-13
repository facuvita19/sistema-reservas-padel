package servicio;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.EstadoPerfilJugador;
import negocio.PosicionJugador;

public class ConsultaJugadoresService {

    public List<JugadorResumen> listar() {
        String sql = "SELECT pj.id, pj.nombre_publico, pj.estado, pj.activo, pj.vinculacion_revisada, "
                + "pj.cliente_id, pj.posicion_preferida, pj.disponible_para_jugar, "
                + "pj.acepta_invitaciones, c.documento, c.telefono, c.email, "
                + "cd.id categoria_id, cd.nombre categoria_nombre, cd.color_hex, "
                + "jc.fecha_desde categoria_desde, "
                + "(SELECT COUNT(*) FROM torneo_inscripcion_jugadores tij "
                + "WHERE tij.perfil_jugador_id=pj.id) participaciones, "
                + "(SELECT COUNT(*) FROM torneo_inscripcion_jugadores tij "
                + "WHERE tij.perfil_jugador_id=pj.id AND tij.requiere_revision=TRUE) revisiones_pendientes "
                + "FROM perfiles_jugador pj "
                + "LEFT JOIN clientes c ON c.id=pj.cliente_id "
                + "LEFT JOIN jugador_categorias jc ON jc.perfil_jugador_id=pj.id "
                + "AND jc.estado='VIGENTE' "
                + "LEFT JOIN categorias_deportivas cd ON cd.id=jc.categoria_deportiva_id "
                + "ORDER BY pj.activo DESC, pj.nombre_publico, pj.id";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            List<JugadorResumen> jugadores = new ArrayList<>();
            while (resultado.next()) jugadores.add(convertir(resultado));
            return jugadores;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron cargar los jugadores.", exception);
        }
    }

    public JugadorResumen buscarPorCliente(long clienteId) {
        if (clienteId <= 0) return null;
        return listar().stream()
                .filter(jugador -> jugador.clienteId() != null
                        && jugador.clienteId() == clienteId)
                .findFirst().orElse(null);
    }

    public List<CategoriaResumen> listarCategorias() {
        String sql = "SELECT id,nombre,color_hex FROM categorias_deportivas "
                + "WHERE activo=TRUE ORDER BY nivel_orden";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            List<CategoriaResumen> categorias = new ArrayList<>();
            while (resultado.next()) categorias.add(new CategoriaResumen(
                    resultado.getLong(1), resultado.getString(2), resultado.getString(3)));
            return categorias;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron cargar las categorías.", exception);
        }
    }

    public List<CategoriaHistorial> historial(long perfilId) {
        if (perfilId <= 0) throw new IllegalArgumentException("El perfil no es válido.");
        String sql = "SELECT cd.nombre,cd.color_hex,jc.estado,jc.origen,jc.fecha_desde,"
                + "jc.fecha_hasta,jc.motivo,jc.observaciones,u.nombre_usuario "
                + "FROM jugador_categorias jc "
                + "JOIN categorias_deportivas cd ON cd.id=jc.categoria_deportiva_id "
                + "LEFT JOIN usuarios u ON u.id=jc.usuario_responsable_id "
                + "WHERE jc.perfil_jugador_id=? ORDER BY jc.fecha_desde DESC,jc.id DESC";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, perfilId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                List<CategoriaHistorial> historial = new ArrayList<>();
                while (resultado.next()) {
                    Date hasta = resultado.getDate("fecha_hasta");
                    historial.add(new CategoriaHistorial(
                            resultado.getString("nombre"), resultado.getString("color_hex"),
                            resultado.getString("estado"), resultado.getString("origen"),
                            resultado.getDate("fecha_desde").toLocalDate(),
                            hasta == null ? null : hasta.toLocalDate(),
                            resultado.getString("motivo"), resultado.getString("observaciones"),
                            resultado.getString("nombre_usuario")));
                }
                return historial;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo cargar el historial deportivo.", exception);
        }
    }

    private JugadorResumen convertir(ResultSet r) throws SQLException {
        long cliente = r.getLong("cliente_id");
        Long clienteId = r.wasNull() ? null : cliente;
        long categoria = r.getLong("categoria_id");
        Long categoriaId = r.wasNull() ? null : categoria;
        String posicion = r.getString("posicion_preferida");
        Date desde = r.getDate("categoria_desde");
        return new JugadorResumen(r.getLong("id"), r.getString("nombre_publico"),
                EstadoPerfilJugador.valueOf(r.getString("estado")), r.getBoolean("activo"),
                r.getBoolean("vinculacion_revisada"),
                r.getInt("participaciones"), r.getInt("revisiones_pendientes"),
                clienteId, r.getString("documento"), r.getString("telefono"),
                r.getString("email"), posicion == null ? null : PosicionJugador.valueOf(posicion),
                r.getBoolean("disponible_para_jugar"), r.getBoolean("acepta_invitaciones"),
                categoriaId, r.getString("categoria_nombre"), r.getString("color_hex"),
                desde == null ? null : desde.toLocalDate());
    }

    public record JugadorResumen(long id, String nombre, EstadoPerfilJugador identidad,
            boolean activo, boolean vinculacionRevisada, int participaciones,
            int revisionesPendientes, Long clienteId, String documento, String telefono, String email,
            PosicionJugador posicion, boolean disponible, boolean aceptaInvitaciones,
            Long categoriaId, String categoria, String categoriaColor,
            java.time.LocalDate categoriaDesde) {
        public boolean requiereRevision() {
            return identidad == EstadoPerfilJugador.PROVISIONAL
                    || !vinculacionRevisada || revisionesPendientes > 0;
        }
        public String estadoRevision() {
            if (revisionesPendientes > 0) return "REVISAR INSCRIPCIÓN";
            if (identidad == EstadoPerfilJugador.PROVISIONAL) return "ASOCIAR FICHA";
            return vinculacionRevisada ? "REVISADO" : "REVISIÓN PENDIENTE";
        }
    }
    public record CategoriaResumen(long id, String nombre, String color) {
        @Override public String toString() { return nombre; }
    }
    public record CategoriaHistorial(String categoria, String color, String estado,
            String origen, java.time.LocalDate desde, java.time.LocalDate hasta,
            String motivo, String observaciones, String responsable) { }
}
