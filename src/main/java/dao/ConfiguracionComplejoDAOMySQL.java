package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import config.ConexionBD;
import negocio.ConfiguracionComplejo;

public class ConfiguracionComplejoDAOMySQL
        implements ConfiguracionComplejoDAO {

    @Override
    public ConfiguracionComplejo obtener() {
        String sql = "SELECT id, nombre_comercial, razon_social, direccion, "
                + "telefono, whatsapp, email, instagram, moneda, "
                + "porcentaje_senia, anticipacion_minima_horas, "
                + "cancelacion_minima_horas, color_principal, ruta_logo, "
                + "fecha_actualizacion FROM configuracion_complejo "
                + "WHERE id = 1";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql);
                ResultSet resultado = sentencia.executeQuery()
        ) {
            return resultado.next() ? convertir(resultado) : null;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo recuperar la configuración del complejo.",
                    exception
            );
        }
    }

    @Override
    public void guardar(ConfiguracionComplejo configuracion) {
        String sql = "INSERT INTO configuracion_complejo (id, "
                + "nombre_comercial, razon_social, direccion, telefono, "
                + "whatsapp, email, instagram, moneda, porcentaje_senia, "
                + "anticipacion_minima_horas, cancelacion_minima_horas, "
                + "color_principal, ruta_logo) "
                + "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "nombre_comercial = VALUES(nombre_comercial), "
                + "razon_social = VALUES(razon_social), "
                + "direccion = VALUES(direccion), "
                + "telefono = VALUES(telefono), "
                + "whatsapp = VALUES(whatsapp), email = VALUES(email), "
                + "instagram = VALUES(instagram), moneda = VALUES(moneda), "
                + "porcentaje_senia = VALUES(porcentaje_senia), "
                + "anticipacion_minima_horas = VALUES(anticipacion_minima_horas), "
                + "cancelacion_minima_horas = VALUES(cancelacion_minima_horas), "
                + "color_principal = VALUES(color_principal), "
                + "ruta_logo = VALUES(ruta_logo)";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, configuracion.getNombreComercial());
            sentencia.setString(2, configuracion.getRazonSocial());
            sentencia.setString(3, configuracion.getDireccion());
            sentencia.setString(4, configuracion.getTelefono());
            sentencia.setString(5, configuracion.getWhatsapp());
            sentencia.setString(6, configuracion.getEmail());
            sentencia.setString(7, configuracion.getInstagram());
            sentencia.setString(8, configuracion.getMoneda());
            sentencia.setBigDecimal(9, configuracion.getPorcentajeSenia());
            sentencia.setInt(10, configuracion.getAnticipacionMinimaHoras());
            sentencia.setInt(11, configuracion.getCancelacionMinimaHoras());
            sentencia.setString(12, configuracion.getColorPrincipal());
            sentencia.setString(13, configuracion.getRutaLogo());
            sentencia.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo guardar la configuración del complejo.",
                    exception
            );
        }
    }

    private ConfiguracionComplejo convertir(ResultSet resultado)
            throws SQLException {
        ConfiguracionComplejo configuracion = new ConfiguracionComplejo();
        configuracion.setId(resultado.getLong("id"));
        configuracion.setNombreComercial(
                resultado.getString("nombre_comercial"));
        configuracion.setRazonSocial(resultado.getString("razon_social"));
        configuracion.setDireccion(resultado.getString("direccion"));
        configuracion.setTelefono(resultado.getString("telefono"));
        configuracion.setWhatsapp(resultado.getString("whatsapp"));
        configuracion.setEmail(resultado.getString("email"));
        configuracion.setInstagram(resultado.getString("instagram"));
        configuracion.setMoneda(resultado.getString("moneda"));
        configuracion.setPorcentajeSenia(
                resultado.getBigDecimal("porcentaje_senia"));
        configuracion.setAnticipacionMinimaHoras(
                resultado.getInt("anticipacion_minima_horas"));
        configuracion.setCancelacionMinimaHoras(
                resultado.getInt("cancelacion_minima_horas"));
        configuracion.setColorPrincipal(
                resultado.getString("color_principal"));
        configuracion.setRutaLogo(resultado.getString("ruta_logo"));

        Timestamp actualizacion =
                resultado.getTimestamp("fecha_actualizacion");
        if (actualizacion != null) {
            configuracion.setFechaActualizacion(
                    actualizacion.toLocalDateTime());
        }
        return configuracion;
    }
}
