package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.BloqueoCancha;

public class BloqueoCanchaDAOMySQL implements BloqueoCanchaDAO {

    @Override
    public void guardar(BloqueoCancha bloqueo) {
        if (bloqueo == null) {
            throw new IllegalArgumentException(
                    "El bloqueo no puede ser nulo."
            );
        }

        if (bloqueo.getId() <= 0) {
            insertar(bloqueo);
        } else {
            actualizar(bloqueo);
        }
    }

    private void insertar(BloqueoCancha bloqueo) {
        String sql =
                "INSERT INTO bloqueos_cancha "
                + "(cancha_id, fecha, hora_inicio, hora_fin, motivo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            cargarParametros(sentencia, bloqueo);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    bloqueo.setId(claves.getLong(1));
                }
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar el bloqueo de cancha.",
                    exception
            );
        }
    }

    private void actualizar(BloqueoCancha bloqueo) {
        String sql =
                "UPDATE bloqueos_cancha SET cancha_id = ?, "
                + "fecha = ?, hora_inicio = ?, hora_fin = ?, "
                + "motivo = ? WHERE id = ?";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            cargarParametros(sentencia, bloqueo);
            sentencia.setLong(6, bloqueo.getId());

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El bloqueo no existe."
                );
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo actualizar el bloqueo.",
                    exception
            );
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            BloqueoCancha bloqueo)
            throws SQLException {

        sentencia.setLong(1, bloqueo.getCanchaId());
        sentencia.setDate(2, Date.valueOf(bloqueo.getFecha()));
        sentencia.setTime(3, Time.valueOf(bloqueo.getHoraInicio()));
        sentencia.setTime(4, Time.valueOf(bloqueo.getHoraFin()));
        sentencia.setString(5, bloqueo.getMotivo());
    }

    @Override
    public void eliminar(long id) {
        String sql = "DELETE FROM bloqueos_cancha WHERE id = ?";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, id);

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El bloqueo no existe."
                );
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo eliminar el bloqueo.",
                    exception
            );
        }
    }

    @Override
    public BloqueoCancha buscar(long id) {
        String sql = consultaBase() + " WHERE b.id = ?";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                        ? convertirResultado(resultado)
                        : null;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el bloqueo.",
                    exception
            );
        }
    }

    @Override
    public List<BloqueoCancha> listar() {
        String sql = consultaBase()
                + " ORDER BY b.fecha DESC, b.hora_inicio";
        return listarConConsulta(sql, null);
    }

    @Override
    public List<BloqueoCancha> listarPorCancha(long canchaId) {
        String sql = consultaBase()
                + " WHERE b.cancha_id = ? "
                + "ORDER BY b.fecha DESC, b.hora_inicio";
        return listarConConsulta(sql, canchaId);
    }

    private List<BloqueoCancha> listarConConsulta(
            String sql,
            Long canchaId) {

        List<BloqueoCancha> bloqueos = new ArrayList<>();

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            if (canchaId != null) {
                sentencia.setLong(1, canchaId);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    bloqueos.add(convertirResultado(resultado));
                }
            }

            return bloqueos;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar los bloqueos.",
                    exception
            );
        }
    }

    @Override
    public boolean horarioBloqueado(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long bloqueoExcluidoId) {

        String sql =
                "SELECT COUNT(*) FROM bloqueos_cancha "
                + "WHERE cancha_id = ? AND fecha = ? AND id <> ? "
                + "AND hora_inicio < ? AND hora_fin > ?";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, canchaId);
            sentencia.setDate(2, Date.valueOf(fecha));
            sentencia.setLong(3, bloqueoExcluidoId);
            sentencia.setTime(4, Time.valueOf(horaFin));
            sentencia.setTime(5, Time.valueOf(horaInicio));

            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar el bloqueo de cancha.",
                    exception
            );
        }
    }

    private String consultaBase() {
        return "SELECT b.id, b.cancha_id, b.fecha, b.hora_inicio, "
                + "b.hora_fin, b.motivo, b.fecha_creacion, "
                + "c.nombre AS nombre_cancha "
                + "FROM bloqueos_cancha b "
                + "INNER JOIN canchas c ON c.id = b.cancha_id";
    }

    private BloqueoCancha convertirResultado(ResultSet resultado)
            throws SQLException {

        BloqueoCancha bloqueo = new BloqueoCancha();
        bloqueo.setId(resultado.getLong("id"));
        bloqueo.setCanchaId(resultado.getLong("cancha_id"));
        bloqueo.setFecha(resultado.getDate("fecha").toLocalDate());
        bloqueo.setHoraInicio(
                resultado.getTime("hora_inicio").toLocalTime()
        );
        bloqueo.setHoraFin(
                resultado.getTime("hora_fin").toLocalTime()
        );
        bloqueo.setMotivo(resultado.getString("motivo"));
        bloqueo.setNombreCancha(
                resultado.getString("nombre_cancha")
        );

        Timestamp fechaCreacion =
                resultado.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            bloqueo.setFechaCreacion(
                    fechaCreacion.toLocalDateTime()
            );
        }

        return bloqueo;
    }
}
