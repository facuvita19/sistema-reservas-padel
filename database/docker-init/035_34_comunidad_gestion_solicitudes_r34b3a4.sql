-- R3.4B3A4 - permite cerrar una convocatoria con cuatro participantes.
USE padel_reservas;
SET @chk_confirmados_existe=(SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_convocatorias' AND CONSTRAINT_NAME='chk_comunidad_convocatoria_confirmados');
SET @sql_drop_chk=IF(@chk_confirmados_existe=1,'ALTER TABLE comunidad_convocatorias DROP CHECK chk_comunidad_convocatoria_confirmados','SELECT 1');
PREPARE stmt FROM @sql_drop_chk; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE comunidad_convocatorias ADD CONSTRAINT chk_comunidad_convocatoria_confirmados CHECK(participantes_confirmados BETWEEN 1 AND 4);
