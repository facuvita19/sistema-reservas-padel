# Definición de terminado de Plataforma Deportiva W2

La web solo puede declararse terminada cuando se cumplen todos los puntos siguientes.

## Producto

- todas las rutas W2 tienen propósito y propietario claros;
- no existen datos demostrativos en producción;
- no existen botones o paneles que prometan una migración futura;
- las rutas heredadas están migradas, redirigidas o justificadas;
- Inicio, Reservar, Torneos, Rankings, Comunidad, Jugador y Cuenta están completos.

## Datos

- cada cifra visible tiene fuente autoritativa;
- carga, vacío, error, reintento y sesión vencida están definidos;
- no se calculan reglas críticas de forma divergente en frontend;
- los cambios administrativos se reflejan correctamente;
- no hay datos privados en catálogos, URLs, HTML ni logs.

## Cuenta

- Datos personales y Seguridad están integrados;
- Reservas y pagos son autoritativos;
- Perfil deportivo es completo;
- Comunidad privada tiene sección propia;
- invitaciones, solicitudes y convocatorias están sincronizadas;
- errores parciales no expulsan visualmente al usuario.

## Reservas y pagos

- concurrencia e idempotencia validadas;
- duración, moneda, precio y seña son reales;
- Mercado Pago productivo validado;
- transferencia manual documentada;
- retornos y webhook reconciliados;
- Cuenta e Inicio muestran el estado final correcto.

## Torneos, Rankings y Comunidad

- inscripciones solo por parejas;
- reglas Libre, Femenino, Mixto y Suma N coherentes;
- grupos, posiciones, partidos y cuadro son consistentes;
- fórmula de ranking y desempates son auditables;
- privacidad de Comunidad certificada;
- notificaciones convergen con el dominio.

## Visual y accesibilidad

- oscuro y claro completos;
- sin desbordes ni superposiciones;
- navegación completa por teclado;
- foco visible y restaurado;
- textos largos y zoom 200 % soportados;
- movimiento reducido respetado;
- viewports 1920, 1440, 1280, 900 y 390 validados;
- escritorio recibe la experiencia premium principal.

## Rendimiento y PWA

- presupuestos por ruta registrados;
- imágenes, fuentes, CSS y JavaScript optimizados;
- recursos no utilizados retirados;
- actualización PWA previsible;
- caché vieja invalidada;
- offline y reintento coherentes;
- activos Android generados no contaminan builds ni auditorías web.

## Calidad y operación

- check y build en verde;
- pruebas proporcionales por bloque;
- suite integral final en verde;
- backend validado cuando corresponda;
- CI, CodeQL, Secret Scanning y dependencias revisados;
- despliegue y rollback documentados;
- repositorio limpio;
- continuidad inequívoca.

## Exclusión explícita

La aplicación móvil independiente no bloquea el cierre de la web. Comenzará después, compartiendo backend y reglas de negocio, pero no la interfaz web.
