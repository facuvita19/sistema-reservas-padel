# Arquitectura funcional W2

> Especificación funcional y técnica vigente de Plataforma Deportiva W2. Actualizada el 19 de octubre de 2026.


<!-- W2-COMUNIDAD-ESTADO-INICIO -->
## Arquitectura actual de Comunidad y próximo contrato

### Modelo y fuente de verdad

La relación conceptual vigente es:

```text
Reserva
  └─ Convocatoria
       ├─ Solicitudes
       ├─ Participantes
       └─ Eventos de auditoría
```

El backend es la fuente de verdad para estado, cupos, autorización y transiciones. La interfaz nunca debe decidir por sí sola si un lugar sigue disponible.

### Contratos ya estabilizados

- consulta pública segura de perfiles;
- lectura y actualización versionada de preferencias;
- consulta pública de convocatorias abiertas;
- publicación vinculada a una reserva real;
- cierre manual con motivo explícito;
- preservación de la reserva al cerrar la búsqueda;
- aislamiento entre fallas de Turnos abiertos y Buscar pareja.

### Invariantes para solicitudes

- el titular no puede solicitar unirse a su propia convocatoria;
- un participante confirmado no puede generar otra solicitud activa;
- un perfil no puede ocupar dos lugares;
- no se aceptan más participantes que cupos disponibles;
- aceptar debe recalcular cupos dentro de la transacción;
- las operaciones repetidas deben ser idempotentes;
- una convocatoria cerrada, completa, vencida o inválida no acepta solicitudes;
- una reserva cancelada, vencida o iniciada invalida el flujo;
- los conflictos por el último cupo deben devolver un resultado controlado;
- cada transición relevante debe producir auditoría y, cuando corresponda, un evento de dominio.

### Próxima inspección técnica

R3.4B3A / R3.3H debe localizar en el código real:

- `SolicitudComunidad` y `ParticipanteComunidad`;
- estados, motivos y restricciones de unicidad;
- repositorios y bloqueos;
- servicios transaccionales;
- DTO públicos y privados;
- controladores existentes;
- autorización e IDOR;
- eventos de dominio y pruebas;
- comportamiento ante aceptación simultánea del último cupo.

Los endpoints definitivos se decidirán después de esta inspección. No se deben fijar rutas nuevas por documentación si ya existe un contrato equivalente en backend.

### Evolución desacoplada

WhatsApp será un canal autorizado y auditado, no una dependencia del dominio. Las notificaciones internas, Push y PWA consumirán eventos estables después de consolidar solicitudes, participantes y cierres automáticos.
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

`src/scripts/torneos.ts` actúa como composition root. `src/scripts/torneos/` separa dominio, estado, API, caché, presentación, interacción, inscripción, navegación y arranque. `w2-tournaments.css` es la capa visual autoritativa.

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

Contratos públicos ya implementados para catálogo y preferencias; los contratos de solicitudes quedan sujetos a inspección del backend existente:

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
