# Arquitectura

## Vista general

```text
Navegador o celular
        │ HTTP
        ▼
Nginx, contenedor padel-web, puerto 4321
        │ HTTP JSON
        ▼
API Java, contenedor padel-api, puerto 8080
        │ JDBC
        ▼
MySQL, contenedor padel-mysql, puerto interno 3306
        ▲
        │ JDBC por 127.0.0.1:3307
Aplicación administrativa JavaFX
```

## Web pública

La única web activa es `web-publica-v2`, construida con Astro y TypeScript y servida por Nginx. `web-publica` queda únicamente como referencia histórica.

Rutas canónicas principales:

```text
/w2
/w2/reservar
/w2/torneos
/w2/rankings
/w2/comunidad
/w2/cuenta
```

Responsabilidades:

- disponibilidad y reservas;
- cuenta, seguimiento y pagos;
- torneos, rankings y perfiles;
- Comunidad, invitaciones y centro de notificaciones;
- experiencia responsive, accesible y preparada para PWA.

## API pública

Clase principal:

```text
api.publica.ApiPublicaApp
```

Responsabilidades:

- Iniciar el servidor HTTP.
- Ejecutar el procesador de vencimientos.
- Exponer respuestas JSON.
- Aplicar límites y cabeceras de seguridad.

## Procesador de vencimientos

`ProcesadorVencimientosService` se ejecuta dentro de la API independiente.

Condiciones para expirar:

```text
origen = WEB
estado = PENDIENTE
fecha_vencimiento <= ahora
sin pagos ACREDITADOS
```

El intervalo Docker se configura con:

```yaml
VENCIMIENTOS_INTERVALO_SEGUNDOS: "30"
```

## Base de datos

MySQL Docker se publica sólo en la interfaz local:

```text
127.0.0.1:3307
```

La API se conecta internamente a:

```text
mysql:3306
```

## Zona horaria

MySQL y Java usan:

```text
America/Argentina/Buenos_Aires
```

Esto evita diferencias de 180 minutos entre servidor y navegador.

## Persistencia

El volumen:

```text
sistema-reservas-padel_padel_mysql_data
```

conserva los datos al detener o recrear contenedores. `docker compose down -v` elimina ese volumen.

## Decision arquitectonica: aplicacion movil independiente (25 de octubre de 2026)

La prueba de envolver la web publica W2 completa con Capacitor se cierra como prototipo tecnico, no como direccion de produccion. Permitio validar compilacion Android, conectividad LAN, sesion, navegacion, boton Atras y comportamiento del teclado, pero tambien demostro que reutilizar directamente la interfaz web no alcanza el estandar movil premium buscado.

### Decision

1. Terminar, estabilizar y optimizar primero la web publica de escritorio con Astro.
2. Mantener la PWA como experiencia web instalada y responsive, sin exigirle que sustituya una aplicacion nativa premium.
3. Crear despues un proyecto movil independiente que comparta API, autenticacion, reglas de negocio y datos, pero no la estructura HTML, el CSS ni los overlays de la web.
4. Evaluar React Native y Flutter mediante un vertical slice medido en dispositivos reales; Ionic/Capacitor puede participar como referencia, no como opcion automatica.
5. Priorizar el mejor resultado aunque implique una reconstruccion completa o mayor costo.

### Vertical slice obligatorio

- arranque y restauracion de sesion;
- Inicio;
- login de pantalla completa;
- Reservar;
- Cuenta;
- navegacion inferior persistente;
- tema oscuro principal;
- teclado integrado sin parpadeos ni controles ocultos;
- cache local y comportamiento con red lenta;
- notificaciones basicas.

### Criterios de aceptacion

- respuesta tactil inmediata y navegacion sin recargas de documentos;
- 60 fps estables en el dispositivo objetivo;
- apertura y cambio de foco del teclado sin saltos;
- barras del sistema e insets correctos;
- interfaz moderna, profesional e imponente;
- accesibilidad, privacidad, seguridad y mantenibilidad sin regresiones;
- medicion de arranque, memoria, cuadros perdidos y estabilidad antes de ampliar alcance.

### Estado del prototipo Capacitor

El codigo experimental de CAP5, CAP5B y MOBILE DEFINITIVO fue retirado de la web. Se restauraron la identidad original, la cabecera, el tema, la autenticacion, la navegacion movil y el runtime Android rastreados en el ultimo estado estable. Rankings conserva su hero editorial sin fotografia conceptual y Dependabot conserva el bloqueo de actualizaciones mayores automaticas de TypeScript.
