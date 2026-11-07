import { useState } from "react";
import { useSession } from "../context/SessionContext";
import { useToast } from "../context/ToastContext";

export default function RegistroView({ onIngresar, onIrLogin }) {
  const { registrar } = useSession();
  const { mostrarToast } = useToast();
  const [nombre, setNombre] = useState("");
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
      const usuario = await registrar(nombre.trim(), email.trim(), clave);
      mostrarToast(`¡Cuenta creada! Bienvenido, ${usuario.nombre} 🎉`);
      onIngresar();
    } catch (e) {
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div id="vista-registro" className="vista">
      <form className="auth-container" onSubmit={manejarEnvio}>
        <h2>Crear cuenta</h2>

        <div className="form-group">
          <label htmlFor="regNombre">Nombre</label>
          <input
            type="text"
            id="regNombre"
            placeholder="Tu nombre"
            value={nombre}
            onChange={(evento) => setNombre(evento.target.value)}
            autoComplete="name"
          />
        </div>

        <div className="form-group">
          <label htmlFor="regEmail">Correo electrónico</label>
          <input
            type="email"
            id="regEmail"
            placeholder="tu@email.com"
            value={email}
            onChange={(evento) => setEmail(evento.target.value)}
            autoComplete="email"
          />
        </div>

        <div className="form-group">
          <label htmlFor="regClave">Contraseña</label>
          <input
            type={verClave ? "text" : "password"}
            id="regClave"
            placeholder="••••••••"
            value={clave}
            onChange={(evento) => setClave(evento.target.value)}
            autoComplete="new-password"
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
          Registrarme
        </button>

        <p id="registroError" className="error-msg">
          {error}
        </p>

        <p className="auth-hint">
          ¿Ya tienes cuenta?{" "}
          <a
            href="#"
            onClick={(evento) => {
              evento.preventDefault();
              onIrLogin();
            }}
          >
            Inicia sesión
          </a>
        </p>
      </form>
    </div>
  );
}
