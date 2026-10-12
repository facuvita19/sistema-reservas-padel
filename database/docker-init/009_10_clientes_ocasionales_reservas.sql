-- reservas-ocasionales-v2-idempotente
-- El esquema principal ya puede contener estas columnas. Cada cambio se aplica
-- solamente cuando la columna correspondiente todavia no existe.

SET @schema_actual := DATABASE();

SET @sql := IF(
    EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_actual AND table_name='reservas' AND column_name='cliente_id'),
    'ALTER TABLE reservas MODIFY COLUMN cliente_id BIGINT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_actual AND table_name='reservas' AND column_name='cliente_ocasional_nombre'),
    'ALTER TABLE reservas ADD COLUMN cliente_ocasional_nombre VARCHAR(150) NULL AFTER cliente_id',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_actual AND table_name='reservas' AND column_name='cliente_ocasional_telefono'),
    'ALTER TABLE reservas ADD COLUMN cliente_ocasional_telefono VARCHAR(40) NULL AFTER cliente_ocasional_nombre',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_actual AND table_name='reservas' AND column_name='cliente_ocasional_email'),
    'ALTER TABLE reservas ADD COLUMN cliente_ocasional_email VARCHAR(150) NULL AFTER cliente_ocasional_telefono',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=@schema_actual AND table_name='reservas' AND column_name='cliente_ocasional_documento'),
    'ALTER TABLE reservas ADD COLUMN cliente_ocasional_documento VARCHAR(40) NULL AFTER cliente_ocasional_email',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
