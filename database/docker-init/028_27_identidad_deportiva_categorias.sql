USE padel_reservas;

CREATE TABLE IF NOT EXISTS categorias_deportivas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    nivel_orden INT NOT NULL,
    color_hex CHAR(7) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_categorias_deportivas_codigo UNIQUE (codigo),
    CONSTRAINT uq_categorias_deportivas_nivel UNIQUE (nivel_orden),
    CONSTRAINT chk_categorias_deportivas_nivel CHECK (nivel_orden BETWEEN 1 AND 99),
    CONSTRAINT chk_categorias_deportivas_color CHECK (color_hex REGEXP '^#[0-9A-Fa-f]{6}$'),
    INDEX idx_categorias_deportivas_activas (activo, nivel_orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categorias_deportivas (codigo,nombre,nivel_orden,color_hex,activo) VALUES
('1RA','1.ª',1,'#D7192D',TRUE),('2DA','2.ª',2,'#E94152',TRUE),
('3RA','3.ª',3,'#F06A75',TRUE),('4TA','4.ª',4,'#F2939B',TRUE),
('5TA','5.ª',5,'#C54B5A',TRUE),('6TA','6.ª',6,'#9E3A47',TRUE),
('7MA','7.ª',7,'#742A35',TRUE),('8VA','8.ª',8,'#4D1C24',TRUE)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre),color_hex=VALUES(color_hex),activo=TRUE;

CREATE TABLE IF NOT EXISTS perfiles_jugador (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NULL,
    estado ENUM('DEFINITIVO','PROVISIONAL') NOT NULL,
    nombre_publico VARCHAR(220) NOT NULL,
    posicion_preferida ENUM('DRIVE','REVES') NULL,
    disponible_para_jugar BOOLEAN NOT NULL DEFAULT FALSE,
    acepta_invitaciones BOOLEAN NOT NULL DEFAULT FALSE,
    vinculacion_revisada BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_revision_id BIGINT NULL,
    fecha_revision DATETIME NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_perfiles_jugador_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_perfiles_jugador_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_perfiles_jugador_revision FOREIGN KEY (usuario_revision_id) REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_perfiles_jugador_identidad CHECK ((estado='DEFINITIVO' AND cliente_id IS NOT NULL) OR (estado='PROVISIONAL' AND cliente_id IS NULL)),
    CONSTRAINT chk_perfiles_jugador_revision CHECK ((vinculacion_revisada=FALSE AND usuario_revision_id IS NULL AND fecha_revision IS NULL) OR (vinculacion_revisada=TRUE AND usuario_revision_id IS NOT NULL AND fecha_revision IS NOT NULL)),
    INDEX idx_perfiles_jugador_estado (activo,estado),
    INDEX idx_perfiles_jugador_nombre (nombre_publico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS jugador_categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    perfil_jugador_id BIGINT NOT NULL,
    categoria_deportiva_id BIGINT NOT NULL,
    vigente_principal TINYINT GENERATED ALWAYS AS (CASE WHEN estado='VIGENTE' THEN 1 ELSE NULL END) STORED,
    fecha_desde DATE NOT NULL,
    fecha_hasta DATE NULL,
    estado ENUM('PROVISIONAL','VIGENTE','SUSPENDIDA','FINALIZADA') NOT NULL,
    origen ENUM('ADMINISTRATIVA','RESULTADOS','IMPORTADA','PROVISIONAL') NOT NULL,
    motivo VARCHAR(300) NOT NULL,
    observaciones VARCHAR(1000) NULL,
    usuario_responsable_id BIGINT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_jugador_categoria_vigente UNIQUE (perfil_jugador_id,vigente_principal),
    CONSTRAINT fk_jugador_categorias_perfil FOREIGN KEY (perfil_jugador_id) REFERENCES perfiles_jugador(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_jugador_categorias_categoria FOREIGN KEY (categoria_deportiva_id) REFERENCES categorias_deportivas(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_jugador_categorias_usuario FOREIGN KEY (usuario_responsable_id) REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_jugador_categorias_fechas CHECK (fecha_hasta IS NULL OR fecha_hasta >= fecha_desde),
    CONSTRAINT chk_jugador_categorias_vigencia CHECK ((estado='VIGENTE' AND fecha_hasta IS NULL) OR estado<>'VIGENTE'),
    INDEX idx_jugador_categorias_historial (perfil_jugador_id,fecha_desde DESC,id DESC),
    INDEX idx_jugador_categorias_publicas (categoria_deportiva_id,estado,fecha_desde)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @columna_perfil_existe=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='torneo_inscripcion_jugadores' AND COLUMN_NAME='perfil_jugador_id');
SET @sql_columna=IF(@columna_perfil_existe=0,'ALTER TABLE torneo_inscripcion_jugadores ADD COLUMN perfil_jugador_id BIGINT NULL AFTER cliente_id','SELECT 1');
PREPARE stmt FROM @sql_columna; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @indice_perfil_existe=(SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='torneo_inscripcion_jugadores' AND INDEX_NAME='idx_torneo_jugadores_perfil');
SET @sql_indice=IF(@indice_perfil_existe=0,'ALTER TABLE torneo_inscripcion_jugadores ADD INDEX idx_torneo_jugadores_perfil (perfil_jugador_id,inscripcion_id)','SELECT 1');
PREPARE stmt FROM @sql_indice; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @fk_perfil_existe=(SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='torneo_inscripcion_jugadores' AND CONSTRAINT_NAME='fk_torneo_jugadores_perfil');
SET @sql_fk=IF(@fk_perfil_existe=0,'ALTER TABLE torneo_inscripcion_jugadores ADD CONSTRAINT fk_torneo_jugadores_perfil FOREIGN KEY (perfil_jugador_id) REFERENCES perfiles_jugador(id) ON UPDATE CASCADE ON DELETE RESTRICT','SELECT 1');
PREPARE stmt FROM @sql_fk; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO perfiles_jugador (cliente_id,estado,nombre_publico,posicion_preferida,vinculacion_revisada,activo)
SELECT DISTINCT c.id,'DEFINITIVO',TRIM(CONCAT(c.nombre,' ',c.apellido)),c.posicion_preferida,FALSE,c.activo
FROM clientes c INNER JOIN torneo_inscripcion_jugadores tij ON tij.cliente_id=c.id
ON DUPLICATE KEY UPDATE nombre_publico=VALUES(nombre_publico),posicion_preferida=VALUES(posicion_preferida),activo=VALUES(activo);

UPDATE torneo_inscripcion_jugadores tij INNER JOIN perfiles_jugador pj ON pj.cliente_id=tij.cliente_id
SET tij.perfil_jugador_id=pj.id WHERE tij.cliente_id IS NOT NULL AND tij.perfil_jugador_id IS NULL;
