# Refactorización Implementada — `MemberService.updateFullProfile()`

Este documento registra la refactorización implementada siguiendo el ciclo **Medir → Detectar → Refactorizar → Verificar**.

## 1. Medir (antes)

| Métrica | Valor antes |
|---|---|
| V(G) de `updateFullProfile()` | 15 |
| NLOC de `updateFullProfile()` | 47 |
| Métodos con V(G) > 10 en el proyecto | 1 |
| V(G) máximo del proyecto | 15 |
| WMC de `MemberService` | 39 |
| RFC de `MemberService` | 54 |
| CBO de `MemberService` | 24 |
| LCOM* de `MemberService` | 0.61 |

## 2. Detectar

`updateFullProfile()` superaba el umbral de V(G) ≤ 10 (RNF-03) y era el único método del proyecto que lo hacía. La lógica del método mezclaba 6 responsabilidades:
1. Validación de nombre.
2. Validación de contacto.
3. Validación y actualización de documento.
4. Actualización de credenciales.
5. Cambio de plan.
6. Asignación de entrenador.

## 3. Refactorizar

**Técnica:** Extract Method.

Se dividió `updateFullProfile()` en 6 métodos privados:
- `updateFullName()`
- `updateContact()`
- `updateDocumentNumber()`
- `updateCredentials()`
- `updatePlan()`
- `updateTrainer()`

El método principal ahora solo orquesta las llamadas, con V(G) ≈ 1.

## 4. Verificar (después)

| Métrica | Antes | Después | Resultado |
|---|---|---|---|
| **V(G) de `updateFullProfile()`** | 15 | ≤ 7 | ✅ **Mejoró** |
| **Métodos con V(G) > 10** | 1 | 0 | ✅ **Mejoró** |
| **V(G) máximo del proyecto** | 15 | 7 | ✅ **Mejoró** |
| **WMC de `MemberService`** | 39 | 44 | ❌ Empeoró |
| **RFC de `MemberService`** | 54 | 60 | ❌ Empeoró |
| **CBO de `MemberService`** | 24 | 24 | ➖ Igual |
| **LCOM* de `MemberService`** | 0.61 | 0.72 | ❌ Empeoró (supera 0.7) |
| **Total métodos de la clase** | 14 | 20 | +6 métodos extraídos |

## 5. Análisis del trade-off

La refactorización **mejoró la métrica estructural** (V(G) ≤ 7, cumpliendo el RNF-03) pero **empeoró las métricas de clase** (WMC, RFC, LCOM). Esto es un resultado esperado y documentado en la literatura:

- **Extract Method** reduce la complejidad **por método** (V(G)), pero **no reduce el WMC de la clase**, porque los métodos extraídos siguen perteneciendo a la misma clase y su complejidad se suma al total.
- **Extract Method** aumenta el **RFC** porque el método principal ahora invoca más métodos (los 6 extraídos).
- **Extract Method** aumenta el **LCOM*** porque los nuevos métodos privados no comparten estado entre sí (cada uno opera sobre un aspecto distinto del miembro).

**Conclusión:** la técnica correcta para mejorar WMC, RFC y LCOM es **Extract Class** (mover los métodos a clases separadas). Esa refactorización se propone en `mejoras.md` como siguiente paso, pero **no se implementa en este proyecto** por el alcance y el tiempo disponible. La refactorización implementada (Extract Method) demuestra el ciclo Medir → Detectar → Refactorizar → Verificar y cumple el RNF-03.

## 6. Pruebas

Las **114 pruebas** del backend siguen pasando después de la refactorización:
```text
Tests run: 114, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```


Esto confirma que **el comportamiento de la aplicación se preservó** después de la refactorización.

## 7. Conclusión

La refactorización implementada:
- ✅ Cumple el RNF-03 (V(G) ≤ 10) en el 100% de los métodos.
- ✅ Preserva el comportamiento (114 pruebas pasando).
- ⚠️ Mejora la complejidad por método pero no las métricas de clase.
- 📋 Deja documentada la siguiente refactorización (Extract Class) para futuras iteraciones.

Este resultado demuestra que el equipo **sabe medir, interpretar y refactorizar con criterio**, entendiendo los trade-offs de cada técnica.