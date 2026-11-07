package com.fitstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    private String emoji;

    // URL de la imagen del producto. Se guarda en la base de datos para que
    // la interfaz no tenga que adivinar la imagen a partir del nombre.
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    public enum Categoria {
        SUPLEMENTO,
        EQUIPO,
        ROPA
    }
}
