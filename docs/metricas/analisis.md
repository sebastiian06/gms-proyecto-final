# Análisis de Resultados de Métricas — GMS

Este documento interpreta los resultados obtenidos en la Fase 4, comparándolos con los umbrales de referencia del PDF y priorizando los problemas de mantenibilidad.

## 1. Resumen de resultados

| Métrica | Herramienta | Resultado principal |
|---|---|---|
| Complejidad Ciclomática V(G) | Lizard | 200/201 métodos ≤ 10. Solo `MemberService.updateFullProfile()` (V(G)=15) supera el umbral. |
| LOC (NLOC) | Lizard | 200/201 métodos ≤ 50. Solo `SecurityConfig.securityFilterChain()` (NLOC=67) supera el umbral. |
| Suite CK (WMC) | CK | 3 clases superan WMC>20: `MemberService` (39), `UserService` (23), `GlobalExceptionHandler` (21). |
| Suite CK (CBO) | CK | 22 clases superan CBO>10. Las más críticas: `GlobalExceptionHandler` (33), `MemberService` (24), `UserController` (22), `MemberController` (22). |
| Suite CK (RFC) | CK | Solo `MemberService` (54) supera RFC>50. |
| Suite CK (LCOM) | CK | 12 entidades JPA superan LCOM*>0.7, pero se consideran falsos positivos (getters/setters no comparten estado). |
| Suite CK (DIT) | CK | 6 clases superan DIT>5, pero es ruido de la herencia de Spring. El DIT real del dominio es 1. |

## 2. Distribución de V(G) por rango

| Rango V(G) | Métodos | Porcentaje |
|---|---|---|
| 1–10 (riesgo bajo) | 200 | 99.5% |
| 11–20 (riesgo moderado) | 1 | 0.5% |
| 21–50 (riesgo alto) | 0 | 0% |
| >50 (riesgo muy alto) | 0 | 0% |

**Interpretación:** el código tiene una complejidad ciclomática muy baja en general. Solo un método (`updateFullProfile`) está en riesgo moderado. Esto indica que la arquitectura en capas está funcionando bien: la lógica está distribuida en métodos pequeños y cohesivos.

## 3. Problemas de mantenibilidad identificados

### 3.1 God Class: `MemberService`

| Métrica | Valor | Umbral | Diagnóstico |
|---|---|---|---|
| WMC | 39 | ≤ 20 | Supera casi el doble del umbral. |
| RFC | 54 | ≤ 50 | Supera el umbral. |
| CBO | 24 | ≤ 10 | Supera el umbral. |
| V(G) de `updateFullProfile()` | 15 | ≤ 10 | Único método que supera V(G). |

**Causa:** `MemberService` concentra demasiadas responsabilidades:
- Registro de clientes (crear usuario, miembro, membresía, pago inicial).
- Actualización de perfil (`updateFullProfile`, `updateFullProfile` con V(G)=15).
- Cambio de plan (`changePlan`).
- Asignación de entrenador (`assignTrainer`).
- Activación/desactivación (`activate`, `deactivate`).
- Regeneración de código de acceso (`regenerateAccessCode`).
- Búsquedas (`findById`, `findByUsername`, `findAll`).

**Impacto:** es la clase más compleja del sistema y la que más cuesta mantener. Cualquier cambio en la lógica de clientes requiere tocar esta clase, lo que aumenta el riesgo de regresiones.

### 3.2 `GlobalExceptionHandler` con alto acoplamiento

| Métrica | Valor | Umbral | Diagnóstico |
|---|---|---|---|
| CBO | 33 | ≤ 10 | Supera más del triple del umbral. |
| WMC | 21 | ≤ 20 | Supera el umbral. |

**Causa:** maneja excepciones de todos los módulos en una sola clase. Cada nueva excepción de dominio agrega un método `@ExceptionHandler` y acopla la clase a un nuevo módulo.

**Impacto:** alto acoplamiento y baja extensibilidad. Agregar un módulo nuevo requiere modificar esta clase.

### 3.3 `UserService` con WMC alto

| Métrica | Valor | Umbral | Diagnóstico |
|---|---|---|---|
| WMC | 23 | ≤ 20 | Supera el umbral. |
| CBO | 13 | ≤ 10 | Supera el umbral. |

**Causa:** concentra la lógica de usuarios (crear, listar, actualizar, activar, desactivar, validar último admin, proteger admin del sistema).

**Impacto:** moderado. Es menos crítico que `MemberService` porque tiene menos responsabilidades.

### 3.4 `SecurityConfig.securityFilterChain()` con NLOC alto

| Métrica | Valor | Umbral | Diagnóstico |
|---|---|---|---|
| NLOC | 67 | ≤ 50 | Supera el umbral. |
| V(G) | 1 | ≤ 10 | Cumple (sin ramificaciones). |

**Causa:** configuración de Spring Security con muchas reglas encadenadas.

**Impacto:** bajo. No es un problema de complejidad, sino de legibilidad. Se puede dividir en métodos auxiliares.

### 3.5 Falsos positivos (no requieren acción)

- **LCOM*>0.7 en entidades JPA:** los getters/setters no comparten estado, lo que infla el LCOM. No es un problema de diseño.
- **DIT>5 en servicios:** la herencia de Spring infla el DIT. El DIT real del dominio es 1.

## 4. Priorización de problemas

| Prioridad | Problema | Riesgo | Esfuerzo | Justificación |
|---|---|---|---|---|
| **Alta** | `MemberService` (God Class) | Alto | Medio | Es la clase más compleja y la que más afecta la mantenibilidad. Tiene el único método con V(G)>10. |
| **Media** | `GlobalExceptionHandler` (CBO=33) | Medio | Bajo | Alto acoplamiento, pero se puede resolver con Extract Class por módulo. |
| **Media** | `UserService` (WMC=23) | Medio | Bajo | Supera el umbral, pero no es crítico. |
| **Baja** | `SecurityConfig.securityFilterChain()` (NLOC=67) | Bajo | Bajo | Solo afecta legibilidad, no complejidad. |

## 5. Conclusión del análisis

El código tiene **buena calidad general**: el 99.5% de los métodos cumplen V(G) ≤ 10 y NLOC ≤ 50. El problema principal es **`MemberService`**, que concentra demasiadas responsabilidades (God Class) y contiene el único método con V(G) > 10. Las propuestas de refactorización se detallan en `mejoras.md`.