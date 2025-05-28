package com.tuempresa.gestioninventario.service;

import com.tuempresa.gestioninventario.entity.Product;
import com.tuempresa.gestioninventario.entity.Warehouse;
import com.tuempresa.gestioninventario.repository.ProductRepository;
import com.tuempresa.gestioninventario.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList; // Para lista vacía de productos
import java.util.Arrays;
import java.util.Collections; // Para Collections.emptyList()
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository; // Necesario para la lógica de deleteWarehouse

    @InjectMocks
    private WarehouseService warehouseService;

    private Warehouse warehouse1;
    private Warehouse warehouse2;
    private Product productInWarehouse1;

    @BeforeEach
    void setUp() {
        warehouse1 = new Warehouse(1L, "Almacén Central", "Calle Principal 123", new ArrayList<>());
        warehouse2 = new Warehouse(2L, "Depósito Norte", "Av. Norte 456", new ArrayList<>());

        // Producto que existe en warehouse1 para probar la lógica de eliminación
        productInWarehouse1 = new Product(1L, "Laptop", "Desc Laptop", new BigDecimal("1200"), 5, "Electrónicos", warehouse1);
        // No es necesario añadirlo explícitamente a warehouse1.products aquí,
        // el mock de productRepository.findByWarehouse se encargará de simularlo.
    }

    @Test
    void getAllWarehouses_shouldReturnListOfWarehouses() {
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));

        List<Warehouse> warehouses = warehouseService.getAllWarehouses();

        assertNotNull(warehouses);
        assertEquals(2, warehouses.size());
        verify(warehouseRepository, times(1)).findAll();
    }

    @Test
    void getWarehouseById_whenIdIsValidAndWarehouseExists_shouldReturnWarehouse() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));

        Optional<Warehouse> foundWarehouse = warehouseService.getWarehouseById(1L);

        assertTrue(foundWarehouse.isPresent());
        assertEquals(warehouse1.getName(), foundWarehouse.get().getName());
        verify(warehouseRepository, times(1)).findById(1L);
    }

    @Test
    void getWarehouseById_whenIdIsValidAndWarehouseDoesNotExist_shouldReturnEmptyOptional() {
        when(warehouseRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Warehouse> foundWarehouse = warehouseService.getWarehouseById(3L);

        assertFalse(foundWarehouse.isPresent());
        verify(warehouseRepository, times(1)).findById(3L);
    }

    @Test
    void getWarehouseById_whenIdIsInvalid_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.getWarehouseById(null));
        assertEquals("El ID del almacén debe ser un número positivo.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.getWarehouseById(0L));
        assertEquals("El ID del almacén debe ser un número positivo.", exception.getMessage());
    }

    @Test
    void createWarehouse_whenDataIsValid_shouldCreateAndReturnWarehouse() {
        Warehouse newWarehouseDetails = new Warehouse(null, "Nuevo Depósito", "Calle Nueva 789", null);
        when(warehouseRepository.findByName("Nuevo Depósito")).thenReturn(Optional.empty());
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse w = invocation.getArgument(0);
            w.setId(3L); // Simular asignación de ID
            return w;
        });

        Warehouse createdWarehouse = warehouseService.createWarehouse(newWarehouseDetails);

        assertNotNull(createdWarehouse);
        assertEquals("Nuevo Depósito", createdWarehouse.getName());
        assertNotNull(createdWarehouse.getId());
        verify(warehouseRepository, times(1)).findByName("Nuevo Depósito");
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_whenNameIsEmpty_shouldThrowException() {
        Warehouse newWarehouse = new Warehouse(null, "", "Dirección", null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.createWarehouse(newWarehouse));
        assertEquals("El nombre del almacén es obligatorio.", exception.getMessage());
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_whenNameIsNull_shouldThrowException() {
        Warehouse newWarehouse = new Warehouse(null, null, "Dirección", null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.createWarehouse(newWarehouse));
        assertEquals("El nombre del almacén es obligatorio.", exception.getMessage());
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void createWarehouse_whenNameAlreadyExists_shouldThrowException() {
        Warehouse newWarehouseDetails = new Warehouse(null, "Almacén Central", "Calle Nueva 789", null);
        when(warehouseRepository.findByName("Almacén Central")).thenReturn(Optional.of(warehouse1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.createWarehouse(newWarehouseDetails));
        assertEquals("Ya existe un almacén con el nombre: Almacén Central", exception.getMessage());
        verify(warehouseRepository, times(1)).findByName("Almacén Central");
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void updateWarehouse_whenDataIsValid_shouldUpdateAndReturnWarehouse() {
        Warehouse detailsToUpdate = new Warehouse();
        detailsToUpdate.setName("Almacén Central V2");
        detailsToUpdate.setLocationDetails("Calle Principal 123, Actualizado");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1)); // warehouse1 es el existente
        when(warehouseRepository.findByName("Almacén Central V2")).thenReturn(Optional.empty()); // Nuevo nombre no existe
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Warehouse> updatedWarehouseOpt = warehouseService.updateWarehouse(1L, detailsToUpdate);

        assertTrue(updatedWarehouseOpt.isPresent());
        Warehouse updatedWarehouse = updatedWarehouseOpt.get();
        assertEquals("Almacén Central V2", updatedWarehouse.getName());
        assertEquals("Calle Principal 123, Actualizado", updatedWarehouse.getLocationDetails());
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findByName("Almacén Central V2");
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }
    
    @Test
    void updateWarehouse_whenNameIsNotChanged_shouldUpdateAndReturnWarehouse() {
        Warehouse detailsToUpdate = new Warehouse();
        detailsToUpdate.setName(warehouse1.getName()); // Mismo nombre que el existente
        detailsToUpdate.setLocationDetails("Nueva Ubicación Detallada");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));
        // No se debería llamar a findByName si el nombre no cambia
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse1);


        Optional<Warehouse> updatedWarehouseOpt = warehouseService.updateWarehouse(1L, detailsToUpdate);

        assertTrue(updatedWarehouseOpt.isPresent());
        assertEquals(warehouse1.getName(), updatedWarehouseOpt.get().getName());
        assertEquals("Nueva Ubicación Detallada", updatedWarehouseOpt.get().getLocationDetails());
        verify(warehouseRepository, times(1)).findById(1L);
        verify(warehouseRepository, never()).findByName(anyString()); // Verifica que findByName no se llamó
        verify(warehouseRepository, times(1)).save(warehouse1);
    }


    @Test
    void updateWarehouse_whenWarehouseNotFound_shouldReturnEmptyOptional() {
        Warehouse detailsToUpdate = new Warehouse(null, "No Importa", "Dir", null);
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Warehouse> updatedWarehouseOpt = warehouseService.updateWarehouse(99L, detailsToUpdate);

        assertFalse(updatedWarehouseOpt.isPresent());
        verify(warehouseRepository, times(1)).findById(99L);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void updateWarehouse_whenNewNameIsEmpty_shouldThrowException() {
        Warehouse detailsToUpdate = new Warehouse();
        detailsToUpdate.setName(""); // Nombre vacío
        detailsToUpdate.setLocationDetails("Alguna dirección");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.updateWarehouse(1L, detailsToUpdate));
        assertEquals("El nombre del almacén es obligatorio.", exception.getMessage());
        verify(warehouseRepository, times(1)).findById(1L)); // Se busca el almacén antes de validar datos
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void updateWarehouse_whenNewNameAlreadyExistsForAnotherWarehouse_shouldThrowException() {
        Warehouse detailsToUpdate = new Warehouse();
        detailsToUpdate.setName("Depósito Norte"); // Nombre de warehouse2
        detailsToUpdate.setLocationDetails("Alguna dirección");

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));
        when(warehouseRepository.findByName("Depósito Norte")).thenReturn(Optional.of(warehouse2)); // El nombre ya existe y es de otro almacén

        Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.updateWarehouse(1L, detailsToUpdate));
        assertEquals("Ya existe otro almacén con el nombre: Depósito Norte", exception.getMessage());
        verify(warehouseRepository, times(1)).findById(1L));
        verify(warehouseRepository, times(1)).findByName("Depósito Norte");
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void deleteWarehouse_whenWarehouseExistsAndHasNoProducts_shouldReturnTrue() {
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse2)); // warehouse2 no tiene productos asociados en este test
        when(productRepository.findByWarehouse(warehouse2)).thenReturn(Collections.emptyList()); // No hay productos
        doNothing().when(warehouseRepository).deleteById(2L);

        boolean deleted = warehouseService.deleteWarehouse(2L);

        assertTrue(deleted);
        verify(warehouseRepository, times(1)).findById(2L);
        verify(productRepository, times(1)).findByWarehouse(warehouse2);
        verify(warehouseRepository, times(1)).deleteById(2L);
    }

    @Test
    void deleteWarehouse_whenWarehouseDoesNotExist_shouldThrowException() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.deleteWarehouse(99L));
        assertEquals("Almacén no encontrado con ID: 99 para eliminar.", exception.getMessage());
        verify(warehouseRepository, times(1)).findById(99L);
        verify(productRepository, never()).findByWarehouse(any(Warehouse.class));
        verify(warehouseRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteWarehouse_whenWarehouseHasProducts_shouldThrowIllegalStateException() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));
        // Simular que warehouse1 tiene un producto
        when(productRepository.findByWarehouse(warehouse1)).thenReturn(Arrays.asList(productInWarehouse1));

        Exception exception = assertThrows(IllegalStateException.class, () -> warehouseService.deleteWarehouse(1L));
        assertEquals("No se puede eliminar el almacén 'Almacén Central' porque tiene 1 producto(s) asociado(s).", exception.getMessage());
        verify(warehouseRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findByWarehouse(warehouse1);
        verify(warehouseRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteWarehouse_whenIdIsInvalid_shouldThrowIllegalArgumentException() {
         Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.deleteWarehouse(null));
        assertEquals("El ID del almacén a eliminar debe ser un número positivo.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.deleteWarehouse(0L));
        assertEquals("El ID del almacén a eliminar debe ser un número positivo.", exception.getMessage());
    }


    @Test
    void getWarehouseByName_whenNameIsValidAndWarehouseExists_shouldReturnWarehouse() {
        when(warehouseRepository.findByName("Almacén Central")).thenReturn(Optional.of(warehouse1));

        Optional<Warehouse> foundWarehouse = warehouseService.getWarehouseByName("Almacén Central");

        assertTrue(foundWarehouse.isPresent());
        assertEquals(warehouse1.getName(), foundWarehouse.get().getName());
        verify(warehouseRepository, times(1)).findByName("Almacén Central");
    }

    @Test
    void getWarehouseByName_whenNameIsValidAndWarehouseDoesNotExist_shouldReturnEmpty() {
        when(warehouseRepository.findByName("Nombre Inexistente")).thenReturn(Optional.empty());

        Optional<Warehouse> foundWarehouse = warehouseService.getWarehouseByName("Nombre Inexistente");

        assertFalse(foundWarehouse.isPresent());
        verify(warehouseRepository, times(1)).findByName("Nombre Inexistente");
    }
    
    @Test
    void getWarehouseByName_whenNameIsInvalid_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.getWarehouseByName(null));
        assertEquals("El nombre del almacén para buscar no puede estar vacío.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> warehouseService.getWarehouseByName("  ")); // Espacios en blanco
        assertEquals("El nombre del almacén para buscar no puede estar vacío.", exception.getMessage());
    }
}