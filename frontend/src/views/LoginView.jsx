import { useState } from "react";
import { useSession } from "../context/SessionContext";
import { useToast } from "../context/ToastContext";

export default function LoginView({ onIngresar, onIrRegistro }) {
  const { login } = useSession();
  const { mostrarToast } = useToast();
  const [email, setEmail] = useState("");
  const [clave, setClave] = useState("");
  const [verClave, setVerClave] = useState(false);
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setError("");
    setEnviando(true);

    try {
      const usuario = await login(email.trim(), clave.trim());
      mostrarToast(`¡Bienvenido, ${usuario.nombre}! 👋`);
      onIngresar(usuario);
    } catch (e) {
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div id="vista-login" className="vista">
      <form className="auth-container" onSubmit={manejarEnvio}>
        <h2>Iniciar Sesion</h2>

        <div className="form-group">
          <label htmlFor="loginEmail">Correo electronico</label>
          <input
            type="email"
            id="loginEmail"
            placeholder="tu@email.com"
            value={email}
            onChange={(evento) => setEmail(evento.target.value)}
            autoComplete="email"
          />
        </div>

        <div className="form-group">
          <label htmlFor="loginClave">Contrasena</label>
          <input
            type={verClave ? "text" : "password"}
            id="loginClave"
            placeholder="••••••••"
            value={clave}
            onChange={(evento) => setClave(evento.target.value)}
            autoComplete="current-password"
          />
        </div>

        <label className="mostrar-clave">
          <input
            type="checkbox"
            checked={verClave}
            onChange={(evento) => setVerClave(evento.target.checked)}
          />
          Mostrar contraseña
        </label>

        <button type="submit" className="btn-primary btn-full" disabled={enviando}>
          Entrar
        </button>

        <p id="loginError" className="error-msg">
          {error}
        </p>

        <p className="auth-hint">
          ¿No tienes cuenta?{" "}
          <a
            href="#"
            onClick={(evento) => {
              evento.preventDefault();
              onIrRegistro();
            }}
          >
            Regístrate
          </a>
        </p>

        <p className="auth-hint">
          Cliente: <code>juan@email.com / clave123</code>
          <br />
          Admin: <code>admin@fitstore.com / admin123</code>
        </p>
      </form>
    </div>
  );
}
