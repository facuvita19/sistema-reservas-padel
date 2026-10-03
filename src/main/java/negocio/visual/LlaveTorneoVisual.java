package negocio.visual;

import java.util.List;

public record LlaveTorneoVisual(
        List<FaseLlaveVisual> fases,
        List<ConexionLlaveVisual> conexiones,
        List<String> problemas) {
    public LlaveTorneoVisual {
        fases = fases == null ? List.of() : List.copyOf(fases);
        conexiones = conexiones == null ? List.of() : List.copyOf(conexiones);
        problemas = problemas == null ? List.of() : List.copyOf(problemas);
    }
}
