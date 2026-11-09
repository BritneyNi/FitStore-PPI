export function esAdmin(usuario) {
  return usuario?.rol === "ADMIN";
}

export function esPersonal(usuario) {
  const rol = usuario?.rol;
  return rol === "ADMIN" || rol === "VENDEDOR";
}
