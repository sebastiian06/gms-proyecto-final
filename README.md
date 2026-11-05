# GMS — Gym Management System

> Proyecto final del curso **Métricas y Estándares de Calidad**.
> Aplicación web para la gestión integral de un gimnasio: clientes, planes, membresías, pagos, asistencia y rutinas.

## Integrantes del grupo

| Nombre | Usuario de GitHub | Rol / Aporte principal |
|---|---|---|
| Juan Sebastian Agudelo Quintero | [@sebastiian06](https://github.com/sebastiian06) | Backend (Spring Boot), arquitectura, métricas |
| Dayana Motta Camayo | [@dayana2100](https://github.com/dayana2100) | Frontend (React), documentación, infraestructura |

## 1. Descripción del proyecto

**GMS** es una aplicación web para gimnasios pequeños y medianos que centraliza la gestión de clientes, membresías, pagos, asistencia y rutinas de entrenamiento. Resuelve la dispersión de información (hojas de cálculo, cuadernos) y permite controlar el acceso de los clientes, validar pagos y asignar rutinas a cada cliente.

**Usuarios:**
- **Administrador:** gestiona usuarios, planes, clientes y reportes.
- **Recepcionista:** registra clientes, membresías, pagos y asistencia.
- **Entrenador:** crea rutinas y ejercicios para sus clientes asignados.
- **Cliente:** consulta su perfil, rutina, historial de pagos y registra su entrada/salida.

## 2. Requisitos

### Requisitos funcionales (RF)
Ver [`docs/requisitos.md`](docs/requisitos.md) para el detalle de los 10 RF con criterios de aceptación.

| ID | Descripción corta |
|---|---|
| RF-01 | Autenticación con JWT |
| RF-02 | Gestión de usuarios con roles |
| RF-03 | Registro de clientes |
| RF-04 | Consulta y actualización de clientes |
| RF-05 | Gestión de planes |
| RF-06 | Creación de membresías |
| RF-07 | Registro de pagos |
| RF-08 | Registro de asistencia (entrada/salida) |
| RF-09 | Gestión de entrenadores y rutinas |
| RF-10 | Reportes (resumen y pagos recientes) |

### Requisitos no funcionales (RNF)
Ver [`docs/requisitos.md`](docs/requisitos.md) para el detalle.

| ID | Categoría | Descripción corta |
|---|---|---|
| RNF-01 | Rendimiento | Consultas ≤ 2s en el 95% de los casos |
| RNF-02 | Seguridad | BCrypt + JWT |
| RNF-03 | Mantenibilidad | V(G) ≤ 10 por método |

## 3. Arquitectura

**Monolito modular con arquitectura en capas.** Cada módulo de negocio (`identity`, `members`, `plans`, `billing`, `attendance`, `training`, `reports`) es un paquete independiente dentro de `com.gms`, con capas internas (`controller`, `dto`, `entity`, `repository`, `service`). La comunicación entre módulos se hace mediante una **Facade** por módulo.

Ver [`docs/arquitectura/arquitectura.md`](docs/arquitectura/arquitectura.md) para la justificación y los diagramas.

## 4. Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot (Web, Data JPA, Security), JWT, OpenAPI/Swagger |
| Frontend | React + TypeScript, Vite, Tailwind CSS, Vitest |
| Base de datos | PostgreSQL 16, Flyway (migraciones versionadas) |
| Infraestructura | Docker, Docker Compose, Nginx |
| Métricas | Lizard (V(G) y LOC), CK (suite CK), scripts propios |

## 5. Instalación y ejecución

### Prerrequisitos
- Docker Desktop (con Docker Compose v2)
- Git

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/sebastiian06/gms-proyecto-final
cd gms-proyecto-final

# 2. Copiar variables de entorno
cd infra
Copy-Item .env.example .env

# 3. Levantar todo (base de datos + API + portal)
docker compose up --build

# 4. Abrir en el navegador
- Portal:   http://localhost:5173
- Swagger:  http://localhost:8080/api/v1/swagger-ui.html
- Login:    admin / admin123
```

### Ejecutar las pruebas del backend

```bash
cd infra
docker compose up -d db
cd ..\src\api
.\mvnw.cmd test
```

Resultado esperado: **114 pruebas pasan** (BUILD SUCCESS).

### Ejecutar las pruebas del frontend

```bash
cd src\portal
npm install
npm run test
```

Resultado esperado: **2 pruebas pasan**.

## 6. Métricas aplicadas

Ver [`docs/metricas/`](docs/metricas/) para las fichas de métricas, resultados y gráficos.

| Métrica | Herramienta | Alcance |
|---|---|---|
| Complejidad ciclomática V(G) y LOC | Lizard | Todos los métodos de `src/main/java` |
| Suite CK (WMC, DIT, NOC, CBO, RFC, LCOM) | CK | Todas las clases de `src/main/java` |
| Índice de Mantenibilidad (MI) | Lizard / script | Todos los métodos de `src/main/java` |

## 7. Mejoras propuestas

Ver [`docs/metricas/mejoras.md`](docs/metricas/mejoras.md) para el análisis y las refactorizaciones propuestas.

## 8. Estructura del repositorio

```text
gms-proyecto-final/
├── README.md                  ← este archivo
├── docs/                      ← documentación del proyecto
│   ├── requisitos.md
│   ├── trazabilidad.md
│   ├── verificacion-rnf.md
│   ├── informe-final.md
│   ├── arquitectura/
│   └── metricas/
├── infra/                     ← Docker Compose y Dockerfiles
├── metricas/                  ← scripts y resultados de medición
│   ├── README.md
│   ├── scripts/               ← scripts de medición (Lizard, CK)
│   └── resultados/            ← CSV de resultados
├── src/
│   ├── api/                   ← backend Spring Boot
│   ├── portal/                ← frontend React + Vite
│   └── db/                    ← migraciones Flyway y seed
└── tests/                     ← pruebas de carga y pruebas adicionales
    ├── README.md
    └── carga-rnf01.ps1
```

## 9. Informe final

El informe final en PDF está en [`docs/informe-final.pdf`](docs/informe-final.pdf).

## 10. Licencia y estado

- **Licencia:** MIT
- **Estado:** En desarrollo (Fases 1–3 completadas, Fase 4 en progreso)
- **Repositorio:** [enlace al repositorio]