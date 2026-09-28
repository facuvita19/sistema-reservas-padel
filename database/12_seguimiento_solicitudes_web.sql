USE padel_reservas;

CREATE TABLE IF NOT EXISTS solicitudes_web_seguimiento (
    reserva_id BIGINT NOT NULL,
    codigo_publico CHAR(36) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (reserva_id),
    CONSTRAINT uq_solicitudes_web_codigo UNIQUE (codigo_publico),
    CONSTRAINT fk_solicitudes_web_reserva
        FOREIGN KEY (reserva_id) REFERENCES reservas(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB;

INSERT INTO solicitudes_web_seguimiento (reserva_id, codigo_publico)
SELECT r.id, UUID()
FROM reservas r
LEFT JOIN solicitudes_web_seguimiento s ON s.reserva_id = r.id
WHERE r.origen = 'WEB' AND s.reserva_id IS NULL;

DROP TRIGGER IF EXISTS trg_reservas_web_seguimiento;
DELIMITER $$
CREATE TRIGGER trg_reservas_web_seguimiento
AFTER INSERT ON reservas
FOR EACH ROW
BEGIN
    IF NEW.origen = 'WEB' THEN
        INSERT INTO solicitudes_web_seguimiento (reserva_id, codigo_publico)
        VALUES (NEW.id, UUID());
    END IF;
END$$
DELIMITER ;
