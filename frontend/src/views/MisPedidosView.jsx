import { useState } from "react";
import Api from "../api";
import { useToast } from "../context/ToastContext";
import { usePedidos } from "../hooks/usePedidos";
import { claseEstado, formatPrecio } from "../utils/format";

export default function MisPedidosView() {
  const { pedidos, cargando, error, recargar } = usePedidos(true);
  const { mostrarToast } = useToast();
  const [cancelando, setCancelando] = useState(null);

  async function cancelarPedido(id) {
    if (!window.confirm(`¿Seguro que quieres cancelar el pedido #${id}?`)) return;

    setCancelando(id);
    try {
      await Api.patch(`/pedidos/${id}/cancelar`);
      mostrarToast(`Pedido #${id} cancelado`);
      recargar();
    } catch (err) {
      mostrarToast(err.message || "No se pudo cancelar el pedido");
    } finally {
      setCancelando(null);
    }
  }

  return (
    <div id="vista-mis-pedidos" className="vista">
      <div className="section-header">
        <h2>Mis Pedidos</h2>
      </div>

      <div id="listaPedidos" className="lista-pedidos">
        {cargando ? (
          <p className="empty-msg">Cargando pedidos...</p>
        ) : error ? (
          <p className="empty-msg">{error}</p>
        ) : pedidos.length === 0 ? (
          <p className="empty-msg">Aún no tienes pedidos.</p>
        ) : (
          pedidos.map((pedido) => (
            <div className="pedido-card" key={pedido.id}>
              <div className="pedido-header">
                <span className="pedido-id">#{pedido.id}</span>
                <span className="pedido-fecha">{pedido.fecha?.split("T")[0] || ""}</span>
                <span className={`pedido-estado estado-${claseEstado(pedido.estado)}`}>
                  {pedido.estado}
                </span>
              </div>

              <div className="pedido-items">
                {(pedido.items || []).map((item, indice) => (
                  <div className="pedido-item-row" key={item.id ?? `${pedido.id}-${indice}`}>
                    <span>{item.producto?.nombre || item.nombreProducto}</span>
                    <span>x{item.cantidad}</span>
                    <span>${formatPrecio(item.precioUnitario * item.cantidad)}</span>
                  </div>
                ))}
              </div>

              <div className="pedido-total">
                Total: <strong>${formatPrecio(pedido.total)}</strong>
              </div>

              {pedido.estado === "CONFIRMADO" && (
                <div className="pedido-acciones">
                  <button
                    type="button"
                    className="btn-cancelar-pedido"
                    disabled={cancelando === pedido.id}
                    onClick={() => cancelarPedido(pedido.id)}
                  >
                    {cancelando === pedido.id ? "Cancelando..." : "Cancelar pedido"}
                  </button>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
