import { useCart } from "../context/CartContext";
import { useSession } from "../context/SessionContext";
import { esPersonal } from "../utils/roles";

export default function Navbar({ vista, onNavegar, onAbrirCarrito, onSalir }) {
  const { usuario } = useSession();
  const { cantidad } = useCart();
  const personal = esPersonal(usuario);

  return (
    <nav className="navbar">
      <div className="nav-brand">
        <span className="brand-icon">💪</span>
        <span className="brand-text">FitStore</span>
      </div>

      <div className="nav-links">
        <button
          type="button"
          className={`nav-btn ${vista === "catalogo" ? "active" : ""}`}
          onClick={() => onNavegar("catalogo")}
        >
          Catalogo
        </button>

        {!personal && (
          <button type="button" className="nav-btn" onClick={() => onNavegar("mis-pedidos")}>
            Mis Pedidos
          </button>
        )}

        {usuario?.rol === "ADMIN" && (
          <button type="button" className="nav-btn admin-btn" onClick={() => onNavegar("admin")}>
            Admin
          </button>
        )}
      </div>

      <div className="nav-right">
        {!personal && (
          <button type="button" className="carrito-btn" onClick={onAbrirCarrito}>
            🛒 <span id="carritoCount">{cantidad}</span>
          </button>
        )}

        {usuario ? (
          <div id="userInfo" className="user-info">
            <span id="userName">{usuario.nombre}</span>
            <button type="button" className="btn-logout" onClick={onSalir}>
              Salir
            </button>
          </div>
        ) : (
          <button type="button" id="btnLogin" className="btn-primary" onClick={() => onNavegar("login")}>
            Iniciar sesion
          </button>
        )}
      </div>
    </nav>
  );
}
