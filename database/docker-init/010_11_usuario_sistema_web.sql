USE padel_reservas;

INSERT INTO usuarios (
    nombre_usuario,
    password_hash,
    rol,
    cliente_id,
    activo
)
SELECT
    'web-reservas',
    'CUENTA_SISTEMA_SIN_ACCESO',
    'OPERADOR',
    NULL,
    FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM usuarios
    WHERE nombre_usuario = 'web-reservas'
);
