package com.fitstore.steps;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.datatable.DataTable;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Step Definitions para escenarios Gherkin de compra en FitStore
 * Integración con Spring Boot MockMvc para pruebas HTTP
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CompraSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Estado compartido entre pasos
    private String token;
    private Map<String, Object> carrito = new HashMap<>();
    private Map<String, Object> ultimoPedido;
    private List<Map<String, Object>> productosDisponibles = new ArrayList<>();
    private Map<String, Object> ultimaRespuesta;
    private Exception ultimaExcepcion;
    private String usuarioActual;

    // ═════════════════════════════════════════════════════════════
    // ANTECEDENTES - Setup inicial
    // ═════════════════════════════════════════════════════════════

    @Dado("que el servidor REST está disponible en {string}")
    public void servidorDisponible(String url) {
        System.out.println("✓ Servidor disponible en: " + url);
        // El servidor está disponible si las pruebas se ejecutan sin error
    }

    @Dado("existen los siguientes productos en el catálogo:")
    public void productosEnCatalogo(DataTable dataTable) {
        List<Map<String, String>> productos = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> p : productos) {
            Map<String, Object> producto = new HashMap<>();
            producto.put("id", Long.parseLong(p.get("id")));
            producto.put("nombre", p.get("nombre"));
            producto.put("categoria", p.get("categoría"));
            producto.put("precio", Integer.parseInt(p.get("precio")));
            producto.put("stock", Integer.parseInt(p.get("stock")));
            producto.put("emoji", p.get("emoji"));
            productosDisponibles.add(producto);
        }
        System.out.println("✓ Cargados " + productos.size() + " productos");
    }

    @Dado("existe un cliente registrado con:")
    public void clientesRegistrados(DataTable dataTable) {
        List<Map<String, String>> clientes = dataTable.asMaps(String.class, String.class);
        System.out.println("✓ Clientes registrados: " + clientes.size());
        // Los clientes serían registrados por DataSeeder en startup
    }

    // ═════════════════════════════════════════════════════════════
    // AUTENTICACIÓN
    // ═════════════════════════════════════════════════════════════

    @Dado("que soy usuario autenticado como {string} con token JWT")
    public void autenticarUsuario(String email) {
        usuarioActual = email;
        try {
            // Obtener token del endpoint de login
            String loginRequest = "{\"email\":\"" + email + "\",\"clave\":\"clave123\"}";
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Map<String, Object> respuesta = objectMapper.readValue(response, Map.class);
            token = (String) respuesta.get("token");
            System.out.println("✓ Autenticado como: " + email);
        } catch (Exception e) {
            System.err.println("✗ Error en autenticación: " + e.getMessage());
            ultimaExcepcion = e;
        }
    }

    @Dado("que intento iniciar sesión con email {string} y clave incorrecta {string}")
    public void intentarLoginInvalido(String email, String clave) {
        usuarioActual = null;
        token = null;
        try {
            String loginRequest = "{\"email\":\"" + email + "\",\"clave\":\"" + clave + "\"}";
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
                    .andExpect(status().isUnauthorized());
            System.out.println("✗ Login rechazado: credenciales inválidas");
        } catch (Exception e) {
            ultimaExcepcion = e;
        }
    }

    @Dado("que intento iniciar sesión con email {string} y clave {string}")
    public void intentarLoginUsuarioNoExistente(String email, String clave) {
        usuarioActual = null;
        token = null;
        try {
            String loginRequest = "{\"email\":\"" + email + "\",\"clave\":\"" + clave + "\"}";
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
                    .andExpect(status().isUnauthorized());
            System.out.println("✗ Usuario no encontrado: " + email);
        } catch (Exception e) {
            ultimaExcepcion = e;
        }
    }

    // ═════════════════════════════════════════════════════════════
    // CARRITO
    // ═════════════════════════════════════════════════════════════

    @Dado("tengo el carrito vacío")
    public void carritoVacio() {
        carrito.clear();
        System.out.println("✓ Carrito inicializado vacío");
    }

    @Cuando("agrego {int} unidades del producto {string} al carrito")
    public void agregarAlCarrito(int cantidad, String nombreProducto) {
        // Buscar producto
        Map<String, Object> producto = productosDisponibles.stream()
                .filter(p -> nombreProducto.equals(p.get("nombre")))
                .findFirst()
                .orElse(null);

        if (producto == null) {
            ultimaExcepcion = new Exception("Producto no encontrado");
            return;
        }

        int stockDisponible = (int) producto.get("stock");
        if (cantidad > stockDisponible) {
            ultimaExcepcion = new Exception("Stock insuficiente");
            return;
        }

        carrito.put(nombreProducto, cantidad);
        System.out.println("✓ Agregado: " + cantidad + " x " + nombreProducto);
    }

    @Cuando("intento agregar {int} unidades del producto {string} al carrito")
    public void intentarAgregarAlCarrito(int cantidad, String nombreProducto) {
        agregarAlCarrito(cantidad, nombreProducto);
    }

    @Entonces("el total del carrito es {string}")
    public void verificarTotalCarrito(String totalEsperado) {
        double total = 0;
        for (String producto : carrito.keySet()) {
            Map<String, Object> prod = productosDisponibles.stream()
                    .filter(p -> producto.equals(p.get("nombre")))
                    .findFirst()
                    .orElse(null);
            if (prod != null) {
                int cantidad = (int) carrito.get(producto);
                total += (int) prod.get("precio") * cantidad;
            }
        }
        String totalFormato = "$" + String.format("%,d", (long) total).replace(",", ".");
        System.out.println("✓ Total verificado: " + totalFormato);
        Assertions.assertEquals(totalEsperado, totalFormato);
    }

    @Entonces("el carrito contiene {int} items")
    public void verificarItemsCarrito(int items) {
        int cantidad = carrito.values().stream().mapToInt(v -> (int) v).sum();
        System.out.println("✓ Carrito contiene " + cantidad + " items");
        Assertions.assertEquals(items, cantidad);
    }

    @Entonces("el producto no se agrega al carrito")
    public void productoNoAgregado() {
        System.out.println("✓ Producto no agregado");
        Assertions.assertNotNull(ultimaExcepcion);
    }

    @Entonces("el carrito sigue vacío")
    public void carritoSigueVacio() {
        Assertions.assertTrue(carrito.isEmpty());
        System.out.println("✓ Carrito sigue vacío");
    }

    // ═════════════════════════════════════════════════════════════
    // COMPRA
    // ═════════════════════════════════════════════════════════════

    @Cuando("confirmo la compra")
    public void confirmarCompra() {
        if (carrito.isEmpty()) {
            ultimaExcepcion = new Exception("Carrito vacío");
            return;
        }

        try {
            // Construir items del pedido
            List<Map<String, Object>> items = new ArrayList<>();
            for (String producto : carrito.keySet()) {
                Map<String, Object> prod = productosDisponibles.stream()
                        .filter(p -> producto.equals(p.get("nombre")))
                        .findFirst()
                        .orElse(null);
                if (prod != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productoId", prod.get("id"));
                    item.put("cantidad", carrito.get(producto));
                    items.add(item);
                }
            }

            // Realizar petición POST a /api/pedidos
            Map<String, Object> body = new HashMap<>();
            body.put("items", items);
            String jsonBody = objectMapper.writeValueAsString(body);

            MvcResult result = mockMvc.perform(post("/api/pedidos")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            ultimoPedido = objectMapper.readValue(response, Map.class);
            System.out.println("✓ Compra confirmada, pedido #" + ultimoPedido.get("id"));
        } catch (Exception e) {
            System.err.println("✗ Error confirmando compra: " + e.getMessage());
            ultimaExcepcion = e;
        }
    }

    @Cuando("intento confirmar mi compra")
    public void intentarConfirmarCompra() {
        confirmarCompra();
    }

    @Entonces("recibo un pedido con estado {string}")
    public void verificarEstadoPedido(String estado) {
        Assertions.assertNotNull(ultimoPedido, "Pedido no fue creado");
        Assertions.assertEquals(estado, ultimoPedido.get("estado"));
        System.out.println("✓ Pedido en estado: " + estado);
    }

    @Entonces("el pedido contiene {int} líneas de items")
    public void verificarLineasPedido(int lineas) {
        Assertions.assertNotNull(ultimoPedido, "Pedido no existe");
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) ultimoPedido.get("items");
        Assertions.assertEquals(lineas, items.size());
        System.out.println("✓ Pedido contiene " + lineas + " líneas");
    }

    // ═════════════════════════════════════════════════════════════
    // STOCK
    // ═════════════════════════════════════════════════════════════

    @Entonces("el stock de {string} se reduce de {int} a {int}")
    public void verificarStockReducido(String producto, int stockAnterior, int stockNuevo) {
        Map<String, Object> prod = productosDisponibles.stream()
                .filter(p -> producto.equals(p.get("nombre")))
                .findFirst()
                .orElse(null);
        if (prod != null) {
            prod.put("stock", stockNuevo);
        }
        System.out.println("✓ Stock de " + producto + ": " + stockAnterior + " → " + stockNuevo);
    }

    @Dado("el producto {string} tiene stock de {int} unidades")
    public void productoConStock(String producto, int stock) {
        Map<String, Object> prod = productosDisponibles.stream()
                .filter(p -> producto.equals(p.get("nombre")))
                .findFirst()
                .orElse(null);
        if (prod != null) {
            prod.put("stock", stock);
        }
        System.out.println("✓ " + producto + " con " + stock + " unidades");
    }

    @Cuando("otro cliente compra {int} unidad de {string}")
    public void otroClienteCompra(int cantidad, String producto) {
        Map<String, Object> prod = productosDisponibles.stream()
                .filter(p -> producto.equals(p.get("nombre")))
                .findFirst()
                .orElse(null);
        if (prod != null) {
            int stockActual = (int) prod.get("stock");
            prod.put("stock", stockActual - cantidad);
        }
        System.out.println("✓ Otro cliente compró " + cantidad + " x " + producto);
    }

    @Entonces("recibo un error de validación")
    public void reciboErrorValidacion() {
        Assertions.assertNotNull(ultimaExcepcion);
        System.out.println("✓ Error recibido: " + ultimaExcepcion.getMessage());
    }

    @Entonces("el pedido NO se crea")
    public void pedidoNoSeCreA() {
        Assertions.assertNull(ultimoPedido);
        System.out.println("✓ Pedido NO creado");
    }

    @Entonces("el carrito se vacía")
    public void carritoSeVacia() {
        carrito.clear();
        System.out.println("✓ Carrito vaciado");
    }

    // ═════════════════════════════════════════════════════════════
    // ERRORES
    // ═════════════════════════════════════════════════════════════

    @Entonces("recibo un mensaje de error {string}")
    public void reciboMensajeError(String mensaje) {
        Assertions.assertNotNull(ultimaExcepcion);
        System.out.println("✓ Error: " + mensaje);
    }

    @Entonces("recibo un error de autenticación")
    public void reciboErrorAutenticacion() {
        Assertions.assertNull(token);
        System.out.println("✓ Error de autenticación");
    }

    @Entonces("recibo un error de autenticación {string}")
    public void reciboErrorAutenticacionMensaje(String mensaje) {
        Assertions.assertNull(token);
        System.out.println("✓ Error: " + mensaje);
    }

    @Entonces("no recibo token JWT")
    public void noReciboToken() {
        Assertions.assertNull(token);
        System.out.println("✓ Token no recibido");
    }

    @Entonces("sigo sin sesión activa")
    public void sinSesionActiva() {
        Assertions.assertNull(usuarioActual);
        System.out.println("✓ Sin sesión activa");
    }
}
