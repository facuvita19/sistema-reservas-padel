package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.CanchaDAO;
import negocio.Cancha;
import negocio.TipoCancha;

class CanchaServiceTest {

    private CanchaDAODoble dao;
    private CanchaService service;

    @BeforeEach
    void preparar() {
        dao = new CanchaDAODoble();
        service = new CanchaService(dao);
    }

    @Test
    void guardaYNormalizaCanchaValida() {
        Cancha cancha = crearCancha();
        cancha.setNombre("  Cancha   Central  ");
        cancha.setPrecio(new BigDecimal("25000.555"));

        service.guardar(cancha);

        assertTrue(dao.guardarInvocado);
        assertSame(cancha, dao.ultimoGuardado);
        assertEquals("Cancha Central", cancha.getNombre());
        assertEquals(new BigDecimal("25000.56"), cancha.getPrecio());
    }

    @Test
    void rechazaNombreDuplicado() {
        dao.nombreDuplicado = true;

        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(crearCancha())
        );
        assertFalse(dao.guardarInvocado);
    }

    @Test
    void rechazaTipoNulo() {
        Cancha cancha = crearCancha();
        cancha.setTipo(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(cancha));
    }

    @Test
    void rechazaHorarioInvertido() {
        Cancha cancha = crearCancha();
        cancha.setHoraApertura(LocalTime.of(22, 0));
        cancha.setHoraCierre(LocalTime.of(8, 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(cancha));
    }

    @Test
    void rechazaDuracionFueraDeRango() {
        Cancha cancha = crearCancha();
        cancha.setDuracionReserva(300);
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(cancha));
    }

    @Test
    void rechazaDuracionNoMultiploDeTreinta() {
        Cancha cancha = crearCancha();
        cancha.setDuracionReserva(75);
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(cancha));
    }

    @Test
    void rechazaPrecioNegativo() {
        Cancha cancha = crearCancha();
        cancha.setPrecio(new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(cancha));
    }

    @Test
    void rechazaCanchaSinDiasDisponibles() {
        Cancha cancha = crearCancha();
        cancha.setDiasDisponibles(EnumSet.noneOf(DayOfWeek.class));
        assertThrows(IllegalArgumentException.class,
                () -> service.guardar(cancha));
    }

    @Test
    void calculaHoraFinYDisponibilidad() {
        Cancha cancha = crearCancha();
        dao.buscada = cancha;

        assertEquals(
                LocalTime.of(19, 30),
                service.calcularHoraFin(10L, LocalTime.of(18, 0))
        );
        assertTrue(service.estaDisponibleElDia(
                10L, DayOfWeek.MONDAY));
        assertFalse(service.estaDisponibleElDia(
                10L, DayOfWeek.SUNDAY));
    }

    @Test
    void buscaListaYElimina() {
        Cancha cancha = crearCancha();
        dao.buscada = cancha;
        dao.canchas.add(cancha);

        assertSame(cancha, service.buscar(10L));
        assertEquals(1, service.listar().size());
        service.eliminar(10L);
        assertEquals(10L, dao.idEliminado);
        assertNull(service.buscar(0L));
    }

    private Cancha crearCancha() {
        Cancha cancha = new Cancha();
        cancha.setId(10L);
        cancha.setNombre("Cancha 1");
        cancha.setTipo(TipoCancha.CUBIERTA);
        cancha.setSuperficie("Cesped sintetico");
        cancha.setHoraApertura(LocalTime.of(8, 0));
        cancha.setHoraCierre(LocalTime.of(23, 30));
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
        return cancha;
    }

    private static final class CanchaDAODoble implements CanchaDAO {
        private boolean guardarInvocado;
        private boolean nombreDuplicado;
        private long idEliminado;
        private Cancha ultimoGuardado;
        private Cancha buscada;
        private final List<Cancha> canchas = new ArrayList<>();

        @Override
        public void guardar(Cancha cancha) {
            guardarInvocado = true;
            ultimoGuardado = cancha;
        }

        @Override
        public void eliminar(long id) {
            idEliminado = id;
        }

        @Override
        public Cancha buscar(long id) {
            return buscada;
        }

        @Override
        public List<Cancha> listar() {
            return new ArrayList<>(canchas);
        }

        @Override
        public boolean existeNombre(String nombre, long id) {
            return nombreDuplicado;
        }
    }
}
