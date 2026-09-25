package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.BloqueoCanchaDAO;
import dao.CanchaDAO;
import negocio.BloqueoCancha;
import negocio.Cancha;
import negocio.TipoCancha;

class BloqueoCanchaServiceTest {

    private BloqueoDAODoble bloqueoDAO;
    private CanchaDAODoble canchaDAO;
    private BloqueoCanchaService service;

    @BeforeEach
    void preparar() {
        bloqueoDAO = new BloqueoDAODoble();
        canchaDAO = new CanchaDAODoble();
        canchaDAO.cancha = crearCancha();
        service = new BloqueoCanchaService(
                bloqueoDAO,
                new CanchaService(canchaDAO)
        );
    }

    @Test
    void guardaYNormalizaBloqueoValido() {
        BloqueoCancha bloqueo = crearBloqueo();
        bloqueo.setMotivo("  Mantenimiento   de luces  ");

        service.guardar(bloqueo);

        assertTrue(bloqueoDAO.guardarInvocado);
        assertSame(bloqueo, bloqueoDAO.ultimoGuardado);
        assertEquals("Mantenimiento de luces", bloqueo.getMotivo());
    }

    @Test
    void rechazaBloqueoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(null));
        assertFalse(bloqueoDAO.guardarInvocado);
    }

    @Test
    void rechazaFechaPasada() {
        BloqueoCancha bloqueo = crearBloqueo();
        bloqueo.setFecha(LocalDate.now().minusDays(1));
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(bloqueo));
    }

    @Test
    void rechazaHorarioInvertido() {
        BloqueoCancha bloqueo = crearBloqueo();
        bloqueo.setHoraInicio(LocalTime.of(20, 0));
        bloqueo.setHoraFin(LocalTime.of(19, 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(bloqueo));
    }

    @Test
    void rechazaMotivoVacio() {
        BloqueoCancha bloqueo = crearBloqueo();
        bloqueo.setMotivo("   ");
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(bloqueo));
    }

    @Test
    void rechazaDiaNoDisponible() {
        BloqueoCancha bloqueo = crearBloqueo();
        bloqueo.setFecha(proximo(DayOfWeek.SUNDAY));

        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(bloqueo));
    }

    @Test
    void rechazaBloqueoFueraDeJornada() {
        BloqueoCancha bloqueo = crearBloqueo();
        bloqueo.setHoraInicio(LocalTime.of(7, 0));
        bloqueo.setHoraFin(LocalTime.of(8, 0));

        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(bloqueo));
    }

    @Test
    void rechazaSuperposicionConOtroBloqueo() {
        bloqueoDAO.bloqueado = true;

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(crearBloqueo())
        );

        assertTrue(error.getMessage().contains("superpuesto"));
    }

    @Test
    void buscaListaPorCanchaYElimina() {
        BloqueoCancha bloqueo = crearBloqueo();
        bloqueoDAO.buscado = bloqueo;
        bloqueoDAO.bloqueos.add(bloqueo);

        assertSame(bloqueo, service.buscar(1L));
        assertEquals(1, service.listar().size());
        assertEquals(1, service.listarPorCancha(10L).size());
        service.eliminar(1L);
        assertEquals(1L, bloqueoDAO.idEliminado);
        assertNull(service.buscar(0L));
    }

    private BloqueoCancha crearBloqueo() {
        BloqueoCancha bloqueo = new BloqueoCancha();
        bloqueo.setId(1L);
        bloqueo.setCanchaId(10L);
        bloqueo.setFecha(proximo(DayOfWeek.MONDAY));
        bloqueo.setHoraInicio(LocalTime.of(18, 0));
        bloqueo.setHoraFin(LocalTime.of(19, 30));
        bloqueo.setMotivo("Mantenimiento");
        return bloqueo;
    }

    private Cancha crearCancha() {
        Cancha cancha = new Cancha();
        cancha.setId(10L);
        cancha.setNombre("Cancha Central");
        cancha.setTipo(TipoCancha.CUBIERTA);
        cancha.setHoraApertura(LocalTime.of(8, 0));
        cancha.setHoraCierre(LocalTime.of(23, 0));
        cancha.setDuracionReserva(90);
        cancha.setPrecio(new BigDecimal("25000"));
        cancha.setDiasDisponibles(EnumSet.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        ));
        cancha.setActivo(true);
        return cancha;
    }

    private LocalDate proximo(DayOfWeek dia) {
        return LocalDate.now().with(TemporalAdjusters.next(dia));
    }

    private static final class BloqueoDAODoble
            implements BloqueoCanchaDAO {
        private boolean guardarInvocado;
        private boolean bloqueado;
        private long idEliminado;
        private BloqueoCancha ultimoGuardado;
        private BloqueoCancha buscado;
        private final List<BloqueoCancha> bloqueos = new ArrayList<>();

        @Override public void guardar(BloqueoCancha bloqueo) {
            guardarInvocado = true;
            ultimoGuardado = bloqueo;
        }
        @Override public void eliminar(long id) { idEliminado = id; }
        @Override public BloqueoCancha buscar(long id) { return buscado; }
        @Override public List<BloqueoCancha> listar() {
            return new ArrayList<>(bloqueos);
        }
        @Override public List<BloqueoCancha> listarPorCancha(long id) {
            return new ArrayList<>(bloqueos);
        }
        @Override public boolean horarioBloqueado(
                long canchaId, LocalDate fecha, LocalTime inicio,
                LocalTime fin, long bloqueoExcluidoId) {
            return bloqueado;
        }
    }

    private static final class CanchaDAODoble implements CanchaDAO {
        private Cancha cancha;
        @Override public void guardar(Cancha cancha) { this.cancha = cancha; }
        @Override public void eliminar(long id) { }
        @Override public Cancha buscar(long id) { return cancha; }
        @Override public List<Cancha> listar() {
            return new ArrayList<>(List.of(cancha));
        }
        @Override public boolean existeNombre(String nombre, long id) {
            return false;
        }
    }
}
