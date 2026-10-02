package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.EstadoPartidoTorneo;
import negocio.FaseTorneo;
import negocio.PosicionPartidoSiguiente;
import negocio.TorneoPartido;
import negocio.TipoPartidoGrupo;

public class TorneoPartidoDAOMySQL implements TorneoPartidoDAO {

    private final TorneoPartidoSetDAO setDAO;

    public TorneoPartidoDAOMySQL() {
        this(new TorneoPartidoSetDAOMySQL());
    }

    public TorneoPartidoDAOMySQL(TorneoPartidoSetDAO setDAO) {
        if (setDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de sets no puede ser nulo.");
        }
        this.setDAO = setDAO;
    }

    @Override
    public void guardar(TorneoPartido partido) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, partido);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar el partido.", exception);
        }
    }

    @Override
    public void guardar(Connection conexion, TorneoPartido partido) {
        validarConexion(conexion);
        validarPartido(partido);
        if (partido.getId() <= 0) insertar(conexion, partido);
        else actualizar(conexion, partido);
    }

    private void insertar(Connection conexion, TorneoPartido partido) {
        String sql = "INSERT INTO torneo_partidos "
                + "(torneo_categoria_id, grupo_id, fase, tipo_partido_grupo, orden_fase, "
                + "pareja_1_inscripcion_id, pareja_2_inscripcion_id, "
                + "estado, es_bye, fecha, hora_inicio, hora_fin, cancha_id, "
                + "ganadora_inscripcion_id, partido_siguiente_id, "
                + "posicion_siguiente, usuario_resultado_id, "
                + "fecha_finalizacion, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, partido);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException("No se pudo crear el partido.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new RuntimeException(
                            "No se pudo recuperar el ID del partido.");
                }
                partido.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw traducirError("No se pudo crear el partido.", exception);
        }
    }

    private void actualizar(Connection conexion, TorneoPartido partido) {
        String sql = "UPDATE torneo_partidos SET torneo_categoria_id = ?, "
                + "grupo_id = ?, fase = ?, tipo_partido_grupo = ?, "
                + "orden_fase = ?, pareja_1_inscripcion_id = ?, "
                + "pareja_2_inscripcion_id = ?, estado = ?, es_bye = ?, "
                + "fecha = ?, hora_inicio = ?, hora_fin = ?, cancha_id = ?, "
                + "ganadora_inscripcion_id = ?, partido_siguiente_id = ?, "
                + "posicion_siguiente = ?, usuario_resultado_id = ?, "
                + "fecha_finalizacion = ?, observaciones = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, partido);
            sentencia.setLong(20, partido.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("El partido no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError("No se pudo actualizar el partido.", exception);
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            TorneoPartido partido) throws SQLException {
        sentencia.setLong(1, partido.getTorneoCategoriaId());
        setLong(sentencia, 2, partido.getGrupoId());
        sentencia.setString(3, partido.getFase().name());
        sentencia.setString(4, partido.getTipoPartidoGrupo() == null
                ? null : partido.getTipoPartidoGrupo().name());
        sentencia.setInt(5, partido.getOrdenFase());
        setLong(sentencia, 6, partido.getPareja1InscripcionId());
        setLong(sentencia, 7, partido.getPareja2InscripcionId());
        sentencia.setString(8, partido.getEstado().name());
        sentencia.setBoolean(9, partido.isBye());
        setDate(sentencia, 10, partido.getFecha());
        setTime(sentencia, 11, partido.getHoraInicio());
        setTime(sentencia, 12, partido.getHoraFin());
        setLong(sentencia, 13, partido.getCanchaId());
        setLong(sentencia, 14, partido.getGanadoraInscripcionId());
        setLong(sentencia, 15, partido.getPartidoSiguienteId());
        sentencia.setString(16, partido.getPosicionSiguiente() == null
                ? null : partido.getPosicionSiguiente().name());
        setLong(sentencia, 17, partido.getUsuarioResultadoId());
        setTimestamp(sentencia, 18, partido.getFechaFinalizacion());
        sentencia.setString(19, partido.getObservaciones());
    }

    @Override
    public TorneoPartido buscar(long id) {
        validarId(id);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscar(conexion, id);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el partido.", exception);
        }
    }

    @Override
    public TorneoPartido buscar(Connection conexion, long id) {
        return buscarInterno(conexion, id, false);
    }

    @Override
    public TorneoPartido buscarParaActualizar(
            Connection conexion,
            long id) {
        return buscarInterno(conexion, id, true);
    }

    private TorneoPartido buscarInterno(
            Connection conexion,
            long id,
            boolean bloquear) {
        validarConexion(conexion);
        validarId(id);
        String sql = consultaBase() + " WHERE tp.id = ?"
                + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) return null;
                TorneoPartido partido = convertir(resultado);
                partido.setSets(setDAO.listarPorPartido(conexion, id));
                return partido;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el partido.", exception);
        }
    }

    @Override
    public List<TorneoPartido> listarPorCategoria(long categoriaId) {
        validarCategoriaId(categoriaId);
        String sql = consultaBase()
                + " WHERE tp.torneo_categoria_id = ? "
                + "ORDER BY FIELD(tp.fase, 'GRUPOS', 'ACCESO_1', "
                + "'ACCESO_2', 'ACCESO_3', 'ACCESO_4', 'ACCESO_5', "
                + "'DIECISEISAVOS', 'OCTAVOS', 'CUARTOS', "
                + "'SEMIFINAL', 'FINAL'), tp.orden_fase";
        return consultar(sql, categoriaId, null);
    }

    @Override
    public List<TorneoPartido> listarPorFase(
            long categoriaId,
            FaseTorneo fase) {
        validarCategoriaId(categoriaId);
        if (fase == null) {
            throw new IllegalArgumentException("La fase es obligatoria.");
        }
        String sql = consultaBase()
                + " WHERE tp.torneo_categoria_id = ? AND tp.fase = ? "
                + "ORDER BY tp.orden_fase ASC";
        return consultar(sql, categoriaId, fase.name());
    }

    @Override
    public List<TorneoPartido> listarPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        String sql = consultaBase()
                + " WHERE tp.fecha = ? "
                + "AND tp.estado IN ('PROGRAMADO', 'EN_CURSO', 'FINALIZADO') "
                + "ORDER BY tp.hora_inicio ASC, tp.cancha_id ASC, tp.id ASC";
        List<TorneoPartido> partidos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setDate(1, Date.valueOf(fecha));
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    TorneoPartido partido = convertir(resultado);
                    partido.setSets(setDAO.listarPorPartido(
                            conexion, partido.getId()));
                    partidos.add(partido);
                }
            }
            return partidos;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar los partidos por fecha.", exception);
        }
    }

    private List<TorneoPartido> consultar(
            String sql,
            long categoriaId,
            String fase) {
        List<TorneoPartido> partidos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            if (fase != null) sentencia.setString(2, fase);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    TorneoPartido partido = convertir(resultado);
                    partido.setSets(setDAO.listarPorPartido(
                            conexion, partido.getId()));
                    partidos.add(partido);
                }
            }
            return partidos;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar los partidos.", exception);
        }
    }

    @Override
    public List<TorneoPartido> listarPorGrupo(long grupoId) {
        if (grupoId <= 0) throw new IllegalArgumentException("El grupo no es valido.");
        String sql = consultaBase() + " WHERE tp.grupo_id = ? ORDER BY tp.orden_fase";
        return consultar(sql, grupoId, null);
    }

    @Override
    public boolean existenPartidosDeGrupos(Connection conexion, long categoriaId) {
        validarConexion(conexion);
        String sql = "SELECT COUNT(*) FROM torneo_partidos "
                + "WHERE torneo_categoria_id = ? AND fase = 'GRUPOS'";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron comprobar los partidos de grupos.", exception);
        }
    }

    @Override
    public boolean existeCuadroPorCategoria(long categoriaId) {
        validarCategoriaId(categoriaId);
        String sql = "SELECT COUNT(*) FROM torneo_partidos "
                + "WHERE torneo_categoria_id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar el cuadro.", exception);
        }
    }

    @Override
    public void eliminarCuadroPorCategoria(
            Connection conexion,
            long categoriaId) {
        validarConexion(conexion);
        validarCategoriaId(categoriaId);
        String desconectar = "UPDATE torneo_partidos SET "
                + "partido_siguiente_id = NULL, posicion_siguiente = NULL "
                + "WHERE torneo_categoria_id = ? AND fase <> 'GRUPOS'";
        String eliminar = "DELETE FROM torneo_partidos "
                + "WHERE torneo_categoria_id = ? AND fase <> 'GRUPOS'";
        try (PreparedStatement sentenciaDesconexion =
                    conexion.prepareStatement(desconectar);
                PreparedStatement sentenciaEliminacion =
                    conexion.prepareStatement(eliminar)) {
            sentenciaDesconexion.setLong(1, categoriaId);
            sentenciaDesconexion.executeUpdate();
            sentenciaEliminacion.setLong(1, categoriaId);
            sentenciaEliminacion.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo eliminar el cuadro eliminatorio.", exception);
        }
    }

    @Override
    public boolean horarioOcupado(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long partidoExcluidoId) {
        validarHorario(canchaId, fecha, horaInicio, horaFin, partidoExcluidoId);
        String sql = "SELECT COUNT(*) FROM torneo_partidos "
                + "WHERE cancha_id = ? AND fecha = ? "
                + "AND estado IN ('PROGRAMADO', 'EN_CURSO', 'FINALIZADO') "
                + "AND id <> ? AND hora_inicio < ? AND hora_fin > ?";
        return consultarOcupacion(sql, canchaId, fecha, horaInicio,
                horaFin, partidoExcluidoId);
    }

    @Override
    public boolean parejaOcupadaEnHorario(
            long inscripcionId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long partidoExcluidoId) {
        if (inscripcionId <= 0) {
            throw new IllegalArgumentException(
                    "La inscripcion debe ser positiva.");
        }
        validarHorario(1, fecha, horaInicio, horaFin, partidoExcluidoId);
        String sql = "SELECT COUNT(*) FROM torneo_partidos "
                + "WHERE (pareja_1_inscripcion_id = ? "
                + "OR pareja_2_inscripcion_id = ?) AND fecha = ? "
                + "AND estado IN ('PROGRAMADO', 'EN_CURSO', 'FINALIZADO') "
                + "AND id <> ? AND hora_inicio < ? AND hora_fin > ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, inscripcionId);
            sentencia.setLong(2, inscripcionId);
            sentencia.setDate(3, Date.valueOf(fecha));
            sentencia.setLong(4, partidoExcluidoId);
            sentencia.setTime(5, Time.valueOf(horaFin));
            sentencia.setTime(6, Time.valueOf(horaInicio));
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar la agenda de la pareja.", exception);
        }
    }

    private boolean consultarOcupacion(
            String sql,
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long partidoExcluidoId) {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, canchaId);
            sentencia.setDate(2, Date.valueOf(fecha));
            sentencia.setLong(3, partidoExcluidoId);
            sentencia.setTime(4, Time.valueOf(horaFin));
            sentencia.setTime(5, Time.valueOf(horaInicio));
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar la disponibilidad del partido.",
                    exception);
        }
    }

    private String consultaBase() {
        return "SELECT tp.id, tp.torneo_categoria_id, tp.grupo_id, tp.fase, "
                + "tp.tipo_partido_grupo, tp.orden_fase, tp.pareja_1_inscripcion_id, "
                + "tp.pareja_2_inscripcion_id, tp.estado, tp.es_bye, "
                + "tp.fecha, tp.hora_inicio, tp.hora_fin, tp.cancha_id, "
                + "tp.ganadora_inscripcion_id, tp.partido_siguiente_id, "
                + "tp.posicion_siguiente, tp.usuario_resultado_id, "
                + "tp.fecha_finalizacion, tp.observaciones, "
                + "tp.fecha_creacion, tp.fecha_actualizacion "
                + "FROM torneo_partidos tp";
    }

    private TorneoPartido convertir(ResultSet resultado)
            throws SQLException {
        TorneoPartido partido = new TorneoPartido();
        partido.setId(resultado.getLong("id"));
        partido.setTorneoCategoriaId(
                resultado.getLong("torneo_categoria_id"));
        partido.setGrupoId(longNullable(resultado, "grupo_id"));
        partido.setFase(FaseTorneo.valueOf(resultado.getString("fase")));
        String tipoGrupo = resultado.getString("tipo_partido_grupo");
        partido.setTipoPartidoGrupo(tipoGrupo == null ? null
                : TipoPartidoGrupo.valueOf(tipoGrupo));
        partido.setOrdenFase(resultado.getInt("orden_fase"));
        partido.setPareja1InscripcionId(
                longNullable(resultado, "pareja_1_inscripcion_id"));
        partido.setPareja2InscripcionId(
                longNullable(resultado, "pareja_2_inscripcion_id"));
        partido.setEstado(EstadoPartidoTorneo.valueOf(
                resultado.getString("estado")));
        partido.setBye(resultado.getBoolean("es_bye"));
        Date fecha = resultado.getDate("fecha");
        partido.setFecha(fecha == null ? null : fecha.toLocalDate());
        Time inicio = resultado.getTime("hora_inicio");
        partido.setHoraInicio(inicio == null ? null : inicio.toLocalTime());
        Time fin = resultado.getTime("hora_fin");
        partido.setHoraFin(fin == null ? null : fin.toLocalTime());
        partido.setCanchaId(longNullable(resultado, "cancha_id"));
        partido.setGanadoraInscripcionId(
                longNullable(resultado, "ganadora_inscripcion_id"));
        partido.setPartidoSiguienteId(
                longNullable(resultado, "partido_siguiente_id"));
        String posicion = resultado.getString("posicion_siguiente");
        partido.setPosicionSiguiente(posicion == null ? null
                : PosicionPartidoSiguiente.valueOf(posicion));
        partido.setUsuarioResultadoId(
                longNullable(resultado, "usuario_resultado_id"));
        partido.setFechaFinalizacion(
                localDateTime(resultado, "fecha_finalizacion"));
        partido.setObservaciones(resultado.getString("observaciones"));
        partido.setFechaCreacion(localDateTime(resultado, "fecha_creacion"));
        partido.setFechaActualizacion(
                localDateTime(resultado, "fecha_actualizacion"));
        return partido;
    }

    private void validarPartido(TorneoPartido partido) {
        if (partido == null) {
            throw new IllegalArgumentException(
                    "El partido no puede ser nulo.");
        }
        validarCategoriaId(partido.getTorneoCategoriaId());
        validarIdNullable(partido.getGrupoId(), "grupo");
        if (partido.getFase() == null) {
            throw new IllegalArgumentException("La fase es obligatoria.");
        }
        if (partido.getFase() == FaseTorneo.GRUPOS
                && (partido.getGrupoId() == null
                    || partido.getTipoPartidoGrupo() == null)) {
            throw new IllegalArgumentException(
                    "Un partido de grupos requiere grupo y tipo.");
        }
        if (partido.getFase() != FaseTorneo.GRUPOS
                && (partido.getGrupoId() != null
                    || partido.getTipoPartidoGrupo() != null)) {
            throw new IllegalArgumentException(
                    "Solo los partidos de grupos pueden indicar grupo y tipo.");
        }
        if (partido.getOrdenFase() <= 0) {
            throw new IllegalArgumentException(
                    "El orden de la fase debe ser positivo.");
        }
        if (partido.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        validarIdNullable(partido.getPareja1InscripcionId(), "pareja 1");
        validarIdNullable(partido.getPareja2InscripcionId(), "pareja 2");
        validarIdNullable(partido.getCanchaId(), "cancha");
        validarIdNullable(partido.getGanadoraInscripcionId(), "ganadora");
        validarIdNullable(partido.getPartidoSiguienteId(), "partido siguiente");
        validarIdNullable(partido.getUsuarioResultadoId(), "usuario");
        if (partido.getPareja1InscripcionId() != null
                && partido.getPareja1InscripcionId().equals(
                        partido.getPareja2InscripcionId())) {
            throw new IllegalArgumentException(
                    "Las parejas del partido deben ser diferentes.");
        }
        if (partido.isBye() && partido.unicaPareja() == null) {
            throw new IllegalArgumentException(
                    "Un BYE debe tener exactamente una pareja.");
        }
        boolean algunoProgramado = partido.getFecha() != null
                || partido.getHoraInicio() != null
                || partido.getHoraFin() != null
                || partido.getCanchaId() != null;
        if (algunoProgramado && !partido.estaProgramado()) {
            throw new IllegalArgumentException(
                    "La programacion debe indicar fecha, horario y cancha.");
        }
        if (partido.estaProgramado()
                && !partido.getHoraFin().isAfter(partido.getHoraInicio())) {
            throw new IllegalArgumentException(
                    "La hora final debe ser posterior a la inicial.");
        }
        Long ganadora = partido.getGanadoraInscripcionId();
        if (ganadora != null
                && !ganadora.equals(partido.getPareja1InscripcionId())
                && !ganadora.equals(partido.getPareja2InscripcionId())) {
            throw new IllegalArgumentException(
                    "Los ganadores deben pertenecer a uno de los equipos del partido.");
        }
        boolean destinoParcial = (partido.getPartidoSiguienteId() == null)
                != (partido.getPosicionSiguiente() == null);
        if (destinoParcial) {
            throw new IllegalArgumentException(
                    "El destino del ganador debe estar completo.");
        }
        if (partido.getId() > 0
                && partido.getPartidoSiguienteId() != null
                && partido.getId() == partido.getPartidoSiguienteId()) {
            throw new IllegalArgumentException(
                    "Un partido no puede avanzar hacia si mismo.");
        }
        if (partido.getEstado() == EstadoPartidoTorneo.FINALIZADO
                && (ganadora == null
                    || partido.getFechaFinalizacion() == null)) {
            throw new IllegalArgumentException(
                    "Un partido finalizado requiere ganadores y fecha.");
        }
        if (partido.getEstado() != EstadoPartidoTorneo.FINALIZADO
                && partido.getFechaFinalizacion() != null) {
            throw new IllegalArgumentException(
                    "Solo un partido finalizado puede tener fecha final.");
        }
        if (partido.getObservaciones() != null
                && partido.getObservaciones().length() > 500) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar 500 caracteres.");
        }
    }

    private void validarHorario(
            long canchaId,
            LocalDate fecha,
            LocalTime inicio,
            LocalTime fin,
            long excluidoId) {
        if (canchaId <= 0 || fecha == null || inicio == null || fin == null
                || !fin.isAfter(inicio) || excluidoId < 0) {
            throw new IllegalArgumentException(
                    "Los datos del horario no son validos.");
        }
    }

    private void validarIdNullable(Long id, String nombre) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException(
                    "El ID de " + nombre + " debe ser positivo.");
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
                    "El ID del partido debe ser positivo.");
        }
    }

    private void validarCategoriaId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la categoria debe ser positivo.");
        }
    }

    private void setLong(
            PreparedStatement sentencia,
            int indice,
            Long valor) throws SQLException {
        if (valor == null) sentencia.setNull(indice, Types.BIGINT);
        else sentencia.setLong(indice, valor);
    }

    private void setDate(
            PreparedStatement sentencia,
            int indice,
            LocalDate valor) throws SQLException {
        if (valor == null) sentencia.setNull(indice, Types.DATE);
        else sentencia.setDate(indice, Date.valueOf(valor));
    }

    private void setTime(
            PreparedStatement sentencia,
            int indice,
            LocalTime valor) throws SQLException {
        if (valor == null) sentencia.setNull(indice, Types.TIME);
        else sentencia.setTime(indice, Time.valueOf(valor));
    }

    private void setTimestamp(
            PreparedStatement sentencia,
            int indice,
            java.time.LocalDateTime valor) throws SQLException {
        if (valor == null) sentencia.setNull(indice, Types.TIMESTAMP);
        else sentencia.setTimestamp(indice, Timestamp.valueOf(valor));
    }

    private Long longNullable(ResultSet resultado, String columna)
            throws SQLException {
        long valor = resultado.getLong(columna);
        return resultado.wasNull() ? null : valor;
    }

    private java.time.LocalDateTime localDateTime(
            ResultSet resultado,
            String columna) throws SQLException {
        Timestamp valor = resultado.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe un partido con esa fase y orden.", exception);
        }
        if (exception.getErrorCode() == 1452) {
            return new IllegalArgumentException(
                    "La categoria, pareja, cancha, usuario o partido asociado no existe.",
                    exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
