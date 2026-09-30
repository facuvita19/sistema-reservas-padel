package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.EstadoInscripcionTorneo;

public class ConsultaInscripcionTorneoService {

    public List<InscripcionResumen> listar() {
        String sql = "SELECT i.id, i.estado, i.origen, i.precio_inscripcion, "
                + "i.comentarios, i.observaciones_administrativas, "
                + "i.fecha_solicitud, t.nombre AS torneo, "
                + "c.nombre AS categoria, c.rama, "
                + "j1.id AS responsable_jugador_id, j1.nombre AS responsable_nombre, j1.apellido AS responsable_apellido, "
                + "j1.telefono AS responsable_telefono, j1.cliente_id AS responsable_cliente_id, "
                + "j1.tipo_vinculacion AS responsable_vinculacion, "
                + "j1.requiere_revision AS responsable_revision, "
                + "j2.id AS pareja_jugador_id, j2.nombre AS pareja_nombre, j2.apellido AS pareja_apellido, "
                + "j2.telefono AS pareja_telefono, j2.cliente_id AS pareja_cliente_id, "
                + "j2.tipo_vinculacion AS pareja_vinculacion, "
                + "j2.requiere_revision AS pareja_revision "
                + "FROM torneo_inscripciones i "
                + "INNER JOIN torneo_categorias c ON c.id = i.torneo_categoria_id "
                + "INNER JOIN torneos t ON t.id = c.torneo_id "
                + "INNER JOIN torneo_inscripcion_jugadores j1 "
                + "ON j1.inscripcion_id = i.id AND j1.orden_integrante = 1 "
                + "INNER JOIN torneo_inscripcion_jugadores j2 "
                + "ON j2.inscripcion_id = i.id AND j2.orden_integrante = 2 "
                + "ORDER BY CASE i.estado WHEN 'PENDIENTE' THEN 0 "
                + "WHEN 'LISTA_ESPERA' THEN 1 WHEN 'CONFIRMADA' THEN 2 ELSE 3 END, "
                + "i.fecha_solicitud DESC, i.id DESC";

        List<InscripcionResumen> resultado = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet rs = sentencia.executeQuery()) {
            while (rs.next()) resultado.add(convertir(rs));
            return resultado;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron recuperar las inscripciones de torneos.", exception);
        }
    }

    private InscripcionResumen convertir(ResultSet rs) throws SQLException {
        return new InscripcionResumen(
                rs.getLong("id"),
                rs.getString("torneo"),
                rs.getString("categoria"),
                rs.getString("rama"),
                EstadoInscripcionTorneo.valueOf(rs.getString("estado")),
                rs.getString("origen"),
                rs.getBigDecimal("precio_inscripcion"),
                rs.getString("comentarios"),
                rs.getString("observaciones_administrativas"),
                fecha(rs, "fecha_solicitud"),
                jugador(rs, "responsable"),
                jugador(rs, "pareja"));
    }

    private JugadorResumen jugador(ResultSet rs, String prefijo) throws SQLException {
        long clienteId = rs.getLong(prefijo + "_cliente_id");
        Long valorClienteId = rs.wasNull() ? null : clienteId;
        return new JugadorResumen(
                rs.getLong(prefijo + "_jugador_id"),
                rs.getString(prefijo + "_nombre"),
                rs.getString(prefijo + "_apellido"),
                rs.getString(prefijo + "_telefono"),
                valorClienteId,
                rs.getString(prefijo + "_vinculacion"),
                rs.getBoolean(prefijo + "_revision"));
    }

    private LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        Timestamp valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    public record JugadorResumen(long jugadorId, String nombre, String apellido, String telefono,
            Long clienteId, String tipoVinculacion, boolean requiereRevision) {
        public String nombreCompleto() {
            return ((nombre == null ? "" : nombre.trim()) + " "
                    + (apellido == null ? "" : apellido.trim())).trim();
        }
        public boolean vinculado() { return clienteId != null && clienteId > 0; }
        public String estadoVinculacion() {
            if (vinculado()) return "Cliente #" + clienteId + " - " + tipoVinculacion;
            return requiereRevision ? "Sin vincular - requiere revision" : "Sin vincular";
        }
    }

    public record InscripcionResumen(long id, String torneo, String categoria,
            String rama, EstadoInscripcionTorneo estado, String origen,
            java.math.BigDecimal precioInscripcion, String comentarios,
            String observacionesAdministrativas, LocalDateTime fechaSolicitud,
            JugadorResumen responsable, JugadorResumen pareja) {
        public String categoriaCompleta() { return categoria + " - " + rama; }
        public String responsableNombre() { return responsable.nombreCompleto(); }
        public String parejaNombre() { return pareja.nombreCompleto(); }
    }
}
