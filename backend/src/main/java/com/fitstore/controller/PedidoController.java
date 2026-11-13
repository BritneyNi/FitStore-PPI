package com.fitstore.controller;

import com.fitstore.dto.response.ApiResponse;
import com.fitstore.dto.response.Paginacion;
import com.fitstore.entity.Pedido;
import com.fitstore.service.OrderService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Pedidos del cliente autenticado. */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

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

    @PatchMapping("/{id}/cancelar")
    public ApiResponse<Pedido> cancelar(@PathVariable Long id, Authentication auth) {
        return ApiResponse.of(orderService.cancelarPedidoCliente(id, auth.getName()));
    }
}
