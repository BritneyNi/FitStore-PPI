package com.fitstore;

import com.fitstore.entity.Producto;
import com.fitstore.exception.FitStoreException;
import com.fitstore.repository.ProductoRepository;
import com.fitstore.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepo;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Proteína Whey 1kg");
        producto.setPrecio(89900.0);
        producto.setStock(15);
        producto.setCategoria(Producto.Categoria.SUPLEMENTO);
    }

    // ── Scenario: Compra exitosa (Gherkin RF-03) ─────────────
    // Given el cliente está autenticado
    // And "Proteína Whey 1kg" tiene stock
    // When verifica disponibilidad
    // Then el sistema retorna disponible = true

    @Test
    void dadoStockEnRedis_cuandoVerificaStock_entoncesRetornaDisponible() {
        // Given: Redis tiene el stock en caché
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1")).thenReturn(15);

        // When
        boolean disponible = productoService.verificarStock(1L, 5);

        // Then
        assertTrue(disponible);
        verify(productoRepo, never()).findById(any()); // No va a MySQL
    }

    // ── Scenario: Cache Miss — va a MySQL (RNF-01) ───────────
    // Given Redis no tiene el dato
    // When verifica stock
    // Then va a MySQL y guarda en Redis

    @Test
    void dadoCacheMiss_cuandoVerificaStock_entoncesVaAMySQL() {
        // Given: Redis no tiene el dato
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1")).thenReturn(null);
        when(productoRepo.findById(1L)).thenReturn(Optional.of(producto));

        // When
        boolean disponible = productoService.verificarStock(1L, 5);

        // Then
        assertTrue(disponible);
        verify(productoRepo, times(1)).findById(1L); // Sí va a MySQL
    }

    // ── Scenario: Stock insuficiente ─────────────────────────
    // Given el producto tiene stock = 2
    // When el cliente pide cantidad = 5
    // Then retorna disponible = false

    @Test
    void dadoStockInsuficiente_cuandoVerificaStock_entoncesRetornaNoDisponible() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("stock:1"))