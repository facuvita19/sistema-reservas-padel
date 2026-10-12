-- Orden visual configurable de canchas
ALTER TABLE canchas
    ADD COLUMN orden_visual INT NOT NULL DEFAULT 0 AFTER activo;

SET @orden_visual := 0;
UPDATE canchas
SET orden_visual = (@orden_visual := @orden_visual + 1)
ORDER BY id;

CREATE INDEX idx_canchas_orden_visual
    ON canchas (orden_visual, id);
