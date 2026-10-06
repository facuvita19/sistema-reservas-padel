package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dao.BloqueoCanchaDAO;
import dao.ReservaDAO;
import negocio.Cancha;
import negocio.ConfiguracionComplejo;
import negocio.EstadoReserva;
import negocio.OrigenReserva;
import negocio.Reserva;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaDAO reservaDAO;
    @Mock
    private BloqueoCanchaDAO bloqueoDAO;
    @Mock
    private CanchaService canchaService;
    @Mock
    private ConfiguracionComplejoService configuracionService;
    @Mock
    private DisponibilidadCanchaService disponibilidadCanchaService;

    private ReservaService service;
    private Cancha cancha;

    @BeforeEach
    void preparar() {
        service = new ReservaService(
                reservaDAO,
                bloqueoDAO,
                canchaService,
                configuracionService,
                disponibilidadCanchaService);

        cancha = new Cancha();
        cancha.setId(2L);
        cancha.setNombre("Cancha Central");
        cancha.setActivo(true);
        cancha.setHoraApertura(LocalTime.of(8, 0));
        cancha.setHoraCierre(LocalTime.of(23, 0));
        cancha.setDuracionReserva(90);
        cancha.setPrecio(new BigDecimal("12000.00"));
        cancha.setDiasDisponibles(EnumSet.allOf(DayOfWeek.class));

        lenient().when(canchaService.buscar(2L)).thenReturn(cancha);
        lenient().when(disponibilidadCanchaService.estaDisponibleParaReserva(
                anyLong(),
                any(LocalDate.class),
                any(LocalTime.class),
                any(LocalTime.class),
                anyLong())).thenReturn(true);
    }

    @Test
    void guardarPendienteWebConfiguraOrigenEstadoYVencimiento() {
        ConfiguracionComplejo configuracion = new ConfiguracionComplejo();
        configuracion.setMinutosReservaPendiente(10);
        when(configuracionService.obtener()).thenReturn(configuracion);

        LocalDateTime antes = LocalDateTime.now();
        Reserva reserva = reservaValida();

        Reserva resultado = service.guardarPendienteWeb(reserva);
        LocalDateTime despues = LocalDateTime.now();

        assertSame(reserva, resultado);
        assertEquals(OrigenReserva.WEB, reserva.getOrigen());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertNull(reserva.getFechaExpiracion());
        assertNotNull(reserva.getFechaVencimiento());
        assertFalse(reserva.getFechaVencimiento().isBefore(antes.plusMinutes(10)));
        assertFalse(reserva.getFechaVencimiento().isAfter(despues.plusMinutes(10)));
        assertEquals(LocalTime.of(11, 30), reserva.getHoraFin());
        assertEquals(new BigDecimal("12000.00"), reserva.getPrecioTotal());
        verify(reservaDAO).guardar(reserva);
    }

    @Test
    void guardarAdministrativaFuerzaOrigenPersonal() {
        Reserva reserva = reservaValida();
        reserva.setOrigen(OrigenReserva.WEB);

        service.guardar(reserva);

        assertEquals(OrigenReserva.PERSONAL, reserva.getOrigen());
        verify(reservaDAO).guardar(reserva);
    }

    @Test
    void rechazaHorarioSuperpuesto() {
        Reserva reserva = reservaValida();
        when(reservaDAO.horarioOcupado(
                2L,
                reserva.getFecha(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                0L)).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva));

        assertEquals("El horario se superpone con otra reserva.", error.getMessage());
        verify(reservaDAO, never()).guardar(any());
    }

    @Test
    void rechazaHorarioBloqueado() {
        Reserva reserva = reservaValida();
        when(bloqueoDAO.horarioBloqueado(
                2L,
                reserva.getFecha(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                0L)).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva));

        assertEquals(
                "La cancha se encuentra bloqueada en ese horario.",
                error.getMessage());
        verify(reservaDAO, never()).guardar(any());
    }

    @Test
    void rechazaHorarioConPartidoProgramado() {
        Reserva reserva = reservaValida();
        when(disponibilidadCanchaService.estaDisponibleParaReserva(
                2L,
                reserva.getFecha(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                0L)).thenReturn(false);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(reserva));

        assertEquals(
                "La cancha tiene un partido programado en ese horario.",
                error.getMessage());
        verify(reservaDAO, never()).guardar(any());
    }

    @Test
    void expirarPendientesDelegaAlDao() {
        when(reservaDAO.expirarPendientesVencidas(any(LocalDateTime.class)))
                .thenReturn(3);

        int cantidad = service.expirarReservasPendientes();

        assertEquals(3, cantidad);
        verify(reservaDAO).expirarPendientesVencidas(any(LocalDateTime.class));
    }

    @Test
    void permitePendienteAConfirmada() {
        Reserva reserva = reservaExistente(EstadoReserva.PENDIENTE);
        when(reservaDAO.buscar(50L)).thenReturn(reserva);

        service.cambiarEstado(50L, EstadoReserva.CONFIRMADA);

        verify(reservaDAO).actualizarEstado(50L, EstadoReserva.CONFIRMADA);
    }

    @Test
    void permiteConfirmadaACompletada() {
        Reserva reserva = reservaExistente(EstadoReserva.CONFIRMADA);
        when(reservaDAO.buscar(50L)).thenReturn(reserva);

        service.cambiarEstado(50L, EstadoReserva.COMPLETADA);

        verify(reservaDAO).actualizarEstado(50L, EstadoReserva.COMPLETADA);
    }

    @Test
    void permiteConfirmadaAAusente() {
        Reserva reserva = reservaExistente(EstadoReserva.CONFIRMADA);
        when(reservaDAO.buscar(50L)).thenReturn(reserva);

        service.cambiarEstado(50L, EstadoReserva.AUSENTE);

        verify(reservaDAO).actualizarEstado(50L, EstadoReserva.AUSENTE);
    }

    @Test
    void rechazaPendienteACompletada() {
        Reserva reserva = reservaExistente(EstadoReserva.PENDIENTE);
        when(reservaDAO.buscar(50L)).thenReturn(reserva);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.cambiarEstado(50L, EstadoReserva.COMPLETADA));

        assertEquals("El cambio de estado no está permitido.", error.getMessage());
        verify(reservaDAO, never()).actualizarEstado(anyLong(), any());
    }

    @Test
    void rechazaCambiarReservaFinalizada() {
        Reserva reserva = reservaExistente(EstadoReserva.EXPIRADA);
        when(reservaDAO.buscar(50L)).thenReturn(reserva);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.cambiarEstado(50L, EstadoReserva.CONFIRMADA));

        assertEquals(
                "No se puede modificar una reserva finalizada.",
                error.getMessage());
        verify(reservaDAO, never()).actualizarEstado(anyLong(), any());
    }

    @Test
    void listarHorariosOmiteOcupadosYBloqueados() {
        LocalDate fecha = LocalDate.now().plusDays(1);

        cancha.setHoraApertura(LocalTime.of(8, 0));
        cancha.setHoraCierre(LocalTime.of(12, 30));

        when(disponibilidadCanchaService.estaDisponibleParaReserva(
                2L,
                fecha,
                LocalTime.of(9, 30),
                LocalTime.of(11, 0),
                0L)).thenReturn(false);

        when(disponibilidadCanchaService.estaDisponibleParaReserva(
                2L,
                fecha,
                LocalTime.of(11, 0),
                LocalTime.of(12, 30),
                0L)).thenReturn(false);

        var horarios = service.listarHorariosDisponibles(2L, fecha, 0L);

        assertEquals(1, horarios.size());
        assertEquals(LocalTime.of(8, 0), horarios.get(0));
    }

    private Reserva reservaValida() {
        Reserva reserva = new Reserva();
        reserva.setClienteId(1L);
        reserva.setCanchaId(2L);
        reserva.setUsuarioId(3L);
        reserva.setFecha(LocalDate.now().plusDays(1));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setCantidadJugadores(4);
        return reserva;
    }

    private Reserva reservaExistente(EstadoReserva estado) {
        Reserva reserva = reservaValida();
        reserva.setId(50L);
        reserva.setEstado(estado);
        return reserva;
    }
}
