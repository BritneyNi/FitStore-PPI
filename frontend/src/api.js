// ============================================================
//  FitStore — api.js
//  Capa de acceso a la API REST. Centraliza:
//   - La URL base (desde config.js).
//   - El envio del token JWT (Bearer).
//   - El desempaquetado del envelope { data, meta }
//   - El manejo de errores { error: { status, message, details } }.
//   - La sesion expirada (HTTP 401).
// ============================================================

import { CONFIG } from "./config";
import Storage from "./storage";

/** Error de API con status HTTP y detalles de validacion por campo. */
export class ApiError extends Error {
  constructor(status, message, details) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details || {};
  }
}

let onUnauthorized = null;

function authHeaders() {
  const headers = { "Content-Type": "application/json" };
  const token = Storage.getToken();
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
  let res;
  try {
    res = await fetch(CONFIG.API_BASE_URL + path, {
      method,
      headers: authHeaders(),
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    throw new ApiError(0, "No se pudo conectar con el servidor");
  }

  const texto = await res.text();
  let json = null;
  try {
    json = texto ? JSON.parse(texto) : null;
  } catch (e) {
    json = null;
  }

  if (!res.ok) {
    if (res.status === 401 && onUnauthorized) onUnauthorized();
    const err = json && json.error && typeof json.error === "object" ? json.error : {};
    throw new ApiError(res.status, mensajeDeError(res.status, json), err.details);
  }

  // Desempaqueta el envelope { data, ... }; si no lo es, devuelve tal cual.
  return json && Object.prototype.hasOwnProperty.call(json, "data") ? json.data : json;
}

export const Api = {
  get(path) {
    return request("GET", path);
  },
  post(path, body) {
    return request("POST", path, body);
  },
  patch(path, body) {
    return request("PATCH", path, body);
  },
  del(path) {
    return request("DELETE", path);
  },
  setOnUnauthorized(fn) {
    onUnauthorized = fn;
  },
};

export default Api;
