package com.tuempresa.gestioninventario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Nombre del rol, ej: "ROLE_GERENTE_INVENTARIO", "ROLE_EMPLEADO_ALMACEN" [cite: 6, 14]
                         // También para roles personalizados como "supervisor de turno" [cite: 35]

    // Constructor útil
    public Role(String name) {
        this.name = name;
    }
}