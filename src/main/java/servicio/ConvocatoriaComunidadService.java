package servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

import config.ConexionBD;
import dao.ConvocatoriaComunidadDAO;
import dao.ConvocatoriaComunidadDAOMySQL;
import dao.EventoComunidadDAO;
import dao.EventoComunidadDAOMySQL;
import dao.ParticipanteComunidadDAO;
import dao.ParticipanteComunidadDAOMySQL;
import dao.PerfilJugadorDAO;
import dao.PerfilJugadorDAOMySQL;
import dao.SolicitudComunidadDAO;
import dao.SolicitudComunidadDAOMySQL;
import negocio.ConvocatoriaComunidad;
import negocio.EstadoConvocatoriaComunidad;
import negocio.EstadoReserva;
import negocio.EventoComunidad;
import negocio.MotivoCierreConvocatoria;
import negocio.OrigenParticipanteComunidad;
import negocio.ParticipanteComunidad;
import negocio.PerfilJugador;
import negocio.PosicionJugador;
import negocio.TipoEventoComunidad;
import negocio.TipoParticipanteComunidad;

public class ConvocatoriaComunidadService {
    private final ConvocatoriaComunidadDAO convocatorias;
    private final ParticipanteComunidadDAO participantes;
    private final SolicitudComunidadDAO solicitudes;
    private final EventoComunidadDAO eventos;
    private final PerfilJugadorDAO perfiles;
    private final ProveedorConexion conexiones;
    private final ProveedorFecha reloj;
    private final LectorReservaBloqueada reservas;

    public ConvocatoriaComunidadService(){
        this(new ConvocatoriaComunidadDAOMySQL(),new ParticipanteComunidadDAOMySQL(),
             new SolicitudComunidadDAOMySQL(),new EventoComunidadDAOMySQL(),
             new PerfilJugadorDAOMySQL(),ConexionBD::obtenerConexion,
             LocalDateTime::now,ConvocatoriaComunidadService::bloquearReserva);
    }

    public ConvocatoriaComunidadService(ConvocatoriaComunidadDAO convocatorias,
            ParticipanteComunidadDAO participantes,SolicitudComunidadDAO solicitudes,
            EventoComunidadDAO eventos,PerfilJugadorDAO perfiles,
            ProveedorConexion conexiones,ProveedorFecha reloj,
            LectorReservaBloqueada reservas){
        this.convocatorias=Objects.requireNonNull(convocatorias);
        this.participantes=Objects.requireNonNull(participantes);
        this.solicitudes=Objects.requireNonNull(solicitudes);
        this.eventos=Objects.requireNonNull(eventos);
        this.perfiles=Objects.requireNonNull(perfiles);
        this.conexiones=Objects.requireNonNull(conexiones);
        this.reloj=Objects.requireNonNull(reloj);
        this.reservas=Objects.requireNonNull(reservas);
    }

    public ConvocatoriaComunidad publicar(long clienteId,Publicar entrada){
        validarCliente(clienteId);
        if(entrada==null||entrada.reservaId()<=0)throw new IllegalArgumentException("La reserva es obligatoria.");
        if(entrada.participantesConfirmados()<1||entrada.participantesConfirmados()>3)
            throw new IllegalArgumentException("Los participantes confirmados deben estar entre 1 y 3.");
        if(entrada.lugaresBuscados()<1||entrada.lugaresBuscados()>3
                ||entrada.participantesConfirmados()+entrada.lugaresBuscados()!=4)
            throw new IllegalArgumentException("La convocatoria debe completar exactamente cuatro jugadores.");
        String comentario=limitar(entrada.comentario(),500,"El comentario no puede superar 500 caracteres.");
        try(Connection c=conexiones.obtener()){
            c.setAutoCommit(false);
            try{
                ReservaBloqueada reserva=requerirReservaPublicable(c,clienteId,entrada.reservaId());
                PerfilJugador perfil=requerirPerfil(c,clienteId);
                ConvocatoriaComunidad existente=convocatorias.buscarActivaPorReserva(c,reserva.id(),true);
                if(existente!=null){
                    if(equivalente(existente,entrada,comentario)){
                        c.commit();return existente;
                    }
                    throw new ConflictoConvocatoriaActivaException();
                }
                LocalDateTime ahora=reloj.ahora();
                ConvocatoriaComunidad v=new ConvocatoriaComunidad();
                v.setReservaId(reserva.id());v.setClienteCreadorId(clienteId);v.setPerfilCreadorId(perfil.getId());
                v.setCategoriaDeportivaId(entrada.categoriaDeportivaId());v.setPosicionBuscada(entrada.posicionBuscada());
                v.setParticipantesConfirmados(entrada.participantesConfirmados());v.setLugaresBuscados(entrada.lugaresBuscados());
                v.setEstado(EstadoConvocatoriaComunidad.ABIERTA);v.setComentario(comentario);
                v.setFechaPublicacion(ahora);v.setFechaExpiracion(reserva.inicio());v.validar();
                convocatorias.guardar(c,v);
                guardarParticipantesIniciales(c,v,perfil,entrada.participantesConfirmados(),ahora);
                int confirmados=participantes.contarConfirmados(c,v.getId());
                if(confirmados!=entrada.participantesConfirmados())throw new IllegalStateException("No coinciden los participantes confirmados.");
                registrar(c,TipoEventoComunidad.CONVOCATORIA_PUBLICADA,v,clienteId,perfil.getId(),
                    "{\"reservaId\":"+reserva.id()+",\"participantesConfirmados\":"+confirmados+",\"lugaresBuscados\":"+entrada.lugaresBuscados()+"}",ahora);
                c.commit();return v;
            }catch(RuntimeException|SQLException e){rollback(c,e);throw e;}finally{auto(c);}
        }catch(SQLException e){throw new RuntimeException("No se pudo publicar la convocatoria.",e);}
    }

    public ConvocatoriaComunidad cerrarExternamente(long clienteId,long convocatoriaId,MotivoCierreConvocatoria motivo){
        validarCliente(clienteId);if(convocatoriaId<=0)throw new IllegalArgumentException("La convocatoria es obligatoria.");
        MotivoCierreConvocatoria efectivo=motivo==null?MotivoCierreConvocatoria.JUGADOR_ENCONTRADO_EXTERNAMENTE:motivo;
        try(Connection c=conexiones.obtener()){
            c.setAutoCommit(false);
            try{
                ConvocatoriaComunidad v=requerirConvocatoria(c,convocatoriaId);
                requerirPropietario(v,clienteId);
                if(v.getEstado()==EstadoConvocatoriaComunidad.CERRADA_EXTERNAMENTE){c.commit();return v;}
                if(!v.getEstado().esActiva())throw new ConvocatoriaNoAdmiteCambiosException();
                LocalDateTime ahora=reloj.ahora();v.setEstado(EstadoConvocatoriaComunidad.CERRADA_EXTERNAMENTE);
                v.setMotivoCierre(efectivo);v.setFechaCierre(ahora);convocatorias.guardar(c,v);
                solicitudes.cerrarPendientes(c,v.getId());
                registrar(c,TipoEventoComunidad.CONVOCATORIA_CERRADA_EXTERNAMENTE,v,clienteId,v.getPerfilCreadorId(),
                    "{\"motivo\":\""+efectivo.name()+"\"}",ahora);
                c.commit();return v;
            }catch(RuntimeException|SQLException e){rollback(c,e);throw e;}finally{auto(c);}
        }catch(SQLException e){throw new RuntimeException("No se pudo cerrar la convocatoria.",e);}
    }

    public ConvocatoriaComunidad completarSiCupo(long convocatoriaId){
        if(convocatoriaId<=0)throw new IllegalArgumentException("La convocatoria es obligatoria.");
        try(Connection c=conexiones.obtener()){
            c.setAutoCommit(false);
            try{
                ConvocatoriaComunidad v=requerirConvocatoria(c,convocatoriaId);
                if(v.getEstado()==EstadoConvocatoriaComunidad.COMPLETA){c.commit();return v;}
                if(!v.getEstado().esActiva())throw new ConvocatoriaNoAdmiteCambiosException();
                int confirmados=participantes.contarConfirmados(c,v.getId());
                if(confirmados<4){v.setParticipantesConfirmados(confirmados);convocatorias.guardar(c,v);c.commit();return v;}
                LocalDateTime ahora=reloj.ahora();v.setEstado(EstadoConvocatoriaComunidad.COMPLETA);
                v.setMotivoCierre(MotivoCierreConvocatoria.SOLICITUD_ACEPTADA);v.setFechaCierre(ahora);convocatorias.guardar(c,v);
                solicitudes.cerrarPendientes(c,v.getId());
                registrar(c,TipoEventoComunidad.CONVOCATORIA_COMPLETA,v,v.getClienteCreadorId(),v.getPerfilCreadorId(),
                    "{\"participantesConfirmados\":"+confirmados+"}",ahora);
                c.commit();return v;
            }catch(RuntimeException|SQLException e){rollback(c,e);throw e;}finally{auto(c);}
        }catch(SQLException e){throw new RuntimeException("No se pudo completar la convocatoria.",e);}
    }

    public ConvocatoriaComunidad vencerSiCorresponde(long convocatoriaId){
        if(convocatoriaId<=0)throw new IllegalArgumentException("La convocatoria es obligatoria.");
        try(Connection c=conexiones.obtener()){
            c.setAutoCommit(false);
            try{
                ConvocatoriaComunidad v=requerirConvocatoria(c,convocatoriaId);
                if(!v.getEstado().esActiva()||reloj.ahora().isBefore(v.getFechaExpiracion())){c.commit();return v;}
                LocalDateTime ahora=reloj.ahora();v.setEstado(EstadoConvocatoriaComunidad.VENCIDA);v.setFechaCierre(ahora);
                convocatorias.guardar(c,v);solicitudes.cerrarPendientes(c,v.getId());
                registrar(c,TipoEventoComunidad.CONVOCATORIA_VENCIDA,v,v.getClienteCreadorId(),v.getPerfilCreadorId(),null,ahora);
                c.commit();return v;
            }catch(RuntimeException|SQLException e){rollback(c,e);throw e;}finally{auto(c);}
        }catch(SQLException e){throw new RuntimeException("No se pudo vencer la convocatoria.",e);}
    }

    private ReservaBloqueada requerirReservaPublicable(Connection c,long clienteId,long reservaId)throws SQLException{
        ReservaBloqueada r=reservas.buscar(c,reservaId);if(r==null||r.clienteId()!=clienteId)throw new ReservaAjenaONoEncontradaException();
        if(r.estado()!=EstadoReserva.PENDIENTE&&r.estado()!=EstadoReserva.CONFIRMADA)throw new ReservaNoPublicableException();
        if(!r.inicio().isAfter(reloj.ahora()))throw new ReservaNoPublicableException();return r;
    }
    private PerfilJugador requerirPerfil(Connection c,long clienteId){PerfilJugador p=perfiles.buscarPorCliente(c,clienteId);if(p==null||!p.isActivo())throw new PerfilComunidadNoVinculadoException();return p;}
    private ConvocatoriaComunidad requerirConvocatoria(Connection c,long id){ConvocatoriaComunidad v=convocatorias.buscar(c,id,true);if(v==null)throw new ConvocatoriaNoEncontradaException();return v;}
    private void requerirPropietario(ConvocatoriaComunidad v,long clienteId){if(v.getClienteCreadorId()!=clienteId)throw new ConvocatoriaAjenaException();}
    private boolean equivalente(ConvocatoriaComunidad v,Publicar e,String comentario){return v.getClienteCreadorId()>0&&v.getParticipantesConfirmados()==e.participantesConfirmados()&&v.getLugaresBuscados()==e.lugaresBuscados()&&Objects.equals(v.getCategoriaDeportivaId(),e.categoriaDeportivaId())&&v.getPosicionBuscada()==e.posicionBuscada()&&Objects.equals(v.getComentario(),comentario);}
    private void guardarParticipantesIniciales(Connection c,ConvocatoriaComunidad v,PerfilJugador perfil,int cantidad,LocalDateTime ahora){ParticipanteComunidad titular=participante(v.getId(),perfil.getId(),TipoParticipanteComunidad.TITULAR,OrigenParticipanteComunidad.TITULAR,"Titular de la reserva",ahora);participantes.guardar(c,titular);for(int i=1;i<cantidad;i++)participantes.guardar(c,participante(v.getId(),null,TipoParticipanteComunidad.EXTERNO,OrigenParticipanteComunidad.EXTERNO,"Participante confirmado "+(i+1),ahora));}
    private ParticipanteComunidad participante(long convocatoria,Long perfil,TipoParticipanteComunidad tipo,OrigenParticipanteComunidad origen,String descripcion,LocalDateTime ahora){ParticipanteComunidad p=new ParticipanteComunidad();p.setConvocatoriaId(convocatoria);p.setPerfilJugadorId(perfil);p.setTipo(tipo);p.setOrigen(origen);p.setDescripcionPublica(descripcion);p.setFechaConfirmacion(ahora);return p;}
    private void registrar(Connection c,TipoEventoComunidad tipo,ConvocatoriaComunidad v,Long cliente,Long perfil,String detalle,LocalDateTime ahora){EventoComunidad e=new EventoComunidad();e.setTipo(tipo);e.setConvocatoriaId(v.getId());e.setActorClienteId(cliente);e.setActorPerfilId(perfil);e.setDetalleJson(detalle);e.setFechaEvento(ahora);eventos.guardar(c,e);}
    private static ReservaBloqueada bloquearReserva(Connection c,long id)throws SQLException{String q="SELECT id,cliente_id,estado,fecha,hora_inicio FROM reservas WHERE id=? FOR UPDATE";try(PreparedStatement s=c.prepareStatement(q)){s.setLong(1,id);try(ResultSet r=s.executeQuery()){if(!r.next())return null;return new ReservaBloqueada(r.getLong("id"),r.getLong("cliente_id"),EstadoReserva.valueOf(r.getString("estado")),LocalDateTime.of(r.getDate("fecha").toLocalDate(),r.getTime("hora_inicio").toLocalTime()));}}}
    private String limitar(String v,int max,String mensaje){if(v==null||v.isBlank())return null;String x=v.trim();if(x.length()>max)throw new IllegalArgumentException(mensaje);return x;}
    private void validarCliente(long id){if(id<=0)throw new IllegalArgumentException("El cliente es invalido.");}
    private void rollback(Connection c,Exception e){try{c.rollback();}catch(SQLException r){e.addSuppressed(r);}}
    private void auto(Connection c){try{c.setAutoCommit(true);}catch(SQLException ignored){}}

    public record Publicar(long reservaId,int participantesConfirmados,int lugaresBuscados,Long categoriaDeportivaId,PosicionJugador posicionBuscada,String comentario){}
    public record ReservaBloqueada(long id,long clienteId,EstadoReserva estado,LocalDateTime inicio){}
    @FunctionalInterface public interface ProveedorConexion{Connection obtener()throws SQLException;}
    @FunctionalInterface public interface ProveedorFecha{LocalDateTime ahora();}
    @FunctionalInterface public interface LectorReservaBloqueada{ReservaBloqueada buscar(Connection c,long id)throws SQLException;}
    public static final class ConflictoConvocatoriaActivaException extends IllegalStateException{public ConflictoConvocatoriaActivaException(){super("La reserva ya tiene una convocatoria activa con otros datos.");}}
    public static final class ConvocatoriaNoAdmiteCambiosException extends IllegalStateException{public ConvocatoriaNoAdmiteCambiosException(){super("La convocatoria ya no admite cambios.");}}
    public static final class ReservaAjenaONoEncontradaException extends IllegalArgumentException{public ReservaAjenaONoEncontradaException(){super("La reserva no existe o no pertenece a la cuenta.");}}
    public static final class ReservaNoPublicableException extends IllegalStateException{public ReservaNoPublicableException(){super("Solo se pueden publicar reservas futuras pendientes o confirmadas.");}}
    public static final class PerfilComunidadNoVinculadoException extends IllegalArgumentException{public PerfilComunidadNoVinculadoException(){super("Tu cuenta todavia no tiene un perfil deportivo activo vinculado.");}}
    public static final class ConvocatoriaNoEncontradaException extends IllegalArgumentException{public ConvocatoriaNoEncontradaException(){super("La convocatoria no existe.");}}
    public static final class ConvocatoriaAjenaException extends SecurityException{public ConvocatoriaAjenaException(){super("No podes administrar una convocatoria de otra cuenta.");}}
}
