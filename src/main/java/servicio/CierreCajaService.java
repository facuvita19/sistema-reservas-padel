package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import dao.CierreCajaDAO;
import dao.CierreCajaDAOMySQL;
import negocio.CierreCaja;
import negocio.EstadoCierreCaja;
import negocio.ResumenCajaDiaria;

public class CierreCajaService {
    private final CierreCajaDAO cierreDAO;
    public CierreCajaService() { this(new CierreCajaDAOMySQL()); }
    public CierreCajaService(CierreCajaDAO cierreDAO) {
        if (cierreDAO == null) throw new IllegalArgumentException("El DAO de caja no puede ser nulo.");
        this.cierreDAO = cierreDAO;
    }
    public ResumenCajaDiaria obtenerResumen(LocalDate fecha) { validarFecha(fecha); return cierreDAO.calcularResumen(fecha); }
    public CierreCaja buscarPorFecha(LocalDate fecha) { validarFecha(fecha); return cierreDAO.buscarPorFecha(fecha); }
    public List<CierreCaja> listar() { return cierreDAO.listar(); }
    public CierreCaja cerrar(LocalDate fecha, BigDecimal efectivoDeclarado, String observaciones, long usuarioId) {
        validarFecha(fecha);
        if (fecha.isAfter(LocalDate.now())) throw new IllegalArgumentException("No se puede cerrar una fecha futura.");
        if (usuarioId <= 0) throw new IllegalArgumentException("El usuario del cierre es obligatorio.");
        if (efectivoDeclarado == null || efectivoDeclarado.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El efectivo declarado no puede ser negativo.");
        if (cierreDAO.buscarPorFecha(fecha) != null) throw new IllegalArgumentException("La caja de esa fecha ya fue cerrada.");
        ResumenCajaDiaria resumen = cierreDAO.calcularResumen(fecha);
        CierreCaja cierre = new CierreCaja();
        cierre.setFecha(fecha);
        cierre.setEstado(EstadoCierreCaja.CERRADA);
        cierre.setTotalAcreditado(moneda(resumen.getTotalAcreditado()));
        cierre.setTotalEfectivoCalculado(moneda(resumen.getTotalEfectivo()));
        cierre.setEfectivoDeclarado(moneda(efectivoDeclarado));
        cierre.setDiferenciaEfectivo(moneda(efectivoDeclarado.subtract(resumen.getTotalEfectivo())));
        cierre.setTotalReembolsado(moneda(resumen.getTotalReembolsado()));
        cierre.setCantidadPagosAcreditados(resumen.getCantidadPagosAcreditados());
        cierre.setPagosPendientes(resumen.getPagosPendientes());
        cierre.setReservasCompletadas(resumen.getReservasCompletadas());
        cierre.setReservasAusentes(resumen.getReservasAusentes());
        cierre.setReservasCanceladas(resumen.getReservasCanceladas());
        cierre.setObservaciones(limpiarTexto(observaciones));
        cierre.setUsuarioCierreId(usuarioId);
        cierre.setFechaCierre(LocalDateTime.now());
        cierre.setDetalles(resumen.getDetalles());
        cierreDAO.guardar(cierre);
        return cierre;
    }
    private void validarFecha(LocalDate fecha) { if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria."); }
    private BigDecimal moneda(BigDecimal valor) { return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP); }
    private String limpiarTexto(String valor) { return valor == null || valor.isBlank() ? null : valor.trim().replaceAll("\\s+", " "); }
}
