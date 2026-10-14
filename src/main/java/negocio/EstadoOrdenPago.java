package negocio;
import java.util.Set;
public enum EstadoOrdenPago {
 CREADA, ESPERANDO_PAGO, ESPERANDO_VERIFICACION, PROCESANDO, APROBADA, RECHAZADA, CANCELADA, EXPIRADA, REEMBOLSO_PENDIENTE, REEMBOLSADA, ERROR;
 public boolean esTerminal(){return Set.of(APROBADA,RECHAZADA,CANCELADA,EXPIRADA,REEMBOLSADA).contains(this);}
 public boolean puedeCambiarA(EstadoOrdenPago siguiente){
  if(siguiente==null||siguiente==this)return siguiente==this;
  return switch(this){
   case CREADA -> Set.of(ESPERANDO_PAGO,ESPERANDO_VERIFICACION,CANCELADA,ERROR).contains(siguiente);
   case ESPERANDO_PAGO -> Set.of(PROCESANDO,APROBADA,RECHAZADA,CANCELADA,EXPIRADA,ERROR).contains(siguiente);
   case ESPERANDO_VERIFICACION -> Set.of(PROCESANDO,APROBADA,RECHAZADA,CANCELADA,EXPIRADA,ERROR).contains(siguiente);
   case PROCESANDO -> Set.of(APROBADA,RECHAZADA,EXPIRADA,ERROR).contains(siguiente);
   case APROBADA -> siguiente==REEMBOLSO_PENDIENTE;
   case REEMBOLSO_PENDIENTE -> Set.of(REEMBOLSADA,ERROR).contains(siguiente);
   case ERROR -> Set.of(ESPERANDO_PAGO,ESPERANDO_VERIFICACION,PROCESANDO,CANCELADA).contains(siguiente);
   default -> false;
  };
 }
}
