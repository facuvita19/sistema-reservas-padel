package servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dao.CierreCajaDAO;
import negocio.CierreCaja;
import negocio.DetalleMedioPago;
import negocio.MetodoPago;
import negocio.ResumenCajaDiaria;

class CierreCajaServiceTest {

    private CierreCajaDAODoble dao;
    private CierreCajaService service;

    @BeforeEach
    void preparar() {
        dao = new CierreCajaDAODoble();
        service = new CierreCajaService(dao);
    }

    @Test
    void cierraCajaCalculandoDiferenciaDeEfectivo() {
        dao.resumen = crearResumen();

        CierreCaja cierre = service.cerrar(
                LocalDate.now(),
                new BigDecimal("48000"),
                "  Faltante   revisado  ",
                7L);

        assertSame(cierre, dao.guardado);
        assertEquals(new BigDecimal("170000.00"), cierre.getTotalAcreditado());
        assertEquals(new BigDecimal("50000.00"), cierre.getTotalEfectivoCalculado());
        assertEquals(new BigDecimal("48000.00"), cierre.getEfectivoDeclarado());
        assertEquals(new BigDecimal("-2000.00"), cierre.getDiferenciaEfectivo());
        assertEquals("Faltante revisado", cierre.getObservaciones());
        assertEquals(7L, cierre.getUsuarioCierreId());
        assertEquals(2, cierre.getDetalles().size());
    }

    @Test
    void permiteCierreSinObservaciones() {
        dao.resumen = crearResumen();
        CierreCaja cierre = service.cerrar(
                LocalDate.now(), new BigDecimal("50000"), "  ", 7L);
        assertNull(cierre.getObservaciones());
        assertEquals(new BigDecimal("0.00"), cierre.getDiferenciaEfectivo());
    }

    @Test
    void rechazaCierreDuplicado() {
        dao.existente = new CierreCaja();
        assertThrows(IllegalArgumentException.class,
                () -> service.cerrar(
                        LocalDate.now(), BigDecimal.ZERO, null, 7L));
    }

    @Test
    void rechazaFechaFuturaEImporteNegativoYUsuarioInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.cerrar(
                        LocalDate.now().plusDays(1), BigDecimal.ZERO, null, 7L));
        assertThrows(IllegalArgumentException.class,
                () -> service.cerrar(
                        LocalDate.now(), new BigDecimal("-1"), null, 7L));
        assertThrows(IllegalArgumentException.class,
                () -> service.cerrar(
                        LocalDate.now(), BigDecimal.ZERO, null, 0L));
    }

    @Test
    void buscaListaYObtieneResumen() {
        dao.resumen = crearResumen();
        dao.existente = new CierreCaja();
        dao.cierres.add(dao.existente);

        assertSame(dao.resumen, service.obtenerResumen(LocalDate.now()));
        assertSame(dao.existente, service.buscarPorFecha(LocalDate.now()));
        assertEquals(1, service.listar().size());
    }

    private ResumenCajaDiaria crearResumen() {
        ResumenCajaDiaria resumen = new ResumenCajaDiaria();
        resumen.setFecha(LocalDate.now());
        resumen.setTotalAcreditado(new BigDecimal("170000"));
        resumen.setTotalEfectivo(new BigDecimal("50000"));
        resumen.setTotalReembolsado(new BigDecimal("10000"));
        resumen.setCantidadPagosAcreditados(3);
        resumen.setPagosPendientes(1);
        resumen.setReservasCompletadas(4);
        resumen.setReservasAusentes(1);
        resumen.setReservasCanceladas(2);

        DetalleMedioPago efectivo = new DetalleMedioPago();
        efectivo.setMetodoPago(MetodoPago.EFECTIVO);
        efectivo.setCantidadMovimientos(1);
        efectivo.setTotal(new BigDecimal("50000"));

        DetalleMedioPago transferencia = new DetalleMedioPago();
        transferencia.setMetodoPago(MetodoPago.TRANSFERENCIA);
        transferencia.setCantidadMovimientos(2);
        transferencia.setTotal(new BigDecimal("120000"));

        resumen.setDetalles(List.of(efectivo, transferencia));
        return resumen;
    }

    private static final class CierreCajaDAODoble implements CierreCajaDAO {
        private ResumenCajaDiaria resumen;
        private CierreCaja existente;
        private CierreCaja guardado;
        private final List<CierreCaja> cierres = new ArrayList<>();

        @Override
        public ResumenCajaDiaria calcularResumen(LocalDate fecha) {
            return resumen;
        }

        @Override
        public CierreCaja buscarPorFecha(LocalDate fecha) {
            return existente;
        }

        @Override
        public List<CierreCaja> listar() {
            return new ArrayList<>(cierres);
        }

        @Override
        public void guardar(CierreCaja cierre) {
            guardado = cierre;
        }
    }
}
