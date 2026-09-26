USE padel_reservas;

CREATE TABLE IF NOT EXISTS reprogramaciones_reserva (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reserva_id BIGINT NOT NULL,
    cancha_anterior_id BIGINT NOT NULL,
    fecha_anterior DATE NOT NULL,
    hora_inicio_anterior TIME NOT NULL,
    hora_fin_anterior TIME NOT NULL,
    cancha_nueva_id BIGINT NOT NULL,
    fecha_nueva DATE NOT NULL,
    hora_inicio_nueva TIME NOT NULL,
    hora_fin_nueva TIME NOT NULL,
    precio_anterior DECIMAL(12,2) NOT NULL,
    precio_nuevo DECIMAL(12,2) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_reprogramacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_reprogramacion_reserva
        FOREIGN KEY (reserva_id) REFERENCES reservas(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_reprogramacion_cancha_anterior
        FOREIGN KEY (cancha_anterior_id) REFERENCES canchas(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_reprogramacion_cancha_nueva
        FOREIGN KEY (cancha_nueva_id) REFERENCES canchas(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_reprogramacion_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_reprogramacion_reserva_fecha
    ON reprogramaciones_reserva (reserva_id, fecha_reprogramacion);
