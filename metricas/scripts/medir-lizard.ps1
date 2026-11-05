# Ejecuta Lizard sobre el código fuente y guarda el CSV de resultados.
# Uso: .\medir-lizard.ps1 [ruta-de-salida]
# Ejemplo: .\medir-lizard.ps1 ..\resultados\lizard-vg-loc.csv

param(
    [string]$Salida = "..\resultados\lizard-vg-loc.csv"
)

Write-Host "Ejecutando Lizard sobre src/api/src/main/java/com/gms..."
lizard src/api/src/main/java/com/gms -l java -C 10 --csv > $Salida
Write-Host "Resultados guardados en $Salida"