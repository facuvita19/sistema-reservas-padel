package servicio;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import negocio.ConfiguracionComplejo;
import negocio.FaseTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoPartido;
import negocio.TorneoPartidoSet;
import util.FormateadorMoneda;

public class CuadroTorneoPdfService {
    // CUADRO_MURAL_LAYOUT_V2
    private static final PDFont BOLD = new PDType1Font(
            Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont REGULAR = new PDType1Font(
            Standard14Fonts.FontName.HELVETICA);
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final Color NAVY = new Color(20, 43, 56);
    private static final Color INK = new Color(24, 48, 61);
    private static final Color BORDER = new Color(82, 103, 114);
    private static final Color LIGHT_BORDER = new Color(190, 198, 202);
    private static final Color PAPER = new Color(248, 250, 251);
    private static final Color GOLD = new Color(255, 220, 105);

    public void generar(File destino, Torneo torneo,
            TorneoCategoria categoria, List<TorneoPartido> partidos,
            Function<Long, String> nombrePareja,
            Function<Long, String> nombreCancha,
            ConfiguracionComplejo configuracion,
            boolean incluirResultados) {
        if (destino == null || torneo == null || categoria == null
                || partidos == null || partidos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Faltan datos para generar el cuadro.");
        }
        List<TorneoPartido> eliminatorios = soloEliminatorios(partidos);
        if (eliminatorios.isEmpty()) {
            throw new IllegalArgumentException(
                    "La categoria no tiene un cuadro eliminatorio para imprimir.");
        }
        int parejas = cantidadParejas(eliminatorios);
        PDRectangle pagina = parejas <= 4
                ? new PDRectangle(PDRectangle.A4.getHeight(),
                        PDRectangle.A4.getWidth())
                : new PDRectangle(1190.55f, 841.89f);
        try (PDDocument documento = new PDDocument()) {
            PDPage hoja = new PDPage(pagina);
            documento.addPage(hoja);
            try (PDPageContentStream lienzo =
                    new PDPageContentStream(documento, hoja)) {
                dibujar(lienzo, pagina, torneo, categoria, eliminatorios,
                        nombrePareja, nombreCancha, configuracion,
                        incluirResultados);
            }
            documento.save(destino);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "No se pudo escribir el archivo PDF.", exception);
        }
    }

    private void dibujar(PDPageContentStream c, PDRectangle page,
            Torneo torneo, TorneoCategoria categoria,
            List<TorneoPartido> partidos,
            Function<Long, String> pareja,
            Function<Long, String> cancha,
            ConfiguracionComplejo complejo,
            boolean resultados) throws IOException {
        float w = page.getWidth();
        float h = page.getHeight();
        float margin = 34;
        dibujarEncabezado(c, w, h, margin, torneo, categoria,
                complejo, resultados);

        Map<FaseTorneo, List<TorneoPartido>> fases =
                agruparFases(partidos);
        List<FaseTorneo> orden = fases.keySet().stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .toList();
        float areaTop = h - 140;
        float areaBottom = 38;
        float areaHeight = areaTop - areaBottom;
        float gap = orden.size() <= 1 ? 0 : 34;
        float columnW = orden.size() == 1
                ? Math.min(620, w - 2 * margin)
                : (w - 2 * margin - gap * (orden.size() - 1))
                        / orden.size();
        float startX = orden.size() == 1
                ? (w - columnW) / 2 : margin;

        List<List<Box>> boxesByColumn = new ArrayList<>();
        for (int col = 0; col < orden.size(); col++) {
            List<TorneoPartido> list = fases.get(orden.get(col));
            float x = startX + col * (columnW + gap);
            List<Box> boxes = calcularBoxes(x, columnW,
                    areaBottom, areaTop, list.size(), orden.size() == 1);
            boxesByColumn.add(boxes);
            drawPhaseTitle(c, orden.get(col), list, partidos,
                    x, columnW, areaTop + 17);
            for (int i = 0; i < list.size(); i++) {
                drawMatch(c, boxes.get(i), list.get(i), pareja,
                        cancha, resultados, orden.size() == 1);
            }
        }
        dibujarConectores(c, boxesByColumn);
    }

    private void dibujarEncabezado(PDPageContentStream c, float w,
            float h, float margin, Torneo torneo,
            TorneoCategoria categoria, ConfiguracionComplejo complejo,
            boolean resultados) throws IOException {
        c.setNonStrokingColor(NAVY);
        c.addRect(0, h - 110, w, 110);
        c.fill();
        c.setNonStrokingColor(Color.WHITE);
        text(c, seguro(complejo == null ? null
                : complejo.getNombreComercial(), "Padel Reservas"),
                margin, h - 28, 12, BOLD);
        fitText(c, torneo.getNombre(), margin, h - 59,
                Math.min(25, w > 900 ? 25 : 22), BOLD, w * .56f);
        text(c, categoria.getNombre() + " - " + categoria.getRama(),
                margin, h - 84, 14, BOLD);
        textRight(c, FECHA.format(torneo.getFechaInicio()) + " al "
                + FECHA.format(torneo.getFechaFin()),
                w - margin, h - 29, 11, REGULAR);
        textRight(c, resultados ? "CUADRO ACTUALIZADO"
                : "PLANILLA PARA COMPLETAR",
                w - margin, h - 58, 11, BOLD);
        dibujarPremios(c, categoria, margin, h - 102, w - margin);
    }

    private Map<FaseTorneo, List<TorneoPartido>> agruparFases(
            List<TorneoPartido> partidos) {
        Map<FaseTorneo, List<TorneoPartido>> result =
                new EnumMap<>(FaseTorneo.class);
        partidos.stream().sorted(Comparator
                .comparing((TorneoPartido p) -> p.getFase().ordinal())
                .thenComparingInt(TorneoPartido::getOrdenFase))
                .forEach(p -> result.computeIfAbsent(
                        p.getFase(), key -> new ArrayList<>()).add(p));
        return result;
    }

    private List<Box> calcularBoxes(float x, float w,
            float bottom, float top, int count, boolean onlyFinal) {
        List<Box> result = new ArrayList<>();
        float area = top - bottom;
        if (count <= 0) return result;
        if (onlyFinal) {
            float boxH = Math.min(235, area * .56f);
            result.add(new Box(x, bottom + (area - boxH) / 2, w, boxH));
            return result;
        }
        float boxH = count == 1 ? Math.min(185, area * .38f)
                : Math.min(160, (area - 45 * (count - 1)) / count);
        if (count == 1) {
            result.add(new Box(x, bottom + (area - boxH) / 2, w, boxH));
            return result;
        }
        float spacing = (area - count * boxH) / (count - 1);
        for (int i = 0; i < count; i++) {
            float y = top - boxH - i * (boxH + spacing);
            result.add(new Box(x, y, w, boxH));
        }
        return result;
    }

    private void drawPhaseTitle(PDPageContentStream c,
            FaseTorneo fase, List<TorneoPartido> fasePartidos,
            List<TorneoPartido> todos, float x, float width, float y)
            throws IOException {
        c.setNonStrokingColor(INK);
        String title = nombreFasePdf(fase, fasePartidos, todos).toUpperCase();
        float titleWidth = BOLD.getStringWidth(latin(title)) / 1000 * 12;
        text(c, title, x + (width - titleWidth) / 2, y, 12, BOLD);
        c.setStrokingColor(INK);
        c.setLineWidth(1.2f);
        c.moveTo(x + width * .18f, y - 5);
        c.lineTo(x + width * .82f, y - 5);
        c.stroke();
    }

    private void drawMatch(PDPageContentStream c, Box b,
            TorneoPartido p, Function<Long, String> pareja,
            Function<Long, String> cancha, boolean resultados,
            boolean large) throws IOException {
        c.setNonStrokingColor(PAPER);
        c.setStrokingColor(BORDER);
        c.setLineWidth(1.1f);
        c.addRect(b.x, b.y, b.w, b.h);
        c.fillAndStroke();
        c.setNonStrokingColor(INK);
        float pad = large ? 16 : 10;
        float titleSize = large ? 11 : 8;
        text(c, "PARTIDO " + p.getOrdenFase(), b.x + pad,
                b.y + b.h - pad - 2, titleSize, BOLD);
        float headerY = b.y + b.h - (large ? 43 : 28);
        drawScoreHeaders(c, b.x + b.w - (large ? 118 : 88),
                headerY + 10, large);
        float line1 = b.y + b.h - (large ? 72 : 55);
        float line2 = b.y + b.h - (large ? 116 : 89);
        participant(c, b, b.x + pad, line1,
                pareja.apply(p.getPareja1InscripcionId()), p, 1,
                resultados, large);
        participant(c, b, b.x + pad, line2,
                pareja.apply(p.getPareja2InscripcionId()), p, 2,
                resultados, large);
        c.setStrokingColor(LIGHT_BORDER);
        c.moveTo(b.x + pad, (line1 + line2) / 2 + 2);
        c.lineTo(b.x + b.w - pad, (line1 + line2) / 2 + 2);
        c.stroke();
        String schedule = p.estaProgramado()
                ? FECHA.format(p.getFecha()) + " "
                        + HORA.format(p.getHoraInicio()) + " - "
                        + seguro(cancha.apply(p.getCanchaId()), "Cancha")
                : "Fecha / hora / cancha: __________________________";
        fitText(c, schedule, b.x + pad, b.y + (large ? 37 : 25),
                large ? 9 : 7, REGULAR, b.w - 2 * pad);
        String winner = resultados && p.getGanadoraInscripcionId() != null
                ? "Ganadores: "
                        + pareja.apply(p.getGanadoraInscripcionId())
                : "Ganadores: _________________________________";
        fitText(c, winner, b.x + pad, b.y + (large ? 17 : 10),
                large ? 9 : 7, resultados ? BOLD : REGULAR,
                b.w - 2 * pad);
    }

    private void drawScoreHeaders(PDPageContentStream c,
            float startX, float y, boolean large) throws IOException {
        float step = large ? 34 : 26;
        for (int i = 0; i < 3; i++) {
            textCentered(c, "S" + (i + 1), startX + i * step
                    + (large ? 12 : 9), y, large ? 7 : 6, BOLD);
        }
    }

    private void participant(PDPageContentStream c, Box b,
            float x, float y, String name, TorneoPartido p, int side,
            boolean resultados, boolean large) throws IOException {
        float scoreWidth = large ? 112 : 86;
        float available = b.w - (x - b.x) - scoreWidth - 8;
        boolean winner = resultados && p.getGanadoraInscripcionId() != null
                && p.getGanadoraInscripcionId().equals(side == 1
                        ? p.getPareja1InscripcionId()
                        : p.getPareja2InscripcionId());
        String shown = (winner ? "* " : "")
                + seguro(name, "Por definir");
        fitText(c, shown, x, y, large ? 11 : 8, BOLD, available);
        float boxSize = large ? 24 : 18;
        float step = large ? 34 : 26;
        float scoreX = b.x + b.w - scoreWidth;
        List<TorneoPartidoSet> sets = resultados ? p.getSets() : List.of();
        for (int i = 0; i < 3; i++) {
            float sx = scoreX + i * step;
            c.setStrokingColor(BORDER);
            c.setLineWidth(1);
            c.addRect(sx, y - 6, boxSize, boxSize);
            c.stroke();
            if (i < sets.size()) {
                int value = side == 1
                        ? sets.get(i).getPuntosPareja1()
                        : sets.get(i).getPuntosPareja2();
                textCentered(c, String.valueOf(value),
                        sx + boxSize / 2, y + (large ? 1 : 0),
                        large ? 10 : 8, BOLD);
            }
        }
    }

    private void dibujarConectores(PDPageContentStream c,
            List<List<Box>> columns) throws IOException {
        c.setStrokingColor(new Color(115, 135, 145));
        c.setLineWidth(1.2f);
        for (int col = 0; col < columns.size() - 1; col++) {
            List<Box> from = columns.get(col);
            List<Box> to = columns.get(col + 1);
            if (from.isEmpty() || to.isEmpty()) continue;
            for (int target = 0; target < to.size(); target++) {
                int first = Math.min(from.size() - 1, target * 2);
                int second = Math.min(from.size() - 1, first + 1);
                Box a = from.get(first);
                Box b = from.get(second);
                Box destination = to.get(target);
                float startX = a.x + a.w;
                float jointX = startX + (destination.x - startX) * .52f;
                float ay = a.centerY();
                float by = b.centerY();
                float dy = destination.centerY();
                c.moveTo(startX, ay); c.lineTo(jointX, ay);
                if (second != first) {
                    c.moveTo(startX, by); c.lineTo(jointX, by);
                    c.moveTo(jointX, Math.min(ay, by));
                    c.lineTo(jointX, Math.max(ay, by));
                }
                c.moveTo(jointX, dy);
                c.lineTo(destination.x, dy);
                c.stroke();
            }
        }
    }

    private void dibujarPremios(PDPageContentStream c,
            TorneoCategoria categoria, float left, float y, float right)
            throws IOException {
        List<String> data = new ArrayList<>();
        if (categoria.getPremioCampeon() != null) {
            data.add("Campeones: " + money(categoria.getPremioCampeon()));
        }
        if (categoria.getPremioSubcampeon() != null) {
            data.add("Subcampeones: "
                    + money(categoria.getPremioSubcampeon()));
        }
        if (categoria.getPremioDescripcion() != null
                && !categoria.getPremioDescripcion().isBlank()) {
            data.add(categoria.getPremioDescripcion());
        }
        if (data.isEmpty()) return;
        c.setNonStrokingColor(GOLD);
        fitText(c, "PREMIOS - " + String.join(" | ", data),
                left, y, 8, BOLD, right - left);
    }

    private List<TorneoPartido> soloEliminatorios(
            List<TorneoPartido> partidos) {
        return partidos.stream()
                .filter(p -> p.getFase() != FaseTorneo.GRUPOS)
                .toList();
    }

    private String nombreFasePdf(FaseTorneo fase,
            List<TorneoPartido> fasePartidos,
            List<TorneoPartido> todos) {
        if (!fase.name().startsWith("ACCESO_")) return fase.toString();
        int numero = numeroPrevia(fase);
        boolean posterior = todos.stream()
                .anyMatch(p -> p.getFase().name().startsWith("ACCESO_")
                        && numeroPrevia(p.getFase()) > numero);
        if (posterior) return fase.toString();
        FaseTorneo destino = fasePartidos.stream()
                .map(TorneoPartido::getPartidoSiguienteId)
                .filter(java.util.Objects::nonNull)
                .map(id -> todos.stream().filter(p -> p.getId() == id)
                        .map(TorneoPartido::getFase).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .filter(f -> !f.name().startsWith("ACCESO_"))
                .findFirst().orElse(null);
        return destino == null ? fase.toString()
                : "Clasificacion a " + destinoVisible(destino);
    }

    private String destinoVisible(FaseTorneo fase) {
        return switch (fase) {
            case FINAL -> "la final";
            case SEMIFINAL -> "semifinales";
            case CUARTOS -> "cuartos de final";
            case OCTAVOS -> "octavos de final";
            case DIECISEISAVOS -> "dieciseisavos de final";
            default -> fase.toString().toLowerCase();
        };
    }

    private int numeroPrevia(FaseTorneo fase) {
        return fase.name().startsWith("ACCESO_")
                ? Integer.parseInt(fase.name().substring(7)) : 0;
    }

    private int cantidadParejas(List<TorneoPartido> partidos) {
        return (int) partidos.stream()
                .flatMap(p -> java.util.stream.Stream.of(
                        p.getPareja1InscripcionId(),
                        p.getPareja2InscripcionId()))
                .filter(java.util.Objects::nonNull).distinct().count();
    }

    private String money(BigDecimal value) {
        return FormateadorMoneda.pesos(value);
    }

    private String seguro(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void fitText(PDPageContentStream c, String value,
            float x, float y, float preferredSize, PDFont font,
            float maxWidth) throws IOException {
        String clean = latin(seguro(value, ""));
        float size = preferredSize;
        while (size > 6.5f
                && font.getStringWidth(clean) / 1000 * size > maxWidth) {
            size -= .5f;
        }
        if (font.getStringWidth(clean) / 1000 * size > maxWidth) {
            clean = ellipsis(clean, font, size, maxWidth);
        }
        text(c, clean, x, y, size, font);
    }

    private String ellipsis(String value, PDFont font,
            float size, float maxWidth) throws IOException {
        String suffix = "...";
        int end = value.length();
        while (end > 1 && font.getStringWidth(
                value.substring(0, end) + suffix) / 1000 * size
                > maxWidth) {
            end--;
        }
        return value.substring(0, end).stripTrailing() + suffix;
    }

    private void text(PDPageContentStream c, String value,
            float x, float y, float size, PDFont font) throws IOException {
        c.beginText();
        c.setFont(font, size);
        c.newLineAtOffset(x, y);
        c.showText(latin(value));
        c.endText();
    }

    private void textRight(PDPageContentStream c, String value,
            float right, float y, float size, PDFont font)
            throws IOException {
        String clean = latin(value);
        float width = font.getStringWidth(clean) / 1000 * size;
        text(c, clean, right - width, y, size, font);
    }

    private void textCentered(PDPageContentStream c, String value,
            float center, float y, float size, PDFont font)
            throws IOException {
        String clean = latin(value);
        float width = font.getStringWidth(clean) / 1000 * size;
        text(c, clean, center - width / 2, y, size, font);
    }

    private String latin(String value) {
        if (value == null) return "";
        return value.replace('★', '*').replace('→', '>')
                .replace('–', '-').replace('—', '-');
    }

    private record Box(float x, float y, float w, float h) {
        float centerY() { return y + h / 2; }
    }
}
