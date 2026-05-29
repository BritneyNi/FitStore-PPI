package com.fitstore.controller;

import com.fitstore.entity.Pedido;
import com.fitstore.entity.Producto;
import com.fitstore.service.AuthService;
import com.fitstore.service.OrderService;
import com.fitstore.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ── /api/auth ──────────────────────────────────────────────

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
            authService.login(body.get("email"), body.get("clave"))
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
            authService.registrar(body.get("nombre"), body.get("email"), body.get("clave"))
        );
    }
}

// ── /api/productos ─────────────────────────────────────────

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String buscar) {

        if (categoria != null && !categoria.isEmpty()) {
            return ResponseEntity.ok(productoService.listarPorCategoria(categoria));
        }
        if (buscar != null && !buscar.isEmpty()) {
            return ResponseEntity.ok(productoService.buscarPorNombre(buscar));
        }
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // RF-02: verificar disponibilidad de stock (RNF-01: < 1 segundo)
    @GetMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> verificarStock(
            @PathVariable Long id,
            @RequestParam int cantidad) {

        boolean disponible = productoService.verificarStock(id, cantidad);
        return ResponseEntity.ok(Map.of(
            "productoId",  id,
            "cantidad",    cantidad,
            "disponible",  disponible
        ));
    }
}

// ── /api/pedidos ───────────────────────────────────────────

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
class PedidoController {

    private final OrderService orderService;

    // RF-03: confirmar compra — requiere JWT
    @PostMapping
    public ResponseEntity<Pedido> confirmar(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items =
            (List<Map<String, Object>>) body.get("items");

        Pedido pedido = orderService.confirmarPedido(auth.getName(), items);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> misPedidos(Authentication auth) {
        return ResponseEntity.ok(orderService.obtenerPedidosCliente(auth.getName()));
    }
}

// ── /api/admin ─────────────────────────────────────────────

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
class AdminController {

    private final ProductoService productoService;
    private final OrderService    orderService;

    // RF-04: gestión de inventario
    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> listarProductos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @PatchMapping("/productos/{id}/stock")
    public ResponseEntity<Producto> actualizarStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(productoService.actualizarStock(id, body.get("stock")));
    }

    @GetMapping("/productos/stock-bajo")
    public ResponseEntity<List<Producto>> stockBajo() {
        return ResponseEntity.ok(productoService.productosConStockBajo());
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<Pedido>> todosPedidos() {
        return ResponseEntity.ok(orderService.obtenerPedidosCliente(""));
    }

    @PatchMapping("/pedidos/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.actualizarEstado(id, body.get("estado")));
    }
}
