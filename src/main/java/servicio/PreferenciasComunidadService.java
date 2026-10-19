package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import api.publica.PreferenciasComunidadDTO.Actualizar;
import api.publica.PreferenciasComunidadDTO.Estado;
import config.ConexionBD;
import dao.EventoComunidadDAO;
import dao.EventoComunidadDAOMySQL;
import dao.PerfilJugadorDAO;
import dao.PerfilJugadorDAOMySQL;
import dao.PreferenciasComunidadDAO;
import dao.PreferenciasComunidadDAOMySQL;
import negocio.EventoComunidad;
import negocio.PerfilJugador;
import negocio.PreferenciasComunidad;
import negocio.TipoEventoComunidad;

public class PreferenciasComunidadService {
    private final PreferenciasComunidadDAO preferencias;
    private final PerfilJugadorDAO perfiles;
    private final EventoComunidadDAO eventos;
    private final ProveedorConexion conexiones;
    private final ProveedorFecha reloj;

    public PreferenciasComunidadService(){this(new PreferenciasComunidadDAOMySQL(),new PerfilJugadorDAOMySQL(),new EventoComunidadDAOMySQL(),ConexionBD::obtenerConexion,LocalDateTime::now);}
    public PreferenciasComunidadService(PreferenciasComunidadDAO preferencias,PerfilJugadorDAO perfiles,EventoComunidadDAO eventos,ProveedorConexion conexiones,ProveedorFecha reloj){
        if(preferencias==null||perfiles==null||eventos==null||conexiones==null||reloj==null)throw new IllegalArgumentException("Las dependencias de Comunidad son obligatorias.");
        this.preferencias=preferencias;this.perfiles=perfiles;this.eventos=eventos;this.conexiones=conexiones;this.reloj=reloj;
    }

    public Estado obtener(long clienteId){
        validarCliente(clienteId);
        try(Connection c=conexiones.obtener()){
            PerfilJugador perfil=requerirPerfil(c,clienteId);
            return estado(perfil,preferencias.buscar(c,clienteId));
        }catch(SQLException e){throw new RuntimeException("No se pudieron consultar las preferencias de Comunidad.",e);}
    }

    public Estado actualizar(long clienteId,Actualizar entrada){
        validarCliente(clienteId);validarEntrada(entrada);
        try(Connection c=conexiones.obtener()){
            c.setAutoCommit(false);
            try{
                PerfilJugador perfil=requerirPerfil(c,clienteId);
                PreferenciasComunidad actual=preferencias.buscar(c,clienteId);
                long versionActual=actual==null?0:actual.getVersion();
                if(entrada.version()!=versionActual)throw new ConflictoPreferenciasComunidadException();
                boolean disponible=entrada.disponibleParaJugar();
                boolean invitaciones=disponible&&entrada.aceptaInvitaciones();
                boolean turnos=disponible&&entrada.buscarTurnos();
                boolean torneos=disponible&&entrada.buscarTorneos();
                boolean whatsapp=entrada.contactoWhatsapp();
                LocalDateTime ahora=reloj.ahora();
                LocalDateTime consentimiento=whatsapp?(actual!=null&&actual.isContactoWhatsapp()&&actual.getFechaConsentimientoWhatsapp()!=null?actual.getFechaConsentimientoWhatsapp():ahora):null;

                perfil.setDisponibleParaJugar(disponible);perfil.setAceptaInvitaciones(invitaciones);perfiles.guardar(c,perfil);
                PreferenciasComunidad siguiente=new PreferenciasComunidad();siguiente.setClienteId(clienteId);siguiente.setBuscarTurnos(turnos);siguiente.setBuscarTorneos(torneos);siguiente.setContactoWhatsapp(whatsapp);siguiente.setFechaConsentimientoWhatsapp(consentimiento);
                boolean guardado=actual==null?preferencias.insertarInicial(c,siguiente):preferencias.actualizarSiVersion(c,siguiente,versionActual);
                if(!guardado)throw new ConflictoPreferenciasComunidadException();
                registrar(c,TipoEventoComunidad.PREFERENCIAS_COMUNIDAD_ACTUALIZADAS,clienteId,perfil.getId(),detalle(disponible,invitaciones,turnos,torneos,whatsapp,versionActual+1),ahora);
                if((actual==null||!actual.isContactoWhatsapp())&&whatsapp)registrar(c,TipoEventoComunidad.CONTACTO_WHATSAPP_AUTORIZADO,clienteId,perfil.getId(),null,ahora);
                if(actual!=null&&actual.isContactoWhatsapp()&&!whatsapp)registrar(c,TipoEventoComunidad.CONTACTO_WHATSAPP_REVOCADO,clienteId,perfil.getId(),null,ahora);
                c.commit();siguiente.setVersion(versionActual+1);siguiente.setFechaActualizacion(ahora);return estado(perfil,siguiente);
            }catch(RuntimeException|SQLException e){rollback(c,e);throw e;}finally{auto(c);}
        }catch(SQLException e){throw new RuntimeException("No se pudieron actualizar las preferencias de Comunidad.",e);}
    }

    private PerfilJugador requerirPerfil(Connection c,long clienteId){PerfilJugador p=perfiles.buscarPorCliente(c,clienteId);if(p==null||!p.isActivo())throw new PerfilComunidadNoVinculadoException();return p;}
    private Estado estado(PerfilJugador p,PreferenciasComunidad x){boolean turnos=x!=null&&x.isBuscarTurnos(),torneos=x!=null&&x.isBuscarTorneos(),whatsapp=x!=null&&x.isContactoWhatsapp();boolean publicada=p.isDisponibleParaJugar()&&(turnos||torneos);return new Estado(p.isDisponibleParaJugar(),p.isAceptaInvitaciones(),turnos,torneos,whatsapp,whatsapp&&x.getFechaConsentimientoWhatsapp()!=null,publicada,x==null?null:x.getFechaConsentimientoWhatsapp(),x==null?null:x.getFechaActualizacion(),x==null?0:x.getVersion());}
    private void validarEntrada(Actualizar e){if(e==null||e.disponibleParaJugar()==null||e.aceptaInvitaciones()==null||e.buscarTurnos()==null||e.buscarTorneos()==null||e.contactoWhatsapp()==null||e.version()==null||e.version()<0)throw new IllegalArgumentException("Todas las preferencias y la version son obligatorias.");}
    private void validarCliente(long id){if(id<=0)throw new IllegalArgumentException("El cliente es invalido.");}
    private void registrar(Connection c,TipoEventoComunidad tipo,long cliente,long perfil,String detalle,LocalDateTime fecha){EventoComunidad e=new EventoComunidad();e.setTipo(tipo);e.setActorClienteId(cliente);e.setActorPerfilId(perfil);e.setDetalleJson(detalle);e.setFechaEvento(fecha);eventos.guardar(c,e);}
    private String detalle(boolean d,boolean i,boolean t,boolean o,boolean w,long v){return "{\"disponibleParaJugar\":"+d+",\"aceptaInvitaciones\":"+i+",\"buscarTurnos\":"+t+",\"buscarTorneos\":"+o+",\"contactoWhatsapp\":"+w+",\"version\":"+v+"}";}
    private void rollback(Connection c,Exception e){try{c.rollback();}catch(SQLException r){e.addSuppressed(r);}}
    private void auto(Connection c){try{c.setAutoCommit(true);}catch(SQLException ignored){}}
    public static final class ConflictoPreferenciasComunidadException extends RuntimeException{public ConflictoPreferenciasComunidadException(){super("Tus preferencias cambiaron desde otra sesion. Volve a cargarlas antes de guardar.");}}
    public static final class PerfilComunidadNoVinculadoException extends IllegalArgumentException{public PerfilComunidadNoVinculadoException(){super("Tu cuenta todavia no tiene un perfil deportivo activo vinculado.");}}
    @FunctionalInterface public interface ProveedorConexion{Connection obtener()throws SQLException;}
    @FunctionalInterface public interface ProveedorFecha{LocalDateTime ahora();}
}
