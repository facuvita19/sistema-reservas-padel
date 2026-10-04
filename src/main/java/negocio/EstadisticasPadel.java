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
    private int reservasPersonal;
    private int reservasWeb;
    private int cancelacionesCliente;
    private int cancelacionesAdministrativas;
    private int cancelacionesSinClasificar;
    private int cantidadPagosAcreditados;
    private BigDecimal ingresosAcreditados = BigDecimal.ZERO;
    private BigDecimal totalReembolsado = BigDecimal.ZERO;
    private BigDecimal ticketPromedio = BigDecimal.ZERO;
    private BigDecimal ingresosManuales = BigDecimal.ZERO;
    private BigDecimal egresosManuales = BigDecimal.ZERO;
    private int pagosPendientes;
    private BigDecimal diferenciaEfectivoAcumulada = BigDecimal.ZERO;
    private BigDecimal porcentajeOcupacion = BigDecimal.ZERO;
    private int totalClientes;
    private int clientesActivos;
    private int clientesInactivos;
    private int clientesNuevos;
    private int clientesConReservas;
    private int clientesRecurrentes;
    private int clientesUnaReserva;
    private int clientesConCuentaWeb;
    private int cuentasWebNuevas;
    private int participantesTorneos;
    private BigDecimal promedioReservasPorCliente = BigDecimal.ZERO;
    private int totalCanchas, canchasActivas, canchasInactivas, bloqueosCancha, partidosTorneo;
    private BigDecimal minutosCapacidad=BigDecimal.ZERO,minutosReservados=BigDecimal.ZERO,minutosBloqueados=BigDecimal.ZERO,minutosTorneos=BigDecimal.ZERO;
    private int webSolicitudesCreadas,webPendientes,webConfirmadasActuales,webExpiradas,webCanceladas,webCompletadas,webAusentes,webConPago;
    private BigDecimal webDineroRecibido=BigDecimal.ZERO,webDineroReembolsado=BigDecimal.ZERO,webPromedioMinutosPrimerPago=BigDecimal.ZERO;
    private BigDecimal variacionReservas = BigDecimal.ZERO;
    private BigDecimal variacionIngresos = BigDecimal.ZERO;
    private List<DatoGrafico> reservasPorEstado = new ArrayList<>();
    private List<DatoGrafico> reservasPorOrigen = new ArrayList<>();
    private List<DatoGrafico> reservasPorDia = new ArrayList<>();
    private List<DatoGrafico> cancelacionesPorTipo = new ArrayList<>();
    private List<DatoGrafico> evolucionIncidencias = new ArrayList<>();
    private List<DatoGrafico> reservasPorCancha = new ArrayList<>();
    private List<DatoGrafico> ingresosPorMes = new ArrayList<>();
    private List<DatoGrafico> ingresosPorMetodo = new ArrayList<>();
    private List<DatoGrafico> pagosRecibidosPorDia = new ArrayList<>();
    private List<DatoGrafico> reembolsosPorDia = new ArrayList<>();
    private List<DatoGrafico> movimientosCajaPorTipo = new ArrayList<>();
    private List<DatoGrafico> movimientosCajaPorMedio = new ArrayList<>();
    private List<DatoGrafico> diferenciasCajaPorDia = new ArrayList<>();
    private List<DatoGrafico> horariosMasSolicitados = new ArrayList<>();
    private List<DatoGrafico> diasMasSolicitados = new ArrayList<>();
    private List<DatoGrafico> clientesFrecuentes = new ArrayList<>();
    private List<DatoGrafico> clientesPorFacturacion = new ArrayList<>();
    private List<DatoGrafico> clientesNuevosPorMes = new ArrayList<>();
    private List<DatoGrafico> clientesPorPosicion = new ArrayList<>();
    private List<DatoGrafico> actividadClientes = new ArrayList<>();
    private List<DatoGrafico> ocupacionPorCancha=new ArrayList<>(), ingresosPorCancha=new ArrayList<>(), usoMinutosPorCancha=new ArrayList<>(), bloqueosPorCancha=new ArrayList<>(), torneosPorCancha=new ArrayList<>();
    private List<DatoGrafico> webSolicitudesPorDia=new ArrayList<>(),webEmbudo=new ArrayList<>(),webHorarios=new ArrayList<>(),webDias=new ArrayList<>(),webMetodosPago=new ArrayList<>(),webEstados=new ArrayList<>();
    private List<CierreCaja> cierresCaja = new ArrayList<>();

    public int getTotalReservas(){return totalReservas;} public void setTotalReservas(int v){totalReservas=v;}
    public int getReservasCompletadas(){return reservasCompletadas;} public void setReservasCompletadas(int v){reservasCompletadas=v;}
    public int getReservasCanceladas(){return reservasCanceladas;} public void setReservasCanceladas(int v){reservasCanceladas=v;}
    public int getReservasAusentes(){return reservasAusentes;} public void setReservasAusentes(int v){reservasAusentes=v;}
    public int getReservasPendientes(){return reservasPendientes;} public void setReservasPendientes(int v){reservasPendientes=v;}
    public int getReservasConfirmadas(){return reservasConfirmadas;} public void setReservasConfirmadas(int v){reservasConfirmadas=v;}
    public int getReservasExpiradas(){return reservasExpiradas;} public void setReservasExpiradas(int v){reservasExpiradas=v;}
    public int getReservasEfectivas(){return reservasEfectivas;} public void setReservasEfectivas(int v){reservasEfectivas=v;}
    public int getReservasPersonal(){return reservasPersonal;} public void setReservasPersonal(int v){reservasPersonal=v;}
    public int getReservasWeb(){return reservasWeb;} public void setReservasWeb(int v){reservasWeb=v;}
    public int getCancelacionesCliente(){return cancelacionesCliente;} public void setCancelacionesCliente(int v){cancelacionesCliente=v;}
    public int getCancelacionesAdministrativas(){return cancelacionesAdministrativas;} public void setCancelacionesAdministrativas(int v){cancelacionesAdministrativas=v;}
    public int getCancelacionesSinClasificar(){return cancelacionesSinClasificar;} public void setCancelacionesSinClasificar(int v){cancelacionesSinClasificar=v;}
    public int getCantidadPagosAcreditados(){return cantidadPagosAcreditados;} public void setCantidadPagosAcreditados(int v){cantidadPagosAcreditados=v;}
    public BigDecimal getIngresosAcreditados(){return ingresosAcreditados;} public void setIngresosAcreditados(BigDecimal v){ingresosAcreditados=nz(v);}
    public BigDecimal getTotalReembolsado(){return totalReembolsado;} public void setTotalReembolsado(BigDecimal v){totalReembolsado=nz(v);}
    public BigDecimal getTicketPromedio(){return ticketPromedio;} public void setTicketPromedio(BigDecimal v){ticketPromedio=nz(v);}
    public BigDecimal getIngresosManuales(){return ingresosManuales;} public void setIngresosManuales(BigDecimal v){ingresosManuales=nz(v);}
    public BigDecimal getEgresosManuales(){return egresosManuales;} public void setEgresosManuales(BigDecimal v){egresosManuales=nz(v);}
    public int getPagosPendientes(){return pagosPendientes;} public void setPagosPendientes(int v){pagosPendientes=v;}
    public BigDecimal getDiferenciaEfectivoAcumulada(){return diferenciaEfectivoAcumulada;} public void setDiferenciaEfectivoAcumulada(BigDecimal v){diferenciaEfectivoAcumulada=nz(v);}
    public BigDecimal getIngresosNetos(){return ingresosAcreditados.subtract(totalReembolsado);}
    public BigDecimal getResultadoOperativo(){return getIngresosNetos().add(ingresosManuales).subtract(egresosManuales);}
    public BigDecimal getPorcentajeOcupacion(){return porcentajeOcupacion;} public void setPorcentajeOcupacion(BigDecimal v){porcentajeOcupacion=nz(v);}
    public int getTotalClientes(){return totalClientes;} public void setTotalClientes(int v){totalClientes=v;}
    public int getClientesActivos(){return clientesActivos;} public void setClientesActivos(int v){clientesActivos=v;}
    public int getClientesInactivos(){return clientesInactivos;} public void setClientesInactivos(int v){clientesInactivos=v;}
    public int getClientesNuevos(){return clientesNuevos;} public void setClientesNuevos(int v){clientesNuevos=v;}
    public int getClientesConReservas(){return clientesConReservas;} public void setClientesConReservas(int v){clientesConReservas=v;}
    public int getClientesRecurrentes(){return clientesRecurrentes;} public void setClientesRecurrentes(int v){clientesRecurrentes=v;}
    public int getClientesUnaReserva(){return clientesUnaReserva;} public void setClientesUnaReserva(int v){clientesUnaReserva=v;}
    public int getClientesConCuentaWeb(){return clientesConCuentaWeb;} public void setClientesConCuentaWeb(int v){clientesConCuentaWeb=v;}
    public int getCuentasWebNuevas(){return cuentasWebNuevas;} public void setCuentasWebNuevas(int v){cuentasWebNuevas=v;}
    public int getParticipantesTorneos(){return participantesTorneos;} public void setParticipantesTorneos(int v){participantesTorneos=v;}
    public BigDecimal getPromedioReservasPorCliente(){return promedioReservasPorCliente;} public void setPromedioReservasPorCliente(BigDecimal v){promedioReservasPorCliente=nz(v);}
    public BigDecimal getPorcentajeClientesRecurrentes(){return clientesConReservas==0?BigDecimal.ZERO:BigDecimal.valueOf(clientesRecurrentes).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(clientesConReservas),2,RoundingMode.HALF_UP);}
    public int getTotalCanchas(){return totalCanchas;} public void setTotalCanchas(int v){totalCanchas=v;} public int getCanchasActivas(){return canchasActivas;} public void setCanchasActivas(int v){canchasActivas=v;} public int getCanchasInactivas(){return canchasInactivas;} public void setCanchasInactivas(int v){canchasInactivas=v;} public int getBloqueosCancha(){return bloqueosCancha;} public void setBloqueosCancha(int v){bloqueosCancha=v;} public int getPartidosTorneo(){return partidosTorneo;} public void setPartidosTorneo(int v){partidosTorneo=v;} public BigDecimal getMinutosCapacidad(){return minutosCapacidad;} public void setMinutosCapacidad(BigDecimal v){minutosCapacidad=nz(v);} public BigDecimal getMinutosReservados(){return minutosReservados;} public void setMinutosReservados(BigDecimal v){minutosReservados=nz(v);} public BigDecimal getMinutosBloqueados(){return minutosBloqueados;} public void setMinutosBloqueados(BigDecimal v){minutosBloqueados=nz(v);} public BigDecimal getMinutosTorneos(){return minutosTorneos;} public void setMinutosTorneos(BigDecimal v){minutosTorneos=nz(v);} public BigDecimal getCapacidadDisponible(){return minutosCapacidad.subtract(minutosBloqueados).subtract(minutosTorneos).max(BigDecimal.ZERO);} public BigDecimal getOcupacionReservasReal(){return getCapacidadDisponible().signum()==0?BigDecimal.ZERO:minutosReservados.multiply(BigDecimal.valueOf(100)).divide(getCapacidadDisponible(),2,RoundingMode.HALF_UP);} public BigDecimal getUsoTotalCanchas(){return minutosCapacidad.signum()==0?BigDecimal.ZERO:minutosReservados.add(minutosBloqueados).add(minutosTorneos).multiply(BigDecimal.valueOf(100)).divide(minutosCapacidad,2,RoundingMode.HALF_UP);}
    public BigDecimal getVariacionReservas(){return variacionReservas;} public void setVariacionReservas(BigDecimal v){variacionReservas=nz(v);}
    public BigDecimal getVariacionIngresos(){return variacionIngresos;} public void setVariacionIngresos(BigDecimal v){variacionIngresos=nz(v);}
    public List<DatoGrafico> getReservasPorEstado(){return ro(reservasPorEstado);} public void setReservasPorEstado(List<DatoGrafico> v){reservasPorEstado=cp(v);}
    public List<DatoGrafico> getReservasPorOrigen(){return ro(reservasPorOrigen);} public void setReservasPorOrigen(List<DatoGrafico> v){reservasPorOrigen=cp(v);}
    public List<DatoGrafico> getReservasPorDia(){return ro(reservasPorDia);} public void setReservasPorDia(List<DatoGrafico> v){reservasPorDia=cp(v);}
    public List<DatoGrafico> getCancelacionesPorTipo(){return ro(cancelacionesPorTipo);} public void setCancelacionesPorTipo(List<DatoGrafico> v){cancelacionesPorTipo=cp(v);}
    public List<DatoGrafico> getEvolucionIncidencias(){return ro(evolucionIncidencias);} public void setEvolucionIncidencias(List<DatoGrafico> v){evolucionIncidencias=cp(v);}
    public List<DatoGrafico> getReservasPorCancha(){return ro(reservasPorCancha);} public void setReservasPorCancha(List<DatoGrafico> v){reservasPorCancha=cp(v);}
    public List<DatoGrafico> getIngresosPorMes(){return ro(ingresosPorMes);} public void setIngresosPorMes(List<DatoGrafico> v){ingresosPorMes=cp(v);}
    public List<DatoGrafico> getIngresosPorMetodo(){return ro(ingresosPorMetodo);} public void setIngresosPorMetodo(List<DatoGrafico> v){ingresosPorMetodo=cp(v);}
    public List<DatoGrafico> getPagosRecibidosPorDia(){return ro(pagosRecibidosPorDia);} public void setPagosRecibidosPorDia(List<DatoGrafico> v){pagosRecibidosPorDia=cp(v);}
    public List<DatoGrafico> getReembolsosPorDia(){return ro(reembolsosPorDia);} public void setReembolsosPorDia(List<DatoGrafico> v){reembolsosPorDia=cp(v);}
    public List<DatoGrafico> getMovimientosCajaPorTipo(){return ro(movimientosCajaPorTipo);} public void setMovimientosCajaPorTipo(List<DatoGrafico> v){movimientosCajaPorTipo=cp(v);}
    public List<DatoGrafico> getMovimientosCajaPorMedio(){return ro(movimientosCajaPorMedio);} public void setMovimientosCajaPorMedio(List<DatoGrafico> v){movimientosCajaPorMedio=cp(v);}
    public List<DatoGrafico> getDiferenciasCajaPorDia(){return ro(diferenciasCajaPorDia);} public void setDiferenciasCajaPorDia(List<DatoGrafico> v){diferenciasCajaPorDia=cp(v);}
    public List<DatoGrafico> getHorariosMasSolicitados(){return ro(horariosMasSolicitados);} public void setHorariosMasSolicitados(List<DatoGrafico> v){horariosMasSolicitados=cp(v);}
    public List<DatoGrafico> getDiasMasSolicitados(){return ro(diasMasSolicitados);} public void setDiasMasSolicitados(List<DatoGrafico> v){diasMasSolicitados=cp(v);}
    public List<DatoGrafico> getClientesFrecuentes(){return ro(clientesFrecuentes);} public void setClientesFrecuentes(List<DatoGrafico> v){clientesFrecuentes=cp(v);}
    public List<DatoGrafico> getClientesPorFacturacion(){return ro(clientesPorFacturacion);} public void setClientesPorFacturacion(List<DatoGrafico> v){clientesPorFacturacion=cp(v);}
    public List<DatoGrafico> getClientesNuevosPorMes(){return ro(clientesNuevosPorMes);} public void setClientesNuevosPorMes(List<DatoGrafico> v){clientesNuevosPorMes=cp(v);}
    public List<DatoGrafico> getClientesPorPosicion(){return ro(clientesPorPosicion);} public void setClientesPorPosicion(List<DatoGrafico> v){clientesPorPosicion=cp(v);}
    public List<DatoGrafico> getActividadClientes(){return ro(actividadClientes);} public void setActividadClientes(List<DatoGrafico> v){actividadClientes=cp(v);}
    public int getWebSolicitudesCreadas(){return webSolicitudesCreadas;} public void setWebSolicitudesCreadas(int v){webSolicitudesCreadas=v;} public int getWebPendientes(){return webPendientes;} public void setWebPendientes(int v){webPendientes=v;} public int getWebConfirmadasActuales(){return webConfirmadasActuales;} public void setWebConfirmadasActuales(int v){webConfirmadasActuales=v;} public int getWebExpiradas(){return webExpiradas;} public void setWebExpiradas(int v){webExpiradas=v;} public int getWebCanceladas(){return webCanceladas;} public void setWebCanceladas(int v){webCanceladas=v;} public int getWebCompletadas(){return webCompletadas;} public void setWebCompletadas(int v){webCompletadas=v;} public int getWebAusentes(){return webAusentes;} public void setWebAusentes(int v){webAusentes=v;} public int getWebConPago(){return webConPago;} public void setWebConPago(int v){webConPago=v;} public BigDecimal getWebDineroRecibido(){return webDineroRecibido;} public void setWebDineroRecibido(BigDecimal v){webDineroRecibido=nz(v);} public BigDecimal getWebDineroReembolsado(){return webDineroReembolsado;} public void setWebDineroReembolsado(BigDecimal v){webDineroReembolsado=nz(v);} public BigDecimal getWebPromedioMinutosPrimerPago(){return webPromedioMinutosPrimerPago;} public void setWebPromedioMinutosPrimerPago(BigDecimal v){webPromedioMinutosPrimerPago=nz(v);} public BigDecimal getWebTasaAcreditacion(){return webSolicitudesCreadas==0?BigDecimal.ZERO:BigDecimal.valueOf(webConPago).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(webSolicitudesCreadas),2,RoundingMode.HALF_UP);} public BigDecimal getWebTasaExpiracion(){return webSolicitudesCreadas==0?BigDecimal.ZERO:BigDecimal.valueOf(webExpiradas).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(webSolicitudesCreadas),2,RoundingMode.HALF_UP);}
    public List<DatoGrafico> getWebSolicitudesPorDia(){return ro(webSolicitudesPorDia);} public void setWebSolicitudesPorDia(List<DatoGrafico> v){webSolicitudesPorDia=cp(v);} public List<DatoGrafico> getWebEmbudo(){return ro(webEmbudo);} public void setWebEmbudo(List<DatoGrafico> v){webEmbudo=cp(v);} public List<DatoGrafico> getWebHorarios(){return ro(webHorarios);} public void setWebHorarios(List<DatoGrafico> v){webHorarios=cp(v);} public List<DatoGrafico> getWebDias(){return ro(webDias);} public void setWebDias(List<DatoGrafico> v){webDias=cp(v);} public List<DatoGrafico> getWebMetodosPago(){return ro(webMetodosPago);} public void setWebMetodosPago(List<DatoGrafico> v){webMetodosPago=cp(v);} public List<DatoGrafico> getWebEstados(){return ro(webEstados);} public void setWebEstados(List<DatoGrafico> v){webEstados=cp(v);}
    public List<DatoGrafico> getOcupacionPorCancha(){return ro(ocupacionPorCancha);} public void setOcupacionPorCancha(List<DatoGrafico> v){ocupacionPorCancha=cp(v);} public List<DatoGrafico> getIngresosPorCancha(){return ro(ingresosPorCancha);} public void setIngresosPorCancha(List<DatoGrafico> v){ingresosPorCancha=cp(v);} public List<DatoGrafico> getUsoMinutosPorCancha(){return ro(usoMinutosPorCancha);} public void setUsoMinutosPorCancha(List<DatoGrafico> v){usoMinutosPorCancha=cp(v);} public List<DatoGrafico> getBloqueosPorCancha(){return ro(bloqueosPorCancha);} public void setBloqueosPorCancha(List<DatoGrafico> v){bloqueosPorCancha=cp(v);} public List<DatoGrafico> getTorneosPorCancha(){return ro(torneosPorCancha);} public void setTorneosPorCancha(List<DatoGrafico> v){torneosPorCancha=cp(v);}
    public List<CierreCaja> getCierresCaja(){return Collections.unmodifiableList(cierresCaja);} public void setCierresCaja(List<CierreCaja> v){cierresCaja=v==null?new ArrayList<>():new ArrayList<>(v);}
    public BigDecimal getTasaCancelacion(){return porcentaje(reservasCanceladas);} public BigDecimal getTasaAusencia(){return porcentaje(reservasAusentes);}
    public BigDecimal getTasaConfirmacion(){int confirmadas=reservasConfirmadas+reservasCompletadas+reservasAusentes;return porcentaje(confirmadas);}
    public BigDecimal getTasaFinalizacion(){return reservasEfectivas==0?BigDecimal.ZERO:BigDecimal.valueOf(reservasCompletadas).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(reservasEfectivas),2,RoundingMode.HALF_UP);}
    public BigDecimal getTasaExpiracionWeb(){return reservasWeb==0?BigDecimal.ZERO:BigDecimal.valueOf(reservasExpiradas).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(reservasWeb),2,RoundingMode.HALF_UP);}
    public BigDecimal getConversionWeb(){if(reservasWeb==0)return BigDecimal.ZERO;int convertidas=valorEstadoOrigen("CONFIRMADA_WEB")+valorEstadoOrigen("COMPLETADA_WEB")+valorEstadoOrigen("AUSENTE_WEB");return BigDecimal.valueOf(convertidas).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(reservasWeb),2,RoundingMode.HALF_UP);}
    private int valorEstadoOrigen(String clave){return reservasPorOrigen.stream().filter(d->clave.equals(d.etiqueta())).map(DatoGrafico::valor).mapToInt(BigDecimal::intValue).sum();}
    private BigDecimal porcentaje(int c){return totalReservas==0?BigDecimal.ZERO:BigDecimal.valueOf(c).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalReservas),2,RoundingMode.HALF_UP);}
    private BigDecimal nz(BigDecimal v){return v==null?BigDecimal.ZERO:v;} private List<DatoGrafico> cp(List<DatoGrafico> v){return v==null?new ArrayList<>():new ArrayList<>(v);} private List<DatoGrafico> ro(List<DatoGrafico> v){return Collections.unmodifiableList(v);}
    public record DatoGrafico(String etiqueta, BigDecimal valor){public DatoGrafico{if(etiqueta==null||etiqueta.isBlank())throw new IllegalArgumentException("La etiqueta es obligatoria."); valor=valor==null?BigDecimal.ZERO:valor;}}
}
