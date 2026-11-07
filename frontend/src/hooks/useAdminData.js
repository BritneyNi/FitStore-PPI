import { useCallback, useEffect, useState } from "react";
import Api from "../api";
import { useSession } from "../context/SessionContext";

// Datos del panel de administrador: inventario y pedidos.
export function useAdminData(activo) {
  const { usuario } = useSession();
  const [productos, setProductos] = useState([]);
  const [pedidos, setPedidos] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState("");
  const [recarga, setRecarga] = useState(0);

  useEffect(() => {
    if (!activo) return undefined;

    if (!usuario || usuario.rol !== "ADMIN") {
      setError("Acceso denegado.");
      setCargando(false);
      return undefined;
    }

    let vigente = true;
    setCargando(true);
    setError("");

    Promise.all([Api.get("/admin/productos"), Api.get("/admin/pedidos")])
      .then(([listaProductos, listaPedidos]) => {
        if (!vigente) return;
        setProductos(Array.isArray(listaProductos) ? listaProductos : []);
        setPedidos(Array.isArray(listaPedidos) ? listaPedidos : []);
      })
      .catch((e) => {
        if (vigente) setError(`⚠️ Error cargando admin: ${e.message}`);
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });

    return () => {
      vigente = false;
    };
  }, [activo, usuario, recarga]);

  const recargar = useCallback(() => setRecarga((n) => n + 1), []);

  return { productos, pedidos, cargando, error, recargar };
}
