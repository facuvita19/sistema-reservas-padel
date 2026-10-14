package negocio;
public enum EstadoIntentoPago { INICIADO, CHECKOUT_CREADO, REDIRIGIDO, APROBADO, PENDIENTE, RECHAZADO, CANCELADO, EXPIRADO, ERROR; public boolean esFinal(){return this==APROBADO||this==RECHAZADO||this==CANCELADO||this==EXPIRADO||this==ERROR;} }
