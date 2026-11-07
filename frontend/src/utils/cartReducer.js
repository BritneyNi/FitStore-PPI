// Reducer del carrito. Cada linea del carrito es un producto con su talla,
// por lo que un mismo producto puede aparecer dos veces con tallas distintas.

function mismaVariante(item, id, talla) {
  return item.id === id && (item.talla || null) === (talla || null);
}

export function cartReducer(state, action) {
  switch (action.type) {
    case "add": {
      const { producto, talla = null } = action;
      const indice = state.findIndex((item) => mismaVariante(item, producto.id, talla));

      if (indice === -1) {
        return [
          ...state,
          {
            id: producto.id,
            nombre: producto.nombre,
            precio: producto.precio,
            stock: producto.stock,
            emoji: producto.emoji,
            imagenUrl: producto.imagenUrl,
            cantidad: 1,
            talla: talla || null,
          },
        ];
      }

      return state.map((item, i) =>
        i === indice
          ? {
              ...item,
              cantidad: item.cantidad + 1,
              stock: producto.stock,
              precio: producto.precio,
              imagenUrl: producto.imagenUrl,
            }
          : item
      );
    }
    case "change": {
      const { id, talla = null, delta } = action;
      return state
        .map((item) => {
          if (!mismaVariante(item, id, talla)) return item;
          const cantidad = item.cantidad + delta;
          if (cantidad > item.stock) return { ...item, cantidad: item.stock };
          return { ...item, cantidad };
        })
        .filter((item) => item.cantidad > 0);
    }
    case "remove":
      return state.filter((item) => !mismaVariante(item, action.id, action.talla));
    case "clear":
      return [];
    default:
      return state;
  }
}
