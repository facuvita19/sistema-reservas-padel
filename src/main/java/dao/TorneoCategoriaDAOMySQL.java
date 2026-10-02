package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import config.ConexionBD;
import negocio.FormatoCompetenciaTorneo;
import negocio.RamaTorneo;
import negocio.TorneoCategoria;

public class TorneoCategoriaDAOMySQL implements TorneoCategoriaDAO {

    @Override
    public void guardar(TorneoCategoria categoria) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            guardar(conexion, categoria);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar la categoria del torneo.",
                    exception);
        }
    }

    @Override
    public void guardar(
            Connection conexion,
            TorneoCategoria categoria) {
        validarConexion(conexion);
        validarCategoria(categoria);
        if (categoria.getId() <= 0) {
            insertar(conexion, categoria);
        } else {
            actualizar(conexion, categoria);
        }
    }

    private void insertar(
            Connection conexion,
            TorneoCategoria categoria) {
        String sql = "INSERT INTO torneo_categorias "
                + "(torneo_id, nombre, rama, cupo_parejas, "
                + "precio_inscripcion, premio_campeon, "
                + "premio_subcampeon, premio_descripcion, "
                + "formato_competencia, cantidad_grupos_3, "
                + "cantidad_grupos_4, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(sentencia, categoria);
            if (sentencia.executeUpdate() == 0) {
                throw new RuntimeException(
                        "No se pudo crear la categoria del torneo.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new RuntimeException(
                            "No se pudo recuperar el ID de la categoria.");
                }
                categoria.setId(claves.getLong(1));
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo crear la categoria del torneo.",
                    exception);
        }
    }

    private void actualizar(
            Connection conexion,
            TorneoCategoria categoria) {
        String sql = "UPDATE torneo_categorias SET torneo_id = ?, "
                + "nombre = ?, rama = ?, cupo_parejas = ?, "
                + "precio_inscripcion = ?, premio_campeon = ?, "
                + "premio_subcampeon = ?, premio_descripcion = ?, "
                + "formato_competencia = ?, cantidad_grupos_3 = ?, "
                + "cantidad_grupos_4 = ?, activo = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            cargarParametros(sentencia, categoria);
            sentencia.setLong(13, categoria.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "La categoria del torneo no existe.");
            }
        } catch (SQLException exception) {
            throw traducirError(
                    "No se pudo actualizar la categoria del torneo.",
                    exception);
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            TorneoCategoria categoria) throws SQLException {
        sentencia.setLong(1, categoria.getTorneoId());
        sentencia.setString(2, categoria.getNombre());
        sentencia.setString(3, categoria.getRama().name());
        sentencia.setInt(4, categoria.getCupoParejas());
        sentencia.setBigDecimal(5, categoria.getPrecioInscripcion());
        sentencia.setBigDecimal(6, categoria.getPremioCampeon());
        sentencia.setBigDecimal(7, categoria.getPremioSubcampeon());
        sentencia.setString(8, categoria.getPremioDescripcion());
        sentencia.setString(9, categoria.getFormatoCompetencia().name());
        sentencia.setInt(10, categoria.getCantidadGruposTres());
        sentencia.setInt(11, categoria.getCantidadGruposCuatro());
        sentencia.setBoolean(12, categoria.isActivo());
    }

    @Override
    public TorneoCategoria buscar(long id) {
        validarId(id);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscar(conexion, id);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar la categoria del torneo.",
                    exception);
        }
    }

    @Override
    public TorneoCategoria buscar(
            Connection conexion,
            long id) {
        validarConexion(conexion);
        validarId(id);
        String sql = consultaBase() + " WHERE tc.id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? convertir(resultado) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar la categoria del torneo.",
                    exception);
        }
    }

    @Override
    public List<TorneoCategoria> listarPorTorneo(long torneoId) {
        validarTorneoId(torneoId);
        return listarPorTorneo(torneoId, false);
    }

    @Override
    public List<TorneoCategoria> listarActivasPorTorneo(
            long torneoId) {
        validarTorneoId(torneoId);
        return listarPorTorneo(torneoId, true);
    }

    private List<TorneoCategoria> listarPorTorneo(
            long torneoId,
            boolean soloActivas) {
        String sql = consultaBase()
                + " WHERE tc.torneo_id = ?"
                + (soloActivas ? " AND tc.activo = TRUE" : "")
                + " ORDER BY tc.nombre ASC, tc.rama ASC, tc.id ASC";
        List<TorneoCategoria> categorias = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, torneoId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    categorias.add(convertir(resultado));
                }
            }
            return categorias;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudieron listar las categorias del torneo.",
                    exception);
        }
    }

    @Override
    public boolean existeNombreYRama(
            long torneoId,
            String nombre,
            RamaTorneo rama,
            long categoriaExcluidaId) {
        validarTorneoId(torneoId);
        if (nombre == null || nombre.isBlank() || rama == null) {
            throw new IllegalArgumentException(
                    "El nombre y la rama son obligatorios.");
        }
        if (categoriaExcluidaId < 0) {
            throw new IllegalArgumentException(
                    "El ID de la categoria excluida no es valido.");
        }

        String sql = "SELECT COUNT(*) FROM torneo_categorias "
                + "WHERE torneo_id = ? "
                + "AND LOWER(nombre) = LOWER(?) "
                + "AND rama = ? AND id <> ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, torneoId);
            sentencia.setString(2, nombre.trim());
            sentencia.setString(3, rama.name());
            sentencia.setLong(4, categoriaExcluidaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo comprobar la categoria duplicada.",
                    exception);
        }
    }

    @Override
    public void desactivar(long id) {
        validarId(id);
        String sql = "UPDATE torneo_categorias "
                + "SET activo = FALSE WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "La categoria del torneo no existe.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo desactivar la categoria del torneo.",
                    exception);
        }
    }

    private String consultaBase() {
        return "SELECT tc.id, tc.torneo_id, tc.nombre, tc.rama, "
                + "tc.cupo_parejas, tc.precio_inscripcion, "
                + "tc.premio_campeon, tc.premio_subcampeon, "
                + "tc.premio_descripcion, tc.formato_competencia, "
                + "tc.cantidad_grupos_3, tc.cantidad_grupos_4, "
                + "tc.activo, tc.fecha_creacion, "
                + "tc.fecha_actualizacion, t.nombre AS nombre_torneo, "
                + "(SELECT COUNT(*) FROM torneo_inscripciones ti "
                + "WHERE ti.torneo_categoria_id = tc.id "
                + "AND ti.estado = 'CONFIRMADA') AS parejas_confirmadas "
                 + "FROM torneo_categorias tc "
                + "INNER JOIN torneos t ON t.id = tc.torneo_id";
    }

    private TorneoCategoria convertir(ResultSet resultado)
            throws SQLException {
        TorneoCategoria categoria = new TorneoCategoria();
        categoria.setId(resultado.getLong("id"));
        categoria.setTorneoId(resultado.getLong("torneo_id"));
        categoria.setNombre(resultado.getString("nombre"));
        categoria.setRama(RamaTorneo.valueOf(
                resultado.getString("rama")));
        categoria.setCupoParejas(resultado.getInt("cupo_parejas"));
        categoria.setPrecioInscripcion(
                resultado.getBigDecimal("precio_inscripcion"));
        categoria.setPremioCampeon(
                resultado.getBigDecimal("premio_campeon"));
        categoria.setPremioSubcampeon(
                resultado.getBigDecimal("premio_subcampeon"));
        categoria.setPremioDescripcion(
                resultado.getString("premio_descripcion"));
        categoria.setFormatoCompetencia(FormatoCompetenciaTorneo.valueOf(
                resultado.getString("formato_competencia")));
        categoria.setCantidadGruposTres(
                resultado.getInt("cantidad_grupos_3"));
        categoria.setCantidadGruposCuatro(
                resultado.getInt("cantidad_grupos_4"));
        categoria.setActivo(resultado.getBoolean("activo"));
        categoria.setFechaCreacion(fecha(
                resultado, "fecha_creacion"));
        categoria.setFechaActualizacion(fecha(
                resultado, "fecha_actualizacion"));
        categoria.setNombreTorneo(
                resultado.getString("nombre_torneo"));
        categoria.setParejasConfirmadas(
                resultado.getInt("parejas_confirmadas"));
        return categoria;
    }

    private java.time.LocalDateTime fecha(
            ResultSet resultado,
            String columna) throws SQLException {
        Timestamp valor = resultado.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private void validarCategoria(TorneoCategoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoria no puede ser nula.");
        }
        validarTorneoId(categoria.getTorneoId());
        if (categoria.getNombre() == null
                || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la categoria es obligatorio.");
        }
        String nombre = categoria.getNombre().trim();
        if (nombre.length() > 50) {
            throw new IllegalArgumentException(
                    "El nombre de la categoria no puede superar 50 caracteres.");
        }
        if (categoria.getRama() == null) {
            throw new IllegalArgumentException(
                    "La rama de la categoria es obligatoria.");
        }
        if (categoria.getCupoParejas() <= 0) {
            throw new IllegalArgumentException(
                    "El cupo de parejas debe ser positivo.");
        }
        if (categoria.getPrecioInscripcion() == null
                || categoria.getPrecioInscripcion().signum() < 0) {
            throw new IllegalArgumentException(
                    "El precio de inscripcion no puede ser negativo.");
        }
        validarPremio(categoria.getPremioCampeon(),
                "El premio para los campeones");
        validarPremio(categoria.getPremioSubcampeon(),
                "El premio para los subcampeones");
        if (categoria.getPremioDescripcion() != null
                && categoria.getPremioDescripcion().length() > 500) {
            throw new IllegalArgumentException(
                    "La descripcion de premios no puede superar 500 caracteres.");
        }
        if (categoria.getFormatoCompetencia() == null) {
            throw new IllegalArgumentException(
                    "El formato de competencia es obligatorio.");
        }
        if (categoria.usaFaseGrupos()) {
            if (categoria.getCantidadGrupos() <= 0) {
                throw new IllegalArgumentException(
                        "La categoria debe tener al menos un grupo.");
            }
            if (categoria.getCapacidadGrupos() != categoria.getCupoParejas()) {
                throw new IllegalArgumentException(
                        "El cupo debe coincidir con la capacidad de los grupos.");
            }
        } else {
            categoria.setCantidadGruposTres(0);
            categoria.setCantidadGruposCuatro(0);
        }
        categoria.setNombre(nombre);
    }

    private void validarPremio(
            java.math.BigDecimal valor,
            String etiqueta) {
        if (valor != null && valor.signum() < 0) {
            throw new IllegalArgumentException(
                    etiqueta + " no puede ser negativo.");
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
                    "El ID de la categoria debe ser positivo.");
        }
    }

    private void validarTorneoId(long torneoId) {
        if (torneoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del torneo debe ser positivo.");
        }
    }

    private RuntimeException traducirError(
            String mensaje,
            SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new IllegalArgumentException(
                    "Ya existe una categoria con el mismo nombre y rama.",
                    exception);
        }
        if (exception.getErrorCode() == 1452) {
            return new IllegalArgumentException(
                    "El torneo asociado no existe.", exception);
        }
        return new RuntimeException(mensaje, exception);
    }
}
