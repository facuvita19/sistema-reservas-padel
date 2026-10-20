package api.publica;

import java.time.LocalDateTime;
import java.util.List;

public final class ConvocatoriaClienteDTO {
    private ConvocatoriaClienteDTO(){}
    public record Publicar(Integer participantesConfirmados,Integer lugaresBuscados,Long categoriaDeportivaId,String posicionBuscada,String comentario){}
    public record Cerrar(String motivo){}
    public record Convocatoria(long convocatoriaId,long reservaId,String estado,int participantesConfirmados,int lugaresBuscados,int lugaresDisponibles,Long categoriaDeportivaId,String posicionBuscada,String comentario,String motivoCierre,LocalDateTime fechaPublicacion,LocalDateTime fechaCierre,LocalDateTime fechaExpiracion,long version,boolean activa,boolean puedeCerrar){}
    public record Listado(List<Convocatoria> convocatorias,int total){public Listado{convocatorias=convocatorias==null?List.of():List.copyOf(convocatorias);if(total<0)throw new IllegalArgumentException("El total no puede ser negativo.");}}
    public record SinConvocatoria(long reservaId,String mensaje){}
}
