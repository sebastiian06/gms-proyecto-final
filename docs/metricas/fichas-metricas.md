# Fichas de Métricas — GMS

Este documento contiene las fichas de las 3 métricas seleccionadas para el proyecto, siguiendo el formato del **Anexo B** del proyecto final.

---

## Ficha 1: Complejidad Ciclomática V(G) (McCabe)

| Campo | Contenido |
|---|---|
| **Nombre de la métrica** | Complejidad Ciclomática V(G) (McCabe) |
| **Atributo que mide** | Complejidad de control de flujo de un método (número de caminos linealmente independientes). |
| **Fórmula o forma de cálculo** | V(G) = número de puntos de decisión + 1 (práctica). Formalmente: V(G) = E - N + 2P, donde E = aristas, N = nodos, P = componentes conectados. |
| **Umbral de referencia e interpretación** | 1–10: riesgo bajo; 11–20: moderado; 21–50: alto; >50: muy alto. |
| **Herramienta o script de medición** | Lizard v1.25.0 (Python). Comando: `lizard src/api/src/main/java/com/gms -l java -C 10 --csv > metricas/resultados/lizard-vg-loc.csv`. |
| **Alcance de la medición** | Todos los métodos de `src/api/src/main/java/com/gms/` (módulos identity, members, plans, billing, attendance, training, reports, platform). |
| **Justificación** | Métrica estructural obligatoria según la regla de selección. Conecta directamente con el RNF-03 (V(G) ≤ 10). Permite identificar métodos que requieren refactorización (Extract Method). |
| **Acción asociada si se supera el umbral** | Aplicar **Extract Method** para dividir el método en partes más pequeñas y cohesivas. |

---

## Ficha 2: LOC (líneas efectivas) y % de comentarios

| Campo | Contenido |
|---|---|
| **Nombre de la métrica** | Líneas de código (LOC efectivas) y porcentaje de comentarios |
| **Atributo que mide** | Tamaño del código (excluyendo líneas en blanco y comentarios) y proporción de documentación. |
| **Fórmula o forma de cálculo** | NLOC (Non-Comment Lines of Code) = líneas efectivas. % comentarios = (líneas de comentario / líneas totales) × 100. |
| **Umbral de referencia e interpretación** | Más de 50 líneas efectivas por método es señal de alerta. Un % de comentarios muy bajo (<10%) o muy alto (>50%) puede indicar problemas de documentación. |
| **Herramienta o script de medición** | Lizard v1.25.0 (Python). Comando: `lizard src/api/src/main/java/com/gms -l java --csv > metricas/resultados/lizard-vg-loc.csv`. |
| **Alcance de la medición** | Todos los métodos de `src/api/src/main/java/com/gms/`. |
| **Justificación** | Métrica estructural obligatoria. Complementa a V(G): un método puede tener baja complejidad pero muchas líneas (señal de alerta). |
| **Acción asociada si se supera el umbral** | Aplicar **Extract Method** para reducir el tamaño del método. Si el % de comentarios es bajo, agregar documentación (Javadoc). |

---

## Ficha 3: Suite CK (WMC, DIT, NOC, CBO, RFC, LCOM)

| Campo | Contenido |
|---|---|
| **Nombre de la métrica** | Suite CK (Chidamber & Kemerer) |
| **Atributo que mide** | Calidad del diseño orientado a objetos: complejidad ponderada (WMC), profundidad de herencia (DIT), número de hijos (NOC), acoplamiento (CBO), respuesta (RFC), falta de cohesión (LCOM). |
| **Fórmula o forma de cálculo** | WMC = suma de complejidades de métodos de la clase. DIT = profundidad en el árbol de herencia. NOC = número de subclases directas. CBO = número de clases acopladas. RFC = número de métodos que pueden ejecutarse en respuesta a un mensaje. LCOM = falta de cohesión entre métodos (método de Chidamber & Kemerer). |
| **Umbral de referencia e interpretación** | WMC > 20 (clase con demasiadas responsabilidades); DIT > 5 (herencia profunda); CBO > 10 (alto acoplamiento); RFC > 50 (respuesta excesiva); LCOM > 0.7 (baja cohesión). |
| **Herramienta o script de medición** | CK (Mauricio Aniche) v0.7.0. Comando: `java -jar ck-0.7.0-jar-with-dependencies.jar src/api/src/main/java/com/gms true 0 false metricas/resultados`. |
| **Alcance de la medición** | Todas las clases de `src/api/src/main/java/com/gms/` (94 clases medidas). |
| **Justificación** | Métrica de diseño/evolución obligatoria según la regla de selección. Permite identificar clases con problemas de diseño (God Class, alto acoplamiento, baja cohesión) que afectan la mantenibilidad. |
| **Acción asociada si se supera el umbral** | WMC alto → **Extract Class**. CBO alto → **inyección de dependencias** o **Facade**. LCOM alto → **Extract Class** o **Move Method**. RFC alto → **Extract Method** / **Extract Class**. |

---

## Resumen de métricas seleccionadas

| Métrica | Tipo (según PDF) | Herramienta | Umbral principal |
|---|---|---|---|
| Complejidad Ciclomática V(G) | Estructural | Lizard | ≤ 10 |
| LOC (NLOC) | Estructural | Lizard | ≤ 50 por método |
| Suite CK | Diseño / Evolución | CK | WMC≤20, DIT≤5, CBO≤10, RFC≤50, LCOM≤0.7 |

**Cumplimiento de las reglas de selección del PDF:**
- ✅ Mínimo 3 métricas.
- ✅ Al menos una estructural (V(G) y LOC).
- ✅ Al menos una de diseño/evolución (Suite CK).
- ✅ Todas aplicables a Java (lenguaje orientado a objetos).
- ✅ Todas con herramienta de medición documentada y reproducible.