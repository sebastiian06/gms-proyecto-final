# Resultados de Lizard — V(G) y LOC

**Herramienta:** Lizard v1.25.0
**Comando:** `lizard src/api/src/main/java/com/gms -l java -C 10 --csv > metricas/resultados/lizard-vg-loc.csv`
**Alcance:** 201 métodos de `src/api/src/main/java/com/gms/`.
**Umbrales de referencia (PDF):**
- **V(G):** 1–10 riesgo bajo; 11–20 moderado; 21–50 alto; >50 muy alto.
- **NLOC:** más de 50 líneas efectivas por método es señal de alerta.

---

## 1. Resumen general

| Indicador | Valor |
|---|---|
| Métodos medidos | 201 |
| Métodos con V(G) > 10 | **1** (`MemberService.updateFullProfile`, V(G)=15) |
| Métodos con NLOC > 50 | **1** (`SecurityConfig.securityFilterChain`, NLOC=67) |
| V(G) máximo | 15 |
| NLOC máximo | 67 |

---

## 2. Métodos que superan V(G) > 10

| Método | V(G) | NLOC | Umbral | Estado / Acción propuesta |
|---|---|---|---|---|
| `com.gms.members.service.MemberService.updateFullProfile()` | 15 | 47 | ≤ 10 | ⚠️ **Riesgo moderado** (11–20). Aplicar **Extract Method** para dividir la lógica de actualización en pasos más pequeños (validación, actualización de datos, actualización de plan, actualización de entrenador). |

**Nota:** el CSV de Lizard reporta este método bajo la clase interna `AlreadyInactiveException::updateFullProfile` porque la excepción está anidada en `MemberService`. El método real es `MemberService.updateFullProfile()`.

**Resto de métodos:** 200 de 201 métodos tienen V(G) ≤ 10, lo que cumple el RNF-03 (V(G) ≤ 10) en el 99.5% del código.

---

## 3. Métodos que superan NLOC > 50

| Método | NLOC | V(G) | Umbral | Estado / Acción propuesta |
|---|---|---|---|---|
| `com.gms.platform.config.SecurityConfig.securityFilterChain()` | 67 | 1 | ≤ 50 | ⚠️ Señal de alerta por tamaño, pero **V(G)=1** (sin ramificaciones). Es un método de configuración de Spring Security con muchas llamadas encadenadas. Se puede dividir en métodos auxiliares (`configurePublicEndpoints()`, `configureRoleEndpoints()`, etc.) para mejorar la legibilidad, pero **no es un problema de complejidad**. |

**Resto de métodos:** 200 de 201 métodos tienen NLOC ≤ 50, lo que cumple el umbral del PDF en el 99.5% del código.

---

## 4. Distribución de V(G) por rango

| Rango | Métodos | Porcentaje |
|---|---|---|
| 1–10 (riesgo bajo) | 200 | 99.5% |
| 11–20 (riesgo moderado) | 1 | 0.5% |
| 21–50 (riesgo alto) | 0 | 0% |
| > 50 (riesgo muy alto) | 0 | 0% |

---

## 5. Conclusión

- **RNF-03 (V(G) ≤ 10):** se cumple en el **99.5%** de los métodos. Solo `MemberService.updateFullProfile()` (V(G)=15) supera el umbral y requiere refactorización en la Fase 5 (Extract Method).
- **NLOC ≤ 50:** se cumple en el **99.5%** de los métodos. Solo `SecurityConfig.securityFilterChain()` (NLOC=67) supera el umbral, pero su V(G)=1 indica que no es un problema de complejidad, sino de tamaño por configuración. Se puede dividir por legibilidad.
- **Coherencia con CK:** el método problemático (`updateFullProfile`) pertenece a la clase que CK identificó como God Class (`MemberService`, WMC=39, RFC=54). Ambas métricas apuntan al mismo problema de diseño, lo que refuerza la propuesta de refactorización.

---

## 6. Archivos generados

- `metricas/resultados/lizard-vg-loc.csv`: resultados completos de Lizard (201 métodos).
- `metricas/resultados/resultadosclass.csv`: resultados de CK a nivel de clase.
- `metricas/resultados/resultadosmethod.csv`: resultados de CK a nivel de método.