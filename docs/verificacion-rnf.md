# Verificación de Requisitos No Funcionales (RNF) — GMS

Este documento detalla cómo se verifica cada uno de los 3 RNF definidos en `docs/requisitos.md`.

---

## RNF-01 — Rendimiento (≤ 2 segundos en el 95% de los casos)

### Descripción medible
Las consultas de listado de clientes, planes y membresías deben responder en **≤ 2 segundos** en el 95% de los casos, con hasta 100 registros por página.

### Forma de verificación

**Herramienta:** `curl` o JMeter (se usará `curl` por simplicidad, midiendo el tiempo total con `Measure-Command` en PowerShell).

**Prerrequisitos:**
- Base de datos con al menos 100 registros en cada tabla (se puede generar con el seed o con un script de inserción masiva).
- API en ejecución (`docker compose up -d`).

**Comandos de medición:**
```powershell
# Medir 5 veces el listado de clientes con 100 registros
1..5 | ForEach-Object {
    Measure-Command {
        curl -s -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/v1/members?size=100 | Out-Null
    } | Select-Object TotalSeconds
}
```

**Criterio de aceptación:**
- El percentil 95 de los tiempos debe ser ≤ 2.0 segundos.
- Si alguna medición supera 2s, se documenta y se analiza la causa (índice faltante, consulta N+1, etc.).

### Resultados esperados
Con la base de datos local y 100 registros, los tiempos típicos son < 500 ms. Se documentará la tabla de resultados en `docs/metricas/resultados-rnf01.md` durante la Fase 4.

---

## RNF-02 — Seguridad (BCrypt + JWT)

### Descripción medible
Las contraseñas deben almacenarse cifradas con **BCrypt** y todo endpoint protegido debe requerir un **token JWT válido** con firma verificable y expiración.

### Forma de verificación

**1. Cifrado de contraseñas (BCrypt):**

Revisión de código en `src/api/src/main/java/com/gms/platform/config/SecurityConfig.java`:
- Se declara un bean `PasswordEncoder` que retorna `new BCryptPasswordEncoder()`.
- En `UserService` y `AuthService`, las contraseñas se cifran con `passwordEncoder.encode(...)` antes de persistirlas.

**Verificación en base de datos:**
```sql
SELECT username, password_hash FROM identity_users LIMIT 5;
```

El campo `password_hash` debe empezar con `$2a$` o `$2b$` (formato BCrypt), nunca con texto plano.

**2. Autenticación con JWT:**

Revisión de código en `src/api/src/main/java/com/gms/identity/service/JwtAuthFilter.java`:
- Intercepta cada petición y valida el header `Authorization: Bearer <token>`.
- Verifica la firma con `JwtService` y la expiración.
- Si el token es inválido o expiró, responde HTTP 401.

**Pruebas automatizadas existentes:**
- `AuthControllerTest.login_whenWrongPassword_returns401()` → valida que credenciales inválidas no generan token.
- `RolesPermissionsTest` → valida que sin token o con rol insuficiente se retorna 401/403.
- `UserControllerTest.createUser_withoutToken_returns401()` → valida que endpoints protegidos rechazan peticiones sin token.

**Criterio de aceptación:**
- Toda contraseña en `identity_users.password_hash` cumple el formato BCrypt.
- Todos los endpoints bajo `/api/v1/**` (excepto `/auth/login` y `/attendance/entries|exits`) requieren token válido.
- Existen pruebas automatizadas que verifican 401 sin token y 403 con rol insuficiente.

---

## RNF-03 — Mantenibilidad (V(G) ≤ 10)

### Descripción medible
Ningún método de `src/main/java` debe superar una **complejidad ciclomática V(G) de 10** (umbral de riesgo bajo según McCabe).

### Forma de verificación

**Herramienta:** [Lizard](https://github.com/terryyin/lizard) (analizador de complejidad ciclomática para múltiples lenguajes, incluido Java).

**Instalación:**
```bash
pip install lizard
```

**Comando de medición:**
```bash
lizard src/api/src/main/java/com/gms --CCN 10
```

**Interpretación:**
- `lizard` lista cada método con su V(G).
- Si un método tiene V(G) > 10, aparece en la salida como advertencia.
- El objetivo es que la lista de advertencias esté vacía, o que los métodos que la superen estén justificados y tengan una refactorización planificada en la Fase 5.

**Criterio de aceptación:**
- Ningún método de `src/main/java` supera V(G) = 10.
- Si alguno lo supera, se documenta en `docs/metricas/resultados-vg.md` con la refactorización propuesta (Extract Method, etc.).

### Resultados esperados
Se documentarán en `docs/metricas/resultados-vg.md` durante la Fase 4, junto con el resto de métricas.

---

## Resumen de verificación

| RNF | Herramienta | Evidencia | Estado |
|---|---|---|---|
| RNF-01 | `curl` / JMeter | Tabla de tiempos en `docs/metricas/resultados-rnf01.md` | Pendiente Fase 4 |
| RNF-02 | Revisión de código + pruebas | `SecurityConfig`, `JwtAuthFilter`, `AuthControllerTest`, `RolesPermissionsTest` | ✅ Verificado |
| RNF-03 | Lizard | Tabla de V(G) en `docs/metricas/resultados-vg.md` | Pendiente Fase 4 |
```
