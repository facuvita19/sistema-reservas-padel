-- orden-visual-canchas-v2-idempotente
SET @schema_actual := DATABASE();

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_actual AND table_name='canchas' AND column_name='orden_visual'),
    'ALTER TABLE canchas ADD COLUMN orden_visual INT NOT NULL DEFAULT 0 AFTER activo',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @orden_visual := 0;
UPDATE canchas
SET orden_visual = (@orden_visual := @orden_visual + 1)
WHERE orden_visual = 0
ORDER BY id;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=@schema_actual AND table_name='canchas' AND index_name='idx_canchas_orden_visual'),
    'CREATE INDEX idx_canchas_orden_visual ON canchas (orden_visual, id)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
