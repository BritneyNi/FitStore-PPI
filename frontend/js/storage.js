// ============================================================
//  FitStore — storage.js
//  Encapsula la persistencia en localStorage (token, usuario,
//  carrito). Evita leer/escribir localStorage directamente en app.js.
// ============================================================
(function () {
  "use strict";

  var KEYS = {
    token: "fitstore_token",
    user: "fitstore_user",
    cart: "fitstore_carrito",
  };

  function leerJSON(key, fallback) {
    try {
      return JSON.parse(localStorage.getItem(key) || JSON.stringify(fallback));
    } catch (e) {
      return fallback;
    }
  }

  var Storage = {
    getToken: function () {
      return localStorage.getItem(KEYS.token);
    },
    setToken: function (token) {
      if (token) localStorage.setItem(KEYS.token, token);
      else localStorage.removeItem(KEYS.token);
    },
    getUser: function () {
      return leerJSON(KEYS.user, null);
    },
    setUser: function (user) {
      if (user) localStorage.setItem(KEYS.user, JSON.stringify(user));
      else localStorage.removeItem(KEYS.user);
    },
    getCart: function () {
      return leerJSON(KEYS.cart, []);
    },
    setCart: function (cart) {
      localStorage.setItem(KEYS.cart, JSON.stringify(cart));
    },
    clear: function () {
      localStorage.removeItem(KEYS.token);
      localStorage.removeItem(KEYS.user);
      localStorage.removeItem(KEYS.cart);
    },
  };

  window.FitStore = window.FitStore || {};
  window.FitStore.storage = Storage;
})();
