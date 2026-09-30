package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.EstadoTorneo;
import negocio.Torneo;

public class TorneoDAOMySQL implements TorneoDAO {

    @Override
    public void guardar(Torneo torneo) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, torneo);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar el torneo.", exception);
        }
    }

    @Override
    public void guardar(Connection conexion, Torneo torneo) {
        validarConexion(conexion);
        validarTorneo(torneo);
        if (torneo.getId() <= 0) {
            insertar(conexion, torneo);
        } else {
            actualizar(conexion, torneo);
        }
    }

    private void insertar(Connection conexion, Torneo torneo) {
        String sql = "INSERT INTO torneos "
                + "(nombre, descripcion, fecha_inicio, fecha_fin, "
                + "inscripcion_desde, inscripcion_hasta, estado, "
                + "reglamento, activo, usuario_creacion_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, torneo);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException("No se pudo crear el torneo.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new RuntimeException(
                            "No se pudo recuperar el ID del torneo.");
                }
                torneo.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw error("No se pudo crear el torneo.", exception);
        }
    }

    private void actualizar(Connection conexion, Torneo torneo) {
        String sql = "UPDATE torneos SET nombre = ?, descripcion = ?, "
                + "fecha_inicio = ?, fecha_fin = ?, inscripcion_desde = ?, "
                + "inscripcion_hasta = ?, estado = ?, reglamento = ?, "
                + "activo = ?, usuario_creacion_id = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, torneo);
            sentencia.setLong(11, torneo.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("El torneo no existe.");
            }
        } catch (SQLException exception) {
            throw error("No se pudo actualizar el torneo.", exception);
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            Torneo torneo) throws SQLException {
        sentencia.setString(1, torneo.getNombre());
        sentencia.setString(2, torneo.getDescripcion());
        sentencia.setDate(3, Date.valueOf(torneo.getFechaInicio()));
        sentencia.setDate(4, Date.valueOf(torneo.getFechaFin()));
        sentencia.setTimestamp(5, Timestamp.valueOf(
                torneo.getInscripcionDesde()));
        sentencia.setTimestamp(6, Timestamp.valueOf(
                torneo.getInscripcionHasta()));
        sentencia.setString(7, torneo.getEstado().name());
        sentencia.setString(8, torneo.getReglamento());
        sentencia.setBoolean(9, torneo.isActivo());
        sentencia.setLong(10, torneo.getUsuarioCreacionId());
    }

    @Override
    public Torneo buscar(long id) {
        validarId(id);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscar(conexion, id);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el torneo.", exception);
        }
    }

    @Override
    public Torneo buscar(Connection conexion, long id) {
        validarConexion(conexion);
        validarId(id);
        String sql = consultaBase() + " WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertir(resultado) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el torneo.", exception);
        }
    }

    @Override
    public List<Torneo> listar() {
        return consultar(consultaBase()
                + " ORDER BY fecha_inicio DESC, id DESC");
    }

    @Override
    public List<Torneo> listarActivos() {
        return consultar(consultaBase()
                + " WHERE activo = TRUE "
                + "ORDER BY fecha_inicio ASC, id ASC");
    }

    @Override
    public List<Torneo> listarPorEstado(EstadoTorneo estado) {
        if (estado == null) {
            throw new IllegalArgumentException(
                    "El estado del torneo es obligatorio.");
        }
        String sql = consultaBase()
                + " WHERE estado = ? ORDER BY fecha_inicio ASC, id ASC";
        List<Torneo> torneos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, estado.name());
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    torneos.add(convertir(resultado));
                }
            }
            return torneos;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar los torneos por estado.",
                    exception);
        }
    }

    private List<Torneo> consultar(String sql) {
        List<Torneo> torneos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                torneos.add(convertir(resultado));
            }
            return torneos;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar los torneos.", exception);
        }
    }

    @Override
    public void desactivar(long id) {
        validarId(id);
        String sql = "UPDATE torneos SET activo = FALSE WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("El torneo no existe.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo desactivar el torneo.", exception);
        }
    }

    private String consultaBase() {
        return "SELECT id, nombre, descripcion, fecha_inicio, fecha_fin, "
                + "inscripcion_desde, inscripcion_hasta, estado, "
                + "reglamento, activo, usuario_creacion_id, "
                + "fecha_creacion, fecha_actualizacion FROM torneos";
    }

    private Torneo convertir(ResultSet resultado) throws SQLException {
        Torneo torneo = new Torneo();
        torneo.setId(resultado.getLong("id"));
        torneo.setNombre(resultado.getString("nombre"));
        torneo.setDescripcion(resultado.getString("descripcion"));
        torneo.setFechaInicio(
                resultado.getDate("fecha_inicio").toLocalDate());
        torneo.setFechaFin(
                resultado.getDate("fecha_fin").toLocalDate());
        torneo.setInscripcionDesde(
                resultado.getTimestamp("inscripcion_desde")
                        .toLocalDateTime());
        torneo.setInscripcionHasta(
                resultado.getTimestamp("inscripcion_hasta")
                        .toLocalDateTime());
        torneo.setEstado(EstadoTorneo.valueOf(
                resultado.getString("estado")));
        torneo.setReglamento(resultado.getString("reglamento"));
        torneo.setActivo(resultado.getBoolean("activo"));
        torneo.setUsuarioCreacionId(
                resultado.getLong("usuario_creacion_id"));
        torneo.setFechaCreacion(
                resultado.getTimestamp("fecha_creacion")
                        .toLocalDateTime());
        torneo.setFechaActualizacion(
                resultado.getTimestamp("fecha_actualizacion")
                        .toLocalDateTime());
        return torneo;
    }

    private void validarTorneo(Torneo torneo) {
        if (torneo == null) {
            throw new IllegalArgumentException(
                    "El torneo no puede ser nulo.");
        }
        if (torneo.getNombre() == null
                || torneo.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del torneo es obligatorio.");
        }
        if (torneo.getNombre().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "El nombre del torneo no puede superar 150 caracteres.");
        }
        if (torneo.getFechaInicio() == null
                || torneo.getFechaFin() == null) {
            throw new IllegalArgumentException(
                    "Las fechas del torneo son obligatorias.");
        }
        if (torneo.getFechaFin().isBefore(torneo.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha final no puede ser anterior al inicio.");
        }
        if (torneo.getInscripcionDesde() == null
                || torneo.getInscripcionHasta() == null) {
            throw new IllegalArgumentException(
                    "Las fechas de inscripcion son obligatorias.");
        }
        if (torneo.getInscripcionHasta().isBefore(
                torneo.getInscripcionDesde())) {
            throw new IllegalArgumentException(
                    "El cierre de inscripcion no puede ser anterior a la apertura.");
        }
        if (torneo.getInscripcionHasta().toLocalDate().isAfter(
                torneo.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La inscripcion debe cerrar antes de iniciar el torneo.");
        }
        if (torneo.getEstado() == null) {
            throw new IllegalArgumentException(
                    "El estado del torneo es obligatorio.");
        }
        if (torneo.getUsuarioCreacionId() <= 0) {
            throw new IllegalArgumentException(
                    "El usuario creador del torneo no es valido.");
        }
        torneo.setNombre(torneo.getNombre().trim());
    }

    private void validarConexion(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
    }

    private void validarId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID del torneo debe ser positivo.");
        }
    }

    private RuntimeException error(
            String mensaje,
            SQLException exception) {
        return new RuntimeException(mensaje, exception);
    }
}
