package negocio.visual;

import java.util.List;

public record FaseLlaveVisual(
        String nombre,
        int ronda,
        int indiceVisual,
        List<PartidoLlaveVisual> partidos) {
    public FaseLlaveVisual {
        partidos = partidos == null ? List.of() : List.copyOf(partidos);
    }
}
