package dao;
import java.sql.Connection; import java.util.List; import negocio.EstadoParticipanteComunidad; import negocio.ParticipanteComunidad;
public interface ParticipanteComunidadDAO {
 void guardar(Connection c,ParticipanteComunidad v);
 List<ParticipanteComunidad> listarPorConvocatoria(Connection c,long convocatoriaId,boolean bloquear);
 int contarConfirmados(Connection c,long convocatoriaId);
 default ParticipanteComunidad buscarConfirmadoPorPerfil(Connection c,long convocatoriaId,long perfilId,boolean bloquear){return listarPorConvocatoria(c,convocatoriaId,bloquear).stream().filter(v->v.getEstado()==EstadoParticipanteComunidad.CONFIRMADO&&v.getPerfilJugadorId()!=null&&v.getPerfilJugadorId()==perfilId).findFirst().orElse(null);}
}
