import { createContext, useCallback, useContext, useMemo, useState } from "react";
import Api from "../api";
import Storage from "../storage";

const SessionContext = createContext(null);

export function SessionProvider({ children }) {
  const [token, setToken] = useState(() => Storage.getToken());
  const [usuario, setUsuario] = useState(() => Storage.getUser());

  const guardarSesion = useCallback((data) => {
    const nuevoUsuario = { nombre: data.nombre, email: data.email, rol: data.rol };
    setToken(data.token);
    setUsuario(nuevoUsuario);
    Storage.setToken(data.token);
    Storage.setUser(nuevoUsuario);
    return nuevoUsuario;
  }, []);

  const login = useCallback(
    async (email, clave) => {
      const data = await Api.post("/auth/login", { email, clave });
      return guardarSesion(data);
    },
    [guardarSesion]
  );

  const registrar = useCallback(
    async (nombre, email, clave) => {
      const data = await Api.post("/auth/register", { nombre, email, clave });
      return guardarSesion(data);
    },
    [guardarSesion]
  );

  const logout = useCallback(() => {
    setToken(null);
    setUsuario(null);
    Storage.clear();
  }, []);

  const value = useMemo(
    () => ({ token, usuario, login, registrar, logout }),
    [token, usuario, login, registrar, logout]
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession() {
  const contexto = useContext(SessionContext);
  if (!contexto) throw new Error("useSession debe usarse dentro de SessionProvider");
  return contexto;
}
