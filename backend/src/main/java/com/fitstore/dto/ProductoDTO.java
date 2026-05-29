package com.fitstore.dto;

import com.fitstore.entity.Producto;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private Producto.Categoria categoria;
    private String emoji;

    public static ProductoDTO from(Producto p) {
        return new ProductoDTO(
            p.getId(),
            p.getNombre(),
            p.getDescripcion(),
            p.getPrecio(),
            p.getStock(),
            p.getCategoria(),
            p.getEmoji()
        );
    }
}