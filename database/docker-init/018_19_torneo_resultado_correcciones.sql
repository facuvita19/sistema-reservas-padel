USE padel_reservas;

CREATE TABLE IF NOT EXISTS torneo_resultado_correcciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partido_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    ganadora_anterior_inscripcion_id BIGINT NULL,
    ganadora_nueva_inscripcion_id BIGINT NULL,
    resultado_anterior VARCHAR(100) NOT NULL,
    resultado_nuevo VARCHAR(100) NOT NULL,
    sets_anteriores_json JSON NOT NULL,
    sets_nuevos_json JSON NOT NULL,
    fecha_correccion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_torneo_correcciones_partido_fecha (partido_id, fecha_correccion),
    INDEX idx_torneo_correcciones_usuario (usuario_id),
    CONSTRAINT fk_torneo_correcciones_partido
        FOREIGN KEY (partido_id) REFERENCES torneo_partidos(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_torneo_correcciones_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_correcciones_ganadora_anterior
        FOREIGN KEY (ganadora_anterior_inscripcion_id)
        REFERENCES torneo_inscripciones(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_correcciones_ganadora_nueva
        FOREIGN KEY (ganadora_nueva_inscripcion_id)
        REFERENCES torneo_inscripciones(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_torneo_correcciones_motivo
        CHECK (CHAR_LENGTH(TRIM(motivo)) BETWEEN 10 AND 500)
) ENGINE = InnoDB;
