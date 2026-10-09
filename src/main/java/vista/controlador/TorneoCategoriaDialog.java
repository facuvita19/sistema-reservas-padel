package vista.controlador;

import java.math.BigDecimal;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import negocio.FormatoCompetenciaTorneo;
import negocio.RamaTorneo;
import negocio.TorneoCategoria;
import vista.Dialogos;

public class TorneoCategoriaDialog {
    private final Dialog<ButtonType> dialogo=new Dialog<>();
    private final TextField nombre=new TextField();
    private final ComboBox<RamaTorneo> rama=new ComboBox<>();
    private final Spinner<Integer> cupo=new Spinner<>(1,512,16);
    private final TextField precio=new TextField("0");
    private final ComboBox<FormatoCompetenciaTorneo> formato=new ComboBox<>();
    private final Spinner<Integer> gruposTres=new Spinner<>(0,64,0);
    private final Spinner<Integer> gruposCuatro=new Spinner<>(0,64,0);
    private final Label resumenGrupos=new Label();
    private final VBox panelGrupos=new VBox(8);
    private final ScrollPane desplazamiento=new ScrollPane();
    private final TextField premioCampeon=new TextField();
    private final TextField premioSubcampeon=new TextField();
    private final TextArea premioDescripcion=new TextArea();
    private final Label error=new Label();
    private final long torneoId;
    private final TorneoCategoria original;
    private TorneoCategoria resultadoValidado;
    private final ButtonType guardar;

    public TorneoCategoriaDialog(long torneoId,TorneoCategoria categoria){
        this.torneoId=torneoId; original=categoria;
        guardar=new ButtonType(categoria==null?"CREAR CATEGORÍA":"GUARDAR CAMBIOS",ButtonBar.ButtonData.OK_DONE);
        configurar(); if(categoria!=null)cargar(categoria); actualizarFormato(); actualizarEstadoGuardar();
    }
    public Optional<TorneoCategoria> mostrar(){resultadoValidado=null;Optional<ButtonType> r=dialogo.showAndWait();return r.isPresent()&&r.get()==guardar?Optional.ofNullable(resultadoValidado):Optional.empty();}

    private void configurar(){
        boolean nuevo=original==null;
        dialogo.setTitle(nuevo?"Nueva categoría":"Editar categoría");
        dialogo.setHeaderText(nuevo?"Crear una categoría":"Actualizar categoría");
        dialogo.getDialogPane().getButtonTypes().addAll(guardar,new ButtonType("CANCELAR",ButtonBar.ButtonData.CANCEL_CLOSE));
        Dialogos.preparar(dialogo,"category-editor-dialog");
        dialogo.getDialogPane().getStyleClass().add("category-layout-final-v1");
        nombre.setPromptText("Ejemplo: 6ta"); rama.setPromptText("Seleccionar rama");
        precio.setPromptText("Ejemplo: 24000"); premioCampeon.setPromptText("Ejemplo: 300000");
        premioSubcampeon.setPromptText("Ejemplo: 100000"); premioDescripcion.setPromptText("Trofeos, paletas u otros premios");
        premioDescripcion.setWrapText(true); premioDescripcion.setPrefRowCount(2);
        premioDescripcion.setMinHeight(64); premioDescripcion.setPrefHeight(64); premioDescripcion.setMaxHeight(64);
        rama.setItems(FXCollections.observableArrayList(RamaTorneo.values())); rama.getSelectionModel().selectFirst();
        formato.setItems(FXCollections.observableArrayList(FormatoCompetenciaTorneo.values())); formato.setValue(FormatoCompetenciaTorneo.ELIMINACION_DIRECTA);
        cupo.setEditable(true); gruposTres.setEditable(true); gruposCuatro.setEditable(true);
        for(Node c:new Node[]{nombre,rama,cupo,precio,formato,gruposTres,gruposCuatro,premioCampeon,premioSubcampeon,premioDescripcion})c.getStyleClass().add("dialog-field");

        GridPane identidad=dosColumnas(62,38); identidad.add(campo("Nombre *",nombre),0,0); identidad.add(campo("Rama *",rama),1,0);
        VBox identidadSec=seccion("IDENTIDAD"); identidadSec.getChildren().add(identidad);
        GridPane capacidad=dosColumnas(40,60); capacidad.add(campo("Cupo de parejas *",cupo),0,0); capacidad.add(campo("Precio de inscripción (ARS) *",precio),1,0);
        VBox capacidadSec=seccion("CAPACIDAD Y PRECIO"); capacidadSec.getChildren().add(capacidad);
        panelGrupos.getStyleClass().add("category-groups-panel-v1");
        GridPane grupos=dosColumnas(50,50); grupos.add(campo("Grupos de 3 parejas",gruposTres),0,0); grupos.add(campo("Grupos de 4 parejas",gruposCuatro),1,0);
        resumenGrupos.getStyleClass().add("category-groups-summary"); resumenGrupos.setWrapText(true);
        panelGrupos.getChildren().addAll(grupos,resumenGrupos);
        VBox formatoSec=seccion("FORMATO COMPETITIVO"); formatoSec.getChildren().addAll(campo("Formato de competencia *",formato),panelGrupos);
        GridPane premios=dosColumnas(50,50); premios.add(campo("Premio para campeones (ARS)",premioCampeon),0,0); premios.add(campo("Premio para subcampeones (ARS)",premioSubcampeon),1,0);
        VBox premiosSec=seccion("PREMIOS OPCIONALES"); premiosSec.getChildren().addAll(premios,campo("Descripción adicional",premioDescripcion));
        error.getStyleClass().add("dialog-validation-error"); error.setWrapText(true); ocultarError();
        VBox contenido=new VBox(10,identidadSec,capacidadSec,formatoSec,premiosSec,error); contenido.setPadding(new Insets(2));
        desplazamiento.setContent(contenido);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        desplazamiento.setPannable(false);
        desplazamiento.setPrefViewportWidth(820);
        desplazamiento.setPrefViewportHeight(620);
        desplazamiento.setMaxHeight(620);
        desplazamiento.getStyleClass().add("category-layout-scroll-v2");
        dialogo.getDialogPane().setContent(desplazamiento);
        dialogo.getDialogPane().setMinWidth(820); dialogo.getDialogPane().setPrefWidth(860); dialogo.getDialogPane().setMaxWidth(900); dialogo.getDialogPane().setPrefHeight(760);
        dialogo.setOnShown(e->{desplazamiento.setVvalue(0); Platform.runLater(()->{desplazamiento.setVvalue(0); if(dialogo.getDialogPane().getScene()!=null && dialogo.getDialogPane().getScene().getWindow()!=null) dialogo.getDialogPane().getScene().getWindow().centerOnScreen();});});
        // categoria-scroll-grupos-fix-v2

        Node boton=dialogo.getDialogPane().lookupButton(guardar);
        boton.addEventFilter(javafx.event.ActionEvent.ACTION,e->{try{ocultarError();resultadoValidado=construir();}catch(IllegalArgumentException ex){resultadoValidado=null;mostrarError(ex.getMessage());e.consume();}});
        formato.valueProperty().addListener((o,a,n)->{actualizarFormato();actualizarEstadoGuardar();});
        gruposTres.valueProperty().addListener((o,a,n)->{actualizarProyeccion();actualizarEstadoGuardar();});
        gruposCuatro.valueProperty().addListener((o,a,n)->{actualizarProyeccion();actualizarEstadoGuardar();});
        nombre.textProperty().addListener((o,a,n)->actualizarEstadoGuardar());
        rama.valueProperty().addListener((o,a,n)->actualizarEstadoGuardar());
        precio.textProperty().addListener((o,a,n)->actualizarEstadoGuardar());
        Platform.runLater(nombre::requestFocus);
    }
    private VBox seccion(String t){Label l=new Label(t);l.getStyleClass().add("tournament-dialog-section-title-v1");VBox v=new VBox(8,l);v.getStyleClass().add("tournament-dialog-section-v1");return v;}
    private VBox campo(String t,Node n){Label l=new Label(t);l.getStyleClass().add("dialog-field-label");if(n instanceof Region r)r.setMaxWidth(Double.MAX_VALUE);VBox v=new VBox(5,l,n);return v;}
    private GridPane dosColumnas(double a,double b){GridPane g=new GridPane();g.setHgap(14);ColumnConstraints c1=new ColumnConstraints();c1.setPercentWidth(a);c1.setHgrow(Priority.ALWAYS);ColumnConstraints c2=new ColumnConstraints();c2.setPercentWidth(b);c2.setHgrow(Priority.ALWAYS);g.getColumnConstraints().addAll(c1,c2);return g;}
    private void actualizarFormato(){
        boolean usa=formato.getValue()==FormatoCompetenciaTorneo.GRUPOS_ELIMINACION;
        panelGrupos.setVisible(usa);
        panelGrupos.setManaged(usa);
        cupo.setDisable(usa);

        // compactar-solo-eliminacion-directa-v2
        premioDescripcion.setMinHeight(usa?64:52);
        premioDescripcion.setPrefHeight(usa?64:52);
        premioDescripcion.setMaxHeight(usa?64:52);
        if(desplazamiento.getContent() instanceof VBox contenidoRaiz){
            contenidoRaiz.setSpacing(usa?10:8);
        }
        dialogo.getDialogPane().getStyleClass().remove("category-elimination-compact-v2");
        if(!usa) dialogo.getDialogPane().getStyleClass().add("category-elimination-compact-v2");

        desplazamiento.setPrefViewportHeight(usa?620:610);
        desplazamiento.setMaxHeight(usa?620:610);
        dialogo.getDialogPane().setPrefHeight(usa?760:750);
        if(!usa) resumenGrupos.setText("");
        actualizarProyeccion();
        Platform.runLater(()->{
            desplazamiento.setVvalue(0);
            if(dialogo.getDialogPane().getScene()!=null
                    && dialogo.getDialogPane().getScene().getWindow()!=null){
                dialogo.getDialogPane().getScene().getWindow().sizeToScene();
                dialogo.getDialogPane().getScene().getWindow().centerOnScreen();
            }
        });
    }
private void actualizarProyeccion(){if(formato.getValue()!=FormatoCompetenciaTorneo.GRUPOS_ELIMINACION)return;int t=gruposTres.getValue(),c=gruposCuatro.getValue(),cap=t*3+c*4,clas=t*2+c*3;if(cap>0)cupo.getValueFactory().setValue(cap);resumenGrupos.setText("Capacidad: "+cap+" parejas · Clasificados: "+clas+" · Grupos: "+(t+c));}
    private void actualizarEstadoGuardar(){Node b=dialogo.getDialogPane().lookupButton(guardar);if(b==null)return;boolean grupos=formato.getValue()!=FormatoCompetenciaTorneo.GRUPOS_ELIMINACION||gruposTres.getValue()+gruposCuatro.getValue()>0;b.setDisable(nombre.getText()==null||nombre.getText().isBlank()||rama.getValue()==null||formato.getValue()==null||!numeroValido(precio.getText())||!grupos);}
    private boolean numeroValido(String t){try{return new BigDecimal(t==null?"":t.trim().replace(',','.')).signum()>=0;}catch(RuntimeException e){return false;}}
    private void cargar(TorneoCategoria c){nombre.setText(c.getNombre());rama.setValue(c.getRama());cupo.getValueFactory().setValue(c.getCupoParejas());precio.setText(c.getPrecioInscripcion().stripTrailingZeros().toPlainString());formato.setValue(c.getFormatoCompetencia());gruposTres.getValueFactory().setValue(c.getCantidadGruposTres());gruposCuatro.getValueFactory().setValue(c.getCantidadGruposCuatro());premioCampeon.setText(textoImporte(c.getPremioCampeon()));premioSubcampeon.setText(textoImporte(c.getPremioSubcampeon()));premioDescripcion.setText(c.getPremioDescripcion()==null?"":c.getPremioDescripcion());}
    private String textoImporte(BigDecimal v){return v==null?"":v.stripTrailingZeros().toPlainString();}
    private BigDecimal leerPremio(TextField c,String e){String t=c.getText();if(t==null||t.isBlank())return null;try{BigDecimal v=new BigDecimal(t.trim().replace(',','.'));if(v.signum()<0)throw new IllegalArgumentException(e+" no puede ser negativo.");if(v.scale()>2)throw new IllegalArgumentException(e+" admite hasta dos decimales.");return v;}catch(NumberFormatException x){throw new IllegalArgumentException(e+" debe ser un número válido.");}}
    private TorneoCategoria construir(){if(nombre.getText()==null||nombre.getText().isBlank())throw new IllegalArgumentException("Ingresá el nombre de la categoría.");if(rama.getValue()==null)throw new IllegalArgumentException("Seleccioná la rama.");BigDecimal importe;try{importe=new BigDecimal(precio.getText().trim().replace(',','.'));}catch(RuntimeException e){throw new IllegalArgumentException("El precio debe ser un número válido.");}if(importe.signum()<0)throw new IllegalArgumentException("El precio no puede ser negativo.");int t=gruposTres.getValue(),c=gruposCuatro.getValue();if(formato.getValue()==FormatoCompetenciaTorneo.GRUPOS_ELIMINACION&&t+c==0)throw new IllegalArgumentException("Configurá al menos un grupo.");TorneoCategoria x=new TorneoCategoria();if(original!=null)x.setId(original.getId());x.setTorneoId(torneoId);x.setNombre(nombre.getText().trim());x.setRama(rama.getValue());x.setCupoParejas(cupo.getValue());x.setPrecioInscripcion(importe);x.setFormatoCompetencia(formato.getValue());x.setCantidadGruposTres(formato.getValue()==FormatoCompetenciaTorneo.GRUPOS_ELIMINACION?t:0);x.setCantidadGruposCuatro(formato.getValue()==FormatoCompetenciaTorneo.GRUPOS_ELIMINACION?c:0);x.setPremioCampeon(leerPremio(premioCampeon,"El premio para los campeones"));x.setPremioSubcampeon(leerPremio(premioSubcampeon,"El premio para los subcampeones"));String d=premioDescripcion.getText();if(d!=null&&d.trim().length()>500)throw new IllegalArgumentException("La descripción de premios no puede superar 500 caracteres.");x.setPremioDescripcion(d);return x;}
    private void mostrarError(String m){error.setText(m);error.setVisible(true);error.setManaged(true);dialogo.getDialogPane().setPrefHeight(Math.max(dialogo.getDialogPane().getPrefHeight(),700));}
    private void ocultarError(){error.setText("");error.setVisible(false);error.setManaged(false);}
}
