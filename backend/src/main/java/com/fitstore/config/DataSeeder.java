package com.fitstore.config;

import com.fitstore.entity.Cliente;
import com.fitstore.entity.Producto;
import com.fitstore.repository.ClienteRepository;
import com.fitstore.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ClienteRepository  clienteRepo;
    private final ProductoRepository productoRepo;
    private final PasswordEncoder    passwordEncoder;

    @Override
    public void run(String... args) {
        if (clienteRepo.count() == 0) seedClientes();
        if (productoRepo.count() == 0) seedProductos();
    }

    private void seedClientes() {
        clienteRepo.saveAll(List.of(
            buildCliente("Juan Pérez",     "juan@email.com",        "clave123",  Cliente.Rol.CLIENTE),
            buildCliente("Administrador",  "admin@fitstore.com",    "admin123",  Cliente.Rol.ADMIN)
        ));
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
            buildProducto("Proteína Whey 1kg",          "Proteína de suero 24g por porción.",              89900.0, 15, Producto.Categoria.SUPLEMENTO, "🥛"),
            buildProducto("Creatina Monohidratada 300g", "Aumenta fuerza y rendimiento muscular.",          54900.0, 20, Producto.Categoria.SUPLEMENTO, "💊"),
            buildProducto("Pre-Entreno X-Force",         "Máxima energía para entrenamientos intensos.",    69900.0,  8, Producto.Categoria.SUPLEMENTO, "⚡"),
            buildProducto("BCAA Aminoácidos 250g",       "Recuperación muscular post-entrenamiento.",       64900.0,  0, Producto.Categoria.SUPLEMENTO, "🔬"),
            buildProducto("Mancuernas Ajustables 20kg",  "Set ajustable de 2kg a 20kg. Acero recubierto.", 349900.0, 5, Producto.Categoria.EQUIPO,     "🏋️"),
            buildProducto("Colchoneta Yoga Pro",         "Antideslizante 6mm. Ideal para yoga y pilates.",  79900.0, 12, Producto.Categoria.EQUIPO,     "🧘"),
            buildProducto("Cuerda para Saltar",          "Velocidad con rodamientos. Ajustable 3 metros.",  29900.0, 25, Producto.Categoria.EQUIPO,     "🪢"),
            buildProducto("Guantes de Gym",              "Palma acolchada. Protección en levantamiento.",   34900.0, 22, Producto.Categoria.EQUIPO,     "🧤"),
            buildProducto("Banda Elástica Set x3",       "Resistencia ligera, media y fuerte.",             39900.0, 17, Producto.Categoria.EQUIPO,     "🎽"),
            buildProducto("Camiseta Dry-Fit",            "Tela transpirable. Elimina la humedad.",          45900.0, 30, Producto.Categoria.ROPA,       "👕"),
            buildProducto("Licra Deportiva",             "Compresión con tejido elástico.",                 59900.0, 18, Producto.Categoria.ROPA,       "🩱"),
            buildProducto("Tenis Deportivos",            "Suela antideslizante y soporte de tobillo.",     189900.0, 10, Producto.Categoria.ROPA,       "👟")
        ));
        log.info("[DataSeeder] Productos creados ✓");
    }

    private Producto buildProducto(String nombre, String desc, Double precio,
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
