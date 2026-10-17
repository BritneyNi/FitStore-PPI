package com.fitstore.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
