# Plan maestro de finalización de Plataforma Deportiva W2

## Actualización de fase · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

La convergencia visual por sectores de escritorio se considera completa en su alcance actual. Los últimos cierres fueron Rankings `18d838b` y Comunidad `010ca8e`.

El trabajo continúa dentro de F8 con **T1 · experiencia transversal de navegación**:

- navegación interna fluida;
- transiciones con degradación segura;
- shell, tema y sesión estables;
- restauración de scroll y foco;
- historial Atrás/Adelante;
- estados transitorios sin flashes;
- movimiento reducido;
- pruebas proporcionales.

F9 no se inicia como auditoría integral automática. Rendimiento se medirá durante T1 y solo se ampliará ante evidencia. F10 se mantiene posterior a la estabilización transversal.


Base: commit `6a0be52`
Fecha: 25 de octubre de 2026

## Objetivo final

Declarar la web W2 funcionalmente completa, conectada a datos reales, visualmente premium, accesible, segura, optimizada, operable y documentada. La futura aplicación móvil independiente comienza únicamente después de este cierre.

## Orden obligatorio

### F0. Consolidar estado y herramientas

Estado: COMPLETADO el 25 de octubre de 2026.

Objetivo: preparar una base inequívoca.

- excluir activos Android generados de auditorías y contexto Docker;
- decidir si el prototipo Android se archiva, ignora o retira;
- clasificar pruebas históricas de Capacitor;
- actualizar documentación histórica contradictoria;
- crear matriz de rutas heredadas y W2;
- fijar presupuestos de CSS, JavaScript, imágenes y solicitudes.

Cierre:

- documentación vigente única;
- repositorio limpio;
- build/check en verde;
- sin borrar todavía rutas con funcionalidad no migrada.

### F1. Inicio real y reconstrucción visual premium

Objetivo: terminar `/w2/`.

- trasladar contratos reutilizables de `portada.ts` a una arquitectura W2;
- cargar complejo, canchas, disponibilidad y torneo real;
- personalizar por sesión y reservas;
- incorporar ranking y Comunidad solo con datos reales;
- eliminar todos los datos demostrativos;
- crear estados carga/vacío/error;
- reconstruir el escritorio en oscuro y claro;
- añadir pruebas E2E específicas del Inicio real.

Cierre:

- cero datos simulados;
- visitante y autenticado validados;
- rendimiento y capturas aprobados.

### F2. Cuenta completa: Datos y Seguridad

Objetivo: eliminar las redirecciones funcionales a `/mis-datos`.

- integrar edición de datos personales;
- integrar cambio de contraseña;
- estados de validación, guardado, éxito y error;
- sincronizar cabecera, avatar y sesión;
- separar sesión vencida de fallo parcial;
- agregar pruebas E2E y backend necesarias.

Cierre:

- Datos y Seguridad funcionan dentro de `/w2/cuenta`;
- no hay enlaces temporales de migración.

### F3. Reorganizar Cuenta y Comunidad privada

Objetivo: convertir Cuenta en centro personal definitivo.

- crear sección propia Comunidad;
- mover preferencias, invitaciones, solicitudes y convocatorias;
- conservar Perfil deportivo para categoría, ranking e historial;
- dividir `cuenta.ts` por dominios;
- extraer CSS embebido de `cuenta.astro`;
- mejorar navegación lateral y resumen por prioridad;
- evitar que APIs secundarias derriben toda la cuenta.

Cierre:

- secciones independientes y recuperables;
- diseño premium de escritorio;
- pruebas existentes preservadas y ampliadas.

### F4. Convergencia definitiva de rutas

Objetivo: establecer `/w2/*` como experiencia principal.

- inventariar `/`, `/reservar`, `/mis-reservas`, `/mis-datos`, `/torneos` y `/restablecer`;
- migrar cualquier función exclusiva;
- definir redirecciones permanentes o puentes;
- actualizar enlaces, sitemap, canonical, pruebas y documentación;
- evitar dos interfaces completas mantenidas en paralelo.

Cierre:

- cada capacidad tiene una única ruta autoritativa;
- compatibilidad heredada documentada.

### F5. Reservas y pagos funcionalmente definitivos

Objetivo: cerrar el recorrido reserva → pago → cuenta.

- usar duración y moneda autoritativas;
- revisar identificadores reserva/solicitud;
- validar doble envío, timeout e idempotencia;
- sincronizar Inicio, Cuenta y disponibilidad;
- consolidar detalle e historial;
- completar Mercado Pago productivo con HTTPS, webhook, firma, retornos y reconciliación;
- documentar preflight, operación y rollback.

Cierre:

- sandbox y producción validados;
- aprobado, pendiente, rechazado y vencido probados;
- ningún estado depende de la URL de retorno.

### F6. Torneos y Rankings finales

Objetivo: certificar competencia e identidad deportiva.

- validar estados extremos de Torneos;
- validar inscripción por parejas y reglas Libre/Femenino/Mixto/Suma N;
- comprobar convergencia administrativa;
- verificar fórmula y desempates de Rankings;
- decidir alcance obligatorio de temporadas;
- mejorar catálogo, detalle, centro, tabla y auditoría;
- reutilizar navegación secundaria flotante como patrón global donde aporte valor.

Cierre:

- datos auditables y consistentes;
- rutas profundas preservadas;
- pruebas y capturas aprobadas.

### F7. Comunidad y notificaciones finales

Objetivo: cerrar recorridos transversales.

- conectar Inicio con actividad real;
- validar invitación, solicitud, aceptación, rechazo, cancelación y vencimiento;
- validar contacto protegido y revocación;
- comprobar cierre manual y completo;
- reconciliar notificaciones y contadores;
- mejorar catálogo, turnos y detalle en escritorio.

Cierre:

- matriz de notificaciones validada de punta a punta;
- privacidad y concurrencia certificadas.

### F8. Tema, PWA y experiencia transversal

Objetivo: estabilizar la plataforma completa.

- modo oscuro principal y claro completo;
- primera carga sin parpadeo;
- estrategia de actualización PWA;
- limpieza de cachés viejos;
- offline y reintento por ruta;
- autenticación/restablecimiento coherentes;
- sesión entre pestañas y vencimiento;
- accesibilidad WCAG funcional;
- validación responsive web.

Cierre:

- ambos temas y todos los estados validados;
- actualización visible e inmediata cuando corresponda.

### F9. Consolidación técnica y rendimiento

Objetivo: reducir deuda sin regresiones.

- inventario de selectores usados;
- eliminar CSS muerto por módulos, nunca de forma masiva;
- consolidar capas autoritativas e `!important`;
- separar tokens, componentes y rutas;
- retirar imágenes sin uso y originales pesados cuando existan variantes;
- lazy loading y caché medidos;
- reducir contexto Docker;
- presupuesto final por ruta;
- mantener pruebas proporcionales en cada bloque.

Cierre:

- sin duplicaciones críticas conocidas;
- métricas de rendimiento registradas;
- build reproducible.

### F10. Certificación final y documentación

Objetivo: declarar W2 terminada.

- ejecutar suite integral frontend y backend;
- validar CI, CodeQL, Secret Scanning y auditoría de dependencias;
- capturas forzadas oscuras y claras en viewports definidos;
- revisión manual de recorridos críticos;
- actualizar manuales, arquitectura, despliegue, rollback y continuidad;
- crear changelog y commit de cierre.

Hito final:

`Plataforma Deportiva W2 web finalizada`

## Estrategia de commits

- commits simples y descriptivos;
- validaciones proporcionales durante bloques;
- suite completa solo en cierres amplios y F10;
- backend y reconstrucción Docker únicamente cuando el bloque lo requiera;
- documentación actualizada al final de cada hito funcional.

## Regla para mejoras visuales

Cualquier bloque puede ampliar su alcance si una reconstrucción visual, técnica o arquitectónica mejora el resultado sin sacrificar estabilidad, privacidad, accesibilidad, seguridad, rendimiento ni mantenibilidad. El costo de implementación no es una restricción.
