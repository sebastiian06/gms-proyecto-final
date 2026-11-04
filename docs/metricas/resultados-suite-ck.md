# Resultados de la Suite CK — GMS

**Herramienta:** CK (Mauricio Aniche) v0.7.0
**Comando:** `java -jar ck-0.7.0-jar-with-dependencies.jar src/api/src/main/java/com/gms true 0 false metricas/resultados`
**Alcance:** 94 clases de `src/api/src/main/java/com/gms/` (todos los módulos).
**Umbrales de referencia (PDF):** WMC > 20, DIT > 5, CBO > 10, RFC > 50, LCOM > 0.7.

---

## 1. Resumen general

| Métrica | Clases que superan el umbral | Observación |
|---|---|---|
| **WMC > 20** | 3 | `MemberService` (39), `UserService` (23), `GlobalExceptionHandler` (21) |
| **CBO > 10** | 22 | Esperable en controllers y facades (inyección de dependencias). Las más críticas: `GlobalExceptionHandler` (33), `MemberService` (24), `UserController` (22), `MemberController` (22). |
| **RFC > 50** | 1 | Solo `MemberService` (54). Confirma que es una God Class. |
| **LCOM > 0.7** | 12 | Todas son entidades JPA. Se consideran **falsos positivos** (ver sección 5). |
| **DIT > 5** | 6 | Valores inflados por la herencia de Spring (`Object → ... → Service`). El DIT real del dominio es 1. **No se reporta como problema.** |

---

## 2. Clases con WMC > 20 (complejidad ponderada alta)

| Clase | WMC | CBO | RFC | LCOM* | Estado / Acción propuesta |
|---|---|---|---|---|---|
| `com.gms.members.service.MemberService` | 39 | 24 | 54 | 0.0 | ⚠️ **God Class**. Aplicar **Extract Class** para separar responsabilidades (registro, actualización, activación, cambio de plan). |
| `com.gms.identity.service.UserService` | 23 | 13 | 26 | 0.0 | ⚠️ Supera el umbral. Revisar si la lógica de activación/desactivación puede extraerse a un `UserActivationService`. |
| `com.gms.platform.errors.GlobalExceptionHandler` | 21 | 33 | 6 | 0.0 | ⚠️ Muchos `@ExceptionHandler`. Aplicar **Extract Class** por módulo (IdentityExceptionHandler, BillingExceptionHandler, etc.). |

---

## 3. Clases con CBO > 10 (alto acoplamiento)

| Clase | CBO | WMC | Estado / Acción propuesta |
|---|---|---|---|
| `com.gms.platform.errors.GlobalExceptionHandler` | 33 | 21 | ⚠️ Acopla con excepciones de todos los módulos. **Extract Class** por módulo. |
| `com.gms.members.service.MemberService` | 24 | 39 | ⚠️ Acopla con `MembersFacade`, `PlansFacade`, `BillingFacade`, `TrainingFacade`. Revisar si puede delegar más a las Facades. |
| `com.gms.identity.controller.UserController` | 22 | 11 | ⚠️ Inyecta `IdentityFacade`, `UserService`, `AccessCodeService`, `TrainingFacade`. Es esperable en un controller, pero se puede simplificar moviendo lógica al Facade. |
| `com.gms.members.controller.MemberController` | 22 | 11 | ⚠️ Similar al anterior. |
| `com.gms.billing.controller.MembershipController` | 19 | 8 | ⚠️ Inyecta `BillingFacade` y `PaymentService`. Se puede unificar en el Facade. |
| `com.gms.plans.controller.PlanController` | 19 | 7 | ⚠️ Inyecta `PlansFacade` y repositorios. Se puede simplificar. |
| `com.gms.training.controller.RoutineController` | 17 | 8 | ⚠️ Similar. |
| `com.gms.billing.controller.PaymentController` | 17 | 6 | ⚠️ Similar. |
| `com.gms.billing.service.MembershipService` | 16 | 11 | ⚠️ Acopla con `MemberRepository`, `PlanRepository`, `MembershipRepository`, `PaymentRepository`. Revisar si puede delegar a Facades. |
| `com.gms.training.controller.ExerciseController` | 15 | 5 | ⚠️ |
| `com.gms.members.service.MembersFacade` | 13 | 16 | ⚠️ |
| `com.gms.training.service.TrainingFacade` | 13 | 17 | ⚠️ |
| `com.gms.identity.service.UserService` | 13 | 23 | ⚠️ |
| `com.gms.plans.service.PlanService` | 9 | 13 | ⚠️ |
| `com.gms.attendance.service.AttendanceService` | 12 | 9 | ⚠️ |
| `com.gms.training.service.RoutineService` | 13 | 8 | ⚠️ |
| `com.gms.billing.service.PaymentService` | 12 | 15 | ⚠️ |
| `com.gms.identity.service.IdentityFacade` | 11 | 9 | ⚠️ |
| `com.gms.platform.config.SecurityConfig` | 13 | 4 | ⚠️ Configuración de seguridad; acoplamiento esperado. |
| `com.gms.identity.service.JwtAuthFilter` | 12 | 6 | ⚠️ Filtro de seguridad; acoplamiento esperado. |
| `com.gms.reports.service.ReportService` | 13 | 6 | ⚠️ Acopla con varios repositorios y facades. |
| `com.gms.billing.entity.Payment` | 12 | 13 | Entidad; acoplamiento con `Membership`. |

**Nota:** Muchos de estos CBO son **esperables en un monolito modular** (controllers que inyectan facades, servicios que inyectan repositorios). Se documentan como "acoplamiento aceptado" y solo se propone refactorización en los casos críticos (`GlobalExceptionHandler`, `MemberService`).

---

## 4. Clases con RFC > 50 (respuesta excesiva)

| Clase | RFC | WMC | Estado / Acción propuesta |
|---|---|---|---|
| `com.gms.members.service.MemberService` | 54 | 39 | ⚠️ Confirma God Class. **Extract Class** y **Extract Method** para reducir los métodos que puede invocar. |

---

## 5. Clases con LCOM* > 0.7 (baja cohesión)

| Clase | LCOM* | WMC | Estado / Acción propuesta |
|---|---|---|---|
| `com.gms.members.entity.Member` | 0.85 | 17 | Entidad JPA. **Falso positivo**: los getters/setters no comparten estado. Se excluye de la refactorización. |
| `com.gms.identity.entity.User` | 0.85 | 18 | Entidad JPA. Falso positivo. |
| `com.gms.billing.entity.Membership` | 0.85 | 15 | Entidad JPA. Falso positivo. |
| `com.gms.training.entity.Exercise` | 0.84 | 16 | Entidad JPA. Falso positivo. |
| `com.gms.plans.entity.Plan` | 0.83 | 16 | Entidad JPA. Falso positivo. |
| `com.gms.billing.entity.Payment` | 0.82 | 13 | Entidad JPA. Falso positivo. |
| `com.gms.training.entity.Routine` | 0.81 | 12 | Entidad JPA. Falso positivo. |
| `com.gms.identity.entity.AccessCode` | 0.80 | 11 | Entidad JPA. Falso positivo. |
| `com.gms.training.entity.Trainer` | 0.78 | 10 | Entidad JPA. Falso positivo. |
| `com.gms.training.entity.TrainerMember` | 0.78 | 9 | Entidad JPA. Falso positivo. |
| `com.gms.training.entity.RoutineAssignment` | 0.75 | 7 | Entidad JPA. Falso positivo. |
| `com.gms.attendance.entity.AttendanceRecord` | 0.75 | 8 | Entidad JPA. Falso positivo. |

**Justificación:** Las entidades JPA tienen muchos métodos de acceso (getters/setters) que no comparten estado, lo que infla el LCOM. En el análisis de la Fase 5 se documenta como **falso positivo** y se propone **excluir entidades de la medición de LCOM** o usar una variante de LCOM que no penalice los getters/setters (como LCOM* o TCC). Para el resto de clases (servicios, controllers, facades), el LCOM* es ≤ 0.7, lo que indica **buena cohesión**.

---

## 6. DIT > 5 (herencia profunda)

| Clase | DIT | Estado / Acción propuesta |
|---|---|---|
| `com.gms.members.service.MemberService` | 13 | El DIT incluye la cadena de Spring (`Object → ... → Service`). El DIT real del dominio es 1. **No se reporta como problema.** |
| `com.gms.identity.service.UserService` | 19 | Ídem. |
| `com.gms.billing.service.MembershipService` | 16 | Ídem. |
| `com.gms.plans.service.PlanService` | 13 | Ídem. |
| `com.gms.attendance.service.AttendanceService` | 10 | Ídem. |
| `com.gms.training.service.RoutineService` | 7 | Ídem. |

**Justificación:** CK mide la profundidad de herencia incluyendo las clases de la librería estándar (Spring). Como las clases de servicio extienden `Object` (o implementan interfaces), el DIT real del diseño es 1. Se documenta como **ruido de la herramienta** y no se propone refactorización.

---

## 7. Conclusión

- **Problema real:** `MemberService` es una **God Class** (WMC=39, RFC=54, CBO=24). Requiere refactorización urgente en la Fase 5.
- **Problemas moderados:** `UserService` (WMC=23) y `GlobalExceptionHandler` (WMC=21, CBO=33) requieren revisión.
- **Falsos positivos:** las entidades JPA con LCOM* > 0.7 y las clases de servicio con DIT > 5 (por Spring).
- **Acoplamiento aceptado:** los controllers y facades con CBO > 10 son esperables en un monolito modular; se documentan pero no se refactorizan todos.