package api.publica;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import config.ConexionBD;
import dao.ConvocatoriaComunidadDAO;
import dao.ConvocatoriaComunidadDAOMySQL;
import negocio.ConvocatoriaComunidad;
import negocio.MotivoCierreConvocatoria;
import negocio.PosicionJugador;
import servicio.ConvocatoriaComunidadService;
import api.publica.ConvocatoriaClienteDTO.*;

public class ConvocatoriaClienteService {
    private final ConvocatoriaComunidadService operaciones;
    private final ConvocatoriaComunidadDAO convocatorias;
    private final ProveedorConexion conexiones;
    public ConvocatoriaClienteService(){this(new ConvocatoriaComunidadService(),new ConvocatoriaComunidadDAOMySQL(),ConexionBD::obtenerConexion);}
    public ConvocatoriaClienteService(ConvocatoriaComunidadService operaciones,ConvocatoriaComunidadDAO convocatorias,ProveedorConexion conexiones){if(operaciones==null||convocatorias==null||conexiones==null)throw new IllegalArgumentException("Las dependencias son obligatorias.");this.operaciones=operaciones;this.convocatorias=convocatorias;this.conexiones=conexiones;}
    public Listado listar(long clienteId){validarCliente(clienteId);try(Connection c=conexiones.obtener()){List<Convocatoria> items=convocatorias.listarPorCliente(c,clienteId).stream().map(this::dto).toList();return new Listado(items,items.size());}catch(SQLException e){throw new RuntimeException("No se pudieron consultar tus convocatorias.",e);}}
    public Convocatoria buscarPorReserva(long clienteId,long reservaId){validarCliente(clienteId);if(reservaId<=0)throw new IllegalArgumentException("La reserva debe ser positiva.");try(Connection c=conexiones.obtener()){ConvocatoriaComunidad v=convocatorias.buscarActivaPorReserva(c,reservaId,false);if(v==null)return null;if(v.getClienteCreadorId()!=clienteId)throw new ConvocatoriaComunidadService.ReservaAjenaONoEncontradaException();return dto(v);}catch(SQLException e){throw new RuntimeException("No se pudo consultar la convocatoria.",e);}}
    public Convocatoria publicar(long clienteId,long reservaId,Publicar entrada){if(entrada==null||entrada.participantesConfirmados()==null||entrada.lugaresBuscados()==null)throw new IllegalArgumentException("Participantes confirmados y lugares buscados son obligatorios.");PosicionJugador posicion=parsePosicion(entrada.posicionBuscada());var v=operaciones.publicar(clienteId,new ConvocatoriaComunidadService.Publicar(reservaId,entrada.participantesConfirmados(),entrada.lugaresBuscados(),entrada.categoriaDeportivaId(),posicion,entrada.comentario()));return dto(v);}
    public Convocatoria cerrar(long clienteId,long convocatoriaId,Cerrar entrada){MotivoCierreConvocatoria motivo=parseMotivo(entrada==null?null:entrada.motivo());return dto(operaciones.cerrarExternamente(clienteId,convocatoriaId,motivo));}
    private Convocatoria dto(ConvocatoriaComunidad v){boolean activa=v.getEstado().esActiva();return new Convocatoria(v.getId(),v.getReservaId(),v.getEstado().name(),v.getParticipantesConfirmados(),v.getLugaresBuscados(),v.lugaresDisponibles(),v.getCategoriaDeportivaId(),v.getPosicionBuscada()==null?null:v.getPosicionBuscada().name(),v.getComentario(),v.getMotivoCierre()==null?null:v.getMotivoCierre().name(),v.getFechaPublicacion(),v.getFechaCierre(),v.getFechaExpiracion(),v.getVersion(),activa,activa);}
    private PosicionJugador parsePosicion(String v){if(v==null||v.isBlank())return null;try{return PosicionJugador.valueOf(v.trim().toUpperCase());}catch(RuntimeException e){throw new IllegalArgumentException("La posicion buscada no es valida.");}}
    private MotivoCierreConvocatoria parseMotivo(String v){if(v==null||v.isBlank())return MotivoCierreConvocatoria.JUGADOR_ENCONTRADO_EXTERNAMENTE;try{return MotivoCierreConvocatoria.valueOf(v.trim().toUpperCase());}catch(RuntimeException e){throw new IllegalArgumentException("El motivo de cierre no es valido.");}}
    private void validarCliente(long id){if(id<=0)throw new IllegalArgumentException("El cliente es invalido.");}
    @FunctionalInterface public interface ProveedorConexion{Connection obtener()throws SQLException;}
}
