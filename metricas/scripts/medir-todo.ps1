# Ejecuta todas las mediciones (Lizard y CK) y guarda los resultados.
# Uso: .\medir-todo.ps1

Write-Host "=== Iniciando mediciones ==="

Write-Host ""
Write-Host "--- Lizard (V(G) y LOC) ---"
& "$PSScriptRoot\medir-lizard.ps1" "..\resultados\lizard-vg-loc.csv"

Write-Host ""
Write-Host "--- CK (Suite CK) ---"
& "$PSScriptRoot\medir-ck.ps1" "..\resultados\post\"

Write-Host ""
Write-Host "=== Mediciones completadas ==="
Write-Host "Resultados en metricas\resultados\"