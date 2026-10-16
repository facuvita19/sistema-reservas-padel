# Plataforma Deportiva W2

> Evolución integral de `web-publica-v2` hacia una plataforma deportiva, competitiva y comercial premium. Actualizado el 16 de octubre de 2026.

## 1. Visión

W2 es la reconstrucción definitiva de la web pública. Une reservas, identidad deportiva, categorías, rankings auditables, historial, perfiles, parejas, comunidad, torneos, notificaciones y pagos reales sin duplicar personas ni fragmentar la experiencia.

## 2. Principios obligatorios

- W2 reemplaza integralmente la experiencia anterior.
- Ninguna función se retira antes de una alternativa igual o superior.
- La web anterior es referencia funcional, no límite visual ni técnico.
- Modo oscuro es la experiencia principal; modo claro es una alternativa real.
- La aplicación administrativa es independiente de la web pública.
- Cuenta, cliente y perfil deportivo representan responsabilidades distintas de una misma persona.
- Ranking, categorías y correcciones deben ser auditables.
- Pagos se confirman en backend, nunca por la URL de retorno.
- La excelencia visual, accesibilidad y mantenibilidad prevalecen sobre el costo de implementación.

## 3. Identidad deportiva

- Cuenta: autenticación y seguridad.
- Cliente: relación comercial, reservas y pagos.
- Perfil deportivo: categoría, ranking, historial y preferencias deportivas.
- Participantes sin cuenta: identidad provisional vinculable posteriormente con revisión segura.
- La vinculación conserva puntos, torneos y partidos y evita fusiones ambiguas.

## 4. Categorías y elegibilidad

Catálogo global inicial: 1.ª a 8.ª, con orden competitivo explícito.

La categoría vigente posee historial, vigencia, origen, motivo y responsable administrativo. Ascensos y descensos son decisiones manuales y auditables.

### Condiciones de inscripción

- categoría única;
- categorías combinadas, por ejemplo 4.ª + 5.ª;
- Suma 8, Suma 10, Suma 12, etc.;
- excepción administrativa auditable.

Nunca usar “suma máxima”. La condición se nombra por su identidad pública: **Suma 8**, **Suma 10**, **Suma 12**.

## 5. Taxonomía de Torneos

Las siguientes dimensiones son independientes y combinables:

### Rama

- Libre;
- Femenino;
- Mixto.

Libre admite parejas integradas por personas de cualquier sexo. Mixto exige una mujer y un hombre.

### Formato competitivo

- Eliminación directa.
- Grupos + eliminación.

El filtro público Formato solo debe contener estas organizaciones competitivas.

### Condición de inscripción

- Categoría única.
- Categorías combinadas.
- Suma 8, Suma 10, Suma 12, etc.

### Modalidad de partido

- Un set.
- Dos sets más super tie-break.
- Mejor de tres sets.

### Dinámica

- Mismo día.
- Varios días.

### Americano

Americano es un tipo especial de evento rápido, no un formato competitivo ni una condición de inscripción.

Reglas:

- evento ocasional;
- normalmente finaliza el mismo día;
- inscripción siempre por pareja;
- puede usar grupos más eliminación o eliminación directa;
- puede usar un set o dos sets más super tie-break;
- no se modela como competencia semanal o mensual.

No mostrar “Convencional” como etiqueta pública. Los torneos estándar no necesitan distintivo; los Americanos sí.

## 6. Inscripciones

- Exclusivamente por pareja en todos los torneos.
- No hay inscripción individual, tampoco en Americanos.
- Comunidad permitirá encontrar pareja previamente.
- La aceptación administrativa es la decisión final de elegibilidad.
- Las excepciones no cambian automáticamente la categoría del perfil.

## 7. Ranking e historial

- Ranking individual por categoría.
- Ventana móvil prevista de 12 meses.
- Puntos reproducibles por fase y regla versionada.
- Ambos integrantes reciben los puntos del resultado compartido.
- Correcciones recalculan movimientos de forma auditada.
- Masters de fin de año por categoría con configuración propia.
- Historial con torneos, partidos, pareja, rivales, resultado, fase y puntos.

## 8. Reserva, comunidad y pagos

### Reserva inteligente

Matriz de horarios, filtros, alternativas, repetición, favoritos, lista de espera, retención temporal y resumen premium.

### Comunidad

Buscar pareja, completar partidos, buscar rivales, publicar disponibilidad y asociar convocatorias con reservas, siempre con privacidad y moderación.

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

## 9. Torneos W2: estado real

Ruta principal:

```text
/w2/torneos
```

No existe una sección principal llamada Competir.

Estado completado:

- catálogo W2;
- detalle nativo;
- centro competitivo con Resumen, Posiciones, Partidos y Cuadro;
- persistencia de categoría, vista, ronda y filtros;
- caché y cancelación de solicitudes;
- nomenclatura pública Libre;
- tarjetas con geometría uniforme;
- modularización progresiva de `torneos.ts`.

Módulos actuales:

```text
src/scripts/torneos/
├── adaptador-catalogo-torneos.ts
├── datos-torneos.ts
├── dominio-torneos.ts
└── estado-catalogo-torneos.ts
```

Próximo bloque: R2.1C5, presentación y navegación del detalle.

## 10. Fases

- W2.0: dirección y arquitectura, completado.
- W2.1: identidad y categorías, alcance inicial completado.
- W2.2: historial competitivo, alcance inicial completado.
- W2.3: ranking inicial operativo; motor avanzado pendiente.
- W2.4: experiencia web deportiva y convergencia, en curso.
- W2.5: reserva inteligente.
- W2.6: comunidad.
- W2.7: Mercado Pago sandbox, completado.
- W2.8: Mercado Pago producción, pendiente.
- W2.9: engagement, PWA y notificaciones.

## 11. Criterios de aceptación

Cada fase debe evitar duplicación de personas, preservar privacidad, admitir instalación limpia, incluir pruebas proporcionales, mantener responsive y teclado, no exponer secretos y actualizar las guías cuando cambie el punto real de continuidad.
