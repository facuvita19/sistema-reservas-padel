USE padel_reservas;

ALTER TABLE reservas
    ADD COLUMN origen ENUM('PERSONAL', 'WEB') NOT NULL DEFAULT 'PERSONAL'
        AFTER usuario_id;

UPDATE reservas
SET origen = 'WEB'
WHERE fecha_vencimiento IS NOT NULL
   OR fecha_expiracion IS NOT NULL;

CREATE INDEX idx_reservas_origen_estado
    ON reservas (origen, estado, fecha_vencimiento);
