package com.fitstore.repository;

import com.fitstore.entity.Pedido;
import com.fitstore.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteOrderByFechaDesc(Cliente cliente);
}
