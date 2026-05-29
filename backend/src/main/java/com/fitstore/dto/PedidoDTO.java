package com.fitstore.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoDTO {
    private Long id;
    private String estado;
    private Double total;
    private LocalDateTime fecha;
    private List<ItemPedidoDTO> items;
}