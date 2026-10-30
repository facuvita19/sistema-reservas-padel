# Estado funcional real de Plataforma Deportiva W2

## Actualización autoritativa · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

### Corrección del estado real

La convergencia visual de escritorio ya recorrió Inicio, Reservar, Torneos, Rankings, Comunidad y Cuenta. Rankings se cerró en `18d838b` y Comunidad en `010ca8e`. Las referencias posteriores que todavía describan estas áreas como pendientes de convergencia deben interpretarse como historial, no como estado vigente.

Persisten como evoluciones de producto, no como defectos de la convergencia cerrada: motor histórico avanzado de Rankings, Mercado Pago productivo, optimización basada en mediciones, PWA transversal y aplicación móvil independiente.

### Próximo bloque

T1 debe evaluar y mejorar navegación interna, transiciones, restauración de scroll, foco, historial, shell global y estados transitorios. La auditoría general de rendimiento solo se abrirá si las mediciones del bloque muestran una necesidad concreta.


Fecha de corte: 25 de octubre de 2026
Commit base auditado: `6a0be52`

## 1. Resumen ejecutivo

Plataforma Deportiva W2 no parte de cero. La web cuenta con una base funcional extensa, 17 rutas públicas detectadas y 152 pruebas frontend/backend. Los módulos más avanzados son Reservas, Torneos, Rankings, Comunidad, perfil deportivo, notificaciones, PWA y pagos en sandbox.

La web todavía no puede declararse finalizada por cuatro motivos principales:

1. `/w2/` continúa siendo un prototipo visual con datos demostrativos.
2. Cuenta no ha integrado Datos personales ni Seguridad y todavía redirige a `/mis-datos`.
3. Conviven rutas heredadas y rutas W2.
4. Mercado Pago está validado en sandbox, pero no está cerrado para producción.

Además queda una deuda visual y técnica considerable: CSS acumulado, estilos autoritativos superpuestos, código generado de Android dentro del árbol inspeccionado y documentación histórica contradictoria.

## 2. Estado por área

### Inicio `/w2/`

Estado: prototipo visual avanzado, funcionalmente incompleto.

Pendientes críticos:

- eliminar ranking, nombres, fechas, precios, señas, partidos, perfil y convocatoria demostrativos;
- integrar complejo, canchas, disponibilidad, torneo destacado y reservas reales;
- incorporar actividad deportiva real solo cuando exista contrato autoritativo;
- diferenciar visitante, sesión autenticada, reserva pendiente, reserva próxima, estado vacío y error;
- conectar Ranking y Comunidad reales;
- reconstruir visualmente el escritorio con una jerarquía menos saturada.

Base reutilizable: `src/scripts/portada.ts` ya resuelve complejo, canchas, disponibilidad, próximos horarios, torneo destacado, sesión, reservas personales, vencimientos, métricas y estados vacíos/error.

### Reservar `/w2/reservar`

Estado: avanzado y conectado a datos reales.

Ya implementado:

- selección de cancha y horario;
- disponibilidad real y panorama;
- revalidación antes de crear;
- bloqueo de doble envío;
- precarga del cliente autenticado;
- creación de solicitud;
- seguimiento y recuperación;
- transferencia y Mercado Pago sandbox;
- manejo de concurrencia y errores.

Pendientes:

- eliminar duraciones o importes endurecidos fuera del contrato;
- validar idempotencia y respuestas tardías;
- converger el estado con Inicio, Cuenta, notificaciones y administración;
- mejorar experiencia de escritorio sin perder contratos ni pruebas;
- cerrar pagos productivos.

### Torneos `/w2/torneos`

Estado: funcionalmente avanzado.

Ya implementado:

- catálogo;
- próximos, en curso y finalizados;
- detalle con información, categorías y reglamento;
- inscripción exclusivamente por parejas;
- centro competitivo con Resumen, Posiciones, Partidos y Cuadro;
- grupos, clasificación, filtros, rondas y navegación profunda;
- estados históricos y pruebas amplias.

Pendientes:

- validar todos los estados extremos y vacíos;
- comprobar convergencia exacta con administración;
- cerrar inscripción duplicada, cupo agotado durante el flujo y reglas de elegibilidad;
- preservar nomenclatura Libre, Femenino, Mixto y Suma N;
- pulir catálogo, detalle e inscripción en escritorio.

### Rankings `/w2/rankings`

Estado: avanzado y auditable.

Ya implementado:

- categorías;
- podio y clasificación completa;
- búsqueda;
- ranking largo, corto y vacío;
- perfil de jugador;
- auditoría de aportes;
- puntos, partidos, victorias, efectividad, sets y games;
- estados sin resultados;
- integración con Cuenta.

Pendientes:

- verificar que fórmula y desempates visibles coincidan exactamente con backend;
- decidir el alcance obligatorio de temporadas e historial;
- distinguir primera versión terminada de evoluciones futuras del motor;
- mejorar tabla, categorías y auditoría en escritorio;
- retirar recursos del hero ya no utilizados.

### Comunidad `/w2/comunidad`

Estado: funcionalmente avanzado y convergido.

Ya implementado:

- catálogo público seguro de jugadores;
- turnos abiertos;
- filtros y URL;
- categorías globales;
- perfiles;
- convocatorias desde reservas confirmadas;
- solicitudes;
- invitaciones recibidas y enviadas;
- contacto protegido;
- consentimiento de WhatsApp;
- notificaciones;
- cierre manual de convocatoria;
- privacidad y concurrencia.

Pendientes:

- conectar actividad real con Inicio;
- separar Comunidad privada de Perfil deportivo dentro de Cuenta;
- validar el recorrido notificación → entidad → resolución → reconciliación;
- validar reglas de parejas para torneo según Libre, Femenino, Mixto y Suma N;
- mejorar catálogo, turnos y detalle en escritorio.

### Cuenta `/w2/cuenta`

Estado: avanzada, pero parcialmente convergida.

Implementado:

- Resumen;
- Reservas e historial;
- filtros, búsqueda y detalle;
- seguimiento por código;
- pagos sandbox;
- perfil deportivo;
- categoría, ranking y auditoría;
- historial de partidos y categorías;
- preferencias de Comunidad;
- invitaciones;
- convocatorias desde reservas.

No implementado dentro de W2:

- Datos personales;
- Seguridad y cambio de contraseña.

Problemas de arquitectura:

- `cuenta.ts` concentra demasiadas responsabilidades;
- `cuenta.astro` contiene CSS extenso embebido;
- Comunidad privada está mezclada con Perfil deportivo;
- un error de una API secundaria puede degradar toda Cuenta a estado de invitado;
- pagos y detalle acumulan capas visuales superpuestas.

### Autenticación y sesión

Estado: avanzado.

Pendientes:

- converger recuperación/restablecimiento con identidad W2;
- diferenciar sesión vencida de errores parciales;
- validar sincronización entre pestañas y actualización de perfil;
- cerrar el tema claro/oscuro en todos los estados;
- mantener carga diferida sin parpadeo inicial.

### Notificaciones

Estado: funcionalmente cerrado en bloques anteriores.

Pendiente: validación transversal final de contador, enlaces, lectura, resolución y reconciliación con Cuenta y Comunidad.

### PWA

Estado: implementada.

Pendientes:

- estrategia autoritativa de actualización;
- invalidación de cachés viejos;
- visibilidad de nuevas versiones;
- comportamiento offline por ruta;
- separar claramente PWA web del prototipo Android cerrado.

### Pagos

Estado: sandbox avanzado; producción pendiente.

Pendientes productivos:

- credenciales y URLs públicas HTTPS fuera del repositorio;
- webhook productivo;
- firma;
- idempotencia;
- retornos aprobado, pendiente y rechazado;
- reconciliación;
- preflight, despliegue y rollback;
- validación operativa con Mercado Pago.

## 3. Deuda transversal

- rutas heredadas: `/`, `/reservar`, `/mis-reservas`, `/mis-datos`, `/torneos`;
- 152 pruebas existentes, incluidas pruebas ya históricas del prototipo móvil;
- aproximadamente 822 KB de CSS fuente W2 principal antes de minificación;
- múltiples capas `authoritative`, `final`, `definitive`, `override` e `!important`;
- recursos sin uso probable, como `w2-ranking-hero.png` después del hero editorial;
- PNG originales de más de 2,6 MB conservados junto a variantes WebP/AVIF;
- activos generados de Capacitor dentro de `android/app/src/main/assets/public`;
- documentación con próximos bloques ya superados.

## 4. Conclusión

La web está avanzada, pero no terminada. El camino correcto no es otra auditoría genérica, sino una secuencia de convergencia funcional, reconstrucción visual de escritorio, producción de pagos, consolidación técnica y certificación final.
