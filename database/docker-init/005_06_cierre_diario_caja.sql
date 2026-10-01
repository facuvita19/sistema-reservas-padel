USE padel_reservas;

ALTER TABLE pagos
    ADD COLUMN fecha_reembolso DATETIME NULL AFTER fecha_pago;

CREATE TABLE IF NOT EXISTS cierres_caja (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'CERRADA',
    total_acreditado DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_efectivo_calculado DECIMAL(12,2) NOT NULL DEFAULT 0,
    efectivo_declarado DECIMAL(12,2) NOT NULL DEFAULT 0,
    diferencia_efectivo DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_reembolsado DECIMAL(12,2) NOT NULL DEFAULT 0,
    cantidad_pagos_acreditados INT NOT NULL DEFAULT 0,
    pagos_pendientes INT NOT NULL DEFAULT 0,
    reservas_completadas INT NOT NULL DEFAULT 0,
    reservas_ausentes INT NOT NULL DEFAULT 0,
    reservas_canceladas INT NOT NULL DEFAULT 0,
    observaciones VARCHAR(1000) NULL,
    usuario_cierre_id BIGINT NOT NULL,
    fecha_cierre DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_cierre_caja_fecha UNIQUE (fecha),
    CONSTRAINT fk_cierre_caja_usuario
        FOREIGN KEY (usuario_cierre_id) REFERENCES usuarios(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_cierre_caja_estado
        CHECK (estado IN ('ABIERTA', 'CERRADA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS cierres_caja_detalle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cierre_caja_id BIGINT NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    cantidad_movimientos INT NOT NULL DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_cierre_detalle_metodo
        UNIQUE (cierre_caja_id, metodo_pago),
    CONSTRAINT fk_cierre_detalle_cierre
        FOREIGN KEY (cierre_caja_id) REFERENCES cierres_caja(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
