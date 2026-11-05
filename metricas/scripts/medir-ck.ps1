# Ejecuta CK sobre el código fuente y guarda los CSV de resultados.
# Requiere que el jar de CK esté en metricas/herramientas/ck-ck-0.7.0/target/
# Uso: .\medir-ck.ps1 [ruta-de-salida]
# Ejemplo: .\medir-ck.ps1 ..\resultados\post\

param(
    [string]$Salida = "..\resultados\post\"
)

$jar = "..\herramientas\ck-ck-0.7.0\target\ck-0.7.0-jar-with-dependencies.jar"

if (-not (Test-Path $jar)) {
    Write-Error "No se encontró el jar de CK en $jar. Compílalo primero con: cd metricas\herramientas\ck-ck-0.7.0; .\mvnw.cmd clean package -DskipTests"
    exit 1
}

Write-Host "Ejecutando CK sobre src/api/src/main/java/com/gms..."
java -jar $jar src/api/src/main/java/com/gms true 0 false $Salida
Write-Host "Resultados guardados en $Salida"