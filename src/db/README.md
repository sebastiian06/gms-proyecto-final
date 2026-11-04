# db · Base de datos de GMS

> **GMS (Gym Management System)**: esquema, migraciones y datos semilla de PostgreSQL.

Parte del **Proyecto Final del curso Métricas y Estándares de Calidad**. La base de datos respalda los requisitos funcionales del sistema; el esquema evoluciona mediante migraciones versionadas, lo que deja trazabilidad de los cambios (útil para el análisis de mantenimiento y evolución). Fue desarrollada originalmente en la asignatura Desarrollo Fullstack y se reutiliza aquí con autorización del docente.

## Tecnologías

- PostgreSQL
- Flyway (migraciones versionadas)

## Estructura de la carpeta

```text
src/db/
├── flyway.conf        ← configuración de Flyway
├── .env.example       ← plantilla de variables de entorno
├── migrations/        ← scripts SQL versionados (V001 … V010)
└── seed/
    └── dev-seed.sql   ← datos de prueba solo para desarrollo
```

## Migraciones

Cada script crea o modifica una parte del esquema. Los módulos coinciden con los paquetes del backend (`src/api`).

| Versión | Descripción | Módulo |
|---|---|---|
| V001 | Tablas de identidad | identity |
| V002 | Tablas de clientes | members |
| V003 | Tablas de planes | plans |
| V004 | Tablas de facturación | billing |
| V005 | Tablas de asistencia | attendance |
| V006 | Tablas de entrenamiento | training |
| V007 | Tabla de relación entrenador-cliente | training |
| V008 | Agrega el rol de recepcionista | identity |
| V009 | Refactorización del esquema de entrenamiento | training |
| V010 | Agrega la marca de administrador del sistema | identity |

**Reglas:**

- Una migración ya aplicada **no se edita**: cualquier cambio se hace con una nueva versión (`V011__descripcion.sql`).
- Nombre con el formato `V<número>__<descripcion_en_snake_case>.sql` (dos guiones bajos).
- Las migraciones V008, V009 y V010 muestran la evolución real del esquema durante el desarrollo.

## Datos semilla

`seed/dev-seed.sql` carga datos de prueba (incluye los usuarios de prueba listados en [`../../infra/README.md`](../../infra/README.md)). **Es solo para desarrollo**: no debe usarse en ningún entorno real.

## Ejecución

La base de datos se levanta con el resto del sistema mediante Docker Compose. Ver [`../../infra/README.md`](../../infra/README.md).

Para levantar solo la base de datos (por ejemplo, para correr las pruebas del backend):

```powershell
cd infra
docker compose up -d db
```

Para reiniciarla desde cero (elimina los datos):

```powershell
docker compose down -v
```

## Variables de entorno

`.env.example` es la plantilla. Se copia a `.env`, que **nunca se sube a Git**.

## Métricas

El código SQL no entra en la medición de métricas: Lizard y ck analizan el código Java del backend (`src/api/src/main/java`). La base de datos aporta al análisis de evolución por medio del historial de migraciones.

## Flujo de trabajo

Se trabaja con una rama por funcionalidad y se integra a `main` mediante Pull Request revisado por el compañero. Los mensajes de commit describen qué cambió y por qué.