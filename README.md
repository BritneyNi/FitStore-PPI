# FitStore — Tienda Online de Productos de Gym

[![CI](https://github.com/BritneyNi/FitStore-PPI/actions/workflows/ci.yml/badge.svg)](https://github.com/BritneyNi/FitStore-PPI/actions/workflows/ci.yml)

**Ingeniería de Software II · TdeA · Mayo 2026**

Stack: React 18 + Vite 5 · Spring Boot 3.5.7 · SQL Server · Redis · RabbitMQ · AWS

---

## Estructura del Proyecto

```
FitStore-PPI/
├── backend/                  ← Spring Boot (Java 25)
│   ├── pom.xml               ← Dependencias Maven
│   └── src/main/java/com/fitstore/
│       ├── FitStoreApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java   ← JWT + CORS
│       │   ├── JwtUtil.java          ← Generación/validación tokens
│       │   ├── InfraConfig.java      ← Redis + RabbitMQ beans
│       │   └── DataSeeder.java       ← Datos iniciales automáticos
│       ├── controller/              ← Un controlador por recurso (Auth, Producto, Pedido, Admin)
│       ├── entity/                  ← Producto, Cliente, Pedido, ItemPedido
│       ├── repository/              ← JPA Repositories
│       ├── service/
│       │   ├── AuthService.java     ← Login + Registro
│       │   ├── ProductoService.java ← Cache-Aside con Redis (RNF-01)
│       │   ├── OrderService.java    ← Flujo transaccional de compra
│       │   └── NotificationService.java ← RabbitMQ Producer + Consumer
│       └── exception/               ← Manejo global de errores
├── frontend/                 ← React 18 + Vite 5
│   ├── index.html            ← Punto de entrada de Vite
│   ├── src/
│   │   ├── config.js         ← URL base de la API (VITE_API_BASE_URL)
│   │   ├── storage.js        ← Persistencia en localStorage
│   │   ├── api.js            ← Capa HTTP: JWT + envelope + errores
│   │   ├── App.jsx           ← Vistas y composición general
│   │   ├── context/          ← Sesión, carrito (useReducer) y avisos
│   │   ├── hooks/            ← Carga de datos de la API
│   │   ├── components/       ← Navbar, tarjetas, carrito, modal de talla…
│   │   └── views/            ← Catálogo, login, registro, pedidos y admin
│   ├── public/img/           ← Imágenes de los productos
│   ├── css/style.css         ← Estilos (se conservan los de la versión anterior)
│   ├── eslint.config.mjs     ← Reglas de lint del frontend
│   └── README.md             ← Documentación de la capa frontend
├── docs/
│   ├── setup-sqlserver.sql   ← Script de base de datos (T-SQL)
│   ├── JUSTIFICACION_ARQUITECTURA.md ← Decisiones técnicas del proyecto
│   └── PULL_REQUEST.md       ← Resumen del cambio para la revisión
├── docker-compose.yml        ← Redis + RabbitMQ con Docker
├── start.ps1 / start.bat     ← Arranque completo con un clic (verifica todo)
├── .env.example              ← Plantilla de variables de entorno (sin secretos)
└── README.md
```

> Cada capa tiene su propia documentación: **[`backend/README.md`](backend/README.md)** (config, endpoints, envelope, errores) y **[`frontend/README.md`](frontend/README.md)** (capas, contexto, hooks, ESLint y flujo de vistas).

---

## Requisitos Previos

| Herramienta | Versión mínima | Descarga |
|-------------|---------------|---------|
| Java JDK    | 25 (LTS)      | https://adoptium.net |
| Maven       | Ninguna (repo incluye Maven Wrapper: `mvnw`/`mvnw.cmd`) | — |
| SQL Server  | 2022          | https://www.microsoft.com/sql-server |
| Docker      | 20+           | https://docker.com (para Redis y RabbitMQ) |

> El backend compila y se ejecuta con **Java 25 (LTS)** y **Spring Boot 3.5.7**. Maven no necesita instalarse: el repositorio incluye el **Maven Wrapper** (`backend/mvnw` en Linux/macOS, `backend/mvnw.cmd` en Windows) que descarga Maven 3.9.12 automáticamente en el primer uso.

---

## Arranque Rápido (recomendado para la demo)

Con un solo paso, `start.bat` verifica Docker Desktop, levanta Redis y RabbitMQ, comprueba SQL Server y arranca el backend:

```
start.bat        (Windows: doble clic)
```

o desde PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File start.ps1
```

Si prefieres hacerlo manualmente, sigue los Pasos 1–4.

---

## Paso 1 — Levantar Redis y RabbitMQ

```bash
# En la carpeta raíz del proyecto
docker-compose up -d

# Verificar que están corriendo
docker ps
```

**Panel RabbitMQ:** http://localhost:15672
Usuario: `guest` / Contraseña: `guest`

---

## Paso 2 — Crear la base de datos en SQL Server

Abrir **SSMS**, conectarse al servidor local (autenticación de Windows) y ejecutar el script versionado. La contraseña del login de aplicación **no está escrita en el repositorio**: se pasa como variable.

```sql
-- En SSMS: activar el modo SQLCMD y definir la variable antes de ejecutar
:setvar DB_PASSWORD "TuClaveLocal"
```

```bash
# O desde la terminal, con sqlcmd
sqlcmd -S localhost -E -i docs/setup-sqlserver.sql -v DB_PASSWORD="TuClaveLocal"
```

El script crea la base `fitstore_db`, el login `fitstore_user` y la columna `imagen_url` de los productos. Usa la misma clave en `application-local.properties` (Paso 3).

---

## Paso 3 — Configurar credenciales SQL Server y JWT

Las credenciales **no se versionan**. Copia la plantilla y edita tus valores:

```bash
copy backend\src\main\resources\application-local.properties.example backend\src\main\resources\application-local.properties
```

```properties
# backend/src/main/resources/application-local.properties  (ignorado por Git)
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contrasena
fitstore.jwt.secret=una-clave-secreta-de-al-menos-32-caracteres
```

Alternativamente, define `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET` como variables de entorno (hay una plantilla en `.env.example`). Si falta alguna, la aplicación **falla al arrancar** con un mensaje claro (*fail-fast*). El resto de valores por defecto están en `backend/README.md`.

---

## Paso 4 — Compilar y ejecutar el backend

En Windows usa `mvnw.cmd`; en Linux/macOS, `./mvnw`. Si tienes Maven global en PATH, `mvn` funciona igual.

```bash
cd backend

# Windows:
mvnw clean install -DskipTests
mvnw spring-boot:run

# Linux/macOS (o con Maven global):
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

> El proyecto requiere **JDK 25**. Asegúrate de que `JAVA_HOME` apunte a un JDK 25 antes de compilar o ejecutar los tests (ver `java.version` en `backend/pom.xml`). El script `start.ps1` busca y configura el JDK 25 automáticamente.

Al iniciar, Spring Boot:
1. Crea las tablas automáticamente en SQL Server
2. Inserta usuarios y productos de prueba (DataSeeder)
3. Se conecta a Redis y RabbitMQ

**API corriendo en:** http://localhost:8080

---

## Paso 5 — Ejecutar el frontend

El frontend usa **React 18 + Vite 5**. En otra terminal:

```bash
cd frontend
npm install          # primera vez
npm run dev          # servidor de desarrollo en http://localhost:5173
```

Para probar el build de producción:

```bash
npm run build        # genera frontend/dist/
npm run preview      # sirve el build en http://localhost:4173
```

La URL de la API se configura en `frontend/.env` (copia `frontend/.env.example`). Por defecto apunta a `http://localhost:8080/api`.

**Credenciales de prueba:**
- Cliente: `juan@email.com` / `clave123`
- Admin:   `admin@fitstore.com` / `admin123`

---

## Pruebas Automatizadas

```bash
cd backend
mvnw test        # Windows
./mvnw test      # Linux/macOS
```

Ejecuta:

- **ProductoServiceTest** — pruebas unitarias del servicio de productos y caché Redis.
- **CucumberRunner** — suite BDD (Cucumber + JUnit Platform) con los 15 escenarios de `backend/src/test/resources/features/compra.feature`: login con JWT, catálogo, filtros por categoría, búsqueda, carrito/localStorage, compra ACID, verificación de stock < 1 s (RNF-01), notificaciones asíncronas por RabbitMQ, estados de pedido (incluye restauración de stock al cancelar) y panel admin.

**Requisitos:** SQL Server con la BD creada, Redis y RabbitMQ arriba (`docker-compose up -d`).

> Los tests usan la BD real, pero se auto-limpian: al finalizar, `TestDataRestorer` vuelve a insertar el catálogo original de 12 productos, borra pedidos/ítems y vacía la caché de Redis.

---

## Calidad de Código (Lint y Formato)

| Capa | Herramienta | Comandos |
|------|-------------|----------|
| Backend (Java) | **Spotless** + palantir-java-format 2.80.0 | `mvnw.cmd spotless:apply` (formatea) · `mvnw.cmd spotless:check` (valida) |
| Frontend (React) | **ESLint 9** + **Vite 5** | `cd frontend && npm install && npm run lint && npm run build` |

> **Integración continua:** `.github/workflows/ci.yml` ejecuta Spotless, las 18 pruebas (con SQL Server, Redis y RabbitMQ en contenedores), ESLint y el build del frontend en cada `push` y `pull request` a `main`.

---

## Endpoints de la API

> **Formato de respuesta (envelope):** los resultados de éxito van dentro de `{ "data": …, "meta": { … } }` y los errores en `{ "error": { "status", "message", "details" } }`. Detalle completo en [`backend/README.md`](backend/README.md).

### Autenticación
| Método | Endpoint            | Descripción         | Auth |
|--------|--------------------|--------------------|------|
| POST   | /api/auth/login    | Iniciar sesión      | No   |
| POST   | /api/auth/register | Registrar usuario   | No   |

### Sistema
| Método | Endpoint      | Descripción                                          | Auth |
|--------|--------------|------------------------------------------------------|------|
| GET    | /api/health  | Estado de la API + BD, Redis y RabbitMQ (UP/DEGRADED) | No   |

### Productos
| Método | Endpoint                        | Descripción                      | Auth |
|--------|---------------------------------|----------------------------------|------|
| GET    | /api/productos                  | Listar todos                     | No   |
| GET    | /api/productos?categoria=ROPA   | Filtrar por categoría             | No   |
| GET    | /api/productos?buscar=proteína  | Buscar por nombre                | No   |
| GET    | /api/productos/{id}/stock?cantidad=2 | Verificar stock (Redis) — RNF-01 | No   |

### Pedidos
| Método | Endpoint                  | Descripción               | Auth |
|--------|--------------------------|---------------------------|------|
| POST   | /api/pedidos              | Confirmar compra (ACID)   | JWT  |
| GET    | /api/pedidos/mis-pedidos  | Historial del cliente     | JWT  |

### Admin
| Método | Endpoint                           | Descripción             | Auth      |
|--------|------------------------------------|------------------------|-----------|
| GET    | /api/admin/productos               | Listar inventario       | JWT ADMIN |
| PATCH  | /api/admin/productos/{id}/stock    | Actualizar stock        | JWT ADMIN |
| GET    | /api/admin/productos/stock-bajo    | Alertas stock bajo      | JWT ADMIN |
| PATCH  | /api/admin/pedidos/{id}/estado     | Cambiar estado pedido   | JWT ADMIN |

---

## Patrones de Diseño Implementados

| Patrón           | Dónde                  | Para qué |
|------------------|------------------------|----------|
| MVC / Capas      | Spring Boot completo   | Separación de responsabilidades |
| Repository       | JPA + SQL Server       | Abstracción de persistencia |
| **Cache-Aside**  | ProductoService.java   | Stock en Redis → RNF-01 < 1 seg |
| **Producer-Consumer** | NotificationService.java | Emails asíncronos con RabbitMQ |
| **Degradación grácil** | ProductoService / NotificationService | Si Redis o RabbitMQ caen, la tienda sigue operando (fallback a BD) |
| Facade           | OrderService.java      | Coordina todo el flujo de compra |
| DTO / Mapper     | `controller/` (un controlador por recurso) | No expone entidades directamente |

---

## Arquitectura AWS (Producción)

```
Internet
   │
[Route 53] → fitstore.com
   │
[CloudFront + S3]  ← Frontend (React/Vite)
   │
[EC2 / ECS]        ← Spring Boot API (Auto Scaling)
   │
[RDS SQL Server Multi-AZ]    ← Base de datos (99.5% uptime)
[ElastiCache Redis]     ← Caché de stock
[Amazon MQ RabbitMQ]   ← Cola de notificaciones
```

---

## Requisitos del Proyecto

| ID     | Descripción                                   | Implementado |
|--------|-----------------------------------------------|-------------|
| RF-01  | Registro e inicio de sesión con JWT           | ✅ AuthService + SecurityConfig |
| RF-02  | Buscar y filtrar productos por categoría      | ✅ ProductoController |
| RF-03  | Carrito de compras y pago en línea            | ✅ OrderService @Transactional |
| RF-04  | Gestión de inventario (Admin)                 | ✅ AdminProductoController + AdminPedidoController |
| RNF-01 | Stock verificado en < 1 segundo               | ✅ Redis Cache-Aside |
| RNF-02 | Pagos cifrados HTTPS/SSL                      | ✅ Config AWS + Spring Security |
| RNF-03 | Control de acceso por roles                   | ✅ JWT + @PreAuthorize |
| RNF-04 | Backend desarrollado en Java                  | ✅ Spring Boot Java 25 |
