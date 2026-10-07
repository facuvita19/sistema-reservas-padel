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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
    @FXML private Label etiquetaUsuario, etiquetaRolUsuario, etiquetaRolLateral,
            etiquetaFecha, etiquetaReservasHoy, etiquetaCanchasActivas,
            etiquetaCanchasActivasOperador, etiquetaSolicitudesKpiOperador,
            etiquetaIngresosDia, etiquetaIngresosMes, etiquetaPagosPendientes,
            etiquetaEstadoCarga, etiquetaUltimaActualizacion,
            etiquetaProximoTurno, etiquetaDetalleProximoTurno,
            etiquetaPendientesOperativos, etiquetaDetallePendientes,
            etiquetaResumenAlertas, etiquetaCantidadAlertas,
            etiquetaSeniasOperador, etiquetaTurnosOperador,
            etiquetaSolicitudesOperador, alertaSenias, alertaVencimientos,
            alertaPagos, alertaTurnos, alertaCaja,
            etiquetaNombreComplejo, logoFallback;
    @FXML private ProgressIndicator indicadorCarga;
    @FXML private Button botonActualizar, botonUsuarios,
            botonConfiguracion, botonCanchas, botonBloqueos,
            botonEstadisticas, botonPagosMenu, botonCajaMenu,
            accesoRapidoPagos, accesoRapidoCierreCaja;
    @FXML private VBox grupoAnalisis, grupoSistema, grupoFinanzas,
            panelAlertas, panelRendimientoAdministrador,
            panelEstadoOperador, tarjetaIngresosDia,
            tarjetaCanchasOperador;
    @FXML private VBox filaAlertaSenias, filaAlertaVencimientos,
            filaAlertaPagos, filaAlertaTurnos, filaAlertaCaja;
    @FXML private HBox filaSinAlertas;
    @FXML private GridPane contenedorTarjetasAlertas;
    @FXML private GridPane accionesRapidasAdmin, accionesRapidasOperador;
    @FXML private ImageView logoComplejo;
    @FXML private TableView<Reserva> tablaProximasReservas;
    @FXML private TableColumn<Reserva,LocalDate> columnaFecha;
    @FXML private TableColumn<Reserva,LocalTime> columnaHora;
    @FXML private TableColumn<Reserva,String> columnaCancha,columnaCliente;
    @FXML private TableColumn<Reserva,EstadoReserva> columnaEstado;

    @FXML private void initialize(){mostrarDatosSesion();cargarIdentidad();configurarTabla();aplicarPermisos();contenedorTarjetasAlertas.widthProperty().addListener((o,a,n)->Platform.runLater(()->ajustarAnchoAlertas((int)contenedorTarjetasAlertas.getChildren().stream().filter(Node::isManaged).count())));Platform.runLater(()->{configurarDeseleccionExterna();actualizarDashboard();});}    private void aplicarPermisos() {
        Usuario usuario = Navegacion.getUsuarioActual();
        boolean admin = usuario != null && usuario.esAdministrador();
        controlar(botonUsuarios, admin);
        controlar(botonConfiguracion, admin);
        controlar(botonEstadisticas, admin);
        controlar(grupoAnalisis, admin);
        controlar(grupoSistema, admin);
        controlar(grupoFinanzas, admin);
        controlar(panelRendimientoAdministrador, admin);
        controlar(panelEstadoOperador, !admin);
        controlar(tarjetaIngresosDia, admin);
        controlar(tarjetaCanchasOperador, !admin);
        controlar(accionesRapidasAdmin, admin);
        controlar(accionesRapidasOperador, !admin);
        controlar(botonCanchas, true);
        controlar(botonBloqueos, true);
    }
    private void controlar(Node n,boolean permitido){if(n==null)throw new IllegalStateException("Falta conectar un control de permisos.");n.setVisible(permitido);n.setManaged(permitido);}    private void mostrarDatosSesion() {
        Usuario u = Navegacion.getUsuarioActual();
        boolean admin = u != null && u.esAdministrador();
        etiquetaUsuario.setText(u == null ? "Personal" : u.getNombreUsuario());
        etiquetaRolUsuario.setText(admin ? "ADMINISTRADOR" : "OPERADOR");
        etiquetaRolLateral.setText(admin ? "Administración" : "Operación");
        DateTimeFormatter formato = DateTimeFormatter.ofPattern(
                "EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "AR"));
        etiquetaFecha.setText(capitalizar(LocalDate.now().format(formato)));
    }
    private void cargarIdentidad(){ConfiguracionComplejo c=Navegacion.getConfiguracionActual();String nombre=c==null||c.getNombreComercial()==null||c.getNombreComercial().isBlank()?"Padel Reservas":c.getNombreComercial();etiquetaNombreComplejo.setText(nombre);if(c==null||c.getRutaLogo()==null||c.getRutaLogo().isBlank())return;File f=new File(c.getRutaLogo());if(!f.isFile())return;try{Image i=new Image(f.toURI().toString(),false);if(!i.isError()){logoComplejo.setImage(i);logoFallback.setVisible(false);logoFallback.setManaged(false);}}catch(RuntimeException ignored){}}    private void configurarTabla() {
        columnaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columnaFecha.setCellFactory(x -> celdaCentradaFecha());
        columnaHora.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        columnaHora.setCellFactory(x -> celdaCentradaHora());
        columnaCancha.setCellValueFactory(new PropertyValueFactory<>("nombreCancha"));
        columnaCancha.setCellFactory(x -> celdaTextoCentrada());
        columnaCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        columnaCliente.setCellFactory(x -> celdaTextoIzquierda());
        columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        columnaEstado.setCellFactory(x -> celdaEstadoReserva());

        tablaProximasReservas.setRowFactory(tabla -> {
            var fila = new javafx.scene.control.TableRow<Reserva>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getButton() == MouseButton.PRIMARY
                        && evento.getClickCount() == 2
                        && !fila.isEmpty()) {
                    abrirReservaSeleccionada(fila.getItem());
                }
            });
            return fila;
        });

        vincularColumna(columnaFecha, 0.13);
        vincularColumna(columnaHora, 0.10);
        vincularColumna(columnaCancha, 0.19);
        vincularColumna(columnaCliente, 0.34);
        vincularColumna(columnaEstado, 0.21);
    }

    private TableCell<Reserva, LocalDate> celdaCentradaFecha() {
        return new TableCell<>() {
            @Override protected void updateItem(LocalDate valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(javafx.geometry.Pos.CENTER);
                setText(vacia || valor == null ? null : valor.format(FECHA));
            }
        };
    }

    private TableCell<Reserva, LocalTime> celdaCentradaHora() {
        return new TableCell<>() {
            @Override protected void updateItem(LocalTime valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(javafx.geometry.Pos.CENTER);
                setText(vacia || valor == null ? null : valor.format(HORA));
            }
        };
    }

    private TableCell<Reserva, String> celdaTextoIzquierda() {
        return new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setText(vacia || valor == null ? null : valor);
            }
        };
    }

    private TableCell<Reserva, String> celdaTextoCentrada() {
        return new TableCell<>() {
            @Override protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setAlignment(javafx.geometry.Pos.CENTER);
                setText(vacia || valor == null ? null : valor);
            }
        };
    }

    private TableCell<Reserva, EstadoReserva> celdaEstadoReserva() {
        return new TableCell<>() {
            {
                setAlignment(javafx.geometry.Pos.CENTER);
                setContentDisplay(javafx.scene.control.ContentDisplay.TEXT_ONLY);
                setGraphic(null);
                setSnapToPixel(true);
                setCache(false);
            }

            @Override
            protected void updateItem(EstadoReserva estado, boolean vacia) {
                super.updateItem(estado, vacia);
                getStyleClass().removeAll(
                        "dashboard-state-cell-pending-v6",
                        "dashboard-state-cell-confirmed-v6",
                        "dashboard-state-cell-neutral-v6");
                setGraphic(null);

                if (vacia || estado == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(estado.toString());
                getStyleClass().add(
                        estado == EstadoReserva.PENDIENTE
                                ? "dashboard-state-cell-pending-v6"
                                : "dashboard-state-cell-confirmed-v6");
            }
        };
    }

    private void vincularColumna(TableColumn<Reserva, ?> columna, double proporcion) {
        columna.prefWidthProperty().bind(
                javafx.beans.binding.Bindings.createDoubleBinding(
                        () -> Math.floor((tablaProximasReservas.getWidth() - 16.0)
                                * proporcion),
                        tablaProximasReservas.widthProperty()));
        columna.setResizable(false);
        columna.setReorderable(false);
    }
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
    @FXML public void actualizarDashboard(){cambiarEstadoCarga(true,"Actualizando información...");Task<DatosDashboard> t=new Task<>(){@Override protected DatosDashboard call(){return new DatosDashboard(dashboardService.obtenerResumen(),dashboardService.listarProximasReservas());}};t.setOnSucceeded(e->{DatosDashboard d=t.getValue();mostrarResumen(d.resumen(),d.proximas());tablaProximasReservas.getItems().setAll(d.proximas().stream().limit(5).toList());cambiarEstadoCarga(false,"Datos actualizados correctamente.");etiquetaUltimaActualizacion.setText("Actualizado " + LocalTime.now().format(ACTUALIZACION));});t.setOnFailed(e->{Throwable x=t.getException();if(x!=null)x.printStackTrace();cambiarEstadoCarga(false,x==null||x.getMessage()==null?"No se pudo actualizar el inicio.":x.getMessage());});Thread h=new Thread(t,"dashboard-padel-reservas");h.setDaemon(true);h.start();}
    private void mostrarResumen(ResumenDashboard resumen, List<Reserva> proximas) {
        Usuario usuario = Navegacion.getUsuarioActual();
        boolean admin = usuario != null && usuario.esAdministrador();
        etiquetaReservasHoy.setText(String.valueOf(resumen.getReservasHoy()));
        etiquetaCanchasActivas.setText(String.valueOf(resumen.getCanchasActivas()));
        etiquetaCanchasActivasOperador.setText(String.valueOf(resumen.getCanchasActivas()));
        etiquetaSolicitudesKpiOperador.setText(String.valueOf(resumen.getReservasProximasAVencer()));
        etiquetaSolicitudesOperador.setText(String.valueOf(resumen.getReservasProximasAVencer()));
        etiquetaIngresosDia.setText(moneda(resumen.getIngresosDia()));
        etiquetaIngresosMes.setText(moneda(resumen.getIngresosMes()));
        etiquetaPagosPendientes.setText(String.valueOf(resumen.getPagosPendientes()));
        mostrarProximoTurno(proximas);
        int pendientes = resumen.getReservasPendientesSenia()
                + resumen.getTurnosPendientesCierre()
                + resumen.getReservasProximasAVencer();
        if (admin) {
            pendientes += resumen.getPagosPendientes()
                    + (resumen.isCajaCerradaHoy() ? 0 : 1);
        }
        etiquetaPendientesOperativos.setText(String.valueOf(pendientes));
        etiquetaDetallePendientes.setText(detallePendientes(resumen, admin));
        etiquetaSeniasOperador.setText(String.valueOf(resumen.getReservasPendientesSenia()));
        etiquetaTurnosOperador.setText(String.valueOf(resumen.getTurnosPendientesCierre()));
        mostrarAlertas(resumen, admin);
    }
    private void mostrarProximoTurno(List<Reserva> ps){if(ps==null||ps.isEmpty()){etiquetaProximoTurno.setText("Sin turnos");etiquetaDetalleProximoTurno.setText("No hay reservas próximas");return;}Reserva p=ps.get(0);etiquetaProximoTurno.setText(p.getHoraInicio().format(HORA));String fecha=p.getFecha().equals(LocalDate.now())?"Hoy":p.getFecha().format(FECHA);etiquetaDetalleProximoTurno.setText(fecha+" · "+p.getNombreCancha()+" · "+p.getNombreCliente());}    private String detallePendientes(ResumenDashboard resumen, boolean admin) {
        List<String> detalles = new java.util.ArrayList<>();
        if (resumen.getReservasPendientesSenia() > 0)
            detalles.add(resumen.getReservasPendientesSenia() + " "
                    + plural(resumen.getReservasPendientesSenia(), "seña", "señas"));
        if (resumen.getReservasProximasAVencer() > 0)
            detalles.add(resumen.getReservasProximasAVencer() + " web");
        if (resumen.getTurnosPendientesCierre() > 0)
            detalles.add(resumen.getTurnosPendientesCierre() + " "
                    + plural(resumen.getTurnosPendientesCierre(), "cierre", "cierres"));
        if (admin && resumen.getPagosPendientes() > 0)
            detalles.add(resumen.getPagosPendientes() + " "
                    + plural(resumen.getPagosPendientes(), "pago", "pagos"));
        if (admin && !resumen.isCajaCerradaHoy()) detalles.add("1 caja");
        return detalles.isEmpty() ? "Sin acciones pendientes" : String.join(" · ", detalles);
    }
    private void mostrarAlertas(
            ResumenDashboard resumen, boolean administrador) {
        int cantidad = 0;
        cantidad += activar(filaAlertaSenias,
                resumen.getReservasPendientesSenia() > 0);
        cantidad += activar(filaAlertaVencimientos,
                resumen.getReservasProximasAVencer() > 0);
        cantidad += activar(filaAlertaPagos,
                administrador && resumen.getPagosPendientes() > 0);
        cantidad += activar(filaAlertaTurnos,
                resumen.getTurnosPendientesCierre() > 0);
        cantidad += activar(filaAlertaCaja,
                administrador && !resumen.isCajaCerradaHoy());

        activar(filaSinAlertas, cantidad == 0);
        alertaSenias.setText(textoCantidad(
                resumen.getReservasPendientesSenia(),
                "reserva esperando seña", "reservas esperando seña"));
        alertaVencimientos.setText(textoCantidad(
                resumen.getReservasProximasAVencer(),
                "solicitud web vence en menos de 5 minutos",
                "solicitudes web vencen en menos de 5 minutos"));
        alertaPagos.setText(textoCantidad(
                resumen.getPagosPendientes(),
                "pago pendiente de acreditar",
                "pagos pendientes de acreditar"));
        alertaTurnos.setText(textoCantidad(
                resumen.getTurnosPendientesCierre(),
                "turno pendiente de cerrar",
                "turnos pendientes de cerrar"));
        alertaCaja.setText("Caja del día pendiente de cierre");
        etiquetaCantidadAlertas.setText(textoCantidad(
                cantidad, "tipo de alerta", "tipos de alerta"));
        etiquetaResumenAlertas.setText(cantidad == 0
                ? "La operación no requiere acciones urgentes"
                : "Revisá los temas que requieren intervención");

        distribuirTarjetasAlertasEnGrilla();
    }

    private void ajustarAnchoAlertas(
            int cantidadVisible) {
        distribuirTarjetasAlertasEnGrilla();
    }
    private void distribuirTarjetasAlertasEnGrilla() {
        List<VBox> visibles = List.of(
                filaAlertaSenias,
                filaAlertaVencimientos,
                filaAlertaPagos,
                filaAlertaTurnos,
                filaAlertaCaja).stream()
                .filter(Node::isManaged)
                .toList();

        contenedorTarjetasAlertas.getChildren().clear();
        contenedorTarjetasAlertas.getColumnConstraints().clear();
        contenedorTarjetasAlertas.getRowConstraints().clear();

        if (visibles.isEmpty()) return;

        int columnas = visibles.size() <= 3 ? visibles.size() : 3;
        double porcentaje = 100.0 / columnas;
        for (int indice = 0; indice < columnas; indice++) {
            ColumnConstraints restriccion = new ColumnConstraints();
            restriccion.setPercentWidth(porcentaje);
            restriccion.setHgrow(Priority.ALWAYS);
            restriccion.setFillWidth(true);
            contenedorTarjetasAlertas.getColumnConstraints().add(restriccion);
        }

        for (int indice = 0; indice < visibles.size(); indice++) {
            VBox tarjeta = visibles.get(indice);
            int fila = indice / columnas;
            int columna = indice % columnas;
            tarjeta.setMinWidth(0);
            tarjeta.setPrefWidth(-1);
            tarjeta.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(tarjeta, Priority.ALWAYS);
            GridPane.setFillWidth(tarjeta, true);
            contenedorTarjetasAlertas.add(tarjeta, columna, fila);
        }
    }

    private int activar(Node n,boolean activo){n.setVisible(activo);n.setManaged(activo);return activo?1:0;}
    private String textoCantidad(int n,String singular,String plural){return n+" "+(n==1?singular:plural);}
    private String plural(int n,String singular,String plural){return n==1?singular:plural;}
    private String moneda(BigDecimal v){return NumberFormat.getCurrencyInstance(new Locale("es","AR")).format(v==null?BigDecimal.ZERO:v.setScale(2,RoundingMode.HALF_UP));}
    private void cambiarEstadoCarga(boolean c,String m){indicadorCarga.setVisible(c);botonActualizar.setDisable(c);etiquetaEstadoCarga.setText(m);etiquetaEstadoCarga.getStyleClass().removeAll("dashboard-status-ok","dashboard-status-error");if(!c)etiquetaEstadoCarga.getStyleClass().add(m!=null&&(m.toLowerCase(Locale.ROOT).contains("no se pudo")||m.toLowerCase(Locale.ROOT).contains("error"))?"dashboard-status-error":"dashboard-status-ok");}
    @FXML private void verEsperandoSenia(){Navegacion.mostrarReservasConFiltro(FiltroReservas.PENDIENTES_SENIA);}@FXML private void verPagosPendientes(){Navegacion.mostrarPagosConFiltro(FiltroPagos.PENDIENTES_ACREDITACION);}@FXML private void verTurnosPendientes(){Navegacion.mostrarReservasConFiltro(FiltroReservas.PENDIENTES_CIERRE);}@FXML private void abrirSolicitudesWeb(){Navegacion.mostrarSolicitudesWeb();}@FXML private void abrirTorneos(){Navegacion.mostrarTorneos();}@FXML private void abrirTorneosInscripciones(){Navegacion.mostrarTorneosInscripciones();}@FXML private void abrirCanchas(){Navegacion.mostrarCanchas();}@FXML private void abrirClientes(){Navegacion.mostrarClientes();}@FXML private void abrirAgenda(){Navegacion.mostrarAgenda();}@FXML private void abrirReservas(){Navegacion.mostrarReservas();}@FXML private void abrirPagos(){if(esAdministradorActual())Navegacion.mostrarPagos();else cambiarEstadoCarga(false,"Acceso financiero restringido.");}@FXML private void abrirCierreCaja(){if(esAdministradorActual())Navegacion.mostrarCierreCaja();else cambiarEstadoCarga(false,"Acceso financiero restringido.");}@FXML private void abrirBloqueos(){Navegacion.mostrarBloqueos();}@FXML private void abrirEstadisticas(){Navegacion.mostrarEstadisticas();}@FXML private void abrirConfiguracion(){Navegacion.mostrarConfiguracion();}@FXML private void abrirUsuarios(){Navegacion.mostrarUsuarios();}@FXML private void cerrarSesion(){Navegacion.cerrarSesion();}
    private boolean esAdministradorActual(){Usuario u=Navegacion.getUsuarioActual();return u!=null&&u.esAdministrador();}
    private String capitalizar(String t){return t==null||t.isBlank()?"":Character.toUpperCase(t.charAt(0))+t.substring(1);}
    private record DatosDashboard(ResumenDashboard resumen,List<Reserva> proximas){}
}
