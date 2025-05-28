package com.tuempresa.gestioninventario.repository;

import com.tuempresa.gestioninventario.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name); // [cite: 6, 14]
}