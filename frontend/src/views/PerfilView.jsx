import { useState } from "react";
import Api from "../api";
import { useSession } from "../context/SessionContext";
import { useToast } from "../context/ToastContext";

export default function PerfilView() {
  const { usuario } = useSession();
  const { mostrarToast } = useToast();
  const [claveActual, setClaveActual] = useState("");
  const [claveNueva, setClaveNueva] = useState("");
  const [confirmacion, setConfirmacion] = useState("");
  const [verClaves, setVerClaves] = useState(false);
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setError("");

    if (claveNueva.length < 6) {
      setError("La nueva contraseña debe tener al menos 6 caracteres.");
      return;
    }
    if (claveNueva !== confirmacion) {
      setError("Las contraseñas nuevas no coinciden.");
      return;
    }

    setEnviando(true);
    try {
      await Api.patch("/perfil/clave", { claveActual, claveNueva });
      mostrarToast("Contraseña actualizada ✅");
      setClaveActual("");
      setClaveNueva("");
      setConfirmacion("");
    } catch (e) {
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  }

  const tipo = verClaves ? "text" : "password";

  return (
    <div id="vista-perfil" className="vista">
      <form className="auth-container" onSubmit={manejarEnvio}>
        <h2>Mi perfil</h2>
        <p className="auth-hint">
          {usuario?.nombre} · {usuario?.email}
        </p>

        <h3>Cambiar contraseña</h3>

        <div className="form-group">
          <label htmlFor="claveActual">Contraseña actual</label>
          <input
            type={tipo}
            id="claveActual"
            value={claveActual}
            onChange={(evento) => setClaveActual(evento.target.value)}
            autoComplete="current-password"
          />
        </div>

        <div className="form-group">
          <label htmlFor="claveNueva">Nueva contraseña</label>
          <input
            type={tipo}
            id="claveNueva"
            value={claveNueva}
            onChange={(evento) => setClaveNueva(evento.target.value)}
            autoComplete="new-password"
          />
        </div>

        <div className="form-group">
          <label htmlFor="claveConfirmacion">Confirmar nueva contraseña</label>
          <input
            type={tipo}
            id="claveConfirmacion"
            value={confirmacion}
            onChange={(evento) => setConfirmacion(evento.target.value)}
            autoComplete="new-password"
          />
        </div>

        <label className="mostrar-clave">
          <input
            type="checkbox"
            checked={verClaves}
            onChange={(evento) => setVerClaves(evento.target.checked)}
          />
          Mostrar contraseñas
        </label>

        <button type="submit" className="btn-primary btn-full" disabled={enviando}>
          {enviando ? "Guardando..." : "Cambiar contraseña"}
        </button>

        <p className="error-msg">{error}</p>
      </form>
    </div>
  );
}
