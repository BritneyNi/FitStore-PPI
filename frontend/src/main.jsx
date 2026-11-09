import { StrictMode } from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { CartProvider } from "./context/CartContext";
import { SessionProvider } from "./context/SessionContext";
import { ToastProvider } from "./context/ToastContext";
import "../css/style.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <StrictMode>
    <ToastProvider>
      <SessionProvider>
        <CartProvider>
          <App />
        </CartProvider>
      </SessionProvider>
    </ToastProvider>
  </StrictMode>
);
