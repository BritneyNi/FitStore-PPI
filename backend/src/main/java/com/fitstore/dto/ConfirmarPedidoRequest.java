package com.fitstore.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmarPedidoRequest {
    private List<ItemCarritoDTO> items;
}