# Prueba de carga para el RNF-01: mide el tiempo de respuesta de los listados.
# Uso: .\carga-rnf01.ps1

$baseUrl = "http://localhost:8080/api/v1"
$token = Read-Host "Pega el token JWT (obtenido de POST /auth/login)"
$repeticiones = 5

function Medir-Endpoint {
    param($nombre, $url)
    Write-Host "Midiendo $nombre..."
    $tiempos = @()
    for ($i = 1; $i -le $repeticiones; $i++) {
        $inicio = Get-Date
        Invoke-RestMethod -Uri $url -Headers @{ Authorization = "Bearer $token" } | Out-Null
        $fin = Get-Date
        $tiempos += ($fin - $inicio).TotalSeconds
    }
    $tiempos = $tiempos | Sort-Object
    $p95 = $tiempos[[math]::Floor($tiempos.Count * 0.95) - 1]
    Write-Host "$nombre -> tiempos: $($tiempos -join ', ') s"
    Write-Host "$nombre -> percentil 95: $p95 s"
    Write-Host ""
}

Medir-Endpoint "GET /members" "$baseUrl/members?size=100"
Medir-Endpoint "GET /plans" "$baseUrl/plans?size=100"
Medir-Endpoint "GET /memberships" "$baseUrl/memberships?size=100"