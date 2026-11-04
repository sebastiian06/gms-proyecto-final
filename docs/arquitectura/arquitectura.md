# Arquitectura del Sistema — GMS (Gym Management System)

## 1. Estilo arquitectónico seleccionado

**Monolito modular con arquitectura en capas.**

El sistema se organiza en **módulos de negocio independientes** (identity, members, plans, billing, attendance, training, reports), y cada módulo implementa internamente una **arquitectura en capas** (controller → service → repository → entity). La comunicación entre módulos se realiza exclusivamente a través de una **Facade** por módulo, sin acceso directo a las entidades o repositorios de otro módulo.

## 2. Justificación de la elección

### 2.1 Frente a los requisitos funcionales (RF)

| Requisito | Cómo lo soporta el monolito modular |
|---|---|
| **RF-01 (Autenticación)** | El módulo `identity` concentra usuarios, roles, JWT y códigos de acceso. Es el único punto de entrada para la seguridad. |
| **RF-02 (Gestión de usuarios)** | `identity` expone su `IdentityFacade` para que otros módulos validen usuarios sin acoplarse a su repositorio. |
| **RF-03, RF-04 (Clientes)** | `members` encapsula toda la lógica de clientes; otros módulos lo consultan vía `MembersFacade`. |
| **RF-05 (Planes)** | `plans` es un módulo independiente; `billing` lo consulta vía `PlansFacade` para validar planes activos. |
| **RF-06, RF-07 (Membresías y pagos)** | `billing` agrupa membresías y pagos, con reglas de negocio propias (una membresía activa por cliente, pago que no excede saldo). |
| **RF-08 (Asistencia)** | `attendance` consulta a `members` e `identity` vía sus Facades para validar el código de acceso y el estado del cliente. |
| **RF-09 (Entrenadores y rutinas)** | `training` agrupa entrenadores, rutinas y ejercicios; consulta a `members` vía `MembersFacade`. |
| **RF-10 (Reportes)** | `reports` es un módulo de solo lectura que consulta a otros módulos vía sus Facades, sin acoplarse a sus entidades. |

### 2.2 Frente a los requisitos no funcionales (RNF)

| RNF | Cómo lo soporta el monolito modular |
|---|---|
| **RNF-01 (Rendimiento ≤ 2s)** | Al ser un monolito, las llamadas entre módulos son en memoria (no hay latencia de red entre servicios). Esto favorece tiempos de respuesta bajos. |
| **RNF-02 (Seguridad con JWT + BCrypt)** | `platform` centraliza `SecurityConfig`, `JwtAuthFilter` y `PasswordEncoder`. Todos los módulos heredan la seguridad sin duplicar configuración. |
| **RNF-03 (Mantenibilidad: V(G) ≤ 10)** | La separación en capas y módulos concentra la lógica en servicios pequeños y cohesivos, lo que reduce la complejidad ciclomática por método y facilita el análisis estático con Lizard. |

### 2.3 Ventajas del monolito modular

- **Despliegue simple:** un solo artefacto (JAR) y un solo contenedor para la API, lo que reduce la complejidad operativa frente a microservicios.
- **Llamadas en memoria:** sin latencia de red entre módulos, lo que ayuda al RNF-01.
- **Transacciones ACID:** las operaciones que cruzan módulos (ej. registrar cliente + crear membresía inicial + registrar pago) se resuelven en una sola transacción de base de datos.
- **Bajo acoplamiento:** cada módulo expone solo su Facade; los demás no conocen sus entidades ni repositorios.
- **Alta cohesión:** cada módulo agrupa todo lo relacionado con su dominio (controller, dto, entity, repository, service).
- **Facilidad de medición:** las métricas de la Fase 4 (V(G), LOC, CK) se pueden calcular por módulo, lo que permite identificar qué módulo necesita refactorización.

### 2.4 Compromisos aceptados

- **Escalabilidad vertical (no horizontal):** a diferencia de los microservicios, no podemos escalar un módulo de forma independiente. Se acepta porque el volumen esperado del gimnasio no lo requiere.
- **Un solo lenguaje y framework:** todo el backend es Java + Spring Boot. No hay poliglotía. Se acepta por la curva de aprendizaje del equipo y por la disponibilidad de herramientas de medición para Java.
- **Riesgo de "monolito espagueti" si no se respetan las fronteras:** se mitiga con la regla de que **ningún módulo accede directamente a entidades o repositorios de otro módulo**; solo a través de su Facade. Esto se documenta y se verifica en revisiones de código.
- **Un solo pipeline de CI/CD:** un cambio en cualquier módulo requiere reconstruir y redesplegar toda la API. Se acepta porque el proyecto es académico y el equipo es pequeño.

## 3. Cómo la arquitectura favorece la mantenibilidad

### 3.1 Separación de responsabilidades

Cada capa tiene una responsabilidad única:

| Capa | Responsabilidad |
|---|---|
| **Controller** | Recibir peticiones HTTP, validar entrada (`@Valid`), delegar al Facade y devolver respuestas. |
| **DTO** | Transportar datos entre capas y hacia el exterior, sin exponer entidades JPA. |
| **Entity** | Modelar el dominio persistente (tablas de base de datos). |
| **Repository** | Acceso a datos (Spring Data JPA). |
| **Service** | Lógica de negocio del módulo y reglas de dominio. |
| **Facade** | Punto de entrada del módulo para otros módulos. |

Esto facilita el análisis de métricas: la complejidad ciclomática (V(G)) se concentra en los servicios, mientras que los controllers y DTOs son simples y tienen baja V(G).

### 3.2 Bajo acoplamiento

- Los módulos se comunican solo por Facades.
- No hay claves foráneas entre tablas de distintos módulos (ej. `members_members.user_id` no tiene FK a `identity_users`; se valida en tiempo de escritura vía `IdentityFacade`).
- Esto reduce el **CBO (Coupling Between Objects)** de la suite CK, que se medirá en la Fase 4.

### 3.3 Alta cohesión

- Cada módulo agrupa todo lo relacionado con su dominio.
- Cada clase tiene una única responsabilidad (Single Responsibility Principle).
- Esto reduce el **LCOM (Lack of Cohesion of Methods)** de la suite CK.

### 3.4 Facilidad de refactorización

- Si un método de un servicio supera la V(G) ≤ 10 (RNF-03), se puede aplicar **Extract Method** sin afectar a otros módulos.
- Si una clase tiene demasiadas responsabilidades (WMC alto), se puede aplicar **Extract Class** dentro del mismo módulo.
- Si hay alto acoplamiento (CBO alto), se puede introducir una **Facade** adicional o aplicar **inyección de dependencias**.

## 4. Estructura de la arquitectura

Ver los siguientes documentos:

- [`diagrama-componentes.md`](./diagrama-componentes.md): diagrama de componentes (módulos y sus dependencias).
- [`diagrama-capas.md`](./diagrama-capas.md): diagrama de capas dentro de un módulo.
- [`estructura-carpetas.md`](./estructura-carpetas.md): estructura de carpetas/paquetes que refleja la arquitectura.

## 5. Conclusión

El **monolito modular con arquitectura en capas** es coherente con los 10 RF (cada módulo cubre un conjunto de requisitos), cumple los 3 RNF (rendimiento por llamadas en memoria, seguridad centralizada en `platform`, mantenibilidad por separación de responsabilidades) y facilita la medición de métricas de la Fase 4 (módulos cohesivos y bajo acoplamiento). Los compromisos aceptados (escalabilidad vertical, un solo lenguaje, despliegue monolítico) son razonables para el alcance del proyecto.