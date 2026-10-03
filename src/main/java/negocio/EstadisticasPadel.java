package negocio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EstadisticasPadel {
    private int totalReservas;
    private int reservasCompletadas;
    private int reservasCanceladas;
    private int reservasAusentes;
    private int reservasPendientes;
    private int reservasConfirmadas;
    private int reservasExpiradas;
    private int reservasEfectivas;
    private int cantidadPagosAcreditados;
    private BigDecimal ingresosAcreditados = BigDecimal.ZERO;
    private BigDecimal totalReembolsado = BigDecimal.ZERO;
    private BigDecimal ticketPromedio = BigDecimal.ZERO;
    private BigDecimal ingresosManuales = BigDecimal.ZERO;
    private BigDecimal egresosManuales = BigDecimal.ZERO;
    private BigDecimal porcentajeOcupacion = BigDecimal.ZERO;
    private BigDecimal variacionReservas = BigDecimal.ZERO;
    private BigDecimal variacionIngresos = BigDecimal.ZERO;
    private List<DatoGrafico> reservasPorEstado = new ArrayList<>();
    private List<DatoGrafico> reservasPorCancha = new ArrayList<>();
    private List<DatoGrafico> ingresosPorMes = new ArrayList<>();
    private List<DatoGrafico> ingresosPorMetodo = new ArrayList<>();
    private List<DatoGrafico> horariosMasSolicitados = new ArrayList<>();
    private List<DatoGrafico> diasMasSolicitados = new ArrayList<>();
    private List<DatoGrafico> clientesFrecuentes = new ArrayList<>();
    private List<CierreCaja> cierresCaja = new ArrayList<>();

    public int getTotalReservas(){return totalReservas;} public void setTotalReservas(int v){totalReservas=v;}
    public int getReservasCompletadas(){return reservasCompletadas;} public void setReservasCompletadas(int v){reservasCompletadas=v;}
    public int getReservasCanceladas(){return reservasCanceladas;} public void setReservasCanceladas(int v){reservasCanceladas=v;}
    public int getReservasAusentes(){return reservasAusentes;} public void setReservasAusentes(int v){reservasAusentes=v;}
    public int getReservasPendientes(){return reservasPendientes;} public void setReservasPendientes(int v){reservasPendientes=v;}
    public int getReservasConfirmadas(){return reservasConfirmadas;} public void setReservasConfirmadas(int v){reservasConfirmadas=v;}
    public int getReservasExpiradas(){return reservasExpiradas;} public void setReservasExpiradas(int v){reservasExpiradas=v;}
    public int getReservasEfectivas(){return reservasEfectivas;} public void setReservasEfectivas(int v){reservasEfectivas=v;}
    public int getCantidadPagosAcreditados(){return cantidadPagosAcreditados;} public void setCantidadPagosAcreditados(int v){cantidadPagosAcreditados=v;}
    public BigDecimal getIngresosAcreditados(){return ingresosAcreditados;} public void setIngresosAcreditados(BigDecimal v){ingresosAcreditados=nz(v);}
    public BigDecimal getTotalReembolsado(){return totalReembolsado;} public void setTotalReembolsado(BigDecimal v){totalReembolsado=nz(v);}
    public BigDecimal getTicketPromedio(){return ticketPromedio;} public void setTicketPromedio(BigDecimal v){ticketPromedio=nz(v);}
    public BigDecimal getIngresosManuales(){return ingresosManuales;} public void setIngresosManuales(BigDecimal v){ingresosManuales=nz(v);}
    public BigDecimal getEgresosManuales(){return egresosManuales;} public void setEgresosManuales(BigDecimal v){egresosManuales=nz(v);}
    public BigDecimal getIngresosNetos(){return ingresosAcreditados.subtract(totalReembolsado);}
    public BigDecimal getResultadoOperativo(){return getIngresosNetos().add(ingresosManuales).subtract(egresosManuales);}
    public BigDecimal getPorcentajeOcupacion(){return porcentajeOcupacion;} public void setPorcentajeOcupacion(BigDecimal v){porcentajeOcupacion=nz(v);}
    public BigDecimal getVariacionReservas(){return variacionReservas;} public void setVariacionReservas(BigDecimal v){variacionReservas=nz(v);}
    public BigDecimal getVariacionIngresos(){return variacionIngresos;} public void setVariacionIngresos(BigDecimal v){variacionIngresos=nz(v);}
    public List<DatoGrafico> getReservasPorEstado(){return ro(reservasPorEstado);} public void setReservasPorEstado(List<DatoGrafico> v){reservasPorEstado=cp(v);}
    public List<DatoGrafico> getReservasPorCancha(){return ro(reservasPorCancha);} public void setReservasPorCancha(List<DatoGrafico> v){reservasPorCancha=cp(v);}
    public List<DatoGrafico> getIngresosPorMes(){return ro(ingresosPorMes);} public void setIngresosPorMes(List<DatoGrafico> v){ingresosPorMes=cp(v);}
    public List<DatoGrafico> getIngresosPorMetodo(){return ro(ingresosPorMetodo);} public void setIngresosPorMetodo(List<DatoGrafico> v){ingresosPorMetodo=cp(v);}
    public List<DatoGrafico> getHorariosMasSolicitados(){return ro(horariosMasSolicitados);} public void setHorariosMasSolicitados(List<DatoGrafico> v){horariosMasSolicitados=cp(v);}
    public List<DatoGrafico> getDiasMasSolicitados(){return ro(diasMasSolicitados);} public void setDiasMasSolicitados(List<DatoGrafico> v){diasMasSolicitados=cp(v);}
    public List<DatoGrafico> getClientesFrecuentes(){return ro(clientesFrecuentes);} public void setClientesFrecuentes(List<DatoGrafico> v){clientesFrecuentes=cp(v);}
    public List<CierreCaja> getCierresCaja(){return Collections.unmodifiableList(cierresCaja);} public void setCierresCaja(List<CierreCaja> v){cierresCaja=v==null?new ArrayList<>():new ArrayList<>(v);}
    public BigDecimal getTasaCancelacion(){return porcentaje(reservasCanceladas);} public BigDecimal getTasaAusencia(){return porcentaje(reservasAusentes);}
    private BigDecimal porcentaje(int c){return totalReservas==0?BigDecimal.ZERO:BigDecimal.valueOf(c).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalReservas),2,RoundingMode.HALF_UP);}
    private BigDecimal nz(BigDecimal v){return v==null?BigDecimal.ZERO:v;} private List<DatoGrafico> cp(List<DatoGrafico> v){return v==null?new ArrayList<>():new ArrayList<>(v);} private List<DatoGrafico> ro(List<DatoGrafico> v){return Collections.unmodifiableList(v);}
    public record DatoGrafico(String etiqueta, BigDecimal valor){public DatoGrafico{if(etiqueta==null||etiqueta.isBlank())throw new IllegalArgumentException("La etiqueta es obligatoria."); valor=valor==null?BigDecimal.ZERO:valor;}}
}
