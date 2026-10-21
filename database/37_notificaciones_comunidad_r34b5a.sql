USE padel_reservas;

CREATE TABLE IF NOT EXISTS comunidad_notificaciones (
 id BIGINT NOT NULL AUTO_INCREMENT,
 cliente_destinatario_id BIGINT NOT NULL,
 evento_id BIGINT NOT NULL,
 tipo VARCHAR(60) NOT NULL,
 titulo VARCHAR(120) NOT NULL,
 mensaje VARCHAR(300) NOT NULL,
 recurso_tipo VARCHAR(30) NOT NULL,
 recurso_id BIGINT NULL,
 url_contextual VARCHAR(500) NOT NULL,
 fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 fecha_lectura DATETIME NULL,
 version BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY(id),
 UNIQUE KEY uk_comunidad_notificacion_evento_destino(evento_id,cliente_destinatario_id),
 KEY idx_comunidad_notificaciones_bandeja(cliente_destinatario_id,fecha_lectura,fecha_creacion),
 CONSTRAINT fk_comunidad_notificacion_cliente FOREIGN KEY(cliente_destinatario_id) REFERENCES clientes(id) ON DELETE CASCADE ON UPDATE CASCADE,
 CONSTRAINT fk_comunidad_notificacion_evento FOREIGN KEY(evento_id) REFERENCES comunidad_eventos(id) ON DELETE CASCADE ON UPDATE CASCADE,
 CONSTRAINT chk_comunidad_notificacion_recurso CHECK(recurso_tipo IN ('SOLICITUD','CONVOCATORIA','INVITACION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
