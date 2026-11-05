# Métricas — Scripts y resultados

Esta carpeta contiene los resultados crudos de las métricas aplicadas al proyecto y la documentación de cómo se generaron.

## Herramientas utilizadas

- **Lizard** v1.25.0 (Python) — Complejidad Ciclomática V(G) y LOC.
- **CK** v0.7.0 (Mauricio Aniche) — Suite CK (WMC, DIT, NOC, CBO, RFC, LCOM).

## Comandos de medición

### Lizard (V(G) y LOC)

```bash
lizard src/api/src/main/java/com/gms -l java -C 10 --csv > metricas/resultados/lizard-vg-loc.csv
```

### CK (Suite CK)

1. Descargar y compilar CK:
   git clone https://github.com/mauricioaniche/ck.git
   cd ck
   mvn clean package -DskipTests

2. Ejecutar CK sobre el código fuente:
   java -jar ck/target/ck-0.7.0-jar-with-dependencies.jar src/api/src/main/java/com/gms true 0 false metricas/resultados

## Resultados

| Archivo | Métrica | Herramienta |
|---|---|---|
| `resultados/lizard-vg-loc.csv` | V(G) y LOC por método | Lizard |
| `resultados/resultadosclass.csv` | Suite CK por clase | CK |
| `resultados/resultadosmethod.csv` | Métricas por método | CK |
## Interpretación

Ver [`docs/metricas/`](../docs/metricas/) para las fichas, resultados interpretados y conclusiones.
