package com.fitstore.service;

import com.fitstore.entity.*;
import com.fitstore.exception.FitStoreException;
import com.fitstore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final PedidoRepository     pedidoRepo;
    private final ClienteRepository    clienteRepo;
    private final ProductoService      productoService;
    private final NotificationService  notificationService;

    // ── Confirmar pedido — flujo transaccional completo ──────
    //
    // 1. Validar cliente (JWT ya verificado por el filtro)
    // 2. Verificar stock de cada item (Redis Cache-Aside)
    // 3. Crear pedido ACID en MySQL
    // 4. Descontar stock y limpiar caché Redis
    // 5. Publicar evento en RabbitMQ (async — no bloquea)
    //
    @Transactional
    public Pedido confirmarPedido(String emailCliente, List<Map<String, Object>> items) {

        // 1. Obtener cliente
        Cliente cliente = clienteRepo.findByEmail(emailCliente)
            .orElseThrow(() -> new FitStoreException("Cliente no encontrado"));

        // 2. Verificar stock de todos los items antes de proceder
        for (Map<String, Object> item : items) {
            Long   productoId = Long.valueOf(item.get("productoId").toString());
            int    cantidad   = Integer.parseInt(item.get("cantidad").toString());
            Producto producto = productoService.obtenerPorId(productoId);

            if (!productoService.verificarStock(productoId, cantidad)) {
                throw new FitStoreException("Stock insuficiente: " + producto.getNombre()
                    + " (solicitado: " + cantidad + ")");
            }
        }

        // 3. Crear pedido en MySQL con @Transactional (ACID)
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(Pedido.Estado.CONFIRMADO);

        List<ItemPedido> itemsPedido = new ArrayList<>();
        double total = 0.0;

        for (Map<String, Object> item : items) {
            Long     productoId = Long.valueOf(item.get("productoId").toString());
            int      cantidad   = Integer.parseInt(item.get("cantidad").toString());
            Producto producto   = productoService.obtenerPorId(productoId);

            // 4. Descontar stock (invalida caché Redis)
            productoService.descontarStock(productoId, cantidad);

            ItemPedido ip = new ItemPedido();
            ip.setPedido(pedido);
            ip.setProducto(producto);
            ip.setCantidad(cantidad);
            ip.setPrecioUnitario(producto.getPrecio());
            itemsPedido.add(ip);

            total += producto.getPrecio() * cantidad;
        }

        pedido.setItems(itemsPedido);
        pedido.setTotal(total);
        Pedido pedidoGuardado = pedidoRepo.save(pedido);

        log.info("[OrderService] Pedido #{} confirmado para {}", pedidoGuardado.getId(), emailCliente);

        // 5. Notificación asíncrona por RabbitMQ — el checkout no espera
        notificationService.publicarPedidoConfirmado(
            pedidoGuardado.getId(),
            cliente.getEmail(),
            cliente.getNombre(),
            total
        );

        return pedidoGuardado;
    }

    public List<Pedido> obtenerPedidosCliente(String emailCliente) {
        Cliente cliente = clienteRepo.findByEmail(emailCliente)
            .orElseThrow(() -> new FitStoreException("Cliente no encontrado"));
        return pedidoRepo.findByClienteOrderByFechaDesc(cliente);
    }

    @Transactional
    public Pedido actualizarEstado(Long pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepo.findById(pedidoId)
            .orElseThrow(() -> new FitStoreException("Pedido no encontrado: " + pedidoId));
        pedido.setEstado(Pedido.Estado.valueOf(nuevoEstado.toUpperCase().replace(" ", "_")));
        return pedidoRepo.save(pedido);
    }
}
