import { useCallback, useEffect, useState } from "react";
import Api from "../api";

// Carga el catalogo desde la API. La busqueda tiene prioridad sobre el
// filtro por categoria, igual que en la version anterior.
export function useProductos({ categoria = "todos", buscar = "" } = {}) {
  const [productos, setProductos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [recarga, setRecarga] = useState(0);

  useEffect(() => {
    let vigente = true;
    const params = new URLSearchParams();
    const termino = buscar.trim();

    if (termino) params.set("buscar", termino);
    else if (categoria && categoria !== "todos") params.set("categoria", categoria);

    const consulta = params.toString();
    setCargando(true);
    setError("");

    Api.get(`/productos${consulta ? `?${consulta}` : ""}`)
      .then((data) => {
        if (vigente) setProductos(Array.isArray(data) ? data : []);
      })
      .catch(() => {
        if (vigente) {
          setError("⚠️ No se pudo conectar al servidor. ¿Está corriendo Spring Boot?");
        }
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });

    return () => {
      vigente = false;
    };
  }, [categoria, buscar, recarga]);

  const recargar = useCallback(() => setRecarga((n) => n + 1), []);

  return { productos, cargando, error, recargar };
}
