import { useState } from "react";
import Api from "../api";
import { useCart } from "../context/CartContext";
import { useSession } from "../context/SessionContext";
import { useToast } from "../context/ToastContext";
import { formatPrecio, resolverImagen } from "../utils/format";

export default function CartPanel({ onCerrar, onIrCatalogo, onCompraConfirmada }) {
  const { items, total, cambiarCantidad, eliminar, vaciar } = useCart();
  const { usuario } = useSession();
  const { mostrarToast } = useToast();
  const [procesando, setProcesando] = useState(false);

  async function confirmarCompra() {
    if (!usuario || !items.length || procesando) return;

    setProcesando(true);
    try {
      const pedido = await Api.post("/pedidos", {
        items: items.map((item) => ({ productoId: item.id, cantidad: item.cantidad })),
      });
      vaciar();
      onCompraConfirmada();
      onCerrar();
      mostrarToast(`🎉 ¡Pedido #${pedido.id} confirmado! Email enviado.`);
    } catch (e) {
      mostrarToast("❌ " + e.message);
    } finally {
      setProcesando(false);
    }
  }

  return (
    <div id="carritoPanel" className="carrito-panel">
      <div className="carrito-header">
        <h3>🛒 Carrito</h3>
        <button type="button" className="carrito-close" onClick={onCerrar}>
          ✕
        </button>
      </div>

      <div id="carritoItems" className="carrito-items">
        {items.length === 0 ? (
          <>
            <p className="empty-msg">Tu carrito está vacío. 🛒</p>
            <button
              type="button"
              className="btn-primary btn-full"
              onClick={() => {
                onCerrar();
                onIrCatalogo();
              }}
            >
              Ir al catálogo
            </button>
          </>
        ) : (
          items.map((item) => {
            const imagen = resolverImagen(item.imagenUrl);
            return (
              <div className="carrito-item" key={`${item.id}-${item.talla || "sin"}`}>
                <span className="carrito-emoji">
                  {imagen ? <img className="carrito-img" src={imagen} alt="" /> : item.emoji}
                </span>

                <div className="carrito-item-info">
                  <span className="carrito-item-nombre">
                    {item.nombre}
                    {item.talla ? ` (Talla ${item.talla})` : ""}
                  </span>
                  <span className="carrito-item-precio">
                    ${formatPrecio(item.precio)} (subtotal: ${formatPrecio(item.precio * item.cantidad)})
                  </span>
                </div>

                <div className="carrito-item-controles">
                  <button type="button" onClick={() => cambiarCantidad(item, -1)}>
                    −
                  </button>
                  <span>{item.cantidad}</span>
                  <button type="button" onClick={() => cambiarCantidad(item, 1)}>
                    +
                  </button>
                  <button type="button" className="btn-eliminar" onClick={() => eliminar(item)}>
                    🗑
                  </button>
                </div>
              </div>
            );
          })
        )}
      </div>

      <div className="carrito-footer">
        <div className="carrito-total">
          Total: <strong id="carritoTotal">${formatPrecio(total)}</strong>
        </div>
        <button
          type="button"
          id="btnComprar"
          className="btn-primary btn-full"
          onClick={confirmarCompra}
          disabled={procesando || items.length === 0}
        >
          {procesando ? "Procesando..." : "Confirmar compra"}
        </button>
      </div>
    </div>
  );
}
