# Plataforma Deportiva W2

> Evolución integral de `web-publica-v2` hacia una plataforma deportiva, competitiva y comercial premium.

## 1. Visión

Plataforma Deportiva W2 transforma la web pública en el centro digital del complejo. La experiencia debe unir reservas, identidad deportiva, categorías, rankings, historial competitivo, comunidad, torneos y pagos reales sin duplicar personas ni fragmentar la información.

Objetivos:

- aumentar reservas y recurrencia;
- reducir tareas administrativas manuales;
- crear identidad y trayectoria deportiva;
- transparentar categorías y rankings;
- fortalecer la comunidad del complejo;
- reemplazar comprobantes manuales por pagos conciliados;
- elevar la percepción visual a un nivel premium, moderno y profesional.

## 2. Principios obligatorios

- W2 reemplazará integralmente la experiencia anterior, no convivirá indefinidamente como sección paralela.
- Ninguna capacidad vigente se elimina hasta alcanzar paridad funcional y una experiencia superior verificada.
- La excelencia visual y de producto prevalece sobre el costo de implementación: se rehace lo necesario cuando mejora el resultado.
- Cada detalle debe reforzar una presencia premium, profesional, moderna e imponente.
- La innovación tecnológica se evalúa de forma continua y se adopta cuando mejora experiencia, seguridad, rendimiento o mantenibilidad.

- Una persona no debe duplicarse entre cuenta, cliente y jugador.
- La cuenta autentica, el cliente representa la relación comercial y el jugador representa la identidad deportiva.
- Las categorías tienen vigencia e historial; no son un texto fijo en el jugador.
- El ranking es reproducible, versionado y auditable.
- Todo punto debe vincularse con su origen deportivo o ajuste administrativo.
- El navegador nunca confirma por sí solo un pago.
- Ninguna credencial de pago vive en Astro ni en JavaScript público.
- Webhooks, conciliación e idempotencia son obligatorios.
- La privacidad de jugadores y datos de contacto se controla explícitamente.
- El diseño debe conservar accesibilidad, responsive y rendimiento.
- Cada fase debe incluir pruebas Java y Playwright según corresponda.

## 3. Modelo conceptual

### 3.1 Identidad

- Cuenta: autenticación, sesión y seguridad.
- Cliente: reservas, pagos, historial comercial y contacto privado.
- Jugador: identidad deportiva, nombre público, avatar, preferencias y estadísticas.
- Vinculación: una cuenta puede vincularse con un cliente y un jugador sin duplicar datos.

### 3.2 Categorías deportivas

Cada asignación tendrá:

- jugador;
- categoría;
- fecha desde y hasta;
- estado;
- origen administrativo, provisional, importado o por resultados;
- puntuación inicial;
- observaciones auditables.

La categoría actual se obtiene desde la asignación vigente. Los cambios conservan historial.

### 3.3 Competencia

Entidades previstas:

- temporada;
- ranking por categoría;
- movimiento de ranking;
- regla y versión de cálculo;
- historial de torneo;
- historial de partido;
- pareja o equipo competitivo;
- logro deportivo;
- publicación y corrección auditada.

### 3.4 Comunidad

Entidades previstas:

- convocatoria deportiva;
- tipo: buscar pareja, completar partido, buscar partido o disponibilidad;
- categoría y nivel;
- fecha y horario;
- cupos;
- solicitudes;
- aceptación o rechazo;
- cierre y vencimiento;
- asociación opcional con una reserva.

Los datos privados de contacto nunca se publican sin consentimiento.

### 3.5 Pagos externos

Entidades previstas:

- orden de pago interna;
- proveedor;
- identificadores externos;
- clave idempotente;
- importe esperado e informado;
- moneda;
- estado interno y externo;
- eventos y webhooks;
- conciliación;
- reembolso;
- contracargo;
- vinculación con reserva, inscripción, pago y caja.

## 4. Reglas deportivas confirmadas

### 4.1 Catálogo inicial de categorías

El catálogo deportivo global debe incluir todas las categorías desde **1.ª hasta 8.ª**.

Cada categoría tendrá un orden competitivo explícito:

- 1.ª es la categoría de mayor nivel;
- 8.ª es la categoría de iniciación o menor nivel dentro del catálogo;
- el orden no se inferirá desde el texto del nombre;
- el catálogo podrá ampliarse o desactivarse sin borrar historia.

La rama, modalidad o segmentación adicional no se fija todavía como una división obligatoria. El modelo debe permitir incorporarla después sin duplicar el catálogo principal.

### 4.2 Asignación, ascensos y descensos

- La categoría vigente de un jugador es asignada por el complejo según criterio del organizador.
- Los ascensos y descensos no son automáticos.
- El organizador realiza y audita cada cambio desde la aplicación administrativa.
- Cada cambio conserva categoría anterior, categoría nueva, fecha, motivo, observaciones y usuario responsable.
- La plataforma debe permitir publicar ascensos y descensos en un anuncio general para que los jugadores puedan informarse.
- Una publicación puede agrupar múltiples cambios y mostrar fecha de vigencia.
- El ranking puede aportar información al organizador, pero no cambia categorías por sí mismo.

### 4.3 Elegibilidad e inscripción excepcional

La categoría propia del jugador funciona como referencia y regla predeterminada, no como única condición posible de participación.

- Una inscripción en categoría diferente requiere autorización del organizador.
- Jugar en una categoría inferior no debe aceptarse de forma predeterminada.
- El organizador puede aprobar excepcionalmente una categoría inferior y debe registrar el motivo.
- Jugar en otra categoría, superior o combinada, queda sujeto a las reglas del torneo y aprobación administrativa.
- La aceptación administrativa de la inscripción constituye la decisión final de elegibilidad.
- Las excepciones deben quedar auditadas y no modifican automáticamente la categoría personal del jugador.

Estados de elegibilidad sugeridos:

- COMPATIBLE;
- REQUIERE_REVISION;
- AUTORIZADA_EXCEPCION;
- RECHAZADA_CATEGORIA.

### 4.4 Formatos de categoría de torneo

Una categoría de torneo podrá usar alguno de estos criterios:

#### Categoría única

Ejemplo: 6.ª. Los integrantes pertenecen a la categoría objetivo o tienen autorización excepcional.

#### Categorías combinadas

Ejemplo: **4.ª + 5.ª**. El organizador define qué combinaciones de categorías son válidas para los integrantes de una pareja.

#### Regla de suma

Ejemplo: **Suma 12**. La suma numérica de las categorías de ambos integrantes debe cumplir el valor configurado. Una pareja de dos jugadores de 6.ª cumple Suma 12.

La regla debe ser configurable por torneo y no codificarse exclusivamente para el número 12. Debe admitir:

- valor objetivo;
- suma exacta o rango permitido;
- categorías mínimas y máximas opcionales;
- autorización administrativa excepcional;
- explicación visible para el jugador antes de inscribirse.

#### Americano

El formato Americano será una modalidad configurable y diferenciada de grupos más eliminación. El organizador podrá definir:

- composición y rotación de participantes o parejas;
- cantidad de rondas;
- duración o criterio de cierre;
- un solo set;
- dos sets con definición mediante super tie-break;
- puntuación y criterio de clasificación;
- clasificación individual o por pareja;
- reglas de desempate.

No se debe suponer una única variante de Americano. La configuración del torneo será la fuente de verdad.

### 4.5 Configuración de partidos

Las reglas de partido no deben quedar fijas globalmente. Cada torneo o categoría podrá definir:

- cantidad de sets;
- formato del set;
- uso de tie-break;
- uso de super tie-break decisivo;
- juegos necesarios para ganar;
- diferencia mínima;
- reglas ante retiro, ausencia o walkover.

Esto permitirá mantener los formatos actuales y agregar Americanos u otras modalidades sin alterar resultados históricos.

## 5. Ranking: decisiones confirmadas

### 5.1 Ventana y alcance

- El ranking utiliza una ventana móvil de los últimos **12 meses**.
- Los puntos se asignan principalmente según la fase alcanzada.
- Los integrantes de una pareja reciben los mismos puntos por el resultado compartido.
- Los walkovers otorgan los puntos que correspondan según la regla publicada del torneo.
- Una corrección de resultado debe recalcular los movimientos afectados y actualizar el ranking de forma auditada.
- Todos los torneos ordinarios pueden entregar puntos según la tabla vigente.

### 5.2 Masters de fin de año

Existirán Masters por categoría al final del año:

- reúnen a los jugadores mejor posicionados de la categoría;
- la clasificación se basa en el ranking definido por el complejo;
- tendrán configuración propia de cupos, fechas, formato y puntos;
- el organizador confirma la nómina final;
- un Master no debe confundirse con el cierre técnico de la ventana móvil de 12 meses.

La cantidad de clasificados y la tabla de puntos del Master se definirán como configuración, no como constantes de código.

### 5.3 Participantes sin cuenta

Los jugadores sin cuenta también pueden integrar ranking e historial mediante una identidad deportiva pendiente de vinculación.

La plataforma debe:

- crear o reutilizar una identidad provisional sin exigir cuenta web;
- usar nombre, apellido y teléfono normalizado como señales de vinculación;
- evitar asociaciones automáticas ambiguas;
- permitir revisión administrativa ante coincidencias parciales;
- vincular posteriormente la identidad con un cliente o cuenta;
- conservar todos los puntos, torneos y partidos previos después de la vinculación;
- impedir que una vinculación duplique puntos o fusione personas incorrectas.

La coincidencia por datos personales genera una propuesta, no una unión irreversible. La confirmación corresponde al organizador o a un flujo seguro de verificación del titular.

### 5.4 Recalculo y auditoría

Cuando cambia un resultado:

1. se conserva la corrección del partido;
2. se identifican los movimientos de ranking dependientes;
3. se recalculan en una operación transaccional;
4. se registran valores anteriores y nuevos;
5. se actualiza la publicación vigente o se genera una revisión nueva;
6. se evita acreditar dos veces el mismo torneo o fase.

Las reglas de puntuación pendientes de definición son:

- tabla exacta por fase;
- tratamiento detallado de walkovers, retiros y descalificaciones;
- puntos especiales para Masters;
- ponderación eventual según importancia del torneo;
- límites o mínimos de participación.

## 6. Privacidad: decisiones confirmadas

- El área personal del jugador será privada y requerirá sesión.
- El historial deportivo no necesita una opción general para ocultarse dentro del área privada.
- Cuando exista una vista pública de ranking o competencia, se mostrará el nombre completo.
- El avatar queda pendiente de definición.
- Las estadísticas que podrán ser públicas se decidirán después de revisar cada métrica.
- No se establece una variante funcional especial para menores en esta fase.
- Teléfono, correo, documento, historial comercial y datos de pago nunca se exponen en perfiles ni rankings públicos.

La visibilidad debe definirse por campo y contexto, no mediante un único interruptor ambiguo de “perfil público”.

## 7. Rankings por categoría

El ranking debe permitir:

- temporadas y períodos;
- posición actual y variación;
- puntos actuales;
- historial de movimientos;
- torneos y partidos considerados;
- mejores resultados;
- reglas configurables;
- cálculo preliminar;
- revisión administrativa;
- publicación;
- corrección auditada;
- eventual vencimiento o período móvil de puntos.

Reglas posibles, a definir con el negocio:

- participación;
- victoria por partido;
- fase alcanzada;
- campeonato o final;
- importancia del torneo;
- rival o categoría;
- límite de mejores resultados;
- vigencia de seis o doce meses.

La fórmula debe versionarse para que los rankings históricos sigan siendo explicables.

## 8. Historial deportivo

Cada jugador podrá consultar, según privacidad:

- categoría actual e historial;
- ranking actual y mejor posición;
- torneos disputados;
- partidos, rivales y marcadores;
- victorias, derrotas y efectividad;
- títulos, finales y fases alcanzadas;
- parejas frecuentes;
- evolución de puntos;
- próximos encuentros;
- recorrido por grupos y cuadros.

Cada torneo debe mostrar categoría, pareja, resultado final, partidos, puntos obtenidos y recorrido completo.

## 9. Reserva inteligente

La nueva experiencia incluirá progresivamente:

- matriz de horarios y canchas;
- filtros por fecha, franja, duración y cancha;
- próximos turnos libres;
- alternativas cuando un turno deje de estar disponible;
- resumen de reserva premium;
- temporizador visible para completar seña;
- repetición de reserva;
- cancha y horario favoritos;
- lista de espera;
- aviso cuando se libere un turno;
- invitación de jugadores;
- vínculo con convocatorias comunitarias.

## 10. Comunidad deportiva

Funciones previstas:

- buscar pareja;
- completar un partido;
- buscar rivales;
- publicar disponibilidad;
- solicitar unirse;
- aceptar o rechazar;
- cerrar automáticamente cupos;
- vencimiento automático;
- moderación, bloqueo y reporte;
- notificaciones internas, correo o WhatsApp con consentimiento.

## 11. Evolución de torneos

- favoritos;
- cuenta regresiva;
- calendario personal;
- recordatorios;
- perfiles de jugadores y parejas;
- ranking previo;
- historial de enfrentamientos;
- forma reciente;
- puntos asignados por el torneo;
- seguimiento de grupos y cuadros;
- resultados actualizados;
- historial de campeones;
- contenido compartible;
- notificación de cambios de horario.

Al finalizar un torneo, el cálculo de ranking debe generar una liquidación preliminar, revisión administrativa y publicación explícita.

## 12. Mercado Pago y pagos reales

### 9.1 Primera integración

La primera alternativa prevista es Checkout Pro mediante backend. El cliente será redirigido al entorno seguro del proveedor y regresará a la web, pero el retorno del navegador no confirmará el pago.

### 9.2 Flujo obligatorio

1. El cliente selecciona un turno.
2. El servidor crea una reserva temporal.
3. El servidor calcula el importe de la seña.
4. El backend crea la orden externa con idempotencia.
5. El cliente paga en el proveedor.
6. El proveedor notifica mediante webhook HTTPS.
7. El backend consulta y valida el estado real.
8. Se verifican identidad de orden, moneda e importe.
9. Pago, reserva y caja se actualizan transaccionalmente una sola vez.
10. El cliente recibe confirmación.

### 9.3 Estados internos previstos

- CREADO;
- PENDIENTE;
- EN_PROCESO;
- APROBADO;
- RECHAZADO;
- CANCELADO;
- VENCIDO;
- REEMBOLSADO;
- CONTRACARGO;
- ERROR.

### 9.4 Seguridad

- Access Token exclusivamente en backend.
- Credenciales de prueba y producción separadas.
- Importe calculado por servidor.
- Webhook público mediante HTTPS.
- Eventos repetidos no duplican acreditaciones.
- Logs redactados.
- Conciliación periódica.
- Reembolsos y contracargos auditados.
- Ninguna confirmación basada solo en query strings de retorno.

### 9.5 Dependencias externas

El desarrollo puede comenzar con credenciales de prueba. La validación completa de webhooks y producción requiere URL pública HTTPS, credenciales productivas y una transacción real de bajo monto con posterior reembolso controlado.

## 13. Dirección visual premium

### 10.1 Lenguaje visual

- base grafito, negro suave y verdes luminosos;
- tipografía editorial con jerarquía fuerte;
- fotografía deportiva de calidad;
- gradientes ambientales y profundidad;
- glassmorphism moderado, no decorativo;
- tarjetas con función y no repetición indiscriminada;
- composiciones asimétricas controladas;
- iconografía SVG coherente;
- microinteracciones útiles;
- skeletons y estados vacíos premium;
- accesibilidad y contraste como requisitos.

### 10.2 Imágenes

Tipos previstos:

- fotografías reales del complejo cuando estén disponibles;
- imágenes deportivas generadas específicamente para secciones conceptuales;
- imágenes de canchas, torneo, competencia y comunidad;
- avatares o fotografías opcionales de jugadores;
- identidad visual específica por torneo;
- fondos abstractos sin texto para héroes y transiciones.

Cada imagen debe tener finalidad comercial, informativa o identitaria. Se evitarán bancos genéricos incoherentes y recursos meramente decorativos que reduzcan rendimiento.

### 10.3 Estructuras por sección

- Inicio: hero deportivo, disponibilidad inmediata, próximos turnos y torneos.
- Reservas: matriz de disponibilidad y checkout premium.
- Jugadores: perfil, categoría, ranking, evolución y trayectoria.
- Rankings: podio, tabla, variación, temporada y explicación de puntos.
- Historial: línea de tiempo, recorridos y resultados.
- Comunidad: convocatorias visuales, cupos y compatibilidad.
- Torneos: identidad propia, cuenta regresiva, grupos, cuadro y campeones.
- Pagos: seña, saldo, estado, devolución y comprobante interno.

## 14. Fases de implementación

### W2.0: dirección creativa y arquitectura

- auditoría visual;
- inventario de componentes e imágenes;
- mapa de páginas;
- sistema visual;
- estructura de navegación;
- prototipos conceptuales de Inicio, Reservas, Jugador y Ranking;
- modelo de datos de alto nivel.

### W2.1: identidad deportiva y categorías

- jugador;
- vínculo con cliente y cuenta;
- categorías e historial;
- privacidad;
- administración;
- migraciones, servicios, API y pruebas.

### W2.2: historial competitivo

- vínculo con inscripciones, parejas y partidos;
- torneos disputados;
- estadísticas;
- ficha deportiva;
- privacidad y pruebas.

### W2.3: motor de ranking

- temporadas;
- reglas versionadas;
- movimientos;
- cálculo preliminar;
- revisión, publicación y corrección;
- rankings por categoría;
- pruebas deportivas completas.

### W2.4: experiencia web deportiva

- perfiles;
- rankings;
- historial;
- categorías;
- buscador;
- imágenes premium;
- responsive, SEO, accesibilidad y Playwright.

### W2.5: reserva inteligente

- matriz;
- filtros;
- alternativas;
- repetición;
- favoritos;
- lista de espera;
- concurrencia y E2E.

### W2.6: comunidad

- convocatorias;
- solicitudes;
- privacidad;
- moderación;
- asociación con reservas;
- notificaciones.

### W2.7: Mercado Pago en pruebas

- persistencia;
- adaptador;
- orden;
- retorno;
- webhook;
- validación e idempotencia;
- conciliación;
- caja;
- expiración y reembolsos;
- sandbox y pruebas.

### W2.8: Mercado Pago en producción

- dominio y HTTPS;
- credenciales productivas;
- webhook público;
- pago real de bajo monto;
- reembolso controlado;
- monitoreo, documentación y rollback.

### W2.9: engagement

- favoritos;
- calendario;
- notificaciones;
- resultados en vivo;
- PWA;
- contenido comercial configurable.

## 14A. Convergencia y paridad funcional

W2 debe absorber y mejorar la web pública vigente completa: autenticación, registro, recuperación, menú de cuenta, Mis datos, seguridad, Mis reservas, seguimiento, reserva de canchas, torneos, inscripción y centro competitivo.

La convergencia se realizará mediante componentes y módulos compartidos, no mediante copias aisladas. El estado de sesión, el cliente API, las validaciones, los contratos y los flujos maduros se reutilizarán.

Una función solo se considera migrada cuando cumple:

- misma funcionalidad o superior;
- mismos datos reales y reglas de seguridad;
- estados de carga, vacío, error, éxito y sesión vencida;
- responsive específico y accesibilidad de teclado;
- pruebas Java, build y Playwright aplicables;
- mejor jerarquía visual, legibilidad y velocidad de uso;
- ausencia de duplicación innecesaria;
- redirección o compatibilidad para la ruta anterior.

### Estado real de las fases

- W2.0, W2.1 y W2.2: completados en su alcance inicial.
- W2.3: ranking inicial operativo y auditable; quedan pendientes temporadas, reglas versionadas, ventana móvil, publicaciones históricas, variaciones y Masters.
- W2.4: iniciada con rankings y perfiles públicos reales; continúa con shell autenticado, centro de cuenta, paridad y Competir.


## 15. Orden de trabajo inmediato

> Punto actual: W2.4 en curso. Rankings y perfiles públicos reales están operativos. El próximo entregable es W2.4A: shell autenticado y menú de cuenta, seguido por el centro de cuenta y la paridad funcional completa.



1. Inspección de modelos actuales de cuenta, cliente, categoría, inscripción, pareja, partido y pago.
2. Auditoría visual y estructural de `web-publica-v2`.
3. Inventario de endpoints y pruebas reutilizables.
4. Identificación de brechas de datos.
5. Diseño W2.0 y modelo W2.1.
6. Aprobación funcional de categorías y ranking.
7. Primera migración de identidad deportiva.

## 16. Criterios de aceptación W2

Cada fase debe:

- evitar duplicación de personas;
- mantener compatibilidad con datos actuales;
- preservar permisos y privacidad;
- ser migrable e instalable desde cero;
- incluir rollback de esquema si corresponde;
- incluir pruebas automatizadas;
- mantener CI verde;
- validar experiencia móvil y teclado;
- no exponer secretos;
- actualizar este documento y el plan estratégico.
