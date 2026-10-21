package negocio;
public enum EstadoSolicitudComunidad { PENDIENTE, CONTACTADA, ACEPTADA, RECHAZADA, CANCELADA, CERRADA_POR_CONVOCANTE, CERRADA_POR_CUPO, VENCIDA;
    public boolean esPendiente(){ return this==PENDIENTE || this==CONTACTADA; }
}
