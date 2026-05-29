package com.fitstore.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fitstore.entity.Producto;
import com.fitstore.entity.Pedido;
import java.time.LocalDateTime;
import java.util.List;

// ── Auth ─────────────────────────────────────────────────────

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginRequest {
    private String email;
    private String clave;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class RegisterRequest {
    private String nombre;
    private String email;
    private String clave;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class AuthResponse {
    private String token;
    private String nombre;
    private String email;
    private String rol;
}

// ── Producto ─────────────────────────────────────────────────

@Data
@AllArgsConstructor
@NoArgsConstructor
class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private Producto.Categoria categoria;
    private String emoji;

    public static ProductoDTO from(Producto p) {
        return new ProductoDTO(
            p.getId(), p.getNombre(), p.getDescripcion(),
            p.getPrecio(), p.getStock(), p.getCategoria(), p.getEmoji()
        );
    }
}

// ── Pedido ───────────────────────────────────────────────────

@Data
@AllArgsConstructor
@NoArgsConstructor
class ItemCarritoDTO {
    private Long productoId;
    private Integer cantidad;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ConfirmarPedidoRequest {
    private List<ItemCarritoDTO> items;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ItemPedidoDTO {
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class PedidoDTO {
    private Long id;
    private String estado;
    private Double total;
    private LocalDateTime fecha;
    private List<ItemPedidoDTO> items;
}

// ── Admin ────────────────────────────────────────────────────

@Data
@AllArgsConstructor
@NoArgsConstructor
class ActualizarStockRequest {
    private Integer stock;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ActualizarEstadoRequest {
    private String estado;
}
