# 📄 Global-Invoice — Microservicio Core (Java / Spring Boot)

Microservicio transaccional de facturas: motor tributario dinámico (Strategy / SOLID), CRUD, JWT con RBAC y puente SOAP → JSON para el front.

Comparte PostgreSQL con el micro de métricas en Python. Este servicio **escribe** facturas; Python solo **agrega** para el dashboard.

---

## Tecnologías

* Java 17 · Spring Boot 4.1 · Spring Data JPA · PostgreSQL
* Spring Security + JWT
* SOAP DataFlex NumberConversion (consumido en backend)
* JWT + RBAC
* Aviso interno al micro Python al crear factura (`POST /internal/events/invoice-created`)
* SSE opcional (`/api/invoices/events`); el dashboard del front debe usar el **WebSocket de Python**
* JaCoCo (umbral 80%) · Docker · GitHub Actions

---

## Contrato para Angular y Python

### Auth

`POST /api/auth/login`

```json
{ "username": "operador", "password": "Operador123!" }
```

Respuesta: `{ "token", "username", "role" }` (`OPERADOR` | `AUDITOR`).

Usuarios semilla:

| Usuario   | Password       | Rol       | Puede crear | Listado | Dashboard (front) |
|-----------|----------------|-----------|-------------|---------|-------------------|
| operador  | Operador123!   | OPERADOR  | Sí          | Sí      | No                |
| auditor   | Auditor123!    | AUDITOR   | No          | Sí      | Sí                |

Header: `Authorization: Bearer <token>`.

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — Authorize con el JWT. OpenAPI: `/v3/api-docs`.

### Facturas

* `POST /api/invoices` — **OPERADOR**
* `GET /api/invoices` — OPERADOR y AUDITOR. Lista paginada y permite buscar por cliente o tipo.
* `GET /api/invoices/{id}` — detalle + `totalInWords` (SOAP)

Parámetros opcionales del listado:

* `q` — texto del cliente, o tipo exacto (`NACIONAL`, `EXPORTACION`, `GUBERNAMENTAL`), sin distinción entre mayúsculas y minúsculas.
* `page` — página desde `0` (por defecto `0`).
* `size` — cantidad de resultados por página (por defecto `10`).
* `sort` — orden Spring Data, por defecto `createdAt,desc`.

Ejemplo: `GET /api/invoices?q=Acme&page=0&size=10`.

### Clientes

* `GET /api/clients` — OPERADOR y AUDITOR. Consulta paginada para el dropdown de alta de facturas.

Parámetros opcionales:

* `q` — busca por nombre, número de documento o email, sin distinción entre mayúsculas y minúsculas.
* `page`, `size`, `sort` — paginación y orden, con valores por defecto `0`, `10` y `name,asc`.

Ejemplo: `GET /api/clients?q=Acme&page=0&size=10`.

### Dashboard (Python — usar esto en el front)

* `GET http://localhost:5000/api/v1/metrics/by-type` — **AUDITOR**, `Authorization: Bearer <jwt>`
* `WS ws://localhost:5000/ws/metrics?token=<jwt>` — snapshot al conectar; push al crear factura
* Claim JWT: `role` = `ROLE_AUDITOR` | `ROLE_OPERADOR` (mismo `JWT_SECRET` que Python)

No hace falta SSE de Java ni refetch a BD: el WS de Python ya empuja el agregado.

### Tabla `invoices` (Python)

Columnas: `id`, `invoice_type`, `subtotal`, `iva`, `withholding`, `total`, `customs_code`, `client_id`, `description`, `created_at`, `created_by`.

Al crear, Java notifica a Python:

`POST {METRICS_BASE_URL}/internal/events/invoice-created`  
Header `X-Internal-Key: {INTERNAL_API_KEY}`  
`{"invoice_type":"NACIONAL","total":"119.00"}`

Copia el mismo `JWT_SECRET` e `INTERNAL_API_KEY` del `.env` de Python.

#### Alta

```json
{
  "type": "NACIONAL | EXPORTACION | GUBERNAMENTAL",
  "subtotal": 100.00,
  "clientId": 1,
  "customsCode": "solo EXPORTACION, obligatorio",
  "description": "opcional"
}
```

Si el tipo no es `EXPORTACION`, `customsCode` no se persiste (el front no debe enviarlo).

Cálculos (Open/Closed: una clase Strategy por tipo, registro automático):

* Nacional: subtotal + 19% IVA
* Exportación: subtotal + 0% IVA
* Gubernamental: subtotal + 19% IVA − 5% retención sobre el subtotal

### Tabla `invoices` (Python)

Columnas: `id`, `invoice_type`, `subtotal`, `iva`, `withholding`, `total`, `customs_code`, `client_name`, `description`, `created_at`, `created_by`.

Métricas: `SELECT invoice_type, SUM(total) FROM invoices GROUP BY invoice_type`.

Tiempo real (RF-04): el auditor se conecta al WebSocket de Python; Java avisa al crear y Python empuja el gráfico **sin** nueva consulta HTTP a la BD.

---

## Ejecución local

Red Docker (compartida con Postgres y Python):

```bash
docker network create global-invoice-net
```

Postgres (carpeta `mysql/docker-compose.yml` del repo) y luego:

```bash
docker compose up --build
```

Para recrear la base local una sola vez con la relación `clients` → `invoices`:

```bash
docker compose down -v
SPRING_JPA_HIBERNATE_DDL_AUTO=create docker compose up --build
```

Después de confirmar que los clientes iniciales fueron sembrados, reinicia normalmente:

```bash
docker compose down
docker compose up --build
```

El valor normal es `update`, para conservar los datos en los siguientes arranques.

Health: `GET http://localhost:8080/api/health`

Tests:

```bash
mvn -B verify
```

---

## Nuevo tipo de factura (sin tocar cálculos existentes)

1. Valor en `InvoiceType`.
2. Nueva clase `@Component` que implemente `TaxCalculationStrategy`.
3. El `TaxStrategyRegistry` la descubre sola.
