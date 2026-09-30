package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.TipoVinculacionTorneo;
import negocio.TorneoInscripcionJugador;

public class TorneoInscripcionJugadorDAOMySQL
        implements TorneoInscripcionJugadorDAO {

    @Override
    public void guardar(TorneoInscripcionJugador jugador) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, jugador);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar el integrante del torneo.",
                    exception);
        }
    }

    @Override
    public void guardar(
            Connection conexion,
            TorneoInscripcionJugador jugador) {
        validarConexion(conexion);
        validarJugador(jugador);
        if (jugador.getId() <= 0) {
            insertar(conexion, jugador);
        } else {
            actualizar(conexion, jugador);
        }
    }

    private void insertar(
            Connection conexion,
            TorneoInscripcionJugador jugador) {
        String sql = "INSERT INTO torneo_inscripcion_jugadores "
                + "(inscripcion_id, orden_integrante, cliente_id, "
                + "nombre, apellido, telefono, telefono_normalizado, "
                + "es_responsable, tipo_vinculacion, requiere_revision) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, jugador);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException(
                        "No se pudo crear el integrante del torneo.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new RuntimeException(
                            "No se pudo recuperar el ID del integrante.");
                }
                jugador.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo crear el integrante del torneo.",
                    exception);
        }
    }

    private void actualizar(
            Connection conexion,
            TorneoInscripcionJugador jugador) {
        String sql = "UPDATE torneo_inscripcion_jugadores SET "
                + "inscripcion_id = ?, orden_integrante = ?, "
                + "cliente_id = ?, nombre = ?, apellido = ?, "
                + "telefono = ?, telefono_normalizado = ?, "
                + "es_responsable = ?, tipo_vinculacion = ?, "
                + "requiere_revision = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, jugador);
            sentencia.setLong(11, jugador.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El integrante del torneo no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo actualizar el integrante del torneo.",
                    exception);
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            TorneoInscripcionJugador jugador) throws SQLException {
        sentencia.setLong(1, jugador.getInscripcionId());
        sentencia.setInt(2, jugador.getOrdenIntegrante());
        if (jugador.getClienteId() == null) {
            sentencia.setNull(3, Types.BIGINT);
        } else {
            sentencia.setLong(3, jugador.getClienteId());
        }
        sentencia.setString(4, jugador.getNombre());
        sentencia.setString(5, jugador.getApellido());
        sentencia.setString(6, jugador.getTelefono());
        sentencia.setString(7, jugador.getTelefonoNormalizado());
        sentencia.setBoolean(8, jugador.isResponsable());
        sentencia.setString(9, jugador.getTipoVinculacion().name());
        sentencia.setBoolean(10, jugador.isRequiereRevision());
    }

    @Override
    public TorneoInscripcionJugador buscar(long id) {
        validarId(id);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscar(conexion, id);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el integrante del torneo.",
                    exception);
        }
    }

    @Override
    public TorneoInscripcionJugador buscar(
            Connection conexion,
            long id) {
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
                    "No se pudo buscar el integrante del torneo.",
                    exception);
        }
    }

    @Override
    public List<TorneoInscripcionJugador> listarPorInscripcion(
            long inscripcionId) {
        validarInscripcionId(inscripcionId);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return listarPorInscripcion(conexion, inscripcionId);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar los integrantes del torneo.",
                    exception);
        }
    }

    @Override
    public List<TorneoInscripcionJugador> listarPorInscripcion(
            Connection conexion,
            long inscripcionId) {
        validarConexion(conexion);
        validarInscripcionId(inscripcionId);
        String sql = consultaBase()
                + " WHERE inscripcion_id = ? "
                + "ORDER BY orden_integrante ASC, id ASC";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, inscripcionId);
            return consultar(sentencia);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar los integrantes del torneo.",
                    exception);
        }
    }

    @Override
    public List<TorneoInscripcionJugador> buscarPorTelefonoNormalizado(
            Connection conexion,
            String telefonoNormalizado) {
        validarConexion(conexion);
        validarTelefonoNormalizado(telefonoNormalizado);
        String sql = consultaBase()
                + " WHERE telefono_normalizado = ? "
                + "ORDER BY fecha_creacion DESC, id DESC";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, telefonoNormalizado);
            return consultar(sentencia);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron buscar integrantes por telefono.",
                    exception);
        }
    }

    private List<TorneoInscripcionJugador> consultar(
            PreparedStatement sentencia) throws SQLException {
        List<TorneoInscripcionJugador> jugadores = new ArrayList<>();
        try (ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                jugadores.add(convertir(resultado));
            }
        }
        return jugadores;
    }

    @Override
    public void eliminarPorInscripcion(
            Connection conexion,
            long inscripcionId) {
        validarConexion(conexion);
        validarInscripcionId(inscripcionId);
        String sql = "DELETE FROM torneo_inscripcion_jugadores "
                + "WHERE inscripcion_id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, inscripcionId);
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron eliminar los integrantes del torneo.",
                    exception);
        }
    }

    private String consultaBase() {
        return "SELECT id, inscripcion_id, orden_integrante, cliente_id, "
                + "nombre, apellido, telefono, telefono_normalizado, "
                + "es_responsable, tipo_vinculacion, requiere_revision, "
                + "fecha_creacion, fecha_actualizacion "
                + "FROM torneo_inscripcion_jugadores";
    }

    private TorneoInscripcionJugador convertir(ResultSet resultado)
            throws SQLException {
        TorneoInscripcionJugador jugador =
                new TorneoInscripcionJugador();
        jugador.setId(resultado.getLong("id"));
        jugador.setInscripcionId(
                resultado.getLong("inscripcion_id"));
        jugador.setOrdenIntegrante(
                resultado.getInt("orden_integrante"));
        long clienteId = resultado.getLong("cliente_id");
        jugador.setClienteId(
                resultado.wasNull() ? null : clienteId);
        jugador.setNombre(resultado.getString("nombre"));
        jugador.setApellido(resultado.getString("apellido"));
        jugador.setTelefono(resultado.getString("telefono"));
        jugador.setTelefonoNormalizado(
                resultado.getString("telefono_normalizado"));
        jugador.setResponsable(
                resultado.getBoolean("es_responsable"));
        jugador.setTipoVinculacion(TipoVinculacionTorneo.valueOf(
                resultado.getString("tipo_vinculacion")));
        jugador.setRequiereRevision(
                resultado.getBoolean("requiere_revision"));
        jugador.setFechaCreacion(fecha(
                resultado, "fecha_creacion"));
        jugador.setFechaActualizacion(fecha(
                resultado, "fecha_actualizacion"));
        return jugador;
    }

    private java.time.LocalDateTime fecha(
            ResultSet resultado,
            String columna) throws SQLException {
        Timestamp valor = resultado.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private void validarJugador(TorneoInscripcionJugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException(
                    "El integrante no puede ser nulo.");
        }
        validarInscripcionId(jugador.getInscripcionId());
        if (jugador.getOrdenIntegrante() != 1
                && jugador.getOrdenIntegrante() != 2) {
            throw new IllegalArgumentException(
                    "El orden del integrante debe ser 1 o 2.");
        }
        if (jugador.getNombre() == null
                || jugador.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del integrante es obligatorio.");
        }
        if (jugador.getApellido() == null
                || jugador.getApellido().isBlank()) {
            throw new IllegalArgumentException(
                    "El apellido del integrante es obligatorio.");
        }
        String nombre = jugador.getNombre().trim();
        String apellido = jugador.getApellido().trim();
        if (nombre.length() > 100 || apellido.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre y el apellido no pueden superar 100 caracteres.");
        }
        if (jugador.getTelefono() == null
                || jugador.getTelefono().isBlank()) {
            throw new IllegalArgumentException(
                    "El telefono del integrante es obligatorio.");
        }
        String telefono = jugador.getTelefono().trim();
        if (telefono.length() > 30) {
            throw new IllegalArgumentException(
                    "El telefono no puede superar 30 caracteres.");
        }
        validarTelefonoNormalizado(
                jugador.getTelefonoNormalizado());
        if (jugador.getClienteId() != null
                && jugador.getClienteId() <= 0) {
            throw new IllegalArgumentException(
                    "El cliente vinculado no es valido.");
        }
        if (jugador.getTipoVinculacion() == null) {
            throw new IllegalArgumentException(
                    "El tipo de vinculacion es obligatorio.");
        }
        if (jugador.getTipoVinculacion()
                == TipoVinculacionTorneo.SIN_VINCULAR
                && jugador.getClienteId() != null) {
            throw new IllegalArgumentException(
                    "Un integrante sin vincular no puede tener cliente asociado.");
        }
        if (jugador.getTipoVinculacion()
                != TipoVinculacionTorneo.SIN_VINCULAR
                && jugador.getClienteId() == null) {
            throw new IllegalArgumentException(
                    "Una vinculacion automatica o manual requiere un cliente.");
        }
        jugador.setNombre(nombre);
        jugador.setApellido(apellido);
        jugador.setTelefono(telefono);
    }

    private void validarTelefonoNormalizado(String telefono) {
        if (telefono == null
                || !telefono.matches("^[0-9]{8,15}$")) {
            throw new IllegalArgumentException(
                    "El telefono normalizado debe contener entre 8 y 15 digitos.");
        }
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
                    "El ID del integrante debe ser positivo.");
        }
    }

    private void validarInscripcionId(long inscripcionId) {
        if (inscripcionId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la inscripcion debe ser positivo.");
        }
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "La inscripcion ya tiene esa posicion o ese cliente.",
                    exception);
        }
        if (exception.getErrorCode() == 1452) {
            return new IllegalArgumentException(
                    "La inscripcion o el cliente asociado no existe.",
                    exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
