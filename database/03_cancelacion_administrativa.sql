USE padel_reservas;

ALTER TABLE reservas
    ADD COLUMN tipo_cancelacion VARCHAR(20) NULL AFTER estado,
    ADD COLUMN motivo_cancelacion VARCHAR(500) NULL AFTER tipo_cancelacion,
    ADD COLUMN fecha_cancelacion DATETIME NULL AFTER motivo_cancelacion,
    ADD COLUMN usuario_cancelacion_id BIGINT NULL AFTER fecha_cancelacion;

ALTER TABLE reservas
    ADD CONSTRAINT fk_reserva_usuario_cancelacion
        FOREIGN KEY (usuario_cancelacion_id)
        REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    ADD CONSTRAINT chk_tipo_cancelacion
        CHECK (
            tipo_cancelacion IS NULL
            OR tipo_cancelacion IN ('CLIENTE', 'ADMINISTRATIVA')
        );
