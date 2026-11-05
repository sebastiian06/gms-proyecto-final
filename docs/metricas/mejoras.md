# Propuesta de Mejoras — GMS

Este documento propone las refactorizaciones para resolver los problemas identificados en `analisis.md`, con la técnica asociada y el efecto esperado sobre las métricas.

## 1. Refactorización de `MemberService` (prioridad alta)

### Problema
`MemberService` es una God Class (WMC=39, RFC=54, CBO=24) con un método que supera V(G) > 10 (`updateFullProfile`, V(G)=15).

### Técnica: Extract Class + Extract Method

**Extract Class:** dividir `MemberService` en servicios especializados por responsabilidad:
- `MemberRegistrationService`: registro de clientes (crear usuario, miembro, membresía, pago inicial).
- `MemberProfileService`: actualización de perfil (`updateFullProfile`, `updateFullProfile` con V(G)=15).
- `MemberPlanService`: cambio de plan (`changePlan`).
- `MemberTrainerService`: asignación de entrenador (`assignTrainer`).
- `MemberActivationService`: activación/desactivación (`activate`, `deactivate`).
- `MemberQueryService`: búsquedas (`findById`, `findByUsername`, `findAll`).

**Extract Method:** dentro de `MemberProfileService.updateFullProfile()`, dividir la lógica en:
- `validateUpdateRequest()`: validaciones previas.
- `updateBasicData()`: actualización de nombre y contacto.
- `updatePlanIfChanged()`: cambio de plan si se solicitó.
- `updateTrainerIfChanged()`: asignación de entrenador si se solicitó.

### Efecto esperado sobre las métricas

| Métrica | Antes | Después (estimado) |
|---|---|---|
| WMC de `MemberService` | 39 | ~10 por servicio (6 servicios) |
| RFC de `MemberService` | 54 | ~15 por servicio |
| CBO de `MemberService` | 24 | ~5 por servicio |
| V(G) de `updateFullProfile()` | 15 | ~4 por método extraído |

### Riesgo y esfuerzo
- **Riesgo:** medio (hay que mover lógica y actualizar las pruebas).
- **Esfuerzo:** medio (6 servicios nuevos, ~10 métodos extraídos).
- **Beneficio:** alto (resuelve el problema más crítico del sistema).

---

## 2. Refactorización de `GlobalExceptionHandler` (prioridad media)

### Problema
`GlobalExceptionHandler` tiene CBO=33 (supera el umbral de 10) y WMC=21 (supera el umbral de 20). Maneja excepciones de todos los módulos en una sola clase.

### Técnica: Extract Class por módulo

Dividir en manejadores especializados:
- `IdentityExceptionHandler`: excepciones de `identity`.
- `MembersExceptionHandler`: excepciones de `members`.
- `BillingExceptionHandler`: excepciones de `billing`.
- `TrainingExceptionHandler`: excepciones de `training`.
- `PlatformExceptionHandler`: excepciones transversales (validación, errores genéricos).

Cada manejador se anota con `@RestControllerAdvice(basePackages = "com.gms.<modulo>")` para que Spring lo aplique solo a su módulo.

### Efecto esperado sobre las métricas

| Métrica | Antes | Después (estimado) |
|---|---|---|
| CBO de `GlobalExceptionHandler` | 33 | ~7 por manejador |
| WMC de `GlobalExceptionHandler` | 21 | ~4 por manejador |

### Riesgo y esfuerzo
- **Riesgo:** bajo (la lógica de manejo no cambia, solo se distribuye).
- **Esfuerzo:** bajo (5 clases nuevas, mover métodos).
- **Beneficio:** medio (reduce acoplamiento y mejora extensibilidad).

---

## 3. Refactorización de `UserService` (prioridad media)

### Problema
`UserService` tiene WMC=23 (supera el umbral de 20) y CBO=13 (supera el umbral de 10).

### Técnica: Extract Class

Dividir en:
- `UserCrudService`: crear, listar, actualizar usuarios.
- `UserActivationService`: activar/desactivar, validar último admin, proteger admin del sistema.

### Efecto esperado sobre las métricas

| Métrica | Antes | Después (estimado) |
|---|---|---|
| WMC de `UserService` | 23 | ~12 por servicio |
| CBO de `UserService` | 13 | ~7 por servicio |

### Riesgo y esfuerzo
- **Riesgo:** bajo.
- **Esfuerzo:** bajo (2 servicios nuevos).
- **Beneficio:** medio.

---

## 4. Refactorización de `SecurityConfig.securityFilterChain()` (prioridad baja)

### Problema
`securityFilterChain()` tiene NLOC=67 (supera el umbral de 50), pero V(G)=1 (sin ramificaciones).

### Técnica: Extract Method

Dividir en métodos auxiliares:
- `configurePublicEndpoints()`: endpoints públicos (`/auth/login`, `/attendance/entries`, `/attendance/exits`).
- `configureRoleEndpoints()`: endpoints con roles específicos.
- `configureAuthenticatedEndpoints()`: endpoints que requieren autenticación.

### Efecto esperado sobre las métricas

| Métrica | Antes | Después (estimado) |
|---|---|---|
| NLOC de `securityFilterChain()` | 67 | ~25 por método |

### Riesgo y esfuerzo
- **Riesgo:** bajo (la configuración de seguridad no cambia, solo se reorganiza).
- **Esfuerzo:** bajo.
- **Beneficio:** bajo (mejora legibilidad).

---

## 5. Resumen de priorización

| Prioridad | Clase | Técnica | Riesgo | Esfuerzo | Beneficio |
|---|---|---|---|---|---|
| Alta | `MemberService` | Extract Class + Extract Method | Medio | Medio | Alto |
| Media | `GlobalExceptionHandler` | Extract Class | Bajo | Bajo | Medio |
| Media | `UserService` | Extract Class | Bajo | Bajo | Medio |
| Baja | `SecurityConfig` | Extract Method | Bajo | Bajo | Bajo |

## 6. Refactorización implementada

Para cumplir con la recomendación del PDF (implementar al menos una refactorización y volver a medir), se implementará la refactorización de **`MemberService.updateFullProfile()`** (prioridad alta) usando **Extract Method**. Los resultados antes/después se documentan en `refactorizacion.md`.