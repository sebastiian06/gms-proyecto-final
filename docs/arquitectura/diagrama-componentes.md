# Diagrama de Componentes — GMS

El siguiente diagrama muestra los módulos del backend y sus dependencias a través de Facades. Las flechas indican "consulta a través de".

```mermaid
graph TD
    subgraph API["API REST (Spring Boot)"]
        PLATFORM[platform<br/>Seguridad, CORS, OpenAPI,<br/>manejo de errores]
        IDENTITY[identity<br/>Usuarios, roles,<br/>JWT, códigos de acceso]
        MEMBERS[members<br/>Clientes]
        PLANS[plans<br/>Planes de membresía]
        BILLING[billing<br/>Membresías y pagos]
        ATTENDANCE[attendance<br/>Registro de asistencia]
        TRAINING[training<br/>Entrenadores, rutinas,<br/>ejercicios]
        REPORTS[reports<br/>Resumen y pagos recientes]
    end

    subgraph DB["Base de Datos"]
        PG[(PostgreSQL)]
    end

    subgraph FRONT["Portal (React)"]
        PORTAL[Portal web<br/>Admin, Recepcionista,<br/>Entrenador, Cliente]
    end

    PORTAL -->|HTTP /api/v1| IDENTITY
    PORTAL -->|HTTP /api/v1| MEMBERS
    PORTAL -->|HTTP /api/v1| PLANS
    PORTAL -->|HTTP /api/v1| BILLING
    PORTAL -->|HTTP /api/v1| ATTENDANCE
    PORTAL -->|HTTP /api/v1| TRAINING
    PORTAL -->|HTTP /api/v1| REPORTS

    MEMBERS -->|IdentityFacade| IDENTITY
    PLANS -->|IdentityFacade| IDENTITY
    BILLING -->|MembersFacade| MEMBERS
    BILLING -->|PlansFacade| PLANS
    ATTENDANCE -->|MembersFacade| MEMBERS
    ATTENDANCE -->|IdentityFacade| IDENTITY
    TRAINING -->|MembersFacade| MEMBERS
    TRAINING -->|IdentityFacade| IDENTITY
    REPORTS -->|MembersFacade / BillingFacade| MEMBERS
    REPORTS -->|BillingFacade| BILLING

    IDENTITY --> PG
    MEMBERS --> PG
    PLANS --> PG
    BILLING --> PG
    ATTENDANCE --> PG
    TRAINING --> PG

    PLATFORM -.->|configura| IDENTITY
    PLATFORM -.->|configura| MEMBERS
    PLATFORM -.->|configura| PLANS
    PLATFORM -.->|configura| BILLING
    PLATFORM -.->|configura| ATTENDANCE
    PLATFORM -.->|configura| TRAINING
    PLATFORM -.->|configura| REPORTS
```

## Reglas de dependencia
1. Ningún módulo accede directamente a entidades o repositorios de otro módulo. Solo a través de su Facade.

2. platform es transversal: configura seguridad, CORS, OpenAPI y manejo global de errores para todos los módulos.

3. reports es de solo lectura: consulta a otros módulos vía sus Facades, nunca escribe en sus tablas.

4. No hay claves foráneas entre tablas de distintos módulos. La integridad se valida en tiempo de escritura vía Facades.