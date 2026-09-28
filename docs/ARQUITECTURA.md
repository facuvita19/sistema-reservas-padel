# Arquitectura

## Vista general

```text
Navegador o celular
        │ HTTP
        ▼
Nginx, contenedor padel-web, puerto 5173
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

Archivos principales:

```text
web-publica/index.html
web-publica/styles.css
web-publica/app.js
web-publica/nginx.conf
```

Responsabilidades:

- Mostrar configuración del complejo.
- Consultar canchas y disponibilidad.
- Crear solicitudes.
- Mostrar pago y vencimiento.
- Consultar seguimiento.
- Abrir WhatsApp.

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
