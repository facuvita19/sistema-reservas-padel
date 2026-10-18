package negocio;
public enum EstadoSolicitudComunidad { PENDIENTE, CONTACTADA, ACEPTADA, RECHAZADA, CANCELADA, CERRADA_POR_CONVOCANTE, VENCIDA;
    public boolean esPendiente(){ return this==PENDIENTE || this==CONTACTADA; }
}