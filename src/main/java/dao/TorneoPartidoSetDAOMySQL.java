package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import config.ConexionBD;
import negocio.TipoSetTorneo;
import negocio.TorneoPartidoSet;

public class TorneoPartidoSetDAOMySQL implements TorneoPartidoSetDAO {

    @Override
    public void guardar(Connection conexion, TorneoPartidoSet set) {
        validarConexion(conexion);
        validarSet(set);
        if (set.getId() <= 0) {
            insertar(conexion, set);
        } else {
            actualizar(conexion, set);
        }
    }

    private void insertar(Connection conexion, TorneoPartidoSet set) {
        String sql = "INSERT INTO torneo_partido_sets "
                + "(partido_id, numero_set, tipo, puntos_pareja_1, "
                + "puntos_pareja_2) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, set);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException("No se pudo crear el set.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new RuntimeException(
                            "No se pudo recuperar el ID del set.");
                }
                set.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw traducirError("No se pudo crear el set.", exception);
        }
    }

    private void actualizar(Connection conexion, TorneoPartidoSet set) {
        String sql = "UPDATE torneo_partido_sets SET partido_id = ?, "
                + "numero_set = ?, tipo = ?, puntos_pareja_1 = ?, "
                + "puntos_pareja_2 = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, set);
            sentencia.setLong(6, set.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("El set no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError("No se pudo actualizar el set.", exception);
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            TorneoPartidoSet set) throws SQLException {
        sentencia.setLong(1, set.getPartidoId());
        sentencia.setInt(2, set.getNumeroSet());
        sentencia.setString(3, set.getTipo().name());
        sentencia.setInt(4, set.getPuntosPareja1());
        sentencia.setInt(5, set.getPuntosPareja2());
    }

    @Override
    public List<TorneoPartidoSet> listarPorPartido(long partidoId) {
        validarPartidoId(partidoId);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return listarPorPartido(conexion, partidoId);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar los sets.", exception);
        }
    }

    @Override
    public List<TorneoPartidoSet> listarPorPartido(
            Connection conexion,
            long partidoId) {
        validarConexion(conexion);
        validarPartidoId(partidoId);
        String sql = "SELECT id, partido_id, numero_set, tipo, "
                + "puntos_pareja_1, puntos_pareja_2 "
                + "FROM torneo_partido_sets WHERE partido_id = ? "
                + "ORDER BY numero_set ASC, id ASC";
        List<TorneoPartidoSet> sets = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, partidoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) sets.add(convertir(resultado));
            }
            return sets;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar los sets.", exception);
        }
    }

    @Override
    public void reemplazarPorPartido(
            Connection conexion,
            long partidoId,
            List<TorneoPartidoSet> sets) {
        validarConexion(conexion);
        validarPartidoId(partidoId);
        validarLista(sets, partidoId);
        eliminarPorPartido(conexion, partidoId);
        for (TorneoPartidoSet set : sets) {
            set.setId(0);
            set.setPartidoId(partidoId);
            guardar(conexion, set);
        }
    }

    @Override
    public void eliminarPorPartido(
            Connection conexion,
            long partidoId) {
        validarConexion(conexion);
        validarPartidoId(partidoId);
        String sql = "DELETE FROM torneo_partido_sets WHERE partido_id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, partidoId);
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron eliminar los sets.", exception);
        }
    }

    private TorneoPartidoSet convertir(ResultSet resultado)
            throws SQLException {
        TorneoPartidoSet set = new TorneoPartidoSet();
        set.setId(resultado.getLong("id"));
        set.setPartidoId(resultado.getLong("partido_id"));
        set.setNumeroSet(resultado.getInt("numero_set"));
        set.setTipo(TipoSetTorneo.valueOf(resultado.getString("tipo")));
        set.setPuntosPareja1(resultado.getInt("puntos_pareja_1"));
        set.setPuntosPareja2(resultado.getInt("puntos_pareja_2"));
        return set;
    }

    private void validarLista(
            List<TorneoPartidoSet> sets,
            long partidoId) {
        if (sets == null) {
            throw new IllegalArgumentException(
                    "La lista de sets no puede ser nula.");
        }
        Set<Integer> numeros = new HashSet<>();
        for (TorneoPartidoSet set : sets) {
            if (set != null) set.setPartidoId(partidoId);
            validarSet(set);
            if (!numeros.add(set.getNumeroSet())) {
                throw new IllegalArgumentException(
                        "No puede repetirse el numero de set.");
            }
        }
    }

    private void validarSet(TorneoPartidoSet set) {
        if (set == null) {
            throw new IllegalArgumentException("El set no puede ser nulo.");
        }
        validarPartidoId(set.getPartidoId());
        if (set.getNumeroSet() < 1 || set.getNumeroSet() > 5) {
            throw new IllegalArgumentException(
                    "El numero de set debe estar entre 1 y 5.");
        }
        if (set.getTipo() == null) {
            throw new IllegalArgumentException(
                    "El tipo de set es obligatorio.");
        }
        if (set.getPuntosPareja1() < 0 || set.getPuntosPareja2() < 0) {
            throw new IllegalArgumentException(
                    "Los puntos no pueden ser negativos.");
        }
        if (set.getPuntosPareja1() == set.getPuntosPareja2()) {
            throw new IllegalArgumentException(
                    "Un set no puede terminar empatado.");
        }
    }

    private void validarConexion(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
    }

    private void validarPartidoId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID del partido debe ser positivo.");
        }
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "El partido ya tiene ese numero de set.", exception);
        }
        if (exception.getErrorCode() == 1452) {
            return new IllegalArgumentException(
                    "El partido asociado no existe.", exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
