-- Reparacion idempotente para bases existentes creadas antes de R3.4B3A1.
USE padel_reservas;

SET @columna_existe=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_participantes' AND COLUMN_NAME='solicitud_origen_id');
SET @sql_columna=IF(@columna_existe=0,'ALTER TABLE comunidad_participantes ADD COLUMN solicitud_origen_id BIGINT NULL AFTER perfil_jugador_id','SELECT 1');
PREPARE stmt FROM @sql_columna; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @indice_existe=(SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_participantes' AND INDEX_NAME='uk_comunidad_participante_solicitud');
SET @sql_indice=IF(@indice_existe=0,'ALTER TABLE comunidad_participantes ADD UNIQUE KEY uk_comunidad_participante_solicitud (solicitud_origen_id)','SELECT 1');
PREPARE stmt FROM @sql_indice; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk_existe=(SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_participantes' AND CONSTRAINT_NAME='fk_comunidad_participante_solicitud');
SET @sql_fk=IF(@fk_existe=0,'ALTER TABLE comunidad_participantes ADD CONSTRAINT fk_comunidad_participante_solicitud FOREIGN KEY (solicitud_origen_id) REFERENCES comunidad_solicitudes(id) ON DELETE RESTRICT ON UPDATE CASCADE','SELECT 1');
PREPARE stmt FROM @sql_fk; EXECUTE stmt; DEALLOCATE PREPARE stmt;
