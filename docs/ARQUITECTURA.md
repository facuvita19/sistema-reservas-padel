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
