import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// La ruta base relativa permite publicar el contenido de `dist/` en
// cualquier subcarpeta (S3, un servidor estatico, etc.) sin cambiar codigo.
export default defineConfig({
  plugins: [react()],
  base: "./",
  server: {
    port: 5173,
  },
});
