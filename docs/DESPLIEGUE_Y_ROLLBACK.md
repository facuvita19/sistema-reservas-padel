# Despliegue y rollback

## Despliegue controlado

El despliegue exige repositorio limpio, preflight seguro, backup con checksum, migraciones preparadas y validación posterior.

```powershell
.\desplegar-seguro.ps1 -Modo local
```

En producción usar `-Modo produccion`. La salida no imprime secretos.

## Validación posterior

```powershell
.\validar-despliegue.ps1
```

Comprueba salud de los tres servicios, API mediante Nginx, web y CSP.

## Rollback

```powershell
.\rollback-seguro.ps1
```

La base no se restaura por defecto. Usar `-RestaurarBase` solo cuando el cambio haya modificado datos o esquema de forma incompatible. El rollback siempre pide escribir `ROLLBACK`.

## Checklist productivo

- DNS y HTTPS activos.
- `WEB_ORIGIN` y URL pública exactos.
- `API_COOKIES_SECURE=true`.
- SMTP validado.
- Backup externo disponible.
- CI verde para el commit.
- Ventana de mantenimiento comunicada.
- Responsable y criterio de rollback definidos.
