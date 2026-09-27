USE padel_reservas;

ALTER TABLE configuracion_complejo
    ADD COLUMN minutos_reserva_pendiente INT NOT NULL DEFAULT 15
        AFTER cancelacion_minima_horas;

ALTER TABLE reservas
    MODIFY COLUMN estado ENUM(
        'PENDIENTE',
        'CONFIRMADA',
        'COMPLETADA',
        'CANCELADA',
        'AUSENTE',
        'EXPIRADA'
    ) NOT NULL DEFAULT 'PENDIENTE',
    ADD COLUMN fecha_vencimiento DATETIME NULL AFTER estado,
    ADD COLUMN fecha_expiracion DATETIME NULL AFTER fecha_vencimiento;

CREATE INDEX idx_reservas_vencimiento
    ON reservas (estado, fecha_vencimiento);
