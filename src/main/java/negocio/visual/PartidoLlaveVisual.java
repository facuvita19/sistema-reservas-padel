package negocio.visual;

public record PartidoLlaveVisual(
        String clave,
        String fase,
        int ronda,
        int orden,
        PlazaLlaveVisual plaza1,
        PlazaLlaveVisual plaza2) {
}
