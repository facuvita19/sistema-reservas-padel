package dao;

import java.sql.*;
import negocio.PreferenciasComunidad;

public class PreferenciasComunidadDAOMySQL implements PreferenciasComunidadDAO {
    public PreferenciasComunidad buscar(Connection c,long id){
        try(PreparedStatement s=c.prepareStatement("SELECT cliente_id,buscar_turnos,buscar_torneos,contacto_whatsapp,fecha_consentimiento_whatsapp,fecha_actualizacion,version FROM comunidad_preferencias WHERE cliente_id=?")){
            s.setLong(1,id);
            try(ResultSet r=s.executeQuery()){
                if(!r.next())return null;
                PreferenciasComunidad p=new PreferenciasComunidad();
                p.setClienteId(r.getLong(1));p.setBuscarTurnos(r.getBoolean(2));p.setBuscarTorneos(r.getBoolean(3));p.setContactoWhatsapp(r.getBoolean(4));
                Timestamp t=r.getTimestamp(5);if(t!=null)p.setFechaConsentimientoWhatsapp(t.toLocalDateTime());
                t=r.getTimestamp(6);if(t!=null)p.setFechaActualizacion(t.toLocalDateTime());p.setVersion(r.getLong(7));return p;
            }
        }catch(SQLException e){throw new RuntimeException("No se pudieron consultar las preferencias de Comunidad.",e);}
    }

    public void guardar(Connection c,PreferenciasComunidad p){
        p.validar();
        String q="INSERT INTO comunidad_preferencias(cliente_id,buscar_turnos,buscar_torneos,contacto_whatsapp,fecha_consentimiento_whatsapp,version) VALUES(?,?,?,?,?,0) ON DUPLICATE KEY UPDATE buscar_turnos=VALUES(buscar_turnos),buscar_torneos=VALUES(buscar_torneos),contacto_whatsapp=VALUES(contacto_whatsapp),fecha_consentimiento_whatsapp=VALUES(fecha_consentimiento_whatsapp),version=version+1";
        try(PreparedStatement s=c.prepareStatement(q)){parametros(s,p);s.executeUpdate();}catch(SQLException e){throw new RuntimeException("No se pudieron guardar las preferencias de Comunidad.",e);}
    }

    public boolean insertarInicial(Connection c,PreferenciasComunidad p){
        p.validar();
        String q="INSERT IGNORE INTO comunidad_preferencias(cliente_id,buscar_turnos,buscar_torneos,contacto_whatsapp,fecha_consentimiento_whatsapp,version) VALUES(?,?,?,?,?,1)";
        try(PreparedStatement s=c.prepareStatement(q)){parametros(s,p);return s.executeUpdate()==1;}catch(SQLException e){throw new RuntimeException("No se pudieron crear las preferencias de Comunidad.",e);}
    }

    public boolean actualizarSiVersion(Connection c,PreferenciasComunidad p,long versionEsperada){
        p.validar();
        String q="UPDATE comunidad_preferencias SET buscar_turnos=?,buscar_torneos=?,contacto_whatsapp=?,fecha_consentimiento_whatsapp=?,version=version+1 WHERE cliente_id=? AND version=?";
        try(PreparedStatement s=c.prepareStatement(q)){
            s.setBoolean(1,p.isBuscarTurnos());s.setBoolean(2,p.isBuscarTorneos());s.setBoolean(3,p.isContactoWhatsapp());
            fecha(s,4,p);s.setLong(5,p.getClienteId());s.setLong(6,versionEsperada);return s.executeUpdate()==1;
        }catch(SQLException e){throw new RuntimeException("No se pudieron actualizar las preferencias de Comunidad.",e);}
    }

    private void parametros(PreparedStatement s,PreferenciasComunidad p)throws SQLException{
        s.setLong(1,p.getClienteId());s.setBoolean(2,p.isBuscarTurnos());s.setBoolean(3,p.isBuscarTorneos());s.setBoolean(4,p.isContactoWhatsapp());fecha(s,5,p);
    }
    private void fecha(PreparedStatement s,int i,PreferenciasComunidad p)throws SQLException{
        if(p.getFechaConsentimientoWhatsapp()==null)s.setNull(i,Types.TIMESTAMP);else s.setTimestamp(i,Timestamp.valueOf(p.getFechaConsentimientoWhatsapp()));
    }
}
