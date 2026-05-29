package com.fitstore.repository;

import com.fitstore.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoria(Producto.Categoria categoria);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByStockLessThanEqual(int umbral);
}
