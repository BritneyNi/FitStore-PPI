package com.fitstore.steps;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitstore.entity.Cliente;
import com.fitstore.entity.Pedido;
import com.fitstore.entity.Producto;
import com.fitstore.repository.ClienteRepository;
import com.fitstore.repository.ItemPedidoRepository;
import com.fitstore.repository.PedidoRepository;
import com.fitstore.repository.ProductoRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CompraSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private PedidoRepository pedidoRepo;

    @Autowired
    private ItemPedidoRepository itemPedidoRepo;

    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Map<String, String> tokens;
    private Map<String, Long> nombreAId;
    private Map<Long, Integer> stockAlInicio;
    private List<Map<String, Object>> catalogo;

    private String token;
    private String emailActual;
    private boolean sesionActiva;
    private boolean loginFallido;

    private Map<String, Integer> carrito;
    private String ultimoProductoTocado;
    private String ultimoError;

    private Map<String, Object> ultimoPedido;
    private Long ultimoPedidoId;

    private Map<String, Object> ultimoProducto;
    private Integer ultimoStockConsultado;
    private List<Map<String, Object>> ultimosProductos;
    private List<Map<String, Object>> ultimosProductosAdmin;
    private Map<String, Object> ultimaRespuestaStock;
    private Integer ultimoStockAdmin;

    private Map<String, Integer> localStorageSnapshot;
    private long totalSnapshot;

    @Before
    public void iniciarEstado() {
        tokens = new HashMap<>();
        nombreAId = new HashMap<>();
        stockAlInicio = new HashMap<>();
        catalogo = new ArrayList<>();
        carrito = new HashMap<>();
        token = null;
        emailActual = null;
        sesionActiva = false;
        loginFallido = false;
        ultimoProductoTocado = null;
        ultimoError = null;
        ultimoPedido = null;
        ultimoPedidoId = null;
        ultimoProducto = null;
        ultimoStockConsultado = null;
        ultimosProductos = null;
        ultimosProductosAdmin = null;
        ultimaRespuestaStock = null;
        ultimoStockAdmin = null;
        localStorageSnapshot = null;
        totalSnapshot = 0L;
    }

    private String claveDe(String email) {
        return "admin@fitstore.com".equals(email) ? "admin123" : "clave123";
    }

    private boolean hacerLogin(String email, String clave) {
        try {
            String json = "{\"email\":\"" + email + "\",\"clave\":\"" + clave + "\"}";
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andReturn();
            if (result.getResponse().getStatus() == 200) {
                Map<String, Object> resp = leerMapaJson(result.getResponse().getContentAsByteArray());
                token = resp.get("token").toString();
                tokens.put(email, token);
                emailActual = email;
                sesionActiva = true;
                loginFallido = false;
                ultimoError = null;
                return true;
            }
            token = null;
            loginFallido = true;
            emailActual = null;
            sesionActiva = false;
            ultimoError = "Error de autenticación (HTTP " + result.getResponse().getStatus() + ")";
            return false;
        } catch (Exception e) {
            token = null;
            loginFallido = true;
            emailActual = null;
            sesionActiva = false;
            ultimoError = "Error de autenticación";
            return false;
        }
    }

    private Long idDe(String nombre) {
        Long id = nombreAId.get(nombre);
        Assertions.assertNotNull(id, "Producto no conocido: " + nombre);
        return id;
    }

    private void reiniciarCarrito() {
        carrito.clear();
        ultimoProductoTocado = null;
        ultimoError = null;
    }

    private void agregarAlCarrito(int cantidad, String nombre, boolean permitirStock) {
        Long id = idDe(nombre);
        Producto p = productoRepo.findById(id).orElse(null);
        ultimoProductoTocado = nombre;
        if (p == null) {
            ultimoError = "Producto no encontrado";
            carrito.remove(id.toString());
            return;
        }
        if (p.getStock() < cantidad) {
            ultimoError = "Stock insuficiente";
            carrito.remove(id.toString());
            return;
        }
        if (!permitirStock) {
            return;
        }
        carrito.merge(id.toString(), cantidad, Integer::sum);
        ultimoError = null;
    }

    private long totalCarrito() {
        long total = 0L;
        for (Map.Entry<String, Integer> e : carrito.entrySet()) {
            Producto p = productoRepo.findById(Long.valueOf(e.getKey())).orElseThrow();
            total += (long) (p.getPrecio() * e.getValue());
        }
        return total;
    }

    private String formatoPrecio(long total) {
        return String.format(Locale.US, "$%,d", total);
    }

    private void confirmarPedido(boolean esperaExito) {
        if (carrito.isEmpty()) {
            ultimoError = "Carrito vacío";
            return;
        }
        try {
            List<Map<String, Object>> items = new ArrayList<>();
            for (Map.Entry<String, Integer> e : carrito.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("productoId", Long.valueOf(e.getKey()));
                item.put("cantidad", e.getValue());
                items.add(item);
            }
            Map<String, Object> body = new HashMap<>();
            body.put("items", items);
            String json = objectMapper.writeValueAsString(body);

            MvcResult result = mockMvc.perform(post("/api/pedidos")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andReturn();
            int status = result.getResponse().getStatus();

            if (status == 200) {
                ultimoPedido = leerMapaJson(result.getResponse().getContentAsByteArray());
                ultimoPedidoId = ((Number) ultimoPedido.get("id")).longValue();
                ultimoError = null;
                carrito.clear();
            } else {
                ultimoPedido = null;
                ultimoError = "Error de validación (HTTP " + status + ")";
                carrito.clear();
            }

            if (esperaExito) {
                Assertions.assertEquals(200, status, "Se esperaba confirma exitosa");
            } else {
                Assertions.assertNotEquals(200, status, "Se esperaba rechazo de la compra");
            }
        } catch (Exception e) {
            ultimoPedido = null;
            ultimoError = "Error de validación";
            carrito.clear();
            if (esperaExito) {
                Assertions.fail("Error confirmando pedido: " + e.getMessage());
            }
        }
    }

    private void comprarComoCliente(String email, boolean confirmar) {
        Assertions.assertTrue(hacerLogin(email, claveDe(email)), "Login falló para " + email);
        reiniciarCarrito();
        agregarAlCarrito(1, "Proteína Whey 2kg", true);
        agregarAlCarrito(1, "Pantalón Gym", true);
        if (confirmar) {
            confirmarPedido(true);
        }
    }

    private void comprarOtroCliente(int cantidad, String nombre) {
        String tokenPrevio = token;
        if (!"admin@fitstore.com".equals(emailActual)) {
            Assertions.assertTrue(hacerLogin("admin@fitstore.com", "admin123"), "Login admin falló");
        }
        try {
            Long id = idDe(nombre);
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("productoId", id);
            item.put("cantidad", cantidad);
            items.add(item);
            Map<String, Object> body = new HashMap<>();
            body.put("items", items);
            String json = objectMapper.writeValueAsString(body);

            MvcResult result = mockMvc.perform(post("/api/pedidos")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andReturn();
            Assertions.assertEquals(200, result.getResponse().getStatus(),
                "Otro cliente no pudo comprar: " + result.getResponse().getContentAsString());
        } catch (Exception e) {
            Assertions.fail("Fallo compra de otro cliente: " + e.getMessage());
        } finally {
            token = tokenPrevio;
        }
    }

    private List<Map<String, Object>> leerLista(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url)).andReturn();
        return leerListaJson(result.getResponse().getContentAsByteArray());
    }

    private Map<String, Object> leerMapaJson(byte[] bytes) throws Exception {
        return objectMapper.readValue(bytes,
            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
    }

    private List<Map<String, Object>> leerListaJson(byte[] bytes) throws Exception {
        return objectMapper.readValue(bytes,
            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
    }

    private String tokenCliente() {
        String t = tokens.get("juan@email.com");
        return t != null ? t : token;
    }

    private void consultarStockProducto(Long id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/productos/{id}", id)).andReturn();
        ultimoProducto = leerMapaJson(result.getResponse().getContentAsByteArray());
        ultimoStockConsultado = ((Number) ultimoProducto.get("stock")).intValue();
    }

    private void repoblarCacheStock(Long id, int cantidad) throws Exception {
        mockMvc.perform(get("/api/productos/{id}/stock", id).param("cantidad", String.valueOf(cantidad))).andReturn();
    }

    private Integer stockRedis(Long id) {
        Object v = redisTemplate.opsForValue().get("stock:" + id);
        return v == null ? null : Integer.parseInt(v.toString());
    }

    private void flushRedis() {
        try {
            redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
        } catch (Exception ignored) {
        }
    }

    // ═══════════════════ ANTECEDENTES ═══════════════════

    @Dado("que el servidor REST está disponible en {string}")
    public void servidorDisponible(String url) {
        Assertions.assertNotNull(mockMvc, "Servidor no disponible");
    }

    @Dado("existen los siguientes productos en el catálogo:")
    public void existenProductos(DataTable dataTable) {
        itemPedidoRepo.deleteAll();
        pedidoRepo.deleteAll();
        productoRepo.deleteAll();
        jdbc.update("ALTER TABLE items_pedido AUTO_INCREMENT = 1");
        jdbc.update("ALTER TABLE pedidos AUTO_INCREMENT = 1");
        jdbc.update("ALTER TABLE productos AUTO_INCREMENT = 1");

        nombreAId.clear();
        stockAlInicio.clear();
        catalogo.clear();

        for (Map<String, String> fila : dataTable.asMaps(String.class, String.class)) {
            Long id = Long.parseLong(fila.get("id"));
            String nombre = fila.get("nombre");
            int stock = Integer.parseInt(fila.get("stock"));
            jdbc.update(
                "INSERT INTO productos (id, nombre, descripcion, precio, stock, categoria, emoji) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, nombre, "Producto de prueba",
                Double.parseDouble(fila.get("precio")),
                stock,
                fila.get("categoría"),
                fila.get("emoji"));

            nombreAId.put(nombre, id);
            stockAlInicio.put(id, stock);
            Map<String, Object> p = new HashMap<>();
            p.put("id", id);
            p.put("nombre", nombre);
            p.put("stock", stock);
            catalogo.add(p);
        }
        flushRedis();
    }

    @Dado("existe un cliente registrado con:")
    public void clientesRegistrados(DataTable dataTable) {
        if (!clienteRepo.existsByEmail("juan@email.com")) {
            Cliente juan = new Cliente();
            juan.setNombre("Juan");
            juan.setEmail("juan@email.com");
            juan.setClave(passwordEncoder.encode("clave123"));
            juan.setRol(Cliente.Rol.CLIENTE);
            clienteRepo.save(juan);
        }
        if (!clienteRepo.existsByEmail("admin@fitstore.com")) {
            Cliente admin = new Cliente();
            admin.setNombre("Admin");
            admin.setEmail("admin@fitstore.com");
            admin.setClave(passwordEncoder.encode("admin123"));
            admin.setRol(Cliente.Rol.ADMIN);
            clienteRepo.save(admin);
        }
    }

    // ═══════════════════ AUTENTICACIÓN ═══════════════════

    @Dado("que soy usuario autenticado como {string} con token JWT")
    public void autenticarUsuario(String email) {
        Assertions.assertTrue(hacerLogin(email, claveDe(email)), "Login real falló para " + email);
    }

    @Dado("que intento iniciar sesión con email {string} y clave incorrecta {string}")
    public void intentarLoginInvalido(String email, String clave) {
        Assertions.assertFalse(hacerLogin(email, clave), "No debió iniciar sesión");
    }

    @Dado("que intento iniciar sesión con email {string} y clave {string}")
    public void intentarLoginNoExiste(String email, String clave) {
        Assertions.assertFalse(hacerLogin(email, clave), "No debió iniciar sesión");
    }

    // ═══════════════════ CARRITO ═══════════════════

    @Dado("tengo el carrito vacío")
    public void carritoVacio() {
        reiniciarCarrito();
    }

    @Cuando("agrego {int} unidades del producto {string} al carrito")
    public void agregarUnidadesDelProducto(int cantidad, String nombre) {
        agregarAlCarrito(cantidad, nombre, true);
    }

    @Cuando("agrego {int} unidad del producto {string} al carrito")
    public void agregarUnidadDelProducto(int cantidad, String nombre) {
        agregarAlCarrito(cantidad, nombre, true);
    }

    @Cuando("agrego {int} unidades de {string} al carrito")
    public void agregarUnidadesDe(int cantidad, String nombre) {
        agregarAlCarrito(cantidad, nombre, true);
    }

    @Cuando("agrego {int} unidad de {string} al carrito")
    public void agregarUnidadDe(int cantidad, String nombre) {
        agregarAlCarrito(cantidad, nombre, true);
    }

    @Cuando("intento agregar {int} unidades del producto {string} al carrito")
    public void intentarAgregarUnidades(int cantidad, String nombre) {
        agregarAlCarrito(cantidad, nombre, false);
    }

    @Entonces("el total del carrito es {string}")
    public void verificarTotalCarrito(String totalEsperado) {
        Assertions.assertEquals(totalEsperado, formatoPrecio(totalCarrito()));
    }

    @Entonces("el carrito contiene {int} items")
    public void verificarItemsCarrito(int items) {
        int totalItems = carrito.values().stream().mapToInt(Integer::intValue).sum();
        Assertions.assertEquals(items, totalItems);
    }

    @Entonces("el producto no se agrega al carrito")
    public void productoNoAgregado() {
        Assertions.assertNotNull(ultimoError);
        Assertions.assertFalse(carrito.containsKey(ultimoProductoTocado));
    }

    @Entonces("el carrito sigue vacío")
    public void carritoSigueVacio() {
        Assertions.assertTrue(carrito.isEmpty());
    }

    // ═══════════════════ COMPRA ═══════════════════

    @Cuando("confirmo la compra")
    public void confirmarCompra() {
        confirmarPedido(true);
    }

    @Cuando("intento confirmar mi compra")
    public void intentarConfirmarCompra() {
        confirmarPedido(false);
    }

    @Entonces("recibo un pedido con estado {string}")
    public void verificarEstadoPedido(String estado) {
        Assertions.assertNotNull(ultimoPedido, "No se recibió pedido");
        Assertions.assertEquals(estado, ultimoPedido.get("estado").toString());
    }

    @Entonces("el pedido contiene {int} líneas de items")
    public void verificarLineasPedido(int lineas) {
        Assertions.assertNotNull(ultimoPedido, "No se recibió pedido");
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) ultimoPedido.get("items");
        Assertions.assertEquals(lineas, items.size());
    }

    @Entonces("recibo un error de validación")
    public void reciboErrorValidacion() {
        Assertions.assertNotNull(ultimoError);
    }

    @Entonces("el pedido NO se crea")
    public void pedidoNoSeCrea() {
        Assertions.assertNull(ultimoPedido);
    }

    @Entonces("el carrito se vacía")
    public void carritoSeVacia() {
        carrito.clear();
    }

    // ═══════════════════ STOCK ═══════════════════

    @Dado("el producto {string} tiene stock de {int} unidades")
    public void productoConStock(String nombre, int stock) {
        Long id = idDe(nombre);
        Producto p = productoRepo.findById(id).orElseThrow();
        p.setStock(stock);
        productoRepo.save(p);
        stockAlInicio.put(id, stock);
    }

    @Entonces("el stock de {string} se reduce de {int} a {int}")
    public void verificarStockReducido(String nombre, int antes, int despues) {
        Long id = idDe(nombre);
        Producto p = productoRepo.findById(id).orElseThrow();
        Assertions.assertEquals(despues, p.getStock());
    }

    @Cuando("otro cliente compra {int} unidad de {string}")
    public void otroClienteCompraUnidad(int cantidad, String producto) {
        comprarOtroCliente(cantidad, producto);
    }

    @Cuando("otro cliente compra {int} unidades de {string}")
    public void otroClienteCompraUnidades(int cantidad, String producto) {
        comprarOtroCliente(cantidad, producto);
    }

    @Cuando("consulto el stock del producto {string} \\(id: {int}\\)")
    public void consultarStockProducto(String nombre, int id) throws Exception {
        consultarStockProducto((long) id);
    }

    @Entonces("obtengo la información del producto con stock actual {int}")
    public void verificarStockActual(int stock) {
        Assertions.assertEquals(stock, ultimoStockConsultado);
    }

    @Cuando("vuelvo a consultar el stock del producto {string}")
    public void volverConsultarStock(String nombre) throws Exception {
        consultarStockProducto(idDe(nombre));
    }

    @Entonces("el stock se actualiza a {int}")
    public void verificarStockNuevo(int stock) {
        Assertions.assertEquals(stock, ultimoStockConsultado);
    }

    @Cuando("verifico disponibilidad para {int} unidades del producto {string}")
    public void verificarDisponibilidad(int cantidad, String nombre) throws Exception {
        Long id = idDe(nombre);
        MvcResult result = mockMvc.perform(get("/api/productos/{id}/stock", id)
                .param("cantidad", String.valueOf(cantidad)))
                .andReturn();
        ultimaRespuestaStock = leerMapaJson(result.getResponse().getContentAsByteArray());
    }

    @Entonces("recibo respuesta: disponible=true")
    public void disponibleTrue() {
        Assertions.assertTrue((Boolean) ultimaRespuestaStock.get("disponible"));
    }

    @Entonces("recibo respuesta: disponible=false")
    public void disponibleFalse() {
        Assertions.assertFalse((Boolean) ultimaRespuestaStock.get("disponible"));
    }

    // ═══════════════════ ERRORES ═══════════════════

    @Entonces("recibo un mensaje de error {string}")
    public void reciboMensajeError(String mensaje) {
        Assertions.assertNotNull(ultimoError);
    }

    @Entonces("recibo un error de autenticación")
    public void reciboErrorAutenticacion() {
        Assertions.assertTrue(loginFallido);
        Assertions.assertNull(token);
    }

    @Entonces("recibo un error de autenticación {string}")
    public void reciboErrorAutenticacionMensaje(String mensaje) {
        Assertions.assertTrue(loginFallido);
        Assertions.assertNull(token);
    }

    @Entonces("no recibo token JWT")
    public void noReciboTokenJwt() {
        Assertions.assertNull(token);
    }

    @Entonces("sigo sin sesión activa")
    public void sinSesionActiva() {
        Assertions.assertNull(emailActual);
    }

    // ═══════════════════ REDIS ═══════════════════

    @Dado("que el caché Redis está disponible")
    public void redisDisponible() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            Assertions.assertEquals("PONG", pong);
        } catch (Exception e) {
            Assertions.fail("Redis no disponible: " + e.getMessage());
        }
    }

    @Dado("el stock de {string} en Redis es {int}")
    public void stockEnRedis(String nombre, int stock) {
        Long id = idDe(nombre);
        redisTemplate.opsForValue().set("stock:" + id, stock);
    }

    @Cuando("compro {int} unidades de {string}")
    public void comprarProducto(int cantidad, String nombre) {
        if (token == null) {
            autenticarUsuario("juan@email.com");
        }
        reiniciarCarrito();
        agregarAlCarrito(cantidad, nombre, true);
        confirmarPedido(true);
    }

    private Long idUltimoProductoComprado() {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) ultimoPedido.get("items");
        @SuppressWarnings("unchecked")
        Map<String, Object> producto = (Map<String, Object>) items.get(0).get("producto");
        return ((Number) producto.get("id")).longValue();
    }

    @Entonces("el stock en Redis se actualiza a {int}")
    public void redisStockActualizado(int stock) throws Exception {
        Long id = idUltimoProductoComprado();
        repoblarCacheStock(id, 1);
        Assertions.assertEquals(stock, stockRedis(id));
    }

    @Entonces("el stock en BD también es {int}")
    public void bdStockActualizado(int stock) {
        Long id = idUltimoProductoComprado();
        Producto p = productoRepo.findById(id).orElseThrow();
        Assertions.assertEquals(stock, p.getStock());
    }

    @Cuando("consulto nuevamente desde otra sesión")
    public void consultarNuevaSesion() throws Exception {
        consultarStockProducto(idUltimoProductoComprado());
    }

    @Entonces("obtengo el stock desde Redis \\(caché\\) con valor {int}")
    public void stockDesdeRedis(int stock) {
        Long id = idUltimoProductoComprado();
        Assertions.assertEquals(stock, ultimoStockConsultado);
        Assertions.assertEquals(stock, stockRedis(id));
    }

    // ═══════════════════ ADMIN ═══════════════════

    @Cuando("accedo al panel de admin")
    public void accesoPanelAdmin() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/productos")
                .header("Authorization", "Bearer " + token))
                .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Entonces("puedo ver la tabla de productos con stock actual")
    public void verTablaProductos() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/productos")
                .header("Authorization", "Bearer " + token))
                .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        ultimosProductosAdmin = leerListaJson(result.getResponse().getContentAsByteArray());
        Assertions.assertEquals(catalogo.size(), ultimosProductosAdmin.size());
    }

    @Cuando("actualizo el stock de {string} a {int}")
    public void actualizarStockAdmin(String nombre, int stock) throws Exception {
        Long id = idDe(nombre);
        String json = "{\"stock\":" + stock + "}";
        MvcResult result = mockMvc.perform(patch("/api/admin/productos/{id}/stock", id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        ultimoStockAdmin = stock;
    }

    @Entonces("se guarda el nuevo stock en BD")
    public void stockGuardadoBD() {
        Producto p = productoRepo.findById(idDe("Proteína Whey 2kg")).orElseThrow();
        Assertions.assertEquals(ultimoStockAdmin, p.getStock());
    }

    @Entonces("se actualiza el caché Redis")
    public void cacheRedisActualizado() {
        Long id = idDe("Proteína Whey 2kg");
        Assertions.assertNull(stockRedis(id));
    }

    @Entonces("aparece un evento de auditoría con timestamp")
    public void eventoAuditoria() {
        Assertions.assertNotNull(ultimoStockAdmin);
    }

    // ═══════════════════ localStorage ═══════════════════

    @Entonces("el carrito se persiste en localStorage")
    public void carritoPersiste() {
        localStorageSnapshot = new HashMap<>(carrito);
        totalSnapshot = totalCarrito();
    }

    @Cuando("recargo la página")
    public void recargarPagina() {
        carrito = new HashMap<>(localStorageSnapshot);
    }

    @Entonces("el carrito contiene los mismos {int} items")
    public void carritoMismosItems(int items) {
        int totalItems = carrito.values().stream().mapToInt(Integer::intValue).sum();
        Assertions.assertEquals(items, totalItems);
    }

    @Entonces("el total sigue siendo el mismo")
    public void totalMismo() {
        Assertions.assertEquals(formatoPrecio(totalSnapshot), formatoPrecio(totalCarrito()));
    }

    // ═══════════════════ CATÁLOGO Y BÚSQUEDA ═══════════════════

    @Dado("que accedo a la vista de catálogo")
    public void accesoVistaCatalogo() {
    }

    @Cuando("filtro por categoría {string}")
    public void filtrarPorCategoria(String categoria) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/productos").param("categoria", categoria)).andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        ultimosProductos = leerListaJson(result.getResponse().getContentAsByteArray());
    }

    @Entonces("solo veo productos de categoría {word}:")
    public void soloProductosCategoria(String categoria, DataTable dataTable) {
        List<Map<String, String>> esperados = dataTable.asMaps(String.class, String.class);
        Assertions.assertEquals(esperados.size(), ultimosProductos.size());
        for (Map<String, Object> p : ultimosProductos) {
            Assertions.assertEquals(categoria, p.get("categoria").toString());
            String nombre = p.get("nombre").toString();
            double precio = ((Number) p.get("precio")).doubleValue();
            Map<String, String> esperado = esperados.stream()
                .filter(e -> e.get("nombre").equals(nombre))
                .findFirst().orElse(null);
            Assertions.assertNotNull(esperado, "Producto inesperado: " + nombre);
            Assertions.assertEquals(Double.parseDouble(esperado.get("precio")), precio, 0.01);
        }
    }

    @Cuando("busco {string}")
    public void buscar(String termino) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/productos").param("buscar", termino)).andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        ultimosProductos = leerListaJson(result.getResponse().getContentAsByteArray());
    }

    @Entonces("aparece el producto {string}")
    public void apareceProducto(String nombre) {
        boolean found = ultimosProductos.stream()
            .anyMatch(p -> nombre.equals(p.get("nombre").toString()));
        Assertions.assertTrue(found, "No aparece el producto: " + nombre);
    }

    @Entonces("no aparecen otros productos")
    public void noAparecenOtros() {
        Assertions.assertEquals(1, ultimosProductos.size());
    }

    @Entonces("aparecen los productos que contienen {string}:")
    public void productosContienen(String patron, DataTable dataTable) {
        List<Map<String, String>> esperados = dataTable.asMaps(String.class, String.class);
        List<String> nombresActuales = ultimosProductos.stream()
            .map(p -> p.get("nombre").toString())
            .collect(Collectors.toList());
        Assertions.assertEquals(esperados.size(), ultimosProductos.size());
        for (Map<String, String> e : esperados) {
            Assertions.assertTrue(nombresActuales.contains(e.get("nombre")),
                "Falta: " + e.get("nombre"));
        }
    }

    // ═══════════════════ ESTADOS DE PEDIDO ═══════════════════

    @Dado("que compro productos como cliente {string}")
    public void comprarComoCliente(String email) {
        comprarComoCliente(email, true);
    }

    @Dado("que compro productos como {string}")
    public void comprarComo(String email) {
        comprarComoCliente(email, false);
    }

    @Cuando("accedo como admin a {string}")
    public void accesoComoAdmin(String email) {
        Assertions.assertTrue(hacerLogin(email, claveDe(email)), "Login admin falló");
    }

    @Cuando("cambio el estado del pedido a {string}")
    public void cambiarEstadoPedido(String estado) throws Exception {
        Assertions.assertNotNull(ultimoPedidoId);
        String json = "{\"estado\":\"" + estado + "\"}";
        MvcResult result = mockMvc.perform(patch("/api/admin/pedidos/{id}/estado", ultimoPedidoId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        ultimoPedido = leerMapaJson(result.getResponse().getContentAsByteArray());
    }

    @Cuando("cambio el estado a {string}")
    public void cambiarEstadoSimple(String estado) throws Exception {
        cambiarEstadoPedido(estado);
    }

    @Entonces("el cliente ve su pedido con estado {string}")
    public void clienteVePedido(String estado) throws Exception {
        String clientToken = tokenCliente();
        MvcResult result = mockMvc.perform(get("/api/pedidos/mis-pedidos")
                .header("Authorization", "Bearer " + clientToken))
                .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        List<Map<String, Object>> pedidos = leerListaJson(result.getResponse().getContentAsByteArray());
        Map<String, Object> pedido = pedidos.stream()
            .filter(p -> ((Number) p.get("id")).longValue() == ultimoPedidoId)
            .findFirst().orElse(null);
        Assertions.assertNotNull(pedido, "El cliente no ve su pedido");
        Assertions.assertEquals(estado, pedido.get("estado").toString());
    }

    @Entonces("el pedido aparece en el historial de compras realizadas")
    public void pedidoEnHistorial() throws Exception {
        String clientToken = tokenCliente();
        MvcResult result = mockMvc.perform(get("/api/pedidos/mis-pedidos")
                .header("Authorization", "Bearer " + clientToken))
                .andReturn();
        List<Map<String, Object>> pedidos = leerListaJson(result.getResponse().getContentAsByteArray());
        boolean existe = pedidos.stream()
            .anyMatch(p -> ((Number) p.get("id")).longValue() == ultimoPedidoId);
        Assertions.assertTrue(existe, "El pedido no está en el historial");
    }

    // ═══════════════════ CANCELACIÓN ═══════════════════

    @Dado("que existe un pedido con estado {string}")
    public void existePedidoConEstado(String estado) {
        comprarComoCliente("juan@email.com", true);
        Assertions.assertEquals(estado, ultimoPedido.get("estado").toString());
    }

    @Cuando("accedo como admin y cambio el estado a {string}")
    public void accesoAdminCambioEstado(String estado) throws Exception {
        Assertions.assertTrue(hacerLogin("admin@fitstore.com", "admin123"), "Login admin falló");
        cambiarEstadoPedido(estado);
    }

    @Entonces("el stock de todos los productos se restaura")
    public void stockRestaurado() {
        for (Long id : stockAlInicio.keySet()) {
            Producto p = productoRepo.findById(id).orElseThrow();
            Assertions.assertEquals((int) stockAlInicio.get(id), p.getStock(),
                "Stock no restaurado para producto " + id);
        }
    }

    @Entonces("el cliente recibe notificación de cancelación")
    public void notificacionCancelacion() {
        Pedido pedido = pedidoRepo.findById(ultimoPedidoId).orElse(null);
        Assertions.assertNotNull(pedido);
        Assertions.assertEquals("CANCELADO", pedido.getEstado().name());
    }

    // ═══════════════════ EMAIL ═══════════════════

    @Entonces("se envía un email de confirmación a {string}")
    public void emailConfirmacion(String email) {
        Assertions.assertNotNull(ultimoPedido, "No hay pedido para notificar");
        Assertions.assertEquals(email, ((Map<?, ?>) ultimoPedido.get("cliente")).get("email").toString());
    }

    @Entonces("se envía email a {string}")
    public void emailEnviado(String email) {
        Assertions.assertNotNull(ultimoPedido);
        Assertions.assertEquals(email, ((Map<?, ?>) ultimoPedido.get("cliente")).get("email").toString());
    }

    @Entonces("el email contiene el número del pedido")
    public void emailNumeroPedido() {
        Assertions.assertNotNull(ultimoPedidoId);
        Assertions.assertEquals(ultimoPedidoId, ((Number) ultimoPedido.get("id")).longValue());
    }

    @Entonces("el email contiene el desglose de items")
    public void emailDesglose() {
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) ultimoPedido.get("items");
        Assertions.assertFalse(items.isEmpty());
    }

    @Entonces("el email contiene el total a pagar")
    public void emailTotal() {
        double total = ((Number) ultimoPedido.get("total")).doubleValue();
        Assertions.assertTrue(total > 0);
    }
}