package negocio;
import java.time.LocalDateTime;
public class CategoriaDeportiva {
 private long id; private String codigo,nombre,colorHex; private int nivelOrden; private boolean activo=true; private LocalDateTime fechaCreacion,fechaActualizacion;
 public long getId(){return id;} public void setId(long v){id=v;} public String getCodigo(){return codigo;} public void setCodigo(String v){codigo=v;} public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;} public int getNivelOrden(){return nivelOrden;} public void setNivelOrden(int v){nivelOrden=v;} public String getColorHex(){return colorHex;} public void setColorHex(String v){colorHex=v;} public boolean isActivo(){return activo;} public void setActivo(boolean v){activo=v;} public LocalDateTime getFechaCreacion(){return fechaCreacion;} public void setFechaCreacion(LocalDateTime v){fechaCreacion=v;} public LocalDateTime getFechaActualizacion(){return fechaActualizacion;} public void setFechaActualizacion(LocalDateTime v){fechaActualizacion=v;}
}
