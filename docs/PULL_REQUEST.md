# Pull Request — Correcciones de la revisión

## Título

```
refactor: migra el frontend a React, ordena controladores y corrige seguridad
```

## Descripción

Este PR reúne las correcciones solicitadas en la revisión del proyecto. El
objetivo es cumplir los requisitos de la asignatura sin cambiar la apariencia
visual de la tienda ni retirar componentes de infraestructura (Redis,
RabbitMQ, Spotless, Cucumber y CI).

### Qué cambia

- **Frontend React + Vite.** La interfaz pasa de HTML/JS vanilla a React 18 con
  Vite 5. La sesión, el carrito y los avisos viven en Context API; el carrito
  usa `useReducer` (`add`, `change`, `remove`, `clear`). La carga de datos está
  en hooks (`useProductos`, `usePedidos`, `useAdminData`).
- **Capas conservadas.** `config.js`, `storage.js` y `api.js` siguen separados
  del resto de la aplicación; ningún componente usa `fetch` o `localStorage`
  directamente.
- **Lockfile reproducible.** `frontend/package-lock.json` se regeneró con el
  registro oficial (`registry.npmjs.org`); ya no contiene referencias al mirror
  `registry.npmmirror.com`, así que `npm ci` no depende de una red alternativa.
- **Talla sin `prompt()`.** Los productos de ropa abren un modal para elegir
  talla (o continuar sin talla).
- **Imagen desde la base de datos.** Se agrega `imagen_url` a la tabla de
  productos, `imagenUrl` a la entidad y al DTO, y el frontend usa ese valor.
  Si el campo está vacío se muestra el emoji del producto.
- **Seguridad.** No se usa `innerHTML` ni `dangerouslySetInnerHTML`; no quedan
  `onclick` con datos incrustados. Se elimina `null` de los orígenes CORS.
- **Sin secretos versionados.** La contraseña de la base de datos sale del
  README y del script SQL. En local se usan variables de entorno o
  `application-local.properties` (ignorado); en CI se usan
  `secrets.DB_PASSWORD` y `secrets.JWT_SECRET`.
- **Controladores por recurso.** `Controllers.java` se divide en
  `AuthController`, `ProductoController`, `PedidoController`,
  `AdminProductoController` y `AdminPedidoController`, sin cambiar rutas ni
  comportamiento.
- **Documentación.** Se agrega `docs/JUSTIFICACION_ARQUITECTURA.md` y se
  actualizan `README.md`, `backend/README.md` y `frontend/README.md`.
- **Higiene del repositorio.** Se deja de versionar `.vscode/settings.json`,
  se ignoran `.env*` (salvo `.env.example`) y `dist/`, y se versionan las
  plantillas de variables de entorno.

### Cómo probar

```bash
# Backend (requiere SQL Server, Redis y RabbitMQ)
cd backend
./mvnw spotless:check
./mvnw verify

# Frontend
cd frontend
npm ci
npm run lint
npm run build
npm run dev     # http://localhost:5173
```

Para la verificación manual: catálogo con imágenes, filtros y búsqueda; agregar
ropa (modal de talla) y otros productos; cambiar cantidades y confirmar compra;
login/registro; "Mis pedidos"; panel de administrador (stock y estado); cierre
de sesión por 401.

### Verificación realizada

- Frontend: `npm ci`, `npm run lint` y `npm run build` correctos.
- `frontend/package-lock.json` sin referencias a `registry.npmmirror.com`
  (310 entradas resueltas contra `registry.npmjs.org`).
- Backend: `./mvnw -B -DskipTests compile` y `./mvnw -B spotless:check`
  correctos; `./mvnw -B verify` con SQL Server, Redis y RabbitMQ activos:
  **18 pruebas, 0 fallos**.
- CI revisada: ejecuta `npm ci`, `npm run lint` y `npm run build` del
  frontend, y usa `secrets.DB_PASSWORD` y `secrets.JWT_SECRET` sin credenciales
  en claro.

### Checklist de las 14 observaciones

- [x] 1. Migrar el frontend a React + Vite.
- [x] 2. Manejar sesión y carrito con Context API.
- [x] 3. Usar `useReducer` para las acciones del carrito.
- [x] 4. Encapsular las llamadas a la API en hooks personalizados.
- [x] 5. Conservar las capas `config.js`, `storage.js` y `api.js`.
- [x] 6. Reemplazar el `prompt()` de talla por un selector/modal.
- [x] 7. Guardar y leer la URL de la imagen del producto en la base de datos.
- [x] 8. Eliminar `innerHTML`/`dangerouslySetInnerHTML` y auditar el proyecto.
- [x] 9. Quitar los `onclick` con datos incrustados (props/closures).
- [x] 10. Eliminar el origen `null` de CORS en el backend.
- [x] 11. Sacar la contraseña de la BD del README y del SQL; usar env vars,
      `.env.example` y secretos de GitHub.
- [x] 12. Dividir `Controllers.java` en un controlador por recurso.
- [x] 13. Documentar Redis, RabbitMQ, Spotless, Cucumber y CI en
      `docs/JUSTIFICACION_ARQUITECTURA.md` sin retirar componentes.
- [x] 14. Dejar de versionar `.vscode/settings.json`, actualizar `.gitignore`
      y documentar la configuración de correo personal.

### Pendientes manuales

- Configurar en GitHub los secretos `DB_PASSWORD` y `JWT_SECRET` antes de
  habilitar el workflow.
- Definir el correo personal de los commits con
  `git config user.email "MI_CORREO_PERSONAL"` (no se modificó la
  configuración global).
- Subir la rama `feature/apoyo-frontend` y abrir el PR contra `main`; no
  fusionarlo todavía.
- Rotar la contraseña antigua de la base de datos que quedó en el historial
  previo de Git; el historial no se reescribió.
