package com.fitstore.service;

import com.fitstore.entity.Producto;
import com.fitstore.exception.FitStoreException;
import com.fitstore.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_KEY   = "stock:";
    private static final long   STOCK_TTL   = 300; // 5 minutos (RNF-01)

    // ── Consultas ────────────────────────────────────────────

    public List<Producto> listarTodos() {
        return productoRepo.findAll();
    }

    public List<Producto> listarPorCategoria(String categoria) {
        Producto.Categoria cat = Producto.Categoria.valueOf(categoria.toUpperCase());
        return productoRepo.findByCategoria(cat);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepo.findByNombreContainingIgnoreCase(nombre);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepo.findById(id)
            .orElseThrow(() -> new FitStoreException("Producto no encontrado: " + id));
    }

    // ── Cache-Aside — RNF-01: stock < 1 segundo ─────────────
    //
    //  1. Busca en Redis (~15ms)
    //  2. Si no hay, va a MySQL y guarda en Redis con TTL 5 min
    //  3. Retorna si hay suficiente stock
    //
    public boolean verificarStock(Long idProducto, int cantidad) {
        String key = STOCK_KEY + idProducto;

        // 1. Redis primero
        Object cached = redisTemplate.opsForValue().get(key);
        Integer stockActual;

        if (cached != null) {
            stockActual = Integer.parseInt(cached.toString());
            log.info("[Cache HIT] stock:{} = {}", idProducto, stockActual);
        } else {
            // 2. MySQL fallback
            Producto p = obtenerPorId(idProducto);
            stockActual = p.getStock();

            // 3. Guardar en Redis
            redisTemplate.opsForValue().set(key, stockActual, STOCK_TTL, TimeUnit.SECONDS);
            log.info("[Cache MISS] stock:{} = {} → guardado en Redis", idProducto, stockActual);
        }

        return stockActual >= cantidad;
    }

    @Transactional
    public void descontarStock(Long idProducto, int cantidad) {
        Producto p = obtenerPorId(idProducto);
        if (p.getStock() < cantidad) {
            throw new FitStoreException("Stock insuficiente para: " + p.getNombre());
        }
        p.setStock(p.getStock() - cantidad);
        productoRepo.save(p);

        // Invalidar caché para que el próximo read vaya a MySQL
        redisTemplate.delete(STOCK_KEY + idProducto);
        log.info("[Cache INVALIDADO] stock:{}", idProducto);
    }

    // ── Admin ────────────────────────────────────────────────

    @Transactional
    public Producto actualizarStock(Long id, int nuevoStock) {
        Producto p = obtenerPorId(id);
        p.setStock(nuevoStock);
        productoRepo.save(p);
        redisTemplate.delete(STOCK_KEY + id);
        return p;
    }

    @Transactional
    public Producto guardar(Producto producto) {
        return productoRepo.save(producto);
    }

    public List<Producto> productosConStockBajo() {
        return productoRepo.findByStockLessThanEqual(5);
    }
}
