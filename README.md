# FitStore — Tienda Online de Productos de Gym
**Ingeniería de Software II · TdeA · Mayo 2026**

Stack: React.js · Spring Boot · MySQL · Redis · RabbitMQ · AWS

---

## Estructura del Proyecto

```
FitStore-COMPLETO/
├── backend/                  ← Spring Boot (Java 17)
│   ├── pom.xml               ← Dependencias Maven
│   └── src/main/java/com/fitstore/
│       ├── FitStoreApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java   ← JWT + CORS
│       │   ├── JwtUtil.java          ← Generación/validación tokens
│       │   ├── InfraConfig.java      ← Redis + RabbitMQ beans
│       │   └── DataSeeder.java       ← Datos iniciales automáticos
│       ├── controller/
│       │   └── Controllers.java      ← /api/auth, /api/productos, /api/pedidos, /api/admin
│       ├── entity/                   ← Producto, Cliente, Pedido, ItemPedido
│       ├── repository/               ← JPA Repositories
│       ├── service/
│       │   ├── AuthService.java      ← Login + Registro
│       │   ├── ProductoService.java  ← Cache-Aside con Redis (RNF-01)
│       │   ├── OrderService.java     ← Flujo transaccional de compra
│       │   └── NotificationService.java ← RabbitMQ Producer + Consumer
│       └── exception/               ← Manejo global de errores
├── frontend/                 ← HTML + CSS + JS vanilla
│   ├── index.html
│   ├── css/style.css
│   └── js/app.js             ← Consume la API REST real
├── docs/
│   └── setup-mysql.sql       ← Script de base de datos
├── docker-compose.yml        ← Redis + RabbitMQ con Docker
└── README.md
```

---

## Requisitos Previos

| Herramienta   | Versión mínima | Descarga |
|---------------|---------------|---------|
| Java JDK      | 17            | https://adoptium.net |
| Maven         | 3.9+          | https://maven.apache.org |
| MySQL         | 8.0+          | https://dev.mysql.com |
| Docker        | 20+           | https://docker.com (para Redis y RabbitMQ) |

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

## Paso 2 — Crear la base de datos MySQL

```bash
mysql -u root -p
```

```sql
-- En la consola MySQL:
CREATE DATABASE fitstore_db CHARACTER SET utf8mb4;
exit;
```

O ejecutar el archivo:
```bash
mysql -u root -p < docs/setup-mysql.sql
```

---

## Paso 3 — Configurar credenciales MySQL

Editar `backend/src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD_MYSQL
```

---

## Paso 4 — Compilar y ejecutar el backend

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Al iniciar, Spring Boot:
1. Crea las tablas automáticamente en MySQL
2. Inserta usuarios y productos de prueba (DataSeeder)
3. Se conecta a Redis y RabbitMQ

**API corriendo en:** http://localhost:8080

---

## Paso 5 — Abrir el frontend

Abrir `frontend/index.html` directamente en el navegador.

**Credenciales de prueba:**
- Cliente: `juan@email.com` / `clave123`
- Admin:   `admin@fitstore.com` / `admin123`

---

## Endpoints de la API

### Autenticación
| Método | Endpoint            | Descripción         | Auth |
|--------|--------------------|--------------------|------|
| POST   | /api/auth/login    | Iniciar sesión      | No   |
| POST   | /api/auth/register | Registrar usuario   | No   |

### Productos
| Método | Endpoint                        | Descripción                      | Auth |
|--------|---------------------------------|----------------------------------|------|
| GET    | /api/productos                  | Listar todos                     | No   |
| GET    | /api/productos?categoria=ROPA   | Filtrar por categoría             | No   |
| GET    | /api/productos?buscar=proteína  | Buscar por nombre                | No   |
| GET    | /api/productos/{id}/stock?cantidad=2 | Verificar stock (Redis) — RNF-01 | No   |

### Pedidos
| Método | Endpoint                  | Descripción               | Auth     |
|--------|--------------------------|---------------------------|----------|
| POST   | /api/pedidos              | Confirmar compra (ACID)   | JWT      |
| GET    | /api/pedidos/mis-pedidos  | Historial del cliente     | JWT      |

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
| Repository       | JPA + MySQL            | Abstracción de persistencia |
| **Cache-Aside**  | ProductoService.java   | Stock en Redis → RNF-01 < 1 seg |
| **Producer-Consumer** | NotificationService.java | Emails asíncronos con RabbitMQ |
| Facade           | OrderService.java      | Coordina todo el flujo de compra |
| DTO / Mapper     | Controllers.java       | No expone entidades directamente |

---

## Arquitectura AWS (Producción)

```
Internet
   │
[Route 53] → fitstore.com
   │
[CloudFront + S3]  ← Frontend (HTML/CSS/JS)
   │
[EC2 / ECS]        ← Spring Boot API (Auto Scaling)
   │
[RDS MySQL Multi-AZ]    ← Base de datos (99.5% uptime)
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
| RF-04  | Gestión de inventario (Admin)                 | ✅ AdminController |
| RNF-01 | Stock verificado en < 1 segundo               | ✅ Redis Cache-Aside |
| RNF-02 | Pagos cifrados HTTPS/SSL                      | ✅ Config AWS + Spring Security |
| RNF-03 | Control de acceso por roles                   | ✅ JWT + @PreAuthorize |
| RNF-04 | Backend desarrollado en Java                  | ✅ Spring Boot Java 17 |
