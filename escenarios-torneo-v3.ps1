param(
    [ValidateSet("2x3", "2x4", "4x3", "3x4", "2x3-1x4", "4x4", "5x3", "limpiar", "resumen")]
    [string]$Escenario = "resumen",
    [long]$CategoriaId = 11,
    [string]$Contenedor = "padel-mysql"
)

$ErrorActionPreference = "Stop"

function Invoke-MySql {
    param([Parameter(Mandatory)][string]$Sql)
    $salida = $Sql | docker exec -i $Contenedor sh -lc 'mysql --batch --raw --skip-column-names -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"'
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL devolvio codigo $LASTEXITCODE."
    }
    return $salida
}

function Get-ScenarioGroups {
    param([Parameter(Mandatory)][string]$Nombre)
    switch ($Nombre) {
        "2x3"     { return [int[]]@(3, 3) }
        "2x4"     { return [int[]]@(4, 4) }
        "4x3"     { return [int[]]@(3, 3, 3, 3) }
        "3x4"     { return [int[]]@(4, 4, 4) }
        "2x3-1x4" { return [int[]]@(3, 3, 4) }
        "4x4"     { return [int[]]@(4, 4, 4, 4) }
        "5x3"     { return [int[]]@(3, 3, 3, 3, 3) }
        default    { return [int[]]@() }
    }
}

function Add-SqlLine {
    param(
        [Parameter(Mandatory)][System.Text.StringBuilder]$Builder,
        [Parameter(Mandatory)][string]$Line
    )
    [void]$Builder.AppendLine($Line)
}

function Add-TestMatch {
    param(
        [Parameter(Mandatory)][System.Text.StringBuilder]$Builder,
        [Parameter(Mandatory)][long]$CategoriaId,
        [Parameter(Mandatory)][string]$GrupoVar,
        [Parameter(Mandatory)][int]$GrupoNumero,
        [Parameter(Mandatory)][int]$Orden,
        [Parameter(Mandatory)][string]$Tipo,
        [Parameter(Mandatory)][string]$Pareja1,
        [Parameter(Mandatory)][string]$Pareja2,
        [Parameter(Mandatory)][string]$Ganadora,
        [int]$Set1P1 = 6,
        [int]$Set1P2 = 2,
        [int]$Set2P1 = 6,
        [int]$Set2P2 = 3
    )
    $partidoVar = "@m${GrupoNumero}_${Orden}"
    Add-SqlLine $Builder "INSERT INTO torneo_partidos(torneo_categoria_id,grupo_id,fase,tipo_partido_grupo,orden_fase,pareja_1_inscripcion_id,pareja_2_inscripcion_id,estado,ganadora_inscripcion_id,usuario_resultado_id,fecha_finalizacion,observaciones) VALUES(@categoria,$GrupoVar,'GRUPOS','$Tipo',$Orden,$Pareja1,$Pareja2,'FINALIZADO',$Ganadora,@usuario,NOW(),'Resultado automatizado de prueba');"
    Add-SqlLine $Builder "SET $partidoVar := LAST_INSERT_ID();"
    Add-SqlLine $Builder "INSERT INTO torneo_partido_sets(partido_id,numero_set,tipo,puntos_pareja_1,puntos_pareja_2) VALUES($partidoVar,1,'NORMAL',$Set1P1,$Set1P2),($partidoVar,2,'NORMAL',$Set2P1,$Set2P2);"
    return $partidoVar
}

function New-ScenarioSql {
    param(
        [Parameter(Mandatory)][long]$CategoryId,
        [Parameter(Mandatory)][int[]]$Sizes,
        [Parameter(Mandatory)][string]$Name
    )

    $total = ($Sizes | Measure-Object -Sum).Sum
    $groups3 = @($Sizes | Where-Object { $_ -eq 3 }).Count
    $groups4 = @($Sizes | Where-Object { $_ -eq 4 }).Count
    $sb = [System.Text.StringBuilder]::new()

    Add-SqlLine $sb "SET NAMES utf8mb4;"
    Add-SqlLine $sb "START TRANSACTION;"
    Add-SqlLine $sb "SET @categoria := $CategoryId;"
    Add-SqlLine $sb "SET @usuario := (SELECT id FROM usuarios WHERE activo=1 ORDER BY id LIMIT 1);"
    Add-SqlLine $sb "UPDATE torneo_partidos SET partido_siguiente_id=NULL,posicion_siguiente=NULL WHERE torneo_categoria_id=@categoria;"
    Add-SqlLine $sb "DELETE FROM torneo_partidos WHERE torneo_categoria_id=@categoria;"
    Add-SqlLine $sb "DELETE gi FROM torneo_grupo_integrantes gi INNER JOIN torneo_grupos g ON g.id=gi.grupo_id WHERE g.torneo_categoria_id=@categoria;"
    Add-SqlLine $sb "DELETE FROM torneo_grupos WHERE torneo_categoria_id=@categoria;"
    Add-SqlLine $sb "DELETE FROM torneo_inscripciones WHERE torneo_categoria_id=@categoria;"
    Add-SqlLine $sb "UPDATE torneo_categorias SET cupo_parejas=$total,formato_competencia='GRUPOS_ELIMINACION',cantidad_grupos_3=$groups3,cantidad_grupos_4=$groups4 WHERE id=@categoria;"

    for ($i = 1; $i -le $total; $i++) {
        $parejaVar = "@p$i"
        $telefono1 = "1155{0:D6}" -f ($CategoryId * 100 + $i * 2 - 1)
        $telefono2 = "1155{0:D6}" -f ($CategoryId * 100 + $i * 2)
        Add-SqlLine $sb "INSERT INTO torneo_inscripciones(torneo_categoria_id,estado,origen,precio_inscripcion,comentarios,observaciones_administrativas,usuario_gestion_id,fecha_confirmacion) VALUES(@categoria,'CONFIRMADA','ADMINISTRACION',0,'ESCENARIO $Name','DATOS DE PRUEBA AUTOMATIZADOS',@usuario,NOW());"
        Add-SqlLine $sb "SET $parejaVar := LAST_INSERT_ID();"
        Add-SqlLine $sb "INSERT INTO torneo_inscripcion_jugadores(inscripcion_id,orden_integrante,nombre,apellido,telefono,telefono_normalizado,es_responsable,tipo_vinculacion,requiere_revision) VALUES($parejaVar,1,'PRUEBA-$Name-P$i','Jugador A','$telefono1','$telefono1',1,'SIN_VINCULAR',0),($parejaVar,2,'PRUEBA-$Name-P$i','Jugador B','$telefono2','$telefono2',0,'SIN_VINCULAR',0);"
    }

    $parejaGlobal = 0
    $ordenGlobalPartido = 0
    for ($grupoIndice = 0; $grupoIndice -lt $Sizes.Count; $grupoIndice++) {
        $grupoNumero = $grupoIndice + 1
        $capacidad = $Sizes[$grupoIndice]
        $letra = [char](64 + $grupoNumero)
        $grupoVar = "@g$grupoNumero"
        Add-SqlLine $sb "INSERT INTO torneo_grupos(torneo_categoria_id,nombre,orden,capacidad,modo_asignacion,confirmado) VALUES(@categoria,'Grupo $letra',$grupoNumero,$capacidad,'MANUAL',1);"
        Add-SqlLine $sb "SET $grupoVar := LAST_INSERT_ID();"

        $parejasGrupo = @()
        for ($posicion = 1; $posicion -le $capacidad; $posicion++) {
            $parejaGlobal++
            $parejaVar = "@p$parejaGlobal"
            $parejasGrupo += $parejaVar
            $cabeza = if ($posicion -eq 1) { 1 } else { 0 }
            Add-SqlLine $sb "INSERT INTO torneo_grupo_integrantes(grupo_id,inscripcion_id,cabeza_serie,orden_sorteo) VALUES($grupoVar,$parejaVar,$cabeza,$posicion);"
        }

        if ($capacidad -eq 3) {
            $ordenGlobalPartido++
            [void](Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "TODOS_CONTRA_TODOS" $parejasGrupo[0] $parejasGrupo[1] $parejasGrupo[0] 6 2 6 3)
            $ordenGlobalPartido++
            [void](Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "TODOS_CONTRA_TODOS" $parejasGrupo[0] $parejasGrupo[2] $parejasGrupo[0] 6 1 6 2)
            $ordenGlobalPartido++
            [void](Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "TODOS_CONTRA_TODOS" $parejasGrupo[1] $parejasGrupo[2] $parejasGrupo[1] 6 3 6 4)
        } else {
            $ordenGlobalPartido++
            $m1 = Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "CRUCE_INICIAL" $parejasGrupo[0] $parejasGrupo[3] $parejasGrupo[0] 6 2 6 3
            $ordenGlobalPartido++
            if ($Name -eq "3x4" -and $grupoNumero -eq 1) {
                $m2 = Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "CRUCE_INICIAL" $parejasGrupo[1] $parejasGrupo[2] $parejasGrupo[1] 7 5 7 5
            } elseif ($Name -eq "3x4" -and $grupoNumero -eq 2) {
                $m2 = Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "CRUCE_INICIAL" $parejasGrupo[1] $parejasGrupo[2] $parejasGrupo[1] 6 4 6 4
            } else {
                $m2 = Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "CRUCE_INICIAL" $parejasGrupo[1] $parejasGrupo[2] $parejasGrupo[1] 6 2 6 3
            }
            $ordenGlobalPartido++
            $m3 = Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "DEFINICION_PRIMERO_SEGUNDO" $parejasGrupo[0] $parejasGrupo[1] $parejasGrupo[0] 6 2 6 4
            $ordenGlobalPartido++
            if ($Name -eq "3x4" -and $grupoNumero -eq 1) {
                $m4 = Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "DEFINICION_TERCERO_CUARTO" $parejasGrupo[2] $parejasGrupo[3] $parejasGrupo[2] 6 0 6 0
            } elseif ($Name -eq "3x4" -and $grupoNumero -eq 2) {
                $m4 = Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "DEFINICION_TERCERO_CUARTO" $parejasGrupo[2] $parejasGrupo[3] $parejasGrupo[2] 6 2 6 2
            } else {
                $m4 = Add-TestMatch $sb $CategoryId $grupoVar $grupoNumero $ordenGlobalPartido "DEFINICION_TERCERO_CUARTO" $parejasGrupo[2] $parejasGrupo[3] $parejasGrupo[2] 6 4 6 4
            }
            Add-SqlLine $sb "INSERT INTO torneo_partido_enlaces(partido_origen_id,resultado_origen,partido_destino_id,posicion_destino) VALUES($m1,'GANADOR',$m3,'PAREJA_1'),($m1,'PERDEDOR',$m4,'PAREJA_2'),($m2,'GANADOR',$m3,'PAREJA_2'),($m2,'PERDEDOR',$m4,'PAREJA_1');"
        }
    }

    Add-SqlLine $sb "COMMIT;"
    Add-SqlLine $sb "SELECT CONCAT('ESCENARIO CREADO|',@categoria,'|$Name|$total');"
    Add-SqlLine $sb "SELECT CONCAT(g.nombre,'|',g.capacidad,'|',COUNT(gi.id),'|',g.confirmado) FROM torneo_grupos g LEFT JOIN torneo_grupo_integrantes gi ON gi.grupo_id=g.id WHERE g.torneo_categoria_id=@categoria GROUP BY g.id ORDER BY g.orden;"
    Add-SqlLine $sb "SELECT CONCAT(fase,'|',COALESCE(tipo_partido_grupo,''),'|',COUNT(*),'|',SUM(estado='FINALIZADO')) FROM torneo_partidos WHERE torneo_categoria_id=@categoria GROUP BY fase,tipo_partido_grupo ORDER BY fase,tipo_partido_grupo;"
    Add-SqlLine $sb "SELECT CONCAT('ORDENES|',COUNT(*),'|',COUNT(DISTINCT orden_fase),'|',MIN(orden_fase),'|',MAX(orden_fase)) FROM torneo_partidos WHERE torneo_categoria_id=@categoria AND fase='GRUPOS';"
    return $sb.ToString()
}

function New-CleanupSql {
    param([Parameter(Mandatory)][long]$CategoryId)
    return @"
SET NAMES utf8mb4;
START TRANSACTION;
SET @categoria := $CategoryId;
UPDATE torneo_partidos SET partido_siguiente_id=NULL,posicion_siguiente=NULL WHERE torneo_categoria_id=@categoria;
DELETE FROM torneo_partidos WHERE torneo_categoria_id=@categoria;
DELETE gi FROM torneo_grupo_integrantes gi INNER JOIN torneo_grupos g ON g.id=gi.grupo_id WHERE g.torneo_categoria_id=@categoria;
DELETE FROM torneo_grupos WHERE torneo_categoria_id=@categoria;
DELETE FROM torneo_inscripciones WHERE torneo_categoria_id=@categoria AND observaciones_administrativas='DATOS DE PRUEBA AUTOMATIZADOS';
COMMIT;
SELECT CONCAT('ESCENARIO LIMPIO|',@categoria);
"@
}

function New-SummarySql {
    param([Parameter(Mandatory)][long]$CategoryId)
    return @"
SET @categoria := $CategoryId;
SELECT CONCAT(t.id,'|',t.nombre,'|',t.estado,'|',c.id,'|',c.nombre,'|',c.formato_competencia,'|',c.cantidad_grupos_3,'|',c.cantidad_grupos_4,'|',c.cupo_parejas) FROM torneo_categorias c INNER JOIN torneos t ON t.id=c.torneo_id WHERE c.id=@categoria;
SELECT CONCAT(g.id,'|',g.nombre,'|',g.capacidad,'|',g.confirmado,'|',COUNT(gi.id)) FROM torneo_grupos g LEFT JOIN torneo_grupo_integrantes gi ON gi.grupo_id=g.id WHERE g.torneo_categoria_id=@categoria GROUP BY g.id ORDER BY g.orden;
SELECT CONCAT(fase,'|',COUNT(*),'|',SUM(estado='FINALIZADO'),'|',SUM(estado='PENDIENTE')) FROM torneo_partidos WHERE torneo_categoria_id=@categoria GROUP BY fase ORDER BY FIELD(fase,'GRUPOS','ACCESO_1','ACCESO_2','ACCESO_3','ACCESO_4','ACCESO_5','DIECISEISAVOS','OCTAVOS','CUARTOS','SEMIFINAL','FINAL');
SELECT CONCAT('INSCRIPCIONES|',COUNT(*),'|',SUM(estado='CONFIRMADA')) FROM torneo_inscripciones WHERE torneo_categoria_id=@categoria;
"@
}

if ($Escenario -eq "resumen") {
    Invoke-MySql -Sql (New-SummarySql -CategoryId $CategoriaId)
    exit 0
}

Write-Host "Categoria objetivo: $CategoriaId" -ForegroundColor Cyan
Write-Host "Escenario: $Escenario" -ForegroundColor Cyan
Write-Host "Esta operacion elimina grupos, inscripciones y partidos de la categoria indicada." -ForegroundColor Yellow
$confirmacion = Read-Host "Escribi exactamente PRUEBA-$CategoriaId para continuar"
if ($confirmacion -ne "PRUEBA-$CategoriaId") {
    Write-Host "Operacion cancelada." -ForegroundColor Yellow
    exit 0
}

$checkSql = @"
SELECT CONCAT(c.id,'|',t.estado,'|',c.formato_competencia,'|',(SELECT COUNT(*) FROM torneo_partidos p WHERE p.torneo_categoria_id=c.id AND p.fase<>'GRUPOS' AND (p.estado IN ('EN_CURSO','FINALIZADO') OR p.ganadora_inscripcion_id IS NOT NULL)),'|',(SELECT COUNT(*) FROM torneo_inscripciones i WHERE i.torneo_categoria_id=c.id AND COALESCE(i.observaciones_administrativas,'')<>'DATOS DE PRUEBA AUTOMATIZADOS')) FROM torneo_categorias c INNER JOIN torneos t ON t.id=c.torneo_id WHERE c.id=$CategoriaId;
"@
$linea = @(Invoke-MySql -Sql $checkSql | Where-Object { $_ -and $_.Trim() }) | Select-Object -Last 1
if (-not $linea) { throw "La categoria $CategoriaId no existe." }
$partes = $linea -split "\|"
if ($partes[1] -notin @("INSCRIPCION_CERRADA", "EN_CURSO")) {
    throw "Estado de torneo no permitido: $($partes[1])."
}
if ([int]$partes[3] -gt 0) {
    throw "Hay partidos eliminatorios disputados. No se modifico nada."
}
if ([int]$partes[4] -gt 0) {
    Write-Warning "La categoria contiene inscripciones ajenas al generador. Tambien seran eliminadas."
    $extra = Read-Host "Escribi BORRAR-TODO-$CategoriaId para autorizar"
    if ($extra -ne "BORRAR-TODO-$CategoriaId") {
        Write-Host "Operacion cancelada." -ForegroundColor Yellow
        exit 0
    }
}

if ($Escenario -eq "limpiar") {
    Invoke-MySql -Sql (New-CleanupSql -CategoryId $CategoriaId)
    exit 0
}

$grupos = Get-ScenarioGroups -Nombre $Escenario
$sql = New-ScenarioSql -CategoryId $CategoriaId -Sizes $grupos -Name $Escenario
Invoke-MySql -Sql $sql
Write-Host "Escenario listo. Abri la categoria y presiona GENERAR CUADRO." -ForegroundColor Green
