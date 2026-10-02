package negocio;
public enum ModoAsignacionGrupoTorneo { SORTEO_DIRIGIDO("Sorteo con cabezas de serie"), MANUAL("Armado manual"); private final String d; ModoAsignacionGrupoTorneo(String d){this.d=d;} public String toString(){return d;} }
