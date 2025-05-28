package com.tuempresa.gestioninventario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal; // Usaremos BigDecimal para el precio para mayor precisión

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Nombre del producto

    private String description; // Descripción

    @Column(nullable = false, precision = 10, scale = 2) // precision: total de dígitos, scale: dígitos después del punto decimal
    private BigDecimal price; // Precio del producto

    @Column(nullable = false)
    private Integer quantity; // Cantidad en stock

    private String category; // Categoría del producto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse; // Almacén al que pertenece el producto

    // Constructor simplificado (opcional, Lombok ya provee @AllArgsConstructor)
    public Product(String name, String description, BigDecimal price, Integer quantity, String category, Warehouse warehouse) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.warehouse = warehouse;
    }
}