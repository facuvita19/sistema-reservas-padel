# Mapa de experiencia W2

> Navegación, páginas y recorridos prioritarios de Plataforma Deportiva W2.

## 1. Navegación principal

```text
Inicio
Reservar
Competir
Rankings
Comunidad
Mi cuenta
```

Competir agrupa Torneos, Calendario, Resultados y Jugadores. Mi cuenta agrupa Resumen, Mis reservas, Mis datos, Seguridad, Mi perfil deportivo, Mi historial, Mis torneos, Mis pagos y Preferencias. La navegación W2 debe reutilizar la sesión real y reemplazar gradualmente el shell anterior sin perder funciones.

## 2. Mapa de páginas

### Públicas

- Inicio;
- Reservar;
- Seguimiento de reserva;
- Torneos;
- Detalle de torneo;
- Centro competitivo;
- Rankings;
- Ranking por categoría;
- Jugador público limitado;
- Cambios de categoría;
- Comunidad y convocatorias visibles;
- acceso, registro y recuperación.

### Privadas

- Resumen de cuenta;
- Mis reservas;
- Detalle de reserva y pago;
- Mi perfil deportivo;
- Mi historial;
- Mis torneos;
- Mis partidos;
- Mis convocatorias y solicitudes;
- Mis pagos;
- Preferencias y seguridad.

## 2A. Estado de convergencia al 13 de octubre de 2026

### Operativo en W2

- ranking real por categoría;
- auditoría de puntos;
- perfil deportivo público limitado;
- navegación entre ranking y perfil;
- estados vacíos y de error iniciales.

### Operativo en la web vigente y pendiente de integrar al shell W2

- autenticación, registro, recuperación y sesión persistente;
- menú de cuenta;
- Mis datos y seguridad;
- Mis reservas, seguimiento, señas y estados;
- reserva real de canchas;
- catálogo, detalle, inscripción y centro competitivo de torneos;
- navegación móvil funcional.

### Regla de retiro

La versión anterior de una experiencia solo se retira después de comprobar paridad de datos, seguridad, validaciones, estados, responsive, accesibilidad y pruebas.

## 2B. Centro de cuenta W2

~~~text
Mi cuenta
├── Resumen
├── Mis reservas
├── Mis datos
├── Seguridad
├── Mi perfil deportivo
├── Mi historial
├── Mis torneos
├── Mis pagos
└── Preferencias
~~~

El centro de cuenta será la unión ordenada entre identidad comercial y deportiva. Los datos de categoría, ranking y resultados son de lectura; las preferencias permitidas se editan con privacidad explícita.


## 3. Recorrido Reservar

1. Seleccionar fecha o próximo turno.
2. Comparar disponibilidad por cancha.
3. Elegir turno.
4. Ver alternativas y condiciones.
5. Identificarse o registrarse.
6. Confirmar datos.
7. Crear retención temporal.
8. Pagar seña.
9. Procesar webhook.
10. Mostrar confirmación verificable.

Estados diseñados: sin disponibilidad, turno tomado, sesión vencida, pago pendiente, aprobado, rechazado, vencido y revisión manual.

## 4. Recorrido Ranking

1. Elegir categoría de 1.ª a 8.ª.
2. Ver podio y tabla.
3. Buscar jugador.
4. Abrir trayectoria.
5. Consultar movimientos y torneos computados.
6. Entender reglas y ventana de 12 meses.
7. Ver clasificación al Master.

## 5. Recorrido Perfil deportivo

### Vista privada

- resumen personal;
- categoría vigente;
- posición y variación;
- historial completo;
- próximos partidos;
- torneos;
- parejas;
- convocatorias;
- datos editables permitidos.

### Vista pública limitada

- nombre completo;
- categoría;
- ranking;
- resultados deportivos autorizados;
- nunca teléfono, correo, documento, pagos ni datos comerciales.

## 6. Recorrido Inscripción a torneo

1. Seleccionar categoría o formato.
2. Elegir o identificar pareja.
3. Evaluar categoría de ambos integrantes.
4. Mostrar compatible o requiere revisión.
5. Explicar Suma o combinación.
6. Enviar inscripción.
7. Organizador aprueba, autoriza excepción o rechaza.
8. Pagar inscripción cuando se habilite.

## 7. Recorrido Americano

1. Ver reglas configuradas.
2. Inscribirse como individuo o pareja según modalidad.
3. Consultar rondas y rotaciones.
4. Ver marcadores y tabla.
5. Consultar desempates y posición final.

## 8. Recorrido Ascensos y descensos

1. Organizador registra cambios.
2. Sistema valida vigencias.
3. Se prepara publicación agrupada.
4. Organizador revisa y publica.
5. Web muestra anuncio y detalle.
6. Perfil refleja nueva categoría desde la fecha indicada.
7. Historial conserva categoría anterior.

## 9. Recorrido Comunidad

1. Crear convocatoria.
2. Definir tipo, fecha, categoría y cupos.
3. Recibir solicitudes.
4. Aceptar o rechazar.
5. Completar cupos.
6. Asociar o crear reserva.
7. Cerrar y notificar.

## 10. Inicio W2

Orden recomendado:

1. hero y acción Reservar;
2. disponibilidad o próximos turnos;
3. torneos activos;
4. ranking destacado;
5. cambios de categoría;
6. convocatorias;
7. beneficios e instalaciones;
8. ubicación, horarios y contacto.

## 11. Prototipo W2.0B

El primer prototipo estático deberá cubrir:

- Inicio W2;
- Ranking categoría 6.ª;
- Perfil de jugador;
- Matriz de reserva;
- navegación desktop y móvil;
- estados de carga, vacío y error.

Los datos serán fixtures de desarrollo explícitos y nunca se presentarán como reales en producción.

## 12A. Próximo recorrido prioritario

1. Entrar o registrarse desde el shell W2.
2. Ver avatar y menú de cuenta.
3. Abrir Resumen de cuenta.
4. Navegar a Mis reservas, Mis datos, Seguridad o Perfil deportivo.
5. Mantener sesión y contexto al cambiar entre áreas comerciales y deportivas.
6. Resolver sesión vencida, API inaccesible y móvil sin perder el recorrido.

Este recorrido precede a Comunidad para asegurar identidad, control de privacidad y paridad funcional.


## 12. Métricas futuras

- tiempo hasta elegir turno;
- abandono por paso;
- conversión a reserva;
- aprobación de pago;
- búsquedas de ranking;
- perfiles consultados;
- convocatorias completadas;
- inscripciones que requieren excepción;
- tiempo de respuesta del organizador.
