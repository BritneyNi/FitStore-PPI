import { useEffect } from "react";

const TALLAS = ["S", "M", "L", "XL", "XXL"];

export default function TallaModal({ producto, onSeleccionar, onCerrar }) {
  useEffect(() => {
    function manejarTecla(evento) {
      if (evento.key === "Escape") onCerrar();
    }
    document.addEventListener("keydown", manejarTecla);
    return () => document.removeEventListener("keydown", manejarTecla);
  }, [onCerrar]);

  return (
    <div className="modal-overlay" onClick={onCerrar}>
      <div
        className="modal-talla"
        role="dialog"
        aria-modal="true"
        aria-labelledby="tituloTalla"
        onClick={(evento) => evento.stopPropagation()}
      >
        <h3 id="tituloTalla">{producto.nombre}</h3>
        <p>Selecciona una talla (opcional)</p>

        <div className="talla-opciones">
          {TALLAS.map((talla) => (
            <button
              key={talla}
              type="button"
              className="talla-btn"
              onClick={() => onSeleccionar(talla)}
            >
              {talla}
            </button>
          ))}
          <button type="button" className="talla-btn talla-sin" onClick={() => onSeleccionar(null)}>
            Sin talla
          </button>
        </div>

        <button type="button" className="btn-primary btn-full" onClick={onCerrar}>
          Cancelar
        </button>
      </div>
    </div>
  );
}
