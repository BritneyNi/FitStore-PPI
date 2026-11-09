import { createContext, useCallback, useContext, useEffect, useMemo, useReducer } from "react";
import Storage from "../storage";
import { cartReducer } from "../utils/cartReducer";
import { useToast } from "./ToastContext";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [items, dispatch] = useReducer(cartReducer, undefined, () => {
    const guardado = Storage.getCart();
    return Array.isArray(guardado) ? guardado : [];
  });
  const { mostrarToast } = useToast();

  useEffect(() => {
    Storage.setCart(items);
  }, [items]);

  const agregar = useCallback(
    (producto, talla = null) => {
      const variante = talla || null;
      const existente = items.find(
        (item) => item.id === producto.id && (item.talla || null) === variante
      );
      const cantidadActual = existente ? existente.cantidad : 0;

      if (producto.stock < 1) {
        mostrarToast("⚠️ Producto agotado.");
        return false;
      }
      if (cantidadActual >= producto.stock) {
        mostrarToast("⚠️ No hay más stock disponible.");
        return false;
      }

      dispatch({ type: "add", producto, talla: variante });
      const etiqueta = variante ? ` (Talla ${variante})` : "";
      mostrarToast(`✅ ${producto.nombre}${etiqueta} x${cantidadActual + 1} agregado al carrito.`);
      return true;
    },
    [items, mostrarToast]
  );

  const cambiarCantidad = useCallback(
    (item, delta) => {
      if (delta > 0 && item.cantidad >= item.stock) {
        mostrarToast("⚠️ No hay más stock disponible.");
        return;
      }
      dispatch({ type: "change", id: item.id, talla: item.talla || null, delta });
    },
    [mostrarToast]
  );

  const eliminar = useCallback((item) => {
    dispatch({ type: "remove", id: item.id, talla: item.talla || null });
  }, []);

  const vaciar = useCallback(() => dispatch({ type: "clear" }), []);

  const total = useMemo(
    () => items.reduce((suma, item) => suma + item.precio * item.cantidad, 0),
    [items]
  );
  const cantidad = useMemo(
    () => items.reduce((suma, item) => suma + item.cantidad, 0),
    [items]
  );

  const value = useMemo(
    () => ({ items, total, cantidad, agregar, cambiarCantidad, eliminar, vaciar }),
    [items, total, cantidad, agregar, cambiarCantidad, eliminar, vaciar]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const contexto = useContext(CartContext);
  if (!contexto) throw new Error("useCart debe usarse dentro de CartProvider");
  return contexto;
}
