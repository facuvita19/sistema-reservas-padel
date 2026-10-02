USE padel_reservas;

CREATE TABLE IF NOT EXISTS torneo_partido_enlaces (
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
