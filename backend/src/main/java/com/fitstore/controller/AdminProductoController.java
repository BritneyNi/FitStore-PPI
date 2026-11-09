package com.fitstore.controller;

import com.fitstore.dto.response.ApiResponse;
import com.fitstore.dto.response.Paginacion;
import com.fitstore.entity.Producto;
import com.fitstore.service.ProductoService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Gestión del inventario para administradores. */
@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ApiResponse<List<Producto>> listar(
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) {
        return Paginacion.de(productoService.listarTodos(), page, limit);
    }

    @PatchMapping("/{id}/stock")
    public ApiResponse<Producto> actualizarStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return ApiResponse.of(productoService.actualizarStock(id, body.get("stock")));
    }

    @GetMapping("/stock-bajo")
    public ApiResponse<List<Producto>> stockBajo() {
        return Paginacion.de(productoService.productosConStockBajo(), null, null);
    }
}
