package negocio;

import java.util.ArrayList;
import java.util.List;

public class PropuestaEtapaEliminatoria {
    private final List<ClasificadoEtapaEliminatoria> clasificados =
            new ArrayList<>();
    private final List<ClasificadoEtapaEliminatoria> pases =
            new ArrayList<>();
    private final List<CrucePropuestoTorneo> cruces = new ArrayList<>();
    private String explicacion;
    private boolean valida;
    private String error;

    public List<ClasificadoEtapaEliminatoria> getClasificados() {
        return new ArrayList<>(clasificados);
    }
    public void setClasificados(List<ClasificadoEtapaEliminatoria> valores) {
        clasificados.clear(); if (valores != null) clasificados.addAll(valores);
    }
    public List<ClasificadoEtapaEliminatoria> getPases() {
        return new ArrayList<>(pases);
    }
    public void setPases(List<ClasificadoEtapaEliminatoria> valores) {
        pases.clear(); if (valores != null) pases.addAll(valores);
    }
    public List<CrucePropuestoTorneo> getCruces() {
        return new ArrayList<>(cruces);
    }
    public void setCruces(List<CrucePropuestoTorneo> valores) {
        cruces.clear(); if (valores != null) cruces.addAll(valores);
    }
    public String getExplicacion() { return explicacion; }
    public void setExplicacion(String valor) { explicacion = valor; }
    public boolean isValida() { return valida; }
    public void setValida(boolean valor) { valida = valor; }
    public String getError() { return error; }
    public void setError(String valor) { error = valor; }
}
