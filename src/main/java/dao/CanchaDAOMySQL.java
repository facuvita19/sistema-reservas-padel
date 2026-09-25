package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import config.ConexionBD;
import negocio.Cancha;
import negocio.TipoCancha;

public class CanchaDAOMySQL implements CanchaDAO {

    @Override
    public void guardar(Cancha cancha) {
        if (cancha == null) {
            throw new IllegalArgumentException(
                    "La cancha no puede ser nula."
            );
        }

        try (Connection conexion =
                ConexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {
                if (cancha.getId() <= 0) {
                    insertarCancha(conexion, cancha);
                } else {
                    actualizarCancha(conexion, cancha);
                }

                reemplazarDias(conexion, cancha);
                conexion.commit();

            } catch (SQLException | RuntimeException exception) {
                conexion.rollback();
                throw exception;

            } finally {
                conexion.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo guardar la cancha en MySQL.",
                    exception
            );
        }
    }

    private void insertarCancha(
            Connection conexion,
            Cancha cancha)
            throws SQLException {

        String sql =
                "INSERT INTO canchas "
                + "(nombre, descripcion, tipo, superficie, "
                + "tiene_iluminacion, hora_apertura, hora_cierre, "
                + "duracion_reserva, precio, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)";

        try (PreparedStatement sentencia =
                conexion.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )) {

            cargarParametros(sentencia, cancha);

            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException(
                        "No se pudo crear la cancha."
                );
            }

            try (ResultSet claves =
                    sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    cancha.setId(claves.getLong(1));
                }
            }
        }
    }

    private void actualizarCancha(
            Connection conexion,
            Cancha cancha)
            throws SQLException {

        String sql =
                "UPDATE canchas SET "
                + "nombre = ?, descripcion = ?, tipo = ?, "
                + "superficie = ?, tiene_iluminacion = ?, "
                + "hora_apertura = ?, hora_cierre = ?, "
                + "duracion_reserva = ?, precio = ? "
                + "WHERE id = ? AND activo = TRUE";

        try (PreparedStatement sentencia =
                conexion.prepareStatement(sql)) {

            cargarParametros(sentencia, cancha);
            sentencia.setLong(10, cancha.getId());

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "La cancha no existe o esta inactiva."
                );
            }
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            Cancha cancha)
            throws SQLException {

        sentencia.setString(1, cancha.getNombre());
        sentencia.setString(2, cancha.getDescripcion());
        sentencia.setString(3, cancha.getTipo().name());
        sentencia.setString(4, cancha.getSuperficie());
        sentencia.setBoolean(5, cancha.isTieneIluminacion());
        sentencia.setTime(
                6,
                java.sql.Time.valueOf(cancha.getHoraApertura())
        );
        sentencia.setTime(
                7,
                java.sql.Time.valueOf(cancha.getHoraCierre())
        );
        sentencia.setInt(8, cancha.getDuracionReserva());
        sentencia.setBigDecimal(9, cancha.getPrecio());
    }

    private void reemplazarDias(
            Connection conexion,
            Cancha cancha)
            throws SQLException {

        try (PreparedStatement eliminar =
                conexion.prepareStatement(
                        "DELETE FROM cancha_dias_disponibles "
                                + "WHERE cancha_id = ?"
                )) {
            eliminar.setLong(1, cancha.getId());
            eliminar.executeUpdate();
        }

        String sql =
                "INSERT INTO cancha_dias_disponibles "
                + "(cancha_id, dia_semana) VALUES (?, ?)";

        try (PreparedStatement insertar =
                conexion.prepareStatement(sql)) {

            for (DayOfWeek dia : cancha.getDiasDisponibles()) {
                insertar.setLong(1, cancha.getId());
                insertar.setString(2, dia.name());
                insertar.addBatch();
            }

            insertar.executeBatch();
        }
    }

    @Override
    public void eliminar(long id) {
        String sql =
                "UPDATE canchas SET activo = FALSE "
                + "WHERE id = ? AND activo = TRUE";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, id);

            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "La cancha no existe o ya esta inactiva."
                );
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo desactivar la cancha.",
                    exception
            );
        }
    }

    @Override
    public Cancha buscar(long id) {
        String sql = consultaBase()
                + " WHERE c.id = ? GROUP BY c.id";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, id);

            try (ResultSet resultado =
                    sentencia.executeQuery()) {
                return resultado.next()
                        ? convertirResultado(resultado)
                        : null;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar la cancha.",
                    exception
            );
        }
    }

    @Override
    public List<Cancha> listar() {
        String sql = consultaBase()
                + " WHERE c.activo = TRUE "
                + "GROUP BY c.id ORDER BY c.nombre";

        List<Cancha> canchas = new ArrayList<>();

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()
        ) {
            while (resultado.next()) {
                canchas.add(convertirResultado(resultado));
            }

            return canchas;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron recuperar las canchas.",
                    exception
            );
        }
    }

    @Override
    public boolean existeNombre(
            String nombre,
            long canchaExcluidaId) {

        String sql =
                "SELECT COUNT(*) FROM canchas "
                + "WHERE LOWER(nombre) = LOWER(?) AND id <> ?";

        try (
                Connection conexion =
                        ConexionBD.obtenerConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, nombre);
            sentencia.setLong(2, canchaExcluidaId);

            try (ResultSet resultado =
                    sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar el nombre de la cancha.",
                    exception
            );
        }
    }

    private String consultaBase() {
        return "SELECT c.id, c.nombre, c.descripcion, c.tipo, "
                + "c.superficie, c.tiene_iluminacion, "
                + "c.hora_apertura, c.hora_cierre, "
                + "c.duracion_reserva, c.precio, c.activo, "
                + "c.fecha_creacion, "
                + "GROUP_CONCAT(d.dia_semana "
                + "ORDER BY FIELD(d.dia_semana, "
                + "'MONDAY', 'TUESDAY', 'WEDNESDAY', "
                + "'THURSDAY', 'FRIDAY', 'SATURDAY', "
                + "'SUNDAY') SEPARATOR ',') AS dias "
                + "FROM canchas c "
                + "LEFT JOIN cancha_dias_disponibles d "
                + "ON d.cancha_id = c.id";
    }

    private Cancha convertirResultado(ResultSet resultado)
            throws SQLException {

        Cancha cancha = new Cancha();
        cancha.setId(resultado.getLong("id"));
        cancha.setNombre(resultado.getString("nombre"));
        cancha.setDescripcion(resultado.getString("descripcion"));
        cancha.setTipo(TipoCancha.valueOf(
                resultado.getString("tipo")
        ));
        cancha.setSuperficie(resultado.getString("superficie"));
        cancha.setTieneIluminacion(
                resultado.getBoolean("tiene_iluminacion")
        );
        cancha.setHoraApertura(
                resultado.getTime("hora_apertura").toLocalTime()
        );
        cancha.setHoraCierre(
                resultado.getTime("hora_cierre").toLocalTime()
        );
        cancha.setDuracionReserva(
                resultado.getInt("duracion_reserva")
        );
        cancha.setPrecio(resultado.getBigDecimal("precio"));
        cancha.setActivo(resultado.getBoolean("activo"));

        Timestamp fecha = resultado.getTimestamp("fecha_creacion");
        if (fecha != null) {
            cancha.setFechaCreacion(fecha.toLocalDateTime());
        }

        Set<DayOfWeek> dias =
                EnumSet.noneOf(DayOfWeek.class);
        String diasTexto = resultado.getString("dias");

        if (diasTexto != null && !diasTexto.isBlank()) {
            for (String dia : diasTexto.split(",")) {
                dias.add(DayOfWeek.valueOf(dia));
            }
        }

        cancha.setDiasDisponibles(dias);
        return cancha;
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {

        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe una cancha con ese nombre.",
                    exception
            );
        }

        return new RuntimeException(mensaje, exception);
    }
}
