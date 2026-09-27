package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dao.CierreCajaDAO;
import dao.CierreCajaDAOMySQL;
import dao.MovimientoCajaDAO;
import dao.MovimientoCajaDAOMySQL;
import negocio.CierreCaja;
import negocio.EstadoCierreCaja;
import negocio.MetodoPago;
import negocio.ResumenCajaDiaria;
import negocio.TipoMovimientoCaja;

public class CierreCajaService {
    private final CierreCajaDAO cierreDAO;
    private final MovimientoCajaDAO movimientoDAO;
    public CierreCajaService() {
        this(new CierreCajaDAOMySQL(), new MovimientoCajaDAOMySQL());
    }

    public CierreCajaService(CierreCajaDAO cierreDAO) {
        if (cierreDAO == null) {
            throw new IllegalArgumentException("El DAO de caja no puede ser nulo.");
        }
        this.cierreDAO = cierreDAO;
        this.movimientoDAO = null;
    }

    public CierreCajaService(
            CierreCajaDAO cierreDAO,
            MovimientoCajaDAO movimientoDAO) {
        if (cierreDAO == null || movimientoDAO == null) {
            throw new IllegalArgumentException(
                    "Los DAO de caja no pueden ser nulos.");
        }
        this.cierreDAO = cierreDAO;
        this.movimientoDAO = movimientoDAO;
    }
    public ResumenCajaDiaria obtenerResumen(LocalDate fecha) {
        validarFecha(fecha);
        ResumenCajaDiaria resumen = cierreDAO.calcularResumen(fecha);
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal egresos = BigDecimal.ZERO;
        BigDecimal efectivo = BigDecimal.ZERO;

        if (movimientoDAO == null) {
            return resumen;
        }

        for (var movimiento : movimientoDAO.listarPorFecha(fecha)) {
            BigDecimal firmado = movimiento.getTipo() == TipoMovimientoCaja.INGRESO
                    ? movimiento.getImporte() : movimiento.getImporte().negate();
            if (movimiento.getTipo() == TipoMovimientoCaja.INGRESO) ingresos = ingresos.add(movimiento.getImporte());
            else egresos = egresos.add(movimiento.getImporte());
            if (movimiento.getMedioPago() == MetodoPago.EFECTIVO) efectivo = efectivo.add(firmado);
        }
        resumen.setIngresosManuales(ingresos);
        resumen.setEgresosManuales(egresos);
        resumen.setMovimientosEfectivo(efectivo);
        return resumen;
    }
    public CierreCaja buscarPorFecha(LocalDate fecha) { validarFecha(fecha); return cierreDAO.buscarPorFecha(fecha); }
    public List<CierreCaja> listar() { return cierreDAO.listar(); }
    public CierreCaja cerrar(LocalDate fecha, BigDecimal efectivoDeclarado, String observaciones, long usuarioId) {
        validarFecha(fecha);
        if (fecha.isAfter(LocalDate.now())) throw new IllegalArgumentException("No se puede cerrar una fecha futura.");
        if (usuarioId <= 0) throw new IllegalArgumentException("El usuario del cierre es obligatorio.");
        if (efectivoDeclarado == null || efectivoDeclarado.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El efectivo declarado no puede ser negativo.");
        if (cierreDAO.buscarPorFecha(fecha) != null) throw new IllegalArgumentException("La caja de esa fecha ya fue cerrada.");
        ResumenCajaDiaria resumen = obtenerResumen(fecha);
        CierreCaja cierre = new CierreCaja();
        cierre.setFecha(fecha); cierre.setEstado(EstadoCierreCaja.CERRADA);
        cierre.setTotalAcreditado(moneda(resumen.getTotalAcreditado().add(resumen.getIngresosManuales()).subtract(resumen.getEgresosManuales())));
        cierre.setTotalEfectivoCalculado(moneda(resumen.getEfectivoEsperado()));
        cierre.setEfectivoDeclarado(moneda(efectivoDeclarado));
        cierre.setDiferenciaEfectivo(moneda(efectivoDeclarado.subtract(resumen.getEfectivoEsperado())));
        cierre.setTotalReembolsado(moneda(resumen.getTotalReembolsado()));
        cierre.setCantidadPagosAcreditados(resumen.getCantidadPagosAcreditados());
        cierre.setPagosPendientes(resumen.getPagosPendientes());
        cierre.setReservasCompletadas(resumen.getReservasCompletadas());
        cierre.setReservasAusentes(resumen.getReservasAusentes());
        cierre.setReservasCanceladas(resumen.getReservasCanceladas());
        cierre.setObservaciones(limpiarTexto(observaciones));
        cierre.setUsuarioCierreId(usuarioId); cierre.setFechaCierre(LocalDateTime.now());
        cierre.setDetalles(resumen.getDetalles()); cierreDAO.guardar(cierre); return cierre;
    }
    private void validarFecha(LocalDate fecha) { if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria."); }
    private BigDecimal moneda(BigDecimal valor) { return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP); }
    private String limpiarTexto(String valor) { return valor == null || valor.isBlank() ? null : valor.trim().replaceAll("\\s+", " "); }
}
