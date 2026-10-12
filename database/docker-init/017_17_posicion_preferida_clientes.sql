-- 17_posicion_preferida_clientes.sql
-- Agrega una posicion preferida opcional al perfil del cliente.
-- Valores admitidos: DRIVE, REVES o NULL.

SET @columna_existe = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND COLUMN_NAME = 'posicion_preferida'
);

SET @agregar_columna = IF(
    @columna_existe = 0,
    'ALTER TABLE clientes ADD COLUMN posicion_preferida VARCHAR(10) NULL AFTER email',
    'SELECT ''La columna posicion_preferida ya existe'' AS resultado'
);

PREPARE sentencia_columna FROM @agregar_columna;
EXECUTE sentencia_columna;
DEALLOCATE PREPARE sentencia_columna;

SET @restriccion_existe = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'clientes'
      AND CONSTRAINT_NAME = 'chk_clientes_posicion_preferida'
      AND CONSTRAINT_TYPE = 'CHECK'
);

SET @agregar_restriccion = IF(
    @restriccion_existe = 0,
    'ALTER TABLE clientes ADD CONSTRAINT chk_clientes_posicion_preferida CHECK (posicion_preferida IS NULL OR posicion_preferida IN (''DRIVE'', ''REVES''))',
    'SELECT ''La restriccion chk_clientes_posicion_preferida ya existe'' AS resultado'
);

PREPARE sentencia_restriccion FROM @agregar_restriccion;
EXECUTE sentencia_restriccion;
DEALLOCATE PREPARE sentencia_restriccion;
