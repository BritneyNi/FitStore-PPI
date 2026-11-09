package com.fitstore.controller;

import com.fitstore.dto.response.ApiResponse;
import com.fitstore.dto.response.Paginacion;
import com.fitstore.entity.Producto;
import com.fitstore.service.ProductoService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consultas públicas del catálogo. */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

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
