USE padel_reservas;

CREATE TABLE movimientos_caja (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    tipo ENUM('INGRESO', 'EGRESO') NOT NULL,
    concepto VARCHAR(120) NOT NULL,
    importe DECIMAL(12,2) NOT NULL,
    medio_pago ENUM('EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'OTRO') NOT NULL,
    observaciones VARCHAR(500) NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_movimientos_caja_fecha (fecha, fecha_creacion),
    CONSTRAINT fk_movimientos_caja_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT chk_movimientos_caja_importe CHECK (importe > 0)
);
