package api.publica;
import java.time.LocalDateTime;import java.util.List;
public final class NotificacionComunidadDTO {private NotificacionComunidadDTO(){}
 public record Notificacion(long notificacionId,String tipo,String titulo,String mensaje,String recursoTipo,Long recursoId,String urlContextual,LocalDateTime fechaCreacion,LocalDateTime fechaLectura,boolean leida){}
 public record Bandeja(List<Notificacion> notificaciones,long noLeidas,int total){}
 public record Lectura(long notificacionId,LocalDateTime fechaLectura,boolean leida){}
 public record LecturaTodas(int actualizadas,long noLeidas){}
}
