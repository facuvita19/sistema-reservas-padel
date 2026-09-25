USE padel_reservas;

CREATE TABLE IF NOT EXISTS configuracion_complejo (
    id BIGINT NOT NULL,
    nombre_comercial VARCHAR(120) NOT NULL,
    razon_social VARCHAR(160) NULL,
    direccion VARCHAR(220) NULL,
    telefono VARCHAR(40) NULL,
    whatsapp VARCHAR(40) NULL,
    email VARCHAR(160) NULL,
    instagram VARCHAR(100) NULL,
    moneda VARCHAR(10) NOT NULL DEFAULT 'ARS',
    porcentaje_senia DECIMAL(5,2) NOT NULL DEFAULT 25.00,
    anticipacion_minima_horas INT NOT NULL DEFAULT 2,
    cancelacion_minima_horas INT NOT NULL DEFAULT 12,
    color_principal VARCHAR(7) NOT NULL DEFAULT '#486B86',
    ruta_logo VARCHAR(500) NULL,
    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT chk_configuracion_unica
        CHECK (id = 1),

    CONSTRAINT chk_porcentaje_senia
        CHECK (
            porcentaje_senia >= 0
            AND porcentaje_senia <= 100
        ),

    CONSTRAINT chk_anticipacion_minima
        CHECK (anticipacion_minima_horas >= 0),

    CONSTRAINT chk_cancelacion_minima
        CHECK (cancelacion_minima_horas >= 0)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO configuracion_complejo (
    id,
    nombre_comercial,
    moneda,
    porcentaje_senia,
    anticipacion_minima_horas,
    cancelacion_minima_horas,
    color_principal
)
VALUES (
    1,
    'Padel Reservas',
    'ARS',
    25.00,
    2,
    12,
    '#486B86'
)
ON DUPLICATE KEY UPDATE
    porcentaje_senia = 25.00;