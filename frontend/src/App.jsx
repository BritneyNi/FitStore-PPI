import { useCallback, useEffect, useState } from "react";
import Api from "./api";
import Storage from "./storage";
import CartPanel from "./components/CartPanel";
import Footer from "./components/Footer";
import Navbar from "./components/Navbar";
import PromoBanner from "./components/PromoBanner";
import TallaModal from "./components/TallaModal";
import { useCart } from "./context/CartContext";
import { useSession } from "./context/SessionContext";
import { useToast } from "./context/ToastContext";
import { useProductos } from "./hooks/useProductos";
import AdminView from "./views/AdminView";
import CatalogoView from "./views/CatalogoView";
import LoginView from "./views/LoginView";
import MisPedidosView from "./views/MisPedidosView";
import RegistroView from "./views/RegistroView";
import { esAdmin, esPersonal } from "./utils/roles";

function vistaInicial(usuario) {
  const guardada = Storage.getVista();

  if (!usuario) {
    return guardada === "login" || guardada === "registro" ? guardada : "catalogo";
  }

  if (esAdmin(usuario)) {
    return guardada === "admin" ? "admin" : "catalogo";
  }

  return guardada === "mis-pedidos" && !esPersonal(usuario) ? "mis-pedidos" : "catalogo";
}

export default function App() {
  const { usuario, logout } = useSession();
  const { agregar, vaciar } = useCart();
  const { mostrarToast } = useToast();

  const [vista, setVista] = useState(() => vistaInicial(usuario));
  const [carritoAbierto, setCarritoAbierto] = useState(false);
  const [productoTalla, setProductoTalla] = useState(null);
  const [filtro, setFiltro] = useState("todos");
  const [busqueda, setBusqueda] = useState("");

  const { productos, cargando, error, recargar } = useProductos({ categoria: filtro, buscar: busqueda });

  const salir = useCallback(() => {
    logout();
    vaciar();
    setVista("catalogo");
    mostrarToast("Sesión cerrada.");
  }, [logout, vaciar, mostrarToast]);

  useEffect(() => {
    Api.setOnUnauthorized(() => {
      logout();
      vaciar();
      setVista("catalogo");
      mostrarToast("⚠️ Sesión expirada. Inicia sesión de nuevo.");
    });
  }, [logout, vaciar, mostrarToast]);

  useEffect(() => {
    if (usuario && vista === "login") setVista(esAdmin(usuario) ? "admin" : "catalogo");
  }, [usuario, vista]);

  useEffect(() => {
    Storage.setVista(vista);
  }, [vista]);

  const navegar = useCallback(
    (destino) => {
      if (destino === "login" && usuario) {
        setVista("catalogo");
        return;
      }
      setVista(destino);
    },
    [usuario]
  );

  const agregarAlCarrito = useCallback(
    (producto) => {
      if (!usuario) {
        mostrarToast("⚠️ Debes iniciar sesión para comprar.");
        setVista("login");
        return;
      }

      if ((producto.categoria || "").toUpperCase().includes("ROPA")) {
        setProductoTalla(producto);
        return;
      }

      agregar(producto);
    },
    [usuario, mostrarToast, agregar]
  );

  const elegirTalla = useCallback(
    (talla) => {
      const producto = productoTalla;
      setProductoTalla(null);
      if (producto) agregar(producto, talla);
    },
    [productoTalla, agregar]
  );

  return (
    <>
      <PromoBanner />

      <Navbar
        vista={vista}
        onNavegar={navegar}
        onAbrirCarrito={() => setCarritoAbierto(true)}
        onSalir={salir}
      />

      {vista === "catalogo" && (
        <CatalogoView
          productos={productos}
          cargando={cargando}
          error={error}
          filtro={filtro}
          busqueda={busqueda}
          onFiltrar={setFiltro}
          onBuscar={setBusqueda}
          onAgregar={agregarAlCarrito}
        />
      )}

      {vista === "login" && (
        <LoginView
          onIngresar={(usuario) => setVista(esAdmin(usuario) ? "admin" : "catalogo")}
          onIrRegistro={() => setVista("registro")}
        />
      )}

      {vista === "registro" && (
        <RegistroView onIngresar={() => setVista("catalogo")} onIrLogin={() => setVista("login")} />
      )}

      {vista === "mis-pedidos" && <MisPedidosView />}

      {vista === "admin" && <AdminView />}

      <Footer />

      {carritoAbierto && (
        <>
          <div id="overlay" className="overlay" onClick={() => setCarritoAbierto(false)} />
          <CartPanel
            onCerrar={() => setCarritoAbierto(false)}
            onIrCatalogo={() => setVista("catalogo")}
            onCompraConfirmada={recargar}
          />
        </>
      )}

      {productoTalla && (
        <TallaModal
          producto={productoTalla}
          onSeleccionar={elegirTalla}
          onCerrar={() => setProductoTalla(null)}
        />
      )}
    </>
  );
}
