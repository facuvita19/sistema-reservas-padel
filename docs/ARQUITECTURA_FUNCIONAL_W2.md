# Arquitectura funcional W2

> Especificación funcional y técnica vigente de Plataforma Deportiva W2. Actualizada el 17 de octubre de 2026 después del cierre integral de Torneos.

## 1. Objetivo

Construir una plataforma deportiva global sobre clientes, cuentas, torneos, inscripciones, partidos, reservas y pagos, conservando historia y evitando personas duplicadas.

## 2. Fronteras del sistema

- Administración JavaFX: aplicación independiente.
- API Java: contratos, seguridad y reglas de negocio.
- MySQL: fuente persistente.
- `web-publica-v2`: única web activa y base de W2.
- `api.ts` y sesión global: infraestructura compartida, no duplicada.
- Rutas heredadas: se retiran solo después de paridad y redirección.

## 3. Identidad

```text
clientes 1 ----- 0..1 usuarios
clientes 1 ----- 0..1 perfiles_jugador
perfiles_jugador 1 ----- N jugador_categorias
perfiles_jugador 1 ----- N ranking_movimientos
torneo_inscripcion_jugadores N ----- 0..1 perfiles_jugador
```

El teléfono normalizado sirve como señal de vinculación, nunca como autorización automática irreversible.

## 4. Dominio deportivo

### Categorías

Catálogo 1.ª a 8.ª con orden explícito, vigencia, estado e historial.

### Elegibilidad

Tipo de condición:

- CATEGORIA_UNICA;
- CATEGORIAS_COMBINADAS;
- SUMA_CATEGORIAS;
- REQUIERE_REVISION o excepción administrativa.

Americano no pertenece a elegibilidad.

Resultado de evaluación:

- COMPATIBLE;
- REQUIERE_REVISION;
- AUTORIZADA_EXCEPCION;
- RECHAZADA_CATEGORIA.

### Taxonomía de competencia

```text
Torneo
├── tipo de evento: estándar interno o AMERICANO
├── dinámica: MISMO_DIA o VARIOS_DIAS
└── categorías
    ├── rama: LIBRE, FEMENINO, MIXTO
    ├── condición: única, combinada o SUMA_CATEGORIAS
    ├── formato: ELIMINACION_DIRECTA o GRUPOS_ELIMINACION
    └── modalidad: UN_SET, DOS_SETS_SUPER_TIE_BREAK o MEJOR_DE_TRES
```

La interfaz no debe mostrar “Convencional”. El tipo estándar puede ser interno y sin distintivo público.

Todas las inscripciones son por pareja.

## 5. Torneos web

Ruta canónica:

```text
/w2/torneos
```

Niveles de navegación:

1. catálogo;
2. detalle;
3. centro competitivo por categoría;
4. vista: resumen, posiciones, partidos o cuadro;
5. ronda cuando corresponde.

El estado relevante persiste en URL y `popstate`.

### Arquitectura actual del frontend

`src/scripts/torneos.ts` actua como composition root. El directorio `src/scripts/torneos/` separa dominio, estado del catalogo, adaptacion de API, datos y cache, presentacion, interaccion, coordinacion del detalle, navegacion, inscripcion, arranque y retorno. `centro-competitivo-premium.ts` conserva el motor unico del centro; `cuadro-torneo.ts` controla la llave completa; `w2-tournaments.css` es la capa visual autoritativa.

Decisiones de cierre:

- `w2-tournaments.css` se carga despues de `global.css`;
- `global.css` conserva infraestructura historica compartida y no se limpia desde una seccion aislada;
- `.premium-*` y `.f4-*` siguen siendo contratos productivos y de pruebas;
- reducir `!important`, modularizar `global.css` y extraer globalmente el sistema segmentado son deuda transversal;
- esa deuda no bloquea Torneos porque la cobertura especifica e integral esta verde.

### Reglas de datos asíncronos

- una solicitud nueva cancela la anterior del mismo alcance;
- una respuesta obsoleta nunca actualiza la interfaz;
- caché temporal por catálogo, detalle y categoría;
- recarga forzada omite caché;
- una inscripción confirmada invalida catálogo y detalle relacionado;
- skeleton y composición final deben ser atómicos.

## 6. Ranking e historial

Ranking individual, por categoría, auditable y versionado. Ventana móvil prevista de 12 meses. Publicaciones históricas inmutables y correcciones transaccionales.

El historial se deriva desde partido, inscripción, integrantes y perfil. Las proyecciones de rendimiento deben ser reconstruibles.

## 7. API pública

Contratos implementados o activos:

```text
GET /api/publica/torneos
GET /api/publica/torneos/{id}
GET /api/publica/torneos/{id}/categorias/{categoriaId}/competencia
GET /api/publica/rankings/categorias
GET /api/publica/rankings
GET /api/publica/rankings/auditoria
GET /api/publica/jugadores/{id}
GET /api/publica/cliente/perfil-deportivo
```

Las rutas privadas usan cookie de sesión. Los DTO públicos nunca exponen teléfono, correo, documento, historial comercial ni pagos.

## 8. Pagos

Separación obligatoria entre orden externa, evento del proveedor, pago interno, caja y reembolso.

Flujo implementado en sandbox:

1. reserva temporal;
2. elección de pago;
3. orden idempotente;
4. preferencia;
5. webhook auditable;
6. consulta y validación del proveedor;
7. acreditación transaccional;
8. conciliación defensiva.

Producción requiere dominio estable, HTTPS, credenciales productivas, observabilidad, pago controlado y rollback.

## 9. Migraciones

- Toda migración debe existir también en `database/docker-init`.
- Instalación limpia y actualización deben llegar al mismo esquema.
- No borrar historia.
- No modificar la base principal durante pruebas.
- La taxonomía de Torneos se incorporará en migraciones explícitas y compatibles, no mediante reinterpretaciones silenciosas.

## 10. Modernización técnica

Adoptar herramientas modernas solo con beneficio explícito. Prioridades:

- componentes reutilizables;
- contratos tipados;
- carga progresiva;
- AVIF/WebP y `srcset`;
- View Transitions con degradación segura;
- PWA y notificaciones en su fase;
- observabilidad sin datos personales;
- pruebas visuales y E2E para flujos críticos.

## 11. Riesgos y mitigaciones

- duplicación de jugadores: revisión humana;
- pérdida histórica: no sobrescribir fotografías de inscripción;
- ranking doble: claves únicas por origen y regla;
- respuestas asíncronas obsoletas: abortos y versiones;
- exposición privada: DTO públicos específicos;
- formatos rígidos: dimensiones independientes y configuración congelada;
- pagos duplicados: idempotencia y conciliación;
- documentación obsoleta: actualización al cerrar bloques significativos.
