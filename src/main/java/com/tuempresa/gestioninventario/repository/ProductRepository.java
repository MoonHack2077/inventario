package com.tuempresa.gestioninventario.repository;

import com.tuempresa.gestioninventario.entity.Product;
import com.tuempresa.gestioninventario.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByName(String name); // Sigue siendo útil
    List<Product> findByCategory(String category); // Sigue siendo útil
    List<Product> findByWarehouse(Warehouse warehouse); // Sigue siendo útil

    // Métodos como findBySku y findByBarcode ya no aplican y deben ser eliminados si existían.
}