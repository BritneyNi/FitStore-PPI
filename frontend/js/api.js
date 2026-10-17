// ============================================================
//  FitStore — api.js
//  Capa de acceso a la API REST. Centraliza:
//   - La URL base (desde config.js).
//   - El envio del token JWT (Bearer).
//   - El desempaquetado del envelope { data, meta }
//   - El manejo de errores { error: { status, message, details } }.
//   - La sesion expirada (HTTP 401).
// ============================================================
(function () {
  "use strict";

  var API_BASE_URL = window.FitStore.config.API_BASE_URL;
  var Storage = window.FitStore.storage;

  /** Error de API con status HTTP y detalles de validacion por campo. */
  function ApiError(status, message, details) {
    var err = new Error(message);
    err.name = "ApiError";
    err.status = status;
    err.details = details || {};
    return err;
  }

  var onUnauthorized = null;

  function authHeaders() {
    var headers = { "Content-Type": "application/json" };
    var token = Storage.getToken();
    if (token) headers.Authorization = "Bearer " + token;
    return headers;
  }

  function mensajeDeError(status, body) {
    if (body && body.error && typeof body.error === "object" && body.error.message) {
      return body.error.message;
    }
    if (body && typeof body.error === "string") {
      return body.error;
    }
    return "Error " + status;
  }

  async function request(method, path, body) {
    var res;
    try {
      res = await fetch(API_BASE_URL + path, {
        method: method,
        headers: authHeaders(),
        body: body !== undefined ? JSON.stringify(body) : undefined,
      });
    } catch (e) {
      throw ApiError(0, "No se pudo conectar con el servidor");
    }

    var texto = await res.text();
    var json = texto ? JSON.parse(texto) : null;

    if (!res.ok) {
      if (res.status === 401 && onUnauthorized) onUnauthorized();
      var err = json && json.error ? json.error : {};
      throw ApiError(res.status, mensajeDeError(res.status, json), err.details);
    }

    // Desempaqueta el envelope { data, ... }; si no lo es, devuelve tal cual.
    return json && Object.prototype.hasOwnProperty.call(json, "data") ? json.data : json;
  }

  var Api = {
    get: function (path) {
      return request("GET", path);
    },
    post: function (path, body) {
      return request("POST", path, body);
    },
    patch: function (path, body) {
      return request("PATCH", path, body);
    },
    del: function (path) {
      return request("DELETE", path);
    },
    setOnUnauthorized: function (fn) {
      onUnauthorized = fn;
    },
  };

  window.FitStore = window.FitStore || {};
  window.FitStore.api = Api;
})();
