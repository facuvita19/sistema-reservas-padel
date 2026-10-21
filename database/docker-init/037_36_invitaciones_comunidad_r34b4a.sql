-- R3.4B4A - invitaciones entre perfiles de Comunidad (MySQL 8.4)
USE padel_reservas;

CREATE TABLE IF NOT EXISTS comunidad_invitaciones (
 id BIGINT NOT NULL AUTO_INCREMENT,
 perfil_emisor_id BIGINT NOT NULL,
 perfil_destinatario_id BIGINT NOT NULL,
 objetivo VARCHAR(35) NOT NULL,
 estado VARCHAR(25) NOT NULL DEFAULT 'PENDIENTE',
 clave_pendiente TINYINT GENERATED ALWAYS AS (CASE WHEN estado='PENDIENTE' THEN 1 ELSE NULL END) STORED,
 mensaje VARCHAR(300) NULL,
 fecha_invitacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 fecha_resolucion DATETIME NULL,
 fecha_expiracion DATETIME NOT NULL,
 fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 version BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY(id),
 UNIQUE KEY uk_comunidad_invitacion_pendiente(perfil_emisor_id,perfil_destinatario_id,objetivo,clave_pendiente),
 KEY idx_comunidad_invitaciones_destino(estado,perfil_destinatario_id,fecha_invitacion),
 KEY idx_comunidad_invitaciones_emisor(perfil_emisor_id,fecha_invitacion),
 CONSTRAINT fk_comunidad_invitacion_emisor FOREIGN KEY(perfil_emisor_id) REFERENCES perfiles_jugador(id) ON DELETE RESTRICT ON UPDATE CASCADE,
 CONSTRAINT fk_comunidad_invitacion_destino FOREIGN KEY(perfil_destinatario_id) REFERENCES perfiles_jugador(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_comunidad_invitacion_objetivo CHECK(objetivo IN ('COMPLETAR_PARTIDO','FORMAR_PAREJA_TORNEO')),
 CONSTRAINT chk_comunidad_invitacion_estado CHECK(estado IN ('PENDIENTE','ACEPTADA','RECHAZADA','CANCELADA','VENCIDA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @col_inv=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_eventos' AND COLUMN_NAME='invitacion_id');
SET @sql_inv=IF(@col_inv=0,'ALTER TABLE comunidad_eventos ADD COLUMN invitacion_id BIGINT NULL AFTER solicitud_id','SELECT 1');
PREPARE stmt FROM @sql_inv; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @idx_inv=(SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_eventos' AND INDEX_NAME='idx_comunidad_eventos_invitacion');
SET @sql_idx_inv=IF(@idx_inv=0,'ALTER TABLE comunidad_eventos ADD INDEX idx_comunidad_eventos_invitacion (invitacion_id,fecha_evento)','SELECT 1');
PREPARE stmt FROM @sql_idx_inv; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk_inv=(SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_eventos' AND CONSTRAINT_NAME='fk_comunidad_evento_invitacion');
SET @sql_fk_inv=IF(@fk_inv=0,'ALTER TABLE comunidad_eventos ADD CONSTRAINT fk_comunidad_evento_invitacion FOREIGN KEY(invitacion_id) REFERENCES comunidad_invitaciones(id) ON DELETE SET NULL ON UPDATE CASCADE','SELECT 1');
PREPARE stmt FROM @sql_fk_inv; EXECUTE stmt; DEALLOCATE PREPARE stmt;
