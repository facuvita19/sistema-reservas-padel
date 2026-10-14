-- P4.2 - modelo de pago online e intentos auditables (MySQL 8.4)
ALTER TABLE ordenes_pago ADD COLUMN preferencia_externa_id VARCHAR(150) NULL AFTER referencia_externa;
ALTER TABLE ordenes_pago ADD COLUMN pago_externo_id VARCHAR(150) NULL AFTER preferencia_externa_id;
ALTER TABLE ordenes_pago ADD UNIQUE KEY uk_ordenes_pago_proveedor_preferencia (proveedor, preferencia_externa_id);
ALTER TABLE ordenes_pago ADD KEY idx_ordenes_pago_pago_externo (proveedor, pago_externo_id);
