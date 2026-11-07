# Justificación de la arquitectura — FitStore

Este documento explica por qué el proyecto mantiene las piezas de infraestructura y las herramientas de calidad que se usan en la asignatura, y cómo encajan con los cambios recientes del frontend y del backend.

## 1. Frontend con React + Vite

La interfaz se migró de HTML/JS vanilla a **React 18** con **Vite 5** para separar la interfaz en componentes reutilizables y evitar el manejo manual del DOM.

- **Context API** concentra el estado compartido: la sesión (`SessionContext`), el carrito (`CartContext`) y los avisos (`ToastContext`). Ningún componente lee o escribe `localStorage` por su cuenta.
- **`useReducer`** gobierna el carrito con acciones explícitas: agregar, cambiar cantidad, eliminar y vaciar. Así las transiciones son predecibles y fáciles de probar.
- **Hooks personalizados** (`useProductos`, `usePedidos`, `useAdminData`) encapsulan la carga de datos, los estados de carga/error y la recarga después de una compra o de una edición del administrador.
- **Capas conservadas**: `config.js` (URL de la API), `storage.js` (persistencia) y `api.js` (HTTP, JWT, envelope y errores) siguen separadas de la vista.
- **Sin cambios visuales**: se conservaron las clases, colores, tipografías y espaciados de `css/style.css`. El único elemento nuevo es el modal para elegir talla, que reemplaza al `prompt()`.

Vite se eligió por su arranque rápido, su soporte nativo de JSX y porque el build se puede publicar en cualquier ruta con `base: "./"`.

## 2. Redis — caché de stock (RNF-01)

Redis se mantiene porque el requisito no funcional exige verificar el stock en menos de un segundo. `ProductoService` aplica el patrón **Cache-Aside**:

1. Consulta la llave `stock:{id}` en Redis.
2. Si no existe, lee la base de datos y guarda el valor con TTL de 5 minutos.
3. Al confirmar una compra o cambiar el stock, invalida la llave para no servir datos viejos.

La aplicación tolera que Redis esté caído: cae a la base de datos y registra el aviso, sin bloquear la tienda (degradación grácil).

## 3. RabbitMQ — notificaciones asíncronas

RabbitMQ se mantiene porque el checkout no debe esperar al envío del correo. `OrderService` publica un evento cuando el pedido queda confirmado y `NotificationService` lo consume con el patrón **Producer-Consumer**.

- Si RabbitMQ está disponible, el correo se procesa en segundo plano.
- Si no está disponible, la compra **no se revierte**: se registra el error y el pedido queda confirmado.
- El panel de administración de RabbitMQ (`http://localhost:15672`) permite ver las colas durante la demostración.

## 4. Spotless — formato uniforme del backend

Spotless con **palantir-java-format** evita discusiones de estilo en los pull requests y mantiene el código Java legible. Se ejecuta en dos modos:

```bash
cd backend
mvnw.cmd spotless:apply   # formatea
mvnw.cmd spotless:check   # valida (lo usa la CI)
```

No cambia la lógica del proyecto; solo normaliza imports, espacios y saltos de línea.

## 5. Cucumber — pruebas de comportamiento

Cucumber se mantiene porque los escenarios en español (`compra.feature`) documentan el flujo completo del negocio y sirven como prueba de regresión:

- login con JWT y control de roles;
- catálogo, filtros y búsqueda;
- compra ACID con descuento de stock;
- verificación de stock con Redis (RNF-01);
- notificación asíncrona con RabbitMQ;
- cambios de estado y restauración de stock al cancelar;
- panel de administración.

Los pasos usan `MockMvc` contra la aplicación real y `TestDataRestorer` deja la base y la caché limpias al terminar.

## 6. Integración continua (GitHub Actions)

El flujo `.github/workflows/ci.yml` se ejecuta en cada `push` y `pull request` a `main`:

1. **Backend**: levanta SQL Server, Redis y RabbitMQ como servicios; espera a SQL Server; ejecuta `spotless:check` y `mvnw verify` (pruebas unitarias + Cucumber).
2. **Frontend**: instala dependencias con `npm ci`, ejecuta ESLint y compila con Vite.

Las credenciales **no están escritas en el repositorio**. El flujo usa `secrets.DB_PASSWORD` para SQL Server y `secrets.JWT_SECRET` para firmar los tokens. En local se usan variables de entorno o `application-local.properties` (ignorado por Git).

## 7. Separación de controladores por recurso

`Controllers.java` se dividió en un controlador por recurso, sin cambiar rutas ni comportamiento:

- `AuthController` — `/api/auth`
- `ProductoController` — `/api/productos`
- `PedidoController` — `/api/pedidos`
- `AdminProductoController` — `/api/admin/productos`
- `AdminPedidoController` — `/api/admin/pedidos`
- `HealthController` — `/api/health` (ya existía)

Cada archivo tiene una responsabilidad clara, es más fácil de leer y de probar, y los tests de Cucumber siguen apuntando a las mismas rutas.

## 8. Imagen del producto en la base de datos

La imagen ya no se deduce del nombre en el frontend. La tabla `productos` tiene la columna `imagen_url`, la entidad `Producto` expone `imagenUrl` y la API devuelve ese campo. El frontend solo construye la ruta final con la base de Vite y, si no hay imagen, muestra el emoji del producto.

## 9. Decisiones de seguridad

- Se eliminó `innerHTML` y no se usa `dangerouslySetInnerHTML`; React escapa el contenido.
- Se eliminaron los `onclick` con datos incrustados; ahora se usan props y closures.
- El modal de talla reemplaza al `prompt()`.
- Se quitó `null` de los orígenes CORS y se dejaron solo los orígenes locales de Vite y el puerto de preview.
- La contraseña de la base de datos salió del README y del script SQL; se pasa como variable (`$(DB_PASSWORD)`) y en CI como secreto.

## 10. Qué no se eliminó y por qué

No se retiró Redis, RabbitMQ, Spotless, Cucumber ni la integración continua. Cada componente cumple un requisito del proyecto (RNF-01, notificaciones asíncronas, calidad de código, pruebas BDD y validación automática). La migración del frontend y la división de controladores son cambios de organización, no una reducción del alcance técnico.
