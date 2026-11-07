/** Formato de precios usado en toda la interfaz. */
export function formatPrecio(valor) {
  return Number(valor).toLocaleString("es-CO");
}

/** Convierte el estado del pedido en la clase CSS correspondiente. */
export function claseEstado(estado) {
  return (estado || "").toLowerCase().replace("_", "-");
}

/**
 * Resuelve la URL de la imagen guardada en la base de datos.
 * Las rutas relativas se apoyan en la base de Vite para que el build
 * funcione igual en la raiz del dominio o en una subcarpeta.
 */
export function resolverImagen(url) {
  if (!url) return null;
  if (/^(https?:)?\/\//i.test(url) || url.startsWith("data:")) return url;
  const base = import.meta.env.BASE_URL || "/";
  return `${base.replace(/\/$/, "")}/${url.replace(/^\.?\//, "")}`;
}
