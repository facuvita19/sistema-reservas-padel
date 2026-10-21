-- R3.4B3A1 - Comunidad: esquema persistente y trazabilidad de solicitudes (MySQL 8.4)
USE padel_reservas;

CREATE TABLE IF NOT EXISTS comunidad_preferencias (
    cliente_id BIGINT NOT NULL,
    buscar_turnos TINYINT(1) NOT NULL DEFAULT 0,
    buscar_torneos TINYINT(1) NOT NULL DEFAULT 0,
    contacto_whatsapp TINYINT(1) NOT NULL DEFAULT 0,
    fecha_consentimiento_whatsapp DATETIME NULL,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (cliente_id),
    CONSTRAINT fk_comunidad_preferencias_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comunidad_convocatorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reserva_id BIGINT NOT NULL,
    cliente_creador_id BIGINT NOT NULL,
    perfil_creador_id BIGINT NOT NULL,
    categoria_deportiva_id BIGINT NULL,
    posicion_buscada VARCHAR(20) NULL,
    participantes_confirmados TINYINT NOT NULL,
    lugares_buscados TINYINT NOT NULL,
    estado VARCHAR(35) NOT NULL DEFAULT 'BORRADOR',
    clave_activa TINYINT NULL,
    comentario VARCHAR(500) NULL,
    motivo_cierre VARCHAR(50) NULL,
    fecha_publicacion DATETIME NULL,
    fecha_cierre DATETIME NULL,
    fecha_expiracion DATETIME NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_comunidad_convocatoria_activa (reserva_id, clave_activa),
    KEY idx_comunidad_convocatorias_publicas (estado, fecha_expiracion, categoria_deportiva_id),
    KEY idx_comunidad_convocatorias_cliente (cliente_creador_id, fecha_creacion),
    CONSTRAINT fk_comunidad_convocatoria_reserva FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_convocatoria_cliente FOREIGN KEY (cliente_creador_id) REFERENCES clientes(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_convocatoria_perfil FOREIGN KEY (perfil_creador_id) REFERENCES perfiles_jugador(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_convocatoria_categoria FOREIGN KEY (categoria_deportiva_id) REFERENCES categorias_deportivas(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_comunidad_convocatoria_confirmados CHECK (participantes_confirmados BETWEEN 1 AND 3),
    CONSTRAINT chk_comunidad_convocatoria_lugares CHECK (lugares_buscados BETWEEN 1 AND 3),
    CONSTRAINT chk_comunidad_convocatoria_total CHECK (participantes_confirmados + lugares_buscados <= 4),
    CONSTRAINT chk_comunidad_convocatoria_clave CHECK ((estado IN ('ABIERTA','EN_COORDINACION') AND clave_activa=1) OR (estado NOT IN ('ABIERTA','EN_COORDINACION') AND clave_activa IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comunidad_solicitudes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    convocatoria_id BIGINT NOT NULL,
    perfil_solicitante_id BIGINT NOT NULL,
    estado VARCHAR(35) NOT NULL DEFAULT 'PENDIENTE',
    mensaje VARCHAR(300) NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_contacto DATETIME NULL,
    fecha_resolucion DATETIME NULL,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_comunidad_solicitud_perfil (convocatoria_id, perfil_solicitante_id),
    KEY idx_comunidad_solicitudes_estado (convocatoria_id, estado, fecha_solicitud),
    KEY idx_comunidad_solicitudes_perfil (perfil_solicitante_id, fecha_solicitud),
    CONSTRAINT fk_comunidad_solicitud_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES comunidad_convocatorias(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_solicitud_perfil FOREIGN KEY (perfil_solicitante_id) REFERENCES perfiles_jugador(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comunidad_participantes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    convocatoria_id BIGINT NOT NULL,
    perfil_jugador_id BIGINT NULL,
    solicitud_origen_id BIGINT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    descripcion_publica VARCHAR(120) NULL,
    origen VARCHAR(25) NOT NULL,
    fecha_confirmacion DATETIME NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_comunidad_participante_perfil (convocatoria_id, perfil_jugador_id),
    UNIQUE KEY uk_comunidad_participante_solicitud (solicitud_origen_id),
    KEY idx_comunidad_participantes_estado (convocatoria_id, estado),
    CONSTRAINT fk_comunidad_participante_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES comunidad_convocatorias(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_participante_perfil FOREIGN KEY (perfil_jugador_id) REFERENCES perfiles_jugador(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_participante_solicitud FOREIGN KEY (solicitud_origen_id) REFERENCES comunidad_solicitudes(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comunidad_eventos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo VARCHAR(60) NOT NULL,
    convocatoria_id BIGINT NULL,
    solicitud_id BIGINT NULL,
    actor_cliente_id BIGINT NULL,
    actor_perfil_id BIGINT NULL,
    detalle_json JSON NULL,
    fecha_evento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_comunidad_eventos_convocatoria (convocatoria_id, fecha_evento),
    KEY idx_comunidad_eventos_solicitud (solicitud_id, fecha_evento),
    KEY idx_comunidad_eventos_tipo (tipo, fecha_evento),
    CONSTRAINT fk_comunidad_evento_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES comunidad_convocatorias(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_evento_solicitud FOREIGN KEY (solicitud_id) REFERENCES comunidad_solicitudes(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_evento_cliente FOREIGN KEY (actor_cliente_id) REFERENCES clientes(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_comunidad_evento_perfil FOREIGN KEY (actor_perfil_id) REFERENCES perfiles_jugador(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @columna_solicitud_origen_existe=(SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_participantes' AND COLUMN_NAME='solicitud_origen_id');
SET @sql_columna_solicitud_origen=IF(@columna_solicitud_origen_existe=0,'ALTER TABLE comunidad_participantes ADD COLUMN solicitud_origen_id BIGINT NULL AFTER perfil_jugador_id','SELECT 1');
PREPARE stmt FROM @sql_columna_solicitud_origen; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @indice_solicitud_origen_existe=(SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_participantes' AND INDEX_NAME='uk_comunidad_participante_solicitud');
SET @sql_indice_solicitud_origen=IF(@indice_solicitud_origen_existe=0,'ALTER TABLE comunidad_participantes ADD UNIQUE KEY uk_comunidad_participante_solicitud (solicitud_origen_id)','SELECT 1');
PREPARE stmt FROM @sql_indice_solicitud_origen; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @fk_solicitud_origen_existe=(SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='comunidad_participantes' AND CONSTRAINT_NAME='fk_comunidad_participante_solicitud');
SET @sql_fk_solicitud_origen=IF(@fk_solicitud_origen_existe=0,'ALTER TABLE comunidad_participantes ADD CONSTRAINT fk_comunidad_participante_solicitud FOREIGN KEY (solicitud_origen_id) REFERENCES comunidad_solicitudes(id) ON DELETE RESTRICT ON UPDATE CASCADE','SELECT 1');
PREPARE stmt FROM @sql_fk_solicitud_origen; EXECUTE stmt; DEALLOCATE PREPARE stmt;
