import ProductCard from "../components/ProductCard";

const FILTROS = [
  { valor: "todos", texto: "Todos" },
  { valor: "SUPLEMENTO", texto: "Suplementos" },
  { valor: "EQUIPO", texto: "Equipos" },
  { valor: "ROPA", texto: "Ropa" },
];

export default function CatalogoView({
  productos,
  cargando,
  error,
  filtro,
  busqueda,
  onFiltrar,
  onBuscar,
  onAgregar,
}) {
  return (
    <div id="vista-catalogo" className="vista">
      <div className="catalogo-header">
        <h1>Nuestros Productos</h1>

        <div className="filtros">
          {FILTROS.map((opcion) => (
            <button
              key={opcion.valor}
              type="button"
              className={`filtro-btn ${filtro === opcion.valor ? "active" : ""}`}
              onClick={() => onFiltrar(opcion.valor)}
            >
              {opcion.texto}
            </button>
          ))}
        </div>

        <input
          type="text"
          id="buscador"
          className="buscador"
          placeholder="🔍 Buscar producto..."
          value={busqueda}
          onChange={(evento) => onBuscar(evento.target.value)}
        />
      </div>

      <div id="gridProductos" className="grid-productos">
        {cargando ? (
          <p className="empty-msg">Cargando productos...</p>
        ) : error ? (
          <p className="empty-msg">{error}</p>
        ) : productos.length === 0 ? (
          <p className="empty-msg">No se encontraron productos.</p>
        ) : (
          productos.map((producto) => (
            <ProductCard key={producto.id} producto={producto} onAgregar={onAgregar} />
          ))
        )}
      </div>
    </div>
  );
}
