package com.tuempresa.gestioninventario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Nombre del almacén

    private String locationDetails; // Detalles de la ubicación del almacén (e.g., dirección)

    // Un almacén puede tener muchos productos.
    // mappedBy="warehouse" indica que la entidad Product maneja la clave foránea.
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Product> products; // Lista de productos en este almacén [cite: 19, 40]
}