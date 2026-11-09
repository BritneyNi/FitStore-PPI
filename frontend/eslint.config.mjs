import js from "@eslint/js";
import globals from "globals";
import react from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";

// Configuracion de ESLint (flat config) para el frontend React.
// Ejecutar:  npm run lint       (analiza)
//            npm run lint:fix   (corrige lo posible)
export default [
  js.configs.recommended,
  {
    files: ["src/**/*.{js,jsx}"],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: "module",
      parserOptions: {
        ecmaFeatures: { jsx: true },
      },
      globals: {
        ...globals.browser,
      },
    },
    plugins: {
      react,
      "react-hooks": reactHooks,
    },
    rules: {
      "react/jsx-uses-vars": "error",
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",
      "no-unused-vars": ["warn", { args: "none", caughtErrors: "none" }],
      "no-console": "warn",
    },
  },
  {
    ignores: ["node_modules/**", "dist/**", "public/**"],
  },
];
