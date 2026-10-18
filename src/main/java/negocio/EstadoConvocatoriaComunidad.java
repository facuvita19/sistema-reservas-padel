package negocio;
public enum EstadoConvocatoriaComunidad {
    BORRADOR, ABIERTA, EN_COORDINACION, COMPLETA, CERRADA_EXTERNAMENTE, CANCELADA, VENCIDA;
    public boolean esActiva(){ return this==ABIERTA || this==EN_COORDINACION; }
    public boolean esFinal(){ return this==COMPLETA || this==CERRADA_EXTERNAMENTE || this==CANCELADA || this==VENCIDA; }
}