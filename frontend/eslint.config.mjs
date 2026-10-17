import js from "@eslint/js";
import globals from "globals";

// Configuracion de ESLint (flat config) para el frontend vanilla.
// Ejecutar:  npm run lint       (analiza)
//            npm run lint:fix   (corrige lo posible)
export default [
  js.configs.recommended,
  {
    files: ["js/**/*.js"],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: "script",
      globals: {
        ...globals.browser,
      },
    },
    rules: {
      // Las funciones se invocan desde atributos onclick en index.html
      // (se declaran con /* exported */ en app.js). No marcamos como error
      // variables no usadas: solo advertencia.
      "no-unused-vars": ["warn", { args: "none", caughtErrors: "none" }],
      "no-console": "warn",
    },
  },
  {
    ignores: ["node_modules/**", "dist/**"],
  },
];
