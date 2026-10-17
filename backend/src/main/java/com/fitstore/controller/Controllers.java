package com.fitstore.controller;

import com.fitstore.dto.response.ApiResponse;
import com.fitstore.dto.response.Paginacion;
import com.fitstore.entity.Pedido;
import com.fitstore.entity.Producto;
import com.fitstore.repository.PedidoRepository;
import com.fitstore.service.AuthService;
import com.fitstore.service.OrderService;
import com.fitstore.service.ProductoService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// ── /api/auth ──────────────────────────────────────────────

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody Map<String, String> body) {
        return ApiResponse.of(authService.login(body.get("email"), body.get("clave")));
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, String>> register(@RequestBody Map<String, String> body) {
        return ApiResponse.of(authService.registrar(body.get("nombre"), body.get("email"), body.get("clave")));
    }
}

// ── /api/productos ─────────────────────────────────────────

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ApiResponse<List<Producto>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {

        List<Producto> productos;
        if (categoria != null && !categoria.isEmpty()) {
            productos = productoService.listarPorCategoria(categoria);
        } else if (buscar != null && !buscar.isEmpty()) {
            productos = productoService.buscarPorNombre(buscar);
        } else {
            productos = productoService.listarTodos();
        }
        return Paginacion.de(productos, page, limit);
    }

    @GetMapping("/{id}")
    public ApiResponse<Producto> obtener(@PathVariable Long id) {
        return ApiResponse.of(productoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/stock")
    public ApiResponse<Map<String, Object>> verificarStock(@PathVariable Long id, @RequestParam int cantidad) {

        boolean disponible = productoService.verificarStock(id, cantidad);
        return ApiResponse.of(Map.of(
                "productoId", id,
                "cantidad", cantidad,
                "disponible", disponible));
    }
}

// ── /api/pedidos ───────────────────────────────────────────

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
class PedidoController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<Pedido> confirmar(@RequestBody Map<String, Object> body, Authentication auth) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

        return ApiResponse.of(orderService.confirmarPedido(auth.getName(), items));
    }

    @GetMapping("/mis-pedidos")
    public ApiResponse<List<Pedido>> misPedidos(
            Authentication auth,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {

        return Paginacion.de(orderService.obtenerPedidosCliente(auth.getName()), page, limit);
    }
}

// ── /api/admin ─────────────────────────────────────────────

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
class AdminController {

    private final ProductoService productoService;
    private final OrderService orderService;
    private final PedidoRepository pedidoRepo;

    @GetMapping("/productos")
    public ApiResponse<List<Producto>> listarProductos(
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) {
        return Paginacion.de(productoService.listarTodos(), page, limit);
    }

    @PatchMapping("/productos/{id}/stock")
    public ApiResponse<Producto> actualizarStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return ApiResponse.of(productoService.actualizarStock(id, body.get("stock")));
    }

    @GetMapping("/productos/stock-bajo")
    public ApiResponse<List<Producto>> stockBajo() {
        return Paginacion.de(productoService.productosConStockBajo(), null, null);
    }

    @GetMapping("/pedidos")
    public ApiResponse<List<Pedido>> todosPedidos(
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) {
        return Paginacion.de(pedidoRepo.findAll(), page, limit);
    }

    @PatchMapping("/pedidos/{id}/estado")
    public ApiResponse<Pedido> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.of(orderService.actualizarEstado(id, body.get("estado")));
    }
}
