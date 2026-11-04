# Postman Collection — GMS API

Tests the login and one full CRUD (Plans), satisfying the c3-project rubric
requirement: "una colección de Postman en `postman/` que prueba el login y un
CRUD".

## Prerequisites

1. The API is running on `http://localhost:8080/api/v1`.
2. The database has an admin user. Run the seed script from `gms-db` (or
   create one via `POST /api/v1/users` with an existing admin).

## How to run

1. **Import** both files into Postman:
   - `GMS-API.postman_collection.json`
   - `GMS-API.postman_environment.json`
2. **Select the environment** `GMS API — Local` in the top-right selector.
3. **Run the collection**:
   - Click the collection → **Run** → **Run GMS API — c3-project**.
   - Or use **Collection Runner**.

The collection runs in order:

| # | Request | What it verifies |
|---|---|---|
| 1 | Login | Returns 200 with `token` and `user` |
| 2 | Create plan | Returns 201 with the new plan; saves `planId` |
| 3 | List plans | Returns 200 with a non-empty `content` |
| 4 | Get plan by id | Returns 200 with the plan |
| 5 | Update plan | Returns 200 with updated fields |
| 6 | Deactivate plan | Returns 204 |

## Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `baseUrl` | `http://localhost:8080/api/v1` | API base URL |
| `adminUsername` | `admin` | Admin credentials |
| `adminPassword` | `admin123` | Admin credentials |
| `adminToken` | _(empty)_ | Auto-set by the Login request |
| `planId` | _(empty)_ | Auto-set by Create plan |

## CI integration (optional)

With `newman` installed:

```bash
newman run postman/GMS-API.postman_collection.json \
  -e postman/GMS-API.postman_environment.json \
  --reporters cli
```
