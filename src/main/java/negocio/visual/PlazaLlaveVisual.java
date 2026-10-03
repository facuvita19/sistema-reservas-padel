package negocio.visual;

public record PlazaLlaveVisual(
        int posicion,
        String referencia,
        String textoVisible,
        boolean ganadorPartido,
        String partidoOrigenClave) {
}
