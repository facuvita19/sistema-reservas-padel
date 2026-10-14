package servicio;

import java.time.LocalDateTime;
import dao.*;
import negocio.*;

public class ProcesamientoOrdenPagoService {
    private final OrdenPagoDAO ordenes;
    private final EventoPagoDAO eventos;
    private final PagoService pagos;
    private final AcreditacionOrdenPagoTransaccionalService transaccional;

    public ProcesamientoOrdenPagoService() {
        this.ordenes=null; this.eventos=null; this.pagos=null;
        this.transaccional=new AcreditacionOrdenPagoTransaccionalService();
    }

    public ProcesamientoOrdenPagoService(OrdenPagoDAO o,EventoPagoDAO e,PagoService p) {
        if(o==null||e==null||p==null)throw new IllegalArgumentException("Las dependencias son obligatorias.");
        ordenes=o; eventos=e; pagos=p; transaccional=null;
    }

    public ProcesamientoOrdenPagoService(AcreditacionOrdenPagoTransaccionalService t) {
        if(t==null)throw new IllegalArgumentException("La dependencia es obligatoria.");
        ordenes=null; eventos=null; pagos=null; transaccional=t;
    }

    public OrdenPago crearIdempotente(OrdenPago o){new OrdenPagoService().preparar(o);return new OrdenPagoDAOMySQL().guardar(o);}

    public boolean procesarAprobacion(EventoPago e,String referencia){
        if(transaccional!=null)return transaccional.procesarAprobacion(e,referencia);
        if(e==null||!e.isFirmaValida())throw new IllegalArgumentException("El evento debe tener firma válida.");
        if(!eventos.registrarSiNuevo(e))return false;
        try{
            OrdenPago o=ordenes.buscar(e.getOrdenPagoId());
            if(o==null)throw new IllegalArgumentException("La orden no existe.");
            if(o.getEstado()==EstadoOrdenPago.APROBADA){eventos.marcarProcesado(e.getId(),"La orden ya estaba aprobada.");return false;}
            if(!o.getEstado().puedeCambiarA(EstadoOrdenPago.APROBADA))throw new IllegalStateException("La orden no admite aprobación desde "+o.getEstado());
            if(!ordenes.actualizarEstado(o.getId(),o.getVersion(),EstadoOrdenPago.APROBADA,referencia))throw new IllegalStateException("La orden cambió durante el procesamiento.");
            Pago p=new Pago();p.setReservaId(o.getReservaId());p.setOrdenPagoId(o.getId());p.setImporte(o.getImporte());p.setMetodoPago(o.getMetodo()==MetodoOrdenPago.TRANSFERENCIA_MANUAL?MetodoPago.TRANSFERENCIA:MetodoPago.TARJETA);p.setOrigenAcreditacion(e.getProveedor()==ProveedorPago.MANUAL?OrigenAcreditacion.ADMINISTRACION:OrigenAcreditacion.WEBHOOK);p.setEstado(EstadoPago.ACREDITADO);p.setReferencia(referencia);p.setReferenciaExterna(referencia);p.setFechaPago(LocalDateTime.now());pagos.guardar(p);eventos.marcarProcesado(e.getId(),"Acreditación registrada.");return true;
        }catch(RuntimeException x){if(e.getId()>0)eventos.marcarError(e.getId(),x.getMessage());throw x;}
    }
}
