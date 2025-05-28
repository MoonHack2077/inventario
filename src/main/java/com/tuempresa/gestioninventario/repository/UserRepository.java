package com.tuempresa.gestioninventario.repository;

import com.tuempresa.gestioninventario.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // [cite: 6]
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}