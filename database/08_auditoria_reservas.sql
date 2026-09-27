USE padel_reservas;

CREATE TABLE IF NOT EXISTS auditoria_reservas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reserva_id BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    accion VARCHAR(40) NOT NULL,
    estado_anterior VARCHAR(20) NULL,
    estado_nuevo VARCHAR(20) NULL,
    detalle VARCHAR(1000) NOT NULL,
    fecha_evento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_auditoria_reserva
        FOREIGN KEY (reserva_id) REFERENCES reservas(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX idx_auditoria_reserva_fecha (reserva_id, fecha_evento),
    INDEX idx_auditoria_usuario (usuario_id),
    INDEX idx_auditoria_accion (accion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TRIGGER IF EXISTS trg_reservas_auditoria_insert;
DROP TRIGGER IF EXISTS trg_reservas_auditoria_update;

DELIMITER $$

CREATE TRIGGER trg_reservas_auditoria_insert
AFTER INSERT ON reservas
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_reservas (
        reserva_id, usuario_id, accion, estado_anterior,
        estado_nuevo, detalle, fecha_evento
    ) VALUES (
        NEW.id,
        NEW.usuario_id,
        'CREACION',
        NULL,
        NEW.estado,
        CONCAT(
            'Reserva creada para el ', DATE_FORMAT(NEW.fecha, '%d/%m/%Y'),
            ' de ', TIME_FORMAT(NEW.hora_inicio, '%H:%i'),
            ' a ', TIME_FORMAT(NEW.hora_fin, '%H:%i'),
            '. Importe: ARS ', FORMAT(NEW.precio_total, 2, 'es_AR'), '.'
        ),
        CURRENT_TIMESTAMP
    );
END$$

CREATE TRIGGER trg_reservas_auditoria_update
AFTER UPDATE ON reservas
FOR EACH ROW
BEGIN
    DECLARE v_accion VARCHAR(40);
    DECLARE v_detalle VARCHAR(1000);
    DECLARE v_usuario BIGINT;

    SET v_usuario = COALESCE(NEW.usuario_cancelacion_id, NEW.usuario_id);

    IF NOT (OLD.estado <=> NEW.estado) THEN
        SET v_accion = CASE
            WHEN NEW.estado = 'CONFIRMADA' THEN 'CONFIRMACION'
            WHEN NEW.estado = 'CANCELADA' AND NEW.tipo_cancelacion = 'ADMINISTRATIVA'
                THEN 'CANCELACION_ADMINISTRATIVA'
            WHEN NEW.estado = 'CANCELADA' THEN 'CANCELACION'
            WHEN NEW.estado = 'EXPIRADA' THEN 'EXPIRACION_AUTOMATICA'
            WHEN NEW.estado = 'COMPLETADA' THEN 'CIERRE_COMPLETADA'
            WHEN NEW.estado = 'AUSENTE' THEN 'CIERRE_AUSENTE'
            ELSE 'CAMBIO_ESTADO'
        END;

        IF NEW.estado = 'EXPIRADA' THEN
            SET v_usuario = NULL;
        END IF;

        SET v_detalle = CONCAT(
            'Estado modificado de ', COALESCE(OLD.estado, 'SIN ESTADO'),
            ' a ', COALESCE(NEW.estado, 'SIN ESTADO'),
            CASE
                WHEN NEW.motivo_cancelacion IS NOT NULL
                THEN CONCAT('. Motivo: ', NEW.motivo_cancelacion)
                ELSE ''
            END,
            '.'
        );

        INSERT INTO auditoria_reservas (
            reserva_id, usuario_id, accion, estado_anterior,
            estado_nuevo, detalle, fecha_evento
        ) VALUES (
            NEW.id, v_usuario, v_accion, OLD.estado,
            NEW.estado, v_detalle, CURRENT_TIMESTAMP
        );
    END IF;

    IF NOT (OLD.cancha_id <=> NEW.cancha_id)
       OR NOT (OLD.fecha <=> NEW.fecha)
       OR NOT (OLD.hora_inicio <=> NEW.hora_inicio)
       OR NOT (OLD.hora_fin <=> NEW.hora_fin) THEN

        SET v_accion = CASE
            WHEN NEW.observaciones_administrativas LIKE '%administrativa%'
                THEN 'REPROGRAMACION_ADMINISTRATIVA'
            ELSE 'REPROGRAMACION'
        END;

        SET v_detalle = CONCAT(
            'Reserva reprogramada. Anterior: ',
            DATE_FORMAT(OLD.fecha, '%d/%m/%Y'), ' ',
            TIME_FORMAT(OLD.hora_inicio, '%H:%i'), '-',
            TIME_FORMAT(OLD.hora_fin, '%H:%i'),
            ', cancha ID ', OLD.cancha_id,
            '. Nueva: ', DATE_FORMAT(NEW.fecha, '%d/%m/%Y'), ' ',
            TIME_FORMAT(NEW.hora_inicio, '%H:%i'), '-',
            TIME_FORMAT(NEW.hora_fin, '%H:%i'),
            ', cancha ID ', NEW.cancha_id, '.'
        );

        INSERT INTO auditoria_reservas (
            reserva_id, usuario_id, accion, estado_anterior,
            estado_nuevo, detalle, fecha_evento
        ) VALUES (
            NEW.id, NEW.usuario_id, v_accion, OLD.estado,
            NEW.estado, v_detalle, CURRENT_TIMESTAMP
        );
    ELSEIF NOT (OLD.cliente_id <=> NEW.cliente_id)
       OR NOT (OLD.cantidad_jugadores <=> NEW.cantidad_jugadores)
       OR NOT (OLD.comentarios <=> NEW.comentarios)
       OR NOT (OLD.observaciones_administrativas <=> NEW.observaciones_administrativas)
       OR NOT (OLD.precio_total <=> NEW.precio_total) THEN

        INSERT INTO auditoria_reservas (
            reserva_id, usuario_id, accion, estado_anterior,
            estado_nuevo, detalle, fecha_evento
        ) VALUES (
            NEW.id,
            NEW.usuario_id,
            'MODIFICACION',
            OLD.estado,
            NEW.estado,
            'Se modificaron datos generales de la reserva.',
            CURRENT_TIMESTAMP
        );
    END IF;
END$$

DELIMITER ;
