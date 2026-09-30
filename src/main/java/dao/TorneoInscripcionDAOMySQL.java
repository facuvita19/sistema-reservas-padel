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
import java.util.StringJoiner;

import config.ConexionBD;
import negocio.EstadoInscripcionTorneo;
import negocio.OrigenInscripcionTorneo;
import negocio.RamaTorneo;
import negocio.TorneoInscripcion;

public class TorneoInscripcionDAOMySQL
        implements TorneoInscripcionDAO {

    private final TorneoInscripcionJugadorDAO jugadorDAO;

    public TorneoInscripcionDAOMySQL() {
        this(new TorneoInscripcionJugadorDAOMySQL());
    }

    public TorneoInscripcionDAOMySQL(
            TorneoInscripcionJugadorDAO jugadorDAO) {
        if (jugadorDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de integrantes no puede ser nulo.");
        }
        this.jugadorDAO = jugadorDAO;
    }

    @Override
    public void guardar(TorneoInscripcion inscripcion) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, inscripcion);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar la inscripcion al torneo.",
                    exception);
        }
    }

    @Override
    public void guardar(
            Connection conexion,
            TorneoInscripcion inscripcion) {
        validarConexion(conexion);
        validarInscripcion(inscripcion);
        if (inscripcion.getId() <= 0) {
            insertar(conexion, inscripcion);
        } else {
            actualizar(conexion, inscripcion);
        }
    }

    private void insertar(
            Connection conexion,
            TorneoInscripcion inscripcion) {
        String sql = "INSERT INTO torneo_inscripciones "
                + "(torneo_categoria_id, estado, origen, "
                + "responsable_cliente_id, precio_inscripcion, comentarios, "
                + "observaciones_administrativas, usuario_gestion_id, "
                + "fecha_confirmacion, fecha_cancelacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, inscripcion);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException(
                        "No se pudo crear la inscripcion al torneo.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new RuntimeException(
                            "No se pudo recuperar el ID de la inscripcion.");
                }
                inscripcion.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo crear la inscripcion al torneo.",
                    exception);
        }
    }

    private void actualizar(
            Connection conexion,
            TorneoInscripcion inscripcion) {
        String sql = "UPDATE torneo_inscripciones SET "
                + "torneo_categoria_id = ?, estado = ?, origen = ?, "
                + "responsable_cliente_id = ?, precio_inscripcion = ?, "
                + "comentarios = ?, "
                + "observaciones_administrativas = ?, "
                + "usuario_gestion_id = ?, fecha_confirmacion = ?, "
                + "fecha_cancelacion = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, inscripcion);
            sentencia.setLong(11, inscripcion.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "La inscripcion al torneo no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo actualizar la inscripcion al torneo.",
                    exception);
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            TorneoInscripcion inscripcion) throws SQLException {
        sentencia.setLong(1, inscripcion.getTorneoCategoriaId());
        sentencia.setString(2, inscripcion.getEstado().name());
        sentencia.setString(3, inscripcion.getOrigen().name());
        setLongNullable(
                sentencia, 4, inscripcion.getResponsableClienteId());
        sentencia.setBigDecimal(5, inscripcion.getPrecioInscripcion());
        sentencia.setString(6, inscripcion.getComentarios());
        sentencia.setString(
                7, inscripcion.getObservacionesAdministrativas());
        setLongNullable(sentencia, 8, inscripcion.getUsuarioGestionId());
        setTimestampNullable(
                sentencia, 9, inscripcion.getFechaConfirmacion());
        setTimestampNullable(
                sentencia, 10, inscripcion.getFechaCancelacion());
    }

    @Override
    public TorneoInscripcion buscar(long id) {
        validarId(id);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscar(conexion, id);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar la inscripcion al torneo.",
                    exception);
        }
    }

    @Override
    public TorneoInscripcion buscar(
            Connection conexion,
            long id) {
        validarConexion(conexion);
        validarId(id);
        String sql = consultaBase() + " WHERE ti.id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) return null;
                TorneoInscripcion inscripcion = convertir(resultado);
                cargarJugadores(conexion, inscripcion);
                return inscripcion;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar la inscripcion al torneo.",
                    exception);
        }
    }

    @Override
    public List<TorneoInscripcion> listarPorCategoria(
            long torneoCategoriaId) {
        validarCategoriaId(torneoCategoriaId);
        String sql = consultaBase()
                + " WHERE ti.torneo_categoria_id = ? "
                + "ORDER BY ti.fecha_solicitud DESC, ti.id DESC";
        return consultar(sql, torneoCategoriaId, null);
    }

    @Override
    public List<TorneoInscripcion> listarPorEstado(
            EstadoInscripcionTorneo estado) {
        if (estado == null) {
            throw new IllegalArgumentException(
                    "El estado de la inscripcion es obligatorio.");
        }
        String sql = consultaBase()
                + " WHERE ti.estado = ? "
                + "ORDER BY ti.fecha_solicitud ASC, ti.id ASC";
        return consultar(sql, null, estado.name());
    }

    @Override
    public List<TorneoInscripcion> listarPorCliente(long clienteId) {
        validarClienteId(clienteId);
        String sql = consultaBase()
                + " WHERE EXISTS (SELECT 1 "
                + "FROM torneo_inscripcion_jugadores tij "
                + "WHERE tij.inscripcion_id = ti.id "
                + "AND tij.cliente_id = ?) "
                + "ORDER BY t.fecha_inicio DESC, ti.id DESC";
        return consultar(sql, clienteId, null);
    }

    private List<TorneoInscripcion> consultar(
            String sql,
            Long parametroLong,
            String parametroTexto) {
        List<TorneoInscripcion> inscripciones = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (parametroLong != null) {
                sentencia.setLong(1, parametroLong);
            } else if (parametroTexto != null) {
                sentencia.setString(1, parametroTexto);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    TorneoInscripcion inscripcion = convertir(resultado);
                    cargarJugadores(conexion, inscripcion);
                    inscripciones.add(inscripcion);
                }
            }
            return inscripciones;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar las inscripciones al torneo.",
                    exception);
        }
    }

    private void cargarJugadores(
            Connection conexion,
            TorneoInscripcion inscripcion) {
        inscripcion.setJugadores(jugadorDAO.listarPorInscripcion(
                conexion, inscripcion.getId()));
    }

    @Override
    public int contarPorCategoriaYEstados(
            Connection conexion,
            long torneoCategoriaId,
            List<EstadoInscripcionTorneo> estados) {
        validarConexion(conexion);
        validarCategoriaId(torneoCategoriaId);
        if (estados == null || estados.isEmpty()
                || estados.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Debe indicarse al menos un estado valido.");
        }

        StringJoiner parametros = new StringJoiner(", ");
        estados.forEach(estado -> parametros.add("?"));
        String sql = "SELECT COUNT(*) FROM torneo_inscripciones "
                + "WHERE torneo_categoria_id = ? AND estado IN ("
                + parametros + ")";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, torneoCategoriaId);
            for (int indice = 0; indice < estados.size(); indice++) {
                sentencia.setString(
                        indice + 2, estados.get(indice).name());
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron contar las inscripciones.",
                    exception);
        }
    }

    @Override
    public boolean clienteParticipaEnCategoria(
            Connection conexion,
            long torneoCategoriaId,
            long clienteId,
            Long inscripcionExcluidaId) {
        validarConexion(conexion);
        validarCategoriaId(torneoCategoriaId);
        validarClienteId(clienteId);
        if (inscripcionExcluidaId != null
                && inscripcionExcluidaId <= 0) {
            throw new IllegalArgumentException(
                    "La inscripcion excluida no es valida.");
        }

        String sql = "SELECT COUNT(*) "
                + "FROM torneo_inscripciones ti "
                + "INNER JOIN torneo_inscripcion_jugadores tij "
                + "ON tij.inscripcion_id = ti.id "
                + "WHERE ti.torneo_categoria_id = ? "
                + "AND tij.cliente_id = ? "
                + "AND ti.estado NOT IN ('RECHAZADA', 'CANCELADA')"
                + (inscripcionExcluidaId == null
                        ? ""
                        : " AND ti.id <> ?");
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, torneoCategoriaId);
            sentencia.setLong(2, clienteId);
            if (inscripcionExcluidaId != null) {
                sentencia.setLong(3, inscripcionExcluidaId);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar la participacion del cliente.",
                    exception);
        }
    }

    private String consultaBase() {
        return "SELECT ti.id, ti.torneo_categoria_id, ti.estado, "
                + "ti.origen, ti.responsable_cliente_id, "
                + "ti.precio_inscripcion, "
                + "ti.comentarios, ti.observaciones_administrativas, "
                + "ti.usuario_gestion_id, ti.fecha_solicitud, "
                + "ti.fecha_confirmacion, ti.fecha_cancelacion, "
                + "ti.fecha_actualizacion, t.nombre AS nombre_torneo, "
                + "tc.nombre AS nombre_categoria, tc.rama "
                + "FROM torneo_inscripciones ti "
                + "INNER JOIN torneo_categorias tc "
                + "ON tc.id = ti.torneo_categoria_id "
                + "INNER JOIN torneos t ON t.id = tc.torneo_id";
    }

    private TorneoInscripcion convertir(ResultSet resultado)
            throws SQLException {
        TorneoInscripcion inscripcion = new TorneoInscripcion();
        inscripcion.setId(resultado.getLong("id"));
        inscripcion.setTorneoCategoriaId(
                resultado.getLong("torneo_categoria_id"));
        inscripcion.setEstado(EstadoInscripcionTorneo.valueOf(
                resultado.getString("estado")));
        inscripcion.setOrigen(OrigenInscripcionTorneo.valueOf(
                resultado.getString("origen")));
        inscripcion.setResponsableClienteId(
                longNullable(resultado, "responsable_cliente_id"));
        inscripcion.setPrecioInscripcion(
                resultado.getBigDecimal("precio_inscripcion"));
        inscripcion.setComentarios(resultado.getString("comentarios"));
        inscripcion.setObservacionesAdministrativas(
                resultado.getString("observaciones_administrativas"));
        inscripcion.setUsuarioGestionId(
                longNullable(resultado, "usuario_gestion_id"));
        inscripcion.setFechaSolicitud(
                fecha(resultado, "fecha_solicitud"));
        inscripcion.setFechaConfirmacion(
                fecha(resultado, "fecha_confirmacion"));
        inscripcion.setFechaCancelacion(
                fecha(resultado, "fecha_cancelacion"));
        inscripcion.setFechaActualizacion(
                fecha(resultado, "fecha_actualizacion"));
        inscripcion.setNombreTorneo(
                resultado.getString("nombre_torneo"));
        inscripcion.setNombreCategoria(
                resultado.getString("nombre_categoria"));
        inscripcion.setRama(RamaTorneo.valueOf(
                resultado.getString("rama")));
        return inscripcion;
    }

    private Long longNullable(ResultSet resultado, String columna)
            throws SQLException {
        long valor = resultado.getLong(columna);
        return resultado.wasNull() ? null : valor;
    }

    private java.time.LocalDateTime fecha(
            ResultSet resultado,
            String columna) throws SQLException {
        Timestamp valor = resultado.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private void setLongNullable(
            PreparedStatement sentencia,
            int indice,
            Long valor) throws SQLException {
        if (valor == null) {
            sentencia.setNull(indice, Types.BIGINT);
        } else {
            sentencia.setLong(indice, valor);
        }
    }

    private void setTimestampNullable(
            PreparedStatement sentencia,
            int indice,
            java.time.LocalDateTime valor) throws SQLException {
        if (valor == null) {
            sentencia.setNull(indice, Types.TIMESTAMP);
        } else {
            sentencia.setTimestamp(indice, Timestamp.valueOf(valor));
        }
    }

    private void validarInscripcion(TorneoInscripcion inscripcion) {
        if (inscripcion == null) {
            throw new IllegalArgumentException(
                    "La inscripcion no puede ser nula.");
        }
        validarCategoriaId(inscripcion.getTorneoCategoriaId());
        if (inscripcion.getEstado() == null) {
            throw new IllegalArgumentException(
                    "El estado de la inscripcion es obligatorio.");
        }
        if (inscripcion.getOrigen() == null) {
            throw new IllegalArgumentException(
                    "El origen de la inscripcion es obligatorio.");
        }
        if (inscripcion.getResponsableClienteId() != null
                && inscripcion.getResponsableClienteId() <= 0) {
            throw new IllegalArgumentException(
                    "El cliente responsable no es valido.");
        }
        if (inscripcion.getPrecioInscripcion() == null
                || inscripcion.getPrecioInscripcion().signum() < 0) {
            throw new IllegalArgumentException(
                    "El precio de inscripcion no puede ser negativo.");
        }
        if (inscripcion.getComentarios() != null
                && inscripcion.getComentarios().length() > 500) {
            throw new IllegalArgumentException(
                    "Los comentarios no pueden superar 500 caracteres.");
        }
        if (inscripcion.getObservacionesAdministrativas() != null
                && inscripcion.getObservacionesAdministrativas().length()
                        > 1000) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar 1000 caracteres.");
        }
        if (inscripcion.getUsuarioGestionId() != null
                && inscripcion.getUsuarioGestionId() <= 0) {
            throw new IllegalArgumentException(
                    "El usuario de gestion no es valido.");
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
                    "El ID de la inscripcion debe ser positivo.");
        }
    }

    private void validarCategoriaId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la categoria debe ser positivo.");
        }
    }

    private void validarClienteId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID del cliente debe ser positivo.");
        }
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {
        if (exception.getErrorCode() == 1452) {
            return new IllegalArgumentException(
                    "La categoria, el cliente o el usuario asociado no existe.",
                    exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
