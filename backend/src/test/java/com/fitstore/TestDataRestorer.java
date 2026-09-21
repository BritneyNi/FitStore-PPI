package com.fitstore;

import com.fitstore.entity.Producto;
import com.fitstore.repository.ItemPedidoRepository;
import com.fitstore.repository.PedidoRepository;
import com.fitstore.repository.ProductoRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestDataRestorer {

    private final ItemPedidoRepository itemPedidoRepo;
    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;
    private final JdbcTemplate jdbc;
    private final RedisTemplate<String, Object> redisTemplate;

    public TestDataRestorer(ItemPedidoRepository itemPedidoRepo,
                            PedidoRepository pedidoRepo,
                            ProductoRepository productoRepo,
                            JdbcTemplate jdbc,
                            RedisTemplate<String, Object> redisTemplate) {
        this.itemPedidoRepo = itemPedidoRepo;
        this.pedidoRepo = pedidoRepo;
        this.productoRepo = productoRepo;
        this.jdbc = jdbc;
        this.redisTemplate = redisTemplate;
    }

    @PreDestroy
    public void restaurarCatalogoOriginal() {
        itemPedidoRepo.deleteAll();
        pedidoRepo.deleteAll();
        productoRepo.deleteAll();
        jdbc.update("DBCC CHECKIDENT ('items_pedido', RESEED, 0)");
        jdbc.update("DBCC CHECKIDENT ('pedidos', RESEED, 0)");
        jdbc.update("DBCC CHECKIDENT ('productos', RESEED, 0)");
        productoRepo.saveAll(productosOriginales());
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    private List<Producto> productosOriginales() {
        return List.of(
            build("Proteína Whey 1kg",           "Proteína de suero 24g por porción.",              89900.0, 15, Producto.Categoria.SUPLEMENTO, "🥛"),
            build("Creatina Monohidratada 300g", "Aumenta fuerza y rendimiento muscular.",          54900.0, 20, Producto.Categoria.SUPLEMENTO, "💊"),
            build("Pre-Entreno X-Force",         "Máxima energía para entrenamientos intensos.",    69900.0,  8, Producto.Categoria.SUPLEMENTO, "⚡"),
            build("BCAA Aminoácidos 250g",       "Recuperación muscular post-entrenamiento.",       64900.0,  0, Producto.Categoria.SUPLEMENTO, "🔬"),
            build("Mancuernas Ajustables 20kg",  "Set ajustable de 2kg a 20kg. Acero recubierto.", 349900.0, 5, Producto.Categoria.EQUIPO,     "🏋️"),
            build("Colchoneta Yoga Pro",         "Antideslizante 6mm. Ideal para yoga y pilates.",  79900.0, 12, Producto.Categoria.EQUIPO,     "🧘"),
            build("Cuerda para Saltar",          "Velocidad con rodamientos. Ajustable 3 metros.",  29900.0, 25, Producto.Categoria.EQUIPO,     "🪢"),
            build("Guantes de Gym",              "Palma acolchada. Protección en levantamiento.",   34900.0, 22, Producto.Categoria.EQUIPO,     "🧤"),
            build("Banda Elástica Set x3",       "Resistencia ligera, media y fuerte.",             39900.0, 17, Producto.Categoria.EQUIPO,     "🎽"),
            build("Camiseta Dry-Fit",            "Tela transpirable. Elimina la humedad.",          45900.0, 30, Producto.Categoria.ROPA,       "👕"),
            build("Licra Deportiva",             "Compresión con tejido elástico.",                 59900.0, 18, Producto.Categoria.ROPA,       "🩱"),
            build("Tenis Deportivos",            "Suela antideslizante y soporte de tobillo.",     189900.0, 10, Producto.Categoria.ROPA,       "👟")
        );
    }

    private Producto build(String nombre, String desc, Double precio,
                           int stock, Producto.Categoria cat, String emoji) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(desc);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setCategoria(cat);
        p.setEmoji(emoji);
        return p;
    }
}