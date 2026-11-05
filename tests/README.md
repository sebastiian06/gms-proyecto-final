# Pruebas adicionales — GMS

Esta carpeta contiene pruebas que no encajan en el ciclo de vida de Maven ni de Vite.

## Pruebas del backend y frontend

Las pruebas unitarias e integración están dentro de `src`:
- Backend: `src/api/src/test/java/com/gms/` (114 pruebas con JUnit + MockMvc).
- Frontend: `src/portal/src/test/` (2 pruebas con Vitest).

## Pruebas de carga (RNF-01)

El script `carga-rnf01.ps1` mide el tiempo de respuesta de los endpoints de listado para verificar el RNF-01 (≤ 2 segundos en el 95% de los casos).

### Uso

1. Levanta la base de datos y la API:
```
cd infra
docker compose up -d
```

2. Ejecuta el script:
```
.\tests\carga-rnf01.ps1
```