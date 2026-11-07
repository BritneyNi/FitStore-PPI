package com.fitstore.config;

import com.fitstore.entity.Cliente;
import com.fitstore.entity.Producto;
import com.fitstore.repository.ClienteRepository;
import com.fitstore.repository.ProductoRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ClienteRepository clienteRepo;
    private final ProductoRepository productoRepo;
    private final PasswordEncoder passwordEncoder;

    // Imágenes de los productos de demostración. La aplicación siempre lee la
    // URL desde la columna `imagen_url`; este mapa solo se usa para poblar la
    // base de datos la primera vez y para completar registros antiguos.
    private static final Map<String, String> IMAGENES_DEMO = Map.ofEntries(
            Map.entry("Proteína Whey 1kg", "img/suplementos/proteina.jpg"),
            Map.entry("Creatina Monohidratada 300g", "img/suplementos/creatina.jpg"),
            Map.entry("Pre-Entreno X-Force", "img/suplementos/pre-entreno.jpg"),
            Map.entry("BCAA Aminoácidos 250g", "img/suplementos/aminoacidos.jpg"),
            Map.entry("Mancuernas Ajustables 20kg", "img/equipos/mancuernas.jpg"),
            Map.entry("Colchoneta Yoga Pro", "img/equipos/colchoneta.jpg"),
            Map.entry("Cuerda para Saltar", "img/equipos/cuerda.jpg"),
            Map.entry("Guantes de Gym", "img/equipos/guantes.jpg"),
            Map.entry("Banda Elástica Set x3", "img/equipos/bandas.jpg"),
            Map.entry("Camiseta Dry-Fit", "img/ropa/camiseta.jpg"),
            Map.entry("Licra Deportiva", "img/ropa/licra.jpg"),
            Map.entry("Tenis Deportivos", "img/ropa/tenis.jpg"));

    @Override
    public void run(String... args) {
        if (clienteRepo.count() == 0) seedClientes();
        if (productoRepo.count() == 0) seedProductos();
        completarImagenesFaltantes();
    }

    private void seedClientes() {
        clienteRepo.saveAll(List.of(
                buildCliente("Juan Pérez", "juan@email.com", "clave123", Cliente.Rol.CLIENTE),
                buildCliente("Administrador", "admin@fitstore.com", "admin123", Cliente.Rol.ADMIN)));
        log.info("[DataSeeder] Usuarios creados ✓");
    }

    private Cliente buildCliente(String nombre, String email, String clave, Cliente.Rol rol) {
        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setEmail(email);
        c.setClave(passwordEncoder.encode(clave));
        c.setRol(rol);
        return c;
    }

    private void seedProductos() {
        productoRepo.saveAll(List.of(
                buildProducto(
                        "Proteína Whey 1kg",
                        "Proteína de suero 24g por porción.",
                        89900.0,
                        15,
                        Producto.Categoria.SUPLEMENTO,
                        "🥛",
                        "img/suplementos/proteina.jpg"),
                buildProducto(
                        "Creatina Monohidratada 300g",
                        "Aumenta fuerza y rendimiento muscular.",
                        54900.0,
                        20,
                        Producto.Categoria.SUPLEMENTO,
                        "💊",
                        "img/suplementos/creatina.jpg"),
                buildProducto(
                        "Pre-Entreno X-Force",
                        "Máxima energía para entrenamientos intensos.",
                        69900.0,
                        8,
                        Producto.Categoria.SUPLEMENTO,
                        "⚡",
                        "img/suplementos/pre-entreno.jpg"),
                buildProducto(
                        "BCAA Aminoácidos 250g",
                        "Recuperación muscular post-entrenamiento.",
                        64900.0,
                        0,
                        Producto.Categoria.SUPLEMENTO,
                        "🔬",
                        "img/suplementos/aminoacidos.jpg"),
                buildProducto(
                        "Mancuernas Ajustables 20kg",
                        "Set ajustable de 2kg a 20kg. Acero recubierto.",
                        349900.0,
                        5,
                        Producto.Categoria.EQUIPO,
                        "🏋️",
                        "img/equipos/mancuernas.jpg"),
                buildProducto(
                        "Colchoneta Yoga Pro",
                        "Antideslizante 6mm. Ideal para yoga y pilates.",
                        79900.0,
                        12,
                        Producto.Categoria.EQUIPO,
                        "🧘",
                        "img/equipos/colchoneta.jpg"),
                buildProducto(
                        "Cuerda para Saltar",
                        "Velocidad con rodamientos. Ajustable 3 metros.",
                        29900.0,
                        25,
                        Producto.Categoria.EQUIPO,
                        "🪢",
                        "img/equipos/cuerda.jpg"),
                buildProducto(
                        "Guantes de Gym",
                        "Palma acolchada. Protección en levantamiento.",
                        34900.0,
                        22,
                        Producto.Categoria.EQUIPO,
                        "🧤",
                        "img/equipos/guantes.jpg"),
                buildProducto(
                        "Banda Elástica Set x3",
                        "Resistencia ligera, media y fuerte.",
                        39900.0,
                        17,
                        Producto.Categoria.EQUIPO,
                        "🎽",
                        "img/equipos/bandas.jpg"),
                buildProducto(
                        "Camiseta Dry-Fit",
                        "Tela transpirable. Elimina la humedad.",
                        45900.0,
                        30,
                        Producto.Categoria.ROPA,
                        "👕",
                        "img/ropa/camiseta.jpg"),
                buildProducto(
                        "Licra Deportiva",
                        "Compresión con tejido elástico.",
                        59900.0,
                        18,
                        Producto.Categoria.ROPA,
                        "🩱",
                        "img/ropa/licra.jpg"),
                buildProducto(
                        "Tenis Deportivos",
                        "Suela antideslizante y soporte de tobillo.",
                        189900.0,
                        10,
                        Producto.Categoria.ROPA,
                        "👟",
                        "img/ropa/tenis.jpg")));
        log.info("[DataSeeder] Productos creados ✓");
    }

    /**
     * Completa la URL de los productos de demostración que ya existían antes de
     * agregar la columna {@code imagen_url}. Es una migración de datos única: si
     * el producto tiene URL, no se toca.
     */
    private void completarImagenesFaltantes() {
        int actualizados = 0;
        for (Producto producto : productoRepo.findAll()) {
            if (producto.getImagenUrl() == null || producto.getImagenUrl().isBlank()) {
                String imagen = IMAGENES_DEMO.get(producto.getNombre());
                if (imagen != null) {
                    producto.setImagenUrl(imagen);
                    productoRepo.save(producto);
                    actualizados++;
                }
            }
        }
        if (actualizados > 0) {
            log.info("[DataSeeder] Imágenes completadas en {} producto(s)", actualizados);
        }
    }

    private Producto buildProducto(
            String nombre,
            String desc,
            Double precio,
            int stock,
            Producto.Categoria cat,
            String emoji,
            String imagenUrl) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(desc);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setCategoria(cat);
        p.setEmoji(emoji);
        p.setImagenUrl(imagenUrl);
        return p;
    }
}
