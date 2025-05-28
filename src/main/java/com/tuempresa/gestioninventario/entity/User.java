package com.tuempresa.gestioninventario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set; // Para manejar una colección de roles sin duplicados

@Entity
@Table(name = "app_users") // Cambiado de "users" para evitar conflictos con palabras reservadas de SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Nombre de usuario para login [cite: 6]

    @Column(nullable = false)
    private String password; // Contraseña (se almacenará hasheada)

    private String email; // Email del usuario (para notificaciones, por ejemplo) [cite: 29]

    private boolean enabled = true; // Para activar/desactivar la cuenta del usuario

    // Relación Muchos a Muchos con Role
    // Un usuario puede tener múltiples roles, y un rol puede ser asignado a múltiples usuarios.
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_roles", // Nombre de la tabla intermedia
        joinColumns = @JoinColumn(name = "user_id"), // Columna que referencia a User
        inverseJoinColumns = @JoinColumn(name = "role_id") // Columna que referencia a Role
    )
    private Set<Role> roles; // Roles asignados al usuario [cite: 6, 9, 14]
}