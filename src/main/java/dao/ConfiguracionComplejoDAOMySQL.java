package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import config.ConexionBD;
import negocio.ConfiguracionComplejo;

public class ConfiguracionComplejoDAOMySQL implements ConfiguracionComplejoDAO {
    @Override
    public ConfiguracionComplejo obtener() {
        String sql = "SELECT id, nombre_comercial, razon_social, direccion, "
                + "telefono, whatsapp, email, instagram, moneda, porcentaje_senia, "
                + "anticipacion_minima_horas, cancelacion_minima_horas, "
                + "minutos_reserva_pendiente, pago_alias, pago_titular, "
                + "pago_entidad, pago_instrucciones, color_principal, ruta_logo, "
                + "fecha_actualizacion FROM configuracion_complejo WHERE id = 1";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            return rs.next() ? convertir(rs) : null;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo recuperar la configuración del complejo.", exception);
        }
    }

    @Override
    public void guardar(ConfiguracionComplejo c) {
        String sql = "INSERT INTO configuracion_complejo (id, nombre_comercial, "
                + "razon_social, direccion, telefono, whatsapp, email, instagram, "
                + "moneda, porcentaje_senia, anticipacion_minima_horas, "
                + "cancelacion_minima_horas, minutos_reserva_pendiente, pago_alias, "
                + "pago_titular, pago_entidad, pago_instrucciones, color_principal, ruta_logo) "
                + "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE nombre_comercial=VALUES(nombre_comercial), "
                + "razon_social=VALUES(razon_social), direccion=VALUES(direccion), "
                + "telefono=VALUES(telefono), whatsapp=VALUES(whatsapp), email=VALUES(email), "
                + "instagram=VALUES(instagram), moneda=VALUES(moneda), "
                + "porcentaje_senia=VALUES(porcentaje_senia), "
                + "anticipacion_minima_horas=VALUES(anticipacion_minima_horas), "
                + "cancelacion_minima_horas=VALUES(cancelacion_minima_horas), "
                + "minutos_reserva_pendiente=VALUES(minutos_reserva_pendiente), "
                + "pago_alias=VALUES(pago_alias), pago_titular=VALUES(pago_titular), "
                + "pago_entidad=VALUES(pago_entidad), pago_instrucciones=VALUES(pago_instrucciones), "
                + "color_principal=VALUES(color_principal), ruta_logo=VALUES(ruta_logo)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setString(1, c.getNombreComercial()); st.setString(2, c.getRazonSocial());
            st.setString(3, c.getDireccion()); st.setString(4, c.getTelefono());
            st.setString(5, c.getWhatsapp()); st.setString(6, c.getEmail());
            st.setString(7, c.getInstagram()); st.setString(8, c.getMoneda());
            st.setBigDecimal(9, c.getPorcentajeSenia()); st.setInt(10, c.getAnticipacionMinimaHoras());
            st.setInt(11, c.getCancelacionMinimaHoras()); st.setInt(12, c.getMinutosReservaPendiente());
            st.setString(13, c.getPagoAlias()); st.setString(14, c.getPagoTitular());
            st.setString(15, c.getPagoEntidad()); st.setString(16, c.getPagoInstrucciones());
            st.setString(17, c.getColorPrincipal()); st.setString(18, c.getRutaLogo());
            st.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudo guardar la configuración del complejo.", exception);
        }
    }

    private ConfiguracionComplejo convertir(ResultSet rs) throws SQLException {
        ConfiguracionComplejo c = new ConfiguracionComplejo();
        c.setId(rs.getLong("id")); c.setNombreComercial(rs.getString("nombre_comercial"));
        c.setRazonSocial(rs.getString("razon_social")); c.setDireccion(rs.getString("direccion"));
        c.setTelefono(rs.getString("telefono")); c.setWhatsapp(rs.getString("whatsapp"));
        c.setEmail(rs.getString("email")); c.setInstagram(rs.getString("instagram"));
        c.setMoneda(rs.getString("moneda")); c.setPorcentajeSenia(rs.getBigDecimal("porcentaje_senia"));
        c.setAnticipacionMinimaHoras(rs.getInt("anticipacion_minima_horas"));
        c.setCancelacionMinimaHoras(rs.getInt("cancelacion_minima_horas"));
        c.setMinutosReservaPendiente(rs.getInt("minutos_reserva_pendiente"));
        c.setPagoAlias(rs.getString("pago_alias")); c.setPagoTitular(rs.getString("pago_titular"));
        c.setPagoEntidad(rs.getString("pago_entidad")); c.setPagoInstrucciones(rs.getString("pago_instrucciones"));
        c.setColorPrincipal(rs.getString("color_principal")); c.setRutaLogo(rs.getString("ruta_logo"));
        Timestamp fecha = rs.getTimestamp("fecha_actualizacion");
        if (fecha != null) c.setFechaActualizacion(fecha.toLocalDateTime());
        return c;
    }
}
