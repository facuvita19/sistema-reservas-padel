package servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import api.publica.PreferenciasComunidadDTO.Actualizar;
import dao.EventoComunidadDAO;
import dao.PerfilJugadorDAO;
import dao.PreferenciasComunidadDAO;
import negocio.PerfilJugador;
import negocio.PreferenciasComunidad;

class PreferenciasComunidadServiceTest {
    PreferenciasComunidadDAO preferencias;PerfilJugadorDAO perfiles;EventoComunidadDAO eventos;Connection conexion;PreferenciasComunidadService servicio;PerfilJugador perfil;LocalDateTime ahora;
    @BeforeEach void preparar()throws Exception{
        preferencias=mock(PreferenciasComunidadDAO.class);perfiles=mock(PerfilJugadorDAO.class);eventos=mock(EventoComunidadDAO.class);conexion=mock(Connection.class);ahora=LocalDateTime.of(2026,10,19,11,30);
        servicio=new PreferenciasComunidadService(preferencias,perfiles,eventos,()->conexion,()->ahora);
        perfil=new PerfilJugador();perfil.setId(71);perfil.setClienteId(7L);perfil.setNombrePublico("Ana Prueba");perfil.setActivo(true);when(perfiles.buscarPorCliente(conexion,7)).thenReturn(perfil);
    }
    @Test void lecturaInicialNoCreaFila(){var r=servicio.obtener(7);assertEquals(0,r.version());assertFalse(r.publicadoEnComunidad());verify(preferencias,never()).guardar(any(),any());}
    @Test void guardaObjetivosYFechaConsentimiento()throws Exception{when(preferencias.insertarInicial(eq(conexion),any())).thenReturn(true);var r=servicio.actualizar(7,new Actualizar(true,true,true,true,true,0L));assertTrue(r.publicadoEnComunidad());assertTrue(r.consentimientoWhatsappVigente());assertEquals(1,r.version());assertEquals(ahora,r.fechaConsentimientoWhatsapp());verify(perfiles).guardar(conexion,perfil);verify(eventos,times(2)).guardar(eq(conexion),any());verify(conexion).commit();}
    @Test void pausarNormalizaObjetivosEInvitaciones()throws Exception{PreferenciasComunidad actual=new PreferenciasComunidad();actual.setClienteId(7);actual.setBuscarTurnos(true);actual.setBuscarTorneos(true);actual.setContactoWhatsapp(true);actual.setFechaConsentimientoWhatsapp(ahora.minusDays(2));actual.setVersion(4);when(preferencias.buscar(conexion,7)).thenReturn(actual);when(preferencias.actualizarSiVersion(eq(conexion),any(),eq(4L))).thenReturn(true);var r=servicio.actualizar(7,new Actualizar(false,true,true,true,true,4L));assertFalse(r.disponibleParaJugar());assertFalse(r.aceptaInvitaciones());assertFalse(r.buscarTurnos());assertFalse(r.buscarTorneos());assertTrue(r.contactoWhatsapp());}
    @Test void rechazaVersionObsoleta()throws Exception{PreferenciasComunidad actual=new PreferenciasComunidad();actual.setClienteId(7);actual.setVersion(3);when(preferencias.buscar(conexion,7)).thenReturn(actual);assertThrows(PreferenciasComunidadService.ConflictoPreferenciasComunidadException.class,()->servicio.actualizar(7,new Actualizar(true,true,true,false,false,2L)));verify(conexion).rollback();verify(perfiles,never()).guardar(any(),any());}
    @Test void revocacionEliminaFechaActiva()throws Exception{PreferenciasComunidad actual=new PreferenciasComunidad();actual.setClienteId(7);actual.setContactoWhatsapp(true);actual.setFechaConsentimientoWhatsapp(ahora.minusDays(1));actual.setVersion(2);when(preferencias.buscar(conexion,7)).thenReturn(actual);when(preferencias.actualizarSiVersion(eq(conexion),any(),eq(2L))).thenReturn(true);var r=servicio.actualizar(7,new Actualizar(true,false,true,false,false,2L));assertFalse(r.consentimientoWhatsappVigente());assertNull(r.fechaConsentimientoWhatsapp());verify(eventos,times(2)).guardar(eq(conexion),any());}
    @Test void exigePerfilActivo(){when(perfiles.buscarPorCliente(conexion,7)).thenReturn(null);assertThrows(PreferenciasComunidadService.PerfilComunidadNoVinculadoException.class,()->servicio.obtener(7));}
}
