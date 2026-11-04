# Requisitos del Proyecto — GMS (Gym Management System)

## 1. Contexto y tema elegido

**GMS (Gym Management System)** es una aplicación web para la gestión integral de un gimnasio. Permite administrar clientes, planes de membresía, pagos, asistencia y rutinas de entrenamiento, con control de acceso basado en roles (administrador, recepcionista, entrenador y cliente).

## 2. Problema que resuelve

Los gimnasios pequeños y medianos suelen gestionar sus operaciones con hojas de cálculo, cuadernos o sistemas dispersos que no se comunican entre sí. Esto genera pérdida de información sobre membresías vencidas, dificultad para controlar el acceso de los clientes, falta de trazabilidad sobre rutinas asignadas e imposibilidad de obtener reportes consolidados. GMS centraliza estas operaciones en una sola aplicación web con API REST y base de datos relacional.

## 3. Usuarios del sistema

| Rol | Descripción |
|---|---|
| **Administrador (ADMIN)** | Gestiona usuarios, planes, clientes, membresías y reportes. Acceso total. |
| **Recepcionista (RECEPTIONIST)** | Registra clientes, crea membresías, registra pagos y controla la asistencia. |
| **Entrenador (TRAINER)** | Crea rutinas y ejercicios para los clientes que tiene asignados. |
| **cliente (MEMBER)** | Consulta su perfil, rutina, historial de pagos y registra su entrada/salida. |

## 4. Alcance

**Entra (in scope):** autenticación con JWT, gestión de usuarios con roles, CRUD de clientes, gestión de planes, creación de membresías, registro de pagos, registro de asistencia (entrada/salida) por código, gestión de entrenadores/rutinas/ejercicios, asignación de rutinas, reportes (resumen y pagos recientes), portal web para cada rol, base de datos PostgreSQL con migraciones Flyway, despliegue con Docker Compose.

**No entra (out of scope):** facturación electrónica o pasarelas de pago reales, notificaciones por correo/SMS, app móvil nativa, inventario de suplementos, reserva de clases grupales, módulo contable completo, integración con hardware biométrico.

---

## 5. Requisitos Funcionales (RF)

| ID | Descripción | Criterio de aceptación | Componente |
|---|---|---|---|
| **RF-01** | El sistema debe permitir a un usuario autenticarse con nombre de usuario y contraseña, devolviendo un token JWT válido con su rol y fecha de expiración. | Un usuario con credenciales válidas recibe un token JWT. Credenciales inválidas retornan HTTP 401 con mensaje "Invalid credentials". | `AuthController`, `AuthService`, `JwtService` |
| **RF-02** | El sistema debe permitir al administrador crear, listar, consultar, actualizar, activar y desactivar usuarios, asignando roles ADMIN, RECEPTIONIST, TRAINER o MEMBER. | Un ADMIN puede crear un usuario. Un usuario sin rol ADMIN recibe HTTP 403. El sistema impide desactivar al administrador del sistema. | `UserController`, `UserService` |
| **RF-03** | El sistema debe permitir a un ADMIN o RECEPTIONIST registrar un cliente con sus datos personales, creando su usuario de acceso y membresía inicial. | El cliente queda almacenado y consultable. Documento o username duplicado retorna HTTP 409. Un MEMBER no puede registrar clientes (HTTP 403). | `MemberController`, `MemberService` |
| **RF-04** | El sistema debe permitir consultar el listado paginado de clientes (con filtro por estado), el detalle de un cliente y actualizar su nombre y contacto. | El listado retorna página con metadatos. Actualización de cliente inexistente retorna HTTP 404. | `MemberController`, `MemberService` |
| **RF-05** | El sistema debe permitir al administrador crear, listar, consultar, actualizar, activar y desactivar planes de membresía (nombre, precio, duración en días). | Nombre duplicado entre planes activos retorna HTTP 409. Precio negativo o duración ≤ 0 es rechazado (HTTP 400). | `PlanController`, `PlanService` |
| **RF-06** | El sistema debe permitir a un ADMIN o RECEPTIONIST crear una membresía para un cliente, asociándole un plan activo y calculando la fecha de fin según la duración. | Un cliente solo puede tener una membresía ACTIVE a la vez (HTTP 409 si ya tiene). Plan inactivo retorna HTTP 409. Se registra el precio del plan al momento (locked_price). | `MembershipController`, `MembershipService` |
| **RF-07** | El sistema debe permitir registrar pagos parciales o totales sobre una membresía activa, validando que la suma no exceda el precio bloqueado. | Pago que excede el saldo retorna HTTP 409. Pago sobre membresía no activa retorna HTTP 409. El cliente solo puede ver su propio historial (HTTP 403 para otros). | `MembershipController`, `PaymentController`, `PaymentService` |
| **RF-08** | El sistema debe permitir registrar entrada y salida de un cliente mediante su código de acceso, rechazando entradas duplicadas y salidas sin entrada previa. | Código válido registra entrada (HTTP 201). Entrada duplicada retorna HTTP 409. Salida sin entrada previa retorna HTTP 409. Código inexistente retorna HTTP 404. | `AttendanceController`, `AttendanceService` |
| **RF-09** | El sistema debe permitir al administrador crear entrenadores, y a los entrenadores crear rutinas con ejercicios y asignarlas a sus clientes. | TRAINER puede crear rutinas/ejercicios; ADMIN no (HTTP 403). cliente sin rutina retorna HTTP 404. Ejercicios se listan en orden de visualización. | `TrainerController`, `RoutineController`, `ExerciseController` |
| **RF-10** | El sistema debe permitir a un ADMIN o RECEPTIONIST consultar un resumen general (totales de clientes, membresías activas, ingresos) y una lista de pagos recientes con límite configurable. | El resumen retorna los totales. La lista respeta el parámetro `limit` (por defecto 5). Sin rol ADMIN o RECEPTIONIST retorna HTTP 403. | `ReportController`, `ReportService` |

---

## 6. Requisitos No Funcionales (RNF)

| ID | Categoría | Descripción medible | Forma de verificación |
|---|---|---|---|
| **RNF-01** | Rendimiento | Las consultas de listado de clientes, planes y membresías deben responder en **≤ 2 segundos** en el 95% de los casos, con hasta 100 registros por página. | Prueba de carga con `curl` o JMeter midiendo `GET /members`, `GET /plans`, `GET /memberships`. Se registra el percentil 95. |
| **RNF-02** | Seguridad | Las contraseñas deben almacenarse cifradas con **BCrypt** y todo endpoint protegido debe requerir un **token JWT válido** con firma y expiración. | Revisión de código en `SecurityConfig` y `PasswordEncoder`. Pruebas automatizadas que verifican HTTP 401 sin token y HTTP 403 con rol insuficiente. |
| **RNF-03** | Mantenibilidad | Ningún método de `src/main/java` debe superar una **complejidad ciclomática V(G) de 10**. | Análisis estático con **Lizard** sobre `src/main/java`. Se genera reporte con V(G) por método e identificación de violaciones. |

---

## 7. Criterios para redactar los requisitos

- Cada RF expresa una sola necesidad y evita términos ambiguos.
- Los criterios de aceptación son verificables mediante las pruebas automatizadas existentes.
- Los 10 RF cubren el núcleo del dominio: autenticación (RF-01), administración (RF-02, RF-05), registro (RF-03, RF-06, RF-07), consulta (RF-04, RF-10), procesos de negocio (RF-07, RF-08) y reportes (RF-10).
- Los 3 RNF son medibles y uno de ellos (RNF-03) conecta directamente con las métricas de la Fase 4.