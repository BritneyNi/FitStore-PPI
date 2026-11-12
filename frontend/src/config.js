const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api").replace(/\/$/, "");

export const CONFIG = { API_BASE_URL };

export default CONFIG;
