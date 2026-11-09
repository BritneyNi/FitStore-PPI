import { useEffect, useState } from "react";
import Api from "../api";
import { useToast } from "../context/ToastContext";
import { useAdminData } from "../hooks/useAdminData";
import { claseEstado, formatPrecio } from "../utils/format";

const ESTADOS = [
  { valor: "CONFIRMADO", texto: "Confirmado" },
  { valor: "EN_CAMINO", texto: "En camino" },
  { valor: "ENTREGADO", texto: "Entregado" },
  { valor: "CANCELADO", texto: "Cancelado" },
];

export default function AdminView() {
  const { productos, pedidos, cargando, error, recargar } = useAdminData(true);
  const { mostrarToast } = useToast();
  const [stockEdicion, setStockEdicion] = useState({});
  const [estadoEdicion, setEstadoEdicion] = useState({});

  useEffect(() => {
    setStockEdicion(Object.fromEntries(productos.map((producto) => [producto.id, producto.stock])));
  }, [productos]);

  useEffect(() => {
    setEstadoEdicion(Object.fromEntries(pedidos.map((pedido) => [pedido.id, pedido.estado])));
  }, [pedidos]);

  async function guardarStock(id) {
    const valor = Number.parseInt(stockEdicion[id], 10);
    if (Number.isNaN(valor) || valor < 0) {
      mostrarToast("⚠️ Stock inválido.");
      return;
    }

    try {
      await Api.patch(`/admin/productos/${id}/stock`, { stock: valor });
      mostrarToast("✅ Stock actualizado.");
      recargar();
    } catch (e) {
      mostrarToast("❌ " + e.message);
    }
  }

  async function guardarEstado(id) {
    const estado = estadoEdicion[id];
    try {
      await Api.patch(`/admin/pedidos/${id}/estado`, { estado });
      mostrarToast(`✅ Pedido #${id} actualizado a ${estado}.`);
      recargar();
    } catch (e) {
      mostrarToast("❌ " + e.message);
    }
  }

  async function eliminarPedido(id) {
    if (!window.confirm(`¿Eliminar el pedido #${id}? Esta acción no se puede deshacer.`)) return;

    try {
      await Api.del("/admin/pedidos/" + id);
      mostrarToast(`🗑 Pedido #${id} eliminado.`);
      recargar();
    } catch (e) {
      mostrarToast("❌ " + e.message);
    }
  }

  return (
    <div id="vista-admin" className="vista">
      <div className="section-header">
        <h2>Panel de Administrador</h2>
      </div>

      <div id="tablaAdmin">
        {error ? (
          <p className="empty-msg">{error}</p>
        ) : cargando && productos.length === 0 && pedidos.length === 0 ? (
          <p className="empty-msg">Cargando...</p>
        ) : (
          <>
            <div className="admin-section">
              <h3 className="admin-subtitle">📦 Gestión de Inventario</h3>
              <table className="admin-tabla">
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th>Categoría</th>
                    <th>Precio</th>
                    <th>Stock</th>
                    <th>Estado</th>
                    <th>Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {productos.map((producto) => (
                    <tr
                      key={producto.id}
                      className={
                        producto.stock === 0 ? "fila-agotado" : producto.stock <= 5 ? "fila-alerta" : ""
                      }
                    >
                      <td>
                        {producto.emoji || "📦"} {producto.nombre}
                      </td>
                      <td>{producto.categoria}</td>
                      <td>${formatPrecio(producto.precio)}</td>
                      <td>
                        <input
                          type="number"
                          className="input-stock"
                          value={stockEdicion[producto.id] ?? producto.stock}
                          min="0"
                          id={`stock-${producto.id}`}
                          onChange={(evento) =>
                            setStockEdicion((anterior) => ({
                              ...anterior,
                              [producto.id]: evento.target.value,
                            }))
                          }
                        />
                      </td>
                      <td>
                        <span
                          className={`stock-badge ${
                            producto.stock === 0
                              ? "sin-stock"
                              : producto.stock <= 5
                                ? "poco-stock"
                                : "en-stock"
                          }`}
                        >
                          {producto.stock === 0 ? "Agotado" : producto.stock <= 5 ? "Bajo stock" : "OK"}
                        </span>
                      </td>
                      <td>
                        <button type="button" className="btn-guardar" onClick={() => guardarStock(producto.id)}>
                          Guardar
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="admin-section">
              <h3 className="admin-subtitle">📋 Gestión de Pedidos</h3>
              <table className="admin-tabla">
                <thead>
                  <tr>
                    <th>Pedido ID</th>
                    <th>Cliente</th>
                    <th>Total</th>
                    <th>Estado</th>
                    <th>Fecha</th>
                    <th>Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {pedidos.map((pedido) => (
                    <tr key={pedido.id}>
                      <td>
                        <strong>#{pedido.id}</strong>
                      </td>
                      <td>{pedido.cliente?.nombre || pedido.clienteNombre || "N/A"}</td>
                      <td>${formatPrecio(pedido.total)}</td>
                      <td>
                        <span className={`pedido-estado estado-${claseEstado(pedido.estado)}`}>
                          {pedido.estado}
                        </span>
                      </td>
                      <td>{pedido.fecha?.split("T")[0] || ""}</td>
                      <td>
                        <select
                          className="select-estado"
                          id={`estado-${pedido.id}`}
                          value={estadoEdicion[pedido.id] ?? pedido.estado}
                          onChange={(evento) =>
                            setEstadoEdicion((anterior) => ({
                              ...anterior,
                              [pedido.id]: evento.target.value,
                            }))
                          }
                        >
                          {ESTADOS.map((estado) => (
                            <option key={estado.valor} value={estado.valor}>
                              {estado.texto}
                            </option>
                          ))}
                        </select>
                        <button type="button" className="btn-guardar" onClick={() => guardarEstado(pedido.id)}>
                          Actualizar
                        </button>
                        <button
                          type="button"
                          className="btn-eliminar"
                          title="Eliminar pedido"
                          onClick={() => eliminarPedido(pedido.id)}
                        >
                          🗑
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
