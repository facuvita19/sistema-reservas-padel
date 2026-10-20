package api.publica;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class ConvocatoriaPublicaDTO {
    private ConvocatoriaPublicaDTO(){}
    public record Categoria(long id,String nombre,String color){}
    public record Convocante(long perfilId,String nombre,String iniciales,String posicion,Categoria categoria){}
    public record Turno(long convocatoriaId,long reservaId,LocalDate fecha,LocalTime horaInicio,LocalTime horaFin,String cancha,int participantesConfirmados,int lugaresBuscados,int lugaresDisponibles,String resumenCupo,Categoria categoriaBuscada,String posicionBuscada,String comentario,Convocante convocante){}
    public record Pagina(List<Turno> convocatorias,int pagina,int tamano,long total,int totalPaginas){public Pagina{convocatorias=convocatorias==null?List.of():List.copyOf(convocatorias);if(pagina<1||tamano<1||total<0||totalPaginas<0)throw new IllegalArgumentException("La pagina publica de turnos es invalida.");}}
}
