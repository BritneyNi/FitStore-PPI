package com.fitstore.repository;

import com.fitstore.entity.Cliente;
import com.fitstore.entity.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteOrderByFechaDesc(Cliente cliente);
}
