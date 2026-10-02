package negocio;

public record ClasificadoEtapaEliminatoria(
        long inscripcionId,
        long grupoId,
        String nombreGrupo,
        int posicionGrupo,
        int partidosGanados,
        int diferenciaSets,
        int diferenciaGames,
        int setsGanados,
        int gamesGanados) {

    public String referencia() {
        return posicionGrupo + "° " + nombreGrupo;
    }
}
