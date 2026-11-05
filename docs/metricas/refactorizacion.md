# Refactorización Implementada — `MemberService.updateFullProfile()`

Este documento registra la refactorización implementada siguiendo el ciclo **Medir → Detectar → Refactorizar → Verificar**.

## 1. Medir (antes)

| Métrica | Valor antes |
|---|---|
| V(G) de `updateFullProfile()` | 15 |
| NLOC de `updateFullProfile()` | 47 |
| WMC de `MemberService` | 39 |
| RFC de `MemberService` | 54 |
| CBO de `MemberService` | 24 |

## 2. Detectar

`updateFullProfile()` supera el umbral de V(G) ≤ 10 (RNF-03) y contribuye al WMC alto de `MemberService`. La lógica del método mezcla:
- Validación de datos.
- Actualización de nombre y contacto.
- Cambio de plan.
- Asignación de entrenador.

## 3. Refactorizar

**Técnica:** Extract Method.

Se dividió `updateFullProfile()` en:
- `validateUpdateRequest()`: validaciones previas.
- `updateBasicData()`: actualización de nombre y contacto.
- `updatePlanIfChanged()`: cambio de plan si se solicitó.
- `updateTrainerIfChanged()`: asignación de entrenador si se solicitó.

## 4. Verificar (después)

| Métrica | Antes | Después | Mejora |
|---|---|---|---|
| V(G) de `updateFullProfile()` | 15 | (pendiente) | (pendiente) |
| NLOC de `updateFullProfile()` | 47 | (pendiente) | (pendiente) |
| WMC de `MemberService` | 39 | (pendiente) | (pendiente) |

*(Se llenará después de implementar y volver a medir)*

## 5. Pruebas

Las 114 pruebas del backend deben seguir pasando después de la refactorización. Comando:

```text
cd src/api
.\mvnw.cmd test
```


## 6. Conclusión

*(Se llenará después de la re-medición)*