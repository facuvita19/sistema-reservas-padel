# Arquitectura funcional W2

> Especificación funcional y técnica base de Plataforma Deportiva W2. No representa todavía una migración aplicada.

## 1. Objetivo

Crear una capa deportiva global sobre clientes, cuentas, torneos, inscripciones, partidos, reservas y pagos existentes, conservando toda la historia y evitando personas duplicadas.

## 2. Decisiones estructurales

### 2.0 Convergencia del frontend

- `web-publica-v2` es la única web activa y la base de la evolución W2.
- `W2Layout.astro` debe converger con la autenticación, cuenta y navegación funcionales existentes.
- `api.ts` y `sesion-global.ts` son infraestructura compartida; no se duplicarán clientes HTTP ni estados de sesión.
- Las páginas actuales se mantienen hasta que su reemplazo W2 apruebe paridad funcional y E2E.
- Componentes de encabezado, menú, formularios y estados deben extraerse o refactorizarse para reutilización.

### 2.1 Identidad única

- `clientes` continúa siendo la persona comercial central.
- `usuarios` mantiene autenticación, rol, contraseña y sesión.
- `perfiles_jugador` agrega identidad deportiva sin duplicar nombre, documento, correo ni teléfono.
- `torneo_inscripcion_jugadores` conserva la fotografía histórica con la que una persona participó.
- Un participante sin cuenta puede tener una identidad deportiva provisional.
- La vinculación posterior conserva puntos, torneos y partidos previos.

Relación objetivo:

```text
clientes 1 ----- 0..1 usuarios
clientes 1 ----- 0..1 perfiles_jugador
perfiles_jugador 1 ----- N jugador_categorias
perfiles_jugador 1 ----- N ranking_movimientos
torneo_inscripcion_jugadores N ----- 0..1 perfiles_jugador
```

### 2.2 Perfil deportivo

Campos previstos:

- identificador;
- cliente vinculado opcional;
- estado DEFINITIVO o PROVISIONAL;
- nombre público derivado o histórico;
- posición preferida;
- mano dominante pendiente de definición;
- disponibilidad para jugar;
- aceptación de invitaciones;
- fechas de creación y actualización;
- estado activo;
- metadatos de vinculación revisada.

El teléfono normalizado se usa como señal para proponer vínculos, nunca como autorización automática irreversible.

## 3. Categorías globales

### 3.1 Catálogo

Valores iniciales: 1.ª, 2.ª, 3.ª, 4.ª, 5.ª, 6.ª, 7.ª y 8.ª.

Cada categoría define código, nombre, nivel ordenado, color, estado y vigencia. El orden competitivo es explícito: 1.ª es superior a 8.ª.

### 3.2 Historial

`jugador_categorias` conserva:

- perfil;
- categoría;
- vigencia desde y hasta;
- estado PROVISIONAL, VIGENTE, SUSPENDIDA o FINALIZADA;
- origen ADMINISTRATIVA, RESULTADOS, IMPORTADA o PROVISIONAL;
- motivo;
- usuario responsable;
- fecha de registro.

Solo una asignación VIGENTE será la categoría principal, salvo una futura decisión explícita de admitir múltiples categorías simultáneas.

### 3.3 Publicación de cambios

`publicaciones_cambios_categoria` agrupa ascensos y descensos con título, fecha de publicación, vigencia y detalle. La publicación informa, pero no ejecuta automáticamente decisiones deportivas.

## 4. Elegibilidad de torneos

### 4.1 Tipos

- CATEGORIA_UNICA;
- CATEGORIAS_COMBINADAS;
- SUMA_CATEGORIAS;
- ABIERTO_CON_REVISION;
- AMERICANO.

### 4.2 Evaluación

El sistema genera un resultado:

- COMPATIBLE;
- REQUIERE_REVISION;
- AUTORIZADA_EXCEPCION;
- RECHAZADA_CATEGORIA.

El organizador tiene decisión final. Una excepción registra motivo, usuario, fecha y categorías comparadas, sin modificar la categoría personal.

### 4.3 Suma configurable

La regla admite valor objetivo, suma exacta o rango, niveles mínimo y máximo y excepción administrativa. Ejemplo: Suma 12 acepta 6.ª + 6.ª si el valor numérico de ambas categorías totaliza 12.

## 5. Formatos de competencia y partido

### 5.1 Formatos

- eliminación directa;
- grupos más eliminación;
- Americano configurable;
- futuras variantes sin alterar resultados históricos.

### 5.2 Configuración de partido

- cantidad de sets;
- juegos por set;
- diferencia mínima;
- tie-break;
- super tie-break decisivo;
- reglas de retiro, ausencia y walkover;
- puntuación y desempate del Americano;
- clasificación individual o por pareja.

La configuración efectiva queda congelada en el torneo o categoría para conservar interpretación histórica.

## 6. Historial competitivo

La primera versión se deriva desde datos existentes:

```text
partido -> inscripción -> integrantes -> perfil jugador
```

Debe entregar torneos, pareja, categoría, grupo, posición, partidos, sets, rivales, fase alcanzada, títulos y cronología.

No se copiarán resultados a tablas redundantes salvo que sea necesario congelar una publicación o mejorar rendimiento con una proyección reconstruible.

## 7. Ranking

### 7.1 Principios

- individual;
- ventana móvil de 12 meses;
- misma acreditación para ambos integrantes;
- puntos por fase;
- walkovers según regla publicada;
- recálculo auditado ante correcciones;
- participantes provisionales incluidos;
- publicación histórica inmutable.

### 7.2 Entidades

- `temporadas_ranking`;
- `reglas_ranking` versionadas;
- `ranking_movimientos`;
- `publicaciones_ranking`;
- `ranking_posiciones`.

### 7.3 Flujo

1. Finalización o corrección del torneo.
2. Generación preliminar.
3. Validación de integridad.
4. Revisión administrativa.
5. Publicación.
6. Actualización de posiciones y variaciones.
7. Notificación futura.

### 7.4 Masters

El Master pertenece a una categoría, toma clasificados del ranking y conserva cupos, formato y puntos configurables. El organizador confirma la nómina final.

## 8. Contratos preliminares de API

Rutas previstas, sujetas a diseño final:

```text
GET  /api/publica/rankings/categorias                  [implementado]
GET  /api/publica/rankings?categoriaId=                 [implementado inicial]
GET  /api/publica/rankings/auditoria?categoriaId=&perfilId= [implementado]
GET  /api/publica/jugadores/{id}                        [implementado]
GET  /api/publica/rankings/mi-posicion                  [pendiente]
GET  /api/publica/cliente/perfil-deportivo              [pendiente W2.4]
PUT  /api/publica/cliente/perfil-deportivo              [pendiente W2.4]
GET  /api/publica/cambios-categoria
GET  /api/publica/convocatorias
POST /api/publica/convocatorias
```

Las rutas privadas requieren cookie de sesión. Las públicas nunca entregan teléfono, correo, documento, datos comerciales ni pagos.

## 9. Pagos externos

Separación obligatoria:

- orden externa;
- evento del proveedor;
- pago interno;
- movimiento de caja;
- reembolso o contracargo.

La confirmación se produce por backend después de validar proveedor, orden, importe, moneda y estado. La operación es idempotente y transaccional.

## 10. Migraciones previstas

Orden preliminar:

1. catálogo y perfiles deportivos;
2. historial de categorías y publicaciones;
3. vínculo de participantes históricos;
4. reglas de elegibilidad y formatos;
5. temporadas y ranking;
6. convocatorias;
7. órdenes y eventos de pago externo.

Cada migración deberá existir también en `database/docker-init`, ser idempotente cuando corresponda y aprobar instalación limpia y restauración.

## 11. Riesgos y mitigación

- Duplicación de jugadores: propuestas revisadas, no fusiones automáticas.
- Pérdida histórica: datos de inscripción no se sobrescriben.
- Ranking doble: claves únicas por origen, jugador y regla.
- Recalculo inconsistente: transacciones y versión de regla.
- Exposición privada: DTO públicos específicos.
- Formatos rígidos: configuración congelada por torneo.
- Pagos duplicados: idempotencia y conciliación.

## 12. Primer bloque funcional W2.1

El primer cambio de base contendrá únicamente catálogo 1.ª a 8.ª, perfiles deportivos, historial de categorías y servicios de administración. Ranking, comunidad y pagos quedarán fuera de ese commit para mantener alcance controlado.


## 13. Criterios de modernización técnica

Las tecnologías nuevas se incorporarán con evaluación explícita de beneficio, madurez, seguridad, compatibilidad, accesibilidad, rendimiento y costo operativo. Se priorizan:

- arquitectura por componentes reutilizables;
- islas interactivas solo donde aporten valor;
- carga progresiva, streaming o renderizado híbrido cuando el despliegue lo justifique;
- imágenes AVIF/WebP, srcset, lazy loading y presupuestos de rendimiento;
- View Transitions con degradación segura cuando mejoren continuidad;
- PWA, notificaciones y sincronización resiliente en su fase prevista;
- observabilidad de experiencia y errores sin exponer datos personales;
- pruebas visuales y E2E para flujos críticos.

No se adoptará una herramienta por novedad. La innovación debe reducir fricción, elevar calidad o habilitar una capacidad diferencial sostenible.
