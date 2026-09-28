# Backup y restauración

## Crear backup

MySQL Docker debe estar saludable.

```powershell
cd "C:\Users\facuv\eclipse-workspace\sistema-reservas-padel"
.\backup-mysql.ps1
```

Los archivos se guardan en:

```text
backups/
```

## Listar backups

```powershell
Get-ChildItem ".\backups\*.sql" |
    Sort-Object LastWriteTime -Descending |
    Select-Object Name, Length, LastWriteTime
```

## Restaurar el más reciente

```powershell
$backup = Get-ChildItem ".\backups\*.sql" |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

.\restaurar-mysql.ps1 -Archivo $backup.FullName
```

Escribir `RESTAURAR` cuando sea solicitado.

## Qué hace la restauración

1. Detiene API y web.
2. Conserva el contenedor MySQL.
3. Recrea la base `padel_reservas`.
4. Importa el archivo SQL.
5. Reinicia API y web.

## Validación posterior

```powershell
docker compose ps
```

Comprobar:

- Configuración del complejo.
- Canchas.
- Clientes.
- Reservas.
- Pagos.
- Seguimiento público.
- Caracteres con tildes y `ñ`.

## Reglas operativas

- Crear backups periódicos.
- Mantener una copia fuera del equipo principal.
- No guardar backups en Git.
- Probar restauraciones periódicamente.
- No utilizar archivos que contengan caracteres dañados como `P??del`.
