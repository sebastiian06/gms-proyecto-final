# api · Backend de GMS

> **GMS (Gym Management System)**: API REST con la lógica de negocio del sistema.

Parte del **Proyecto Final del curso Métricas y Estándares de Calidad**. Este backend es el **objeto principal de medición** de métricas de mantenibilidad y evolución (complejidad ciclomática, LOC y suite CK). La aplicación fue desarrollada originalmente en la asignatura Desarrollo Fullstack y se reutiliza aquí con autorización del docente.

## Tecnologías

- Java 21
- Spring Boot (Web, Data JPA, Security)
- Autenticación con JWT
- PostgreSQL (el esquema lo gestionan las migraciones de [`../db`](../db))
- Documentación de la API con OpenAPI/Swagger
- Maven (incluye wrapper: `mvnw` / `mvnw.cmd`)

## Arquitectura

**Monolito modular con arquitectura en capas.** Cada módulo de negocio es un paquete independiente dentro de `com.gms`, con las mismas capas internas. La comunicación entre módulos se hace mediante una **Facade** por módulo, no accediendo directamente a las entidades o repositorios de otro módulo.

```text
src/main/java/com/gms/
├── identity/     ← usuarios, roles, login, JWT, códigos de acceso
├── members/      ← socios y cambio de plan
├── plans/        ← planes de membresía
├── billing/      ← membresías y pagos
├── attendance/   ← registro de entrada y salida
├── training/     ← entrenadores, ejercicios y rutinas
├── reports/      ← resumen y pagos recientes
└── platform/     ← configuración transversal (seguridad, CORS, OpenAPI, manejo de errores)
```

Estructura interna de cada módulo:

```text
<módulo>/
├── controller/   ← endpoints REST
├── dto/          ← objetos de transferencia (requests y responses)
├── entity/       ← entidades JPA
├── repository/   ← acceso a datos
└── service/      ← lógica de negocio y Facade del módulo
```

> `reports` y `platform` no tienen todas las capas (`reports` no tiene `entity` ni `repository`; `platform` solo contiene `config` y `errors`).

Esta separación es relevante para las métricas: reduce el acoplamiento entre módulos (CBO) y concentra la lógica en la capa de servicios (WMC, V(G)).

## Estructura de la carpeta

```text
src/api/
├── pom.xml
├── mvnw / mvnw.cmd
├── postman/                ← colección y entorno de Postman para probar la API
└── src/
    ├── main/java/com/gms/  ← código fuente (ver arquitectura)
    ├── main/resources/     ← application.properties
    └── test/java/com/gms/  ← pruebas, organizadas por módulo
```

## Compilar

No requiere base de datos:

```powershell
cd src\api
.\mvnw.cmd clean compile -DskipTests
```

En Linux/macOS: `./mvnw clean compile -DskipTests`

## Ejecutar

La forma recomendada es con Docker Compose, que levanta la base de datos, la API y el portal. Ver [`../../infra/README.md`](../../infra/README.md).

Con la aplicación en marcha:

- **API:** http://localhost:8080/api/v1
- **Swagger:** http://localhost:8080/api/v1/swagger-ui.html

## Pruebas

Las pruebas levantan el contexto de Spring y necesitan la base de datos en ejecución:

```powershell
cd infra
docker compose up -d db
cd ..\src\api
.\mvnw.cmd test
```

Resultado esperado: 103 pruebas pasan (BUILD SUCCESS). Las pruebas cubren los controladores de cada módulo, el servicio de pagos y las reglas de roles y permisos.

## Métricas de mantenibilidad

Las métricas se calculan con **análisis estático** sobre `src/main/java`, por lo que no requieren compilar, ni base de datos, ni Docker.

| Métrica | Herramienta | Alcance |
|---|---|---|
| Complejidad ciclomática V(G) y LOC | Lizard | Todos los métodos de `src/main/java` |
| Suite CK (WMC, DIT, NOC, CBO, RFC, LCOM) | ck | Todas las clases de `src/main/java` |

Los comandos exactos, las fichas y los resultados están en [`../../metricas/`](../../metricas/) y [`../../docs/metricas/`](../../docs/metricas/).

## Flujo de trabajo

Se trabaja con una rama por funcionalidad y se integra a `main` mediante Pull Request revisado por el compañero. Los mensajes de commit describen qué cambió y por qué.