# FitStore — Backend (Spring Boot)

API REST de la tienda de productos de gimnasio **FitStore**.
Java 25 (LTS) · Spring Boot 3.5.7 · SQL Server · Redis · RabbitMQ.

---

## Requisitos

| Herramienta | Versión | Notas |
|-------------|---------|-------|
| Java JDK    | 25 (LTS)| `JAVA_HOME` debe apuntar al JDK 25 |
| Maven       | —       | No hace falta instalarlo: el repo incluye el **Maven Wrapper** (`mvnw.cmd` / `mvnw`) |
| SQL Server  | 2022    | Base de datos `fitstore_db` |
| Redis       | 7       | Levantado con `docker-compose up -d` (puerto 6380) |
| RabbitMQ    | 3       | Levantado con `docker-compose up -d` (puerto 5672 / panel 15672) |

---

## Configuración

La configuración está separada por entorno y **no versiona secretos**:

- `application.properties` — configuración base. Todos los valores sensibles se leen de variables de entorno (`${DB_USERNAME}`, `${JWT_SECRET}`, …) con valores por defecto para el resto.
- `application-local.properties` — **archivo local, ignorado por Git** (`.gitignore`). Contiene las credenciales reales.
- `application-local.properties.example` — plantilla versionada. Copiar y ajustar:

  ```bash
  copy application-local.properties.example application-local.properties   # Windows
  cp    application-local.properties.example application-local.properties # Linux/macOS
  ```

Si falta una variable obligatoria (p. ej. `JWT_SECRET`), la aplicación **falla al arrancar** con un mensaje claro en lugar de arrancar mal configurada (*fail-fast*).

### Variables de entorno

| Variable | Por defecto | Obligatoria |
|----------|-------------|-------------|
| `SPRING_PROFILES_ACTIVE` | `local` | No |
| `SERVER_PORT` | `8080` | No |
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=fitstore_db;…` | No |
| `DB_USERNAME` | — | **Sí** (o en `application-local.properties`) |
| `DB_PASSWORD` | — | **Sí** (o en `application-local.properties`) |
| `JWT_SECRET` | — | **Sí** (mínimo 32 caracteres) |
| `JWT_EXPIRATION` | `86400000` (24 h) | No |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6380` | No |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | `localhost` / `5672` | No |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `guest` / `guest` | No |
| `CORS_ORIGINS` | `http://localhost:3000,http://127.0.0.1:5500,http://localhost:5500,null` | No |
| `LOG_LEVEL` | `INFO` | No |

---

## Compilar y ejecutar

```bash
# Windows
mvnw.cmd clean install
mvnw.cmd spring-boot:run

# Linux/macOS (o con Maven global)
./mvnw clean install
./mvnw spring-boot:run
```

Al iniciar, Spring Boot crea/actualiza las tablas, inserta datos de prueba (`DataSeeder`) y se conecta a Redis y RabbitMQ. La API queda en `http://localhost:8080`.

---

## Pruebas

```bash
mvnw.cmd test        # Windows
./mvnw test          # Linux/macOS
```

- **ProductoServiceTest** — pruebas unitarias del servicio de productos y la caché Redis.
- **CucumberRunner** — 15 escenarios BDD en `src/test/resources/features/compra.feature`: login con JWT, catálogo, filtros, búsqueda, carrito, compra ACID, verificación de stock < 1 s (RNF-01), notificaciones asíncronas por RabbitMQ, estados de pedido (con restauración de stock al cancelar) y panel admin.

**Total: 18 pruebas.** Requieren SQL Server, Redis y RabbitMQ arriba.

> `TestDataRestorer` deja la base limpia al terminar: restaura el catálogo original de 12 productos, borra pedidos/ítems y vacía la caché de Redis.

---

## Formato y análisis de código (Spotless)

El formato se aplica con [`spotless-maven-plugin`](https://github.com/diffplug/spotless) usando **palantir-java-format 2.80.0** (compatible con JDK 25), más eliminación de imports sin usar y limpieza de espacios finales.

```bash
mvnw.cmd spotless:apply   # formatea el código
mvnw.cmd spotless:check   # valida (falla si hay algo sin formatear)
```

---

## Contrato de la API

### Envelope de respuestas

Todas las respuestas de éxito tienen la forma:

```json
{
  "data": [ ... ],
  "meta": { "page": 1, "limit": 10, "total": 42, "totalPages": 5, "hasNextPage": true, "hasPrevPage": false }
}
```

- `meta` solo aparece en listados cuando se envían `?page=` y `?limit=`.
- `data` contiene la entidad, la lista o el mapa de resultado.

### Formato de errores

```json
{
  "error": {
    "status": 400,
    "message": "El cuerpo de la petición está mal formado o no es JSON válido",
    "details": { "email": "debe ser un correo válido" }
  }
}
```

| Situación | HTTP | Ejemplo |
|-----------|------|---------|
| Validación de campos (`@Valid`) | `400` | `details` con el error por campo |
| Regla de negocio (`FitStoreException`) | `400` | `"Usuario no encontrado"`, `"Credenciales inválidas"` |
| JSON mal formado / tipo de parámetro inválido | `400` | `"Parámetro inválido: id"` |
| Sin autorización / token inválido | `401` / `403` | — |
| Recurso no encontrado | `404` | — |
| Violación de integridad en BD (unique/FK) | `409` | `"La operación viola una restricción de la base de datos"` |
| Error inesperado | `500` | mensaje genérico (sin filtrar detalles internos) |

### Endpoints

**Autenticación** — `/api/auth`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST   | `/api/auth/login`    | Iniciar sesión (`{ email, clave }`) | No |
| POST   | `/api/auth/register` | Registrar usuario (`{ nombre, email, clave }`) | No |

**Sistema**

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET    | `/api/health` | Estado de la API + BD, Redis y RabbitMQ (`UP`/`DEGRADED`) | No |

**Productos** — `/api/productos`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET    | `/api/productos` | Listar todos (soporta `?page=` y `?limit=`) | No |
| GET    | `/api/productos?categoria=ROPA` | Filtrar por categoría | No |
| GET    | `/api/productos?buscar=proteína` | Buscar por nombre | No |
| GET    | `/api/productos/{id}` | Obtener uno | No |
| GET    | `/api/productos/{id}/stock?cantidad=2` | Verificar stock en caché (RNF-01) | No |

**Pedidos** — `/api/pedidos`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST   | `/api/pedidos` | Confirmar compra ACID (`{ items: [{ productoId, cantidad }] }`) | JWT |
| GET    | `/api/pedidos/mis-pedidos` | Historial del cliente (soporta paginación) | JWT |

**Admin** — `/api/admin` (rol `ADMIN`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET    | `/api/admin/productos` | Listar inventario (soporta paginación) | JWT ADMIN |
| PATCH  | `/api/admin/productos/{id}/stock` | Actualizar stock (`{ stock }`) | JWT ADMIN |
| GET    | `/api/admin/productos/stock-bajo` | Alertas de stock bajo | JWT ADMIN |
| GET    | `/api/admin/pedidos` | Listar pedidos (soporta paginación) | JWT ADMIN |
| PATCH  | `/api/admin/pedidos/{id}/estado` | Cambiar estado (`{ estado }`) | JWT ADMIN |

### Ejemplos

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@email.com","clave":"clave123"}'
# → {"data":{"token":"eyJ...","nombre":"Juan Pérez","rol":"CLIENTE","email":"juan@email.com"}}

# Listar productos paginados
curl "http://localhost:8080/api/productos?page=1&limit=5"
# → {"data":[ ... ],"meta":{"page":1,"limit":5,"total":12,"totalPages":3,"hasNextPage":true,"hasPrevPage":false}}

# Error de negocio
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nadie@x.com","clave":"mala"}'
# → 400 {"error":{"status":400,"message":"Usuario no encontrado"}}
```

---

## Arquitectura por capas

```
controller/    → REST: recibe/valida, envuelve en ApiResponse, mapea a DTO
service/       → Reglas de negocio (AuthService, ProductoService, OrderService, NotificationService)
repository/    → Persistencia JPA sobre SQL Server
entity/        → Modelo (Producto, Cliente, Pedido, ItemPedido)
dto/response/  → Contrato de salida (ApiResponse, PageMeta, Paginacion)
exception/     → GlobalExceptionHandler: traduce excepciones a { error: { ... } }
config/        → SecurityConfig (JWT+CORS), JwtUtil, InfraConfig (Redis/RabbitMQ), DataSeeder
```

| Patrón | Dónde | Para qué |
|--------|-------|----------|
| MVC / Capas | Proyecto completo | Separación de responsabilidades |
| Repository | JPA + SQL Server | Abstracción de persistencia |
| Cache-Aside | `ProductoService` | Stock en Redis → RNF-01 < 1 s |
| Producer-Consumer | `NotificationService` | Emails asíncronos con RabbitMQ |
| Degradación grácil | `ProductoService` / `NotificationService` | Si Redis o RabbitMQ caen, la tienda sigue operando |
| Facade / Transacción | `OrderService` | Coordina el flujo de compra ACID |
| DTO / Envelope | `Controllers` + `ApiResponse` | No expone entidades ni detalles internos |
