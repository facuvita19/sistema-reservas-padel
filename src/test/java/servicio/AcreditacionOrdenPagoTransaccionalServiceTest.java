package servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import negocio.EventoPago;
import negocio.ProveedorPago;

class AcreditacionOrdenPagoTransaccionalServiceTest {
    private EventoPago evento(){EventoPago e=new EventoPago();e.setOrdenPagoId(1);e.setProveedor(ProveedorPago.MERCADO_PAGO);e.setEventoExternoId("evt-1");e.setTipo("payment.updated");e.setPayloadHash("a".repeat(64));e.setFirmaValida(true);return e;}

    @Test void exigeFirmaValidaAntesDeAbrirConexion(){
        var factory=mock(AcreditacionOrdenPagoTransaccionalService.ConexionFactory.class);
        var service=new AcreditacionOrdenPagoTransaccionalService(factory,Clock.systemUTC());
        EventoPago e=evento();e.setFirmaValida(false);
        assertThrows(IllegalArgumentException.class,()->service.procesarAprobacion(e,"mp-1"));
        verifyNoInteractions(factory);
    }

    @Test void rollbackSiLaTransaccionFallaAntesDeProcesar(){
        Connection connection=mock(Connection.class);
        var factory=mock(AcreditacionOrdenPagoTransaccionalService.ConexionFactory.class);
        when(factory.abrir()).thenReturn(connection);
        try{when(connection.getAutoCommit()).thenReturn(true);when(connection.prepareStatement(anyString(),anyInt())).thenThrow(new java.sql.SQLException("fallo simulado"));}catch(Exception ex){fail(ex);}
        var service=new AcreditacionOrdenPagoTransaccionalService(factory,Clock.systemUTC());
        assertThrows(RuntimeException.class,()->service.procesarAprobacion(evento(),"mp-1"));
        try{verify(connection).setAutoCommit(false);verify(connection).rollback();verify(connection).setAutoCommit(true);}catch(Exception ex){fail(ex);}
    }
}
