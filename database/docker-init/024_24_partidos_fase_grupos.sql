USE padel_reservas;

ALTER TABLE torneo_partidos
    MODIFY COLUMN fase ENUM(
        'GRUPOS',
        'DIECISEISAVOS',
        'OCTAVOS',
        'CUARTOS',
        'SEMIFINAL',
        'FINAL'
    ) COLLATE utf8mb4_unicode_ci NOT NULL,
    ADD COLUMN grupo_id BIGINT NULL AFTER torneo_categoria_id,
    ADD COLUMN tipo_partido_grupo ENUM(
        'TODOS_CONTRA_TODOS',
        'CRUCE_INICIAL',
        'DEFINICION_PRIMERO_SEGUNDO',
        'DEFINICION_TERCERO_CUARTO'
    ) COLLATE utf8mb4_unicode_ci NULL AFTER fase,
    ADD INDEX idx_torneo_partidos_grupo (grupo_id, orden_fase),
    ADD CONSTRAINT fk_torneo_partidos_grupo
        FOREIGN KEY (grupo_id)
        REFERENCES torneo_grupos(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT;

CREATE TABLE torneo_partido_enlaces (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partido_origen_id BIGINT NOT NULL,
    resultado_origen ENUM('GANADOR', 'PERDEDOR')
        COLLATE utf8mb4_unicode_ci NOT NULL,
    partido_destino_id BIGINT NOT NULL,
    posicion_destino ENUM('PAREJA_1', 'PAREJA_2')
        COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_partido_enlace_origen_resultado_destino (
        partido_origen_id,
        resultado_origen,
        partido_destino_id
    ),
    UNIQUE KEY uq_partido_enlace_destino_posicion (
        partido_destino_id,
        posicion_destino
    ),
    CONSTRAINT fk_partido_enlace_origen
        FOREIGN KEY (partido_origen_id)
        REFERENCES torneo_partidos(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_partido_enlace_destino
        FOREIGN KEY (partido_destino_id)
        REFERENCES torneo_partidos(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
