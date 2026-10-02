package negocio;

public class CrucePropuestoTorneo {
    private String instancia;
    private final int ronda;
    private final int orden;
    private String participante1;
    private String participante2;

    public CrucePropuestoTorneo(String instancia, int ronda, int orden,
            String participante1, String participante2) {
        this.instancia = instancia;
        this.ronda = ronda;
        this.orden = orden;
        this.participante1 = participante1;
        this.participante2 = participante2;
    }

    public String getInstancia() { return instancia; }
    public void setInstancia(String valor) { instancia = valor; }
    public int getRonda() { return ronda; }
    public int getOrden() { return orden; }
    public String getParticipante1() { return participante1; }
    public void setParticipante1(String valor) { participante1 = valor; }
    public String getParticipante2() { return participante2; }
    public void setParticipante2(String valor) { participante2 = valor; }

    @Override
    public String toString() {
        return instancia + " " + orden + ": " + participante1
                + " vs " + participante2;
    }
}
