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

import java.math.BigDecimal; // Para el precio
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private Warehouse warehouse1;

    @BeforeEach
    void setUp() {
        warehouse1 = new Warehouse(1L, "Almacén Principal", "Calle Central 123", null);

        // Productos simplificados
        product1 = new Product(1L, "Laptop Gamer", "Potente laptop para juegos",
                            new BigDecimal("1200.99"), 10, "Electrónicos", warehouse1);

        product2 = new Product(2L, "Mouse Inalámbrico", "Mouse ergonómico",
                            new BigDecimal("25.50"), 50, "Periféricos", warehouse1);
    }

    @Test
    void getAllProducts_shouldReturnListOfProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<Product> products = productService.getAllProducts();

        assertNotNull(products);
        assertEquals(2, products.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_whenIdIsValidAndProductExists_shouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        Optional<Product> foundProduct = productService.getProductById(1L);

        assertTrue(foundProduct.isPresent());
        assertEquals(product1.getName(), foundProduct.get().getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_whenIdIsValidAndProductDoesNotExist_shouldReturnEmptyOptional() {
        when(productRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Product> foundProduct = productService.getProductById(3L);

        assertFalse(foundProduct.isPresent());
        verify(productRepository, times(1)).findById(3L);
    }

    @Test
    void getProductById_whenIdIsInvalid_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.getProductById(null));
        assertEquals("El ID del producto debe ser un número positivo.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> productService.getProductById(0L));
        assertEquals("El ID del producto debe ser un número positivo.", exception.getMessage());
    }

    @Test
    void createProduct_whenDataIsValid_shouldCreateAndReturnProduct() {
        Product newProductDetails = new Product("Teclado RGB", "Mecánico con luces",
                                        new BigDecimal("75.00"), 20, "Periféricos", null); // Warehouse se asignará

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));
        // No más mocks para findBySku o findByBarcode
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(3L); // Simular asignación de ID al guardar
            p.setWarehouse(warehouse1); // Simular que el servicio asigna el almacén
            return p;
        });

        Product createdProduct = productService.createProduct(newProductDetails, 1L);

        assertNotNull(createdProduct);
        assertEquals("Teclado RGB", createdProduct.getName());
        assertEquals(new BigDecimal("75.00"), createdProduct.getPrice());
        assertEquals(20, createdProduct.getQuantity());
        assertEquals(warehouse1, createdProduct.getWarehouse());
        assertNotNull(createdProduct.getId());
        // Ya no verificamos entryDate
        verify(warehouseRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_whenProductNameIsEmpty_shouldThrowException() {
        Product newProduct = new Product("", "Descripción", new BigDecimal("10.00"), 5, "Categoría", null);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1)); // Necesario para que no falle antes

        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.createProduct(newProduct, 1L));
        assertEquals("El nombre del producto es obligatorio.", exception.getMessage());
    }

    @Test
    void createProduct_whenQuantityIsNegative_shouldThrowException() {
        Product newProduct = new Product("Producto Test", "Descripción", new BigDecimal("10.00"), -5, "Categoría", null);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.createProduct(newProduct, 1L));
        assertEquals("La cantidad no puede ser nula o negativa.", exception.getMessage());
    }

    @Test
    void createProduct_whenPriceIsNegative_shouldThrowException() {
        Product newProduct = new Product("Producto Test", "Descripción", new BigDecimal("-10.00"), 5, "Categoría", null);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.createProduct(newProduct, 1L));
        assertEquals("El precio no puede ser nulo o negativo.", exception.getMessage());
    }

    @Test
    void createProduct_whenWarehouseNotFound_shouldThrowException() {
        Product newProduct = new Product("Producto Test", "Descripción", new BigDecimal("10.00"), 5, "Categoría", null);
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.createProduct(newProduct, 99L));
        assertEquals("Almacén no encontrado con ID: 99", exception.getMessage());
    }

    // No más tests para SKU/Barcode duplicados en creación

    @Test
    void updateProduct_whenDataIsValid_shouldUpdateAndReturnProduct() {
        Product detailsToUpdate = new Product();
        detailsToUpdate.setName("Laptop Gamer V2");
        detailsToUpdate.setDescription("Versión actualizada");
        detailsToUpdate.setPrice(new BigDecimal("1250.00"));
        detailsToUpdate.setQuantity(8);
        detailsToUpdate.setCategory("Electrónicos Pro");

        Warehouse newWarehouse = new Warehouse(2L, "Almacén Express", "Calle Secundaria 789", null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1)); // product1 es el existente
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(newWarehouse));
        // No más mocks para findBySku/Barcode
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Optional<Product> updatedProductOpt = productService.updateProduct(1L, detailsToUpdate, 2L);

        assertTrue(updatedProductOpt.isPresent());
        Product updatedProduct = updatedProductOpt.get();
        assertEquals("Laptop Gamer V2", updatedProduct.getName());
        assertEquals(8, updatedProduct.getQuantity());
        assertEquals(new BigDecimal("1250.00"), updatedProduct.getPrice());
        assertEquals(newWarehouse, updatedProduct.getWarehouse());

        verify(productRepository, times(1)).findById(1L);
        verify(warehouseRepository, times(1)).findById(2L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_whenProductNotFound_shouldReturnEmptyOptional() {
        Product detailsToUpdate = new Product("No Importa", "Desc", new BigDecimal("1.00"), 1, "Cat", null);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> updatedProductOpt = productService.updateProduct(99L, detailsToUpdate, 1L);

        assertFalse(updatedProductOpt.isPresent());
        verify(productRepository, times(1)).findById(99L);
        verify(warehouseRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    // No más tests para SKU/Barcode duplicados en actualización

    @Test
    void deleteProduct_whenProductExists_shouldReturnTrue() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        boolean deleted = productService.deleteProduct(1L);

        assertTrue(deleted);
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_whenProductDoesNotExist_shouldReturnFalse() {
        when(productRepository.existsById(99L)).thenReturn(false);

        boolean deleted = productService.deleteProduct(99L);

        assertFalse(deleted);
        verify(productRepository, times(1)).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void recordEntry_whenDataIsValid_shouldIncreaseQuantityAndReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1); // Devuelve el producto modificado
        int initialQuantity = product1.getQuantity();
        int entryQuantity = 5;

        Optional<Product> updatedProductOpt = productService.recordEntry(1L, entryQuantity);

        assertTrue(updatedProductOpt.isPresent());
        assertEquals(initialQuantity + entryQuantity, updatedProductOpt.get().getQuantity());
        // Ya no verificamos entryDate
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product1);
    }

    @Test
    void recordEntry_whenQuantityIsNegativeOrZero_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.recordEntry(1L, 0));
        assertEquals("La cantidad para registrar entrada debe ser positiva.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> productService.recordEntry(1L, -5));
        assertEquals("La cantidad para registrar entrada debe ser positiva.", exception.getMessage());
    }

    @Test
    void recordExit_whenDataIsValidAndQuantitySufficient_shouldDecreaseQuantityAndReturnProduct() {
        // product1 tiene quantity = 10
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1); // Devuelve el producto modificado
        int initialQuantity = product1.getQuantity();
        int exitQuantity = 3;

        Optional<Product> updatedProductOpt = productService.recordExit(1L, exitQuantity);

        assertTrue(updatedProductOpt.isPresent());
        assertEquals(initialQuantity - exitQuantity, updatedProductOpt.get().getQuantity());
        // Ya no verificamos exitDate
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product1);
    }

    @Test
    void recordExit_whenQuantityIsInsufficient_shouldThrowException() {
        // product1 tiene quantity = 10
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        int exitQuantity = 15; // Intentar sacar más de lo que hay

        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.recordExit(1L, exitQuantity));
        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void recordExit_whenQuantityIsNegativeOrZero_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.recordExit(1L, 0));
        assertEquals("La cantidad para registrar salida debe ser positiva.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> productService.recordExit(1L, -5));
        assertEquals("La cantidad para registrar salida debe ser positiva.", exception.getMessage());
    }
}