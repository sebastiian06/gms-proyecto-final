# infra · Infraestructura y despliegue local

> **GMS (Gym Management System)**: orquestación con Docker Compose, variables de entorno y guía de ejecución del monorepo.

Parte del **Proyecto Final del curso Métricas y Estándares de Calidad**: medición de métricas de mantenibilidad y evolución aplicadas a una aplicación real. La aplicación se reutiliza del trabajo desarrollado en la asignatura Desarrollo Fullstack, con autorización del docente.

## Contenido de esta carpeta

| Archivo | Función |
|---|---|
| `docker-compose.yml` | Levanta base de datos, API y portal |
| `Dockerfile.api` | Imagen del backend (Spring Boot, Java 21) |
| `Dockerfile.portal` | Imagen del frontend (React + Vite, servido con Nginx) |
| `.env.example` | Plantilla de variables de entorno (se copia a `.env`) |

## Estructura del monorepo

```text
gms-proyecto-final/
├── README.md
├── docs/            ← informe, requisitos, arquitectura, métricas
├── src/
│   ├── api/         ← backend: Spring Boot + Java 21
│   ├── portal/      ← frontend: React + Vite + TypeScript
│   └── db/          ← migraciones Flyway y datos semilla (PostgreSQL)
├── infra/           ← (esta carpeta) Docker Compose y variables de entorno
├── tests/
├── metricas/        ← scripts y resultados de medición
└── .gitignore
```

## Manual de ejecución

### 1. Requisitos

- **Docker Desktop** (v4.0+)
- **Git** (v2.30+)
- *Opcional, para desarrollo y medición local:* Java 21, Node.js 20+, Python 3 (para Lizard)

### 2. Clonar el repositorio

Un único repositorio contiene todo el sistema:

```bash
git clone https://github.com/sebastiian06/gms-proyecto-final.git
cd gms-proyecto-final
```

### 3. Crear el archivo `.env`

Copia la plantilla y edítala si es necesario. **El `.env` nunca se sube a Git** (está en `.gitignore`).

```powershell
cd infra
Copy-Item .env.example .env
```

En Linux/macOS: `cp .env.example .env`

### 4. Levantar la aplicación

Desde la carpeta `infra/`:

```bash
docker compose up --build
```

Espera a que los 3 contenedores estén en ejecución (aprox. 2-3 minutos la primera vez).

### 5. URLs y puertos

| Servicio | URL |
|---|---|
| Portal | http://localhost:5173 |
| API | http://localhost:8080/api/v1 |
| Swagger | http://localhost:8080/api/v1/swagger-ui.html |

### 6. Usuarios de prueba (solo entorno de desarrollo)

| Rol | Usuario | Contraseña |
|---|---|---|
| ADMIN | admin | admin123 |
| RECEPTIONIST | reception1 | secret123 |
| TRAINER | trainer1 | secret123 |
| MEMBER | member1 | secret123 |

*Los usuarios se crean desde la interfaz de ADMIN o desde Swagger. Estas credenciales son solo para pruebas locales y no deben usarse en ningún entorno real.*

### 7. Ejecutar las pruebas

**Backend (API).** La base de datos debe estar en ejecución (`docker compose up -d db`). Luego, desde la raíz del repositorio:

```powershell
cd src\api
.\mvnw.cmd test
```

En Linux/macOS: `./mvnw test`

Resultado esperado: 103 pruebas pasan (BUILD SUCCESS).

**Frontend (Portal):**

```bash
cd src/portal
npm install
npm run test
```

Resultado esperado: 2 pruebas pasan.

**Build de producción (Portal):**

```bash
cd src/portal
npm run build
```

### 8. Detener la aplicación y limpiar datos

```bash
docker compose down -v
```

*(La opción `-v` elimina el volumen de la base de datos.)*

### 9. Problemas comunes

**Puerto en uso (8080, 5173, 5433):** edita `.env` y cambia `API_PORT`, `PORTAL_PORT` o `POSTGRES_PORT`.

**Error de CORS:** verifica que el backend tenga `http://localhost:5173` como origen permitido.

**La base de datos no inicializa:** si hay errores de migración, limpia el volumen con `docker compose down -v` y vuelve a levantar.

**Rutas no encontradas al construir las imágenes:** el `context` de cada servicio en `docker-compose.yml` debe apuntar a `../src/api`, `../src/portal` y `../src/db`.

## Medición de métricas

Las métricas de mantenibilidad se calculan sobre el código fuente (análisis estático), por lo que **no requieren** levantar Docker ni la base de datos. Los scripts, comandos y resultados están en [`../metricas/`](../metricas/) y la documentación en [`../docs/metricas/`](../docs/metricas/).

## Flujo de ramas

Una rama permanente (`main`). No se hacen commits directos sobre ella: se trabaja en una rama por funcionalidad y se integra mediante Pull Request con revisión del compañero.

```text
main  <--PR--  feat/...  fix/...  docs/...  chore/...
```

Mensajes de commit descriptivos que expliquen qué cambió y por qué, por ejemplo: `Ajusta rutas del docker-compose a la estructura del monorepo`.

La entrega final se marca con la etiqueta `v1.0`.