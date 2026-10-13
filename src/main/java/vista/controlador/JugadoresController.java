package vista.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.layout.VBox;
import servicio.ConsultaJugadoresService;
import servicio.IdentidadDeportivaService;
import negocio.EstadoJugadorCategoria;
import negocio.OrigenJugadorCategoria;
import vista.Dialogos;
import servicio.ConsultaJugadoresService.CategoriaHistorial;
import servicio.ConsultaJugadoresService.CategoriaResumen;
import servicio.ConsultaJugadoresService.JugadorResumen;
import vista.Navegacion;

public class JugadoresController {
    private static Long perfilSolicitado;
    public static void preseleccionarPerfil(Long perfilId) {
        perfilSolicitado = perfilId != null && perfilId > 0 ? perfilId : null;
    }
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ConsultaJugadoresService service = new ConsultaJugadoresService();
    private final IdentidadDeportivaService identidadService = new IdentidadDeportivaService();
    private final ObservableList<JugadorResumen> jugadores = FXCollections.observableArrayList();
    private final ObservableList<CategoriaHistorial> historial = FXCollections.observableArrayList();
    private final ObservableList<servicio.ConsultaHistorialCompetitivoService.Partido> historialCompetitivo = FXCollections.observableArrayList();
    private final servicio.ConsultaHistorialCompetitivoService historialCompetitivoService = new servicio.ConsultaHistorialCompetitivoService();
    private FilteredList<JugadorResumen> filtrados;
    private boolean cargaEnCurso;
    private boolean categoriasCargadas;
    private Long perfilAReseleccionar = consumirPerfilSolicitado();
    private boolean operacionEnCurso;
    private static Long consumirPerfilSolicitado() {
        Long solicitado = perfilSolicitado;
        perfilSolicitado = null;
        return solicitado;
    }

    @FXML private TextField campoBuscar;
    @FXML private Button botonActualizar, botonVincular, botonEstadoPerfil, botonMarcarRevisado, botonAsignarCategoria, botonSuspenderCategoria, botonFinalizarCategoria;
    @FXML private ComboBox<String> filtroIdentidad, filtroEstado;
    @FXML private ComboBox<CategoriaResumen> filtroCategoria;
    @FXML private Label etiquetaResultados, etiquetaRevisionResumen, etiquetaEstado, etiquetaNombre, etiquetaIdentidad, etiquetaRevisionPerfil,
            etiquetaCategoria, etiquetaCategoriaDesde, etiquetaPosicion, etiquetaVinculacion,
            etiquetaDocumento, etiquetaTelefono, etiquetaEmail, etiquetaDisponibilidad,
            etiquetaInvitaciones, etiquetaHistorialVacio;
    @FXML private Label etiquetaHistorialAyuda, etiquetaCompetitivoVacio, etiquetaPartidosCompetitivos, etiquetaVictoriasCompetitivas, etiquetaDerrotasCompetitivas, etiquetaEfectividadCompetitiva;
    @FXML private TableView<servicio.ConsultaHistorialCompetitivoService.Partido> tablaHistorialCompetitivo;
    @FXML private TableColumn<servicio.ConsultaHistorialCompetitivoService.Partido,String> columnaCompetitivaFecha, columnaCompetitivaTorneo, columnaCompetitivaFase, columnaCompetitivaPareja, columnaCompetitivaRivales, columnaCompetitivaResultado;
    @FXML private VBox panelEstadoVacio, panelDetalleJugador;
    @FXML private ToggleButton botonTodos, botonConCategoria, botonSinCategoria;
    @FXML private TableView<JugadorResumen> tablaJugadores;
    @FXML private TableColumn<JugadorResumen,String> columnaJugador, columnaCategoria,
            columnaIdentidad, columnaPosicion, columnaEstado;
    @FXML private TableView<CategoriaHistorial> tablaHistorial;
    @FXML private TableColumn<CategoriaHistorial,String> columnaHistorialCategoria,
            columnaHistorialPeriodo, columnaHistorialEstado, columnaHistorialOrigen,
            columnaHistorialMotivo;

    @FXML private void initialize() {
        configurarTabla(); configurarHistorial(); configurarHistorialCompetitivo(); configurarFiltros(); aplicarPermisosOperaciones(); limpiarDetalle(); cargar();
    }

    private void configurarTabla() {
        tablaJugadores.setFixedCellSize(54);
        tablaJugadores.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        for (TableColumn<JugadorResumen, ?> columna : List.of(columnaJugador, columnaCategoria, columnaIdentidad, columnaPosicion, columnaEstado)) { columna.setReorderable(false); }
        vincularColumna(columnaJugador, 0.29);
        vincularColumna(columnaCategoria, 0.19);
        vincularColumna(columnaIdentidad, 0.19);
        vincularColumna(columnaPosicion, 0.16);
        vincularColumna(columnaEstado, 0.17);
        columnaJugador.setCellFactory(columna -> celdaTextoConTooltip());
        columnaJugador.setCellValueFactory(d -> texto(d.getValue().nombre()));
        columnaCategoria.setCellValueFactory(d -> texto(valor(d.getValue().categoria(), "Sin asignar")));
        columnaIdentidad.setCellValueFactory(d -> texto(d.getValue().identidad()==negocio.EstadoPerfilJugador.DEFINITIVO ? "Vinculado" : "Provisional"));
        columnaPosicion.setCellValueFactory(d -> texto(nombrePosicion(d.getValue().posicion())));
        columnaEstado.setCellValueFactory(d -> texto(d.getValue().activo() ? "Activo" : "Inactivo"));
        columnaCategoria.setCellFactory(columna -> celdaCategoria());
        columnaIdentidad.setCellFactory(c -> badgeJugador("player-identity-cell"));
        columnaEstado.setCellFactory(c -> badgeJugador("player-state-cell"));
        tablaJugadores.getSelectionModel().selectedItemProperty().addListener((o,a,n)->mostrar(n));
    }

    private void configurarHistorialCompetitivo() {
        tablaHistorialCompetitivo.setItems(historialCompetitivo);
        columnaCompetitivaFecha.setCellValueFactory(d -> texto(d.getValue().fecha()==null?"-":d.getValue().fecha().format(FECHA)));
        columnaCompetitivaTorneo.setCellValueFactory(d -> texto(d.getValue().torneo()+" · "+d.getValue().categoria()));
        columnaCompetitivaFase.setCellValueFactory(d -> texto(d.getValue().fase()));
        columnaCompetitivaPareja.setCellValueFactory(d -> texto(valor(d.getValue().companero(),"Sin compañero")));
        columnaCompetitivaRivales.setCellValueFactory(d -> texto(valor(d.getValue().rivales(),"Sin rivales")));
        columnaCompetitivaResultado.setCellValueFactory(d -> texto(("GANADOR".equals(d.getValue().resultado())?"VICTORIA":"DERROTA")+" · "+valor(d.getValue().marcador(),"Sin marcador")));
        tablaHistorialCompetitivo.setFixedCellSize(48);
    }

    private void configurarHistorial() {
        tablaHistorial.setItems(historial);
        configurarGeometriaHistorial();
        columnaHistorialCategoria.setCellValueFactory(d -> texto(d.getValue().categoria()));
        columnaHistorialCategoria.setCellFactory(columna -> celdaCategoriaHistorial());
        columnaHistorialPeriodo.setCellValueFactory(d -> texto(periodo(d.getValue())));
        columnaHistorialEstado.setCellValueFactory(d -> texto(d.getValue().estado()));
        columnaHistorialOrigen.setCellValueFactory(d -> texto(capitalizarEnum(d.getValue().origen())));
        columnaHistorialMotivo.setCellValueFactory(d -> texto(d.getValue().motivo()));
        columnaHistorialMotivo.setCellFactory(columna -> celdaHistorialConTooltip());
        tablaHistorial.setFixedCellSize(48);
        historial.addListener((javafx.collections.ListChangeListener<CategoriaHistorial>) cambio -> ajustarAltoHistorial());
        columnaHistorialEstado.setCellFactory(c -> badgeHistorial("player-history-state"));
    }

    private void configurarFiltros() {
        filtrados = new FilteredList<>(jugadores, j -> true); tablaJugadores.setItems(filtrados);
        filtroIdentidad.getItems().setAll("Todas las vinculaciones", "Vinculados a clientes",
                "Provisionales sin vincular", "Revisión pendiente"); filtroIdentidad.setValue("Todas las vinculaciones");
        filtroEstado.getItems().setAll("Todos los estados", "Activos", "Inactivos"); filtroEstado.setValue("Todos los estados");
        campoBuscar.textProperty().addListener((o,a,n)->filtrar());
        filtroIdentidad.valueProperty().addListener((o,a,n)->filtrar());
        filtroEstado.valueProperty().addListener((o,a,n)->filtrar());
        filtroCategoria.valueProperty().addListener((o,a,n)->filtrar());
        filtroCategoria.setVisibleRowCount(8);
        filtroCategoria.setMaxWidth(Double.MAX_VALUE);
        botonTodos.setSelected(true);
    }

    @FXML private void cargar() {
        if (cargaEnCurso) return;
        cargaEnCurso = true;
        botonActualizar.setDisable(true);
        estado("Cargando identidades deportivas...", false);
        Task<Datos> tarea = new Task<>() { @Override protected Datos call() {
            return new Datos(service.listar(), categoriasCargadas ? List.of() : service.listarCategorias()); }};
        tarea.setOnSucceeded(e->{jugadores.setAll(tarea.getValue().jugadores());
            if (!categoriasCargadas) {
                filtroCategoria.getItems().setAll(tarea.getValue().categorias());
                categoriasCargadas = true;
            }
            filtrar(); estado("Actualizado correctamente", false); reSeleccionarPendiente(); ocultarEstadoLuego(); cargaEnCurso=false; botonActualizar.setDisable(false);});
        tarea.setOnFailed(e->{Throwable x=tarea.getException(); if(x!=null)x.printStackTrace(); estado(x==null?"No se pudieron cargar los jugadores.":x.getMessage(), true); cargaEnCurso=false; botonActualizar.setDisable(false);});
        Thread hilo=new Thread(tarea,"jugadores-w2"); hilo.setDaemon(true); hilo.start();
    }

    private void filtrar() {
        String q=campoBuscar.getText()==null?"":campoBuscar.getText().trim().toLowerCase(Locale.ROOT);
        filtrados.setPredicate(j->{boolean texto=q.isBlank()||contiene(j.nombre(),q)||contiene(j.documento(),q)||contiene(j.telefono(),q)||contiene(j.email(),q);
            boolean identidad="Todas las vinculaciones".equals(filtroIdentidad.getValue())
                    ||("Vinculados a clientes".equals(filtroIdentidad.getValue())&&j.clienteId()!=null)
                    ||("Provisionales sin vincular".equals(filtroIdentidad.getValue())&&j.clienteId()==null)
                    ||("Revisión pendiente".equals(filtroIdentidad.getValue())&&j.requiereRevision());
            boolean estado="Todos los estados".equals(filtroEstado.getValue())||("Activos".equals(filtroEstado.getValue())&&j.activo())||("Inactivos".equals(filtroEstado.getValue())&&!j.activo());
            boolean categoria=filtroCategoria.getValue()==null||(j.categoriaId()!=null&&j.categoriaId()==filtroCategoria.getValue().id());
            boolean alcance=botonTodos.isSelected()||(botonConCategoria.isSelected()&&j.categoriaId()!=null)||(botonSinCategoria.isSelected()&&j.categoriaId()==null);
            return texto&&identidad&&estado&&categoria&&alcance;});
        etiquetaResultados.setText(filtrados.size()+" "+(filtrados.size()==1?"jugador visible":"jugadores visibles"));
        long pendientes=jugadores.stream().filter(JugadorResumen::requiereRevision).count();
        etiquetaRevisionResumen.setText(pendientes==0?"IDENTIDADES AL DÍA":pendientes+" PENDIENTES DE REVISIÓN");
        etiquetaRevisionResumen.getStyleClass().removeAll("players-review-ok","players-review-pending");
        etiquetaRevisionResumen.getStyleClass().add(pendientes==0?"players-review-ok":"players-review-pending");
    }

    @FXML private void mostrarTodos(){seleccionarAlcance(botonTodos);}
    @FXML private void mostrarConCategoria(){seleccionarAlcance(botonConCategoria);}
    @FXML private void mostrarSinCategoria(){seleccionarAlcance(botonSinCategoria);}
    private void seleccionarAlcance(ToggleButton activo){for(ToggleButton b:List.of(botonTodos,botonConCategoria,botonSinCategoria))b.setSelected(b==activo);filtrar();}
    @FXML private void limpiarFiltros(){campoBuscar.clear();filtroIdentidad.setValue("Todas las vinculaciones");filtroEstado.setValue("Todos los estados");filtroCategoria.setValue(null);seleccionarAlcance(botonTodos);tablaJugadores.getSelectionModel().clearSelection();limpiarDetalle();campoBuscar.requestFocus();}

    private void aplicarPermisosOperaciones(){boolean admin=Navegacion.getUsuarioActual()!=null&&Navegacion.getUsuarioActual().esAdministrador();for(Button b:List.of(botonVincular,botonEstadoPerfil,botonSuspenderCategoria,botonFinalizarCategoria)){b.setVisible(admin);b.setManaged(admin);}}

    private void mostrar(JugadorResumen j) {
        if(j==null){limpiarDetalle();return;}
        alternarDetalle(true);
        etiquetaNombre.setText(j.nombre());
        etiquetaIdentidad.setText(j.identidad()==negocio.EstadoPerfilJugador.DEFINITIVO?"VINCULADO A CLIENTE":"PERFIL PROVISIONAL ACTIVO");
        etiquetaRevisionPerfil.setText(j.estadoRevision());
        etiquetaRevisionPerfil.getStyleClass().removeAll("players-review-ok","players-review-pending");
        etiquetaRevisionPerfil.getStyleClass().add(j.requiereRevision()?"players-review-pending":"players-review-ok");
        etiquetaCategoria.setText(valor(j.categoria(),"SIN CATEGORÍA"));
        etiquetaCategoriaDesde.setText(j.categoriaDesde()==null?"Pendiente de evaluación":"Vigente desde "+j.categoriaDesde().format(FECHA));
        etiquetaPosicion.setText(nombrePosicion(j.posicion()));
        etiquetaVinculacion.setText(j.clienteId()==null?"Activo para competir · pendiente de vincular con un cliente":"Ficha de cliente asociada");
        etiquetaDocumento.setText(valor(j.documento(),"No disponible")); etiquetaTelefono.setText(valor(j.telefono(),"No disponible")); etiquetaEmail.setText(valor(j.email(),"No disponible"));
        aplicarTooltip(etiquetaNombre, j.nombre()); aplicarTooltip(etiquetaDocumento, j.documento()); aplicarTooltip(etiquetaTelefono, j.telefono()); aplicarTooltip(etiquetaEmail, j.email());
        etiquetaDisponibilidad.setText(j.disponible()?"Disponible para jugar":"No disponible para jugar"); etiquetaInvitaciones.setText(j.aceptaInvitaciones()?"Acepta invitaciones":"No acepta invitaciones");
        actualizarAcciones(j);
        cargarHistorial(j.id());
        cargarHistorialCompetitivo(j.id());
    }

    private void cargarHistorialCompetitivo(long id) {
        historialCompetitivo.clear(); etiquetaCompetitivoVacio.setText("Cargando actividad competitiva...");
        javafx.concurrent.Task<servicio.ConsultaHistorialCompetitivoService.Resumen> tarea=new javafx.concurrent.Task<>(){@Override protected servicio.ConsultaHistorialCompetitivoService.Resumen call(){return historialCompetitivoService.consultar(id);}};
        tarea.setOnSucceeded(e->{var r=tarea.getValue();historialCompetitivo.setAll(r.historial());etiquetaPartidosCompetitivos.setText(String.valueOf(r.partidos()));etiquetaVictoriasCompetitivas.setText(String.valueOf(r.victorias()));etiquetaDerrotasCompetitivas.setText(String.valueOf(r.derrotas()));etiquetaEfectividadCompetitiva.setText(r.efectividad()+"%");boolean vacio=r.historial().isEmpty();tablaHistorialCompetitivo.setVisible(!vacio);tablaHistorialCompetitivo.setManaged(!vacio);etiquetaCompetitivoVacio.setVisible(vacio);etiquetaCompetitivoVacio.setManaged(vacio);etiquetaCompetitivoVacio.setText(vacio?"Este jugador todavía no registra partidos finalizados.":"");});
        tarea.setOnFailed(e->{etiquetaCompetitivoVacio.setText("No se pudo cargar el historial competitivo.");tablaHistorialCompetitivo.setVisible(false);tablaHistorialCompetitivo.setManaged(false);});
        Thread hilo=new Thread(tarea,"historial-competitivo-"+id);hilo.setDaemon(true);hilo.start();
    }

    private void cargarHistorial(long id) {
        historial.clear(); etiquetaHistorialVacio.setText("Cargando historial..."); alternarHistorial(false);
        Task<List<CategoriaHistorial>> tarea=new Task<>(){@Override protected List<CategoriaHistorial> call(){return service.historial(id);}};
        tarea.setOnSucceeded(e->{historial.setAll(tarea.getValue()); boolean vacio=historial.isEmpty(); etiquetaHistorialVacio.setText(vacio?"Este jugador todavía no tiene movimientos de categoría.":""); alternarHistorial(!vacio);});
        tarea.setOnFailed(e->{etiquetaHistorialVacio.setText("No se pudo cargar el historial.");alternarHistorial(false);});
        Thread hilo=new Thread(tarea,"historial-jugador-"+id);hilo.setDaemon(true);hilo.start();
    }

    private void actualizarAcciones(JugadorResumen j){boolean admin=Navegacion.getUsuarioActual()!=null&&Navegacion.getUsuarioActual().esAdministrador();boolean sinFicha=j.clienteId()==null;botonVincular.setVisible(admin&&sinFicha);botonVincular.setManaged(admin&&sinFicha);botonVincular.setDisable(!admin||!sinFicha);botonVincular.setText("ASOCIAR FICHA");botonMarcarRevisado.setVisible(admin&&j.clienteId()!=null&&!j.vinculacionRevisada());botonMarcarRevisado.setManaged(admin&&j.clienteId()!=null&&!j.vinculacionRevisada());botonMarcarRevisado.setDisable(!admin||j.clienteId()==null||j.vinculacionRevisada());botonEstadoPerfil.setDisable(!admin);botonEstadoPerfil.setText(j.activo()?"DESACTIVAR PERFIL":"ACTIVAR PERFIL");botonAsignarCategoria.setText(j.categoriaId()==null?"ASIGNAR CATEGORÍA":"CAMBIAR CATEGORÍA");boolean conCategoria=j.categoriaId()!=null;botonSuspenderCategoria.setVisible(admin&&conCategoria);botonSuspenderCategoria.setManaged(admin&&conCategoria);botonSuspenderCategoria.setDisable(!admin||!conCategoria);botonFinalizarCategoria.setVisible(admin&&conCategoria);botonFinalizarCategoria.setManaged(admin&&conCategoria);botonFinalizarCategoria.setDisable(!admin||!conCategoria);}
    private void limpiarDetalle(){alternarDetalle(false);historial.clear();historialCompetitivo.clear();etiquetaHistorialVacio.setText("");alternarHistorial(false);}
    private void alternarDetalle(boolean seleccionado){if(panelEstadoVacio!=null){panelEstadoVacio.setVisible(!seleccionado);panelEstadoVacio.setManaged(!seleccionado);}if(panelDetalleJugador!=null){panelDetalleJugador.setVisible(seleccionado);panelDetalleJugador.setManaged(seleccionado);}}
    private TableCell<JugadorResumen,String> badgeJugador(String clase){return celdaInsignia(clase);}
    private TableCell<CategoriaHistorial,String> badgeHistorial(String clase){return celdaInsignia(clase);}
    private <T> TableCell<T,String> celdaInsignia(String clase){return new TableCell<T,String>(){private final Label insignia=new Label();{insignia.getStyleClass().add(clase);setText(null);setAlignment(javafx.geometry.Pos.CENTER);}@Override protected void updateItem(String v,boolean vacia){super.updateItem(v,vacia);insignia.getStyleClass().removeIf(x->x.startsWith(clase+"-")&&!x.equals(clase));if(vacia||v==null){setGraphic(null);return;}insignia.setText(v.toUpperCase(Locale.ROOT));insignia.getStyleClass().add(clase+"-"+v.toLowerCase(Locale.ROOT).replace(' ','-'));setGraphic(insignia);}};}
    private void vincularColumna(TableColumn<JugadorResumen, ?> columna,double proporcion){columna.prefWidthProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(() -> Math.max(68, Math.floor(Math.max(0, tablaJugadores.getWidth()-28)*proporcion)),tablaJugadores.widthProperty()));columna.setMinWidth(68);columna.setMaxWidth(Double.MAX_VALUE);columna.setResizable(false);columna.setReorderable(false);}
    private TableCell<JugadorResumen,String> celdaTextoConTooltip(){return new TableCell<>(){@Override protected void updateItem(String valor,boolean vacia){super.updateItem(valor,vacia);setText(vacia||valor==null?null:valor);setTooltip(vacia||valor==null||valor.isBlank()?null:new Tooltip(valor));setAlignment(javafx.geometry.Pos.CENTER_LEFT);}};}
    private String nombrePosicion(negocio.PosicionJugador posicion){if(posicion==null)return "SIN DEFINIR";return posicion==negocio.PosicionJugador.REVES?"REVÉS":"DRIVE";}
    private void aplicarTooltip(Label etiqueta,String valor){etiqueta.setTooltip(valor==null||valor.isBlank()?null:new Tooltip(valor));}
    private void alternarHistorial(boolean mostrar){tablaHistorial.setVisible(mostrar);tablaHistorial.setManaged(mostrar);etiquetaHistorialVacio.setVisible(!mostrar);etiquetaHistorialVacio.setManaged(!mostrar);if(etiquetaHistorialAyuda!=null){etiquetaHistorialAyuda.setVisible(mostrar);etiquetaHistorialAyuda.setManaged(mostrar);}ajustarAltoHistorial();}
    private void cerrarW21B1Perfecto(){}
    private TableCell<JugadorResumen,String> celdaCategoria(){return new TableCell<>(){private final Label insignia=new Label();{insignia.getStyleClass().add("player-category-cell");setAlignment(javafx.geometry.Pos.CENTER);}@Override protected void updateItem(String valor,boolean vacia){super.updateItem(valor,vacia);setText(null);if(vacia||valor==null){setGraphic(null);return;}insignia.setText(valor.toUpperCase(Locale.ROOT));JugadorResumen jugador=getTableRow()==null?null:getTableRow().getItem();String color=jugador==null?null:jugador.categoriaColor();if(jugador==null||jugador.categoriaId()==null||color==null){insignia.setStyle("");insignia.getStyleClass().remove("player-category-assigned");insignia.getStyleClass().add("player-category-cell-sin-asignar");}else{insignia.getStyleClass().remove("player-category-cell-sin-asignar");if(!insignia.getStyleClass().contains("player-category-assigned"))insignia.getStyleClass().add("player-category-assigned");insignia.setStyle("");}setGraphic(insignia);}};}
    private void configurarGeometriaHistorial(){
        tablaHistorial.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        vincularColumnaHistorial(columnaHistorialCategoria,.13,82);
        vincularColumnaHistorial(columnaHistorialPeriodo,.24,150);
        vincularColumnaHistorial(columnaHistorialEstado,.16,102);
        vincularColumnaHistorial(columnaHistorialOrigen,.17,112);
        vincularColumnaHistorial(columnaHistorialMotivo,.30,175);
    }
    private void vincularColumnaHistorial(TableColumn<CategoriaHistorial,?> columna,double proporcion,double minimo){
        columna.prefWidthProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
                () -> Math.max(minimo,Math.floor(Math.max(0,tablaHistorial.getWidth()-16)*proporcion)),
                tablaHistorial.widthProperty()));
        columna.setMinWidth(minimo);columna.setResizable(false);columna.setReorderable(false);
    }
    private TableCell<CategoriaHistorial,String> celdaCategoriaHistorial(){return new TableCell<>(){
        private final Label insignia=new Label();
        {insignia.getStyleClass().add("player-history-category");setAlignment(javafx.geometry.Pos.CENTER);} 
        @Override protected void updateItem(String valor,boolean vacia){super.updateItem(valor,vacia);setText(null);if(vacia||valor==null){setGraphic(null);return;}insignia.setText(valor.toUpperCase(Locale.ROOT));setGraphic(insignia);}
    };}
    private String hexRgba(String hex,double alpha){if(hex==null||!hex.matches("#[0-9A-Fa-f]{6}"))return "rgba(66,149,113,.16)";int r=Integer.parseInt(hex.substring(1,3),16),g=Integer.parseInt(hex.substring(3,5),16),b=Integer.parseInt(hex.substring(5,7),16);return "rgba("+r+","+g+","+b+","+alpha+")";}
    private TableCell<CategoriaHistorial,String> celdaHistorialConTooltip(){return new TableCell<>(){@Override protected void updateItem(String valor,boolean vacia){super.updateItem(valor,vacia);setText(vacia?null:valor);CategoriaHistorial h=getTableRow()==null?null:getTableRow().getItem();if(vacia||h==null){setTooltip(null);return;}String detalle=valor(h.motivo(),"Sin motivo");if(h.observaciones()!=null&&!h.observaciones().isBlank())detalle+="\nObservaciones: "+h.observaciones();if(h.responsable()!=null&&!h.responsable().isBlank())detalle+="\nResponsable: "+h.responsable();setTooltip(new Tooltip(detalle));}};}
    private void ajustarAltoHistorial(){int filas=Math.max(1,historial.size());double alto=Math.min(282,48+filas*48+4);tablaHistorial.setMinHeight(alto);tablaHistorial.setPrefHeight(alto);tablaHistorial.setMaxHeight(alto);}
    private String capitalizarEnum(String valor){String x=formatearEnum(valor);return x.isBlank()?x:Character.toUpperCase(x.charAt(0))+x.substring(1);}
    private void cierrePremiumW21B2(){}
    private javafx.beans.property.SimpleStringProperty texto(String v){return new javafx.beans.property.SimpleStringProperty(v);}
    private String periodo(CategoriaHistorial h){return h.hasta()==null?"Desde "+h.desde().format(FECHA):h.desde().format(FECHA)+" · "+h.hasta().format(FECHA);}
    private String formatearEnum(String v){return v==null?"—":v.replace('_',' ').toLowerCase(Locale.ROOT);}
    private boolean contiene(String v,String q){return v!=null&&v.toLowerCase(Locale.ROOT).contains(q);}
    private String valor(String v,String defecto){return v==null||v.isBlank()?defecto:v;}
    private void estado(String m,boolean error){etiquetaEstado.setText(m);etiquetaEstado.getStyleClass().removeAll("players-status-error","players-status-ok");etiquetaEstado.getStyleClass().add(error?"players-status-error":"players-status-ok");etiquetaEstado.setTooltip(m==null||m.isBlank()?null:new Tooltip(m));}
    @FXML private void nuevoPerfilProvisional(){new JugadorPerfilDialog().mostrar("Nuevo perfil provisional","",null,false,false).ifPresent(d->{ejecutarOperacion("Perfil provisional creado",()->identidadService.crearProvisional(d.nombre()));});}
    @FXML private void nuevoJugadorVinculado(){new JugadorClienteDialog().mostrar("Seleccionar cliente").ifPresent(cliente->{ejecutarOperacion("Jugador vinculado creado",()->identidadService.crearDefinitivo(cliente.id(),cliente.nombre()));});}
    @FXML private void editarPerfil(){JugadorResumen j=seleccionado();if(j==null)return;new JugadorPerfilDialog().mostrar("Editar perfil",j.nombre(),j.posicion(),j.disponible(),j.aceptaInvitaciones()).ifPresent(d->{ejecutarOperacion("Perfil actualizado",()->identidadService.actualizarPerfil(j.id(),d.nombre(),d.posicion(),d.disponible(),d.invitaciones()));});}
    @FXML private void vincularCliente(){JugadorResumen j=seleccionado();if(j==null)return;new JugadorClienteDialog().mostrar("Vincular cliente al perfil").ifPresent(cliente->{ejecutarOperacion("Perfil vinculado",()->identidadService.vincularCliente(j.id(),cliente.id(),cliente.nombre(),1L,esAdmin()));});}
    @FXML private void marcarIdentidadRevisada(){JugadorResumen j=seleccionado();if(j==null||!esAdmin())return;long usuario=Navegacion.getUsuarioActual().getId();ejecutarOperacion("Identidad revisada",()->identidadService.marcarVinculacionRevisada(j.id(),usuario,true));}
    @FXML private void asignarCategoria(){JugadorResumen j=seleccionado();if(j==null)return;new JugadorCategoriaOperacionDialog().asignar(List.copyOf(filtroCategoria.getItems()),j.nombre(),j.categoria()).ifPresent(d->{ejecutarOperacion("Categoría actualizada",()->identidadService.asignarCategoria(j.id(),d.categoria().id(),d.fecha(),OrigenJugadorCategoria.ADMINISTRATIVA,d.motivo(),null));});}
    @FXML private void suspenderCategoria(){cerrarCategoria(EstadoJugadorCategoria.SUSPENDIDA,"Suspender categoría");}
    @FXML private void finalizarCategoria(){cerrarCategoria(EstadoJugadorCategoria.FINALIZADA,"Finalizar categoría");}
    private void cerrarCategoria(EstadoJugadorCategoria estado,String titulo){JugadorResumen j=seleccionado();if(j==null)return;new JugadorCategoriaOperacionDialog().cerrar(titulo,j.nombre(),j.categoria()).ifPresent(d->{ejecutarOperacion("Categoría cerrada",()->{identidadService.cerrarCategoria(j.id(),d.fecha(),estado,d.motivo(),d.observaciones(),null,esAdmin());return null;});});}
    @FXML private void cambiarEstadoPerfil(){JugadorResumen j=seleccionado();if(j==null)return;boolean nuevo=!j.activo();boolean confirmado=nuevo?Dialogos.confirmar("Activar perfil deportivo","El perfil de "+j.nombre()+" volverá a aparecer entre los jugadores activos."):Dialogos.confirmarPeligro("Desactivar perfil deportivo","El perfil de "+j.nombre()+" dejará de aparecer entre los jugadores activos. Su historial y sus categorías se conservarán.");if(!confirmado)return;ejecutarOperacion("Estado actualizado",()->identidadService.cambiarActivo(j.id(),nuevo,esAdmin()));}
    private JugadorResumen seleccionado(){JugadorResumen j=tablaJugadores.getSelectionModel().getSelectedItem();if(j==null)Dialogos.informacion("Selección requerida","Seleccioná un jugador para continuar.");return j;}
    private boolean esAdmin(){return Navegacion.getUsuarioActual()!=null&&Navegacion.getUsuarioActual().esAdministrador();}
    private <T> void ejecutarOperacion(String exito,java.util.concurrent.Callable<T> accion){if(operacionEnCurso)return;JugadorResumen previo=tablaJugadores.getSelectionModel().getSelectedItem();perfilAReseleccionar=previo==null?null:previo.id();operacionEnCurso=true;bloquearAcciones(true);try{accion.call();Dialogos.exito("Operación completada",exito+" correctamente.");cargar();}catch(Exception e){Dialogos.error("No se pudo completar la operación",mensajeRaiz(e));}finally{operacionEnCurso=false;bloquearAcciones(false);}}
    private void reSeleccionarPendiente(){if(perfilAReseleccionar==null)return;long id=perfilAReseleccionar;perfilAReseleccionar=null;tablaJugadores.getItems().stream().filter(x->x.id()==id).findFirst().ifPresent(x->tablaJugadores.getSelectionModel().select(x));}
    private void bloquearAcciones(boolean bloquear){for(Button b:List.of(botonActualizar,botonVincular,botonEstadoPerfil,botonMarcarRevisado,botonAsignarCategoria,botonSuspenderCategoria,botonFinalizarCategoria))if(b!=null)b.setDisable(bloquear);}
    private String mensajeRaiz(Throwable e){Throwable x=e;while(x.getCause()!=null)x=x.getCause();return x.getMessage()==null?"Ocurrió un error inesperado.":x.getMessage();}
    private void ocultarEstadoLuego(){PauseTransition pausa=new PauseTransition(Duration.seconds(2.2));pausa.setOnFinished(e->etiquetaEstado.setText(""));pausa.play();}

    @FXML private void volver(){Navegacion.mostrarDashboard(Navegacion.getUsuarioActual());}
    private record Datos(List<JugadorResumen> jugadores,List<CategoriaResumen> categorias){}
}
