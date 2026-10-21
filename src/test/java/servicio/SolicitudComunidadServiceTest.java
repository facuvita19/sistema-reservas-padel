package servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.*;
import negocio.*;

class SolicitudComunidadServiceTest {
    private final LocalDateTime ahora = LocalDateTime.of(2026, 10, 21, 12, 0);
    private ConvocatoriaComunidadDAO convocatorias;
    private SolicitudComunidadDAO solicitudes;
    private ParticipanteComunidadDAO participantes;
    private PerfilJugadorDAO perfiles;
    private PreferenciasComunidadDAO preferencias;
    private EventoComunidadDAO eventos;
    private Connection conexion;
    private SolicitudComunidadService servicio;
    private ConvocatoriaComunidad convocatoria;
    private PerfilJugador perfil;

    @BeforeEach
    void preparar() throws Exception {
        convocatorias = mock(ConvocatoriaComunidadDAO.class);
        solicitudes = mock(SolicitudComunidadDAO.class);
        participantes = mock(ParticipanteComunidadDAO.class);
        perfiles = mock(PerfilJugadorDAO.class);
        preferencias = mock(PreferenciasComunidadDAO.class);
        eventos = mock(EventoComunidadDAO.class);
        conexion = mock(Connection.class);
        servicio = new SolicitudComunidadService(convocatorias, solicitudes,
                participantes, perfiles, preferencias, eventos,
                () -> conexion, () -> ahora,
                (c, id) -> new SolicitudComunidadService.ReservaBloqueada(
                        10, EstadoReserva.CONFIRMADA, ahora.plusDays(1)));

        convocatoria = new ConvocatoriaComunidad();
        convocatoria.setId(30);
        convocatoria.setReservaId(10);
        convocatoria.setClienteCreadorId(2);
        convocatoria.setPerfilCreadorId(20);
        convocatoria.setParticipantesConfirmados(3);
        convocatoria.setLugaresBuscados(1);
        convocatoria.setEstado(EstadoConvocatoriaComunidad.ABIERTA);
        convocatoria.setFechaExpiracion(ahora.plusDays(1));
        when(convocatorias.buscar(conexion, 30, true))
                .thenReturn(convocatoria);

        perfil = new PerfilJugador();
        perfil.setId(71);
        perfil.setClienteId(7L);
        perfil.setEstado(EstadoPerfilJugador.DEFINITIVO);
        perfil.setNombrePublico("Ana Prueba");
        perfil.setActivo(true);
        perfil.setDisponibleParaJugar(true);
        perfil.setAceptaInvitaciones(true);
        when(perfiles.buscarPorCliente(conexion, 7)).thenReturn(perfil);
        PreferenciasComunidad p = new PreferenciasComunidad();
        p.setClienteId(7);
        p.setBuscarTurnos(true);
        when(preferencias.buscar(conexion, 7)).thenReturn(p);
        when(participantes.contarConfirmados(conexion, 30)).thenReturn(3);
        doAnswer(inv -> {
            SolicitudComunidad s = inv.getArgument(1);
            if (s.getId() == 0) s.setId(90);
            return null;
        }).when(solicitudes).guardar(eq(conexion), any());
    }

    @Test
    void creaSolicitudYEventoEnLaMismaTransaccion() throws Exception {
        SolicitudComunidad r = servicio.solicitar(7, 30, "  Me sumo  ");
        assertEquals(90, r.getId());
        assertEquals("Me sumo", r.getMensaje());
        assertEquals(EstadoSolicitudComunidad.PENDIENTE, r.getEstado());
        verify(solicitudes).guardar(conexion, r);
        verify(eventos).guardar(eq(conexion), argThat(e ->
                e.getTipo() == TipoEventoComunidad.SOLICITUD_RECIBIDA
                && e.getSolicitudId() == 90
                && e.getActorPerfilId() == 71));
        verify(conexion).commit();
    }

    @Test
    void repeticionPendienteEsIdempotente() throws Exception {
        SolicitudComunidad existente = solicitud(EstadoSolicitudComunidad.PENDIENTE);
        when(solicitudes.buscarPorConvocatoriaYPerfil(
                conexion, 30, 71, true)).thenReturn(existente);
        assertSame(existente, servicio.solicitar(7, 30, "otro texto"));
        verify(solicitudes, never()).guardar(any(), any());
        verify(eventos, never()).guardar(any(), any());
        verify(conexion).commit();
    }

    @Test
    void reabreSoloUnaSolicitudCancelada() throws Exception {
        SolicitudComunidad cancelada = solicitud(EstadoSolicitudComunidad.CANCELADA);
        when(solicitudes.buscarPorConvocatoriaYPerfil(
                conexion, 30, 71, true)).thenReturn(cancelada);
        doAnswer(inv -> {
            cancelada.setEstado(EstadoSolicitudComunidad.PENDIENTE);
            cancelada.setMensaje(inv.getArgument(2));
            return null;
        }).when(solicitudes).reabrirCancelada(eq(conexion),
                eq(cancelada), any(), eq(ahora));
        SolicitudComunidad r = servicio.solicitar(7, 30, "Vuelvo a intentar");
        assertEquals(EstadoSolicitudComunidad.PENDIENTE, r.getEstado());
        verify(solicitudes).reabrirCancelada(conexion, cancelada,
                "Vuelvo a intentar", ahora);
        verify(eventos).guardar(eq(conexion), argThat(e ->
                e.getDetalleJson().contains("reabierta")));
    }

    @Test
    void retirarEsIdempotenteYAuditable() throws Exception {
        SolicitudComunidad pendiente = solicitud(EstadoSolicitudComunidad.PENDIENTE);
        when(solicitudes.buscar(conexion, 45, true)).thenReturn(pendiente);
        when(convocatorias.buscar(conexion, 30, true)).thenReturn(convocatoria);
        SolicitudComunidad r = servicio.retirar(7, 45);
        assertEquals(EstadoSolicitudComunidad.CANCELADA, r.getEstado());
        assertEquals(ahora, r.getFechaResolucion());
        verify(eventos).guardar(eq(conexion), argThat(e ->
                e.getTipo() == TipoEventoComunidad.SOLICITUD_CANCELADA));
        verify(conexion).commit();
    }

    @Test
    void impideAutoSolicitudParticipanteYCupoCubierto() throws Exception {
        convocatoria.setPerfilCreadorId(71);
        assertThrows(SolicitudComunidadService.AutoSolicitudException.class,
                () -> servicio.solicitar(7, 30, null));
        convocatoria.setPerfilCreadorId(20);
        when(participantes.buscarConfirmadoPorPerfil(
                conexion, 30, 71, true)).thenReturn(new ParticipanteComunidad());
        assertThrows(SolicitudComunidadService.PerfilYaParticipaException.class,
                () -> servicio.solicitar(7, 30, null));
        when(participantes.buscarConfirmadoPorPerfil(
                conexion, 30, 71, true)).thenReturn(null);
        when(participantes.contarConfirmados(conexion, 30)).thenReturn(4);
        assertThrows(SolicitudComunidadService.ConvocatoriaSinCupoException.class,
                () -> servicio.solicitar(7, 30, null));
    }

    @Test
    void exigePreferenciasActivasYRevierteUnFallo() throws Exception {
        when(preferencias.buscar(conexion, 7)).thenReturn(null);
        assertThrows(
                SolicitudComunidadService.PerfilNoDisponibleParaTurnosException.class,
                () -> servicio.solicitar(7, 30, null));
        verify(conexion).rollback();
        verify(solicitudes, never()).guardar(any(), any());
    }

    @Test
    void noPermiteRetirarSolicitudAceptadaOAjenA() throws Exception {
        SolicitudComunidad aceptada = solicitud(EstadoSolicitudComunidad.ACEPTADA);
        when(solicitudes.buscar(conexion, 45, true)).thenReturn(aceptada);
        assertThrows(SolicitudComunidadService.SolicitudNoRetirableException.class,
                () -> servicio.retirar(7, 45));
        aceptada.setPerfilSolicitanteId(999);
        assertThrows(
                SolicitudComunidadService.SolicitudPropiaNoEncontradaException.class,
                () -> servicio.retirar(7, 45));
    }

    private SolicitudComunidad solicitud(EstadoSolicitudComunidad estado) {
        SolicitudComunidad s = new SolicitudComunidad();
        s.setId(45);
        s.setConvocatoriaId(30);
        s.setPerfilSolicitanteId(71);
        s.setEstado(estado);
        s.setFechaSolicitud(ahora.minusHours(1));
        return s;
    }
}
