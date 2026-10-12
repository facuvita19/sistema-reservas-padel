USE padel_reservas;

ALTER TABLE configuracion_complejo
    ADD COLUMN pago_alias VARCHAR(120) NULL AFTER minutos_reserva_pendiente,
    ADD COLUMN pago_titular VARCHAR(160) NULL AFTER pago_alias,
    ADD COLUMN pago_entidad VARCHAR(120) NULL AFTER pago_titular,
    ADD COLUMN pago_instrucciones VARCHAR(700) NULL AFTER pago_entidad;
