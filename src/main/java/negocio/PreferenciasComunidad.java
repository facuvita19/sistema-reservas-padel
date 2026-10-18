package negocio;
import java.time.LocalDateTime;
public class PreferenciasComunidad {
 private long clienteId; private boolean buscarTurnos,buscarTorneos,contactoWhatsapp; private LocalDateTime fechaConsentimientoWhatsapp,fechaActualizacion; private long version;
 public long getClienteId(){return clienteId;} public void setClienteId(long v){clienteId=v;}
 public boolean isBuscarTurnos(){return buscarTurnos;} public void setBuscarTurnos(boolean v){buscarTurnos=v;}
 public boolean isBuscarTorneos(){return buscarTorneos;} public void setBuscarTorneos(boolean v){buscarTorneos=v;}
 public boolean isContactoWhatsapp(){return contactoWhatsapp;} public void setContactoWhatsapp(boolean v){contactoWhatsapp=v;}
 public LocalDateTime getFechaConsentimientoWhatsapp(){return fechaConsentimientoWhatsapp;} public void setFechaConsentimientoWhatsapp(LocalDateTime v){fechaConsentimientoWhatsapp=v;}
 public LocalDateTime getFechaActualizacion(){return fechaActualizacion;} public void setFechaActualizacion(LocalDateTime v){fechaActualizacion=v;}
 public long getVersion(){return version;} public void setVersion(long v){version=v;}
 public void validar(){if(clienteId<=0)throw new IllegalArgumentException("El cliente es obligatorio.");if(contactoWhatsapp&&fechaConsentimientoWhatsapp==null)throw new IllegalArgumentException("El consentimiento de WhatsApp debe quedar fechado.");}
}