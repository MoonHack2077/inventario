package com.tuempresa.gestioninventario.service;

import com.tuempresa.gestioninventario.entity.Warehouse;
import com.tuempresa.gestioninventario.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para operaciones de escritura

import java.util.List;
import java.util.Optional;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Autowired
    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true) // Es buena práctica marcar las transacciones de solo lectura
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Warehouse> getWarehouseById(Long id) {
        return warehouseRepository.findById(id);
    }

    @Transactional // Las operaciones de escritura no son readOnly
    public Warehouse createWarehouse(Warehouse warehouse) {
        // Aquí podrías añadir validaciones antes de guardar
        // Por ejemplo, verificar que el nombre no esté duplicado si es un requisito
        if (warehouseRepository.findByName(warehouse.getName()).isPresent()) {
            // Podrías lanzar una excepción personalizada aquí
            throw new IllegalArgumentException("Ya existe un almacén con el nombre: " + warehouse.getName());
        }
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Optional<Warehouse> updateWarehouse(Long id, Warehouse warehouseDetails) {
        return warehouseRepository.findById(id)
            .map(existingWarehouse -> {
                existingWarehouse.setName(warehouseDetails.getName());
                existingWarehouse.setLocationDetails(warehouseDetails.getLocationDetails());
                // Si tienes más campos, actualízalos aquí
                // existingWarehouse.setProducts(warehouseDetails.getProducts()); // Cuidado con manejar colecciones directamente
                return warehouseRepository.save(existingWarehouse);
            });
    }

    @Transactional
    public boolean deleteWarehouse(Long id) {
        if (warehouseRepository.existsById(id)) {
            // Considera la lógica de negocio aquí: ¿qué pasa con los productos en este almacén?
            // Por ahora, solo eliminamos el almacén.
            // Si hay productos asociados y la relación tiene CascadeType.ALL o similar,
            // se podrían eliminar en cascada, lo cual puede o no ser deseado.
            // Si la relación es orphanRemoval=true en Warehouse para la lista de productos
            // y los productos solo pueden pertenecer a UN almacén, se eliminarían.
            // Si los productos deben ser reasignados o la eliminación debe prevenirse si tiene productos,
            // necesitarás añadir esa lógica.
            // Para un CRUD simple, la eliminación directa es el primer paso.
            warehouseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Puedes añadir otros métodos si los necesitas, por ejemplo:
    @Transactional(readOnly = true)
    public Optional<Warehouse> getWarehouseByName(String name) {
        return warehouseRepository.findByName(name);
    }
}