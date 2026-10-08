package vista.controlador;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import negocio.Cliente;
import negocio.EstadoReserva;
import negocio.OrigenReserva;
import negocio.Reserva;
import servicio.ClienteService;
import servicio.MensajeReservaService;
import servicio.PagoService;
import servicio.ReservaService;
import servicio.WhatsAppService;
import util.FormateadorMoneda;
import vista.Navegacion;

public class SolicitudesWebController {
    // solicitudes-web-acciones-diferenciadas-v1
    // solicitudes-web-regex-escape-v1
    // solicitudes-web-inconsistencias-v2
    // solicitudes-web-cierre-administrativo-v1
    private static final DateTimeFormatter FECHA=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA=DateTimeFormatter.ofPattern("HH:mm");
    private final ReservaService reservaService=new ReservaService();
    private final ClienteService clienteService=new ClienteService();
    private final PagoService pagoService=new PagoService();
    private final MensajeReservaService mensajeService=new MensajeReservaService();
    private final WhatsAppService whatsAppService=new WhatsAppService();
    private final ObservableList<Reserva> solicitudes=FXCollections.observableArrayList();
    private FilteredList<Reserva> solicitudesFiltradas;
    private Reserva seleccionada;
    private Timeline reloj;

    @FXML private TextField campoBuscar;
    @FXML private ComboBox<EstadoReserva> filtroEstado;
    @FXML private TableView<Reserva> tablaSolicitudes;
    @FXML private TableColumn<Reserva,Long> columnaId;
    @FXML private TableColumn<Reserva,String> columnaCliente,columnaTurno,columnaCancha;
    @FXML private TableColumn<Reserva,EstadoReserva> columnaEstado;
    @FXML private TableColumn<Reserva,Reserva> columnaVencimiento;
    @FXML private TableColumn<Reserva,Reserva> columnaTiempo;
    @FXML private Label etiquetaTotal,etiquetaVigentes,etiquetaProximas,etiquetaExpiradas,etiquetaMensaje,etiquetaActualizacion,etiquetaEstadoCarga,detalleTitulo,detalleCliente,detalleTelefono,detalleTurno,detalleCancha,detalleEstado,detalleOrigen,detalleSituacion,detalleVencimiento,detalleAyudaSituacion,detallePrecio,detalleAcreditado,detalleSaldo,detallePorcentajePago;
    @FXML private ProgressBar detalleProgresoPago;
    @FXML private Button botonReserva,botonPagos,botonWhatsApp,botonLimpiar,botonActualizar,tarjetaTotal,tarjetaVigentes,tarjetaProximas,tarjetaExpiradas;
    @FXML private VBox panelDetalle,panelDetalleVacio;
    @FXML private HBox panelAccionesSecundarias;

    @FXML private void initialize(){configurarTabla();configurarFiltros();mostrarDetalleVacio();cargarSolicitudes();iniciarReloj();Platform.runLater(this::configurarDeseleccion);}

    private void configurarTabla(){
        tablaSolicitudes.setFixedCellSize(40);
        columnaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnaId.setStyle("-fx-alignment: CENTER;");
        columnaId.setCellFactory(c->new TableCell<>(){
            @Override protected void updateItem(Long valor,boolean vacia){
                super.updateItem(valor,vacia);
                setAlignment(Pos.CENTER);
                setText(vacia||valor==null?null:String.valueOf(valor));
            }
        });
        columnaCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        columnaCliente.setStyle("-fx-alignment: CENTER-LEFT;");
        columnaCliente.setCellFactory(c->celdaTextoIzquierda());
        columnaCancha.setCellValueFactory(new PropertyValueFactory<>("nombreCancha"));
        columnaCancha.setStyle("-fx-alignment: CENTER;");
        columnaCancha.setCellFactory(c->celdaTextoCentrada());
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columnaEstado.setStyle("-fx-alignment: CENTER;");
        columnaEstado.setCellFactory(c->new TableCell<>(){@Override protected void updateItem(EstadoReserva e,boolean vacia){super.updateItem(e,vacia);setAlignment(Pos.CENTER);getStyleClass().removeAll("web-state-pending","web-state-confirmed","web-state-expired","web-state-cancelled","web-state-completed","web-state-absent");setStyle("");setText(vacia||e==null?null:nombreEstado(e));if(!vacia&&e!=null){getStyleClass().add(claseEstado(e));setStyle("-fx-text-fill:"+colorEstado(e)+";-fx-font-weight:900;");}}});
        columnaTurno.setStyle("-fx-alignment: CENTER;");
        columnaTurno.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().getFecha().format(FECHA)+" "+d.getValue().getHoraInicio().format(HORA)));
        columnaTurno.setCellFactory(c->celdaTextoCentrada());
        columnaVencimiento.setStyle("-fx-alignment: CENTER;");
        columnaVencimiento.setCellValueFactory(d->new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        columnaVencimiento.setCellFactory(c->new TableCell<>(){
            @Override protected void updateItem(Reserva r,boolean vacia){
                super.updateItem(r,vacia);
                setAlignment(Pos.CENTER);
                setText(vacia||r==null?null:textoVencimiento(r));
                getStyleClass().removeAll("web-time-ok","web-time-warning","web-time-expired");
                if(!vacia&&r!=null)getStyleClass().add(claseTiempo(r));
            }
        });
        columnaTiempo.setStyle("-fx-alignment: CENTER;");
        columnaTiempo.setCellValueFactory(d->new javafx.beans.property.SimpleObjectProperty<>(d.getValue()));
        columnaTiempo.setCellFactory(c->new TableCell<>(){@Override protected void updateItem(Reserva r,boolean vacia){super.updateItem(r,vacia);setAlignment(Pos.CENTER);setText(vacia||r==null?null:textoSituacion(r));getStyleClass().removeAll("web-time-ok","web-time-warning","web-time-expired");setStyle("");if(!vacia&&r!=null){getStyleClass().add(claseTiempo(r));setStyle("-fx-text-fill:"+colorSituacion(r)+";-fx-font-weight:900;");}}});
        tablaSolicitudes.getSelectionModel().selectedItemProperty().addListener((o,a,n)->{if(n!=null)mostrarDetalle(n);});
        tablaSolicitudes.setRowFactory(t->{
            TableRow<Reserva> fila=new TableRow<>();
            fila.setOnMousePressed(e->{
                if(!fila.isEmpty()&&fila.getItem()!=null){
                    tablaSolicitudes.getSelectionModel().select(fila.getItem());
                    tablaSolicitudes.requestFocus();
                }
            });
            fila.setOnMouseClicked(e->{
                if(e.getButton()==MouseButton.PRIMARY&&e.getClickCount()==2&&!fila.isEmpty()){
                    detenerReloj();
                    Navegacion.mostrarReservaDesdeAgenda(fila.getItem().getId());
                }
            });
            return fila;
        });
    }

    private TableCell<Reserva,String> celdaTextoIzquierda(){
        return new TableCell<>(){
            @Override protected void updateItem(String valor,boolean vacia){
                super.updateItem(valor,vacia);
                setAlignment(Pos.CENTER_LEFT);
                setText(vacia||valor==null?null:valor);
            }
        };
    }

    private TableCell<Reserva,String> celdaTextoCentrada(){
        return new TableCell<>(){
            @Override protected void updateItem(String valor,boolean vacia){
                super.updateItem(valor,vacia);
                setAlignment(Pos.CENTER);
                setText(vacia||valor==null?null:valor);
            }
        };
    }

    private void configurarFiltros(){
        filtroEstado.setItems(FXCollections.observableArrayList(
                EstadoReserva.PENDIENTE,EstadoReserva.CONFIRMADA,
                EstadoReserva.EXPIRADA,EstadoReserva.CANCELADA,
                EstadoReserva.COMPLETADA,EstadoReserva.AUSENTE));
        filtroEstado.setCellFactory(lista->crearCeldaEstadoFiltro(false));
        filtroEstado.setButtonCell(crearCeldaEstadoFiltro(true));
        solicitudesFiltradas=new FilteredList<>(solicitudes,v->true);
        tablaSolicitudes.setItems(solicitudesFiltradas);
        campoBuscar.textProperty().addListener((o,a,n)->aplicarFiltros());
        filtroEstado.valueProperty().addListener((o,a,n)->aplicarFiltros());
        campoBuscar.setOnKeyPressed(e->{
            if(e.getCode()==KeyCode.ESCAPE)limpiarFiltros();
        });
        botonLimpiar.setDisable(false);
    }

    private javafx.scene.control.ListCell<EstadoReserva> crearCeldaEstadoFiltro(
            boolean boton){
        return new javafx.scene.control.ListCell<>(){
            @Override protected void updateItem(
                    EstadoReserva estado,boolean vacia){
                super.updateItem(estado,vacia);
                if(vacia||estado==null){
                    setText(boton?"Todos los estados":null);
                }else{
                    setText(nombreEstado(estado));
                }
                setGraphic(null);
            }
        };
    }

    @FXML public void cargarSolicitudes(){
        botonActualizar.setDisable(true);etiquetaEstadoCarga.setText("Actualizando...");
        try{solicitudes.setAll(reservaService.listar().stream().filter(r->r.getOrigen()==OrigenReserva.WEB).toList());aplicarFiltros();actualizarResumen();etiquetaActualizacion.setText("Última actualización: "+LocalTime.now().format(HORA));etiquetaEstadoCarga.setText("");}
        catch(RuntimeException e){etiquetaEstadoCarga.setText(e.getMessage()==null?"No se pudieron cargar las solicitudes.":e.getMessage());}
        finally{botonActualizar.setDisable(false);}
    }

    private void aplicarFiltros(){if(solicitudesFiltradas==null)return;String texto=campoBuscar.getText()==null?"":campoBuscar.getText().trim().toLowerCase(Locale.ROOT);EstadoReserva estado=filtroEstado.getValue();solicitudesFiltradas.setPredicate(r->(estado==null||r.getEstado()==estado)&&(texto.isBlank()||contiene(r.getNombreCliente(),texto)||contiene(r.getNombreCancha(),texto)||contiene(nombreEstado(r.getEstado()),texto)||String.valueOf(r.getId()).contains(texto)));actualizarMensajeResultados();}
    private boolean contiene(String v,String f){return v!=null&&v.toLowerCase(Locale.ROOT).contains(f);}
    private void actualizarMensajeResultados(){
        int n=solicitudesFiltradas==null?0:solicitudesFiltradas.size();
        String busqueda=campoBuscar.getText()==null?"":campoBuscar.getText().trim();
        EstadoReserva estado=filtroEstado.getValue();
        String texto=n+" "+(n==1?"solicitud visible":"solicitudes visibles");
        if(estado!=null)texto+=" · "+nombreEstado(estado);
        if(!busqueda.isBlank())texto+=" para \""+busqueda+"\"";
        etiquetaMensaje.setText(texto);
    }
    @FXML private void limpiarFiltros(){campoBuscar.clear();filtroEstado.getSelectionModel().clearSelection();}
    @FXML private void mostrarTodas(){limpiarFiltros();}
    @FXML private void mostrarVigentes(){campoBuscar.clear();filtroEstado.setValue(EstadoReserva.PENDIENTE);}
    @FXML private void mostrarExpiradas(){campoBuscar.clear();filtroEstado.setValue(EstadoReserva.EXPIRADA);}
    @FXML private void mostrarProximas(){campoBuscar.clear();filtroEstado.setValue(EstadoReserva.PENDIENTE);solicitudesFiltradas.setPredicate(this::proximaAVencer);actualizarMensajeResultados();botonLimpiar.setDisable(false);}

    private void actualizarResumen(){LocalDateTime a=LocalDateTime.now();long vigentes=solicitudes.stream().filter(r->r.getEstado()==EstadoReserva.PENDIENTE&&r.getFechaVencimiento()!=null&&r.getFechaVencimiento().isAfter(a)).count();long proximas=solicitudes.stream().filter(this::proximaAVencer).count();long expiradas=solicitudes.stream().filter(r->r.getEstado()==EstadoReserva.EXPIRADA).count();etiquetaTotal.setText(String.valueOf(solicitudes.size()));etiquetaVigentes.setText(String.valueOf(vigentes));etiquetaProximas.setText(String.valueOf(proximas));etiquetaExpiradas.setText(String.valueOf(expiradas));tarjetaProximas.getStyleClass().remove("web-metric-warning");if(proximas>0)tarjetaProximas.getStyleClass().add("web-metric-warning");}
    private boolean proximaAVencer(Reserva r){LocalDateTime a=LocalDateTime.now();return r.getEstado()==EstadoReserva.PENDIENTE&&r.getFechaVencimiento()!=null&&r.getFechaVencimiento().isAfter(a)&&!r.getFechaVencimiento().isAfter(a.plusMinutes(5));}

    private void iniciarReloj(){reloj=new Timeline(new KeyFrame(javafx.util.Duration.seconds(1),e->{tablaSolicitudes.refresh();actualizarResumen();if(seleccionada!=null)actualizarSituacionDetalle();}));reloj.setCycleCount(Timeline.INDEFINITE);reloj.play();}
    private void detenerReloj(){if(reloj!=null)reloj.stop();}
    private String textoVencimiento(Reserva r){
        if(r==null)return "-";
        if(r.getEstado()==EstadoReserva.PENDIENTE&&r.getFechaVencimiento()!=null){
            Duration d=Duration.between(LocalDateTime.now(),r.getFechaVencimiento());
            if(d.isNegative()||d.isZero())return "Vencida";
            long m=d.toMinutes(),s=d.minusMinutes(m).getSeconds();
            return String.format("%02d:%02d",m,s);
        }
        return switch(r.getEstado()){
            case CONFIRMADA->"Acreditada";
            case EXPIRADA->"Vencida";
            case CANCELADA->"Cancelada";
            case COMPLETADA->"Cerrada";
            case AUSENTE->"Cerrada";
            case PENDIENTE->"Sin plazo";
        };
    }
    private String textoSituacion(Reserva r){if(r.getEstado()==EstadoReserva.EXPIRADA)return "Expirada";if(r.getEstado()==EstadoReserva.PENDIENTE&&r.getFechaVencimiento()!=null){Duration d=Duration.between(LocalDateTime.now(),r.getFechaVencimiento());if(d.isNegative()||d.isZero())return "Venciendo...";long m=d.toMinutes(),s=d.minusMinutes(m).getSeconds();return String.format("%02d:%02d",m,s);}LocalDateTime i=LocalDateTime.of(r.getFecha(),r.getHoraInicio()),f=LocalDateTime.of(r.getFecha(),r.getHoraFin()),a=LocalDateTime.now();if(a.isBefore(i))return "Próxima";if(a.isBefore(f))return "En curso";return "Finalizada";}
    private String claseTiempo(Reserva r){if(r.getEstado()==EstadoReserva.EXPIRADA)return "web-time-expired";if(proximaAVencer(r))return "web-time-warning";return "web-time-ok";}
    private String claseEstado(EstadoReserva e){return switch(e){case PENDIENTE->"web-state-pending";case CONFIRMADA->"web-state-confirmed";case EXPIRADA->"web-state-expired";case CANCELADA->"web-state-cancelled";case COMPLETADA->"web-state-completed";case AUSENTE->"web-state-absent";};}
    private String colorEstado(EstadoReserva e){return switch(e){case PENDIENTE->"#e0b75e";case CONFIRMADA->"#82c5aa";case EXPIRADA->"#d9828d";case CANCELADA->"#e0777f";case COMPLETADA->"#71b9a0";case AUSENTE->"#de9f69";};}
    private String colorSituacion(Reserva r){String s=textoSituacion(r);if("Expirada".equals(s))return "#d9828d";if("Venciendo...".equals(s)||s.matches("\\d{2}:\\d{2}"))return proximaAVencer(r)?"#efbc60":"#82c5aa";if("En curso".equals(s))return "#82c5aa";if("Próxima".equals(s))return "#8fbed1";return "#aebbc1";}

    private void mostrarDetalle(Reserva r){seleccionada=r;Cliente c=clienteService.buscar(r.getClienteId());panelDetalleVacio.setVisible(false);panelDetalleVacio.setManaged(false);panelDetalle.setVisible(true);panelDetalle.setManaged(true);detalleTitulo.setText("Solicitud web #"+r.getId());detalleCliente.setText(r.getNombreCliente());detalleTelefono.setText(c==null||c.getTelefono()==null||c.getTelefono().isBlank()?"Sin teléfono registrado":formatearTelefonoVisual(c.getTelefono()));detalleTurno.setText(r.getFecha().format(FECHA)+" · "+r.getHoraInicio().format(HORA)+" a "+r.getHoraFin().format(HORA));detalleCancha.setText(r.getNombreCancha());detalleEstado.setText(nombreEstado(r.getEstado()));detalleEstado.getStyleClass().removeAll("web-state-pending","web-state-confirmed","web-state-expired","web-state-cancelled","web-state-completed","web-state-absent");detalleEstado.getStyleClass().add(claseEstado(r.getEstado()));detalleEstado.setStyle("-fx-text-fill:"+colorEstado(r.getEstado())+";-fx-font-weight:900;");detalleOrigen.setText(r.getOrigen().toString());BigDecimal acreditado=pagoService.totalAcreditado(r.getId()),saldo=pagoService.calcularSaldo(r.getId());detallePrecio.setText(moneda(r.getPrecioTotal()));detalleAcreditado.setText(moneda(acreditado));detalleSaldo.setText(moneda(saldo));
        BigDecimal precio=r.getPrecioTotal()==null?BigDecimal.ZERO:r.getPrecioTotal();
        double progreso=precio.signum()<=0?0:acreditado.divide(precio,4,java.math.RoundingMode.HALF_UP).doubleValue();
        progreso=Math.max(0,Math.min(1,progreso));
        detalleProgresoPago.setProgress(progreso);
        detallePorcentajePago.setText(Math.round(progreso*100)+" % acreditado");
        actualizarSituacionDetalle();
        botonReserva.setText(r.getEstado()==EstadoReserva.EXPIRADA||r.getEstado()==EstadoReserva.CANCELADA?"VER RESERVA":"ABRIR RESERVA");
        botonReserva.setVisible(true);botonReserva.setManaged(true);configurarBoton(botonPagos,r.getEstado()==EstadoReserva.PENDIENTE||r.getEstado()==EstadoReserva.CONFIRMADA||r.getEstado()==EstadoReserva.COMPLETADA);configurarBoton(botonWhatsApp,c!=null&&c.getTelefono()!=null&&!c.getTelefono().isBlank());actualizarAlineacionAccionesSecundarias();}
    private void actualizarSituacionDetalle(){
        if(seleccionada==null)return;
        String situacion=textoSituacion(seleccionada);
        boolean repetida=nombreEstado(seleccionada.getEstado())
                .equalsIgnoreCase(situacion);
        detalleSituacion.setVisible(!repetida);
        detalleSituacion.setManaged(!repetida);
        detalleSituacion.setText(situacion);
        detalleSituacion.setStyle("-fx-text-fill:"
                +colorSituacion(seleccionada)+";-fx-font-weight:900;");
        detalleVencimiento.setText(situacion);
        detalleVencimiento.setStyle("-fx-text-fill:"
                +colorSituacion(seleccionada)+";-fx-font-weight:900;");
        detalleAyudaSituacion.setText(
                ayudaSituacion(seleccionada,situacion));
    }
    private String ayudaSituacion(Reserva r,String s){
        if(r.getEstado()==EstadoReserva.EXPIRADA)
            return "La solicitud venció y el turno fue liberado por falta de acreditación.";
        if(r.getEstado()==EstadoReserva.CANCELADA)
            return "La solicitud fue cancelada y el turno quedó liberado.";
        if(r.getEstado()==EstadoReserva.PENDIENTE)
            return s.matches("\\d{2}:\\d{2}")
                    ? "Tiempo restante para acreditar la seña."
                    : "La solicitud está alcanzando su vencimiento.";
        if("Finalizada".equals(s)&&r.getEstado()==EstadoReserva.CONFIRMADA)
            return "El turno terminó y puede requerir cierre como completado o ausente.";
        if("Próxima".equals(s))return "El turno todavía no comenzó.";
        if("En curso".equals(s))
            return "El turno se encuentra dentro de su horario reservado.";
        return "Situación temporal del turno seleccionado.";
    }
    private void mostrarDetalleVacio(){seleccionada=null;panelDetalle.setVisible(false);panelDetalle.setManaged(false);panelDetalleVacio.setVisible(true);panelDetalleVacio.setManaged(true);}

    private void configurarDeseleccion(){
        if(tablaSolicitudes.getScene()==null)return;
        tablaSolicitudes.getScene().addEventFilter(
                MouseEvent.MOUSE_PRESSED,
                evento->{
                    boolean dentroTabla=perteneceA(
                            evento.getTarget(),tablaSolicitudes);
                    boolean filaConDatos=perteneceAFilaConDatos(
                            evento.getTarget());
                    boolean dentroDetalle=perteneceA(
                            evento.getTarget(),panelDetalle);

                    if(!dentroDetalle
                            && (!dentroTabla || !filaConDatos)){
                        Platform.runLater(this::limpiarSeleccion);
                    }
                });
    }
    private boolean perteneceAFilaConDatos(Object o){if(!(o instanceof Node n))return false;Node a=n;while(a!=null&&a!=tablaSolicitudes){if(a instanceof TableRow<?> fila)return !fila.isEmpty();a=a.getParent();}return false;}
    private boolean perteneceA(Object o,Node contenedor){if(!(o instanceof Node n))return false;Node a=n;while(a!=null){if(a==contenedor)return true;Parent p=a.getParent();a=p;}return false;}
    private void limpiarSeleccion(){tablaSolicitudes.getSelectionModel().clearSelection();tablaSolicitudes.getFocusModel().focus(-1);tablaSolicitudes.refresh();mostrarDetalleVacio();}

    private String nombreEstado(EstadoReserva e){return switch(e){case PENDIENTE->"Esperando seña";case CONFIRMADA->"Confirmada";case EXPIRADA->"Expirada";case CANCELADA->"Cancelada";case COMPLETADA->"Completada";case AUSENTE->"Ausente";};}
    private String moneda(BigDecimal v){return FormateadorMoneda.pesos(v);}
    private String formatearTelefonoVisual(String telefono){
        if(telefono==null)return "";
        String limpio=telefono.replaceAll("\\D","");
        if(limpio.length()==10)return limpio.substring(0,4)+" "+limpio.substring(4);
        if(limpio.length()>6)return limpio.substring(0,limpio.length()-6)+" "+limpio.substring(limpio.length()-6);
        return telefono.trim();
    }
    private void actualizarAlineacionAccionesSecundarias(){
        if(panelAccionesSecundarias==null)return;
        panelAccionesSecundarias.setAlignment(Pos.CENTER);
        boolean pagosVisible=botonPagos.isManaged()&&botonPagos.isVisible();
        boolean whatsappVisible=botonWhatsApp.isManaged()&&botonWhatsApp.isVisible();
        botonPagos.setMaxWidth(Double.MAX_VALUE);
        botonWhatsApp.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(botonPagos,pagosVisible?javafx.scene.layout.Priority.ALWAYS:javafx.scene.layout.Priority.NEVER);
        HBox.setHgrow(botonWhatsApp,whatsappVisible?javafx.scene.layout.Priority.ALWAYS:javafx.scene.layout.Priority.NEVER);
    }
    private void configurarBoton(Button b,boolean visible){b.setVisible(visible);b.setManaged(visible);}
    @FXML private void abrirReserva(){if(seleccionada!=null){detenerReloj();Navegacion.mostrarReservaDesdeAgenda(seleccionada.getId());}}
    @FXML private void abrirPagos(){if(seleccionada!=null){detenerReloj();Navegacion.mostrarPagosDeReserva(seleccionada.getId());}}
    @FXML private void abrirWhatsApp(){if(seleccionada==null)return;try{Cliente c=clienteService.buscar(seleccionada.getClienteId());if(c==null||c.getTelefono()==null||c.getTelefono().isBlank())throw new IllegalArgumentException("El cliente no tiene un teléfono registrado.");BigDecimal saldo=pagoService.calcularSaldo(seleccionada.getId());String m=switch(seleccionada.getEstado()){case PENDIENTE->mensajeService.crearSolicitudSenia(c,seleccionada,saldo.max(BigDecimal.ZERO));case CONFIRMADA->saldo.signum()>0?mensajeService.crearAvisoSaldo(c,seleccionada,saldo):mensajeService.crearRecordatorio(c,seleccionada);case CANCELADA,EXPIRADA->mensajeService.crearAvisoCancelacion(c,seleccionada);case COMPLETADA,AUSENTE->mensajeService.crearRecordatorio(c,seleccionada);};whatsAppService.abrirConversacion(c.getTelefono(),m);}catch(RuntimeException e){etiquetaEstadoCarga.setText(e.getMessage());}}
    @FXML private void volver(){detenerReloj();Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());}
    // solicitudes-web-ajuste-final-v1
}
