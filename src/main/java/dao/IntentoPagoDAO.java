package dao;
import java.util.List;import negocio.*;
public interface IntentoPagoDAO { IntentoPago iniciar(long ordenPagoId); void finalizar(long id,EstadoIntentoPago estado,String referenciaExterna,String detalleError); List<IntentoPago> listarPorOrden(long ordenPagoId); }
