# FitStore — Frontend (React + Vite)

Interfaz web de la tienda **FitStore**. Está construida con **React 18** y **Vite 5**, y consume la API REST del backend.

---

## Estructura

```
frontend/
├── index.html                  ← Punto de entrada de Vite
├── vite.config.js              ← Configuración del build (base relativa)
├── src/
│   ├── main.jsx                ← Providers + render de React
│   ├── App.jsx                 ← Navegación y composición de vistas
│   ├── config.js               ← URL base de la API (VITE_API_BASE_URL)
│   ├── storage.js              ← Persistencia en localStorage
│   ├── api.js                  ← Capa HTTP: fetch + JWT + envelope + errores + 401
│   ├── context/
│   │   ├── SessionContext.jsx  ← Sesión (token + usuario)
│   │   ├── CartContext.jsx     ← Carrito con useReducer
│   │   └── ToastContext.jsx    ← Avisos flotantes
│   ├── hooks/
│   │   ├── useProductos.js     ← Catálogo, filtros y búsqueda
│   │   ├── usePedidos.js       ← Historial del cliente
│   │   └── useAdminData.js     ← Inventario y pedidos del admin
│   ├── components/             ← Navbar, tarjetas, carrito, modal de talla…
│   ├── views/                  ← Catálogo, login, registro, pedidos y admin
│   └── utils/                  ← Formato de precios, reducer y URLs de imágenes
├── public/img/                 ← Imágenes de los productos (se sirven tal cual)
├── css/style.css               ← Estilos
└── eslint.config.mjs           ← Reglas de lint (ESLint 9)
```

Las capas de infraestructura (`config.js`, `storage.js`, `api.js`) se mantienen separadas del resto de la aplicación. Ningún componente llama a `fetch` ni a `localStorage` directamente: usan `Api` y `Storage`.

---

## Configuración

La URL base de la API se lee de una variable de entorno de Vite:

```bash
# frontend/.env  (ignorado por Git)
VITE_API_BASE_URL=http://localhost:8080/api
```

Hay una plantilla versionada en `frontend/.env.example`. Si la variable no existe, el valor por defecto es `http://localhost:8080/api`.

---

## Ejecutar

El backend debe estar corriendo en `http://localhost:8080` (ver `backend/README.md`).

```bash
cd frontend
npm install        # primera vez
npm run dev        # desarrollo: http://localhost:5173
```

Para probar el build de producción:

```bash
npm run build      # genera frontend/dist/
npm run preview    # sirve el build: http://localhost:4173
```

`vite.config.js` usa `base: "./"`, de modo que `dist/` puede publicarse en la raíz de un dominio o en una subcarpeta (S3, servidor estático, etc.) sin cambiar código.

**Credenciales de prueba:**

| Rol | Email | Clave |
|-----|-------|-------|
| Cliente | `juan@email.com` | `clave123` |
| Admin | `admin@fitstore.com` | `admin123` |

---

## Estado y datos

- **Sesión** (`SessionContext`): guarda token y usuario en `localStorage`; expone `login`, `registrar` y `logout`.
- **Carrito** (`CartContext` + `useReducer`): acciones `add`, `change`, `remove` y `clear`. El estado se persiste en `localStorage` y se identifican las líneas por producto + talla.
- **Avisos** (`ToastContext`): mensajes de confirmación y error con la misma apariencia anterior.
- **Datos de la API** (`hooks/`): cada hook maneja carga, error y recarga. `useProductos` aplica filtro por categoría o búsqueda por nombre.

---

## Seguridad en la interfaz

- No se usa `innerHTML` ni `dangerouslySetInnerHTML`: React escapa todo el contenido.
- Los eventos se manejan con `onClick` y props/closures, sin atributos `onclick` con datos incrustados.
- La talla de la ropa se elige en un **modal** (no se usa `prompt()`).
- La imagen de cada producto se toma de `imagenUrl`, el valor guardado en la base de datos. Las rutas relativas se resuelven con la base de Vite.

---

## Análisis de código (ESLint)

```bash
cd frontend
npm run lint      # analiza
npm run lint:fix  # corrige lo automático
```

Reglas: recomendadas de `@eslint/js`, `react/jsx-uses-vars` y las de `eslint-plugin-react-hooks` (reglas de hooks y dependencias).

---

## Cómo usa la API (`api.js`)

`Api` expone `get`, `post`, `patch` y `del`, y devuelve directamente el `data` del envelope del backend:

```js
const productos = await Api.get("/productos");           // → array
const pedido    = await Api.post("/pedidos", { items });  // → objeto pedido
```

Responsabilidades centralizadas:

- **URL base** desde `config.js`.
- **Token JWT** `Authorization: Bearer …` (desde `storage.js`).
- **Envelope**: devuelve `json.data` si existe; si no, el JSON tal cual.
- **Errores**: lanza un `ApiError` con `status` y `details` a partir de `{ error: { status, message, details } }`.
- **401**: notifica mediante `Api.setOnUnauthorized(fn)` para cerrar sesión.

---

## Vistas

- **Catálogo** — `GET /api/productos` con filtros por categoría y buscador.
- **Carrito** — panel lateral; se vacía al confirmar la compra.
- **Mis pedidos** — `GET /api/pedidos/mis-pedidos`.
- **Admin** — `GET /api/admin/productos` y `/api/admin/pedidos`; actualiza stock y estados.
- **Login / Registro** — `POST /api/auth/login` y `/api/auth/register`; guardan el token y el usuario en `localStorage`.
