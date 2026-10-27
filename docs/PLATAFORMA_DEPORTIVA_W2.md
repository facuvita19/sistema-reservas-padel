# Plataforma Deportiva W2

> Evolución integral de `web-publica-v2` hacia una plataforma deportiva, competitiva y comercial premium. Actualizado el 21 de octubre de 2026.


<!-- W2-COMUNIDAD-ESTADO-INICIO -->
## Estado actual de Comunidad y continuidad

Comunidad cubre el recorrido deportivo principal de extremo a extremo:

- catálogo seguro y preferencias opt-in;
- navegación Toda la Comunidad / Turnos abiertos / Buscar pareja;
- convocatorias vinculadas a reservas reales, cierre manual y sincronización con el turno;
- solicitudes, participantes, cupos, concurrencia y auditoría;
- invitaciones privadas y contacto protegido por consentimiento vigente;
- centro global de notificaciones internas, privado e idempotente;
- matriz completa para aceptación, rechazo, cupo, cierre externo, cancelación, vencimiento e invitaciones.

R3.4B5A–D / R3.3H quedó cerrado en `2735dfb`. `comunidad_eventos` conserva la historia inmutable y `comunidad_notificaciones` materializa la bandeja por destinatario. PWA, Push y Capacitor consumirán estos contratos sin acoplar el dominio a canales externos.

R3.3I · Convergencia final de Comunidad quedó cerrado en `3b1688f`. Comienza la fundación PWA y después el prototipo Capacitor Android.
<!-- W2-COMUNIDAD-ESTADO-FIN -->

## 1. Visión

W2 es la reconstrucción definitiva de la web pública. Une reservas, identidad deportiva, categorías, rankings auditables, historial, perfiles, parejas, Comunidad, torneos, notificaciones y pagos reales sin duplicar personas ni fragmentar la experiencia.

## 2. Emblema permanente

Plataforma Deportiva W2 debe ser **profesional, moderna e imponente**. Puede incorporar cualquier mejora mínima, costosa o una reconstrucción completa cuando eleve la experiencia, visibilidad, código, arquitectura, estructura, diseño, accesibilidad, rendimiento, seguridad o mantenibilidad.

La versión anterior es referencia funcional y nada más. La nueva web debe superarla en todos los sentidos. El costo no es una razón suficiente para rechazar una mejora valiosa.

## 3. Principios obligatorios


<!-- W2-VISION-VISUAL-INICIO -->
## Referencia visual autoritativa: Inicio W2

La ruta `/w2/` define la gramática visual de Plataforma Deportiva W2. Las demás secciones no deben copiar literalmente el HTML ni las clases `w2x-*`; deben trasladar sus principios a la función propia de cada recorrido.

### Composición autoritativa

1. **Cabecera flotante contenida**: navegación centrada en el viewport, superficie grafito propia, sin bandas laterales y geometría estable entre rutas y estados de sesión.
2. **Hero deportivo de primer frame**: fotografía real o recurso visual contextual, oscurecimiento por capas, contenido asimétrico, propuesta principal y módulo vivo visible sin desplazar la acción esencial fuera del primer viewport.
3. **Titular editorial**: escala display, interlineado compacto, espaciado negativo controlado y combinación de texto sólido con una línea o palabra contorneada. El contorno es un recurso de énfasis, no una obligación en todos los títulos.
4. **Kicker de identidad**: mayúsculas, tracking amplio, peso alto y línea roja corta. Debe introducir contexto, no repetir el título.
5. **Acciones jerarquizadas**: CTA rojo para la acción principal; acción secundaria grafito o transparente; icono o flecha integrada en un módulo compacto; foco visible y área táctil mínima.
6. **Panel contextual vivo**: superficie grafito profunda, borde discreto, radio asimétrico, iluminación radial roja moderada y datos reales con carga, contenido, vacío y error.
7. **Franja de propuesta de valor**: tres señales compactas conectan Reservas, Competencia y Comunidad. En otras rutas puede convertirse en métricas o accesos rápidos, pero debe conservar ritmo, alineación y utilidad.
8. **Secciones editoriales numeradas**: índice corto, título fuerte, explicación limitada y módulos alineados al contenido real. La numeración ordena la historia de la página.
9. **Bento y superficies**: paneles grafito con bordes finos, sombras moderadas y radios diagonales. La grilla responde al contenido; no se fuerzan alturas que generen vacío artificial.
10. **Profundidad y color**: negro y grafito estructuran; blanco establece jerarquía; rojo identifica y acciona. Verde queda reservado para éxito, clasificación o acreditación; ámbar para advertencia o espera; azul para Mercado Pago o identidad externa equivalente.
11. **Movimiento útil**: revelado breve, hover contenido y transformaciones pequeñas. `prefers-reduced-motion` elimina animaciones sin ocultar contenido.
12. **Cierre editorial**: la página concluye conectando Jugar, Competir y Perfil antes del footer global.

### Qué se replica y qué no

Se replican escala, ritmo, jerarquía, contraste, profundidad, comportamiento, focos, estados y lenguaje de controles. No se replica automáticamente la fotografía, el bento, la agenda, la cantidad de módulos ni las clases de Inicio. Reservar sigue siendo operativo; Torneos, competitivo; Rankings, analítico; Comunidad, de descubrimiento; Mi cuenta, de gestión personal.

### Archivos de referencia

- `src/pages/w2/index.astro`: composición y orden narrativo.
- `src/w2/styles/w2-home-v2.css`: resultado visual autoritativo de Inicio.
- `src/w2/scripts/inicio-v2.ts`: datos reales, estados y comportamiento de Inicio.
- `src/w2/styles/w2-ui-unification.css`: tokens transversales nuevos.
- `src/w2/styles/w2-tournaments-unification.css`: primera aplicación conservadora en Torneos.
- `src/w2/styles/w2-booking-v2.css`: referencia para recorridos operativos complejos.

### Regla de continuidad

Antes de modificar una sección se inspeccionan su Astro, CSS, scripts y E2E. La unificación debe preservar identificadores, URL, estado, accesibilidad y reglas de negocio. Se prefieren capas compartidas pequeñas y archivos por ruta antes que copiar clases de Inicio o acumular overrides indefinidos.
<!-- W2-VISION-VISUAL-FIN -->
- W2 reemplaza integralmente la experiencia anterior.
- Ninguna función se retira antes de una alternativa igual o superior.
- Modo oscuro es la experiencia principal; modo claro es una alternativa real.
- La aplicación administrativa es independiente de la web pública.
- Cuenta, cliente y perfil deportivo son responsabilidades distintas de una misma persona.
- Ranking, categorías, Comunidad y correcciones deben ser auditables.
- Pagos se confirman en backend, nunca por la URL de retorno.
- Privacidad, accesibilidad, seguridad y mantenibilidad no se sacrifican por velocidad visual.
- Cada pantalla se cierra con calidad definitiva antes de avanzar.
- Las herramientas modernas se incorporan cuando aportan valor real y sostenible.

## 4. Identidad deportiva

- Cuenta: autenticación y seguridad.
- Cliente: relación comercial, reservas y pagos.
- Perfil deportivo: categoría, ranking, historial y preferencias deportivas.
- Participante sin cuenta: identidad provisional vinculable posteriormente con revisión segura.
- La vinculación conserva puntos, torneos y partidos y evita fusiones ambiguas.

## 5. Categorías y elegibilidad

Catálogo inicial: 1.ª a 8.ª, con orden competitivo explícito.

La categoría vigente posee historial, vigencia, origen, motivo y responsable. Ascensos y descensos son decisiones manuales y auditables.

Condiciones:

- categoría única;
- categorías combinadas;
- Suma 8, Suma 10, Suma 12, etc.;
- excepción administrativa auditable.

Nunca usar “suma máxima”.

## 6. Taxonomía de Torneos

### Rama

- Libre.
- Femenino.
- Mixto.

Libre admite parejas integradas por personas de cualquier sexo. Mixto exige una mujer y un hombre.

### Formato competitivo

- Eliminación directa.
- Grupos + eliminación.

### Condición de inscripción

- Categoría única.
- Categorías combinadas.
- Suma 8, Suma 10, Suma 12, etc.

### Modalidad

- Un set.
- Dos sets más super tie-break.
- Mejor de tres sets.

### Dinámica

- Mismo día.
- Varios días.

### Americano

Americano es un evento rápido especial, no un formato competitivo ni una condición de inscripción. Es ocasional, normalmente termina el mismo día, siempre recibe parejas y no se modela como liga semanal o mensual.

No mostrar “Convencional”.

## 7. Inscripciones

- Exclusivamente por pareja en todos los torneos.
- No hay inscripción individual, tampoco en Americanos.
- Comunidad permite encontrar pareja previamente.
- La aceptación administrativa es la decisión final de elegibilidad.
- Una excepción no cambia automáticamente la categoría del perfil.

## 8. Ranking e historial

- Ranking individual por categoría.
- Ventana móvil prevista de 12 meses.
- Puntos reproducibles por fase y regla versionada.
- Ambos integrantes reciben puntos del resultado compartido.
- Correcciones recalculan movimientos de forma auditada.
- Masters por categoría como evolución configurada.
- Historial con torneo, partido, pareja, rivales, resultado, fase y puntos.

## 9. Reserva y pagos

### Reserva W2

- Ruta canónica `/w2/reservar`.
- `/reservar` es un puente estático que preserva parámetros y fragmento.
- El formato es fijo: cuatro jugadores, dos contra dos.
- La disponibilidad se valida nuevamente antes de crear la solicitud.
- El error accesible se conserva al enfocar y se limpia cuando la persona modifica el valor.
- La reserva temporal no equivale a confirmación.

### Mercado Pago

Sandbox validado. Producción pendiente.

Obligatorios:

- Access Token solo en backend;
- orden interna e idempotencia;
- webhook HTTPS;
- validación de referencia, importe, moneda y estado;
- conciliación;
- acreditación transaccional;
- reembolsos y contracargos auditados.

## 10. Comunidad

Comunidad es infraestructura deportiva para descubrir jugadores, formar parejas y completar turnos. No es una red social genérica.

### Casos principales

1. Encontrar pareja para un torneo, contactar principalmente por WhatsApp, coordinar y luego inscribirse juntos.
2. Publicar una reserva existente que necesita participantes, por ejemplo: “3 de 4, falta 1 jugador de 5.ª”.
3. Solicitar unirse a una convocatoria compatible.
4. Aceptar, rechazar o cancelar solicitudes.
5. Cerrar manualmente una convocatoria completada fuera de la web sin cancelar ni modificar la reserva.

### Privacidad

- presencia pública solo por opt-in;
- teléfono nunca listado públicamente;
- contacto contextual y autorizado;
- DTO público separado del modelo interno;
- documento, correo, pagos e historial comercial nunca expuestos;
- auditoría de acciones sensibles;
- moderación y bloqueo previstos.

### Estado real

Comunidad completó R3.4B1–R3.4B5D / R3.3A–R3.3H en su alcance actual; queda R3.3I como convergencia final.

## 11. Torneos W2: estado real

Ruta canónica: `/w2/torneos`. No existe una sección principal llamada Competir.

Estado cerrado:

- catálogo con Próximos, En curso y Finalizados;
- búsqueda, filtros y URL persistente;
- detalle con Información, Categorías y Reglamento;
- inscripción exclusivamente por pareja;
- centro competitivo con Resumen, Posiciones, Partidos y Cuadro;
- nomenclatura Libre, Femenino y Mixto;
- oscuro, claro y responsive;
- arquitectura modular.

Torneos conserva su arquitectura funcional completa. Puede recibir evolución transversal de tipografía, superficies, controles, filtros y navegación para converger con Inicio, sin reescribir dominio ni recorridos.

## 12. Fases y continuidad

- W2.0: dirección y arquitectura, completado.
- W2.1: identidad y categorías, alcance inicial completado.
- W2.2: historial competitivo, alcance inicial completado.
- W2.3: Rankings y perfiles estabilizados en el recorrido actual; motor avanzado pendiente como evolución posterior.
- W2.4: experiencia deportiva pública en curso; Torneos cerrado.
- W2.5: reserva W2 operativa, con evoluciones inteligentes futuras.
- W2.6 / R3.3: Comunidad completó R3.3A–R3.3H; queda R3.3I como convergencia final.
- W2.7: Mercado Pago sandbox, completado.
- W2.8: Mercado Pago producción, pendiente.
- W2.9: fundación PWA, Push y aplicación móvil con Capacitor después de R3.3I.

Próximo bloque: convergencia visual gradual de las rutas W2, terminando primero la experiencia web de escritorio.

## 13. Criterios de aceptación

Cada fase debe evitar duplicación de personas, preservar privacidad, admitir instalación limpia, incluir pruebas proporcionales, mantener responsive y teclado, no exponer secretos, cerrar visualmente oscuro y claro, actualizar las guías y dejar una continuidad inequívoca.

## Aplicacion movil independiente

La evolucion movil premium se realizara despues del cierre de la web de escritorio como producto independiente, compartiendo backend y reglas de negocio, con React Native y Flutter como candidatas principales. La decision final se tomara con un prototipo medido, no por reutilizacion de codigo.

## Estado autoritativo posterior al prototipo móvil

> Nota vigente desde el 25 de octubre de 2026: las secciones anteriores sobre Capacitor y CAP3 se conservan únicamente como historial técnico. El proyecto Android, las dependencias Capacitor, las capas nativas y sus pruebas fueron retirados del producto web activo en F0. La dirección vigente es finalizar primero la web de escritorio y desarrollar después una aplicación móvil independiente.
