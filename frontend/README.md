# FitStore — Frontend

Interfaz web de la tienda **FitStore**, en **HTML, CSS y JavaScript vanilla** (sin frameworks ni build step). Consume la API REST del backend.

---

## Estructura

```
frontend/
├── index.html            ← Única página (contenedor de todas las vistas)
├── css/
│   └── style.css         ← Estilos
└── js/
    ├── config.js         ← URL base de la API (leída de <meta name="api-base-url">)
    ├── storage.js        ← Persistencia en localStorage (token, usuario, carrito)
    ├── api.js            ← Capa HTTP: fetch + JWT + envelope + errores + 401
    └── app.js            ← Vistas, estado de la interfaz y manejadores de eventos
```

Las capas se cargan en orden en `index.html` (config → storage → api → app) y se comunican a través del namespace global `window.FitStore`. `app.js` **no** llama a `fetch` ni a `localStorage` directamente: usa `Api` y `Store`.

---

## Configuración

La URL base de la API se define en el `<head>` de `index.html`, no en el código:

```html
<meta name="api-base-url" content="http://localhost:8080/api">
```

`config.js` la lee de ahí. Para apuntar a otro servidor (por ejemplo, uno desplegado), basta con cambiar el `content` de esa etiqueta. Si se omite, el valor por defecto es `http://localhost:8080/api`.

---

## Ejecutar

El backend debe estar corriendo en `http://localhost:8080` (ver `backend/README.md`).

**Opción A — abrir el archivo directamente**

Abre `frontend/index.html` en el navegador. El backend permite el origen `null` en CORS para este caso.

**Opción B — servidor estático** (recomendado)

```bash
# Con Python
python -m http.server 5500 --directory frontend

# o con Node
npx serve frontend -l 5500
```

Luego abre `http://localhost:5500` (origen `http://localhost:5500` ya está permitido en CORS).

**Credenciales de prueba:**

| Rol | Email | Clave |
|-----|-------|-------|
| Cliente | `juan@email.com` | `clave123` |
| Admin | `admin@fitstore.com` | `admin123` |

---

## Análisis de código (ESLint)

Análisis estático con **ESLint 9** (flat config) sobre el código de `js/`.

```bash
cd frontend
npm install     # primera vez (instala ESLint; node_modules/ está ignorado por Git)
npm run lint    # analiza
npm run lint:fix # corrige lo que sea automático
```

Reglas: las recomendadas de `@eslint/js` con los globals del navegador. Las funciones que se invocan desde atributos `onclick` en `index.html` se declaran con `/* exported … */` en `app.js`.

---

## Cómo usa la API (`api.js`)

`Api` expone `get`, `post`, `patch`, `del` y devuelve directamente el `data` del envelope del backend:

```js
const productos = await Api.get("/productos");          // → array (ya desenvuelto)
const pedido    = await Api.post("/pedidos", { items }); // → objeto pedido
```

Responsabilidades centralizadas:

- **URL base** desde `config.js`.
- **Token JWT** `Authorization: Bearer …` (desde `storage.js`).
- **Envelope**: devuelve `json.data` si existe; si no, el JSON tal cual.
- **Errores**: lanza un `Error` con `status` y `details` a partir de `{ error: { status, message, details } }`.
- **401**: notifica mediante `Api.setOnUnauthorized(fn)` para cerrar sesión.

---

## Flujo de vistas

`app.js` maneja una SPA de una sola página con secciones mostradas/ocultas (`mostrarVista`):

- **Catálogo** — `GET /api/productos` con filtros por categoría y buscador.
- **Carrito** — estado en `localStorage`; se vacía al confirmar.
- **Mis pedidos** — `GET /api/pedidos/mis-pedidos`.
- **Admin** — `GET /api/admin/productos` y `/api/admin/pedidos`; actualiza stock y estados.
- **Login** — `POST /api/auth/login`; guarda el token y el usuario en `localStorage`.
