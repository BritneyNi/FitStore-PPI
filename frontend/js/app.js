// ============================================================
//  FitStore — app.js (integrado con API Spring Boot)
//  Base URL: http://localhost:8080/api
// ============================================================

const API = "http://localhost:8080/api";

// ── Estado global ────────────────────────────────────────────
let token          = localStorage.getItem("fitstore_token") || null;
let usuarioActual  = JSON.parse(localStorage.getItem("fitstore_user") || "null");
let carrito        = JSON.parse(localStorage.getItem("fitstore_carrito") || "[]");

// ── Helpers HTTP ─────────────────────────────────────────────
async function get(path) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = "Bearer " + token;
  const res = await fetch(API + path, { headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || "Error " + res.status);
  }
  return res.json();
}

async function post(path, body) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = "Bearer " + token;
  const res = await fetch(API + path, {
    method: "POST",
    headers,
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || "Error " + res.status);
  }
  return res.json();
}

async function patch(path, body) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = "Bearer " + token;
  const res = await fetch(API + path, {
    method: "PATCH",
    headers,
    body: JSON.stringify(body)
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || "Error " + res.status);
  }
  return res.json();
}

// ── Inicialización ───────────────────────────────────────────
document.addEventListener("DOMContentLoaded", async () => {
  if (usuarioActual) aplicarSesion(usuarioActual);
  actualizarContadorCarrito();
  await cargarCatalogo();
});

// ── Navegación ───────────────────────────────────────────────
function mostrarVista(nombre) {
  document.querySelectorAll(".vista").forEach(v => v.style.display = "none");
  const vista = document.getElementById("vista-" + nombre);
  if (vista) vista.style.display = "block";

  document.querySelectorAll(".nav-btn").forEach(b => b.classList.remove("active"));
  if (nombre === "catalogo") document.querySelector(".nav-btn").classList.add("active");

  if (nombre === "mis-pedidos") cargarPedidos();
  if (nombre === "admin")       cargarAdmin();
  if (nombre === "login" && usuarioActual) mostrarVista("catalogo");
}

// ── Auth ─────────────────────────────────────────────────────
async function login() {
  const email = document.getElementById("loginEmail").value.trim();
  const clave = document.getElementById("loginClave").value.trim();
  const errEl = document.getElementById("loginError");
  errEl.textContent = "";

  try {
    const data = await post("/auth/login", { email, clave });
    token = data.token;
    usuarioActual = { nombre: data.nombre, email: data.email, rol: data.rol };
    localStorage.setItem("fitstore_token", token);
    localStorage.setItem("fitstore_user", JSON.stringify(usuarioActual));
    aplicarSesion(usuarioActual);
    mostrarVista("catalogo");
    mostrarToast(`¡Bienvenido, ${data.nombre}! 👋`);
  } catch (e) {
    errEl.textContent = e.message;
  }
}

function aplicarSesion(usuario) {
  document.getElementById("userInfo").style.display  = "flex";
  document.getElementById("userName").textContent    = usuario.nombre;
  document.getElementById("btnLogin").style.display  = "none";
  document.getElementById("btnAdmin").style.display  = usuario.rol === "ADMIN" ? "inline-block" : "none";
}

function logout() {
  token = null; usuarioActual = null; carrito = [];
  localStorage.removeItem("fitstore_token");
  localStorage.removeItem("fitstore_user");
  localStorage.removeItem("fitstore_carrito");
  document.getElementById("userInfo").style.display  = "none";
  document.getElementById("btnLogin").style.display  = "inline-block";
  document.getElementById("btnAdmin").style.display  = "none";
  actualizarContadorCarrito();
  mostrarVista("catalogo");
  mostrarToast("Sesión cerrada.");
}

// ── Catálogo (consume GET /api/productos) ────────────────────
async function cargarCatalogo(categoria, buscar) {
  const grid = document.getElementById("gridProductos");
  grid.innerHTML = `<p class="empty-msg">Cargando productos...</p>`;

  try {
    let path = "/productos";
    if (categoria && categoria !== "todos") path += "?categoria=" + categoria;
    else if (buscar) path += "?buscar=" + encodeURIComponent(buscar);

    const productos = await get(path);
    renderCatalogo(productos);
  } catch (e) {
    grid.innerHTML = `<p class="empty-msg">⚠️ No se pudo conectar al servidor. ¿Está corriendo Spring Boot?</p>`;
  }
}

function renderCatalogo(lista) {
  const grid = document.getElementById("gridProductos");
  if (!lista.length) {
    grid.innerHTML = `<p class="empty-msg">No se encontraron productos.</p>`;
    return;
  }
  grid.innerHTML = lista.map(p => `
    <div class="producto-card ${p.stock === 0 ? 'sin-stock' : ''}">
      <div class="producto-emoji">${p.emoji || '📦'}</div>
      <div class="producto-info">
        <span class="producto-categoria">${p.categoria}</span>
        <h3 class="producto-nombre">${p.nombre}</h3>
        <p class="producto-desc">${p.descripcion}</p>
        <div class="producto-footer">
          <span class="producto-precio">$${formatPrecio(p.precio)}</span>
          <span class="stock-badge ${p.stock === 0 ? 'sin-stock' : p.stock <= 5 ? 'poco-stock' : 'en-stock'}">
            ${p.stock === 0 ? 'Agotado' : p.stock <= 5 ? `¡Solo ${p.stock}!` : `Stock: ${p.stock}`}
          </span>
        </div>
        <button class="btn-agregar" onclick="agregarAlCarrito(${p.id},'${p.nombre}',${p.precio},${p.stock},'${p.emoji || '📦'}')"
          ${p.stock === 0 ? 'disabled' : ''}>
          ${p.stock === 0 ? 'Agotado' : '+ Agregar al carrito'}
        </button>
      </div>
    </div>
  `).join("");
}

function filtrar(categoria, btn) {
  document.querySelectorAll(".filtro-btn").forEach(b => b.classList.remove("active"));
  btn.classList.add("active");
  cargarCatalogo(categoria);
}

function buscar() {
  const termino = document.getElementById("buscador").value.trim();
  cargarCatalogo(null, termino);
}

// ── Carrito ──────────────────────────────────────────────────
function agregarAlCarrito(id, nombre, precio, stock, emoji) {
  if (!usuarioActual) {
    mostrarToast("⚠️ Debes iniciar sesión para comprar.");
    mostrarVista("login");
    return;
  }
  const existente = carrito.find(i => i.id === id);
  if (existente) {
    if (existente.cantidad >= stock) { mostrarToast("⚠️ No hay más stock."); return; }
    existente.cantidad++;
  } else {
    carrito.push({ id, nombre, precio, stock, emoji, cantidad: 1 });
  }
  localStorage.setItem("fitstore_carrito", JSON.stringify(carrito));
  actualizarContadorCarrito();
  mostrarToast(`✅ ${nombre} agregado al carrito.`);
}

function toggleCarrito() {
  const panel   = document.getElementById("carritoPanel");
  const overlay = document.getElementById("overlay");
  const visible = panel.style.display !== "none";
  panel.style.display   = visible ? "none" : "block";
  overlay.style.display = visible ? "none" : "block";
  if (!visible) renderCarrito();
}

function renderCarrito() {
  const cont   = document.getElementById("carritoItems");
  const totalEl = document.getElementById("carritoTotal");
  const btn    = document.getElementById("btnComprar");

  if (!carrito.length) {
    cont.innerHTML = `<p class="empty-msg">Tu carrito está vacío.</p>`;
    totalEl.textContent = "$0"; btn.disabled = true; return;
  }
  cont.innerHTML = carrito.map(i => `
    <div class="carrito-item">
      <span class="carrito-emoji">${i.emoji}</span>
      <div class="carrito-item-info">
        <span class="carrito-item-nombre">${i.nombre}</span>
        <span class="carrito-item-precio">$${formatPrecio(i.precio)}</span>
      </div>
      <div class="carrito-item-controles">
        <button onclick="cambiarCantidad(${i.id},-1)">−</button>
        <span>${i.cantidad}</span>
        <button onclick="cambiarCantidad(${i.id},1)">+</button>
        <button class="btn-eliminar" onclick="eliminarDelCarrito(${i.id})">🗑</button>
      </div>
    </div>
  `).join("");
  const total = carrito.reduce((s, i) => s + i.precio * i.cantidad, 0);
  totalEl.textContent = "$" + formatPrecio(total);
  btn.disabled = false;
}

function cambiarCantidad(id, delta) {
  const item = carrito.find(i => i.id === id);
  if (!item) return;
  item.cantidad += delta;
  if (item.cantidad <= 0) eliminarDelCarrito(id);
  else if (item.cantidad > item.stock) item.cantidad = item.stock;
  else { localStorage.setItem("fitstore_carrito", JSON.stringify(carrito)); actualizarContadorCarrito(); renderCarrito(); }
}

function eliminarDelCarrito(id) {
  carrito = carrito.filter(i => i.id !== id);
  localStorage.setItem("fitstore_carrito", JSON.stringify(carrito));
  actualizarContadorCarrito(); renderCarrito();
}

function actualizarContadorCarrito() {
  document.getElementById("carritoCount").textContent = carrito.reduce((s, i) => s + i.cantidad, 0);
}

// ── Confirmar compra (POST /api/pedidos) ─────────────────────
async function confirmarCompra() {
  if (!usuarioActual || !carrito.length) return;
  const btn = document.getElementById("btnComprar");
  btn.disabled = true; btn.textContent = "Procesando...";

  try {
    const items = carrito.map(i => ({ productoId: i.id, cantidad: i.cantidad }));
    const pedido = await post("/pedidos", { items });

    carrito = [];
    localStorage.removeItem("fitstore_carrito");
    actualizarContadorCarrito();
    toggleCarrito();
    await cargarCatalogo(); // Refresca stock real del servidor
    mostrarToast(`🎉 ¡Pedido #${pedido.id} confirmado! Email enviado.`);
  } catch (e) {
    mostrarToast("❌ " + e.message);
    btn.disabled = false;
  }
  btn.textContent = "Confirmar compra";
}

// ── Mis Pedidos (GET /api/pedidos/mis-pedidos) ───────────────
async function cargarPedidos() {
  const cont = document.getElementById("listaPedidos");
  cont.innerHTML = `<p class="empty-msg">Cargando pedidos...</p>`;

  if (!usuarioActual) {
    cont.innerHTML = `<p class="empty-msg">Debes iniciar sesión.</p>`; return;
  }
  try {
    const pedidos = await get("/pedidos/mis-pedidos");
    if (!pedidos.length) { cont.innerHTML = `<p class="empty-msg">Aún no tienes pedidos.</p>`; return; }
    cont.innerHTML = pedidos.map(p => `
      <div class="pedido-card">
        <div class="pedido-header">
          <span class="pedido-id">#${p.id}</span>
          <span class="pedido-fecha">${p.fecha?.split("T")[0] || ""}</span>
          <span class="pedido-estado estado-${p.estado?.toLowerCase().replace("_","-")}">${p.estado}</span>
        </div>
        <div class="pedido-items">
          ${(p.items || []).map(i => `
            <div class="pedido-item-row">
              <span>${i.producto?.nombre || i.nombreProducto}</span>
              <span>x${i.cantidad}</span>
              <span>$${formatPrecio(i.precioUnitario * i.cantidad)}</span>
            </div>
          `).join("")}
        </div>
        <div class="pedido-total">Total: <strong>$${formatPrecio(p.total)}</strong></div>
      </div>
    `).join("");
  } catch (e) {
    cont.innerHTML = `<p class="empty-msg">⚠️ Error cargando pedidos.</p>`;
  }
}

// ── Panel Admin ──────────────────────────────────────────────
async function cargarAdmin() {
  const cont = document.getElementById("tablaAdmin");
  if (!usuarioActual || usuarioActual.rol !== "ADMIN") {
    cont.innerHTML = `<p class="empty-msg">Acceso denegado.</p>`; return;
  }
  try {
    const [productos, pedidos] = await Promise.all([
      get("/admin/productos"),
      get("/admin/pedidos")
    ]);

    cont.innerHTML = `
      <div class="admin-section">
        <h3 class="admin-subtitle">📦 Gestión de Inventario</h3>
        <table class="admin-tabla">
          <thead><tr><th>Producto</th><th>Categoría</th><th>Precio</th><th>Stock</th><th>Estado</th><th>Acción</th></tr></thead>
          <tbody>
            ${productos.map(p => `
              <tr class="${p.stock === 0 ? 'fila-agotado' : p.stock <= 5 ? 'fila-alerta' : ''}">
                <td>${p.emoji || '📦'} ${p.nombre}</td>
                <td>${p.categoria}</td>
                <td>$${formatPrecio(p.precio)}</td>
                <td><input type="number" class="input-stock" value="${p.stock}" min="0" id="stock-${p.id}"></td>
                <td><span class="stock-badge ${p.stock === 0 ? 'sin-stock' : p.stock <= 5 ? 'poco-stock' : 'en-stock'}">
                  ${p.stock === 0 ? 'Agotado' : p.stock <= 5 ? 'Bajo stock' : 'OK'}
                </span></td>
                <td><button class="btn-guardar" onclick="actualizarStock(${p.id})">Guardar</button></td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>

      <div class="admin-section">
        <h3 class="admin-subtitle">📋 Gestión de Pedidos</h3>
        <table class="admin-tabla">
          <thead><tr><th>Pedido ID</th><th>Cliente</th><th>Total</th><th>Estado</th><th>Fecha</th><th>Acción</th></tr></thead>
          <tbody>
            ${(pedidos || []).map(p => `
              <tr>
                <td><strong>#${p.id}</strong></td>
                <td>${p.cliente?.nombre || p.clienteNombre || 'N/A'}</td>
                <td>$${formatPrecio(p.total)}</td>
                <td><span class="pedido-estado estado-${p.estado?.toLowerCase().replace("_","-")}">${p.estado}</span></td>
                <td>${p.fecha?.split("T")[0] || ""}</td>
                <td>
                  <select class="select-estado" id="estado-${p.id}">
                    <option value="CONFIRMADO" ${p.estado === 'CONFIRMADO' ? 'selected' : ''}>Confirmado</option>
                    <option value="EN_CAMINO" ${p.estado === 'EN_CAMINO' ? 'selected' : ''}>En camino</option>
                    <option value="ENTREGADO" ${p.estado === 'ENTREGADO' ? 'selected' : ''}>Entregado</option>
                    <option value="CANCELADO" ${p.estado === 'CANCELADO' ? 'selected' : ''}>Cancelado</option>
                  </select>
                  <button class="btn-guardar" onclick="actualizarEstadoPedido(${p.id})">Actualizar</button>
                </td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    `;
  } catch (e) {
    cont.innerHTML = `<p class="empty-msg">⚠️ Error cargando admin: ${e.message}</p>`;
  }
}

async function actualizarStock(id) {
  const val = parseInt(document.getElementById("stock-" + id).value);
  if (isNaN(val) || val < 0) { mostrarToast("⚠️ Stock inválido."); return; }
  try {
    await patch("/admin/productos/" + id + "/stock", { stock: val });
    mostrarToast("✅ Stock actualizado.");
    cargarAdmin();
  } catch (e) {
    mostrarToast("❌ " + e.message);
  }
}

async function actualizarEstadoPedido(id) {
  const estado = document.getElementById("estado-" + id).value;
  try {
    await patch("/admin/pedidos/" + id + "/estado", { estado });
    mostrarToast(`✅ Pedido #${id} actualizado a ${estado}.`);
    cargarAdmin();
  } catch (e) {
    mostrarToast("❌ " + e.message);
  }
}

// ── Toast ─────────────────────────────────────────────────────
function mostrarToast(msg) {
  const t = document.getElementById("toast");
  t.textContent = msg; t.classList.add("show");
  setTimeout(() => t.classList.remove("show"), 3000);
}

// ── Utilidades ────────────────────────────────────────────────
function formatPrecio(v) {
  return Number(v).toLocaleString("es-CO");
}
