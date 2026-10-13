-- W2.1C1 - completar asociaciones inequivocas de identidad deportiva.
UPDATE torneo_inscripcion_jugadores tij
INNER JOIN perfiles_jugador pj ON pj.cliente_id = tij.cliente_id
SET tij.perfil_jugador_id = pj.id
WHERE tij.cliente_id IS NOT NULL
  AND tij.perfil_jugador_id IS NULL;
