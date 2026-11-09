import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [mensaje, setMensaje] = useState("");
  const [visible, setVisible] = useState(false);
  const temporizador = useRef(null);

  const mostrarToast = useCallback((texto) => {
    setMensaje(texto);
    setVisible(true);
    if (temporizador.current) clearTimeout(temporizador.current);
    temporizador.current = setTimeout(() => setVisible(false), 3000);
  }, []);

  useEffect(() => {
    return () => {
      if (temporizador.current) clearTimeout(temporizador.current);
    };
  }, []);

  const value = useMemo(() => ({ mostrarToast }), [mostrarToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div id="toast" className={`toast ${visible ? "show" : ""}`}>
        {mensaje}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const contexto = useContext(ToastContext);
  if (!contexto) throw new Error("useToast debe usarse dentro de ToastProvider");
  return contexto;
}
