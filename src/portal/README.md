# portal · Frontend de GMS

> **GMS (Gym Management System)**: interfaz web que consume la API REST del sistema.

Parte del **Proyecto Final del curso Métricas y Estándares de Calidad**. El portal es la capa de presentación de la aplicación: permite a cada rol (administrador, recepcionista, entrenador y socio) usar las funcionalidades del backend. Fue desarrollado originalmente en la asignatura Desarrollo Fullstack y se reutiliza aquí con autorización del docente.

## Tecnologías

- React + TypeScript
- Vite (herramienta de construcción y servidor de desarrollo)
- Tailwind CSS
- Vitest (pruebas)
- Oxlint (análisis estático del código)
- Nginx (sirve el build de producción dentro del contenedor)

## Estructura de la carpeta

```text
src/portal/
├── package.json
├── vite.config.ts / vitest.config.ts
├── tailwind.config.js / postcss.config.js
├── tsconfig*.json
├── nginx.conf              ← configuración de Nginx para el contenedor
├── public/                 ← recursos estáticos (favicon, logo)
└── src/
    ├── api/                ← cliente HTTP hacia la API
    ├── auth/               ← contexto de autenticación y rutas protegidas
    ├── components/         ← componentes reutilizables (Layout y modales de formulario)
    ├── pages/              ← pantallas, una por funcionalidad
    ├── types/              ← tipos de TypeScript compartidos
    ├── test/               ← pruebas
    ├── App.tsx             ← definición de rutas
    └── main.tsx            ← punto de entrada
```

### Pantallas por rol

| Pantalla | Funcionalidad |
|---|---|
| `Login` | Inicio de sesión |
| `Dashboard` | Resumen general |
| `Members`, `MemberDetail` | Gestión y detalle de socios |
| `Plans` | Planes de membresía |
| `Payments` | Pagos del gimnasio |
| `Attendance`, `Kiosk` | Registro de asistencia (entrada y salida) |
| `Users`, `Roles` | Administración de usuarios y roles |
| `Trainers`, `Routines`, `AssignedMembers` | Entrenadores, rutinas y socios asignados |
| `MyProfile`, `MyPayments`, `MyRoutine` | Vista del propio socio |

## Ejecutar

La forma recomendada es con Docker Compose, que levanta base de datos, API y portal. Ver [`../../infra/README.md`](../../infra/README.md).

Para desarrollo local del portal (requiere Node.js 20+ y la API en ejecución):

```bash
cd src/portal
npm install
npm run dev
```

Portal disponible en http://localhost:5173.

## Pruebas y build

```bash
npm run test     # pruebas con Vitest
npm run build    # build de producción
```

Resultado esperado de las pruebas: 2 pruebas pasan.

## Métricas de mantenibilidad

El alcance principal de la medición es el backend Java (`src/api`), porque la suite CK aplica a lenguajes orientados a objetos. El portal queda **fuera del alcance** de la medición principal; si el equipo decide incluirlo, puede medirse con Lizard (que soporta TypeScript) para obtener complejidad ciclomática y LOC. La decisión y su justificación se documentan en [`../../docs/metricas/`](../../docs/metricas/).

## Flujo de trabajo

Se trabaja con una rama por funcionalidad y se integra a `main` mediante Pull Request revisado por el compañero. Los mensajes de commit describen qué cambió y por qué.