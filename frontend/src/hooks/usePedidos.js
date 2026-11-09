import { useCallback, useEffect, useState } from "react";
import Api from "../api";
import { useSession } from "../context/SessionContext";

// Carga el historial del cliente solo cuando la vista esta activa.
export function usePedidos(activo) {
  const { usuario } = useSession();
  const [pedidos, setPedidos] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState("");
  const [recarga, setRecarga] = useState(0);

  useEffect(() => {
    if (!activo) return undefined;

    if (!usuario) {
      setPedidos([]);
      setError("Debes iniciar sesión.");
      setCargando(false);
      return undefined;
    }

    let vigente = true;
    setCargando(true);
    setError("");

    Api.get("/pedidos/mis-pedidos")
      .then((data) => {
        if (vigente) setPedidos(Array.isArray(data) ? data : []);
      })
      .catch(() => {
        if (vigente) setError("⚠️ Error cargando pedidos.");
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });

    return () => {
      vigente = false;
    };
  }, [activo, usuario, recarga]);

  const recargar = useCallback(() => setRecarga((n) => n + 1), []);

  return { pedidos, cargando, error, recargar };
}
