package servicio;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import negocio.ConfiguracionComplejo;

public class GuiaEstadisticasPdfService {
    private static final PDRectangle PAGE = PDRectangle.A4;
    private static final PDFont REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final float M = 42f;
    private static final float W = PAGE.getWidth() - 2 * M;
    private static final float GAP = 14f;
    private static final float COL = (W - GAP) / 2f;
    private static final Color INK = new Color(29, 39, 45);
    private static final Color MUTED = new Color(88, 103, 111);
    private static final Color SOFT = new Color(239, 243, 244);
    private static final Color BORDER = new Color(202, 212, 216);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void generar(File destino, ConfiguracionComplejo configuracion) {
        if (destino == null) throw new IllegalArgumentException("El archivo de destino es obligatorio.");
        ConfiguracionComplejo cfg = configuracion == null ? new ConfiguracionComplejo() : configuracion;
        Color accent = color(cfg.getColorPrincipal());
        try (PDDocument doc = new PDDocument()) {
            portada(doc, cfg, accent);
            indiceYPeriodo(doc, cfg, accent);
            resumenYReservas(doc, cfg, accent);
            finanzasYClientes(doc, cfg, accent);
            canchasYWeb(doc, cfg, accent);
            criteriosYFormulas(doc, cfg, accent);
            doc.save(destino);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo escribir la guía PDF.", e);
        }
    }

    private void portada(PDDocument doc, ConfiguracionComplejo cfg, Color accent) throws IOException {
        PDPage page = page(doc);
        try (PDPageContentStream c = stream(doc, page)) {
            c.setNonStrokingColor(accent); c.addRect(0, PAGE.getHeight() - 300, PAGE.getWidth(), 300); c.fill();
            logo(doc, c, cfg.getRutaLogo(), M, PAGE.getHeight() - 112, 62);
            c.setNonStrokingColor(Color.WHITE);
            text(c, safe(cfg.getNombreComercial(), "Padel Reservas"), M, PAGE.getHeight() - 152, 14, BOLD);
            text(c, "GUÍA DE ESTADÍSTICAS", M, PAGE.getHeight() - 208, 28, BOLD);
            lines(c, "Cómo interpretar indicadores, períodos y resultados del complejo.", M, PAGE.getHeight() - 240, 12, REGULAR, W - 20, 17);

            float y = PAGE.getHeight() - 350;
            c.setNonStrokingColor(INK); text(c, "Una guía para tomar mejores decisiones", M, y, 18, BOLD);
            y -= 30; c.setNonStrokingColor(MUTED);
            lines(c, "Explica qué representa cada dato, qué fecha utiliza y cómo leer los porcentajes sin necesidad de conocer la lógica técnica del sistema.", M, y, 11, REGULAR, W, 16);

            String[] sections = {"RESUMEN", "RESERVAS", "FINANZAS", "CLIENTES", "CANCHAS", "WEB"};
            float chipY = PAGE.getHeight() - 490;
            for (int i = 0; i < sections.length; i++) {
                int row = i / 3, col = i % 3; float cw = (W - 20) / 3;
                float x = M + col * (cw + 10), cy = chipY - row * 53;
                roundBox(c, x, cy, cw, 39, SOFT, BORDER);
                c.setNonStrokingColor(accent); text(c, sections[i], x + 13, cy + 15, 9.5f, BOLD);
            }

            float by = 128;
            roundBox(c, M, by, W, 82, new Color(235, 245, 240), BORDER);
            c.setNonStrokingColor(accent); text(c, "QUÉ VAS A ENCONTRAR", M + 16, by + 57, 10, BOLD);
            c.setNonStrokingColor(INK);
            lines(c, "Definiciones simples, fórmulas principales, criterios de fecha y limitaciones conocidas para interpretar cada sección correctamente.", M + 16, by + 36, 10, REGULAR, W - 32, 14);
            c.setNonStrokingColor(MUTED); text(c, "Generado el " + DATE.format(LocalDate.now()), M, 72, 9, REGULAR);
            String contact = contact(cfg); if (!contact.isBlank()) lines(c, contact, M, 55, 8.5f, REGULAR, W, 12);
        }
    }

    private void indiceYPeriodo(PDDocument doc, ConfiguracionComplejo cfg, Color accent) throws IOException {
        PDPage page = page(doc);
        try (PDPageContentStream c = stream(doc, page)) {
            header(c, cfg, accent, "ÍNDICE Y USO DEL PERÍODO", 1);
            float y = 748;
            c.setNonStrokingColor(INK); text(c, "Contenido de la guía", M, y, 20, BOLD); y -= 28;
            String[][] items = {
                {"01", "Resumen", "Vista ejecutiva del complejo"}, {"02", "Reservas", "Estados, origen y tasas"},
                {"03", "Finanzas", "Pagos, reembolsos y caja"}, {"04", "Clientes", "Altas, recurrencia y cuentas"},
                {"05", "Canchas", "Actividad, ingresos y uso"}, {"06", "Web", "Solicitudes, pagos y expiraciones"}
            };
            for (int i = 0; i < items.length; i++) {
                int col = i % 2, row = i / 2; float x = M + col * (COL + GAP), cy = y - row * 70;
                indexCard(c, x, cy - 53, COL, 56, items[i], accent);
            }
            y -= 232;
            sectionBand(c, "Cómo elegir el período", M, y, W, accent); y -= 38;
            lines(c, "Las fechas Desde y Hasta se comparten entre todas las secciones. Los botones Hoy, Este mes, 30 días y Este año son accesos rápidos que actualizan toda la pantalla.", M, y, 10.5f, REGULAR, W, 15); y -= 62;

            float h = 142;
            infoCard(c, M, y - h, COL, h, "LA FECHA NO SIEMPRE SIGNIFICA LO MISMO",
                    List.of("Reservas: fecha del turno.", "Web: fecha de creación de la solicitud.", "Finanzas: fecha del pago, reembolso o movimiento."), accent);
            infoCard(c, M + COL + GAP, y - h, COL, h, "COMPARACIÓN ANTERIOR",
                    List.of("Compara con un período inmediatamente anterior de igual duración.", "Sirve para reconocer aumentos, caídas o falta de base comparable."), accent);
            footer(c, 1);
        }
    }

    private void resumenYReservas(PDDocument doc, ConfiguracionComplejo cfg, Color accent) throws IOException {
        PDPage page = page(doc);
        try (PDPageContentStream c = stream(doc, page)) {
            header(c, cfg, accent, "RESUMEN Y RESERVAS", 2);
            float y = 744;
            sectionBand(c, "1. Resumen", M, y, W, accent); y -= 34;
            lines(c, "Presenta una lectura rápida de actividad, ingresos y ocupación. Es la pantalla indicada para detectar cambios antes de profundizar en otra sección.", M, y, 10.5f, REGULAR, W, 15); y -= 55;
            float h = 92;
            metricCard(c, M, y-h, COL, h, "INGRESOS NETOS", "Pagos recibidos menos dinero reembolsado.", accent);
            metricCard(c, M+COL+GAP, y-h, COL, h, "RESULTADO OPERATIVO", "Ingreso neto más ingresos manuales menos egresos manuales.", accent); y -= h + 12;
            metricCard(c, M, y-h, COL, h, "RESERVAS EFECTIVAS", "Pendientes, confirmadas, completadas y ausencias.", accent);
            metricCard(c, M+COL+GAP, y-h, COL, h, "OCUPACIÓN TEÓRICA", "Minutos reservados sobre capacidad calculada con la configuración actual.", accent); y -= h + 24;

            sectionBand(c, "2. Reservas", M, y, W, accent); y -= 34;
            lines(c, "Analiza las solicitudes por fecha del turno y diferencia las creadas por el personal de las originadas en la web.", M, y, 10.5f, REGULAR, W, 15); y -= 50;
            float sh = 82;
            smallMetric(c, M, y-sh, COL, sh, "CONFIRMACIÓN", "Confirmadas o resueltas sobre solicitudes.", accent);
            smallMetric(c, M+COL+GAP, y-sh, COL, sh, "FINALIZACIÓN", "Completadas sobre reservas efectivas.", accent); y -= sh + 10;
            smallMetric(c, M, y-sh, COL, sh, "CANCELACIÓN Y AUSENCIA", "Cada indicador usa como base todas las solicitudes.", accent);
            smallMetric(c, M+COL+GAP, y-sh, COL, sh, "EXPIRACIÓN WEB", "Expiradas sobre solicitudes web.", accent);
            footer(c, 2);
        }
    }

    private void finanzasYClientes(PDDocument doc, ConfiguracionComplejo cfg, Color accent) throws IOException {
        PDPage page = page(doc);
        try (PDPageContentStream c = stream(doc, page)) {
            header(c, cfg, accent, "FINANZAS Y CLIENTES", 3);
            float y = 744;
            sectionBand(c, "3. Finanzas", M, y, W, accent); y -= 34;
            lines(c, "Separa el dinero recibido, las devoluciones y los movimientos manuales para evitar que una única cifra oculte movimientos diferentes.", M, y, 10.5f, REGULAR, W, 15); y -= 55;
            float h=90;
            metricCard(c,M,y-h,COL,h,"PAGOS RECIBIDOS","Incluye pagos acreditados aunque después hayan sido reembolsados.",accent);
            metricCard(c,M+COL+GAP,y-h,COL,h,"DINERO REEMBOLSADO","Se organiza por la fecha en que se realizó la devolución.",accent);y-=h+12;
            metricCard(c,M,y-h,COL,h,"RESULTADO DEL PERÍODO","Pagos - reembolsos + ingresos manuales - egresos manuales.",accent);
            metricCard(c,M+COL+GAP,y-h,COL,h,"DIFERENCIA DE EFECTIVO","Sobrantes y faltantes registrados al cerrar caja.",accent);y-=h+24;

            sectionBand(c,"4. Clientes",M,y,W,accent);y-=34;
            lines(c,"Combina altas, actividad por reservas, cuentas web y participación vinculada en torneos.",M,y,10.5f,REGULAR,W,15);y-=48;
            float sh=82;
            smallMetric(c,M,y-sh,COL,sh,"CLIENTES QUE VOLVIERON","Clientes con dos o más reservas efectivas.",accent);
            smallMetric(c,M+COL+GAP,y-sh,COL,sh,"PORCENTAJE QUE VOLVIÓ","Recurrentes sobre clientes que reservaron.",accent);y-=sh+10;
            smallMetric(c,M,y-sh,COL,sh,"CUENTAS WEB","Pueden vincularse con fichas administrativas existentes.",accent);
            smallMetric(c,M+COL+GAP,y-sh,COL,sh,"FACTURACIÓN","Incluye pagos de reservas, no precios de inscripción a torneos.",accent);
            footer(c,3);
        }
    }

    private void canchasYWeb(PDDocument doc, ConfiguracionComplejo cfg, Color accent) throws IOException {
        PDPage page=page(doc);
        try(PDPageContentStream c=stream(doc,page)){
            header(c,cfg,accent,"CANCHAS Y WEB",4);float y=744;
            sectionBand(c,"5. Canchas",M,y,W,accent);y-=34;
            lines(c,"La vista principal prioriza reservas e ingresos. El detalle técnico conserva capacidad, bloqueos y partidos para diagnóstico.",M,y,10.5f,REGULAR,W,15);y-=55;
            float h=86;
            metricCard(c,M,y-h,COL,h,"CANCHA MÁS RESERVADA","Ranking por cantidad de reservas efectivas.",accent);
            metricCard(c,M+COL+GAP,y-h,COL,h,"CANCHA CON MÁS INGRESOS","Pagos recibidos asociados a reservas de cada cancha.",accent);y-=h+10;
            metricCard(c,M,y-h,COL,h,"USO POR RESERVAS","Minutos reservados sobre capacidad disponible.",accent);
            metricCard(c,M+COL+GAP,y-h,COL,h,"USO TOTAL","Reservas, bloqueos y torneos sobre capacidad configurada.",accent);y-=h+22;

            sectionBand(c,"6. Web",M,y,W,accent);y-=34;
            lines(c,"Usa la fecha de creación de la solicitud web, no la fecha futura del turno solicitado.",M,y,10.5f,REGULAR,W,15);y-=48;
            float sh=78;
            smallMetric(c,M,y-sh,COL,sh,"SOLICITUDES CON PAGO","Tuvieron al menos una acreditación, incluso si luego hubo reembolso.",accent);
            smallMetric(c,M+COL+GAP,y-sh,COL,sh,"ACREDITACIÓN WEB","Solicitudes con pago sobre solicitudes creadas.",accent);y-=sh+9;
            smallMetric(c,M,y-sh,COL,sh,"DEMORA DEL PAGO","Promedio desde la solicitud hasta la primera acreditación.",accent);
            smallMetric(c,M+COL+GAP,y-sh,COL,sh,"EXPIRACIÓN","Solicitudes vencidas sin completar la acreditación necesaria.",accent);
            footer(c,4);
        }
    }

    private void criteriosYFormulas(PDDocument doc, ConfiguracionComplejo cfg, Color accent) throws IOException {
        PDPage page=page(doc);
        try(PDPageContentStream c=stream(doc,page)){
            header(c,cfg,accent,"CRITERIOS, FÓRMULAS Y LIMITACIONES",5);float y=744;
            sectionBand(c,"Criterios temporales",M,y,W,accent);y-=38;
            String[][] rows={{"Reservas","Fecha del turno"},{"Solicitudes web","Fecha de creación"},{"Pagos recibidos","Fecha de acreditación"},{"Reembolsos","Fecha de devolución"},{"Caja manual","Fecha operativa"},{"Clientes nuevos","Fecha de creación de la ficha"},{"Cierres","Fecha operativa del cierre"}};
            for(int i=0;i<rows.length;i++){tableRow(c,M,y-29,W,31,rows[i][0],rows[i][1],i%2==0?SOFT:Color.WHITE,accent);y-=32;}
            y-=16;sectionBand(c,"Fórmulas principales",M,y,W,accent);y-=40;
            formulaCard(c,M,y-75,COL,75,"INGRESO NETO","Pagos recibidos - reembolsos",accent);
            formulaCard(c,M+COL+GAP,y-75,COL,75,"RESULTADO","Ingreso neto + ingresos manuales - egresos",accent);y-=87;
            formulaCard(c,M,y-75,COL,75,"RETORNO DE CLIENTES","Recurrentes / clientes que reservaron x 100",accent);
            formulaCard(c,M+COL+GAP,y-75,COL,75,"ACREDITACIÓN WEB","Solicitudes con pago / solicitudes web x 100",accent);y-=98;
            sectionBand(c,"Limitaciones conocidas",M,y,W,accent);y-=40;
            bullet(c,"El estado final no siempre permite reconstruir todas las transiciones anteriores.",M,y,W,accent);y-=37;
            bullet(c,"La capacidad histórica de canchas utiliza la configuración vigente.",M,y,W,accent);y-=37;
            bullet(c,"Las inscripciones de torneos no tienen pagos independientes asociados.",M,y,W,accent);y-=37;
            bullet(c,"La demora del pago incluye el tiempo administrativo hasta registrar la acreditación.",M,y,W,accent);
            footer(c,5);
        }
    }

    private PDPage page(PDDocument doc){PDPage p=new PDPage(PAGE);doc.addPage(p);return p;}
    private PDPageContentStream stream(PDDocument d,PDPage p)throws IOException{return new PDPageContentStream(d,p);}
    private void header(PDPageContentStream c,ConfiguracionComplejo cfg,Color accent,String title,int page)throws IOException{
        c.setNonStrokingColor(accent);c.addRect(0,PAGE.getHeight()-48,PAGE.getWidth(),48);c.fill();
        c.setNonStrokingColor(Color.WHITE);text(c,safe(cfg.getNombreComercial(),"Padel Reservas"),M,PAGE.getHeight()-21,8.5f,BOLD);text(c,title,M,PAGE.getHeight()-38,12,BOLD);
    }
    private void footer(PDPageContentStream c,int page)throws IOException{c.setStrokingColor(BORDER);c.moveTo(M,35);c.lineTo(PAGE.getWidth()-M,35);c.stroke();c.setNonStrokingColor(MUTED);text(c,"Guía de estadísticas",M,20,8,REGULAR);textRight(c,String.valueOf(page),PAGE.getWidth()-M,20,8,BOLD);}
    private void sectionBand(PDPageContentStream c,String title,float x,float y,float w,Color accent)throws IOException{c.setNonStrokingColor(accent);c.addRect(x,y-25,w,29);c.fill();c.setNonStrokingColor(Color.WHITE);text(c,title,x+13,y-16,13,BOLD);}
    private void indexCard(PDPageContentStream c,float x,float y,float w,float h,String[] data,Color accent)throws IOException{roundBox(c,x,y,w,h,SOFT,BORDER);c.setNonStrokingColor(accent);text(c,data[0],x+12,y+31,17,BOLD);c.setNonStrokingColor(INK);text(c,data[1],x+48,y+33,11,BOLD);c.setNonStrokingColor(MUTED);text(c,data[2],x+48,y+17,8.5f,REGULAR);}
    private void metricCard(PDPageContentStream c,float x,float y,float w,float h,String title,String body,Color accent)throws IOException{roundBox(c,x,y,w,h,SOFT,BORDER);c.setNonStrokingColor(accent);text(c,title,x+13,y+h-24,10,BOLD);c.setNonStrokingColor(INK);lines(c,body,x+13,y+h-45,9.5f,REGULAR,w-26,13);}
    private void smallMetric(PDPageContentStream c,float x,float y,float w,float h,String title,String body,Color accent)throws IOException{metricCard(c,x,y,w,h,title,body,accent);}
    private void infoCard(PDPageContentStream c,float x,float y,float w,float h,String title,List<String> bullets,Color accent)throws IOException{roundBox(c,x,y,w,h,new Color(235,245,240),BORDER);c.setNonStrokingColor(accent);text(c,title,x+13,y+h-24,9.5f,BOLD);float ty=y+h-49;for(String b:bullets){c.setNonStrokingColor(accent);text(c,"•",x+14,ty,10,BOLD);c.setNonStrokingColor(INK);lines(c,b,x+28,ty,8.8f,REGULAR,w-42,12);ty-=32;}}
    private void formulaCard(PDPageContentStream c,float x,float y,float w,float h,String title,String body,Color accent)throws IOException{roundBox(c,x,y,w,h,new Color(235,245,240),BORDER);c.setNonStrokingColor(accent);text(c,title,x+13,y+h-23,9.5f,BOLD);c.setNonStrokingColor(INK);lines(c,body,x+13,y+h-45,9.3f,BOLD,w-26,13);}
    private void tableRow(PDPageContentStream c,float x,float y,float w,float h,String left,String right,Color bg,Color accent)throws IOException{c.setNonStrokingColor(bg);c.addRect(x,y,w,h);c.fill();c.setStrokingColor(BORDER);c.addRect(x,y,w,h);c.stroke();c.setNonStrokingColor(accent);text(c,left,x+12,y+11,9.5f,BOLD);c.setNonStrokingColor(INK);text(c,right,x+230,y+11,9.5f,REGULAR);}
    private void bullet(PDPageContentStream c,String value,float x,float y,float w,Color accent)throws IOException{c.setNonStrokingColor(accent);c.addRect(x,y-20,6,25);c.fill();c.setNonStrokingColor(INK);lines(c,value,x+18,y,9.5f,REGULAR,w-18,13);}
    private void roundBox(PDPageContentStream c,float x,float y,float w,float h,Color fill,Color border)throws IOException{c.setNonStrokingColor(fill);c.addRect(x,y,w,h);c.fill();c.setStrokingColor(border);c.addRect(x,y,w,h);c.stroke();}
    private static float lines(PDPageContentStream c,String value,float x,float y,float size,PDFont font,float width,float leading)throws IOException{for(String line:wrap(value,font,size,width)){text(c,line,x,y,size,font);y-=leading;}return y;}
    private static List<String> wrap(String value,PDFont font,float size,float width)throws IOException{List<String> out=new ArrayList<>();StringBuilder line=new StringBuilder();for(String word:latin(value).split("\s+")){String test=line.length()==0?word:line+" "+word;if(font.getStringWidth(test)/1000*size<=width)line=new StringBuilder(test);else{if(line.length()>0)out.add(line.toString());line=new StringBuilder(word);}}if(line.length()>0)out.add(line.toString());return out;}
    private static void text(PDPageContentStream c,String value,float x,float y,float size,PDFont font)throws IOException{c.beginText();c.setFont(font,size);c.newLineAtOffset(x,y);c.showText(latin(value));c.endText();}
    private static void textRight(PDPageContentStream c,String value,float right,float y,float size,PDFont font)throws IOException{String clean=latin(value);float width=font.getStringWidth(clean)/1000*size;text(c,clean,right-width,y,size,font);}
    private static void logo(PDDocument doc,PDPageContentStream c,String ruta,float x,float y,float max){if(ruta==null||ruta.isBlank())return;File f=new File(ruta);if(!f.isFile())return;try{PDImageXObject image=PDImageXObject.createFromFileByContent(f,doc);float scale=Math.min(max/image.getWidth(),max/image.getHeight());c.drawImage(image,x,y,image.getWidth()*scale,image.getHeight()*scale);}catch(Exception ignored){}}
    private static Color color(String hex){try{return Color.decode(hex);}catch(Exception e){return new Color(47,143,131);}}
    private static String contact(ConfiguracionComplejo c){return String.join(" | ",List.of(c.getDireccion(),c.getTelefono(),c.getEmail()).stream().filter(v->v!=null&&!v.isBlank()).toList());}
    private static String safe(String v,String fallback){return v==null||v.isBlank()?fallback:v;}
    private static String latin(String v){return v==null?"":v.replace('–','-').replace('—','-').replace('→','>').replace('×','x').replace('•','-');}
}
