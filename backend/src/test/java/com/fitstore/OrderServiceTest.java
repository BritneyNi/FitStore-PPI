package com.fitstore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fitstore.entity.Cliente;
import com.fitstore.entity.ItemPedido;
import com.fitstore.entity.Pedido;
import com.fitstore.entity.Producto;
import com.fitstore.exception.FitStoreException;
import com.fitstore.repository.ClienteRepository;
import com.fitstore.repository.PedidoRepository;
import com.fitstore.service.NotificationService;
import com.fitstore.service.OrderService;
import com.fitstore.service.ProductoService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PedidoRepository pedidoRepo;

    @Mock
    private ClienteRepository clienteRepo;

    @Mock
    private ProductoService productoService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@fitstore.com");

        Producto producto = new Producto();
        producto.setId(7L);

        ItemPedido item = new ItemPedido();
        item.setProducto(producto);
        item.setCantidad(2);

        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCliente(cliente);
        pedido.setEstado(Pedido.Estado.CONFIRMADO);
        pedido.setItems(List.of(item));
    }

    @Test
    void cancelarPedidoConfirmadoDevuelveElStock() {
        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepo.save(pedido)).thenReturn(pedido);

        Pedido resultado = orderService.cancelarPedidoCliente(1L, "cliente@fitstore.com");

        assertEquals(Pedido.Estado.CANCELADO, resultado.getEstado());
        verify(productoService).restaurarStock(7L, 2);
    }

    @Test
    void noPermiteCancelarPedidoDeOtroCliente() {
        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(FitStoreException.class, () -> orderService.cancelarPedidoCliente(1L, "otro@fitstore.com"));
        verify(productoService, never()).restaurarStock(anyLong(), anyInt());
    }

    @Test
    void noPermiteCancelarPedidoEnCamino() {
        pedido.setEstado(Pedido.Estado.EN_CAMINO);
        when(pedidoRepo.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(FitStoreException.class, () -> orderService.cancelarPedidoCliente(1L, "cliente@fitstore.com"));
        verify(pedidoRepo, never()).save(any());
    }
}
