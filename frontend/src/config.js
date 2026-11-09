// ============================================================
//  FitStore — config.js
//  Configuracion del frontend. La URL base de la API se lee de
//  la variable de entorno VITE_API_BASE_URL (archivo .env) para
//  no dejarla escrita en el codigo.
// ============================================================

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api").replace(/\/$/, "");

export const CONFIG = { API_BASE_URL };

export default CONFIG;
