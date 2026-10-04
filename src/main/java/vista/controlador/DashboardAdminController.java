package vista.controlador;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import negocio.ConfiguracionComplejo;
import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.ResumenDashboard;
import negocio.Usuario;
import servicio.DashboardService;
import vista.FiltroPagos;
import vista.FiltroReservas;
import vista.Navegacion;

public class DashboardAdminController {
    private static final DateTimeFormatter FECHA=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA=DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter ACTUALIZACION=DateTimeFormatter.ofPattern("HH:mm");
    private final DashboardService dashboardService=new DashboardService();
    @FXML private Label etiquetaUsuario,etiquetaFecha,etiquetaReservasHoy,etiquetaCanchasActivas,etiquetaCanchasActivasOperador,etiquetaIngresosDia,etiquetaIngresosMes,etiquetaPagosPendientes,etiquetaEstadoCarga,etiquetaUltimaActualizacion,etiquetaProximoTurno,etiquetaDetalleProximoTurno,etiquetaPendientesOperativos,etiquetaDetallePendientes,etiquetaResumenAlertas,etiquetaCantidadAlertas,etiquetaSeniasOperador,etiquetaTurnosOperador,etiquetaCajaOperador,alertaSenias,alertaVencimientos,alertaPagos,alertaTurnos,alertaCaja,etiquetaNombreComplejo,logoFallback;
    @FXML private ProgressIndicator indicadorCarga;
    @FXML private Button botonActualizar,botonUsuarios,botonConfiguracion,botonCanchas,botonBloqueos,botonEstadisticas,accesoRapidoCanchas;
    @FXML private VBox grupoAnalisis,grupoSistema,panelAlertas,panelRendimientoAdministrador,panelEstadoOperador;
    @FXML private HBox filaAlertaSenias,filaAlertaVencimientos,filaAlertaPagos,filaAlertaTurnos,filaAlertaCaja,filaSinAlertas;
    @FXML private ImageView logoComplejo;
    @FXML private TableView<Reserva> tablaProximasReservas;
    @FXML private TableColumn<Reserva,LocalDate> columnaFecha;
    @FXML private TableColumn<Reserva,LocalTime> columnaHora;
    @FXML private TableColumn<Reserva,String> columnaCancha,columnaCliente;
    @FXML private TableColumn<Reserva,EstadoReserva> columnaEstado;

    @FXML private void initialize(){mostrarDatosSesion();cargarIdentidad();configurarTabla();aplicarPermisos();Platform.runLater(()->{configurarDeseleccionExterna();actualizarDashboard();});}
    private void aplicarPermisos(){Usuario u=Navegacion.getUsuarioActual();boolean admin=u!=null&&u.esAdministrador();controlar(botonUsuarios,admin);controlar(botonConfiguracion,admin);controlar(botonCanchas,true);controlar(botonEstadisticas,admin);controlar(accesoRapidoCanchas,true);controlar(grupoAnalisis,admin);controlar(grupoSistema,admin);controlar(panelRendimientoAdministrador,admin);controlar(panelEstadoOperador,!admin);controlar(botonBloqueos,true);}
    private void controlar(Node n,boolean permitido){if(n==null)throw new IllegalStateException("Falta conectar un control de permisos.");n.setVisible(permitido);n.setManaged(permitido);}
    private void mostrarDatosSesion(){Usuario u=Navegacion.getUsuarioActual();etiquetaUsuario.setText(u==null?"Personal":u.getNombreUsuario());DateTimeFormatter f=DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy",new Locale("es","AR"));etiquetaFecha.setText("Operación del "+capitalizar(LocalDate.now().format(f)).toLowerCase(Locale.ROOT));}
    private void cargarIdentidad(){ConfiguracionComplejo c=Navegacion.getConfiguracionActual();String nombre=c==null||c.getNombreComercial()==null||c.getNombreComercial().isBlank()?"Padel Reservas":c.getNombreComercial();etiquetaNombreComplejo.setText(nombre);if(c==null||c.getRutaLogo()==null||c.getRutaLogo().isBlank())return;File f=new File(c.getRutaLogo());if(!f.isFile())return;try{Image i=new Image(f.toURI().toString(),false);if(!i.isError()){logoComplejo.setImage(i);logoFallback.setVisible(false);logoFallback.setManaged(false);}}catch(RuntimeException ignored){}}
    private void configurarTabla(){columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));columnaFecha.setCellFactory(x->new TableCell<>(){@Override protected void updateItem(LocalDate v,boolean e){super.updateItem(v,e);setText(e||v==null?null:v.format(FECHA));}});columnaHora.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));columnaHora.setCellFactory(x->new TableCell<>(){@Override protected void updateItem(LocalTime v,boolean e){super.updateItem(v,e);setText(e||v==null?null:v.format(HORA));}});columnaCancha.setCellValueFactory(new PropertyValueFactory<>("nombreCancha"));columnaCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));columnaEstado.setCellFactory(x->new TableCell<>(){@Override protected void updateItem(EstadoReserva v,boolean e){super.updateItem(v,e);getStyleClass().removeAll("dashboard-status-pending","dashboard-status-confirmed");setText(e||v==null?null:v.toString());if(!e&&v!=null)getStyleClass().add(v==EstadoReserva.PENDIENTE?"dashboard-status-pending":"dashboard-status-confirmed");}});tablaProximasReservas.setRowFactory(t->{var row=new javafx.scene.control.TableRow<Reserva>();row.setOnMouseClicked(e->{if(e.getButton()==MouseButton.PRIMARY&&e.getClickCount()==2&&!row.isEmpty())abrirReservaSeleccionada(row.getItem());});return row;});}
    private void abrirReservaSeleccionada(Reserva r){if(r==null||r.getId()<=0){cambiarEstadoCarga(false,"No se pudo identificar la reserva seleccionada.");return;}Navegacion.mostrarReservaDesdeAgenda(r.getId());}
    private void configurarDeseleccionExterna(){
        if(tablaProximasReservas.getScene()==null)return;

        tablaProximasReservas.getScene().addEventFilter(
                MouseEvent.MOUSE_PRESSED,
                evento -> {
                    boolean dentroTabla = perteneceA(
                            evento.getTarget(),
                            tablaProximasReservas);
                    boolean filaConReserva = perteneceAFilaConDatos(
                            evento.getTarget());

                    if(!dentroTabla || !filaConReserva){
                        Platform.runLater(
                                this::limpiarSeleccionProximas);
                    }
                });

        tablaProximasReservas.getScene()
                .focusOwnerProperty()
                .addListener((observable, anterior, actual) -> {
                    if(actual != null
                            && !perteneceA(
                                    actual,
                                    tablaProximasReservas)){
                        Platform.runLater(
                                this::limpiarSeleccionProximas);
                    }
                });
    }

    private void limpiarSeleccionProximas(){
        tablaProximasReservas
                .getSelectionModel()
                .clearSelection();

        tablaProximasReservas
                .getFocusModel()
                .focus(-1);

        tablaProximasReservas.refresh();
    }
    private boolean perteneceAFilaConDatos(Object objetivo){
        if(!(objetivo instanceof Node nodo))return false;
        Node actual=nodo;
        while(actual!=null && actual!=tablaProximasReservas){
            if(actual instanceof javafx.scene.control.TableRow<?> fila){
                return !fila.isEmpty();
            }
            actual=actual.getParent();
        }
        return false;
    }

    private boolean perteneceA(Object objetivo,Node contenedor){
        if(!(objetivo instanceof Node nodo))return false;
        Node actual=nodo;
        while(actual!=null){
            if(actual==contenedor)return true;
            Parent padre=actual.getParent();
            actual=padre;
        }
        return false;
    }
    @FXML public void actualizarDashboard(){cambiarEstadoCarga(true,"Actualizando información...");Task<DatosDashboard> t=new Task<>(){@Override protected DatosDashboard call(){return new DatosDashboard(dashboardService.obtenerResumen(),dashboardService.listarProximasReservas());}};t.setOnSucceeded(e->{DatosDashboard d=t.getValue();mostrarResumen(d.resumen(),d.proximas());tablaProximasReservas.getItems().setAll(d.proximas());cambiarEstadoCarga(false,"Datos actualizados correctamente.");etiquetaUltimaActualizacion.setText("Última actualización: "+LocalTime.now().format(ACTUALIZACION));});t.setOnFailed(e->{Throwable x=t.getException();if(x!=null)x.printStackTrace();cambiarEstadoCarga(false,x==null||x.getMessage()==null?"No se pudo actualizar el inicio.":x.getMessage());});Thread h=new Thread(t,"dashboard-padel-reservas");h.setDaemon(true);h.start();}
    private void mostrarResumen(ResumenDashboard r,List<Reserva> proximas){etiquetaReservasHoy.setText(String.valueOf(r.getReservasHoy()));etiquetaCanchasActivas.setText(String.valueOf(r.getCanchasActivas()));etiquetaCanchasActivasOperador.setText(String.valueOf(r.getCanchasActivas()));etiquetaIngresosDia.setText(moneda(r.getIngresosDia()));etiquetaIngresosMes.setText(moneda(r.getIngresosMes()));etiquetaPagosPendientes.setText(String.valueOf(r.getPagosPendientes()));mostrarProximoTurno(proximas);int pendientes=r.getReservasPendientesSenia()+r.getPagosPendientes()+r.getTurnosPendientesCierre()+(r.isCajaCerradaHoy()?0:1);etiquetaPendientesOperativos.setText(String.valueOf(pendientes));etiquetaDetallePendientes.setText(detallePendientes(r));etiquetaSeniasOperador.setText(String.valueOf(r.getReservasPendientesSenia()));etiquetaTurnosOperador.setText(String.valueOf(r.getTurnosPendientesCierre()));etiquetaCajaOperador.setText(r.isCajaCerradaHoy()?"Cerrada":"Pendiente");mostrarAlertas(r);}
    private void mostrarProximoTurno(List<Reserva> ps){if(ps==null||ps.isEmpty()){etiquetaProximoTurno.setText("Sin turnos");etiquetaDetalleProximoTurno.setText("No hay reservas próximas");return;}Reserva p=ps.get(0);etiquetaProximoTurno.setText(p.getHoraInicio().format(HORA));String fecha=p.getFecha().equals(LocalDate.now())?"Hoy":p.getFecha().format(FECHA);etiquetaDetalleProximoTurno.setText(fecha+" · "+p.getNombreCancha()+" · "+p.getNombreCliente());}
    private String detallePendientes(ResumenDashboard r){List<String> d=new java.util.ArrayList<>();if(r.getReservasPendientesSenia()>0)d.add(r.getReservasPendientesSenia()+" "+plural(r.getReservasPendientesSenia(),"seña","señas"));if(r.getTurnosPendientesCierre()>0)d.add(r.getTurnosPendientesCierre()+" "+plural(r.getTurnosPendientesCierre(),"cierre","cierres"));if(r.getPagosPendientes()>0)d.add(r.getPagosPendientes()+" "+plural(r.getPagosPendientes(),"pago","pagos"));return d.isEmpty()?"Sin acciones pendientes":String.join(" · ",d);}
    private void mostrarAlertas(ResumenDashboard r){int cantidad=0;cantidad+=activar(filaAlertaSenias,r.getReservasPendientesSenia()>0);cantidad+=activar(filaAlertaVencimientos,r.getReservasProximasAVencer()>0);cantidad+=activar(filaAlertaPagos,r.getPagosPendientes()>0);cantidad+=activar(filaAlertaTurnos,r.getTurnosPendientesCierre()>0);cantidad+=activar(filaAlertaCaja,!r.isCajaCerradaHoy());activar(filaSinAlertas,cantidad==0);alertaSenias.setText(textoCantidad(r.getReservasPendientesSenia(),"reserva esperando seña","reservas esperando seña"));alertaVencimientos.setText(textoCantidad(r.getReservasProximasAVencer(),"solicitud web vence en menos de 5 minutos","solicitudes web vencen en menos de 5 minutos"));alertaPagos.setText(textoCantidad(r.getPagosPendientes(),"pago pendiente de acreditar","pagos pendientes de acreditar"));alertaTurnos.setText(textoCantidad(r.getTurnosPendientesCierre(),"turno pendiente de cerrar","turnos pendientes de cerrar"));alertaCaja.setText("Caja del día pendiente de cierre");etiquetaCantidadAlertas.setText(textoCantidad(cantidad,"pendiente","pendientes"));etiquetaResumenAlertas.setText(cantidad==0?"La operación no requiere acciones urgentes":"Revisá los temas que requieren intervención");}
    private int activar(Node n,boolean activo){n.setVisible(activo);n.setManaged(activo);return activo?1:0;}
    private String textoCantidad(int n,String singular,String plural){return n+" "+(n==1?singular:plural);}
    private String plural(int n,String singular,String plural){return n==1?singular:plural;}
    private String moneda(BigDecimal v){return NumberFormat.getCurrencyInstance(new Locale("es","AR")).format(v==null?BigDecimal.ZERO:v.setScale(2,RoundingMode.HALF_UP));}
    private void cambiarEstadoCarga(boolean c,String m){indicadorCarga.setVisible(c);botonActualizar.setDisable(c);etiquetaEstadoCarga.setText(m);etiquetaEstadoCarga.getStyleClass().removeAll("dashboard-status-ok","dashboard-status-error");if(!c)etiquetaEstadoCarga.getStyleClass().add(m!=null&&(m.toLowerCase(Locale.ROOT).contains("no se pudo")||m.toLowerCase(Locale.ROOT).contains("error"))?"dashboard-status-error":"dashboard-status-ok");}
    @FXML private void verEsperandoSenia(){Navegacion.mostrarReservasConFiltro(FiltroReservas.PENDIENTES_SENIA);}@FXML private void verPagosPendientes(){Navegacion.mostrarPagosConFiltro(FiltroPagos.PENDIENTES_ACREDITACION);}@FXML private void verTurnosPendientes(){Navegacion.mostrarReservasConFiltro(FiltroReservas.PENDIENTES_CIERRE);}@FXML private void abrirSolicitudesWeb(){Navegacion.mostrarSolicitudesWeb();}@FXML private void abrirTorneos(){Navegacion.mostrarTorneos();}@FXML private void abrirTorneosInscripciones(){Navegacion.mostrarTorneosInscripciones();}@FXML private void abrirCanchas(){Navegacion.mostrarCanchas();}@FXML private void abrirClientes(){Navegacion.mostrarClientes();}@FXML private void abrirAgenda(){Navegacion.mostrarAgenda();}@FXML private void abrirReservas(){Navegacion.mostrarReservas();}@FXML private void abrirPagos(){Navegacion.mostrarPagos();}@FXML private void abrirCierreCaja(){Navegacion.mostrarCierreCaja();}@FXML private void abrirBloqueos(){Navegacion.mostrarBloqueos();}@FXML private void abrirEstadisticas(){Navegacion.mostrarEstadisticas();}@FXML private void abrirConfiguracion(){Navegacion.mostrarConfiguracion();}@FXML private void abrirUsuarios(){Navegacion.mostrarUsuarios();}@FXML private void cerrarSesion(){Navegacion.cerrarSesion();}
    private String capitalizar(String t){return t==null||t.isBlank()?"":Character.toUpperCase(t.charAt(0))+t.substring(1);}
    private record DatosDashboard(ResumenDashboard resumen,List<Reserva> proximas){}
}
