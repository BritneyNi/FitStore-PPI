package com.fitstore.controller;

import com.fitstore.dto.response.ApiResponse;
import com.fitstore.dto.response.Paginacion;
import com.fitstore.entity.Pedido;
import com.fitstore.repository.PedidoRepository;
import com.fitstore.service.OrderService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Gestión de pedidos para administradores. */
@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPedidoController {

    private final OrderService orderService;
    private final PedidoRepository pedidoRepo;

    @GetMapping
    public ApiResponse<List<Pedido>> listar(
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) {
        return Paginacion.de(pedidoRepo.findAll(), page, limit);
    }

    @PatchMapping("/{id}/estado")
    public ApiResponse<Pedido> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.of(orderService.actualizarEstado(id, body.get("estado")));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> eliminar(@PathVariable Long id) {
        orderService.eliminarPedido(id);
        return ApiResponse.of(Map.of("id", id, "eliminado", true));
    }
}
