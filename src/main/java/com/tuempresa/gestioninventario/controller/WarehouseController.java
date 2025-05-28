package com.tuempresa.gestioninventario.controller;

import com.tuempresa.gestioninventario.entity.Warehouse;
import com.tuempresa.gestioninventario.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/warehouses") // Ruta base para todos los endpoints de este controlador
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Autowired
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    // POST /api/warehouses - Crear un nuevo almacén
    @PostMapping
    public ResponseEntity<?> createWarehouse(@RequestBody Warehouse warehouse) {
        try {
            Warehouse createdWarehouse = warehouseService.createWarehouse(warehouse);
            return new ResponseEntity<>(createdWarehouse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Manejo simple de la excepción por nombre duplicado
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // 409 Conflict
                    .body(e.getMessage());
        }
    }

    // GET /api/warehouses - Obtener todos los almacenes
    @GetMapping
    public ResponseEntity<List<Warehouse>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        return new ResponseEntity<>(warehouses, HttpStatus.OK);
    }

    // GET /api/warehouses/{id} - Obtener un almacén por ID
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long id) {
        Optional<Warehouse> warehouse = warehouseService.getWarehouseById(id);
        return warehouse.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        // Alternativa más corta con orElseThrow (requiere manejo de excepciones global o local)
        // Warehouse warehouse = warehouseService.getWarehouseById(id)
        // .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Almacén no encontrado con id: " + id));
        // return ResponseEntity.ok(warehouse);
    }

    // PUT /api/warehouses/{id} - Actualizar un almacén existente
    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse warehouseDetails) {
        Optional<Warehouse> updatedWarehouse = warehouseService.updateWarehouse(id, warehouseDetails);
        return updatedWarehouse.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                               .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // DELETE /api/warehouses/{id} - Eliminar un almacén
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteWarehouse(@PathVariable Long id) {
        boolean deleted = warehouseService.deleteWarehouse(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content es común para delete exitoso
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}