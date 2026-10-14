-- W2.4B.2-P1 - nucleo desacoplado de pagos (MySQL 8.4)
CREATE TABLE IF NOT EXISTS ordenes_pago (
 id BIGINT NOT NULL AUTO_INCREMENT,
 reserva_id BIGINT NOT NULL,
 proveedor VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
 metodo VARCHAR(30) NOT NULL DEFAULT 'TRANSFERENCIA_MANUAL',
 estado VARCHAR(35) NOT NULL DEFAULT 'CREADA',
 importe DECIMAL(12,2) NOT NULL,
 moneda CHAR(3) NOT NULL DEFAULT 'ARS',
 clave_idempotencia VARCHAR(100) NOT NULL,
 referencia_externa VARCHAR(150) NULL,
 url_pago VARCHAR(700) NULL,
 fecha_expiracion DATETIME NULL,
 fecha_aprobacion DATETIME NULL,
 fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 version BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY (id),
 UNIQUE KEY uk_ordenes_pago_idempotencia (clave_idempotencia),
 UNIQUE KEY uk_ordenes_pago_proveedor_referencia (proveedor, referencia_externa),
 KEY idx_ordenes_pago_reserva (reserva_id, fecha_creacion),
 KEY idx_ordenes_pago_estado (estado, fecha_actualizacion),
 CONSTRAINT fk_ordenes_pago_reserva FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT chk_ordenes_pago_importe CHECK (importe > 0),
 CONSTRAINT chk_ordenes_pago_moneda CHECK (CHAR_LENGTH(moneda) = 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS intentos_pago (
 id BIGINT NOT NULL AUTO_INCREMENT,
 orden_pago_id BIGINT NOT NULL,
 numero_intento INT NOT NULL,
 estado VARCHAR(35) NOT NULL DEFAULT 'INICIADO',
 referencia_externa VARCHAR(150) NULL,
 detalle_error VARCHAR(500) NULL,
 fecha_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 fecha_fin DATETIME NULL,
 PRIMARY KEY (id),
 UNIQUE KEY uk_intentos_pago_orden_numero (orden_pago_id, numero_intento),
 KEY idx_intentos_pago_estado (estado, fecha_inicio),
 CONSTRAINT fk_intentos_pago_orden FOREIGN KEY (orden_pago_id) REFERENCES ordenes_pago(id) ON DELETE CASCADE ON UPDATE CASCADE,
 CONSTRAINT chk_intentos_pago_numero CHECK (numero_intento > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS eventos_pago (
 id BIGINT NOT NULL AUTO_INCREMENT,
 orden_pago_id BIGINT NULL,
 proveedor VARCHAR(30) NOT NULL,
 evento_externo_id VARCHAR(160) NOT NULL,
 tipo VARCHAR(80) NOT NULL,
 estado_procesamiento VARCHAR(25) NOT NULL DEFAULT 'RECIBIDO',
 firma_valida TINYINT(1) NOT NULL DEFAULT 0,
 payload_hash CHAR(64) NOT NULL,
 payload_json JSON NULL,
 detalle_resultado VARCHAR(500) NULL,
 fecha_evento DATETIME NULL,
 fecha_recepcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 fecha_procesamiento DATETIME NULL,
 PRIMARY KEY (id),
 UNIQUE KEY uk_eventos_pago_proveedor_evento (proveedor, evento_externo_id),
 KEY idx_eventos_pago_orden (orden_pago_id, fecha_recepcion),
 KEY idx_eventos_pago_estado (estado_procesamiento, fecha_recepcion),
 CONSTRAINT fk_eventos_pago_orden FOREIGN KEY (orden_pago_id) REFERENCES ordenes_pago(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conciliaciones_pago (
 id BIGINT NOT NULL AUTO_INCREMENT,
 orden_pago_id BIGINT NOT NULL,
 estado_local VARCHAR(35) NOT NULL,
 estado_proveedor VARCHAR(80) NULL,
 resultado VARCHAR(30) NOT NULL,
 detalle VARCHAR(500) NULL,
 fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (id),
 KEY idx_conciliaciones_pago_orden (orden_pago_id, fecha),
 KEY idx_conciliaciones_pago_resultado (resultado, fecha),
 CONSTRAINT fk_conciliaciones_pago_orden FOREIGN KEY (orden_pago_id) REFERENCES ordenes_pago(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE pagos ADD COLUMN orden_pago_id BIGINT NULL AFTER reserva_id;
ALTER TABLE pagos ADD COLUMN origen_acreditacion VARCHAR(30) NOT NULL DEFAULT 'ADMINISTRACION' AFTER metodo_pago;
ALTER TABLE pagos ADD COLUMN referencia_externa VARCHAR(150) NULL AFTER referencia;
ALTER TABLE pagos ADD UNIQUE KEY uk_pagos_orden (orden_pago_id);
ALTER TABLE pagos ADD KEY idx_pagos_origen (origen_acreditacion, fecha_creacion);
ALTER TABLE pagos ADD CONSTRAINT fk_pagos_orden FOREIGN KEY (orden_pago_id) REFERENCES ordenes_pago(id) ON DELETE RESTRICT ON UPDATE CASCADE;
