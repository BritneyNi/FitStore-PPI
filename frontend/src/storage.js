// ============================================================
//  FitStore — storage.js
//  Encapsula la persistencia en localStorage (token, usuario y
//  carrito). El resto de la aplicacion no toca localStorage de
//  forma directa.
// ============================================================

const KEYS = {
  token: "fitstore_token",
  user: "fitstore_user",
  cart: "fitstore_carrito",
  vista: "fitstore_vista",
};

function leerJSON(key, fallback) {
  try {
    return JSON.parse(localStorage.getItem(key) || JSON.stringify(fallback));
  } catch (e) {
    return fallback;
  }
}

export const Storage = {
  getToken() {
    return localStorage.getItem(KEYS.token);
  },
  setToken(token) {
    if (token) localStorage.setItem(KEYS.token, token);
    else localStorage.removeItem(KEYS.token);
  },
  getUser() {
    return leerJSON(KEYS.user, null);
  },
  setUser(user) {
    if (user) localStorage.setItem(KEYS.user, JSON.stringify(user));
    else localStorage.removeItem(KEYS.user);
  },
  getCart() {
    return leerJSON(KEYS.cart, []);
  },
  setCart(cart) {
    localStorage.setItem(KEYS.cart, JSON.stringify(cart));
  },
  getVista() {
    return localStorage.getItem(KEYS.vista);
  },
  setVista(vista) {
    if (vista) localStorage.setItem(KEYS.vista, vista);
    else localStorage.removeItem(KEYS.vista);
  },
  clear() {
    localStorage.removeItem(KEYS.token);
    localStorage.removeItem(KEYS.user);
    localStorage.removeItem(KEYS.cart);
  },
};

export default Storage;
