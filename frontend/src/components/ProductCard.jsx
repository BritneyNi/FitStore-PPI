import { formatPrecio, resolverImagen } from "../utils/format";

export default function ProductCard({ producto, onAgregar }) {
  const agotado = producto.stock === 0;
  const imagen = resolverImagen(producto.imagenUrl);
  const claseStock = agotado ? "sin-stock" : producto.stock <= 5 ? "poco-stock" : "en-stock";

  return (
    <div className={`producto-card ${agotado ? "sin-stock" : ""}`}>
      <div className="producto-emoji">
        {imagen ? (
          <img className="producto-img" src={imagen} alt={producto.nombre} />
        ) : (
          producto.emoji || "📦"
        )}
      </div>

      <div className="producto-info">
        <span className="producto-categoria">{producto.categoria}</span>
        <h3 className="producto-nombre">{producto.nombre}</h3>
        <p className="producto-desc">{producto.descripcion}</p>

        <div className="producto-footer">
          <span className="producto-precio">${formatPrecio(producto.precio)}</span>
          <span className={`stock-badge ${claseStock}`}>
            {agotado ? "Agotado" : producto.stock <= 5 ? `¡Solo ${producto.stock}!` : `Stock: ${producto.stock}`}
          </span>
        </div>

        <button type="button" className="btn-agregar" disabled={agotado} onClick={() => onAgregar(producto)}>
          {agotado ? "Agotado" : "+ Agregar al carrito"}
        </button>
      </div>
    </div>
  );
}
