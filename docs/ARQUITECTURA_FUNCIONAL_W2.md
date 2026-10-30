# Arquitectura funcional W2

## Estado autoritativo · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

### Cierre de presentación por rutas

La arquitectura funcional vigente conserva Astro multipágina, API pública independiente y progresive enhancement. La convergencia visual de las rutas principales de escritorio quedó completada en su alcance actual. Rankings se publicó en `18d838b` y Comunidad en `010ca8e`.

### Próxima evolución arquitectónica

El siguiente bloque es transversal y no altera los contratos de dominio: navegación interna fluida entre documentos, View Transitions con degradación segura, persistencia del shell global cuando sea técnicamente apropiado y restauración predecible de scroll y foco. La solución debe preservar URLs canónicas, historial del navegador, accesibilidad, sesión, tema, CSP, carga directa y funcionamiento sin JavaScript mejorado.


<!-- W2-CIERRE-C143387-INICIO -->
## Estado autoritativo · 29 de octubre de 2026

El bloque **Modernizar Mi cuenta y estabilizar cabecera y torneos** quedó publicado en `master` mediante el commit `c143387`. Este estado reemplaza cualquier continuidad anterior dentro de este documento que todavía indique que Mi cuenta está pendiente, que Datos personales o Seguridad redirigen a rutas heredadas, que Torneos sigue únicamente en U1/U2 o que Capacitor constituye la siguiente evolución móvil.

### Cierre funcional y visual

- Mi cuenta converge como centro privado único con **Resumen, Reservas, Datos personales, Seguridad y Perfil deportivo**.
- Resumen publica próxima reserva, recorrido en el club y accesos coherentes con la identidad W2.
- Reservas diferencia próximas e historial, conserva detalle profundo en URL y ofrece consulta estable mediante código de seguimiento.
- Datos personales edita, valida, guarda y descarta cambios dentro de Mi cuenta; aceptar la salida con cambios pendientes restaura el último estado confirmado y cierra el modo edición.
- Seguridad actualiza la contraseña dentro del mismo centro privado.
- Los CTA de Reservar y Consultar comparten geometría, profundidad, foco, estados y movimiento reducido; la consulta mantiene la geometría durante la carga.
- La cabecera H3, el menú de cuenta, el primer frame del tema y la navegación competitiva móvil quedaron estabilizados.
- Torneos conserva catálogo, detalle, inscripción y centro competitivo; Resumen, Posiciones, Partidos y Cuadro mantienen navegación móvil contenida y accesible.

### Validación confirmada

- Astro Check: **0 errores, 0 advertencias y 0 hints**.
- Suite focalizada de Mi cuenta: **78 pruebas aprobadas**.
- Saneamiento de cabecera y Torneos: **148 pruebas aprobadas**, seguido por **3 pruebas focalizadas finales aprobadas**.
- `git diff --check`: correcto; los avisos LF/CRLF de Windows son informativos.
- Commit y push completados sobre `master`; repositorio limpio al cierre.

### Continuidad vigente

La prioridad vuelve a la **reconstrucción premium de la web pública de escritorio**. Inicio continúa como referencia visual autoritativa y Mi cuenta queda cerrada en su alcance actual. El próximo frente debe elegirse por impacto visual y funcional entre las rutas públicas aún pendientes de convergencia, sin volver automáticamente a la antigua auditoría integral de rendimiento. La aplicación móvil definitiva será un producto independiente y se evaluará después de estabilizar la web de escritorio.
<!-- W2-CIERRE-C143387-FIN -->

### Presentación privada de Mi cuenta

- `src/pages/w2/cuenta.astro`: composición del centro privado y sus cinco paneles.
- `src/w2/scripts/cuenta.ts`: navegación, reservas, pagos, perfil deportivo y Comunidad.
- `src/w2/scripts/cuenta-datos.ts`: carga, edición, validación, descarte y actualización de identidad.
- `src/w2/scripts/cuenta-seguridad.ts`: actualización privada de contraseña.
- `src/w2/styles/w2-account.css`: geometría y presentación compartida del centro.

Los paneles permanecen en el DOM y solo uno está visible. Los contratos E2E deben limitar selectores al panel activo cuando exista el mismo control en más de una sección.


> Especificación funcional y técnica vigente de Plataforma Deportiva W2. Actualizada el 21 de octubre de 2026.


<!-- W2-COMUNIDAD-ESTADO-INICIO -->
## Arquitectura actual de Comunidad y notificaciones

### Fuentes de verdad separadas

- `comunidad_eventos` conserva auditoría inmutable y referencias a convocatoria, solicitud o invitación.
- `comunidad_notificaciones` materializa una bandeja privada por `cliente_destinatario_id`.
- La unicidad por evento y destinatario permite múltiples destinatarios sin duplicar proyecciones.
- La API autenticada lista, lee una y lee todas sin aceptar identificadores de cliente enviados por el navegador.
- Las URLs contextuales se restringen a rutas internas W2 y el frontend vuelve a validarlas.

### Contratos implementados

```text
GET  /api/publica/cliente/comunidad/notificaciones
POST /api/publica/cliente/comunidad/notificaciones/{id}/leer
POST /api/publica/cliente/comunidad/notificaciones/leer-todas
```

La lectura es idempotente. Una notificación ajena o inexistente se presenta como recurso no encontrado para impedir enumeración e IDOR. La proyección cubre solicitudes, resoluciones, convocatoria completa, cierre externo, cancelación, vencimiento e invitaciones enviadas, aceptadas, rechazadas, canceladas y vencidas.

### Continuidad arquitectónica

R3.3I debe revisar transversalmente privacidad, accesibilidad, rendimiento, reconstrucción de proyecciones y contratos E2E. PWA, Push y Capacitor se incorporan después sobre esta base estable.
<!-- W2-COMUNIDAD-ESTADO-FIN -->

## 1. Objetivo

Construir una plataforma deportiva global sobre clientes, cuentas, perfiles, torneos, inscripciones, partidos, reservas, Comunidad y pagos, conservando historia, privacidad y trazabilidad sin duplicar personas.

## 2. Fronteras del sistema

- Administración JavaFX: aplicación independiente.
- API Java: contratos, autorización y reglas de negocio.
- MySQL: fuente persistente.
- `web-publica-v2`: única web pública activa.
- `api.ts` y sesión global: infraestructura compartida.
- Rutas heredadas: puentes controlados después de lograr paridad.
- DTO públicos: distintos de entidades, registros comerciales y modelos administrativos.

## 3. Identidad


<!-- W2-ARQUITECTURA-VISUAL-INICIO -->
## Arquitectura de presentación compartida

La unificación visual se organiza en tres niveles:

1. **Shell global**: `W2Layout.astro`, `W2Header.astro`, tema, sesión, notificaciones, navegación y `w2.css`.
2. **Gramática compartida**: `w2-ui-unification.css` contiene tokens neutrales de escala, controles, radios, foco y superficies. No debe absorber composiciones específicas de una ruta.
3. **Presentación por ruta**: Inicio, Reservar, Torneos, Rankings, Comunidad, Cuenta y perfiles mantienen CSS y scripts propios. Las capas de convergencia por ruta se cargan después de la hoja principal y deben ser pequeñas, explícitas y finalmente consolidadas.

### Reglas estructurales

- Inicio define la referencia visual, no una biblioteca de clases para copiar.
- Los identificadores consumidos por scripts y E2E se preservan.
- Los filtros compartidos unifican lenguaje y comportamiento, pero cada ruta conserva la propiedad de su estado y URL.
- La navegación segmentada es patrón global; los datos y acciones siguen perteneciendo al módulo.
- La presentación dinámica debe escapar contenido, no producir HTML desde datos sin sanitización y nunca simular información productiva.
- La limpieza de cascadas históricas se realiza con pruebas después de fijar el resultado final.
<!-- W2-ARQUITECTURA-VISUAL-FIN -->
```text
clientes 1 ----- 0..1 usuarios
clientes 1 ----- 0..1 perfiles_jugador
perfiles_jugador 1 ----- N jugador_categorias
perfiles_jugador 1 ----- N ranking_movimientos
torneo_inscripcion_jugadores N ----- 0..1 perfiles_jugador
```

El teléfono normalizado sirve como señal de vinculación, nunca como autorización irreversible.

## 4. Dominio deportivo

### Categorías

Catálogo 1.ª a 8.ª con orden, vigencia, estado e historial.

### Elegibilidad

- CATEGORIA_UNICA.
- CATEGORIAS_COMBINADAS.
- SUMA_CATEGORIAS.
- REQUIERE_REVISION o excepción administrativa.

Resultados:

- COMPATIBLE.
- REQUIERE_REVISION.
- AUTORIZADA_EXCEPCION.
- RECHAZADA_CATEGORIA.

### Competencia

```text
Torneo
├── tipo: estándar interno o AMERICANO
├── dinámica: MISMO_DIA o VARIOS_DIAS
└── categorías
    ├── rama: LIBRE, FEMENINO, MIXTO
    ├── condición: única, combinada o SUMA_CATEGORIAS
    ├── formato: ELIMINACION_DIRECTA o GRUPOS_ELIMINACION
    └── modalidad: UN_SET, DOS_SETS_SUPER_TIE_BREAK o MEJOR_DE_TRES
```

Todas las inscripciones son por pareja.

## 5. Torneos web

Ruta canónica: `/w2/torneos`.

Niveles:

1. catálogo;
2. detalle;
3. centro competitivo por categoría;
4. Resumen, Posiciones, Partidos o Cuadro;
5. ronda cuando corresponde.

`src/scripts/torneos.ts` actúa como composition root. `src/scripts/torneos/` separa dominio, estado, API, caché, presentación, interacción, inscripción, navegación y arranque. `w2-tournaments.css` conserva la base histórica; `w2-tournaments-unification.css` aplica la convergencia transversal actual y debe consolidarse gradualmente cuando el resultado quede cerrado.

Reglas asíncronas:

- cancelar solicitudes anteriores del mismo alcance;
- descartar respuestas obsoletas;
- caché temporal por recurso;
- recarga forzada sin caché;
- invalidación tras mutaciones;
- skeleton y composición final atómicos.

## 6. Reserva W2

- Ruta canónica `/w2/reservar`.
- Ruta histórica `/reservar` redirige en cliente y conserva `search` y `hash` porque Astro usa salida estática.
- Cuatro jugadores es un contrato fijo y se envía como `cantidadJugadores: 4`.
- Validación progresiva al salir del campo y validación completa al enviar.
- El foco programático conserva el error para lectores de pantalla; la modificación del valor elimina el error anterior.
- Revalidación de disponibilidad antes de crear la solicitud.
- Doble envío bloqueado mediante estado y `aria-busy`.
- Pago y reserva se consultan desde backend; la URL de retorno no acredita.

## 7. Ranking e historial

Ranking individual, por categoría, auditable y versionado. El historial se deriva desde partido, inscripción, integrantes y perfil. Las proyecciones deben ser reconstruibles.

## 8. Comunidad R3.3

### 8.1 Fundación persistente completada

R3.3A incorporó:

- `PreferenciasComunidad`;
- `ConvocatoriaComunidad`;
- `SolicitudComunidad`;
- `ParticipanteComunidad`;
- `EventoComunidad`;
- estados, tipos, orígenes y motivos de cierre;
- interfaces DAO y adaptadores MySQL;
- `MigracionComunidadR33A`;
- pruebas de dominio y contrato de migración.

### 8.2 Relaciones conceptuales

La arquitectura vigente también incorpora `comunidad_invitaciones` y `comunidad_notificaciones`, relacionadas con perfiles, clientes y `comunidad_eventos`.

```text
perfil_jugador 1 ----- 0..1 preferencias_comunidad
perfil_jugador 1 ----- N convocatorias_comunidad
reserva       1 ----- 0..N convocatorias_comunidad
convocatoria  1 ----- N solicitudes_comunidad
convocatoria  1 ----- N participantes_comunidad
convocatoria  1 ----- N eventos_comunidad
```

La migración real es la fuente de verdad de nombres físicos y restricciones. Este diagrama expresa responsabilidades funcionales.

### 8.3 Preferencias

Deben modelar, como mínimo:

- publicación opt-in;
- disponibilidad;
- aceptación de invitaciones;
- posición preferida;
- categorías compatibles;
- días y franjas;
- objetivos: torneo, organizar partido o completar reserva;
- autorización de contacto;
- pausado temporal.

### 8.4 Catálogo público R3.3B

El catálogo debe consultar datos reales y devolver un DTO seguro con lo estrictamente necesario:

- identificador público del perfil;
- nombre público e iniciales;
- categoría vigente autorizada;
- posición;
- objetivos y disponibilidad publicados;
- resumen competitivo permitido;
- indicadores de invitación o contacto contextual.

Nunca devuelve documento, correo, teléfono completo, pagos, historial comercial ni identificadores administrativos innecesarios.

La API debe ofrecer filtros, orden estable y paginación. El frontend no filtra un universo completo descargado.

### 8.5 Convocatorias vinculadas a reservas

Caso central: una reserva real necesita completar participantes.

Reglas:

- solo el titular autorizado publica o cierra;
- cupos confirmados y faltantes deben ser consistentes;
- la convocatoria no modifica fecha, cancha o pago de la reserva;
- cerrar convocatoria no cancela la reserva;
- una reserva cancelada o vencida invalida la convocatoria según política explícita;
- datos de la reserva se proyectan mediante DTO seguro.

### 8.6 Solicitudes y participantes

- una persona no solicita dos veces una incorporación activa equivalente;
- aceptar debe ser transaccional;
- no se aceptan más participantes que cupos;
- solicitudes incompatibles o vencidas no se aceptan;
- el origen distingue Comunidad, carga manual u otros flujos;
- cada transición relevante genera evento auditable;
- operaciones repetibles utilizan idempotencia cuando corresponda.

### 8.7 Cierre manual

El titular puede cerrar la convocatoria cuando completa el turno fuera de la web. La operación:

- impide nuevas solicitudes;
- conserva historial;
- no cancela ni modifica la reserva;
- registra motivo, actor y tiempo;
- resuelve solicitudes pendientes según política explícita.

### 8.8 Contacto y privacidad

- teléfono oculto en catálogo;
- WhatsApp solo mediante acción autorizada y contextual;
- autorización comprobada en backend;
- rate limiting y mitigación de abuso previstos;
- bloqueo y moderación como evolución obligatoria;
- observabilidad sin datos personales.

## 9. API pública

Contratos activos conocidos:

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

Contratos públicos y privados implementados para catálogo, preferencias, solicitudes, invitaciones y notificaciones:

```text
GET /api/publica/comunidad/jugadores
GET /api/publica/cliente/comunidad/preferencias
PUT /api/publica/cliente/comunidad/preferencias
```

Las rutas privadas usan cookie de sesión y protección contra acciones no autorizadas. Los nombres definitivos se fijan con pruebas de contrato antes de publicar el bloque.

## 10. Pagos

Separación obligatoria entre orden externa, evento del proveedor, pago interno, caja y reembolso.

Flujo sandbox:

1. reserva temporal;
2. elección de pago;
3. orden idempotente;
4. preferencia;
5. webhook auditable;
6. validación del proveedor;
7. acreditación transaccional;
8. conciliación defensiva.

## 11. Migraciones

- Toda migración debe existir también en `database/docker-init` cuando corresponda al esquema de instalación.
- Instalación limpia y actualización llegan al mismo esquema.
- No borrar historia.
- No modificar la base principal durante pruebas.
- Constraints, índices y claves únicas deben expresar invariantes críticas.
- Las pruebas de migración verifican contratos, no solo presencia de archivos.

## 12. Modernización técnica

Adoptar herramientas modernas con beneficio explícito:

- componentes reutilizables;
- contratos tipados;
- carga progresiva;
- formatos de imagen modernos;
- View Transitions con degradación segura;
- PWA y notificaciones en su fase;
- observabilidad sin PII;
- pruebas visuales y E2E críticas.

## 13. Riesgos y mitigaciones

- duplicación de jugadores: revisión humana y vínculos explícitos;
- exposición privada: DTO públicos específicos y autorización backend;
- sobrecupo: transacción y bloqueo al aceptar;
- acciones duplicadas: idempotencia;
- convocatorias huérfanas: reconciliación con estado de reserva;
- abuso de contacto: opt-in, límites y moderación;
- respuestas obsoletas: abortos y versiones;
- ranking doble: claves únicas por origen y regla;
- pagos duplicados: idempotencia y conciliación;
- documentación obsoleta: actualización al cerrar bloques.

## Estado autoritativo posterior al prototipo móvil

> Nota vigente desde el 25 de octubre de 2026: las secciones anteriores sobre Capacitor y CAP3 se conservan únicamente como historial técnico. El proyecto Android, las dependencias Capacitor, las capas nativas y sus pruebas fueron retirados del producto web activo en F0. La dirección vigente es finalizar primero la web de escritorio y desarrollar después una aplicación móvil independiente.
