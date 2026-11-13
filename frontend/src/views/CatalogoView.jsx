import { useMemo, useState } from "react";
import ProductCard from "../components/ProductCard";

const FILTROS = [
  { valor: "todos", texto: "Todos" },
  { valor: "SUPLEMENTO", texto: "Suplementos" },
  { valor: "EQUIPO", texto: "Equipos" },
  { valor: "ROPA", texto: "Ropa" },
];

const ORDENES = [
  { valor: "ninguno", texto: "Ordenar por..." },
  { valor: "precio-asc", texto: "Precio: menor a mayor" },
  { valor: "precio-desc", texto: "Precio: mayor a menor" },
  { valor: "nombre", texto: "Nombre (A-Z)" },
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
  const [orden, setOrden] = useState("ninguno");
  const [soloDisponibles, setSoloDisponibles] = useState(false);

  const productosVisibles = useMemo(() => {
    const lista = soloDisponibles ? productos.filter((p) => p.stock > 0) : [...productos];

    if (orden === "precio-asc") lista.sort((a, b) => a.precio - b.precio);
    else if (orden === "precio-desc") lista.sort((a, b) => b.precio - a.precio);
    else if (orden === "nombre") lista.sort((a, b) => a.nombre.localeCompare(b.nombre));

    return lista;
  }, [productos, orden, soloDisponibles]);

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

        <div className="opciones-catalogo">
          <select
            className="orden-select"
            value={orden}
            onChange={(evento) => setOrden(evento.target.value)}
          >
            {ORDENES.map((opcion) => (
              <option key={opcion.valor} value={opcion.valor}>
                {opcion.texto}
              </option>
            ))}
          </select>

          <label className="solo-disponibles">
            <input
              type="checkbox"
              checked={soloDisponibles}
              onChange={(evento) => setSoloDisponibles(evento.target.checked)}
            />
            Solo disponibles
          </label>
        </div>
      </div>

      <div id="gridProductos" className="grid-productos">
        {cargando ? (
          <p className="empty-msg">Cargando productos...</p>
        ) : error ? (
          <p className="empty-msg">{error}</p>
        ) : productosVisibles.length === 0 ? (
          <p className="empty-msg">No se encontraron productos.</p>
        ) : (
          productosVisibles.map((producto) => (
            <ProductCard key={producto.id} producto={producto} onAgregar={onAgregar} />
          ))
        )}
      </div>
    </div>
  );
}
