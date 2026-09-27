package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.ConfiguracionComplejoDAO;
import dao.PagoDAO;
import dao.ReservaDAO;
import negocio.ConfiguracionComplejo;
import negocio.EstadoPago;
import negocio.EstadoReserva;
import negocio.MetodoPago;
import negocio.Pago;
import negocio.Reserva;

class PagoConfirmacionSeniaTest {

    private PagoDAODoble pagoDAO;
    private ReservaDAODoble reservaDAO;
    private PagoService service;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        pagoDAO = new PagoDAODoble();
        reservaDAO = new ReservaDAODoble();

        reserva = crearReservaPendiente();
        reservaDAO.reserva = reserva;

        ConfiguracionComplejo configuracion =
                new ConfiguracionComplejo();
        configuracion.setPorcentajeSenia(
                new BigDecimal("25.00")
        );

        ConfiguracionDAODoble configuracionDAO =
                new ConfiguracionDAODoble();
        configuracionDAO.configuracion = configuracion;

        service = new PagoService(
                pagoDAO,
                reservaDAO,
                new ConfiguracionComplejoService(configuracionDAO)
        );
    }

    @Test
    void confirmaAlGuardarPagoAcreditadoQueAlcanzaSenia() {
        Pago pago = crearPago(
                new BigDecimal("10000.00"),
                EstadoPago.ACREDITADO
        );

        service.guardar(pago);

        assertEquals(
                EstadoReserva.CONFIRMADA,
                reservaDAO.estadoActualizado
        );
        assertEquals(
                EstadoReserva.CONFIRMADA,
                reserva.getEstado()
        );
        assertNull(reserva.getFechaVencimiento());
    }

    @Test
    void noConfirmaSiPagoAcreditadoEsInferiorALaSenia() {
        Pago pago = crearPago(
                new BigDecimal("9999.99"),
                EstadoPago.ACREDITADO
        );

        service.guardar(pago);

        assertNull(reservaDAO.estadoActualizado);
        assertEquals(
                EstadoReserva.PENDIENTE,
                reserva.getEstado()
        );
    }

    @Test
    void acreditaDosPagosYConfirmaAlAlcanzarElTotal() {
        pagoDAO.totalAcreditado = new BigDecimal("5000.00");

        Pago pago = crearPago(
                new BigDecimal("5000.00"),
                EstadoPago.PENDIENTE
        );
        pago.setId(20L);
        pagoDAO.pagoBuscado = pago;

        service.acreditar(20L);

        assertEquals(
                EstadoPago.ACREDITADO,
                pagoDAO.estadoActualizado
        );
        assertEquals(
                EstadoReserva.CONFIRMADA,
                reservaDAO.estadoActualizado
        );
    }

    @Test
    void pagoPendienteNoConfirmaReserva() {
        Pago pago = crearPago(
                new BigDecimal("10000.00"),
                EstadoPago.PENDIENTE
        );

        service.guardar(pago);

        assertNull(reservaDAO.estadoActualizado);
        assertEquals(
                EstadoReserva.PENDIENTE,
                reserva.getEstado()
        );
    }

    @Test
    void noModificaReservaQueYaEstaConfirmada() {
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        Pago pago = crearPago(
                new BigDecimal("10000.00"),
                EstadoPago.ACREDITADO
        );

        service.guardar(pago);

        assertNull(reservaDAO.estadoActualizado);
    }

    @Test
    void rechazaPagoSobreReservaExpirada() {
        reserva.setEstado(EstadoReserva.EXPIRADA);

        Pago pago = crearPago(
                new BigDecimal("10000.00"),
                EstadoPago.ACREDITADO
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.guardar(pago)
        );
    }

    private Reserva crearReservaPendiente() {
        Reserva resultado = new Reserva();
        resultado.setId(10L);
        resultado.setClienteId(1L);
        resultado.setCanchaId(1L);
        resultado.setUsuarioId(1L);
        resultado.setFecha(LocalDate.now().plusDays(1));
        resultado.setHoraInicio(LocalTime.of(18, 0));
        resultado.setHoraFin(LocalTime.of(19, 30));
        resultado.setPrecioTotal(new BigDecimal("40000.00"));
        resultado.setEstado(EstadoReserva.PENDIENTE);
        resultado.setFechaVencimiento(
                LocalDateTime.now().plusMinutes(15)
        );
        return resultado;
    }

    private Pago crearPago(
            BigDecimal importe,
            EstadoPago estado) {

        Pago pago = new Pago();
        pago.setReservaId(10L);
        pago.setImporte(importe);
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setEstado(estado);
        return pago;
    }

    private static final class PagoDAODoble
            implements PagoDAO {

        private Pago pagoBuscado;
        private EstadoPago estadoActualizado;
        private BigDecimal totalAcreditado = BigDecimal.ZERO;
        private final List<Pago> pagos = new ArrayList<>();

        @Override
        public void guardar(Pago pago) {
            pagos.add(pago);

            if (pago.getEstado() == EstadoPago.ACREDITADO) {
                totalAcreditado = totalAcreditado.add(
                        pago.getImporte()
                );
            }
        }

        @Override
        public Pago buscar(long id) {
            return pagoBuscado;
        }

        @Override
        public List<Pago> listar() {
            return new ArrayList<>(pagos);
        }

        @Override
        public List<Pago> listarPorReserva(long reservaId) {
            return new ArrayList<>(pagos);
        }

        @Override
        public void actualizarEstado(
                long id,
                EstadoPago estado) {

            estadoActualizado = estado;

            if (estado == EstadoPago.ACREDITADO
                    && pagoBuscado != null) {

                pagoBuscado.setEstado(estado);
                totalAcreditado = totalAcreditado.add(
                        pagoBuscado.getImporte()
                );
            }
        }

        @Override
        public BigDecimal totalAcreditado(long reservaId) {
            return totalAcreditado;
        }
    }

    private static final class ReservaDAODoble
            implements ReservaDAO {

        private Reserva reserva;
        private EstadoReserva estadoActualizado;

        @Override
        public void guardar(Reserva reserva) {
            this.reserva = reserva;
        }

        @Override
        public Reserva buscar(long id) {
            return reserva;
        }

        @Override
        public List<Reserva> listar() {
            return new ArrayList<>();
        }

        @Override
        public List<Reserva> listarPorCliente(long clienteId) {
            return new ArrayList<>();
        }

        @Override
        public List<Reserva> listarPorFecha(LocalDate fecha) {
            return new ArrayList<>();
        }

        @Override
        public void actualizarEstado(
                long id,
                EstadoReserva estado) {

            estadoActualizado = estado;
            reserva.setEstado(estado);

            if (estado == EstadoReserva.CONFIRMADA) {
                reserva.setFechaVencimiento(null);
                reserva.setFechaExpiracion(null);
            }
        }

        @Override
        public boolean horarioOcupado(
                long canchaId,
                LocalDate fecha,
                LocalTime inicio,
                LocalTime fin,
                long reservaExcluidaId) {
            return false;
        }
    }

    private static final class ConfiguracionDAODoble
            implements ConfiguracionComplejoDAO {

        private ConfiguracionComplejo configuracion;

        @Override
        public ConfiguracionComplejo obtener() {
            return configuracion;
        }

        @Override
        public void guardar(ConfiguracionComplejo valor) {
            configuracion = valor;
        }
    }
}
