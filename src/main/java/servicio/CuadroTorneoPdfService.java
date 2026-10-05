package servicio;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    // CUADRO_MURAL_LAYOUT_V4
    // CUADRO_MURAL_ORIGEN_PLAZAS_V6
    // CUADRO_MURAL_DENSIDAD_V5
    private static final PDFont BOLD = new PDType1Font(
            Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont REGULAR = new PDType1Font(
            Standard14Fonts.FontName.HELVETICA);
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final Color NAVY = new Color(27, 35, 40);
    private static final Color INK = new Color(36, 45, 50);
    private static final Color MUTED = new Color(91, 104, 111);
    private static final Color BORDER = new Color(105, 119, 126);
    private static final Color LIGHT_BORDER = new Color(205, 211, 214);
    private static final Color PAPER = new Color(250, 251, 251);
    private static final Color WINNER = new Color(233, 244, 238);
    private static final Color GOLD = new Color(236, 199, 87);
    private static final Color CONNECTOR = new Color(99, 116, 124);

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
        float margin = 30;
        dibujarEncabezado(c, w, h, margin, torneo, categoria,
                complejo, resultados);

        Map<FaseTorneo, List<TorneoPartido>> fases = agruparFases(partidos);
        List<FaseTorneo> orden = fases.keySet().stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .toList();

        float areaTop = h - 122;
        float areaBottom = 30;
        float gap = orden.size() <= 1 ? 0 : 26;
        float columnW = orden.size() == 1
                ? Math.min(620, w - 2 * margin)
                : (w - 2 * margin - gap * (orden.size() - 1))
                        / orden.size();
        float startX = orden.size() == 1
                ? (w - columnW) / 2 : margin;

        List<List<MatchBox>> columns = calcularMalla(
                fases, orden, startX, columnW, gap,
                areaBottom, areaTop);
        Map<Long, MatchBox> boxesById = new HashMap<>();
        for (List<MatchBox> column : columns) {
            for (MatchBox item : column) {
                boxesById.put(item.partido().getId(), item);
            }
        }

        for (int col = 0; col < orden.size(); col++) {
            float x = startX + col * (columnW + gap);
            drawPhaseTitle(c, orden.get(col), fases.get(orden.get(col)),
                    partidos, x, columnW, areaTop + 15);
        }

        dibujarConectores(c, columns, boxesById);
        for (List<MatchBox> column : columns) {
            for (MatchBox matchBox : column) {
                drawMatch(c, matchBox.box(), matchBox.partido(), partidos,
                        pareja, cancha, resultados, orden.size() == 1);
            }
        }
    }

    private List<List<MatchBox>> calcularMalla(
            Map<FaseTorneo, List<TorneoPartido>> fases,
            List<FaseTorneo> orden, float startX, float columnW,
            float gap, float bottom, float top) {
        List<List<MatchBox>> columns = new ArrayList<>();
        for (int i = 0; i < orden.size(); i++) {
            columns.add(new ArrayList<>());
        }
        if (orden.isEmpty()) return columns;

        int base = 0;
        for (int i = 1; i < orden.size(); i++) {
            if (fases.get(orden.get(i)).size()
                    > fases.get(orden.get(base)).size()) {
                base = i;
            }
        }

        List<TorneoPartido> baseMatches = fases.get(orden.get(base));
        float baseX = startX + base * (columnW + gap);
        float baseHeight = alturaTarjeta(baseMatches.size(), false);
        List<Box> baseBoxes = distribuirCompacto(baseX, columnW,
                bottom, top, baseMatches.size(), baseHeight);
        for (int i = 0; i < baseMatches.size(); i++) {
            columns.get(base).add(
                    new MatchBox(baseMatches.get(i), baseBoxes.get(i)));
        }

        Map<Long, MatchBox> positioned = new HashMap<>();
        registrar(columns.get(base), positioned);

        for (int col = base + 1; col < orden.size(); col++) {
            float x = startX + col * (columnW + gap);
            List<TorneoPartido> matches = fases.get(orden.get(col));
            List<DesiredMatch> desired = new ArrayList<>();
            float height = alturaTarjeta(matches.size(), matches.size() == 1);
            for (TorneoPartido match : matches) {
                List<MatchBox> feeders = positioned.values().stream()
                        .filter(item -> Objects.equals(
                                item.partido().getPartidoSiguienteId(),
                                match.getId()))
                        .toList();
                float center = feeders.isEmpty()
                        ? (top + bottom) / 2
                        : (float) feeders.stream()
                            .mapToDouble(item -> item.box().centerY())
                            .average().orElse((top + bottom) / 2);
                desired.add(new DesiredMatch(match, center));
            }
            List<MatchBox> placed = ubicarPorCentros(
                    desired, x, columnW, height, bottom, top);
            columns.set(col, placed);
            registrar(placed, positioned);
        }

        for (int col = base - 1; col >= 0; col--) {
            float x = startX + col * (columnW + gap);
            List<TorneoPartido> matches = fases.get(orden.get(col));
            List<DesiredMatch> desired = new ArrayList<>();
            float height = alturaTarjeta(matches.size(), false);
            for (TorneoPartido match : matches) {
                MatchBox destination = positioned.get(
                        match.getPartidoSiguienteId());
                float center = destination == null
                        ? (top + bottom) / 2
                        : destination.box().centerY();
                desired.add(new DesiredMatch(match, center));
            }
            List<MatchBox> placed = ubicarPorCentros(
                    desired, x, columnW, height, bottom, top);
            columns.set(col, placed);
            registrar(placed, positioned);
        }
        return columns;
    }

    private float alturaTarjeta(int count, boolean onlyFinal) {
        if (onlyFinal) return 118;
        return count >= 4 ? 104 : 110;
    }

    private List<Box> distribuirCompacto(float x, float width,
            float bottom, float top, int count, float height) {
        List<Box> result = new ArrayList<>();
        if (count <= 0) return result;
        if (count == 1) {
            result.add(new Box(x, (bottom + top - height) / 2,
                    width, height));
            return result;
        }
        float usable = top - bottom;
        float preferredGap = count >= 4 ? 42 : 34;
        float total = count * height + (count - 1) * preferredGap;
        if (total > usable) {
            preferredGap = Math.max(12,
                    (usable - count * height) / (count - 1));
            total = count * height + (count - 1) * preferredGap;
        }
        float y = bottom + (usable + total) / 2 - height;
        for (int i = 0; i < count; i++) {
            result.add(new Box(x, y, width, height));
            y -= height + preferredGap;
        }
        return result;
    }

    private List<MatchBox> ubicarPorCentros(
            List<DesiredMatch> desired, float x, float width,
            float height, float bottom, float top) {
        List<DesiredMatch> sorted = desired.stream()
                .sorted(Comparator.comparingDouble(
                        DesiredMatch::center).reversed())
                .toList();
        List<Float> centers = new ArrayList<>();
        float minDistance = height + 24;
        for (DesiredMatch item : sorted) centers.add(item.center());

        for (int i = 1; i < centers.size(); i++) {
            float maximum = centers.get(i - 1) - minDistance;
            if (centers.get(i) > maximum) centers.set(i, maximum);
        }
        if (!centers.isEmpty()) {
            float minCenter = bottom + height / 2;
            float maxCenter = top - height / 2;
            float shiftUp = minCenter - centers.get(centers.size() - 1);
            if (shiftUp > 0) {
                for (int i = 0; i < centers.size(); i++) {
                    centers.set(i, centers.get(i) + shiftUp);
                }
            }
            float shiftDown = centers.get(0) - maxCenter;
            if (shiftDown > 0) {
                for (int i = 0; i < centers.size(); i++) {
                    centers.set(i, centers.get(i) - shiftDown);
                }
            }
        }

        List<MatchBox> result = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            result.add(new MatchBox(sorted.get(i).partido(),
                    new Box(x, centers.get(i) - height / 2,
                            width, height)));
        }
        return result;
    }

    private void registrar(List<MatchBox> items,
            Map<Long, MatchBox> positioned) {
        for (MatchBox item : items) {
            positioned.put(item.partido().getId(), item);
        }
    }

    private void dibujarEncabezado(PDPageContentStream c, float w,
            float h, float margin, Torneo torneo,
            TorneoCategoria categoria, ConfiguracionComplejo complejo,
            boolean resultados) throws IOException {
        c.setNonStrokingColor(NAVY);
        c.addRect(0, h - 92, w, 92);
        c.fill();
        c.setNonStrokingColor(Color.WHITE);
        text(c, seguro(complejo == null ? null
                : complejo.getNombreComercial(), "Padel Reservas"),
                margin, h - 22, 10, BOLD);
        fitText(c, torneo.getNombre(), margin, h - 49,
                w > 900 ? 22 : 19, BOLD, w * .58f);
        text(c, categoria.getNombre() + " - " + categoria.getRama(),
                margin, h - 72, 12, BOLD);
        textRight(c, FECHA.format(torneo.getFechaInicio()) + " al "
                + FECHA.format(torneo.getFechaFin()),
                w - margin, h - 24, 9.5f, REGULAR);
        textRight(c, resultados ? "CUADRO ACTUALIZADO"
                : "PLANILLA DE RESULTADOS",
                w - margin, h - 49, 10, BOLD);
        dibujarPremios(c, categoria, margin, h - 86, w - margin);
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

    private List<Box> calcularBoxes(float x, float width,
            float bottom, float top, int count, boolean onlyFinal) {
        List<Box> result = new ArrayList<>();
        float area = top - bottom;
        if (count <= 0) return result;

        float preferred = onlyFinal ? 126 : count >= 4 ? 112 : 118;
        float minGap = count <= 1 ? 0 : 20;
        float boxH = Math.min(preferred,
                (area - minGap * Math.max(0, count - 1)) / count);
        boxH = Math.max(90, boxH);

        if (count == 1) {
            result.add(new Box(x, bottom + (area - boxH) / 2,
                    width, boxH));
            return result;
        }

        float used = count * boxH;
        float spacing = Math.max(minGap, (area - used) / (count - 1));
        float total = used + spacing * (count - 1);
        float topUsed = bottom + (area + total) / 2;
        for (int i = 0; i < count; i++) {
            float y = topUsed - boxH - i * (boxH + spacing);
            result.add(new Box(x, y, width, boxH));
        }
        return result;
    }

    private void drawPhaseTitle(PDPageContentStream c,
            FaseTorneo fase, List<TorneoPartido> fasePartidos,
            List<TorneoPartido> todos, float x, float width, float y)
            throws IOException {
        c.setNonStrokingColor(INK);
        String title = nombreFasePdf(fase, fasePartidos, todos).toUpperCase();
        float titleWidth = BOLD.getStringWidth(latin(title)) / 1000 * 11;
        text(c, title, x + (width - titleWidth) / 2, y, 11, BOLD);
        c.setStrokingColor(BORDER);
        c.setLineWidth(.9f);
        c.moveTo(x + width * .24f, y - 5);
        c.lineTo(x + width * .76f, y - 5);
        c.stroke();
    }

    private void drawMatch(PDPageContentStream c, Box b,
            TorneoPartido p, List<TorneoPartido> todos,
            Function<Long, String> pareja,
            Function<Long, String> cancha, boolean resultados,
            boolean large) throws IOException {
        c.setNonStrokingColor(PAPER);
        c.setStrokingColor(BORDER);
        c.setLineWidth(1f);
        c.addRect(b.x, b.y, b.w, b.h);
        c.fillAndStroke();

        float pad = large ? 14 : 9;
        float titleSize = large ? 10 : 7.6f;
        c.setNonStrokingColor(INK);
        text(c, "PARTIDO " + p.getOrdenFase(), b.x + pad,
                b.y + b.h - 17, titleSize, BOLD);

        float scoreWidth = large ? 102 : 72;
        float scoreX = b.x + b.w - scoreWidth - pad;
        drawScoreHeaders(c, scoreX, b.y + b.h - 16, large);

        float rowTop = b.y + b.h - 30;
        float infoHeight = large ? 28 : 23;
        float participantArea = b.h - 30 - infoHeight;
        float rowH = participantArea / 2;
        float separatorY = rowTop - rowH;

        participant(c, b, b.x + pad, rowTop, rowH,
                nombreParticipante(p, 1, todos, pareja), p, 1,
                resultados, large, scoreX);
        participant(c, b, b.x + pad, separatorY, rowH,
                nombreParticipante(p, 2, todos, pareja), p, 2,
                resultados, large, scoreX);

        c.setStrokingColor(LIGHT_BORDER);
        c.setLineWidth(.7f);
        c.moveTo(b.x + pad, separatorY);
        c.lineTo(b.x + b.w - pad, separatorY);
        c.stroke();

        String schedule = p.estaProgramado()
                ? FECHA.format(p.getFecha()) + " - "
                        + HORA.format(p.getHoraInicio()) + " - "
                        + seguro(cancha.apply(p.getCanchaId()), "Cancha")
                : "Fecha / hora / cancha: __________________";
        fitText(c, schedule, b.x + pad, b.y + (large ? 17 : 14),
                large ? 8 : 6.6f, REGULAR, b.w - 2 * pad);

        String winner = resultados && p.getGanadoraInscripcionId() != null
                ? "Ganador: " + pareja.apply(p.getGanadoraInscripcionId())
                : "Ganador: ______________________________";
        fitText(c, winner, b.x + pad, b.y + (large ? 7 : 5),
                large ? 8 : 6.5f,
                resultados ? BOLD : REGULAR, b.w - 2 * pad);
    }

    private String nombreParticipante(TorneoPartido partido, int lado,
            List<TorneoPartido> todos,
            Function<Long, String> pareja) {
        Long pareja1Id = partido.getPareja1InscripcionId();
        Long pareja2Id = partido.getPareja2InscripcionId();
        Long inscripcionId = lado == 1 ? pareja1Id : pareja2Id;
        if (inscripcionId != null) {
            return seguro(pareja.apply(inscripcionId), "Por definir");
        }

        List<TorneoPartido> origenes = todos.stream()
                .filter(p -> Objects.equals(
                        p.getPartidoSiguienteId(), partido.getId()))
                .sorted(Comparator.comparingInt(TorneoPartido::getOrdenFase))
                .toList();

        int indicePlazaVacia = lado == 1 || pareja1Id != null ? 0 : 1;
        if (origenes.size() > indicePlazaVacia) {
            TorneoPartido origen = origenes.get(indicePlazaVacia);
            return "Ganador " + abreviarFase(origen.getFase())
                    + " " + origen.getOrdenFase();
        }
        return "Por definir";
    }

    private String abreviarFase(FaseTorneo fase) {
        return switch (fase) {
            case FINAL -> "Final";
            case SEMIFINAL -> "Semifinal";
            case CUARTOS -> "Cuartos";
            case OCTAVOS -> "Octavos";
            case DIECISEISAVOS -> "Dieciseisavos";
            default -> fase.toString();
        };
    }

    private void drawScoreHeaders(PDPageContentStream c,
            float startX, float y, boolean large) throws IOException {
        float boxSize = large ? 20 : 15;
        float step = large ? 27 : 20;
        for (int i = 0; i < 3; i++) {
            textCentered(c, "S" + (i + 1),
                    startX + i * step + boxSize / 2,
                    y, large ? 6.5f : 5.4f, BOLD);
        }
    }

    private void participant(PDPageContentStream c, Box b,
            float x, float rowTop, float rowH, String name,
            TorneoPartido p, int side, boolean resultados,
            boolean large, float scoreX) throws IOException {
        boolean winner = resultados && p.getGanadoraInscripcionId() != null
                && p.getGanadoraInscripcionId().equals(side == 1
                        ? p.getPareja1InscripcionId()
                        : p.getPareja2InscripcionId());
        if (winner) {
            c.setNonStrokingColor(WINNER);
            c.addRect(b.x + 1, rowTop - rowH + 1,
                    b.w - 2, rowH - 2);
            c.fill();
        }

        float textWidth = scoreX - x - 7;
        float fontSize = large ? 9.2f : 7.1f;
        float leading = large ? 9.4f : 7.4f;
        List<String> lines = wrap(name, BOLD, fontSize, textWidth, 2);
        float textY = rowTop - (rowH - lines.size() * leading) / 2
                - fontSize + 2;
        c.setNonStrokingColor(winner ? new Color(35, 92, 60) : INK);
        for (String line : lines) {
            text(c, line, x, textY, fontSize, BOLD);
            textY -= leading;
        }

        float boxSize = large ? 20 : 15;
        float step = large ? 27 : 20;
        float scoreY = rowTop - (rowH + boxSize) / 2;
        List<TorneoPartidoSet> sets = resultados ? p.getSets() : List.of();
        for (int i = 0; i < 3; i++) {
            float sx = scoreX + i * step;
            c.setStrokingColor(BORDER);
            c.setLineWidth(.8f);
            c.addRect(sx, scoreY, boxSize, boxSize);
            c.stroke();
            if (i < sets.size()) {
                int value = side == 1
                        ? sets.get(i).getPuntosPareja1()
                        : sets.get(i).getPuntosPareja2();
                c.setNonStrokingColor(INK);
                textCentered(c, String.valueOf(value),
                        sx + boxSize / 2,
                        scoreY + boxSize / 2 - 3,
                        large ? 9 : 7.2f, BOLD);
            }
        }
    }

    private void dibujarConectores(PDPageContentStream c,
            List<List<MatchBox>> columns,
            Map<Long, MatchBox> boxesById) throws IOException {
        c.setStrokingColor(CONNECTOR);
        c.setLineWidth(1.35f);
        for (List<MatchBox> column : columns) {
            for (MatchBox item : column) {
                Long siguienteId = item.partido().getPartidoSiguienteId();
                if (siguienteId == null) continue;
                MatchBox destino = boxesById.get(siguienteId);
                if (destino == null) continue;
                Box from = item.box();
                Box to = destino.box();
                float startX = from.x + from.w;
                float endX = to.x;
                float jointX = startX + (endX - startX) * .50f;
                float fromY = from.centerY();
                float toY = to.centerY();
                c.moveTo(startX, fromY);
                c.lineTo(jointX, fromY);
                c.lineTo(jointX, toY);
                c.lineTo(endX, toY);
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
                left, y, 7.5f, BOLD, right - left);
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
                .filter(Objects::nonNull)
                .map(id -> todos.stream().filter(p -> p.getId() == id)
                        .map(TorneoPartido::getFase).findFirst().orElse(null))
                .filter(Objects::nonNull)
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
                .filter(Objects::nonNull).distinct().count();
    }

    private String money(BigDecimal value) {
        return FormateadorMoneda.pesos(value);
    }

    private String seguro(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private List<String> wrap(String value, PDFont font,
            float size, float maxWidth, int maxLines) throws IOException {
        String clean = latin(seguro(value, ""));
        List<String> result = new ArrayList<>();
        String remaining = clean.trim();
        while (!remaining.isEmpty() && result.size() < maxLines) {
            String line = longestLine(remaining, font, size, maxWidth);
            if (line.isEmpty()) break;
            result.add(line);
            remaining = remaining.substring(line.length()).trim();
        }
        if (!remaining.isEmpty() && !result.isEmpty()) {
            int last = result.size() - 1;
            result.set(last, ellipsis(result.get(last) + " " + remaining,
                    font, size, maxWidth));
        }
        if (result.isEmpty()) result.add("");
        return result;
    }

    private String longestLine(String value, PDFont font,
            float size, float maxWidth) throws IOException {
        if (font.getStringWidth(value) / 1000 * size <= maxWidth) {
            return value;
        }
        int lastSpace = -1;
        for (int i = 1; i <= value.length(); i++) {
            String candidate = value.substring(0, i);
            if (candidate.endsWith(" ")) lastSpace = i - 1;
            if (font.getStringWidth(candidate) / 1000 * size > maxWidth) {
                if (lastSpace > 0) return value.substring(0, lastSpace);
                return value.substring(0, Math.max(1, i - 1));
            }
        }
        return value;
    }

    private void fitText(PDPageContentStream c, String value,
            float x, float y, float preferredSize, PDFont font,
            float maxWidth) throws IOException {
        String clean = latin(seguro(value, ""));
        float size = preferredSize;
        while (size > 6.2f
                && font.getStringWidth(clean) / 1000 * size > maxWidth) {
            size -= .4f;
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

    private record MatchBox(TorneoPartido partido, Box box) {
    }

    private record DesiredMatch(TorneoPartido partido, float center) {
    }
}
